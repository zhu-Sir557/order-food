package com.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.entity.OrderItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Mapper interface for {@link OrderItem}.
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * Query all items for a given order ID.
     *
     * @param orderId order ID
     * @return list of order items
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
}
