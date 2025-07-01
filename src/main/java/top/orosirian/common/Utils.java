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
        // InetSocketAddress会反向解析ip，getHostName就会将ip转换为主机名，而本机可能在host文件中就有docker加进去的127.0.0.1 kubernetes.docker.internal
        // 所以，为了避免变成这样，还是用getHostString获取纯正的host
        return serviceAddress.getHostString() + ":" + serviceAddress.getPort();
    }

    public static InetSocketAddress stringToAddress(String address) {
        String[] split = address.split(":");
        return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
    }

    // TODO:待完善
    public static Class<?> getClassForMessageType(int messageType) {
        System.out.println(messageType);
        return User.class;
    }

    // 直接在本地识别，不通过zookeeper了
    public static boolean checkRetry(Method method) {
        return method.isAnnotationPresent(Retryable.class);
    }

}
