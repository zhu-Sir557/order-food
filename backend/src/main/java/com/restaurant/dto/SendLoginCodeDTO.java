package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 账号名 + 验证码登录的「发码」请求 DTO
 */
@Data
public class SendLoginCodeDTO {

    /** 账号名（按 username 定位绑定的手机号后发码） */
    @NotBlank(message = "账号不能为空")
    private String account;

    /** 滑块验证码 token（发送侧校验） */
    @NotBlank(message = "验证码token不能为空")
    private String captchaToken;
}
