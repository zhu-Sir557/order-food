package com.restaurant.service.impl;

import com.restaurant.config.SmsRateLimitProperties;
import com.restaurant.service.SmsCodeStore;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 短信发送 / 校验限频组件（基于 Redis 计数）
 *
 * <p>Redis Key 规范见设计文档 §7.1：</p>
 * <ul>
 *   <li>{@code sms:code:{phone}}（验证码，TTL=codeTtlSeconds）</li>
 *   <li>{@code sms:send:interval:{phone}}（发送间隔占位，TTL=intervalSeconds）</li>
 *   <li>{@code sms:send:limit:phone:{yyyyMMdd}}（单号日计数）</li>
 *   <li>{@code sms:send:limit:ip:{yyyyMMdd}:{ip}}（单 IP 日计数）</li>
 *   <li>{@code sms:fail:{phone}}（错误计数，TTL=lockMinutes）</li>
 *   <li>{@code sms:lock:{phone}}（锁定占位，TTL=lockMinutes）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsRateLimiter {

    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String KEY_INTERVAL = "sms:send:interval:";
    private static final String KEY_PHONE_LIMIT = "sms:send:limit:phone:";
    private static final String KEY_IP_LIMIT = "sms:send:limit:ip:";
    private static final String KEY_FAIL = "sms:fail:";
    private static final String KEY_LOCK = "sms:lock:";

    private final StringRedisTemplate redisTemplate;
    private final SmsRateLimitProperties props;

    /**
     * 判定当前手机号 + IP 是否允许发送验证码
     *
     * <p>判定顺序：①锁定 ②发送间隔 ③单号日上限 ④单 IP 日上限；全部通过则允许。</p>
     *
     * @param phone 手机号
     * @param ip    客户端 IP
     * @return 限频判定结果
     */
    public RateLimitResult canSend(String phone, String ip) {
        // ① 锁定优先
        if (isLocked(phone)) {
            return RateLimitResult.locked();
        }
        // ② 发送间隔
        Long intervalTtl = redisTemplate.getExpire(KEY_INTERVAL + phone, TimeUnit.SECONDS);
        if (intervalTtl != null && intervalTtl > 0) {
            int remainCount = Math.max(0, props.getPhoneDailyLimit() - currentCount(KEY_PHONE_LIMIT + today()));
            return RateLimitResult.interval(intervalTtl, remainCount);
        }
        // ③ 单号日上限
        int phoneCount = currentCount(KEY_PHONE_LIMIT + today());
        if (phoneCount >= props.getPhoneDailyLimit()) {
            return RateLimitResult.phoneDaily(Math.max(0, props.getPhoneDailyLimit() - phoneCount));
        }
        // ④ 单 IP 日上限
        int ipCount = currentCount(KEY_IP_LIMIT + today() + ":" + ip);
        if (ipCount >= props.getIpDailyLimit()) {
            return RateLimitResult.ipDaily(Math.max(0, props.getIpDailyLimit() - ipCount));
        }
        return RateLimitResult.ok();
    }

    /**
     * 发送成功后更新限频计数
     *
     * @param phone 手机号
     * @param ip    客户端 IP
     */
    public void onSendSuccess(String phone, String ip) {
        redisTemplate.opsForValue().set(KEY_INTERVAL + phone, "1", props.getIntervalSeconds(), TimeUnit.SECONDS);
        incrementDaily(KEY_PHONE_LIMIT + today());
        incrementDaily(KEY_IP_LIMIT + today() + ":" + ip);
    }

    /**
     * 校验失败后记录错误次数，达上限则锁定
     *
     * @param phone 手机号
     * @return 失败结果（含是否锁定与当前错误次数）
     */
    public FailResult onVerifyFail(String phone) {
        String failKey = KEY_FAIL + phone;
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1L) {
            // 首次写入时设置 TTL，与锁定窗口对齐
            redisTemplate.expire(failKey, props.getLockMinutes(), TimeUnit.MINUTES);
        }
        boolean locked = count != null && count >= props.getFailMax();
        if (locked) {
            redisTemplate.opsForValue().set(KEY_LOCK + phone, "1", props.getLockMinutes(), TimeUnit.MINUTES);
            log.warn("手机号验证码错误次数达上限已锁定: phone={}", SmsCodeStore.maskPhone(phone));
        }
        int current = (count == null) ? props.getFailMax() + 1 : count.intValue();
        return new FailResult(locked, current, props.getFailMax());
    }

    /**
     * 判断手机号是否处于锁定状态
     *
     * @param phone 手机号
     * @return 锁定中返回 {@code true}
     */
    public boolean isLocked(String phone) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_LOCK + phone));
    }

    private int currentCount(String key) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void incrementDaily(String key) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            // 首次写入：TTL 设为「当日剩余秒数」，确保日计数在凌晨归零
            redisTemplate.expire(key, secondsUntilEndOfDay(), TimeUnit.SECONDS);
        }
    }

    private String today() {
        return LocalDate.now(ZONE_SHANGHAI).format(DATE_FMT);
    }

    private long secondsUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now(ZONE_SHANGHAI);
        LocalDateTime endOfDay = LocalDate.now(ZONE_SHANGHAI).plusDays(1).atStartOfDay();
        return Duration.between(now, endOfDay).getSeconds();
    }
}
