package top.orosirian.server.core.impl;

import lombok.extern.slf4j.Slf4j;
import top.orosirian.server.core.RpcServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ThreadPoolRpcServer implements RpcServer {

    private final ThreadPoolExecutor threadPool;

    public ThreadPoolRpcServer() {
        threadPool = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),     // 可用处理其
                1000,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100)
        );
    }

    @Override
    public void start(int port) {
        System.out.println("[] 服务端已启动");
        try(ServerSocket serverSocket = new ServerSocket()) {
            while(true) {
                Socket socket = serverSocket.accept();
                threadPool.execute(new WorkThread(socket));
            }
        } catch(IOException e) {
            log.info("服务端启动失败");
        }
    }

    @Override
    public void stop() {

    }
}
