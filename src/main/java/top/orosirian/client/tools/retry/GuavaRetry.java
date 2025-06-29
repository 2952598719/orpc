package top.orosirian.client.tools.retry;

import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.client.core.RpcClient;
import top.orosirian.common.message.RpcRequest;
import top.orosirian.common.message.RpcResponse;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GuavaRetry {

    public RpcResponse sendServiceWithRetry(RpcRequest request, RpcClient client) {
        RetryerBuilder<RpcResponse> builder = RetryerBuilder.newBuilder();
        Retryer<RpcResponse> retryer = builder
                                        .retryIfException()     // 出现异常则重试
                                        .retryIfResult(response -> Objects.equals(response.getCode(), 500))     // 返回500状态码则重试
                                        .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))    // 每次重试先等待2s
                                        .withStopStrategy(StopStrategies.stopAfterAttempt(3))           //最多重试3次
                                        .withRetryListener(new RetryListener() {    // 重试监听器，每次重试打印日志
                                            @Override
                                            public <V> void onRetry(Attempt<V> attempt) {
                                                log.info("RetryListener: 第" + attempt.getAttemptNumber() + "次调用");
                                            }
                                        })
                                        .build();
        try {
            return retryer.call(() -> client.sendRequest(request));
        } catch(Exception e) {
            log.error("重试失败: 请求 {} 执行时遇到异常", request.getMethodName(), e);
        }
        return RpcResponse.fail("重试失败，所有重试尝试已结束");
    }
    
}
