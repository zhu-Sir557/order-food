import request from './request'
import type { Dish, Category, Banner } from '@/types'

/**
 * 获取所有菜品分类
 * @returns 分类列表
 */
export function getCategories(): Promise<Category[]> {
  return request.get('/api/h5/categories')
}

/**
 * 获取菜品列表
 * @param categoryId - 可选的分类ID，不传则返回所有菜品
 * @returns 菜品列表
 */
export function getDishes(categoryId?: number): Promise<Dish[]> {
  return request.get('/api/h5/dishes', { params: { categoryId } })
}

/**
 * 搜索菜品
 * @param keyword - 搜索关键词
 * @returns 匹配的菜品列表
 */
export function searchDishes(keyword: string): Promise<Dish[]> {
  return request.get('/api/h5/dishes/search', { params: { keyword } })
}

/**
 * 获取轮播图列表
 * @returns 启用的轮播图列表，按排序排列
 */
export function getBanners(): Promise<Banner[]> {
  return request.get('/api/h5/banners')
}
