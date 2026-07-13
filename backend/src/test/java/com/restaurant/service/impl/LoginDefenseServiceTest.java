package com.restaurant.service.impl;

import com.restaurant.config.LoginDefenseProperties;
import com.restaurant.service.LoginFailResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
 * LoginDefenseServiceImpl 单元测试（mock StringRedisTemplate）。
 *
 * <p>覆盖连续失败计数累加、达阈值后锁定（账号 + IP 双维度）、锁定时长=15 分钟、
 * 以及 resetOnSuccess 重置计数。</p>
 */
@ExtendWith(MockitoExtension.class)
class LoginDefenseServiceTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> ops;

    final LoginDefenseProperties props = new LoginDefenseProperties();
    LoginDefenseServiceImpl service;

    static final String ACC = "user1";
    static final String IP = "1.2.3.4";
    static final String ACC_KEY = "login:fail:" + ACC;
    static final String IP_KEY = "login:fail:ip:" + IP;
    static final String ACC_LOCK = "login:lock:" + ACC;
    static final String IP_LOCK = "login:lock:ip:" + IP;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(ops);
        service = new LoginDefenseServiceImpl(redisTemplate, props);
    }

    @Test
    @DisplayName("onLoginFail: 未达阈值(acc=3,ip=2) → 未锁定, current=max(3,2)=3")
    void onLoginFail_notLocked() {
        when(ops.increment(ACC_KEY)).thenReturn(3L);
        when(ops.increment(IP_KEY)).thenReturn(2L);

        LoginFailResult r = service.onLoginFail(ACC, IP);
        assertFalse(r.isLocked());
        assertEquals(3, r.getCurrentCount());
        assertEquals(props.getLoginFailMax(), r.getMaxCount());
        verify(ops, never()).set(eq(ACC_LOCK), anyString(), anyLong(), any());
        verify(ops, never()).set(eq(IP_LOCK), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("onLoginFail: 账号达阈值(acc=5) → 锁账号, 锁定时长=15分钟; IP 不锁")
    void onLoginFail_accountLockedAtThreshold() {
        when(ops.increment(ACC_KEY)).thenReturn(5L);
        when(ops.increment(IP_KEY)).thenReturn(1L);

        LoginFailResult r = service.onLoginFail(ACC, IP);
        assertTrue(r.isLocked());
        assertEquals(5, r.getCurrentCount());
        verify(ops).set(eq(ACC_LOCK), eq("1"), eq((long) props.getLoginLockMinutes()), eq(TimeUnit.MINUTES));
        verify(ops, never()).set(eq(IP_LOCK), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("onLoginFail: IP 达阈值(ip=5) → 锁 IP, current=5")
    void onLoginFail_ipLockedAtThreshold() {
        when(ops.increment(ACC_KEY)).thenReturn(2L);
        when(ops.increment(IP_KEY)).thenReturn(5L);

        LoginFailResult r = service.onLoginFail(ACC, IP);
        assertTrue(r.isLocked());
        assertEquals(5, r.getCurrentCount());
        verify(ops).set(eq(IP_LOCK), eq("1"), eq((long) props.getLoginLockMinutes()), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("onLoginFail: 首次失败(count=1) → 写失败计数 TTL=15分钟, 未锁定")
    void onLoginFail_firstFailSetsTtl() {
        when(ops.increment(ACC_KEY)).thenReturn(1L);
        when(ops.increment(IP_KEY)).thenReturn(1L);

        LoginFailResult r = service.onLoginFail(ACC, IP);
        assertFalse(r.isLocked());
        assertEquals(1, r.getCurrentCount());
        verify(redisTemplate).expire(eq(ACC_KEY), eq((long) props.getLoginLockMinutes()), eq(TimeUnit.MINUTES));
        verify(ops, never()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("onLoginFail: IP 为 null/空 → 仅账号维度计数, IP 不计数不锁")
    void onLoginFail_nullIp() {
        when(ops.increment(ACC_KEY)).thenReturn(3L);

        LoginFailResult r = service.onLoginFail(ACC, null);
        assertFalse(r.isLocked());
        verify(ops, never()).increment(IP_KEY);
        verify(ops, never()).set(eq(IP_LOCK), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("isAccountLocked: 有 lockKey → true; 无 → false")
    void isAccountLocked() {
        when(redisTemplate.hasKey(ACC_LOCK)).thenReturn(true);
        assertTrue(service.isAccountLocked(ACC));
        when(redisTemplate.hasKey(ACC_LOCK)).thenReturn(false);
        assertFalse(service.isAccountLocked(ACC));
    }

    @Test
    @DisplayName("isIpLocked: 有 lockKey → true; 无 → false")
    void isIpLocked() {
        when(redisTemplate.hasKey(IP_LOCK)).thenReturn(true);
        assertTrue(service.isIpLocked(IP));
        when(redisTemplate.hasKey(IP_LOCK)).thenReturn(false);
        assertFalse(service.isIpLocked(IP));
    }

    @Test
    @DisplayName("resetOnSuccess: 删除账号与 IP 失败计数 Key")
    void resetOnSuccess() {
        service.resetOnSuccess(ACC, IP);
        verify(redisTemplate).delete(ACC_KEY);
        verify(redisTemplate).delete(IP_KEY);
    }
}
