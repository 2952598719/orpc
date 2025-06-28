package top.orosirian.client.proxy;

import lombok.extern.slf4j.Slf4j;
import top.orosirian.client.core.RpcClient;
import top.orosirian.client.core.impl.NettyRpcClient;
import top.orosirian.common.message.RpcRequest;
import top.orosirian.common.message.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class ClientProxy implements InvocationHandler {

    private final RpcClient rpcClient;

    public ClientProxy() {
        this.rpcClient = NettyRpcClient.getInstance();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 1.构建request
        RpcRequest request = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args)
                .paramsType(method.getParameterTypes())
                .build();
        // 3.数据传输
        RpcResponse response;
        response = rpcClient.sendRequest(request);
        // 5.返回数据
        return response != null ? response.getData() : null;
    }

    @SuppressWarnings("unchecked")
    public <T>T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T)o;
    }

    public void stop() {
        rpcClient.stop();
    }

}
