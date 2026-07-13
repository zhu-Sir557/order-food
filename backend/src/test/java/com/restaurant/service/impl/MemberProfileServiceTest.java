package com.restaurant.service.impl;

import com.restaurant.common.BizException;
import com.restaurant.common.ResultCode;
import com.restaurant.config.PasswordProperties;
import com.restaurant.config.ProfileProperties;
import com.restaurant.dto.BindPhoneDTO;
import com.restaurant.dto.SendLoginCodeDTO;
import com.restaurant.dto.SetPasswordDTO;
import com.restaurant.dto.SmsSendRequest;
import com.restaurant.dto.UpdateAvatarDTO;
import com.restaurant.dto.UpdateNicknameDTO;
import com.restaurant.entity.Avatar;
import com.restaurant.entity.Member;
import com.restaurant.mapper.AvatarMapper;
import com.restaurant.mapper.MemberMapper;
import com.restaurant.service.CaptchaService;
import com.restaurant.service.SmsAuthService;
import com.restaurant.service.SmsCodeStore;
import com.restaurant.vo.ChangeLimitVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MemberProfileServiceImpl 单元测试（mock 所有外部依赖，不依赖 Redis / MySQL 真实实例）。
 *
 * <p>覆盖 F1 绑手机前置校验 / 设密码 BCrypt 与长度校验，以及 F3 昵称/头像每日次数 Redis 计数拦截。</p>
 */
@ExtendWith(MockitoExtension.class)
class MemberProfileServiceTest {

    @Mock MemberMapper memberMapper;
    @Mock AvatarMapper avatarMapper;
    @Mock SmsCodeStore smsCodeStore;
    @Mock SmsRateLimiter rateLimiter;
    @Mock CaptchaService captchaService;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> ops;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock SmsAuthService smsAuthService;

    final ProfileProperties profileProperties = new ProfileProperties();
    final PasswordProperties passwordProperties = new PasswordProperties();

    MemberProfileServiceImpl service;

    static final Long MEMBER_ID = 1L;
    static final String PHONE = "13800001111";
    static final String CAPTCHA_TOKEN = "captcha-token";
    static final String CODE = "123456";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(ops);
        service = new MemberProfileServiceImpl(memberMapper, avatarMapper, smsCodeStore,
                rateLimiter, captchaService, redisTemplate, passwordEncoder,
                profileProperties, passwordProperties, smsAuthService);
    }

    /* ===================== 绑手机 bindPhone (F1) ===================== */

    @Test
    @DisplayName("bindPhone: 滑块校验失败 → CAPTCHA_INVALID")
    void bindPhone_captchaInvalid() {
        when(captchaService.verifyAndConsumeCaptcha(CAPTCHA_TOKEN)).thenReturn(false);
        BindPhoneDTO dto = bindDto(PHONE, CODE);

        BizException ex = assertThrows(BizException.class, () -> service.bindPhone(MEMBER_ID, dto));
        assertEquals(ResultCode.CAPTCHA_INVALID.getCode(), ex.getCode());
        verify(memberMapper, never()).selectOne(any());
    }

    @Test
    @DisplayName("bindPhone: 手机号格式非法 → SMS_PHONE_INVALID")
    void bindPhone_phoneInvalid() {
        when(captchaService.verifyAndConsumeCaptcha(CAPTCHA_TOKEN)).thenReturn(true);
        BindPhoneDTO dto = bindDto("123", CODE);

        BizException ex = assertThrows(BizException.class, () -> service.bindPhone(MEMBER_ID, dto));
        assertEquals(ResultCode.SMS_PHONE_INVALID.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("bindPhone: 手机号已被其他账号注册 → PHONE_ALREADY_REGISTERED")
    void bindPhone_phoneRegistered() {
        when(captchaService.verifyAndConsumeCaptcha(CAPTCHA_TOKEN)).thenReturn(true);
        Member other = new Member();
        other.setId(2L);
        when(memberMapper.selectOne(any())).thenReturn(other);
        BindPhoneDTO dto = bindDto(PHONE, CODE);

        BizException ex = assertThrows(BizException.class, () -> service.bindPhone(MEMBER_ID, dto));
        assertEquals(ResultCode.PHONE_ALREADY_REGISTERED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("bindPhone: 账号不存在 → ACCOUNT_NOT_FOUND")
    void bindPhone_memberNotFound() {
        when(captchaService.verifyAndConsumeCaptcha(CAPTCHA_TOKEN)).thenReturn(true);
        when(memberMapper.selectOne(any())).thenReturn(null);
        when(memberMapper.selectById(MEMBER_ID)).thenReturn(null);
        BindPhoneDTO dto = bindDto(PHONE, CODE);

        BizException ex = assertThrows(BizException.class, () -> service.bindPhone(MEMBER_ID, dto));
        assertEquals(ResultCode.ACCOUNT_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("bindPhone: 已绑定手机（phone 非空）→ PHONE_ALREADY_BOUND")
    void bindPhone_alreadyBound() {
        when(captchaService.verifyAndConsumeCaptcha(CAPTCHA_TOKEN)).thenReturn(true);
        when(memberMapper.selectOne(any())).thenReturn(null);
        Member member = newMember(null);
        member.setPhone("13800002222");
        when(memberMapper.selectById(MEMBER_ID)).thenReturn(member);
        BindPhoneDTO dto = bindDto(PHONE, CODE);

        BizException ex = assertThrows(BizException.class, () -> service.bindPhone(MEMBER_ID, dto));
        assertEquals(ResultCode.PHONE_ALREADY_BOUND.getCode(), ex.getCode());
        verify(smsCodeStore, never()).get(anyString());
    }

    @Test
    @DisplayName("bindPhone: 短信验证码过期（无存储）→ SMS_CODE_EXPIRED")
    void bindPhone_codeExpired() {
        when(captchaService.verifyAndConsumeCaptcha(CAPTCHA_TOKEN)).thenReturn(true);
        when(memberMapper.selectOne(any())).thenReturn(null);
        when(memberMapper.selectById(MEMBER_ID)).thenReturn(newMember(null));
        when(smsCodeStore.get(PHONE)).thenReturn(null);
        BindPhoneDTO dto = bindDto(PHONE, CODE);

        BizException ex = assertThrows(BizException.class, () -> service.bindPhone(MEMBER_ID, dto));
        assertEquals(ResultCode.SMS_CODE_EXPIRED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("bindPhone: 短信验证码错误 → 调 rateLimiter.onVerifyFail 且抛 SMS_CODE_ERROR")
    void bindPhone_codeWrong() {
        when(captchaService.verifyAndConsumeCaptcha(CAPTCHA_TOKEN)).thenReturn(true);
        when(memberMapper.selectOne(any())).thenReturn(null);
        when(memberMapper.selectById(MEMBER_ID)).thenReturn(newMember(null));
        when(smsCodeStore.get(PHONE)).thenReturn("999999");
        BindPhoneDTO dto = bindDto(PHONE, CODE);

        BizException ex = assertThrows(BizException.class, () -> service.bindPhone(MEMBER_ID, dto));
        assertEquals(ResultCode.SMS_CODE_ERROR.getCode(), ex.getCode());
        verify(rateLimiter).onVerifyFail(PHONE);
    }

    @Test
    @DisplayName("bindPhone: 全部通过 → 写回手机号并删除验证码")
    void bindPhone_success() {
        when(captchaService.verifyAndConsumeCaptcha(CAPTCHA_TOKEN)).thenReturn(true);
        when(memberMapper.selectOne(any())).thenReturn(null);
        Member member = newMember(null);
        when(memberMapper.selectById(MEMBER_ID)).thenReturn(member);
        when(smsCodeStore.get(PHONE)).thenReturn(CODE);

        service.bindPhone(MEMBER_ID, bindDto(PHONE, CODE));

        assertEquals(PHONE, member.getPhone());
        verify(memberMapper).updateById(member);
        verify(smsCodeStore).delete(PHONE);
    }

    /* ===================== 设密码 setPassword (F1) ===================== */

    @Test
    @DisplayName("setPassword: 滑块校验失败 → CAPTCHA_INVALID")
    void setPassword_captchaInvalid() {
        when(captchaService.verifyAndConsumeCaptcha(CAPTCHA_TOKEN)).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> service.setPassword(MEMBER_ID, setPwdDto("12345678")));
        assertEquals(ResultCode.CAPTCHA_INVALID.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("setPassword: 账号不存在 → ACCOUNT_NOT_FOUND")
    void setPassword_memberNotFound() {
        when(captchaService.verifyAndConsumeCaptcha(CAPTCHA_TOKEN)).thenReturn(true);
        when(memberMapper.selectById(MEMBER_ID)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> service.setPassword(MEMBER_ID, setPwdDto("12345678")));
        assertEquals(ResultCode.ACCOUNT_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("setPassword: 已设密码 → PASSWORD_ALREADY_SET")
    void setPassword_alreadySet() {
        when(captchaService.verifyAndConsumeCaptcha(CAPTCHA_TOKEN)).thenReturn(true);
        Member member = newMember(null);
        member.setPassword("EXIST_ENC");
        when(memberMapper.selectById(MEMBER_ID)).thenReturn(member);

        BizException ex = assertThrows(BizException.class,
                () -> service.setPassword(MEMBER_ID, setPwdDto("12345678")));
        assertEquals(ResultCode.PASSWORD_ALREADY_SET.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("setPassword: 密码过短(<8) → PARAM_ERROR")
    void setPassword_tooShort() {
        when(captchaService.verifyAndConsumeCaptcha(CAPTCHA_TOKEN)).thenReturn(true);
        when(memberMapper.selectById(MEMBER_ID)).thenReturn(newMember(null));

        BizException ex = assertThrows(BizException.class,
                () -> service.setPassword(MEMBER_ID, setPwdDto("123")));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("setPassword: 全部通过 → BCrypt 加密并写回")
    void setPassword_success() {
        when(captchaService.verifyAndConsumeCaptcha(CAPTCHA_TOKEN)).thenReturn(true);
        Member member = newMember(null);
        when(memberMapper.selectById(MEMBER_ID)).thenReturn(member);
        when(passwordEncoder.encode("12345678")).thenReturn("ENC_12345678");

        service.setPassword(MEMBER_ID, setPwdDto("12345678"));

        assertEquals("ENC_12345678", member.getPassword());
        verify(passwordEncoder).encode("12345678");
        verify(memberMapper).updateById(member);
    }

    /* ===================== 昵称 updateNickname (F3) ===================== */

    @Test
    @DisplayName("updateNickname: 昵称为空 → NICK_TOO_LONG")
    void updateNickname_null() {
        UpdateNicknameDTO dto = new UpdateNicknameDTO();
        dto.setNickname(null);
        BizException ex = assertThrows(BizException.class, () -> service.updateNickname(MEMBER_ID, dto));
        assertEquals(ResultCode.NICK_TOO_LONG.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("updateNickname: 昵称超 20 字 → NICK_TOO_LONG")
    void updateNickname_tooLong() {
        UpdateNicknameDTO dto = new UpdateNicknameDTO();
        dto.setNickname("一二三四五六七八九十壹贰叁肆伍陆柒捌玖拾plus");
        BizException ex = assertThrows(BizException.class, () -> service.updateNickname(MEMBER_ID, dto));
        assertEquals(ResultCode.NICK_TOO_LONG.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("updateNickname: 当日次数达上限(count=4>limit3) → NICK_CHANGE_LIMIT 且不写库")
    void updateNickname_limitExceeded() {
        when(ops.increment(anyString())).thenReturn(4L);
        UpdateNicknameDTO dto = new UpdateNicknameDTO();
        dto.setNickname("新昵称");

        BizException ex = assertThrows(BizException.class, () -> service.updateNickname(MEMBER_ID, dto));
        assertEquals(ResultCode.NICK_CHANGE_LIMIT.getCode(), ex.getCode());
        verify(memberMapper, never()).selectById(any());
    }

    @Test
    @DisplayName("updateNickname: 成功(count=1) → 写回昵称，剩余=limit-count=2")
    void updateNickname_success() {
        when(ops.increment(anyString())).thenReturn(1L);
        Member member = newMember(null);
        when(memberMapper.selectById(MEMBER_ID)).thenReturn(member);
        UpdateNicknameDTO dto = new UpdateNicknameDTO();
        dto.setNickname("新昵称");

        ChangeLimitVO vo = service.updateNickname(MEMBER_ID, dto);
        assertEquals("新昵称", member.getNickname());
        verify(memberMapper).updateById(member);
        assertEquals((Integer) (profileProperties.getNickDailyLimit() - 1), vo.getRemaining());
    }

    @Test
    @DisplayName("updateNickname: 边界(count=limit=3) 仍允许，剩余=0")
    void updateNickname_atLimit() {
        when(ops.increment(anyString())).thenReturn(3L);
        Member member = newMember(null);
        when(memberMapper.selectById(MEMBER_ID)).thenReturn(member);
        UpdateNicknameDTO dto = new UpdateNicknameDTO();
        dto.setNickname("第三次昵称");

        ChangeLimitVO vo = service.updateNickname(MEMBER_ID, dto);
        assertEquals(0, vo.getRemaining());
    }

    /* ===================== 头像 updateAvatar (F3) ===================== */

    @Test
    @DisplayName("updateAvatar: 头像不存在 → AVATAR_NOT_FOUND")
    void updateAvatar_notFound() {
        when(avatarMapper.selectById(10L)).thenReturn(null);
        UpdateAvatarDTO dto = new UpdateAvatarDTO();
        dto.setAvatarId(10L);
        BizException ex = assertThrows(BizException.class, () -> service.updateAvatar(MEMBER_ID, dto));
        assertEquals(ResultCode.AVATAR_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("updateAvatar: 头像已删除 → AVATAR_NOT_FOUND")
    void updateAvatar_deleted() {
        Avatar avatar = new Avatar();
        avatar.setDeleted(1);
        when(avatarMapper.selectById(10L)).thenReturn(avatar);
        UpdateAvatarDTO dto = new UpdateAvatarDTO();
        dto.setAvatarId(10L);
        BizException ex = assertThrows(BizException.class, () -> service.updateAvatar(MEMBER_ID, dto));
        assertEquals(ResultCode.AVATAR_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("updateAvatar: 当日次数达上限(count=6>limit5) → AVATAR_CHANGE_LIMIT 且不写库")
    void updateAvatar_limitExceeded() {
        Avatar avatar = new Avatar();
        avatar.setId(10L);
        avatar.setOssUrl("http://oss/a.svg");
        avatar.setDeleted(0);
        when(avatarMapper.selectById(10L)).thenReturn(avatar);
        when(ops.increment(anyString())).thenReturn(6L);
        UpdateAvatarDTO dto = new UpdateAvatarDTO();
        dto.setAvatarId(10L);

        BizException ex = assertThrows(BizException.class, () -> service.updateAvatar(MEMBER_ID, dto));
        assertEquals(ResultCode.AVATAR_CHANGE_LIMIT.getCode(), ex.getCode());
        verify(memberMapper, never()).selectById(any());
    }

    @Test
    @DisplayName("updateAvatar: 成功 → 写回头像 ossUrl，剩余=limit-count=4")
    void updateAvatar_success() {
        Avatar avatar = new Avatar();
        avatar.setId(10L);
        avatar.setOssUrl("http://oss/a.svg");
        avatar.setDeleted(0);
        when(avatarMapper.selectById(10L)).thenReturn(avatar);
        when(ops.increment(anyString())).thenReturn(1L);
        Member member = newMember(null);
        when(memberMapper.selectById(MEMBER_ID)).thenReturn(member);
        UpdateAvatarDTO dto = new UpdateAvatarDTO();
        dto.setAvatarId(10L);

        ChangeLimitVO vo = service.updateAvatar(MEMBER_ID, dto);
        assertEquals("http://oss/a.svg", member.getAvatar());
        verify(memberMapper).updateById(member);
        assertEquals((Integer) (profileProperties.getAvatarDailyLimit() - 1), vo.getRemaining());
    }

    /* ===================== 账号名+验证码 发码 sendLoginCode (F1) ===================== */

    @Test
    @DisplayName("sendLoginCode: 账号不存在 → ACCOUNT_NOT_FOUND")
    void sendLoginCode_memberNotFound() {
        when(memberMapper.selectOne(any())).thenReturn(null);
        SendLoginCodeDTO dto = new SendLoginCodeDTO();
        dto.setAccount("nope");
        dto.setCaptchaToken(CAPTCHA_TOKEN);

        BizException ex = assertThrows(BizException.class, () -> service.sendLoginCode(dto, "1.1.1.1"));
        assertEquals(ResultCode.ACCOUNT_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("sendLoginCode: 账号未绑手机 → ACCOUNT_NO_PHONE")
    void sendLoginCode_noPhone() {
        Member member = newMember(null);
        when(memberMapper.selectOne(any())).thenReturn(member);
        SendLoginCodeDTO dto = new SendLoginCodeDTO();
        dto.setAccount("user1");
        dto.setCaptchaToken(CAPTCHA_TOKEN);

        BizException ex = assertThrows(BizException.class, () -> service.sendLoginCode(dto, "1.1.1.1"));
        assertEquals(ResultCode.ACCOUNT_NO_PHONE.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("sendLoginCode: 成功 → 调用 smsAuthService.sendCode(phone, captchaToken)")
    void sendLoginCode_success() {
        Member member = newMember(PHONE);
        when(memberMapper.selectOne(any())).thenReturn(member);
        SendLoginCodeDTO dto = new SendLoginCodeDTO();
        dto.setAccount("user1");
        dto.setCaptchaToken(CAPTCHA_TOKEN);

        service.sendLoginCode(dto, "1.1.1.1");

        ArgumentCaptor<SmsSendRequest> captor = ArgumentCaptor.forClass(SmsSendRequest.class);
        verify(smsAuthService).sendCode(captor.capture(), eq("1.1.1.1"));
        assertEquals(PHONE, captor.getValue().getPhone());
        assertEquals(CAPTCHA_TOKEN, captor.getValue().getCaptchaToken());
    }

    /* ===================== 默认昵称 (F3 注册默认) ===================== */

    @Test
    @DisplayName("generateDefaultNickname: 前缀『美食家』+ 8 位，总长 11")
    void generateDefaultNickname() {
        String nick = service.generateDefaultNickname();
        assertNotNull(nick);
        assertTrue(nick.startsWith("美食家"));
        assertEquals(11, nick.length());
    }

    /* ===================== 辅助方法 ===================== */

    private Member newMember(String phone) {
        Member m = new Member();
        m.setId(MEMBER_ID);
        m.setUsername("user1");
        m.setPhone(phone);
        m.setPassword(null);
        return m;
    }

    private BindPhoneDTO bindDto(String phone, String code) {
        BindPhoneDTO dto = new BindPhoneDTO();
        dto.setPhone(phone);
        dto.setCode(code);
        dto.setCaptchaToken(CAPTCHA_TOKEN);
        return dto;
    }

    private SetPasswordDTO setPwdDto(String pwd) {
        SetPasswordDTO dto = new SetPasswordDTO();
        dto.setPassword(pwd);
        dto.setCaptchaToken(CAPTCHA_TOKEN);
        return dto;
    }
}
