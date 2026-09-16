<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { statsService } from '@/services/statsService'
import type { DashboardStatsResponse, LeaderboardResponse } from '@/types'
import {
  Collection,
  Pointer,
  TrendCharts,
  Timer,
  Plus,
  Files,
  Medal,
  StarFilled,
  View,
} from '@element-plus/icons-vue'

const auth = useAuthStore()
const router = useRouter()

const dashboardStats = ref<DashboardStatsResponse | null>(null)
const leaderboard = ref<LeaderboardResponse | null>(null)
const loading = ref(true)
const activeLeaderboardTab = ref('rating')

async function loadData() {
  loading.value = true
  try {
    const [dashRes, leadRes] = await Promise.all([
      statsService.getDashboardStats(),
      statsService.getLeaderboard(),
    ])
    dashboardStats.value = dashRes
    leaderboard.value = leadRes
  } catch {
    // fallback or fail gracefully
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="dashboard-page" v-loading="loading">
    <!-- Hero Welcome Banner -->
    <div class="welcome-banner">
      <div class="welcome-text">
        <h1 class="greeting">
          Xin chào, <span>{{ auth.user?.name || 'Bạn' }}</span> 👋
        </h1>
        <p class="greeting-sub">
          Chào mừng đến với AI Skill Store — Nơi chia sẻ, tìm kiếm và nhân rộng các AI Skills giúp nâng cao năng suất toàn phòng ban.
        </p>
      </div>

      <div class="welcome-actions">
        <el-button
          type="primary"
          size="large"
          :icon="Files"
          @click="router.push('/skills')"
          class="hero-btn primary"
        >
          Khám phá Skills
        </el-button>
        <el-button
          size="large"
          :icon="Plus"
          @click="router.push('/skills/upload')"
          class="hero-btn"
        >
          Đóng góp Skill mới
        </el-button>
      </div>
    </div>

    <!-- KPI Statistics Grid -->
    <div class="kpi-grid" v-if="dashboardStats">
      <div class="kpi-card">
        <div class="kpi-icon-box blue">
          <el-icon><Collection /></el-icon>
        </div>
        <div class="kpi-meta">
          <span class="kpi-number">{{ dashboardStats.publishedSkills }}</span>
          <span class="kpi-label">Skills đang lưu hành</span>
        </div>
      </div>

      <div class="kpi-card">
        <div class="kpi-icon-box purple">
          <el-icon><Pointer /></el-icon>
        </div>
        <div class="kpi-meta">
          <span class="kpi-number">{{ dashboardStats.totalUsage }}</span>
          <span class="kpi-label">Tổng lượt áp dụng</span>
        </div>
      </div>

      <div class="kpi-card">
        <div class="kpi-icon-box green">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="kpi-meta">
          <span class="kpi-number">{{ dashboardStats.totalMmSavedThisMonth }} MM</span>
          <span class="kpi-label">Tiết kiệm trong tháng</span>
        </div>
      </div>

      <div class="kpi-card">
        <div class="kpi-icon-box amber">
          <el-icon><Timer /></el-icon>
        </div>
        <div class="kpi-meta">
          <span class="kpi-number">{{ dashboardStats.pendingSkills }}</span>
          <span class="kpi-label">Skill đang chờ duyệt</span>
        </div>
      </div>
    </div>

    <!-- Main Content Layout -->
    <div class="dashboard-grid">
      <!-- Left Column: Leaderboard -->
      <div class="main-panel">
        <div class="card-box">
          <div class="card-header-row">
            <h2 class="card-title">
              <el-icon class="title-icon"><Medal /></el-icon> Bảng xếp hạng AI Skills (Leaderboard)
            </h2>
          </div>

          <el-tabs v-model="activeLeaderboardTab" class="leaderboard-tabs">
            <!-- Tab By Rating -->
            <el-tab-pane label="Đánh giá cao nhất" name="rating">
              <div class="ranking-list" v-if="leaderboard?.byRating.length">
                <div
                  v-for="(item, idx) in leaderboard.byRating"
                  :key="item.id"
                  class="ranking-item"
                  @click="router.push(`/skills/${item.id}`)"
                >
                  <span class="rank-index" :class="`top-${idx + 1}`">{{ idx + 1 }}</span>
                  <div class="rank-info">
                    <span class="rank-name">{{ item.title }}</span>
                    <span class="rank-author">bởi {{ item.author.name }}</span>
                  </div>
                  <div class="rank-score">
                    <el-icon class="star"><StarFilled /></el-icon>
                    <strong>{{ Number(item.averageRating || 0).toFixed(1) }}</strong>
                    <span class="rating-sub">({{ item.ratingCount }})</span>
                  </div>
                </div>
              </div>
              <el-empty v-else description="Chưa có dữ liệu đánh giá" :image-size="70" />
            </el-tab-pane>

            <!-- Tab By Usage -->
            <el-tab-pane label="Áp dụng nhiều nhất" name="usage">
              <div class="ranking-list" v-if="leaderboard?.byUsage.length">
                <div
                  v-for="(item, idx) in leaderboard.byUsage"
                  :key="item.id"
                  class="ranking-item"
                  @click="router.push(`/skills/${item.id}`)"
                >
                  <span class="rank-index" :class="`top-${idx + 1}`">{{ idx + 1 }}</span>
                  <div class="rank-info">
                    <span class="rank-name">{{ item.title }}</span>
                    <span class="rank-author">bởi {{ item.author.name }}</span>
                  </div>
                  <div class="rank-metric">
                    <el-icon><View /></el-icon>
                    <strong>{{ item.usageCount }}</strong> lượt dùng
                  </div>
                </div>
              </div>
              <el-empty v-else description="Chưa có dữ liệu sử dụng" :image-size="70" />
            </el-tab-pane>

            <!-- Tab By Impact -->
            <el-tab-pane label="Giá trị thực tế cao nhất (MM)" name="impact">
              <div class="ranking-list" v-if="leaderboard?.byImpact.length">
                <div
                  v-for="(item, idx) in leaderboard.byImpact"
                  :key="item.id"
                  class="ranking-item"
                  @click="router.push(`/skills/${item.id}`)"
                >
                  <span class="rank-index" :class="`top-${idx + 1}`">{{ idx + 1 }}</span>
                  <div class="rank-info">
                    <span class="rank-name">{{ item.title }}</span>
                    <span class="rank-author">bởi {{ item.author.name }}</span>
                  </div>
                  <div class="rank-impact">
                    <el-icon><TrendCharts /></el-icon>
                    <strong>{{ item.totalMmSaved }} MM</strong>
                  </div>
                </div>
              </div>
              <el-empty v-else description="Chưa có dữ liệu tác động" :image-size="70" />
            </el-tab-pane>

            <!-- Tab Top Contributors -->
            <el-tab-pane label="Top Contributors 🏆" name="contributors">
              <div class="ranking-list" v-if="leaderboard?.topContributors.length">
                <div
                  v-for="(item, idx) in leaderboard.topContributors"
                  :key="item.authorId"
                  class="ranking-item contributor-item"
                >
                  <span class="rank-index" :class="`top-${idx + 1}`">{{ idx + 1 }}</span>
                  <div class="rank-info">
                    <span class="rank-name">{{ item.authorName }}</span>
                    <span class="rank-author">Thành viên phòng ban</span>
                  </div>
                  <div class="rank-badge">
                    <el-tag type="success" size="small">
                      {{ item.publishedSkillsCount }} skills đóng góp
                    </el-tag>
                  </div>
                </div>
              </div>
              <el-empty v-else description="Chưa có dữ liệu người đóng góp" :image-size="70" />
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>

      <!-- Right Column: Top Categories & Recent Skills -->
      <div class="side-panel">
        <!-- Top Categories Breakdown -->
        <div class="card-box" v-if="dashboardStats?.topCategories.length">
          <h3 class="side-box-title">Mảng kỹ năng phổ biến</h3>
          <div class="categories-list">
            <div
              v-for="cat in dashboardStats.topCategories"
              :key="cat.name"
              class="cat-stat-item"
            >
              <span class="cat-stat-name">{{ cat.name }}</span>
              <el-tag size="small" effect="plain">{{ cat.skillCount }} skills</el-tag>
            </div>
          </div>
        </div>

        <!-- Recent Skills -->
        <div class="card-box" v-if="dashboardStats?.recentSkills.length">
          <h3 class="side-box-title">Skills mới phát hành</h3>
          <div class="recent-list">
            <div
              v-for="skill in dashboardStats.recentSkills"
              :key="skill.id"
              class="recent-item"
              @click="router.push(`/skills/${skill.id}`)"
            >
              <span class="recent-title">{{ skill.title }}</span>
              <div class="recent-meta">
                <span>{{ skill.author.name }}</span>
                <span>•</span>
                <span>{{ new Date(skill.createdAt).toLocaleDateString('vi-VN') }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 20px;
}

/* Welcome Banner */
.welcome-banner {
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 40%, #4338ca 100%);
  border-radius: 20px;
  padding: 40px 36px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  box-shadow: 0 10px 30px -5px rgba(49, 46, 129, 0.25);
}

.welcome-text {
  max-width: 720px;
}

.greeting {
  font-size: 30px;
  font-weight: 800;
  margin: 0 0 10px 0;
  letter-spacing: -0.02em;
}

.greeting span {
  color: #818cf8;
}

.greeting-sub {
  font-size: 15px;
  color: #c7d2fe;
  line-height: 1.6;
  margin: 0;
}

.welcome-actions {
  display: flex;
  gap: 12px;
}

.hero-btn {
  font-weight: 600;
  border-radius: 10px;
  height: 46px;
}

.hero-btn.primary {
  background: #4f46e5;
  border-color: #4f46e5;
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
  border-radius: 16px;
  padding: 22px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  gap: 16px;
}

.kpi-icon-box {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.kpi-icon-box.blue {
  background: #eff6ff;
  color: #2563eb;
}

.kpi-icon-box.purple {
  background: #f5f3ff;
  color: #7c3aed;
}

.kpi-icon-box.green {
  background: #ecfdf5;
  color: #059669;
}

.kpi-icon-box.amber {
  background: #fffbeb;
  color: #d97706;
}

.kpi-meta {
  display: flex;
  flex-direction: column;
}

.kpi-number {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
}

.kpi-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

/* 2-Column Content */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
}

.card-box {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
}

.card-header-row {
  margin-bottom: 16px;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  color: #f59e0b;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  border-radius: 10px;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.15s;
}

.ranking-item:hover {
  background: #eef2ff;
  transform: translateX(4px);
}

.rank-index {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  background: #e2e8f0;
  color: #64748b;
  margin-right: 14px;
}

.rank-index.top-1 {
  background: #fef08a;
  color: #854d0e;
}

.rank-index.top-2 {
  background: #e2e8f0;
  color: #334155;
}

.rank-index.top-3 {
  background: #fed7aa;
  color: #9a3412;
}

.rank-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.rank-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.rank-author {
  font-size: 12px;
  color: #64748b;
}

.rank-score {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #d97706;
  font-size: 14px;
}

.rank-score .star {
  color: #f59e0b;
}

.rating-sub {
  font-size: 11px;
  color: #94a3b8;
}

.rank-metric {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #4f46e5;
}

.rank-impact {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #059669;
  font-weight: 600;
}

/* Side Box */
.side-box-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 16px 0;
}

.categories-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cat-stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  border-radius: 8px;
  background: #f8fafc;
}

.cat-stat-name {
  font-size: 13px;
  color: #334155;
  font-weight: 500;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recent-item {
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
}

.recent-item:last-child {
  border-bottom: none;
}

.recent-title {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  display: block;
}

.recent-title:hover {
  color: #4f46e5;
}

.recent-meta {
  font-size: 11px;
  color: #94a3b8;
  display: flex;
  gap: 6px;
  margin-top: 2px;
}
</style>
