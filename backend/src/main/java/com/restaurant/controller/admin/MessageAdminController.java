package com.restaurant.controller.admin;

import com.restaurant.common.PageResult;
import com.restaurant.common.Result;
import com.restaurant.dto.MessageSendDTO;
import com.restaurant.service.MessageService;
import com.restaurant.vo.MessageVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台消息管理接口（发送 / 发送记录 / 详情 / 撤回）
 *
 * <p>路径 /api/admin/message/** 已被 {@code AdminAuthInterceptor} 覆盖（/api/admin/**），
 * 无需额外注册鉴权拦截器。</p>
 */
@RestController
@RequestMapping("/api/admin/message")
@RequiredArgsConstructor
public class MessageAdminController {

    private final MessageService messageService;

    /**
     * 发送消息（全员广播 / 指定用户）
     *
     * @param dto     发送请求
     * @param request HTTP 请求（取 adminId）
     * @return 消息视图
     */
    @PostMapping("/send")
    public Result<MessageVO> send(@Valid @RequestBody MessageSendDTO dto, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        return Result.success(messageService.send(dto, adminId));
    }

    /**
     * 发送记录列表（分页 + 筛选）
     *
     * @param page    页码
     * @param size    每页大小
     * @param type    类型筛选
     * @param scope   范围筛选
     * @param status  状态筛选
     * @param keyword 标题关键词
     * @return 分页消息视图
     */
    @GetMapping("/list")
    public Result<PageResult<MessageVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return Result.success(messageService.adminList(page, size, type, scope, status, keyword));
    }

    /**
     * 消息详情
     *
     * @param id 消息ID
     * @return 消息视图
     */
    @GetMapping("/{id}")
    public Result<MessageVO> detail(@PathVariable Long id) {
        return Result.success(messageService.adminDetail(id));
    }

    /**
     * 撤回消息（5 分钟窗口强校验）
     *
     * @param id      消息ID
     * @param request HTTP 请求（取 adminId）
     * @return 无数据
     */
    @PostMapping("/{id}/revoke")
    public Result<Void> revoke(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        messageService.revoke(id, adminId);
        return Result.success();
    }
}
