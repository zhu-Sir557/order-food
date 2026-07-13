package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 绑定手机请求 DTO
 */
@Data
public class BindPhoneDTO {

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    /** 短信验证码 */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /** 滑块验证码 token */
    @NotBlank(message = "验证码token不能为空")
    private String captchaToken;
}
