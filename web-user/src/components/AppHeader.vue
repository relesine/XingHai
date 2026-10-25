<template>
  <header class="site-header">
    <div class="header-inner">
      <div class="brand" @click="go('/')">
        <span class="brand-mark"><AppIcon name="play" :size="16" :stroke-width="2" /></span>
        <span class="brand-name">星海</span>
      </div>

      <nav class="main-nav">
        <router-link
          v-for="item in navs"
          :key="item.path"
          :to="item.path"
          class="nav-link"
          :class="{ active: isActive(item.path) }"
        >{{ item.label }}</router-link>
      </nav>

      <div class="search">
        <el-input
          v-model="keyword"
          placeholder="搜索视频、创作者或话题"
          clearable
          @keyup.enter="doSearch"
        >
          <template #prefix><AppIcon name="search" :size="16" /></template>
        </el-input>
      </div>

      <div class="actions">
        <el-button class="publish-btn" plain @click="onPublish">
          <AppIcon name="upload" :size="16" />
          <span>投稿</span>
        </el-button>

        <template v-if="userStore.isLogin">
          <button class="icon-btn" title="消息中心" @click="go('/messages')">
            <el-badge :value="unread" :hidden="!unread" :max="99">
              <AppIcon name="message" :size="20" />
            </el-badge>
          </button>
          <el-dropdown trigger="click" @command="onMenu">
            <button class="avatar-btn">
              <img :src="avatar" alt="头像" />
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <div class="menu-user">
                  <div class="menu-name">{{ userStore.user?.nickname }}</div>
                  <div class="menu-role">{{ roleText }}</div>
                </div>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="mall">星海商城</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item v-if="isCreator" command="creator">创作者中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <template v-else>
          <el-button text @click="go('/login')">登录</el-button>
          <el-button type="primary" @click="go('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { apiUnread } from '../api'
import AppIcon from './AppIcon.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const keyword = ref(route.query.keyword || '')
const unread = ref(0)

const navs = [
  { label: '首页', path: '/' },
  { label: '直播', path: '/live' },
  { label: '商城', path: '/mall' }
]

const avatar = computed(() => userStore.user?.avatar || '/upload/avatar/u1.svg')
const isCreator = computed(() => (userStore.user?.roles || []).includes('CREATOR'))
const roleText = computed(() => (isCreator.value ? '认证创作者' : '普通用户'))

function isActive(path) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
function go(path) {
  router.push(path)
}
function doSearch() {
  const value = keyword.value.trim()
  if (value) router.push({ path: '/search', query: { keyword: value } })
}
function onPublish() {
  router.push(userStore.isLogin ? '/publish' : '/login')
}
function onMenu(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/')
  } else if (cmd === 'creator') {
    window.open(import.meta.env.VITE_CREATOR_URL || 'http://localhost:5174', '_blank')
  } else {
    router.push('/' + cmd)
  }
}

onMounted(async () => {
  if (userStore.isLogin) {
    try {
      unread.value = await apiUnread()
    } catch (e) {
      unread.value = 0
    }
  }
})
</script>

<style scoped>
.site-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: saturate(180%) blur(8px);
  border-bottom: 1px solid var(--border-light);
}
.header-inner {
  max-width: 1240px;
  height: 62px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 26px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}
.brand-mark {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--brand);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-name {
  font-size: 21px;
  font-weight: 700;
  letter-spacing: 1px;
  color: var(--text-1);
}
.main-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}
.nav-link {
  padding: 6px 12px;
  font-size: 14px;
  color: var(--text-2);
  border-radius: 6px;
  transition: background 0.15s, color 0.15s;
}
.nav-link:hover {
  background: #f4f5f7;
  color: var(--text-1);
}
.nav-link.active {
  color: var(--text-1);
  font-weight: 600;
}
.search {
  flex: 1;
  max-width: 380px;
  margin-left: auto;
}
.search :deep(.el-input__wrapper) {
  background: #f4f5f7;
  box-shadow: none;
  border: 1px solid transparent;
  height: 36px;
}
.search :deep(.el-input__wrapper:hover) {
  border-color: var(--border);
}
.search :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  border-color: var(--brand);
  box-shadow: none;
}
.search :deep(.el-input__inner) {
  height: 36px;
}
.actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.publish-btn {
  height: 34px;
  padding: 0 14px;
  border-color: var(--brand);
  color: var(--brand);
  background: #fff;
}
.publish-btn:hover {
  background: var(--brand-light);
  border-color: var(--brand);
  color: var(--brand);
}
.publish-btn :deep(.el-button) {
  gap: 6px;
}
.publish-btn span {
  margin-left: 4px;
}
.icon-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: var(--text-2);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-btn:hover {
  background: #f4f5f7;
  color: var(--text-1);
}
.avatar-btn {
  width: 34px;
  height: 34px;
  padding: 0;
  border: none;
  background: transparent;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}
.avatar-btn img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.menu-user {
  padding: 8px 12px 10px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 4px;
}
.menu-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
}
.menu-role {
  font-size: 12px;
  color: var(--text-3);
}
@media (max-width: 900px) {
  .main-nav {
    display: none;
  }
  .search {
    max-width: none;
  }
  .publish-btn span {
    display: none;
  }
}
@media (max-width: 640px) {
  .header-inner {
    gap: 12px;
    padding: 0 14px;
  }
  .brand-name {
    display: none;
  }
  .actions :deep(.el-button--primary) {
    display: none;
  }
}
</style>
