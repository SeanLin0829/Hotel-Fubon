import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

import path from 'path' // 要匯入 path 模組

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 你的 Spring Boot 埠號
        changeOrigin: true,
        // rewrite: path => path.replace(/^\/api/, '') // 通常不需要
      }
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src') // 定義 @ 為 src 資料夾
    }
  }
})
