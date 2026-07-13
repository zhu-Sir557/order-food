package com.restaurant.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 会员登录/注册响应 VO
 */
@Data
public class MemberLoginVO {

    /** JWT token */
    private String token;

    /** 会员ID */
    private Long memberId;

    /** 用户名 */
    private String username;

    /** 账户余额 */
    private BigDecimal balance;

    /** 昵称 */
    private String nickname;

    /** 头像OSS地址 */
    private String avatar;
}
