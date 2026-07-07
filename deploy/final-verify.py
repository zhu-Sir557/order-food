#!/usr/bin/env python3
"""用正确的URL验证Admin登录"""
import paramiko
import os

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

    # 用正确的URL测试Admin登录
    print("=== Admin登录（正确URL: /api/admin/login）===")
    out, _ = run_cmd(ssh, """curl -s -X POST http://localhost:8080/api/admin/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}'""")
    print(f"响应: {out}")

    # 通过公网测试
    print("\n=== 公网Admin登录 ===")
    out, _ = run_cmd(ssh, """curl -s -X POST http://localhost/api/admin/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}'""")
    print(f"响应: {out}")

    # H5注册（用新用户名）
    print("\n=== H5注册（新用户）===")
    out, _ = run_cmd(ssh, """curl -s -X POST http://localhost:8080/api/h5/member/register -H 'Content-Type: application/json' -d '{"username":"finaltest001","password":"123456"}'""")
    print(f"响应: {out}")

    # H5登录（无验证码，预期返回400验证码错误）
    print("\n=== H5登录（无验证码）===")
    out, _ = run_cmd(ssh, """curl -s -X POST http://localhost:8080/api/h5/member/login -H 'Content-Type: application/json' -d '{"username":"finaltest001","password":"123456","captchaToken":"skip"}'""")
    print(f"响应: {out}")

    ssh.close()
    print("\n验证完成")

if __name__ == "__main__":
    main()
