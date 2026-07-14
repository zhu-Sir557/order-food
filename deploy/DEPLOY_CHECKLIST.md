# 智能点餐系统 — 上线部署核对清单（DEPLOY CHECKLIST）

> 用途：部署负责人在跑 `bash deploy.sh` **前后**逐项核对，避免遗漏导致上线故障。
> 本清单固化了 **2026-07-13 生产上线**真实踩坑的两类问题：
> ① 漏跑 member 相关 SQL 迁移脚本（注册报 SQL 错）；② 漏配短信通道 dypnsapi（发短信 429）。
> 每次部署请逐项勾选；未勾完不得视为部署完成。

---

## A. 部署前准备

- [ ] 确认服务器为 Alibaba Cloud Linux 3 / RHEL8 系（`dnf` 可用）
- [ ] 确认已获取服务器 SSH 凭证（IP / 用户 / 密码或私钥）
- [ ] 本地已 `git pull` 最新代码（含所有 bugfix）
- [ ] 本地已构建：后端 `backend/target/order-food-backend-1.0.0.jar`
- [ ] 本地已构建：前端 `frontend-h5/dist` 与 `frontend-admin/dist`

---

## B. SQL 脚本全量核对（关键！本次真实踩坑点）

- [ ] 列出 `deploy/sql/`（或 `backend/src/main/resources/db/`）目录下**全部** `*.sql` 与 `migration_*.sql`
- [ ] 确认执行顺序：**schema.sql → data.sql → migration_user_balance.sql → migration_taste_config.sql → `migration_member_phone.sql` → `migration_member_profile.sql`**（最后两个顺序**不可颠倒**，profile 依赖 phone 字段先存在）
- [ ] 若用 `deploy.sh` 自动导入：确认脚本 `setup_database()` 已包含所有 migration（本仓库已修好，核对无遗漏；脚本对缺失文件有容错 `if [ -f ... ]`，但**缺文件不会报错**——详见下方⚠️提醒）
- [ ] 若手动导入：逐脚本在服务器 `mysql order_food < xxx.sql`
- [ ] 导入后验证：`DESCRIBE member;` 应含 `nickname` / `avatar` / `phone` 三字段；`SHOW TABLES LIKE 'avatar';` 应存在

> ⚠️ **工程提醒（务必注意）**：`deploy.sh` 解析 SQL 目录时优先使用 `deploy/sql/`，仅当该目录不存在才回退到 `backend/src/main/resources/db/`。
> 当前 `deploy/sql/` 下**已有** `migration_member_profile.sql`，但**没有** `migration_member_phone.sql`（仅存在于 `backend/src/main/resources/db/`）。
> 由于脚本对缺失文件是「静默跳过（不报错）」，若 `deploy/sql/` 未补齐 `migration_member_phone.sql`，自动导入仍会漏掉 phone 迁移。
> **处置建议（留给后续加固）**：将 `migration_member_phone.sql` 也纳入 `deploy/sql/`，或调整 `setup_database` 让两份目录的 migration 都参与导入。本次按约束仅改 `deploy.sh` 导入逻辑，未动 `deploy/sql/` 内容。

---

## C. 短信通道配置核对（关键！本次真实踩坑点）

- [ ] `deploy/config/application-prod.yml` 的 `aliyun` 段下存在 `dypnsapi:` 子段
- [ ] dypnsapi 的 `access-key-id` / `access-key-secret` 已填真实值（**复用 OSS 同一 RAM 凭证**，勿留 `__OSS_...__` 占位符）
- [ ] `sign-name: "恒创联众"`、`template-code: "100001"`、`endpoint: dypnsapi.aliyuncs.com` 填写正确
- [ ] 缩进正确：`dypnsapi` 与 `oss` 同级（2 空格），字段 4 空格（曾因缩进错被当成 oss 子项导致 credential null）
- [ ] 部署后验证：真实手机号调 `/api/h5/sms/send` 能收到短信（日志出现「短信验证码已下发」）

> ⚠️ **占位符手动填值提醒**：`deploy.sh` 的 `deploy_files()` 里 `sed` **只替换** `__DB_PASSWORD__` 和 `__JWT_SECRET__` 两个占位符，**OSS 的 `__OSS_ACCESS_KEY_ID__` / `__OSS_ACCESS_KEY_SECRET__` 占位符并不会被脚本替换**（沿用历史手动填值约定）。因此新加的 `dypnsapi` 段里那两个 `__OSS_...__` 占位符**同样需要部署时手动填真实值**（直接复用 `oss` 段的同一 RAM 凭证）。上线前务必在 `application-prod.yml` 中确认已替换为真实 AK/SK，否则短信通道 credential 为 null。

---

## D. Redis 核对

- [ ] 服务器已安装 Redis 6.0+（`redis-cli --version` 显示 6.x / 7.x）
- [ ] `systemctl is-active redis` = active
- [ ] 后端启动日志无 `credential is null` / Redis 连接异常

---

## E. 部署后验收

- [ ] 前端 `http://<公网IP>/` 返回 200
- [ ] 后端 `http://<公网IP>/api/h5/categories` 返回 200
- [ ] 注册流程通畅（短信 + 账号密码两种方式）
- [ ] 头像上传大图不超时（前端压缩 + 60s 超时）
