package com.laodeng.laodengaiagent.config;

import com.laodeng.laodengaiagent.tool.FileReadTools;
import com.laodeng.laodengaiagent.tool.reacttools.*;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/17 09:57
 * @description
 */
@Configuration
public class McpToolsConfig {
    public record FileReadRequest(String path) {}
    public record ImageAnalyseRequest(String path, String prompt) {}
    public record ChatHistoryRequest(String conversationId, Integer page, Integer size) {}
    public record RAGRequest(String userInput, String conversationId) {}

    @Bean("chatClientToolCallbackProvider")
    public ToolCallbackProvider toolCallbackProvider(FileReadTools fileReadTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(fileReadTools)
                .build();
    }

    @Bean("reactAgentToolCallbackProvider")
    public ToolCallbackProvider reactAgentToolCallbackProvider(
            PdfReadTool pdfTool,
            DocxReadTool docxTool,
            ExcelReadTool excelTool,
            PPTReadTool pptTool,
            ImageAnalyseTool imageAnalyseTool,
            ChatHistoryTool chatHistoryTool,
            RAGReadTool ragReadTool
    ) {
        ToolCallback pdfCallback = FunctionToolCallback
                .builder("readPdf", pdfTool)
                .description("读取PDF文档")
                .inputType(FileReadRequest.class)
                .build();

        ToolCallback docxCallback = FunctionToolCallback
                .builder("readDocx", docxTool)
                .description("读取Word文档")
                .inputType(FileReadRequest.class)
                .build();

        ToolCallback excelCallback = FunctionToolCallback
                .builder("readExcel", excelTool)
                .description("读取Excel文档")
                .inputType(FileReadRequest.class)
                .build();

        ToolCallback pptCallback = FunctionToolCallback
                .builder("readPptx", pptTool)
                .description("读取PPT文档")
                .inputType(FileReadRequest.class)
                .build();

        ToolCallback imageCallback = FunctionToolCallback
                .builder("imageAnalyse", imageAnalyseTool)
                .description("分析图片内容，支持本地图片路径或URL。path为图片路径，prompt为分析要求（如：描述内容、提取文字、识别物体等）")
                .inputType(ImageAnalyseRequest.class)
                .build();

        ToolCallback chatHistoryCallback = FunctionToolCallback
                .builder("durableMemory", chatHistoryTool)
                .description("查询与用户的历史对话记录（长期记忆）。" +
                        "使用场景：当用户提到'之前聊过''上次说的''你还记得吗''我们讨论过''之前的对话'等涉及回忆历史对话的内容时，必须先调用此工具检索记忆再回答。" +
                        "参数说明：conversationId为会话ID（与当前对话的memoryId一致），page为页码（从0开始，默认0，先查第0页），size为每页条数（默认20）。" +
                        "如果第0页没有找到相关内容，可以翻页查询更早的记录。")
                .inputType(ChatHistoryRequest.class)
                .build();
        ToolCallback ragCallback = FunctionToolCallback
                .builder("ragSearch", ragReadTool)
                .description("基于向量数据库的知识库检索。当用户询问的内容可能存在于已上传的知识文档（技术文档、学习资料、项目文档等）中时调用此工具。" +
                        "参数说明：userInput为用户的查询内容，conversationId为会话ID（与当前对话的memoryId一致）。" +
                        "注意：此工具用于检索已导入的知识文档，不是对话记录。查询历史对话请用durableMemory工具。")
                .inputType(RAGRequest.class)
                .build();

        return() -> new ToolCallback[] { pdfCallback, docxCallback, excelCallback, pptCallback , imageCallback ,chatHistoryCallback , ragCallback};
    }
}
