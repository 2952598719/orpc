package top.orosirian.test.provider;

import top.orosirian.server.core.RpcServer;
import top.orosirian.server.core.impl.NettyRpcServer;
import top.orosirian.server.register.impl.ZKServiceRegister;
import top.orosirian.test.provider.service.UserServiceImpl;

public class TestProvider {

    public static void main(String[] args) {
        // 注册服务
        ZKServiceRegister serviceRegister = ZKServiceRegister.getInstance();
        serviceRegister.register(new UserServiceImpl());
        // 监听连接
        RpcServer rpcServer = NettyRpcServer.getInstance();
        rpcServer.start();
    }

}
