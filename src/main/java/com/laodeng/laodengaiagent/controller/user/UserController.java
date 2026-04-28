package com.laodeng.laodengaiagent.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.laodeng.laodengaiagent.common.R;
import com.laodeng.laodengaiagent.domain.po.User;
import com.laodeng.laodengaiagent.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/20 09:35
 * @description 用户相关接口类
 */

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @return  token
     */
    @PostMapping("/register")
    public R<String> register(@RequestParam String username, @RequestParam String password) {
        userService.register(username, password);
        return R.ok("注册成功");
    }

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return  token
     */
    @PostMapping("/login")
    public R<String> login(@RequestParam String username, @RequestParam String password) {
        String token = userService.login(username, password);
        return R.ok(token);
    }

    /**
     * 用户登出
     * @return 登出成功
     */
    @SaCheckLogin
    @PostMapping("/logout")
    public R<String> logout() {
        userService.logout();
        return R.ok("已登出");
    }

    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    @SaCheckLogin
    @GetMapping("/info")
    public R<User> info() {
        User user = userService.getCurrentUser();
        user.setPassword(null); // 脱敏，不返回密码
        return R.ok(user);
    }

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新成功
     */
    @SaCheckLogin
    @PostMapping("/update")
    public R<String> update(@RequestBody User user) {
        userService.updateUser(user);
        return R.ok("更新成功");
    }


}
