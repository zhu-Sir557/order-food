import request from './request'

/** 商家配置（后台） */
export interface MerchantConfigVO {
  id?: number
  aboutUsContent?: string
  contactPhone?: string
  createTime?: string
  updateTime?: string
}

/** 保存/更新商家配置请求 */
export interface MerchantConfigSaveDTO {
  aboutUsContent?: string
  contactPhone?: string
}

/** 获取商家配置 */
export function getMerchantConfig(): Promise<MerchantConfigVO> {
  return request.get('/api/admin/merchant-config')
}

/** 新增商家配置 */
export function saveMerchantConfig(data: MerchantConfigSaveDTO): Promise<void> {
  return request.post('/api/admin/merchant-config', data)
}

/** 更新商家配置 */
export function updateMerchantConfig(data: MerchantConfigSaveDTO): Promise<void> {
  return request.put('/api/admin/merchant-config', data)
}
