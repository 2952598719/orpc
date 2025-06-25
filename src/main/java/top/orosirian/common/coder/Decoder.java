package top.orosirian.common.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.common.SerializeException;
import top.orosirian.common.message.MessageType;
import top.orosirian.common.serializer.Serializer;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class Decoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 0.检查数据长度是否足够
        if(in.readableBytes() < 8) {    // messageType-2，serializerType-2，length-4
            return;
        }
        // 1.读取消息类型
        short messageType = in.readShort();
        if(messageType != MessageType.REQUEST.getCode() && messageType != MessageType.RESPONSE.getCode()) {
            log.warn("暂不支持此种数据, messageType: {}", messageType);
            return;
        }
        // 2.读取序列化方式，并构造相应的序列化器
        short serializerType = in.readShort();
        Serializer serializer = Serializer.getSerializer(serializerType);
        if(serializer == null) {
            log.error("不存在对应的序列化器, serializerType: {}", serializerType);
            throw new SerializeException("不存在对应的序列化器, serializerType: " + serializerType);
        }
        // 3.读取序列化数组长度
        int length = in.readInt();
        if(in.readableBytes() < length) {
            return;     // 数据不完整，等待更多数据
        }
        // 4.读取数据
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        log.debug("接受的数据: {}", Arrays.toString(bytes));
        Object deserialize = serializer.deserialize(bytes, messageType);
        out.add(deserialize);
    }

}
