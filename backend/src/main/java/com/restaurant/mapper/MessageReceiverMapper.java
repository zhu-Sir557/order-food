package com.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.entity.MessageReceiver;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 消息接收人 / 已读维度 Mapper
 */
@Mapper
public interface MessageReceiverMapper extends BaseMapper<MessageReceiver> {

    /**
     * 惰性写回已读标记（UPSERT）。
     *
     * <p>基于唯一键 (message_id, receiver_id, receiver_type)：不存在则插入 is_read=1，
     * 存在则更新 is_read=1 与 read_time。广播已读与指定已读共用此方法。</p>
     *
     * @param messageId    消息ID
     * @param receiverId   接收人ID
     * @param receiverType 接收人类型 MEMBER/TEMP
     * @param now          当前时间（用作 read_time 与 create_time）
     */
    @Select("""
            INSERT INTO message_receiver (message_id, receiver_id, receiver_type, is_read, read_time, create_time)
            VALUES (#{messageId}, #{receiverId}, #{receiverType}, 1, #{now}, #{now})
            ON DUPLICATE KEY UPDATE is_read = 1, read_time = #{now}
            """)
    void upsertRead(@Param("messageId") Long messageId,
            @Param("receiverId") Long receiverId,
            @Param("receiverType") String receiverType,
            @Param("now") LocalDateTime now);

    /**
     * 统计指定用户未读消息数（SENT 状态、is_read=0）。
     *
     * @param receiverId   接收人ID
     * @param receiverType 接收人类型
     * @return 未读数
     */
    @Select("""
            SELECT COUNT(*) FROM message_receiver r
            JOIN message m ON m.id = r.message_id
            WHERE r.receiver_id = #{receiverId} AND r.receiver_type = #{receiverType}
              AND r.is_read = 0 AND r.deleted = 0
              AND m.status = 'SENT' AND m.deleted = 0
            """)
    long countSpecifiedUnread(@Param("receiverId") Long receiverId,
            @Param("receiverType") String receiverType);

    /**
     * 统计窗口内广播消息总数（SENT）。
     *
     * @param broadcastSince 广播保留窗口起点
     * @return 广播总数
     */
    @Select("""
            SELECT COUNT(*) FROM message
            WHERE type = 'BROADCAST' AND status = 'SENT' AND deleted = 0
              AND create_time >= #{broadcastSince}
            """)
    long countBroadcastTotal(@Param("broadcastSince") LocalDateTime broadcastSince);

    /**
     * 统计窗口内广播消息ID列表（供已读数差量计算）。
     *
     * @param broadcastSince 广播保留窗口起点
     * @return 广播消息ID列表
     */
    @Select("""
            SELECT id FROM message
            WHERE type = 'BROADCAST' AND status = 'SENT' AND deleted = 0
              AND create_time >= #{broadcastSince}
            """)
    List<Long> selectBroadcastIds(@Param("broadcastSince") LocalDateTime broadcastSince);

    /**
     * 统计某用户对指定广播消息已读的数量（is_read=1）。
     *
     * @param broadcastIds 广播消息ID列表（非空）
     * @param receiverId   接收人ID
     * @param receiverType 接收人类型
     * @return 已读广播数
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM message_receiver
            WHERE message_id IN
              <foreach collection='broadcastIds' item='id' open='(' separator=',' close=')'>
                #{id}
              </foreach>
              AND receiver_id = #{receiverId} AND receiver_type = #{receiverType}
              AND is_read = 1 AND deleted = 0
            </script>
            """)
    long countBroadcastRead(@Param("broadcastIds") List<Long> broadcastIds,
            @Param("receiverId") Long receiverId,
            @Param("receiverType") String receiverType);

    /**
     * 判断某用户对某条消息是否已读（is_read=1）。
     *
     * @param messageId    消息ID
     * @param receiverId   接收人ID
     * @param receiverType 接收人类型
     * @return 是否已读
     */
    @Select("""
            SELECT COUNT(*) > 0 FROM message_receiver
            WHERE message_id = #{messageId} AND receiver_id = #{receiverId}
              AND receiver_type = #{receiverType} AND is_read = 1 AND deleted = 0
            """)
    boolean existsRead(@Param("messageId") Long messageId,
            @Param("receiverId") Long receiverId,
            @Param("receiverType") String receiverType);
}
