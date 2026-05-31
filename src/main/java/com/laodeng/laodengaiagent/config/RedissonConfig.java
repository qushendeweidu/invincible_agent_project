package com.laodeng.laodengaiagent.config;

import com.laodeng.laodengaiagent.config.properties.RedisProperties;
import com.laodeng.laodengaiagent.factory.RedisTemplateFactory;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/23 14:38
 * @description
 */

@Configuration
@RequiredArgsConstructor
public class RedissonConfig {

    private final RedisProperties redisProperties;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort())
                .setDatabase(RedisTemplateFactory.BizDatabase.REACT_AGENT_MEMORY.getDbIndex());
        return Redisson.create(config);
    }
}