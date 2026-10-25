import { createRouter, createWebHashHistory } from 'vue-router'

// hash 路由：登录页 + 后台各功能页（均需管理员登录）
const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { title: '登录' } },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '数据大盘', auth: true } },
      { path: 'users', component: () => import('../views/Users.vue'), meta: { title: '用户管理', auth: true } },
      { path: 'audit', component: () => import('../views/Audit.vue'), meta: { title: '视频审核', auth: true } },
      { path: 'reports', component: () => import('../views/Reports.vue'), meta: { title: '举报处理', auth: true } },
      { path: 'notices', component: () => import('../views/Notices.vue'), meta: { title: '公告管理', auth: true } },
      { path: 'adverts', component: () => import('../views/Adverts.vue'), meta: { title: '广告管理', auth: true } },
      { path: 'live', component: () => import('../views/Live.vue'), meta: { title: '直播管理', auth: true } },
      { path: 'roles', component: () => import('../views/Roles.vue'), meta: { title: '权限管理', auth: true } }
    ]
  }
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach((to, from, next) => {
  if (to.meta.auth && !localStorage.getItem('xh_admin_token')) {
    next('/login')
  } else {
    next()
  }
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 星海管理后台` : '星海管理后台'
})

export default router
