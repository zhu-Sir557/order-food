package com.restaurant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 头像库相关配置项
 *
 * <p>对应配置前缀 {@code avatar.*}，用于头像库初始化（数量与开关）。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "avatar")
public class AvatarProperties {

    /** 头像库目标数量（初始化时生成的数量） */
    private int count = 12;

    /** 头像初始化子配置 */
    private final Init init = new Init();

    /**
     * 头像初始化开关配置
     */
    @Data
    public static class Init {
        /** 是否在应用启动时初始化头像库（本地无 OSS 凭证时可关闭） */
        private boolean enabled = true;
    }
}
