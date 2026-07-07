package com.restaurant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发放点卡请求 DTO
 */
@Data
public class AssignCardDTO {

    /** 发放给的会员ID */
    @NotNull(message = "会员ID不能为空")
    private Long memberId;
}
