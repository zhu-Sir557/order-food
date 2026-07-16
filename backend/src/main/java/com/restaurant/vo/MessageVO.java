package com.restaurant.vo;

import java.io.Serializable;
import lombok.Data;

/**
 * 消息视图对象（列表 / 详情 / 后台记录共用）
 */
@Data
public class MessageVO implements Serializable {

    /** 消息ID */
    private Long id;

    /** 消息类型：BROADCAST/SPECIFIED/SYSTEM */
    private String type;

    /** 接收范围：ALL/SPECIFIED */
    private String receiverScope;

    /** 发送者ID（admin_user.id） */
    private Long senderId;

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

    /** 是否可撤回（后台视角：状态=SENT 且未过撤回窗口） */
    private Boolean revocable;

    /** 创建时间（格式化字符串 yyyy-MM-dd HH:mm:ss） */
    private String createTime;

    /** 当前用户是否已读（H5 视角） */
    private Boolean isRead;
}
