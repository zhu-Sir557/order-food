package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BannerSaveDTO {

    private String title;

    @NotBlank(message = "图片不能为空")
    private String image;

    private String link;

    private Integer sort;
}
