package com.restaurant.realtime;

import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * WebSocket 入站通道校验拦截器
 *
 * <p>CONNECT 阶段若未携带认证主体（握手未通过），拒绝建立连接；
 * 对 /app/** 入站消息做无主体防御（本项目客户端仅订阅，不发送 app 消息）。</p>
 */
@Slf4j
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            if (principal == null) {
                log.warn("WS CONNECT 未携带认证主体，拒绝连接");
                // 前缀 401 使前端 onStompError 的 body.includes('401') 命中，触发登录跳转
                throw new MessagingException("401 未认证，禁止建立连接");
            }
            return message;
        }
        String destination = accessor.getDestination();
        if (destination != null && destination.startsWith("/app/") && accessor.getUser() == null) {
            throw new MessagingException("未认证，禁止发送消息");
        }
        return message;
    }
}
