package top.orosirian.client.discoverer.balance.impl;

import lombok.extern.slf4j.Slf4j;
import top.orosirian.client.discoverer.balance.LoadBalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RoundBalance implements LoadBalance {

    private final AtomicInteger index = new AtomicInteger(0);

    private static volatile RoundBalance instance = null;

    public static LoadBalance getInstance() {
        if (instance == null) {
            synchronized (ConsistencyBalance.class) {
                if (instance == null) {
                    instance = new RoundBalance();
                }
            }
        }
        return instance;
    }

    @Override
    public String selectAddr(List<String> addrList) {
        if (addrList == null || addrList.isEmpty()) {
            throw new IllegalArgumentException("地址列表不能为空");
        }
        int currentIndex = index.getAndIncrement() % addrList.size();
        log.info("轮询负载均衡选择了: " + currentIndex + "号服务器，地址为: " + addrList.get(currentIndex));
        return addrList.get(currentIndex);
    }
    
}
