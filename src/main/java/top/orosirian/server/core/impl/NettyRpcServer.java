package top.orosirian.server.core.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.server.core.RpcServer;
import top.orosirian.server.netty.Initializer;
import top.orosirian.server.register.impl.ZKServiceRegister;

@Slf4j
public class NettyRpcServer implements RpcServer {

    private static final int port = 9999;

    EventLoopGroup bossGroup;   // 类似线程池，其中的线程监听接受请求，分配给workGroup

    EventLoopGroup workGroup;

    private ChannelFuture channelFuture;

    private final ServerBootstrap serverBootstrap;

    private static volatile NettyRpcServer instance = null;

    private NettyRpcServer() {
        JSON.config(JSONReader.Feature.SupportClassForName);    // 否则fastjson会报错
        bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());   // 类似线程池，其中的线程监听接受请求，分配给workGroup
        workGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());  // 处理实际业务
        serverBootstrap = new ServerBootstrap()
                                .group(bossGroup, workGroup)
                                .channel(NioServerSocketChannel.class)
                                .childHandler(new Initializer());
    }

    public static NettyRpcServer getInstance() {
        if (instance == null) {
            synchronized (ZKServiceRegister.class) {
                if (instance == null) {
                    instance = new NettyRpcServer();
                }
            }
        }
        return instance;
    }

    @Override
    public void start() {
        log.info("Netty服务端启动中...");
        try {
            channelFuture = serverBootstrap.bind(port).sync();
            log.info("Netty服务端已绑定端口：{}", port);
            channelFuture.channel().closeFuture().sync(); // 阻塞直到channel关闭
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("启动中断: {}", e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        try {
            if (channelFuture != null) {
                channelFuture.channel().close().sync();     // 1. 关闭通道触发start()的closeFuture().sync()返回
                bossGroup.shutdownGracefully();
                workGroup.shutdownGracefully();
                log.info("主通道已关闭");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("关闭通道中断: {}", e.getMessage(), e);
        }
    }

}
