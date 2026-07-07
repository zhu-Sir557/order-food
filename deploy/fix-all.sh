#!/bin/bash
# ============================================================
# 一键修复脚本 — 解决所有已知部署问题
# 用法: bash fix-all.sh
# ============================================================

set +e  # 不因单条命令失败就退出

echo "=========================================="
echo "  点餐系统一键修复"
echo "=========================================="

# ---------- 1. 读取/生成凭据 ----------
echo ""
echo "[1/7] 处理凭据..."

CRED_FILE="/opt/order-food/.credentials"
CONFIG_FILE="/opt/order-food/config/application-prod.yml"

# 读取或生成 MySQL 密码
MYSQL_PWD=""
if [ -f "$CRED_FILE" ]; then
    MYSQL_PWD=$(grep -i "mysql" "$CRED_FILE" | grep -oP '(?<=:\s).*' | head -1 | tr -d '[:space:]')
fi

# 如果没读到，尝试用 root 无密码或已知密码连接
if [ -z "$MYSQL_PWD" ]; then
    # 尝试读取当前配置文件里的密码
    MYSQL_PWD=$(grep -oP '(?<=password:\s).*' "$CONFIG_FILE" | head -1 | tr -d '[:space:]')
    if [ "$MYSQL_PWD" = "__DB_PASSWORD__" ] || [ -z "$MYSQL_PWD" ]; then
        # 最后手段：尝试无密码连接
        if mysql -u root -e "SELECT 1" 2>/dev/null; then
            MYSQL_PWD=""
        else
            echo "  [警告] 无法自动获取 MySQL 密码"
            echo "  请手动输入 MySQL root 密码（输入后按回车）："
            read -s MYSQL_PWD
        fi
    fi
fi

# 验证 MySQL 连接
if mysql -u root -p"$MYSQL_PWD" -e "SELECT 1" 2>/dev/null; then
    echo "  MySQL 连接成功"
else
    if mysql -u root -e "SELECT 1" 2>/dev/null; then
        MYSQL_PWD=""
        echo "  MySQL 连接成功（无密码）"
    else
        echo "  [错误] MySQL 连接失败，密码: [$MYSQL_PWD]"
        echo "  请先重置 MySQL 密码，或手动修改本脚本中的 MYSQL_PWD"
        exit 1
    fi
fi

# 生成 JWT 密钥（如果还没有）
JWT_SECRET=""
if [ -f "$CRED_FILE" ]; then
    JWT_SECRET=$(grep -i "jwt" "$CRED_FILE" | grep -oP '(?<=:\s).*' | head -1 | tr -d '[:space:]')
fi
if [ -z "$JWT_SECRET" ] || [ "$JWT_SECRET" = "__JWT_SECRET__" ]; then
    JWT_SECRET=$(openssl rand -base64 48)
    echo "  生成新的 JWT 密钥"
fi

# 保存凭据
cat > "$CRED_FILE" <<EOF
MySQL Password: $MYSQL_PWD
JWT Secret: $JWT_SECRET
EOF
chmod 600 "$CRED_FILE"
echo "  凭据已保存到 $CRED_FILE"

# ---------- 2. 修复配置文件 ----------
echo ""
echo "[2/7] 修复配置文件..."

if [ ! -f "$CONFIG_FILE" ]; then
    echo "  [错误] 配置文件不存在: $CONFIG_FILE"
    exit 1
fi

# 备份
cp "$CONFIG_FILE" "${CONFIG_FILE}.bak.$(date +%s)"

# 替换占位符
sed -i "s|__DB_PASSWORD__|$MYSQL_PWD|g" "$CONFIG_FILE"
sed -i "s|__JWT_SECRET__|$JWT_SECRET|g" "$CONFIG_FILE"

# 验证替换结果
REMAINING=$(grep -c "__" "$CONFIG_FILE" || echo "0")
echo "  配置文件已更新，剩余占位符: $REMAINING"

# ---------- 3. 修复 Nginx 配置 ----------
echo ""
echo "[3/7] 修复 Nginx 配置..."

NGINX_CONF="/etc/nginx/conf.d/order-food.conf"

if [ ! -f "$NGINX_CONF" ]; then
    echo "  [警告] Nginx 配置文件不存在: $NGINX_CONF"
else
    cp "$NGINX_CONF" "${NGINX_CONF}.bak.$(date +%s)"

    # 用 sed 替换 uploads 配置：把 proxy_pass 改成 alias
    # 匹配 location /uploads/ { ... } 块，替换内容
    sed -i '/location \/uploads\//,/}/ {
        /proxy_pass/d
        /proxy_set_header/d
        s/}/    alias \/opt\/order-food\/uploads\/;\n}/
    }' "$NGINX_CONF"

    # 如果上面没替换成功（可能格式不同），用 python 兜底
    if ! grep -q "alias" "$NGINX_CONF"; then
        python3 -c "
import re
with open('$NGINX_CONF', 'r') as f:
    content = f.read()
content = re.sub(
    r'location /uploads/ \{[^}]*\}',
    'location /uploads/ {\n        alias /opt/order-food/uploads/;\n    }',
    content,
    flags=re.DOTALL
)
with open('$NGINX_CONF', 'w') as f:
    f.write(content)
"
    fi

    echo "  Nginx 配置已更新"
    grep -A2 "uploads" "$NGINX_CONF"
fi

# ---------- 4. 确保目录和权限 ----------
echo ""
echo "[4/7] 检查目录和权限..."

mkdir -p /opt/order-food/uploads
mkdir -p /opt/order-food/logs
chmod 755 /opt/order-food/uploads
chmod 755 /opt/order-food/logs

# 检查 uploads 目录里有没有文件
UPLOAD_COUNT=$(ls /opt/order-food/uploads/ 2>/dev/null | wc -l)
echo "  uploads 目录文件数: $UPLOAD_COUNT"

# ---------- 5. 重启服务 ----------
echo ""
echo "[5/7] 重启服务..."

systemctl restart order-food
echo "  后端服务已重启，等待启动..."
sleep 8

# 检查后端是否启动成功
if systemctl is-active --quiet order-food; then
    echo "  后端服务: 运行中"
else
    echo "  [警告] 后端服务未运行，检查日志:"
    tail -20 /opt/order-food/logs/stderr.log 2>/dev/null
fi

# 重载 Nginx
nginx -t 2>/dev/null
if [ $? -eq 0 ]; then
    systemctl reload nginx
    echo "  Nginx: 已重载"
else
    echo "  [警告] Nginx 配置语法错误，尝试重启"
    systemctl restart nginx
fi

# ---------- 6. 验证 ----------
echo ""
echo "[6/7] 验证各功能..."

echo ""
echo "--- 6.1 后端 API (菜品分类) ---"
RESULT=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/h5/categories)
echo "  GET /api/h5/categories -> HTTP $RESULT"

echo ""
echo "--- 6.2 Admin 登录 ---"
LOGIN_RESULT=$(curl -s -X POST http://localhost:8080/api/admin/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}')
echo "  POST /api/admin/auth/login -> $LOGIN_RESULT"

echo ""
echo "--- 6.3 H5 注册 ---"
REG_RESULT=$(curl -s -X POST http://localhost:8080/api/h5/member/register \
    -H "Content-Type: application/json" \
    -d '{"username":"testuser_fix_'$RANDOM'","password":"123456"}')
echo "  POST /api/h5/member/register -> $REG_RESULT"

echo ""
echo "--- 6.4 图片访问 ---"
# 找一个实际存在的图片文件测试
FIRST_IMG=$(ls /opt/order-food/uploads/*.png /opt/order-food/uploads/*.jpg /opt/order-food/uploads/*.jpeg /opt/order-food/uploads/*.gif /opt/order-food/uploads/*.webp 2>/dev/null | head -1)
if [ -n "$FIRST_IMG" ]; then
    IMG_NAME=$(basename "$FIRST_IMG")
    IMG_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost/uploads/$IMG_NAME")
    echo "  GET /uploads/$IMG_NAME -> HTTP $IMG_CODE"
else
    echo "  uploads 目录暂无图片文件，跳过图片测试"
fi

echo ""
echo "--- 6.5 公网访问 ---"
PUB_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/h5/categories)
echo "  GET http://YOUR_SERVER_IP/api/h5/categories -> HTTP $PUB_CODE"

ADMIN_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/admin/)
echo "  GET http://YOUR_SERVER_IP/admin/ -> HTTP $ADMIN_CODE"

# ---------- 7. 总结 ----------
echo ""
echo "=========================================="
echo "  修复完成！"
echo "=========================================="
echo ""
echo "访问地址："
echo "  H5 用户端:  http://YOUR_SERVER_IP/"
echo "  Admin 后台: http://YOUR_SERVER_IP/admin/"
echo "  API 接口:   http://YOUR_SERVER_IP/api/"
echo ""
echo "如果某个功能仍有问题，请把上面 6.1-6.5 的完整输出发给我"
echo "=========================================="
