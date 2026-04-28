package com.laodeng.laodengaiagent.rag.ragTools;

import com.laodeng.laodengaiagent.register.DynamicChatModelRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/29 20:03
 * @description
 */

@Component
public class QueryRewriterTool {

    private final QueryTransformer queryTransformer;

    @Autowired
    public QueryRewriterTool( ChatClient.Builder chatClientBuilder) {
        this.queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .build();
    }

    /**
     * 执行查询重写
     *
     * @param prompt
     * @return
     */
    public String doQueryRewrite(String prompt) {
        Query query = new Query(prompt);
        // 执行查询重写
        Query transformedQuery = queryTransformer.transform(query);
        // 返回重写后的查询
        return transformedQuery.text();

    }



}
