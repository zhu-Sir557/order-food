package com.restaurant.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderItemVO {

    private Long id;
    private Long dishId;
    private String dishName;
    private BigDecimal dishPrice;
    private String dishImage;
    private Integer quantity;
    private BigDecimal subtotal;
    private String tasteSelection;
}
