#!/usr/bin/env python3
"""
通过OSS中转上传JAR：本地→OSS→服务器下载
都在阿里云上海region，速度极快
"""
import paramiko
import oss2
import sys
import time
import os

# === 服务器（从环境变量读取，请勿硬编码） ===
HOST = os.environ.get("DEPLOY_SERVER_HOST", "")
USER = os.environ.get("DEPLOY_SERVER_USER", "root")
PASS = os.environ.get("DEPLOY_SERVER_PASS", "")

# === OSS（从环境变量读取） ===
OSS_ENDPOINT = os.environ.get("OSS_ENDPOINT", "https://oss-cn-shanghai.aliyuncs.com")
OSS_AK = os.environ.get("OSS_ACCESS_KEY_ID", "")
OSS_SK = os.environ.get("OSS_ACCESS_KEY_SECRET", "")
OSS_BUCKET = os.environ.get("OSS_BUCKET_NAME", "")

# === JAR ===
LOCAL_JAR = r"d:\source\workbuddy\repo1\order_food\backend\target\order-food-backend-1.0.0.jar"
OSS_KEY = "deploy/order-food-backend-1.0.0.jar"
REMOTE_JAR = "/opt/order-food/backend/order-food-backend-1.0.0.jar"
REMOTE_TMP = "/tmp/order-food-backend-1.0.0.jar"

def run_cmd(ssh, cmd, timeout=30):
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    exit_code = stdout.channel.recv_exit_status()
    return out, err, exit_code

def upload_progress(transferred, total):
    pct = transferred / total * 100
    print(f"\r  OSS上传: {pct:.0f}% ({transferred // 1024 // 1024}MB / {total // 1024 // 1024}MB)", end="", flush=True)

def main():
    jar_size = os.path.getsize(LOCAL_JAR)
    print(f"JAR文件大小: {jar_size / 1024 / 1024:.1f} MB")

    # 1. 上传JAR到OSS
    print("\n[1/5] 上传 JAR 到 OSS...")
    auth = oss2.AuthProvider(OSS_AK, OSS_SK) if hasattr(oss2, 'AuthProvider') else oss2.Auth(OSS_AK, OSS_SK)
    bucket = oss2.Bucket(auth, OSS_ENDPOINT, OSS_BUCKET)
    
    try:
        bucket.put_object_from_file(OSS_KEY, LOCAL_JAR, progress_callback=upload_progress)
        print("\n  OSS上传完成!")
    except Exception as e:
        print(f"\n  OSS上传失败: {e}")
        sys.exit(1)

    # 构造OSS公网URL
    oss_url = f"https://{OSS_BUCKET}.oss-cn-shanghai.aliyuncs.com/{OSS_KEY}"
    print(f"  OSS URL: {oss_url}")

    # 2. 连接服务器
    print("\n[2/5] 连接服务器...")
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(HOST, port=22, username=USER, password=PASS, timeout=15)
    print("  SSH 连接成功")

    # 3. 从OSS下载JAR到服务器
    print("\n[3/5] 服务器从OSS下载 JAR...")
    out, err, code = run_cmd(ssh, f"curl -sL -o {REMOTE_TMP} '{oss_url}' && ls -lh {REMOTE_TMP}", timeout=120)
    print(f"  下载结果: {out}")
    if err:
        print(f"  stderr: {err}")
    
    # 验证文件大小
    out, err, code = run_cmd(ssh, f"stat -c%s {REMOTE_TMP}")
    remote_size = int(out) if out else 0
    print(f"  服务器端文件大小: {remote_size / 1024 / 1024:.1f} MB (本地: {jar_size / 1024 / 1024:.1f} MB)")
    
    if remote_size != jar_size:
        print(f"  [错误] 文件大小不匹配!")
        ssh.close()
        sys.exit(1)
    print("  文件大小一致 ✓")

    # 4. 停止服务 + 替换JAR + 启动
    print("\n[4/5] 替换JAR并重启服务...")
    out, _, _ = run_cmd(ssh, "systemctl stop order-food && echo 'stopped'")
    print(f"  停止服务: {out}")
    
    out, err, code = run_cmd(ssh, f"cp {REMOTE_TMP} {REMOTE_JAR} && echo 'replaced'")
    print(f"  替换JAR: {out}")
    if err:
        print(f"  [错误] {err}")
    
    out, _, _ = run_cmd(ssh, "systemctl start order-food && echo 'started'")
    print(f"  启动服务: {out}")
    
    print("  等待8秒让Spring Boot启动...")
    time.sleep(8)
    
    out, _, _ = run_cmd(ssh, "systemctl is-active order-food")
    print(f"  服务状态: {out}")
    
    if out != "active":
        print("  [错误] 服务未正常启动，查看日志:")
        out, _, _ = run_cmd(ssh, "tail -30 /opt/order-food/logs/stderr.log 2>/dev/null")
        print(out)
        ssh.close()
        sys.exit(1)

    # 5. 验证接口
    print("\n[5/5] 验证接口...")
    print("-" * 50)
    
    tests = [
        ("H5分类接口", "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/h5/categories"),
        ("H5注册", """curl -s -X POST http://localhost:8080/api/h5/member/register -H 'Content-Type: application/json' -d '{"username":"autotest005","password":"123456"}'"""),
        ("H5登录", """curl -s -X POST http://localhost:8080/api/h5/member/login -H 'Content-Type: application/json' -d '{"username":"autotest005","password":"123456","captchaToken":"skip"}'"""),
        ("Admin登录", """curl -s -X POST http://localhost:8080/api/admin/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}'"""),
        ("公网H5", "curl -s -o /dev/null -w '%{http_code}' http://localhost/"),
        ("公网Admin", "curl -s -o /dev/null -w '%{http_code}' http://localhost/admin/"),
    ]
    
    all_pass = True
    for name, cmd in tests:
        out, err, code = run_cmd(ssh, cmd, timeout=15)
        display = out[:300] if len(out) > 300 else out
        print(f"  {name}: {display}")
        if "500" in out or "error" in out.lower():
            all_pass = False
        time.sleep(0.3)
    
    # 清理
    run_cmd(ssh, f"rm -f {REMOTE_TMP}")
    
    # 删除OSS上的临时文件
    try:
        bucket.delete_object(OSS_KEY)
        print("\n  OSS临时文件已清理")
    except:
        pass
    
    print("\n" + "=" * 50)
    if all_pass:
        print("  ✅ 全部验证通过! 部署成功!")
    else:
        print("  ⚠️ 部分接口异常，请检查上方输出")
    print("=" * 50)
    
    ssh.close()

if __name__ == "__main__":
    main()
