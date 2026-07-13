package com.restaurant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 会员密码相关配置项
 *
 * <p>对应配置前缀 {@code password.*}。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "password")
public class PasswordProperties {

    /** 密码最小长度（设置/修改密码时校验） */
    private int minLength = 8;
}
