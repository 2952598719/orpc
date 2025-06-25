package top.orosirian.client.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.common.message.RpcResponse;

@Slf4j
public class Handler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    // channelRead处理类型检查和资源释放等和业务无关的事情，并调用channelRead0这个实际处理业务的函数
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");
        // attr就是个缓冲区，channel线程把自己获取到的对象放入其中后，主线程才能根据对象引用名，从缓冲区中获取内容
        ctx.channel().attr(key).set(response);
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Channel exception occurred", cause);
        ctx.close();
    }

}
