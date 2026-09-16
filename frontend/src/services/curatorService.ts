import api from './api'
import type { PendingSkillResponse, RejectRequest, MergeSkillsRequest, SkillResponse } from '@/types'

export const curatorService = {
  async getPendingSkills(): Promise<PendingSkillResponse[]> {
    const response = await api.get<PendingSkillResponse[]>('/skills/pending')
    return response.data
  },

  async approveSkill(skillId: number): Promise<SkillResponse> {
    const response = await api.post<SkillResponse>(`/skills/${skillId}/approve`)
    return response.data
  },

  async rejectSkill(skillId: number, data: RejectRequest): Promise<SkillResponse> {
    const response = await api.post<SkillResponse>(`/skills/${skillId}/reject`, data)
    return response.data
  },

  async mergeSkills(data: MergeSkillsRequest): Promise<SkillResponse> {
    const response = await api.post<SkillResponse>('/skills/merge', data)
    return response.data
  },
}
