package com.laodeng.laodengaiagent.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/30
 * @description MinIO 对象存储服务接口
 */
public interface MinioService {

    /**
     * 上传文件（MultipartFile）
     *
     * @param file       上传的文件
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（可包含路径，如 "images/2026/03/xxx.jpg"）
     * @return 对象名称
     */
    String uploadFile(MultipartFile file, String bucketName, String objectName);

    /**
     * 上传文件（InputStream）
     *
     * @param inputStream 输入流
     * @param bucketName  存储桶名称
     * @param objectName  对象名称
     * @param contentType 文件类型
     * @param size        文件大小
     * @return 对象名称
     */
    String uploadFile(InputStream inputStream, String bucketName, String objectName, String contentType, long size);

    /**
     * 下载文件为 InputStream
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件输入流
     */
    InputStream downloadFile(String bucketName, String objectName);

    /**
     * 下载文件到临时文件（供 POI/PDFBox 等需要 File 的库使用）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 临时文件
     */
    File downloadToTempFile(String bucketName, String objectName);

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 是否删除成功
     */
    boolean deleteFile(String bucketName, String objectName);

    /**
     * 列出指定前缀的文件
     *
     * @param bucketName 存储桶名称
     * @param prefix     前缀（可为空）
     * @return 文件对象名称列表
     */
    List<String> listFiles(String bucketName, String prefix);

    /**
     * 获取文件的预签名访问 URL
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry     过期时间（秒）
     * @return 预签名 URL
     */
    String getPresignedUrl(String bucketName, String objectName, int expiry);

    /**
     * 检查文件是否存在
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 是否存在
     */
    boolean fileExists(String bucketName, String objectName);

    /**
     * 确保存储桶存在，不存在则创建
     *
     * @param bucketName 存储桶名称
     */
    void ensureBucketExists(String bucketName);

}
