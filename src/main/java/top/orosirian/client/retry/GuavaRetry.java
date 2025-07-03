package top.orosirian.client.retry;

import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.client.core.RpcClient;
import top.orosirian.common.message.RpcRequest;
import top.orosirian.common.message.RpcResponse;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class GuavaRetry {

    public RpcResponse sendServiceWithRetry(RpcRequest request, RpcClient client) {
        final AtomicReference<Throwable> lastError = new AtomicReference<>();
        RetryerBuilder<RpcResponse> builder = RetryerBuilder.newBuilder();
        Retryer<RpcResponse> retryer = builder
                                        .retryIfException(ex -> {
                                            lastError.set(ex); // 记录最后一次异常
                                            return true;
                                        })     // 出现异常则重试
                                        .retryIfResult(response -> Objects.equals(response.getCode(), 500))     // 返回500状态码则重试
                                        .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))    // 每次重试先等待2s
                                        .withStopStrategy(StopStrategies.stopAfterAttempt(3))           //最多重试3次
                                        .withRetryListener(new RetryListener() {    // 重试监听器，每次重试打印日志
                                            @Override
                                            public <V> void onRetry(Attempt<V> attempt) {
                                                if (attempt.hasException()) {
                                                    log.warn("重试中 ({}): {}",
                                                            attempt.getAttemptNumber(),
                                                            attempt.getExceptionCause().getMessage());
                                                }
                                            }
                                        })
                                        .build();
        try {
            return retryer.call(() -> client.sendRequest(request));
        } catch(RetryException e) {
            Throwable cause = lastError.get() != null ? lastError.get() : e;
            String errorMsg = String.format("服务调用失败 [已重试%d次]，最后错误: %s",
                    e.getNumberOfFailedAttempts(),
                    cause.getMessage());

            log.warn(errorMsg); // 警告级别日志
            return RpcResponse.fail(errorMsg);
        } catch (ExecutionException e) {
            Throwable cause = lastError.get() != null ? lastError.get() : e;
            String errorMsg = String.format("服务调用失败，最后错误: %s",
                    cause.getMessage());
            log.warn(errorMsg); // 警告级别日志
            return RpcResponse.fail(errorMsg);
        }
    }
    
}
