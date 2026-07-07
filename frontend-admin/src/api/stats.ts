import request from './request'
import type { Dashboard } from '@/types'

/** 获取仪表盘统计数据 */
export function getDashboard(): Promise<Dashboard> {
  return request.get('/api/admin/stats/dashboard')
}
