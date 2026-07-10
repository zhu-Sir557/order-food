package com.restaurant.service;

import com.restaurant.dto.SmsLoginRequest;
import com.restaurant.dto.SmsSendRequest;
import com.restaurant.vo.MemberLoginVO;

/**
 * 短信验证码登录业务服务接口
 */
public interface SmsAuthService {

    /**
     * 发送短信验证码
     *
     * <p>流程：手机号格式校验 → 滑块校验（Q2 前置）→ 限频校验 → 调阿里云取回明文 → 存 Redis。</p>
     *
     * @param req      发送请求（含手机号与滑块 captchaToken）
     * @param clientIp 客户端真实 IP（用于单 IP 限频）
     */
    void sendCode(SmsSendRequest req, String clientIp);

    /**
     * 短信验证码登录
     *
     * <p>流程：验证码比对 → 会员自动注册/匹配 → 签发 JWT → 返回 {@link MemberLoginVO}。</p>
     *
     * @param req 登录请求（含手机号与验证码）
     * @return 登录响应（含 token）
     */
    MemberLoginVO login(SmsLoginRequest req);
}
