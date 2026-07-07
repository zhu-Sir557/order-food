package com.restaurant.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Admin user entity.
 */
@Data
@TableName("admin_user")
public class AdminUser {

    /** Primary key ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Username */
    private String username;

    /** Password (BCrypt hashed) */
    private String password;

    /** Display name */
    private String name;

    /** Avatar URL */
    private String avatar;

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
