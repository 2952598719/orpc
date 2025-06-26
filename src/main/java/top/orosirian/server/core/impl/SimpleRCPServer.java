package top.orosirian.server.core.impl;

import lombok.extern.slf4j.Slf4j;
import top.orosirian.common.message.RpcRequest;
import top.orosirian.common.message.RpcResponse;
import top.orosirian.server.core.RpcServer;
import top.orosirian.server.register.ServiceRegister;
import top.orosirian.server.register.impl.ZKServiceRegister;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class SimpleRCPServer implements RpcServer {

    @Override
    public void start(int port) {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[] 服务器已启动");
            while(true) {
                Socket socket = serverSocket.accept();  // 阻塞到有连接
                new Thread(new WorkThread(socket)).start();
            }
        } catch(IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void stop() {

    }
}


@Slf4j
class WorkThread implements Runnable {

    private final Socket socket;

    private final ServiceRegister serviceRegister = ZKServiceRegister.getInstance();

    public WorkThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            //读取客户端传过来的request
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();
            //反射调用服务方法获取返回值
            RpcResponse rpcResponse = getResponse(rpcRequest);
            //向客户端写入response
            oos.writeObject(rpcResponse);
            oos.flush();
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    private RpcResponse getResponse(RpcRequest rpcRequest) {
        //得到服务名
        String interfaceName = rpcRequest.getInterfaceName();
        //得到服务端相应服务实现类
        Object service = serviceRegister.getService(interfaceName);
        //反射调用方法
        Method method;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            Object invoke = method.invoke(service, rpcRequest.getParams());
            return RpcResponse.success(invoke);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage(), e);
            return RpcResponse.fail("方法执行错误");
        }
    }
}