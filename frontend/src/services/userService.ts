import api from './api'
import type {
  ChangePasswordRequest,
  UpdateProfileRequest,
  UserResponse,
} from '@/types'

/**
 * User profile & account management API service.
 */
export const userService = {
  async getCurrentUser(): Promise<UserResponse> {
    const response = await api.get<UserResponse>('/user/current-user')
    return response.data
  },

  async updateProfile(data: UpdateProfileRequest): Promise<UserResponse> {
    const response = await api.put<UserResponse>('/user/profile', data)
    return response.data
  },

  async changePassword(data: ChangePasswordRequest): Promise<{ message: string }> {
    const response = await api.put<{ message: string }>('/user/change-password', data)
    return response.data
  },

  async uploadAvatar(file: File): Promise<{ avatarUrl: string }> {
    const formData = new FormData()
    formData.append('avatar', file)
    const response = await api.post<{ avatarUrl: string }>('/user/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  async deleteAccount(): Promise<{ message: string }> {
    const response = await api.delete<{ message: string }>('/user/account')
    return response.data
  },
}
