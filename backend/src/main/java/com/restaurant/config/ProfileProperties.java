package com.restaurant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 会员资料（昵称/头像）相关配置项
 *
 * <p>对应配置前缀 {@code profile.*}，用于控制每日修改次数上限。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "profile")
public class ProfileProperties {

    /** 昵称每日修改次数上限（自然日） */
    private int nickDailyLimit = 3;

    /** 头像每日修改次数上限（自然日） */
    private int avatarDailyLimit = 5;
}
