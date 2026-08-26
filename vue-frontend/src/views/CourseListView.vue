<template>
  <div class="page-shell">
    <AppHeader />
    <main class="page-main">
      <div class="container">
        <div class="page-heading">
          <div>
            <span class="eyebrow">EQUIPMENT CATALOG</span>
            <h1 class="page-title">보유 교보재</h1>
            <p class="page-subtitle">프로젝트에 필요한 장비의 보유 수량과 이용 현황을 확인하세요.</p>
          </div>
          <div class="heading-actions">
            <router-link v-if="!isOperator" to="/requests/new" class="btn btn-outline">＋ 신규 교보재 신청</router-link>
            <router-link v-else to="/courses/new" class="btn btn-primary">＋ 교보재 등록</router-link>
          </div>
        </div>

        <section class="catalog-toolbar surface">
          <label class="search-box">
            <span>⌕</span>
            <input v-model.trim="keyword" type="search" placeholder="교보재 이름이나 설명 검색" />
          </label>
          <label class="available-toggle">
            <input v-model="availableOnly" type="checkbox" />
            <span>대여 가능한 것만</span>
          </label>
        </section>

        <div class="category-tabs">
          <button
            v-for="category in categories"
            :key="category.value"
            :class="['category-tab', { active: selectedCategory === category.value }]"
            @click="courseStore.setCategory(category.value)"
          >
            <span>{{ category.icon }}</span>{{ category.label }}
          </button>
        </div>

        <div class="result-line">
          <span><strong>{{ filteredCourses.length }}</strong>개의 교보재</span>
          <span v-if="courseStore.error" class="load-error">{{ courseStore.error }}</span>
        </div>

        <div v-if="loading" class="loading-state surface"><div class="spinner"></div><span>교보재를 불러오고 있습니다.</span></div>
        <div v-else-if="filteredCourses.length" class="gear-grid fade-in-up">
          <CourseCard v-for="course in filteredCourses" :key="course.id" :course="course" />
        </div>
        <div v-else class="empty-state surface">
          <span class="empty-icon">⌕</span>
          <strong>조건에 맞는 교보재가 없습니다.</strong>
          <p>필터를 바꾸거나 신규 교보재를 신청해 보세요.</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import CourseCard from '@/components/CourseCard.vue'
import { useAuthStore } from '@/store/auth.js'
import { useCourseStore } from '@/store/course.js'

const auth = useAuthStore()
const courseStore = useCourseStore()
const keyword = ref('')
const availableOnly = ref(false)
const isOperator = computed(() => auth.user?.role === 'INSTRUCTOR')
const categories = computed(() => courseStore.categories)
const loading = computed(() => courseStore.loading)
const selectedCategory = computed(() => courseStore.selectedCategory)

const filteredCourses = computed(() => {
  const needle = keyword.value.toLowerCase()
  return courseStore.courses.filter(course => {
    const categoryMatch = selectedCategory.value === 'ALL' || course.categoryCode === selectedCategory.value
    const keywordMatch = !needle || `${course.title} ${course.description || ''}`.toLowerCase().includes(needle)
    const stockMatch = !availableOnly.value || Number(course.availableQuantity) > 0
    return categoryMatch && keywordMatch && stockMatch
  })
})

onMounted(() => courseStore.fetchCourses())
</script>

<style scoped>
.heading-actions { display: flex; gap: 9px; }
.catalog-toolbar { display: flex; align-items: center; gap: 18px; padding: 14px; }
.search-box { flex: 1; display: flex; align-items: center; gap: 10px; height: 43px; padding: 0 13px; background: var(--color-bg-secondary); border-radius: 10px; }
.search-box span { color: var(--color-primary); font-size: 20px; }
.search-box input { width: 100%; border: 0; outline: 0; background: transparent; color: var(--color-text-primary); font-size: 13px; }
.available-toggle { display: flex; align-items: center; gap: 7px; padding-right: 5px; color: var(--color-text-secondary); font-size: 12px; font-weight: 600; white-space: nowrap; }
.available-toggle input { accent-color: var(--color-primary); }
.category-tabs { display: flex; gap: 7px; padding: 20px 0 14px; overflow-x: auto; }
.category-tab { display: inline-flex; align-items: center; gap: 6px; padding: 8px 12px; color: var(--color-text-secondary); background: #fff; border: 1px solid var(--color-border); border-radius: 999px; font-size: 11px; font-weight: 700; white-space: nowrap; }
.category-tab:hover, .category-tab.active { color: #fff; background: var(--color-primary); border-color: var(--color-primary); }
.result-line { min-height: 34px; display: flex; align-items: center; justify-content: space-between; color: var(--color-text-muted); font-size: 11px; }
.result-line strong { color: var(--color-primary); font-size: 14px; }
.load-error { color: var(--color-danger); }
.gear-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 17px; }
.empty-state p { font-size: 12px; }
@media (max-width: 900px) { .gear-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 620px) {
  .gear-grid { grid-template-columns: 1fr; }
  .catalog-toolbar { align-items: stretch; flex-direction: column; }
  .available-toggle { padding-left: 4px; }
}
</style>
