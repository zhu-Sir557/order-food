import request from './request'
import type {
  MemberInfo,
  BalanceRecord,
  MyCard,
  PageResult,
  UnifiedLoginData,
  BindPhoneData,
  SetPasswordData,
  UpdateNicknameData,
  AvatarUpdateResult,
  ChangeLimitVO,
} from '@/types'

// 重新导出业务类型，供 store 等模块按既有约定从本模块引用（定义位于 @/types）
export type {
  UnifiedLoginData,
  BindPhoneData,
  SetPasswordData,
  UpdateNicknameData,
  AvatarUpdateResult,
} from '@/types'

/** 会员登录/注册响应（含昵称/头像） */
export interface MemberLoginVO {
  token: string
  memberId: number
  username: string
  balance: number
  nickname?: string
  avatar?: string
}

/** 注册请求 */
export interface RegisterData {
  username: string
  password: string
}

/** 统一登录请求 */
export type LoginData = UnifiedLoginData

/** 兑换点卡请求 */
export interface RedeemCardData {
  cardNo: string
  cardPassword: string
}

/**
 * 会员注册（自动登录）
 * @param data 注册数据
 * @returns 登录响应（含 token）
 */
export function register(data: RegisterData): Promise<MemberLoginVO> {
  return request.post('/api/h5/member/register', data)
}

/**
 * 统一会员登录（ACCOUNT_PASSWORD / PHONE_CODE）
 * @param data 统一登录数据
 * @returns 登录响应（含 token 与昵称/头像）
 */
export function login(data: UnifiedLoginData): Promise<MemberLoginVO> {
  return request.post('/api/h5/member/login', data)
}

/**
 * 绑定手机
 * @param data 绑定手机数据
 */
export function bindPhone(data: BindPhoneData): Promise<void> {
  return request.post('/api/h5/member/bind-phone', data)
}

/**
 * 设置密码
 * @param data 设置密码数据
 */
export function setPassword(data: SetPasswordData): Promise<void> {
  return request.post('/api/h5/member/set-password', data)
}

/**
 * 修改昵称
 * @param data 修改昵称数据
 * @returns 修改后剩余次数
 */
export function updateNickname(data: UpdateNicknameData): Promise<ChangeLimitVO> {
  return request.post('/api/h5/member/nickname', data)
}

/**
 * 上传头像（multipart/form-data，字段名 file）
 * @param formData 包含 file 的表单数据
 * @returns 新头像地址与剩余可修改次数
 */
export function uploadAvatar(formData: FormData): Promise<AvatarUpdateResult> {
  return request.post('/api/h5/member/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000,
  })
}

/**
 * 获取会员信息
 * @returns 会员信息
 */
export function getMemberInfo(): Promise<MemberInfo> {
  return request.get('/api/h5/member/info')
}

/**
 * 兑换点卡
 * @param data 兑换数据
 */
export function redeemCard(data: RedeemCardData): Promise<void> {
  return request.post('/api/h5/member/redeem', data)
}

/**
 * 获取余额变动记录
 * @param page 页码
 * @param size 每页大小
 * @returns 分页余额记录
 */
export function getBalanceRecords(page?: number, size?: number): Promise<PageResult<BalanceRecord>> {
  return request.get('/api/h5/member/balance/records', { params: { page, size } })
}

/**
 * 获取已发放给我的点卡列表
 * @returns 点卡列表
 */
export function getMyCards(): Promise<MyCard[]> {
  return request.get('/api/h5/member/cards')
}
