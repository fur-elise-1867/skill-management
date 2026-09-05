import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import api from '../api'

describe('Axios Client Interceptors (api.ts)', () => {
  // Extract registered interceptor handlers
  const requestInterceptor = (api.interceptors.request as any).handlers[0].fulfilled
  const responseSuccessInterceptor = (api.interceptors.response as any).handlers[0].fulfilled
  const responseErrorInterceptor = (api.interceptors.response as any).handlers[0].rejected

  const originalLocation = window.location

  beforeEach(() => {
    localStorage.clear()
    // Mock window.location to capture redirect assignment
    delete (window as any).location
    ;(window as any).location = { href: '' }
  })

  afterEach(() => {
    (window as any).location = originalLocation
  })

  describe('Request Interceptor', () => {
    it('TC-FE-AXI-001: injects Authorization Bearer token when token is present in localStorage', () => {
      localStorage.setItem('token', 'test-jwt-token')
      const config = { headers: {} as Record<string, string> }

      const modifiedConfig = requestInterceptor(config)

      expect(modifiedConfig.headers.Authorization).toBe('Bearer test-jwt-token')
    })

    it('TC-FE-AXI-002: does not attach Authorization header when localStorage is empty', () => {
      const config = { headers: {} as Record<string, string> }

      const modifiedConfig = requestInterceptor(config)

      expect(modifiedConfig.headers.Authorization).toBeUndefined()
    })
  })

  describe('Response Interceptor', () => {
    it('passes successful responses through unchanged', () => {
      const mockResponse = { status: 200, data: { success: true } }
      const result = responseSuccessInterceptor(mockResponse)
      expect(result).toBe(mockResponse)
    })

    it('TC-FE-AXI-003: on 401 error, removes token from localStorage and redirects to /login', async () => {
      localStorage.setItem('token', 'expired-jwt-token')
      const error401 = { response: { status: 401, data: { message: 'Token expired' } } }

      await expect(responseErrorInterceptor(error401)).rejects.toEqual(error401)

      expect(localStorage.getItem('token')).toBeNull()
      expect(window.location.href).toBe('/login')
    })

    it('TC-FE-AXI-004: on non-401 errors (403, 500), retains token in localStorage and does not redirect', async () => {
      localStorage.setItem('token', 'valid-jwt-token')
      const error403 = { response: { status: 403, data: { message: 'Forbidden' } } }

      await expect(responseErrorInterceptor(error403)).rejects.toEqual(error403)

      expect(localStorage.getItem('token')).toBe('valid-jwt-token')
      expect(window.location.href).toBe('')

      const error500 = { response: { status: 500, data: { message: 'Internal Server Error' } } }
      await expect(responseErrorInterceptor(error500)).rejects.toEqual(error500)

      expect(localStorage.getItem('token')).toBe('valid-jwt-token')
      expect(window.location.href).toBe('')
    })
  })
})
