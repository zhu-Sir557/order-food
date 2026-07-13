package com.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.common.BizException;
import com.restaurant.common.ResultCode;
import com.restaurant.dto.MemberLoginDTO;
import com.restaurant.dto.MemberRegisterDTO;
import com.restaurant.entity.Member;
import com.restaurant.mapper.MemberMapper;
import com.restaurant.service.CaptchaService;
import com.restaurant.service.LoginDefenseService;
import com.restaurant.service.MemberAuthService;
import com.restaurant.service.MemberProfileService;
import com.restaurant.service.SmsAuthService;
import com.restaurant.service.SmsCodeStore;
import com.restaurant.util.JwtUtil;
import com.restaurant.vo.MemberLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 会员认证服务实现（统一登录分发）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAuthServiceImpl implements MemberAuthService {

    /** 登录方式：账号名 + 密码 */
    public static final String LOGIN_TYPE_ACCOUNT_PASSWORD = "ACCOUNT_PASSWORD";

    /** 登录方式：手机号 + 验证码 */
    public static final String LOGIN_TYPE_PHONE_CODE = "PHONE_CODE";

    /** 登录方式：手机号 + 密码 */
    public static final String LOGIN_TYPE_PHONE_PASSWORD = "PHONE_PASSWORD";

    /** 登录方式：账号名 + 验证码 */
    public static final String LOGIN_TYPE_ACCOUNT_CODE = "ACCOUNT_CODE";

    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    private final MemberMapper memberMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;
    private final LoginDefenseService loginDefenseService;
    private final SmsAuthService smsAuthService;
    private final SmsCodeStore smsCodeStore;
    private final MemberProfileService memberProfileService;

    @Override
    public MemberLoginVO register(MemberRegisterDTO dto, Long tempUserId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUsername, dto.getUsername());
        Long existCount = memberMapper.selectCount(wrapper);
        if (existCount > 0) {
            throw new BizException("账户名已存在");
        }

        Member member = new Member();
        member.setUsername(dto.getUsername());
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setBalance(BigDecimal.ZERO);
        member.setTempUserId(tempUserId);
        // F3：注册默认昵称 + 随机头像
        member.setNickname(memberProfileService.generateDefaultNickname());
        member.setAvatar(memberProfileService.randomAvatarUrl());
        memberMapper.insert(member);

        log.info("会员注册成功: username={}, memberId={}", dto.getUsername(), member.getId());
        return buildLoginVO(member);
    }

    @Override
    public MemberLoginVO login(MemberLoginDTO dto, HttpServletRequest request) {
        // 滑块校验（四种组合均需要）
        if (!captchaService.verifyAndConsumeCaptcha(dto.getCaptchaToken())) {
            throw new BizException(ResultCode.CAPTCHA_INVALID, "验证码无效或未通过");
        }

        String ip = resolveClientIp(request);
        String account = normalizeAccount(dto.getAccount());

        // 锁定态优先拦截（账号 + IP 双维度）
        if (loginDefenseService.isAccountLocked(account) || loginDefenseService.isIpLocked(ip)) {
            throw new BizException(ResultCode.LOGIN_LOCKED, "账号或网络被锁定，请稍后再试");
        }

        // PHONE_CODE 由 SmsAuthService 完成「验证码匹配 + 自动注册/老账号回填」并直接返回 VO
        if (LOGIN_TYPE_PHONE_CODE.equals(dto.getLoginType())) {
            MemberLoginVO vo = smsAuthService.phoneCodeLogin(account, dto.getCode(), ip);
            loginDefenseService.resetOnSuccess(account, ip);
            return vo;
        }

        Member member;
        switch (dto.getLoginType()) {
            case LOGIN_TYPE_ACCOUNT_PASSWORD:
                member = loginByAccountPassword(account, dto.getPassword(), ip);
                break;
            case LOGIN_TYPE_PHONE_PASSWORD:
                member = loginByPhonePassword(account, dto.getPassword(), ip);
                break;
            case LOGIN_TYPE_ACCOUNT_CODE:
                member = loginByAccountCode(account, dto.getCode(), ip);
                break;
            default:
                throw new BizException(ResultCode.PARAM_ERROR, "不支持的登录方式");
        }

        // 成功后重置失败计数
        loginDefenseService.resetOnSuccess(account, ip);
        return buildLoginVO(member);
    }

    private Member loginByAccountPassword(String account, String password, String ip) {
        if (password == null || password.isBlank()) {
            loginDefenseService.onLoginFail(account, ip);
            throw new BizException(ResultCode.PARAM_ERROR, "请输入密码");
        }
        Member member = memberMapper.selectOne(new LambdaQueryWrapper<Member>()
                .eq(Member::getUsername, account)
                .eq(Member::getDeleted, 0));
        if (member == null || member.getPassword() == null
                || !passwordEncoder.matches(password, member.getPassword())) {
            loginDefenseService.onLoginFail(account, ip);
            throw new BizException("账户名或密码错误");
        }
        log.info("会员登录成功(账号密码): memberId={}", member.getId());
        return member;
    }

    private Member loginByPhonePassword(String account, String password, String ip) {
        if (!account.matches(PHONE_REGEX)) {
            throw new BizException(ResultCode.SMS_PHONE_INVALID, "请输入正确的手机号");
        }
        if (password == null || password.isBlank()) {
            loginDefenseService.onLoginFail(account, ip);
            throw new BizException(ResultCode.PARAM_ERROR, "请输入密码");
        }
        Member member = memberMapper.selectOne(new LambdaQueryWrapper<Member>()
                .eq(Member::getPhone, account)
                .eq(Member::getDeleted, 0));
        if (member == null) {
            loginDefenseService.onLoginFail(account, ip);
            throw new BizException("手机号或密码错误");
        }
        if (member.getPassword() == null) {
            throw new BizException(ResultCode.PASSWORD_NOT_SET, "尚未设置密码，请先设置密码");
        }
        if (!passwordEncoder.matches(password, member.getPassword())) {
            loginDefenseService.onLoginFail(account, ip);
            throw new BizException("手机号或密码错误");
        }
        log.info("会员登录成功(手机号密码): memberId={}", member.getId());
        return member;
    }

    private Member loginByAccountCode(String account, String code, String ip) {
        if (code == null || code.isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR, "请输入验证码");
        }
        Member member = memberMapper.selectOne(new LambdaQueryWrapper<Member>()
                .eq(Member::getUsername, account)
                .eq(Member::getDeleted, 0));
        if (member == null) {
            throw new BizException(ResultCode.ACCOUNT_NOT_FOUND, "账号不存在");
        }
        if (member.getPhone() == null) {
            throw new BizException(ResultCode.ACCOUNT_NO_PHONE, "账号未绑定手机号，请先绑定手机");
        }
        String stored = smsCodeStore.get(member.getPhone());
        if (stored == null || stored.isBlank()) {
            throw new BizException(ResultCode.SMS_CODE_EXPIRED, "验证码已过期，请重新获取");
        }
        if (!stored.equals(code)) {
            smsCodeStore.delete(member.getPhone());
            loginDefenseService.onLoginFail(account, ip);
            throw new BizException(ResultCode.SMS_CODE_ERROR, "验证码错误");
        }
        smsCodeStore.delete(member.getPhone());
        log.info("会员登录成功(账号验证码): memberId={}", member.getId());
        return member;
    }

    private String normalizeAccount(String account) {
        return account == null ? "" : account.trim();
    }

    private MemberLoginVO buildLoginVO(Member member) {
        String token = generateMemberToken(member);
        MemberLoginVO vo = new MemberLoginVO();
        vo.setToken(token);
        vo.setMemberId(member.getId());
        vo.setUsername(member.getUsername());
        vo.setBalance(member.getBalance());
        vo.setNickname(member.getNickname());
        vo.setAvatar(member.getAvatar());
        return vo;
    }

    private String generateMemberToken(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "MEMBER");
        if (member.getTempUserId() != null) {
            claims.put("tempUserId", member.getTempUserId());
        }
        return jwtUtil.generateToken(String.valueOf(member.getId()), claims, 720);
    }

    private static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            for (String part : xff.split(",")) {
                String ip = part.trim();
                if (!ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                    return ip;
                }
            }
        }
        return request.getRemoteAddr();
    }
}
