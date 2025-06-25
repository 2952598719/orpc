package top.orosirian.common.serializer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SerializerType {

    OBJECT_SERIALIZER(0, "ObjectSerializer"),
    JSON_SERIALIZER(1, "JsonSerializer"),
    KRYO_SERIALIZER(2, "KryoSerializer"),
    HESSIAN_SERIALIZER(3, "HessianSerializer"),
    PROTOSTUFF_SERIALIZER(4, "ProtostuffSerializer"),
    ;


    public final int code;

    public final String name;

}
