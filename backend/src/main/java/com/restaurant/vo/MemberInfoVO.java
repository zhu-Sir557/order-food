package com.restaurant.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 会员信息响应 VO
 */
@Data
public class MemberInfoVO {

    /** 会员ID */
    private Long memberId;

    /** 用户名 */
    private String username;

    /** 账户余额 */
    private BigDecimal balance;

    /** 注册时间 */
    private String createTime;

    /** 昵称 */
    private String nickname;

    /** 头像OSS地址 */
    private String avatar;

    /** 脱敏后的绑定手机号（未绑定则为 null） */
    private String phoneMasked;
}
