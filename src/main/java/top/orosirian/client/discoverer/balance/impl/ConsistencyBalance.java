package top.orosirian.client.discoverer.balance.impl;

import lombok.extern.slf4j.Slf4j;
import top.orosirian.client.discoverer.balance.LoadBalance;

import java.util.*;

@Slf4j
public class ConsistencyBalance implements LoadBalance {

    private static final int VIRTUAL_NUM = 5;

    private List<String> lastAddressList;            // 真实节点列表

    private final SortedMap<Integer, String> virtualNodes;    // 虚拟节点分配，key是节点哈希值，value是其所在服务器地址

    private static volatile ConsistencyBalance instance = null;

    private ConsistencyBalance() {
        lastAddressList = Collections.emptyList();
        virtualNodes = new TreeMap<>();
    }

    public static LoadBalance getInstance() {
        if (instance == null) {
            synchronized (ConsistencyBalance.class) {
                if (instance == null) {
                    instance = new ConsistencyBalance();
                }
            }
        }
        return instance;
    }


    @Override
    public String selectAddr(List<String> addrList) {
        updateRing(addrList);
        String requestId = UUID.randomUUID().toString();
        int hash = getHash(requestId);
        SortedMap<Integer, String> tailMap = virtualNodes.tailMap(hash);    // hash值大于等于所请求hash值的虚拟节点
        String virtualNode = tailMap.isEmpty() ? virtualNodes.get(virtualNodes.firstKey()) : tailMap.get(tailMap.firstKey());   // 如果没找到，代表大于所有节点，从已有选个最大的就行
        log.info("负载均衡选中{}", virtualNode);
        return virtualNode.split("&&")[0];
    }

    private void updateRing(List<String> addressList) {
        if (addressList == null || addressList.isEmpty()) {
            throw new IllegalArgumentException("地址列表不能为空");
        }
        if(addressList.equals(lastAddressList)) return;

        virtualNodes.clear();
        for(String address : addressList) {
            for(int i = 0; i <= VIRTUAL_NUM - 1; i++) {
                String virtualNode = address + "&&VN" + i;
                int hash = getHash(virtualNode);
                virtualNodes.put(hash, virtualNode);
                log.info("添加虚拟节点: " + virtualNode + " hash值为" + hash);
            }
        }
        lastAddressList = new ArrayList<>(addressList);
    }

    // FNV1_32_HASH算法
    // TODO: 找个第三方库，免得面试官问怎么哈希的
    private static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }
    
}
