import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright E2E configuration for Skill Management frontend.
 * Assumes backend running on http://localhost:8080 and frontend dev on http://localhost:5173.
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  retries: 0,
  workers: 1,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
})
