package top.orosirian.common.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {

    private String interfaceName;   // 服务类的名字

    private String methodName;      // 调用的方法名

    private Object[] params;        // 参数列表

    private Class<?>[] paramsType;  // 参数类型
    
}
