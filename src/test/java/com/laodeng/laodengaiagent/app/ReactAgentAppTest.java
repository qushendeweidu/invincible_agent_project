package com.laodeng.laodengaiagent.app;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.laodeng.laodengaiagent.annotition.ToolMetadata;
import com.laodeng.laodengaiagent.domain.po.StreamEvent;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/21 22:36
 * @description
 */

@Log4j2
@SpringBootTest
@ToolMetadata(maxResultChars = 5000, concurrencySafe = true)
public class ReactAgentAppTest {
    @Autowired
    private ReactAgentApp reactAgentApp;


    @Test
    void testMultipleAgents() throws GraphRunnerException {
        Flux<StreamEvent> execute = reactAgentApp.execute("分析一下   第一天任务.docx   这个文档内容说了什么分析意图告诉我", "test_123");
        log.info("执行结果: {}", execute);
        Flux<StreamEvent> execute1 = reactAgentApp.execute("我之前问了什么", "test_123");
        log.info("执行结果: {}", execute1);
    }

    @Test
    void testToolMetadata() throws GraphRunnerException {
        System.out.println(this.getClass().getAnnotation(ToolMetadata.class).maxResultChars());
    }

}
