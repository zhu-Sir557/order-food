package com.restaurant.service.impl;

import com.restaurant.config.LoginDefenseProperties;
import com.restaurant.service.LoginDefenseService;
import com.restaurant.service.LoginFailResult;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 登录防暴破服务实现（基于 Redis）
 *
 * <p>Redis Key 规范见设计文档 §7：</p>
 * <ul>
 *   <li>{@code login:fail:{account}}（账号失败计数，TTL=lockMinutes）</li>
 *   <li>{@code login:lock:{account}}（账号锁定占位，TTL=lockMinutes）</li>
 *   <li>{@code login:fail:ip:{ip}}（IP 失败计数，TTL=lockMinutes）</li>
 *   <li>{@code login:lock:ip:{ip}}（IP 锁定占位，TTL=lockMinutes）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginDefenseServiceImpl implements LoginDefenseService {

    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String KEY_FAIL_ACCOUNT = "login:fail:";
    private static final String KEY_LOCK_ACCOUNT = "login:lock:";
    private static final String KEY_FAIL_IP = "login:fail:ip:";
    private static final String KEY_LOCK_IP = "login:lock:ip:";

    private final StringRedisTemplate redisTemplate;
    private final LoginDefenseProperties props;

    @Override
    public boolean isAccountLocked(String account) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_LOCK_ACCOUNT + account));
    }

    @Override
    public boolean isIpLocked(String ip) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_LOCK_IP + ip));
    }

    @Override
    public LoginFailResult onLoginFail(String account, String ip) {
        int max = props.getLoginFailMax();
        int lockMinutes = props.getLoginLockMinutes();

        Long accCount = redisTemplate.opsForValue().increment(KEY_FAIL_ACCOUNT + account);
        if (accCount != null && accCount == 1L) {
            redisTemplate.expire(KEY_FAIL_ACCOUNT + account, lockMinutes, TimeUnit.MINUTES);
        }
        Long ipCount = (ip == null || ip.isBlank()) ? null
                : redisTemplate.opsForValue().increment(KEY_FAIL_IP + ip);
        if (ipCount != null && ipCount == 1L) {
            redisTemplate.expire(KEY_FAIL_IP + ip, lockMinutes, TimeUnit.MINUTES);
        }

        boolean accLocked = accCount != null && accCount >= max;
        boolean ipLocked = ipCount != null && ipCount >= max;
        if (accLocked) {
            redisTemplate.opsForValue().set(KEY_LOCK_ACCOUNT + account, "1", lockMinutes, TimeUnit.MINUTES);
            log.warn("账号登录失败次数达上限已锁定: account={}", maskAccount(account));
        }
        if (ipLocked) {
            redisTemplate.opsForValue().set(KEY_LOCK_IP + ip, "1", lockMinutes, TimeUnit.MINUTES);
            log.warn("IP 登录失败次数达上限已锁定: ip={}", maskIp(ip));
        }

        int current = Math.max(
                accCount == null ? 0 : accCount.intValue(),
                ipCount == null ? 0 : ipCount.intValue());
        return new LoginFailResult(accLocked || ipLocked, current, max);
    }

    @Override
    public void resetOnSuccess(String account, String ip) {
        redisTemplate.delete(KEY_FAIL_ACCOUNT + account);
        if (ip != null && !ip.isBlank()) {
            redisTemplate.delete(KEY_FAIL_IP + ip);
        }
    }

    private static String maskAccount(String account) {
        if (account == null || account.length() < 4) {
            return "***";
        }
        // 手机号走通用脱敏，账户名仅保留首字符
        if (account.matches("^1[3-9]\\d{9}$")) {
            return com.restaurant.service.SmsCodeStore.maskPhone(account);
        }
        return account.charAt(0) + "***";
    }

    private static String maskIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return "***";
        }
        return ip;
    }
}
