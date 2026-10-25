import { createRouter, createWebHashHistory } from "vue-router";

/**
 * 用户端路由（Hash 模式）
 * - meta.hideChrome：登录/注册页隐藏全局站点导航
 * - meta.requiresAuth：需要登录后访问
 * - meta.title：浏览器标题
 */
const routes = [
  {
    path: "/",
    component: () => import("../views/Home.vue"),
    meta: { title: "首页" },
  },
  {
    path: "/video/:id",
    component: () => import("../views/VideoPlay.vue"),
    meta: { title: "视频详情" },
  },
  {
    path: "/search",
    component: () => import("../views/Search.vue"),
    meta: { title: "搜索" },
  },
  {
    path: "/category/:id",
    component: () => import("../views/Category.vue"),
    meta: { title: "频道" },
  },
  {
    path: "/user/:id",
    component: () => import("../views/UserHome.vue"),
    meta: { title: "创作者主页" },
  },
  {
    path: "/mall",
    component: () => import("../views/Mall.vue"),
    meta: { title: "商城" },
  },
  {
    path: "/product/:id",
    component: () => import("../views/ProductDetail.vue"),
    meta: { title: "商品详情" },
  },
  {
    path: "/shop/:id",
    component: () => import("../views/Shop.vue"),
    meta: { title: "店铺" },
  },
  {
    path: "/live",
    component: () => import("../views/Live.vue"),
    meta: { title: "直播" },
  },
  {
    path: "/login",
    component: () => import("../views/Login.vue"),
    meta: { hideChrome: true, title: "登录" },
  },
  {
    path: "/register",
    component: () => import("../views/Register.vue"),
    meta: { hideChrome: true, title: "注册" },
  },
  {
    path: "/publish",
    component: () => import("../views/Publish.vue"),
    meta: { requiresAuth: true, title: "发布视频" },
  },
  {
    path: "/profile",
    component: () => import("../views/Profile.vue"),
    meta: { requiresAuth: true, title: "个人中心" },
  },
  {
    path: "/messages",
    component: () => import("../views/Messages.vue"),
    meta: { requiresAuth: true, title: "消息中心" },
  },
  {
    path: "/orders",
    component: () => import("../views/Orders.vue"),
    meta: { requiresAuth: true, title: "我的订单" },
  },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("xh_token");
  if (to.meta.requiresAuth && !token) {
    next({ path: "/login", query: { redirect: to.fullPath } });
  } else {
    next();
  }
});

router.afterEach((to) => {
  document.title = to.meta.title
    ? `${to.meta.title} - 星海`
    : "星海 · 视频社区";
});

export default router;
