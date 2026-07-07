package com.restaurant.vo;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class DashboardVO {

    private Integer todayOrderCount;
    private BigDecimal todayRevenue;
    private Double tableUsage;
    private Integer totalDishCount;
    private List<RevenueTrend> revenueTrend;
    private List<TopDish> topDishes;

    @Data
    public static class RevenueTrend {
        private String date;
        private BigDecimal revenue;
    }

    @Data
    public static class TopDish {
        private String dishName;
        private Integer orderCount;
    }
}
