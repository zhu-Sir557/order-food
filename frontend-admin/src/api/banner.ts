import request from './request'
import type { Banner, PageResult } from '@/types'

export interface BannerPageParams {
  current?: number
  size?: number
}

/** 轮播图分页查询 */
export function getBannerPage(params: BannerPageParams): Promise<PageResult<Banner>> {
  return request.get('/api/admin/banners/page', { params })
}

/** 新增轮播图 */
export function addBanner(data: Partial<Banner>): Promise<void> {
  return request.post('/api/admin/banners', data)
}

/** 更新轮播图 */
export function updateBanner(id: number, data: Partial<Banner>): Promise<void> {
  return request.put(`/api/admin/banners/${id}`, data)
}

/** 删除轮播图 */
export function deleteBanner(id: number): Promise<void> {
  return request.delete(`/api/admin/banners/${id}`)
}

/** 更新轮播图状态 */
export function updateBannerStatus(id: number, status: number): Promise<void> {
  return request.patch(`/api/admin/banners/${id}/status`, { status })
}
