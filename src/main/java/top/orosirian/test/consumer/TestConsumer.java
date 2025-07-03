package top.orosirian.test.consumer;

import lombok.extern.slf4j.Slf4j;
import top.orosirian.client.proxy.ClientProxy;
import top.orosirian.test.common.pojo.User;
import top.orosirian.test.common.service.UserService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class TestConsumer {

    private static final int THREAD_POOL_SIZE = 20;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);


    public static void main(String[] args) throws InterruptedException {
        ClientProxy clientProxy = new ClientProxy();
        UserService proxy = clientProxy.getProxy(UserService.class);
        for (int i = 1; i < 120; i++) {
            final Integer i1 = i;
            if (i % 30 == 0) {
                // Simulate delay for every 30 requests
                Thread.sleep(10000);
            }

            // Submit tasks to executor service (thread pool)
            executorService.submit(() -> {
                try {
                    User user = proxy.getUserByUserId(i1);
                    if (user != null) {
                        log.info("从服务端得到的user={}", user);
                    } else {
                        log.warn("获取的 user 为 null, userId={}", i1);
                    }

                    Integer id = proxy.insertUser(User.builder()
                            .id(i1)
                            .userName("User" + i1)
                            .sex(true)
                            .build());

                    if (id != null) {
                        log.info("向服务端插入user的id={}", id);
                    } else {
                        log.warn("插入失败，返回的id为null, userId={}", i1);
                    }
                } catch (Exception e) {
                    log.error("调用服务时发生异常，userId={}", i1, e);
                }
            });
        }

        // Gracefully shutdown the executor service
        executorService.shutdown();
        clientProxy.stop();

    }

}
