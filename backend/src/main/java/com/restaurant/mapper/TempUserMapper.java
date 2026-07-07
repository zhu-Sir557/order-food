package com.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.entity.TempUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper interface for {@link TempUser}.
 */
@Mapper
public interface TempUserMapper extends BaseMapper<TempUser> {
}
