import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 创作者商家端：5174 端口，/api 与 /upload 代理到后端 8080
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5174,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/upload': { target: 'http://localhost:8080', changeOrigin: true }
    }
  },
  build: {
    // 分包：把体积较大的第三方库拆成独立 chunk，便于并行下载与长期缓存
    rollupOptions: {
      output: {
        manualChunks: {
          vue: ['vue', 'vue-router', 'pinia'],
          element: ['element-plus'],
          echarts: ['echarts']
        }
      }
    }
  }
})
