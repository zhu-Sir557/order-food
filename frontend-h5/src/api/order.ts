import request from './request'
import type { Order, PageResult } from '@/types'

/** 提交订单的请求数据 */
export interface SubmitOrderData {
  tableId: number
  remark: string
  items: { dishId: number; quantity: number; tasteSelection?: string }[]
}

/** 订单列表查询参数 */
export interface OrderListParams {
  status?: number
  page?: number
  size?: number
}

/**
 * 提交订单
 * @param data - 订单数据
 * @returns 创建的订单信息
 */
export function submitOrder(data: SubmitOrderData): Promise<Order> {
  return request.post('/api/h5/orders', data)
}

/**
 * 支付订单（微信/支付宝）
 * @param id - 订单ID
 * @param payMethod - 支付方式：1微信，2支付宝
 * @returns 支付后的订单信息
 */
export function payOrder(id: number, payMethod: number): Promise<void> {
  return request.post(`/api/h5/orders/${id}/pay`, null, { params: { payMethod } })
}

/**
 * 余额支付
 * @param id - 订单ID
 */
export function payByBalance(id: number): Promise<void> {
  return request.post(`/api/h5/orders/${id}/pay/balance`)
}

/**
 * 获取订单列表
 * @param params - 查询参数（状态、分页）
 * @returns 分页订单列表
 */
export function getOrderList(params?: OrderListParams): Promise<PageResult<Order>> {
  return request.get('/api/h5/orders', { params })
}

/**
 * 获取订单详情
 * @param id - 订单ID
 * @returns 订单详情
 */
export function getOrderDetail(id: number): Promise<Order> {
  return request.get(`/api/h5/orders/${id}`)
}
