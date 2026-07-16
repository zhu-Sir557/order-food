package com.restaurant.realtime;

import com.restaurant.common.MessageConstants;
import com.restaurant.vo.MessagePushVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket 实时推送服务
 *
 * <p>封装 {@link SimpMessagingTemplate}：
 * <ul>
 *   <li>指定用户 → convertAndSendToUser（按 Principal name 路由个人队列）；</li>
 *   <li>全员广播 → convertAndSend 到广播主题；</li>
 *   <li>撤回事件复用同一通道，仅 status=REVOKED。</li>
 * </ul>
 * 当前单实例直接应用内推送；多实例可改为 Redis pub/sub 转发（P2）。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePushService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 推送给指定用户（个人队列 /user/queue/messages）。
     *
     * @param principalName 主体名称（M:/C: 前缀）
     * @param vo            推送内容
     */
    public void pushToUser(String principalName, MessagePushVO vo) {
        messagingTemplate.convertAndSendToUser(principalName, "/queue/messages", vo);
    }

    /**
     * 广播到全员主题。
     *
     * @param vo 推送内容
     */
    public void pushBroadcast(MessagePushVO vo) {
        messagingTemplate.convertAndSend(MessageConstants.WS_BROADCAST_TOPIC, vo);
    }

    /**
     * 推送撤回事件（广播语义；指定用户撤回由调用方循环 pushToUser 完成）。
     *
     * @param messageId 消息ID
     * @param vo        撤回后的推送内容（status=REVOKED）
     */
    public void pushRevoke(Long messageId, MessagePushVO vo) {
        messagingTemplate.convertAndSend(MessageConstants.WS_BROADCAST_TOPIC, vo);
    }
}
