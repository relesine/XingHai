<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand" @click="$router.push('/dashboard')">
        <span class="brand-mark">星</span>
        <span class="brand-text">
          <strong>星海</strong>
          <em>创作者中心</em>
        </span>
      </div>

      <nav class="nav">
        <router-link v-for="item in menu" :key="item.path" :to="item.path" class="nav-item"
          :class="{ active: $route.path === item.path }">
          <AppIcon :name="item.icon" :size="18" />
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-foot">
        <span>已认证创作者</span>
        <span class="foot-badge">CREATOR</span>
      </div>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="crumb">
          <span>创作者中心</span>
          <AppIcon name="chevron-right" :size="14" />
          <b>{{ $route.meta.title }}</b>
        </div>

        <div class="right">
          <button class="link-btn" @click="openUserSite">
            <AppIcon name="external" :size="14" />用户端
          </button>
          <span class="divider"></span>
          <el-dropdown trigger="click" @command="onCmd">
            <button class="user-btn">
              <img :src="store.user?.avatar || '/upload/avatar/c1.svg'" class="avatar" alt="" />
              <span class="user-meta">
                <span class="user-name">{{ store.user?.nickname || '创作者' }}</span>
                <span class="user-role">{{ store.user?.username }}</span>
              </span>
              <AppIcon name="chevron-down" :size="14" />
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <span class="menu-icon">
                    <AppIcon name="settings" :size="15" />
                  </span>账号设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <span class="menu-icon">
                    <AppIcon name="logout" :size="15" />
                  </span>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content"><router-view /></main>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useCreatorStore } from '../stores/creator'
import AppIcon from '../components/AppIcon.vue'

const router = useRouter()
const store = useCreatorStore()

const menu = [
  { path: '/dashboard', label: '数据看板', icon: 'dashboard' },
  { path: '/videos', label: '作品管理', icon: 'film' },
  { path: '/shop', label: '店铺管理', icon: 'shop' },
  { path: '/products', label: '商品管理', icon: 'bag' },
  { path: '/orders', label: '订单处理', icon: 'receipt' },
  { path: '/live', label: '直播管理', icon: 'live' },
  { path: '/profile', label: '账号设置', icon: 'settings' }
]

// 用户端是独立工程（独立端口），属于外部链接，必须用 window.open 新开页签
const USER_SITE_URL = import.meta.env.VITE_USER_URL || 'http://localhost:5173'
function openUserSite() {
  window.open(USER_SITE_URL, '_blank')
}

function onCmd(cmd) {
  if (cmd === 'logout') {
    store.logout()
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg);
}

.sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
}

.brand {
  height: var(--header-height);
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 0 18px;
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
}

.brand-mark {
  width: 28px;
  height: 28px;
  border-radius: 7px;
  background: var(--primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.brand-text {
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.brand-text strong {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-1);
  letter-spacing: .5px;
}

.brand-text em {
  font-style: normal;
  font-size: 11px;
  color: var(--text-3);
}

.nav {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 38px;
  padding: 0 12px;
  margin-bottom: 2px;
  border-radius: var(--radius);
  color: var(--text-2);
  font-size: 13.5px;
  transition: background .15s, color .15s;
}

.nav-item:hover {
  background: #f7f8fa;
  color: var(--text-1);
}

.nav-item.active {
  background: var(--primary-light);
  color: var(--primary);
  font-weight: 500;
}

.sidebar-foot {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 18px;
  font-size: 12px;
  color: var(--text-3);
  border-top: 1px solid var(--border-light);
}

.foot-badge {
  font-size: 10px;
  color: var(--primary);
  background: var(--primary-light);
  border-radius: 3px;
  padding: 1px 5px;
  letter-spacing: .3px;
}

.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: var(--header-height);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid var(--border-light);
  position: sticky;
  top: 0;
  z-index: 10;
}

.crumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-3);
}

.crumb b {
  font-weight: 500;
  color: var(--text-1);
}

.right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.link-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 30px;
  padding: 0 9px;
  border: 0;
  border-radius: var(--radius);
  background: transparent;
  color: var(--text-2);
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  transition: background .15s, color .15s;
}

.link-btn:hover {
  background: #f7f8fa;
  color: var(--primary);
}

.divider {
  width: 1px;
  height: 18px;
  background: var(--border);
  margin: 0 4px;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px 4px 4px;
  border: 0;
  border-radius: var(--radius);
  background: transparent;
  cursor: pointer;
  font-family: inherit;
  transition: background .15s;
}

.user-btn:hover {
  background: #f7f8fa;
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
  text-align: left;
}

.user-name {
  font-size: 13px;
  color: var(--text-1);
}

.user-role {
  font-size: 11px;
  color: var(--text-3);
}

.menu-icon {
  display: inline-flex;
  margin-right: 6px;
  vertical-align: -2px;
}

.content {
  flex: 1;
  padding: 20px 24px 32px;
}

@media (max-width: 900px) {
  .sidebar {
    width: 64px;
  }

  .brand-text,
  .nav-item span,
  .sidebar-foot span {
    display: none;
  }

  .brand {
    justify-content: center;
    padding: 0;
  }

  .nav-item {
    justify-content: center;
    padding: 0;
  }
}
</style>
