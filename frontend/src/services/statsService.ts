import api from './api'
import type { DashboardStatsResponse, LeaderboardResponse, MySkillsStatsResponse } from '@/types'

export const statsService = {
  async getDashboardStats(): Promise<DashboardStatsResponse> {
    const response = await api.get<DashboardStatsResponse>('/stats/dashboard')
    return response.data
  },

  async getLeaderboard(): Promise<LeaderboardResponse> {
    const response = await api.get<LeaderboardResponse>('/stats/leaderboard')
    return response.data
  },

  async getMyStats(): Promise<MySkillsStatsResponse> {
    const response = await api.get<MySkillsStatsResponse>('/stats/my')
    return response.data
  },
}
