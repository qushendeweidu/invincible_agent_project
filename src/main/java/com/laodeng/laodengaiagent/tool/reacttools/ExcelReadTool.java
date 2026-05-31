package com.laodeng.laodengaiagent.tool.reacttools;

import java.util.function.BiFunction;

import com.laodeng.laodengaiagent.annotition.ToolMetadata;
import com.laodeng.laodengaiagent.config.McpToolsConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/19 10:44
 * @description Excel 文档读取工具，从 MinIO 下载后读取
 */
@Log4j2
@Component
public class ExcelReadTool extends OfficialReadTool implements BiFunction<McpToolsConfig.FileReadRequest, ToolContext, String> {

    @Override
    public String apply(McpToolsConfig.FileReadRequest s, ToolContext toolContext) {
        File tempFile = null;
        try {
            String objectName = s.path();
            log.info("从 MinIO 下载 Excel 文件: {}", objectName);
            tempFile = downloadFromMinio(objectName);
            
            try (FileInputStream fis = new FileInputStream(tempFile);
                 XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
                log.info("开始读取 Excel 文件: {}", objectName);
                StringBuilder content = new StringBuilder();

                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    var sheet = workbook.getSheetAt(i);
                    content.append("【Sheet ").append(i + 1).append(": ").append(sheet.getSheetName()).append("】\n");

                    for (var row : sheet) {
                        StringBuilder rowContent = new StringBuilder();
                        for (var cell : row) {
                            String cellValue = getCellValue(cell);
                            if (cellValue != null && !cellValue.isEmpty()) {
                                rowContent.append(cellValue).append("\t");
                            }
                        }
                        if (rowContent.length() > 0) {
                            content.append(rowContent).append("\n");
                        }
                    }
                    content.append("\n");
                }
                int maxChars = this.getClass().getAnnotation(ToolMetadata.class).maxResultChars();
                return truncateResult(content.toString(), maxChars);
            }
        } catch (Exception e) {
            log.error("读取 Excel 失败: {}", s.path(), e);
            return "读取 Excel 失败: " + e.getMessage();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
