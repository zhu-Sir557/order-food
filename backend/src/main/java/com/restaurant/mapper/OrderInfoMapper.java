package com.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.entity.OrderInfo;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Mapper interface for {@link OrderInfo}.
 *
 * <p>Includes custom query for order details with associated order items.</p>
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /**
     * Query order detail by order ID, including all order items.
     *
     * @param orderId order ID
     * @return a map containing order info and item list
     */
    Map<String, Object> selectOrderDetailById(@Param("orderId") Long orderId);

    /**
     * Query orders by temporary user ID with their items.
     *
     * @param tempUserId temporary user ID
     * @return list of order detail maps
     */
    List<Map<String, Object>> selectOrdersByTempUserId(@Param("tempUserId") Long tempUserId);
}
