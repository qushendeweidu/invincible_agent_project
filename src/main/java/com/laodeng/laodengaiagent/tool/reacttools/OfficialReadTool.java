package com.laodeng.laodengaiagent.tool.reacttools;

import com.laodeng.laodengaiagent.annotition.ToolMetadata;
import com.laodeng.laodengaiagent.config.MinioConfig;
import com.laodeng.laodengaiagent.service.MinioService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/19 10:37
 * @description 文档读取工具基类，支持从 MinIO 下载文件
 */
@Log4j2
@ToolMetadata(readOnly = true, concurrencySafe = true)
public abstract class OfficialReadTool {

    @Autowired
    protected MinioService minioService;

    @Autowired
    protected MinioConfig minioConfig;

    // ==================== 辅助方法 ====================

    /**
     * 从 MinIO 下载文件到临时目录
     *
     * @param objectName MinIO 对象名称
     * @return 临时文件
     */
    protected File downloadFromMinio(String objectName) {
        String bucketName = minioConfig.getBucketName();
        return minioService.downloadToTempFile(bucketName, objectName);
    }

    protected String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                yield String.valueOf(cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }


    // 截断工具调用的时候截断结果，避免返回内容过长导致模型崩溃
    protected String truncateResult(String content, int maxChars) {
        if (content.length() <= maxChars) return content;
        return content.substring(0, maxChars)
                + "\n\n...[内容过长，已截断，共 " + content.length() + " 字符，仅显示前 " + maxChars + " 字符]";
    }
}
