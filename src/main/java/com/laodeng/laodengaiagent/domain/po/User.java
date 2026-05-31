package com.laodeng.laodengaiagent.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/19 17:12
 * @description 用户实体类
 */

@Data
@Builder
@Accessors(chain = true)
@TableName(value = "user", autoResultMap = true)
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键id
     */
    private Long id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
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
