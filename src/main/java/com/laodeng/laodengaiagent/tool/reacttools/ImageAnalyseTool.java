package com.laodeng.laodengaiagent.tool.reacttools;

import com.laodeng.laodengaiagent.annotition.ToolMetadata;
import com.laodeng.laodengaiagent.config.McpToolsConfig;
import com.laodeng.laodengaiagent.config.MinioConfig;
import com.laodeng.laodengaiagent.register.DynamicChatModelRegistry;
import com.laodeng.laodengaiagent.service.MinioService;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/30 15:01
 * @description 图片分析工具，支持 MinIO 图片和 URL 图片
 */
@Log4j2
@Component
@ToolMetadata(readOnly = true, concurrencySafe = true)
public class ImageAnalyseTool implements BiFunction<McpToolsConfig.ImageAnalyseRequest, ToolContext, String> {

    private final ChatModel visionModel;
    private final MinioService minioService;
    private final MinioConfig minioConfig;

    public ImageAnalyseTool(DynamicChatModelRegistry registry, MinioService minioService, MinioConfig minioConfig) {
        this.visionModel = registry.getModel("multiple");
        this.minioService = minioService;
        this.minioConfig = minioConfig;
    }

    @Override
    public String apply(McpToolsConfig.ImageAnalyseRequest request, ToolContext toolContext) {
        try {
            String imagePath = request.path();
            String prompt = request.prompt() != null ? request.prompt() : "请描述这张图片的内容";

            log.info("开始分析图片: {}, prompt: {}", imagePath, prompt);

            Media media;
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                String mimeType = getMimeType(imagePath);
                media = new Media(MimeTypeUtils.parseMimeType(mimeType), URI.create(imagePath));
            } else {
                String bucketName = minioConfig.getBucketName();
                log.info("从 MinIO 下载图片: bucket={}, object={}", bucketName, imagePath);
                
                try (InputStream inputStream = minioService.downloadFile(bucketName, imagePath)) {
                    byte[] imageBytes = inputStream.readAllBytes();
                    String base64 = Base64.getEncoder().encodeToString(imageBytes);
                    String mimeType = getMimeType(imagePath);
                    media = new Media(MimeTypeUtils.parseMimeType(mimeType),
                            URI.create("data:" + mimeType + ";base64," + base64));
                }
            }

            UserMessage userMessage = UserMessage.builder()
                    .text(prompt)
                    .media(List.of(media))
                    .build();

            ChatResponse response = visionModel.call(new Prompt(userMessage));
            int maxChars = this.getClass().getAnnotation(ToolMetadata.class).maxResultChars();
            String result = truncateResult(
                    Objects.requireNonNull(response.getResult().getOutput().getText()), maxChars
            );
            log.info("图片分析完成，结果长度: {} 字符", result.length());
            return result;

        } catch (Exception e) {
            log.error("图片分析失败", e);
            return "图片分析失败: " + e.getMessage();
        }
    }

    private String getMimeType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        return "image/png";
    }

    private String truncateResult(String content, int maxChars) {
        if (content.length() <= maxChars) return content;
        return content.substring(0, maxChars)
                + "\n\n...[内容过长，已截断，共 " + content.length() + " 字符，仅显示前 " + maxChars + " 字符]";
    }
}
