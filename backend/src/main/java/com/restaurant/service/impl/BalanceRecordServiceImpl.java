package com.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.PageResult;
import com.restaurant.entity.BalanceRecord;
import com.restaurant.enums.BalanceRecordType;
import com.restaurant.mapper.BalanceRecordMapper;
import com.restaurant.service.BalanceRecordService;
import com.restaurant.vo.BalanceRecordVO;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 余额记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceRecordServiceImpl implements BalanceRecordService {

    private final BalanceRecordMapper recordMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void recordRecharge(Long memberId, BigDecimal amount, BigDecimal balanceAfter, String cardNo) {
        BalanceRecord record = new BalanceRecord();
        record.setMemberId(memberId);
        record.setType(BalanceRecordType.RECHARGE.getCode());
        record.setAmount(amount);
        record.setBalanceAfter(balanceAfter);
        record.setCardNo(cardNo);
        record.setRemark("点卡充值");
        recordMapper.insert(record);
        log.info("记录充值: memberId={}, amount={}, cardNo={}", memberId, amount, cardNo);
    }

    @Override
    public void recordConsume(Long memberId, BigDecimal amount, BigDecimal balanceAfter, String orderNo, Long orderId) {
        BalanceRecord record = new BalanceRecord();
        record.setMemberId(memberId);
        record.setType(BalanceRecordType.CONSUME.getCode());
        record.setAmount(amount);
        record.setBalanceAfter(balanceAfter);
        record.setOrderNo(orderNo);
        record.setOrderId(orderId);
        record.setRemark("余额支付订单");
        recordMapper.insert(record);
        log.info("记录消费: memberId={}, amount={}, orderNo={}", memberId, amount, orderNo);
    }

    @Override
    public PageResult<BalanceRecordVO> getRecordsByMember(Long memberId, Integer page, Integer size) {
        LambdaQueryWrapper<BalanceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BalanceRecord::getMemberId, memberId);
        wrapper.orderByDesc(BalanceRecord::getCreateTime);

        Page<BalanceRecord> pageObj = new Page<>(page, size);
        Page<BalanceRecord> result = recordMapper.selectPage(pageObj, wrapper);

        List<BalanceRecordVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 实体转 VO
     *
     * @param record 余额记录实体
     * @return VO
     */
    private BalanceRecordVO toVO(BalanceRecord record) {
        BalanceRecordVO vo = new BalanceRecordVO();
        vo.setId(record.getId());
        vo.setType(record.getType());
        BalanceRecordType type = BalanceRecordType.fromCode(record.getType());
        vo.setTypeText(type != null ? type.getDesc() : "未知");
        vo.setAmount(record.getAmount());
        vo.setBalanceAfter(record.getBalanceAfter());
        vo.setCardNo(record.getCardNo());
        vo.setOrderNo(record.getOrderNo());
        vo.setRemark(record.getRemark());
        if (record.getCreateTime() != null) {
            vo.setCreateTime(record.getCreateTime().format(FMT));
        }
        return vo;
    }
}
