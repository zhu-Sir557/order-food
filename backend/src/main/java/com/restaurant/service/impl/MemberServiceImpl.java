package com.restaurant.service.impl;

import com.restaurant.common.BizException;
import com.restaurant.common.PageResult;
import com.restaurant.dto.RedeemCardDTO;
import com.restaurant.entity.Member;
import com.restaurant.entity.RechargeCard;
import com.restaurant.enums.CardStatus;
import com.restaurant.mapper.MemberMapper;
import com.restaurant.service.BalanceRecordService;
import com.restaurant.service.MemberService;
import com.restaurant.service.RechargeCardService;
import com.restaurant.vo.BalanceRecordVO;
import com.restaurant.vo.MemberInfoVO;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会员信息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final RechargeCardService rechargeCardService;
    private final BalanceRecordService balanceRecordService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public MemberInfoVO getMemberInfo(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException("会员不存在");
        }

        MemberInfoVO vo = new MemberInfoVO();
        vo.setMemberId(member.getId());
        vo.setUsername(member.getUsername());
        vo.setBalance(member.getBalance());
        vo.setNickname(member.getNickname());
        vo.setAvatar(member.getAvatar());
        if (member.getCreateTime() != null) {
            vo.setCreateTime(member.getCreateTime().format(FMT));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void redeemCard(Long memberId, RedeemCardDTO dto) {
        // 查找点卡
        RechargeCard card = rechargeCardService.findByCardNoAndPassword(dto.getCardNo(), dto.getCardPassword());
        if (card == null) {
            throw new BizException("卡号或卡密错误");
        }

        // 校验卡状态
        if (card.getStatus() != CardStatus.ASSIGNED.getCode()) {
            throw new BizException("该卡不可用或已被使用");
        }

        // 校验卡属于该会员（如果卡已发放给其他会员则不允许兑换）
        if (card.getMemberId() != null && !card.getMemberId().equals(memberId)) {
            throw new BizException("该卡不属于当前会员");
        }

        // 查询会员
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException("会员不存在");
        }

        // 增加余额
        BigDecimal newBalance = member.getBalance().add(card.getAmount());
        member.setBalance(newBalance);
        memberMapper.updateById(member);

        // 更新卡状态为已使用
        rechargeCardService.markCardAsUsed(card.getId());

        // 记录充值
        balanceRecordService.recordRecharge(memberId, card.getAmount(), newBalance, card.getCardNo());

        log.info("点卡兑换成功: memberId={}, cardNo={}, amount={}", memberId, card.getCardNo(), card.getAmount());
    }

    @Override
    public PageResult<BalanceRecordVO> getBalanceRecords(Long memberId, Integer page, Integer size) {
        return balanceRecordService.getRecordsByMember(memberId, page, size);
    }
}
