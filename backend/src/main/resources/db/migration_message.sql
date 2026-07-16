-- =============================================
-- 消息模块增量迁移（message + message_receiver）
-- 部署阶段在线上 MySQL 手动执行；禁止 DROP TABLE。
-- 与 schema.sql 保持同一风格：InnoDB / utf8mb4 / 逻辑删除 deleted。
-- =============================================

CREATE TABLE IF NOT EXISTS `message` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type`            VARCHAR(20)  NOT NULL COMMENT '消息类型：BROADCAST/SPECIFIED/SYSTEM',
    `receiver_scope`  VARCHAR(20)  NOT NULL DEFAULT 'SPECIFIED' COMMENT '接收范围：ALL/SPECIFIED',
    `sender_id`       BIGINT       DEFAULT NULL COMMENT '发送者ID（admin_user.id）',
    `title`           VARCHAR(50)  NOT NULL COMMENT '标题',
    `content`         TEXT         DEFAULT NULL COMMENT '正文',
    `image_url`       VARCHAR(512) DEFAULT NULL COMMENT '图片OSS地址（复用 /api/admin/upload）',
    `link_url`        VARCHAR(512) DEFAULT NULL COMMENT '跳转链接',
    `status`          VARCHAR(20)  NOT NULL DEFAULT 'SENT' COMMENT '状态：SENT/REVOKED',
    `revocable_before` DATETIME    DEFAULT NULL COMMENT '撤回截止时间（发送后5分钟）',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_sender_id` (`sender_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息主表';

CREATE TABLE IF NOT EXISTS `message_receiver` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `message_id`   BIGINT       NOT NULL COMMENT '消息ID',
    `receiver_id`  BIGINT       NOT NULL COMMENT '接收人ID（member.id 或 temp_user.id）',
    `receiver_type` VARCHAR(20) NOT NULL COMMENT '接收人类型：MEMBER/TEMP',
    `is_read`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读：0未读，1已读',
    `read_time`    DATETIME     DEFAULT NULL COMMENT '阅读时间',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`      TINYINT      DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_message_id` (`message_id`),
    KEY `idx_receiver` (`receiver_id`, `receiver_type`),
    UNIQUE KEY `uk_msg_receiver` (`message_id`, `receiver_id`, `receiver_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息接收人/已读维度表';
