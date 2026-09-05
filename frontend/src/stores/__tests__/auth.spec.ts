import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'
import { authService } from '@/services/authService'
import type { UserResponse } from '@/types'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}))

vi.mock('@/services/authService', () => ({
  authService: {
    login: vi.fn(),
    register: vi.fn(),
    getCurrentUser: vi.fn(),
  },
}))

describe('Auth Store (useAuthStore)', () => {
  const sampleUser: UserResponse = {
    id: 1,
    name: 'Test User',
    email: 'test@example.com',
    gender: 'Male',
    mobile: '0912345678',
    role: 'USER',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  }

  const adminUser: UserResponse = {
    ...sampleUser,
    id: 2,
    role: 'ADMIN',
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('TC-FE-STR-001: initializes with default values when localStorage is empty', () => {
    const auth = useAuthStore()
    expect(auth.token).toBeNull()
    expect(auth.user).toBeNull()
    expect(auth.loading).toBe(false)
    expect(auth.isAuthenticated).toBe(false)
    expect(auth.isAdmin).toBe(false)
  })

  it('TC-FE-STR-002: login updates token, fetches user, and navigates to /dashboard', async () => {
    vi.mocked(authService.login).mockResolvedValueOnce({ token: 'mocked-jwt-token' })
    vi.mocked(authService.getCurrentUser).mockResolvedValueOnce(sampleUser)

    const auth = useAuthStore()
    await auth.login('test@example.com', 'password123')

    expect(authService.login).toHaveBeenCalledWith({
      email: 'test@example.com',
      password: 'password123',
    })
    expect(auth.token).toBe('mocked-jwt-token')
    expect(localStorage.getItem('token')).toBe('mocked-jwt-token')
    expect(auth.user).toEqual(sampleUser)
    expect(auth.isAuthenticated).toBe(true)
    expect(auth.isAdmin).toBe(false)
    expect(mockPush).toHaveBeenCalledWith('/dashboard')
    expect(auth.loading).toBe(false)
  })

  it('TC-FE-STR-003: register updates token, fetches user, and navigates to /dashboard', async () => {
    vi.mocked(authService.register).mockResolvedValueOnce({ token: 'mocked-jwt-token' })
    vi.mocked(authService.getCurrentUser).mockResolvedValueOnce(sampleUser)

    const auth = useAuthStore()
    await auth.register('Test User', 'test@example.com', 'password123', 'Male', '0912345678')

    expect(authService.register).toHaveBeenCalledWith({
      name: 'Test User',
      email: 'test@example.com',
      password: 'password123',
      gender: 'Male',
      mobile: '0912345678',
    })
    expect(auth.token).toBe('mocked-jwt-token')
    expect(localStorage.getItem('token')).toBe('mocked-jwt-token')
    expect(auth.user).toEqual(sampleUser)
    expect(mockPush).toHaveBeenCalledWith('/dashboard')
    expect(auth.loading).toBe(false)
  })

  it('TC-FE-STR-004: fetchCurrentUser error automatically triggers logout', async () => {
    localStorage.setItem('token', 'expired-token')
    vi.mocked(authService.getCurrentUser).mockRejectedValueOnce(new Error('Unauthorized'))

    const auth = useAuthStore()
    auth.token = 'expired-token'
    auth.user = sampleUser

    await auth.fetchCurrentUser()

    expect(auth.token).toBeNull()
    expect(auth.user).toBeNull()
    expect(auth.isAuthenticated).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()
    expect(mockPush).toHaveBeenCalledWith('/login')
  })

  it('TC-FE-STR-005: logout resets state, removes token from localStorage and redirects to /login', () => {
    localStorage.setItem('token', 'sample-token')
    const auth = useAuthStore()
    auth.token = 'sample-token'
    auth.user = adminUser

    expect(auth.isAdmin).toBe(true)
    expect(auth.isAuthenticated).toBe(true)

    auth.logout()

    expect(auth.token).toBeNull()
    expect(auth.user).toBeNull()
    expect(auth.isAuthenticated).toBe(false)
    expect(auth.isAdmin).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()
    expect(mockPush).toHaveBeenCalledWith('/login')
  })
})
