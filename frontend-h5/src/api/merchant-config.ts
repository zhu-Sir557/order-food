import request from './request'

/** 商家公开配置 */
export interface MerchantConfigPublic {
  aboutUsContent?: string
  contactPhone?: string
}

/** 获取商家公开配置（无需登录、无需 token） */
export function getMerchantConfig(): Promise<MerchantConfigPublic> {
  return request.get('/api/h5/merchant-config')
}
