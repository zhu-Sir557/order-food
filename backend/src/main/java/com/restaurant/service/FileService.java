package com.restaurant.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传 MultipartFile 到 OSS
     *
     * @param file 上传文件
     * @return 可访问的公网 URL
     */
    String upload(MultipartFile file);

    /**
     * 以字节数组方式上传到 OSS（供程序化生成内容，如头像库初始化）
     *
     * @param data 文件字节
     * @param ext  文件扩展名（不含点，如 svg/png/jpg）
     * @return 可访问的公网 URL
     */
    String upload(byte[] data, String ext);
}
