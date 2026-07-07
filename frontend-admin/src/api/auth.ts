import request from './request'

export interface LoginData {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  adminInfo: {
    id: number
    name: string
    avatar: string
  }
}

/** 管理员登录 */
export function login(data: LoginData): Promise<LoginResult> {
  return request.post('/api/admin/login', data)
}
