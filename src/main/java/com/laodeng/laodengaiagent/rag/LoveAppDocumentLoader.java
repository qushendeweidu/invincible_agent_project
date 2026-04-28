package com.laodeng.laodengaiagent.rag;

import cn.hutool.core.util.ObjUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/2 14:22
 * @description 恋爱大师应用文档加载器
 * <a href="https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html">相关SpringAI官网示例在此</a>
 */

@Log4j2
@Component
@RequiredArgsConstructor
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    /**
     * 从当前的Resource加载Markdown文档
     * @return 文档列表
     */
    public List<Document> loadMarkdowns(){
        List<Document> allDocuments = new ArrayList<>();
        //加载多篇Markdown文档
        try{
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                allDocuments.addAll(loadMarkdown(resource, filename));
                log.info("成功加载Markdown文档:{}", resources.length);
            }
            if (ObjUtil.isNotEmpty(allDocuments)){
                return allDocuments;
            }else {
                log.info("警告当前读取的文档为空!!!!!!!!");
                return null;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 加载Markdown文档(来源是SpringAI官网示例代码)
     * @param resource 资源对象
     * @param fileName 文件名
     * @return 文档列表
     */
    List<Document> loadMarkdown(Resource resource,String fileName) {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true) // 是否按照分隔符创建文档
                .withIncludeCodeBlock(false) // 是否包含代码块
                .withIncludeBlockquote(false) // 是否包含引号
                .withAdditionalMetadata("filename", fileName) // 查询的文件名
                .build();
        // 创建Markdown文档读取器
        MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
        return reader.get();
    }



}
