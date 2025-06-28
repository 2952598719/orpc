package top.orosirian.server.register.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import top.orosirian.common.Utils;
import top.orosirian.server.register.ServiceRegister;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ZKServiceRegister implements ServiceRegister {

    private static final String ZK_ADDRESS = "localhost:2181";

    private static final String ROOT_PATH = "ORPC";

    private static final String host = "127.0.0.1";

    private static final int port = 9999;

    private final Map<String, Object> serviceMap;     // 服务接口名 -> 服务实例

    private final CuratorFramework zkClient;

    private static volatile ZKServiceRegister instance = null;

    private ZKServiceRegister() {
        this.serviceMap = new HashMap<>();
        this.zkClient = CuratorFrameworkFactory.builder()
                .connectString(ZK_ADDRESS)
                .sessionTimeoutMs(40000)    // Server和zk的连接session在40s没通信后关闭
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .namespace(ROOT_PATH)   // server和client指定相同的namespace，这样才能获取对应的服务地址
                .build();
        this.zkClient.start();
    }

    public static ZKServiceRegister getInstance() {
        if (instance == null) {
            synchronized (ZKServiceRegister.class) {
                if (instance == null) {
                    instance = new ZKServiceRegister();
                }
            }
        }
        return instance;
    }

    @Override
    public void register(Object serviceImpl) {
        Class<?>[] interfaceClasses = serviceImpl.getClass().getInterfaces();
        for (Class<?> interfaceClass : interfaceClasses) {
            localRegister(interfaceClass, serviceImpl);
            remoteRegister(interfaceClass, new InetSocketAddress(host, port));
            log.info("服务注册成功，服务名：{}，地址：{}", interfaceClass.getName(), Utils.addressToString(new InetSocketAddress(host, port)));
        }
    }

    @Override
    public Object getService(String interfaceName) {
        return serviceMap.get(interfaceName);
    }

    private void localRegister(Class<?> clazz, Object serviceImpl) {
        serviceMap.put(clazz.getName(), serviceImpl);
    }

    private void remoteRegister(Class<?> clazz, InetSocketAddress address) {
        String serviceName = clazz.getName();
        try {
            // zk理解成一个文件夹就好
            // 比如说先create一个/service，接着create一个/service/a，create一个/service/b，那么此时的结构就是service下有a和b两个节点
            // 不要理解成map的形式，不会相互覆盖
            // 这里的path就是相对于namespace的，可以理解成每个zkClient对应一个文件夹，无论如何都是在这个文件夹底下的路径
            String path = "/" + serviceName + "/" + Utils.addressToString(address);
            if(zkClient.checkExists().forPath(path) != null) {  // zk没有类似于python中的exist_ok=True，还是要判断一下，不然会报错
                log.warn("服务注册失败，服务名：{}，地址：{}，原因：节点已存在", serviceName, address);
                return;
            }
            zkClient.create()
                    // creatingParentsIfNeeded自动创建的父节点是持久的，creatingParentContainersIfNeeded在子节点全删除之后也会删除
                    .creatingParentsIfNeeded()         // 递归创建，比如路径是/service/a，如果没有service节点就会自动创建
                    .withMode(CreateMode.EPHEMERAL)             // 临时节点，服务器和zk的连接断开后就删除
                    .forPath(path);
        } catch (Exception e) {
            log.error("服务注册失败，服务名：{}，错误信息：{}", serviceName, e.getMessage(), e);
        }
    }

}
