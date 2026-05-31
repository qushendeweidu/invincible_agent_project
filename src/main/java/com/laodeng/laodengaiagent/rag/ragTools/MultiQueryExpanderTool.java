package com.laodeng.laodengaiagent.rag.ragTools;

import com.laodeng.laodengaiagent.advisor.MyReReadingAdvisor;
import com.laodeng.laodengaiagent.register.DynamicChatModelRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/15 22:30
 * @description 当前为多查询扩展器
 */

@Log4j2
@Component
public class MultiQueryExpanderTool {
    private final MultiQueryExpander queryExpander;
    private final static String MODEL_KEY = "multiple";

    @Autowired
    public MultiQueryExpanderTool(
            DynamicChatModelRegistry registry
    ) {
        ChatModel chatModel = registry.getModel(MODEL_KEY);
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        builder
                .defaultAdvisors(
                        new MyReReadingAdvisor()
                );
        this.queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(builder)
                .numberOfQueries(3)
                .build();
    }


    public List<Query> expand(String query) {
        List<Query> queries = queryExpander.expand(new Query(query));
        log.info("查询结果：{}", queries);
        return queries;
    }


}
