import { defineStore } from 'pinia'
import {
  register as registerApi,
  login as loginApi,
  getMemberInfo,
  bindPhone as bindPhoneApi,
  setPassword as setPasswordApi,
  updateNickname as updateNicknameApi,
  uploadAvatar as uploadAvatarApi,
} from '@/api/member'
import type { RegisterData, UnifiedLoginData, BindPhoneData, SetPasswordData, UpdateNicknameData, AvatarUpdateResult } from '@/api/member'
import { smsLogin as smsLoginApi } from '@/api/sms'
import type { SmsLoginData } from '@/api/sms'

interface MemberState {
  memberId: number | null
  username: string
  balance: number
  nickname: string
  avatar: string
  /** 今日昵称剩余可修改次数（null 表示尚未获知，按默认上限展示） */
  nickRemaining: number | null
  /** 今日头像剩余可修改次数 */
  avatarRemaining: number | null
}

/**
 * 会员 Store
 * 管理会员登录状态、余额、昵称、头像等信息
 */
export const useMemberStore = defineStore('member', {
  state: (): MemberState => ({
    memberId: localStorage.getItem('memberId') ? Number(localStorage.getItem('memberId')) : null,
    username: localStorage.getItem('memberUsername') || '',
    balance: localStorage.getItem('memberBalance') ? Number(localStorage.getItem('memberBalance')) : 0,
    nickname: localStorage.getItem('memberNickname') || '',
    avatar: localStorage.getItem('memberAvatar') || '',
    nickRemaining: null,
    avatarRemaining: null,
  }),
  getters: {
    /** 是否已登录会员 */
    isLoggedIn: (state): boolean => !!state.memberId,
    /** 展示名：昵称优先，否则用户名 */
    displayName: (state): string => state.nickname || state.username || '食客',
  },
  actions: {
    /**
     * 注册（自动登录）
     * @param data 注册数据
     */
    async register(data: RegisterData): Promise<void> {
      const res = await registerApi(data)
      this.setMemberInfo(res.token, res.memberId, res.username, res.balance, res.nickname, res.avatar)
    },

    /**
     * 统一登录
     * @param data 统一登录数据
     */
    async login(data: UnifiedLoginData): Promise<void> {
      const res = await loginApi(data)
      this.setMemberInfo(res.token, res.memberId, res.username, res.balance, res.nickname, res.avatar)
    },

    /**
     * 短信验证码登录（兼容旧接口，自动注册 / 匹配会员）
     * @param data 短信登录数据（手机号 + 验证码）
     */
    async smsLogin(data: SmsLoginData): Promise<void> {
      const res = await smsLoginApi(data.phone, data.code)
      this.setMemberInfo(res.token, res.memberId, res.username, res.balance, res.nickname, res.avatar)
    },

    /**
     * 绑定手机（需先发码 + 滑块）
     */
    async bindPhone(data: BindPhoneData): Promise<void> {
      await bindPhoneApi(data)
      await this.refreshInfo()
    },

    /**
     * 设置密码（需滑块）
     */
    async setPassword(data: SetPasswordData): Promise<void> {
      await setPasswordApi(data)
    },

    /**
     * 修改昵称
     * @param data 修改昵称数据
     * @returns 修改后剩余次数
     */
    async updateNickname(data: UpdateNicknameData): Promise<number> {
      const res = await updateNicknameApi(data)
      this.nickname = data.nickname
      this.nickRemaining = res.remaining
      localStorage.setItem('memberNickname', data.nickname)
      return res.remaining
    },

    /**
     * 上传头像（调用后端 multipart 接口，返回新头像地址与剩余次数）
     * @param file 用户选择的图片文件
     * @returns 当日剩余可修改次数
     */
    async uploadAvatar(file: File): Promise<number> {
      const formData = new FormData()
      formData.append('file', file)
      const res: AvatarUpdateResult = await uploadAvatarApi(formData)
      this.avatar = res.avatar
      this.avatarRemaining = res.remaining
      localStorage.setItem('memberAvatar', res.avatar)
      return res.remaining
    },

    /**
     * 刷新会员信息（从服务端获取最新余额/昵称/头像）
     */
    async refreshInfo(): Promise<void> {
      if (!this.memberId) return
      try {
        const info = await getMemberInfo()
        this.balance = info.balance
        if (info.nickname) {
          this.nickname = info.nickname
          localStorage.setItem('memberNickname', info.nickname)
        }
        if (info.avatar) {
          this.avatar = info.avatar
          localStorage.setItem('memberAvatar', info.avatar)
        }
      } catch (error) {
        console.error('刷新会员信息失败:', error)
      }
    },

    /**
     * 设置会员信息到 state 和 localStorage
     */
    setMemberInfo(
      token: string,
      memberId: number,
      username: string,
      balance: number,
      nickname?: string,
      avatar?: string,
    ): void {
      this.memberId = memberId
      this.username = username
      this.balance = balance
      if (nickname) {
        this.nickname = nickname
        localStorage.setItem('memberNickname', nickname)
      }
      if (avatar) {
        this.avatar = avatar
        localStorage.setItem('memberAvatar', avatar)
      }
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
      this.nickname = ''
      this.avatar = ''
      this.nickRemaining = null
      this.avatarRemaining = null
      localStorage.removeItem('memberId')
      localStorage.removeItem('memberUsername')
      localStorage.removeItem('memberBalance')
      localStorage.removeItem('memberNickname')
      localStorage.removeItem('memberAvatar')
    },
  },
})
