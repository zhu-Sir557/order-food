package com.restaurant.controller.admin;

import com.restaurant.common.PageResult;
import com.restaurant.common.Result;
import com.restaurant.dto.AssignCardDTO;
import com.restaurant.dto.BatchCreateCardDTO;
import com.restaurant.dto.CardQueryDTO;
import com.restaurant.service.RechargeCardService;
import com.restaurant.vo.CardVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台点卡管理接口
 */
@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
public class AdminCardController {

    private final RechargeCardService rechargeCardService;

    /**
     * 批量创建点卡
     *
     * @param dto 批量创建请求
     * @return 创建的点卡列表
     */
    @PostMapping("/batch")
    public Result<List<CardVO>> batchCreate(@Valid @RequestBody BatchCreateCardDTO dto) {
        return Result.success(rechargeCardService.batchCreateCards(dto));
    }

    /**
     * 点卡列表（分页 + 筛选）
     *
     * @param dto 查询请求
     * @return 分页点卡列表
     */
    @GetMapping
    public Result<PageResult<CardVO>> getCardList(CardQueryDTO dto) {
        return Result.success(rechargeCardService.getCardPage(dto));
    }

    /**
     * 发放点卡给会员
     *
     * @param id  卡ID
     * @param dto 发放请求
     * @return 操作结果
     */
    @PostMapping("/{id}/assign")
    public Result<Void> assignCard(@PathVariable Long id, @Valid @RequestBody AssignCardDTO dto) {
        rechargeCardService.assignCard(id, dto);
        return Result.success();
    }
}
