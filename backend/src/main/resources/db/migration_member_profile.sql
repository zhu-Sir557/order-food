-- ============================================================
-- 会员表新增 nickname / avatar 字段 + 新建 avatar 头像库表
-- 用途：F3 昵称 + 头像功能的数据层支撑
-- ============================================================
-- ⚠️ 本脚本为手动迁移脚本，部署时由 DBA/运维执行。
--    项目当前未接入 Flyway / Liquibase，请勿引入新的迁移框架。
--    建议在低峰期、先备份 member / 相关业务表后执行。

-- 1. member 表新增昵称、头像字段
ALTER TABLE `member`
    ADD COLUMN `nickname` VARCHAR(20) NULL COMMENT '昵称' AFTER `phone`,
    ADD COLUMN `avatar`   VARCHAR(512) NULL COMMENT '头像OSS地址（直接存url）' AFTER `nickname`;

-- 2. 新建头像库表
CREATE TABLE `avatar` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `oss_url`     VARCHAR(512) NOT NULL COMMENT '头像OSS地址',
    `sort`        INT          DEFAULT 0 COMMENT '展示排序',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡通头像库';
