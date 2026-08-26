<template>
  <div class="page-shell">
    <AppHeader />
    <main class="page-main">
      <div class="container">
        <router-link to="/courses" class="back-link">← 교보재 목록</router-link>

        <div v-if="loading" class="loading-state surface"><div class="spinner"></div></div>
        <div v-else-if="course" class="detail-grid fade-in-up">
          <section class="detail-main">
            <div class="gear-visual" :class="`visual-${(course.categoryCode || 'etc').toLowerCase().replace('_', '-')}`">
              <span>{{ categoryIcon(course.categoryCode || course.category) }}</span>
              <div class="asset-number">ASSET #{{ String(course.id).padStart(4, '0') }}</div>
            </div>
            <div class="detail-copy surface">
              <span class="badge">{{ course.category }}</span>
              <h1>{{ course.title }}</h1>
              <p>{{ course.description || '조직의 프로젝트와 교육에 사용할 수 있는 보유 장비입니다.' }}</p>
              <div class="spec-grid">
                <div><small>자산가치</small><strong>{{ money(course.price) }}</strong></div>
                <div><small>전체 수량</small><strong>{{ course.totalQuantity }}개</strong></div>
                <div><small>가용 수량</small><strong :class="{ danger: available === 0 }">{{ available }}개</strong></div>
                <div><small>누적 이용</small><strong>{{ course.enrollmentCount }}회</strong></div>
              </div>
            </div>
          </section>

          <aside class="request-panel surface">
            <div class="panel-label">LOAN REQUEST</div>
            <h2>{{ isOperator ? '교보재 운영 정보' : '대여 신청' }}</h2>

            <template v-if="isOperator">
              <p>운영진 계정에서는 교보재를 신청하지 않습니다. 승인 관리에서 교육생 신청을 확인할 수 있습니다.</p>
              <router-link to="/admin/approvals" class="btn btn-primary btn-block">승인 관리로 이동</router-link>
            </template>

            <template v-else>
              <div class="stock-line"><span>현재 상태</span><strong :class="{ out: available === 0 }">{{ available > 0 ? '대여 가능' : '재고 없음' }}</strong></div>

              <div v-if="enrollmentStatus !== 'NONE'" :class="['request-status', statusClass]">
                <strong>{{ statusLabel }}</strong>
                <span>{{ statusDescription }}</span>
              </div>

              <label v-if="enrollmentStatus === 'NONE'" class="field">
                <span>사용 목적</span>
                <textarea v-model.trim="reason" class="form-textarea" placeholder="예: 모바일 앱 테스트 및 팀 프로젝트 시연" maxlength="500"></textarea>
              </label>

              <div v-if="error" class="error-box">{{ error }}</div>
              <button
                class="btn btn-primary btn-block"
                :disabled="submitting || available === 0 || enrollmentStatus !== 'NONE'"
                @click="submitLoan"
              >
                {{ submitting ? '신청 중...' : buttonLabel }}
              </button>
              <p class="helper">운영진 승인 후 대여가 확정되며 실제 결제는 발생하지 않습니다.</p>
            </template>
          </aside>
        </div>

        <div v-else class="empty-state surface"><span class="empty-icon">!</span><strong>교보재 정보를 찾을 수 없습니다.</strong></div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { enrollmentApi } from '@/api/enrollment.js'
import { useAuthStore } from '@/store/auth.js'
import { categoryIcon, useCourseStore } from '@/store/course.js'

const route = useRoute()
const auth = useAuthStore()
const courseStore = useCourseStore()
const reason = ref('')
const error = ref('')
const submitting = ref(false)
const enrollmentStatus = ref('NONE')

const course = computed(() => courseStore.selectedCourse)
const loading = computed(() => courseStore.loading)
const isOperator = computed(() => auth.user?.role === 'INSTRUCTOR')
const available = computed(() => Number(course.value?.availableQuantity || 0))
const buttonLabel = computed(() => available.value > 0 ? '대여 신청하기' : '현재 대여 불가')
const statusLabel = computed(() => ({ PENDING: '승인 대기', ACTIVE: '대여 승인', REJECTED: '신청 반려' }[enrollmentStatus.value] || ''))
const statusDescription = computed(() => ({
  PENDING: '운영진이 신청 내용을 확인하고 있습니다.',
  ACTIVE: '운영진이 대여 신청을 승인했습니다.',
  REJECTED: '내 신청 화면에서 반려 사유를 확인해 주세요.'
}[enrollmentStatus.value] || ''))
const statusClass = computed(() => ({ PENDING: 'pending', ACTIVE: 'active', REJECTED: 'rejected' }[enrollmentStatus.value]))

function money(value) { return `${Number(value || 0).toLocaleString()}원` }

async function loadStatus() {
  if (isOperator.value || !course.value?.id) return
  try {
    const res = await enrollmentApi.getMyEnrollments()
    const items = Array.isArray(res.data?.data) ? res.data.data : []
    const match = items.find(item => Number(item.courseId) === Number(course.value.id))
    enrollmentStatus.value = match?.status || 'NONE'
  } catch {
    enrollmentStatus.value = 'NONE'
  }
}

async function submitLoan() {
  error.value = ''
  if (!reason.value) {
    error.value = '교보재 사용 목적을 입력해 주세요.'
    return
  }
  submitting.value = true
  try {
    await enrollmentApi.enroll(course.value.id, reason.value)
    enrollmentStatus.value = 'PENDING'
  } catch (e) {
    error.value = e.response?.data?.message || '대여 신청에 실패했습니다.'
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await courseStore.fetchCourse(route.params.id)
  await loadStatus()
})
</script>

<style scoped>
.back-link { display: inline-block; margin-bottom: 20px; color: var(--color-text-secondary); font-size: 12px; font-weight: 600; }
.back-link:hover { color: var(--color-primary); }
.detail-grid { display: grid; grid-template-columns: 1fr 350px; gap: 22px; align-items: start; }
.detail-main { display: flex; flex-direction: column; gap: 17px; }
.gear-visual { position: relative; height: 270px; display: grid; place-items: center; color: rgba(16,42,67,.72); background: #e8f5f1; border-radius: var(--radius-xl); overflow: hidden; }
.gear-visual::before, .gear-visual::after { content: ''; position: absolute; border: 1px solid rgba(16,42,67,.08); border-radius: 50%; }
.gear-visual::before { width: 310px; height: 310px; }
.gear-visual::after { width: 190px; height: 190px; }
.gear-visual > span { z-index: 1; font-size: 94px; font-weight: 300; }
.asset-number { position: absolute; left: 20px; bottom: 17px; font-size: 9px; font-weight: 800; letter-spacing: .15em; }
.detail-copy { padding: 28px; }
.detail-copy h1 { margin-top: 12px; color: var(--color-navy); font-size: 29px; letter-spacing: -.04em; }
.detail-copy > p { margin-top: 12px; color: var(--color-text-secondary); font-size: 14px; line-height: 1.8; }
.spec-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-top: 25px; padding-top: 20px; border-top: 1px solid var(--color-border); }
.spec-grid div { display: flex; flex-direction: column; }
.spec-grid small { color: var(--color-text-muted); font-size: 10px; }
.spec-grid strong { margin-top: 3px; font-size: 13px; }
.spec-grid .danger { color: var(--color-danger); }
.request-panel { position: sticky; top: 90px; padding: 25px; }
.panel-label { color: var(--color-primary); font-size: 9px; font-weight: 800; letter-spacing: .15em; }
.request-panel h2 { margin-top: 6px; color: var(--color-navy); font-size: 21px; }
.request-panel > p { margin: 14px 0 20px; color: var(--color-text-secondary); font-size: 12px; line-height: 1.7; }
.stock-line { display: flex; align-items: center; justify-content: space-between; margin: 20px 0; padding: 12px 0; border-top: 1px solid var(--color-border); border-bottom: 1px solid var(--color-border); font-size: 12px; }
.stock-line strong { color: var(--color-success); }
.stock-line .out { color: var(--color-danger); }
.request-panel .field { margin-bottom: 14px; }
.request-panel .field > span { font-size: 12px; font-weight: 700; }
.request-status { display: flex; flex-direction: column; gap: 3px; margin-bottom: 15px; padding: 13px; border-radius: 11px; }
.request-status strong { font-size: 13px; }
.request-status span { font-size: 10px; }
.request-status.pending { color: var(--color-warning); background: var(--color-warning-light); }
.request-status.active { color: var(--color-success); background: var(--color-success-light); }
.request-status.rejected { color: var(--color-danger); background: var(--color-danger-light); }
.helper { margin-top: 12px; color: var(--color-text-muted); font-size: 10px; text-align: center; }
@media (max-width: 850px) {
  .detail-grid { grid-template-columns: 1fr; }
  .request-panel { position: static; }
}
@media (max-width: 560px) { .spec-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
