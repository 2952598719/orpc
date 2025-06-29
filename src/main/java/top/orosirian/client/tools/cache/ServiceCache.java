package top.orosirian.client.tools.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ServiceCache {

    private final Map<String, List<String>> addressCache;  // 服务名->服务地址列表。为什么列表不存Inet，因为String简单，扩展性强

    private static volatile ServiceCache instance = null;

    private ServiceCache() {
        addressCache = new HashMap<>();
    }

    public static ServiceCache getInstance() {
        if(instance == null) {
            synchronized (ServiceCache.class) {
                if(instance == null) {
                    instance = new ServiceCache();
                }
            }
        }
        return instance;
    }

    public boolean containService(String serviceName) {
        return addressCache.containsKey(serviceName);
    }

    public List<String> getServiceAddressList(String serviceName) {
        return addressCache.get(serviceName);
    }

    public void addServiceAddress(String serviceName, String serviceAddress) {
        if (containService(serviceName)) {
            List<String> addressList = addressCache.get(serviceName);
            if(!addressList.contains(serviceAddress)) {     // 没有才往里加，不然这个list就放重复了
                addressList.add(serviceAddress);
            }
        } else {
            List<String> addressList = new ArrayList<>();
            addressList.add(serviceAddress);
            addressCache.put(serviceName, addressList);
        }
        log.info("将name为{}，地址为{}的服务添加到本地缓存中", serviceName, serviceAddress);
    }

    public void replaceServiceAddress(String serviceName, String oldAddress, String newAddress) {
        if (containService(serviceName)) {
            List<String> addressList = addressCache.get(serviceName);
            addressList.remove(oldAddress);     // 如果不存在oldAddress也正常工作
            addressList.add(newAddress);
            log.info("替换服务{}的地址{}为{}", serviceName, oldAddress, newAddress);
        } else {
            log.error("服务{}在缓存中不存在，修改失败", serviceName);
        }
    }

    public void deleteServiceAddress(String serviceName, String serviceAddress) {
        if(containService(serviceName)) {
            List<String> addressList = addressCache.get(serviceName);
            addressList.remove(serviceAddress);
            log.info("已将name为{}和地址为{}的服务从本地缓存中删除", serviceName, serviceAddress);
            if (addressList.isEmpty()) {
                addressCache.remove(serviceName);  // 移除该服务的缓存条目
                log.info("服务{}的地址列表为空，已从缓存中清除", serviceName);
            }
        } else {
            log.warn("未发现服务{}，删除失败", serviceName);
        }
    }

}
