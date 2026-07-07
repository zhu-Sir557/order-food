package com.restaurant.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DishVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private BigDecimal price;
    private String image;
    private String description;
    private String tasteConfig;
    private Integer stock;
    private Integer status;
    private String createTime;
}
