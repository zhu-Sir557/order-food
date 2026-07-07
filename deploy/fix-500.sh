#!/bin/bash
# ============================================================
# fix-500.sh — 修复注册/登录 500 错误
# 修复内容：
#   1. 替换后端 JAR（修复 Map.of() NPE bug）
#   2. 确保 JWT 密钥和 MySQL 密码占位符被替换
# 用法：把 order-food-fix500.tar.gz 上传到服务器后执行
# ============================================================

set +e

TAR_FILE="/root/order-food-fix500.tar.gz"
EXTRACT_DIR="/tmp/fix500"
PROD_CONFIG="/opt/order-food/config/application-prod.yml"
CRED_FILE="/opt/order-food/.credentials"

echo "=========================================="
echo "  修复注册/登录 500 错误"
echo "=========================================="

# ---------- 1. 解压 ----------
echo ""
echo "[1/4] 解压更新包..."
if [ ! -f "$TAR_FILE" ]; then
    echo "  [错误] 找不到 $TAR_FILE"
    exit 1
fi
rm -rf "$EXTRACT_DIR"
mkdir -p "$EXTRACT_DIR"
tar -xzf "$TAR_FILE" -C "$EXTRACT_DIR"
echo "  解压完成"

# ---------- 2. 替换后端 JAR ----------
echo ""
echo "[2/4] 替换后端 JAR..."
systemctl stop order-food
cp "$EXTRACT_DIR/order-food-backend-1.0.0.jar" /opt/order-food/backend/
echo "  JAR 已替换"

# ---------- 3. 修复配置文件 ----------
echo ""
echo "[3/4] 检查并修复配置文件..."

# 检查是否还有占位符
NEED_JWT=$(grep -c "__JWT_SECRET__" "$PROD_CONFIG" 2>/dev/null)
NEED_DB=$(grep -c "__DB_PASSWORD__" "$PROD_CONFIG" 2>/dev/null)

if [ "$NEED_JWT" -gt 0 ] || [ "$NEED_DB" -gt 0 ]; then
    echo "  发现未替换的占位符，开始修复..."

    # 生成 JWT 密钥（48字节 base64 = 64字符，远超 256 bits 要求）
    if [ "$NEED_JWT" -gt 0 ]; then
        JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')
        sed -i "s|__JWT_SECRET__|$JWT_SECRET|g" "$PROD_CONFIG"
        echo "  JWT 密钥已生成并替换"

        # 保存到 .credentials
        if [ -f "$CRED_FILE" ]; then
            # 如果文件存在但没有 JWT 行，追加
            if ! grep -qi "jwt" "$CRED_FILE"; then
                echo "JWT Secret: $JWT_SECRET" >> "$CRED_FILE"
            else
                # 替换已有的 JWT 行
                sed -i "s|^JWT Secret:.*|JWT Secret: $JWT_SECRET|I" "$CRED_FILE"
            fi
        else
            echo "MySQL Password: __TODO__" > "$CRED_FILE"
            echo "JWT Secret: $JWT_SECRET" >> "$CRED_FILE"
        fi
    fi

    # 修复 MySQL 密码
    if [ "$NEED_DB" -gt 0 ]; then
        # 尝试从 .credentials 读取（用 awk，不用 grep -oP）
        if [ -f "$CRED_FILE" ]; then
            MYSQL_PWD=$(awk -F': ' '/[Mm]y[Ss][Qq][Ll]/{print $2}' "$CRED_FILE" | head -1 | tr -d '[:space:]')
        fi

        if [ -z "$MYSQL_PWD" ] || [ "$MYSQL_PWD" = "__TODO__" ]; then
            echo ""
            echo "  [!] 无法从 .credentials 读取 MySQL 密码"
            echo "  请输入 MySQL root 密码:"
            read -r MYSQL_PWD
            if [ -z "$MYSQL_PWD" ]; then
                echo "  [错误] 密码不能为空"
                exit 1
            fi
        fi

        sed -i "s|__DB_PASSWORD__|$MYSQL_PWD|g" "$PROD_CONFIG"
        echo "  MySQL 密码已替换"

        # 更新 .credentials
        if [ -f "$CRED_FILE" ]; then
            sed -i "s|^MySQL Password:.*|MySQL Password: $MYSQL_PWD|I" "$CRED_FILE"
        fi
    fi
else
    echo "  配置文件无需修复（占位符已替换）"
fi

# ---------- 4. 启动并验证 ----------
echo ""
echo "[4/4] 启动服务并验证..."

systemctl start order-food
echo "  后端启动中，等待 8 秒..."
sleep 8

if ! systemctl is-active --quiet order-food; then
    echo "  [错误] 后端服务未运行"
    echo "  日志："
    tail -30 /opt/order-food/logs/stderr.log 2>/dev/null
    tail -30 /opt/order-food/logs/stdout.log 2>/dev/null
    exit 1
fi
echo "  后端服务: 运行中"

echo ""
echo "=========================================="
echo "  验证接口"
echo "=========================================="

echo ""
echo "--- 1. H5 注册 ---"
curl -s -X POST http://localhost:8080/api/h5/member/register \
    -H "Content-Type: application/json" \
    -d '{"username":"testfix001","password":"123456"}'
echo ""

echo ""
echo "--- 2. H5 登录 ---"
curl -s -X POST http://localhost:8080/api/h5/member/login \
    -H "Content-Type: application/json" \
    -d '{"username":"testfix001","password":"123456","captchaToken":"skip"}'
echo ""

echo ""
echo "--- 3. Admin 登录 ---"
curl -s -X POST http://localhost:8080/api/admin/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}'
echo ""

echo ""
echo "--- 4. 公网访问 ---"
echo "H5:  http://YOUR_SERVER_IP/"
echo "Admin: http://YOUR_SERVER_IP/admin/"

echo ""
echo "=========================================="
echo "  修复完成！"
echo "  如果注册返回 code:200 表示修复成功"
echo "  如果返回 code:500 请把上方输出发给我"
echo "=========================================="
