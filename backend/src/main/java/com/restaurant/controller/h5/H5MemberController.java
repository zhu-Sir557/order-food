package com.restaurant.controller.h5;

import com.restaurant.common.PageResult;
import com.restaurant.common.Result;
import com.restaurant.dto.RedeemCardDTO;
import com.restaurant.service.MemberService;
import com.restaurant.service.RechargeCardService;
import com.restaurant.vo.BalanceRecordVO;
import com.restaurant.vo.CardVO;
import com.restaurant.vo.MemberInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * H5 会员信息/兑换/余额记录接口
 */
@RestController
@RequestMapping("/api/h5/member")
@RequiredArgsConstructor
public class H5MemberController {

    private final MemberService memberService;
    private final RechargeCardService rechargeCardService;

    /**
     * 获取会员信息
     *
     * @param request HTTP 请求（含 memberId 属性）
     * @return 会员信息
     */
    @GetMapping("/info")
    public Result<MemberInfoVO> getMemberInfo(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        return Result.success(memberService.getMemberInfo(memberId));
    }

    /**
     * 兑换点卡
     *
     * @param dto     兑换请求
     * @param request HTTP 请求（含 memberId 属性）
     * @return 操作结果
     */
    @PostMapping("/redeem")
    public Result<Void> redeemCard(@Valid @RequestBody RedeemCardDTO dto,
                                   HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        memberService.redeemCard(memberId, dto);
        return Result.success();
    }

    /**
     * 获取余额变动记录
     *
     * @param request HTTP 请求（含 memberId 属性）
     * @param page    页码
     * @param size    每页大小
     * @return 分页余额记录
     */
    @GetMapping("/balance/records")
    public Result<PageResult<BalanceRecordVO>> getBalanceRecords(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long memberId = (Long) request.getAttribute("memberId");
        return Result.success(memberService.getBalanceRecords(memberId, page, size));
    }

    /**
     * 获取已发放给当前会员的点卡列表
     *
     * @param request HTTP 请求（含 memberId 属性）
     * @return 点卡列表
     */
    @GetMapping("/cards")
    public Result<List<CardVO>> getMyCards(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        return Result.success(rechargeCardService.getAssignedCardsByMember(memberId));
    }
}
