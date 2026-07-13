package com.restaurant.service.impl;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.restaurant.common.BizException;
import com.restaurant.common.ResultCode;
import com.restaurant.config.AliyunDypnsapiProperties;
import com.restaurant.config.SmsRateLimitProperties;
import com.restaurant.dto.SmsSendRequest;
import com.restaurant.service.CaptchaService;
import com.restaurant.service.SmsCodeStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.json.JSONObject;

/**
 * 独立回归测试（QA）：针对 Dypnsapi SendSmsVerifyCode 修复，覆盖任务要求的补充用例。
 *
 * <p>修复核心：{@code setTemplateParam} 原先只传 {@code min} 漏传 {@code code}，
 * 导致含 {@code ${code}} 变量的用户模板报 {@code isv.INVALID_PARAMETERS}。
 * 本类从独立视角验证：
 * ① TemplateParam 同时含 {@code code} 与 {@code min} 且值为 6 位纯数字 / 5；
 * ② JSON 严格为 {@code {"code":"<6位数字>","min":"5"}}（恰好两键）；
 * ③ validMinutes 推导正确性（codeTtlSeconds=300 → min=5；600 → 10）；
 * ④ 验证码闭环：存 Redis 的 code 来自响应 {@code getVerifyCode()} 而非占位 code；
 * ⑤ 阿里云返回 null 验证码 → 统一 SMS_SEND_FAILED（不透传原始错误）。</p>
 */
@ExtendWith(MockitoExtension.class)
class SmsAuthServiceImplBugfixRegressionTest {

    @Mock SmsCodeStore smsCodeStore;
    @Mock SmsRateLimiter rateLimiter;
    @Mock Client dypnsapiClient;
    @Mock CaptchaService captchaService;

    SmsRateLimitProperties rateLimitProps = new SmsRateLimitProperties();
    AliyunDypnsapiProperties dypnsapiProperties = new AliyunDypnsapiProperties();

    SmsAuthServiceImpl service;

    @BeforeEach
    void setUp() {
        dypnsapiProperties.setSignName("测试签名");
        dypnsapiProperties.setTemplateCode("SMS_TEST");
        service = new SmsAuthServiceImpl(smsCodeStore, rateLimiter, dypnsapiClient,
                null, null, rateLimitProps, dypnsapiProperties, captchaService);
    }

    private SmsSendRequest sendReq(String phone, String token) {
        SmsSendRequest r = new SmsSendRequest();
        r.setPhone(phone);
        r.setCaptchaToken(token);
        return r;
    }

    private SendSmsVerifyCodeResponse buildResponse(String code) {
        SendSmsVerifyCodeResponse response = new SendSmsVerifyCodeResponse();
        SendSmsVerifyCodeResponseBody body = new SendSmsVerifyCodeResponseBody();
        SendSmsVerifyCodeResponseBody.SendSmsVerifyCodeResponseBodyModel model =
                new SendSmsVerifyCodeResponseBody.SendSmsVerifyCodeResponseBodyModel();
        model.setVerifyCode(code);
        body.setModel(model);
        response.setBody(body);
        return response;
    }

    // ---- ① + ② 严格 JSON 结构 ----

    @Test
    @DisplayName("回归: TemplateParam 严格为 {\"code\":\"<6位数字>\",\"min\":\"5\"}（恰好两键，无多余）")
    void templateParam_strictJson_shape() throws Exception {
        when(captchaService.verifyAndConsumeCaptcha("tok")).thenReturn(true);
        when(rateLimiter.canSend(anyString(), anyString())).thenReturn(RateLimitResult.ok());
        when(dypnsapiClient.sendSmsVerifyCode(any(SendSmsVerifyCodeRequest.class)))
                .thenReturn(buildResponse("654321")); // 故意让响应码 != 占位码

        service.sendCode(sendReq("13800001111", "tok"), "1.2.3.4");

        ArgumentCaptor<SendSmsVerifyCodeRequest> reqCaptor =
                ArgumentCaptor.forClass(SendSmsVerifyCodeRequest.class);
        verify(dypnsapiClient).sendSmsVerifyCode(reqCaptor.capture());
        String tp = reqCaptor.getValue().getTemplateParam();

        JSONObject json = new JSONObject(tp);
        // 恰好两个键，且分别为 code、min
        assertEquals(2, json.keySet().size(), "TemplateParam 应仅含 code 与 min 两键");
        assertTrue(json.containsKey("code"), "必须含 code");
        assertTrue(json.containsKey("min"), "必须含 min");
        // code 为 6 位纯数字
        String code = json.getStr("code");
        assertTrue(code.matches("\\d{6}"), "code 应为 6 位纯数字: " + code);
        // min 为 "5"（默认 codeTtlSeconds=300）
        assertEquals("5", json.getStr("min"), "min 应为字符串 \"5\"");
    }

    // ---- ③ validMinutes 推导 ----

    @Test
    @DisplayName("回归: codeTtlSeconds=300 → 模板 min=\"5\"（推导正确）")
    void validMinutes_derivation_default() throws Exception {
        rateLimitProps.setCodeTtlSeconds(300);
        when(captchaService.verifyAndConsumeCaptcha("tok")).thenReturn(true);
        when(rateLimiter.canSend(anyString(), anyString())).thenReturn(RateLimitResult.ok());
        when(dypnsapiClient.sendSmsVerifyCode(any())).thenReturn(buildResponse("123456"));

        service.sendCode(sendReq("13800001111", "tok"), "1.2.3.4");

        ArgumentCaptor<SendSmsVerifyCodeRequest> reqCaptor =
                ArgumentCaptor.forClass(SendSmsVerifyCodeRequest.class);
        verify(dypnsapiClient).sendSmsVerifyCode(reqCaptor.capture());
        JSONObject json = new JSONObject(reqCaptor.getValue().getTemplateParam());
        assertEquals(String.valueOf(300 / 60), json.getStr("min"), "300/60 应得 min=5");
        assertEquals("5", json.getStr("min"));
    }

    @Test
    @DisplayName("回归: codeTtlSeconds=600 → 模板 min=\"10\"（验证整数除法推导，非硬编码 5）")
    void validMinutes_derivation_nonDefault() throws Exception {
        rateLimitProps.setCodeTtlSeconds(600);
        when(captchaService.verifyAndConsumeCaptcha("tok")).thenReturn(true);
        when(rateLimiter.canSend(anyString(), anyString())).thenReturn(RateLimitResult.ok());
        when(dypnsapiClient.sendSmsVerifyCode(any())).thenReturn(buildResponse("123456"));

        service.sendCode(sendReq("13800001111", "tok"), "1.2.3.4");

        ArgumentCaptor<SendSmsVerifyCodeRequest> reqCaptor =
                ArgumentCaptor.forClass(SendSmsVerifyCodeRequest.class);
        verify(dypnsapiClient).sendSmsVerifyCode(reqCaptor.capture());
        JSONObject json = new JSONObject(reqCaptor.getValue().getTemplateParam());
        assertEquals(String.valueOf(600 / 60), json.getStr("min"));
        assertEquals("10", json.getStr("min"));
    }

    // ---- ④ 验证码闭环（响应码 != 占位码时，Redis 必须存响应码） ----

    @Test
    @DisplayName("回归: 闭环以响应 getVerifyCode() 为准——占位 code 与响应码不同时，Redis 存响应码")
    void codeClosure_usesResponseVerifyCode() throws Exception {
        when(captchaService.verifyAndConsumeCaptcha("tok")).thenReturn(true);
        when(rateLimiter.canSend(anyString(), anyString())).thenReturn(RateLimitResult.ok());
        // 模拟阿里云自生成验证码（与随机占位 code 不同）
        when(dypnsapiClient.sendSmsVerifyCode(any())).thenReturn(buildResponse("777888"));

        service.sendCode(sendReq("13800001111", "tok"), "1.2.3.4");

        ArgumentCaptor<String> savedCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsCodeStore).save(eq("13800001111"), savedCaptor.capture(), anyInt());
        assertEquals("777888", savedCaptor.getValue(),
                "存 Redis 的 code 必须等于响应 getVerifyCode()，而非内部占位 code");
    }

    // ---- ⑤ 阿里云返回 null 验证码 → SMS_SEND_FAILED ----

    @Test
    @DisplayName("回归: 阿里云返回 verifyCode=null → SMS_SEND_FAILED（不透传原始错误）")
    void aliyun_nullVerifyCode_throwsSmsSendFailed() throws Exception {
        when(captchaService.verifyAndConsumeCaptcha("tok")).thenReturn(true);
        when(rateLimiter.canSend(anyString(), anyString())).thenReturn(RateLimitResult.ok());
        when(dypnsapiClient.sendSmsVerifyCode(any())).thenReturn(buildResponse(null));

        BizException ex = assertThrows(BizException.class,
                () -> service.sendCode(sendReq("13800001111", "tok"), "1.2.3.4"));
        assertEquals(ResultCode.SMS_SEND_FAILED.getCode(), ex.getCode());
        assertEquals("验证码发送失败，请稍后再试", ex.getMessage());
        verify(smsCodeStore, org.mockito.Mockito.never()).save(anyString(), anyString(), anyInt());
    }
}
