#!/usr/bin/env python3
"""验证部署结果"""
import paramiko
import os
import time

HOST = os.environ.get("DEPLOY_SERVER_HOST", "")
USER = os.environ.get("DEPLOY_SERVER_USER", "root")
PASS = os.environ.get("DEPLOY_SERVER_PASS", "")

def run_cmd(ssh, cmd, timeout=15):
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    return out, err

def main():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(HOST, port=22, username=USER, password=PASS, timeout=15)
    print("SSH连接成功\n")

    # 1. 服务状态
    print("=== 服务状态 ===")
    out, _ = run_cmd(ssh, "systemctl is-active order-food")
    print(f"  后端: {out}")
    out, _ = run_cmd(ssh, "systemctl is-active nginx")
    print(f"  Nginx: {out}")

    # 2. 后端日志（看是否启动成功）
    print("\n=== 后端日志（最后20行）===")
    out, _ = run_cmd(ssh, "tail -20 /opt/order-food/logs/stdout.log 2>/dev/null")
    print(out)

    # 3. 检查端口
    print("\n=== 端口监听 ===")
    out, _ = run_cmd(ssh, "ss -tlnp | grep -E '8080|80' ")
    print(out)

    # 4. API测试
    print("\n=== API测试 ===")
    tests = [
        ("H5分类接口", "curl -s -o /dev/null -w '%{http_code}' --max-time 5 http://localhost:8080/api/h5/categories"),
        ("H5注册", """curl -s --max-time 5 -X POST http://localhost:8080/api/h5/member/register -H 'Content-Type: application/json' -d '{"username":"verifytest001","password":"123456"}'"""),
        ("H5登录", """curl -s --max-time 5 -X POST http://localhost:8080/api/h5/member/login -H 'Content-Type: application/json' -d '{"username":"verifytest001","password":"123456","captchaToken":"skip"}'"""),
        ("Admin登录", """curl -s --max-time 5 -X POST http://localhost:8080/api/admin/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}'"""),
        ("公网H5", "curl -s -o /dev/null -w '%{http_code}' --max-time 5 http://localhost/"),
        ("公网Admin", "curl -s -o /dev/null -w '%{http_code}' --max-time 5 http://localhost/admin/"),
    ]

    for name, cmd in tests:
        out, err = run_cmd(ssh, cmd, timeout=10)
        display = out[:300] if len(out) > 300 else out
        print(f"  {name}: {display}")
        if err:
            print(f"    stderr: {err}")
        time.sleep(0.5)

    ssh.close()
    print("\n验证完成")

if __name__ == "__main__":
    main()
