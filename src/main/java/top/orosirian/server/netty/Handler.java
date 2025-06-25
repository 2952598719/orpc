package top.orosirian.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.common.message.RpcRequest;
import top.orosirian.common.message.RpcResponse;
import top.orosirian.server.register.ServiceRegister;
import top.orosirian.server.register.impl.ZKServiceRegister;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class Handler extends SimpleChannelInboundHandler<RpcRequest> {

    private ServiceRegister serviceRegister = ZKServiceRegister.getInstance();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcResponse response = getResponse(request);
        ctx.channel().writeAndFlush(response);  // 不要写成ctx.writeAndFlush，那会导致并非从链表尾部开始处理，而是从NettyServerHandler开始处理
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }

    private RpcResponse getResponse(RpcRequest rpcRequest) {
        String interfaceName = rpcRequest.getInterfaceName();

        Object service = serviceRegister.getService(interfaceName);
        Method method;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            Object invoke = method.invoke(service, rpcRequest.getParams());
            return RpcResponse.success(invoke);
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage(), e);
            return RpcResponse.fail("方法执行错误");
        }
    }

}
