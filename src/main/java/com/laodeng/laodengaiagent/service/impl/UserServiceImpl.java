package com.laodeng.laodengaiagent.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laodeng.laodengaiagent.domain.po.User;
import com.laodeng.laodengaiagent.domain.po.UserRole;
import com.laodeng.laodengaiagent.mapper.UserMapper;
import com.laodeng.laodengaiagent.mapper.UserRoleMapper;
import com.laodeng.laodengaiagent.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/19 20:27
 * @description 用户服务实现类
 */

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 密码
     */
    @Override
    public void register(String username, String password) {
        //校验用户名的唯一性
        Optional<User> optionalUser = new LambdaQueryChainWrapper<>(userMapper)
                .eq(User::getUsername, username)
                .oneOpt();
        if (optionalUser.isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        // 加密密码
        String encryptedPassword = passwordEncoder.encode(password);
        // 保存用户
        User user = User.builder()
                .username(username)
                .password(encryptedPassword)
                .build();
        userMapper.insert(user);
        // 保存用户角色
        userRoleMapper.insert(UserRole.builder()
                .userId(user.getId())
                .userRole(List.of(
                        "user"
                ))
                .rolePermission(List.of(
                        "user.ai_base"
                ))
                .build());
        log.info("用户注册成功:{}", user);
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return token
     */
    @Override
    public String login(String username, String password) {
        String token = null;
        // 查询用户
        User user = new LambdaQueryChainWrapper<>(userMapper)
                .eq(User::getUsername, username)
                .one();
        // 判断是否存在该用户
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 密码验证
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        // 登录
        StpUtil.login(user.getId(), 604800);
        log.info("用户登录成功:{}", user);
        token = StpUtil.getTokenValue();
        return token;
    }

    /**
     * 用户登出
     */
    @Override
    public void logout() {
        StpUtil.logout(StpUtil.getLoginId());
    }

    /**
     * 获取当前用户的的对象实体类信息
     *
     * @return 用户对象
     */
    @Override
    public User getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return userMapper.selectById(userId);

    }

    /**
     * 更新用户信息
     * @param user 用户对象
     */
    @Override
    public void updateUser(User user) {
        Long userId = StpUtil.getLoginIdAsLong();
        if (user.getId() == null){
            throw new RuntimeException("用户id不能为空");
        }
        if (user.getId().equals(userId) || StpUtil.hasRole("admin")){
            userMapper.updateById(user);
        }else {
            throw new RuntimeException("无权限");
        }
    }
}
