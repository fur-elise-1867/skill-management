import api from './api'
import type { CategoryDto } from '@/types'

export const categoryService = {
  async getCategories(): Promise<CategoryDto[]> {
    const response = await api.get<CategoryDto[]>('/categories')
    return response.data
  },

  async createCategory(data: { name: string; description?: string; icon?: string }): Promise<CategoryDto> {
    const response = await api.post<CategoryDto>('/categories', data)
    return response.data
  },

  async updateCategory(
    id: number,
    data: { name: string; description?: string; icon?: string },
  ): Promise<CategoryDto> {
    const response = await api.put<CategoryDto>(`/categories/${id}`, data)
    return response.data
  },

  async deleteCategory(id: number): Promise<void> {
    await api.delete(`/categories/${id}`)
  },
}
