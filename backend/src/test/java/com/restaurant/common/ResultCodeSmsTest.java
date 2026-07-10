package com.restaurant.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ResultCode 短信相关枚举校验：9 个新增枚举的 code/message 与设计 §7.2 一致。
 */
class ResultCodeSmsTest {

    @Test
    @DisplayName("9 个短信相关枚举存在且 code/message 正确")
    void smsResultCodes() {
        assertEquals(400, ResultCode.SMS_PHONE_INVALID.getCode());
        assertEquals("请输入正确的手机号", ResultCode.SMS_PHONE_INVALID.getMessage());

        assertEquals(400, ResultCode.CAPTCHA_INVALID.getCode());
        assertEquals("验证码无效或未通过", ResultCode.CAPTCHA_INVALID.getMessage());

        assertEquals(400, ResultCode.SMS_CODE_EXPIRED.getCode());
        assertEquals("验证码已过期，请重新获取", ResultCode.SMS_CODE_EXPIRED.getMessage());

        assertEquals(400, ResultCode.SMS_CODE_ERROR.getCode());
        assertEquals("验证码错误", ResultCode.SMS_CODE_ERROR.getMessage());

        assertEquals(429, ResultCode.SMS_SEND_FAILED.getCode());
        assertEquals("验证码发送失败，请稍后再试", ResultCode.SMS_SEND_FAILED.getMessage());

        assertEquals(429, ResultCode.RATE_LIMIT_PHONE_INTERVAL.getCode());
        assertEquals("发送过于频繁，请稍后再试", ResultCode.RATE_LIMIT_PHONE_INTERVAL.getMessage());

        assertEquals(429, ResultCode.RATE_LIMIT_PHONE_DAILY.getCode());
        assertEquals("今日验证码发送次数已达上限，请明天再试", ResultCode.RATE_LIMIT_PHONE_DAILY.getMessage());

        assertEquals(429, ResultCode.RATE_LIMIT_IP_DAILY.getCode());
        assertEquals("当前网络发送过于频繁，请稍后再试", ResultCode.RATE_LIMIT_IP_DAILY.getMessage());

        assertEquals(429, ResultCode.RATE_LIMIT_LOCKED.getCode());
        assertEquals("验证码错误次数过多，请 10 分钟后再试", ResultCode.RATE_LIMIT_LOCKED.getMessage());
    }
}
