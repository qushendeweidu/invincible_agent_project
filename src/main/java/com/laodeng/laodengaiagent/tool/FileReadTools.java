package com.laodeng.laodengaiagent.tool;

import com.laodeng.laodengaiagent.annotition.ToolMetadata;
import com.laodeng.laodengaiagent.config.MinioConfig;
import com.laodeng.laodengaiagent.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static java.util.stream.Collectors.joining;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/17 09:42
 * @description 文件读取工具，从 MinIO 下载后读取
 */
@Log4j2
@Component
@RequiredArgsConstructor
@ToolMetadata(readOnly = true, concurrencySafe = true)
public class FileReadTools {

    private final MinioService minioService;
    private final MinioConfig minioConfig;
    private final int MAX_CHARS = this.getClass().getAnnotation(ToolMetadata.class).maxResultChars();

    // ==================== Word ====================

    @Tool(description = "读取并解析 MinIO 中的 .docx 格式的 Word 文档。参数 path 为 MinIO 对象名称。")
    public String readDocx(String path) {
        File tempFile = null;
        try {
            log.info("从 MinIO 下载 Word 文件: {}", path);
            tempFile = minioService.downloadToTempFile(minioConfig.getBucketName(), path);
            
            try (FileInputStream fis = new FileInputStream(tempFile);
                 XWPFDocument document = new XWPFDocument(fis)) {
                log.info("开始读取 Word 文件: {}", path);
                String result = document.getParagraphs().stream()
                        .map(XWPFParagraph::getText)
                        .collect(joining("\n"));
                return truncateResult(result, MAX_CHARS);
            }
        } catch (Exception e) {
            log.error("读取 Docx 失败: {}", path, e);
            return "读取 Docx 失败: " + e.getMessage();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // ==================== Excel ====================
    @Tool(description = "读取并解析 MinIO 中的 .xlsx 格式的 Excel 文档，返回纯文本内容。参数 path 为 MinIO 对象名称。")
    public String readExcel(String path) {
        File tempFile = null;
        try {
            log.info("从 MinIO 下载 Excel 文件: {}", path);
            tempFile = minioService.downloadToTempFile(minioConfig.getBucketName(), path);
            
            try (FileInputStream fis = new FileInputStream(tempFile);
                 XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
                log.info("开始读取 Excel 文件: {}", path);
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
                return truncateResult(content.toString(), MAX_CHARS);
            }
        } catch (Exception e) {
            log.error("读取 Excel 失败: {}", path, e);
            return "读取 Excel 失败: " + e.getMessage();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // ==================== PPT ====================
    @Tool(description = "读取并解析 MinIO 中的 .pptx 格式的 PowerPoint 文档，返回纯文本内容。参数 path 为 MinIO 对象名称。")
    public String readPptx(String path) {
        File tempFile = null;
        try {
            log.info("从 MinIO 下载 PPT 文件: {}", path);
            tempFile = minioService.downloadToTempFile(minioConfig.getBucketName(), path);
            
            try (FileInputStream fis = new FileInputStream(tempFile);
                 XMLSlideShow slideShow = new XMLSlideShow(fis)) {
                log.info("开始读取 PPT 文件: {}", path);
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
                return truncateResult(content.toString(), MAX_CHARS);
            }
        } catch (Exception e) {
            log.error("读取 Pptx 失败: {}", path, e);
            return "读取 Pptx 失败: " + e.getMessage();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // ==================== PDF ====================
    @Tool(description = "读取并解析 MinIO 中的 .pdf 格式的 PDF 文档，返回纯文本内容。参数 path 为 MinIO 对象名称。")
    public String readPdf(String path) {
        File tempFile = null;
        try {
            log.info("从 MinIO 下载 PDF 文件: {}", path);
            tempFile = minioService.downloadToTempFile(minioConfig.getBucketName(), path);
            
            try (PDDocument document = Loader.loadPDF(tempFile)) {
                log.info("开始读取 PDF 文件: {}", path);
                PDFTextStripper stripper = new PDFTextStripper();
                return truncateResult(stripper.getText(document), MAX_CHARS);
            }
        } catch (IOException e) {
            log.error("读取 PDF 失败: {}", path, e);
            return "读取 PDF 失败: " + e.getMessage();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // ==================== 辅助方法 ====================

    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
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
