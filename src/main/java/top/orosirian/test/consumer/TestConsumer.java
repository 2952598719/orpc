package top.orosirian.test.consumer;

import lombok.extern.slf4j.Slf4j;
import top.orosirian.client.proxy.ClientProxy;
import top.orosirian.test.common.pojo.User;
import top.orosirian.test.common.service.UserService;

@Slf4j
public class TestConsumer {

    public static void main(String[] args) {
        ClientProxy clientProxy = new ClientProxy();
        UserService proxy = clientProxy.getProxy(UserService.class);
        
        User user = proxy.getUserByUserId(1);
        log.info("从服务端得到的user=" + user);

        User u = User.builder().id(100).userName("wxx").sex(true).build();
        Integer id = proxy.insertUser(u);
        log.info("向服务端插入user的id " + id);

        clientProxy.stop();
    }
}
