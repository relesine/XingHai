import { createRouter, createWebHashHistory } from "vue-router";

const routes = [
  { path: "/login", component: () => import("../views/Login.vue") },
  {
    path: "/",
    component: () => import("../views/Layout.vue"),
    meta: { requiresAuth: true },
    children: [
      { path: "", redirect: "/dashboard" },
      {
        path: "dashboard",
        component: () => import("../views/Dashboard.vue"),
        meta: { title: "数据看板" },
      },
      {
        path: "videos",
        component: () => import("../views/Videos.vue"),
        meta: { title: "作品管理" },
      },
      {
        path: "shop",
        component: () => import("../views/Shop.vue"),
        meta: { title: "店铺管理" },
      },
      {
        path: "products",
        component: () => import("../views/Products.vue"),
        meta: { title: "商品管理" },
      },
      {
        path: "orders",
        component: () => import("../views/Orders.vue"),
        meta: { title: "订单处理" },
      },
      {
        path: "live",
        component: () => import("../views/Live.vue"),
        meta: { title: "直播管理" },
      },
      {
        path: "profile",
        component: () => import("../views/Profile.vue"),
        meta: { title: "账号设置" },
      },
    ],
  },
];

const router = createRouter({ history: createWebHashHistory(), routes });

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("xh_creator_token");
  if (to.meta.requiresAuth && !token) next("/login");
  else next();
});

router.afterEach((to) => {
  document.title = to.meta.title
    ? `${to.meta.title} - 星海创作者中心`
    : "星海创作者中心";
});

export default router;
