import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 鐢ㄦ埛缃戦〉绔?Vite 閰嶇疆锛氱鍙?5173锛屽紑鍙戠幆澧冧唬鐞嗗埌鍚庣 8080
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 鎺ュ彛浠ｇ悊锛氬墠绔?/api 璇锋眰杞彂鍒板悗绔紝瑙勯伩璺ㄥ煙
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      // 闈欐€佽祫婧愪唬鐞嗭細瑙嗛/鍥剧墖璁块棶鍚庣 uploads 鐩綍
      '/upload': { target: 'http://localhost:8080', changeOrigin: true }
    }
  },
  build: {
    // 鍒嗗寘锛氭妸浣撶Н杈冨ぇ鐨勭涓夋柟搴撴媶鎴愮嫭绔?chunk锛屼究浜庢祻瑙堝櫒骞惰涓嬭浇涓庣紦瀛橈紝缂╁皬棣栧睆涓诲寘
    rollupOptions: {
      output: {
        manualChunks: {
          vue: ['vue', 'vue-router', 'pinia'],
          element: ['element-plus']
        }
      }
    }
  }
})

