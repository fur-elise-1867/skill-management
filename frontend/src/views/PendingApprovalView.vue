<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { curatorService } from '@/services/curatorService'
import { skillService } from '@/services/skillService'
import type { PendingSkillResponse } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  WarningFilled,
  CircleCheckFilled,
  CircleCloseFilled,
  Download,
  View,
} from '@element-plus/icons-vue'

const router = useRouter()

const pendingSkills = ref<PendingSkillResponse[]>([])
const loading = ref(false)

// Reject Dialog
const rejectDialogVisible = ref(false)
const rejectingSkill = ref<PendingSkillResponse | null>(null)
const rejectReason = ref('')
const submittingReject = ref(false)

// Security Scan Detail Dialog
const securityDialogVisible = ref(false)
const selectedSecuritySkill = ref<PendingSkillResponse | null>(null)

async function fetchPendingList() {
  loading.value = true
  try {
    pendingSkills.value = await curatorService.getPendingSkills()
  } catch {
    ElMessage.error('Không thể tải danh sách skill chờ duyệt')
  } finally {
    loading.value = false
  }
}

async function handleApprove(skill: PendingSkillResponse) {
  try {
    await ElMessageBox.confirm(
      `Phê duyệt skill "${skill.title}" và công khai trên AI Skill Store?`,
      'Xác nhận phê duyệt',
      { type: 'success' },
    )
    await curatorService.approveSkill(skill.id)
    ElMessage.success('Đã phê duyệt skill thành công!')
    fetchPendingList()
  } catch {
    // cancelled
  }
}

function openRejectDialog(skill: PendingSkillResponse) {
  rejectingSkill.value = skill
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

async function submitReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('Vui lòng nhập lý do từ chối')
    return
  }
  if (!rejectingSkill.value) return

  submittingReject.value = true
  try {
    await curatorService.rejectSkill(rejectingSkill.value.id, {
      reason: rejectReason.value.trim(),
    })
    ElMessage.success('Đã từ chối skill')
    rejectDialogVisible.value = false
    fetchPendingList()
  } catch {
    ElMessage.error('Lỗi khi từ chối skill')
  } finally {
    submittingReject.value = false
  }
}

function showSecurityDetail(skill: PendingSkillResponse) {
  selectedSecuritySkill.value = skill
  securityDialogVisible.value = true
}

async function handleDownload(skill: PendingSkillResponse) {
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
  } catch {
    ElMessage.error('Lỗi khi tải file')
  }
}

onMounted(() => {
  fetchPendingList()
})
</script>

<template>
  <div class="curator-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Kiểm duyệt AI Skill (Curator)</h1>
        <p class="page-subtitle">
          Xem xét, kiểm tra an ninh và phê duyệt hoặc từ chối các AI skills mới được đóng góp
        </p>
      </div>
      <el-tag size="large" type="warning" effect="dark">
        {{ pendingSkills.length }} skill chờ duyệt
      </el-tag>
    </div>

    <div class="content-card">
      <el-table
        :data="pendingSkills"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column label="Thông tin Skill" min-width="280">
          <template #default="{ row }">
            <div class="skill-info-col">
              <span class="skill-title" @click="router.push(`/skills/${row.id}`)">
                {{ row.title }}
              </span>
              <p class="skill-desc-snippet">{{ row.description }}</p>
              <div class="category-tags">
                <span
                  v-for="c in row.categories"
                  :key="c.id"
                  class="cat-badge"
                >
                  {{ c.icon }} {{ c.name }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Tác giả" width="160">
          <template #default="{ row }">
            <div class="author-cell">
              <el-avatar :size="24">
                {{ row.author.name.charAt(0).toUpperCase() }}
              </el-avatar>
              <span>{{ row.author.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="File đính kèm" width="160">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              :icon="Download"
              @click="handleDownload(row)"
            >
              {{ row.fileName || 'Tải file' }}
            </el-button>
          </template>
        </el-table-column>

        <el-table-column label="Kiểm tra An ninh" width="180" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.securityScanFlag"
              type="danger"
              size="small"
              plain
              :icon="WarningFilled"
              @click="showSecurityDetail(row)"
            >
              Cảnh báo An ninh!
            </el-button>
            <el-tag v-else type="success" size="small" effect="plain">
              ✓ An toàn
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Ngày nộp" width="130" align="center">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleDateString('vi-VN') }}
          </template>
        </el-table-column>

        <el-table-column label="Hành động" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="success"
              size="small"
              :icon="CircleCheckFilled"
              @click="handleApprove(row)"
            >
              Duyệt
            </el-button>
            <el-button
              type="danger"
              size="small"
              :icon="CircleCloseFilled"
              @click="openRejectDialog(row)"
            >
              Từ chối
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="pendingSkills.length === 0 && !loading" class="empty-state">
        <el-empty description="Tuyệt vời! Hiện không còn skill nào đang chờ duyệt." />
      </div>
    </div>

    <!-- Reject Reason Dialog -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="Từ chối phê duyệt Skill"
      width="480px"
    >
      <p class="dialog-text">
        Vui lòng cung cấp lý do cụ thể để tác giả hiểu và hoàn thiện lại skill:
      </p>
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="Ví dụ: Thiếu tài liệu hướng dẫn mẫu đầu vào, file script có cú pháp chưa tương thích..."
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">Hủy</el-button>
        <el-button
          type="danger"
          :loading="submittingReject"
          @click="submitReject"
        >
          Xác nhận từ chối
        </el-button>
      </template>
    </el-dialog>

    <!-- Security Alert Detail Dialog -->
    <el-dialog
      v-model="securityDialogVisible"
      title="Chi tiết Cảnh báo An ninh (Security Scan)"
      width="560px"
    >
      <div v-if="selectedSecuritySkill" class="security-alert-box">
        <div class="alert-icon-head">
          <el-icon class="big-warn"><WarningFilled /></el-icon>
          <h3>Phát hiện mẫu mã nguồn tiềm ẩn rủi ro</h3>
        </div>
        <p class="scan-summary">
          Hệ thống quét tự động đã phát hiện các dấu hiệu nhạy cảm trong mã nguồn của skill
          <strong>"{{ selectedSecuritySkill.title }}"</strong>:
        </p>
        <div class="scan-detail-code">
          <pre>{{ selectedSecuritySkill.securityScanDetail || 'Mã nguồn chứa lệnh nguy hiểm hoặc khóa xác thực tiềm năng.' }}</pre>
        </div>
        <p class="scan-advice">
          ⚠️ Hãy kiểm tra kỹ lưỡng nội dung file trước khi đưa ra quyết định phê duyệt.
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="securityDialogVisible = false">Đã hiểu</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.curator-container {
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

.content-card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.skill-info-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.skill-title {
  font-weight: 700;
  color: #1e293b;
  cursor: pointer;
  font-size: 15px;
}

.skill-title:hover {
  color: #4f46e5;
  text-decoration: underline;
}

.skill-desc-snippet {
  font-size: 12px;
  color: #64748b;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.category-tags {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}

.cat-badge {
  font-size: 11px;
  background: #f1f5f9;
  color: #475569;
  padding: 1px 6px;
  border-radius: 4px;
}

.author-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.dialog-text {
  font-size: 14px;
  color: #475569;
  margin-bottom: 12px;
}

.security-alert-box {
  padding: 8px 0;
}

.alert-icon-head {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #dc2626;
  margin-bottom: 12px;
}

.big-warn {
  font-size: 26px;
}

.alert-icon-head h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

.scan-summary {
  font-size: 14px;
  color: #334155;
  margin-bottom: 12px;
}

.scan-detail-code {
  background: #fef2f2;
  border: 1px solid #fee2e2;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 14px;
}

.scan-detail-code pre {
  margin: 0;
  font-family: monospace;
  font-size: 13px;
  color: #991b1b;
  white-space: pre-wrap;
}

.scan-advice {
  font-size: 13px;
  color: #b91c1c;
  font-weight: 600;
  margin: 0;
}
</style>
