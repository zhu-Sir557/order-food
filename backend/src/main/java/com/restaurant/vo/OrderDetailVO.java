package com.restaurant.vo;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class OrderDetailVO {

    private Long id;
    private String orderNo;
    private Long tempUserId;
    private Long memberId;
    private Long tableId;
    private String tableCode;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusText;
    private Integer payMethod;
    private String payMethodText;
    private String remark;
    private String createTime;
    private String updateTime;
    private List<OrderItemVO> items;
}
