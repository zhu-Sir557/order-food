package com.restaurant.service.impl;

import com.restaurant.service.SmsCodeStore;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RedisSmsCodeStore 单元测试（mock StringRedisTemplate）。
 *
 * <p>验证 save/get/delete/exists 使用正确的 Key（前缀 sms:code:）与 TTL，
 * 以及 SmsCodeStore.maskPhone 脱敏格式。</p>
 */
@ExtendWith(MockitoExtension.class)
class RedisSmsCodeStoreTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> ops;

    RedisSmsCodeStore store;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(ops);
        store = new RedisSmsCodeStore(redisTemplate);
    }

    @Test
    @DisplayName("save: key=sms:code:{phone}, TTL=codeTtlSeconds")
    void save() {
        store.save("13800001111", "123456", 300);
        verify(ops).set(eq("sms:code:13800001111"), eq("123456"), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("get: 返回存储的明文")
    void get() {
        when(ops.get("sms:code:13800001111")).thenReturn("123456");
        assertEquals("123456", store.get("13800001111"));
    }

    @Test
    @DisplayName("delete: 删除对应 key")
    void delete() {
        store.delete("13800001111");
        verify(redisTemplate).delete("sms:code:13800001111");
    }

    @Test
    @DisplayName("exists: hasKey → true/false")
    void exists() {
        when(redisTemplate.hasKey("sms:code:13800001111")).thenReturn(true);
        assertTrue(store.exists("13800001111"));
        when(redisTemplate.hasKey("sms:code:13800001111")).thenReturn(false);
        assertFalse(store.exists("13800001111"));
    }

    @Test
    @DisplayName("maskPhone: 脱敏格式 138****1111（中间4位打码）")
    void maskPhone() {
        assertEquals("138****1111", SmsCodeStore.maskPhone("13800001111"));
        assertEquals("138****0000", SmsCodeStore.maskPhone("13812340000"));
        assertEquals("****", SmsCodeStore.maskPhone(null));
        assertEquals("****", SmsCodeStore.maskPhone("123"));
        assertEquals("123****4567", SmsCodeStore.maskPhone("1234567"));
    }
}
