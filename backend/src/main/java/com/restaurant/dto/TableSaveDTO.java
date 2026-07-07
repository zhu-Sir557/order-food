package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TableSaveDTO {

    @NotBlank(message = "桌号不能为空")
    private String code;

    @NotBlank(message = "桌台名称不能为空")
    private String name;

    @NotNull(message = "容量不能为空")
    private Integer capacity;

    private Integer status = 0;
}
