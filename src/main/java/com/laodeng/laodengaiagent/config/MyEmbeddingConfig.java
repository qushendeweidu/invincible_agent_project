package com.laodeng.laodengaiagent.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laodeng.laodengaiagent.domain.po.AiModelConfig;
import com.laodeng.laodengaiagent.service.AiModelConfigService;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/2 14:59
 * @description 恋爱大师向量数据库配置(初始化基于内存的向量数据库Bean)
 */

@Log4j2
@Configuration
public class MyEmbeddingConfig {
    private final AiModelConfig aiModelConfig;


    public MyEmbeddingConfig(AiModelConfigService aiModelConfigService) {
        LambdaQueryWrapper<AiModelConfig> query = new LambdaQueryWrapper<>();
        query.eq(AiModelConfig::getConfigKey, "embedding");
        this.aiModelConfig = aiModelConfigService.getOne(query);
    }

    @Bean
    public EmbeddingModel loveAppEmbeddingModel() {
        // 1. 获取动态配置
        String apiKey = aiModelConfig.getApiKey();
        String baseUrl = aiModelConfig.getBaseUrl();
        String modelName = aiModelConfig.getModelName();
        String embeddingsPath = aiModelConfig.getCompletionsPath();

        // 2. 初始化底层 API 客户端
        OpenAiApi openAiApi = OpenAiApi.builder()
                .embeddingsPath(embeddingsPath)
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        // 3. 构建 EmbeddingModel
        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder().model(modelName).build(),
                RetryUtils.DEFAULT_RETRY_TEMPLATE); // 加上这个参数
    }

}