package top.orosirian.client.discoverer.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import top.orosirian.client.cache.ServiceCache;
import top.orosirian.client.discoverer.ServiceDiscoverer;
import top.orosirian.common.Utils;
import top.orosirian.server.register.impl.ZKServiceRegister;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZKServiceDiscoverer implements ServiceDiscoverer {

    private static final String ZK_PATH = "127.0.0.1:2181";

    private static final String ROOT_PATH = "ORPC";

    @Getter
    private final CuratorFramework zkClient;    // zk客户端

    private final ServiceCache serviceCache;

    private static volatile ZKServiceDiscoverer instance = null;

    private ZKServiceDiscoverer() {
        this.zkClient = CuratorFrameworkFactory.builder()
                .connectString(ZK_PATH)     // zk的地址固定，作为生产者和消费者的预备知识
                .namespace(ROOT_PATH)       // 命名空间为MyRPC
                .sessionTimeoutMs(40000)           // client在40s内需要发送一次心跳，否则判断
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))  // client无法连接到zk服务器时，按照1s-2s-4s的时间来重新尝试连接
                .build();
        this.zkClient.start();
        this.serviceCache = ServiceCache.getInstance();
    }

    public static ZKServiceDiscoverer getInstance() {
        if (instance == null) {
            synchronized (ZKServiceRegister.class) {
                if (instance == null) {
                    instance = new ZKServiceDiscoverer();
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

}
