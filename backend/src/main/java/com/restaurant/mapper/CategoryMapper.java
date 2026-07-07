package com.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper interface for {@link Category}.
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
