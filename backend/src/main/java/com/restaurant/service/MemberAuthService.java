package com.restaurant.service;

import com.restaurant.dto.MemberLoginDTO;
import com.restaurant.dto.MemberRegisterDTO;
import com.restaurant.vo.MemberLoginVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 会员认证服务接口
 */
public interface MemberAuthService {

    /**
     * 注册（自动登录返回 JWT）
     *
     * @param dto        注册请求
     * @param tempUserId 当前临时用户ID
     * @return 登录响应（含 JWT token）
     */
    MemberLoginVO register(MemberRegisterDTO dto, Long tempUserId);

    /**
     * 统一登录（按 loginType 分发四种组合，校验 captchaToken + 登录防暴破）
     *
     * @param dto     登录请求（含 loginType / account / password / code / captchaToken）
     * @param request HTTP 请求（用于解析真实客户端 IP）
     * @return 登录响应（含 JWT token 与昵称/头像）
     */
    MemberLoginVO login(MemberLoginDTO dto, HttpServletRequest request);
}
