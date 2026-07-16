package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * 后台消息发送请求体
 */
@Data
public class MessageSendDTO {

    /** 消息类型：BROADCAST / SPECIFIED（必填） */
    @NotNull(message = "消息类型必填")
    private String type;

    /** 标题（必填，≤50字） */
    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题不能超过50个字符")
    private String title;

    /** 正文（必填） */
    @NotBlank(message = "正文不能为空")
    private String content;

    /** 图片OSS地址（可选，复用 /api/admin/upload 的返回值） */
    private String imageUrl;

    /**
     * 跳转链接（可选）。
     * 允许为空或空串；非空时须以 http(s):// 或 / 开头。
     */
    @Pattern(regexp = "^$|^(https?://|/).*$", message = "跳转链接非法，须以 http(s):// 或 / 开头")
    private String linkUrl;

    /** 指定用户（SPECIFIED）时的接收人ID列表（member.id）；BROADCAST 忽略 */
    private List<Long> receiverIds;
}
