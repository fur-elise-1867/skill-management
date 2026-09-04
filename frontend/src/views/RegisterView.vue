<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()

const form = ref({
  name: '',
  email: '',
  password: '',
  gender: '',
  mobile: '',
})

async function handleRegister() {
  try {
    await auth.register(
      form.value.name,
      form.value.email,
      form.value.password,
      form.value.gender || undefined,
      form.value.mobile || undefined,
    )
    ElMessage.success('Đăng ký thành công!')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || 'Đăng ký thất bại. Vui lòng thử lại.')
  }
}
</script>

<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h1>Tạo tài khoản</h1>
        <p>Đăng ký tài khoản Skill Management</p>
      </div>

      <el-form @submit.prevent="handleRegister" label-position="top" size="large">
        <el-form-item label="Họ và tên" required>
          <el-input v-model="form.name" placeholder="Nguyễn Văn A" />
        </el-form-item>

        <el-form-item label="Email" required>
          <el-input v-model="form.email" type="email" placeholder="email@example.com" />
        </el-form-item>

        <el-form-item label="Mật khẩu" required>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="Tối thiểu 8 ký tự"
            show-password
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Giới tính">
              <el-select v-model="form.gender" placeholder="Chọn" style="width: 100%">
                <el-option label="Nam" value="Male" />
                <el-option label="Nữ" value="Female" />
                <el-option label="Khác" value="Other" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Số điện thoại">
              <el-input v-model="form.mobile" placeholder="0123456789" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="auth.loading"
            style="width: 100%"
          >
            Đăng ký
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <p>Đã có tài khoản? <router-link to="/login">Đăng nhập</router-link></p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 480px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.register-header p {
  color: #6b7280;
  margin: 0;
  font-size: 14px;
}

.register-footer {
  text-align: center;
  margin-top: 16px;
  color: #6b7280;
  font-size: 14px;
}

.register-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

.register-footer a:hover {
  text-decoration: underline;
}
</style>
