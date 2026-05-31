package com.laodeng.laodengaiagent.register;

import com.laodeng.laodengaiagent.domain.po.AiModelConfig;
import com.laodeng.laodengaiagent.event.ModelConfigChangedEvent;
import com.laodeng.laodengaiagent.service.AiModelConfigService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/27 11:55
 * @description
 */

@Log4j2
@Component
@RequiredArgsConstructor
public class DynamicChatModelRegistry {

    private final AiModelConfigService configService;
    private final Map<String, ChatModel> modelMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        configService.lambdaQuery()
                .eq(AiModelConfig::getEnabled, true)
                .list().forEach(this::register);
        log.info("已加载 {} 个 ChatModel", modelMap.size());
    }

    @EventListener
    public void handleConfigUpdate(ModelConfigChangedEvent event) {
        if (event.isDeleted()) {
            this.remove(event.getConfigKey());
        } else {
            AiModelConfig newConfig = configService.getById(event.getConfigId());
            if (newConfig != null) {
                this.register(newConfig);
            }
        }
    }

    public void register(AiModelConfig config) {
        modelMap.put(config.getConfigKey(), buildModel(config));
        log.info("注册 ChatModel: key={}, model={}", config.getConfigKey(), config.getModelName());
    }

    public void remove(String configKey) {
        modelMap.remove(configKey);
    }

    public ChatModel getModel(String configKey) {
        ChatModel model = modelMap.get(configKey);
        if (model == null) throw new IllegalArgumentException("未找到 ChatModel: " + configKey);
        return model;
    }

    public Set<String> listKeys() {
        return modelMap.keySet();
    }

    private ChatModel buildModel(AiModelConfig config) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(50 * 1000);
        requestFactory.setReadTimeout(300 * 1000);
        RestClient.Builder restClientBuilder = RestClient.builder().requestFactory(requestFactory);

        OpenAiApi.Builder builder = OpenAiApi.builder()
                .baseUrl(StringUtils.hasText(config.getBaseUrl()) ? config.getBaseUrl() : "http://localhost:11434/v1")
                .apiKey(StringUtils.hasText(config.getApiKey()) ? config.getApiKey() : "placeholder")
                .restClientBuilder(restClientBuilder);

        if (StringUtils.hasText(config.getCompletionsPath())) {
            builder.completionsPath(config.getCompletionsPath());
        }

        return OpenAiChatModel.builder()
                .openAiApi(builder.build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(config.getModelName())
                        .temperature(config.getTemperature())
                        .build())
                .build();
    }
}