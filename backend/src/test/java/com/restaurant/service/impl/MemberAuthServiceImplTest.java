package com.restaurant.service.impl;

import com.restaurant.common.BizException;
import com.restaurant.common.ResultCode;
import com.restaurant.dto.MemberLoginDTO;
import com.restaurant.entity.Member;
import com.restaurant.mapper.MemberMapper;
import com.restaurant.service.CaptchaService;
import com.restaurant.service.LoginDefenseService;
import com.restaurant.service.MemberProfileService;
import com.restaurant.service.SmsAuthService;
import com.restaurant.service.SmsCodeStore;
import com.restaurant.util.JwtUtil;
import com.restaurant.vo.MemberLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MemberAuthServiceImpl.login(...) 单元测试（mock 所有外部依赖）。
 *
 * <p>覆盖四种登录组合（ACCOUNT_PASSWORD / PHONE_CODE / PHONE_PASSWORD / ACCOUNT_CODE）的
 * loginType 分发、滑块前置、登录防暴破锁定拦截，以及各分支的失败/拦截路径。</p>
 */
@ExtendWith(MockitoExtension.class)
class MemberAuthServiceImplTest {

    @Mock MemberMapper memberMapper;
    @Mock JwtUtil jwtUtil;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock CaptchaService captchaService;
    @Mock LoginDefenseService loginDefenseService;
    @Mock SmsAuthService smsAuthService;
    @Mock SmsCodeStore smsCodeStore;
    @Mock MemberProfileService memberProfileService;
    @Mock HttpServletRequest request;

    MemberAuthServiceImpl service;

    static final String ACC = "user1";
    static final String PHONE = "13800001111";
    static final String IP = "1.2.3.4";
    static final String TOKEN = "captcha-token";

    @BeforeEach
    void setUp() {
        service = new MemberAuthServiceImpl(memberMapper, jwtUtil, passwordEncoder,
                captchaService, loginDefenseService, smsAuthService, smsCodeStore, memberProfileService);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn(IP);
        lenient().when(jwtUtil.generateToken(anyString(), anyMap(), anyInt())).thenReturn("jwt-token");
    }

    /* ===================== 公共前置 ===================== */

    @Test
    @DisplayName("login: 滑块校验失败 → CAPTCHA_INVALID（不查后续）")
    void login_captchaInvalid() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(false);
        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("ACCOUNT_PASSWORD", ACC, "pw", null), request));
        assertEquals(ResultCode.CAPTCHA_INVALID.getCode(), ex.getCode());
        verify(loginDefenseService, never()).isAccountLocked(anyString());
    }

    @Test
    @DisplayName("login: 账号被锁定 → LOGIN_LOCKED")
    void login_accountLocked() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(ACC)).thenReturn(true);
        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("ACCOUNT_PASSWORD", ACC, "pw", null), request));
        assertEquals(ResultCode.LOGIN_LOCKED.getCode(), ex.getCode());
        verify(smsAuthService, never()).phoneCodeLogin(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("login: IP 被锁定 → LOGIN_LOCKED")
    void login_ipLocked() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(ACC)).thenReturn(false);
        when(loginDefenseService.isIpLocked(IP)).thenReturn(true);
        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("ACCOUNT_PASSWORD", ACC, "pw", null), request));
        assertEquals(ResultCode.LOGIN_LOCKED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("login: 不支持的 loginType → PARAM_ERROR")
    void login_unsupportedType() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);
        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("FOO", ACC, "pw", null), request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
        verify(loginDefenseService, never()).onLoginFail(anyString(), anyString());
    }

    /* ===================== PHONE_CODE ===================== */

    @Test
    @DisplayName("login(PHONE_CODE): 委托 smsAuthService.phoneCodeLogin 并 resetOnSuccess")
    void login_phoneCode() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);
        MemberLoginVO vo = new MemberLoginVO();
        vo.setToken("phone-vo");
        when(smsAuthService.phoneCodeLogin(PHONE, "654321", IP)).thenReturn(vo);

        MemberLoginVO result = service.login(dto("PHONE_CODE", PHONE, null, "654321"), request);
        assertEquals(vo, result);
        verify(smsAuthService).phoneCodeLogin(PHONE, "654321", IP);
        verify(loginDefenseService).resetOnSuccess(PHONE, IP);
    }

    /* ===================== ACCOUNT_PASSWORD ===================== */

    @Test
    @DisplayName("login(ACCOUNT_PASSWORD): 密码为空 → onLoginFail + PARAM_ERROR")
    void login_accountPassword_blank() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("ACCOUNT_PASSWORD", ACC, "", null), request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
        verify(loginDefenseService).onLoginFail(ACC, IP);
        verify(memberMapper, never()).selectOne(any());
    }

    @Test
    @DisplayName("login(ACCOUNT_PASSWORD): 账号不存在 → onLoginFail + 『账户名或密码错误』")
    void login_accountPassword_notFound() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);
        when(memberMapper.selectOne(any())).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("ACCOUNT_PASSWORD", ACC, "pw", null), request));
        assertEquals("账户名或密码错误", ex.getMessage());
        verify(loginDefenseService).onLoginFail(ACC, IP);
    }

    @Test
    @DisplayName("login(ACCOUNT_PASSWORD): 密码错误 → onLoginFail + 『账户名或密码错误』")
    void login_accountPassword_wrong() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);
        when(memberMapper.selectOne(any())).thenReturn(withPassword("ENC"));
        when(passwordEncoder.matches("pw", "ENC")).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("ACCOUNT_PASSWORD", ACC, "pw", null), request));
        assertEquals("账户名或密码错误", ex.getMessage());
        verify(loginDefenseService).onLoginFail(ACC, IP);
    }

    @Test
    @DisplayName("login(ACCOUNT_PASSWORD): 成功 → 返回 VO 并 resetOnSuccess")
    void login_accountPassword_success() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);
        Member m = withPassword("ENC");
        when(memberMapper.selectOne(any())).thenReturn(m);
        when(passwordEncoder.matches("pw", "ENC")).thenReturn(true);

        MemberLoginVO vo = service.login(dto("ACCOUNT_PASSWORD", ACC, "pw", null), request);
        assertNotNull(vo);
        assertEquals("jwt-token", vo.getToken());
        verify(loginDefenseService).resetOnSuccess(ACC, IP);
        verify(loginDefenseService, never()).onLoginFail(anyString(), anyString());
    }

    /* ===================== PHONE_PASSWORD ===================== */

    @Test
    @DisplayName("login(PHONE_PASSWORD): 账号非手机号格式 → SMS_PHONE_INVALID（不记失败）")
    void login_phonePassword_invalidPhone() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("PHONE_PASSWORD", "abc", "pw", null), request));
        assertEquals(ResultCode.SMS_PHONE_INVALID.getCode(), ex.getCode());
        verify(loginDefenseService, never()).onLoginFail(anyString(), anyString());
    }

    @Test
    @DisplayName("login(PHONE_PASSWORD): 密码为空 → onLoginFail + PARAM_ERROR")
    void login_phonePassword_blank() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("PHONE_PASSWORD", PHONE, "", null), request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
        verify(loginDefenseService).onLoginFail(PHONE, IP);
    }

    @Test
    @DisplayName("login(PHONE_PASSWORD): 账号不存在 → onLoginFail + 『手机号或密码错误』")
    void login_phonePassword_notFound() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);
        when(memberMapper.selectOne(any())).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("PHONE_PASSWORD", PHONE, "pw", null), request));
        assertEquals("手机号或密码错误", ex.getMessage());
        verify(loginDefenseService).onLoginFail(PHONE, IP);
    }

    @Test
    @DisplayName("login(PHONE_PASSWORD): 尚未设密码 → PASSWORD_NOT_SET（不记失败）")
    void login_phonePassword_notSet() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);
        when(memberMapper.selectOne(any())).thenReturn(withPassword(null));

        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("PHONE_PASSWORD", PHONE, "pw", null), request));
        assertEquals(ResultCode.PASSWORD_NOT_SET.getCode(), ex.getCode());
        verify(loginDefenseService, never()).onLoginFail(anyString(), anyString());
    }

    @Test
    @DisplayName("login(PHONE_PASSWORD): 密码错误 → onLoginFail + 『手机号或密码错误』")
    void login_phonePassword_wrong() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);
        when(memberMapper.selectOne(any())).thenReturn(withPassword("ENC"));
        when(passwordEncoder.matches("pw", "ENC")).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("PHONE_PASSWORD", PHONE, "pw", null), request));
        assertEquals("手机号或密码错误", ex.getMessage());
        verify(loginDefenseService).onLoginFail(PHONE, IP);
    }

    @Test
    @DisplayName("login(PHONE_PASSWORD): 成功 → 返回 VO 并 resetOnSuccess")
    void login_phonePassword_success() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);
        Member m = withPassword("ENC");
        when(memberMapper.selectOne(any())).thenReturn(m);
        when(passwordEncoder.matches("pw", "ENC")).thenReturn(true);

        MemberLoginVO vo = service.login(dto("PHONE_PASSWORD", PHONE, "pw", null), request);
        assertNotNull(vo);
        assertEquals("jwt-token", vo.getToken());
        verify(loginDefenseService).resetOnSuccess(PHONE, IP);
    }

    /* ===================== ACCOUNT_CODE ===================== */

    @Test
    @DisplayName("login(ACCOUNT_CODE): 验证码为空 → PARAM_ERROR（不记失败）")
    void login_accountCode_blank() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("ACCOUNT_CODE", ACC, null, ""), request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
        verify(loginDefenseService, never()).onLoginFail(anyString(), anyString());
    }

    @Test
    @DisplayName("login(ACCOUNT_CODE): 账号不存在 → ACCOUNT_NOT_FOUND（不记失败）")
    void login_accountCode_notFound() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);
        when(memberMapper.selectOne(any())).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("ACCOUNT_CODE", ACC, null, "654321"), request));
        assertEquals(ResultCode.ACCOUNT_NOT_FOUND.getCode(), ex.getCode());
        verify(loginDefenseService, never()).onLoginFail(anyString(), anyString());
    }

    @Test
    @DisplayName("login(ACCOUNT_CODE): 账号未绑手机 → ACCOUNT_NO_PHONE（不记失败）")
    void login_accountCode_noPhone() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);
        Member m = withPassword("ENC");
        m.setPhone(null);
        when(memberMapper.selectOne(any())).thenReturn(m);

        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("ACCOUNT_CODE", ACC, null, "654321"), request));
        assertEquals(ResultCode.ACCOUNT_NO_PHONE.getCode(), ex.getCode());
        verify(loginDefenseService, never()).onLoginFail(anyString(), anyString());
    }

    @Test
    @DisplayName("login(ACCOUNT_CODE): 验证码过期 → SMS_CODE_EXPIRED（不记失败）")
    void login_accountCode_expired() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);
        Member m = withPassword("ENC");
        m.setPhone(PHONE);
        when(memberMapper.selectOne(any())).thenReturn(m);
        when(smsCodeStore.get(PHONE)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("ACCOUNT_CODE", ACC, null, "654321"), request));
        assertEquals(ResultCode.SMS_CODE_EXPIRED.getCode(), ex.getCode());
        verify(loginDefenseService, never()).onLoginFail(anyString(), anyString());
    }

    @Test
    @DisplayName("login(ACCOUNT_CODE): 验证码错误 → 删码 + onLoginFail + SMS_CODE_ERROR")
    void login_accountCode_wrong() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);
        Member m = withPassword("ENC");
        m.setPhone(PHONE);
        when(memberMapper.selectOne(any())).thenReturn(m);
        when(smsCodeStore.get(PHONE)).thenReturn("999999");

        BizException ex = assertThrows(BizException.class,
                () -> service.login(dto("ACCOUNT_CODE", ACC, null, "654321"), request));
        assertEquals(ResultCode.SMS_CODE_ERROR.getCode(), ex.getCode());
        verify(smsCodeStore).delete(PHONE);
        verify(loginDefenseService).onLoginFail(ACC, IP);
    }

    @Test
    @DisplayName("login(ACCOUNT_CODE): 成功 → 删码 + 返回 VO + resetOnSuccess")
    void login_accountCode_success() {
        when(captchaService.verifyAndConsumeCaptcha(TOKEN)).thenReturn(true);
        when(loginDefenseService.isAccountLocked(anyString())).thenReturn(false);
        when(loginDefenseService.isIpLocked(anyString())).thenReturn(false);
        Member m = withPassword("ENC");
        m.setPhone(PHONE);
        when(memberMapper.selectOne(any())).thenReturn(m);
        when(smsCodeStore.get(PHONE)).thenReturn("654321");

        MemberLoginVO vo = service.login(dto("ACCOUNT_CODE", ACC, null, "654321"), request);
        assertNotNull(vo);
        assertEquals("jwt-token", vo.getToken());
        verify(smsCodeStore).delete(PHONE);
        verify(loginDefenseService).resetOnSuccess(ACC, IP);
    }

    /* ===================== 辅助方法 ===================== */

    private Member withPassword(String enc) {
        Member m = new Member();
        m.setId(1L);
        m.setUsername(ACC);
        m.setPhone(PHONE);
        m.setPassword(enc);
        return m;
    }

    private MemberLoginDTO dto(String type, String account, String password, String code) {
        MemberLoginDTO d = new MemberLoginDTO();
        d.setLoginType(type);
        d.setAccount(account);
        d.setPassword(password);
        d.setCode(code);
        d.setCaptchaToken(TOKEN);
        return d;
    }
}
