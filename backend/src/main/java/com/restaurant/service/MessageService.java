package com.restaurant.service;

import com.restaurant.common.PageResult;
import com.restaurant.dto.MessageSendDTO;
import com.restaurant.vo.MessageVO;
import java.util.List;

/**
 * 消息业务服务接口
 */
public interface MessageService {

    /**
     * 发送消息（落库 + 展开接收人 + 实时推送在线用户）。
     *
     * @param dto     发送请求
     * @param senderId 发送者ID（admin_user.id）
     * @return 消息视图
     */
    MessageVO send(MessageSendDTO dto, Long senderId);

    /**
     * 后台发送记录列表（分页 + 筛选）。
     *
     * @param page    页码（从1开始）
     * @param size    每页大小
     * @param type    类型筛选（可空）
     * @param scope   范围筛选（可空）
     * @param status  状态筛选（可空）
     * @param keyword 标题关键词（可空）
     * @return 分页消息视图
     */
    PageResult<MessageVO> adminList(int page, int size, String type, String scope, String status, String keyword);

    /**
     * 后台消息详情。
     *
     * @param id 消息ID
     * @return 消息视图
     */
    MessageVO adminDetail(Long id);

    /**
     * 撤回消息（强校验5分钟窗口，并推送撤回事件）。
     *
     * @param id      消息ID
     * @param adminId 操作管理员ID
     */
    void revoke(Long id, Long adminId);

    /**
     * H5 用户收件箱（分页，合并广播与指定，含已读标记）。
     *
     * @param receiverId   接收人ID（member.id 或 temp_user.id）
     * @param receiverType 接收人类型 MEMBER/TEMP
     * @param page         页码
     * @param size         每页大小
     * @return 分页消息视图
     */
    PageResult<MessageVO> listForUser(Long receiverId, String receiverType, int page, int size);

    /**
     * H5 消息详情（含当前用户已读态）。
     *
     * @param id           消息ID
     * @param receiverId   接收人ID
     * @param receiverType 接收人类型
     * @return 消息视图
     */
    MessageVO detailForUser(Long id, Long receiverId, String receiverType);

    /**
     * 标记单条已读（惰性写回）。
     *
     * @param id           消息ID
     * @param receiverId   接收人ID
     * @param receiverType 接收人类型
     */
    void markRead(Long id, Long receiverId, String receiverType);

    /**
     * 批量标记已读（进入消息中心时调用）。
     *
     * @param ids          消息ID列表
     * @param receiverId   接收人ID
     * @param receiverType 接收人类型
     */
    void markReadBatch(List<Long> ids, Long receiverId, String receiverType);

    /**
     * 当前用户未读总数（指定未读 + 广播未读差量）。
     *
     * @param receiverId   接收人ID
     * @param receiverType 接收人类型
     * @return 未读数
     */
    long unreadCount(Long receiverId, String receiverType);
}
