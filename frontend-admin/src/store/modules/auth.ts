import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface AdminInfo {
  id: number
  name: string
  avatar: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('admin_token') || '')
  const adminInfo = ref<AdminInfo | null>(
    (() => {
      const stored = localStorage.getItem('admin_info')
      return stored ? JSON.parse(stored) : null
    })()
  )

  const isLoggedIn = computed(() => !!token.value)

  function login(newToken: string, info: AdminInfo): void {
    token.value = newToken
    adminInfo.value = info
    localStorage.setItem('admin_token', newToken)
    localStorage.setItem('admin_info', JSON.stringify(info))
  }

  function logout(): void {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_info')
  }

  return {
    token,
    adminInfo,
    isLoggedIn,
    login,
    logout
  }
})
