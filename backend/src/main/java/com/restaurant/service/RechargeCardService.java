package com.restaurant.service;

import com.restaurant.common.PageResult;
import com.restaurant.dto.AssignCardDTO;
import com.restaurant.dto.BatchCreateCardDTO;
import com.restaurant.dto.CardQueryDTO;
import com.restaurant.entity.RechargeCard;
import com.restaurant.vo.CardVO;
import java.util.List;

/**
 * 点卡服务接口
 */
public interface RechargeCardService {

    /**
     * 批量创建点卡
     *
     * @param dto 批量创建请求
     * @return 创建的点卡列表
     */
    List<CardVO> batchCreateCards(BatchCreateCardDTO dto);

    /**
     * 点卡列表（分页 + 筛选）
     *
     * @param dto 查询请求
     * @return 分页点卡列表
     */
    PageResult<CardVO> getCardPage(CardQueryDTO dto);

    /**
     * 发放点卡给会员
     *
     * @param cardId 卡ID
     * @param dto    发放请求
     */
    void assignCard(Long cardId, AssignCardDTO dto);

    /**
     * 按卡号 + 卡密查询（兑换时用）
     *
     * @param cardNo       卡号
     * @param cardPassword 卡密
     * @return 点卡实体，未找到返回 null
     */
    RechargeCard findByCardNoAndPassword(String cardNo, String cardPassword);

    /**
     * 标记点卡为已使用
     *
     * @param cardId 卡ID
     */
    void markCardAsUsed(Long cardId);

    /**
     * 查询已发放给指定会员的点卡（状态=已发放）
     *
     * @param memberId 会员ID
     * @return 点卡列表
     */
    List<CardVO> getAssignedCardsByMember(Long memberId);
}
