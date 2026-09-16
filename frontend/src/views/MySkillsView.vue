<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { skillService } from '@/services/skillService'
import { statsService } from '@/services/statsService'
import type { SkillResponse, MySkillsStatsResponse } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Collection,
  Timer,
  View,
  TrendCharts,
  Edit,
  Delete,
  Download,
} from '@element-plus/icons-vue'

const router = useRouter()

const stats = ref<MySkillsStatsResponse | null>(null)
const skills = ref<SkillResponse[]>([])
const loading = ref(false)
const activeTab = ref('ALL')

// Edit / Update Version Dialog
const editDialogVisible = ref(false)
const updating = ref(false)
const selectedSkill = ref<SkillResponse | null>(null)
const editForm = reactive({
  title: '',
  description: '',
  changelog: '',
})

async function fetchMyData() {
  loading.value = true
  try {
    const [statsRes, skillsRes] = await Promise.all([
      statsService.getMyStats(),
      skillService.getSkills({
        page: 0,
        size: 50,
        status: activeTab.value === 'ALL' ? undefined : activeTab.value,
      }),
    ])
    stats.value = statsRes
    skills.value = skillsRes.content
  } catch {
    ElMessage.error('Không thể tải dữ liệu kỹ năng của tôi')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  fetchMyData()
}

function openEditDialog(skill: SkillResponse) {
  selectedSkill.value = skill
  editForm.title = skill.title
  editForm.description = skill.description
  editForm.changelog = ''
  editDialogVisible.value = true
}

async function handleUpdateSkill() {
  if (!selectedSkill.value) return
  updating.value = true
  try {
    await skillService.updateSkill(selectedSkill.value.id, {
      title: editForm.title,
      description: editForm.description,
      changelog: editForm.changelog || 'Cập nhật thông tin',
    })
    ElMessage.success('Đã cập nhật skill thành công!')
    editDialogVisible.value = false
    fetchMyData()
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || 'Không thể cập nhật skill')
  } finally {
    updating.value = false
  }
}

async function handleDeleteSkill(skill: SkillResponse) {
  try {
    await ElMessageBox.confirm(
      `Bạn có chắc chắn muốn xóa skill "${skill.title}"?`,
      'Xác nhận lưu trữ/xóa',
      { type: 'warning' },
    )
    await skillService.deleteSkill(skill.id)
    ElMessage.success('Đã xóa skill thành công')
    fetchMyData()
  } catch {
    // cancelled
  }
}

function getStatusBadgeType(status: string) {
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

onMounted(() => {
  fetchMyData()
})
</script>

<template>
  <div class="my-skills-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Kỹ năng của tôi</h1>
        <p class="page-subtitle">Quản lý, cập nhật phiên bản và theo dõi giá trị tạo ra từ các AI skills của bạn</p>
      </div>
      <el-button
        type="primary"
        size="large"
        :icon="Plus"
        @click="router.push('/skills/upload')"
        class="create-btn"
      >
        Đóng góp Skill mới
      </el-button>
    </div>

    <!-- Stats KPI Cards -->
    <div class="kpi-grid" v-if="stats">
      <div class="kpi-card published">
        <div class="kpi-icon-wrap"><el-icon><Collection /></el-icon></div>
        <div class="kpi-info">
          <span class="kpi-num">{{ stats.myPublishedSkillsCount }}</span>
          <span class="kpi-label">Skill đã phát hành</span>
        </div>
      </div>

      <div class="kpi-card pending">
        <div class="kpi-icon-wrap"><el-icon><Timer /></el-icon></div>
        <div class="kpi-info">
          <span class="kpi-num">{{ stats.myPendingSkillsCount }}</span>
          <span class="kpi-label">Đang chờ duyệt</span>
        </div>
      </div>

      <div class="kpi-card usage">
        <div class="kpi-icon-wrap"><el-icon><View /></el-icon></div>
        <div class="kpi-info">
          <span class="kpi-num">{{ stats.totalUsageOfMySkills }}</span>
          <span class="kpi-label">Lượt người khác áp dụng</span>
        </div>
      </div>

      <div class="kpi-card impact">
        <div class="kpi-icon-wrap"><el-icon><TrendCharts /></el-icon></div>
        <div class="kpi-info">
          <span class="kpi-num">{{ stats.totalMmSavedByMySkills }} MM</span>
          <span class="kpi-label">Giá trị tiết kiệm tạo ra</span>
        </div>
      </div>
    </div>

    <!-- Tabs & Table -->
    <div class="table-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="custom-tabs">
        <el-tab-pane label="Tất cả" name="ALL" />
        <el-tab-pane label="Đã phát hành (Published)" name="PUBLISHED" />
        <el-tab-pane label="Đang chờ duyệt (Pending)" name="PENDING" />
        <el-tab-pane label="Bị từ chối (Rejected)" name="REJECTED" />
        <el-tab-pane label="Ngưng sử dụng (Deprecated)" name="DEPRECATED" />
      </el-tabs>

      <el-table
        :data="skills"
        v-loading="loading"
        stripe
        style="width: 100%"
        class="skills-table"
      >
        <el-table-column label="Tên Skill & Danh mục" min-width="260">
          <template #default="{ row }">
            <div class="title-cell">
              <span class="cell-title" @click="router.push(`/skills/${row.id}`)">
                {{ row.title }}
              </span>
              <div class="cat-tags">
                <span
                  v-for="c in row.categories"
                  :key="c.id"
                  class="cat-chip"
                >
                  {{ c.icon }} {{ c.name }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Trạng thái" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusBadgeType(row.status)" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Phiên bản" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">v{{ row.currentVersion }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Lượt dùng" width="110" align="center">
          <template #default="{ row }">
            <strong>{{ row.usageCount }}</strong>
          </template>
        </el-table-column>

        <el-table-column label="Đánh giá" width="110" align="center">
          <template #default="{ row }">
            <span>⭐ {{ Number(row.averageRating || 0).toFixed(1) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Tiết kiệm" width="110" align="center">
          <template #default="{ row }">
            <span class="impact-text">{{ row.totalMmSaved }} MM</span>
          </template>
        </el-table-column>

        <el-table-column label="Cập nhật cuối" width="140" align="center">
          <template #default="{ row }">
            {{ new Date(row.updatedAt || row.createdAt).toLocaleDateString('vi-VN') }}
          </template>
        </el-table-column>

        <el-table-column label="Thao tác" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="router.push(`/skills/${row.id}`)"
            >
              Xem
            </el-button>
            <el-button
              link
              type="warning"
              size="small"
              :icon="Edit"
              @click="openEditDialog(row)"
            >
              Sửa
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDeleteSkill(row)"
            >
              Xóa
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Edit Skill Dialog -->
    <el-dialog
      v-model="editDialogVisible"
      title="Cập nhật thông tin Skill"
      width="600px"
    >
      <el-form label-position="top">
        <el-form-item label="Tên Skill">
          <el-input v-model="editForm.title" />
        </el-form-item>

        <el-form-item label="Mô tả & Hướng dẫn">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="5"
          />
        </el-form-item>

        <el-form-item label="Ghi chú thay đổi (Changelog)">
          <el-input
            v-model="editForm.changelog"
            placeholder="Ví dụ: Tối ưu hoá prompt cho Java 21..."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">Hủy</el-button>
        <el-button
          type="primary"
          :loading="updating"
          @click="handleUpdateSkill"
        >
          Lưu thay đổi
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.my-skills-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 6px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.create-btn {
  background: #4f46e5;
  border-color: #4f46e5;
  font-weight: 600;
}

/* KPI Grid */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.kpi-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.kpi-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.kpi-card.published .kpi-icon-wrap {
  background: #ecfdf5;
  color: #10b981;
}

.kpi-card.pending .kpi-icon-wrap {
  background: #fffbeb;
  color: #f59e0b;
}

.kpi-card.usage .kpi-icon-wrap {
  background: #eef2ff;
  color: #4f46e5;
}

.kpi-card.impact .kpi-icon-wrap {
  background: #f0fdf4;
  color: #16a34a;
}

.kpi-info {
  display: flex;
  flex-direction: column;
}

.kpi-num {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
}

.kpi-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

/* Table Card */
.table-card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.title-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cell-title {
  font-weight: 600;
  color: #1e293b;
  cursor: pointer;
}

.cell-title:hover {
  color: #4f46e5;
  text-decoration: underline;
}

.cat-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.cat-chip {
  font-size: 11px;
  background: #f8fafc;
  color: #64748b;
  padding: 1px 6px;
  border-radius: 4px;
}

.impact-text {
  color: #16a34a;
  font-weight: 600;
}
</style>
