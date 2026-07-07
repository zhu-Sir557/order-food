#!/usr/bin/env python3
"""查看Admin登录500错误的详细日志"""
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

    # 1. 触发一次Admin登录
    print("=== 触发Admin登录 ===")
    out, _ = run_cmd(ssh, """curl -s -X POST http://localhost:8080/api/admin/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}'""")
    print(f"响应: {out}\n")

    # 2. 看stderr.log最后50行
    print("=== stderr.log 最后50行 ===")
    out, _ = run_cmd(ssh, "tail -50 /opt/order-food/logs/stderr.log 2>/dev/null")
    print(out)

    # 3. 看stdout.log中包含Exception/Error的最后30行
    print("\n=== stdout.log 中的错误 ===")
    out, _ = run_cmd(ssh, "grep -A5 'Exception\\|ERROR\\|500' /opt/order-food/logs/stdout.log 2>/dev/null | tail -50")
    print(out)

    # 4. 检查配置文件中的JWT配置
    print("\n=== JWT配置 ===")
    out, _ = run_cmd(ssh, "grep -A2 'jwt' /opt/order-food/config/application-prod.yml 2>/dev/null")
    print(out)

    # 5. 检查数据库中admin用户
    print("\n=== 数据库admin用户 ===")
    out, _ = run_cmd(ssh, "mysql -u root -p$(awk -F': ' '/[Mm]y[Ss][Qq][Ll]/{print $2}' /opt/order-food/.credentials | head -1 | tr -d '[:space:]') order_food -e 'SELECT id, username, password FROM admin_user LIMIT 5;' 2>/dev/null")
    print(out)

    ssh.close()

if __name__ == "__main__":
    main()
