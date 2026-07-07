package com.restaurant.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 余额变动记录响应 VO
 */
@Data
public class BalanceRecordVO {

    /** 记录ID */
    private Long id;

    /** 类型：1充值，2消费 */
    private Integer type;

    /** 类型文本 */
    private String typeText;

    /** 变动金额 */
    private BigDecimal amount;

    /** 变动后余额 */
    private BigDecimal balanceAfter;

    /** 充值卡号（充值时） */
    private String cardNo;

    /** 订单号（消费时） */
    private String orderNo;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private String createTime;
}
