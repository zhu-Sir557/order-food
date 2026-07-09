import axios, { type AxiosInstance, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000
})

// 请求拦截器：添加 Authorization
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 标记是否正在跳转登录页，避免 401 时重复弹窗/重复跳转
let isRedirectingToLogin = false

/**
 * 统一处理令牌过期/无效：清空登录态、受控弹一次提示、跳转登录页。
 * 绝不把后端原始 message（如「令牌无效或已过期」）外抛给业务层。
 *
 * @returns 永远 reject，终止后续链式调用
 */
function handleTokenExpired(): Promise<never> {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_info')
  if (!isRedirectingToLogin && router.currentRoute.value.path !== '/login') {
    isRedirectingToLogin = true
    ElMessage.error('登录已过期，请重新登录')
  }
  router.push('/login').finally(() => {
    isRedirectingToLogin = false
  })
  return Promise.reject(new Error('TOKEN_EXPIRED'))
}

// 响应拦截器：统一处理业务码
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    if (res.code === 401) {
      return handleTokenExpired()
    }
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  (error) => {
    if (error.response?.status === 401) {
      return handleTokenExpired()
    }
    ElMessage.error(error.message || '网络请求异常')
    return Promise.reject(error)
  }
)

export default service
