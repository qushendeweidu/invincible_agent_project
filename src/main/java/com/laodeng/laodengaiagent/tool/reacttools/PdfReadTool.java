package com.laodeng.laodengaiagent.tool.reacttools;

import java.util.function.BiFunction;

import com.laodeng.laodengaiagent.annotition.ToolMetadata;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import com.laodeng.laodengaiagent.config.McpToolsConfig.FileReadRequest;

import java.io.File;
import java.io.IOException;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/19 10:51
 * @description PDF 文档读取工具，从 MinIO 下载后读取
 */
@Log4j2
@Component
public class PdfReadTool extends OfficialReadTool implements BiFunction<FileReadRequest, ToolContext, String> {

    @Override
    public String apply(FileReadRequest s, ToolContext toolContext) {
        File tempFile = null;
        try {
            String objectName = s.path();
            log.info("从 MinIO 下载 PDF 文件: {}", objectName);
            tempFile = downloadFromMinio(objectName);
            
            try (PDDocument document = Loader.loadPDF(tempFile)) {
                log.info("开始读取 PDF 文件: {}", objectName);
                PDFTextStripper stripper = new PDFTextStripper();
                int maxChars = this.getClass().getAnnotation(ToolMetadata.class).maxResultChars();
                return truncateResult(stripper.getText(document), maxChars);
            }
        } catch (IOException e) {
            log.error("读取 PDF 失败: {}", s.path(), e);
            return "读取 PDF 失败: " + e.getMessage();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
