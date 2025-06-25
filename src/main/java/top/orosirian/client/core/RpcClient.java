package top.orosirian.client.core;

import top.orosirian.common.message.RpcRequest;
import top.orosirian.common.message.RpcResponse;

public interface RpcClient {

    RpcResponse sendRequest(RpcRequest request);

}
