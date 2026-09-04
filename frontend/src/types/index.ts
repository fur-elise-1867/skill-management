/**
 * TypeScript type definitions for the Skill Management API.
 */

export interface UserResponse {
  id: number
  name: string
  email: string
  gender: string | null
  mobile: string | null
  role: 'USER' | 'ADMIN'
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
