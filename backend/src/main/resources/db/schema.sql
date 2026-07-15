-- ============================================
-- Smart Restaurant Order Food System - Database Schema
-- MySQL 8.0, InnoDB, UTF-8MB4
-- ============================================

CREATE DATABASE IF NOT EXISTS `order_food`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `order_food`;

-- --------------------------------------------
-- Table: admin_user (管理员用户)
-- --------------------------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    `name`        VARCHAR(50)  DEFAULT NULL COMMENT '姓名',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员用户表';

-- --------------------------------------------
-- Table: category (菜品分类)
-- --------------------------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    `sort`        INT          DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态：0禁用，1启用',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';

-- --------------------------------------------
-- Table: dish (菜品)
-- --------------------------------------------
DROP TABLE IF EXISTS `dish`;
CREATE TABLE `dish` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_id` BIGINT         NOT NULL COMMENT '分类ID',
    `name`        VARCHAR(100)   NOT NULL COMMENT '菜品名称',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `image`       VARCHAR(255)   DEFAULT NULL COMMENT '图片URL',
    `description` VARCHAR(500)   DEFAULT NULL COMMENT '描述',
    `taste_config` TEXT          DEFAULT NULL COMMENT '口味配置JSON',
    `stock`       INT            DEFAULT 0 COMMENT '库存',
    `status`      TINYINT        DEFAULT 1 COMMENT '状态：0下架，1上架',
    `create_time` DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT        DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- --------------------------------------------
-- Table: dining_table (餐桌)
-- --------------------------------------------
DROP TABLE IF EXISTS `dining_table`;
CREATE TABLE `dining_table` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code`        VARCHAR(20) NOT NULL COMMENT '桌台编码',
    `name`        VARCHAR(50) DEFAULT NULL COMMENT '桌台名称',
    `capacity`    INT         DEFAULT 4 COMMENT '容纳人数',
    `status`      TINYINT     DEFAULT 0 COMMENT '状态：0空闲，1就餐中',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT     DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐桌表';

-- --------------------------------------------
-- Table: temp_user (临时用户)
-- --------------------------------------------
DROP TABLE IF EXISTS `temp_user`;
CREATE TABLE `temp_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `token`       VARCHAR(255) NOT NULL COMMENT '用户令牌',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='临时用户表';

-- --------------------------------------------
-- Table: order_info (订单信息)
-- --------------------------------------------
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no`      VARCHAR(32)    NOT NULL COMMENT '订单号',
    `temp_user_id`  BIGINT         DEFAULT NULL COMMENT '临时用户ID',
    `table_id`      BIGINT         DEFAULT NULL COMMENT '餐桌ID',
    `table_code`    VARCHAR(20)    DEFAULT NULL COMMENT '餐桌编码',
    `total_amount`  DECIMAL(10, 2) DEFAULT 0.00 COMMENT '总金额',
    `status`        TINYINT        DEFAULT 0 COMMENT '订单状态：0待支付，1待接单，2制作中，3已完成，4已取餐，5已取消',
    `remark`        VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    `create_time`   DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT        DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_temp_user_id` (`temp_user_id`),
    KEY `idx_table_id` (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单信息表';

-- --------------------------------------------
-- Table: order_item (订单明细)
-- --------------------------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id`    BIGINT         NOT NULL COMMENT '订单ID',
    `dish_id`     BIGINT         NOT NULL COMMENT '菜品ID',
    `dish_name`   VARCHAR(100)   NOT NULL COMMENT '菜品名称',
    `dish_price`  DECIMAL(10, 2) NOT NULL COMMENT '菜品价格',
    `dish_image`  VARCHAR(255)   DEFAULT NULL COMMENT '菜品图片',
    `quantity`    INT            NOT NULL COMMENT '数量',
    `subtotal`    DECIMAL(10, 2) NOT NULL COMMENT '小计金额',
    `taste_selection` VARCHAR(500) DEFAULT NULL COMMENT '口味选择',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- --------------------------------------------
-- Table: banner (轮播图)
-- --------------------------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`       VARCHAR(100) DEFAULT NULL COMMENT '轮播图标题',
    `image`       VARCHAR(255) NOT NULL COMMENT '图片URL',
    `link`        VARCHAR(500) DEFAULT NULL COMMENT '跳转链接（为空则纯展示）',
    `sort`        INT          DEFAULT 0 COMMENT '排序（数字越小越靠前）',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态：0禁用，1启用',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- --------------------------------------------
-- Table: avatar (卡通头像库，F3 昵称 + 头像)
-- --------------------------------------------
DROP TABLE IF EXISTS `avatar`;
CREATE TABLE `avatar` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `oss_url`     VARCHAR(512) NOT NULL COMMENT '头像OSS地址',
    `sort`        INT          DEFAULT 0 COMMENT '展示排序',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡通头像库';

-- --------------------------------------------
-- Table: merchant_config (商家配置，单行)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `merchant_config` (
    `id`                BIGINT       NOT NULL COMMENT '主键，固定为1（单行配置）',
    `about_us_content` LONGTEXT     DEFAULT NULL COMMENT '关于我们富文本HTML',
    `contact_phone`     VARCHAR(32)  DEFAULT NULL COMMENT '联系电话（中国大陆手机11位或固话）',
    `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家配置表（单行）';
