package com.restaurant.controller.h5;

import com.restaurant.common.PageResult;
import com.restaurant.common.Result;
import com.restaurant.dto.OrderSubmitDTO;
import com.restaurant.service.OrderService;
import com.restaurant.vo.OrderDetailVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/h5/orders")
@RequiredArgsConstructor
public class H5OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderDetailVO> submitOrder(@Valid @RequestBody OrderSubmitDTO submitDTO,
                                             HttpServletRequest request) {
        Long tempUserId = (Long) request.getAttribute("tempUserId");
        Long memberId = (Long) request.getAttribute("memberId");
        return Result.success(orderService.submitOrder(submitDTO, tempUserId, memberId));
    }

    @PostMapping("/{id}/pay")
    public Result<Void> payOrder(@PathVariable Long id,
                                 @RequestParam Integer payMethod) {
        orderService.payOrder(id, payMethod);
        return Result.success();
    }

    @PostMapping("/{id}/pay/balance")
    public Result<Void> payByBalance(@PathVariable Long id,
                                     HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        orderService.payByBalance(id, memberId);
        return Result.success();
    }

    @GetMapping
    public Result<PageResult<OrderDetailVO>> getOrderList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long tempUserId = (Long) request.getAttribute("tempUserId");
        Long memberId = (Long) request.getAttribute("memberId");
        return Result.success(orderService.getH5OrderList(tempUserId, memberId, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable Long id,
                                                HttpServletRequest request) {
        Long tempUserId = (Long) request.getAttribute("tempUserId");
        Long memberId = (Long) request.getAttribute("memberId");
        return Result.success(orderService.getH5OrderDetail(id, tempUserId, memberId));
    }
}
