package com.restaurant.service;

import com.restaurant.dto.MemberLoginDTO;
import com.restaurant.dto.MemberRegisterDTO;
import com.restaurant.vo.MemberLoginVO;

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
     * 登录（校验 captchaToken + 账号密码）
     *
     * @param dto 登录请求
     * @return 登录响应（含 JWT token）
     */
    MemberLoginVO login(MemberLoginDTO dto);
}
