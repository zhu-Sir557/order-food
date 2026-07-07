package com.restaurant.dto;

import lombok.Data;

@Data
public class OrderQueryDTO {

    private Integer status;

    private String tableCode;

    private String startDate;

    private String endDate;

    private Integer page = 1;

    private Integer size = 10;
}
