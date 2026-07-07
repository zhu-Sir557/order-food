import { defineStore } from 'pinia'
import { createTempUser } from '@/api/user'
import type { DiningTable } from '@/types'

interface UserState {
  token: string
  tempUserId: number | null
  tableId: number | null
  tableCode: string
  tableName: string
}

/**
 * 用户 Store
 * 管理临时用户令牌和桌台选择
 * 支持自动创建临时用户、恢复会话、设置桌台
 */
export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('token') || '',
    tempUserId: localStorage.getItem('tempUserId')
      ? Number(localStorage.getItem('tempUserId'))
      : null,
    tableId: localStorage.getItem('tableId')
      ? Number(localStorage.getItem('tableId'))
      : null,
    tableCode: localStorage.getItem('tableCode') || '',
    tableName: localStorage.getItem('tableName') || '',
  }),
  getters: {
    /** 是否已登录（有 token） */
    isLoggedIn: (state): boolean => !!state.token,

    /** 是否已登录会员（检查 localStorage 中的 memberId） */
    isMember: (): boolean => !!localStorage.getItem('memberId'),
  },
  actions: {
    /**
     * 创建临时用户
     * 调用后端 API 获取 token 和 tempUserId，存储到 state 和 localStorage
     */
    async initUser(): Promise<void> {
      try {
        const res = await createTempUser()
        this.token = res.token
        this.tempUserId = res.tempUserId
        localStorage.setItem('token', res.token)
        localStorage.setItem('tempUserId', String(res.tempUserId))
      } catch (error) {
        console.error('创建临时用户失败:', error)
      }
    },

    /**
     * 设置桌台
     * @param table - 桌台对象
     */
    setTable(table: DiningTable): void {
      this.tableId = table.id
      this.tableCode = table.code
      this.tableName = table.name
      localStorage.setItem('tableId', String(table.id))
      localStorage.setItem('tableCode', table.code)
      localStorage.setItem('tableName', table.name)
    },

    /**
     * 退出会员登录
     * 清除所有 member 相关 localStorage，重新创建临时用户
     */
    async logout(): Promise<void> {
      localStorage.removeItem('memberId')
      localStorage.removeItem('memberUsername')
      localStorage.removeItem('memberBalance')
      localStorage.removeItem('token')
      localStorage.removeItem('tempUserId')
      this.token = ''
      this.tempUserId = null
      await this.initUser()
    },

    /**
     * 从 localStorage 恢复用户会话
     * 如果没有 token 则自动创建临时用户
     */
    async restore(): Promise<void> {
      const token = localStorage.getItem('token')
      const tempUserId = localStorage.getItem('tempUserId')
      const tableId = localStorage.getItem('tableId')
      const tableCode = localStorage.getItem('tableCode')
      const tableName = localStorage.getItem('tableName')

      if (token) {
        this.token = token
      }
      if (tempUserId) {
        this.tempUserId = Number(tempUserId)
      }
      if (tableId) {
        this.tableId = Number(tableId)
      }
      if (tableCode) {
        this.tableCode = tableCode
      }
      if (tableName) {
        this.tableName = tableName
      }

      // 没有 token 时自动创建临时用户
      if (!token) {
        await this.initUser()
      }
    },
  },
})
