<template>
  <div class="page-shell">
    <AppHeader />
    <main class="page-main">
      <div class="container profile-layout">
        <aside class="profile-card surface">
          <div class="large-avatar">{{ auth.user?.name?.charAt(0) || '?' }}</div>
          <span class="role-badge">{{ isOperator ? 'SKALA 자산 운영자' : 'SKALA 구성원' }}</span>
          <h2>{{ auth.user?.name || '사용자' }}</h2>
          <p>{{ auth.user?.email }}</p>
          <div class="profile-divider"></div>
          <small>GearHub에서는 실제 결제가 발생하지 않습니다. 모든 금액은 교보재 자산가치와 예산 검토용입니다.</small>
        </aside>

        <section>
          <div class="page-heading compact-heading">
            <div><span class="eyebrow">MY GEARHUB</span><h1 class="page-title">{{ isOperator ? '운영 현황' : '나의 GearHub' }}</h1><p class="page-subtitle">{{ greeting }}</p></div>
          </div>

          <div v-if="loading" class="loading-state surface"><div class="spinner"></div></div>
          <template v-else>
            <div class="summary-grid">
              <div v-for="item in summaries" :key="item.label" class="summary-card surface"><span class="summary-label">{{ item.label }}</span><strong class="summary-value">{{ item.value }}</strong></div>
            </div>

            <div class="quick-grid">
              <router-link v-for="action in actions" :key="action.title" :to="action.to" class="quick-card surface">
                <span>{{ action.icon }}</span><div><strong>{{ action.title }}</strong><p>{{ action.desc }}</p></div><i>→</i>
              </router-link>
            </div>

            <section class="principle surface">
              <span>AGILE PRACTICE</span>
              <div><h3>기존 MSA 틀은 그대로, 사용자 가치만 빠르게 전환</h3><p>Course는 교보재, Enrollment는 신청, Payment는 예산 승인으로 기존 구조를 재사용합니다.</p></div>
            </section>
          </template>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import { enrollmentApi } from '@/api/enrollment.js'
import { paymentApi } from '@/api/payment.js'
import { useAuthStore } from '@/store/auth.js'

const auth = useAuthStore()
const loading = ref(true)
const counts = ref({ total: 0, pending: 0, approved: 0 })
const isOperator = computed(() => auth.user?.role === 'INSTRUCTOR')
const greeting = computed(() => isOperator.value ? '조직의 대기 요청과 예산 검토를 한곳에서 관리하세요.' : '신청 현황과 필요한 조직 장비를 빠르게 확인하세요.')
const summaries = computed(() => isOperator.value
  ? [
      { label: '대여 승인 대기', value: counts.value.pending },
      { label: '예산 검토 대기', value: counts.value.total },
      { label: '검토 대기 전체', value: counts.value.pending + counts.value.total }
    ]
  : [
      { label: '전체 신청', value: counts.value.total },
      { label: '승인 대기', value: counts.value.pending },
      { label: '승인 완료', value: counts.value.approved }
    ])
const actions = computed(() => isOperator.value
  ? [
      { icon: '✓', title: '승인 관리', desc: '대여와 예산 신청을 검토합니다.', to: '/admin/approvals' },
      { icon: '＋', title: '교보재 등록', desc: '보유 장비와 수량을 추가합니다.', to: '/courses/new' },
      { icon: '▦', title: '전체 교보재', desc: '현재 카탈로그를 확인합니다.', to: '/courses' }
    ]
  : [
      { icon: '⌕', title: '교보재 찾기', desc: '대여 가능한 장비를 확인합니다.', to: '/courses' },
      { icon: '＋', title: '신규 교보재 신청', desc: '대체재 확인 후 구매를 제안합니다.', to: '/requests/new' },
      { icon: '□', title: '내 신청', desc: '승인 상태를 확인합니다.', to: '/enrollments' }
    ])

onMounted(async () => {
  try {
    if (isOperator.value) {
      const [loans, budgets] = await Promise.all([enrollmentApi.getPending('LOAN'), paymentApi.getPending()])
      counts.value.pending = Array.isArray(loans.data?.data) ? loans.data.data.length : 0
      counts.value.total = Array.isArray(budgets.data?.data) ? budgets.data.data.length : 0
    } else {
      const res = await enrollmentApi.getMyEnrollments()
      const items = Array.isArray(res.data?.data) ? res.data.data : []
      counts.value = {
        total: items.length,
        pending: items.filter(item => item.status === 'PENDING').length,
        approved: items.filter(item => item.status === 'ACTIVE').length
      }
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.profile-layout { display: grid; grid-template-columns: 270px 1fr; gap: 24px; align-items: start; }
.profile-card { position: sticky; top: 90px; padding: 27px; text-align: center; }
.large-avatar { width: 70px; height: 70px; margin: 0 auto 13px; display: grid; place-items: center; color: #fff; background: var(--color-navy); border-radius: 22px 22px 8px 22px; font-size: 25px; font-weight: 800; }
.role-badge { display: inline-block; padding: 4px 8px; color: var(--color-primary); background: var(--color-primary-light); border-radius: 999px; font-size: 9px; font-weight: 800; }
.profile-card h2 { margin-top: 10px; color: var(--color-navy); font-size: 19px; }
.profile-card > p { margin-top: 3px; color: var(--color-text-muted); font-size: 10px; word-break: break-all; }
.profile-divider { margin: 21px 0; border-top: 1px solid var(--color-border); }
.profile-card small { color: var(--color-text-muted); font-size: 9px; line-height: 1.7; }
.compact-heading { margin-bottom: 22px; }
.quick-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 13px; }
.quick-card { display: grid; grid-template-columns: 40px 1fr auto; align-items: center; gap: 11px; padding: 17px; transition: var(--transition); }
.quick-card:hover { transform: translateY(-2px); border-color: var(--color-border-hover); box-shadow: var(--shadow-md); }
.quick-card > span { width: 39px; height: 39px; display: grid; place-items: center; color: var(--color-primary); background: var(--color-primary-light); border-radius: 11px; font-size: 17px; }
.quick-card div { display: flex; flex-direction: column; }
.quick-card strong { font-size: 12px; }
.quick-card p { margin-top: 2px; color: var(--color-text-muted); font-size: 9px; line-height: 1.5; }
.quick-card i { color: var(--color-text-muted); font-style: normal; }
.principle { display: grid; grid-template-columns: 120px 1fr; gap: 20px; margin-top: 18px; padding: 22px; background: linear-gradient(120deg, #102a43, #14493f); color: #fff; }
.principle > span { color: #72dbc0; font-size: 9px; font-weight: 800; letter-spacing: .1em; }
.principle h3 { font-size: 14px; }
.principle p { margin-top: 5px; color: #b9c8d0; font-size: 10px; }
@media (max-width: 850px) { .profile-layout { grid-template-columns: 1fr; } .profile-card { position: static; } }
@media (max-width: 650px) { .quick-grid { grid-template-columns: 1fr; } .principle { grid-template-columns: 1fr; } }
</style>
