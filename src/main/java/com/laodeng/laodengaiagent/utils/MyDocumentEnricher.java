package com.laodeng.laodengaiagent.utils;

import com.laodeng.laodengaiagent.register.DynamicChatModelRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/4 11:33
 * @description
 */

@Component
class MyDocumentEnricher {

    private final ChatModel chatModel;

    MyDocumentEnricher(DynamicChatModelRegistry dynamicChatModelRegistry) {
        this.chatModel = dynamicChatModelRegistry.getModel("multiple");
    }

    /**
     * 提取关键词
     * @param documents SpringAI框架下的document
     * @return
     */
    List<Document> enrichDocumentsByKeyword(List<Document> documents) {
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(chatModel, 5);
        return enricher.apply(documents);
    }

    /**
     * 全面总结文档
     * @param documents SpringAI框架下的document
     * @return
     */
    List<Document> enrichDocumentsBySummary(List<Document> documents) {
        SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(chatModel,
                List.of(SummaryMetadataEnricher.SummaryType.PREVIOUS, SummaryMetadataEnricher.SummaryType.CURRENT, SummaryMetadataEnricher.SummaryType.NEXT));
        return enricher.apply(documents);
    }
}
