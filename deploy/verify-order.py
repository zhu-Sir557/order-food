#!/usr/bin/env python3
"""验证订单NPE修复"""
import requests
import os

BASE = os.environ.get('DEPLOY_SERVER_URL', 'http://localhost')
SEP = '=' * 50

# 1. 注册获取token
print('=== 1. 注册测试用户 ===')
r = requests.post(f'{BASE}/api/h5/member/register', json={
    'username': 'test_order_e2e_004',
    'password': '123456'
}, timeout=10)
reg_data = r.json()
token = reg_data.get('data', {}).get('token', '')
print(f'  Code: {reg_data.get("code")}, Token: {token[:40]}...')

headers = {'Authorization': f'Bearer {token}'}

# 2. 获取菜品列表
print('\n=== 2. 获取菜品列表 ===')
r = requests.get(f'{BASE}/api/h5/dishes', headers=headers, timeout=10)
dishes = r.json().get('data', [])
print(f'  菜品数量: {len(dishes)}')
dish = dishes[0] if dishes else None
if dish:
    print(f'  测试菜品: id={dish["id"]}, name={dish["name"]}, price={dish["price"]}')

# 3. 提交订单
print('\n=== 3. 提交订单 ===')
order_data = {
    'tableId': 1,
    'remark': 'NPE修复验证',
    'items': [{
        'dishId': dish['id'],
        'quantity': 2,
        'tasteSelection': '微辣'
    }]
}
r = requests.post(f'{BASE}/api/h5/orders', json=order_data, headers=headers, timeout=10)
order_resp = r.json()
print(f'  Code: {order_resp.get("code")}')
print(f'  Message: {order_resp.get("message")}')

if order_resp.get('code') != 200 or not order_resp.get('data'):
    print('  [失败] 提交订单失败')
    exit(1)

order = order_resp['data']
order_id = order['id']
print(f'  订单ID: {order_id}')
print(f'  订单号: {order.get("orderNo")}')
print(f'  总金额: {order.get("totalAmount")}')
print(f'  memberId: {order.get("memberId")}')
print(f'  tempUserId: {order.get("tempUserId")}  <-- null 是正常的(会员无临时ID)')

# 4. 获取订单详情 - 之前 NPE 的地方!
print('\n=== 4. 获取订单详情 (之前 NPE!) ===')
r = requests.get(f'{BASE}/api/h5/orders/{order_id}', headers=headers, timeout=10)
detail_resp = r.json()
print(f'  HTTP Status: {r.status_code}')
print(f'  Code: {detail_resp.get("code")}')
print(f'  Message: {detail_resp.get("message")}')

if detail_resp.get('code') == 200:
    detail = detail_resp['data']
    print(f'  订单号: {detail.get("orderNo")}')
    items = detail.get('items', [])
    print(f'  菜品数: {len(items)}')
    if items:
        item = items[0]
        print(f'  菜品: {item.get("dishName")} x{item.get("quantity")} = {item.get("subtotal")}')
    print('  >>> NPE 已修复! 订单详情获取成功! <<<')
else:
    print('  [失败] 获取订单详情失败!')
    exit(1)

# 5. 获取订单列表
print('\n=== 5. 获取订单列表 ===')
r = requests.get(f'{BASE}/api/h5/orders', headers=headers, timeout=10)
list_resp = r.json()
print(f'  Code: {list_resp.get("code")}')

if list_resp.get('code') == 200:
    list_data = list_resp['data']
    print(f'  订单总数: {list_data.get("total")}')
    records = list_data.get('records', [])
    print(f'  当前页订单数: {len(records)}')
    print('  >>> 订单列表查询正常! <<<')
else:
    print('  [失败] 获取订单列表失败!')
    exit(1)

print(f'\n{SEP}')
print('  全部验证通过! Bug 已修复并部署上线!')
print(SEP)
