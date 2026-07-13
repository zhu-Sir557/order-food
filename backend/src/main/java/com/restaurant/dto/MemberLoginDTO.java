package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 统一会员登录请求 DTO
 *
 * <p>通过 {@code loginType} 区分四种组合：</p>
 * <ul>
 *   <li>ACCOUNT_PASSWORD：账号名 + 密码</li>
 *   <li>PHONE_CODE：手机号 + 验证码</li>
 *   <li>PHONE_PASSWORD：手机号 + 密码</li>
 *   <li>ACCOUNT_CODE：账号名 + 验证码</li>
 * </ul>
 * <p>四种组合均携带 {@code captchaToken}（一次性消费）。</p>
 */
@Data
public class MemberLoginDTO {

    /** 登录方式（必填） */
    @NotBlank(message = "登录方式不能为空")
    private String loginType;

    /** 账号（必填）：账号名或手机号，按 loginType 解释 */
    @NotBlank(message = "账号不能为空")
    private String account;

    /** 密码（ACCOUNT_PASSWORD / PHONE_PASSWORD 时必填） */
    private String password;

    /** 验证码（PHONE_CODE / ACCOUNT_CODE 时必填） */
    private String code;

    /** 滑块验证码 token（必填，四种组合均校验） */
    @NotBlank(message = "验证码token不能为空")
    private String captchaToken;
}
