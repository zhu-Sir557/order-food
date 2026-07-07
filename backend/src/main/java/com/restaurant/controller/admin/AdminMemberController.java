package com.restaurant.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.PageResult;
import com.restaurant.common.Result;
import com.restaurant.entity.Member;
import com.restaurant.mapper.MemberMapper;
import com.restaurant.service.BalanceRecordService;
import com.restaurant.vo.AdminMemberVO;
import com.restaurant.vo.BalanceRecordVO;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台会员管理接口
 */
@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberMapper memberMapper;
    private final BalanceRecordService balanceRecordService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 会员列表（分页 + 关键词搜索）
     *
     * @param page    页码
     * @param size    每页大小
     * @param keyword 搜索关键词（用户名）
     * @return 分页会员列表
     */
    @GetMapping
    public Result<PageResult<AdminMemberVO>> getMemberList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Member::getUsername, keyword);
        }
        wrapper.orderByDesc(Member::getCreateTime);

        Page<Member> pageObj = new Page<>(page, size);
        Page<Member> result = memberMapper.selectPage(pageObj, wrapper);

        List<AdminMemberVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return Result.success(new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * 查看会员余额变动记录
     *
     * @param id   会员ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页余额记录
     */
    @GetMapping("/{id}/balance/records")
    public Result<PageResult<BalanceRecordVO>> getMemberBalanceRecords(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(balanceRecordService.getRecordsByMember(id, page, size));
    }

    /**
     * 实体转 VO
     *
     * @param member 会员实体
     * @return VO
     */
    private AdminMemberVO toVO(Member member) {
        AdminMemberVO vo = new AdminMemberVO();
        vo.setId(member.getId());
        vo.setUsername(member.getUsername());
        vo.setBalance(member.getBalance());
        if (member.getCreateTime() != null) {
            vo.setCreateTime(member.getCreateTime().format(FMT));
        }
        return vo;
    }
}
