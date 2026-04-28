package com.laodeng.laodengaiagent.config;

import com.laodeng.laodengaiagent.intercetor.JWTInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/18 11:31
 * @description 跨域配置
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final JWTInterceptor jwtInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 覆盖所有请求
                .allowedOriginPatterns("*") // 允许所有域名（生产环境建议指定具体域名）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许所有请求方法
                .allowedHeaders("*") // 允许所有请求头
                .exposedHeaders("*") // 暴露所有请求头
                .allowCredentials(true); // 允许携带 Cookie
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/error",
                        "/user/login",
                        "/user/register",
                        "/health",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/doc.html",
                        "/webjars/**"
                );
    }

    /**
     * 配置异步支持
     * @param configurer
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 创建一个支持虚拟线程的简易异步执行器
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        // 开启虚拟线程
        executor.setVirtualThreads(true);
        executor.setThreadNamePrefix("web-");

        // 将其设置为 Web MVC 异步处理的默认执行器
        configurer.setTaskExecutor(executor);
        // 设置超时时间（可选，针对长连接 SSE 建议设置长一点，如 60 秒）
        configurer.setDefaultTimeout(60_000);
    }
}
