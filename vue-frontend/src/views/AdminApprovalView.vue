<template>
  <div class="page-shell">
    <AppHeader />
    <main class="page-main">
      <div class="container">
        <div class="page-heading">
          <div><span class="eyebrow">ASSET OPERATIONS DESK</span><h1 class="page-title">승인 관리</h1><p class="page-subtitle">조직 구성원의 대여 신청과 신규 장비 예산을 검토합니다.</p></div>
          <button class="btn btn-ghost" :disabled="loading" @click="loadAll">↻ 새로고침</button>
        </div>

        <div class="summary-grid">
          <div class="summary-card surface"><span class="summary-label">대여 승인 대기</span><strong class="summary-value">{{ loans.length }}</strong></div>
          <div class="summary-card surface"><span class="summary-label">예산 검토 대기</span><strong class="summary-value budget">{{ budgets.length }}</strong></div>
          <div class="summary-card surface"><span class="summary-label">현재 처리 대상 금액</span><strong class="summary-value money-value">{{ money(totalBudget) }}</strong></div>
        </div>

        <div v-if="message" class="success-box action-message">{{ message }}</div>
        <div v-if="error" class="error-box action-message">{{ error }}</div>

        <div class="tabs">
          <button :class="{ active: tab === 'loan' }" @click="tab = 'loan'">대여 신청 <span>{{ loans.length }}</span></button>
          <button :class="{ active: tab === 'budget' }" @click="tab = 'budget'">예산 검토 <span>{{ budgets.length }}</span></button>
        </div>

        <div v-if="loading" class="loading-state surface"><div class="spinner"></div></div>
        <div v-else-if="tab === 'loan' && loans.length" class="approval-list">
          <article v-for="item in loans" :key="item.id" class="approval-card surface">
            <div class="approval-main">
              <span class="request-type">LOAN · REQ-{{ String(item.id).padStart(4, '0') }}</span>
              <h3>{{ item.course?.title }}</h3>
              <p class="purpose">“{{ item.reason }}”</p>
              <div class="meta"><span>구성원 #{{ item.userId }}</span><span>{{ item.course?.category }}</span><span>가용 {{ item.course?.availableQuantity }}개</span><span>{{ date(item.createdAt) }}</span></div>
            </div>
            <div class="approval-actions">
              <input v-model.trim="reasons[item.id]" class="reason-input" placeholder="반려 시 사유 입력" />
              <div><button class="btn btn-danger btn-sm" :disabled="busyId === item.id" @click="rejectLoan(item)">반려</button><button class="btn btn-primary btn-sm" :disabled="busyId === item.id" @click="approveLoan(item)">대여 승인</button></div>
            </div>
          </article>
        </div>

        <div v-else-if="tab === 'budget' && budgets.length" class="approval-list">
          <article v-for="item in budgets" :key="item.paymentId" class="approval-card surface budget-card">
            <div class="approval-main">
              <span class="request-type">BUDGET · PAY-{{ String(item.paymentId).padStart(4, '0') }}</span>
              <h3>{{ item.course?.title || `신규 교보재 #${item.courseId}` }}</h3>
              <p class="purpose">{{ item.course?.description }}</p>
              <div class="meta"><span>구성원 #{{ item.userId }}</span><span>{{ categoryName(item.course?.category) }}</span><span>{{ item.course?.totalQuantity || 1 }}개</span><a v-if="item.course?.purchaseUrl" :href="item.course.purchaseUrl" target="_blank" rel="noopener">구매 링크 ↗</a></div>
            </div>
            <div class="budget-amount"><small>검토 총액</small><strong>{{ money(item.amount) }}</strong><span>{{ date(item.createdAt) }}</span></div>
            <div class="approval-actions compact"><button class="btn btn-danger btn-sm" :disabled="busyId === item.paymentId" @click="rejectBudget(item)">예산 반려</button><button class="btn btn-primary btn-sm" :disabled="busyId === item.paymentId" @click="approveBudget(item)">예산 승인</button></div>
          </article>
        </div>

        <div v-else class="empty-state surface"><span class="empty-icon">✓</span><strong>현재 검토할 신청이 없습니다.</strong><p>새 신청이 들어오면 이 화면에 표시됩니다.</p></div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import { courseApi } from '@/api/course.js'
import { enrollmentApi } from '@/api/enrollment.js'
import { paymentApi } from '@/api/payment.js'
import { categoryLabel } from '@/store/course.js'

const tab = ref('loan')
const loans = ref([])
const budgets = ref([])
const loading = ref(true)
const busyId = ref(null)
const reasons = reactive({})
const message = ref('')
const error = ref('')
const totalBudget = computed(() => budgets.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))

function money(value) { return `${Number(value || 0).toLocaleString()}원` }
function date(value) { return value ? new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(new Date(value)) : '-' }
function categoryName(value) { return categoryLabel(value) }
function resetMessage() { message.value = ''; error.value = '' }

async function loadAll() {
  loading.value = true
  resetMessage()
  try {
    const [loanRes, paymentRes] = await Promise.all([
      enrollmentApi.getPending('LOAN'),
      paymentApi.getPending()
    ])
    loans.value = Array.isArray(loanRes.data?.data) ? loanRes.data.data : []
    const payments = Array.isArray(paymentRes.data?.data) ? paymentRes.data.data : []
    budgets.value = await Promise.all(payments.map(async payment => {
      try {
        const res = await courseApi.getById(payment.courseId)
        return { ...payment, course: res.data?.data ?? res.data }
      } catch {
        return payment
      }
    }))
  } catch (e) {
    error.value = e.response?.data?.message || '승인 대기 목록을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

async function approveLoan(item) {
  resetMessage(); busyId.value = item.id
  try {
    await enrollmentApi.approve(item.id)
    loans.value = loans.value.filter(row => row.id !== item.id)
    message.value = `${item.course?.title} 대여 신청을 승인했습니다.`
  } catch (e) { error.value = e.response?.data?.message || '대여 승인에 실패했습니다.' }
  finally { busyId.value = null }
}
async function rejectLoan(item) {
  resetMessage()
  if (!reasons[item.id]) { error.value = '반려 사유를 입력해 주세요.'; return }
  busyId.value = item.id
  try {
    await enrollmentApi.reject(item.id, reasons[item.id])
    loans.value = loans.value.filter(row => row.id !== item.id)
    message.value = `${item.course?.title} 대여 신청을 반려했습니다.`
  } catch (e) { error.value = e.response?.data?.message || '대여 반려에 실패했습니다.' }
  finally { busyId.value = null }
}
async function approveBudget(item) {
  resetMessage(); busyId.value = item.paymentId
  try {
    await paymentApi.approve(item.paymentId)
    budgets.value = budgets.value.filter(row => row.paymentId !== item.paymentId)
    message.value = `${item.course?.title || '신규 교보재'} 예산을 승인했습니다.`
  } catch (e) { error.value = e.response?.data?.message || '예산 승인에 실패했습니다.' }
  finally { busyId.value = null }
}
async function rejectBudget(item) {
  resetMessage(); busyId.value = item.paymentId
  try {
    await paymentApi.reject(item.paymentId)
    budgets.value = budgets.value.filter(row => row.paymentId !== item.paymentId)
    message.value = `${item.course?.title || '신규 교보재'} 예산을 반려했습니다.`
  } catch (e) { error.value = e.response?.data?.message || '예산 반려에 실패했습니다.' }
  finally { busyId.value = null }
}

onMounted(loadAll)
</script>

<style scoped>
.summary-value.budget { color: var(--color-warning); }
.money-value { font-size: 19px; }
.action-message { margin-bottom: 15px; }
.tabs { display: flex; gap: 8px; margin-bottom: 14px; }
.tabs button { display: flex; align-items: center; gap: 7px; padding: 9px 14px; color: var(--color-text-secondary); background: #fff; border: 1px solid var(--color-border); border-radius: 10px; font-size: 11px; font-weight: 700; }
.tabs button.active { color: #fff; background: var(--color-primary); border-color: var(--color-primary); }
.tabs span { opacity: .75; }
.approval-list { display: flex; flex-direction: column; gap: 11px; }
.approval-card { display: grid; grid-template-columns: minmax(0, 1fr) 270px; align-items: center; gap: 20px; padding: 20px; }
.budget-card { grid-template-columns: minmax(0, 1fr) 140px 190px; }
.request-type { color: var(--color-primary); font-size: 9px; font-weight: 800; letter-spacing: .09em; }
.approval-main h3 { margin-top: 5px; color: var(--color-navy); font-size: 15px; }
.purpose { margin-top: 5px; color: var(--color-text-secondary); font-size: 11px; }
.meta { display: flex; flex-wrap: wrap; gap: 13px; margin-top: 9px; color: var(--color-text-muted); font-size: 9px; }
.meta a { color: var(--color-primary); font-weight: 700; }
.approval-actions { display: flex; flex-direction: column; gap: 8px; }
.approval-actions > div, .approval-actions.compact { display: flex; justify-content: flex-end; gap: 7px; }
.reason-input { width: 100%; height: 36px; padding: 0 10px; border: 1px solid var(--color-border); border-radius: 9px; outline: none; font-size: 10px; }
.reason-input:focus { border-color: var(--color-primary); }
.budget-amount { display: flex; flex-direction: column; text-align: right; }
.budget-amount small, .budget-amount span { color: var(--color-text-muted); font-size: 9px; }
.budget-amount strong { margin: 3px 0; color: var(--color-navy); font-size: 17px; }
@media (max-width: 800px) {
  .approval-card, .budget-card { grid-template-columns: 1fr; }
  .approval-actions > div, .approval-actions.compact { justify-content: flex-start; }
  .budget-amount { text-align: left; }
}
</style>
