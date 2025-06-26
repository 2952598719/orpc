package top.orosirian.common.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.common.message.MessageType;
import top.orosirian.common.message.RpcRequest;
import top.orosirian.common.message.RpcResponse;
import top.orosirian.common.serializer.Serializer;

@SuppressWarnings("rawtypes")
@Slf4j
public class Encoder extends MessageToByteEncoder {

    private final Serializer serializer;

    public Encoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        log.debug("正在编码消息类型: {}", msg.getClass());
        // 0.得到数据
        byte[] serializeBytes = serializer.serialize(msg);
        if(serializeBytes == null || serializeBytes.length == 0) {
            throw new IllegalArgumentException("序列化消息为空");
        }
        // 1.写入消息类型，2字节
        if(msg instanceof RpcRequest) {
            out.writeShort(MessageType.REQUEST.getCode());
        } else if(msg instanceof RpcResponse) {
            out.writeShort(MessageType.RESPONSE.getCode());
        } else {
            log.error("未知消息类型: {}", msg.getClass());
            throw new IllegalArgumentException("未知消息类型: " + msg.getClass());
        }
        // 2.写入序列化方式，2字节
        out.writeShort(serializer.getType());
        // 3.写入长度，4字节
        out.writeInt(serializeBytes.length);
        // 4.写入数据
        out.writeBytes(serializeBytes);
    }

}
