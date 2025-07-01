package top.orosirian.client.proxy;

import lombok.extern.slf4j.Slf4j;
import top.orosirian.client.circuitBreaker.CircuitBreaker;
import top.orosirian.client.circuitBreaker.CircuitBreakerMap;
import top.orosirian.client.core.impl.NettyRpcClient;
import top.orosirian.client.retry.GuavaRetry;
import top.orosirian.common.Utils;
import top.orosirian.common.message.RpcRequest;
import top.orosirian.common.message.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class ClientProxy implements InvocationHandler {

    private final NettyRpcClient rpcClient = NettyRpcClient.getInstance();

    private final CircuitBreakerMap circuitBreakerMap = CircuitBreakerMap.getInstance();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 1.构建request
        RpcRequest request = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args).paramsType(method.getParameterTypes()).build();
        // 2.获取服务对应的熔断器
        String methodSignature = Utils.getMethodSignature(method.getDeclaringClass(), method);
        CircuitBreaker circuitBreaker = circuitBreakerMap.getCircuitBreaker(methodSignature);
        if(!circuitBreaker.allowRequest()) {
            log.warn("熔断器开启，请求被拒绝: {}", request);
            return null;
        }
        // 3.数据传输
        RpcResponse response;
        if(Utils.checkRetry(method)) {
            try {
                log.info("尝试重试调用服务: {}", method);
                response = new GuavaRetry().sendServiceWithRetry(request, rpcClient);
            } catch(Exception e) {
                log.error("重试调用失败: {}", method.getName());
                circuitBreaker.recordFailure();
                throw e;  // 将异常抛给调用者
            }
        } else {    // 只调用一次
            log.info("方法无需重试");
            response = rpcClient.sendRequest(request);
        }
        // 4.记录response状态
        if(response != null) {
            log.info("收到响应: {} 状态码: {}，正在设置熔断器", request.getInterfaceName(), response.getCode());
            if(response.getCode() == 200) {
                circuitBreaker.recordSuccess();
            } else {
                circuitBreaker.recordFailure();
            }
        }
        // 5.返回数据
        return response.getData();
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }

    public void stop() {
        rpcClient.stop();
    }
}
