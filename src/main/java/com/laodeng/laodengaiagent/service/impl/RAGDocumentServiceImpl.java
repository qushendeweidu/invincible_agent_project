package com.laodeng.laodengaiagent.service.impl;

import com.laodeng.laodengaiagent.config.MinioConfig;
import com.laodeng.laodengaiagent.service.MinioService;
import com.laodeng.laodengaiagent.service.RAGDocumentService;
import com.laodeng.laodengaiagent.utils.TextSplitterUtils;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/14 18:55
 * @description RAG 文档服务实现类
 */

@Log4j2
@Service
@RequiredArgsConstructor
public class RAGDocumentServiceImpl implements RAGDocumentService {
    private final TextSplitterUtils textSplitterUtils;
    private final MinioConfig minioConfig;
    private final MinioService minioService;

    /**
     * 从 MinIO 导入所有 md 文档到 ES
     * @param prefix 文件前缀（可选，如 "docs/"）
     */
    @Override
    public void insertAllMdFromMinio(String prefix) throws IOException {
        String bucketName = minioConfig.getBucketName();

        // 列出所有文件
        List<String> allFiles = minioService.listFiles(bucketName, prefix);

        // 过滤出 .md 文件
        List<String> mdFiles = allFiles.stream()
                .filter(name -> name.toLowerCase().endsWith(".md"))
                .toList();

        log.info("从 MinIO 找到 {} 个 md 文件，开始导入到 ES", mdFiles.size());

        for (String objectName : mdFiles) {
            File tempFile = null;
            try {
                tempFile = minioService.downloadToTempFile(bucketName, objectName);
                Resource resource = new FileSystemResource(tempFile);
                textSplitterUtils.updateDoc(resource);
                log.info("成功处理并包装文件: {}", objectName);
            } finally {
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }

        log.info("✅ 从 MinIO 导入 {} 个 md 文档到 ES 完成", mdFiles.size());
    }


}
