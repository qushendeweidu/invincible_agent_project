package com.laodeng.laodengaiagent.controller.load;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.laodeng.laodengaiagent.config.MinioConfig;
import com.laodeng.laodengaiagent.service.MinioService;
import com.laodeng.laodengaiagent.service.RAGDocumentService;
import com.laodeng.laodengaiagent.utils.ImageUploadUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/25 23:23
 * @description 文件上传下载控制器，基于 MinIO 对象存储
 */
@Log4j2
@RestController
@RequestMapping("/file_load")
@RequiredArgsConstructor
public class FileLoadController {
    private final RAGDocumentService ragDocumentService;
    private final ImageUploadUtils imageUploadUtils;
    private final MinioService minioService;
    private final MinioConfig minioConfig;

    /**
     * 上传图片到 MinIO
     * @param file 图片文件
     * @return String 上传后的文件访问路径
     */
    @SaCheckPermission("user.ai_base")
    @PostMapping("/image")
    public String uploadImage(@RequestParam("file") MultipartFile file) {
        return imageUploadUtils.upload(file);
    }

    /**
     * 上传文档到 MinIO
     * @param file 文档文件
     * @param objectName 自定义对象名称，为空则使用原文件名
     * @return String 上传后的文件访问路径
     */
    @PostMapping("/document")
    public String uploadDocument(@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "objectName", required = false) String objectName) {
        String bucketName = minioConfig.getBucketName();
        if (objectName == null || objectName.isBlank()) {
            objectName = file.getOriginalFilename();
        }
        return minioService.uploadFile(file, bucketName, objectName);
    }

    /**
     * 下载文件
     * @param bucket 桶名称
     * @param objectName 对象名称
     * @return ResponseEntity<InputStreamResource> 文件流响应
     */
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @RequestParam("bucket") String bucket,
            @RequestParam("objectName") String objectName) {
        try {
            InputStream inputStream = minioService.downloadFile(bucket, objectName);
            String fileName = objectName.contains("/") 
                    ? objectName.substring(objectName.lastIndexOf('/') + 1) 
                    : objectName;
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            log.error("下载文件失败: bucket={}, object={}", bucket, objectName, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除文件
     * @param bucket 桶名称
     * @param objectName 对象名称
     * @return Boolean 删除结果
     */
    @DeleteMapping("/delete")
    public Boolean deleteFile(@RequestParam("bucket") String bucket,
                              @RequestParam("objectName") String objectName) {
        return minioService.deleteFile(bucket, objectName);
    }

    /**
     * 列出文件
     * @param bucket 桶名称
     * @param prefix 文件前缀，用于过滤
     * @return List<String> 文件名列表
     */
    @GetMapping("/list")
    public List<String> listFiles(@RequestParam("bucket") String bucket,
                                   @RequestParam(value = "prefix", required = false, defaultValue = "") String prefix) {
        return minioService.listFiles(bucket, prefix);
    }

    /**
     * 获取文件预签名 URL
     * @param bucket 桶名称
     * @param objectName 对象名称
     * @param expiry 链接有效期（秒），默认3600秒
     * @return String 可直接下载的临时URL链接
     */
    @GetMapping("/url")
    public String getFileUrl(@RequestParam(value = "bucket" , defaultValue = "") String bucket,
                              @RequestParam("objectName") String objectName,
                              @RequestParam(value = "expiry", defaultValue = "3600") int expiry) {
        return minioService.getPresignedUrl(bucket, objectName, expiry);
    }

    /**
     * 批量上传图片到 MinIO
     * @param files 图片文件列表
     * @return List<String> 上传后的文件访问路径列表
     */
    @PostMapping("/images")
    public List<String> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        return imageUploadUtils.uploadBatch(files);
    }

    /**
     * 批量上传文档到 MinIO
     * @param files 文档文件列表
     * @return List<String> 上传后的文件访问路径列表
     */
    @PostMapping("/documents")
    public List<String> uploadDocuments(@RequestParam("files") List<MultipartFile> files) {
        String bucketName = minioConfig.getBucketName();
        return files.stream()
                .map(file -> minioService.uploadFile(file, bucketName, file.getOriginalFilename()))
                .toList();
    }

    /**
     * 从 MinIO 导入所有 md 文档到 ES
     * @param prefix 文件前缀，用于过滤要导入的文件
     * @return Boolean 导入结果
     */
    @PostMapping("/add_ragdoc_minio")
    public Boolean addDocumentFromMinio(
            @RequestParam(value = "prefix", defaultValue = "") String prefix
    ) {
        try {
            ragDocumentService.insertAllMdFromMinio(prefix);
            return true;
        } catch (IOException e) {
            log.error("从 MinIO 导入文档失败", e);
            return false;
        }
    }

    /**
     * 从 URL 下载图片并上传到 MinIO
     * @param imageUrl
     * @return
     */
    @GetMapping("/download_from_url")
    public String downloadFromUrlAndUploadMinio(String imageUrl) {
        return imageUploadUtils.downloadFromUrl(imageUrl);
    }

}
