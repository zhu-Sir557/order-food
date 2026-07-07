import request from './request'

/** 创建临时用户的返回结果 */
export interface TempUserResult {
  token: string
  tempUserId: number
}

/**
 * 创建临时用户
 * @returns 包含 token 和 tempUserId 的结果
 */
export function createTempUser(): Promise<TempUserResult> {
  return request.post('/api/h5/user/temp')
}
