#!/usr/bin/env bash
# ============================================================================
#  deploy/sshx.sh  --  项目内置 SSH 包装（替代临时 /tmp/sshx）
#  用法: ./sshx.sh root@<服务器IP> '要执行的命令'
#
#  服务器密码读取顺序（兼容本地与 CI）：
#    1. 优先读 DEPLOY_SERVER_PASSWORD（与 CI secret 同名）；
#    2. 缺失时回退读旧字段 DEPLOY_SERVER_PASS。
#
#  配置加载：
#    - 同目录 .deploy.env 存在时仍 source 它（本地开发优先）；
#    - source 之后，调用方已 export 的同名环境变量覆盖文件值（env 优先，CI 注入的 secret 生效）；
#    - .deploy.env 不存在时（CI），纯依赖环境变量。
#
#  注意：本文件配合 .deploy.env 使用；.deploy.env 已被 .gitignore 排除，不会上传。
#  安全红线：密码仅写入临时 askpass 文件供 ssh 读取，绝不 echo / 打印到日志。
# ============================================================================
set -euo pipefail

# 定位脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.deploy.env"

# 部署相关变量（需要支持 env 覆盖文件）
DEPLOY_VARS=(
  DEPLOY_SERVER_HOST
  DEPLOY_SERVER_USER
  DEPLOY_SERVER_PASSWORD
  DEPLOY_SERVER_PASS
  OSS_ACCESS_KEY_ID
  OSS_ACCESS_KEY_SECRET
  OSS_BUCKET_NAME
  OSS_ENDPOINT
)

# 快照当前环境变量（CI 注入的 secret 以环境变量形式存在，应优先于文件）
declare -A _ENV_SNAPSHOT
for _k in "${DEPLOY_VARS[@]}"; do
  if [[ -n "${!_k:-}" ]]; then
    _ENV_SNAPSHOT["$_k"]="${!_k}"
  fi
done

# 加载配置：.deploy.env 存在则 source（本地优先）；不存在则纯 env（CI）
if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

# 环境变量覆盖文件值（env 优先）
for _k in "${!_ENV_SNAPSHOT[@]}"; do
  export "$_k=${_ENV_SNAPSHOT[$_k]}"
done

# 读取服务器密码：优先 DEPLOY_SERVER_PASSWORD，缺失回退 DEPLOY_SERVER_PASS（旧名兼容）
PASS="${DEPLOY_SERVER_PASSWORD:-${DEPLOY_SERVER_PASS:-}}"
if [[ -z "$PASS" ]]; then
  echo "[sshx] 缺少服务器密码：请设置 DEPLOY_SERVER_PASSWORD（或旧字段 DEPLOY_SERVER_PASS）；本地可写入 .deploy.env" >&2
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
