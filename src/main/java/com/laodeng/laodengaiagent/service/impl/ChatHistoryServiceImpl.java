package com.laodeng.laodengaiagent.service.impl;

import com.laodeng.laodengaiagent.domain.po.ChatHistoryMessage;
import com.laodeng.laodengaiagent.respority.ChatHistoryRepository;
import com.laodeng.laodengaiagent.service.ChatHistoryService;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author laodeng
 * @description 对话历史服务实现
 *
 * 这里同时用了两个 MongoDB 操作方式：
 * 1. ChatHistoryRepository（Spring Data 方式）→ 简单的 CRUD、分页查询
 *    类似 MyBatis-Plus 的 BaseMapper，方法名自动生成查询
 *
 * 2. MongoTemplate（模板方式）→ 复杂操作，如部分字段更新
 *    类似 MyBatis 的 XML 手写 SQL，更灵活
 *
 * 为什么撤回用 MongoTemplate 而不用 Repository？
 * 因为 Repository 的 save() 方法是全量替换整个文档，
 * 而撤回只需要更新 withdrawn 和 withdrawnAt 两个字段，
 * MongoTemplate 的 updateFirst() 可以只更新指定字段，更高效
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ChatHistoryServiceImpl implements ChatHistoryService {

    private final ChatHistoryRepository chatHistoryRepository;
    private final MongoTemplate mongoTemplate;

    /**
     * 保存一条对话历史消息
     * @param conversationId 会话ID（和 Redis 中的 memoryId 一致）
     * @param role 角色（USER / ASSISTANT）
     * @param content 消息内容
     * @param source 来源（LOVE / REACT / IMAGE）
     */
    @Override
    public void saveMessage(String conversationId, String role, String content, String source) {
        ChatHistoryMessage chatHistoryMessage = ChatHistoryMessage.builder()
                .conversationId(conversationId)
                .role(role)
                .content(content)
                .timestamp(LocalDateTime.now())
                .withdrawn(false)
                .source(source)
                .build();
        chatHistoryRepository.save(chatHistoryMessage);

    }

    /**
     * 获取某个会话的历史消息
     * @param conversationId 会话ID （和 Redis 中的 memoryId 一致）
     * @param page 页码（从0开始） 默认从0开始
     * @param size 每页条数 默认10
     * @return Page<ChatHistoryMessage> 分页结果
     */
    @Override
    public Page<ChatHistoryMessage> getHistory(String conversationId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "timestamp"));
        return chatHistoryRepository
                .findByConversationIdAndWithdrawnFalseOrderByTimestampAsc(conversationId, pageRequest);
    }

    /**
     * 删除对话消息
     * @param messageId MongoDB 文档ID （MongoDB 文档ID）
     * @return 撤回成功与否
     */
    @Override
    public boolean withdrawMessage(String messageId) {
        // @Id
        // private String id; 查询匹配唯一字段,mongoDB会把id字段作为文档的_id字段且是唯一字段
        Query query = new Query(Criteria.where("_id").is(messageId));
        // 更新撤回状态
        Update update = new Update()
                .set("withdrawn", true)
                .set("withdrawnAt", LocalDateTime.now());
        UpdateResult result = mongoTemplate.updateFirst(query, update, ChatHistoryMessage.class);
        boolean success = result.getModifiedCount() > 0; //这里代表着修改的数据数量判断是否大于零,若是大于零则意味着修改执行了
        log.info("撤回消息: messageId={}, success={}", messageId, success);
        return success;
    }

    /**
     * 根据会话消息删除所有的会话消息
     * @param conversationId 会话ID （和 Redis 中的 memoryId 一致）
     */
    @Override
    public void deleteConversation(String conversationId) {
        // 硬删除：物理删除该会话的所有消息，不可恢复
        chatHistoryRepository.deleteByConversationId(conversationId);
        log.info("删除会话历史: conversationId={}", conversationId);
    }

    /**
     * 获取所有的会话ID列表
     * @return List<String> 会话ID列表
     */
    @Override
    public List<String> getAllConversationIds() {
        // distinct 查询：获取所有不重复的 conversationId
        // 相当于 MySQL 的 SELECT DISTINCT conversation_id FROM chat_history
        return mongoTemplate.findDistinct(
                new Query(), "conversationId", ChatHistoryMessage.class, String.class); //根据 ChatHistoryMessage.class 中的字段 conversationId 查询
    }

    /**
     * 回滚删除最后一条消息
     * @param conversationId 会话ID （和 Redis 中的 memoryId 一致）
     * @param role 角色（USER / ASSISTANT） 删除指定角色的最后一条消息
     */
    @Override
    public void deleteLastMessage(String conversationId, String role) {
        Query query = new Query(Criteria.where("conversationId").is(conversationId)
                .and("role").is(role)
                .and("withdrawn").is(false))
                .with(Sort.by(Sort.Direction.DESC, "timestamp"))
                .limit(1);
        ChatHistoryMessage msg = mongoTemplate.findOne(query, ChatHistoryMessage.class);
        if (msg != null) {
            mongoTemplate.remove(msg);
            log.info("回滚删除消息: conversationId={}, role={}, id={}", conversationId, role, msg.getId());
        }
    }
}