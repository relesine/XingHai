import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import App from "./App.vue";
import router from "./router";
import "./styles/index.css";

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(ElementPlus, { locale: zhCn });

// 全局错误兜底：避免单个组件异常导致整页白屏
app.config.errorHandler = (err, instance, info) => {
  console.error("[XingHai] 未捕获异常:", info, err);
};

app.mount("#app");
