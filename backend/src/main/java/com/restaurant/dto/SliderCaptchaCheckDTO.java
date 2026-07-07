package com.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 滑块验证码校验请求 DTO
 */
@Data
public class SliderCaptchaCheckDTO {

    /** 验证码ID */
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;

    /** 滑块 X 轴偏移量 */
    @NotNull(message = "偏移量不能为空")
    @JsonProperty("xOffset")
    private Integer xOffset;
}
