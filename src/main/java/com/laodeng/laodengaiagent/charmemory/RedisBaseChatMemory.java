package com.laodeng.laodengaiagent.charmemory;

import com.laodeng.laodengaiagent.factory.RedisTemplateFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/25 18:09
 * @description Redis 存储的会话记忆
 */

@Slf4j
@Component
public class RedisBaseChatMemory implements ChatMemory {
    private final RedisTemplate<String, Message> redisTemplate;
    public RedisBaseChatMemory(RedisTemplateFactory<Message> redisTemplateFactory) {
        redisTemplate = redisTemplateFactory.createTemplate(RedisTemplateFactory.BizDatabase.CHAT_MEMORY);
    }

    @Override
    public void add(@NotNull String conversationId, @NotNull Message message) {
        saveConversation(conversationId, List.of(message));
    }

    @Override
    public void add(@NotNull String conversationId, @NotNull List<Message> messages) {
        saveConversation(conversationId, messages);
    }

    @NotNull
    @Override
    public List<Message> get(@NotNull String conversationId) {
        return getConversation(conversationId);
    }

    @Override
    public void clear(@NotNull String conversationId) {
        deleteConversation(conversationId);
    }

    public void saveConversation(String conversationId, List<Message> messages) {
        // 2. 批量保存
        redisTemplate.opsForList().rightPushAll(conversationId, messages);
        log.info("保存会话: {}", conversationId);
    }

    public List<Message> getConversation(String conversationId) {
        // 2. 批量获取
        return redisTemplate.opsForList().range(conversationId, 0, -1);
    }

    public void deleteConversation(String conversationId) {
        // 删除会话
        redisTemplate.delete(conversationId);
    }

    public void removeLastN(String conversationId, int n) {
        for (int i = 0; i < n; i++) {
            redisTemplate.opsForList().rightPop(conversationId);
        }
        log.info("从Redis移除最后{}条记忆: conversationId={}", n, conversationId);
    }


}
