import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/authService'
import type { UserResponse } from '@/types'
import { useRouter } from 'vue-router'

/**
 * Auth store using Pinia Composition API (Setup Store).
 * Manages JWT token, current user, and auth state.
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<UserResponse | null>(null)
  const loading = ref(false)
  const router = useRouter()

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role?.name === 'ADMIN')
  const isEditor = computed(() => user.value?.role?.name === 'EDITOR')

  async function register(name: string, email: string, password: string, gender?: string, mobile?: string) {
    loading.value = true
    try {
      await authService.register({ name, email, password, gender, mobile })
      router.push('/login')
    } finally {
      loading.value = false
    }
  }

  async function login(email: string, password: string) {
    loading.value = true
    try {
      const response = await authService.login({ email, password })
      token.value = response.token
      localStorage.setItem('token', response.token)
      await fetchCurrentUser()
      router.push('/dashboard')
    } finally {
      loading.value = false
    }
  }

  async function fetchCurrentUser() {
    if (!token.value) return
    try {
      user.value = await authService.getCurrentUser()
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    router.push('/login')
  }

  return {
    token,
    user,
    loading,
    isAuthenticated,
    isAdmin,
    isEditor,
    register,
    login,
    fetchCurrentUser,
    logout,
  }
})
