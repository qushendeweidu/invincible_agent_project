package com.laodeng.laodengaiagent;

import com.laodeng.laodengaiagent.config.properties.RedisProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RedisProperties.class)
@MapperScan("com.laodeng.laodengaiagent.mapper")
public class LaodengAiAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(LaodengAiAgentApplication.class, args);
	}

}
