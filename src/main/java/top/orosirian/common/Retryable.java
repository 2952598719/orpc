package top.orosirian.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 该注解表明某个方法可重试，用于替代原本的白名单
@Retention(RetentionPolicy.RUNTIME)     // 注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Target(ElementType.METHOD)             // 用在方法上
public @interface Retryable {

}