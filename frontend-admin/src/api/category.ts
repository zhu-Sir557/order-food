import request from './request'
import type { Category } from '@/types'

export interface SortItem {
  id: number
  sort: number
}

/** 获取分类列表 */
export function getCategoryList(): Promise<Category[]> {
  return request.get('/api/admin/categories')
}

/** 新增分类 */
export function addCategory(data: Partial<Category>): Promise<void> {
  return request.post('/api/admin/categories', data)
}

/** 更新分类 */
export function updateCategory(id: number, data: Partial<Category>): Promise<void> {
  return request.put(`/api/admin/categories/${id}`, data)
}

/** 删除分类 */
export function deleteCategory(id: number): Promise<void> {
  return request.delete(`/api/admin/categories/${id}`)
}

/** 更新分类排序 */
export function updateCategorySort(data: SortItem[]): Promise<void> {
  return request.put('/api/admin/categories/sort', { items: data })
}
