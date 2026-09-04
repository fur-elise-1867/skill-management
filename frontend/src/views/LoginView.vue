<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()

const form = ref({
  email: '',
  password: '',
})

async function handleLogin() {
  try {
    await auth.login(form.value.email, form.value.password)
    ElMessage.success('Đăng nhập thành công!')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.')
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>Skill Management</h1>
        <p>Đăng nhập vào hệ thống</p>
      </div>

      <el-form @submit.prevent="handleLogin" label-position="top" size="large">
        <el-form-item label="Email">
          <el-input
            v-model="form.email"
            type="email"
            placeholder="email@example.com"
            prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item label="Mật khẩu">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="Nhập mật khẩu"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="auth.loading"
            style="width: 100%"
          >
            Đăng nhập
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>Chưa có tài khoản? <router-link to="/register">Đăng ký ngay</router-link></p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.login-header p {
  color: #6b7280;
  margin: 0;
  font-size: 14px;
}

.login-footer {
  text-align: center;
  margin-top: 16px;
  color: #6b7280;
  font-size: 14px;
}

.login-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

.login-footer a:hover {
  text-decoration: underline;
}
</style>
