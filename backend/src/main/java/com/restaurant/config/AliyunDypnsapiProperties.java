package com.restaurant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云号码认证服务（Dypnsapi）配置项
 *
 * <p>对应配置前缀 {@code aliyun.dypnsapi.*}，AK/SK 复用同 RAM 用户的 OSS 凭证。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.dypnsapi")
public class AliyunDypnsapiProperties {

    /** AccessKey ID */
    private String accessKeyId;

    /** AccessKey Secret */
    private String accessKeySecret;

    /** 短信签名（阿里云控制台「赠送签名」） */
    private String signName;

    /** 短信模板 CODE（阿里云控制台「赠送模板」） */
    private String templateCode;

    /** 服务接入点 */
    private String endpoint = "dypnsapi.aliyuncs.com";
}
