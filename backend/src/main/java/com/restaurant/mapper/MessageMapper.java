package com.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.entity.Message;
import com.restaurant.vo.MessageVO;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 消息主表 Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 查询当前用户的消息收件箱（手动分页）。
     *
     * <p>合并两类消息并按创建时间倒序：
     * <ul>
     *   <li>全员广播（BROADCAST，窗口内，不展开接收人）：左连接 message_receiver 计算 isRead；</li>
     *   <li>指定用户（SPECIFIED，命中 receiver 展开行）：内连接 message_receiver 取 isRead。</li>
     * </ul>
     * 列名以驼峰别名对齐 {@link MessageVO} 属性；createTime 统一 DATE_FORMAT 为字符串，避免依赖下划线自动映射。
     * 采用手动 LIMIT 分页（而非 MyBatis-Plus IPage 自动 COUNT），规避 UNION 查询自动计数不稳的问题。</p>
     *
     * @param offset         偏移量 (page-1)*size
     * @param size           每页大小
     * @param receiverId     接收人ID（member.id 或 temp_user.id）
     * @param receiverType   接收人类型 MEMBER/TEMP
     * @param broadcastSince 广播保留窗口起点
     * @return 消息视图列表
     */
    @Select("""
            (SELECT m.id AS id, m.type AS type, m.receiver_scope AS receiverScope, m.sender_id AS senderId,
                    m.title AS title, m.content AS content, m.image_url AS imageUrl, m.link_url AS linkUrl,
                    m.status AS status, DATE_FORMAT(m.create_time, '%Y-%m-%d %H:%i:%s') AS createTime,
                    (COALESCE(r.is_read, 0) <> 0) AS isRead
               FROM message m
               LEFT JOIN message_receiver r
                 ON r.message_id = m.id AND r.receiver_id = #{receiverId}
                AND r.receiver_type = #{receiverType} AND r.deleted = 0
               WHERE m.deleted = 0 AND m.type = 'BROADCAST' AND m.status = 'SENT'
                 AND m.create_time >= #{broadcastSince})
            UNION ALL
            (SELECT m.id, m.type, m.receiver_scope, m.sender_id, m.title, m.content,
                    m.image_url, m.link_url, m.status, DATE_FORMAT(m.create_time, '%Y-%m-%d %H:%i:%s'),
                    (COALESCE(r.is_read, 0) <> 0)
               FROM message m
               JOIN message_receiver r
                 ON r.message_id = m.id AND r.receiver_id = #{receiverId}
                AND r.receiver_type = #{receiverType} AND r.deleted = 0
               WHERE m.deleted = 0 AND m.type = 'SPECIFIED' AND m.status = 'SENT')
            ORDER BY createTime DESC
            LIMIT #{offset}, #{size}
            """)
    List<MessageVO> selectUserMessages(@Param("offset") long offset,
            @Param("size") long size,
            @Param("receiverId") Long receiverId,
            @Param("receiverType") String receiverType,
            @Param("broadcastSince") LocalDateTime broadcastSince);

    /**
     * 统计当前用户收件箱消息总数（与 {@link #selectUserMessages} 同口径）。
     *
     * @param receiverId     接收人ID
     * @param receiverType   接收人类型
     * @param broadcastSince 广播保留窗口起点
     * @return 消息总数
     */
    @Select("""
            SELECT COUNT(*) FROM (
              (SELECT m.id
                 FROM message m
                 LEFT JOIN message_receiver r
                   ON r.message_id = m.id AND r.receiver_id = #{receiverId}
                    AND r.receiver_type = #{receiverType} AND r.deleted = 0
                 WHERE m.deleted = 0 AND m.type = 'BROADCAST' AND m.status = 'SENT'
                   AND m.create_time >= #{broadcastSince})
              UNION ALL
              (SELECT m.id
                 FROM message m
                 JOIN message_receiver r
                   ON r.message_id = m.id AND r.receiver_id = #{receiverId}
                    AND r.receiver_type = #{receiverType} AND r.deleted = 0
                 WHERE m.deleted = 0 AND m.type = 'SPECIFIED' AND m.status = 'SENT')
            ) t
            """)
    long countUserMessages(@Param("receiverId") Long receiverId,
            @Param("receiverType") String receiverType,
            @Param("broadcastSince") LocalDateTime broadcastSince);
}
