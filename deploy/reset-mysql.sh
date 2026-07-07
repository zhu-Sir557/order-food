#!/bin/bash
# ============================================================
# MySQL 密码重置脚本
# 适用：MySQL root 密码丢失，无法连接
# 执行：bash reset-mysql.sh
# ============================================================
set +e

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

if [ "$EUID" -ne 0 ]; then
    error "请使用 root 用户执行：sudo bash reset-mysql.sh"
    exit 1
fi

echo -e "${CYAN}"
echo "  ╔═══════════════════════════════════╗"
echo "  ║   MySQL 密码重置 / 重装工具       ║"
echo "  ╚═══════════════════════════════════╝"
echo -e "${NC}"

# ---- 方式 1：skip-grant-tables 重置密码 ----
reset_password() {
    info "方式 1：通过 skip-grant-tables 重置密码"

    info "停止 MySQL..."
    systemctl stop mysqld 2>/dev/null

    info "写入临时配置（跳过权限验证）..."
    cat > /etc/my.cnf.d/reset-root.cnf << 'EOF'
[mysqld]
skip-grant-tables
EOF

    info "启动 MySQL（无权限模式）..."
    systemctl start mysqld
    sleep 3

    info "重置 root 密码为空（socket 认证）..."
    mysql -u root << 'SQL'
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '';
FLUSH PRIVILEGES;
SQL

    info "移除临时配置..."
    rm -f /etc/my.cnf.d/reset-root.cnf

    info "重启 MySQL（正常模式）..."
    systemctl restart mysqld
    sleep 3

    # 验证
    if mysql -u root -e "SELECT 'OK' AS status;" 2>/dev/null; then
        info "✅ 密码重置成功！root 现在无密码"
        return 0
    else
        warn "方式 1 失败，尝试方式 2（卸载重装）..."
        return 1
    fi
}

# ---- 方式 2：卸载重装 MySQL ----
reinstall_mysql() {
    info "方式 2：卸载重装 MySQL"

    info "停止 MySQL..."
    systemctl stop mysqld 2>/dev/null

    info "卸载 MySQL..."
    dnf remove -y mysql-server mysql 2>/dev/null || yum remove -y mysql-server mysql 2>/dev/null

    info "清理数据目录..."
    rm -rf /var/lib/mysql
    rm -rf /var/log/mysqld.log
    rm -f /etc/my.cnf.d/reset-root.cnf

    info "重新安装 MySQL..."
    dnf install -y mysql-server 2>/dev/null || yum install -y mysql-server 2>/dev/null

    info "初始化并启动 MySQL..."
    systemctl start mysqld
    systemctl enable mysqld
    sleep 5

    # 验证
    if mysql -u root -e "SELECT 'OK' AS status;" 2>/dev/null; then
        info "✅ MySQL 重装成功！root 无密码"
        return 0
    else
        error "❌ MySQL 重装失败，请手动检查"
        error "手动安装命令：dnf install -y mysql-server && systemctl start mysqld"
        return 1
    fi
}

# ---- 主流程 ----
if reset_password; then
    echo ""
    info "现在可以重新运行部署脚本：bash deploy.sh"
elif reinstall_mysql; then
    echo ""
    info "现在可以重新运行部署脚本：bash deploy.sh"
else
    echo ""
    error "两种方式都失败了，请手动排查 MySQL 问题"
    exit 1
fi
