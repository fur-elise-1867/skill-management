import api from './api'
import type {
  AuthenticationRequest,
  AuthenticationResponse,
  RegisterRequest,
  UserResponse,
} from '@/types'

/**
 * Authentication API service.
 */
export const authService = {
  async register(data: RegisterRequest): Promise<AuthenticationResponse> {
    const response = await api.post<AuthenticationResponse>('/auth/register', data)
    return response.data
  },

  async login(data: AuthenticationRequest): Promise<AuthenticationResponse> {
    const response = await api.post<AuthenticationResponse>('/auth/authenticate', data)
    return response.data
  },

  async getCurrentUser(): Promise<UserResponse> {
    const response = await api.get<UserResponse>('/user/current-user')
    return response.data
  },

  async getAllUsers(): Promise<UserResponse[]> {
    const response = await api.get<UserResponse[]>('/admin/get-users')
    return response.data
  },

  async deleteUser(email: string): Promise<string> {
    const response = await api.delete<string>(`/admin/delete-user/${email}`)
    return response.data
  },
}
