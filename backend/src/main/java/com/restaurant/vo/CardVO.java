package com.restaurant.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 点卡响应 VO（后台管理用）
 */
@Data
public class CardVO {

    /** 卡ID */
    private Long id;

    /** 卡号 */
    private String cardNo;

    /** 卡密 */
    private String cardPassword;

    /** 额度 */
    private BigDecimal amount;

    /** 状态：0未使用，1已发放，2已使用 */
    private Integer status;

    /** 状态文本 */
    private String statusText;

    /** 发放给的会员ID */
    private Long memberId;

    /** 发放给的会员用户名 */
    private String memberName;

    /** 发放时间 */
    private String assignedAt;

    /** 使用时间 */
    private String usedAt;

    /** 创建时间 */
    private String createTime;
}
