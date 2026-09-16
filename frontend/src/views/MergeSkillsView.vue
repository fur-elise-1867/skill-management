<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { skillService } from '@/services/skillService'
import { curatorService } from '@/services/curatorService'
import type { SkillResponse } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Operation, Right, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()

const publishedSkills = ref<SkillResponse[]>([])
const loadingSkills = ref(false)
const submittingMerge = ref(false)

const mergeForm = reactive({
  sourceSkillId: undefined as number | undefined,
  targetSkillId: undefined as number | undefined,
  reason: '',
})

const sourceSkill = ref<SkillResponse | null>(null)
const targetSkill = ref<SkillResponse | null>(null)

async function fetchSkills() {
  loadingSkills.value = true
  try {
    const res = await skillService.getSkills({ size: 100, status: 'PUBLISHED' })
    publishedSkills.value = res.content
  } catch {
    ElMessage.error('Không thể tải danh sách skills')
  } finally {
    loadingSkills.value = false
  }
}

function handleSourceChange(id: number) {
  sourceSkill.value = publishedSkills.value.find((s) => s.id === id) || null
}

function handleTargetChange(id: number) {
  targetSkill.value = publishedSkills.value.find((s) => s.id === id) || null
}

async function handleMerge() {
  if (!mergeForm.sourceSkillId || !mergeForm.targetSkillId) {
    ElMessage.warning('Vui lòng chọn cả Skill nguồn và Skill đích')
    return
  }
  if (mergeForm.sourceSkillId === mergeForm.targetSkillId) {
    ElMessage.error('Skill nguồn và Skill đích không thể là cùng một skill')
    return
  }
  if (!mergeForm.reason.trim()) {
    ElMessage.warning('Vui lòng cung cấp lý do gộp skill')
    return
  }

  try {
    await ElMessageBox.confirm(
      `Bạn có chắc chắn muốn gộp skill "${sourceSkill.value?.title}" vào "${targetSkill.value?.title}"? Skill nguồn sẽ chuyển sang trạng thái MERGED và không thể hoàn tác.`,
      'Xác nhận gộp Skill trùng lặp',
      { type: 'warning' },
    )

    submittingMerge.value = true
    await curatorService.mergeSkills({
      sourceSkillId: mergeForm.sourceSkillId,
      targetSkillId: mergeForm.targetSkillId,
      reason: mergeForm.reason.trim(),
    })

    ElMessage.success('Đã gộp 2 skill thành công!')
    router.push(`/skills/${mergeForm.targetSkillId}`)
  } catch {
    // cancelled or error
  } finally {
    submittingMerge.value = false
  }
}

onMounted(() => {
  fetchSkills()
})
</script>

<template>
  <div class="merge-container">
    <div class="page-header">
      <h1 class="page-title">Gộp Skill trùng lặp (Merge Skills)</h1>
      <p class="page-subtitle">
        Kết hợp hai skill có cùng mục đích để tập trung lượt dùng, đánh giá và tránh phân mảnh chất lượng
      </p>
    </div>

    <!-- Explanation Box -->
    <div class="info-alert">
      <el-icon class="info-icon"><InfoFilled /></el-icon>
      <div class="info-text">
        <strong>Quy tắc khi gộp Skill:</strong>
        <ul>
          <li><strong>Skill nguồn:</strong> Sẽ chuyển sang trạng thái <code>MERGED</code> và tự động điều hướng sang Skill đích.</li>
          <li><strong>Skill đích:</strong> Tiếp tục hoạt động, tiếp nhận các đánh giá (Rating) và bình luận (Review) từ skill nguồn.</li>
          <li><strong>Điểm Rating:</strong> Hệ thống tính lại điểm trung bình có trọng số theo công thức:
            <code>(AvgA × CountA + AvgB × CountB) / (CountA + CountB)</code>.
          </li>
        </ul>
      </div>
    </div>

    <!-- Merge Form Card -->
    <div class="form-card">
      <el-form label-position="top">
        <div class="skills-select-grid">
          <!-- Source Skill -->
          <div class="select-col source">
            <el-form-item label="1. Chọn Skill nguồn (sẽ bị gộp)" required>
              <el-select
                v-model="mergeForm.sourceSkillId"
                placeholder="Chọn skill trùng lặp..."
                filterable
                size="large"
                style="width: 100%"
                @change="handleSourceChange"
              >
                <el-option
                  v-for="s in publishedSkills"
                  :key="s.id"
                  :label="s.title"
                  :value="s.id"
                  :disabled="s.id === mergeForm.targetSkillId"
                />
              </el-select>
            </el-form-item>

            <div v-if="sourceSkill" class="skill-preview-card">
              <span class="preview-title">{{ sourceSkill.title }}</span>
              <span class="preview-author">Tác giả: {{ sourceSkill.author.name }}</span>
              <div class="preview-stats">
                <span>⭐ {{ Number(sourceSkill.averageRating || 0).toFixed(1) }} ({{ sourceSkill.ratingCount }})</span>
                <span>👀 {{ sourceSkill.usageCount }} lượt dùng</span>
              </div>
            </div>
          </div>

          <!-- Arrow Icon -->
          <div class="merge-arrow-col">
            <el-icon class="big-arrow"><Right /></el-icon>
          </div>

          <!-- Target Skill -->
          <div class="select-col target">
            <el-form-item label="2. Chọn Skill đích (giữ lại & nhận dữ liệu)" required>
              <el-select
                v-model="mergeForm.targetSkillId"
                placeholder="Chọn skill chuẩn để giữ lại..."
                filterable
                size="large"
                style="width: 100%"
                @change="handleTargetChange"
              >
                <el-option
                  v-for="s in publishedSkills"
                  :key="s.id"
                  :label="s.title"
                  :value="s.id"
                  :disabled="s.id === mergeForm.sourceSkillId"
                />
              </el-select>
            </el-form-item>

            <div v-if="targetSkill" class="skill-preview-card">
              <span class="preview-title">{{ targetSkill.title }}</span>
              <span class="preview-author">Tác giả: {{ targetSkill.author.name }}</span>
              <div class="preview-stats">
                <span>⭐ {{ Number(targetSkill.averageRating || 0).toFixed(1) }} ({{ targetSkill.ratingCount }})</span>
                <span>👀 {{ targetSkill.usageCount }} lượt dùng</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Reason -->
        <el-form-item label="Lý do gộp Skill *" required style="margin-top: 24px">
          <el-input
            v-model="mergeForm.reason"
            type="textarea"
            :rows="3"
            placeholder="Ví dụ: Hai skill này cùng giải quyết bài toán review mã nguồn Spring Boot, gộp lại để tránh phân tán..."
          />
        </el-form-item>

        <div class="form-actions">
          <el-button size="large" @click="router.back()">Quay lại</el-button>
          <el-button
            type="primary"
            size="large"
            :icon="Operation"
            :loading="submittingMerge"
            @click="handleMerge"
            class="merge-btn"
          >
            Thực hiện gộp Skill
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.merge-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 20px;
}

.page-header {
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

.info-alert {
  background: #f0f9ff;
  border: 1px solid #e0f2fe;
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.info-icon {
  font-size: 22px;
  color: #0284c7;
  margin-top: 2px;
}

.info-text {
  font-size: 13px;
  color: #0369a1;
  line-height: 1.6;
}

.info-text ul {
  margin: 6px 0 0 0;
  padding-left: 18px;
}

.form-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.skills-select-grid {
  display: grid;
  grid-template-columns: 1fr 40px 1fr;
  align-items: flex-start;
  gap: 16px;
}

.merge-arrow-col {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 40px;
}

.big-arrow {
  font-size: 24px;
  color: #94a3b8;
}

.skill-preview-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.preview-title {
  font-weight: 600;
  font-size: 13px;
  color: #1e293b;
}

.preview-author {
  font-size: 12px;
  color: #64748b;
}

.preview-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #475569;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
}

.merge-btn {
  background: #4f46e5;
  border-color: #4f46e5;
  font-weight: 600;
}
</style>
