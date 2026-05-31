package com.laodeng.laodengaiagent.respority;

import com.laodeng.laodengaiagent.domain.po.ChatHistoryMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/1 19:55
 * @description MongoDB实现的聊天记里的Mapper层
 */

@Component
public interface ChatHistoryRepository extends MongoRepository<ChatHistoryMessage, String> {

    /**
     * 按会话ID分页查询未撤回的消息，按时间升序
     *
     * @param conversationId 会话ID
     * @param pageable       分页参数（页码、每页条数、排序）
     * @return Page<ChatHistoryMessage> 分页结果，包含总数、总页数等信息
     */
    Page<ChatHistoryMessage> findByConversationIdAndWithdrawnFalseOrderByTimestampAsc(
            String conversationId, Pageable pageable);

    /**
     * 查询某个会话的所有未撤回消息（不分页，用于导出等场景）
     *
     * @param conversationId 会话ID
     * @return List<ChatHistoryMessage> 消息列表
     */
    List<ChatHistoryMessage> findByConversationIdAndWithdrawnFalseOrderByTimestampAsc(
            String conversationId);

    /**
     * 删除整个会话的所有消息（硬删除）
     *
     * @param conversationId 会话ID
     */
    void deleteByConversationId(String conversationId);

    /**
     * 统计会话中未撤回的消息数量
     *
     * @param conversationId 会话ID
     * @return 消息数量
     */
    long countByConversationIdAndWithdrawnFalse(String conversationId);


}
