import request from './request'
import type { Order, PageResult } from '@/types'

export interface OrderPageParams {
  current?: number
  size?: number
  status?: number
  startTime?: string
  endTime?: string
}

/** 订单分页查询 */
export function getOrderPage(params: OrderPageParams): Promise<PageResult<Order>> {
  return request.get('/api/admin/orders/page', { params })
}

/** 获取订单详情 */
export function getOrderDetail(id: number): Promise<Order> {
  return request.get(`/api/admin/orders/${id}`)
}

/** 更新订单状态 */
export function updateOrderStatus(id: number, status: number): Promise<void> {
  return request.patch(`/api/admin/orders/${id}/status`, { status })
}
