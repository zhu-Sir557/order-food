package com.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.BizException;
import com.restaurant.common.MessageConstants;
import com.restaurant.common.PageResult;
import com.restaurant.common.ResultCode;
import com.restaurant.dto.MessageSendDTO;
import com.restaurant.entity.Message;
import com.restaurant.entity.MessageReceiver;
import com.restaurant.mapper.MessageMapper;
import com.restaurant.mapper.MessageReceiverMapper;
import com.restaurant.realtime.MessagePushService;
import com.restaurant.service.MessageService;
import com.restaurant.vo.MessagePushVO;
import com.restaurant.vo.MessageVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 消息业务服务实现
 *
 * <p>负责：落库、指定用户展开接收人 / 广播不展开、实时推送在线用户、
 * 后台列表/详情/撤回、H5 收件箱/详情/已读回写/未读统计。</p>
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MessageMapper messageMapper;
    private final MessageReceiverMapper receiverMapper;
    private final MessagePushService pushService;

    public MessageServiceImpl(MessageMapper messageMapper, MessageReceiverMapper receiverMapper,
            MessagePushService pushService) {
        this.messageMapper = messageMapper;
        this.receiverMapper = receiverMapper;
        this.pushService = pushService;
    }

    @Override
    public MessageVO send(MessageSendDTO dto, Long senderId) {
        String type = dto.getType();
        if (!MessageConstants.MessageType.BROADCAST.equals(type)
                && !MessageConstants.MessageType.SPECIFIED.equals(type)) {
            throw new BizException(ResultCode.PARAM_ERROR, "消息类型非法，仅支持 BROADCAST / SPECIFIED");
        }

        // 指定用户必须至少选择一个接收人
        if (MessageConstants.MessageType.SPECIFIED.equals(type)
                && (dto.getReceiverIds() == null || dto.getReceiverIds().isEmpty())) {
            throw new BizException(ResultCode.MESSAGE_RECEIVER_EMPTY);
        }

        LocalDateTime now = LocalDateTime.now();
        Message message = new Message();
        message.setType(type);
        message.setReceiverScope(MessageConstants.MessageType.BROADCAST.equals(type)
                ? MessageConstants.ReceiverScope.ALL : MessageConstants.ReceiverScope.SPECIFIED);
        message.setSenderId(senderId);
        message.setTitle(dto.getTitle());
        message.setContent(dto.getContent());
        message.setImageUrl(dto.getImageUrl());
        message.setLinkUrl(dto.getLinkUrl());
        message.setStatus(MessageConstants.MessageStatus.SENT);
        message.setRevocableBefore(now.plusMinutes(MessageConstants.REVOKE_WINDOW_MINUTES));
        messageMapper.insert(message);

        MessagePushVO pushVO = toPushVO(message);

        if (MessageConstants.MessageType.SPECIFIED.equals(type)) {
            // 展开接收人（仅会员），每行 is_read=0
            for (Long receiverId : dto.getReceiverIds()) {
                MessageReceiver receiver = new MessageReceiver();
                receiver.setMessageId(message.getId());
                receiver.setReceiverId(receiverId);
                receiver.setReceiverType(MessageConstants.ReceiverType.MEMBER);
                receiver.setIsRead(0);
                receiverMapper.insert(receiver);
                // 推送在线指定用户（仅在线会话收到，离线由 REST 列表补齐）
                pushService.pushToUser(MessageConstants.PRINCIPAL_PREFIX_MEMBER + receiverId, pushVO);
            }
        } else {
            // 广播不展开，直接推全体在线连接
            pushService.pushBroadcast(pushVO);
        }

        log.info("消息发送成功: id={}, type={}, scope={}", message.getId(), type, message.getReceiverScope());
        return toVO(message, true, null);
    }

    @Override
    public PageResult<MessageVO> adminList(int page, int size, String type, String scope,
            String status, String keyword) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(type)) {
            wrapper.eq(Message::getType, type);
        }
        if (StringUtils.hasText(scope)) {
            wrapper.eq(Message::getReceiverScope, scope);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Message::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Message::getTitle, keyword);
        }
        wrapper.orderByDesc(Message::getCreateTime);

        Page<Message> pageObj = new Page<>(page, size);
        Page<Message> result = messageMapper.selectPage(pageObj, wrapper);

        LocalDateTime now = LocalDateTime.now();
        List<MessageVO> voList = result.getRecords().stream()
                .map(m -> toVO(m, isRevocable(m, now), null))
                .toList();
        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public MessageVO adminDetail(Long id) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BizException(ResultCode.MESSAGE_NOT_FOUND);
        }
        return toVO(message, isRevocable(message, LocalDateTime.now()), null);
    }

    @Override
    public void revoke(Long id, Long adminId) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BizException(ResultCode.MESSAGE_NOT_FOUND);
        }
        if (!MessageConstants.MessageStatus.SENT.equals(message.getStatus())) {
            throw new BizException(ResultCode.MESSAGE_REVOKE_NOT_ALLOWED);
        }
        if (message.getRevocableBefore() == null
                || LocalDateTime.now().isAfter(message.getRevocableBefore())) {
            throw new BizException(ResultCode.MESSAGE_REVOKE_NOT_ALLOWED);
        }

        message.setStatus(MessageConstants.MessageStatus.REVOKED);
        messageMapper.updateById(message);

        // 推送撤回事件（复用推送通道，仅 status 置 REVOKED）
        MessagePushVO revokeVO = toPushVO(message);
        if (MessageConstants.ReceiverScope.ALL.equals(message.getReceiverScope())) {
            pushService.pushBroadcast(revokeVO);
        } else {
            List<MessageReceiver> receivers = receiverMapper.selectList(
                    new LambdaQueryWrapper<MessageReceiver>().eq(MessageReceiver::getMessageId, id));
            for (MessageReceiver r : receivers) {
                pushService.pushToUser(MessageConstants.PRINCIPAL_PREFIX_MEMBER + r.getReceiverId(), revokeVO);
            }
        }
        log.info("消息撤回成功: id={}, operator={}", id, adminId);
    }

    @Override
    public PageResult<MessageVO> listForUser(Long receiverId, String receiverType, int page, int size) {
        long offset = (long) (page - 1) * size;
        LocalDateTime since = MessageConstants.broadcastSince(LocalDateTime.now());
        List<MessageVO> records = messageMapper.selectUserMessages(offset, size, receiverId, receiverType, since);
        long total = messageMapper.countUserMessages(receiverId, receiverType, since);
        return new PageResult<>(records, total, page, size);
    }

    @Override
    public MessageVO detailForUser(Long id, Long receiverId, String receiverType) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BizException(ResultCode.MESSAGE_NOT_FOUND);
        }
        boolean isRead;
        if (MessageConstants.ReceiverScope.ALL.equals(message.getReceiverScope())) {
            isRead = receiverMapper.existsRead(id, receiverId, receiverType);
        } else {
            MessageReceiver receiver = receiverMapper.selectOne(new LambdaQueryWrapper<MessageReceiver>()
                    .eq(MessageReceiver::getMessageId, id)
                    .eq(MessageReceiver::getReceiverId, receiverId)
                    .eq(MessageReceiver::getReceiverType, receiverType));
            isRead = receiver != null && receiver.getIsRead() != null && receiver.getIsRead() == 1;
        }
        return toVO(message, false, isRead);
    }

    @Override
    public void markRead(Long id, Long receiverId, String receiverType) {
        receiverMapper.upsertRead(id, receiverId, receiverType, LocalDateTime.now());
    }

    @Override
    public void markReadBatch(List<Long> ids, Long receiverId, String receiverType) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long id : ids) {
            receiverMapper.upsertRead(id, receiverId, receiverType, now);
        }
    }

    @Override
    public long unreadCount(Long receiverId, String receiverType) {
        long specifiedUnread = receiverMapper.countSpecifiedUnread(receiverId, receiverType);

        LocalDateTime since = MessageConstants.broadcastSince(LocalDateTime.now());
        long broadcastTotal = receiverMapper.countBroadcastTotal(since);
        long broadcastUnread = 0L;
        if (broadcastTotal > 0) {
            List<Long> broadcastIds = receiverMapper.selectBroadcastIds(since);
            long broadcastRead = broadcastIds.isEmpty() ? 0L
                    : receiverMapper.countBroadcastRead(broadcastIds, receiverId, receiverType);
            broadcastUnread = broadcastTotal - broadcastRead;
            if (broadcastUnread < 0) {
                broadcastUnread = 0L;
            }
        }
        return specifiedUnread + broadcastUnread;
    }

    // ===================== 私有辅助 =====================

    private boolean isRevocable(Message message, LocalDateTime now) {
        return MessageConstants.MessageStatus.SENT.equals(message.getStatus())
                && message.getRevocableBefore() != null
                && now.isBefore(message.getRevocableBefore());
    }

    private MessageVO toVO(Message message, Boolean revocable, Boolean isRead) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setType(message.getType());
        vo.setReceiverScope(message.getReceiverScope());
        vo.setSenderId(message.getSenderId());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setImageUrl(message.getImageUrl());
        vo.setLinkUrl(message.getLinkUrl());
        vo.setStatus(message.getStatus());
        vo.setRevocable(revocable);
        vo.setCreateTime(message.getCreateTime() == null ? null : message.getCreateTime().format(FMT));
        vo.setIsRead(isRead);
        return vo;
    }

    private MessagePushVO toPushVO(Message message) {
        MessagePushVO vo = new MessagePushVO();
        vo.setId(message.getId());
        vo.setType(message.getType());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setImageUrl(message.getImageUrl());
        vo.setLinkUrl(message.getLinkUrl());
        vo.setStatus(message.getStatus());
        vo.setCreateTime(message.getCreateTime() == null ? null : message.getCreateTime().format(FMT));
        return vo;
    }
}
