package com.laodeng.laodengaiagent.utils;

import com.laodeng.laodengaiagent.rag.ragTools.QueryRewriterTool;
import com.laodeng.laodengaiagent.register.DynamicChatModelRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/7 18:59
 * @description 自定义重写的查询转换工具包
 */

@Log4j2
@Component
public class MyTransformerUtils {
    private final QueryRewriterTool queryRewriterTool;
    private final String reWritePrompt = """
            请根据以下问题直接回答，不要在回答中显示"重写后的问题"或"原始问题"等内容，直接给出答案：
            {query}
            要求：{target}，必须用中文回答。
            """;

    public MyTransformerUtils(
            DynamicChatModelRegistry dynamicChatModelRegistry
    ) {
        ChatClient.Builder chatClientBuilder = ChatClient.builder(dynamicChatModelRegistry.getModel("multiple"));
        this.queryRewriterTool = new QueryRewriterTool(chatClientBuilder);
    }

    /**
     * 当前查询转换工具为实现一个重写用户输入内容的工具类
     * @param input 用户输入内容
     * @return 重写后的用户输入内容
     */
    public String reWriteTransformer(String input) {
        PromptTemplate promptTemplate = PromptTemplate.builder()
            .template(reWritePrompt)
            .variables(Map.of("query", input, "target", "分析用户意图回答问题"))
            .build();
        log.info("重写后的用户输入内容: {}", input);
        return promptTemplate.render();
    }

    /**
     * 多轮会话的查询转换工具
     * @param input 用户输入内容
     * @return 重写后的用户输入内容
     */
    public String reWriteMultipleTransformer(String input) {
        String afterMultiple = queryRewriterTool.doQueryRewrite(input);
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(reWritePrompt)
                .variables(Map.of("query", afterMultiple, "target", "分析用户意图回答问题"))
                .build();
        log.info("重写后的用户输入内容: {}", afterMultiple);
        return promptTemplate.render();
    }

}
