import request from './request'
import type { DiningTable } from '@/types'

/** 获取桌台列表 */
export function getTableList(): Promise<DiningTable[]> {
  return request.get('/api/admin/tables')
}

/** 新增桌台 */
export function addTable(data: Partial<DiningTable>): Promise<void> {
  return request.post('/api/admin/tables', data)
}

/** 更新桌台 */
export function updateTable(id: number, data: Partial<DiningTable>): Promise<void> {
  return request.put(`/api/admin/tables/${id}`, data)
}

/** 删除桌台 */
export function deleteTable(id: number): Promise<void> {
  return request.delete(`/api/admin/tables/${id}`)
}
