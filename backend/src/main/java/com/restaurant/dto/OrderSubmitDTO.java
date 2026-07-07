package com.restaurant.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class OrderSubmitDTO {

    @NotNull(message = "桌台ID不能为空")
    private Long tableId;

    private String remark;

    @NotEmpty(message = "购物车不能为空")
    private List<CartItemDTO> items;
}
