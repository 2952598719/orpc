package top.orosirian.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.common.coder.Decoder;
import top.orosirian.common.coder.Encoder;
import top.orosirian.common.serializer.Serializer;
import top.orosirian.common.serializer.SerializerType;

@Slf4j
public class Initializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        try {
            // 网络 <->
            pipeline.addLast(new Encoder(Serializer.getSerializer(SerializerType.JSON_SERIALIZER.code)));
            pipeline.addLast(new Decoder());
            pipeline.addLast(new Handler());
            // <->主机
            log.info("Netty 服务器 pipeline 以类型{}初始化", Serializer.getSerializer(SerializerType.JSON_SERIALIZER.code).toString());
        } catch (Exception e) {
            log.error("初始化netty客户端pipeline失败", e);
            throw e;  // 重新抛出异常，确保管道初始化失败时处理正确
        }
    }
}
