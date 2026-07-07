package com.restaurant.controller.admin;

import com.restaurant.common.PageResult;
import com.restaurant.common.Result;
import com.restaurant.dto.OrderQueryDTO;
import com.restaurant.dto.OrderStatusDTO;
import com.restaurant.service.OrderService;
import com.restaurant.vo.OrderDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/page")
    public Result<PageResult<OrderDetailVO>> getOrderPage(OrderQueryDTO queryDTO) {
        return Result.success(orderService.getOrderPage(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatusDTO statusDTO) {
        orderService.updateOrderStatus(id, statusDTO.getStatus());
        return Result.success();
    }
}
