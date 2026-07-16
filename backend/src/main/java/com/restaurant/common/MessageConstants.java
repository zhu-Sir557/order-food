package com.restaurant.common;

import java.time.LocalDateTime;

/**
 * 消息模块共享常量（枚举值统一大写字符串落库，与 schema.sql 风格一致）。
 *
 * <p>包含消息类型、接收范围、状态、接收人类型，以及 WebSocket 目的地、
 * Principal 前缀、撤回窗口与广播保留窗口等跨层约定。</p>
 */
public final class MessageConstants {

    private MessageConstants() {
    }

    /** 消息类型：全员广播 / 指定用户 / 系统通知 */
    public static final class MessageType {
        public static final String BROADCAST = "BROADCAST";
        public static final String SPECIFIED = "SPECIFIED";
        public static final String SYSTEM = "SYSTEM";

        private MessageType() {
        }
    }

    /** 接收范围：全员 / 指定 */
    public static final class ReceiverScope {
        public static final String ALL = "ALL";
        public static final String SPECIFIED = "SPECIFIED";

        private ReceiverScope() {
        }
    }

    /** 消息状态：已发送 / 已撤回 */
    public static final class MessageStatus {
        public static final String SENT = "SENT";
        public static final String REVOKED = "REVOKED";

        private MessageStatus() {
        }
    }

    /** 接收人类型：会员 / 游客 */
    public static final class ReceiverType {
        public static final String MEMBER = "MEMBER";
        public static final String TEMP = "TEMP";

        private ReceiverType() {
        }
    }

    /** WebSocket 个人队列（Spring user destination） */
    public static final String WS_USER_QUEUE = "/user/queue/messages";

    /** WebSocket 广播主题 */
    public static final String WS_BROADCAST_TOPIC = "/topic/messages/broadcast";

    /** Principal name 前缀：会员 / 游客 */
    public static final String PRINCIPAL_PREFIX_MEMBER = "M:";
    public static final String PRINCIPAL_PREFIX_TEMP = "C:";

    /** 撤回窗口（分钟） */
    public static final int REVOKE_WINDOW_MINUTES = 5;

    /** 广播保留窗口（天），用于未读统计与历史回溯 */
    public static final int BROADCAST_RETENTION_DAYS = 30;

    /**
     * 计算广播保留窗口起点。
     *
     * @param now 当前时间
     * @return 窗口起点（now - BROADCAST_RETENTION_DAYS 天）
     */
    public static LocalDateTime broadcastSince(LocalDateTime now) {
        return now.minusDays(BROADCAST_RETENTION_DAYS);
    }
}
