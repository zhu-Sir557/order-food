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
 * Order information entity.
 */
@Data
@TableName("order_info")
public class OrderInfo {

    /** Primary key ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Order number */
    private String orderNo;

    /** Temporary user ID */
    private Long tempUserId;

    /** 会员ID（注册用户下单时关联） */
    private Long memberId;

    /** Dining table ID */
    private Long tableId;

    /** Dining table code */
    private String tableCode;

    /** Total amount */
    private BigDecimal totalAmount;

    /** Order status: 0 pending pay, 1 pending accept, 2 cooking, 3 completed, 4 picked up, 5 cancelled */
    private Integer status;

    /** 支付方式：1微信，2支付宝，3余额 */
    private Integer payMethod;

    /** Remark */
    private String remark;

    /** Creation time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** Update time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** Logical delete flag: 0 not deleted, 1 deleted */
    @TableLogic
    private Integer deleted;
}
