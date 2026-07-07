# 智能点餐系统 — 服务器部署指南

> 针对 2C2G Alibaba Cloud Linux 3 服务器，无域名，公网 IP 直接访问

---

## 部署包内容

```
order-food-deploy.tar.gz (31MB)
├── deploy.sh                    # 一键部署脚本
├── backend/
│   └── order-food-backend-1.0.0.jar   # 后端 JAR (34MB)
├── frontend/
│   ├── h5/                      # H5 用户端静态文件
│   └── admin/                   # Admin 管理端静态文件
├── sql/                         # 数据库脚本 (4个)
├── config/
│   └── application-prod.yml     # 生产环境配置模板
├── nginx/
│   └── order-food.conf          # Nginx 配置
└── systemd/
    └── order-food.service       # 后端系统服务
```

---

## 部署步骤

### 第 1 步：上传部署包到服务器

在本地终端执行（替换为你的服务器公网 IP）：

```bash
scp order-food-deploy.tar.gz root@你的公网IP:/root/
```

### 第 2 步：解压并执行部署脚本

SSH 登录服务器后执行：

```bash
cd /root
tar -xzf order-food-deploy.tar.gz -C order-food-deploy
cd order-food-deploy
chmod +x deploy.sh
bash deploy.sh
```

脚本会自动完成以下操作：
1. ✅ 安装 JDK 21
2. ✅ 安装 MySQL 8.0 并设置随机密码
3. ✅ 创建数据库 + 导入全部表结构和数据
4. ✅ 安装 Nginx
5. ✅ 部署后端 JAR + 前端静态文件
6. ✅ 配置 Nginx 反向代理
7. ✅ 创建 systemd 服务并启动
8. ✅ 验证并输出访问地址和密码

整个过程约 3-5 分钟。

### 第 3 步：开放阿里云安全组 80 端口

1. 登录阿里云控制台 → ECS → 实例
2. 点击你的服务器实例 → 安全组
3. 添加入方向规则：
   - 协议：TCP
   - 端口：80
   - 授权对象：0.0.0.0/0
4. 保存

### 第 4 步：访问系统

部署脚本执行完毕后会输出访问地址：

| 入口 | 地址 |
|------|------|
| H5 用户端 | `http://你的公网IP/` |
| Admin 管理端 | `http://你的公网IP/admin/` |
| 管理员账号 | `admin` / `admin123` |

---

## 重要信息

部署脚本执行完毕后，会自动生成并显示：
- **MySQL root 密码**（随机生成 20 位）
- **JWT 密钥**（随机生成 48 位）

这些信息也会保存在服务器的 `/opt/order-food/.credentials` 文件中。

> ⚠️ 首次登录管理后台后，请立即在「系统设置」中修改管理员密码。

---

## 内存优化说明（2G 服务器）

| 服务 | 内存占用 | 优化措施 |
|------|---------|---------|
| MySQL | ~200MB | innodb_buffer_pool_size 默认适配 |
| Java 后端 | ~450MB | -Xms128m -Xmx384m -XX:MaxMetaspaceSize=128m |
| Nginx | ~10MB | 默认配置 |
| 系统 | ~400MB | — |
| **合计** | **~1.1GB** | 剩余 ~900MB 余量 |

---

## 常用运维命令

```bash
# 重启后端
systemctl restart order-food

# 重启 Nginx
systemctl restart nginx

# 查看后端日志
journalctl -u order-food -f

# 查看后端应用日志
tail -f /opt/order-food/logs/app.log

# 查看服务状态
systemctl status order-food
systemctl status nginx
systemctl status mysqld

# 查看凭据
cat /opt/order-food/.credentials
```

---

## 常见问题

### Q: 访问不了？

1. 检查阿里云安全组是否开放了 80 端口
2. 检查服务器防火墙：`firewall-cmd --list-ports`
3. 检查服务状态：`systemctl status order-food nginx`

### Q: 后端启动失败？

```bash
journalctl -u order-food -f    # 查看实时日志
```

常见原因：MySQL 未就绪。后端服务会在 10 秒后自动重试。

### Q: 图片上传后不显示？

检查 Nginx 是否正确代理了 `/uploads/` 路径：
```bash
curl http://localhost/uploads/    # 应返回 404 而非 502
```

### Q: 需要更新代码怎么办？

1. 在本地重新构建（mvn package + npm build）
2. 上传新的 JAR / dist 到服务器
3. 重启服务：`systemctl restart order-food`
4. Nginx 静态文件更新不需要重启
