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
 * Temporary user entity for H5 customer sessions.
 */
@Data
@TableName("temp_user")
public class TempUser {

    /** Primary key ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** User token */
    private String token;

    /** Creation time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** Logical delete flag: 0 not deleted, 1 deleted */
    @TableLogic
    private Integer deleted;
}
