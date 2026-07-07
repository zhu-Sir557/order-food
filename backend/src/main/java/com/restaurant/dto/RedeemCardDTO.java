package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 兑换点卡请求 DTO
 */
@Data
public class RedeemCardDTO {

    /** 卡号 */
    @NotBlank(message = "卡号不能为空")
    private String cardNo;

    /** 卡密 */
    @NotBlank(message = "卡密不能为空")
    private String cardPassword;
}
