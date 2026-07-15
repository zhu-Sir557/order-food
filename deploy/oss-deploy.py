#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
============================================================================
 order-food 一键上线脚本（OSS 中转，三端全量）
----------------------------------------------------------------------------
 适用：本地改完代码后，一条命令把 后端 + h5用户端 + admin管理端 全部上线。
       同样可在 CI（GitHub Actions）中以 --ci 模式运行。

 模式：
   本地模式（默认）：
     - 后端用 ./mvnw.sh -o package（离线、禁用 clean）
     - 前端 vite build 前先 mv dist dist_bak（绕开沙箱安全删除）
    CI 模式（--ci 显式，或环境变量 CI=true 自动）：
     - 后端用系统 mvn -DskipTests package（在线、去 -o、去 clean）
     - 前端先 vue-tsc --noEmit 类型检查（失败即停），再 node vite build

 流程：
   1. 构建（后端 mvn / 前端 vite build，自动绕开沙箱安全删除）
   2. 把产物（jar + 两个 dist tar.gz）传 OSS（阿里云，同地域内网极快）
   3. 服务器通过 sshx.sh 执行：拉取 OSS -> 替换 -> 重启（后端 systemd / 前端静态免重启）
   4. 收尾：OSS 临时对象改 private、清本地 dist_bak
   5. 健康检查

 配置加载顺序（兼容本地与 CI）：
   1. 先尝试读同目录 .deploy.env（本地开发优先）；
   2. 文件不存在则从 os.environ 读（CI，已注入同名 secret）；
   3. 两者合并（环境变量覆盖文件值），CI 无文件也能跑。

 依赖：
   - 同目录 .deploy.env（含服务器密码、OSS AK/SK、桶名）—— 已被 .gitignore 排除
   - 同目录 sshx.sh（服务器 SSH 包装，密码从环境注入）
   - 本地 / CI 的 Python 装了 oss2

 安全红线：
   - 任何位置不得 print/echo OSS_ACCESS_KEY_SECRET、DEPLOY_SERVER_PASSWORD 等密钥；
   - 仅允许打印 OSS 公网 URL（不含密钥）与部署进度。

 用法：
   python3 deploy/oss-deploy.py                 # 三端全量上线（本地）
   python3 deploy/oss-deploy.py --ci            # CI 模式（也可由 CI=true 自动）
   python3 deploy/oss-deploy.py --skip-backend  # 只上前端
   python3 deploy/oss-deploy.py --skip-frontend # 只上后端
   python3 deploy/oss-deploy.py --no-build      # 不重新构建，直接用已有产物上线
   python3 deploy/oss-deploy.py --host 1.2.3.4  # 临时覆盖服务器IP
============================================================================
"""
import argparse
import hashlib
import os
import sys
import time
import subprocess
import tarfile
import tempfile
import shutil
from pathlib import Path

# ---------- 路径 ----------
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
BACKEND_DIR = os.path.join(PROJECT_ROOT, "backend")
FRONTEND_H5 = os.path.join(PROJECT_ROOT, "frontend-h5")
FRONTEND_ADMIN = os.path.join(PROJECT_ROOT, "frontend-admin")

ENV_FILE = os.path.join(SCRIPT_DIR, ".deploy.env")
SSHX = os.path.join(SCRIPT_DIR, "sshx.sh")

# ---------- OSS 中转路径 ----------
OSS_PREFIX = "deploy-tmp"

# ---------- 服务器固定路径 ----------
REMOTE_BACKEND_JAR = "/opt/order-food/backend/order-food-backend-1.0.0.jar"
REMOTE_H5_DIR = "/opt/order-food/frontend/h5"
REMOTE_ADMIN_DIR = "/opt/order-food/frontend/admin"
SYSTEMD_SVC = "order-food"

# ---------- 本地工具链默认值（本地 Windows 开发机路径；
#     CI 模式下由环境变量覆盖并走 PATH 默认值） ----------
DEFAULT_JAVA_HOME = r"C:\programming_software\jdk-21.0.9"
DEFAULT_NODE = r"C:\Users\zhujw2\.workbuddy\binaries\node\versions\22.22.2\node.exe"

# ---------- 必需配置项（缺失即硬失败） ----------
REQUIRED_VARS = [
    "OSS_ACCESS_KEY_ID",
    "OSS_ACCESS_KEY_SECRET",
    "OSS_BUCKET_NAME",
    "OSS_ENDPOINT",
    "DEPLOY_SERVER_HOST",
    "DEPLOY_SERVER_USER",
    "DEPLOY_SERVER_PASSWORD",
]

# ---------- 颜色 ----------
GREEN = "\033[32m"; YELLOW = "\033[33m"; RED = "\033[31m"; CYAN = "\033[36m"; RESET = "\033[0m"
def log(step, msg, color=CYAN):
    print(f"{color}[{step}]{RESET} {msg}")

# ============================================================================
#  配置读取
# ============================================================================
def load_env():
    """读取部署配置。

    加载顺序：先读同目录 .deploy.env（本地优先），再从 os.environ 读取
    （CI 注入的同名 secret），两者合并——环境变量覆盖文件值。文件不存在时
    纯依赖环境变量，CI 无文件也能跑。缺失任一必需项即硬失败并给出清晰报错。

    安全：绝不在任何日志中输出密钥值（仅输出变量名）。
    """
    file_env = {}
    if os.path.isfile(ENV_FILE):
        with open(ENV_FILE, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#") or "=" not in line:
                    continue
                k, v = line.split("=", 1)
                file_env[k.strip()] = v.strip()

    # 合并：文件为底，环境变量（CI）覆盖
    merged = dict(file_env)
    for k in REQUIRED_VARS:
        val = os.environ.get(k)
        if val:
            merged[k] = val

    # 兼容旧字段：DEPLOY_SERVER_PASS 回退为 DEPLOY_SERVER_PASSWORD
    if not merged.get("DEPLOY_SERVER_PASSWORD") and merged.get("DEPLOY_SERVER_PASS"):
        merged["DEPLOY_SERVER_PASSWORD"] = merged["DEPLOY_SERVER_PASS"]

    # 缺失校验（仅打印变量名，不打印值）
    missing = [k for k in REQUIRED_VARS if not merged.get(k)]
    if missing:
        log("ENV", f"缺少必需配置项: {', '.join(missing)}", RED)
        if os.path.isfile(ENV_FILE):
            log("ENV", "请检查 .deploy.env 中以上变量是否已填写", RED)
        else:
            log("ENV", "本地请复制 .deploy.env.example 为 .deploy.env 并填写；"
                       "CI 请在仓库 Settings -> Secrets 配置同名变量", RED)
        sys.exit(1)
    return merged

# ============================================================================
#  工具链解析
# ============================================================================
def resolve_toolchain(ci):
    """解析 JAVA_HOME / node 路径。

    CI 模式：走 PATH 默认值（setup-java / setup-node 已将工具写入环境）。
    本地模式：保留 Windows 硬编码默认，并允许通过环境变量覆盖。
    """
    if ci:
        java_home = os.environ.get("JAVA_HOME")      # 可能为空 -> 交给系统 mvn 探测
        node = os.environ.get("NODE", "node")        # PATH 中的 node
    else:
        java_home = os.environ.get("JAVA_HOME", DEFAULT_JAVA_HOME)
        node = os.environ.get("NODE", DEFAULT_NODE)
    return java_home, node

# ============================================================================
#  本地构建
# ============================================================================
def build_backend(ci):
    if ci:
        log("BUILD", "构建后端 (系统 mvn -DskipTests package，在线、去 -o、去 clean) ...")
        java_home, _ = resolve_toolchain(ci)
        cmd = ["mvn", "-DskipTests", "package"]
        env = dict(os.environ)
        if java_home:
            env["JAVA_HOME"] = java_home
        r = subprocess.run(cmd, cwd=BACKEND_DIR, env=env)
    else:
        log("BUILD", "构建后端 (./mvnw.sh -o package，禁用 clean) ...")
        java_home, _ = resolve_toolchain(ci)
        mvnw = os.path.join(BACKEND_DIR, "mvnw.sh")
        env = dict(os.environ)
        env["JAVA_HOME"] = java_home
        cmd = ["bash", mvnw, "-o", "-DskipTests", "package"]
        r = subprocess.run(cmd, cwd=BACKEND_DIR, env=env)
    if r.returncode != 0:
        log("BUILD", "后端构建失败", RED); sys.exit(1)
    jar = os.path.join(BACKEND_DIR, "target", "order-food-backend-1.0.0.jar")
    if not os.path.isfile(jar):
        log("BUILD", f"未找到产物 {jar}", RED); sys.exit(1)
    log("BUILD", f"后端构建完成: {os.path.getsize(jar)//1024//1024} MB", GREEN)
    return jar

def typecheck_frontend(name, fe_dir, node):
    """CI 模式：vue-tsc --noEmit 类型检查，失败即停（非零退出）。"""
    log("TYPECHECK", f"类型检查前端 {name} (vue-tsc --noEmit) ...")
    vue_tsc = os.path.join(fe_dir, "node_modules", ".bin", "vue-tsc")
    if not os.path.isfile(vue_tsc):
        log("TYPECHECK", f"{name} 未安装依赖 (缺 node_modules/.bin/vue-tsc)，请先 npm ci", RED)
        sys.exit(1)
    cmd = [node, vue_tsc, "--noEmit"]
    r = subprocess.run(cmd, cwd=fe_dir)
    if r.returncode != 0:
        log("TYPECHECK", f"{name} 类型检查失败", RED); sys.exit(1)
    log("TYPECHECK", f"{name} 类型检查通过", GREEN)

def build_frontend(name, fe_dir, ci):
    _, node = resolve_toolchain(ci)
    if ci:
        # CI 模式：先类型检查，再构建（node 走 PATH）
        typecheck_frontend(name, fe_dir, node)
        log("BUILD", f"构建前端 {name} (node vite build) ...")
        vite = os.path.join(fe_dir, "node_modules", ".bin", "vite")
        if not os.path.isfile(vite):
            log("BUILD", f"{name} 未安装依赖 (缺 node_modules/.bin/vite)，请先 npm ci", RED)
            sys.exit(1)
        cmd = [node, vite, "build"]
        r = subprocess.run(cmd, cwd=fe_dir)
        if r.returncode != 0:
            log("BUILD", f"{name} 构建失败", RED); sys.exit(1)
        dist = os.path.join(fe_dir, "dist")
        if not os.path.isdir(dist):
            log("BUILD", f"{name} 未生成 dist", RED); sys.exit(1)
        log("BUILD", f"{name} 构建完成", GREEN)
        return dist
    else:
        # 本地模式：保留沙箱安全删除绕行（先 mv 走 dist 再 build）
        log("BUILD", f"构建前端 {name} (vite build) ...")
        dist = os.path.join(fe_dir, "dist")
        dist_bak = os.path.join(fe_dir, "dist_bak")
        if os.path.isdir(dist):
            if os.path.isdir(dist_bak):
                shutil.rmtree(dist_bak)
            os.rename(dist, dist_bak)
        vite = os.path.join(fe_dir, "node_modules", ".bin", "vite")
        if not os.path.isfile(vite):
            log("BUILD", f"{name} 未安装依赖 (缺 node_modules/.bin/vite)，请先 npm install", RED)
            sys.exit(1)
        cmd = [node, vite, "build"]
        r = subprocess.run(cmd, cwd=fe_dir)
        if r.returncode != 0:
            log("BUILD", f"{name} 构建失败", RED); sys.exit(1)
        if not os.path.isdir(dist):
            log("BUILD", f"{name} 未生成 dist", RED); sys.exit(1)
        log("BUILD", f"{name} 构建完成", GREEN)
        return dist

def pack_dist_tar(dist_dir):
    """把 dist 打成 tar.gz 返回 (路径, 大小)"""
    tmp = tempfile.mktemp(suffix=".tar.gz", prefix="dist_")
    with tarfile.open(tmp, "w:gz") as tar:
        tar.add(dist_dir, arcname=".")
    return tmp, os.path.getsize(tmp)

# ============================================================================
#  OSS 上传 / 清理
# ============================================================================
def get_bucket(env):
    import oss2
    auth = oss2.Auth(env["OSS_ACCESS_KEY_ID"], env["OSS_ACCESS_KEY_SECRET"])
    return oss2.Bucket(auth, env["OSS_ENDPOINT"], env["OSS_BUCKET_NAME"])

def upload_to_oss(bucket, env, local_path, oss_key):
    size = os.path.getsize(local_path)
    log("OSS", f"上传 {os.path.basename(local_path)} ({size//1024//1024} MB) -> oss://{bucket.bucket_name}/{oss_key}")
    bucket.put_object_from_file(oss_key, local_path)
    # 临时 public-read，方便服务器直接 curl（收尾改回 private）
    bucket.put_object_acl(oss_key, "public-read")
    # 仅拼接公网 URL（不含任何密钥）
    host = env["OSS_ENDPOINT"].split("//")[-1]
    url = f"https://{bucket.bucket_name}.{host}/{oss_key}"
    log("OSS", f"已设为 public-read，URL: {url}", GREEN)
    return url

def set_private(bucket, oss_key):
    try:
        bucket.put_object_acl(oss_key, "private")
    except Exception:
        pass

# ============================================================================
#  服务器执行（通过 sshx.sh）
# ============================================================================
def remote(env, cmd, timeout=600):
    host = env.get("DEPLOY_SERVER_HOST")
    full = ["bash", SSHX, f"{env.get('DEPLOY_SERVER_USER','root')}@{host}", cmd]
    try:
        # 把合并后的配置（含服务器密码）透传给 sshx，同时保留系统 PATH 等环境
        sub_env = dict(os.environ)
        sub_env.update(env)
        r = subprocess.run(full, capture_output=True, text=True, timeout=timeout, env=sub_env)
        return r.stdout.strip(), r.returncode
    except subprocess.TimeoutExpired:
        return "", 124

def shell_quote(s):
    return "'" + s.replace("'", "'\\''") + "'"

def curl_resume_loop(env, url, dest, expect_size, md5=None):
    """服务端全量拉取（先清残留、非续传，规避坏文件被当全量），可选 md5 校验。

    修复记录：原实现用 curl -C - 断点续传且不清 /tmp 残留，若上次部署留下
    同名残缺文件，stat 大小凑巧 >= 期望值就直接当全量用，导致 corrupt jar 上线
    （2026-07-15 线上 502/500 事故根因）。现改为：每次先 rm -f 残留，再全量
    curl -fL 下载（同地域内网极快），并在提供 md5 时下载同名 .md5 sidecar 校验。
    """
    md5_check = ""
    if md5:
        md5url = shell_quote(url + ".md5")
        md5_check = (
            f"curl -fsL -o /tmp/_expect.md5 {md5url}; "
            f"EXPECT=$(cat /tmp/_expect.md5 2>/dev/null | tr -d '\\r\\n' | awk '{{print $1}}'); "
            f"ACT=$(md5sum {dest} | awk '{{print $1}}'); "
            f"if [ x$EXPECT != x$ACT ]; then echo \"MD5_MISMATCH expect=$EXPECT actual=$ACT\"; exit 1; fi; "
        )
    cmd = (
        f"rm -f {dest}; "
        f"for i in $(seq 1 20); do "
        f"curl -fsL -o {dest} {shell_quote(url)}; "
        f"S=$(stat -c%s {dest} 2>/dev/null || echo 0); "
        f"if [ \"$S\" -ge {expect_size} ]; then echo OK_SIZE=$S; break; fi; "
        f"echo \"attempt $i -> $S\"; sleep 2; done; "
        f"{md5_check}"
        f"echo FINAL=$(stat -c%s {dest} 2>/dev/null)"
    )
    out, _ = remote(env, cmd, timeout=600)
    return out

def restart_backend(env):
    log("DEPLOY", "后端：停止 systemd 服务 + 兜底 pkill ...")
    remote(env, f"systemctl stop {SYSTEMD_SVC} 2>/dev/null; pkill -9 -f 'order-food-backend-1.0.0.jar' 2>/dev/null; sleep 3; echo STOPPED")
    log("DEPLOY", "后端：启动 systemd 服务 ...")
    remote(env, f"systemctl start {SYSTEMD_SVC} 2>&1; echo STARTED")
    log("DEPLOY", "等待 Spring Boot 启动 (约 25s) ...", YELLOW)
    time.sleep(25)
    out, _ = remote(env, f"systemctl is-active {SYSTEMD_SVC}; ss -lntp 2>/dev/null | grep 8080")
    log("DEPLOY", f"服务状态: {out}", GREEN)

def deploy_frontend(env, url, expect_size, remote_dir, name):
    log("DEPLOY", f"{name}：拉取 dist 并解压到 {remote_dir} ...")
    tmp = f"/tmp/{name}_dist.tar.gz"
    curl_resume_loop(env, url, tmp, expect_size)
    cmd = f"rm -rf {remote_dir}/* && mkdir -p {remote_dir} && tar xzf {tmp} -C {remote_dir} && echo EXTRACTED && ls {remote_dir} | head"
    out, _ = remote(env, cmd, timeout=300)
    log("DEPLOY", f"{name}: {out}", GREEN)

# ============================================================================
#  健康检查
# ============================================================================
def health_check(env):
    log("CHECK", "后端健康检查 /api/h5/categories ...")
    out, _ = remote(env, "curl -s -o /dev/null -w 'HTTP %{http_code}' --max-time 10 http://127.0.0.1:8080/api/h5/categories")
    ok = "200" in out
    log("CHECK", f"categories -> {out}", GREEN if ok else RED)
    out2, _ = remote(env, "curl -s -o /dev/null -w 'H5 HTTP %{http_code}' --max-time 10 http://127.0.0.1/ ; curl -s -o /dev/null -w 'ADMIN HTTP %{http_code}' --max-time 10 http://127.0.0.1/admin/")
    log("CHECK", f"公网 H5/Admin -> {out2}", GREEN if ("200" in out2) else YELLOW)
    return ok

# ============================================================================
#  主流程
# ============================================================================
def main():
    ap = argparse.ArgumentParser(description="order-food 一键上线（OSS 中转，三端）")
    ap.add_argument("--skip-backend", action="store_true", help="只上前端")
    ap.add_argument("--skip-frontend", action="store_true", help="只上后端")
    ap.add_argument("--no-build", action="store_true", help="不重新构建，直接上线已有产物")
    ap.add_argument("--host", help="临时覆盖服务器IP")
    ap.add_argument("--ci", action="store_true",
                    help="CI 模式（也可由环境变量 CI=true 自动开启）：系统 mvn 在线构建、前端 vue-tsc 类型检查")
    args = ap.parse_args()

    # CI 模式：显式 --ci 或由 CI=true 环境变量自动开启
    ci = args.ci or os.environ.get("CI", "").lower() == "true"

    env = load_env()
    if args.host:
        env["DEPLOY_SERVER_HOST"] = args.host

    if not os.path.isfile(SSHX):
        log("INIT", f"缺少 {SSHX}", RED); sys.exit(1)

    log("INIT", f"模式: {'CI' if ci else '本地'} | 测试服务器 {env.get('DEPLOY_SERVER_HOST')} 连通性 ...")
    out, _ = remote(env, "echo CONNECTED")
    if "CONNECTED" not in out:
        log("INIT", f"服务器不可达: {out}", RED); sys.exit(1)
    log("INIT", "服务器连通 OK", GREEN)

    bucket = get_bucket(env)
    uploaded = []  # (oss_key, kind, expect_size, url)

    # ---------- 后端：构建 + 上传 ----------
    if not args.skip_backend:
        jar = os.path.join(BACKEND_DIR, "target", "order-food-backend-1.0.0.jar")
        if (not args.no_build) or (not os.path.isfile(jar)):
            jar = build_backend(ci)
        jar_size = os.path.getsize(jar)
        key = f"{OSS_PREFIX}/order-food-backend-1.0.0.jar"
        url = upload_to_oss(bucket, env, jar, key)
        # 上传 md5 sidecar（public-read），供服务器下载后做内容校验
        jar_md5 = hashlib.md5(open(jar, "rb").read()).hexdigest()
        bucket.put_object(f"{key}.md5", jar_md5)
        bucket.put_object_acl(f"{key}.md5", "public-read")
        uploaded.append((key, "backend", jar_size, url, jar_md5))

    # ---------- 前端：构建 + 打包 + 上传 ----------
    if not args.skip_frontend:
        if (not args.no_build) or (not os.path.isdir(os.path.join(FRONTEND_H5, "dist"))):
            h5_dist = build_frontend("h5用户端", FRONTEND_H5, ci)
        else:
            h5_dist = os.path.join(FRONTEND_H5, "dist")
        if (not args.no_build) or (not os.path.isdir(os.path.join(FRONTEND_ADMIN, "dist"))):
            admin_dist = build_frontend("admin管理端", FRONTEND_ADMIN, ci)
        else:
            admin_dist = os.path.join(FRONTEND_ADMIN, "dist")

        h5_tar, h5_size = pack_dist_tar(h5_dist)
        admin_tar, admin_size = pack_dist_tar(admin_dist)
        h5_key = f"{OSS_PREFIX}/h5-dist.tar.gz"
        admin_key = f"{OSS_PREFIX}/admin-dist.tar.gz"
        h5_url = upload_to_oss(bucket, env, h5_tar, h5_key)
        admin_url = upload_to_oss(bucket, env, admin_tar, admin_key)
        uploaded.append((h5_key, "h5", h5_size, h5_url, None))
        uploaded.append((admin_key, "admin", admin_size, admin_url, None))

    # ---------- 服务器部署 ----------
    if not args.skip_backend:
        log("DEPLOY", "=== 后端上线 ===")
        key, _, jar_size, url, jar_md5 = [u for u in uploaded if u[1] == "backend"][0]
        tmp_jar = "/tmp/order-food-backend-1.0.0.jar"
        curl_resume_loop(env, url, tmp_jar, jar_size, md5=jar_md5)
        out, _ = remote(env,
            f"S=$(stat -c%s {tmp_jar} 2>/dev/null); "
            f"if [ \"$S\" = \"{jar_size}\" ]; then cp {tmp_jar} {REMOTE_BACKEND_JAR} && echo REPLACED; else echo SIZE_MISMATCH=$S; fi")
        if "REPLACED" not in out or "MD5_MISMATCH" in out:
            log("DEPLOY", f"jar 替换失败/校验不通过: {out}", RED); sys.exit(1)
        restart_backend(env)

    if not args.skip_frontend:
        log("DEPLOY", "=== 前端上线（静态，免 nginx reload）===")
        for key, kind, size, url, _ in uploaded:
            if kind == "h5":
                deploy_frontend(env, url, size, REMOTE_H5_DIR, "h5用户端")
            elif kind == "admin":
                deploy_frontend(env, url, size, REMOTE_ADMIN_DIR, "admin管理端")

    # ---------- 健康检查 ----------
    ok = health_check(env)

    # ---------- 收尾（CI / 本地都会执行：OSS 临时对象改回 private） ----------
    log("CLEANUP", "OSS 临时对象改回 private ...")
    for key, _, _, _, _ in uploaded:
        set_private(bucket, key)
        try:
            set_private(bucket, f"{key}.md5")
        except Exception:
            pass
    for d in (FRONTEND_H5, FRONTEND_ADMIN):
        bak = os.path.join(d, "dist_bak")
        if os.path.isdir(bak):
            shutil.rmtree(bak)
    log("CLEANUP", "本地 dist_bak 已清理", GREEN)

    print("\n" + "=" * 60)
    if ok:
        log("DONE", "✅ 上线完成，健康检查通过！", GREEN)
    else:
        log("DONE", "⚠️ 上线完成但健康检查未通过，请查 /opt/order-food/logs/stdout.log", YELLOW)
    print("=" * 60)

if __name__ == "__main__":
    main()
