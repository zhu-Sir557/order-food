package com.restaurant.controller.h5;

import com.restaurant.common.Result;
import com.restaurant.dto.SmsLoginRequest;
import com.restaurant.dto.SmsSendRequest;
import com.restaurant.service.SmsAuthService;
import com.restaurant.vo.MemberLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * H5 短信验证码登录接口
 */
@RestController
@RequestMapping("/api/h5/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsAuthService smsAuthService;

    /**
     * 发送短信验证码
     *
     * @param req     发送请求（手机号 + 滑块 captchaToken）
     * @param request HTTP 请求（用于解析真实客户端 IP）
     * @return 成功（不返回明文验证码）
     */
    @PostMapping("/send")
    public Result<Void> send(@Valid @RequestBody SmsSendRequest req, HttpServletRequest request) {
        smsAuthService.sendCode(req, resolveClientIp(request));
        return Result.success();
    }

    /**
     * 短信验证码登录（已废弃，过渡期保留；请改用 {@code POST /api/h5/member/login}）
     *
     * @param req 登录请求（手机号 + 验证码）
     * @return 登录响应（含 JWT token）
     * @deprecated 统一登录请使用 {@code /api/h5/member/login}
     */
    @Deprecated
    @PostMapping("/login")
    public Result<MemberLoginVO> login(@Valid @RequestBody SmsLoginRequest req) {
        return Result.success(smsAuthService.login(req));
    }

    /**
     * 解析客户端真实 IP
     *
     * <p>优先取 {@code X-Forwarded-For} 中第一个非 {@code unknown} 的 IP，
     * 降级使用 {@code request.getRemoteAddr()}。</p>
     *
     * @param request HTTP 请求
     * @return 客户端真实 IP
     */
    private static String resolveClientIp(HttpServletRequest request) {
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
