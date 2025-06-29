package top.orosirian.client.discoverer.balance.impl;

import lombok.extern.slf4j.Slf4j;
import top.orosirian.client.discoverer.balance.LoadBalance;

import java.util.List;
import java.util.Random;

@Slf4j
public class RandomBalance implements LoadBalance {

    Random random = new Random();

    private static volatile RandomBalance instance = null;

    public static LoadBalance getInstance() {
        if (instance == null) {
            synchronized (ConsistencyBalance.class) {
                if (instance == null) {
                    instance = new RandomBalance();
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
        int currentIndex = random.nextInt(addrList.size());
        log.info("随机负载均衡选择了: " + currentIndex + "号服务器，地址为: " + addrList.get(currentIndex));
        return addrList.get(currentIndex);
    }
    
}
