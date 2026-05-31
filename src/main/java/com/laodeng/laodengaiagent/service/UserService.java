package com.laodeng.laodengaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laodeng.laodengaiagent.domain.po.User;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/19 20:27
 * @description
 */

public interface UserService extends IService<User> {

    void register(String username, String password);
    String login(String username, String password);
    void logout();
    User getCurrentUser();
    void updateUser(User user);

}
