package com.laodeng.laodengaiagent.tool.reacttools;

import java.util.function.BiFunction;

import com.laodeng.laodengaiagent.annotition.ToolMetadata;
import com.laodeng.laodengaiagent.config.McpToolsConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;

import static java.util.stream.Collectors.joining;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/19 10:15
 * @description Word 文档读取工具，从 MinIO 下载后读取
 */
@Log4j2
@Component
public class DocxReadTool extends OfficialReadTool implements BiFunction<McpToolsConfig.FileReadRequest, ToolContext, String> {

    @Override
    public String apply(McpToolsConfig.FileReadRequest s, ToolContext toolContext) {
        File tempFile = null;
        try {
            String objectName = s.path();
            log.info("从 MinIO 下载 Word 文件: {}", objectName);
            tempFile = downloadFromMinio(objectName);
            
            try (FileInputStream fis = new FileInputStream(tempFile);
                 XWPFDocument document = new XWPFDocument(fis)) {
                log.info("开始读取 Word 文件: {}", objectName);
                String result = document.getParagraphs().stream()
                        .map(XWPFParagraph::getText)
                        .collect(joining("\n"));
                int maxChars = this.getClass().getAnnotation(ToolMetadata.class).maxResultChars();
                return truncateResult(result, maxChars);
            }
        } catch (Exception e) {
            log.error("读取 Docx 失败: {}", s.path(), e);
            return "读取 Docx 失败: " + e.getMessage();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
