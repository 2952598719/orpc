package top.orosirian.server.limiter.impl;

import top.orosirian.server.limiter.RateLimiter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TokenBucketRateLimiterImpl implements RateLimiter {

    private final int tokenInterval;            // 每多久产生一个令牌

    private final int capacity;                 // 桶容量

    private final AtomicInteger tokenNum;       // 当前令牌数

    private final AtomicLong lastRefillTime;    // 上次生成令牌的时间

    public TokenBucketRateLimiterImpl(int ratePerSecond, int capacity) {
        this.tokenInterval = 1000 / ratePerSecond;
        this.capacity = capacity;
        this.tokenNum = new AtomicInteger(capacity);
        this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
    }

    @Override
    public boolean canGetToken() {
        refillToken();  // 懒惰生成令牌
        int currentTokenNum = tokenNum.get();
        while(currentTokenNum > 0) {    // CAS修改
            if(tokenNum.compareAndSet(currentTokenNum, currentTokenNum - 1)) {
                return true;
            }
            currentTokenNum = tokenNum.get();
        }
        return false;
    }

    private void refillToken() {
        long now = System.currentTimeMillis();
        long lastTime = lastRefillTime.get();
        if(now - lastTime < tokenInterval) {    // 未到生成令牌的时间
            return;
        }
        long tokensToAdd = (now - lastTime) / tokenInterval;
        if(tokensToAdd > 0) {
            long newRefillTime = lastTime + tokensToAdd * tokenInterval;    // 时间余量问题：因为token是离散生成的，当前可能是上一个token生成后的一小段时间后，如果以now作为上次生成时间，会导致这段时间被抛弃，生成的token就减少
            if(lastRefillTime.compareAndSet(lastTime, newRefillTime)) {
                tokenNum.updateAndGet(current -> Math.min(capacity, current + (int)tokensToAdd));
            }
        }
    }

}
