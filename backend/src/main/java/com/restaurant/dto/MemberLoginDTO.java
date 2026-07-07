package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 会员登录请求 DTO
 */
@Data
public class MemberLoginDTO {

    /** 账户名 */
    @NotBlank(message = "账户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 滑块验证码 token */
    @NotBlank(message = "验证码token不能为空")
    private String captchaToken;
}
