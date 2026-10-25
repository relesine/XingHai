import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 统一 axios 实例：Bearer token + 401 跳登录
const request = axios.create({ baseURL: '/api', timeout: 30000 })

request.interceptors.request.use(config => {
  const token = localStorage.getItem('xh_admin_token')
  if (token) config.headers.Authorization = 'Bearer ' + token
  return config
})

request.interceptors.response.use(
  res => {
    const data = res.data
    if (data && data.code !== undefined && data.code !== 0 && data.code !== 200) {
      // 后端未登录/登录过期统一以 HTTP 200 + body code=401 返回，需在此清理并跳登录
      if (data.code === 401) {
        localStorage.removeItem('xh_admin_token')
        localStorage.removeItem('xh_admin_user')
        if (!location.hash.includes('/login')) {
          router.push('/login')
        }
        ElMessage.warning(data.message || '登录已过期，请重新登录')
      } else {
        ElMessage.error(data.message || '请求失败')
      }
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return data.data !== undefined ? data.data : data
  },
  err => {
    // 兼容部分网关/过滤器直接返回 HTTP 401 的情况
    if (err.response && err.response.status === 401) {
      localStorage.removeItem('xh_admin_token')
      localStorage.removeItem('xh_admin_user')
      router.push('/login')
      ElMessage.warning('登录已过期，请重新登录')
    } else {
      ElMessage.error((err.response && err.response.data && err.response.data.message) || err.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default request
