import { test, expect } from '@playwright/test'

/**
 * Playwright E2E test suite for Authentication & RBAC flows:
 * - TC-E2E-001: Register -> Dashboard -> USER badge
 * - TC-E2E-002: Regular USER is blocked from /admin -> Redirected to /dashboard
 * - TC-E2E-003: Login -> Dashboard -> Logout -> Session cleared
 */
test.describe('Auth and User Management E2E Flow', () => {
  const timestamp = Date.now()
  const testUser = {
    name: `E2E User ${timestamp}`,
    email: `e2e_user_${timestamp}@example.com`,
    password: 'Password123!',
    mobile: '0912345678',
  }

  test('TC-E2E-001: Register new user, automatically redirect to dashboard and show USER role badge', async ({ page }) => {
    await page.goto('/register')

    // Fill registration form
    await page.getByPlaceholder('Nguyễn Văn A').fill(testUser.name)
    await page.getByPlaceholder('email@example.com').fill(testUser.email)
    await page.getByPlaceholder('Tối thiểu 8 ký tự').fill(testUser.password)
    await page.getByPlaceholder('0123456789').fill(testUser.mobile)

    // Submit form
    await page.getByRole('button', { name: 'Đăng ký' }).click()

    // Assert redirection to dashboard
    await expect(page).toHaveURL(/\/dashboard/)
    await expect(page.locator('h1')).toHaveText('Dashboard')
    await expect(page.locator('.header-left p')).toContainText(testUser.name)
    await expect(page.locator('.header-right')).toContainText('USER')

    // Assert user details section
    await expect(page.locator('.profile-section')).toBeVisible()
    await expect(page.locator('.profile-section')).toContainText(testUser.email)
  })

  test('TC-E2E-002: Regular user (ROLE_USER) is blocked when directly accessing /admin and redirected to /dashboard', async ({ page }) => {
    // Login as the regular user
    await page.goto('/login')
    await page.getByPlaceholder('email@example.com').fill(testUser.email)
    await page.getByPlaceholder('Nhập mật khẩu').fill(testUser.password)
    await page.getByRole('button', { name: 'Đăng nhập' }).click()

    await expect(page).toHaveURL(/\/dashboard/)

    // Try accessing /admin directly via URL
    await page.goto('/admin')

    // Expect router guard to intercept and redirect back to /dashboard
    await expect(page).toHaveURL(/\/dashboard/)
    await expect(page.locator('h1')).toHaveText('Dashboard')
    await expect(page.locator('h1')).not.toHaveText('Quản trị người dùng')
  })

  test('TC-E2E-003: Login -> Dashboard -> Logout flow clears session and redirects to /login', async ({ page }) => {
    await page.goto('/login')
    await page.getByPlaceholder('email@example.com').fill(testUser.email)
    await page.getByPlaceholder('Nhập mật khẩu').fill(testUser.password)
    await page.getByRole('button', { name: 'Đăng nhập' }).click()

    await expect(page).toHaveURL(/\/dashboard/)

    // Click logout
    await page.getByRole('button', { name: 'Đăng xuất' }).click()

    // Expect redirection to /login
    await expect(page).toHaveURL(/\/login/)
    await expect(page.locator('.login-card h1')).toHaveText('Skill Management')

    // Try navigating back to /dashboard, should be redirected back to /login
    await page.goto('/dashboard')
    await expect(page).toHaveURL(/\/login/)
  })
})
