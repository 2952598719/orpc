package top.orosirian.client.proxy;

import top.orosirian.client.core.impl.NettyRpcClient;
import top.orosirian.client.core.impl.SocketRpcClient;
import top.orosirian.common.message.RpcRequest;
import top.orosirian.common.message.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ClientProxy implements InvocationHandler {
    //传入参数service接口的class对象，反射封装成一个request
    private String host = "localhost";
    private int port = 9999;

    private final NettyRpcClient rpcClient = NettyRpcClient.getInstance();

    //jdk动态代理，每一次代理对象调用方法，都会经过此方法增强（反射获取request对象，socket发送到服务端）
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        //构建request
        RpcRequest request = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args).paramsType(method.getParameterTypes()).build();
        //IOClient.sendRequest 和服务端进行数据传输
        RpcResponse response = rpcClient.sendRequest(request);
        return response.getData();
    }

    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }

    public void stop() {
        rpcClient.stop();
    }
}
