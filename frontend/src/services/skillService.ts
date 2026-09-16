import api from './api'
import type {
  PageResponse,
  SkillResponse,
  SkillVersionResponse,
  SkillUpdateRequest,
  RatingResponse,
  ReviewResponse,
  ReviewCreateRequest,
  ReviewUpdateRequest,
  ReviewReportRequest,
  UsageRecordResponse,
  ImpactRecordRequest,
  ImpactRecordResponse,
} from '@/types'

export interface GetSkillsParams {
  page?: number
  size?: number
  search?: string
  categoryId?: number
  status?: string
  sortBy?: string
  sortOrder?: 'ASC' | 'DESC'
}

export const skillService = {
  async getSkills(params?: GetSkillsParams): Promise<PageResponse<SkillResponse>> {
    const response = await api.get<PageResponse<SkillResponse>>('/skills', { params })
    return response.data
  },

  async getSkill(id: number): Promise<SkillResponse> {
    const response = await api.get<SkillResponse>(`/skills/${id}`)
    return response.data
  },

  async uploadSkill(formData: FormData): Promise<SkillResponse> {
    const response = await api.post<SkillResponse>('/skills', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  async updateSkill(id: number, data: SkillUpdateRequest): Promise<SkillResponse> {
    const response = await api.put<SkillResponse>(`/skills/${id}`, data)
    return response.data
  },

  async deleteSkill(id: number): Promise<void> {
    await api.delete(`/skills/${id}`)
  },

  async getSkillVersions(id: number): Promise<SkillVersionResponse[]> {
    const response = await api.get<SkillVersionResponse[]>(`/skills/${id}/versions`)
    return response.data
  },

  async downloadSkill(id: number, version?: number): Promise<Blob> {
    const url = version ? `/skills/${id}/versions/${version}/download` : `/skills/${id}/download`
    const response = await api.get(url, { responseType: 'blob' })
    return response.data
  },

  async getSimilarSkills(title: string, description: string): Promise<SkillResponse[]> {
    const response = await api.get<SkillResponse[]>('/skills/similar', {
      params: { title, description },
    })
    return response.data
  },

  async deprecateSkill(id: number, reason: string): Promise<SkillResponse> {
    const response = await api.post<SkillResponse>(`/skills/${id}/deprecate`, { reason })
    return response.data
  },

  // Ratings
  async rateSkill(skillId: number, rating: number): Promise<RatingResponse> {
    const response = await api.post<RatingResponse>(`/skills/${skillId}/ratings`, { rating })
    return response.data
  },

  async getMyRating(skillId: number): Promise<RatingResponse | null> {
    try {
      const response = await api.get<RatingResponse>(`/skills/${skillId}/ratings/my`)
      return response.data
    } catch {
      return null
    }
  },

  // Reviews
  async getReviews(skillId: number, page = 0, size = 10): Promise<PageResponse<ReviewResponse>> {
    const response = await api.get<PageResponse<ReviewResponse>>(`/skills/${skillId}/reviews`, {
      params: { page, size },
    })
    return response.data
  },

  async createReview(skillId: number, data: ReviewCreateRequest): Promise<ReviewResponse> {
    const response = await api.post<ReviewResponse>(`/skills/${skillId}/reviews`, data)
    return response.data
  },

  async updateReview(reviewId: number, data: ReviewUpdateRequest): Promise<ReviewResponse> {
    const response = await api.put<ReviewResponse>(`/reviews/${reviewId}`, data)
    return response.data
  },

  async deleteReview(reviewId: number): Promise<void> {
    await api.delete(`/reviews/${reviewId}`)
  },

  async voteHelpful(reviewId: number): Promise<number> {
    const response = await api.post<number>(`/reviews/${reviewId}/helpful`)
    return response.data
  },

  async reportReview(reviewId: number, data: ReviewReportRequest): Promise<void> {
    await api.post(`/reviews/${reviewId}/report`, data)
  },

  // Usage & Impact
  async recordUsage(skillId: number): Promise<UsageRecordResponse> {
    const response = await api.post<UsageRecordResponse>(`/skills/${skillId}/usage`)
    return response.data
  },

  async recordImpact(skillId: number, data: ImpactRecordRequest): Promise<ImpactRecordResponse> {
    const response = await api.post<ImpactRecordResponse>(`/skills/${skillId}/impact`, data)
    return response.data
  },

  async getSkillImpacts(skillId: number): Promise<ImpactRecordResponse[]> {
    const response = await api.get<ImpactRecordResponse[]>(`/skills/${skillId}/impact`)
    return response.data
  },
}
