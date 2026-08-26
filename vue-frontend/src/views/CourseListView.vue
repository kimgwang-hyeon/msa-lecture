<template>
  <div class="page-shell"><AppHeader /><main class="page-main"><div class="container">
    <div class="page-heading"><div><span class="eyebrow">{{ group?.name || 'GROUP' }} · ASSET CATALOG</span><h1 class="page-title">대여 자산 찾기</h1><p class="page-subtitle">학교 공용 자산과 이 그룹의 전용 자산을 함께 조회합니다.</p></div><div class="heading-actions"><router-link :to="path('/acquisitions/new')" class="btn btn-outline">＋ 미보유 장비 요청</router-link><router-link v-if="isManager" :to="path('/assets/new')" class="btn btn-primary">＋ 보유 자산 등록</router-link></div></div>
    <section class="catalog-toolbar surface"><label class="search-box"><span>⌕</span><input v-model.trim="keyword" type="search" placeholder="자산명, 설명, 대여 장소 검색" /></label><label class="available-toggle"><input v-model="availableOnly" type="checkbox" /><span>대여 가능한 것만</span></label></section>
    <div class="category-tabs"><button v-for="category in categories" :key="category.value" :class="['category-tab',{active:selectedCategory===category.value}]" @click="courseStore.setCategory(category.value)"><span>{{ category.icon }}</span>{{ category.label }}</button></div>
    <div class="result-line"><span><strong>{{ filteredCourses.length }}</strong>개 자산</span><span v-if="courseStore.error" class="load-error">{{ courseStore.error }}</span></div>
    <div v-if="loading" class="loading-state surface"><div class="spinner"></div><span>자산을 불러오고 있습니다.</span></div>
    <div v-else-if="filteredCourses.length" class="gear-grid fade-in-up"><CourseCard v-for="course in filteredCourses" :key="course.id" :course="course" :group-id="groupId" /></div>
    <div v-else class="empty-state surface"><span class="empty-icon">⌕</span><strong>조건에 맞는 자산이 없습니다.</strong><p>필터를 바꾸거나 미보유 장비 도입을 요청해 보세요.</p></div>
  </div></main></div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import CourseCard from '@/components/CourseCard.vue'
import { useAuthStore } from '@/store/auth.js'
import { useCourseStore } from '@/store/course.js'
import { useGroupStore } from '@/store/group.js'
const route=useRoute(),auth=useAuthStore(),courseStore=useCourseStore(),groupStore=useGroupStore()
const groupId=computed(()=>Number(route.params.groupId)),group=computed(()=>groupStore.currentGroup)
const keyword=ref(''),availableOnly=ref(false),isManager=computed(()=>auth.isInstructor||group.value?.currentRole==='MANAGER')
const categories=computed(()=>courseStore.categories),loading=computed(()=>courseStore.loading),selectedCategory=computed(()=>courseStore.selectedCategory)
const path=suffix=>`/groups/${groupId.value}${suffix}`
const filteredCourses=computed(()=>{const needle=keyword.value.toLowerCase();return courseStore.courses.filter(course=>(selectedCategory.value==='ALL'||course.categoryCode===selectedCategory.value)&&(!needle||`${course.title} ${course.description||''} ${course.pickupLocation||''}`.toLowerCase().includes(needle))&&(!availableOnly.value||Number(course.availableQuantity)>0))})
async function load(){await groupStore.loadGroup(groupId.value).catch(()=>{});await courseStore.fetchCourses(groupId.value)}
onMounted(load);watch(groupId,load)
</script>

<style scoped>
.heading-actions{display:flex;gap:9px;flex-wrap:wrap}.catalog-toolbar{display:flex;align-items:center;gap:18px;padding:14px}.search-box{flex:1;display:flex;align-items:center;gap:10px;height:43px;padding:0 13px;background:var(--color-bg-secondary);border-radius:10px}.search-box span{color:var(--color-primary);font-size:20px}.search-box input{width:100%;border:0;outline:0;background:transparent;color:var(--color-text-primary);font-size:12px}.available-toggle{display:flex;align-items:center;gap:7px;padding-right:5px;color:var(--color-text-secondary);font-size:11px;font-weight:600;white-space:nowrap}.available-toggle input{accent-color:var(--color-primary)}.category-tabs{display:flex;gap:7px;padding:20px 0 14px;overflow-x:auto}.category-tab{display:inline-flex;align-items:center;gap:6px;padding:8px 12px;color:var(--color-text-secondary);background:#fff;border:1px solid var(--color-border);border-radius:999px;font-size:10px;font-weight:700;white-space:nowrap}.category-tab:hover,.category-tab.active{color:#fff;background:var(--color-primary);border-color:var(--color-primary)}.result-line{min-height:34px;display:flex;align-items:center;justify-content:space-between;color:var(--color-text-muted);font-size:10px}.result-line strong{color:var(--color-primary);font-size:14px}.load-error{color:var(--color-danger)}.gear-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:17px}.empty-state p{font-size:11px}@media(max-width:900px){.gear-grid{grid-template-columns:repeat(2,1fr)}}@media(max-width:620px){.gear-grid{grid-template-columns:1fr}.catalog-toolbar{align-items:stretch;flex-direction:column}.heading-actions{width:100%}.heading-actions .btn{flex:1}}
</style>
