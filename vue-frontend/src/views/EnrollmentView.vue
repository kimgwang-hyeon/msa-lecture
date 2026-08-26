<template>
  <div class="page-shell">
    <AppHeader />
    <main class="page-main">
      <div class="container">
        <div class="page-heading">
          <div><span class="eyebrow">MY REQUESTS</span><h1 class="page-title">내 신청</h1><p class="page-subtitle">대여와 신규 교보재 구매요청의 처리 상태를 확인합니다.</p></div>
          <router-link to="/requests/new" class="btn btn-outline">＋ 신규 교보재 신청</router-link>
        </div>

        <div class="summary-grid">
          <div class="summary-card surface"><span class="summary-label">전체 신청</span><strong class="summary-value">{{ enrollments.length }}</strong></div>
          <div class="summary-card surface"><span class="summary-label">승인 대기</span><strong class="summary-value pending-value">{{ pendingCount }}</strong></div>
          <div class="summary-card surface"><span class="summary-label">승인 완료</span><strong class="summary-value approved-value">{{ approvedCount }}</strong></div>
        </div>

        <div class="tabs">
          <button :class="{ active: tab === 'LOAN' }" @click="tab = 'LOAN'">대여 신청 <span>{{ countByType('LOAN') }}</span></button>
          <button :class="{ active: tab === 'PURCHASE' }" @click="tab = 'PURCHASE'">구매요청 <span>{{ countByType('PURCHASE') }}</span></button>
        </div>

        <div v-if="loading" class="loading-state surface"><div class="spinner"></div></div>
        <div v-else-if="filtered.length" class="request-list fade-in-up">
          <article v-for="item in filtered" :key="item.id" class="request-card surface">
            <div class="request-icon">{{ categoryIcon(item.course?.category) }}</div>
            <div class="request-info">
              <div class="request-tags"><span class="badge">{{ item.course?.category || '기타' }}</span><span class="request-number">REQ-{{ String(item.id).padStart(4, '0') }}</span></div>
              <h3>{{ item.course?.title || '교보재 정보 없음' }}</h3>
              <p>{{ item.reason }}</p>
              <div class="request-meta">
                <span>신청일 {{ date(item.createdAt) }}</span>
                <span v-if="item.requestType === 'PURCHASE'">{{ item.course?.totalQuantity }}개 · 총 {{ money(total(item)) }}</span>
                <span v-else>자산가치 {{ money(item.course?.price) }}</span>
              </div>
              <div v-if="item.status === 'REJECTED' && item.reviewComment" class="reject-reason">반려 사유: {{ item.reviewComment }}</div>
            </div>
            <div class="request-side">
              <span :class="['status-badge', statusClass(item.status)]">{{ statusLabel(item) }}</span>
              <router-link v-if="item.requestType === 'LOAN'" :to="`/courses/${item.courseId}`" class="btn btn-ghost btn-sm">상세 보기</router-link>
              <a v-else-if="item.course?.purchaseUrl" :href="item.course.purchaseUrl" target="_blank" rel="noopener" class="btn btn-ghost btn-sm">상품 링크 ↗</a>
            </div>
          </article>
        </div>
        <div v-else class="empty-state surface"><span class="empty-icon">□</span><strong>{{ tab === 'LOAN' ? '대여 신청이 없습니다.' : '구매요청이 없습니다.' }}</strong><router-link :to="tab === 'LOAN' ? '/courses' : '/requests/new'" class="btn btn-primary btn-sm">신청하러 가기</router-link></div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import { enrollmentApi } from '@/api/enrollment.js'
import { categoryIcon } from '@/store/course.js'

const enrollments = ref([])
const loading = ref(true)
const tab = ref('LOAN')
const filtered = computed(() => enrollments.value.filter(item => item.requestType === tab.value))
const pendingCount = computed(() => enrollments.value.filter(item => item.status === 'PENDING').length)
const approvedCount = computed(() => enrollments.value.filter(item => item.status === 'ACTIVE').length)
function countByType(type) { return enrollments.value.filter(item => item.requestType === type).length }
function money(value) { return `${Number(value || 0).toLocaleString()}원` }
function total(item) { return Number(item.course?.price || 0) * Number(item.course?.totalQuantity || 1) }
function date(value) { return value ? new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(new Date(value)) : '-' }
function statusClass(status) { return ({ PENDING: 'status-pending', ACTIVE: 'status-active', REJECTED: 'status-rejected' }[status] || 'status-muted') }
function statusLabel(item) {
  if (item.status === 'PENDING') return '승인 대기'
  if (item.status === 'REJECTED') return '반려'
  return item.requestType === 'LOAN' ? '대여 승인' : '예산 승인'
}
onMounted(async () => {
  try {
    const res = await enrollmentApi.getMyEnrollments()
    enrollments.value = Array.isArray(res.data?.data) ? res.data.data : []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.pending-value { color: var(--color-warning); }
.approved-value { color: var(--color-success); }
.tabs { display: flex; gap: 5px; margin-bottom: 14px; border-bottom: 1px solid var(--color-border); }
.tabs button { display: flex; align-items: center; gap: 7px; padding: 11px 15px; color: var(--color-text-muted); background: none; border: 0; border-bottom: 2px solid transparent; font-size: 12px; font-weight: 700; }
.tabs button.active { color: var(--color-primary); border-bottom-color: var(--color-primary); }
.tabs span { min-width: 20px; padding: 2px 5px; background: var(--color-bg-tertiary); border-radius: 999px; font-size: 9px; }
.request-list { display: flex; flex-direction: column; gap: 11px; }
.request-card { display: grid; grid-template-columns: 56px 1fr auto; align-items: center; gap: 17px; padding: 18px; }
.request-icon { width: 55px; height: 55px; display: grid; place-items: center; color: var(--color-primary); background: var(--color-primary-light); border-radius: 14px; font-size: 24px; }
.request-tags { display: flex; align-items: center; gap: 8px; }
.request-number { color: var(--color-text-muted); font-size: 9px; font-weight: 700; }
.request-info h3 { margin-top: 7px; color: var(--color-navy); font-size: 15px; }
.request-info > p { margin-top: 3px; color: var(--color-text-secondary); font-size: 11px; }
.request-meta { display: flex; flex-wrap: wrap; gap: 14px; margin-top: 8px; color: var(--color-text-muted); font-size: 9px; }
.reject-reason { margin-top: 8px; padding: 7px 9px; color: var(--color-danger); background: var(--color-danger-light); border-radius: 8px; font-size: 10px; }
.request-side { min-width: 104px; display: flex; align-items: flex-end; flex-direction: column; gap: 10px; }
@media (max-width: 620px) {
  .request-card { grid-template-columns: 46px 1fr; align-items: start; }
  .request-icon { width: 44px; height: 44px; }
  .request-side { grid-column: 2; align-items: flex-start; flex-direction: row; }
}
</style>
