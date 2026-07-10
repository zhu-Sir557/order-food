package com.restaurant.service.impl;

import com.restaurant.config.SmsRateLimitProperties;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SmsRateLimiter 限频组件单元测试（mock StringRedisTemplate）。
 *
 * <p>覆盖 canSend / onSendSuccess / onVerifyFail / isLocked 各分支，
 * 并校验 Redis Key 命名与 TTL 符合设计 §7.1。</p>
 */
@ExtendWith(MockitoExtension.class)
class SmsRateLimiterTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> ops;

    SmsRateLimitProperties props = new SmsRateLimitProperties();
    SmsRateLimiter limiter;

    static final String PHONE = "13800001111";
    static final String IP = "1.2.3.4";
    static final String TODAY = LocalDate.now(ZoneId.of("Asia/Shanghai"))
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(ops);
        limiter = new SmsRateLimiter(redisTemplate, props);
    }

    @Test
    @DisplayName("canSend: 锁定态优先 → LOCKED")
    void canSend_locked() {
        when(redisTemplate.hasKey("sms:lock:" + PHONE)).thenReturn(true);

        RateLimitResult r = limiter.canSend(PHONE, IP);
        assertTrue(r.isLocked());
        assertEquals(RateLimitResult.Reason.LOCKED, r.getReason());
    }

    @Test
    @DisplayName("canSend: 发送间隔内 → INTERVAL(remainSeconds, remainCount)")
    void canSend_interval() {
        when(redisTemplate.getExpire("sms:send:interval:" + PHONE, TimeUnit.SECONDS)).thenReturn(59L);

        RateLimitResult r = limiter.canSend(PHONE, IP);
        assertFalse(r.isAllowed());
        assertEquals(RateLimitResult.Reason.INTERVAL, r.getReason());
        assertEquals(59, r.getRemainSeconds());
        assertEquals(props.getPhoneDailyLimit(), r.getRemainCount());
    }

    @Test
    @DisplayName("canSend: 单号日上限 → PHONE_DAILY")
    void canSend_phoneDaily() {
        when(redisTemplate.getExpire(anyString(), any())).thenReturn(null);
        when(ops.get("sms:send:limit:phone:" + TODAY)).thenReturn("10");

        RateLimitResult r = limiter.canSend(PHONE, IP);
        assertEquals(RateLimitResult.Reason.PHONE_DAILY, r.getReason());
        assertEquals(0, r.getRemainCount());
    }

    @Test
    @DisplayName("canSend: 单IP日上限 → IP_DAILY（Key 含 ip 后缀）")
    void canSend_ipDaily() {
        when(redisTemplate.getExpire(anyString(), any())).thenReturn(null);
        when(ops.get("sms:send:limit:phone:" + TODAY)).thenReturn("0");
        when(ops.get("sms:send:limit:ip:" + TODAY + ":" + IP)).thenReturn("50");

        RateLimitResult r = limiter.canSend(PHONE, IP);
        assertEquals(RateLimitResult.Reason.IP_DAILY, r.getReason());
        assertEquals(0, r.getRemainCount());
    }

    @Test
    @DisplayName("canSend: 全部通过 → OK")
    void canSend_ok() {
        RateLimitResult r = limiter.canSend(PHONE, IP);
        assertTrue(r.isAllowed());
        assertEquals(RateLimitResult.Reason.OK, r.getReason());
    }

    @Test
    @DisplayName("onSendSuccess: 写间隔Key(TTL=interval)+递增单号/单IP日计数(到当日结束TTL)")
    void onSendSuccess() {
        when(ops.increment("sms:send:limit:phone:" + TODAY)).thenReturn(1L);
        when(ops.increment("sms:send:limit:ip:" + TODAY + ":" + IP)).thenReturn(1L);

        limiter.onSendSuccess(PHONE, IP);

        verify(ops).set(eq("sms:send:interval:" + PHONE), eq("1"),
                eq((long) props.getIntervalSeconds()), eq(TimeUnit.SECONDS));
        verify(ops).increment("sms:send:limit:phone:" + TODAY);
        verify(ops).increment("sms:send:limit:ip:" + TODAY + ":" + IP);
        verify(redisTemplate).expire(eq("sms:send:limit:phone:" + TODAY), anyLong(), eq(TimeUnit.SECONDS));
        verify(redisTemplate).expire(eq("sms:send:limit:ip:" + TODAY + ":" + IP), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("onVerifyFail: 首次失败 → 写failKey+TTL=lockMinutes，未锁定")
    void onVerifyFail_first() {
        when(ops.increment("sms:fail:" + PHONE)).thenReturn(1L);

        FailResult r = limiter.onVerifyFail(PHONE);
        assertFalse(r.isLocked());
        assertEquals(1, r.getCurrentCount());
        verify(redisTemplate).expire(eq("sms:fail:" + PHONE), eq((long) props.getLockMinutes()), eq(TimeUnit.MINUTES));
        verify(ops, never()).set(eq("sms:lock:" + PHONE), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("onVerifyFail: 错误超上限 → 锁定并写lockKey(TTL=lockMinutes)")
    void onVerifyFail_locked() {
        when(ops.increment("sms:fail:" + PHONE)).thenReturn(6L);

        FailResult r = limiter.onVerifyFail(PHONE);
        assertTrue(r.isLocked());
        assertEquals(6, r.getCurrentCount());
        verify(ops).set(eq("sms:lock:" + PHONE), eq("1"),
                eq((long) props.getLockMinutes()), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("onVerifyFail: 错误次数【达到】上限(failMax)即锁定（达上限锁，非超上限才锁）")
    void onVerifyFail_lockAtLimit() {
        when(ops.increment("sms:fail:" + PHONE)).thenReturn(5L);

        FailResult r = limiter.onVerifyFail(PHONE);
        assertTrue(r.isLocked(), "达到上限(5次)即应锁定");
        assertEquals(5, r.getCurrentCount());
        verify(ops).set(eq("sms:lock:" + PHONE), eq("1"),
                eq((long) props.getLockMinutes()), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("onVerifyFail: 阿里云/Redis 返回 null（降级）→ 不抛异常，当前次数取上限+1")
    void onVerifyFail_nullFallback() {
        when(ops.increment("sms:fail:" + PHONE)).thenReturn(null);

        FailResult r = limiter.onVerifyFail(PHONE);
        assertFalse(r.isLocked());
        assertEquals(props.getFailMax() + 1, r.getCurrentCount());
    }

    @Test
    @DisplayName("isLocked: 有 lockKey → true")
    void isLocked_true() {
        when(redisTemplate.hasKey("sms:lock:" + PHONE)).thenReturn(true);
        assertTrue(limiter.isLocked(PHONE));
    }

    @Test
    @DisplayName("isLocked: 无 lockKey → false")
    void isLocked_false() {
        when(redisTemplate.hasKey("sms:lock:" + PHONE)).thenReturn(false);
        assertFalse(limiter.isLocked(PHONE));
    }
}
