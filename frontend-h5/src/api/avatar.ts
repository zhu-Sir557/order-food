import request from './request'
import type { AvatarVO } from '@/types'

/**
 * 获取头像列表
 * @returns 头像选项列表
 */
export function getAvatars(): Promise<AvatarVO[]> {
  return request.get('/api/h5/member/avatars')
}
