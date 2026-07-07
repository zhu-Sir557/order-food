import request from './request'
import type { Dish, PageResult } from '@/types'

export interface DishPageParams {
  current?: number
  size?: number
  name?: string
  categoryId?: number
}

/** 菜品分页查询 */
export function getDishPage(params: DishPageParams): Promise<PageResult<Dish>> {
  return request.get('/api/admin/dishes/page', { params })
}

/** 新增菜品 */
export function addDish(data: Partial<Dish>): Promise<void> {
  return request.post('/api/admin/dishes', data)
}

/** 更新菜品 */
export function updateDish(id: number, data: Partial<Dish>): Promise<void> {
  return request.put(`/api/admin/dishes/${id}`, data)
}

/** 删除菜品 */
export function deleteDish(id: number): Promise<void> {
  return request.delete(`/api/admin/dishes/${id}`)
}

/** 更新菜品上下架状态 */
export function updateDishStatus(id: number, status: number): Promise<void> {
  return request.patch(`/api/admin/dishes/${id}/status`, { status })
}
