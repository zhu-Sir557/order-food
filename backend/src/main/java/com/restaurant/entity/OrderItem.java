package com.restaurant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;

/**
 * Order item entity (order line item).
 *
 * <p>This table does not use logical delete or auto-fill timestamps
 * since items are immutable once an order is created.</p>
 */
@Data
@TableName("order_item")
public class OrderItem {

    /** Primary key ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Order ID */
    private Long orderId;

    /** Dish ID */
    private Long dishId;

    /** Dish name (snapshot at order time) */
    private String dishName;

    /** Dish price (snapshot at order time) */
    private BigDecimal dishPrice;

    /** Dish image (snapshot at order time) */
    private String dishImage;

    /** Quantity ordered */
    private Integer quantity;

    /** Subtotal amount (dishPrice * quantity) */
    private BigDecimal subtotal;

    /** Taste selection (e.g. "微辣,不要香菜") */
    private String tasteSelection;
}
