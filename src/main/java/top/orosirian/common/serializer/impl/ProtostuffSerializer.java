package top.orosirian.common.serializer.impl;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import top.orosirian.common.SerializeException;
import top.orosirian.common.Utils;
import top.orosirian.common.serializer.Serializer;

public class ProtostuffSerializer implements Serializer {


    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("序列化对象不能为空");
        }
        Schema schema = RuntimeSchema.getSchema(obj.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        byte[] bytes;
        bytes = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        buffer.clear();
        return bytes;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("反序列化数组不能为空");
        }
        try {
            Class<?> clazz = Utils.getClassForMessageType(messageType);
            Schema schema = RuntimeSchema.getSchema(clazz);
            Object obj = clazz.getDeclaredConstructor().newInstance();
            ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
            return obj;
        } catch (Exception e) {
            throw new SerializeException("反序列化失败，因为反射原因");
        }
    }

    @Override
    public int getType() {
        return 4;
    }
}
