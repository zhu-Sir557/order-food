package com.restaurant.service;

import com.restaurant.common.PageResult;
import com.restaurant.dto.OrderQueryDTO;
import com.restaurant.dto.OrderSubmitDTO;
import com.restaurant.vo.OrderDetailVO;

public interface OrderService {
    PageResult<OrderDetailVO> getOrderPage(OrderQueryDTO queryDTO);
    OrderDetailVO getOrderDetail(Long id);
    void updateOrderStatus(Long id, Integer status);
    OrderDetailVO submitOrder(OrderSubmitDTO submitDTO, Long tempUserId, Long memberId);
    void payOrder(Long id, Integer payMethod);
    void payByBalance(Long id, Long memberId);
    PageResult<OrderDetailVO> getH5OrderList(Long tempUserId, Long memberId, Integer status, Integer page, Integer size);
    OrderDetailVO getH5OrderDetail(Long id, Long tempUserId, Long memberId);
}
