package com.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.common.BizException;
import com.restaurant.dto.MemberLoginDTO;
import com.restaurant.dto.MemberRegisterDTO;
import com.restaurant.entity.Member;
import com.restaurant.mapper.MemberMapper;
import com.restaurant.service.CaptchaService;
import com.restaurant.service.MemberAuthService;
import com.restaurant.util.JwtUtil;
import com.restaurant.vo.MemberLoginVO;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 会员认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAuthServiceImpl implements MemberAuthService {

    private final MemberMapper memberMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;

    @Override
    public MemberLoginVO register(MemberRegisterDTO dto, Long tempUserId) {
        // 校验用户名是否已存在
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUsername, dto.getUsername());
        Long existCount = memberMapper.selectCount(wrapper);
        if (existCount > 0) {
            throw new BizException("账户名已存在");
        }

        // 创建会员
        Member member = new Member();
        member.setUsername(dto.getUsername());
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setBalance(BigDecimal.ZERO);
        member.setTempUserId(tempUserId); // 可为 null，不关联购物车也能注册
        memberMapper.insert(member);

        log.info("会员注册成功: username={}, memberId={}", dto.getUsername(), member.getId());

        // 生成 JWT 并返回
        return buildLoginVO(member);
    }

    @Override
    public MemberLoginVO login(MemberLoginDTO dto) {
        // 验证并消费 captchaToken
        boolean captchaValid = captchaService.verifyAndConsumeCaptcha(dto.getCaptchaToken());
        if (!captchaValid) {
            throw new BizException("验证码无效或未通过");
        }

        // 查询会员
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUsername, dto.getUsername());
        Member member = memberMapper.selectOne(wrapper);
        if (member == null) {
            throw new BizException("账户名或密码错误");
        }

        // 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new BizException("账户名或密码错误");
        }

        log.info("会员登录成功: username={}, memberId={}", dto.getUsername(), member.getId());

        return buildLoginVO(member);
    }

    /**
     * 构建登录响应 VO（生成 JWT）
     *
     * @param member 会员实体
     * @return 登录响应
     */
    private MemberLoginVO buildLoginVO(Member member) {
        String token = generateMemberToken(member);

        MemberLoginVO vo = new MemberLoginVO();
        vo.setToken(token);
        vo.setMemberId(member.getId());
        vo.setUsername(member.getUsername());
        vo.setBalance(member.getBalance());
        return vo;
    }

    /**
     * 生成会员 JWT
     * subject = memberId, claims = {role=MEMBER, tempUserId=member.tempUserId}
     *
     * @param member 会员实体
     * @return JWT token
     */
    private String generateMemberToken(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "MEMBER");
        if (member.getTempUserId() != null) {
            claims.put("tempUserId", member.getTempUserId());
        }
        return jwtUtil.generateToken(String.valueOf(member.getId()), claims, 720);
    }
}
