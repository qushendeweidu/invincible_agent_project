package com.laodeng.laodengaiagent.app;

import com.laodeng.laodengaiagent.advisor.MyReReadingAdvisor;
import com.laodeng.laodengaiagent.charmemory.RedisBaseChatMemory;
import com.laodeng.laodengaiagent.register.DynamicChatModelRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/26 19:36
 * @description 图片识别应用
 */

@Log4j2
@Component
public class ImageApp {
    private final ChatClient client;

    @Value("classpath:/prompt/ImageAssistantPrompt.st")
    private Resource IMAGEASSISTANT_PROMPT;
    public static String MODEL_NAME = "multiple";

    public ImageApp(
            DynamicChatModelRegistry registry
    ) {
        ChatModel chatModel = registry.getModel(MODEL_NAME);
        ChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();
        //初始化基于内存的的对话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
        client = ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        SimpleLoggerAdvisor.builder().build(),
                        new MyReReadingAdvisor()
                )
                .build();
    }
    //jdk14之后的新特性支持直接创建记录,创建的记录类用于格式化输出对于图片人物的动作和详细理解
    public record ContentUnderstand(String originallyContent, String understandContent) {
    }

    /**
     * 图片识别
     * @param msg 用户输入消息
     * @param memoryId 记忆id
     * @param picturePath 照片地址
     * @return
     */

    public ContentUnderstand promptChat(String msg, String memoryId,String picturePath){
        ContentUnderstand response = client.prompt()
                .system(IMAGEASSISTANT_PROMPT)
                .user(u -> u.text(msg)
                        .media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource(picturePath)))
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, memoryId))
                .call()
                .entity(ContentUnderstand.class);
        log.info("response: {}", response);
        return response;
    }

    public String imageChat(String msg, String memoryId,String picturePath){
        ChatClient.CallResponseSpec callResponseSpec = client.prompt()
                .system(IMAGEASSISTANT_PROMPT)
                .user(u -> u.text(msg)
                        .media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource(picturePath)))
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, memoryId))
                .call();
        return callResponseSpec.content();
    }
}
