package top.orosirian.test.common.service;

import top.orosirian.common.Retryable;
import top.orosirian.test.common.pojo.User;

public interface UserService {

    @Retryable
    User getUserByUserId(Integer id);

    @Retryable
    Integer insertUser(User user);

}
