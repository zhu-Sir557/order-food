package com.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.entity.Dish;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Mapper interface for {@link Dish}.
 *
 * <p>Includes custom query for paginated dish list with category name.</p>
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    /**
     * Paginated query of dishes with category name joined.
     *
     * @param page       pagination object
     * @param categoryId optional category filter (nullable)
     * @param name       optional dish name filter (nullable)
     * @param status     optional status filter (nullable)
     * @return paginated result with category name
     */
    IPage<Map<String, Object>> selectDishPage(
            Page<Map<String, Object>> page,
            @Param("categoryId") Long categoryId,
            @Param("name") String name,
            @Param("status") Integer status);
}
