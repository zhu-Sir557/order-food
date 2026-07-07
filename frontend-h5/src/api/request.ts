import axios, { type AxiosInstance, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { showToast } from 'vant'
import router from '@/router'

/**
 * Axios 请求实例
 * 统一处理请求拦截、响应拦截、错误处理
 */
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
})

// 请求拦截器：添加 Authorization 头
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理响应数据和错误
request.interceptors.response.use(
  async (response: AxiosResponse) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    if (res.code === 401) {
      const memberId = localStorage.getItem('memberId')

      if (memberId) {
        // 会员 token 失效：清除会员信息，跳转登录页
        localStorage.removeItem('token')
        localStorage.removeItem('memberId')
        localStorage.removeItem('memberUsername')
        localStorage.removeItem('memberBalance')
        showToast('登录已过期，请重新登录')
        router.push('/login')
        return Promise.reject(new Error(res.message || '未授权'))
      }

      // 临时用户 token 失效：清除旧 token，重新创建临时用户并重试
      localStorage.removeItem('token')
      localStorage.removeItem('tempUserId')

      const originalRequest = response.config as InternalAxiosRequestConfig & { _retry?: boolean }
      if (originalRequest && !originalRequest._retry) {
        originalRequest._retry = true
        try {
          const { useUserStore } = await import('@/store/modules/user')
          const userStore = useUserStore()
          await userStore.initUser()
          // 用新 token 重试原始请求
          originalRequest.headers.Authorization = `Bearer ${userStore.token}`
          return request(originalRequest)
        } catch (retryError) {
          showToast('登录失败，请刷新页面重试')
          return Promise.reject(new Error(res.message || '未授权'))
        }
      }
      return Promise.reject(new Error(res.message || '未授权'))
    }
    showToast(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    showToast(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
