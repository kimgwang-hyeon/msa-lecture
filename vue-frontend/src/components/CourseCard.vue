<template>
  <router-link :to="`/groups/${groupId}/assets/${course.id}`" class="gear-card surface">
    <div class="card-top" :class="toneClass">
      <span class="category-icon">{{ icon }}</span>
      <span :class="['stock-pill', { empty: available === 0 }]">{{ available > 0 ? `${available}개 가능` : '대여 불가' }}</span>
    </div>
    <div class="card-body">
      <div class="scope-line"><span class="badge">{{ course.category }}</span><small>{{ course.visibility === 'ORGANIZATION' ? '학교 공용' : '그룹 전용' }}</small></div>
      <h3>{{ course.title }}</h3>
      <p>{{ course.description || '수업, 연구와 그룹 활동에 사용할 수 있는 자산입니다.' }}</p>
      <div class="card-meta">
        <div><small>대여 장소</small><strong>{{ course.pickupLocation || '그룹 운영실' }}</strong></div>
        <div class="usage"><small>최대 기간</small><strong>{{ course.maxLoanDays || 7 }}일</strong></div>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'
import { categoryIcon } from '@/store/course.js'
const props = defineProps({ course: { type: Object, required: true }, groupId: { type: [String, Number], required: true } })
const available = computed(() => Number(props.course.availableQuantity ?? 0))
const icon = computed(() => categoryIcon(props.course.categoryCode || props.course.category))
const toneClass = computed(() => `tone-${(props.course.categoryCode || 'ETC').toLowerCase().replaceAll('_', '-')}`)
</script>

<style scoped>
.gear-card { overflow: hidden; transition: var(--transition); }
.gear-card:hover { transform: translateY(-4px); border-color: var(--color-border-hover); box-shadow: var(--shadow-md); }
.card-top { height: 112px; padding: 15px; display: flex; align-items: flex-start; justify-content: space-between; background: #eaf4f0; }
.tone-computer { background: #edf2fb; }.tone-camera-audio { background: #f1eef8; }.tone-presentation { background: #fff3df; }.tone-maker { background: #f7ecf2; }.tone-electronics-iot { background: #e9f4f2; }.tone-device { background: #eef3fb; }.tone-accessory { background: #f1f3ef; }
.category-icon { align-self: center; margin-left: 9px; color: rgba(16,42,67,.7); font-size: 42px; }
.stock-pill { padding: 5px 8px; color: var(--color-success); background: rgba(255,255,255,.84); border-radius: 999px; font-size: 9px; font-weight: 800; }.stock-pill.empty { color: var(--color-danger); }
.card-body { padding: 17px; }.scope-line { display: flex; align-items: center; justify-content: space-between; gap: 8px; }.scope-line small { color: var(--color-text-muted); font-size: 9px; }
h3 { margin-top: 9px; color: var(--color-navy); font-size: 16px; line-height: 1.35; }p { height: 42px; margin-top: 7px; overflow: hidden; color: var(--color-text-secondary); font-size: 11px; line-height: 1.8; }
.card-meta { margin-top: 15px; padding-top: 12px; border-top: 1px solid var(--color-border); display: flex; justify-content: space-between; gap: 12px; }.card-meta div { display: flex; flex-direction: column; }.card-meta small { color: var(--color-text-muted); font-size: 9px; }.card-meta strong { margin-top: 2px; font-size: 11px; }.usage { text-align: right; }
</style>
