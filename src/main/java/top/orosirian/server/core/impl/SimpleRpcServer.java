package top.orosirian.server.core.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.common.message.RpcRequest;
import top.orosirian.common.message.RpcResponse;
import top.orosirian.server.core.RpcServer;
import top.orosirian.server.register.impl.ZKServiceRegister;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class SimpleRpcServer implements RpcServer {

    private static int port = 9999;

    private ZKServiceRegister zkServiceRegister;

    private static volatile SimpleRpcServer instance = null;

    private SimpleRpcServer() {
        zkServiceRegister = ZKServiceRegister.getInstance();
    }

    public static SimpleRpcServer getInstance() {
        if (instance == null) {
            synchronized (SimpleRpcServer.class) {
                if (instance == null) {
                    instance = new SimpleRpcServer();
                }
            }
        }
        return instance;
    }


    @Override
    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            log.info("服务器已启动");
            while(true) {
                Socket socket = serverSocket.accept();
                new Thread(new WorkThread(socket, zkServiceRegister)).start();
            }
        } catch (IOException e) {
            log.error("出现错误");
        }
    }

    @Override
    public void stop() {

    }
}


@Slf4j
@AllArgsConstructor
class WorkThread implements Runnable {

    private Socket socket;
    private ZKServiceRegister serviceProvide;
    @Override
    public void run() {
        try {
            ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
            //读取客户端传过来的request
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();
            //反射调用服务方法获取返回值
            RpcResponse rpcResponse=getResponse(rpcRequest);
            //向客户端写入response
            oos.writeObject(rpcResponse);
            oos.flush();
        } catch (IOException | ClassNotFoundException e) {
            log.error("错误");
        }
    }
    private RpcResponse getResponse(RpcRequest rpcRequest){
        //得到服务名
        String interfaceName=rpcRequest.getInterfaceName();
        //得到服务端相应服务实现类
        Object service = serviceProvide.getService(interfaceName);
        //反射调用方法
        Method method=null;
        try {
            method= service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            Object invoke=method.invoke(service,rpcRequest.getParams());
            return RpcResponse.success(invoke);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("方法执行错误");
            return RpcResponse.fail("失败");
        }
    }

}