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
 * 充值点卡实体
 */
@Data
@TableName("recharge_card")
public class RechargeCard {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 卡号（RC+yyyyMMdd+6位随机数） */
    private String cardNo;

    /** 卡密（16位随机字母数字） */
    private String cardPassword;

    /** 额度 */
    private BigDecimal amount;

    /** 状态：0未使用，1已发放，2已使用 */
    private Integer status;

    /** 发放给的会员ID */
    private Long memberId;

    /** 发放时间 */
    private LocalDateTime assignedAt;

    /** 使用时间 */
    private LocalDateTime usedAt;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除：0未删除，1已删除 */
    @TableLogic
    private Integer deleted;
}
