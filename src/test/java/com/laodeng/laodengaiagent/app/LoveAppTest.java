package com.laodeng.laodengaiagent.app;

import com.laodeng.laodengaiagent.utils.MyTransformerUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Log4j2
@SpringBootTest
class LoveAppTest {
    @Autowired
    private LoveApp loveApp;
    @Autowired
    private MyTransformerUtils myTransformerUtils;
    @Value("classpath:/prompt/ChatClientSystemPrompt.st")
    private Resource resource;

    @SneakyThrows
    @Test
    void contextLoads() {
        String chatId = UUID.randomUUID().toString();
        System.out.println(loveApp.doChat("告诉我关于一些spring的设计", chatId));
        System.out.println(loveApp.doChat("你继续扩展讲解原理", chatId));
    }

    /**
     * 测试RAG
     */
    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        LoveApp.LoveReport report = loveApp.doChatWithReport("告诉我如何让另一半更爱我", chatId);
        System.out.println(report);
        Assertions.assertNotNull(report);
    }

    /**
     * 测试PromptTemplate
     */
    @Test
    void promptTemplateTest() {
        String template = """
                你好我是:{userName}
                """;

        PromptTemplate promptTemplate = PromptTemplate.builder().template(template).variables(Map.of("userName", "小明")).build();
        System.out.println(promptTemplate.render());

    }

    /**
     * RAG检索测试
     */
    @Test
    void ragTest(){
        String userMessageInsert = "接下来告诉我Springai如何通过ETL Pipeline提供的DocumentTransformer来整理切分规则";
        String chatId = UUID.randomUUID().toString();
        String result = loveApp.doChatWithRag(userMessageInsert, chatId);
        log.info("最终RAG检索结果: {}", result);
    }


    /**
     * 创建测试流式输出
     */
    @Test
    void doChatWithStream() {
        String userMessageInsert = "搜索一下GitHub的https://github.com/anthropics/skills/tree/main/skills/skill-creator";
        String chatId = UUID.randomUUID().toString();
        Flux<String> result = loveApp.doChatWithStream(userMessageInsert, chatId);
        result.doOnNext(System.out::print) // 每次收到一个 Token 就打印，不换行
                .doOnError(e -> System.err.println("出错啦: " + e.getMessage()))
                .blockLast(); // 【关键】阻塞主线程直到流结束，否则你看不到结果测试就结束了
        log.info("输出结束");
    }

    @Test
    void resourceTest() throws IOException {
        String content = resource.getContentAsString(StandardCharsets.UTF_8);
        System.out.println(content);
    }

}