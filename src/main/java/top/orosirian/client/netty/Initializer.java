package top.orosirian.client.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.common.coder.Decoder;
import top.orosirian.common.coder.Encoder;
import top.orosirian.common.serializer.Serializer;
import top.orosirian.common.serializer.impl.JsonSerializer;

// 处理器，一个请求会经过下面的流程
@Slf4j
public class Initializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();   // 每个处理器都标识了自己是出站ChannelOutboundHandlerAdapter，还是入站ChannelInboundHandlerAdapter
        try {
            pipeline.addLast(new Encoder(new JsonSerializer()));
            pipeline.addLast(new Decoder());
            pipeline.addLast(new Handler());
            log.info("Netty client pipeline initialized with serializer type: {}", Serializer.getSerializer(3).toString());
        } catch (Exception e) {
            log.error("Error initializing Netty client pipeline", e);
            throw e;  // 重新抛出异常，确保管道初始化失败时处理正确
        }



        // // 帧结构：头部（A+长度+B）+内容

        // // 网络<->
        //     // 入站
        // pipeline.addLast(new LengthFieldBasedFrameDecoder(  // 用于从数据流中得到一个帧
        //         Integer.MAX_VALUE,      // 帧最大长度
        //         // 首先通过lengthFieldOffset和lengthFieldLength获取长度字段，长度字段并不一定在开头，也可能在头部的中间，所以要通过偏移量+lengthFieldLength共同获取
        //         0, 4,
        //         // 然后通过lengthAdjustment来将长度转换为固定的含义（内容长度），目的就是得到这个帧的结束位置，这样数据流中之后的字节就是下一个帧了
        //         0,
        //         // 最后通过initialBytesToStrip得到要传递给下一个处理器的部分，由于头部字段不只有长度，其他头部字段可能下一个处理器也要用，所以也不一定这里就是把整个头部拿掉，把内容给下一个，很有可能整个报文（或者去掉长度字段）拿给下一个处理器
        //         4
        //     )
        // );
        // pipeline.addLast(new ObjectDecoder((className) -> Class.forName(className)));   // 将数据反序列化成RPCResponse对象
        // pipeline.addLast(new NettyClientHandler());     // 交给自定义的NettyClientHandler处理
        //     // 出站
        // pipeline.addLast(new LengthFieldPrepender(4));  // 在序列化后的结果前加上4字节的长度字段，表明内容长度
        // pipeline.addLast(new ObjectEncoder());          // 将RPCRequest对象序列化
        // // <->主机
    }

}

