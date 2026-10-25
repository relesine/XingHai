import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 60000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('xh_creator_token')
  if (token) config.headers.Authorization = 'Bearer ' + token
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) return res.data
    if (res.code === 401) {
      localStorage.removeItem('xh_creator_token')
      localStorage.removeItem('xh_creator_user')
      location.hash = '#/login'
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
