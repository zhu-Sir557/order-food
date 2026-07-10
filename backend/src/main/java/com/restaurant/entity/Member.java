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
 * 注册会员实体
 */
@Data
@TableName("member")
public class Member {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账户名（唯一） */
    private String username;

    /** 密码（BCrypt加密） */
    private String password;

    /** 账户余额 */
    private BigDecimal balance;

    /** 关联的临时用户ID */
    private Long tempUserId;

    /** 手机号（短信验证码登录） */
    private String phone;

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
