package com.laodeng.laodengaiagent.domain.po;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * @author laodeng
 * @description MongoDB 对话历史文档实体
 *
 * 对比 AiModelConfig（MySQL实体）的区别：
 * - AiModelConfig 用 @TableName("ai_model_config") → MyBatis-Plus 注解，映射到 MySQL 表
 * - ChatHistoryMessage 用 @Document("chat_history") → Spring Data MongoDB 注解，映射到 MongoDB 集合
 *
 * MySQL 实体需要 @TableId 指定主键，MongoDB 用 @Id 标注，值是 ObjectId 字符串（如 "665f1a2b3c4d5e6f7a8b9c0d"）
 */
@Data
@Accessors(chain = true)  // 和你的 AiModelConfig 一样，支持链式调用 setXxx().setYyy()
@Document("chat_history")  // 指定 MongoDB 集合名，相当于 MySQL 的表名
@Builder
public class ChatHistoryMessage {

    /**
     * MongoDB 文档主键
     * 和 MySQL 的自增 Long id 不同，MongoDB 的 @Id 默认是 ObjectId 字符串
     * 格式类似 "665f1a2b3c4d5e6f7a8b9c0d"，由 MongoDB 自动生成，全局唯一
     */
    @Id
    private String id;

    /**
     * 会话ID，和 Redis 中的 conversationId/memoryId 对应
     * @Indexed 会在 MongoDB 中自动创建索引，相当于 MySQL 的 CREATE INDEX
     * 加索引后按 conversationId 查询速度大幅提升
     */
    @Indexed
    private String conversationId;

    /**
     * 消息角色：USER（用户发的）/ ASSISTANT（AI回复的）/ SYSTEM（系统消息）
     * 对应 Spring AI 的 MessageType 枚举
     */
    private String role;

    /**
     * 消息内容，就是用户说的话或 AI 回复的内容
     * MongoDB 没有 TEXT/VARCHAR 区分，String 字段没有长度限制（最大16MB）
     */
    private String content;

    /**
     * 消息时间戳
     * @Indexed 创建索引，支持按时间排序查询
     */
    @Indexed
    private LocalDateTime timestamp;

    /**
     * 是否已撤回（软删除标记）
     * false = 正常消息，true = 已撤回
     * 撤回后前端不展示，但数据还在 MongoDB 中，可以恢复
     */
    private boolean withdrawn;

    /**
     * 撤回时间，只有 withdrawn=true 时才有值
     */
    private LocalDateTime withdrawnAt;

    /**
     * 消息来源，区分消息来自哪个 App
     * "LOVE" = LoveApp 的对话
     * "REACT" = ReactAgentApp 的多智能体对话
     * "IMAGE" = ImageApp 的图片分析
     */
    private String source;
}