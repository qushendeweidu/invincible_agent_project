package com.laodeng.laodengaiagent.utils;

import com.laodeng.laodengaiagent.config.MinioConfig;
import com.laodeng.laodengaiagent.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/25 23:00
 * @description 图片上传工具类，基于 MinIO 对象存储
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class ImageUploadUtils {

    private final MinioService minioService;
    private final MinioConfig minioConfig;

    private static final long MAX_FILE_SIZE = 800 * 1024 * 1024; // 800MB

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "tiff"
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/bmp",
            "image/webp", "image/svg+xml", "image/tiff","application/octet-stream"
    );

    /**
     * 上传 MultipartFile 图片到 MinIO
     *
     * @param file 上传的图片文件
     * @return MinIO 对象名称
     */
    public String upload(MultipartFile file) {
        validate(file);

        String ext = getExtension(file.getOriginalFilename());
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectName = datePath + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;

        String bucketName = minioConfig.getBucketName();
        minioService.uploadFile(file, bucketName, objectName);

        log.info("图片上传成功: {} -> {}/{}", file.getOriginalFilename(), bucketName, objectName);
        return objectName;
    }

    /**
     * 批量上传图片到 MinIO
     *
     * @param files 上传的图片文件列表
     * @return MinIO 对象名称列表
     */
    public List<String> uploadBatch(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("上传文件列表不能为空");
        }
        return files.stream()
                .map(this::upload)
                .toList();
    }

    /**
     * 从 URL 下载图片并上传到 MinIO
     *
     * @param imageUrl 图片 URL 地址
     * @return MinIO 对象名称
     */
    public String downloadFromUrl(String imageUrl) {
        try {
            URI uri = URI.create(imageUrl);

            String ext = getExtensionFromUrl(imageUrl);
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                ext = "png";
            }

            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectName = datePath + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;

            try (InputStream in = uri.toURL().openStream()) {
                String bucketName = minioConfig.getBucketName();
                String contentType = "image/" + ext;
                minioService.uploadFile(in, bucketName, objectName, contentType, -1);
                log.info("图片下载并上传成功: {} -> {}/{}", imageUrl, bucketName, objectName);
                return objectName;
            }
        } catch (Exception e) {
            log.error("从URL下载图片失败: {}", imageUrl, e);
            throw new RuntimeException("从URL下载图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除已上传的图片
     *
     * @param objectName MinIO 对象名称
     * @return 是否删除成功
     */
    public boolean delete(String objectName) {
        String bucketName = minioConfig.getBucketName();
        return minioService.deleteFile(bucketName, objectName);
    }

    /**
     * 获取图片的预签名访问 URL
     *
     * @param objectName MinIO 对象名称
     * @param expiry     过期时间（秒）
     * @return 预签名 URL
     */
    public String getPresignedUrl(String objectName, int expiry) {
        String bucketName = minioConfig.getBucketName();
        return minioService.getPresignedUrl(bucketName, objectName, expiry);
    }

    /**
     * 获取图片的预签名访问 URL（默认1小时过期）
     *
     * @param objectName MinIO 对象名称
     * @return 预签名 URL
     */
    public String getPresignedUrl(String objectName) {
        return getPresignedUrl(objectName, 3600);
    }

    /**
     * 下载图片为 InputStream
     *
     * @param objectName MinIO 对象名称
     * @return 图片输入流
     */
    public InputStream download(String objectName) {
        String bucketName = minioConfig.getBucketName();
        return minioService.downloadFile(bucketName, objectName);
    }

    /**
     * 校验上传文件：非空、大小、类型
     */
    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小超过限制: 最大 " + MAX_FILE_SIZE / 1024 / 1024 + "MB");
        }
        String ext = getExtension(file.getOriginalFilename()); // 从文件名中获取扩展名
        String contentType = file.getContentType(); // 获取文件内容类型
        boolean contentTypeOk = contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());
        boolean extensionOk = ALLOWED_EXTENSIONS.contains(ext);
        if (!contentTypeOk || !extensionOk) {
            throw new IllegalArgumentException("不支持的图片格式: contentType=" + contentType + ", 或者不支持的文件扩展名=" + ext);
        }
    }

    /**
     * 从文件名提取扩展名（小写）
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "png";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 从 URL 提取扩展名()
     */
    private String getExtensionFromUrl(String url) {
        String path = url.split("\\?")[0];
        return getExtension(path);
    }
}
