-- ============================================================
-- 会员表新增 phone 字段 + 唯一索引 + password 改可空
-- 用途：支持「手机短信验证码登录」按手机号匹配/自动注册会员
-- ============================================================
-- ⚠️ 本脚本为手动迁移脚本，部署时由 DBA/运维执行。
--    项目当前未接入 Flyway / Liquibase，请勿引入新的迁移框架。
--    建议在低峰期、先备份 member 表后执行。

-- 1. password 改为可空（短信注册用户无密码）
ALTER TABLE `member`
    MODIFY COLUMN `password` VARCHAR(255) NULL COMMENT '密码（BCrypt加密，短信注册用户可空）';

-- 2. 新增手机号字段（短信验证码登录）
ALTER TABLE `member`
    ADD COLUMN `phone` VARCHAR(20) NULL COMMENT '手机号（短信验证码登录）' AFTER `temp_user_id`;

-- 3. 手机号唯一索引（MySQL 唯一索引允许多个 NULL，不影响现有无 phone 的账号）
CREATE UNIQUE INDEX `uk_member_phone` ON `member` (`phone`);
