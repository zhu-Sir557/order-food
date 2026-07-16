package com.restaurant.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.common.BizException;
import com.restaurant.common.ResultCode;
import com.restaurant.dto.MessageSendDTO;
import com.restaurant.entity.Message;
import com.restaurant.entity.MessageReceiver;
import com.restaurant.mapper.MessageMapper;
import com.restaurant.mapper.MessageReceiverMapper;
import com.restaurant.realtime.MessagePushService;
import com.restaurant.vo.MessagePushVO;
import com.restaurant.vo.MessageVO;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * MessageServiceImpl 业务编排单元测试（Mockito 隔离全部依赖）。
 *
 * <p>覆盖：广播不展开、指定展开多行、接收人空校验、未读统计差量、撤回窗口强校验。</p>
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock MessageMapper messageMapper;
    @Mock MessageReceiverMapper receiverMapper;
    @Mock MessagePushService pushService;

    @Captor ArgumentCaptor<Message> messageCaptor;
    @Captor ArgumentCaptor<String> principalCaptor;
    @Captor ArgumentCaptor<MessagePushVO> pushVoCaptor;

    @InjectMocks MessageServiceImpl service;

    @BeforeEach
    void setUp() {
        // 模拟 MyBatis 自增回填 id
        when(messageMapper.insert(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            m.setId(1L);
            return 1;
        });
    }

    private MessageSendDTO broadcastDto() {
        MessageSendDTO dto = new MessageSendDTO();
        dto.setType("BROADCAST");
        dto.setTitle("全员通知");
        dto.setContent("内容");
        return dto;
    }

    private MessageSendDTO specifiedDto(List<Long> ids) {
        MessageSendDTO dto = new MessageSendDTO();
        dto.setType("SPECIFIED");
        dto.setTitle("定向");
        dto.setContent("内容");
        dto.setReceiverIds(ids);
        return dto;
    }

    @Test
    @DisplayName("send 广播：不展开 receiver，仅 insert message 并广播推送")
    void send_broadcast_notExpand() {
        MessageVO vo = service.send(broadcastDto(), 1L);
        verify(messageMapper).insert(messageCaptor.capture());
        Message saved = messageCaptor.getValue();
        assertEquals("BROADCAST", saved.getType());
        assertEquals("ALL", saved.getReceiverScope());
        assertEquals("SENT", saved.getStatus());
        assertEquals(Boolean.TRUE, vo.getRevocable());
        verify(receiverMapper, never()).insert(any(MessageReceiver.class));
        verify(pushService).pushBroadcast(any(MessagePushVO.class));
        verify(pushService, never()).pushToUser(anyString(), any());
    }

    @Test
    @DisplayName("send 指定用户：展开 N 行 receiver 并逐人推送，不广播")
    void send_specified_expand() {
        MessageVO vo = service.send(specifiedDto(List.of(11L, 22L)), 1L);
        assertEquals(1L, vo.getId());
        verify(receiverMapper, times(2)).insert(any(MessageReceiver.class));
        verify(pushService).pushToUser(principalCaptor.capture(), any(MessagePushVO.class));
        verify(pushService, never()).pushBroadcast(any());
        assertTrue(principalCaptor.getAllValues().contains("M:11"));
        assertTrue(principalCaptor.getAllValues().contains("M:22"));
    }

    @Test
    @DisplayName("send 指定用户但接收人为空 → MESSAGE_RECEIVER_EMPTY")
    void send_specified_emptyReceiver() {
        BizException ex = assertThrows(BizException.class, () -> service.send(specifiedDto(List.of()), 1L));
        assertEquals(ResultCode.MESSAGE_RECEIVER_EMPTY.getCode(), ex.getCode());
        verify(messageMapper, never()).insert(any(Message.class));
    }

    @Test
    @DisplayName("unreadCount：指定未读 + 广播未读差量 = 6")
    void unreadCount_diff() {
        when(receiverMapper.countSpecifiedUnread(10L, "MEMBER")).thenReturn(2L);
        when(receiverMapper.countBroadcastTotal(any())).thenReturn(5L);
        when(receiverMapper.selectBroadcastIds(any())).thenReturn(List.of(1L, 2L, 3L, 4L, 5L));
        when(receiverMapper.countBroadcastRead(any(), eq(10L), eq("MEMBER"))).thenReturn(1L);

        long count = service.unreadCount(10L, "MEMBER");
        assertEquals(6L, count); // 指定2 + 广播(5-1=4)
    }

    @Test
    @DisplayName("revoke 在 5 分钟窗口内 → 成功置 REVOKED 并广播撤回事件")
    void revoke_withinWindow() {
        Message m = new Message();
        m.setId(1L);
        m.setStatus("SENT");
        m.setReceiverScope("ALL");
        m.setRevocableBefore(LocalDateTime.now().plusMinutes(5));
        m.setCreateTime(LocalDateTime.now());
        when(messageMapper.selectById(1L)).thenReturn(m);

        service.revoke(1L, 1L);

        assertEquals("REVOKED", m.getStatus());
        verify(messageMapper).updateById(m);
        verify(pushService).pushBroadcast(pushVoCaptor.capture());
        assertEquals("REVOKED", pushVoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("revoke 超过 5 分钟窗口 → MESSAGE_REVOKE_NOT_ALLOWED")
    void revoke_expired() {
        Message m = new Message();
        m.setId(1L);
        m.setStatus("SENT");
        m.setRevocableBefore(LocalDateTime.now().minusMinutes(1));
        when(messageMapper.selectById(1L)).thenReturn(m);

        BizException ex = assertThrows(BizException.class, () -> service.revoke(1L, 1L));
        assertEquals(ResultCode.MESSAGE_REVOKE_NOT_ALLOWED.getCode(), ex.getCode());
        verify(messageMapper, never()).updateById(any(Message.class));
    }

    @Test
    @DisplayName("revoke 消息不存在 → MESSAGE_NOT_FOUND")
    void revoke_notFound() {
        when(messageMapper.selectById(99L)).thenReturn(null);
        BizException ex = assertThrows(BizException.class, () -> service.revoke(99L, 1L));
        assertEquals(ResultCode.MESSAGE_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("revoke 已撤回消息 → MESSAGE_REVOKE_NOT_ALLOWED")
    void revoke_alreadyRevoked() {
        Message m = new Message();
        m.setId(1L);
        m.setStatus("REVOKED");
        when(messageMapper.selectById(1L)).thenReturn(m);
        BizException ex = assertThrows(BizException.class, () -> service.revoke(1L, 1L));
        assertEquals(ResultCode.MESSAGE_REVOKE_NOT_ALLOWED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("detailForUser 广播消息：已读标记存在 → isRead=true")
    void detailForUser_broadcast_read() {
        Message m = new Message();
        m.setId(5L);
        m.setReceiverScope("ALL");
        m.setStatus("SENT");
        m.setTitle("x");
        m.setCreateTime(LocalDateTime.now());
        when(messageMapper.selectById(5L)).thenReturn(m);
        when(receiverMapper.existsRead(5L, 10L, "MEMBER")).thenReturn(true);

        MessageVO vo = service.detailForUser(5L, 10L, "MEMBER");
        assertEquals(Boolean.TRUE, vo.getIsRead());
    }
}
