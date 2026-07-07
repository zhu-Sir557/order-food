package com.restaurant.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 余额变动记录实体
 * 注意：该表只有 createTime，没有 updateTime（记录不可修改）
 */
@Data
@TableName("balance_record")
public class BalanceRecord {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会员ID */
    private Long memberId;

    /** 类型：1充值，2消费 */
    private Integer type;

    /** 变动金额 */
    private BigDecimal amount;

    /** 变动后余额 */
    private BigDecimal balanceAfter;

    /** 充值卡号（充值时） */
    private String cardNo;

    /** 订单号（消费时） */
    private String orderNo;

    /** 订单ID（消费时） */
    private Long orderId;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 逻辑删除：0未删除，1已删除 */
    @TableLogic
    private Integer deleted;
}
