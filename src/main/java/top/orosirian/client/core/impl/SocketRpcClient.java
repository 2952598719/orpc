package top.orosirian.client.core.impl;

import lombok.extern.slf4j.Slf4j;
import top.orosirian.client.core.RpcClient;
import top.orosirian.common.message.RpcRequest;
import top.orosirian.common.message.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Slf4j
public class SocketRpcClient implements RpcClient {

    private static String host = "localhost";

    private static int port = 9999;

    public RpcResponse sendRequest(RpcRequest request){
        try {
            Socket socket=new Socket(host, port);
            ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());

            oos.writeObject(request);
            oos.flush();

            RpcResponse response=(RpcResponse) ois.readObject();
            return response;
        } catch (IOException | ClassNotFoundException e) {
            log.error("出现错误");
            return null;
        }
    }

    @Override
    public void stop() {

    }

}
