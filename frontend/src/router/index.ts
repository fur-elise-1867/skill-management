import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/dashboard',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { requiresGuest: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { requiresGuest: true },
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/account',
      name: 'account',
      component: () => import('@/views/AccountInfoView.vue'),
      meta: { requiresAuth: true },
    },
    // Skills Store routes
    {
      path: '/skills',
      name: 'skills',
      component: () => import('@/views/SkillListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/skills/upload',
      name: 'skill-upload',
      component: () => import('@/views/SkillUploadView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/skills/my',
      name: 'my-skills',
      component: () => import('@/views/MySkillsView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/skills/:id',
      name: 'skill-detail',
      component: () => import('@/views/SkillDetailView.vue'),
      meta: { requiresAuth: true },
    },
    // Curator routes
    {
      path: '/curator/pending',
      name: 'curator-pending',
      component: () => import('@/views/PendingApprovalView.vue'),
      meta: { requiresAuth: true, requiresCurator: true },
    },
    {
      path: '/curator/merge',
      name: 'curator-merge',
      component: () => import('@/views/MergeSkillsView.vue'),
      meta: { requiresAuth: true, requiresCurator: true },
    },
    // Admin routes
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/AdminView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/audit',
      name: 'admin-audit',
      component: () => import('@/views/AuditLogView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
  ],
})

// Navigation guard
router.beforeEach(async (to) => {
  const auth = useAuthStore()

  // Try to fetch user if token exists but user is not loaded
  if (auth.isAuthenticated && !auth.user) {
    await auth.fetchCurrentUser()
  }

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login' }
  }

  if (to.meta.requiresGuest && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }

  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return { name: 'dashboard' }
  }

  if (to.meta.requiresCurator && !auth.isCurator) {
    return { name: 'dashboard' }
  }
})

export default router
