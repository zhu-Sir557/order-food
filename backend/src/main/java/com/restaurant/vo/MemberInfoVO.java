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
}
