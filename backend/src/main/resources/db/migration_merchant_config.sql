-- =============================================
-- 商家配置表增量迁移脚本（单行配置）
-- 部署阶段在线上 MySQL 手动执行；禁止 DROP TABLE。
-- =============================================

CREATE TABLE IF NOT EXISTS `merchant_config` (
  `id`                BIGINT       NOT NULL COMMENT '主键，固定为1（单行配置）',
  `about_us_content` LONGTEXT     DEFAULT NULL COMMENT '关于我们富文本HTML',
  `contact_phone`     VARCHAR(32)  DEFAULT NULL COMMENT '联系电话（中国大陆手机11位或固话）',
  `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家配置表（单行）';
