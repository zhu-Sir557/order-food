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
 * Dining table entity.
 */
@Data
@TableName("dining_table")
public class DiningTable {

    /** Primary key ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Table code */
    private String code;

    /** Table name */
    private String name;

    /** Seating capacity */
    private Integer capacity;

    /** Status: 0 idle, 1 in use */
    private Integer status;

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
