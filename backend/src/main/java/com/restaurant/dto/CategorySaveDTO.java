package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategorySaveDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Integer sort = 0;

    private Integer status = 1;
}
