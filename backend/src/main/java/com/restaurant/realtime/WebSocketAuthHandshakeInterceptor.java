package com.restaurant.realtime;

import com.restaurant.common.MessageConstants;
import com.restaurant.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * WebSocket 握手鉴权拦截器
 *
 * <p>复用现有 {@link JwtUtil} 解析 Bearer 令牌，校验 role ∈ {CUSTOMER, MEMBER}，
 * 构建 {@link WebSocketUserPrincipal} 写入 attributes，供 {@link MessageHandshakeHandler} 绑定会话。
 * 不新增鉴权体系。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = resolveToken(request);
        if (token == null || token.isBlank()) {
            log.warn("WS 握手失败：缺少令牌");
            return false;
        }
        Jws<Claims> jws = jwtUtil.parseToken(token);
        if (jws == null) {
            log.warn("WS 握手失败：令牌无效或已过期");
            return false;
        }
        Claims claims = jws.getPayload();
        Object role = claims.get("role");
        if (role == null || (!"CUSTOMER".equals(role.toString()) && !"MEMBER".equals(role.toString()))) {
            log.warn("WS 握手失败：无顾客权限");
            return false;
        }
        String subject = claims.getSubject();
        if (subject == null) {
            return false;
        }
        Long userId = Long.parseLong(subject);
        String name = "MEMBER".equals(role.toString())
                ? (MessageConstants.PRINCIPAL_PREFIX_MEMBER + userId)
                : (MessageConstants.PRINCIPAL_PREFIX_TEMP + userId);
        attributes.put(MessageHandshakeHandler.ATTR_WS_USER,
                new WebSocketUserPrincipal(name, userId, role.toString()));
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // 无需处理
    }

    /**
     * 解析令牌：优先 Authorization 头，其次查询参数 token（SockJS 不携带自定义请求头）。
     *
     * @param request 握手请求
     * @return 原始令牌，或 null
     */
    private String resolveToken(ServerHttpRequest request) {
        List<String> auth = request.getHeaders().get("Authorization");
        if (auth != null && !auth.isEmpty()) {
            String v = auth.get(0);
            if (v.startsWith("Bearer ")) {
                return v.substring(7);
            }
            return v;
        }
        String queryToken = request.getQueryParams().getFirst("token");
        if (queryToken != null && !queryToken.isBlank()) {
            return queryToken;
        }
        if (request instanceof ServletServerHttpRequest servletRequest) {
            return jwtUtil.extractToken(servletRequest.getServletRequest().getHeader("Authorization"));
        }
        return null;
    }
}
