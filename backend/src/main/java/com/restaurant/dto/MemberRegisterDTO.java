package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 会员注册请求 DTO
 */
@Data
public class MemberRegisterDTO {

    /** 账户名 */
    @NotBlank(message = "账户名不能为空")
    @Size(min = 3, max = 20, message = "账户名长度3-20个字符")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20个字符")
    private String password;
}
