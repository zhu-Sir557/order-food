package com.restaurant.config;

import com.restaurant.realtime.MessageHandshakeHandler;
import com.restaurant.realtime.WebSocketAuthChannelInterceptor;
import com.restaurant.realtime.WebSocketAuthHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 消息代理配置（STOMP + SockJS）
 *
 * <p>端点 /ws（启用 SockJS）；个人队列 /user/queue/messages；广播主题 /topic/messages/broadcast。
 * 握手阶段由 {@link WebSocketAuthHandshakeInterceptor} 解析 Bearer 令牌并绑定主体。</p>
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthHandshakeInterceptor handshakeInterceptor;
    private final MessageHandshakeHandler handshakeHandler;
    private final WebSocketAuthChannelInterceptor channelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor)
                .setHandshakeHandler(handshakeHandler)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/user/queue", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor);
    }
}
