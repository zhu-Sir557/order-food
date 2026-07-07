import request from './request'
import type { AdminMember, BalanceRecord, PageResult } from '@/types'

/** 会员查询参数 */
export interface MemberQueryParams {
  page?: number
  size?: number
  keyword?: string
}

/**
 * 获取会员列表
 * @param params 查询参数
 * @returns 分页会员列表
 */
export function getMemberList(params?: MemberQueryParams): Promise<PageResult<AdminMember>> {
  return request.get('/api/admin/members', { params })
}

/**
 * 获取会员余额记录
 * @param id 会员ID
 * @param page 页码
 * @param size 每页大小
 * @returns 分页余额记录
 */
export function getMemberBalanceRecords(id: number, page?: number, size?: number): Promise<PageResult<BalanceRecord>> {
  return request.get(`/api/admin/members/${id}/balance/records`, { params: { page, size } })
}
