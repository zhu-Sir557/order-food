package com.restaurant.vo;

import lombok.Data;

@Data
public class CategoryVO {

    private Long id;
    private String name;
    private Integer sort;
    private Integer status;
    private Integer dishCount;
}
