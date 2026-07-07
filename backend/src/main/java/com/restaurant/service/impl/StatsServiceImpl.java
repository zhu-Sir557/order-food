package com.restaurant.service.impl;

import com.restaurant.entity.DiningTable;
import com.restaurant.entity.Dish;
import com.restaurant.entity.OrderInfo;
import com.restaurant.entity.OrderItem;
import com.restaurant.enums.OrderStatus;
import com.restaurant.mapper.DiningTableMapper;
import com.restaurant.mapper.DishMapper;
import com.restaurant.mapper.OrderInfoMapper;
import com.restaurant.mapper.OrderItemMapper;
import com.restaurant.service.StatsService;
import com.restaurant.vo.DashboardVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final DishMapper dishMapper;
    private final DiningTableMapper diningTableMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        // Today's order count (exclude cancelled)
        LambdaQueryWrapper<OrderInfo> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.ge(OrderInfo::getCreateTime, todayStart);
        orderWrapper.le(OrderInfo::getCreateTime, todayEnd);
        orderWrapper.ne(OrderInfo::getStatus, OrderStatus.CANCELLED.getCode());
        List<OrderInfo> todayOrders = orderInfoMapper.selectList(orderWrapper);
        vo.setTodayOrderCount(todayOrders.size());

        // Today's revenue (completed orders)
        BigDecimal todayRevenue = BigDecimal.ZERO;
        for (OrderInfo order : todayOrders) {
            if (order.getStatus() >= OrderStatus.COMPLETED.getCode()) {
                todayRevenue = todayRevenue.add(order.getTotalAmount());
            }
        }
        vo.setTodayRevenue(todayRevenue);

        // Table usage
        List<DiningTable> allTables = diningTableMapper.selectList(null);
        long inUseCount = allTables.stream().filter(t -> t.getStatus() == 1).count();
        double usage = allTables.isEmpty() ? 0.0 : (double) inUseCount / allTables.size() * 100;
        vo.setTableUsage(usage);

        // Total dish count (on shelf)
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(Dish::getStatus, 1);
        vo.setTotalDishCount(Math.toIntExact(dishMapper.selectCount(dishWrapper)));

        // Revenue trend (last 7 days)
        List<DashboardVO.RevenueTrend> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            LambdaQueryWrapper<OrderInfo> dayWrapper = new LambdaQueryWrapper<>();
            dayWrapper.ge(OrderInfo::getCreateTime, dayStart);
            dayWrapper.le(OrderInfo::getCreateTime, dayEnd);
            dayWrapper.ge(OrderInfo::getStatus, OrderStatus.COMPLETED.getCode());

            List<OrderInfo> dayOrders = orderInfoMapper.selectList(dayWrapper);
            BigDecimal dayRevenue = dayOrders.stream()
                    .map(OrderInfo::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            DashboardVO.RevenueTrend rt = new DashboardVO.RevenueTrend();
            rt.setDate(date.format(DATE_FMT));
            rt.setRevenue(dayRevenue);
            trend.add(rt);
        }
        vo.setRevenueTrend(trend);

        // Top 10 dishes
        LambdaQueryWrapper<OrderItem> topWrapper = new LambdaQueryWrapper<>();
        topWrapper.ge(OrderItem::getId, 0);
        List<OrderItem> allItems = orderItemMapper.selectList(topWrapper);

        Map<String, Integer> dishCountMap = new HashMap<>();
        for (OrderItem item : allItems) {
            dishCountMap.merge(item.getDishName(), item.getQuantity(), Integer::sum);
        }

        List<Map.Entry<String, Integer>> sorted = dishCountMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .collect(Collectors.toList());

        List<DashboardVO.TopDish> topDishes = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sorted) {
            DashboardVO.TopDish td = new DashboardVO.TopDish();
            td.setDishName(entry.getKey());
            td.setOrderCount(entry.getValue());
            topDishes.add(td);
        }
        vo.setTopDishes(topDishes);

        return vo;
    }
}
