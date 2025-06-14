<template>
  <el-container class="app-container">
    <Sidebar />
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import Sidebar from '@/components/Sidebar.vue'
</script>

<style scoped>
.app-container {
  height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.main-content {
  padding: 0;       /* 移除內邊距 */
  margin: 0;        /* 移除外邊距 */
  height: 100%;
  background-color: #f5f7fa;
}
</style>