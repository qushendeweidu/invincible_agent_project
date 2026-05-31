package com.laodeng.laodengaiagent.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/3 14:06
 * @description
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class TextSplitterUtils {
    private final VectorStore vectorStore;
    private final MyDocumentEnricher documentEnricher;
    private final BatchingStrategy batchingStrategy;


    public void updateDoc(Resource resource) {
        // 创建文档
        TikaDocumentReader documentReader = new TikaDocumentReader(resource);
        // 创建文本分词器
        TokenTextSplitter textSplitter = new TokenTextSplitter(300, 200, 10, 400, true);
        // 分割文档
        List<Document> documents = textSplitter.split(documentReader.read());

        // 【新增】提取关键词（增强搜索能力）
        documents = documentEnricher.enrichDocumentsByKeyword(documents);
        log.info("✅ 已完成关键词提取，共处理 {} 个文档片段", documents.size());

//        // 【新增】生成摘要（增强上下文理解）
//        documents = documentEnricher.enrichDocumentsBySummary( documents);
//        log.info("✅ 已完成文档总结，共处理 {} 个文档片段", documents.size());

        // 【优化】使用批处理策略添加文档到向量存储中
        log.info("开始使用批处理策略向 ES 向量数据库添加文档，总共 {} 个文档", documents.size());

        // 使用批处理策略将文档分批
        List<List<Document>> batches = batchingStrategy.batch(documents);
        log.info("文档已分为 {} 个批次进行处理", batches.size());

        batches.forEach(batch -> {
            vectorStore.add(batch);
            log.info("完成一批，剩余 {} 批", batches.size() - batches.indexOf(batch) - 1);
        });

        log.info("✅ 所有文档已成功添加到 ES 向量数据库");

    }

}
