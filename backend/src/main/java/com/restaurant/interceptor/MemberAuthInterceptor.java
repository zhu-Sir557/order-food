package com.restaurant.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.common.Result;
import com.restaurant.common.ResultCode;
import com.restaurant.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 会员认证拦截器
 *
 * <p>校验 JWT 中的 role=MEMBER，保护会员专属接口。
 * 拦截 /api/h5/member 下的路径（排除 register 和 login）以及余额支付端点。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 跳过预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        String token = jwtUtil.extractToken(authHeader);

        if (token == null || token.isBlank()) {
            writeUnauthorizedResponse(response, "未提供有效的认证令牌");
            return false;
        }

        Jws<Claims> jws = jwtUtil.parseToken(token);
        if (jws == null) {
            writeUnauthorizedResponse(response, "令牌无效或已过期");
            return false;
        }

        Claims claims = jws.getPayload();
        Object role = claims.get("role");

        if (role == null || !"MEMBER".equals(role.toString())) {
            writeUnauthorizedResponse(response, "无会员权限");
            return false;
        }

        // 设置 memberId 到 request
        String subject = claims.getSubject();
        if (subject != null) {
            request.setAttribute("memberId", Long.parseLong(subject));
        }

        // 同时设置 tempUserId（会员 JWT 中携带 tempUserId claim）
        Object tempUserIdClaim = claims.get("tempUserId");
        if (tempUserIdClaim != null) {
            request.setAttribute("tempUserId", Long.valueOf(tempUserIdClaim.toString()));
        }

        return true;
    }

    /**
     * 写入 401 未授权 JSON 响应
     *
     * @param response HTTP 响应
     * @param message  错误信息
     * @throws Exception 写入失败时抛出
     */
    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
