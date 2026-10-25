import { defineStore } from 'pinia'
import { apiLogin } from '../api'

// 管理员登录态：token 存 xh_admin_token，用户信息存 xh_admin_user
export const useAdminStore = defineStore('admin', {
  state: () => ({
    token: localStorage.getItem('xh_admin_token') || '',
    user: JSON.parse(localStorage.getItem('xh_admin_user') || 'null')
  }),
  actions: {
    async login(form) {
      const data = await apiLogin(form)
      if (!data.roles || !data.roles.includes('ADMIN')) {
        throw new Error('该账号不是管理员，无法登录后台')
      }
      this.token = data.token
      this.user = data
      localStorage.setItem('xh_admin_token', data.token)
      localStorage.setItem('xh_admin_user', JSON.stringify(data))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('xh_admin_token')
      localStorage.removeItem('xh_admin_user')
    }
  }
})
