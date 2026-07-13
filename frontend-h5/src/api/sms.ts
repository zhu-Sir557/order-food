import request from './request'

/** 发送短信验证码请求 */
export interface SmsSendRequest {
  phone: string
  captchaToken: string
}

/** 短信验证码登录请求 */
export interface SmsLoginRequest {
  phone: string
  code: string
}

/** 短信登录响应（与 MemberLoginVO 结构一致） */
export interface SmsLoginVO {
  token: string
  memberId: number
  username: string
  balance: number
  nickname?: string
  avatar?: string
}

/** 兼容 store 入参类型别名 */
export type SmsLoginData = SmsLoginRequest

/**
 * 发送短信验证码（前置滑块 captchaToken）
 * @param phone 手机号
 * @param captchaToken 滑块校验通过的 token
 */
export function sendSmsCode(phone: string, captchaToken: string): Promise<void> {
  return request.post('/api/h5/sms/send', { phone, captchaToken })
}

/**
 * 短信验证码登录
 * @param phone 手机号
 * @param code 验证码
 * @returns 登录响应（含 token）
 */
export function smsLogin(phone: string, code: string): Promise<SmsLoginVO> {
  return request.post('/api/h5/sms/login', { phone, code })
}
