<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { auditLogService, type GetAuditLogsParams } from '@/services/auditLogService'
import type { AuditLogResponse } from '@/types'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'

const logs = ref<AuditLogResponse[]>([])
const loading = ref(false)
const totalElements = ref(0)

const filter = reactive<GetAuditLogsParams>({
  page: 0,
  size: 20,
  action: undefined,
  entityType: undefined,
  userId: undefined,
})

const dateRange = ref<[Date, Date] | null>(null)

const actions = [
  'SKILL_UPLOAD',
  'SKILL_UPDATE',
  'SKILL_DELETE',
  'SKILL_APPROVE',
  'SKILL_REJECT',
  'SKILL_MERGE',
  'SKILL_DEPRECATE',
  'SKILL_USAGE',
  'REVIEW_CREATE',
  'REVIEW_DELETE',
  'REVIEW_REPORT',
  'ROLE_ASSIGN',
  'ROLE_REVOKE',
]

const entityTypes = ['Skill', 'SkillReview', 'User', 'ImpactRecord', 'SkillRating']

async function fetchLogs() {
  loading.value = true
  try {
    const params: GetAuditLogsParams = { ...filter }
    if (dateRange.value) {
      params.startDate = dateRange.value[0].toISOString()
      params.endDate = dateRange.value[1].toISOString()
    }
    const res = await auditLogService.getAuditLogs(params)
    logs.value = res.content
    totalElements.value = res.totalElements
  } catch {
    ElMessage.error('Không thể tải nhật ký kiểm toán')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  filter.page = 0
  fetchLogs()
}

function handleReset() {
  filter.action = undefined
  filter.entityType = undefined
  filter.userId = undefined
  dateRange.value = null
  filter.page = 0
  fetchLogs()
}

function handlePageChange(newPage: number) {
  filter.page = newPage - 1
  fetchLogs()
}

function getActionTagType(action: string) {
  if (action.includes('APPROVE') || action.includes('UPLOAD')) return 'success'
  if (action.includes('REJECT') || action.includes('DELETE') || action.includes('REVOKE'))
    return 'danger'
  if (action.includes('MERGE') || action.includes('UPDATE')) return 'warning'
  return 'info'
}

onMounted(() => {
  fetchLogs()
})
</script>

<template>
  <div class="audit-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Nhật ký Kiểm toán (Audit Logs)</h1>
        <p class="page-subtitle">
          Theo dõi toàn bộ lịch sử các hành động quan trọng trên hệ thống AI Skill Store để đảm bảo tính minh bạch và an toàn
        </p>
      </div>
      <el-button :icon="Refresh" @click="fetchLogs">Làm mới</el-button>
    </div>

    <!-- Filter Bar -->
    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-col">
          <span class="filter-label">Hành động:</span>
          <el-select
            v-model="filter.action"
            placeholder="Tất cả hành động"
            clearable
            class="filter-select"
          >
            <el-option v-for="act in actions" :key="act" :label="act" :value="act" />
          </el-select>
        </div>

        <div class="filter-col">
          <span class="filter-label">Đối tượng (Entity):</span>
          <el-select
            v-model="filter.entityType"
            placeholder="Tất cả đối tượng"
            clearable
            class="filter-select"
          >
            <el-option v-for="ent in entityTypes" :key="ent" :label="ent" :value="ent" />
          </el-select>
        </div>

        <div class="filter-col">
          <span class="filter-label">Khoảng thời gian:</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="đến"
            start-placeholder="Từ ngày"
            end-placeholder="Đến ngày"
            class="date-picker"
          />
        </div>

        <div class="filter-col actions-col">
          <el-button type="primary" :icon="Search" @click="handleSearch">Tìm kiếm</el-button>
          <el-button @click="handleReset">Đặt lại</el-button>
        </div>
      </div>
    </div>

    <!-- Data Table -->
    <div class="table-card">
      <el-table
        :data="logs"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />

        <el-table-column label="Thời gian" width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString('vi-VN') }}
          </template>
        </el-table-column>

        <el-table-column label="Người thực hiện" width="160">
          <template #default="{ row }">
            <span v-if="row.userName">
              <strong>{{ row.userName }}</strong>
              <span class="sub-id"> (ID: {{ row.userId }})</span>
            </span>
            <span v-else class="text-muted">Hệ thống</span>
          </template>
        </el-table-column>

        <el-table-column label="Hành động" width="180">
          <template #default="{ row }">
            <el-tag :type="getActionTagType(row.action)" size="small">
              {{ row.action }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Đối tượng" width="140">
          <template #default="{ row }">
            <span>{{ row.entityType }}</span>
            <span v-if="row.entityId" class="sub-id"> #{{ row.entityId }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="details" label="Chi tiết hành động" min-width="280">
          <template #default="{ row }">
            <span class="details-cell">{{ row.details || '—' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper" v-if="totalElements > filter.size!">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :current-page="filter.page! + 1"
          :page-size="filter.size"
          :total="totalElements"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.audit-container {
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

.filter-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  border: 1px solid #e2e8f0;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.filter-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-col {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.filter-select {
  width: 180px;
}

.date-picker {
  width: 260px;
}

.actions-col {
  margin-left: auto;
}

.table-card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.sub-id {
  font-size: 11px;
  color: #94a3b8;
}

.details-cell {
  font-family: monospace;
  font-size: 13px;
  color: #334155;
}

.text-muted {
  color: #94a3b8;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
