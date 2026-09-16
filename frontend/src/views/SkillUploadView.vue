<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { skillService } from '@/services/skillService'
import { useCategoryStore } from '@/stores/category'
import type { SkillResponse } from '@/types'
import { ElMessage, type UploadFile } from 'element-plus'
import { UploadFilled, WarningFilled, Document, TopRight } from '@element-plus/icons-vue'

const router = useRouter()
const categoryStore = useCategoryStore()

const submitting = ref(false)
const checkingDuplicate = ref(false)
const similarSkills = ref<SkillResponse[]>([])

const form = reactive({
  title: '',
  description: '',
  categoryIds: [] as number[],
  tagsInput: '',
  changelog: 'Initial version',
})

const selectedFile = ref<File | null>(null)

// Check for duplicates
async function checkDuplicate() {
  if (form.title.trim().length < 3 && form.description.trim().length < 5) {
    similarSkills.value = []
    return
  }

  checkingDuplicate.value = true
  try {
    const list = await skillService.getSimilarSkills(form.title, form.description)
    similarSkills.value = list
  } catch {
    // silently fail
  } finally {
    checkingDuplicate.value = false
  }
}

function handleFileChange(file: UploadFile) {
  if (file.raw) {
    // Check file size 10MB
    if (file.raw.size > 10 * 1024 * 1024) {
      ElMessage.error('Kích thước file không được vượt quá 10MB')
      return
    }
    selectedFile.value = file.raw
  }
}

function openSkillTab(skillId: number) {
  window.open(`/skills/${skillId}`, '_blank')
}

async function handleSubmit() {
  if (!form.title.trim()) {
    ElMessage.warning('Vui lòng nhập tên Skill')
    return
  }
  if (!form.description.trim()) {
    ElMessage.warning('Vui lòng nhập mô tả')
    return
  }
  if (form.categoryIds.length === 0) {
    ElMessage.warning('Vui lòng chọn ít nhất một danh mục')
    return
  }
  if (!selectedFile.value) {
    ElMessage.warning('Vui lòng đính kèm file skill (.md, .py, .sh, .json, .yaml...)')
    return
  }

  submitting.value = true
  try {
    const formData = new FormData()

    const meta = {
      title: form.title.trim(),
      description: form.description.trim(),
      categoryIds: form.categoryIds,
      tagNames: form.tagsInput
        .split(',')
        .map((t) => t.trim())
        .filter((t) => t.length > 0),
      changelog: form.changelog.trim(),
    }

    formData.append(
      'data',
      new Blob([JSON.stringify(meta)], { type: 'application/json' }),
    )
    formData.append('skillFile', selectedFile.value)

    await skillService.uploadSkill(formData)

    ElMessage.success(
      'Upload thành công! Skill của bạn đang ở trạng thái PENDING chờ Curator phê duyệt.',
    )
    router.push('/skills/my')
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || 'Có lỗi xảy ra khi upload skill')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  categoryStore.fetchCategories()
})
</script>

<template>
  <div class="upload-container">
    <div class="upload-header">
      <h1 class="page-title">Đóng góp AI Skill mới</h1>
      <p class="page-subtitle">
        Chia sẻ các prompt, script hoặc template hữu ích để đồng nghiệp trong phòng ban cùng áp dụng và nhân rộng giá trị.
      </p>
    </div>

    <!-- Duplicate Warning Banner -->
    <transition name="el-fade-in">
      <div v-if="similarSkills.length > 0" class="duplicate-warning-banner">
        <div class="warning-icon-col">
          <el-icon class="warn-icon"><WarningFilled /></el-icon>
        </div>
        <div class="warning-body">
          <h4 class="warn-title">Phát hiện Skill có thể trùng lặp!</h4>
          <p class="warn-text">
            Hệ thống nhận thấy nội dung của bạn có nét tương đồng với các skill sau đây. Hãy kiểm tra trước để tránh tạo trùng:
          </p>
          <div class="similar-items">
            <div
              v-for="sim in similarSkills.slice(0, 3)"
              :key="sim.id"
              class="similar-item"
              @click="openSkillTab(sim.id)"
            >
              <el-icon><Document /></el-icon>
              <span class="sim-title">{{ sim.title }}</span>
              <span class="sim-author">bởi {{ sim.author.name }}</span>
              <el-icon><TopRight /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- Upload Form Card -->
    <div class="form-card">
      <el-form label-position="top" class="custom-form">
        <!-- Title -->
        <el-form-item label="Tên AI Skill *" required>
          <el-input
            v-model="form.title"
            placeholder="Ví dụ: Spring Boot Code Review Assistant"
            size="large"
            @blur="checkDuplicate"
          />
        </el-form-item>

        <!-- Categories -->
        <el-form-item label="Danh mục (có thể chọn nhiều) *" required>
          <el-select
            v-model="form.categoryIds"
            multiple
            placeholder="Chọn các mảng áp dụng phù hợp"
            size="large"
            style="width: 100%"
          >
            <el-option
              v-for="cat in categoryStore.categories"
              :key="cat.id"
              :label="cat.icon ? `${cat.icon} ${cat.name}` : cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <!-- Tags -->
        <el-form-item label="Tags (phân cách bằng dấu phẩy)">
          <el-input
            v-model="form.tagsInput"
            placeholder="Ví dụ: java, spring, review, git"
            size="large"
          />
        </el-form-item>

        <!-- Description -->
        <el-form-item label="Mô tả chi tiết & Hướng dẫn sử dụng *" required>
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="Mô tả mục đích của skill, ngữ cảnh sử dụng, các bước thực hiện và lưu ý..."
            @blur="checkDuplicate"
          />
        </el-form-item>

        <!-- Changelog -->
        <el-form-item label="Ghi chú phiên bản (Changelog)">
          <el-input
            v-model="form.changelog"
            placeholder="Ví dụ: Bản khởi tạo ban đầu"
          />
        </el-form-item>

        <!-- File Upload Area -->
        <el-form-item label="Đính kèm file Skill *" required>
          <el-upload
            drag
            action="#"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            accept=".md,.json,.yaml,.yml,.py,.sh,.txt"
            class="drag-upload"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              Kéo thả file vào đây hoặc <em>bấm vào để chọn file</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                Hỗ trợ các định dạng: .md, .py, .sh, .json, .yaml, .txt (tối đa 10MB). Hệ thống sẽ tự động quét rủi ro an ninh.
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <div class="form-actions">
          <el-button size="large" @click="router.back()">Hủy bỏ</el-button>
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            @click="handleSubmit"
            class="submit-btn"
          >
            Đăng tải Skill (Upload)
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.upload-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 20px;
}

.upload-header {
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

/* Duplicate Warning Banner */
.duplicate-warning-banner {
  background: #fffbeb;
  border: 1px solid #fef3c7;
  border-left: 5px solid #f59e0b;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 24px;
  display: flex;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.08);
}

.warn-icon {
  font-size: 24px;
  color: #d97706;
  margin-top: 2px;
}

.warn-title {
  font-size: 15px;
  font-weight: 700;
  color: #92400e;
  margin: 0 0 4px 0;
}

.warn-text {
  font-size: 13px;
  color: #b45309;
  margin: 0 0 10px 0;
}

.similar-items {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.similar-item {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.7);
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  color: #451a03;
  cursor: pointer;
  transition: all 0.15s;
}

.similar-item:hover {
  background: #fff;
  color: #b45309;
}

.sim-title {
  font-weight: 600;
}

.sim-author {
  font-size: 11px;
  color: #78350f;
  margin-left: auto;
}

/* Form Card */
.form-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.custom-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
}

.drag-upload :deep(.el-upload-dragger) {
  border-radius: 12px;
  border-color: #cbd5e1;
}

.drag-upload :deep(.el-upload-dragger:hover) {
  border-color: #4f46e5;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
}

.submit-btn {
  background: #4f46e5;
  border-color: #4f46e5;
  font-weight: 600;
}
</style>
