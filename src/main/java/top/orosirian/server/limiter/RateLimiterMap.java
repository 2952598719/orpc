package top.orosirian.server.limiter;

import top.orosirian.server.limiter.impl.TokenBucketRateLimiterImpl;

import java.util.HashMap;
import java.util.Map;

public class RateLimiterMap {

    private final Map<String, RateLimiter> map;

    private static volatile RateLimiterMap instance = null;

    private RateLimiterMap() {
        map = new HashMap<>();
    }

    public static RateLimiterMap getInstance() {
        if(instance == null) {
            synchronized (RateLimiterMap.class) {
                if(instance == null) {
                    instance = new RateLimiterMap();
                }
            }
        }
        return instance;
    }

    public RateLimiter getRateLimit(String interfaceName) {
        // 这里还是别用getOrDefault了，不然逻辑不太清楚
        if (map.containsKey(interfaceName)) {
            return map.get(interfaceName);
        } else {
            RateLimiter rateLimiter = new TokenBucketRateLimiterImpl(100, 10);
            map.put(interfaceName, rateLimiter);
            return rateLimiter;
        }
    }

}
