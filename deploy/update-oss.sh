#!/bin/bash
# ============================================================
# OSS 更新脚本 — 替换后端JAR + Nginx配置 + 应用配置
# 用法: 把 order-food-oss-update.tar.gz 上传到服务器后执行
# ============================================================

set +e

TAR_FILE="/root/order-food-oss-update.tar.gz"
EXTRACT_DIR="/tmp/oss-update"

echo "=========================================="
echo "  OSS 更新 — 替换后端 + Nginx + 配置"
echo "=========================================="

# ---------- 1. 解压 ----------
echo ""
echo "[1/5] 解压更新包..."
if [ ! -f "$TAR_FILE" ]; then
    echo "  [错误] 找不到 $TAR_FILE"
    echo "  请先上传 order-food-oss-update.tar.gz 到 /root/"
    exit 1
fi
rm -rf "$EXTRACT_DIR"
mkdir -p "$EXTRACT_DIR"
tar -xzf "$TAR_FILE" -C "$EXTRACT_DIR"
echo "  解压完成"

# ---------- 2. 替换后端 JAR ----------
echo ""
echo "[2/5] 替换后端 JAR..."
systemctl stop order-food
cp "$EXTRACT_DIR/order-food-backend-1.0.0.jar" /opt/order-food/backend/
echo "  JAR 已替换"

# ---------- 3. 更新应用配置 ----------
echo ""
echo "[3/5] 更新应用配置..."

PROD_CONFIG="/opt/order-food/config/application-prod.yml"
NEW_CONFIG="$EXTRACT_DIR/config/application-prod.yml"

# 备份旧配置
if [ -f "$PROD_CONFIG" ]; then
    cp "$PROD_CONFIG" "${PROD_CONFIG}.bak.$(date +%s)"
    echo "  旧配置已备份"
fi

# 复制新配置（带 OSS 配置的版本）
cp "$NEW_CONFIG" "$PROD_CONFIG"

# 替换数据库密码和 JWT 密钥的占位符
CRED_FILE="/opt/order-food/.credentials"
if [ -f "$CRED_FILE" ]; then
    MYSQL_PWD=$(grep -i "mysql" "$CRED_FILE" | grep -oP '(?<=:\s).*' | head -1 | tr -d '[:space:]')
    JWT_SECRET=$(grep -i "jwt" "$CRED_FILE" | grep -oP '(?<=:\s).*' | head -1 | tr -d '[:space:]')
    
    if [ -n "$MYSQL_PWD" ]; then
        sed -i "s|__DB_PASSWORD__|$MYSQL_PWD|g" "$PROD_CONFIG"
        echo "  MySQL 密码已替换"
    fi
    if [ -n "$JWT_SECRET" ]; then
        sed -i "s|__JWT_SECRET__|$JWT_SECRET|g" "$PROD_CONFIG"
        echo "  JWT 密钥已替换"
    fi
else
    echo "  [警告] .credentials 文件不存在，请手动替换占位符"
    echo "  配置文件位置: $PROD_CONFIG"
    echo "  需要替换: __DB_PASSWORD__ 和 __JWT_SECRET__"
fi

# ---------- 4. 更新 Nginx 配置 ----------
echo ""
echo "[4/5] 更新 Nginx 配置..."

NGINX_CONF="/etc/nginx/conf.d/order-food.conf"
if [ -f "$NGINX_CONF" ]; then
    cp "$NGINX_CONF" "${NGINX_CONF}.bak.$(date +%s)"
fi
cp "$EXTRACT_DIR/nginx/order-food.conf" "$NGINX_CONF"
echo "  Nginx 配置已替换（/uploads/ location 已移除）"

# ---------- 5. 启动服务 ----------
echo ""
echo "[5/5] 启动服务..."

systemctl start order-food
echo "  后端启动中，等待 8 秒..."
sleep 8

# 检查后端
if systemctl is-active --quiet order-food; then
    echo "  后端服务: 运行中 ✓"
else
    echo "  [警告] 后端服务未运行，查看日志:"
    tail -20 /opt/order-food/logs/stderr.log 2>/dev/null
    tail -20 /opt/order-food/logs/stdout.log 2>/dev/null
fi

# 重载 Nginx
nginx -t 2>/dev/null && systemctl reload nginx && echo "  Nginx: 已重载 ✓" || echo "  [警告] Nginx 重载失败"

# ---------- 验证 ----------
echo ""
echo "=========================================="
echo "  验证"
echo "=========================================="

echo ""
echo "--- 后端 API ---"
curl -s http://localhost:8080/api/h5/categories | head -c 200
echo ""

echo ""
echo "--- Admin 登录 ---"
curl -s -X POST http://localhost:8080/api/admin/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}'
echo ""

echo ""
echo "--- 公网访问 ---"
echo "H5:  http://YOUR_SERVER_IP/"
echo "Admin: http://YOUR_SERVER_IP/admin/"

echo ""
echo "=========================================="
echo "  更新完成！"
echo "  图片上传已切换到阿里云 OSS"
echo "  上传图片后将返回 OSS 公网 URL"
echo "=========================================="
