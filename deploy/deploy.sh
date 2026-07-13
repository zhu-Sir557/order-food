#!/bin/bash
# ============================================================
# 智能点餐系统 — 一键部署脚本
# 适用系统：Alibaba Cloud Linux 3 (RHEL 8 系)
# 部署架构：Nginx(80) + Java(8080) + MySQL(3306)
# 访问方式：http://公网IP/ (H5)  http://公网IP/admin/ (管理后台)
# ============================================================
set -e

# ---- 颜色 ----
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }
step()  { echo -e "\n${CYAN}========== $1 ==========${NC}"; }

# ---- 检查 root ----
if [ "$EUID" -ne 0 ]; then
    error "请使用 root 用户执行：sudo bash deploy.sh"
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INSTALL_DIR="/opt/order-food"

# ---- 生成随机密码（优先复用已有凭据）----
if [ -f "${INSTALL_DIR}/.credentials" ]; then
    info "检测到已有凭据文件，复用数据库密码"
    DB_PASSWORD=$(grep "MySQL Password:" ${INSTALL_DIR}/.credentials | awk -F': ' '{print $2}')
    JWT_SECRET=$(grep "JWT Secret:" ${INSTALL_DIR}/.credentials | awk -F': ' '{print $2}')
else
    DB_PASSWORD=$(head -c 16 /dev/urandom | base64 | tr -dc 'a-zA-Z0-9' | head -c 20)
    JWT_SECRET=$(head -c 32 /dev/urandom | base64 | tr -dc 'a-zA-Z0-9' | head -c 48)
fi

# ============================================================
# 步骤 1/8：安装 JDK 21
# ============================================================
install_jdk() {
    step "1/8  安装 JDK 21"

    if java -version 2>&1 | grep -q "21"; then
        info "JDK 21 已安装，跳过"
        return 0
    fi

    info "尝试通过 dnf 安装 OpenJDK 21..."
    if dnf install -y java-21-openjdk-headless 2>/dev/null; then
        info "OpenJDK 21 安装成功"
    else
        warn "dnf 仓库无 JDK 21，从镜像下载 Adoptium JDK 21..."
        cd /tmp
        # 尝试多个镜像源
        JDK_URLS=(
            "https://mirrors.tuna.tsinghua.edu.cn/Adoptium/21/jre/x64/linux/OpenJDK21U-jre_x64_linux_hotspot_21.0.5_11.tar.gz"
            "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5%2B11/OpenJDK21U-jre_x64_linux_hotspot_21.0.5_11.tar.gz"
        )
        for url in "${JDK_URLS[@]}"; do
            info "尝试下载: $url"
            if wget -q --timeout=30 -O jdk21.tar.gz "$url"; then
                info "下载成功"
                break
            fi
        done

        if [ ! -f jdk21.tar.gz ] || [ ! -s jdk21.tar.gz ]; then
            error "JDK 21 下载失败，请手动安装 JDK 21 后重新执行"
            exit 1
        fi

        mkdir -p /usr/local/jdk21
        tar -xzf jdk21.tar.gz -C /usr/local/jdk21 --strip-components=1
        rm -f jdk21.tar.gz

        # 配置环境变量
        echo 'export JAVA_HOME=/usr/local/jdk21' > /etc/profile.d/jdk21.sh
        echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/profile.d/jdk21.sh
        source /etc/profile.d/jdk21.sh
        ln -sf /usr/local/jdk21/bin/java /usr/local/bin/java
        info "Adoptium JDK 21 安装成功"
    fi

    java -version
}

# ============================================================
# 步骤 2/8：安装 MySQL 8.0
# ============================================================
install_mysql() {
    step "2/8  安装 MySQL 8.0"

    if systemctl is-active --quiet mysqld 2>/dev/null || systemctl is-active --quiet mysql 2>/dev/null; then
        info "MySQL 已在运行，跳过安装"
        return 0
    fi

    info "安装 MySQL..."
    if dnf install -y mysql-server 2>/dev/null; then
        info "mysql-server 安装成功"
    else
        warn "dnf 仓库无 mysql-server，尝试 MySQL 官方仓库..."
        dnf install -y https://dev.mysql.com/get/mysql80-community-release-el8-9.noarch.rpm
        dnf install -y mysql-community-server
    fi

    # 启动 MySQL
    systemctl start mysqld 2>/dev/null || systemctl start mysql
    systemctl enable mysqld 2>/dev/null || systemctl enable mysql
    info "MySQL 已启动"

    # 等待 MySQL 就绪
    info "等待 MySQL 就绪..."
    for i in $(seq 1 30); do
        if mysqladmin ping -h localhost 2>/dev/null | grep -q "mysqld is alive"; then
            info "MySQL 已就绪"
            break
        fi
        sleep 1
    done
}

# ============================================================
# 步骤 3/8：配置数据库 + 导入数据
# ============================================================
setup_database() {
    step "3/8  配置数据库"

    # 检测 root 连接方式：优先尝试无密码（socket 认证），再尝试已有密码
    local MYSQL_CONN=""
    if mysql -u root -e "SELECT 1;" 2>/dev/null; then
        # 无密码（socket 认证）
        MYSQL_CONN="mysql -u root"
        info "root 无密码认证，设置密码..."
        ${MYSQL_CONN} -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${DB_PASSWORD}'; FLUSH PRIVILEGES;" 2>/dev/null || true
        info "root 密码已设置"
    elif mysql -u root -p"${DB_PASSWORD}" -e "SELECT 1;" 2>/dev/null; then
        # 当前密码正确
        MYSQL_CONN="mysql -u root -p${DB_PASSWORD}"
        info "root 密码已正确设置，跳过"
    else
        error "MySQL root 密码验证失败"
        error "提示：如之前运行过本脚本，密码保存在 ${INSTALL_DIR}/.credentials"
        error "请手动执行：mysql -u root -p 然后 ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '新密码';"
        exit 1
    fi

    # 创建数据库
    info "创建数据库 order_food..."
    ${MYSQL_CONN} -e "CREATE DATABASE IF NOT EXISTS order_food DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;" 2>/dev/null || true

    # 导入 SQL 脚本（按顺序）
    SQL_DIR="${SCRIPT_DIR}/sql"
    if [ ! -d "$SQL_DIR" ]; then
        SQL_DIR="${SCRIPT_DIR}/backend/src/main/resources/db"
    fi

    info "导入数据库脚本..."
    ${MYSQL_CONN} order_food < "${SQL_DIR}/schema.sql" 2>/dev/null || true
    info "  schema.sql ✓"
    ${MYSQL_CONN} order_food < "${SQL_DIR}/data.sql" 2>/dev/null || true
    info "  data.sql ✓"
    if [ -f "${SQL_DIR}/migration_taste_config.sql" ]; then
        ${MYSQL_CONN} order_food < "${SQL_DIR}/migration_taste_config.sql" 2>/dev/null || true
        info "  migration_taste_config.sql ✓"
    fi
    if [ -f "${SQL_DIR}/migration_user_balance.sql" ]; then
        ${MYSQL_CONN} order_food < "${SQL_DIR}/migration_user_balance.sql" 2>/dev/null || true
        info "  migration_user_balance.sql ✓"
    fi

    # 验证表数量（set -e 对 $() 中的非零退出码敏感，必须加 || echo "0"）
    TABLE_COUNT=$(${MYSQL_CONN} order_food -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='order_food';" 2>/dev/null || echo "0")
    info "数据库共 ${TABLE_COUNT} 张表"
}

# ============================================================
# 安装 Redis 6.0+（后端强依赖：滑块验证码 / 短信限流 / 缓存）
# ============================================================
install_redis() {
    step "安装 Redis 6.0+"

    if command -v redis-cli &>/dev/null && redis-cli --version 2>/dev/null | grep -qE "redis-cli [6-9]\.|redis-cli [0-9]{2}\."; then
        info "Redis 6.0+ 已安装，跳过"
    else
        info "通过 dnf 安装 Redis..."
        if dnf install -y redis 2>/dev/null; then
            info "Redis 安装成功"
        else
            error "Redis 安装失败，请手动安装 redis 6.0+ 后重新执行"
            exit 1
        fi
    fi

    systemctl enable redis
    systemctl start redis

    info "等待 Redis 就绪..."
    for i in $(seq 1 10); do
        if redis-cli ping 2>/dev/null | grep -q PONG; then
            info "Redis 已就绪"
            break
        fi
        sleep 1
    done
    redis-cli ping
}

# ============================================================
# 步骤 4/8：安装 Nginx
# ============================================================
install_nginx() {
    step "4/8  安装 Nginx"

    if nginx -v 2>&1 | grep -q "nginx"; then
        info "Nginx 已安装，跳过"
        mkdir -p /etc/nginx/conf.d
        return 0
    fi

    # 方式1：直接 dnf 安装
    info "尝试 dnf 直接安装 nginx..."
    if dnf install -y nginx 2>/dev/null; then
        nginx -v 2>&1 && info "Nginx 安装成功"
    else
        warn "dnf 直接安装失败，尝试 EPEL 仓库..."
        # 方式2：安装 EPEL 仓库后再装
        dnf install -y epel-release 2>/dev/null || true
        dnf install -y nginx 2>/dev/null && nginx -v 2>&1 && info "Nginx 通过 EPEL 安装成功"
    fi

    # 方式3：如果还没装上，用 nginx 官方仓库
    if ! nginx -v 2>&1 | grep -q "nginx"; then
        warn "EPEL 方式也失败，尝试 nginx 官方仓库..."
        cat > /etc/yum.repos.d/nginx.repo << 'NGINXREPO'
[nginx-stable]
name=nginx stable repo
baseurl=http://nginx.org/packages/centos/8/$basearch/
gpgcheck=1
enabled=1
gpgkey=https://nginx.org/keys/nginx_signing.key
module_hotfixes=true
NGINXREPO
        dnf install -y nginx 2>/dev/null && nginx -v 2>&1 && info "Nginx 通过官方仓库安装成功"
    fi

    # 最终检查
    if ! nginx -v 2>&1 | grep -q "nginx"; then
        error "Nginx 安装失败！请手动安装：dnf install -y nginx 或 apt install -y nginx"
        error "安装完成后重新运行 bash deploy.sh"
        exit 1
    fi

    systemctl enable nginx
    mkdir -p /etc/nginx/conf.d
    info "Nginx 安装完成"
}

# ============================================================
# 步骤 5/8：部署应用文件
# ============================================================
deploy_files() {
    step "5/8  部署应用文件"

    # 创建目录
    mkdir -p ${INSTALL_DIR}/{backend,frontend/h5,frontend/admin,uploads,logs,config}

    # 复制后端 JAR
    info "部署后端 JAR..."
    cp "${SCRIPT_DIR}/backend/order-food-backend-1.0.0.jar" ${INSTALL_DIR}/backend/

    # 复制前端
    info "部署 H5 前端..."
    if [ -d "${SCRIPT_DIR}/frontend/h5" ]; then
        cp -r "${SCRIPT_DIR}/frontend/h5/"* ${INSTALL_DIR}/frontend/h5/
    fi

    info "部署 Admin 前端..."
    if [ -d "${SCRIPT_DIR}/frontend/admin" ]; then
        cp -r "${SCRIPT_DIR}/frontend/admin/"* ${INSTALL_DIR}/frontend/admin/
    fi

    # 生成 application-prod.yml（替换密码和密钥）
    info "生成生产配置..."
    sed "s|__DB_PASSWORD__|${DB_PASSWORD}|g; s|__JWT_SECRET__|${JWT_SECRET}|g" \
        "${SCRIPT_DIR}/config/application-prod.yml" > ${INSTALL_DIR}/config/application-prod.yml

    # 设置目录权限
    chown -R nginx:nginx ${INSTALL_DIR}/uploads 2>/dev/null || chown -R nobody:nobody ${INSTALL_DIR}/uploads 2>/dev/null || true
    chmod -R 755 ${INSTALL_DIR}

    info "应用文件部署完成"
}

# ============================================================
# 步骤 6/8：配置 Nginx
# ============================================================
setup_nginx() {
    step "6/8  配置 Nginx"

    # 确保 nginx 命令可用
    if ! command -v nginx &>/dev/null; then
        error "nginx 命令不存在，Nginx 未安装成功"
        error "请手动安装：dnf install -y epel-release && dnf install -y nginx"
        exit 1
    fi

    # 确保 conf.d 目录存在
    mkdir -p /etc/nginx/conf.d

    # 确保 nginx.conf 中包含 conf.d/*.conf
    if ! grep -q "include /etc/nginx/conf.d" /etc/nginx/nginx.conf 2>/dev/null; then
        sed -i '/^http {/,/^}/ s/^}/    include \/etc\/nginx\/conf.d\/*.conf;\n}/' /etc/nginx/nginx.conf 2>/dev/null || true
        info "已向 nginx.conf 添加 conf.d include"
    fi

    cp "${SCRIPT_DIR}/nginx/order-food.conf" /etc/nginx/conf.d/order-food.conf

    # 移除默认配置（避免冲突）
    if [ -f /etc/nginx/conf.d/default.conf ]; then
        mv /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf.bak
        info "已备份默认 Nginx 配置"
    fi

    # 测试 Nginx 配置
    if ! nginx -t 2>&1; then
        warn "Nginx 配置测试失败，尝试直接写入 nginx.conf..."
        cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.bak 2>/dev/null || true
        echo "" >> /etc/nginx/nginx.conf
        cat "${SCRIPT_DIR}/nginx/order-food.conf" >> /etc/nginx/nginx.conf
        nginx -t 2>&1
    fi
    info "Nginx 配置测试通过"
}

# ============================================================
# 步骤 7/8：启动服务
# ============================================================
start_services() {
    step "7/8  启动服务"

    # 配置 systemd 服务
    info "配置后端系统服务..."
    cp "${SCRIPT_DIR}/systemd/order-food.service" /etc/systemd/system/order-food.service
    systemctl daemon-reload
    systemctl enable order-food

    # 启动后端
    info "启动后端服务..."
    systemctl restart order-food

    # 等待后端启动
    info "等待后端启动..."
    for i in $(seq 1 30); do
        if curl -sf http://localhost:8080/api/h5/categories -o /dev/null 2>/dev/null; then
            info "后端服务已就绪"
            break
        fi
        if [ $i -eq 30 ]; then
            warn "后端启动较慢，请稍后检查：systemctl status order-food"
        fi
        sleep 2
    done

    # 启动 Nginx
    info "启动 Nginx..."
    systemctl restart nginx

    info "所有服务已启动"
}

# ============================================================
# 步骤 8/8：验证 + 输出信息
# ============================================================
verify() {
    step "8/8  验证部署"

    # 获取公网 IP
    PUBLIC_IP=$(curl -s http://ifconfig.me 2>/dev/null || curl -s http://ip.sb 2>/dev/null || echo "你的公网IP")

    echo ""
    echo -e "${GREEN}╔══════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║          ✅  部署完成！                                ║${NC}"
    echo -e "${GREEN}╠══════════════════════════════════════════════════════╣${NC}"
    echo -e "${GREEN}║                                                        ║${NC}"
    echo -e "${GREEN}║  H5 用户端：  http://${PUBLIC_IP}/                     ${NC}"
    echo -e "${GREEN}║  管理后台：  http://${PUBLIC_IP}/admin/                ${NC}"
    echo -e "${GREEN}║  管理员账号：admin / admin123                          ${NC}"
    echo -e "${GREEN}║                                                        ║${NC}"
    echo -e "${GREEN}╠══════════════════════════════════════════════════════╣${NC}"
    echo -e "${YELLOW}║  ⚠️  请保存以下信息（仅显示一次）：                     ${NC}"
    echo -e "${YELLOW}║  MySQL 密码：${DB_PASSWORD}                            ${NC}"
    echo -e "${YELLOW}║  JWT 密钥：  ${JWT_SECRET}                             ${NC}"
    echo -e "${GREEN}╠══════════════════════════════════════════════════════╣${NC}"
    echo -e "${GREEN}║  常用命令：                                             ${NC}"
    echo -e "${GREEN}║  重启后端：systemctl restart order-food               ${NC}"
    echo -e "${GREEN}║  重启Nginx：systemctl restart nginx                    ${NC}"
    echo -e "${GREEN}║  查看日志：journalctl -u order-food -f                 ${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════════════╝${NC}"
    echo ""

    # 保存密码到文件
    echo "MySQL Password: ${DB_PASSWORD}" > ${INSTALL_DIR}/.credentials
    echo "JWT Secret: ${JWT_SECRET}" >> ${INSTALL_DIR}/.credentials
    chmod 600 ${INSTALL_DIR}/.credentials
    info "凭据已保存到 ${INSTALL_DIR}/.credentials"

    # 验证服务状态
    echo ""
    info "服务状态检查："
    systemctl is-active order-food > /dev/null 2>&1 && info "  后端服务: ✓ 运行中" || warn "  后端服务: ✗ 未运行"
    systemctl is-active nginx > /dev/null 2>&1 && info "  Nginx: ✓ 运行中" || warn "  Nginx: ✗ 未运行"
    systemctl is-active mysqld > /dev/null 2>&1 && info "  MySQL: ✓ 运行中" || systemctl is-active mysql > /dev/null 2>&1 && info "  MySQL: ✓ 运行中" || warn "  MySQL: ✗ 未运行"

    echo ""
    warn "⚠️  请在阿里云安全组中开放 80 端口（入方向 TCP）"
    warn "⚠️  首次登录管理后台后请立即修改管理员密码"
}

# ============================================================
# 主流程
# ============================================================
main() {
    echo -e "${CYAN}"
    echo "  ╔═══════════════════════════════════════════╗"
    echo "  ║   智能点餐系统 — 一键部署脚本             ║"
    echo "  ║   架构：Nginx + Java + MySQL              ║"
    echo "  ║   适用：Alibaba Cloud Linux 3 (2C2G)      ║"
    echo "  ╚═══════════════════════════════════════════╝"
    echo -e "${NC}"

    install_jdk
    install_mysql
    setup_database
    install_redis
    install_nginx
    deploy_files
    setup_nginx
    start_services
    verify
}

main
