import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CategoryDto } from '@/types'
import api from '@/services/api'

export const useCategoryStore = defineStore('category', () => {
  const categories = ref<CategoryDto[]>([])
  const loading = ref(false)

  async function fetchCategories(force = false) {
    if (categories.value.length > 0 && !force) {
      return categories.value
    }
    loading.value = true
    try {
      const response = await api.get<CategoryDto[]>('/categories')
      categories.value = response.data
      return categories.value
    } finally {
      loading.value = false
    }
  }

  function getCategoryName(id: number) {
    const found = categories.value.find((c) => c.id === id)
    return found ? `${found.icon ? found.icon + ' ' : ''}${found.name}` : ''
  }

  return {
    categories,
    loading,
    fetchCategories,
    getCategoryName,
  }
})
