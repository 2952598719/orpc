package top.orosirian.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.common.message.RpcRequest;
import top.orosirian.common.message.RpcResponse;
import top.orosirian.server.register.ServiceRegister;
import top.orosirian.server.register.impl.ZKServiceRegister;
import top.orosirian.server.limiter.RateLimiter;
import top.orosirian.server.limiter.RateLimiterMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class Handler extends SimpleChannelInboundHandler<RpcRequest> {

    private final RateLimiterMap rateLimiterMap;

    private final ServiceRegister serviceRegister;

    public Handler() {
        rateLimiterMap = RateLimiterMap.getInstance();
        serviceRegister = ZKServiceRegister.getInstance();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) {
        RpcResponse response = getResponse(request);
        ctx.channel().writeAndFlush(response);  // 不要写成ctx.writeAndFlush，那会导致并非从链表尾部开始处理，而是从NettyServerHandler开始处理
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }

    private RpcResponse getResponse(RpcRequest rpcRequest) {
        String interfaceName = rpcRequest.getInterfaceName();

        // 首先执行限流
        RateLimiter rateLimiter = rateLimiterMap.getRateLimit(interfaceName);
        if(!rateLimiter.canGetToken()) {
            log.info("服务限流");
            return RpcResponse.fail("服务限流，接口 " + interfaceName + " 当前无法处理请求。请稍后再试。");
        }
        // 其次处理请求
        Object service = serviceRegister.getService(interfaceName);
        Method method;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            Object result = method.invoke(service, rpcRequest.getParams());
            return RpcResponse.success(result);
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage(), e);
            return RpcResponse.fail("方法执行错误");
        }
    }

}
