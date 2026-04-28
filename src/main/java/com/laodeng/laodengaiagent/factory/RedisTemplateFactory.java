package com.laodeng.laodengaiagent.factory;


import com.laodeng.laodengaiagent.config.properties.RedisProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/25 18:32
 * @description 结合枚举与 Kryo 线程安全工厂的多库 RedisTemplate 工厂
 */

@Log4j2
@Component
@RequiredArgsConstructor
public class RedisTemplateFactory<T> {

    private final KryoRedisSerializerFactory<T> kryoSerializer;
    private final RedisProperties redisProperties;

    /**
     * 内置库枚举，用于控制 Database 索引
     */
    @Getter
    @RequiredArgsConstructor
    public enum BizDatabase {
        CHAT_MEMORY(0, "ChtaClient对话记忆库"),
        REACT_AGENT_MEMORY(1, "ReactAgent对话记忆库");

        private final int dbIndex;
        private final String description;
    }

    /**
     * 创建并配置 RedisTemplate
     *
     * @param bizDatabase 业务枚举（决定使用哪个库）
     * @return 配置好的 RedisTemplate
     */
    public RedisTemplate<String, T> createTemplate(BizDatabase bizDatabase) {
        log.info("正在为 [{}] 创建 RedisTemplate，目标 Database: {}",
                bizDatabase.getDescription(), bizDatabase.getDbIndex());

        // 1. 配置 Redis 独立连接
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getHost());
        config.setPort(redisProperties.getPort());
        config.setDatabase(bizDatabase.getDbIndex());

        // 2. 创建连接工厂（Lettuce）
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();

        // 3. 实例化 Template
        RedisTemplate<String, T> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 4. 设置序列化器
        // Key 统一使用 String 序列化
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());

        // Value 使用你提供的 Kryo 工厂获取序列化器
        template.setValueSerializer(kryoSerializer);
        template.setHashValueSerializer(kryoSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
