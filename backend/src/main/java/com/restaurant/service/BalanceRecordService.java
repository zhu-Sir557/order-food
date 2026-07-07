package com.restaurant.service;

import com.restaurant.common.PageResult;
import com.restaurant.vo.BalanceRecordVO;
import java.math.BigDecimal;

/**
 * 余额记录服务接口
 */
public interface BalanceRecordService {

    /**
     * 记录充值
     *
     * @param memberId     会员ID
     * @param amount       充值金额
     * @param balanceAfter 充值后余额
     * @param cardNo       充值卡号
     */
    void recordRecharge(Long memberId, BigDecimal amount, BigDecimal balanceAfter, String cardNo);

    /**
     * 记录消费
     *
     * @param memberId     会员ID
     * @param amount       消费金额
     * @param balanceAfter 消费后余额
     * @param orderNo      订单号
     * @param orderId      订单ID
     */
    void recordConsume(Long memberId, BigDecimal amount, BigDecimal balanceAfter, String orderNo, Long orderId);

    /**
     * 按会员查询余额记录
     *
     * @param memberId 会员ID
     * @param page     页码
     * @param size     每页大小
     * @return 分页余额记录
     */
    PageResult<BalanceRecordVO> getRecordsByMember(Long memberId, Integer page, Integer size);
}
