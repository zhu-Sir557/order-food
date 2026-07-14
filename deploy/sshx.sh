#!/usr/bin/env bash
# ============================================================================
#  deploy/sshx.sh  --  项目内置 SSH 包装（替代临时 /tmp/sshx）
#  用法: ./sshx.sh root@8.133.204.113 '要执行的命令'
#  服务器密码从同目录 .deploy.env 的 DEPLOY_SERVER_PASS 读取（不落明文到参数）
#  注意：本文件配合 .deploy.env 使用；.deploy.env 已被 .gitignore 排除，不会上传。
# ============================================================================
set -euo pipefail

# 定位脚本所在目录，读取 .deploy.env
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.deploy.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "[sshx] 找不到 $ENV_FILE，请先复制 .deploy.env.example 为 .deploy.env 并填写" >&2
  exit 1
fi

# 仅导出需要的变量（避免污染）
set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

PASS="${DEPLOY_SERVER_PASS:-}"
if [[ -z "$PASS" ]]; then
  echo "[sshx] .deploy.env 中缺少 DEPLOY_SERVER_PASS" >&2
  exit 1
fi

# askpass 辅助：让 ssh 从 stdin 之外的安全通道拿密码
ASKPASS="$(mktemp /tmp/sshx_askpass.XXXXXX)"
cat > "$ASKPASS" <<EOF
#!/usr/bin/env bash
echo '$PASS'
EOF
chmod 700 "$ASKPASS"
trap 'rm -f "$ASKPASS"' EXIT

export SSH_ASKPASS="$ASKPASS"
export SSH_ASKPASS_REQUIRE=force
export DISPLAY=:0  # 某些 ssh 实现要求非交互下有 DISPLAY 才走 askpass

# 执行：把剩余所有参数作为整条命令传给远端
ssh -o StrictHostKeyChecking=no \
    -o UserKnownHostsFile=/dev/null \
    -o PreferredAuthentications=password \
    -o PubkeyAuthentication=no \
    -o LogLevel=ERROR \
    "$@"

# askpass 由 trap 自动清理
