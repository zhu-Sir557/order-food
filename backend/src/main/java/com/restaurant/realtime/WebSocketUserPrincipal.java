package com.restaurant.realtime;

import java.security.Principal;
import lombok.Getter;

/**
 * WebSocket 用户身份主体
 *
 * <p>name 格式为 {@code M:<memberId>}（会员）或 {@code C:<tempUserId>}（游客），
 * 与现有 HTTP 请求 attribute 语义对齐，用于 Spring user destination 路由。</p>
 */
@Getter
public class WebSocketUserPrincipal implements Principal {

    /** 主体名称（M:/C: 前缀 + 用户ID） */
    private final String name;

    /** 用户ID */
    private final Long userId;

    /** 角色：MEMBER / CUSTOMER */
    private final String role;

    /**
     * 构造主体。
     *
     * @param name   主体名称（M:/C: 前缀）
     * @param userId 用户ID
     * @param role   角色
     */
    public WebSocketUserPrincipal(String name, Long userId, String role) {
        this.name = name;
        this.userId = userId;
        this.role = role;
    }

    @Override
    public String getName() {
        return name;
    }
}
