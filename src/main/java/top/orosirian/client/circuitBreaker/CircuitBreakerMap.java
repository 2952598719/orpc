package top.orosirian.client.circuitBreaker;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CircuitBreakerMap {

    private final Map<String, CircuitBreaker> circuitBreakerMap;

    private static volatile CircuitBreakerMap instance = null;

    private CircuitBreakerMap() {
        circuitBreakerMap = new ConcurrentHashMap<>();
    }

    public static CircuitBreakerMap getInstance() {
        if(instance == null) {
            synchronized (CircuitBreakerMap.class) {
                if(instance == null) {
                    instance = new CircuitBreakerMap();
                }
            }
        }
        return instance;
    }

    // 方法粒度熔断
    public synchronized CircuitBreaker getCircuitBreaker(String methodSignature) {
        // 为了清晰点分成if-else
        if(circuitBreakerMap.containsKey(methodSignature)) {
            return circuitBreakerMap.get(methodSignature);
        } else {
            log.info("方法 [{}] 不存在熔断器，创建新的熔断器实例", methodSignature);
            CircuitBreaker circuitBreaker = new CircuitBreaker(5, 10000, 0.5);
            circuitBreakerMap.put(methodSignature, circuitBreaker);
            return circuitBreaker;
        }
    }

}
