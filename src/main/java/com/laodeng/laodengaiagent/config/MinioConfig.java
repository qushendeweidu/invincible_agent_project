package com.laodeng.laodengaiagent.config;

import io.minio.MinioClient;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/30
 * @description MinIO 对象存储配置类
 */
@Log4j2
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        log.info("初始化 MinIO 客户端, endpoint: {}, bucket: {}", endpoint, bucketName);
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
