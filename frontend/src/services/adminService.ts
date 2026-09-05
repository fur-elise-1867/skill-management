import api from './api'
import type {
  CreateUserRequest,
  ResetPasswordResponse,
  RoleResponse,
  UpdateRoleRequest,
  UpdateStatusRequest,
  UserResponse,
} from '@/types'

/**
 * Admin management API service.
 */
export const adminService = {
  async getRoles(): Promise<RoleResponse[]> {
    const response = await api.get<RoleResponse[]>('/admin/roles')
    return response.data
  },

  async getAllUsers(): Promise<UserResponse[]> {
    const response = await api.get<UserResponse[]>('/admin/get-users')
    return response.data
  },

  async createUser(data: CreateUserRequest): Promise<UserResponse> {
    const response = await api.post<UserResponse>('/admin/create-user', data)
    return response.data
  },

  async updateRole(id: number, data: UpdateRoleRequest): Promise<UserResponse> {
    const response = await api.put<UserResponse>(`/admin/users/${id}/role`, data)
    return response.data
  },

  async updateStatus(id: number, data: UpdateStatusRequest): Promise<UserResponse> {
    const response = await api.put<UserResponse>(`/admin/users/${id}/status`, data)
    return response.data
  },

  async resetPassword(id: number): Promise<ResetPasswordResponse> {
    const response = await api.post<ResetPasswordResponse>(`/admin/users/${id}/reset-password`)
    return response.data
  },

  async deleteUser(email: string): Promise<string> {
    const response = await api.delete<string>(`/admin/delete-user/${email}`)
    return response.data
  },

  async deleteUserById(id: number): Promise<string> {
    const response = await api.delete<string>(`/admin/users/${id}`)
    return response.data
  },
}
