package com.laodeng.laodengaiagent.config;

import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/23 22:16
 * @description
 */

@Log4j2
@Configuration
public class SkillsConfig {

    @Bean
    public ClasspathSkillRegistry skillRegistry() {
        ClasspathSkillRegistry registry = ClasspathSkillRegistry.builder()
                .classpathPath("skills")
                .build();
        log.info("==== Skills ====");
        registry.listAll().forEach(log::info);
        return registry;
    }

}
