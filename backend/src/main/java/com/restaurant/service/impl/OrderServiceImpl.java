package com.restaurant.service.impl;

import com.restaurant.common.BizException;
import com.restaurant.common.PageResult;
import com.restaurant.dto.CartItemDTO;
import com.restaurant.dto.OrderQueryDTO;
import com.restaurant.dto.OrderSubmitDTO;
import com.restaurant.entity.DiningTable;
import com.restaurant.entity.Dish;
import com.restaurant.entity.Member;
import com.restaurant.entity.OrderInfo;
import com.restaurant.entity.OrderItem;
import com.restaurant.enums.OrderStatus;
import com.restaurant.enums.PayMethod;
import com.restaurant.mapper.DiningTableMapper;
import com.restaurant.mapper.DishMapper;
import com.restaurant.mapper.MemberMapper;
import com.restaurant.mapper.OrderInfoMapper;
import com.restaurant.mapper.OrderItemMapper;
import com.restaurant.service.BalanceRecordService;
import com.restaurant.service.OrderService;
import com.restaurant.util.OrderNoUtil;
import com.restaurant.vo.OrderDetailVO;
import com.restaurant.vo.OrderItemVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final DishMapper dishMapper;
    private final DiningTableMapper diningTableMapper;
    private final MemberMapper memberMapper;
    private final BalanceRecordService balanceRecordService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<OrderDetailVO> getOrderPage(OrderQueryDTO queryDTO) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getStatus() != null) {
            wrapper.eq(OrderInfo::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getTableCode())) {
            wrapper.like(OrderInfo::getTableCode, queryDTO.getTableCode());
        }
        if (StringUtils.hasText(queryDTO.getStartDate())) {
            wrapper.ge(OrderInfo::getCreateTime, LocalDateTime.parse(queryDTO.getStartDate() + " 00:00:00", FMT));
        }
        if (StringUtils.hasText(queryDTO.getEndDate())) {
            wrapper.le(OrderInfo::getCreateTime, LocalDateTime.parse(queryDTO.getEndDate() + " 23:59:59", FMT));
        }
        wrapper.orderByDesc(OrderInfo::getCreateTime);

        Page<OrderInfo> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        Page<OrderInfo> result = orderInfoMapper.selectPage(page, wrapper);

        List<OrderDetailVO> voList = result.getRecords().stream()
                .map(this::toOrderDetailVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public OrderDetailVO getOrderDetail(Long id) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        return toOrderDetailVO(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long id, Integer status) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }

        OrderStatus current = OrderStatus.fromCode(order.getStatus());
        OrderStatus target = OrderStatus.fromCode(status);

        if (current == null || target == null) {
            throw new BizException("无效的订单状态");
        }

        if (!current.canTransitTo(target)) {
            throw new BizException("非法的订单状态变更");
        }

        order.setStatus(target.getCode());
        orderInfoMapper.updateById(order);

        // If picked up, set table to idle
        if (target == OrderStatus.PICKED_UP && order.getTableId() != null) {
            DiningTable table = diningTableMapper.selectById(order.getTableId());
            if (table != null) {
                table.setStatus(0);
                diningTableMapper.updateById(table);
            }
        }
    }

    @Override
    @Transactional
    public OrderDetailVO submitOrder(OrderSubmitDTO submitDTO, Long tempUserId, Long memberId) {
        // Validate table
        DiningTable table = diningTableMapper.selectById(submitDTO.getTableId());
        if (table == null) {
            throw new BizException("桌台不存在");
        }

        // Calculate total and build order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemDTO item : submitDTO.getItems()) {
            Dish dish = dishMapper.selectById(item.getDishId());
            if (dish == null) {
                throw new BizException("菜品不存在: " + item.getDishId());
            }
            if (dish.getStatus() != 1) {
                throw new BizException("菜品已下架: " + dish.getName());
            }

            BigDecimal subtotal = dish.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setDishId(dish.getId());
            orderItem.setDishName(dish.getName());
            orderItem.setDishPrice(dish.getPrice());
            orderItem.setDishImage(dish.getImage());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(subtotal);
            orderItem.setTasteSelection(item.getTasteSelection());
            orderItems.add(orderItem);
        }

        // Create order
        OrderInfo order = new OrderInfo();
        order.setOrderNo(OrderNoUtil.generateOrderNo());
        order.setTempUserId(tempUserId);
        order.setMemberId(memberId);
        order.setTableId(table.getId());
        order.setTableCode(table.getCode());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING_PAY.getCode());
        order.setRemark(submitDTO.getRemark());
        orderInfoMapper.insert(order);

        // Insert order items
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        return toOrderDetailVO(order);
    }

    @Override
    @Transactional
    public void payOrder(Long id, Integer payMethod) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }

        OrderStatus current = OrderStatus.fromCode(order.getStatus());
        if (current != OrderStatus.PENDING_PAY) {
            throw new BizException("订单状态不允许支付");
        }

        order.setStatus(OrderStatus.PENDING_ACCEPT.getCode());
        order.setPayMethod(payMethod);
        orderInfoMapper.updateById(order);

        // Set table to in-use
        if (order.getTableId() != null) {
            DiningTable table = diningTableMapper.selectById(order.getTableId());
            if (table != null) {
                table.setStatus(1);
                diningTableMapper.updateById(table);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payByBalance(Long id, Long memberId) {
        // 查询订单
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }

        // 校验订单状态
        if (order.getStatus() != OrderStatus.PENDING_PAY.getCode()) {
            throw new BizException("订单状态不允许支付");
        }

        // 校验订单归属
        if (memberId != null && !memberId.equals(order.getMemberId())) {
            throw new BizException("无权操作此订单");
        }

        // 查询会员
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException("会员不存在");
        }

        // 校验余额
        if (member.getBalance().compareTo(order.getTotalAmount()) < 0) {
            throw new BizException("余额不足");
        }

        // 扣减余额
        BigDecimal newBalance = member.getBalance().subtract(order.getTotalAmount());
        member.setBalance(newBalance);
        memberMapper.updateById(member);

        // 订单状态变更
        order.setStatus(OrderStatus.PENDING_ACCEPT.getCode());
        order.setPayMethod(PayMethod.BALANCE.getCode());
        orderInfoMapper.updateById(order);

        // 桌台状态设为就餐中
        if (order.getTableId() != null) {
            DiningTable table = diningTableMapper.selectById(order.getTableId());
            if (table != null) {
                table.setStatus(1);
                diningTableMapper.updateById(table);
            }
        }

        // 记录余额变动
        balanceRecordService.recordConsume(memberId, order.getTotalAmount(), newBalance,
                order.getOrderNo(), order.getId());

        log.info("余额支付成功: orderId={}, memberId={}, amount={}", id, memberId, order.getTotalAmount());
    }

    @Override
    public PageResult<OrderDetailVO> getH5OrderList(Long tempUserId, Long memberId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        // 优先用 memberId 查询，否则用 tempUserId
        if (memberId != null) {
            wrapper.eq(OrderInfo::getMemberId, memberId);
        } else if (tempUserId != null) {
            wrapper.eq(OrderInfo::getTempUserId, tempUserId);
        } else {
            // 两者都为 null，返回空列表
            return new PageResult<>(new ArrayList<>(), 0L, (long) page, (long) size);
        }
        if (status != null) {
            wrapper.eq(OrderInfo::getStatus, status);
        }
        wrapper.orderByDesc(OrderInfo::getCreateTime);

        Page<OrderInfo> pageObj = new Page<>(page, size);
        Page<OrderInfo> result = orderInfoMapper.selectPage(pageObj, wrapper);

        List<OrderDetailVO> voList = result.getRecords().stream()
                .map(this::toOrderDetailVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public OrderDetailVO getH5OrderDetail(Long id, Long tempUserId, Long memberId) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        // 归属校验：优先用 memberId，其次用 tempUserId
        boolean hasAccess = false;
        if (memberId != null && memberId.equals(order.getMemberId())) {
            hasAccess = true;
        } else if (tempUserId != null && tempUserId.equals(order.getTempUserId())) {
            hasAccess = true;
        }
        if (!hasAccess) {
            throw new BizException("无权查看此订单");
        }
        return toOrderDetailVO(order);
    }

    private OrderDetailVO toOrderDetailVO(OrderInfo order) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTempUserId(order.getTempUserId());
        vo.setMemberId(order.getMemberId());
        vo.setTableId(order.getTableId());
        vo.setTableCode(order.getTableCode());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setPayMethod(order.getPayMethod());
        vo.setRemark(order.getRemark());

        OrderStatus status = OrderStatus.fromCode(order.getStatus());
        vo.setStatusText(status != null ? status.getDesc() : "未知");

        // 支付方式文本
        if (order.getPayMethod() != null) {
            PayMethod payMethod = PayMethod.fromCode(order.getPayMethod());
            vo.setPayMethodText(payMethod != null ? payMethod.getDesc() : "未知");
        }

        if (order.getCreateTime() != null) {
            vo.setCreateTime(order.getCreateTime().format(FMT));
        }
        if (order.getUpdateTime() != null) {
            vo.setUpdateTime(order.getUpdateTime().format(FMT));
        }

        // Load order items
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        List<OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setDishId(item.getDishId());
            itemVO.setDishName(item.getDishName());
            itemVO.setDishPrice(item.getDishPrice());
            itemVO.setDishImage(item.getDishImage());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setSubtotal(item.getSubtotal());
            itemVO.setTasteSelection(item.getTasteSelection());
            return itemVO;
        }).collect(Collectors.toList());

        vo.setItems(itemVOs);
        return vo;
    }
}
