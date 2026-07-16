package com.restaurant.vo;

import java.io.Serializable;
import lombok.Data;

/**
 * 未读计数响应
 */
@Data
public class MessageUnreadVO implements Serializable {

    /** 当前用户未读消息总数 */
    private Long unreadCount;

    /**
     * 全参构造（便于服务层直接构建）。
     *
     * @param unreadCount 未读数
     */
    public MessageUnreadVO(Long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
