package com.laodeng.laodengaiagent.service.impl;

import com.laodeng.laodengaiagent.config.MinioConfig;
import com.laodeng.laodengaiagent.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/30
 * @description MinIO 对象存储服务实现类
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Override
    public String uploadFile(MultipartFile file, String bucketName, String objectName) {
        try {
            ensureBucketExists(bucketName);
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            
            log.info("文件上传成功: bucket={}, object={}", bucketName, objectName);
            return objectName;
        } catch (Exception e) {
            log.error("文件上传失败: bucket={}, object={}", bucketName, objectName, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }


    @Override
    public String uploadFile(InputStream inputStream, String bucketName, String objectName, String contentType, long size) {
        try {
            ensureBucketExists(bucketName);
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
            
            log.info("文件上传成功: bucket={}, object={}", bucketName, objectName);
            return objectName;
        } catch (Exception e) {
            log.error("文件上传失败: bucket={}, object={}", bucketName, objectName, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("文件下载失败: bucket={}, object={}", bucketName, objectName, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public File downloadToTempFile(String bucketName, String objectName) {
        try {
            InputStream inputStream = downloadFile(bucketName, objectName);
            
            String suffix = "";
            int dotIndex = objectName.lastIndexOf('.');
            if (dotIndex > 0) {
                suffix = objectName.substring(dotIndex);
            }
            
            File tempFile = Files.createTempFile("minio_", suffix).toFile();
            tempFile.deleteOnExit(); //在JVM关闭时删除临时文件
            
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            inputStream.close();
            
            log.info("文件下载到临时文件: bucket={}, object={}, tempFile={}", bucketName, objectName, tempFile.getAbsolutePath());
            return tempFile;
        } catch (Exception e) {
            log.error("下载到临时文件失败: bucket={}, object={}", bucketName, objectName, e);
            throw new RuntimeException("下载到临时文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            log.info("文件删除成功: bucket={}, object={}", bucketName, objectName);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败: bucket={}, object={}", bucketName, objectName, e);
            return false;
        }
    }

    @Override
    public List<String> listFiles(String bucketName, String prefix) {
        List<String> fileNames = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .recursive(true)
                    .build());

            for (Result<Item> result : results) {
                fileNames.add(result.get().objectName());
            }
            log.info("列出文件: bucket={}, prefix={}, count={}", bucketName, prefix, fileNames.size());
        } catch (Exception e) {
            log.error("列出文件失败: bucket={}, prefix={}", bucketName, prefix, e);
            throw new RuntimeException("列出文件失败: " + e.getMessage(), e);
        }
        return fileNames;
    }

    /**
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry     过期时间（秒）
     * @return 返回预签名 URL (预签名URL:可以直接被访问之后直接开始图片的下载)
     */
    @Override
    public String getPresignedUrl(String bucketName, String objectName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expiry, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            log.error("获取预签名URL失败: bucket={}, object={}", bucketName, objectName, e);
            throw new RuntimeException("获取预签名URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证文件是否存在
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 存在返回true，否则返回false
     */
    @Override
    public boolean fileExists(String bucketName, String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证存储桶是否存在,不存在就创建一个这个名字的桶
     * @param bucketName 存储桶名称
     */
    @Override
    public void ensureBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("创建存储桶: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("确保存储桶存在失败: {}", bucketName, e);
            throw new RuntimeException("确保存储桶存在失败: " + e.getMessage(), e);
        }
    }
}
