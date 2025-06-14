<template>
  <div class="login-container">
    <el-form @submit.prevent="handleLogin" label-width="80px" class="login-form">
      <el-form-item label="帳號">
        <el-input v-model="username" placeholder="請輸入帳號" />
      </el-form-item>

      <el-form-item label="密碼">
        <el-input v-model="password" type="password" placeholder="請輸入密碼" show-password />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleLogin" style="width: 100%">登入</el-button>
      </el-form-item>

      <el-alert
        v-if="errorMsg"
        type="error"
        :title="errorMsg"
        show-icon
        :closable="false"
        style="margin-top: 10px"
      />
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import api from '@/api/axios'

const username = ref('')
const password = ref('')
const errorMsg = ref('')
const router = useRouter()
const userStore = useUserStore()

async function handleLogin() {
  errorMsg.value = ''
  try {
    const response = await api.post('/users/login', {
      username: username.value,
      password: password.value
    })

    const user = response.data

    // ✅ 僅允許 admin 或 staff 登入
    if (!['admin', 'staff'].includes(user.role)) {
      throw new Error('此帳號無後台登入權限')
    }

    userStore.login(user)
    router.push('/dashboard')
  } catch (e) {
    errorMsg.value = e.response?.data?.message || e.message || '登入失敗'
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 90vh;
}
.login-form {
  width: 400px;
  padding: 40px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

</style>
