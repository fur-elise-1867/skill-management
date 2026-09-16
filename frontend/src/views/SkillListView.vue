<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { skillService, type GetSkillsParams } from '@/services/skillService'
import { useCategoryStore } from '@/stores/category'
import type { SkillResponse } from '@/types'
import { ElMessage } from 'element-plus'
import {
  Search,
  Download,
  StarFilled,
  View,
  TrendCharts,
  Document,
  Plus,
} from '@element-plus/icons-vue'

const router = useRouter()
const categoryStore = useCategoryStore()

const skills = ref<SkillResponse[]>([])
const loading = ref(false)
const totalElements = ref(0)

const queryParams = reactive<GetSkillsParams>({
  page: 0,
  size: 12,
  search: '',
  categoryId: undefined,
  status: 'PUBLISHED',
  sortBy: 'createdAt',
  sortOrder: 'DESC',
})

async function fetchSkills() {
  loading.value = true
  try {
    const res = await skillService.getSkills(queryParams)
    skills.value = res.content
    totalElements.value = res.totalElements
  } catch (error) {
    ElMessage.error('Không thể tải danh sách skills')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 0
  fetchSkills()
}

function handlePageChange(newPage: number) {
  queryParams.page = newPage - 1
  fetchSkills()
}

async function handleDownload(skill: SkillResponse, event: Event) {
  event.stopPropagation()
  try {
    const blob = await skillService.downloadSkill(skill.id)
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = skill.fileName || `${skill.title}.txt`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    ElMessage.success('Đang tải file xuống...')
  } catch (err) {
    ElMessage.error('Lỗi khi tải file')
  }
}

function getStatusType(status: string) {
  switch (status) {
    case 'PUBLISHED':
      return 'success'
    case 'PENDING':
      return 'warning'
    case 'REJECTED':
      return 'danger'
    case 'DEPRECATED':
      return 'info'
    default:
      return 'info'
  }
}

onMounted(async () => {
  await categoryStore.fetchCategories()
  await fetchSkills()
})
</script>

<template>
  <div class="skill-store-container">
    <!-- Header Banner -->
    <div class="store-hero">
      <div class="hero-content">
        <h1 class="hero-title">Khám phá AI Skills</h1>
        <p class="hero-subtitle">
          Tìm kiếm, chia sẻ và áp dụng các AI skills đã được kiểm duyệt chất lượng phục vụ công việc hàng ngày
        </p>

        <!-- Search Bar -->
        <div class="search-box">
          <el-input v-model="queryParams.search"
            placeholder="Tìm theo tên skill, mô tả hoặc tag (ví dụ: code review, jira...)" size="large" clearable
            @keyup.enter="handleSearch" :prefix-icon="Search" class="search-input">
            <template #append>
              <el-button type="primary" @click="handleSearch">
                Tìm kiếm
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
      <div class="hero-actions">
        <el-button type="primary" size="large" :icon="Plus" @click="router.push('/skills/upload')" class="upload-btn">
          Đóng góp Skill mới
        </el-button>
      </div>
    </div>

    <!-- Filters Section -->
    <div class="filter-card">
      <div class="filter-row">
        <!-- Category Filter -->
        <div class="filter-item">
          <span class="filter-label">Danh mục:</span>
          <el-select v-model="queryParams.categoryId" placeholder="Tất cả danh mục" clearable @change="handleSearch"
            class="filter-select">
            <el-option v-for="cat in categoryStore.categories" :key="cat.id"
              :label="cat.icon ? `${cat.icon} ${cat.name}` : cat.name" :value="cat.id" />
          </el-select>
        </div>

        <!-- Sort by -->
        <div class="filter-item">
          <span class="filter-label">Sắp xếp:</span>
          <el-select v-model="queryParams.sortBy" @change="handleSearch" class="filter-select">
            <el-option label="Mới nhất" value="createdAt" />
            <el-option label="Đánh giá cao nhất" value="averageRating" />
            <el-option label="Dùng nhiều nhất" value="usageCount" />
            <el-option label="Tên (A-Z)" value="title" />
          </el-select>
        </div>

        <!-- Sort Order -->
        <div class="filter-item">
          <el-radio-group v-model="queryParams.sortOrder" @change="handleSearch" size="default">
            <el-radio-button value="DESC">Giảm dần</el-radio-button>
            <el-radio-button value="ASC">Tăng dần</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </div>

    <!-- Skills Grid -->
    <div v-loading="loading" class="skills-section">
      <div v-if="skills.length === 0 && !loading" class="empty-state">
        <el-empty description="Không tìm thấy skill nào phù hợp với bộ lọc">
          <el-button type="primary" @click="router.push('/skills/upload')">
            Đóng góp skill đầu tiên
          </el-button>
        </el-empty>
      </div>

      <div v-else class="skills-grid">
        <div v-for="skill in skills" :key="skill.id" class="skill-card" @click="router.push(`/skills/${skill.id}`)">
          <!-- Card Header -->
          <div class="card-header">
            <div class="card-category-badges">
              <span v-for="cat in skill.categories" :key="cat.id" class="category-badge">
                {{ cat.icon || '📌' }} {{ cat.name }}
              </span>
            </div>
            <el-tag :type="getStatusType(skill.status)" size="small" effect="light">
              v{{ skill.currentVersion }}
            </el-tag>
          </div>

          <!-- Title & Description -->
          <div class="card-body">
            <h3 class="skill-title" :title="skill.title">{{ skill.title }}</h3>
            <p class="skill-desc">{{ skill.description }}</p>
          </div>

          <!-- Tags -->
          <div class="card-tags" v-if="skill.tags && skill.tags.length > 0">
            <span v-for="tag in skill.tags.slice(0, 3)" :key="tag" class="skill-tag">
              #{{ tag }}
            </span>
            <span v-if="skill.tags.length > 3" class="skill-tag-more">
              +{{ skill.tags.length - 3 }}
            </span>
          </div>

          <!-- Metrics / Stats -->
          <div class="card-metrics">
            <div class="metric-item rating">
              <el-icon class="star-icon">
                <StarFilled />
              </el-icon>
              <span class="rating-value">{{ Number(skill.averageRating || 0).toFixed(1) }}</span>
              <span class="rating-count">({{ skill.ratingCount }})</span>
            </div>

            <div class="metric-item usage">
              <el-icon>
                <View />
              </el-icon>
              <span>{{ skill.usageCount }} lượt</span>
            </div>

            <div class="metric-item impact" v-if="skill.totalMmSaved > 0">
              <el-icon>
                <TrendCharts />
              </el-icon>
              <span>{{ skill.totalMmSaved }} MM</span>
            </div>
          </div>

          <!-- Card Footer -->
          <div class="card-footer">
            <div class="author-info">
              <el-avatar :size="24" :src="skill.author.avatarUrl || ''">
                {{ skill.author.name.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="author-name">{{ skill.author.name }}</span>
            </div>

            <div class="card-actions">
              <el-tooltip content="Tải file" placement="top">
                <el-button circle size="small" :icon="Download" @click="handleDownload(skill, $event)" />
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-wrapper" v-if="totalElements > queryParams.size!">
        <el-pagination background layout="prev, pager, next, total" :current-page="queryParams.page! + 1"
          :page-size="queryParams.size" :total="totalElements" @current-change="handlePageChange" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.skill-store-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 20px;
}

/* Hero Section */
.store-hero {
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 50%, #4338ca 100%);
  border-radius: 16px;
  padding: 36px 32px;
  color: #fff;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 10px 25px -5px rgba(49, 46, 129, 0.3);
}

.hero-content {
  max-width: 760px;
}

.hero-title {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.02em;
  margin: 0 0 8px 0;
}

.hero-subtitle {
  font-size: 15px;
  color: #c7d2fe;
  margin: 0 0 24px 0;
  line-height: 1.5;
}

.search-box {
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px 0 0 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.search-input :deep(.el-input-group__append) {
  border-radius: 0 10px 10px 0;
  background-color: #4f46e5;
  color: #fff;
  border: none;
}

.upload-btn {
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  background: #10b981;
  border-color: #10b981;
}

.upload-btn:hover {
  background: #059669;
  border-color: #059669;
}

/* Filter Card */
.filter-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.filter-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.filter-select {
  width: 200px;
}

/* Skills Grid */
.skills-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.skill-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 20px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.skill-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 20px -5px rgba(0, 0, 0, 0.08);
  border-color: #c7d2fe;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.card-category-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.category-badge {
  font-size: 12px;
  font-weight: 500;
  background: #f1f5f9;
  color: #475569;
  padding: 2px 8px;
  border-radius: 6px;
}

.card-body {
  flex: 1;
  margin-bottom: 14px;
}

.skill-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 6px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.skill-desc {
  font-size: 13px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 14px;
}

.skill-tag {
  font-size: 11px;
  color: #4f46e5;
  background: #eef2ff;
  padding: 2px 6px;
  border-radius: 4px;
}

.skill-tag-more {
  font-size: 11px;
  color: #94a3b8;
}

.card-metrics {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 0;
  border-top: 1px dashed #f1f5f9;
  border-bottom: 1px dashed #f1f5f9;
  margin-bottom: 14px;
  font-size: 13px;
}

.metric-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #64748b;
}

.metric-item.rating {
  color: #d97706;
}

.star-icon {
  color: #f59e0b;
}

.rating-value {
  font-weight: 700;
  color: #1e293b;
}

.rating-count {
  font-size: 12px;
  color: #94a3b8;
}

.metric-item.impact {
  color: #059669;
  font-weight: 600;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-size: 13px;
  font-weight: 500;
  color: #334155;
}

.pagination-wrapper {
  margin-top: 36px;
  display: flex;
  justify-content: center;
}
</style>
