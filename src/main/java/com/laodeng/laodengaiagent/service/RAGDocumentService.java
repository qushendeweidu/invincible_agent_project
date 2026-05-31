package com.laodeng.laodengaiagent.service;

import java.io.IOException;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/14 18:54
 * @description RAG文档处理业务层
 */
public interface RAGDocumentService {
    /**
     * 从 MinIO 导入所有 md 文档到 ES
     * @param prefix 文件前缀（可选）
     */
    void insertAllMdFromMinio(String prefix) throws IOException;
}
