package top.orosirian.client.circuitBreaker;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CircuitBreaker {

    @Getter
    private CircuitBreakerState state;

    private final AtomicInteger requestCount;

    private final AtomicInteger failureCount;

    private final AtomicInteger successCount;

    private final int FAILURE_LIMIT;            // CLOSE -> OPEN 失败次数限制

    private final long RETRY_INTERVAL;          // OPEN -> HALF_OPEN 恢复时间

    private final double HALF_OPEN_THRESHOLD;   // HALF_OPEN -> CLOSE 成功比例

    private long lastFailureTime;               // 上次失败时间

    public CircuitBreaker(int failureLimit, long retryInterval, double halfOpenThreshold) {
        this.state = CircuitBreakerState.CLOSE;
        this.requestCount = new AtomicInteger(0);
        this.failureCount = new AtomicInteger(0);
        this.successCount = new AtomicInteger(0);
        this.FAILURE_LIMIT = failureLimit;
        this.RETRY_INTERVAL = retryInterval;
        this.HALF_OPEN_THRESHOLD = halfOpenThreshold;
    }

    // 能否执行请求
    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis();
        switch (state) {
            case CLOSE:         // 熔断器关闭代表正常请求，这不是个开关
                log.info("熔断器关闭，允许请求通过");
                return true;
            case HALF_OPEN:     // HALF_OPEN转OPEN需要在请求逻辑中处理，而不是此处处理
                requestCount.incrementAndGet();
                log.info("熔断器半开启，统计请求次数");
                return true;
            case OPEN:          // 熔断器打开
                if(currentTime - lastFailureTime > RETRY_INTERVAL) {
                    state = CircuitBreakerState.HALF_OPEN;
                    resetCounts();
                    log.info("熔断时间已过，尝试请求");
                    return true;
                } else {
                    log.warn("处于熔断状态，无法请求");
                    return false;
                }
            default:
                return false;
        }
    }

    public synchronized void recordSuccess() {
        switch (state) {
            case CLOSE:
                resetCounts();
                log.info("熔断器处于关闭状态，重置计数器");
                break;
            case HALF_OPEN:
                successCount.incrementAndGet();
                if(successCount.get() >= requestCount.get() * HALF_OPEN_THRESHOLD) {
                    state = CircuitBreakerState.CLOSE;
                    resetCounts();
                    log.info("成功次数已达到阈值，熔断器切换至关闭状态");
                }
                break;
            case OPEN:
                resetCounts();  // 其实不可能走到这步
                break;
        }
    }

    public synchronized void recordFailure() {
        failureCount.incrementAndGet();
        switch(state) {
            case CLOSE:
                if(failureCount.get() >= FAILURE_LIMIT) {
                    state = CircuitBreakerState.OPEN;
                    log.error("失败次数已超过阈值，熔断器切换至开启状态");
                }
                break;
            case HALF_OPEN:
                state = CircuitBreakerState.OPEN;
                lastFailureTime = System.currentTimeMillis();
                log.warn("半开启状态下发生失败，熔断器切换至开启状态");
                break;
            case OPEN:
                break;
        }
    }

    public void resetCounts() {
        requestCount.set(0);
        failureCount.set(0);
        successCount.set(0);
    }

}

// 熔断器关闭、半开、打开
enum CircuitBreakerState {
    CLOSE, HALF_OPEN, OPEN
}