package com.restaurant.service.impl;

import com.restaurant.service.SmsCodeStore;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 基于 Redis（StringRedisTemplate）的短信验证码存储实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSmsCodeStore implements SmsCodeStore {

    private static final String KEY_PREFIX = "sms:code:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String phone, String code, int ttlSeconds) {
        redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public String get(String phone) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + phone);
    }

    @Override
    public void delete(String phone) {
        redisTemplate.delete(KEY_PREFIX + phone);
    }

    @Override
    public boolean exists(String phone) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + phone));
    }
}
