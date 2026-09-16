<template>
  <div class="app-layout">
    <AppNavbar v-if="showNavbar" />
    <main class="main-content" :class="{ 'with-navbar': showNavbar }">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppNavbar from '@/components/layout/AppNavbar.vue'

const auth = useAuthStore()
const route = useRoute()

const showNavbar = computed(() => {
  return auth.isAuthenticated && !['login', 'register'].includes(route.name as string)
})
</script>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    sans-serif;
  background-color: #f8fafc;
  color: #1e293b;
}

.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}

.main-content.with-navbar {
  padding-bottom: 40px;
}
</style>
