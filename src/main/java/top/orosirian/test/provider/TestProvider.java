package top.orosirian.test.provider;

import top.orosirian.server.core.impl.NettyRpcServer;
import top.orosirian.server.register.ServiceRegister;
import top.orosirian.server.register.impl.ZKServiceRegister;
import top.orosirian.test.common.service.UserService;
import top.orosirian.test.provider.service.UserServiceImpl;
import top.orosirian.server.core.RpcServer;

public class TestProvider {

    public static void main(String[] args) {
        UserService userService = new UserServiceImpl();
        ServiceRegister serviceRegister = ZKServiceRegister.getInstance();
        serviceRegister.register(userService);

        RpcServer rpcServer = new NettyRpcServer();
        rpcServer.start(9999);
    }

}
