import { defineStore } from 'pinia'
import { register as registerApi, login as loginApi, getMemberInfo } from '@/api/member'
import type { RegisterData, LoginData } from '@/api/member'

interface MemberState {
  memberId: number | null
  username: string
  balance: number
}

/**
 * 会员 Store
 * 管理会员登录状态、余额等信息
 */
export const useMemberStore = defineStore('member', {
  state: (): MemberState => ({
    memberId: localStorage.getItem('memberId') ? Number(localStorage.getItem('memberId')) : null,
    username: localStorage.getItem('memberUsername') || '',
    balance: localStorage.getItem('memberBalance') ? Number(localStorage.getItem('memberBalance')) : 0,
  }),
  getters: {
    /** 是否已登录会员 */
    isLoggedIn: (state): boolean => !!state.memberId,
  },
  actions: {
    /**
     * 注册（自动登录）
     * @param data 注册数据
     */
    async register(data: RegisterData): Promise<void> {
      const res = await registerApi(data)
      this.setMemberInfo(res.token, res.memberId, res.username, res.balance)
    },

    /**
     * 登录
     * @param data 登录数据
     */
    async login(data: LoginData): Promise<void> {
      const res = await loginApi(data)
      this.setMemberInfo(res.token, res.memberId, res.username, res.balance)
    },

    /**
     * 刷新会员信息（从服务端获取最新余额）
     */
    async refreshInfo(): Promise<void> {
      if (!this.memberId) return
      try {
        const info = await getMemberInfo()
        this.balance = info.balance
        localStorage.setItem('memberBalance', String(info.balance))
      } catch (error) {
        console.error('刷新会员信息失败:', error)
      }
    },

    /**
     * 设置会员信息到 state 和 localStorage
     */
    setMemberInfo(token: string, memberId: number, username: string, balance: number): void {
      this.memberId = memberId
      this.username = username
      this.balance = balance
      localStorage.setItem('token', token)
      localStorage.setItem('memberId', String(memberId))
      localStorage.setItem('memberUsername', username)
      localStorage.setItem('memberBalance', String(balance))
    },

    /**
     * 清除会员信息（退出登录时调用）
     */
    clearMemberInfo(): void {
      this.memberId = null
      this.username = ''
      this.balance = 0
      localStorage.removeItem('memberId')
      localStorage.removeItem('memberUsername')
      localStorage.removeItem('memberBalance')
    },
  },
})
