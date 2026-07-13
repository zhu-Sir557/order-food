package com.restaurant.service.impl;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.common.BizException;
import com.restaurant.common.ResultCode;
import com.restaurant.config.AliyunDypnsapiProperties;
import com.restaurant.config.SmsRateLimitProperties;
import com.restaurant.dto.SmsLoginRequest;
import com.restaurant.dto.SmsSendRequest;
import com.restaurant.entity.Member;
import com.restaurant.mapper.MemberMapper;
import com.restaurant.service.CaptchaService;
import com.restaurant.service.SmsAuthService;
import com.restaurant.service.SmsCodeStore;
import com.restaurant.util.JwtUtil;
import com.restaurant.vo.MemberLoginVO;
import cn.hutool.core.util.RandomUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短信验证码登录业务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsAuthServiceImpl implements SmsAuthService {

    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    private final SmsCodeStore smsCodeStore;
    private final SmsRateLimiter rateLimiter;
    private final Client dypnsapiClient;
    private final MemberMapper memberMapper;
    private final JwtUtil jwtUtil;
    private final SmsRateLimitProperties rateLimitProps;
    private final AliyunDypnsapiProperties dypnsapiProperties;
    private final CaptchaService captchaService;

    @Override
    public void sendCode(SmsSendRequest req, String clientIp) {
        String phone = req.getPhone();

        // ① 手机号格式校验
        if (phone == null || !phone.matches(PHONE_REGEX)) {
            throw new BizException(ResultCode.SMS_PHONE_INVALID, "请输入正确的手机号");
        }

        // ② Q2 滑块前置：限频校验之前先校验并消费 captchaToken
        if (!captchaService.verifyAndConsumeCaptcha(req.getCaptchaToken())) {
            throw new BizException(ResultCode.CAPTCHA_INVALID, "验证码无效或未通过");
        }

        // ③ 限频校验
        RateLimitResult r = rateLimiter.canSend(phone, clientIp);
        if (!r.isAllowed()) {
            if (r.isLocked()) {
                throw new BizException(ResultCode.RATE_LIMIT_LOCKED, "验证码错误次数过多，请 10 分钟后再试");
            }
            if (r.getRemainSeconds() > 0) {
                throw new BizException(ResultCode.RATE_LIMIT_PHONE_INTERVAL,
                        "发送过于频繁，请 " + r.getRemainSeconds() + "s 后再试");
            }
            if (r.getReason() == RateLimitResult.Reason.PHONE_DAILY) {
                throw new BizException(ResultCode.RATE_LIMIT_PHONE_DAILY, "今日验证码发送次数已达上限，请明天再试");
            }
            if (r.getReason() == RateLimitResult.Reason.IP_DAILY) {
                throw new BizException(ResultCode.RATE_LIMIT_IP_DAILY, "当前网络发送过于频繁，请稍后再试");
            }
            // 兜底（理论上不会走到这里）
            throw new BizException(ResultCode.RATE_LIMIT_PHONE_INTERVAL, "发送过于频繁，请稍后再试");
        }

        // ④ 调阿里云发送验证码并取回明文
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest();
        request.setPhoneNumber(phone);
        request.setSignName(dypnsapiProperties.getSignName());
        request.setTemplateCode(dypnsapiProperties.getTemplateCode());
        request.setReturnVerifyCode(true);
        request.setInterval(60L);
        request.setValidTime((long) rateLimitProps.getCodeTtlSeconds());

        // 生成 6 位验证码占位值，并换算有效分钟数（= CodeTtlSeconds / 60）
        String code = RandomUtil.randomNumbers(6);
        int validMinutes = rateLimitProps.getCodeTtlSeconds() / 60;
        // 模板变量：${code} 与 ${min} 用户模板均包含，二者都必须传入，缺一不可；
        // 用户短信中实际收到的验证码即我们传入的本地 ${code}，故下方以本地 code 作为 Redis 存储值
        // （仅当阿里云回传的 verifyCode 确有填充时才优先采用它）。
        request.setTemplateParam("{\"code\":\"" + code + "\",\"min\":\"" + validMinutes + "\"}");

        SendSmsVerifyCodeResponse response;
        try {
            response = dypnsapiClient.sendSmsVerifyCode(request);
        } catch (Exception e) {
            // 绝不透传阿里云原始错误
            log.warn("阿里云发送验证码异常: phone={}, err={}", SmsCodeStore.maskPhone(phone), e.getMessage());
            throw new BizException(ResultCode.SMS_SEND_FAILED, "验证码发送失败，请稍后再试");
        }

        SendSmsVerifyCodeResponseBody body = response.getBody();
        // 成功判定：阿里云返回 code=OK 表示短信已下发；其余值（如 biz.FREQUENCY）为失败。
        // 注意：本账号/SDK 即便 ReturnVerifyCode=true，响应 model.getVerifyCode() 仍为空白，
        // 用户短信中实际收到的验证码是我们在 TemplateParam 中传入的本地 code。
        if (body == null || !"OK".equals(body.getCode())) {
            log.warn("阿里云发送验证码返回异常: phone={}, code={}, msg={}",
                    SmsCodeStore.maskPhone(phone),
                    body == null ? null : body.getCode(),
                    body == null ? null : body.getMessage());
            throw new BizException(ResultCode.SMS_SEND_FAILED, "验证码发送失败，请稍后再试");
        }

        // 验证码取值：优先采用阿里云回传的 verifyCode（若其确有填充），否则回退到本地 code
        // （即短信正文携带的验证码，保证登录时用户凭短信收到的码能比对通过）。
        String verifyCode = (body.getModel() != null && body.getModel().getVerifyCode() != null
                && !body.getModel().getVerifyCode().isBlank())
                ? body.getModel().getVerifyCode()
                : code;
        log.info("短信验证码已生成: phone={}, 本地code={}, 阿里云回传verifyCode={}",
                SmsCodeStore.maskPhone(phone), code,
                (body.getModel() != null ? body.getModel().getVerifyCode() : "<null>"));

        // ⑥ 存 Redis + 更新限频计数
        smsCodeStore.save(phone, verifyCode, rateLimitProps.getCodeTtlSeconds());
        rateLimiter.onSendSuccess(phone, clientIp);

        // ⑦ 不向前端返回明文验证码
        log.info("短信验证码已下发: phone={}", SmsCodeStore.maskPhone(phone));
    }

    @Override
    public MemberLoginVO login(SmsLoginRequest req) {
        String phone = req.getPhone();

        // 锁定期间禁止登录（与发码共用锁定态）
        if (rateLimiter.isLocked(phone)) {
            throw new BizException(ResultCode.RATE_LIMIT_LOCKED, "验证码错误次数过多，请 10 分钟后再试");
        }

        // ① 取验证码
        String code = smsCodeStore.get(phone);
        if (code == null || code.isBlank()) {
            throw new BizException(ResultCode.SMS_CODE_EXPIRED, "验证码已过期，请重新获取");
        }

        // ② 比对
        if (!code.equals(req.getCode())) {
            FailResult fail = rateLimiter.onVerifyFail(phone);
            if (fail.isLocked()) {
                throw new BizException(ResultCode.RATE_LIMIT_LOCKED, "验证码错误次数过多，请 10 分钟后再试");
            }
            int remain = rateLimitProps.getFailMax() - fail.getCurrentCount();
            if (remain < 0) {
                remain = 0;
            }
            throw new BizException(ResultCode.SMS_CODE_ERROR, "验证码错误，还可尝试 " + remain + " 次");
        }

        // ③ 一致：一次性删除验证码
        smsCodeStore.delete(phone);

        // 按手机号匹配会员
        Member member = memberMapper.selectOne(new LambdaQueryWrapper<Member>()
                .eq(Member::getPhone, phone)
                .eq(Member::getDeleted, 0));
        if (member == null) {
            // 兼容老账号：老密码注册未存 phone，若 username 恰好是手机号则回填 phone 后直接登录（不新建）
            member = memberMapper.selectOne(new LambdaQueryWrapper<Member>()
                    .eq(Member::getUsername, phone)
                    .eq(Member::getDeleted, 0));
            if (member != null && member.getPhone() == null) {
                member.setPhone(phone);
                memberMapper.updateById(member);
                log.info("短信登录回填老账号 phone: phone={}, memberId={}", SmsCodeStore.maskPhone(phone), member.getId());
            }
        }
        if (member == null) {
            // 全新手机号：自动注册
            member = new Member();
            member.setUsername(phone);
            member.setPhone(phone);
            member.setPassword(null);
            member.setBalance(BigDecimal.ZERO);
            memberMapper.insert(member);
            log.info("短信验证码自动注册会员: phone={}, memberId={}", SmsCodeStore.maskPhone(phone), member.getId());
        } else {
            log.info("短信验证码登录成功: phone={}, memberId={}", SmsCodeStore.maskPhone(phone), member.getId());
        }

        // 签发 JWT（与现有会员体系一致：role=MEMBER，有效期 720h）
        Map<String, Object> claims = new HashMap<>(4);
        claims.put("role", "MEMBER");
        String token = jwtUtil.generateToken(String.valueOf(member.getId()), claims, 720);

        MemberLoginVO vo = new MemberLoginVO();
        vo.setToken(token);
        vo.setMemberId(member.getId());
        vo.setUsername(member.getUsername());
        vo.setBalance(member.getBalance());
        return vo;
    }
}
