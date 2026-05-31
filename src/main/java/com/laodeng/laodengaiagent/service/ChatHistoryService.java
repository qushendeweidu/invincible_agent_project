package com.laodeng.laodengaiagent.service;

import com.laodeng.laodengaiagent.domain.po.ChatHistoryMessage;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author laodeng
 * @description 对话历史服务（MongoDB 长时记忆）
 */
public interface ChatHistoryService {

    /**
     * 保存一条对话消息到 MongoDB 长时记忆
     * @param conversationId 会话ID（和 Redis 中的 memoryId 一致）
     * @param role 角色（USER / ASSISTANT）
     * @param content 消息内容
     * @param source 来源（LOVE / REACT / IMAGE）
     */
    void saveMessage(String conversationId, String role, String content, String source);

    /**
     * 分页查询对话历史
     * @param conversationId 会话ID
     * @param page 页码（从0开始）
     * @param size 每页条数
     * @return 分页结果
     */
    Page<ChatHistoryMessage> getHistory(String conversationId, int page, int size);

    /**
     * 撤回指定消息（软删除：不物理删除，只标记 withdrawn=true）
     * @param messageId MongoDB 文档ID
     * @return 是否撤回成功
     */
    boolean withdrawMessage(String messageId);

    /**
     * 删除整个会话历史（硬删除）
     * @param conversationId 会话ID
     */
    void deleteConversation(String conversationId);

    /**
     * 获取所有会话ID列表（用于前端展示会话列表）
     * @return 会话ID列表
     */
    List<String> getAllConversationIds();

    /**
     * 删除某个会话中最后一条指定角色的消息（用于错误回滚）
     * @param conversationId 会话ID
     * @param role 角色（USER / ASSISTANT）
     */
    void deleteLastMessage(String conversationId, String role);
}
