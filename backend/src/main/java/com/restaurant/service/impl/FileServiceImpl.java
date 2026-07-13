package com.restaurant.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.restaurant.common.BizException;
import com.restaurant.service.FileService;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final OSS ossClient;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.url-prefix}")
    private String urlPrefix;

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BizException("文件名无效");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        // 按日期分目录：images/2026-07-07/xxx.png
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fileName = "images/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

        try {
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, fileName,
                file.getInputStream(), null);
            ossClient.putObject(putRequest);
            String url = urlPrefix + "/" + fileName;
            log.info("文件上传到OSS成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("文件上传到OSS失败", e);
            throw new BizException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String upload(byte[] data, String ext) {
        if (data == null || data.length == 0) {
            throw new BizException("上传文件不能为空");
        }
        if (ext == null || ext.isBlank()) {
            ext = "bin";
        }

        // 头像库等程序化上传统一放在 avatars/ 目录下
        String fileName = "avatars/avatar_" + UUID.randomUUID().toString().replace("-", "") + "." + ext;

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, fileName, inputStream, null);
            ossClient.putObject(putRequest);
            String url = urlPrefix + "/" + fileName;
            log.info("字节文件上传到OSS成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("字节文件上传到OSS失败", e);
            throw new BizException("文件上传失败: " + e.getMessage());
        }
    }
}
