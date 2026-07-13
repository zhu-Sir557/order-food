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
     * <p>流程：手机号格式校验 → 滑块校验（前置）→ 限频校验 → 调阿里云取回明文 → 存 Redis。</p>
     *
     * @param req      发送请求（含手机号与滑块 captchaToken）
     * @param clientIp 客户端真实 IP（用于单 IP 限频）
     */
    void sendCode(SmsSendRequest req, String clientIp);

    /**
     * 手机号 + 验证码登录（统一登录 PHONE_CODE 分支调用，含自动注册/老账号回填）
     *
     * @param phone    手机号
     * @param code     验证码
     * @param clientIp 客户端真实 IP（登录失败锁定用）
     * @return 登录响应（含 token 与昵称/头像）
     */
    MemberLoginVO phoneCodeLogin(String phone, String code, String clientIp);

    /**
     * 短信验证码登录（旧接口，已废弃，过渡期保留）
     *
     * @param req 登录请求（含手机号与验证码）
     * @return 登录响应（含 JWT token）
     * @deprecated 请使用 {@link #phoneCodeLogin(String, String, String)} 并走统一登录
     */
    @Deprecated
    MemberLoginVO login(SmsLoginRequest req);
}
