import request from './request'
import type { MemberInfo, BalanceRecord, MyCard, PageResult } from '@/types'

/** 会员登录/注册响应 */
export interface MemberLoginVO {
  token: string
  memberId: number
  username: string
  balance: number
}

/** 注册请求 */
export interface RegisterData {
  username: string
  password: string
}

/** 登录请求 */
export interface LoginData {
  username: string
  password: string
  captchaToken: string
}

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
 * 会员登录
 * @param data 登录数据
 * @returns 登录响应（含 token）
 */
export function login(data: LoginData): Promise<MemberLoginVO> {
  return request.post('/api/h5/member/login', data)
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
