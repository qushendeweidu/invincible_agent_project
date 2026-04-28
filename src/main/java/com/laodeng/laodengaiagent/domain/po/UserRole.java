package com.laodeng.laodengaiagent.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/19 18:10
 * @description 用户权限实体类
 */

@Data
@Builder
@Accessors(chain = true)
@TableName(value = "user_role",autoResultMap = true)
public class UserRole {
    /**
     * 主键
     */
    private Long id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 用户权限
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> userRole;
    /**
     * 用户身份
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> rolePermission;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
    /**
     * 逻辑删除
     */
    private Boolean deleted;
    /**
     * 删除时间
     */
    private LocalDateTime deleteTime;
}
