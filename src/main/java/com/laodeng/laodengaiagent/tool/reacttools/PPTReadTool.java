package com.laodeng.laodengaiagent.tool.reacttools;

import com.laodeng.laodengaiagent.annotition.ToolMetadata;
import com.laodeng.laodengaiagent.config.McpToolsConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.function.BiFunction;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/19 10:49
 * @description PPT 文档读取工具，从 MinIO 下载后读取
 */
@Log4j2
@Component
public class PPTReadTool extends OfficialReadTool implements BiFunction<McpToolsConfig.FileReadRequest, ToolContext, String> {

    @Override
    public String apply(McpToolsConfig.FileReadRequest s, ToolContext toolContext) {
        File tempFile = null;
        try {
            String objectName = s.path();
            log.info("从 MinIO 下载 PPT 文件: {}", objectName);
            tempFile = downloadFromMinio(objectName);
            
            try (FileInputStream fis = new FileInputStream(tempFile);
                 XMLSlideShow slideShow = new XMLSlideShow(fis)) {
                log.info("开始读取 PPT 文件: {}", objectName);
                StringBuilder content = new StringBuilder();

                for (int i = 0; i < slideShow.getSlides().size(); i++) {
                    XSLFSlide slide = slideShow.getSlides().get(i);
                    content.append("【Slide ").append(i + 1).append("】\n");

                    var shapes = slide.getShapes();
                    for (var shape : shapes) {
                        if (shape instanceof org.apache.poi.xslf.usermodel.XSLFTextShape) {
                            var textShape = (org.apache.poi.xslf.usermodel.XSLFTextShape) shape;
                            for (var paragraph : textShape.getTextParagraphs()) {
                                String text = paragraph.getText();
                                if (text != null && !text.isEmpty()) {
                                    content.append(text).append("\n");
                                }
                            }
                        }
                    }
                    content.append("\n");
                }
                int maxChars = this.getClass().getAnnotation(ToolMetadata.class).maxResultChars();
                return truncateResult(content.toString(), maxChars);
            }
        } catch (Exception e) {
            log.error("读取 Pptx 失败: {}", s.path(), e);
            return "读取 Pptx 失败: " + e.getMessage();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
