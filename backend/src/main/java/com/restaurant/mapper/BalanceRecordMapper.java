package com.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.entity.BalanceRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 余额变动记录 Mapper 接口
 */
@Mapper
public interface BalanceRecordMapper extends BaseMapper<BalanceRecord> {
}
