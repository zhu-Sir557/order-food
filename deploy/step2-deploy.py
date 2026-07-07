#!/usr/bin/env python3
"""第二步：上传JAR + 替换 + 重启 + 验证"""
import paramiko
import os
import sys
import time

HOST = os.environ.get("DEPLOY_SERVER_HOST", "")
USER = os.environ.get("DEPLOY_SERVER_USER", "root")
PASS = os.environ.get("DEPLOY_SERVER_PASS", "")

LOCAL_JAR = r"d:\source\workbuddy\repo1\order_food\backend\target\order-food-backend-1.0.0.jar"
REMOTE_JAR = "/opt/order-food/backend/order-food-backend-1.0.0.jar"
REMOTE_JAR_TMP = "/tmp/order-food-backend-1.0.0.jar"

def run_cmd(ssh, cmd, timeout=30):
    """执行命令并返回输出"""
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    exit_code = stdout.channel.recv_exit_status()
    return out, err, exit_code

def upload_progress(transferred, total):
    """SFTP上传进度回调"""
    pct = transferred / total * 100
    # 每10%打印一次
    if pct % 10 < 1 or transferred == total:
        print(f"\r  上传进度: {pct:.0f}% ({transferred // 1024 // 1024}MB / {total // 1024 // 1024}MB)", end="", flush=True)

def main():
    jar_size = os.path.getsize(LOCAL_JAR)
    print(f"JAR文件大小: {jar_size / 1024 / 1024:.1f} MB")

    print("\n连接服务器...")
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(HOST, port=22, username=USER, password=PASS, timeout=15)
    print("SSH 连接成功")

    # 1. 上传JAR到/tmp
    print(f"\n[1/4] 上传 JAR 到 /tmp ...")
    sftp = ssh.open_sftp()
    try:
        sftp.put(LOCAL_JAR, REMOTE_JAR_TMP, callback=upload_progress)
        print("\n  上传完成!")
        
        # 验证文件大小
        remote_stat = sftp.stat(REMOTE_JAR_TMP)
        print(f"  服务器端文件大小: {remote_stat.st_size / 1024 / 1024:.1f} MB")
        if remote_stat.st_size != jar_size:
            print(f"  [警告] 文件大小不匹配! 本地={jar_size} 远程={remote_stat.st_size}")
    except Exception as e:
        print(f"\n  上传失败: {e}")
        sftp.close()
        ssh.close()
        sys.exit(1)
    sftp.close()

    # 2. 停止服务 + 替换JAR
    print(f"\n[2/4] 停止服务并替换 JAR...")
    out, err, code = run_cmd(ssh, "systemctl stop order-food && echo 'stopped'")
    print(f"  停止服务: {out}")

    out, err, code = run_cmd(ssh, f"cp {REMOTE_JAR_TMP} {REMOTE_JAR} && echo 'copied'")
    print(f"  替换JAR: {out}")
    if err:
        print(f"  [错误] {err}")

    # 3. 启动服务
    print(f"\n[3/4] 启动服务...")
    out, err, code = run_cmd(ssh, "systemctl start order-food && echo 'started'")
    print(f"  启动: {out}")
    
    print("  等待8秒让Spring Boot启动...")
    time.sleep(8)

    out, err, code = run_cmd(ssh, "systemctl is-active order-food")
    print(f"  服务状态: {out}")

    if out != "active":
        print("  [错误] 服务未正常启动，查看日志:")
        out, err, code = run_cmd(ssh, "tail -30 /opt/order-food/logs/stderr.log 2>/dev/null")
        print(out)
        ssh.close()
        sys.exit(1)

    # 4. 验证接口
    print(f"\n[4/4] 验证接口...")
    print("-" * 50)

    tests = [
        ("H5分类接口", "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/h5/categories"),
        ("H5注册", """curl -s -X POST http://localhost:8080/api/h5/member/register -H 'Content-Type: application/json' -d '{"username":"autotest003","password":"123456"}'"""),
        ("H5登录", """curl -s -X POST http://localhost:8080/api/h5/member/login -H 'Content-Type: application/json' -d '{"username":"autotest003","password":"123456","captchaToken":"skip"}'"""),
        ("Admin登录", """curl -s -X POST http://localhost:8080/api/admin/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}'"""),
        ("公网H5", "curl -s -o /dev/null -w '%{http_code}' http://localhost/"),
        ("公网Admin", "curl -s -o /dev/null -w '%{http_code}' http://localhost/admin/"),
    ]

    all_pass = True
    for name, cmd in tests:
        out, err, code = run_cmd(ssh, cmd, timeout=15)
        # 截取前200字符显示
        display = out[:200] if len(out) > 200 else out
        print(f"  {name}: {display}")
        if "500" in out or "error" in out.lower():
            all_pass = False
        time.sleep(0.3)

    print("\n" + "=" * 50)
    if all_pass:
        print("  ✅ 全部验证通过!")
    else:
        print("  ⚠️ 部分接口异常，请检查上方输出")
    print("=" * 50)

    # 清理临时文件
    run_cmd(ssh, f"rm -f {REMOTE_JAR_TMP}")

    ssh.close()
    print("\n部署完成，连接已关闭")

if __name__ == "__main__":
    main()
