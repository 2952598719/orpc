package top.orosirian.client.discoverer.balance;

import top.orosirian.client.discoverer.balance.impl.ConsistencyBalance;
import top.orosirian.client.discoverer.balance.impl.RandomBalance;
import top.orosirian.client.discoverer.balance.impl.RoundBalance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface LoadBalance {

    String selectAddr(List<String> addrList);

    static LoadBalance getLoadBalance(int code) {
        return LoadBalance.LoadBalanceHolder.LOADBALANCE_MAP.get(code);
    }

    class LoadBalanceHolder {
        static Map<Integer, LoadBalance> LOADBALANCE_MAP = createMap();

        private static Map<Integer, LoadBalance> createMap() {
            Map<Integer, LoadBalance> map = new HashMap<>();
            map.put(LoadBalanceType.RANDOM.code, new RandomBalance());
            map.put(LoadBalanceType.ROUND.code, new RoundBalance());
            map.put(LoadBalanceType.CONSISTENT_HASH.code, ConsistencyBalance.getInstance());
            return map;
        }
    }
    
}
