package com.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.entity.Member;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员 Mapper 接口
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
