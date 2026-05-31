package com.laodeng.laodengaiagent.tool.reacttools;

import com.laodeng.laodengaiagent.annotition.ToolMetadata;
import com.laodeng.laodengaiagent.config.McpToolsConfig;
import com.laodeng.laodengaiagent.register.DynamicChatModelRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/4/15 13:52
 * @description
 */

@Log4j2
@Component
@ToolMetadata(concurrencySafe = true)
@RequiredArgsConstructor
public class RAGReadTool implements BiFunction<McpToolsConfig.RAGRequest, ToolContext, String>{
    private final DynamicChatModelRegistry registry;
    private final VectorStore vectorStore;

    @Override
    public String apply(McpToolsConfig.RAGRequest ragRequest, ToolContext toolContext) {
        log.info("开始调用RAGReadTool>>>>>>>> {}", ragRequest);
        ChatModel chatModel = registry.getModel("multiple");
        ChatClient chatClient = ChatClient.builder(chatModel).build();
        String userQuery = ragRequest.userInput(); // 用户输入
        String conversationId = ragRequest.conversationId(); // 会话ID
        // 直接调用已经写好的RAG智能体APP
        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryAugmenter(
                        ContextualQueryAugmenter.builder()
                                .allowEmptyContext(true) // 允许为空,也就是即使模型没有在向量数据库中找到相关文档也会作出回答
                                .build()
                )
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50) // 搜索相似度
                        .topK(5)
                        .vectorStore(vectorStore) // 向量存储
                        .build())
                .build();
        ChatResponse chatResponse = chatClient.prompt()
                .system("你是知识库检索助手。请严格根据检索到的文档内容回答用户问题。" +
                        "如果检索到的内容能回答问题，请基于文档内容给出准确、简洁的回答，并标注来源片段。" +
                        "如果检索到的内容与问题无关或无法回答，请直接回复：'知识库中未找到与该问题相关的内容。'" +
                        "禁止编造文档中不存在的信息。必须使用中文回答。" +
                        "严禁字符连打与词语循环复读（如‘要要要要要’‘想你想你想你’）。" +
                        "严禁省略号滥用：每条回复中‘……’最多 1 处，禁止连续堆叠（如‘…………’）。")
                .user(userQuery)
                // 使用 M6 版本标准的 Key 常量
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                //开启RAG增强搜索功能
                .advisors(retrievalAugmentationAdvisor)
                .call()
                .chatResponse();
        return Objects.requireNonNull(chatResponse).getResult().getOutput().getText();

    }
}
