import request from './request'
import type { Card, PageResult } from '@/types'

/** 批量创建点卡参数 */
export interface BatchCreateCardData {
  amount: number
  count: number
}

/** 发放点卡参数 */
export interface AssignCardData {
  memberId: number
}

/** 点卡查询参数 */
export interface CardQueryParams {
  cardNo?: string
  status?: number
  page?: number
  size?: number
}

/**
 * 批量创建点卡
 * @param data 批量创建参数
 * @returns 创建的点卡列表
 */
export function batchCreateCards(data: BatchCreateCardData): Promise<Card[]> {
  return request.post('/api/admin/cards/batch', data)
}

/**
 * 获取点卡列表
 * @param params 查询参数
 * @returns 分页点卡列表
 */
export function getCardList(params?: CardQueryParams): Promise<PageResult<Card>> {
  return request.get('/api/admin/cards', { params })
}

/**
 * 发放点卡给会员
 * @param id 卡ID
 * @param data 发放参数
 */
export function assignCard(id: number, data: AssignCardData): Promise<void> {
  return request.post(`/api/admin/cards/${id}/assign`, data)
}
