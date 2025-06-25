package top.orosirian.common.serializer.impl;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import top.orosirian.common.SerializeException;
import top.orosirian.common.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("序列化对象不能为空");
        }
        try {
            // obj => HessianOutput->ByteArrayOutputStream => toByteArray
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("反序列化数组不能为空");
        }
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            return hessianInput.readObject();
        } catch (IOException e) {
            throw new SerializeException("反序列化失败");
        }
    }

    @Override
    public int getType() {
        return 3;
    }
}
