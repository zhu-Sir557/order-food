package com.restaurant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信验证码限频配置项
 *
 * <p>对应配置前缀 {@code sms.rate-limit.*}，所有阈值可配置，便于运营按需调整。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "sms.rate-limit")
public class SmsRateLimitProperties {

    /** 验证码在 Redis 中的 TTL（秒） */
    private int codeTtlSeconds = 300;

    /** 同手机号两次发送的最小间隔（秒） */
    private int intervalSeconds = 60;

    /** 单手机号单日发送上限 */
    private int phoneDailyLimit = 10;

    /** 单 IP 单日发送上限 */
    private int ipDailyLimit = 50;

    /** 验证码错误尝试次数上限 */
    private int failMax = 5;

    /** 达错误上限后的锁定时长（分钟） */
    private int lockMinutes = 10;

    /** 全站单日发送验证码总量上限（口径：全站合计） */
    private int globalDailyLimit = 500;
}
