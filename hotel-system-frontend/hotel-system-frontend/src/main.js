import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import piniaPersist from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/main.css'

const app = createApp(App)

const pinia = createPinia()
pinia.use(piniaPersist) // ✅ 啟用 plugin

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')


