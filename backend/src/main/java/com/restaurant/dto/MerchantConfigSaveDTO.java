package com.restaurant.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 保存/更新商家配置请求体。
 *
 * <p>字段校验规则：
 * <ul>
 *   <li>{@code aboutUsContent}：允许为空；非空时长度不超过 20000 字符。</li>
 *   <li>{@code contactPhone}：允许为空或 null；非空时必须为中国大陆手机号（11 位）或固话。</li>
 * </ul>
 * </p>
 */
@Data
public class MerchantConfigSaveDTO {

    /** 关于我们富文本 HTML（允许为空；非空时最长 20000 字符） */
    @Size(max = 20000, message = "关于我们内容过长（最多 20000 字符）")
    private String aboutUsContent;

    /**
     * 联系电话（允许为空或 null）。
     * 使用可选分组 {@code (...)?} 以支持空串/ null，非空时必须满足手机或固话格式。
     */
    @Pattern(
        regexp = "^(1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})?$",
        message = "联系电话格式不正确（需为 11 位手机号或区号-号码固话）"
    )
    private String contactPhone;
}
