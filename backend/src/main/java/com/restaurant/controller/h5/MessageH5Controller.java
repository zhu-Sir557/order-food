package com.restaurant.controller.h5;

import com.restaurant.common.BizException;
import com.restaurant.common.MessageConstants;
import com.restaurant.common.PageResult;
import com.restaurant.common.Result;
import com.restaurant.common.ResultCode;
import com.restaurant.service.MessageService;
import com.restaurant.vo.MessageUnreadVO;
import com.restaurant.vo.MessageVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * H5 消息接口（消息中心列表 / 详情 / 已读 / 批量已读 / 未读计数）
 *
 * <p>路径 /api/h5/message/** 已在 {@code WebMvcConfig} 注册到 {@code CustomerAuthInterceptor}，
 * 同时覆盖会员（MEMBER）与游客（CUSTOMER）。</p>
 */
@RestController
@RequestMapping("/api/h5/message")
@RequiredArgsConstructor
public class MessageH5Controller {

    private final MessageService messageService;

    /**
     * 消息中心列表（合并广播与指定，含已读标记）
     *
     * @param request HTTP 请求（取 memberId / tempUserId）
     * @param page    页码
     * @param size    每页大小
     * @return 分页消息视图
     */
    @GetMapping("/list")
    public Result<PageResult<MessageVO>> list(HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Receiver receiver = resolveReceiver(request);
        return Result.success(messageService.listForUser(receiver.id(), receiver.type(), page, size));
    }

    /**
     * 消息详情（含当前用户已读态）
     *
     * @param id      消息ID
     * @param request HTTP 请求
     * @return 消息视图
     */
    @GetMapping("/{id}")
    public Result<MessageVO> detail(@PathVariable Long id, HttpServletRequest request) {
        Receiver receiver = resolveReceiver(request);
        return Result.success(messageService.detailForUser(id, receiver.id(), receiver.type()));
    }

    /**
     * 标记单条已读
     *
     * @param id      消息ID
     * @param request HTTP 请求
     * @return 无数据
     */
    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id, HttpServletRequest request) {
        Receiver receiver = resolveReceiver(request);
        messageService.markRead(id, receiver.id(), receiver.type());
        return Result.success();
    }

    /**
     * 批量标记已读（进入消息中心时调用）
     *
     * @param ids     消息ID列表
     * @param request HTTP 请求
     * @return 无数据
     */
    @PutMapping("/read-batch")
    public Result<Void> readBatch(@RequestBody List<Long> ids, HttpServletRequest request) {
        Receiver receiver = resolveReceiver(request);
        messageService.markReadBatch(ids, receiver.id(), receiver.type());
        return Result.success();
    }

    /**
     * 未读计数（红点）
     *
     * @param request HTTP 请求
     * @return 未读视图
     */
    @GetMapping("/unread-count")
    public Result<MessageUnreadVO> unreadCount(HttpServletRequest request) {
        Receiver receiver = resolveReceiver(request);
        return Result.success(new MessageUnreadVO(messageService.unreadCount(receiver.id(), receiver.type())));
    }

    /**
     * 从请求 attribute 解析当前 H5 用户身份（会员优先，游客兜底）。
     *
     * @param request HTTP 请求
     * @return 接收人身份
     */
    private Receiver resolveReceiver(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        Long tempUserId = (Long) request.getAttribute("tempUserId");
        if (memberId != null) {
            return new Receiver(memberId, MessageConstants.ReceiverType.MEMBER);
        }
        if (tempUserId != null) {
            return new Receiver(tempUserId, MessageConstants.ReceiverType.TEMP);
        }
        throw new BizException(ResultCode.UNAUTHORIZED);
    }

    /** 当前接收人身份封装 */
    private record Receiver(Long id, String type) {
    }
}
