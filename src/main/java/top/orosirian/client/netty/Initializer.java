package top.orosirian.client.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.common.coder.Decoder;
import top.orosirian.common.coder.Encoder;
import top.orosirian.common.serializer.Serializer;
import top.orosirian.common.serializer.SerializerType;

// 处理器，一个请求会经过下面的流程
@Slf4j
public class Initializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();   // 每个处理器都标识了自己是出站ChannelOutboundHandlerAdapter，还是入站ChannelInboundHandlerAdapter
        try {
            pipeline.addLast(new Encoder(Serializer.getSerializer(SerializerType.JSON_SERIALIZER.code)));
            pipeline.addLast(new Decoder());
            pipeline.addLast(new Handler());
            log.info("Netty client pipeline initialized with serializer type: {}", Serializer.getSerializer(SerializerType.JSON_SERIALIZER.code).toString());
        } catch (Exception e) {
            log.error("Error initializing Netty client pipeline", e);
            throw e;  // 重新抛出异常，确保管道初始化失败时处理正确
        }
    }

}

