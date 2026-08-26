<template>
  <div class="page-shell">
    <AppHeader />
    <main class="page-main">
      <div class="container narrow">
        <div class="page-heading">
          <div>
            <span class="eyebrow">INVENTORY MANAGEMENT</span>
            <h1 class="page-title">교보재 등록</h1>
            <p class="page-subtitle">운영진이 보유 중인 장비와 수량을 카탈로그에 추가합니다.</p>
          </div>
        </div>

        <section class="form-surface surface">
          <form @submit.prevent="submit">
            <div class="form-grid">
              <label class="field field-full">
                <span>교보재명</span>
                <input v-model.trim="form.title" class="form-input" placeholder="예: iPhone 15 Pro 테스트 기기" maxlength="100" />
              </label>
              <label class="field field-full">
                <span>교보재 설명</span>
                <textarea v-model.trim="form.description" class="form-textarea" placeholder="사양, 사용 범위, 주의사항 등을 입력해 주세요."></textarea>
              </label>
              <label class="field">
                <span>카테고리</span>
                <select v-model="form.category" class="form-select">
                  <option disabled value="">선택하세요</option>
                  <option v-for="item in categories" :key="item.value" :value="item.value">{{ item.label }}</option>
                </select>
              </label>
              <label class="field">
                <span>보유 수량</span>
                <input v-model.number="form.totalQuantity" type="number" min="1" class="form-input" />
              </label>
              <label class="field field-full">
                <span>1개당 자산가치 <small class="field-hint">학생에게 결제되는 금액이 아닙니다.</small></span>
                <input v-model.number="form.price" type="number" min="0" step="1000" class="form-input" placeholder="예: 1200000" />
              </label>
            </div>

            <div v-if="error" class="error-box form-message">{{ error }}</div>
            <div v-if="success" class="success-box form-message">{{ success }}</div>

            <div class="form-actions">
              <router-link to="/courses" class="btn btn-ghost">취소</router-link>
              <button class="btn btn-primary" :disabled="submitting">{{ submitting ? '등록 중...' : '교보재 등록' }}</button>
            </div>
          </form>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { courseApi } from '@/api/course.js'
import { categoryOptions } from '@/store/course.js'

const router = useRouter()
const categories = computed(() => categoryOptions.filter(item => item.value !== 'ALL'))
const form = reactive({ title: '', description: '', category: '', price: null, totalQuantity: 1 })
const submitting = ref(false)
const error = ref('')
const success = ref('')

function validate() {
  if (!form.title || !form.description || !form.category) return '교보재명, 설명, 카테고리를 모두 입력해 주세요.'
  if (Number(form.totalQuantity) < 1) return '보유 수량은 1개 이상이어야 합니다.'
  if (form.price === null || Number(form.price) < 0) return '자산가치는 0원 이상이어야 합니다.'
  return ''
}

async function submit() {
  error.value = validate()
  success.value = ''
  if (error.value) return
  submitting.value = true
  try {
    const res = await courseApi.create({
      title: form.title,
      description: form.description,
      category: form.category,
      price: Number(form.price),
      itemType: 'OWNED',
      totalQuantity: Number(form.totalQuantity)
    })
    const id = res.data?.data?.id ?? res.data?.id
    success.value = '교보재가 등록되었습니다.'
    setTimeout(() => router.push(id ? `/courses/${id}` : '/courses'), 450)
  } catch (e) {
    error.value = e.response?.data?.message || '교보재 등록에 실패했습니다.'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.narrow { max-width: 850px; }
.form-surface { padding: 30px; }
.form-message { margin-top: 18px; }
@media (max-width: 620px) { .form-surface { padding: 20px; } }
</style>
