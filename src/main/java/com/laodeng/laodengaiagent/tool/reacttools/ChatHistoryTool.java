package com.laodeng.laodengaiagent.tool.reacttools;


import com.laodeng.laodengaiagent.annotition.ToolMetadata;
import com.laodeng.laodengaiagent.config.McpToolsConfig;
import com.laodeng.laodengaiagent.domain.po.ChatHistoryMessage;
import com.laodeng.laodengaiagent.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.function.BiFunction;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/4 21:39
 * @description 当前是长时持久化记忆存储工具由AI只能调用填入参数
 */

@Log4j2
@Component
@ToolMetadata(maxResultChars = 2000, concurrencySafe = true)
@RequiredArgsConstructor
public class ChatHistoryTool implements BiFunction<McpToolsConfig.ChatHistoryRequest, ToolContext, String> {
    private final ChatHistoryService chatHistoryService;

    @Override
    public String apply(McpToolsConfig.ChatHistoryRequest request, ToolContext toolContext) {
        try{
            String conversationId = request.conversationId();
            int page = request.page() != null ? request.page() : 0;
            int size = request.size() != null ? request.size() : 20;

            log.info("查询对话历史: conversationId={}, page={}, size={}", conversationId, page, size);

            Page<ChatHistoryMessage> historyMessages = chatHistoryService.getHistory(conversationId, page, size);

            if (historyMessages.isEmpty()) {
                throw new RuntimeException("对话里是为空");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("共 ").append(historyMessages.getTotalElements()).append(" 条记录，当前第 ")
                    .append(page + 1).append("/").append(historyMessages.getTotalPages()).append(" 页：\n\n");

            for (ChatHistoryMessage msg : historyMessages.getContent()) {
                sb.append("[").append(msg.getRole()).append("] ")
                        .append(msg.getTimestamp().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")))
                        .append("\n").append(msg.getContent()).append("\n\n");
            }
            int maxChars = this.getClass().getAnnotation(ToolMetadata.class).maxResultChars();
            String history = truncateResult(sb.toString(), maxChars);
            log.info("历史对话查询结果为>>> {}", history);
            return history;
        } catch (Exception e) {
            log.error("查询对话历史失败", e);
            return "查询对话历史失败: " + e.getMessage();
        }
    }

    // 截断工具调用的时候截断结果，避免返回内容过长导致模型崩溃
    private String truncateResult(String content, int maxChars) {
        if (content.length() <= maxChars) return content;
        return content.substring(0, maxChars)
                + "\n\n...[内容过长，已截断，共 " + content.length() + " 字符，仅显示前 " + maxChars + " 字符]";
    }
}
