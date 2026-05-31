package com.laodeng.laodengaiagent.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/18 10:58
 * @description 配置超时
 */

@Configuration
public class OpenAiConfig {
    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> {
            restClientBuilder.requestFactory(new SimpleClientHttpRequestFactory() {{
                // 连接超时：握手阶段 10s 足够
                setConnectTimeout(10 * 1000);
                // ⚠️ 读取超时：本地大模型生成非常慢，给 30 分钟
                // （与 DynamicChatModelRegistry 保持一致）
                setReadTimeout(30 * 60 * 1000);
            }});
        };
    }
}
