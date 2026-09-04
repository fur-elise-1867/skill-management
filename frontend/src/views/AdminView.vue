<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { authService } from '@/services/authService'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UserResponse } from '@/types'

const auth = useAuthStore()
const users = ref<UserResponse[]>([])
const loading = ref(false)

async function fetchUsers() {
  loading.value = true
  try {
    users.value = await authService.getAllUsers()
  } catch (error: any) {
    ElMessage.error('Không thể tải danh sách người dùng.')
  } finally {
    loading.value = false
  }
}

async function handleDelete(email: string) {
  try {
    await ElMessageBox.confirm(
      `Bạn có chắc muốn xóa người dùng "${email}"?`,
      'Xác nhận xóa',
      { confirmButtonText: 'Xóa', cancelButtonText: 'Hủy', type: 'warning' },
    )
    await authService.deleteUser(email)
    ElMessage.success('Đã xóa người dùng thành công.')
    await fetchUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Xóa người dùng thất bại.')
    }
  }
}

onMounted(fetchUsers)
</script>

<template>
  <div class="admin-page">
    <div class="admin-header">
      <div>
        <h1>Quản trị người dùng</h1>
        <p>Quản lý tất cả tài khoản trong hệ thống</p>
      </div>
      <div class="header-actions">
        <el-button @click="fetchUsers" :loading="loading" type="primary" plain>
          Làm mới
        </el-button>
        <el-button @click="$router.push('/dashboard')" plain>
          ← Dashboard
        </el-button>
        <el-button @click="auth.logout()" type="danger" plain>
          Đăng xuất
        </el-button>
      </div>
    </div>

    <el-divider />

    <el-table
      :data="users"
      v-loading="loading"
      stripe
      border
      style="width: 100%"
      size="large"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="Họ tên" min-width="150" />
      <el-table-column prop="email" label="Email" min-width="200" />
      <el-table-column prop="gender" label="Giới tính" width="100">
        <template #default="{ row }">
          {{ row.gender || '—' }}
        </template>
      </el-table-column>
      <el-table-column prop="mobile" label="SĐT" width="130">
        <template #default="{ row }">
          {{ row.mobile || '—' }}
        </template>
      </el-table-column>
      <el-table-column prop="role" label="Vai trò" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
            {{ row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Hành động" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            type="danger"
            size="small"
            plain
            @click="handleDelete(row.email)"
            :disabled="row.email === auth.user?.email"
          >
            Xóa
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="stats" v-if="!loading">
      <el-tag type="info" size="large">Tổng: {{ users.length }} người dùng</el-tag>
    </div>
  </div>
</template>

<style scoped>
.admin-page {
  max-width: 1100px;
  margin: 40px auto;
  padding: 0 24px;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.admin-header h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
}

.admin-header p {
  margin: 4px 0 0 0;
  color: #6b7280;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats {
  margin-top: 16px;
  text-align: right;
}
</style>
