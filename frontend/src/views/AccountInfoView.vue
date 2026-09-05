<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userService } from '@/services/userService'
import { ElMessage, ElMessageBox, type UploadRequestOptions } from 'element-plus'

const auth = useAuthStore()
const router = useRouter()

const profileLoading = ref(false)
const passwordLoading = ref(false)
const avatarLoading = ref(false)
const deleteLoading = ref(false)

const profileForm = ref({
  name: '',
  gender: '',
  mobile: '',
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

onMounted(async () => {
  if (auth.isAuthenticated && !auth.user) {
    await auth.fetchCurrentUser()
  }
  if (auth.user) {
    profileForm.value.name = auth.user.name || ''
    profileForm.value.gender = auth.user.gender || ''
    profileForm.value.mobile = auth.user.mobile || ''
  }
})

async function handleUpdateProfile() {
  if (!profileForm.value.name.trim()) {
    ElMessage.warning('Vui lòng nhập họ và tên.')
    return
  }

  profileLoading.value = true
  try {
    const updated = await userService.updateProfile({
      name: profileForm.value.name.trim(),
      gender: profileForm.value.gender || undefined,
      mobile: profileForm.value.mobile.trim() || undefined,
    })
    auth.user = updated
    ElMessage.success('Cập nhật thông tin cá nhân thành công!')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || 'Cập nhật thông tin thất bại.')
  } finally {
    profileLoading.value = false
  }
}

async function handleChangePassword() {
  if (!passwordForm.value.oldPassword) {
    ElMessage.warning('Vui lòng nhập mật khẩu hiện tại.')
    return
  }
  if (passwordForm.value.newPassword.length < 8) {
    ElMessage.warning('Mật khẩu mới phải có tối thiểu 8 ký tự.')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('Mật khẩu xác nhận không khớp.')
    return
  }

  passwordLoading.value = true
  try {
    await userService.changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    })
    ElMessage.success('Đổi mật khẩu thành công!')
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || 'Đổi mật khẩu thất bại. Vui lòng kiểm tra lại.')
  } finally {
    passwordLoading.value = false
  }
}

async function handleCustomAvatarUpload(options: UploadRequestOptions) {
  const file = options.file
  const isImage = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('Chỉ chấp nhận file ảnh định dạng JPG, PNG hoặc WEBP!')
    return
  }
  if (!isLt5M) {
    ElMessage.error('Dung lượng ảnh tối đa là 5MB!')
    return
  }

  avatarLoading.value = true
  try {
    const res = await userService.uploadAvatar(file)
    if (auth.user) {
      auth.user.avatarUrl = res.avatarUrl
    }
    ElMessage.success('Cập nhật ảnh đại diện thành công!')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || 'Tải ảnh đại diện thất bại.')
  } finally {
    avatarLoading.value = false
  }
}

async function handleDeleteAccount() {
  try {
    await ElMessageBox.confirm(
      'Hành động này sẽ xóa vĩnh viễn tài khoản của bạn và không thể khôi phục. Bạn có chắc chắn muốn tiếp tục không?',
      'Cảnh báo xóa tài khoản',
      {
        confirmButtonText: 'Xác nhận xóa tài khoản',
        cancelButtonText: 'Hủy bỏ',
        type: 'error',
        confirmButtonClass: 'el-button--danger',
      },
    )

    deleteLoading.value = true
    await userService.deleteAccount()
    ElMessage.success('Tài khoản đã được xóa thành công.')
    auth.logout()
    router.push('/login')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || 'Xóa tài khoản thất bại.')
    }
  } finally {
    deleteLoading.value = false
  }
}

function getRoleTagType(roleName?: string) {
  if (roleName === 'ADMIN') return 'danger'
  if (roleName === 'EDITOR') return 'warning'
  return 'primary'
}
</script>

<template>
  <div class="account-page">
    <div class="account-header">
      <div>
        <h1>Thông tin tài khoản</h1>
        <p>Quản lý thông tin cá nhân, ảnh đại diện và bảo mật tài khoản</p>
      </div>
      <div class="header-actions">
        <el-button @click="$router.push('/dashboard')" plain>
          ← Dashboard
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

    <el-row :gutter="24">
      <!-- Cột trái: Avatar & Hồ sơ -->
      <el-col :xs="24" :md="14">
        <el-card shadow="hover" class="box-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">Hồ sơ cá nhân</span>
              <el-tag :type="getRoleTagType(auth.user?.role?.name)" size="large" effect="dark">
                {{ auth.user?.role?.name || 'USER' }}
              </el-tag>
            </div>
          </template>

          <div class="avatar-section">
            <div class="avatar-wrapper" v-loading="avatarLoading">
              <el-avatar
                :size="100"
                :src="auth.user?.avatarUrl || ''"
                class="user-avatar"
              >
                {{ auth.user?.name?.charAt(0).toUpperCase() || 'U' }}
              </el-avatar>
            </div>
            <div class="avatar-info">
              <el-upload
                class="avatar-uploader"
                action=""
                :show-file-list="false"
                :http-request="handleCustomAvatarUpload"
                accept="image/jpeg,image/png,image/webp"
              >
                <el-button type="primary" plain size="default">
                  Đổi ảnh đại diện
                </el-button>
              </el-upload>
              <p class="avatar-hint">Hỗ trợ JPG, PNG, WEBP tối đa 5MB</p>
            </div>
          </div>

          <el-divider />

          <el-form label-position="top" size="large">
            <el-form-item label="Email (Không thể thay đổi)">
              <el-input :value="auth.user?.email" disabled prefix-icon="Message" />
            </el-form-item>

            <el-form-item label="Họ và tên" required>
              <el-input v-model="profileForm.name" placeholder="Nguyễn Văn A" prefix-icon="User" />
            </el-form-item>

            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="Giới tính">
                  <el-select v-model="profileForm.gender" placeholder="Chọn giới tính" style="width: 100%">
                    <el-option label="Nam" value="Male" />
                    <el-option label="Nữ" value="Female" />
                    <el-option label="Khác" value="Other" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="Số điện thoại">
                  <el-input v-model="profileForm.mobile" placeholder="0123456789" prefix-icon="Phone" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item>
              <el-button
                type="primary"
                :loading="profileLoading"
                @click="handleUpdateProfile"
                style="width: 100%"
              >
                Lưu thay đổi
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- Cột phải: Đổi mật khẩu & Vùng nguy hiểm -->
      <el-col :xs="24" :md="10">
        <!-- Đổi mật khẩu -->
        <el-card shadow="hover" class="box-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">Đổi mật khẩu</span>
            </div>
          </template>

          <el-form label-position="top" size="large">
            <el-form-item label="Mật khẩu hiện tại" required>
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="Nhập mật khẩu hiện tại"
                show-password
                prefix-icon="Lock"
              />
            </el-form-item>

            <el-form-item label="Mật khẩu mới" required>
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="Tối thiểu 8 ký tự"
                show-password
                prefix-icon="Key"
              />
            </el-form-item>

            <el-form-item label="Xác nhận mật khẩu mới" required>
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="Nhập lại mật khẩu mới"
                show-password
                prefix-icon="Key"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="warning"
                :loading="passwordLoading"
                @click="handleChangePassword"
                style="width: 100%"
              >
                Cập nhật mật khẩu
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- Vùng nguy hiểm (Danger Zone) -->
        <el-card shadow="hover" class="box-card danger-zone">
          <template #header>
            <div class="card-header">
              <span class="card-title danger-title">Vùng nguy hiểm</span>
            </div>
          </template>

          <div v-if="auth.isAdmin">
            <el-alert
              title="Tài khoản Quản trị viên (Admin)"
              description="Tài khoản Admin không thể tự xóa để đảm bảo an toàn cho việc vận hành hệ thống."
              type="info"
              show-icon
              :closable="false"
            />
          </div>

          <div v-else class="delete-account-box">
            <p class="delete-desc">
              Khi bạn xóa tài khoản, toàn bộ dữ liệu cá nhân sẽ bị xóa vĩnh viễn khỏi hệ thống và không thể khôi phục.
            </p>
            <el-button
              type="danger"
              :loading="deleteLoading"
              @click="handleDeleteAccount"
              style="width: 100%"
            >
              Xóa tài khoản của tôi
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.account-page {
  max-width: 1100px;
  margin: 40px auto;
  padding: 0 24px;
}

.account-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.account-header h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
}

.account-header p {
  margin: 4px 0 0 0;
  color: #6b7280;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.box-card {
  border-radius: 12px;
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 8px 0;
}

.avatar-wrapper {
  flex-shrink: 0;
}

.user-avatar {
  background: #667eea;
  color: #ffffff;
  font-size: 36px;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.avatar-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.avatar-hint {
  margin: 0;
  font-size: 12px;
  color: #9ca3af;
}

.danger-zone {
  border: 1px solid #fee2e2;
  background-color: #fffafb;
}

.danger-title {
  color: #dc2626;
}

.delete-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 16px 0;
  line-height: 1.5;
}
</style>
