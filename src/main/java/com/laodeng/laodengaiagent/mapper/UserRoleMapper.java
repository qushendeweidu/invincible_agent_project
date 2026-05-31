package com.laodeng.laodengaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laodeng.laodengaiagent.domain.po.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/19 20:25
 * @description 用户权限Mapper
 */

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
