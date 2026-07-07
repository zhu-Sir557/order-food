package com.restaurant.dto;

import lombok.Data;

/**
 * 点卡查询请求 DTO
 */
@Data
public class CardQueryDTO {

    /** 卡号（模糊查询） */
    private String cardNo;

    /** 状态：0未使用，1已发放，2已使用 */
    private Integer status;

    /** 当前页码 */
    private Integer page = 1;

    /** 每页大小 */
    private Integer size = 10;
}
