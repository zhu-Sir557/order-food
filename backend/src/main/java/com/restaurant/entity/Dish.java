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
 * Dish entity.
 */
@Data
@TableName("dish")
public class Dish {

    /** Primary key ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Category ID */
    private Long categoryId;

    /** Dish name */
    private String name;

    /** Price */
    private BigDecimal price;

    /** Image URL */
    private String image;

    /** Description */
    private String description;

    /** Taste configuration JSON (defines available taste options for this dish) */
    private String tasteConfig;

    /** Stock quantity */
    private Integer stock;

    /** Status: 0 off shelf, 1 on shelf */
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
