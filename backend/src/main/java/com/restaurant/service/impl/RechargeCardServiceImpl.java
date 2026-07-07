package com.restaurant.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.BizException;
import com.restaurant.common.PageResult;
import com.restaurant.dto.AssignCardDTO;
import com.restaurant.dto.BatchCreateCardDTO;
import com.restaurant.dto.CardQueryDTO;
import com.restaurant.entity.Member;
import com.restaurant.entity.RechargeCard;
import com.restaurant.enums.CardStatus;
import com.restaurant.mapper.MemberMapper;
import com.restaurant.mapper.RechargeCardMapper;
import com.restaurant.service.RechargeCardService;
import com.restaurant.vo.CardVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 充值点卡服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RechargeCardServiceImpl implements RechargeCardService {

    private final RechargeCardMapper cardMapper;
    private final MemberMapper memberMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 卡号最大重试次数 */
    private static final int MAX_RETRY = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CardVO> batchCreateCards(BatchCreateCardDTO dto) {
        List<CardVO> result = new ArrayList<>();

        for (int i = 0; i < dto.getCount(); i++) {
            RechargeCard card = createSingleCard(dto.getAmount());
            result.add(toVO(card));
        }

        log.info("批量创建点卡: count={}, amount={}", dto.getCount(), dto.getAmount());
        return result;
    }

    @Override
    public PageResult<CardVO> getCardPage(CardQueryDTO dto) {
        LambdaQueryWrapper<RechargeCard> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getCardNo())) {
            wrapper.like(RechargeCard::getCardNo, dto.getCardNo());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(RechargeCard::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(RechargeCard::getCreateTime);

        Page<RechargeCard> page = new Page<>(dto.getPage(), dto.getSize());
        Page<RechargeCard> result = cardMapper.selectPage(page, wrapper);

        List<CardVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignCard(Long cardId, AssignCardDTO dto) {
        RechargeCard card = cardMapper.selectById(cardId);
        if (card == null) {
            throw new BizException("点卡不存在");
        }

        if (card.getStatus() != CardStatus.UNUSED.getCode()) {
            throw new BizException("该卡已发放或已使用");
        }

        Member member = memberMapper.selectById(dto.getMemberId());
        if (member == null) {
            throw new BizException("会员不存在");
        }

        card.setStatus(CardStatus.ASSIGNED.getCode());
        card.setMemberId(dto.getMemberId());
        card.setAssignedAt(LocalDateTime.now());
        cardMapper.updateById(card);

        log.info("点卡发放: cardId={}, cardNo={}, memberId={}", cardId, card.getCardNo(), dto.getMemberId());
    }

    @Override
    public RechargeCard findByCardNoAndPassword(String cardNo, String cardPassword) {
        LambdaQueryWrapper<RechargeCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeCard::getCardNo, cardNo);
        wrapper.eq(RechargeCard::getCardPassword, cardPassword);
        return cardMapper.selectOne(wrapper);
    }

    @Override
    public void markCardAsUsed(Long cardId) {
        RechargeCard card = cardMapper.selectById(cardId);
        if (card == null) {
            throw new BizException("点卡不存在");
        }
        card.setStatus(CardStatus.USED.getCode());
        card.setUsedAt(LocalDateTime.now());
        cardMapper.updateById(card);
    }

    @Override
    public List<CardVO> getAssignedCardsByMember(Long memberId) {
        LambdaQueryWrapper<RechargeCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeCard::getMemberId, memberId);
        wrapper.eq(RechargeCard::getStatus, CardStatus.ASSIGNED.getCode());
        wrapper.orderByDesc(RechargeCard::getAssignedAt);
        List<RechargeCard> cards = cardMapper.selectList(wrapper);
        return cards.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 创建单张点卡（含卡号碰撞重试）
     *
     * @param amount 额度
     * @return 创建的点卡实体
     */
    private RechargeCard createSingleCard(java.math.BigDecimal amount) {
        for (int retry = 0; retry < MAX_RETRY; retry++) {
            try {
                RechargeCard card = new RechargeCard();
                card.setCardNo(generateCardNo());
                card.setCardPassword(generateCardPassword());
                card.setAmount(amount);
                card.setStatus(CardStatus.UNUSED.getCode());
                cardMapper.insert(card);
                return card;
            } catch (DuplicateKeyException e) {
                log.warn("卡号碰撞，重试: retry={}", retry + 1);
            }
        }
        throw new BizException("卡号生成失败，请重试");
    }

    /**
     * 生成卡号：RC + yyyyMMdd + 6位随机数
     *
     * @return 卡号
     */
    private String generateCardNo() {
        return "RC" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
                + RandomUtil.randomNumbers(6);
    }

    /**
     * 生成卡密：16位随机字母数字
     *
     * @return 卡密
     */
    private String generateCardPassword() {
        return RandomUtil.randomString("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", 16);
    }

    /**
     * 实体转 VO
     *
     * @param card 点卡实体
     * @return VO
     */
    private CardVO toVO(RechargeCard card) {
        CardVO vo = new CardVO();
        vo.setId(card.getId());
        vo.setCardNo(card.getCardNo());
        vo.setCardPassword(card.getCardPassword());
        vo.setAmount(card.getAmount());
        vo.setStatus(card.getStatus());
        CardStatus status = CardStatus.fromCode(card.getStatus());
        vo.setStatusText(status != null ? status.getDesc() : "未知");
        vo.setMemberId(card.getMemberId());

        // 查询会员用户名
        if (card.getMemberId() != null) {
            Member member = memberMapper.selectById(card.getMemberId());
            if (member != null) {
                vo.setMemberName(member.getUsername());
            }
        }

        if (card.getAssignedAt() != null) {
            vo.setAssignedAt(card.getAssignedAt().format(FMT));
        }
        if (card.getUsedAt() != null) {
            vo.setUsedAt(card.getUsedAt().format(FMT));
        }
        if (card.getCreateTime() != null) {
            vo.setCreateTime(card.getCreateTime().format(FMT));
        }
        return vo;
    }
}
