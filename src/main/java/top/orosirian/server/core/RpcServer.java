package top.orosirian.server.core;

public interface RpcServer {

    void start(int port);   // 开启监听

    void stop();

}
