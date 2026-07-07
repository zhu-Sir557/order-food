#!/usr/bin/env python3
"""远程部署：上传修复包 + 执行脚本 + 返回结果"""
import paramiko
import os
import sys
import time

HOST = os.environ.get("DEPLOY_SERVER_HOST", "")
USER = os.environ.get("DEPLOY_SERVER_USER", "root")
PASS = os.environ.get("DEPLOY_SERVER_PASS", "")

LOCAL_TAR = r"d:\source\workbuddy\repo1\order_food\order-food-fix500.tar.gz"
LOCAL_SH  = r"d:\source\workbuddy\repo1\order_food\deploy\fix-500.sh"
REMOTE_TAR = "/root/order-food-fix500.tar.gz"
REMOTE_SH  = "/root/fix-500.sh"

def main():
    print("=" * 50)
    print("  远程部署开始")
    print("=" * 50)

    # 1. 连接服务器
    print("\n[1/5] 连接服务器...")
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    try:
        ssh.connect(HOST, port=22, username=USER, password=PASS, timeout=15)
        print("  SSH 连接成功")
    except Exception as e:
        print(f"  SSH 连接失败: {e}")
        sys.exit(1)

    # 2. 先检查服务器当前状态
    print("\n[2/5] 检查服务器状态...")
    checks = [
        "echo '--- .credentials ---'; cat /opt/order-food/.credentials 2>/dev/null || echo '文件不存在'",
        "echo '--- 配置占位符检查 ---'; grep -c '__JWT_SECRET__\\|__DB_PASSWORD__' /opt/order-food/config/application-prod.yml 2>/dev/null || echo '0'",
        "echo '--- 后端服务状态 ---'; systemctl is-active order-food 2>/dev/null || echo 'unknown'",
        "echo '--- Nginx状态 ---'; systemctl is-active nginx 2>/dev/null || echo 'unknown'",
    ]
    for cmd in checks:
        stdin, stdout, stderr = ssh.exec_command(cmd)
        out = stdout.read().decode()
        err = stderr.read().decode()
        print(out.strip())
        if err.strip():
            print(f"  stderr: {err.strip()}")

    # 3. 上传文件
    print(f"\n[3/5] 上传文件...")
    sftp = ssh.open_sftp()

    tar_size = os.path.getsize(LOCAL_TAR)
    print(f"  上传 order-food-fix500.tar.gz ({tar_size / 1024 / 1024:.1f} MB)...")
    sftp.put(LOCAL_TAR, REMOTE_TAR)
    print(f"  上传完成")

    sh_size = os.path.getsize(LOCAL_SH)
    print(f"  上传 fix-500.sh ({sh_size} bytes)...")
    sftp.put(LOCAL_SH, REMOTE_SH)
    print(f"  上传完成")

    sftp.close()

    # 4. 执行修复脚本
    print(f"\n[4/5] 执行修复脚本...")
    print("-" * 50)

    # 先检查是否需要 MySQL 密码（如果配置文件里有占位符且 .credentials 里没有密码）
    # 如果需要，从 .credentials 读取并传给脚本
    # fix-500.sh 里的 read -r MYSQL_PWD 会从 stdin 读取
    stdin, stdout, stderr = ssh.exec_command("grep -c '__DB_PASSWORD__' /opt/order-food/config/application-prod.yml 2>/dev/null || echo 0")
    need_db = stdout.read().decode().strip()

    if need_db and need_db != "0":
        # 需要密码，从 .credentials 读取
        stdin, stdout, stderr = ssh.exec_command("awk -F': ' '/[Mm]y[Ss][Qq][Ll]/{print $2}' /opt/order-food/.credentials 2>/dev/null | head -1 | tr -d '[:space:]'")
        mysql_pwd = stdout.read().decode().strip()

        if mysql_pwd and mysql_pwd != "__TODO__":
            print(f"  检测到需要 MySQL 密码，从 .credentials 读取到密码，将自动传入")
            # 通过管道传入密码
            cmd = f"chmod +x {REMOTE_SH} && echo '{mysql_pwd}' | bash {REMOTE_SH}"
        else:
            print(f"  [警告] 需要 MySQL 密码但 .credentials 中未找到，脚本可能会提示输入")
            cmd = f"chmod +x {REMOTE_SH} && bash {REMOTE_SH}"
    else:
        print(f"  配置文件占位符已替换，直接执行")
        cmd = f"chmod +x {REMOTE_SH} && bash {REMOTE_SH}"

    # 执行脚本，设置较长超时
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=120)

    # 实时读取输出
    while True:
        line = stdout.readline()
        if not line:
            break
        print(f"  {line.rstrip()}")

    err_output = stderr.read().decode()
    if err_output.strip():
        print(f"\n  [stderr]: {err_output.strip()}")

    exit_code = stdout.channel.recv_exit_status()
    print(f"\n  脚本退出码: {exit_code}")

    # 5. 额外验证
    print(f"\n[5/5] 额外验证...")
    print("-" * 50)

    verify_cmds = [
        ("后端服务状态", "systemctl is-active order-food"),
        ("Nginx状态", "systemctl is-active nginx"),
        ("H5分类接口", "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/h5/categories"),
        ("H5注册测试", """curl -s -X POST http://localhost:8080/api/h5/member/register -H 'Content-Type: application/json' -d '{"username":"autotest002","password":"123456"}'"""),
        ("Admin登录测试", """curl -s -X POST http://localhost:8080/api/admin/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}'"""),
        ("公网H5", "curl -s -o /dev/null -w '%{http_code}' http://localhost/"),
        ("公网Admin", "curl -s -o /dev/null -w '%{http_code}' http://localhost/admin/"),
    ]

    for name, cmd in verify_cmds:
        stdin, stdout, stderr = ssh.exec_command(cmd, timeout=15)
        out = stdout.read().decode().strip()
        err = stderr.read().decode().strip()
        if out:
            print(f"  {name}: {out}")
        if err.strip() and not out:
            print(f"  {name}: [ERROR] {err.strip()}")
        time.sleep(0.5)

    print("\n" + "=" * 50)
    print("  部署完成")
    print("=" * 50)

    ssh.close()

if __name__ == "__main__":
    main()
