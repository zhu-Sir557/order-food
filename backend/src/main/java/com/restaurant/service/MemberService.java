package com.restaurant.service;

import com.restaurant.common.PageResult;
import com.restaurant.dto.RedeemCardDTO;
import com.restaurant.vo.BalanceRecordVO;
import com.restaurant.vo.MemberInfoVO;

/**
 * 会员信息服务接口
 */
public interface MemberService {

    /**
     * 获取会员信息
     *
     * @param memberId 会员ID
     * @return 会员信息 VO
     */
    MemberInfoVO getMemberInfo(Long memberId);

    /**
     * 兑换点卡（事务：余额增加 + 卡状态变更 + 记录 balance_record）
     *
     * @param memberId 会员ID
     * @param dto      兑换请求
     */
    void redeemCard(Long memberId, RedeemCardDTO dto);

    /**
     * 获取余额变动记录列表
     *
     * @param memberId 会员ID
     * @param page     页码
     * @param size     每页大小
     * @return 分页余额记录
     */
    PageResult<BalanceRecordVO> getBalanceRecords(Long memberId, Integer page, Integer size);
}
