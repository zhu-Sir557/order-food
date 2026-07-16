package com.restaurant.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 消息接收人 / 已读维度实体
 *
 * <p>同时承载两种语义：
 * <ul>
 *   <li>指定用户（SPECIFIED）发送时展开多行（每条接收人一行，is_read=0）；</li>
 *   <li>全员广播（BROADCAST）的已读惰性写回标记（用户打开时 upsert 一行 is_read=1）。</li>
 * </ul>
 * 唯一键 (message_id, receiver_id, receiver_type) 保证每个用户对一条消息仅一行。
 * </p>
 */
@Data
@TableName("message_receiver")
public class MessageReceiver {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息ID */
    private Long messageId;

    /** 接收人ID（member.id 或 temp_user.id） */
    private Long receiverId;

    /** 接收人类型：MEMBER/TEMP */
    private String receiverType;

    /** 是否已读：0未读，1已读 */
    private Integer isRead;

    /** 阅读时间 */
    private LocalDateTime readTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 逻辑删除：0未删除，1已删除 */
    @TableLogic
    private Integer deleted;
}
