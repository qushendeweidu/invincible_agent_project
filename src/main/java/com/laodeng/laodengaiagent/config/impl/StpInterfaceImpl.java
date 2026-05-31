package com.laodeng.laodengaiagent.config.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laodeng.laodengaiagent.domain.po.UserRole;
import com.laodeng.laodengaiagent.mapper.UserRoleMapper;
import com.laodeng.laodengaiagent.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/20 08:30
 * @description
 */

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    private final UserServiceImpl userService;
    private final UserRoleMapper userRoleMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        UserRole roles = userRoleMapper.selectOne(new LambdaQueryWrapper<>(UserRole.class).eq(UserRole::getUserId, loginId));
        if (Objects.isNull(roles)){
            throw new RuntimeException("用户不存在");
        }
        return roles.getRolePermission();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        UserRole roles = userRoleMapper.selectOne(new LambdaQueryWrapper<>(UserRole.class).eq(UserRole::getUserId, loginId));
        if (Objects.isNull(roles)){
            throw new RuntimeException("用户不存在");
        }
        return roles.getUserRole();

    }
}
