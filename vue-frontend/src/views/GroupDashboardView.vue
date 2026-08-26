<template>
  <div class="page-shell"><AppHeader /><main class="page-main"><div class="container">
    <div v-if="loading" class="loading-state surface"><div class="spinner"></div><span>그룹 현황을 준비하고 있습니다.</span></div>
    <template v-else-if="group">
      <section class="workspace-hero surface fade-in-up">
        <div><span class="eyebrow">GROUP WORKSPACE</span><h1>{{ group.name }}</h1><p>{{ group.description || '우리 그룹의 공용 장비를 함께 운영합니다.' }}</p></div>
        <div v-if="isManager" class="invite"><small>구성원 초대코드</small><button @click="copyInvite">{{ copied ? '복사됨 ✓' : group.inviteCode }}</button></div>
      </section>
      <div class="summary-grid dashboard-summary">
        <div class="summary-card surface"><span class="summary-label">조회 가능한 자산</span><strong class="summary-value">{{ assets.length }}</strong><small>학교 공용 + 그룹 전용</small></div>
        <div class="summary-card surface"><span class="summary-label">현재 대여 중</span><strong class="summary-value">{{ activeLoans }}</strong><small>반납 요청 포함</small></div>
        <div class="summary-card surface"><span class="summary-label">처리 대기 요청</span><strong class="summary-value warning">{{ pendingRequests }}</strong><small>내 요청 기준</small></div>
        <div class="summary-card surface"><span class="summary-label">그룹 구성원</span><strong class="summary-value">{{ memberCount || '—' }}</strong><small>{{ isManager ? '활성 구성원' : '관리자 화면에서 확인' }}</small></div>
      </div>
      <section class="module-grid">
        <router-link :to="path('/assets')" class="module surface"><span class="module-icon green">▦</span><div><strong>자산 카탈로그</strong><p>재고와 대여 조건을 확인합니다.</p></div><b>→</b></router-link>
        <router-link :to="path('/loans')" class="module surface"><span class="module-icon blue">↔</span><div><strong>내 대여·도입 요청</strong><p>승인과 반납 상태를 추적합니다.</p></div><b>→</b></router-link>
        <router-link :to="path('/acquisitions/new')" class="module surface"><span class="module-icon amber">＋</span><div><strong>미보유 장비 요청</strong><p>필요성을 적어 도입 검토를 시작합니다.</p></div><b>→</b></router-link>
        <router-link v-if="isManager" :to="path('/admin')" class="module surface"><span class="module-icon purple">✓</span><div><strong>운영 데스크</strong><p>대여·반납·도입·입고를 처리합니다.</p></div><b>→</b></router-link>
        <router-link v-if="isManager" :to="path('/analytics')" class="module ai-module"><span class="module-icon dark">✦</span><div><strong>AI 수요예측</strong><p>4주 부족 재고와 이동 가능 수량을 봅니다.</p></div><b>→</b></router-link>
      </section>
      <section class="recent surface"><div class="section-head"><div><span class="eyebrow">AVAILABLE NOW</span><h2>지금 대여 가능한 장비</h2></div><router-link :to="path('/assets')">전체 보기 →</router-link></div><div class="asset-row"><router-link v-for="asset in availableAssets" :key="asset.id" :to="path(`/assets/${asset.id}`)" class="mini-asset"><span>{{ categoryIcon(asset.category) }}</span><div><strong>{{ asset.title }}</strong><small>{{ asset.availableQuantity }}개 · {{ asset.pickupLocation || '운영실' }}</small></div></router-link><p v-if="!availableAssets.length" class="muted">현재 대여 가능한 자산이 없습니다.</p></div></section>
    </template>
    <div v-else class="empty-state surface"><strong>그룹 정보를 불러오지 못했습니다.</strong><router-link to="/groups" class="btn btn-outline">그룹 목록</router-link></div>
  </div></main></div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { courseApi } from '@/api/course.js'
import { enrollmentApi } from '@/api/enrollment.js'
import { groupApi } from '@/api/group.js'
import { useAuthStore } from '@/store/auth.js'
import { useGroupStore } from '@/store/group.js'
import { categoryIcon } from '@/store/course.js'
const route = useRoute(), auth = useAuthStore(), groupStore = useGroupStore()
const groupId = computed(() => Number(route.params.groupId)), group = computed(() => groupStore.currentGroup)
const assets = ref([]), requests = ref([]), memberCount = ref(0), loading = ref(true), copied = ref(false)
const isManager = computed(() => auth.isInstructor || group.value?.currentRole === 'MANAGER')
const activeLoans = computed(() => requests.value.filter(item => item.requestType === 'LOAN' && ['ACTIVE','RETURN_REQUESTED'].includes(item.status)).length)
const pendingRequests = computed(() => requests.value.filter(item => ['PENDING','GROUP_APPROVED','BUDGET_APPROVED'].includes(item.status)).length)
const availableAssets = computed(() => assets.value.filter(item => Number(item.availableQuantity)>0).slice(0,4))
const path = suffix => `/groups/${groupId.value}${suffix}`
async function copyInvite(){ await navigator.clipboard?.writeText(group.value.inviteCode); copied.value=true; setTimeout(()=>copied.value=false,1400) }
async function load(){ loading.value=true; try { await groupStore.loadGroup(groupId.value); const [assetRes, requestRes] = await Promise.all([courseApi.getAll({groupId:groupId.value}), enrollmentApi.getMyEnrollments(groupId.value)]); assets.value=assetRes.data?.data??[]; requests.value=requestRes.data?.data??[]; if(isManager.value){ const memberRes=await groupApi.getMembers(groupId.value); memberCount.value=(memberRes.data?.data??[]).length } } finally { loading.value=false } }
onMounted(load)
</script>

<style scoped>
.workspace-hero{display:flex;align-items:center;justify-content:space-between;gap:20px;padding:30px;margin-bottom:18px;background:linear-gradient(125deg,#fff 55%,#e8f5f1)}.workspace-hero h1{color:var(--color-navy);font-size:31px;letter-spacing:-.04em}.workspace-hero p{margin-top:7px;color:var(--color-text-secondary);font-size:13px}.invite{display:flex;flex-direction:column;align-items:flex-end}.invite small{color:var(--color-text-muted);font-size:9px}.invite button{margin-top:5px;padding:8px 13px;color:var(--color-primary);background:#fff;border:1px dashed #85b9a8;border-radius:9px;font-size:12px;font-weight:800;letter-spacing:.1em}.dashboard-summary{grid-template-columns:repeat(4,1fr)}.summary-card small{display:block;margin-top:3px;color:var(--color-text-muted);font-size:9px}.summary-value.warning{color:var(--color-warning)}.module-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:13px;margin:20px 0}.module{display:grid;grid-template-columns:44px 1fr auto;align-items:center;gap:13px;padding:19px;transition:var(--transition)}.module:hover,.ai-module:hover{transform:translateY(-3px);box-shadow:var(--shadow-md)}.module-icon{width:44px;height:44px;display:grid;place-items:center;border-radius:12px;font-size:18px}.green{color:#0b6b57;background:#e8f5f1}.blue{color:#38598b;background:#edf2fb}.amber{color:#9a5b00;background:#fff4df}.purple{color:#7046a1;background:#f2ecf8}.dark{color:#fff;background:#172b3a}.module strong{color:var(--color-navy);font-size:13px}.module p{margin-top:3px;color:var(--color-text-muted);font-size:9px}.module b{color:var(--color-primary)}.ai-module{display:grid;grid-template-columns:44px 1fr auto;align-items:center;gap:13px;padding:19px;color:#fff;background:linear-gradient(130deg,#172b3a,#263f52);border-radius:var(--radius-lg);box-shadow:var(--shadow-sm)}.ai-module strong{color:#fff}.ai-module p{color:#b9c7d1}.ai-module b{color:#8ce0c7}.recent{padding:23px}.section-head{display:flex;align-items:end;justify-content:space-between}.section-head h2{color:var(--color-navy);font-size:18px}.section-head>a{color:var(--color-primary);font-size:10px;font-weight:700}.asset-row{display:grid;grid-template-columns:repeat(4,1fr);gap:9px;margin-top:16px}.mini-asset{display:flex;align-items:center;gap:9px;padding:12px;background:var(--color-bg-secondary);border-radius:10px}.mini-asset>span{width:32px;height:32px;display:grid;place-items:center;color:var(--color-primary);background:#fff;border-radius:9px}.mini-asset div{min-width:0;display:flex;flex-direction:column}.mini-asset strong{overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:10px}.mini-asset small{color:var(--color-text-muted);font-size:8px}.muted{color:var(--color-text-muted);font-size:11px}
@media(max-width:900px){.dashboard-summary{grid-template-columns:repeat(2,1fr)}.module-grid{grid-template-columns:repeat(2,1fr)}.asset-row{grid-template-columns:repeat(2,1fr)}}@media(max-width:600px){.workspace-hero{align-items:flex-start;flex-direction:column}.invite{align-items:flex-start}.module-grid,.asset-row{grid-template-columns:1fr}}
</style>
