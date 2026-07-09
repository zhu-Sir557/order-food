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

// 模块级标记：避免并发 401 时重复弹出登录过期提示
let isRedirectingToLogin = false

/**
 * 统一处理令牌过期/无效：
 * 1. 清空所有登录态（token / 会员信息 / 临时用户标识），避免残留引发反复 401
 * 2. 仅弹出一次友好提示（模块级 flag + 当前路由判断去重）
 * 3. 跳转登录页
 * 4. 以受控的通用错误 reject（绝不把后端原始报文外抛，防止暴露给用户）
 */
function handleTokenExpired(): Promise<never> {
  // 一次性清干净所有登录态
  localStorage.removeItem('token')
  localStorage.removeItem('memberId')
  localStorage.removeItem('memberUsername')
  localStorage.removeItem('memberBalance')
  localStorage.removeItem('tempUserId')

  // 仅弹一次：当前未在登录页，且本次跳转流程尚未弹过
  if (!isRedirectingToLogin && router.currentRoute.value.path !== '/login') {
    isRedirectingToLogin = true
    showToast('登录已过期，请重新登录')
  }

  // 跳转登录页（跳转完成后重置 flag，允许后续真正的新 401 再次提示）
  router.push('/login').finally(() => {
    isRedirectingToLogin = false
  })

  // 受控 reject：通用错误，绝不包含后端原始 message（如“令牌无效或已过期”）
  return Promise.reject(new Error('TOKEN_EXPIRED'))
}

// 响应拦截器：统一处理响应数据和错误
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    if (res.code === 401) {
      // 业务层返回 code=401：令牌无效/过期，统一清登录态 + 跳登录页
      return handleTokenExpired()
    }
    // 其它业务错误：保留原有提示行为，不暴露细节
    showToast(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    // HTTP 层 401（后端直接返回 401 状态码而非 200 + {code:401}）
    if (error.response?.status === 401) {
      return handleTokenExpired()
    }
    // 其它网络/HTTP 错误：保留原有提示行为
    showToast(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
