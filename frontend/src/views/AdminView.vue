<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminService } from '@/services/adminService'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { RoleResponse, UserResponse } from '@/types'

const auth = useAuthStore()
const users = ref<UserResponse[]>([])
const roles = ref<RoleResponse[]>([])
const loading = ref(false)

// Dialog tạo tài khoản
const createDialogVisible = ref(false)
const createLoading = ref(false)
const createForm = ref({
  name: '',
  email: '',
  password: '',
  roleId: null as number | null,
  gender: '',
  mobile: '',
  enabled: true,
})

// Dialog reset mật khẩu
const resetDialogVisible = ref(false)
const resetUserEmail = ref('')
const generatedPassword = ref('')

async function fetchData() {
  loading.value = true
  try {
    const [fetchedUsers, fetchedRoles] = await Promise.all([
      adminService.getAllUsers(),
      adminService.getRoles(),
    ])
    users.value = fetchedUsers
    roles.value = fetchedRoles
  } catch (error: any) {
    ElMessage.error('Không thể tải danh sách người dùng hoặc vai trò.')
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  // Gán roleId mặc định là role USER nếu có
  const defaultUserRole = roles.value.find((r) => r.name === 'USER')
  createForm.value = {
    name: '',
    email: '',
    password: '',
    roleId: defaultUserRole ? defaultUserRole.id : roles.value[0]?.id || null,
    gender: '',
    mobile: '',
    enabled: true,
  }
  createDialogVisible.value = true
}

async function handleCreateUser() {
  if (!createForm.value.name.trim()) {
    ElMessage.warning('Vui lòng nhập họ và tên.')
    return
  }
  if (!createForm.value.email.trim()) {
    ElMessage.warning('Vui lòng nhập email.')
    return
  }
  if (!createForm.value.password || createForm.value.password.length < 8) {
    ElMessage.warning('Mật khẩu phải có tối thiểu 8 ký tự.')
    return
  }
  if (!createForm.value.roleId) {
    ElMessage.warning('Vui lòng chọn vai trò.')
    return
  }

  createLoading.value = true
  try {
    await adminService.createUser({
      name: createForm.value.name.trim(),
      email: createForm.value.email.trim(),
      password: createForm.value.password,
      roleId: createForm.value.roleId,
      gender: createForm.value.gender || undefined,
      mobile: createForm.value.mobile.trim() || undefined,
      enabled: createForm.value.enabled,
    })
    ElMessage.success('Tạo tài khoản mới thành công!')
    createDialogVisible.value = false
    await fetchData()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || 'Tạo tài khoản thất bại.')
  } finally {
    createLoading.value = false
  }
}

async function handleRoleChange(user: UserResponse, newRoleId: number) {
  const newRole = roles.value.find((r) => r.id === newRoleId)
  if (!newRole) return

  try {
    await ElMessageBox.confirm(
      `Bạn có chắc muốn đổi vai trò của "${user.name}" thành "${newRole.name}"?`,
      'Xác nhận thay đổi vai trò',
      { confirmButtonText: 'Đổi vai trò', cancelButtonText: 'Hủy', type: 'info' },
    )
    const updated = await adminService.updateRole(user.id, { roleId: newRoleId })
    user.role = updated.role
    ElMessage.success(`Đã cập nhật vai trò của "${user.name}" thành "${newRole.name}".`)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || 'Đổi vai trò thất bại.')
    }
    // Re-fetch để khôi phục giá trị hiển thị cũ nếu có lỗi
    await fetchData()
  }
}

async function handleStatusChange(user: UserResponse, newStatus: boolean) {
  try {
    await adminService.updateStatus(user.id, { enabled: newStatus })
    ElMessage.success(
      `Đã ${newStatus ? 'kích hoạt' : 'vô hiệu hóa'} tài khoản "${user.name}".`,
    )
  } catch (error: any) {
    user.enabled = !newStatus // revert switch state
    ElMessage.error(error.response?.data?.message || 'Cập nhật trạng thái thất bại.')
  }
}

async function handleResetPassword(user: UserResponse) {
  try {
    await ElMessageBox.confirm(
      `Hệ thống sẽ tạo một mật khẩu ngẫu nhiên mới cho tài khoản "${user.email}". Bạn có muốn tiếp tục?`,
      'Xác nhận đặt lại mật khẩu',
      { confirmButtonText: 'Tạo mật khẩu mới', cancelButtonText: 'Hủy', type: 'warning' },
    )
    const res = await adminService.resetPassword(user.id)
    generatedPassword.value = res.newPassword
    resetUserEmail.value = user.email
    resetDialogVisible.value = true
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || 'Đặt lại mật khẩu thất bại.')
    }
  }
}

async function copyPassword() {
  try {
    await navigator.clipboard.writeText(generatedPassword.value)
    ElMessage.success('Đã sao chép mật khẩu vào bộ nhớ tạm!')
  } catch {
    ElMessage.info('Không thể tự động sao chép. Vui lòng bôi đen và copy thủ công.')
  }
}

async function handleDelete(user: UserResponse) {
  try {
    await ElMessageBox.confirm(
      `Bạn có chắc chắn muốn xóa vĩnh viễn người dùng "${user.name}" (${user.email})?`,
      'Xác nhận xóa tài khoản',
      { confirmButtonText: 'Xóa vĩnh viễn', cancelButtonText: 'Hủy', type: 'error' },
    )
    await adminService.deleteUserById(user.id)
    ElMessage.success('Đã xóa người dùng thành công.')
    await fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || 'Xóa người dùng thất bại.')
    }
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="admin-page">
    <div class="admin-header">
      <div>
        <h1>Quản trị người dùng</h1>
        <p>Quản lý tài khoản, vai trò và phân quyền trong hệ thống</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="openCreateDialog">
          + Thêm tài khoản
        </el-button>
        <el-button @click="fetchData" :loading="loading" plain>
          Làm mới
        </el-button>
        <el-button @click="$router.push('/account')" plain>
          Thông tin tài khoản
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
      <el-table-column label="Avatar" width="80" align="center">
        <template #default="{ row }">
          <el-avatar :size="40" :src="row.avatarUrl || ''">
            {{ row.name?.charAt(0).toUpperCase() || 'U' }}
          </el-avatar>
        </template>
      </el-table-column>

      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="name" label="Họ tên" min-width="140" />
      <el-table-column prop="email" label="Email" min-width="190" />

      <el-table-column prop="gender" label="Giới tính" width="95" align="center">
        <template #default="{ row }">
          {{ row.gender || '—' }}
        </template>
      </el-table-column>

      <el-table-column prop="mobile" label="SĐT" width="120" align="center">
        <template #default="{ row }">
          {{ row.mobile || '—' }}
        </template>
      </el-table-column>

      <!-- Vai trò (Role) với logic bảo vệ Admin -->
      <el-table-column label="Vai trò" width="160">
        <template #default="{ row }">
          <!-- Nếu là ADMIN: vô hiệu hóa đổi role kèm Tooltip -->
          <el-tooltip
            v-if="row.role?.name === 'ADMIN'"
            content="Không thể thay đổi hoặc hạ cấp tài khoản Quản trị viên (Admin)"
            placement="top"
          >
            <el-select :model-value="row.role?.id" disabled size="default" style="width: 100%">
              <el-option
                v-for="r in roles"
                :key="r.id"
                :label="r.name"
                :value="r.id"
              />
            </el-select>
          </el-tooltip>

          <!-- Nếu không phải ADMIN: cho phép đổi role -->
          <el-select
            v-else
            :model-value="row.role?.id"
            @change="(val: number) => handleRoleChange(row, val)"
            size="default"
            style="width: 100%"
          >
            <el-option
              v-for="r in roles"
              :key="r.id"
              :label="r.name"
              :value="r.id"
            />
          </el-select>
        </template>
      </el-table-column>

      <!-- Trạng thái (Active / Inactive) -->
      <el-table-column label="Trạng thái" width="120" align="center">
        <template #default="{ row }">
          <el-tooltip
            v-if="row.email === auth.user?.email"
            content="Không thể tự khóa tài khoản của chính mình"
            placement="top"
          >
            <el-switch
              v-model="row.enabled"
              disabled
              active-color="#13ce66"
              inactive-color="#ff4949"
            />
          </el-tooltip>
          <el-switch
            v-else
            v-model="row.enabled"
            @change="(val: any) => handleStatusChange(row, !!val)"
            active-color="#13ce66"
            inactive-color="#ff4949"
          />
        </template>
      </el-table-column>

      <!-- Hành động (Reset Password, Delete) -->
      <el-table-column label="Hành động" width="170" fixed="right" align="center">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button
              type="warning"
              size="small"
              plain
              @click="handleResetPassword(row)"
            >
              Reset MK
            </el-button>

            <el-button
              type="danger"
              size="small"
              plain
              @click="handleDelete(row)"
              :disabled="row.email === auth.user?.email"
            >
              Xóa
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="stats" v-if="!loading">
      <el-tag type="info" size="large">Tổng số: {{ users.length }} người dùng</el-tag>
    </div>

    <!-- Dialog thêm người dùng mới -->
    <el-dialog
      v-model="createDialogVisible"
      title="Thêm tài khoản mới"
      width="520px"
      destroy-on-close
    >
      <el-form label-position="top" size="large">
        <el-form-item label="Họ và tên" required>
          <el-input v-model="createForm.name" placeholder="Nguyễn Văn A" prefix-icon="User" />
        </el-form-item>

        <el-form-item label="Email đăng nhập" required>
          <el-input v-model="createForm.email" type="email" placeholder="email@example.com" prefix-icon="Message" />
        </el-form-item>

        <el-form-item label="Mật khẩu ban đầu" required>
          <el-input
            v-model="createForm.password"
            type="password"
            placeholder="Tối thiểu 8 ký tự"
            show-password
            prefix-icon="Lock"
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Vai trò" required>
              <el-select v-model="createForm.roleId" placeholder="Chọn vai trò" style="width: 100%">
                <el-option
                  v-for="r in roles"
                  :key="r.id"
                  :label="r.name + ' (' + (r.description || '') + ')'"
                  :value="r.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Giới tính">
              <el-select v-model="createForm.gender" placeholder="Chọn" style="width: 100%">
                <el-option label="Nam" value="Male" />
                <el-option label="Nữ" value="Female" />
                <el-option label="Khác" value="Other" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="14">
            <el-form-item label="Số điện thoại">
              <el-input v-model="createForm.mobile" placeholder="0123456789" prefix-icon="Phone" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="Kích hoạt ngay">
              <el-switch v-model="createForm.enabled" active-text="Bật" inactive-text="Tắt" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">Hủy</el-button>
          <el-button type="primary" :loading="createLoading" @click="handleCreateUser">
            Tạo tài khoản
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Dialog hiển thị kết quả Reset Mật Khẩu -->
    <el-dialog
      v-model="resetDialogVisible"
      title="Đặt lại mật khẩu thành công"
      width="450px"
    >
      <div class="reset-result-box">
        <p>Mật khẩu ngẫu nhiên mới đã được tạo cho tài khoản <strong>{{ resetUserEmail }}</strong>:</p>
        <div class="password-display">
          <code>{{ generatedPassword }}</code>
        </div>
        <p class="reset-hint">Vui lòng sao chép mật khẩu này và gửi cho người dùng. Mật khẩu này sẽ không hiển thị lại sau khi đóng cửa sổ.</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetDialogVisible = false">Đóng</el-button>
          <el-button type="primary" @click="copyPassword">
            Sao chép mật khẩu
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-page {
  max-width: 1200px;
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

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.stats {
  margin-top: 16px;
  text-align: right;
}

.reset-result-box {
  text-align: center;
}

.password-display {
  margin: 16px 0;
  padding: 12px;
  background: #f3f4f6;
  border-radius: 8px;
  font-size: 18px;
  letter-spacing: 1px;
}

.password-display code {
  color: #ef4444;
  font-weight: bold;
}

.reset-hint {
  font-size: 13px;
  color: #6b7280;
  margin-top: 8px;
}
</style>
