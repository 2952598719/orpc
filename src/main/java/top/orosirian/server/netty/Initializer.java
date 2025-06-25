package top.orosirian.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.common.coder.Decoder;
import top.orosirian.common.coder.Encoder;
import top.orosirian.common.serializer.Serializer;
import top.orosirian.common.serializer.impl.JsonSerializer;

@Slf4j
public class Initializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        try {
            pipeline.addLast(new Encoder(new JsonSerializer()));
            pipeline.addLast(new Decoder());
            pipeline.addLast(new Handler());

            log.info("Netty server pipeline initialized with serializer type: {}", Serializer.getSerializer(3).toString());
        } catch (Exception e) {
            log.error("Error initializing Netty client pipeline", e);
            throw e;  // 重新抛出异常，确保管道初始化失败时处理正确
        }
    }
}
