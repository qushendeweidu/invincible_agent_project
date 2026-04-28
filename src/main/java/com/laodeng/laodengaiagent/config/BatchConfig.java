package com.laodeng.laodengaiagent.config;

import com.knuddels.jtokkit.api.EncodingType;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/7 17:42
 * @description 批次配置
 */

@Log4j2
@Configuration
public class BatchConfig {

    @Bean
    public BatchingStrategy batchingStrategy() {
        // 参数 1: 最大 token 数（默认 8192）
        // 参数 2: 保留百分比（默认 0.1，即保留 10% 余量）
        return new TokenCountBatchingStrategy(
                EncodingType.CL100K_BASE,  // 编码类型
                7000,                      // 最大 token 数
                0.2                        // 保留 20%
        );
    }
}
