import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
  }),
}))

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('initializes with default values', () => {
    const auth = useAuthStore()
    expect(auth.token).toBeNull()
    expect(auth.user).toBeNull()
    expect(auth.loading).toBe(false)
    expect(auth.isAuthenticated).toBe(false)
    expect(auth.isAdmin).toBe(false)
  })

  it('logout resets state and clears localStorage', () => {
    localStorage.setItem('token', 'sample-token')
    const auth = useAuthStore()
    auth.token = 'sample-token'
    auth.user = {
      id: 1,
      name: 'Test',
      email: 'test@example.com',
      gender: null,
      mobile: null,
      role: { id: 1, name: 'ADMIN', description: 'Admin' },
      enabled: true,
      avatarUrl: null,
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
    }

    expect(auth.isAdmin).toBe(true)
    expect(auth.isAuthenticated).toBe(true)

    auth.logout()

    expect(auth.token).toBeNull()
    expect(auth.user).toBeNull()
    expect(auth.isAuthenticated).toBe(false)
    expect(auth.isAdmin).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()
  })
})
