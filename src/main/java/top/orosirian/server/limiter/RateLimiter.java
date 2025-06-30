package top.orosirian.server.limiter;

public interface RateLimiter {

    boolean canGetToken();

}
