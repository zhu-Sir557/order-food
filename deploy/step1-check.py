#!/usr/bin/env python3
"""第一步：测试SSH连接 + 检查服务器状态"""
import paramiko
import os
import sys

HOST = os.environ.get("DEPLOY_SERVER_HOST", "")
USER = os.environ.get("DEPLOY_SERVER_USER", "root")
PASS = os.environ.get("DEPLOY_SERVER_PASS", "")

def run_cmd(ssh, cmd, timeout=10):
    """执行命令并返回输出"""
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    return out, err

def main():
    print("连接服务器...")
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    try:
        ssh.connect(HOST, port=22, username=USER, password=PASS, timeout=15)
        print("SSH 连接成功!\n")
    except Exception as e:
        print(f"SSH 连接失败: {e}")
        sys.exit(1)

    # 检查状态
    print("=== 服务器状态 ===")
    
    out, _ = run_cmd(ssh, "hostname")
    print(f"主机名: {out}")
    
    out, _ = run_cmd(ssh, "cat /opt/order-food/.credentials 2>/dev/null || echo '文件不存在'")
    print(f"\n.credentials 文件:")
    print(out)
    
    out, _ = run_cmd(ssh, "grep -c '__JWT_SECRET__\\|__DB_PASSWORD__' /opt/order-food/config/application-prod.yml 2>/dev/null || echo '0'")
    print(f"\n配置文件占位符数量: {out}")
    
    out, _ = run_cmd(ssh, "systemctl is-active order-food 2>/dev/null")
    print(f"后端服务状态: {out}")
    
    out, _ = run_cmd(ssh, "systemctl is-active nginx 2>/dev/null")
    print(f"Nginx状态: {out}")
    
    out, _ = run_cmd(ssh, "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/h5/categories 2>/dev/null || echo '连接失败'")
    print(f"H5分类接口HTTP状态: {out}")

    ssh.close()
    print("\n连接已关闭")

if __name__ == "__main__":
    main()
