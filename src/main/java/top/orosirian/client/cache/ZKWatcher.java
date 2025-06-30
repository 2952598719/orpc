package top.orosirian.client.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import top.orosirian.client.discoverer.impl.ZKDiscoverer;

@Slf4j
public class ZKWatcher {

    private final CuratorFramework client;

    private final ServiceCache serviceCache;

    private static volatile ZKWatcher instance = null;

    private ZKWatcher() {
        client = ZKDiscoverer.getInstance().getZkClient();
        serviceCache = ServiceCache.getInstance();
        this.watchToUpdate();
    }

    public static ZKWatcher getInstance() {
        if (instance == null) {
            synchronized (ZKWatcher.class) {
                if (instance == null) {
                    instance = new ZKWatcher();
                }
            }
        }
        return instance;
    }

    public void watchToUpdate() {
        CuratorCache curatorCache = CuratorCache.build(client, "/");
        curatorCache.listenable().addListener((type, oldData, newData) -> {
            // 下面是start方法内容
            switch(type.name()) {
                // curatorCache.start时，会遍历目标路径下的所有节点，并触发NODE_CREATED事件
                case "NODE_CREATED":    // 得到创建节点的通知，直接放入缓存，此时oldData为空
                    String[] pathToCreate = parsePath(newData);
                    if(pathToCreate.length <= 2) {
                        // path结构为"/<interfaceName>/<serverAddr>"，split结果为["", "<interfaceName>", "<serverAddr>"]
                        // 所以需要长度大于3才表明这个正常，否则可能是递归创建父节点
                        break;
                    } else {
                        String serviceName = pathToCreate[1];
                        String serverAddress = pathToCreate[2];
                        serviceCache.addServiceAddress(serviceName, serverAddress);
                        log.info("节点创建：服务名称 {} 地址 {}", serviceName, serverAddress);
                    }
                    break;
                case "NODE_CHANGED":    // 得到节点更新的通知，删除旧的，放入新的
                    if (oldData.getData() != null) {
                        log.debug("修改前的数据: {}", new String(oldData.getData()));
                    } else {
                        log.debug("节点第一次赋值!");
                    }
                    String[] oldPaths = parsePath(oldData);
                    String[] newPaths = parsePath(newData);
                    serviceCache.replaceServiceAddress(oldPaths[1], oldPaths[2], newPaths[2]);
                    log.info("节点更新：服务名称 {} 地址从 {} 更新为 {}", oldPaths[1], oldPaths[2], newPaths[2]);
                    break;
                case "NODE_DELETE":     // 得到节点删除的通知，此时data为空
                    String[] pathToDelete = parsePath(oldData);
                    if(pathToDelete.length <= 2) {
                        break;
                    } else {
                        String serviceName = pathToDelete[1];
                        String serverAddress = pathToDelete[2];
                        serviceCache.deleteServiceAddress(serviceName, serverAddress);
                        log.info("节点删除：服务名称 {} 地址 {}", serviceName, serverAddress);
                    }
                    break;
                default:
                    break;
            }
        });
        curatorCache.start();   // 这行别忘了，没有start它怎么会主动监听呢
    }

    public String[] parsePath(ChildData data) {
        String path = data.getPath();
        log.info("节点路径：{}", path);
        return path.split("/");
    }


}
