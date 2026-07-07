package com.restaurant.vo;

import lombok.Data;

@Data
public class TableVO {

    private Long id;
    private String code;
    private String name;
    private Integer capacity;
    private Integer status;
}
