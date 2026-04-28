package com.laodeng.laodengaiagent.app;

import com.laodeng.laodengaiagent.advisor.MyReReadingAdvisor;
import com.laodeng.laodengaiagent.charmemory.RedisBaseChatMemory;
import com.laodeng.laodengaiagent.rag.ragTools.QueryRewriterTool;
import com.laodeng.laodengaiagent.register.DynamicChatModelRegistry;
import com.laodeng.laodengaiagent.utils.MyTransformerUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/20 13:23
 * @description 聊天应用
 */

@Component
@Log4j2
public class LoveApp {
    private final ChatClient chatClient;
    private final MyTransformerUtils myTransformerUtils;
    private final static String MODEL_KEY = "multiple";
    @Value("classpath:/prompt/ChatClientSystemPrompt.st")
    private Resource SYSTEM_PROMPT;
    private final VectorStore vectorStore;
    private final QueryRewriterTool queryRewriterTool;

    //线程池创建之后便于复用,防止阻塞,控制并发数量,资源复用
    private final ExecutorService aiExecutor = Executors.newFixedThreadPool(
            20,
            Thread.ofVirtual().name("ai-agent", 1).factory()
    );

    /**
     * 构造函数
     *
     * @param registry            聊天模型
     * @param redisBaseChatMemory 聊天记忆
     */
    public LoveApp(RedisBaseChatMemory redisBaseChatMemory,
                   DynamicChatModelRegistry registry,
                   VectorStore vectorStore,
                   @Qualifier("chatClientToolCallbackProvider") ToolCallbackProvider toolCallbackProvider,
                   MyTransformerUtils myTransformerUtils,
                   QueryRewriterTool queryRewriterTool
                   ) {
        this.queryRewriterTool = queryRewriterTool;
        this.myTransformerUtils = myTransformerUtils;
        this.vectorStore = vectorStore;
        ChatModel chatModel = registry.getModel(MODEL_KEY);
        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(redisBaseChatMemory).build(),
                        SimpleLoggerAdvisor.builder().build(),
                        new MyReReadingAdvisor()
                )
                .defaultToolCallbacks(toolCallbackProvider)
                .build();

    }

    /**
     * 创建了模板的聊天对话模型
     *
     * @param msg      用户输入
     * @param memoryId 对话记忆ID
     * @return 对话结果
     */
    public String promptChat(String msg, String memoryId) {
        ChatResponse chatResponse = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(msg)
                // 使用 M6 版本的标准 Key 常量
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, memoryId))
                .call()
                .chatResponse();
        log.info("chatResponse: {}", chatResponse);
        String content = Objects.requireNonNull(chatResponse).getResult().getOutput().getText();
        log.info("chatResponse: {}", chatResponse);
        return content;
    }

    /**
     * AI基础对话
     *
     * @param msg      用户输入
     * @param memoryId 对话记忆ID
     * @return 对话结果
     */
    public String doChat(String msg, String memoryId) {
        ChatResponse chatResponse = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(msg)
                // 使用 M6 版本的标准 Key 常量
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, memoryId))
                .call()
                .chatResponse();
        log.info("chatResponse: {}", chatResponse);
        String content = Objects.requireNonNull(chatResponse).getResult().getOutput().getText();
        log.info("chatResponse: {}", chatResponse);
        return content;
    }

    /**
     * AI基于文件实现对于对话记忆持久化的对话
     *
     * @param msg      用户输入
     * @param memoryId 对话记忆ID
     * @return 对话结果
     */
    public String doChatByFileMemory(String msg, String memoryId) {
        //这个写法表示文件地址在项目根目录下的
        String fileDir = System.getProperty("user.dir") + "/tmp";
        ChatResponse chatResponse = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(msg)
                // 使用 M6 版本的标准 Key 常量
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, memoryId))
                .call()
                .chatResponse();
        log.info("chatResponse: {}", chatResponse);
        String content = Objects.requireNonNull(chatResponse).getResult().getOutput().getText();
        log.info("chatResponse: {}", chatResponse);
        return content;
    }

    //jdk14之后的新特性支持直接创建记录,创建一个LoveReport类
    public record LoveReport(String title, List<String> suggestions) {
    }

    /**
     * AI 恋爱报告
     *
     * @param msg      用户输入
     * @param memoryId 对话记忆ID
     * @return 恋爱报告
     */
    public LoveReport doChatWithReport(String msg, String memoryId) {
        LoveReport loveReport = chatClient.prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果,标题为{用户名}的恋爱报告,内容为建议列表")
                .user(msg)
                // 使用 M6 版本的标准 Key 常量
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, memoryId))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        String content = String.join("\n", Objects.requireNonNull(loveReport).suggestions);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }


    /**
     * 聊天对话结合RAG
     *
     * @param msg      用户输入
     * @param memoryId 对话记忆ID
     * @return RAG检索之后得出的答案
     */
    public String doChatWithRag(String msg, String memoryId) {
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
        String afterReWriteMsg = myTransformerUtils.reWriteMultipleTransformer(msg);
        ChatResponse chatResponse = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(afterReWriteMsg)
                // 使用 M6 版本标准的 Key 常量
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, memoryId))
                //开启RAG增强搜索功能
                .advisors(retrievalAugmentationAdvisor)
                .call()
                .chatResponse();

        log.info("chatResponse: {}", chatResponse);
        String content = Objects.requireNonNull(chatResponse).getResult().getOutput().getText();
        log.info("最终查询内容如下: {}", content);

        return content;
    }

    /**
     * 聊天对话结合流式输出,创建了线程池提高了线程复用率,
     *
     * @param msg      用户输入
     * @param memoryId 对话记忆ID
     * @return 聊天结果流
     */
    public Flux<String> doChatWithStream(String msg, String memoryId) {
        return Mono.just(msg)
                // 使用 subscribeOn 确保整个过程在虚拟线程中启动
                .subscribeOn(Schedulers.fromExecutor(aiExecutor))
                .flatMapMany(message -> {
                    // 直接返回 Spring AI 的 Flux
                    return chatClient.prompt()
                            .system(SYSTEM_PROMPT)
                            .user(message)
                            .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, memoryId))
                            .stream()
                            .content();
                })
                // 这里是关键：通过 limitRate 或简单的超时控制，保持流的稳定
                .timeout(Duration.ofSeconds(60))
                // 异常时返回友好的提示，不再需要手动擦屁股
                .onErrorResume(e -> {
                    log.error("AI 响应出错: ", e);
                    return Flux.just("【系统提示：AI 响应超时或服务繁忙，请稍后再试】");
                });
    }


}
