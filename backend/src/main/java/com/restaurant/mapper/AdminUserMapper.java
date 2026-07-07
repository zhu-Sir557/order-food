package com.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper interface for {@link AdminUser}.
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}
