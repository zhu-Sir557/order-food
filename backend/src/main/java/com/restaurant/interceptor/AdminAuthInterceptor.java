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
 * Interceptor for admin authentication.
 *
 * <p>Validates JWT token from the Authorization header for all
 * /api/admin/** endpoints (excluding /api/admin/login).
 * Requires the token to contain role=ADMIN claim.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // Skip preflight requests
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

        if (role == null || !"ADMIN".equals(role.toString())) {
            writeUnauthorizedResponse(response, "无管理员权限");
            return false;
        }

        // Store admin user ID in request for downstream use
        String subject = claims.getSubject();
        if (subject != null) {
            request.setAttribute("adminId", Long.parseLong(subject));
        }

        return true;
    }

    /**
     * Write a 401 unauthorized JSON response.
     *
     * @param response the HTTP response
     * @param message  the error message
     * @throws Exception if writing fails
     */
    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
