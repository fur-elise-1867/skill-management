import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import router from '../index'
import { useAuthStore } from '@/stores/auth'
import { authService } from '@/services/authService'
import type { UserResponse } from '@/types'

// Mock view components to avoid full template rendering during router testing
vi.mock('@/views/LoginView.vue', () => ({ default: { template: '<div>Login</div>' } }))
vi.mock('@/views/RegisterView.vue', () => ({ default: { template: '<div>Register</div>' } }))
vi.mock('@/views/DashboardView.vue', () => ({ default: { template: '<div>Dashboard</div>' } }))
vi.mock('@/views/AdminView.vue', () => ({ default: { template: '<div>Admin</div>' } }))

vi.mock('@/services/authService', () => ({
  authService: {
    login: vi.fn(),
    register: vi.fn(),
    getCurrentUser: vi.fn(),
  },
}))

describe('Router Navigation Guards (router/index.ts)', () => {
  const normalUser: UserResponse = {
    id: 1,
    name: 'Normal User',
    email: 'user@example.com',
    gender: 'Male',
    mobile: '0912345678',
    role: 'USER',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  }

  const adminUser: UserResponse = {
    ...normalUser,
    id: 2,
    role: 'ADMIN',
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('TC-FE-RTG-001: unauthenticated user visiting requiresAuth route (/dashboard) redirects to /login', async () => {
    const auth = useAuthStore()
    auth.token = null
    auth.user = null

    await router.push('/dashboard')

    expect(router.currentRoute.value.name).toBe('login')
  })

  it('TC-FE-RTG-001b: unauthenticated user visiting requiresAuth route (/admin) redirects to /login', async () => {
    const auth = useAuthStore()
    auth.token = null
    auth.user = null

    await router.push('/admin')

    expect(router.currentRoute.value.name).toBe('login')
  })

  it('TC-FE-RTG-002: authenticated user visiting requiresGuest route (/login) redirects to /dashboard', async () => {
    const auth = useAuthStore()
    auth.token = 'valid-token'
    auth.user = normalUser

    // Start from /dashboard so navigation to /login is an active transition
    await router.push('/dashboard')
    expect(router.currentRoute.value.name).toBe('dashboard')

    // Try navigating to /login
    await router.push('/login')

    expect(router.currentRoute.value.name).toBe('dashboard')
  })

  it('TC-FE-RTG-002b: authenticated user visiting requiresGuest route (/register) redirects to /dashboard', async () => {
    const auth = useAuthStore()
    auth.token = 'valid-token'
    auth.user = normalUser

    await router.push('/register')

    expect(router.currentRoute.value.name).toBe('dashboard')
  })

  it('TC-FE-RTG-003: regular user (ROLE_USER) visiting requiresAdmin route (/admin) redirects to /dashboard', async () => {
    const auth = useAuthStore()
    auth.token = 'valid-token'
    auth.user = normalUser

    expect(auth.isAdmin).toBe(false)

    await router.push('/admin')

    expect(router.currentRoute.value.name).toBe('dashboard')
  })

  it('TC-FE-RTG-004: admin user (ROLE_ADMIN) visiting requiresAdmin route (/admin) is allowed to proceed', async () => {
    const auth = useAuthStore()
    auth.token = 'admin-token'
    auth.user = adminUser

    expect(auth.isAdmin).toBe(true)

    await router.push('/admin')

    expect(router.currentRoute.value.name).toBe('admin')
  })

  it('TC-FE-RTG-005: token exists but user is null triggers fetchCurrentUser before navigation', async () => {
    vi.mocked(authService.getCurrentUser).mockResolvedValueOnce(normalUser)

    const auth = useAuthStore()
    auth.token = 'existing-token'
    auth.user = null

    expect(auth.isAuthenticated).toBe(true)

    await router.push('/dashboard')

    expect(authService.getCurrentUser).toHaveBeenCalled()
    expect(auth.user).toEqual(normalUser)
    expect(router.currentRoute.value.name).toBe('dashboard')
  })
})
