import request from './request'
import type { DiningTable } from '@/types'

/**
 * 获取可用桌台列表
 * @returns 可用桌台列表
 */
export function getAvailableTables(): Promise<DiningTable[]> {
  return request.get('/api/h5/tables/available')
}
