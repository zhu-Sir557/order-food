package com.restaurant.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 后台会员列表响应 VO
 */
@Data
public class AdminMemberVO {

    /** 会员ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 账户余额 */
    private BigDecimal balance;

    /** 注册时间 */
    private String createTime;
}
