package top.orosirian.test.provider.service;

import top.orosirian.test.common.pojo.User;
import top.orosirian.test.common.service.UserService;

import java.util.Random;
import java.util.UUID;

public class UserServiceImpl implements UserService {

    @Override
    public User getUserByUserId(Integer id) {
        System.out.println("[] 客户端查询了用户" + id);
        // 下面的代码模拟从数据库中取出的步骤
        Random random = new Random();
        return User.builder()
                .userName(UUID.randomUUID().toString())
                .id(id)
                .sex(random.nextBoolean())
                .build();
    }

    @Override
    public Integer insertUser(User user) {
        System.out.println("[] 插入数据成功：" + user.getUserName());
        return user.getId();
    }

}
