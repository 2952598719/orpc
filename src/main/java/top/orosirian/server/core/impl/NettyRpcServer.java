package top.orosirian.server.core.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.server.core.RpcServer;
import top.orosirian.server.netty.Initializer;

@Slf4j
public class NettyRpcServer implements RpcServer {

    private ChannelFuture channelFuture;

    private NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);  // 监听接受请求，分配给workGroup

    private NioEventLoopGroup workGroup = new NioEventLoopGroup();            // 处理实际业务

    @Override
    public void start(int port) {
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        log.info("Netty服务端启动中...");
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new Initializer());
            channelFuture = serverBootstrap.bind(port).sync();
            log.info("Netty服务端已绑定端口：{}", port);
            channelFuture.channel().closeFuture().sync(); // 阻塞直到channel关闭
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("启动中断: {}", e.getMessage(), e);
        } finally {
            if (channelFuture == null || !channelFuture.isSuccess()) {
                shutdown();
            }
        }
    }

    @Override
    public void stop() {
        try {
            if (channelFuture != null) {
                channelFuture.channel().close().sync();     // 1. 关闭通道触发start()的closeFuture().sync()返回
                log.info("主通道已关闭");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("关闭通道中断: {}", e.getMessage(), e);
        } finally {
            shutdown();     // 2. 无论启动是否成功都统一回收资源
        }
    }

    private void shutdown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
        if (workGroup != null) {
            workGroup.shutdownGracefully();
            workGroup = null;
        }
        log.info("资源已释放");
    }


//    @Override
//    public void start(int port) {
//        // netty 服务线程组boss负责建立连接， work负责具体的请求
//        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
//        NioEventLoopGroup workGroup = new NioEventLoopGroup();
//        System.out.println("netty服务端启动了");
//        try {
//            //启动netty服务器
//            ServerBootstrap serverBootstrap = new ServerBootstrap();
//            //初始化
//            serverBootstrap.group(bossGroup,workGroup).channel(NioServerSocketChannel.class)
//                    .childHandler(new Initializer());
//            //同步堵塞
//            ChannelFuture channelFuture=serverBootstrap.bind(port).sync();
//            //死循环监听
//            channelFuture.channel().closeFuture().sync();
//        }catch (InterruptedException e){
//            e.printStackTrace();
//        }finally {
//            bossGroup.shutdownGracefully();
//            workGroup.shutdownGracefully();
//        }
//    }
//
//    @Override
//    public void stop() {
//
//    }

}
