package com.restaurant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 登录防暴破配置项
 *
 * <p>对应配置前缀 {@code login.defense.*}，用于控制账号/IP 维度的登录失败锁定策略。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "login.defense")
public class LoginDefenseProperties {

    /** 同一账号/IP 连续登录失败多少次后锁定 */
    private int loginFailMax = 5;

    /** 锁定时长（分钟） */
    private int loginLockMinutes = 15;
}
