-- ============================================================
-- 用户注册登录 + 余额点卡支付 数据库迁移脚本
-- 新增3张表：member, recharge_card, balance_record
-- 修改1张表：order_info（新增 member_id, pay_method 字段）
-- ============================================================

-- 1. 注册会员表
CREATE TABLE IF NOT EXISTS `member` (
    `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`     VARCHAR(50)    NOT NULL COMMENT '账户名（唯一）',
    `password`     VARCHAR(100)   NOT NULL COMMENT '密码（BCrypt加密）',
    `balance`      DECIMAL(10, 2) DEFAULT 0.00 COMMENT '账户余额',
    `temp_user_id` BIGINT         DEFAULT NULL COMMENT '关联的临时用户ID',
    `create_time`  DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT        DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='注册会员表';

-- 2. 充值点卡表
CREATE TABLE IF NOT EXISTS `recharge_card` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `card_no`       VARCHAR(32)    NOT NULL COMMENT '卡号（RC+yyyyMMdd+6位随机数）',
    `card_password` VARCHAR(16)    NOT NULL COMMENT '卡密（16位随机字母数字）',
    `amount`        DECIMAL(10, 2) NOT NULL COMMENT '额度',
    `status`        TINYINT        DEFAULT 0 COMMENT '状态：0未使用，1已发放，2已使用',
    `member_id`     BIGINT         DEFAULT NULL COMMENT '发放给的会员ID',
    `assigned_at`   DATETIME       DEFAULT NULL COMMENT '发放时间',
    `used_at`       DATETIME       DEFAULT NULL COMMENT '使用时间',
    `create_time`   DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT        DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_card_no` (`card_no`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值点卡表';

-- 3. 余额变动记录表
CREATE TABLE IF NOT EXISTS `balance_record` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_id`     BIGINT         NOT NULL COMMENT '会员ID',
    `type`          TINYINT        NOT NULL COMMENT '类型：1充值，2消费',
    `amount`        DECIMAL(10, 2) NOT NULL COMMENT '变动金额',
    `balance_after` DECIMAL(10, 2) NOT NULL COMMENT '变动后余额',
    `card_no`       VARCHAR(32)    DEFAULT NULL COMMENT '充值卡号（充值时）',
    `order_no`      VARCHAR(32)    DEFAULT NULL COMMENT '订单号（消费时）',
    `order_id`      BIGINT         DEFAULT NULL COMMENT '订单ID（消费时）',
    `remark`        VARCHAR(200)   DEFAULT NULL COMMENT '备注',
    `create_time`   DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`       TINYINT        DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='余额变动记录表';

-- 4. 修改 order_info 表：新增 member_id 和 pay_method 字段
ALTER TABLE `order_info`
    ADD COLUMN `member_id` BIGINT DEFAULT NULL COMMENT '会员ID' AFTER `temp_user_id`,
    ADD COLUMN `pay_method` TINYINT DEFAULT NULL COMMENT '支付方式：1微信，2支付宝，3余额' AFTER `status`;
