package com.restaurant.service.impl;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.restaurant.common.BizException;
import com.restaurant.common.ResultCode;
import com.restaurant.config.AliyunDypnsapiProperties;
import com.restaurant.config.SmsRateLimitProperties;
import com.restaurant.dto.SmsLoginRequest;
import com.restaurant.dto.SmsSendRequest;
import com.restaurant.entity.Member;
import com.restaurant.mapper.MemberMapper;
import com.restaurant.service.CaptchaService;
import com.restaurant.service.SmsCodeStore;
import com.restaurant.service.SmsAuthService;
import com.restaurant.util.JwtUtil;
import com.restaurant.vo.MemberLoginVO;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * SmsAuthServiceImpl 业务编排单元测试（Mockito 隔离全部外部依赖）。
 *
 * <p>重点验证：①手机号格式 ②Q2 滑块前置（限频之前）③限频分支
 * ④阿里云调用/统一文案 ⑤登录锁定/过期/错误/正确（自动注册/匹配会员/JWT）。</p>
 */
@ExtendWith(MockitoExtension.class)
class SmsAuthServiceImplTest {

    @Mock SmsCodeStore smsCodeStore;
    @Mock SmsRateLimiter rateLimiter;
    @Mock Client dypnsapiClient;
    @Mock MemberMapper memberMapper;
    @Mock JwtUtil jwtUtil;
    @Mock CaptchaService captchaService;

    SmsRateLimitProperties rateLimitProps = new SmsRateLimitProperties();
    AliyunDypnsapiProperties dypnsapiProperties = new AliyunDypnsapiProperties();

    SmsAuthServiceImpl service;

    @Captor ArgumentCaptor<Map<String, Object>> claimsCaptor;
    @Captor ArgumentCaptor<Integer> intCaptor;

    @BeforeEach
    void setUp() {
        dypnsapiProperties.setSignName("测试签名");
        dypnsapiProperties.setTemplateCode("SMS_TEST");
        service = new SmsAuthServiceImpl(smsCodeStore, rateLimiter, dypnsapiClient,
                memberMapper, jwtUtil, rateLimitProps, dypnsapiProperties, captchaService);
    }

    private SmsSendRequest sendReq(String phone, String token) {
        SmsSendRequest r = new SmsSendRequest();
        r.setPhone(phone);
        r.setCaptchaToken(token);
        return r;
    }

    private SmsLoginRequest loginReq(String phone, String code) {
        SmsLoginRequest r = new SmsLoginRequest();
        r.setPhone(phone);
        r.setCode(code);
        return r;
    }

    // ===================== sendCode =====================

    @Test
    @DisplayName("sendCode: 手机号非法 → SMS_PHONE_INVALID（且不触发任何外部交互）")
    void sendCode_invalidPhone() {
        BizException ex = assertThrows(BizException.class,
                () -> service.sendCode(sendReq("123", "tok"), "1.2.3.4"));
        assertEquals(ResultCode.SMS_PHONE_INVALID.getCode(), ex.getCode());
        assertEquals("请输入正确的手机号", ex.getMessage());
        verifyNoInteractions(captchaService, dypnsapiClient, rateLimiter, smsCodeStore);
    }

    @Test
    @DisplayName("Q2: captchaToken 无效 → CAPTCHA_INVALID；且不应调阿里云/计限频/存Redis")
    void sendCode_captchaInvalid() {
        when(captchaService.verifyAndConsumeCaptcha("bad")).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> service.sendCode(sendReq("13800001111", "bad"), "1.2.3.4"));

        assertEquals(ResultCode.CAPTCHA_INVALID.getCode(), ex.getCode());
        // 关键 Q2 判定：限频校验之前即失败，不应有任何外部交互
        verify(captchaService).verifyAndConsumeCaptcha("bad");
        verifyNoInteractions(rateLimiter);    // canSend / onSendSuccess 都不应调用
        verifyNoInteractions(dypnsapiClient); // 不应调阿里云
        verifyNoInteractions(smsCodeStore);   // 不应存 Redis（不应发码）
    }

    @Test
    @DisplayName("sendCode: 限频锁定 → RATE_LIMIT_LOCKED")
    void sendCode_rateLocked() {
        when(captchaService.verifyAndConsumeCaptcha("tok")).thenReturn(true);
        when(rateLimiter.canSend("13800001111", "1.2.3.4")).thenReturn(RateLimitResult.locked());

        BizException ex = assertThrows(BizException.class,
                () -> service.sendCode(sendReq("13800001111", "tok"), "1.2.3.4"));
        assertEquals(ResultCode.RATE_LIMIT_LOCKED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("sendCode: 发送间隔不足 → RATE_LIMIT_PHONE_INTERVAL（带剩余秒）")
    void sendCode_rateInterval() {
        when(captchaService.verifyAndConsumeCaptcha("tok")).thenReturn(true);
        when(rateLimiter.canSend(anyString(), anyString()))
                .thenReturn(RateLimitResult.interval(59, 10));

        BizException ex = assertThrows(BizException.class,
                () -> service.sendCode(sendReq("13800001111", "tok"), "1.2.3.4"));
        assertEquals(ResultCode.RATE_LIMIT_PHONE_INTERVAL.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("59"));
    }

    @Test
    @DisplayName("sendCode: 单号日上限 → RATE_LIMIT_PHONE_DAILY")
    void sendCode_ratePhoneDaily() {
        when(captchaService.verifyAndConsumeCaptcha("tok")).thenReturn(true);
        when(rateLimiter.canSend(anyString(), anyString())).thenReturn(RateLimitResult.phoneDaily(0));

        BizException ex = assertThrows(BizException.class,
                () -> service.sendCode(sendReq("13800001111", "tok"), "1.2.3.4"));
        assertEquals(ResultCode.RATE_LIMIT_PHONE_DAILY.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("sendCode: 单IP日上限 → RATE_LIMIT_IP_DAILY")
    void sendCode_rateIpDaily() {
        when(captchaService.verifyAndConsumeCaptcha("tok")).thenReturn(true);
        when(rateLimiter.canSend(anyString(), anyString())).thenReturn(RateLimitResult.ipDaily(0));

        BizException ex = assertThrows(BizException.class,
                () -> service.sendCode(sendReq("13800001111", "tok"), "1.2.3.4"));
        assertEquals(ResultCode.RATE_LIMIT_IP_DAILY.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("sendCode: 滑块通过且不限频 → 阿里云code=OK但回传verifyCode为空，存本地code、onSendSuccess")
    void sendCode_success() throws Exception {
        when(captchaService.verifyAndConsumeCaptcha("tok")).thenReturn(true);
        when(rateLimiter.canSend(anyString(), anyString())).thenReturn(RateLimitResult.ok());
        // 真实场景：阿里云 code=OK（短信已下发），但本账号/SDK 不回传明文 verifyCode（为空），
        // 用户短信中实际收到的验证码是我们传入 TemplateParam 的本地 code。
        SendSmsVerifyCodeResponse resp = buildResponse("");
        resp.getBody().setCode("OK");
        when(dypnsapiClient.sendSmsVerifyCode(any(SendSmsVerifyCodeRequest.class))).thenReturn(resp);

        service.sendCode(sendReq("13800001111", "tok"), "1.2.3.4");

        // 验证阿里云请求参数正确
        var reqCaptor = org.mockito.ArgumentCaptor.forClass(SendSmsVerifyCodeRequest.class);
        verify(dypnsapiClient).sendSmsVerifyCode(reqCaptor.capture());
        SendSmsVerifyCodeRequest r = reqCaptor.getValue();
        assertEquals("13800001111", r.getPhoneNumber());
        assertEquals("测试签名", r.getSignName());
        assertEquals("SMS_TEST", r.getTemplateCode());
        assertTrue(Boolean.TRUE.equals(r.getReturnVerifyCode()));

        // (a) 模板变量必须同时包含 code 与 min 两个键（用户模板含这两个变量），且取值正确
        String templateParam = r.getTemplateParam();
        assertNotNull(templateParam, "TemplateParam 不应为空");
        cn.hutool.json.JSONObject tpJson = new cn.hutool.json.JSONObject(templateParam);
        assertTrue(tpJson.containsKey("code"), "TemplateParam 必须包含 code 变量");
        assertTrue(tpJson.containsKey("min"), "TemplateParam 必须包含 min 变量");
        // 本地 code 即短信正文携带的验证码，从 TemplateParam 取出用于闭环断言
        String localCode = tpJson.getStr("code");
        String reqMin = tpJson.getStr("min");
        // (c) 本地 code 占位必须为 6 位纯数字
        assertTrue(localCode.matches("\\d{6}"), "模板 code 应为 6 位纯数字: " + localCode);
        assertEquals(String.valueOf(rateLimitProps.getCodeTtlSeconds() / 60), reqMin,
                "模板 min 应为有效分钟数(CodeTtlSeconds/60)");

        // (b) 验证码闭环：阿里云回传 verifyCode 为空，存入 Redis 的 code 必须等于本地 code
        //     （即用户短信实际收到的码，确保登录比对通过）
        var savedCodeCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(smsCodeStore).save(eq("13800001111"), savedCodeCaptor.capture(), eq(rateLimitProps.getCodeTtlSeconds()));
        String savedCode = savedCodeCaptor.getValue();
        assertEquals(localCode, savedCode,
                "回传 verifyCode 为空时，存 Redis 的 code 必须等于本地 code（用户短信收到的码）");
        assertTrue(savedCode.matches("\\d{6}"), "存储的 code 应为 6 位纯数字: " + savedCode);

        verify(rateLimiter).onSendSuccess("13800001111", "1.2.3.4");
    }

    @Test
    @DisplayName("sendCode: 阿里云 code=OK 且回传 verifyCode 有值 → 优先存回传明文（而非本地code）")
    void sendCode_success_echoedVerifyCode() throws Exception {
        when(captchaService.verifyAndConsumeCaptcha("tok")).thenReturn(true);
        when(rateLimiter.canSend(anyString(), anyString())).thenReturn(RateLimitResult.ok());
        // 假设某账号/SDK 确实回传明文验证码，则应优先采用回传值（新逻辑分支②）
        SendSmsVerifyCodeResponse resp = buildResponse("654321");
        resp.getBody().setCode("OK");
        when(dypnsapiClient.sendSmsVerifyCode(any(SendSmsVerifyCodeRequest.class))).thenReturn(resp);

        service.sendCode(sendReq("13800001111", "tok"), "1.2.3.4");

        var savedCodeCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(smsCodeStore).save(eq("13800001111"), savedCodeCaptor.capture(), eq(rateLimitProps.getCodeTtlSeconds()));
        assertEquals("654321", savedCodeCaptor.getValue(),
                "回传 verifyCode 有值时，存 Redis 的 code 应等于阿里云回传明文（优先于本地 code）");
        assertTrue(savedCodeCaptor.getValue().matches("\\d{6}"));
        verify(rateLimiter).onSendSuccess("13800001111", "1.2.3.4");
    }

    @Test
    @DisplayName("sendCode: 阿里云抛异常 → SMS_SEND_FAILED（不透传原始错误）")
    void sendCode_aliyunException() throws Exception {
        when(captchaService.verifyAndConsumeCaptcha("tok")).thenReturn(true);
        when(rateLimiter.canSend(anyString(), anyString())).thenReturn(RateLimitResult.ok());
        when(dypnsapiClient.sendSmsVerifyCode(any())).thenThrow(new RuntimeException("ak_invalid_secret_detail"));

        BizException ex = assertThrows(BizException.class,
                () -> service.sendCode(sendReq("13800001111", "tok"), "1.2.3.4"));
        assertEquals(ResultCode.SMS_SEND_FAILED.getCode(), ex.getCode());
        assertEquals("验证码发送失败，请稍后再试", ex.getMessage());
        assertFalse(ex.getMessage().contains("ak_invalid"));
        verify(smsCodeStore, never()).save(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("sendCode: 阿里云返回空验证码 → SMS_SEND_FAILED")
    void sendCode_aliyunEmptyCode() throws Exception {
        when(captchaService.verifyAndConsumeCaptcha("tok")).thenReturn(true);
        when(rateLimiter.canSend(anyString(), anyString())).thenReturn(RateLimitResult.ok());
        when(dypnsapiClient.sendSmsVerifyCode(any())).thenReturn(buildResponse("  "));

        BizException ex = assertThrows(BizException.class,
                () -> service.sendCode(sendReq("13800001111", "tok"), "1.2.3.4"));
        assertEquals(ResultCode.SMS_SEND_FAILED.getCode(), ex.getCode());
    }

    // ===================== login =====================

    @Test
    @DisplayName("login: 锁定态 → RATE_LIMIT_LOCKED（不查Redis/会员/JWT）")
    void login_locked() {
        when(rateLimiter.isLocked("13800001111")).thenReturn(true);

        BizException ex = assertThrows(BizException.class, () -> service.login(loginReq("13800001111", "123456")));
        assertEquals(ResultCode.RATE_LIMIT_LOCKED.getCode(), ex.getCode());
        verifyNoInteractions(smsCodeStore, memberMapper, jwtUtil);
    }

    @Test
    @DisplayName("login: 验证码不存在/空 → SMS_CODE_EXPIRED")
    void login_expired() {
        when(rateLimiter.isLocked(anyString())).thenReturn(false);
        when(smsCodeStore.get("13800001111")).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> service.login(loginReq("13800001111", "123456")));
        assertEquals(ResultCode.SMS_CODE_EXPIRED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("login: 验证码错误（未达上限）→ SMS_CODE_ERROR 带剩余次数")
    void login_wrongCode() {
        when(rateLimiter.isLocked(anyString())).thenReturn(false);
        when(smsCodeStore.get("13800001111")).thenReturn("999999");
        when(rateLimiter.onVerifyFail("13800001111"))
                .thenReturn(new FailResult(false, 1, rateLimitProps.getFailMax()));

        BizException ex = assertThrows(BizException.class, () -> service.login(loginReq("13800001111", "123456")));
        assertEquals(ResultCode.SMS_CODE_ERROR.getCode(), ex.getCode());
        assertEquals("验证码错误，还可尝试 4 次", ex.getMessage());
    }

    @Test
    @DisplayName("login: 验证码错误达上限 → onVerifyFail 锁定 → RATE_LIMIT_LOCKED")
    void login_wrongCodeLocked() {
        lenient().when(rateLimiter.isLocked(anyString())).thenReturn(false);
        when(smsCodeStore.get("13800001111")).thenReturn("999999");
        when(rateLimiter.onVerifyFail("13800001111"))
                .thenReturn(new FailResult(true, 6, rateLimitProps.getFailMax()));

        BizException ex = assertThrows(BizException.class, () -> service.login(loginReq("13800001111", "123456")));
        assertEquals(ResultCode.RATE_LIMIT_LOCKED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("login: 验证码正确 + 已存在会员 → 删除Redis验证码、签发JWT、返回VO（不注册）")
    void login_success_existingMember() {
        when(rateLimiter.isLocked(anyString())).thenReturn(false);
        when(smsCodeStore.get("13800001111")).thenReturn("123456");
        Member m = new Member();
        m.setId(5L);
        m.setUsername("老用户");
        m.setBalance(new BigDecimal("100"));
        when(memberMapper.selectOne(any())).thenReturn(m);
        when(jwtUtil.generateToken(anyString(), any(), anyInt())).thenReturn("jwt-token-xyz");

        MemberLoginVO vo = service.login(loginReq("13800001111", "123456"));

        verify(smsCodeStore).delete("13800001111"); // 一次性删除
        verify(memberMapper, never()).insert((Member) any()); // 不应自动注册
        verify(jwtUtil).generateToken(eq("5"), claimsCaptor.capture(), intCaptor.capture());
        assertEquals(720, intCaptor.getValue());
        assertEquals("MEMBER", claimsCaptor.getValue().get("role"));
        assertEquals("jwt-token-xyz", vo.getToken());
        assertEquals(5L, vo.getMemberId());
        assertEquals("老用户", vo.getUsername());
        assertEquals(new BigDecimal("100"), vo.getBalance());
    }

    @Test
    @DisplayName("login: 验证码正确 + 无会员 → 自动注册(username=phone,password=null,balance=0)并insert")
    void login_success_autoRegister() {
        when(rateLimiter.isLocked(anyString())).thenReturn(false);
        when(smsCodeStore.get("13800001111")).thenReturn("123456");
        when(memberMapper.selectOne(any())).thenReturn(null);
        when(jwtUtil.generateToken(anyString(), any(), anyInt())).thenReturn("jwt-new");
        // 模拟 MyBatis 自增回填 id
        when(memberMapper.insert((Member) any())).thenAnswer(inv -> {
            Member arg = inv.getArgument(0);
            arg.setId(99L);
            return 1;
        });

        MemberLoginVO vo = service.login(loginReq("13800001111", "123456"));

        verify(smsCodeStore).delete("13800001111");
        var mc = org.mockito.ArgumentCaptor.forClass(Member.class);
        verify(memberMapper).insert(mc.capture());
        Member saved = mc.getValue();
        assertEquals("13800001111", saved.getUsername());
        assertEquals("13800001111", saved.getPhone());
        assertNull(saved.getPassword());
        assertEquals(BigDecimal.ZERO, saved.getBalance());
        assertEquals("jwt-new", vo.getToken());
        assertEquals(99L, vo.getMemberId());
    }

    @Test
    @DisplayName("login: 验证码正确 + 老账号(username=手机号,phone=null) → 回填phone并直接登录(不新建)")
    void login_success_backfillOldMember() {
        when(rateLimiter.isLocked(anyString())).thenReturn(false);
        when(smsCodeStore.get("13800008888")).thenReturn("123456");

        // 老密码注册用户：username 恰好是手机号，但未存过 phone（deleted=0）
        Member old = new Member();
        old.setId(7L);
        old.setUsername("13800008888");
        old.setPhone(null);
        old.setBalance(new BigDecimal("50"));
        // 第一次 selectOne(按 phone) 查不到 → null；第二次 selectOne(按 username) 命中老账号
        when(memberMapper.selectOne(any())).thenReturn(null, old);
        when(memberMapper.updateById(any(Member.class))).thenReturn(1);
        when(jwtUtil.generateToken(anyString(), any(), anyInt())).thenReturn("jwt-backfill");

        MemberLoginVO vo = service.login(loginReq("13800008888", "123456"));

        // 关键：全新手机号才 insert；老账号回填不得新建
        verify(memberMapper, never()).insert((Member) any());
        // 关键：回填分支必须执行 updateById；且 service 改写的是同一对象引用
        verify(memberMapper).updateById(any(Member.class));
        assertEquals("13800008888", old.getPhone()); // 回填的 phone 已写入对象
        // 返回 VO 的 username 等于老账号 username（即手机号）
        assertEquals("13800008888", vo.getUsername());
        assertEquals("jwt-backfill", vo.getToken());
        assertEquals(7L, vo.getMemberId());
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
}
