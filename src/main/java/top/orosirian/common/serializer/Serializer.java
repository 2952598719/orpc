package top.orosirian.common.serializer;

import top.orosirian.common.serializer.impl.*;

import java.util.HashMap;
import java.util.Map;

public interface Serializer {

    byte[] serialize(Object obj);   // 对象序列化成字节数组

    Object deserialize(byte[] bytes, int messageType);  // 字节数组反序列化成对象

    int getType();  // 得到所用序列化器，0-java自带，1-alibaba.fastjson

    // 用静态变量Map来缓存序列化器，并且以静态内部类来延迟加载
    static Serializer getSerializer(int code) {
        return SerializerHolder.SERIALIZER_MAP.get(code);
    }

    static class SerializerHolder {
        static Map<Integer, Serializer> SERIALIZER_MAP = createMap();

        private static Map<Integer, Serializer> createMap() {
            Map<Integer, Serializer> map = new HashMap<>();
            map.put(SerializerType.OBJECT_SERIALIZER.code, new ObjectSerializer());
            map.put(SerializerType.JSON_SERIALIZER.code, new JsonSerializer());
            map.put(SerializerType.KRYO_SERIALIZER.code, new KryoSerializer());
            map.put(SerializerType.HESSIAN_SERIALIZER.code, new HessianSerializer());
            map.put(SerializerType.PROTOSTUFF_SERIALIZER.code, new ProtostuffSerializer());
            return map;
        }
    }
    
}
