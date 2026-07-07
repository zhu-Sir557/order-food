#!/usr/bin/env python3
"""用paramiko设置SSH公钥，然后后续用scp传文件"""
import paramiko
import os
import sys

HOST = os.environ.get("DEPLOY_SERVER_HOST", "")
USER = os.environ.get("DEPLOY_SERVER_USER", "root")
PASS = os.environ.get("DEPLOY_SERVER_PASS", "")

PUB_KEY = os.environ.get("DEPLOY_SSH_PUB_KEY", "")

def run_cmd(ssh, cmd, timeout=15):
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    return out, err

def main():
    print("连接服务器...")
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(HOST, port=22, username=USER, password=PASS, timeout=15)
    print("SSH 连接成功\n")

    # 1. 设置 SSH 公钥
    print("=== 设置 SSH 公钥 ===")
    
    # 创建 .ssh 目录
    out, err = run_cmd(ssh, "mkdir -p ~/.ssh && chmod 700 ~/.ssh && echo 'dir ok'")
    print(f"  创建 .ssh 目录: {out}")
    
    # 检查公钥是否已存在
    out, err = run_cmd(ssh, f"grep -c '{PUB_KEY}' ~/.ssh/authorized_keys 2>/dev/null || echo '0'")
    print(f"  公钥已存在: {out}")
    
    if out == "0":
        # 添加公钥
        out, err = run_cmd(ssh, f"echo '{PUB_KEY}' >> ~/.ssh/authorized_keys && echo 'key added'")
        print(f"  添加公钥: {out}")
    else:
        print("  公钥已存在，跳过")
    
    # 设置权限
    out, err = run_cmd(ssh, "chmod 600 ~/.ssh/authorized_keys && echo 'perm ok'")
    print(f"  设置权限: {out}")
    
    # 修复 SELinux 上下文（Alibaba Cloud Linux 3 是 RHEL-based）
    out, err = run_cmd(ssh, "restorecon -Rv ~/.ssh/ 2>&1 || echo 'restorecon skipped'")
    print(f"  SELinux上下文: {out}")
    
    # 验证
    out, err = run_cmd(ssh, "cat ~/.ssh/authorized_keys")
    print(f"\n  authorized_keys 内容:")
    for line in out.split('\n'):
        print(f"    {line}")
    
    # 检查 sshd 配置是否允许公钥认证
    out, err = run_cmd(ssh, "grep -E 'PubkeyAuthentication|AuthorizedKeysFile' /etc/ssh/sshd_config | grep -v '^#'")
    print(f"\n  sshd 公钥认证配置:")
    print(f"    {out}")
    
    # 检查 sshd_config.d 目录
    out, err = run_cmd(ssh, "cat /etc/ssh/sshd_config.d/*.conf 2>/dev/null || echo '无额外配置'")
    print(f"  sshd_config.d:")
    print(f"    {out}")

    ssh.close()
    print("\n公钥设置完成，连接已关闭")

if __name__ == "__main__":
    main()
