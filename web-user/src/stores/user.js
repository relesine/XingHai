import { defineStore } from 'pinia'
import { apiLogin, apiRegister } from '../api'

/**
 * 用户状态管理（Pinia）：
 * 登录成功保存令牌与用户信息到 localStorage，全局共享登录态
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('xh_token') || '',
    user: JSON.parse(localStorage.getItem('xh_user') || 'null')
  }),
  getters: {
    isLogin: state => !!state.token,
    roles: state => state.user?.roles || []
  },
  actions: {
    async login(form) {
      const data = await apiLogin(form)
      this.token = data.token
      this.user = {
        userId: data.userId,
        username: data.username,
        nickname: data.nickname,
        avatar: data.avatar,
        roles: data.roles
      }
      localStorage.setItem('xh_token', data.token)
      localStorage.setItem('xh_user', JSON.stringify(this.user))
    },
    async register(form) {
      const data = await apiRegister(form)
      this.token = data.token
      this.user = {
        userId: data.userId,
        username: data.username,
        nickname: data.nickname,
        avatar: data.avatar,
        roles: data.roles
      }
      localStorage.setItem('xh_token', data.token)
      localStorage.setItem('xh_user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('xh_token')
      localStorage.removeItem('xh_user')
    }
  }
})
