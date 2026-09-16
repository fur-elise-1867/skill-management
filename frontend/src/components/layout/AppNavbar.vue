<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  DataAnalysis,
  Files,
  UploadFilled,
  Collection,
  Checked,
  Operation,
  UserFilled,
  Document,
  SwitchButton,
  Setting,
} from '@element-plus/icons-vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const activeRoute = computed(() => route.path)

function handleSelect(key: string) {
  if (key === 'logout') {
    auth.logout()
  } else if (key) {
    router.push(key)
  }
}
</script>

<template>
  <header class="app-header">
    <div class="header-container">
      <div class="brand" @click="router.push('/dashboard')">
        <div class="brand-logo">
          <span class="logo-icon">✨</span>
        </div>
        <div class="brand-text">
          <span class="brand-title">AI Skill Store</span>
          <span class="brand-subtitle">Samsung SDSV</span>
        </div>
      </div>

      <nav class="nav-links">
        <router-link
          to="/dashboard"
          class="nav-item"
          :class="{ active: activeRoute === '/dashboard' }"
        >
          <el-icon><DataAnalysis /></el-icon>
          <span>Dashboard</span>
        </router-link>

        <router-link
          to="/skills"
          class="nav-item"
          :class="{ active: activeRoute === '/skills' }"
        >
          <el-icon><Files /></el-icon>
          <span>Skills</span>
        </router-link>

        <router-link
          to="/skills/upload"
          class="nav-item"
          :class="{ active: activeRoute === '/skills/upload' }"
        >
          <el-icon><UploadFilled /></el-icon>
          <span>Upload</span>
        </router-link>

        <router-link
          to="/skills/my"
          class="nav-item"
          :class="{ active: activeRoute === '/skills/my' }"
        >
          <el-icon><Collection /></el-icon>
          <span>My Skills</span>
        </router-link>

        <!-- Curator Menu -->
        <el-dropdown
          v-if="auth.isCurator"
          trigger="click"
          @command="handleSelect"
          class="dropdown-trigger"
        >
          <span
            class="nav-item cursor-pointer"
            :class="{ active: activeRoute.startsWith('/curator') }"
          >
            <el-icon><Checked /></el-icon>
            <span>Curator</span>
            <span class="arrow-down">▾</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="/curator/pending">
                <el-icon><Checked /></el-icon> Duyệt Skill (Pending)
              </el-dropdown-item>
              <el-dropdown-item command="/curator/merge">
                <el-icon><Operation /></el-icon> Gộp Skill trùng (Merge)
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- Admin Menu -->
        <el-dropdown
          v-if="auth.isAdmin"
          trigger="click"
          @command="handleSelect"
          class="dropdown-trigger"
        >
          <span
            class="nav-item cursor-pointer"
            :class="{ active: activeRoute.startsWith('/admin') }"
          >
            <el-icon><Setting /></el-icon>
            <span>Admin</span>
            <span class="arrow-down">▾</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="/admin">
                <el-icon><UserFilled /></el-icon> Quản lý User & Role
              </el-dropdown-item>
              <el-dropdown-item command="/admin/audit">
                <el-icon><Document /></el-icon> Nhật ký kiểm toán (Audit)
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </nav>

      <!-- User Profile Dropdown -->
      <div class="user-actions">
        <el-dropdown trigger="click" @command="handleSelect">
          <div class="user-profile-badge">
            <el-avatar
              :size="34"
              :src="auth.user?.avatarUrl || ''"
              class="user-avatar"
            >
              {{ auth.user?.name?.charAt(0).toUpperCase() || 'U' }}
            </el-avatar>
            <span class="user-name">{{ auth.user?.name || 'User' }}</span>
            <el-tag
              size="small"
              :type="auth.isAdmin ? 'danger' : auth.isEditor ? 'warning' : 'info'"
              effect="plain"
              class="user-role-tag"
            >
              {{ auth.user?.role?.name || 'USER' }}
            </el-tag>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="/account">
                <el-icon><UserFilled /></el-icon> Thông tin tài khoản
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon> Đăng xuất
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05);
}

.header-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.brand-logo {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  box-shadow: 0 4px 10px rgba(79, 70, 229, 0.3);
}

.brand-title {
  font-size: 17px;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.02em;
  display: block;
}

.brand-subtitle {
  font-size: 11px;
  color: #6b7280;
  font-weight: 500;
  display: block;
  margin-top: -2px;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 6px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #4b5563;
  text-decoration: none;
  transition: all 0.15s ease;
}

.nav-item:hover {
  color: #4f46e5;
  background-color: #f3f4f6;
}

.nav-item.active {
  color: #4f46e5;
  background-color: #eef2ff;
  font-weight: 600;
}

.cursor-pointer {
  cursor: pointer;
}

.arrow-down {
  font-size: 10px;
  margin-left: 2px;
  color: #9ca3af;
}

.user-profile-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 8px;
  border-radius: 20px;
  cursor: pointer;
  transition: background-color 0.15s;
}

.user-profile-badge:hover {
  background-color: #f3f4f6;
}

.user-avatar {
  background: linear-gradient(135deg, #6366f1, #a855f7);
  color: #fff;
  font-weight: 600;
  font-size: 14px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.user-role-tag {
  font-size: 10px;
  font-weight: 700;
  padding: 0 6px;
  height: 20px;
  line-height: 18px;
}
</style>
