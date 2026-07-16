package com.restaurant.realtime;

import java.security.Principal;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * WebSocket 握手处理器：从握手拦截器写入的 attributes 中还原用户主体。
 *
 * <p>若未通过鉴权（attributes 中无 {@link #ATTR_WS_USER}），返回 null，
 * 该会话将不具备 user destination 路由能力（仅能收广播）。</p>
 */
@Component
public class MessageHandshakeHandler extends DefaultHandshakeHandler {

    /** 握手拦截器写入 attributes 的主体键 */
    public static final String ATTR_WS_USER = "wsUser";

    @Override
    public Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        Object principal = attributes.get(ATTR_WS_USER);
        if (principal instanceof Principal p) {
            return p;
        }
        return null;
    }
}
