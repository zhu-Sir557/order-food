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
 * 消息主表实体
 */
@Data
@TableName("message")
public class Message {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息类型：BROADCAST/SPECIFIED/SYSTEM */
    private String type;

    /** 接收范围：ALL/SPECIFIED */
    private String receiverScope;

    /** 发送者ID（admin_user.id） */
    private Long senderId;

    /** 标题（≤50字） */
    private String title;

    /** 正文 */
    private String content;

    /** 图片OSS地址（复用 /api/admin/upload） */
    private String imageUrl;

    /** 跳转链接 */
    private String linkUrl;

    /** 状态：SENT/REVOKED */
    private String status;

    /** 撤回截止时间（发送后5分钟） */
    private LocalDateTime revocableBefore;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除：0未删除，1已删除 */
    @TableLogic
    private Integer deleted;
}
