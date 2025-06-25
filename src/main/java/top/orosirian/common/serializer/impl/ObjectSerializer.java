package top.orosirian.common.serializer.impl;


import top.orosirian.common.SerializeException;
import top.orosirian.common.serializer.Serializer;

import java.io.*;

public class ObjectSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("序列化对象不能为空");
        }
        byte[] bytes;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();                // 将oos所有数据刷到bos中
            bytes = bos.toByteArray();  // 将bos内部缓冲区数据转换为字节数组
            bos.close();
            oos.close(); 
        } catch(IOException e) {
            throw new SerializeException("序列化失败");
        }
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("反序列化数组不能为空");
        }
        Object obj;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            bis.close();
            ois.close();
        } catch(IOException | ClassNotFoundException e) {
            throw new SerializeException("反序列化失败");
        }
        return obj;
    }

    @Override
    public int getType() {
        return 0;
    }
    
}
