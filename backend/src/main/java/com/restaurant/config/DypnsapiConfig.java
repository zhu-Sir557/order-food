package com.restaurant.config;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云号码认证服务（Dypnsapi）HTTP 客户端封装
 *
 * <p>基于 {@link AliyunDypnsapiProperties} 构建官方 SDK 的 {@link Client} Bean，
 * 供 {@code SmsAuthServiceImpl} 调用 {@code SendSmsVerifyCode} 接口发送短信验证码。</p>
 */
@Configuration
@RequiredArgsConstructor
public class DypnsapiConfig {

    private final AliyunDypnsapiProperties properties;

    /**
     * 构建 Dypnsapi Client
     *
     * @return 阿里云 Dypnsapi 官方 SDK 客户端
     * @throws Exception 当配置非法（如缺少 AK/SK/Endpoint）时由 tea-openapi 抛出
     */
    @Bean
    public Client dypnsapiClient() throws Exception {
        Config config = new Config();
        config.setAccessKeyId(properties.getAccessKeyId());
        config.setAccessKeySecret(properties.getAccessKeySecret());
        config.setEndpoint(properties.getEndpoint());
        return new Client(config);
    }
}
