<template>
  <div class="page-shell">
    <AppHeader />
    <main class="page-main">
      <div class="container request-layout">
        <section>
          <div class="page-heading">
            <div>
              <span class="eyebrow">NEW RESOURCE REQUEST</span>
              <h1 class="page-title">신규 교보재 신청</h1>
              <p class="page-subtitle">구매 링크와 예상 금액을 입력하고, 보유 대체재를 먼저 확인합니다.</p>
            </div>
          </div>

          <div class="stepper">
            <div :class="['step-item', { active: step >= 1, done: step > 1 }]"><span>1</span><strong>상품 정보</strong></div>
            <i></i>
            <div :class="['step-item', { active: step >= 2 }]"><span>2</span><strong>대체재 확인</strong></div>
            <i></i>
            <div :class="['step-item', { active: step >= 3 }]"><span>3</span><strong>신청 완료</strong></div>
          </div>

          <section class="form-surface surface">
            <div class="form-grid">
              <label class="field field-full"><span>신청 상품명</span><input v-model.trim="form.title" class="form-input" placeholder="예: LiDAR 센서 모듈" /></label>
              <label class="field"><span>카테고리</span><select v-model="form.category" class="form-select"><option disabled value="">선택하세요</option><option v-for="item in categories" :key="item.value" :value="item.value">{{ item.label }}</option></select></label>
              <label class="field"><span>수량</span><input v-model.number="form.quantity" type="number" min="1" class="form-input" /></label>
              <label class="field"><span>1개당 가격</span><input v-model.number="form.unitPrice" type="number" min="1" step="1000" class="form-input" placeholder="예: 89000" /></label>
              <label class="field"><span>예상 총액</span><div class="total-field">{{ money(totalAmount) }}</div></label>
              <label class="field field-full"><span>구매 링크</span><input v-model.trim="form.purchaseUrl" type="url" class="form-input" placeholder="https://..." /></label>
              <label class="field field-full"><span>상품 설명 <small class="field-hint">사양이나 모델명을 적어 주세요.</small></span><textarea v-model.trim="form.description" class="form-textarea" placeholder="필요한 사양과 활용 방법"></textarea></label>
              <label class="field field-full"><span>신청 사유</span><textarea v-model.trim="form.reason" class="form-textarea" placeholder="현재 보유 장비로 충족되지 않는 이유와 프로젝트 용도를 적어 주세요."></textarea></label>
            </div>

            <div v-if="error" class="error-box message">{{ error }}</div>
            <div v-if="success" class="success-box message">{{ success }}</div>

            <div v-if="!recommendationChecked" class="recommend-action">
              <div><strong>신청 전에 보유 대체재를 확인합니다.</strong><p>동일 카테고리에서 현재 대여 가능한 교보재를 찾아드립니다.</p></div>
              <button class="btn btn-outline" :disabled="checking" @click="checkAlternatives">{{ checking ? '확인 중...' : '대체재 확인하기' }}</button>
            </div>

            <div v-else class="alternative-section">
              <div class="alternative-head"><div><strong>대체재 확인 결과</strong><p>{{ recommendMessage }}</p></div><button class="reset-btn" @click="resetRecommendation">다시 확인</button></div>
              <div v-if="alternatives.length" class="alternative-list">
                <router-link v-for="item in alternatives" :key="item.id" :to="`/courses/${item.id}`" target="_blank" class="alternative-card">
                  <span class="alt-icon">{{ categoryIcon(item.category) }}</span>
                  <div><strong>{{ item.title }}</strong><small>{{ item.availableQuantity }}개 대여 가능 · 자산가치 {{ money(item.price) }}</small></div>
                  <span>↗</span>
                </router-link>
              </div>
              <label v-if="alternatives.length" class="confirm-check"><input v-model="acceptedAlternatives" type="checkbox" /><span>추천 교보재로는 프로젝트 요구사항을 충족할 수 없어 신규 신청을 계속합니다.</span></label>
            </div>

            <div class="form-actions">
              <router-link to="/courses" class="btn btn-ghost">취소</router-link>
              <button class="btn btn-primary" :disabled="submitting || !canSubmit" @click="submit">{{ submitting ? '신청 중...' : '구매요청 제출' }}</button>
            </div>
          </section>
        </section>

        <aside class="guide-card surface">
          <span class="guide-mark">₩</span>
          <h3>금액은 결제되지 않습니다</h3>
          <p>입력한 금액은 운영진이 예산을 검토하기 위한 예상 총액입니다.</p>
          <ul><li>단가 × 수량 자동 계산</li><li>구매 링크 필수</li><li>대체재 확인 필수</li><li>운영진 승인 후 별도 구매</li></ul>
        </aside>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { enrollmentApi } from '@/api/enrollment.js'
import { categoryIcon, categoryOptions } from '@/store/course.js'

const router = useRouter()
const categories = categoryOptions.filter(item => item.value !== 'ALL')
const form = reactive({ title: '', category: '', quantity: 1, unitPrice: null, purchaseUrl: '', description: '', reason: '' })
const alternatives = ref([])
const recommendationChecked = ref(false)
const acceptedAlternatives = ref(false)
const recommendMessage = ref('')
const checking = ref(false)
const submitting = ref(false)
const error = ref('')
const success = ref('')
const step = computed(() => success.value ? 3 : recommendationChecked.value ? 2 : 1)
const totalAmount = computed(() => Number(form.unitPrice || 0) * Number(form.quantity || 0))
const canSubmit = computed(() => recommendationChecked.value && acceptedAlternatives.value)

watch(() => form.category, resetRecommendation)

function money(value) { return `${Number(value || 0).toLocaleString()}원` }
function validate() {
  if (!form.title || !form.category || !form.purchaseUrl || !form.reason) return '상품명, 카테고리, 구매 링크, 신청 사유를 모두 입력해 주세요.'
  if (Number(form.quantity) < 1 || Number(form.unitPrice) <= 0) return '수량과 단가를 올바르게 입력해 주세요.'
  if (!/^https?:\/\//i.test(form.purchaseUrl)) return '구매 링크는 http:// 또는 https://로 시작해야 합니다.'
  return ''
}
function resetRecommendation() {
  recommendationChecked.value = false
  acceptedAlternatives.value = false
  alternatives.value = []
  recommendMessage.value = ''
}
async function checkAlternatives() {
  error.value = validate()
  if (error.value) return
  checking.value = true
  try {
    const res = await enrollmentApi.getAlternatives(form.category)
    const payload = res.data?.data ?? res.data
    alternatives.value = Array.isArray(payload?.alternatives) ? payload.alternatives : []
    recommendMessage.value = payload?.message || '대체재 확인을 완료했습니다.'
    recommendationChecked.value = true
    acceptedAlternatives.value = alternatives.value.length === 0
  } catch (e) {
    error.value = e.response?.data?.detail || '대체 교보재를 확인하지 못했습니다. 다시 시도해 주세요.'
  } finally {
    checking.value = false
  }
}
async function submit() {
  error.value = validate()
  if (error.value || !canSubmit.value) return
  submitting.value = true
  try {
    await enrollmentApi.requestPurchase({ ...form, unitPrice: Number(form.unitPrice), quantity: Number(form.quantity), alternativeChecked: true })
    success.value = '신규 교보재 구매요청이 접수되었습니다.'
    setTimeout(() => router.push('/enrollments'), 650)
  } catch (e) {
    error.value = e.response?.data?.message || '구매요청 제출에 실패했습니다.'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.request-layout { display: grid; grid-template-columns: minmax(0, 1fr) 270px; gap: 22px; align-items: start; }
.stepper { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; }
.stepper > i { width: 42px; height: 1px; background: var(--color-border); }
.step-item { display: flex; align-items: center; gap: 7px; color: var(--color-text-muted); }
.step-item span { width: 25px; height: 25px; display: grid; place-items: center; border: 1px solid var(--color-border); border-radius: 50%; font-size: 10px; }
.step-item strong { font-size: 10px; }
.step-item.active { color: var(--color-primary); }
.step-item.active span { color: #fff; background: var(--color-primary); border-color: var(--color-primary); }
.form-surface { padding: 28px; }
.total-field { height: 46px; display: flex; align-items: center; padding: 0 13px; color: var(--color-primary); background: var(--color-primary-light); border-radius: var(--radius-md); font-weight: 800; }
.message { margin-top: 18px; }
.recommend-action { display: flex; align-items: center; justify-content: space-between; gap: 15px; margin-top: 23px; padding: 17px; background: #f6f9f7; border: 1px dashed #a9c8bd; border-radius: 13px; }
.recommend-action strong, .alternative-head strong { font-size: 13px; }
.recommend-action p, .alternative-head p { margin-top: 3px; color: var(--color-text-muted); font-size: 10px; }
.alternative-section { margin-top: 23px; padding: 17px; background: var(--color-primary-light); border-radius: 13px; }
.alternative-head { display: flex; justify-content: space-between; gap: 10px; }
.reset-btn { color: var(--color-primary); background: none; border: 0; font-size: 10px; font-weight: 700; }
.alternative-list { display: flex; flex-direction: column; gap: 7px; margin-top: 13px; }
.alternative-card { display: grid; grid-template-columns: 35px 1fr auto; align-items: center; gap: 10px; padding: 10px; background: #fff; border-radius: 10px; }
.alt-icon { width: 34px; height: 34px; display: grid; place-items: center; color: var(--color-primary); background: #edf5f2; border-radius: 9px; }
.alternative-card div { display: flex; flex-direction: column; }
.alternative-card strong { font-size: 11px; }
.alternative-card small { color: var(--color-text-muted); font-size: 9px; }
.confirm-check { display: flex; align-items: flex-start; gap: 8px; margin-top: 13px; color: #31584d; font-size: 10px; }
.confirm-check input { margin-top: 2px; accent-color: var(--color-primary); }
.guide-card { position: sticky; top: 91px; margin-top: 117px; padding: 23px; }
.guide-mark { width: 42px; height: 42px; display: grid; place-items: center; color: var(--color-warning); background: var(--color-warning-light); border-radius: 12px; font-size: 18px; font-weight: 800; }
.guide-card h3 { margin-top: 16px; color: var(--color-navy); font-size: 15px; }
.guide-card p { margin-top: 8px; color: var(--color-text-secondary); font-size: 11px; line-height: 1.7; }
.guide-card ul { margin-top: 17px; padding-top: 15px; border-top: 1px solid var(--color-border); list-style: none; }
.guide-card li { padding: 5px 0; color: var(--color-text-secondary); font-size: 10px; }
.guide-card li::before { content: '✓'; margin-right: 7px; color: var(--color-primary); font-weight: 800; }
@media (max-width: 880px) { .request-layout { grid-template-columns: 1fr; } .guide-card { position: static; margin-top: 0; } }
@media (max-width: 560px) { .form-surface { padding: 19px; } .step-item strong { display: none; } .recommend-action { align-items: stretch; flex-direction: column; } }
</style>
