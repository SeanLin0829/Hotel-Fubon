<template>
  <el-aside width="220px" class="sidebar">
    <div class="sidebar-header">
      <strong>飯店管理系統</strong>
    </div>

    <el-menu
      :default-active="route.path"
      class="el-menu-vertical-demo"
      router
      background-color="#304156"
      text-color="#fff"
      active-text-color="#409EFF"
    >

      <el-menu-item index="/reservations">
        <el-icon><HomeFilled /></el-icon>
        <span>訂房管理</span>
      </el-menu-item>

      <el-menu-item index="/schedules">
        <el-icon><Calendar /></el-icon>
        <span>班表安排</span>
      </el-menu-item>

      <el-menu-item index="/restaurants">
        <el-icon><Dish /></el-icon>
        <span>餐廳訂位</span>
      </el-menu-item>
    </el-menu>

    <div class="sidebar-footer" v-if="userStore.isLoggedIn">
      <div class="user-info">
        {{ userStore.user.fullName || userStore.user.username }}
      </div>
      <el-button type="danger" size="small" @click="logout">登出</el-button>
    </div>
  </el-aside>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter, useRoute } from 'vue-router'
import {
  Monitor,
  User,
  HomeFilled,
  Calendar,
  Dish
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.sidebar {
  position: relative;
  height: 100vh;
  background-color: #304156;
  color: white;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  font-size: 20px;
  text-align: center;
  border-bottom: 1px solid #1f2d3d;
}

.el-menu-vertical-demo {
  flex-grow: 1;
  overflow-y: auto;
}

.sidebar-footer {
  position: absolute;
  bottom: 0;
  width: 100%;
  padding: 16px;
  border-top: 1px solid #1f2d3d;
  text-align: center;
  background-color: #304156;
}

.user-info {
  margin-bottom: 10px;
  font-size: 14px;
  color: #bbb;
}

</style>
