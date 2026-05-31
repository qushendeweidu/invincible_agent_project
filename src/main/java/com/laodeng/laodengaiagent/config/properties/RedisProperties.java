package com.laodeng.laodengaiagent.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/25 18:19
 * @description
 */
@Data // 使用 Lombok 自动生成 Getter/Setter，这是必须的
@Component // 将该类作为 Bean 注入 Spring 容器
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {

    private String host;
    private int port;

}
