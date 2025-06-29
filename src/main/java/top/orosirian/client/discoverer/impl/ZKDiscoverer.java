package top.orosirian.client.discoverer.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.DefaultACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import top.orosirian.client.cache.ServiceCache;
import top.orosirian.client.discoverer.Discoverer;
import top.orosirian.common.Utils;
import top.orosirian.server.register.impl.ZKServiceRegister;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZKDiscoverer implements Discoverer {

    private static final String ZK_PATH = "127.0.0.1:2181";

    private static final String ROOT_PATH = "ORPC";

    @Getter
    private CuratorFramework zkClient = null;    // zk客户端

    private ServiceCache serviceCache = null;

    private static volatile ZKDiscoverer instance = null;

    private ZKDiscoverer() {
        try {
            this.zkClient = CuratorFrameworkFactory.builder()
                    .connectString(ZK_PATH)     // zk的地址固定，作为生产者和消费者的预备知识
                    .namespace(ROOT_PATH)       // 命名空间为ORPC
                    .sessionTimeoutMs(40000)    // client在40s内需要发送一次心跳，否则判断断开
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3))  // client无法连接到zk服务器时，按照1s-2s-4s的时间来重新尝试连接
                    .build();
            this.zkClient.start();
            this.zkClient.blockUntilConnected();
            log.info("zookeeper连接成功");
            this.serviceCache = ServiceCache.getInstance();
        } catch (InterruptedException e) {
            log.info("zookeeper连接失败");
        }
    }

    public static ZKDiscoverer getInstance() {
        if (instance == null) {
            synchronized (ZKServiceRegister.class) {
                if (instance == null) {
                    instance = new ZKDiscoverer();
                }
            }
        }
        return instance;
    }

    @Override
    public InetSocketAddress discoverService(String serviceName) {
        try {
            List<String> addressList;
            if (serviceCache.containService(serviceName)) {
                log.warn("缓存中发现服务{}", serviceName);
                addressList = serviceCache.getServiceAddressList(serviceName);
            } else {
                log.warn("缓存中未发现服务{}，从远程获取", serviceName);
                addressList = zkClient.getChildren().forPath("/" + serviceName);
            }
            return Utils.stringToAddress(addressList.get(0));    // 第一步暂无负载均衡
        } catch(Exception e) {
            log.error("服务发现失败，服务名：{}", serviceName, e);
            return null;
        }
    }

    @Override
    public void stop() {
        zkClient.close();   // 这里zk就是设计得会报错，有点无语
    }

}
