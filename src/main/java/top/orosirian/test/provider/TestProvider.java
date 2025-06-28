package top.orosirian.test.provider;

import top.orosirian.server.core.impl.NettyRpcServer;
import top.orosirian.server.register.ServiceRegister;
import top.orosirian.server.register.impl.ZKServiceRegister;
import top.orosirian.test.provider.service.UserServiceImpl;
import top.orosirian.server.core.RpcServer;

public class TestProvider {

    public static void main(String[] args) {
        ServiceRegister serviceRegister = ZKServiceRegister.getInstance();
        serviceRegister.register(new UserServiceImpl());    // 服务说到底就是接口+具体实现

        RpcServer rpcServer = new NettyRpcServer();
        rpcServer.start();  // stop不知道在哪插进去
    }

}
