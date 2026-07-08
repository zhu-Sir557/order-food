#!/usr/bin/env python3
"""
============================================================
  智能点餐系统 - 统一发版脚本
============================================================
  用法：
    python deploy.py            # 全量部署（默认）
    python deploy.py backend    # 只部署后端
    python deploy.py frontend   # 只部署前端（H5 + Admin）
    python deploy.py all        # 全量部署

  首次使用前：
    1. 复制 .deploy.env.example 为 .deploy.env
    2. 填写服务器和 OSS 的连接信息
    3. 确保本地已安装 Maven、Node.js、Python paramiko + oss2
============================================================
"""
import os
import sys
import subprocess
import tarfile
import time
import shutil
from pathlib import Path

try:
    import paramiko
    import oss2
except ImportError:
    print("[ERROR] 缺少依赖库，请执行：")
    print("  pip install paramiko oss2")
    sys.exit(1)

# ============================================================
# 路径常量
# ============================================================
SCRIPT_DIR = Path(__file__).parent
PROJECT_ROOT = SCRIPT_DIR.parent
BACKEND_DIR = PROJECT_ROOT / "backend"
H5_DIR = PROJECT_ROOT / "frontend-h5"
ADMIN_DIR = PROJECT_ROOT / "frontend-admin"
ENV_FILE = SCRIPT_DIR / ".deploy.env"

# 本地构建产物
LOCAL_JAR = BACKEND_DIR / "target" / "order-food-backend-1.0.0.jar"
H5_DIST = H5_DIR / "dist"
ADMIN_DIST = ADMIN_DIR / "dist"

# 本地临时 tar.gz
TMP_DIR = SCRIPT_DIR / ".tmp"
H5_TAR = TMP_DIR / "h5-dist.tar.gz"
ADMIN_TAR = TMP_DIR / "admin-dist.tar.gz"

# 服务器路径
REMOTE_JAR = "/opt/order-food/backend/order-food-backend-1.0.0.jar"
REMOTE_H5_DIR = "/opt/order-food/frontend/h5/"
REMOTE_ADMIN_DIR = "/opt/order-food/frontend/admin/"
REMOTE_TMP = "/tmp"

# OSS 上传 key
OSS_JAR_KEY = "deploy/order-food-backend.jar"
OSS_H5_KEY = "deploy/h5-dist.tar.gz"
OSS_ADMIN_KEY = "deploy/admin-dist.tar.gz"

# 构建工具路径
MAVEN = r"C:\programming_software\apache-maven-3.9.11\bin\mvn.cmd"
NPM = r"C:\Users\zhujw2\.workbuddy\binaries\node\versions\22.22.2\npm.cmd"

# ============================================================
# 颜色输出
# ============================================================
GREEN = "\033[92m"
RED = "\033[91m"
YELLOW = "\033[93m"
CYAN = "\033[96m"
BOLD = "\033[1m"
NC = "\033[0m"


def info(msg):
    print(f"{GREEN}[INFO]{NC}  {msg}")


def warn(msg):
    print(f"{YELLOW}[WARN]{NC}  {msg}")


def error(msg):
    print(f"{RED}[ERROR]{NC} {msg}")


def step(title):
    print(f"\n{CYAN}{'=' * 20} {title} {'=' * 20}{NC}")


def ok(msg):
    print(f"{GREEN}  ✓ {msg}{NC}")


def fail(msg):
    print(f"{RED}  ✗ {msg}{NC}")


# ============================================================
# 配置加载
# ============================================================
def load_config():
    """从 .deploy.env 读取配置"""
    if not ENV_FILE.exists():
        error(f"配置文件不存在: {ENV_FILE}")
        print(f"\n请执行以下步骤：")
        print(f"  1. 复制模板: cp deploy/.deploy.env.example deploy/.deploy.env")
        print(f"  2. 编辑 .deploy.env 填写服务器和 OSS 信息")
        sys.exit(1)

    config = {}
    with open(ENV_FILE, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            if "=" in line:
                key, value = line.split("=", 1)
                config[key.strip()] = value.strip()

    required = [
        "DEPLOY_SERVER_HOST", "DEPLOY_SERVER_USER", "DEPLOY_SERVER_PASS",
        "OSS_ENDPOINT", "OSS_ACCESS_KEY_ID", "OSS_ACCESS_KEY_SECRET", "OSS_BUCKET_NAME",
    ]
    for key in required:
        if not config.get(key):
            error(f"配置项 {key} 为空，请检查 {ENV_FILE}")
            sys.exit(1)

    return config


# ============================================================
# OSS 工具
# ============================================================
def get_oss_bucket(config):
    auth = oss2.Auth(config["OSS_ACCESS_KEY_ID"], config["OSS_ACCESS_KEY_SECRET"])
    return oss2.Bucket(auth, config["OSS_ENDPOINT"], config["OSS_BUCKET_NAME"])


def oss_url(config, key):
    """构造 OSS 公网下载 URL"""
    bucket = config["OSS_BUCKET_NAME"]
    endpoint = config["OSS_ENDPOINT"].replace("https://", "").replace("http://", "")
    return f"https://{bucket}.{endpoint}/{key}"


def upload_progress(transferred, total):
    pct = transferred / total * 100
    print(f"\r  OSS 上传: {pct:.0f}% ({transferred // 1024 // 1024}MB / {total // 1024 // 1024}MB)", end="", flush=True)


def upload_to_oss(bucket, key, filepath):
    """上传文件到 OSS"""
    size = os.path.getsize(filepath)
    print(f"  上传 {Path(filepath).name} ({size / 1024 / 1024:.1f} MB) ...")
    bucket.put_object_from_file(key, filepath, progress_callback=upload_progress)
    print()  # 换行


def delete_oss(bucket, key):
    """删除 OSS 上的临时文件"""
    try:
        bucket.delete_object(key)
    except Exception:
        pass


# ============================================================
# SSH 工具
# ============================================================
def ssh_connect(config):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(
        config["DEPLOY_SERVER_HOST"],
        port=22,
        username=config["DEPLOY_SERVER_USER"],
        password=config["DEPLOY_SERVER_PASS"],
        timeout=15,
    )
    return ssh


def run_remote(ssh, cmd, timeout=120):
    """执行远程命令，返回 (stdout, stderr, exit_code)"""
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode("utf-8", errors="replace").strip()
    err = stderr.read().decode("utf-8", errors="replace").strip()
    code = stdout.channel.recv_exit_status()
    return out, err, code


# ============================================================
# 构建函数
# ============================================================
def build_backend():
    """Maven 打包后端"""
    step("构建后端 (Maven)")
    info(f"执行: mvn package -DskipTests")
    result = subprocess.run(
        [MAVEN, "package", "-DskipTests", "-q"],
        cwd=str(BACKEND_DIR),
        capture_output=True,
        text=True,
        shell=True,
    )
    if result.returncode != 0:
        fail("Maven 打包失败")
        print(result.stderr[-500:] if result.stderr else result.stdout[-500:])
        return False

    if not LOCAL_JAR.exists():
        fail(f"JAR 文件不存在: {LOCAL_JAR}")
        return False

    size_mb = LOCAL_JAR.stat().st_size / 1024 / 1024
    ok(f"打包成功: {LOCAL_JAR.name} ({size_mb:.1f} MB)")
    return True


def build_frontend(name, work_dir, dist_dir):
    """Vite 构建前端"""
    step(f"构建 {name} (Vite)")
    info(f"执行: npm run build (目录: {work_dir.name})")

    # 确认 node_modules 存在
    if not (work_dir / "node_modules").exists():
        fail(f"node_modules 不存在，请先在 {work_dir.name} 目录执行 npm install")
        return False

    result = subprocess.run(
        f'"{NPM}" run build',
        cwd=str(work_dir),
        capture_output=True,
        text=True,
        shell=True,
    )
    if result.returncode != 0:
        fail(f"{name} 构建失败")
        print(result.stderr[-800:] if result.stderr else result.stdout[-800:])
        return False

    if not dist_dir.exists():
        fail(f"构建产物目录不存在: {dist_dir}")
        return False

    file_count = sum(1 for _ in dist_dir.rglob("*") if _.is_file())
    ok(f"构建成功: {name} ({file_count} 个文件)")
    return True


def make_tar(dist_dir, tar_path):
    """将 dist 目录内容打包为 tar.gz"""
    tar_path.parent.mkdir(parents=True, exist_ok=True)
    with tarfile.open(str(tar_path), "w:gz") as tar:
        for item in dist_dir.iterdir():
            tar.add(str(item), arcname=item.name)
    size_kb = tar_path.stat().st_size / 1024
    ok(f"打包完成: {tar_path.name} ({size_kb:.0f} KB)")


# ============================================================
# 部署函数
# ============================================================
def deploy_backend(config, bucket):
    """部署后端 JAR"""
    step("部署后端到服务器")

    jar_size = LOCAL_JAR.stat().st_size
    url = oss_url(config, OSS_JAR_KEY)

    # 1. 上传 JAR 到 OSS
    info("上传 JAR 到 OSS ...")
    upload_to_oss(bucket, OSS_JAR_KEY, str(LOCAL_JAR))

    # 2. 连接服务器
    info("连接服务器 ...")
    ssh = ssh_connect(config)
    ok("SSH 连接成功")

    try:
        # 3. 从 OSS 下载
        info("服务器从 OSS 下载 JAR ...")
        remote_tmp_jar = f"{REMOTE_TMP}/order-food-backend.jar"
        out, err, code = run_remote(
            ssh, f"curl -sL -o {remote_tmp_jar} '{url}' && ls -lh {remote_tmp_jar}"
        )
        print(f"  {out}")

        # 验证文件大小
        out2, _, _ = run_remote(ssh, f"stat -c%s {remote_tmp_jar}")
        remote_size = int(out2) if out2 else 0
        if remote_size != jar_size:
            fail(f"文件大小不匹配 (本地 {jar_size} vs 远程 {remote_size})")
            return False
        ok("文件大小一致")

        # 4. 停止服务 + 替换 JAR + 启动
        info("停止后端服务 ...")
        run_remote(ssh, "systemctl stop order-food")

        info("替换 JAR 文件 ...")
        out, err, code = run_remote(ssh, f"cp {remote_tmp_jar} {REMOTE_JAR} && echo OK")
        if "OK" not in out:
            fail(f"替换失败: {err}")
            return False
        ok("JAR 已替换")

        info("启动后端服务 ...")
        run_remote(ssh, "systemctl start order-food")

        info("等待 Spring Boot 启动 (8 秒) ...")
        time.sleep(8)

        out, _, _ = run_remote(ssh, "systemctl is-active order-food")
        if out != "active":
            fail("服务未正常启动")
            out3, _, _ = run_remote(ssh, "tail -20 /opt/order-food/logs/stderr.log 2>/dev/null")
            print(out3)
            return False
        ok("后端服务运行中")

        # 清理
        run_remote(ssh, f"rm -f {remote_tmp_jar}")
        delete_oss(bucket, OSS_JAR_KEY)
        ok("临时文件已清理")

    finally:
        ssh.close()

    return True


def deploy_frontend_component(config, bucket, name, tar_path, remote_dir, oss_key):
    """部署单个前端组件"""
    url = oss_url(config, oss_key)

    # 1. 上传 tar.gz 到 OSS
    info(f"上传 {name} 到 OSS ...")
    upload_to_oss(bucket, oss_key, str(tar_path))

    # 2. 连接服务器
    ssh = ssh_connect(config)

    try:
        # 3. 下载
        remote_tmp_tar = f"{REMOTE_TMP}/{Path(tar_path).name}"
        info(f"服务器下载 {name} ...")
        out, err, code = run_remote(
            ssh, f"curl -sL -o {remote_tmp_tar} '{url}' && ls -lh {remote_tmp_tar}"
        )
        print(f"  {out}")

        # 4. 清空旧文件 + 解压
        info(f"替换 {name} 文件 ...")
        out, err, code = run_remote(
            ssh,
            f"rm -rf {remote_dir}/* && "
            f"mkdir -p {remote_dir} && "
            f"tar -xzf {remote_tmp_tar} -C {remote_dir} && "
            f"ls {remote_dir} | head -5 && echo EXTRACT_OK"
        )
        if "EXTRACT_OK" not in out:
            fail(f"解压失败: {err}")
            return False
        ok(f"{name} 文件已替换")

        # 清理
        run_remote(ssh, f"rm -f {remote_tmp_tar}")
        delete_oss(bucket, oss_key)

    finally:
        ssh.close()

    return True


def deploy_frontend(config, bucket):
    """部署前端（H5 + Admin）"""
    # 构建
    if not build_frontend("H5 用户端", H5_DIR, H5_DIST):
        return False
    if not build_frontend("Admin 管理端", ADMIN_DIR, ADMIN_DIST):
        return False

    # 打包
    step("打包前端构建产物")
    make_tar(H5_DIST, H5_TAR)
    make_tar(ADMIN_DIST, ADMIN_TAR)

    # 部署 H5
    if not deploy_frontend_component(config, bucket, "H5", H5_TAR, REMOTE_H5_DIR, OSS_H5_KEY):
        return False

    # 部署 Admin
    if not deploy_frontend_component(config, bucket, "Admin", ADMIN_TAR, REMOTE_ADMIN_DIR, OSS_ADMIN_KEY):
        return False

    # Nginx reload
    step("重载 Nginx")
    info("连接服务器重载 Nginx ...")
    ssh = ssh_connect(config)
    try:
        out, err, code = run_remote(ssh, "nginx -t 2>&1 && systemctl reload nginx && echo RELOAD_OK")
        if "RELOAD_OK" not in out:
            fail(f"Nginx reload 失败: {out}")
            return False
        ok("Nginx 已重载")
    finally:
        ssh.close()

    # 清理本地临时文件
    if TMP_DIR.exists():
        shutil.rmtree(TMP_DIR)

    return True


# ============================================================
# 验证函数
# ============================================================
def verify(config):
    """验证部署结果"""
    step("验证部署结果")

    ssh = ssh_connect(config)
    all_pass = True

    try:
        checks = [
            ("后端服务状态", "systemctl is-active order-food", "active"),
            ("H5 API (/api/h5/categories)", "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/h5/categories", "200"),
            ("公网 H5 页面", "curl -s -o /dev/null -w '%{http_code}' http://localhost/", "200"),
            ("公网 Admin 页面", "curl -s -o /dev/null -w '%{http_code}' http://localhost/admin/", "200"),
        ]

        for name, cmd, expected in checks:
            out, _, _ = run_remote(ssh, cmd, timeout=15)
            status = "PASS" if expected in out else "FAIL"
            icon = "✓" if status == "PASS" else "✗"
            color = GREEN if status == "PASS" else RED
            print(f"  {color}{icon}{NC} {name}: {out}", end="")
            if status == "FAIL":
                print(f" {color}(期望: {expected}){NC}")
                all_pass = False
            else:
                print()
            time.sleep(0.3)

    finally:
        ssh.close()

    print()
    if all_pass:
        print(f"{GREEN}{'=' * 50}")
        print(f"  ✅ 全部验证通过! 发版成功!")
        print(f"{'=' * 50}{NC}")
    else:
        print(f"{YELLOW}{'=' * 50}")
        print(f"  ⚠️  部分检查未通过，请查看上方输出")
        print(f"{'=' * 50}{NC}")

    return all_pass


# ============================================================
# 主流程
# ============================================================
def main():
    print(f"\n{CYAN}{'━' * 55}")
    print(f"  智能点餐系统 - 统一发版脚本")
    print(f"{'━' * 55}{NC}\n")

    # 解析参数
    mode = sys.argv[1] if len(sys.argv) > 1 else "all"
    if mode not in ("backend", "frontend", "all"):
        error(f"未知模式: {mode}")
        print(f"用法: python deploy.py [backend|frontend|all]")
        sys.exit(1)

    print(f"  部署模式: {BOLD}{mode}{NC}")
    print(f"  项目路径: {PROJECT_ROOT}")

    # 加载配置
    config = load_config()
    ok(f"配置已加载: {ENV_FILE.name}")

    # 获取 OSS bucket
    bucket = get_oss_bucket(config)

    # 执行部署
    success = True

    if mode in ("backend", "all"):
        if not build_backend():
            sys.exit(1)
        if not deploy_backend(config, bucket):
            sys.exit(1)

    if mode in ("frontend", "all"):
        if not deploy_frontend(config, bucket):
            sys.exit(1)

    # 验证
    verify(config)


if __name__ == "__main__":
    main()
