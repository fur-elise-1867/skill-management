/**
 * TypeScript type definitions for the Skill Management API.
 */

export interface RoleResponse {
  id: number
  name: string
  description: string | null
}

export interface UserResponse {
  id: number
  name: string
  email: string
  gender: string | null
  mobile: string | null
  role: RoleResponse
  enabled: boolean
  avatarUrl: string | null
  createdAt: string
  updatedAt: string
}

export interface AuthenticationResponse {
  token: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
  gender?: string
  mobile?: string
}

export interface AuthenticationRequest {
  email: string
  password: string
}

export interface UpdateProfileRequest {
  name: string
  gender?: string
  mobile?: string
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

export interface CreateUserRequest {
  name: string
  email: string
  password: string
  roleId: number
  gender?: string
  mobile?: string
  enabled?: boolean
}

export interface UpdateRoleRequest {
  roleId: number
}

export interface UpdateStatusRequest {
  enabled: boolean
}

export interface ResetPasswordResponse {
  newPassword: string
}
