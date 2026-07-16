package com.restaurant.vo;

import java.io.Serializable;
import lombok.Data;

/**
 * WebSocket 实时推送 Payload
 *
 * <p>用于个人队列 /user/queue/messages 与广播主题 /topic/messages/broadcast。
 * 撤回事件复用同一结构，仅将 status 置为 REVOKED，前端据此隐藏或标记「已撤回」。</p>
 */
@Data
public class MessagePushVO implements Serializable {

    /** 消息ID */
    private Long id;

    /** 消息类型：BROADCAST/SPECIFIED/SYSTEM */
    private String type;

    /** 标题 */
    private String title;

    /** 正文 */
    private String content;

    /** 图片OSS地址 */
    private String imageUrl;

    /** 跳转链接 */
    private String linkUrl;

    /** 状态：SENT/REVOKED */
    private String status;

    /** 创建时间（格式化字符串 yyyy-MM-dd HH:mm:ss） */
    private String createTime;
}
