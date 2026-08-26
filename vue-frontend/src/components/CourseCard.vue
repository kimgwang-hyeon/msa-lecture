<template>
  <router-link :to="`/courses/${course.id}`" class="gear-card surface">
    <div class="card-top" :class="toneClass">
      <span class="category-icon">{{ icon }}</span>
      <span :class="['stock-pill', { empty: available === 0 }]">
        {{ available > 0 ? `${available}개 대여 가능` : '대여 불가' }}
      </span>
    </div>
    <div class="card-body">
      <span class="badge">{{ course.category }}</span>
      <h3>{{ course.title }}</h3>
      <p>{{ course.description || '조직의 프로젝트와 교육에 사용할 수 있는 보유 장비입니다.' }}</p>
      <div class="card-meta">
        <div>
          <small>자산가치</small>
          <strong>{{ formatMoney(course.price) }}</strong>
        </div>
        <div class="usage">
          <small>누적 이용</small>
          <strong>{{ Number(course.enrollmentCount || 0).toLocaleString() }}회</strong>
        </div>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'
import { categoryIcon } from '@/store/course.js'

const props = defineProps({ course: { type: Object, required: true } })
const available = computed(() => Number(props.course.availableQuantity ?? 0))
const icon = computed(() => categoryIcon(props.course.categoryCode || props.course.category))
const toneClass = computed(() => `tone-${(props.course.categoryCode || 'ETC').toLowerCase().replace('_', '-')}`)

function formatMoney(value) {
  return `${Number(value || 0).toLocaleString()}원`
}
</script>

<style scoped>
.gear-card { overflow: hidden; transition: var(--transition); }
.gear-card:hover { transform: translateY(-4px); border-color: var(--color-border-hover); box-shadow: var(--shadow-md); }
.card-top { height: 118px; padding: 16px; display: flex; align-items: flex-start; justify-content: space-between; background: #eaf4f0; }
.tone-device { background: #e8f5f1; }
.tone-computer { background: #edf2fb; }
.tone-server-cloud { background: #eeeefa; }
.tone-electronics-iot { background: #fff3df; }
.tone-maker { background: #f7ecf2; }
.tone-camera-audio { background: #edf1f3; }
.category-icon { align-self: center; margin-left: 10px; color: rgba(16,42,67,.72); font-size: 44px; font-weight: 400; }
.stock-pill { padding: 5px 8px; color: var(--color-success); background: rgba(255,255,255,.82); border-radius: 999px; font-size: 10px; font-weight: 800; }
.stock-pill.empty { color: var(--color-danger); }
.card-body { padding: 17px; }
h3 { margin-top: 9px; color: var(--color-navy); font-size: 16px; line-height: 1.35; }
p { height: 42px; margin-top: 7px; overflow: hidden; color: var(--color-text-secondary); font-size: 12px; line-height: 1.7; }
.card-meta { margin-top: 16px; padding-top: 13px; border-top: 1px solid var(--color-border); display: flex; justify-content: space-between; gap: 12px; }
.card-meta div { display: flex; flex-direction: column; }
.card-meta small { color: var(--color-text-muted); font-size: 10px; }
.card-meta strong { margin-top: 2px; color: var(--color-text-primary); font-size: 12px; }
.usage { text-align: right; }
</style>
