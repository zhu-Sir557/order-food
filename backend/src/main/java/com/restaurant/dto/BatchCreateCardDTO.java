package com.restaurant.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 批量创建点卡请求 DTO
 */
@Data
public class BatchCreateCardDTO {

    /** 额度 */
    @NotNull(message = "额度不能为空")
    @DecimalMin(value = "0.01", message = "额度必须大于0")
    private BigDecimal amount;

    /** 创建数量 */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    @Max(value = 100, message = "单次最多创建100张")
    private Integer count;
}
