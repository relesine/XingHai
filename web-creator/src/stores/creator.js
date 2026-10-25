import { defineStore } from 'pinia'
import { apiLogin } from '../api'

/**
 * 创作者端登录态：令牌与用户信息分别存储（与用户端隔离）
 */
export const useCreatorStore = defineStore('creator', {
  state: () => ({
    token: localStorage.getItem('xh_creator_token') || '',
    user: JSON.parse(localStorage.getItem('xh_creator_user') || 'null')
  }),
  getters: {
    isLogin: state => !!state.token
  },
  actions: {
    async login(form) {
      const data = await apiLogin(form)
      if (!data.roles.includes('CREATOR') && !data.roles.includes('ADMIN')) {
        throw new Error('该账号不是认证创作者，无法登录创作者中心')
      }
      this.token = data.token
      this.user = { userId: data.userId, username: data.username, nickname: data.nickname, avatar: data.avatar, roles: data.roles }
      localStorage.setItem('xh_creator_token', data.token)
      localStorage.setItem('xh_creator_user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('xh_creator_token')
      localStorage.removeItem('xh_creator_user')
    }
  }
})
