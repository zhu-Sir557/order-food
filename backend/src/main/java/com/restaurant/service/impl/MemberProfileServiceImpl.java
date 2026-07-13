package com.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.common.BizException;
import com.restaurant.common.ResultCode;
import com.restaurant.config.PasswordProperties;
import com.restaurant.config.ProfileProperties;
import com.restaurant.dto.BindPhoneDTO;
import com.restaurant.dto.SendLoginCodeDTO;
import com.restaurant.dto.SetPasswordDTO;
import com.restaurant.dto.UpdateAvatarDTO;
import com.restaurant.dto.UpdateNicknameDTO;
import com.restaurant.entity.Avatar;
import com.restaurant.entity.Member;
import com.restaurant.mapper.AvatarMapper;
import com.restaurant.mapper.MemberMapper;
import com.restaurant.service.CaptchaService;
import com.restaurant.service.MemberProfileService;
import com.restaurant.service.SmsAuthService;
import com.restaurant.service.SmsCodeStore;
import com.restaurant.service.impl.SmsRateLimiter;
import com.restaurant.vo.AvatarVO;
import com.restaurant.vo.ChangeLimitVO;
import cn.hutool.core.util.RandomUtil;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 会员资料业务服务实现（F1 绑定手机/设密码、F3 昵称/头像）
 */
@Slf4j
@Service
public class MemberProfileServiceImpl implements MemberProfileService {

    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String KEY_NICK_COUNT = "member:nick:count:";
    private static final String KEY_AVATAR_COUNT = "member:avatar:count:";

    private final MemberMapper memberMapper;
    private final AvatarMapper avatarMapper;
    private final SmsCodeStore smsCodeStore;
    private final SmsRateLimiter rateLimiter;
    private final CaptchaService captchaService;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ProfileProperties profileProperties;
    private final PasswordProperties passwordProperties;
    @Lazy
    private final SmsAuthService smsAuthService;

    public MemberProfileServiceImpl(MemberMapper memberMapper, AvatarMapper avatarMapper,
            SmsCodeStore smsCodeStore, SmsRateLimiter rateLimiter, CaptchaService captchaService,
            StringRedisTemplate redisTemplate, BCryptPasswordEncoder passwordEncoder,
            ProfileProperties profileProperties, PasswordProperties passwordProperties,
            @Lazy SmsAuthService smsAuthService) {
        this.memberMapper = memberMapper;
        this.avatarMapper = avatarMapper;
        this.smsCodeStore = smsCodeStore;
        this.rateLimiter = rateLimiter;
        this.captchaService = captchaService;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.profileProperties = profileProperties;
        this.passwordProperties = passwordProperties;
        this.smsAuthService = smsAuthService;
    }

    @Override
    public void bindPhone(Long memberId, BindPhoneDTO dto) {
        // 滑块校验
        if (!captchaService.verifyAndConsumeCaptcha(dto.getCaptchaToken())) {
            throw new BizException(ResultCode.CAPTCHA_INVALID, "验证码无效或未通过");
        }
        // 手机号格式
        if (dto.getPhone() == null || !dto.getPhone().matches(PHONE_REGEX)) {
            throw new BizException(ResultCode.SMS_PHONE_INVALID, "请输入正确的手机号");
        }
        // 手机号是否已被其他账号占用
        Member exist = memberMapper.selectOne(new LambdaQueryWrapper<Member>()
                .eq(Member::getPhone, dto.getPhone())
                .eq(Member::getDeleted, 0));
        if (exist != null && !exist.getId().equals(memberId)) {
            throw new BizException(ResultCode.PHONE_ALREADY_REGISTERED, "该手机号已被注册");
        }
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException(ResultCode.ACCOUNT_NOT_FOUND, "账号不存在");
        }
        // 仅首次绑定
        if (member.getPhone() != null) {
            throw new BizException(ResultCode.PHONE_ALREADY_BOUND, "已绑定手机，不支持改绑");
        }
        // 短信验证码校验
        String stored = smsCodeStore.get(dto.getPhone());
        if (stored == null || stored.isBlank()) {
            throw new BizException(ResultCode.SMS_CODE_EXPIRED, "验证码已过期，请重新获取");
        }
        if (!stored.equals(dto.getCode())) {
            rateLimiter.onVerifyFail(dto.getPhone());
            throw new BizException(ResultCode.SMS_CODE_ERROR, "验证码错误");
        }
        smsCodeStore.delete(dto.getPhone());

        member.setPhone(dto.getPhone());
        memberMapper.updateById(member);
        log.info("会员绑定手机成功: memberId={}, phone={}", memberId, SmsCodeStore.maskPhone(dto.getPhone()));
    }

    @Override
    public void setPassword(Long memberId, SetPasswordDTO dto) {
        // 滑块校验
        if (!captchaService.verifyAndConsumeCaptcha(dto.getCaptchaToken())) {
            throw new BizException(ResultCode.CAPTCHA_INVALID, "验证码无效或未通过");
        }
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException(ResultCode.ACCOUNT_NOT_FOUND, "账号不存在");
        }
        // 仅未设密码可设
        if (member.getPassword() != null) {
            throw new BizException(ResultCode.PASSWORD_ALREADY_SET, "已设置密码");
        }
        // 长度校验
        String pwd = dto.getPassword();
        if (pwd == null || pwd.length() < passwordProperties.getMinLength()) {
            throw new BizException(ResultCode.PARAM_ERROR,
                    "密码长度至少 " + passwordProperties.getMinLength() + " 位");
        }
        member.setPassword(passwordEncoder.encode(pwd));
        memberMapper.updateById(member);
        log.info("会员设置密码成功: memberId={}", memberId);
    }

    @Override
    public ChangeLimitVO updateNickname(Long memberId, UpdateNicknameDTO dto) {
        String nickname = dto.getNickname();
        if (nickname == null || nickname.length() > 20) {
            throw new BizException(ResultCode.NICK_TOO_LONG, "昵称不能超过20个字符");
        }
        int limit = profileProperties.getNickDailyLimit();
        long count = incrementDailyCount(KEY_NICK_COUNT + memberId);
        if (count > limit) {
            throw new BizException(ResultCode.NICK_CHANGE_LIMIT, "今日昵称修改次数已达上限");
        }
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException(ResultCode.ACCOUNT_NOT_FOUND, "账号不存在");
        }
        member.setNickname(nickname);
        memberMapper.updateById(member);
        log.info("会员修改昵称成功: memberId={}", memberId);
        ChangeLimitVO nickResult = new ChangeLimitVO();
        nickResult.setRemaining((int) (limit - count));
        return nickResult;
    }

    @Override
    public ChangeLimitVO updateAvatar(Long memberId, UpdateAvatarDTO dto) {
        Avatar avatar = avatarMapper.selectById(dto.getAvatarId());
        if (avatar == null || (avatar.getDeleted() != null && avatar.getDeleted() == 1)) {
            throw new BizException(ResultCode.AVATAR_NOT_FOUND, "头像不存在");
        }
        int limit = profileProperties.getAvatarDailyLimit();
        long count = incrementDailyCount(KEY_AVATAR_COUNT + memberId);
        if (count > limit) {
            throw new BizException(ResultCode.AVATAR_CHANGE_LIMIT, "今日头像修改次数已达上限");
        }
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException(ResultCode.ACCOUNT_NOT_FOUND, "账号不存在");
        }
        member.setAvatar(avatar.getOssUrl());
        memberMapper.updateById(member);
        log.info("会员修改头像成功: memberId={}, avatarId={}", memberId, dto.getAvatarId());
        ChangeLimitVO avatarResult = new ChangeLimitVO();
        avatarResult.setRemaining((int) (limit - count));
        return avatarResult;
    }

    @Override
    public void sendLoginCode(SendLoginCodeDTO dto, String clientIp) {
        Member member = memberMapper.selectOne(new LambdaQueryWrapper<Member>()
                .eq(Member::getUsername, dto.getAccount())
                .eq(Member::getDeleted, 0));
        if (member == null) {
            throw new BizException(ResultCode.ACCOUNT_NOT_FOUND, "账号不存在");
        }
        if (member.getPhone() == null) {
            throw new BizException(ResultCode.ACCOUNT_NO_PHONE, "账号未绑定手机号，请先绑定手机");
        }
        // 复用短信发送（发送侧会校验 captchaToken + 限频 + 阿里云）
        com.restaurant.dto.SmsSendRequest req = new com.restaurant.dto.SmsSendRequest();
        req.setPhone(member.getPhone());
        req.setCaptchaToken(dto.getCaptchaToken());
        smsAuthService.sendCode(req, clientIp);
    }

    @Override
    public List<AvatarVO> listAvatars() {
        List<Avatar> list = avatarMapper.selectList(new LambdaQueryWrapper<Avatar>()
                .eq(Avatar::getDeleted, 0)
                .orderByAsc(Avatar::getSort));
        List<AvatarVO> result = new ArrayList<>(list.size());
        for (Avatar avatar : list) {
            AvatarVO vo = new AvatarVO();
            vo.setId(avatar.getId());
            vo.setOssUrl(avatar.getOssUrl());
            result.add(vo);
        }
        return result;
    }

    @Override
    public String generateDefaultNickname() {
        // 美食家 + 8 位大小写英文
        return "美食家" + RandomUtil.randomString(RandomUtil.BASE_CHAR, 8);
    }

    @Override
    public String randomAvatarUrl() {
        Avatar avatar = avatarMapper.selectOne(new LambdaQueryWrapper<Avatar>()
                .eq(Avatar::getDeleted, 0)
                .last("ORDER BY RAND() LIMIT 1"));
        return avatar == null ? null : avatar.getOssUrl();
    }

    /**
     * 自然日计数递增（key 含 yyyyMMdd），TTL 设为当日剩余秒数于凌晨归零
     *
     * @param prefix 不含日期的计数 key 前缀
     * @return 递增后的当日计数
     */
    private long incrementDailyCount(String prefix) {
        String key = prefix + ":" + LocalDate.now(ZONE_SHANGHAI).format(DATE_FMT);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, secondsUntilEndOfDay(), TimeUnit.SECONDS);
        }
        return count == null ? 1L : count;
    }

    private long secondsUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now(ZONE_SHANGHAI);
        LocalDateTime endOfDay = LocalDate.now(ZONE_SHANGHAI).plusDays(1).atStartOfDay();
        return Duration.between(now, endOfDay).getSeconds();
    }
}
