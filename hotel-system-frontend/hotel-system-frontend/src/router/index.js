import { createRouter, createWebHistory } from 'vue-router'

import LoginView from '@/views/LoginView.vue'
import ReservationView from '@/views/ReservationView.vue'
import ScheduleView from '@/views/ScheduleView.vue'
import RestaurantView from '@/views/RestaurantView.vue'

import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { requiresAuth: false } // ❗登入頁不需驗證
  },
  {
    path: '/reservations',
    name: 'Reservations',
    component: ReservationView,
    meta: { requiresAuth: true }
  },
  {
    path: '/schedules',
    name: 'Schedules',
    component: ScheduleView,
    meta: { requiresAuth: true }
  },
  {
    path: '/restaurants',
    name: 'Restaurants',
    component: RestaurantView,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 🔐 登入驗證守衛：放在 router 初始化之後
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
