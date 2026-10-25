import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * Axios 请求封装：
 * 1. 请求拦截器：自动附加 JWT 令牌（从 localStorage 读取）
 * 2. 响应拦截器：统一解包 Result 结构，401 跳登录，错误弹提示
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 60000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('xh_token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    // 401：登录过期，清理并跳转登录页
    if (res.code === 401) {
      localStorage.removeItem('xh_token')
      localStorage.removeItem('xh_user')
      if (!location.hash.includes('/login')) {
        location.hash = '#/login'
      }
      ElMessage.error(res.message || '请先登录')
      return Promise.reject(new Error(res.message))
    }
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message))
  },
  error => {
    ElMessage.error('网络异常，请检查后端是否启动')
    return Promise.reject(error)
  }
)

export default request
