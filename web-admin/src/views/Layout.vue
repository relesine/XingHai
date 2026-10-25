<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand" @click="$router.push('/dashboard')">
        <span class="brand-mark">星</span>
        <span class="brand-text">
          <strong>星海</strong>
          <em>管理后台</em>
        </span>
      </div>

      <nav class="nav">
        <router-link v-for="item in menu" :key="item.path" :to="item.path" class="nav-item"
          :class="{ active: $route.path === item.path }">
          <AppIcon :name="item.icon" :size="18" />
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-foot">v1.0.0 · 企业版</div>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="crumb">
          <span>控制台</span>
          <AppIcon name="chevron-right" :size="14" />
          <b>{{ $route.meta.title }}</b>
        </div>

        <div class="right">
          <button class="link-btn" @click="openUserSite">
            <AppIcon name="external" :size="14" />用户端
          </button>
          <button class="link-btn" @click="openCreatorSite">
            <AppIcon name="external" :size="14" />创作者端
          </button>
          <span class="divider"></span>
          <el-dropdown trigger="click" @command="onCmd">
            <button class="user-btn">
              <span class="avatar">{{ initial }}</span>
              <span class="user-meta">
                <span class="user-name">{{ store.user?.nickname || '管理员' }}</span>
                <span class="user-role">{{ store.user?.username }}</span>
              </span>
              <AppIcon name="chevron-down" :size="14" />
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
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
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '../stores/admin'
import AppIcon from '../components/AppIcon.vue'

const router = useRouter()
const store = useAdminStore()

const menu = [
  { path: '/dashboard', label: '数据大盘', icon: 'grid' },
  { path: '/users', label: '用户管理', icon: 'users' },
  { path: '/audit', label: '视频审核', icon: 'film' },
  { path: '/reports', label: '举报处理', icon: 'flag' },
  { path: '/notices', label: '公告管理', icon: 'megaphone' },
  { path: '/adverts', label: '广告管理', icon: 'bell' },
  { path: '/live', label: '直播管理', icon: 'live' },
  { path: '/roles', label: '权限管理', icon: 'shield' }
]

const initial = computed(() => (store.user?.nickname || '管').slice(0, 1))

// 用户端/创作者端为独立工程（独立端口），属于外部链接，需新开页签
const USER_SITE_URL = import.meta.env.VITE_USER_URL || 'http://localhost:5173'
const CREATOR_SITE_URL = import.meta.env.VITE_CREATOR_URL || 'http://localhost:5174'
function openUserSite() { window.open(USER_SITE_URL, '_blank') }
function openCreatorSite() { window.open(CREATOR_SITE_URL, '_blank') }

function onCmd(cmd) {
  if (cmd === 'logout') {
    store.logout()
    router.push('/login')
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
  padding: 10px 10px;
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
  padding: 14px 18px;
  font-size: 11px;
  color: var(--text-4);
  border-top: 1px solid var(--border-light);
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
  gap: 4px;
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
  margin: 0 8px;
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
  background: var(--primary-light);
  color: var(--primary);
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
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
  .nav-item span:not(.nav-badge),
  .sidebar-foot {
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
