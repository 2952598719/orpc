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
public class RpcResponse implements Serializable {

    private int code;           // 状态码

    private String message;     // 对状态码的解释

    private Class<?> dataType;  // 数据类型

    private Object data;

    public static RpcResponse success(Object data) {
        return RpcResponse.builder()
                            .code(200)
                            .dataType(data.getClass())
                            .data(data)
                            .build();
    }

    public static RpcResponse fail(String msg) {
        return RpcResponse.builder()
                            .code(500)
                            .message(msg)
                            .build();
    }

}
