<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { skillService } from '@/services/skillService'
import { useAuthStore } from '@/stores/auth'
import type {
  SkillResponse,
  SkillVersionResponse,
  ReviewResponse,
  ImpactRecordResponse,
} from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Download,
  StarFilled,
  Check,
  Warning,
  TrendCharts,
  Pointer,
  ChatDotSquare,
  Delete,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const skillId = Number(route.params.id)
const skill = ref<SkillResponse | null>(null)
const versions = ref<SkillVersionResponse[]>([])
const reviews = ref<ReviewResponse[]>([])
const impacts = ref<ImpactRecordResponse[]>([])
const loading = ref(true)

// User Rating
const userRating = ref<number>(0)
const submittingRating = ref(false)

// New Review
const newReviewContent = ref('')
const submittingReview = ref(false)

// Impact Record Dialog
const impactDialogVisible = ref(false)
const submittingImpact = ref(false)
const impactForm = ref({
  referenceCode: '',
  effectivenessScore: 5,
  estimatedMmSaved: 0.5,
  note: '',
})

// Report Dialog
const reportDialogVisible = ref(false)
const reportingReviewId = ref<number | null>(null)
const reportForm = ref<{
  category: 'SECURITY_RISK' | 'BUG' | 'SPAM' | 'OTHER'
  detail: string
}>({
  category: 'OTHER',
  detail: '',
})

const canManage = computed(() => {
  return auth.canManageSkill(skill.value?.author.id)
})

async function fetchSkillData() {
  loading.value = true
  try {
    const [skillData, versionsData, reviewsData, impactsData, myRatingData] =
      await Promise.all([
        skillService.getSkill(skillId),
        skillService.getSkillVersions(skillId),
        skillService.getReviews(skillId),
        skillService.getSkillImpacts(skillId),
        skillService.getMyRating(skillId),
      ])

    skill.value = skillData
    versions.value = versionsData
    reviews.value = reviewsData.content
    impacts.value = impactsData
    if (myRatingData) {
      userRating.value = myRatingData.rating
    }
  } catch (error) {
    ElMessage.error('Không tìm thấy thông tin skill')
    router.push('/skills')
  } finally {
    loading.value = false
  }
}

async function handleDownload(version?: number) {
  try {
    const blob = await skillService.downloadSkill(skillId, version)
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = skill.value?.fileName || `${skill.value?.title}.txt`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    ElMessage.success('Đã bắt đầu tải file xuống')
  } catch (err) {
    ElMessage.error('Lỗi khi tải file')
  }
}

async function handleRecordUsage() {
  try {
    const res = await skillService.recordUsage(skillId)
    if (res.newlyRecorded) {
      ElMessage.success('Đã ghi nhận 1 lượt sử dụng skill hôm nay!')
      if (skill.value) {
        skill.value.usageCount = res.totalSkillUsage
      }
    } else {
      ElMessage.info('Bạn đã được tính lượt sử dụng cho skill này trong ngày hôm nay rồi.')
    }
  } catch (err) {
    ElMessage.error('Lỗi khi ghi nhận sử dụng')
  }
}

async function handleRate(value: number) {
  submittingRating.value = true
  try {
    const res = await skillService.rateSkill(skillId, value)
    userRating.value = res.rating
    ElMessage.success('Đã lưu đánh giá của bạn!')
    // Refresh skill metrics
    const updated = await skillService.getSkill(skillId)
    if (skill.value) {
      skill.value.averageRating = updated.averageRating
      skill.value.ratingCount = updated.ratingCount
    }
  } catch (err) {
    ElMessage.error('Không thể lưu đánh giá')
  } finally {
    submittingRating.value = false
  }
}

async function handleAddReview() {
  if (!newReviewContent.value.trim()) {
    ElMessage.warning('Vui lòng nhập nội dung đánh giá')
    return
  }

  submittingReview.value = true
  try {
    const newRev = await skillService.createReview(skillId, {
      content: newReviewContent.value.trim(),
    })
    reviews.value.unshift(newRev)
    newReviewContent.value = ''
    ElMessage.success('Đã gửi nhận xét thành công!')
  } catch (err) {
    ElMessage.error('Lỗi khi gửi nhận xét')
  } finally {
    submittingReview.value = false
  }
}

async function handleVoteHelpful(review: ReviewResponse) {
  try {
    const count = await skillService.voteHelpful(review.id)
    review.helpfulCount = count
    ElMessage.success('Đã vote nhận xét hữu ích')
  } catch (err) {
    ElMessage.error('Không thể vote nhận xét này')
  }
}

function openReportDialog(reviewId: number) {
  reportingReviewId.value = reviewId
  reportForm.value = { category: 'OTHER', detail: '' }
  reportDialogVisible.value = true
}

async function submitReport() {
  if (!reportingReviewId.value) return
  try {
    await skillService.reportReview(reportingReviewId.value, reportForm.value)
    ElMessage.success('Cảm ơn bạn đã gửi báo cáo vi phạm')
    reportDialogVisible.value = false
  } catch (err) {
    ElMessage.error('Lỗi khi gửi báo cáo')
  }
}

async function submitImpact() {
  if (!impactForm.value.referenceCode.trim()) {
    ElMessage.warning('Vui lòng nhập mã RFC/OC')
    return
  }

  submittingImpact.value = true
  try {
    const newRecord = await skillService.recordImpact(skillId, impactForm.value)
    impacts.value.unshift(newRecord)
    impactDialogVisible.value = false
    ElMessage.success('Đã ghi nhận giá trị thực tế của Skill!')
    // Update local mm total
    if (skill.value) {
      skill.value.totalMmSaved += impactForm.value.estimatedMmSaved
    }
  } catch (err) {
    ElMessage.error('Lỗi khi lưu tác động')
  } finally {
    submittingImpact.value = false
  }
}

async function handleDeleteSkill() {
  try {
    await ElMessageBox.confirm('Bạn có chắc muốn xóa skill này? Thao tác sẽ đưa skill vào danh sách lưu trữ.', 'Xác nhận xóa', {
      type: 'warning',
    })
    await skillService.deleteSkill(skillId)
    ElMessage.success('Đã xóa skill')
    router.push('/skills')
  } catch {
    // cancelled
  }
}

onMounted(() => {
  fetchSkillData()
})
</script>

<template>
  <div class="skill-detail-container" v-loading="loading">
    <div v-if="skill">
      <!-- Breadcrumb & Back -->
      <div class="top-nav">
        <el-button link @click="router.push('/skills')">← Quay lại danh sách</el-button>
        <div class="top-actions" v-if="canManage">
          <el-button type="danger" plain size="small" :icon="Delete" @click="handleDeleteSkill">
            Xóa Skill
          </el-button>
        </div>
      </div>

      <!-- Main Header Banner -->
      <div class="detail-header-card">
        <div class="header-main">
          <div class="categories-row">
            <span v-for="cat in skill.categories" :key="cat.id" class="cat-chip">
              {{ cat.icon || '📁' }} {{ cat.name }}
            </span>
            <el-tag :type="skill.status === 'PUBLISHED' ? 'success' : skill.status === 'PENDING' ? 'warning' : 'info'"
              size="small">
              {{ skill.status }}
            </el-tag>
          </div>

          <h1 class="skill-title">{{ skill.title }}</h1>

          <div class="meta-row">
            <div class="author-meta">
              <el-avatar :size="28" :src="skill.author.avatarUrl || ''">
                {{ skill.author.name.charAt(0).toUpperCase() }}
              </el-avatar>
              <span>Tác giả: <strong>{{ skill.author.name }}</strong></span>
            </div>
            <span class="dot">•</span>
            <span>Phiên bản hiện tại: <strong>v{{ skill.currentVersion }}</strong></span>
            <span class="dot">•</span>
            <span>Ngày tạo: {{ new Date(skill.createdAt).toLocaleDateString('vi-VN') }}</span>
          </div>

          <div class="tags-row" v-if="skill.tags && skill.tags.length">
            <span v-for="tag in skill.tags" :key="tag" class="tag-pill">#{{ tag }}</span>
          </div>
        </div>

        <div class="header-actions">
          <el-button type="primary" size="large" :icon="Download" @click="handleDownload()"
            class="action-btn download-btn">
            Tải Skill (v{{ skill.currentVersion }})
          </el-button>

          <el-button type="success" size="large" :icon="Pointer" @click="handleRecordUsage" class="action-btn">
            Đã áp dụng (Use)
          </el-button>

          <el-button type="warning" plain size="large" :icon="TrendCharts" @click="impactDialogVisible = true"
            class="action-btn">
            Ghi nhận Impact (MM)
          </el-button>
        </div>
      </div>

      <!-- Content Layout: 2 Columns -->
      <div class="content-layout">
        <!-- Left Column: Description, Versions, Impact -->
        <div class="main-column">
          <!-- Description Card -->
          <div class="section-card">
            <h2 class="section-title">Mô tả chi tiết & Hướng dẫn sử dụng</h2>
            <div class="markdown-preview">
              {{ skill.description }}
            </div>
          </div>

          <!-- Versions History Card -->
          <div class="section-card">
            <h2 class="section-title">Lịch sử các phiên bản</h2>
            <el-table :data="versions" stripe style="width: 100%">
              <el-table-column prop="version" label="Version" width="90">
                <template #default="{ row }">
                  <el-tag size="small">v{{ row.version }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="changelog" label="Ghi chú thay đổi" />
              <el-table-column prop="createdAt" label="Ngày phát hành" width="160">
                <template #default="{ row }">
                  {{ new Date(row.createdAt).toLocaleDateString('vi-VN') }}
                </template>
              </el-table-column>
              <el-table-column label="Hành động" width="100" align="center">
                <template #default="{ row }">
                  <el-button link type="primary" :icon="Download" @click="handleDownload(row.version)">
                    Tải
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- Impact Records Card -->
          <div class="section-card">
            <div class="card-header-flex">
              <h2 class="section-title">Tác động thực tế (Impact Records)</h2>
              <el-tag type="success" size="large">
                Tổng tiết kiệm: {{ skill.totalMmSaved }} MM
              </el-tag>
            </div>
            <p class="section-desc">
              Các dự án/RFC thực tế trong phòng ban đã áp dụng skill này và đo lường giá trị mang lại.
            </p>

            <el-table :data="impacts" stripe style="width: 100%" v-if="impacts.length">
              <el-table-column prop="referenceCode" label="Mã RFC / OC" width="160" />
              <el-table-column label="Điểm hiệu quả" width="140">
                <template #default="{ row }">
                  <el-rate :model-value="row.effectivenessScore" disabled size="small" />
                </template>
              </el-table-column>
              <el-table-column label="Tiết kiệm" width="120">
                <template #default="{ row }">
                  <strong>{{ row.estimatedMmSaved }} MM</strong>
                </template>
              </el-table-column>
              <el-table-column prop="note" label="Ghi chú kết quả" />
              <el-table-column prop="userName" label="Người ghi" width="140" />
            </el-table>
            <el-empty v-else description="Chưa có bản ghi tác động nào" :image-size="80" />
          </div>
        </div>

        <!-- Right Column: Ratings & Reviews -->
        <div class="side-column">
          <!-- Rating Summary Box -->
          <div class="section-card rating-box">
            <h3 class="side-title">Đánh giá chất lượng</h3>
            <div class="rating-display">
              <span class="big-score">{{ Number(skill.averageRating || 0).toFixed(1) }}</span>
              <div class="stars-col">
                <el-rate :model-value="Number(skill.averageRating || 0)" disabled allow-half />
                <span class="count-text">Dựa trên {{ skill.ratingCount }} lượt đánh giá</span>
              </div>
            </div>

            <el-divider />

            <!-- User rating input -->
            <div class="my-rate-section">
              <span class="rate-prompt">Đánh giá của bạn:</span>
              <el-rate v-model="userRating" @change="handleRate" :disabled="submittingRating" size="large" />
            </div>
          </div>

          <!-- Reviews List Box -->
          <div class="section-card">
            <h3 class="side-title">
              <el-icon>
                <ChatDotSquare />
              </el-icon> Nhận xét từ người dùng ({{ reviews.length }})
            </h3>

            <!-- Write Review Input -->
            <div class="write-review-box">
              <el-input v-model="newReviewContent" type="textarea" :rows="3"
                placeholder="Chia sẻ trải nghiệm của bạn khi sử dụng skill này..." />
              <div class="review-btn-row">
                <el-button type="primary" size="small" :loading="submittingReview" @click="handleAddReview">
                  Gửi nhận xét
                </el-button>
              </div>
            </div>

            <!-- Reviews Stream -->
            <div class="reviews-list">
              <div v-for="rev in reviews" :key="rev.id" class="review-item">
                <div class="review-header">
                  <div class="review-user">
                    <el-avatar :size="26" :src="rev.userAvatarUrl || ''">
                      {{ rev.userName.charAt(0).toUpperCase() }}
                    </el-avatar>
                    <span class="reviewer-name">{{ rev.userName }}</span>
                  </div>
                  <span class="review-date">{{ new Date(rev.createdAt).toLocaleDateString('vi-VN') }}</span>
                </div>

                <p class="review-content">{{ rev.content }}</p>

                <div class="review-actions">
                  <el-button link size="small" @click="handleVoteHelpful(rev)">
                    👍 Hữu ích ({{ rev.helpfulCount }})
                  </el-button>
                  <el-button link type="danger" size="small" @click="openReportDialog(rev.id)">
                    🚩 Báo cáo
                  </el-button>
                </div>
              </div>

              <div v-if="reviews.length === 0" class="empty-reviews">
                Chưa có nhận xét nào. Hãy là người đầu tiên nhận xét!
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Record Impact Modal -->
    <el-dialog v-model="impactDialogVisible" title="Ghi nhận tác động thực tế (Impact Record)" width="520px">
      <el-form label-position="top">
        <el-form-item label="Mã RFC / OC liên quan *" required>
          <el-input v-model="impactForm.referenceCode" placeholder="Ví dụ: RFC-2026-0912" />
        </el-form-item>

        <el-form-item label="Mức độ hiệu quả (1 - 5 sao)">
          <el-rate v-model="impactForm.effectivenessScore" size="large" />
        </el-form-item>

        <el-form-item label="Số Man-Month (MM) ước tính tiết kiệm được">
          <el-input-number v-model="impactForm.estimatedMmSaved" :min="0.1" :step="0.5" :precision="1" />
        </el-form-item>

        <el-form-item label="Ghi chú chi tiết kết quả đạt được">
          <el-input v-model="impactForm.note" type="textarea" :rows="3"
            placeholder="Ví dụ: Giảm thời gian kiểm thử API từ 2 ngày xuống 4 giờ..." />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="impactDialogVisible = false">Hủy</el-button>
        <el-button type="primary" :loading="submittingImpact" @click="submitImpact">
          Lưu bản ghi
        </el-button>
      </template>
    </el-dialog>

    <!-- Report Review Modal -->
    <el-dialog v-model="reportDialogVisible" title="Báo cáo vi phạm" width="450px">
      <el-form label-position="top">
        <el-form-item label="Lý do báo cáo">
          <el-select v-model="reportForm.category" style="width: 100%">
            <el-option label="Rủi ro an ninh (Security Risk)" value="SECURITY_RISK" />
            <el-option label="Lỗi sai chức năng (Bug)" value="BUG" />
            <el-option label="Spam / Quảng cáo" value="SPAM" />
            <el-option label="Khác" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="Chi tiết">
          <el-input v-model="reportForm.detail" type="textarea" :rows="3"
            placeholder="Mô tả cụ thể nội dung vi phạm..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">Đóng</el-button>
        <el-button type="danger" @click="submitReport">Gửi báo cáo</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.skill-detail-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 20px;
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.detail-header-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  margin-bottom: 24px;
}

.header-main {
  flex: 1;
}

.categories-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.cat-chip {
  background: #eef2ff;
  color: #4f46e5;
  font-size: 13px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
}

.skill-title {
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 12px 0;
  line-height: 1.3;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #64748b;
  margin-bottom: 16px;
}

.author-meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  color: #cbd5e1;
}

.tags-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-pill {
  font-size: 12px;
  background: #f1f5f9;
  color: #475569;
  padding: 2px 8px;
  border-radius: 4px;
}

.header-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 220px;
}

.action-btn {
  width: 100%;
  margin: 0 !important;
  font-weight: 600;
  border-radius: 10px;
}

.download-btn {
  background: #4f46e5;
  border-color: #4f46e5;
}

/* 2-Column Layout */
.content-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
}

.section-card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 14px 0;
}

.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-desc {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 16px 0;
}

.markdown-preview {
  font-size: 15px;
  line-height: 1.7;
  color: #334155;
  white-space: pre-wrap;
}

/* Side Column */
.side-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.rating-display {
  display: flex;
  align-items: center;
  gap: 16px;
}

.big-score {
  font-size: 40px;
  font-weight: 800;
  color: #d97706;
}

.stars-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.count-text {
  font-size: 12px;
  color: #94a3b8;
}

.my-rate-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.rate-prompt {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

.write-review-box {
  margin-bottom: 20px;
}

.review-btn-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding-bottom: 14px;
  border-bottom: 1px solid #f1f5f9;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.reviewer-name {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}

.review-date {
  font-size: 11px;
  color: #94a3b8;
}

.review-content {
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
  margin: 0 0 8px 0;
}

.review-actions {
  display: flex;
  gap: 12px;
}

.empty-reviews {
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
  padding: 16px 0;
}
</style>
