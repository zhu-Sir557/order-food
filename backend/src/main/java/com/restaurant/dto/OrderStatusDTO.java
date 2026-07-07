package com.restaurant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;
}
