<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

onMounted(async () => {
  if (auth.isAuthenticated && !auth.user) {
    await auth.fetchCurrentUser()
  }
})
</script>

<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <div class="header-left">
        <h1>Dashboard</h1>
        <p>Chào mừng, {{ auth.user?.name || 'User' }}</p>
      </div>
      <div class="header-right">
        <el-tag v-if="auth.isAdmin" type="danger" size="large">ADMIN</el-tag>
        <el-tag v-else type="primary" size="large">USER</el-tag>
        <el-button v-if="auth.isAdmin" @click="$router.push('/admin')" type="warning" plain>
          Quản trị
        </el-button>
        <el-button @click="auth.logout()" type="danger" plain>
          Đăng xuất
        </el-button>
      </div>
    </div>

    <el-divider />

    <div class="profile-section" v-if="auth.user">
      <h2>Thông tin cá nhân</h2>
      <el-descriptions :column="2" border size="large">
        <el-descriptions-item label="ID">{{ auth.user.id }}</el-descriptions-item>
        <el-descriptions-item label="Họ tên">{{ auth.user.name }}</el-descriptions-item>
        <el-descriptions-item label="Email">{{ auth.user.email }}</el-descriptions-item>
        <el-descriptions-item label="Giới tính">{{ auth.user.gender || '—' }}</el-descriptions-item>
        <el-descriptions-item label="Số điện thoại">{{ auth.user.mobile || '—' }}</el-descriptions-item>
        <el-descriptions-item label="Vai trò">
          <el-tag :type="auth.user.role?.name === 'ADMIN' ? 'danger' : 'primary'">
            {{ auth.user.role?.name }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Ngày tạo">
          {{ auth.user.createdAt ? new Date(auth.user.createdAt).toLocaleString('vi-VN') : '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="Cập nhật lần cuối">
          {{ auth.user.updatedAt ? new Date(auth.user.updatedAt).toLocaleString('vi-VN') : '—' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <el-skeleton :rows="5" animated v-else />
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 24px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
}

.header-left p {
  margin: 4px 0 0 0;
  color: #6b7280;
  font-size: 16px;
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.profile-section h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 16px;
}
</style>
