package top.orosirian.common;

import top.orosirian.test.common.pojo.User;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

public class Utils {

    // 得到方法签名，格式为：接口名#方法名(参数类型1,参数类型2,参数类型3)
    public static String getMethodSignature(Class<?> clazz, Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(clazz.getName()).append("#").append(method.getName()).append("(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(parameterTypes[i].getName());
            if (i < parameterTypes.length - 1) {
                sb.append(",");
            } else{
                sb.append(")");
            }
        }
        return sb.toString();
    }

    public static String addressToString(InetSocketAddress serviceAddress) {
        return serviceAddress.getHostName() + ":" + serviceAddress.getPort();
    }

    public static InetSocketAddress stringToAddress(String address) {
        String[] split = address.split(":");
        return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
    }

    // TODO:待完善
    public static Class<?> getClassForMessageType(int messageType) {
        return User.class;
    }

}
