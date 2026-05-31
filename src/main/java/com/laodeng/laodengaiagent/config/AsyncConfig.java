package com.laodeng.laodengaiagent.config;



import org.apache.tomcat.util.threads.VirtualThreadExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/24 20:49
 * @description
 */

@EnableAsync
@Configuration
public class AsyncConfig {

    /**
     * TTS 异步执行器,用于处理 TTS 请求
     */
    @Bean("ttsExecutor")
    public Executor ttsExecutor() {
        return new VirtualThreadTaskExecutor("tts-");
    }

    @Bean
    public VirtualThreadExecutor virtualThreadExecutor() {
        return new VirtualThreadExecutor("react-agent-");
    }
}
