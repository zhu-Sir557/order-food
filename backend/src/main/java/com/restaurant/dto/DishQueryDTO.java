package com.restaurant.dto;

import lombok.Data;

@Data
public class DishQueryDTO {

    private String name;

    private Long categoryId;

    private Integer status;

    private Integer page = 1;

    private Integer size = 10;
}
