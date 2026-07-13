package com.restaurant.controller.h5;

import com.restaurant.common.Result;
import com.restaurant.dto.MemberLoginDTO;
import com.restaurant.dto.MemberRegisterDTO;
import com.restaurant.service.MemberAuthService;
import com.restaurant.vo.MemberLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * H5 会员注册登录接口
 */
@RestController
@RequestMapping("/api/h5/member")
@RequiredArgsConstructor
public class H5MemberAuthController {

    private final MemberAuthService memberAuthService;

    /**
     * 会员注册（自动登录返回 JWT）
     *
     * @param dto     注册请求
     * @param request HTTP 请求（可能含 tempUserId 属性）
     * @return 登录响应
     */
    @PostMapping("/register")
    public Result<MemberLoginVO> register(@Valid @RequestBody MemberRegisterDTO dto,
                                          HttpServletRequest request) {
        Long tempUserId = (Long) request.getAttribute("tempUserId");
        return Result.success(memberAuthService.register(dto, tempUserId));
    }

    /**
     * 统一会员登录（ACCOUNT_PASSWORD / PHONE_CODE / PHONE_PASSWORD / ACCOUNT_CODE）
     *
     * @param dto     登录请求（含 loginType / account / password / code / captchaToken）
     * @param request HTTP 请求（用于解析真实客户端 IP）
     * @return 登录响应（含 token 与昵称/头像）
     */
    @PostMapping("/login")
    public Result<MemberLoginVO> login(@Valid @RequestBody MemberLoginDTO dto,
                                       HttpServletRequest request) {
        return Result.success(memberAuthService.login(dto, request));
    }
}
