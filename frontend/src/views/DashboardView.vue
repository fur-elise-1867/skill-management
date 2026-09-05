<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

onMounted(async () => {
  if (auth.isAuthenticated && !auth.user) {
    await auth.fetchCurrentUser()
  }
})

function getRoleTagType(roleName?: string) {
  if (roleName === 'ADMIN') return 'danger'
  if (roleName === 'EDITOR') return 'warning'
  return 'primary'
}
</script>

<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <div class="header-left">
        <el-avatar
          :size="54"
          :src="auth.user?.avatarUrl || ''"
          class="header-avatar"
        >
          {{ auth.user?.name?.charAt(0).toUpperCase() || 'U' }}
        </el-avatar>
        <div class="header-titles">
          <h1>Dashboard</h1>
          <p>Chào mừng, <strong>{{ auth.user?.name || 'User' }}</strong></p>
        </div>
      </div>
      <div class="header-right">
        <el-tag :type="getRoleTagType(auth.user?.role?.name)" size="large" effect="dark">
          {{ auth.user?.role?.name || 'USER' }}
        </el-tag>
        <el-button @click="$router.push('/account')" type="primary" plain>
          Thông tin tài khoản
        </el-button>
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
      <div class="section-title-row">
        <h2>Thông tin tổng quan</h2>
        <el-button link type="primary" @click="$router.push('/account')">
          Chỉnh sửa thông tin & Đổi ảnh đại diện →
        </el-button>
      </div>

      <el-descriptions :column="2" border size="large">
        <el-descriptions-item label="ID">{{ auth.user.id }}</el-descriptions-item>
        <el-descriptions-item label="Họ tên">{{ auth.user.name }}</el-descriptions-item>
        <el-descriptions-item label="Email">{{ auth.user.email }}</el-descriptions-item>
        <el-descriptions-item label="Giới tính">{{ auth.user.gender || '—' }}</el-descriptions-item>
        <el-descriptions-item label="Số điện thoại">{{ auth.user.mobile || '—' }}</el-descriptions-item>
        <el-descriptions-item label="Vai trò">
          <el-tag :type="getRoleTagType(auth.user.role?.name)">
            {{ auth.user.role?.name }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Trạng thái">
          <el-tag :type="auth.user.enabled ? 'success' : 'danger'">
            {{ auth.user.enabled ? 'Hoạt động (Active)' : 'Đã khóa (Inactive)' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Ngày tạo">
          {{ auth.user.createdAt ? new Date(auth.user.createdAt).toLocaleString('vi-VN') : '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="Cập nhật lần cuối" :span="2">
          {{ auth.user.updatedAt ? new Date(auth.user.updatedAt).toLocaleString('vi-VN') : '—' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <el-skeleton :rows="5" animated v-else />
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 960px;
  margin: 40px auto;
  padding: 0 24px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-avatar {
  background: #667eea;
  color: #ffffff;
  font-size: 22px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.header-titles h1 {
  margin: 0;
  font-size: 30px;
  font-weight: 700;
  color: #1a1a2e;
}

.header-titles p {
  margin: 4px 0 0 0;
  color: #6b7280;
  font-size: 15px;
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title-row h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}
</style>
