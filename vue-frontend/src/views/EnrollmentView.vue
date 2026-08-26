<template>
  <div class="page-shell"><AppHeader /><main class="page-main"><div class="container">
    <div class="page-heading"><div><span class="eyebrow">MY REQUESTS</span><h1 class="page-title">내 대여·도입 요청</h1><p class="page-subtitle">신청부터 승인, 반납 또는 입고까지 전체 상태를 확인합니다.</p></div><router-link :to="path('/assets')" class="btn btn-outline">자산 찾기</router-link></div>
    <div class="summary-grid"><div class="summary-card surface"><span class="summary-label">승인 대기</span><strong class="summary-value">{{ countStatus('PENDING') }}</strong></div><div class="summary-card surface"><span class="summary-label">대여 중</span><strong class="summary-value">{{ activeCount }}</strong></div><div class="summary-card surface"><span class="summary-label">연체</span><strong class="summary-value danger">{{ overdueCount }}</strong></div></div>
    <div v-if="message" class="success-box feedback">{{ message }}</div><div v-if="error" class="error-box feedback">{{ error }}</div>
    <div class="tabs"><button :class="{active:tab==='LOAN'}" @click="tab='LOAN'">대여 <span>{{ countType('LOAN') }}</span></button><button :class="{active:tab==='PURCHASE'}" @click="tab='PURCHASE'">도입 <span>{{ countType('PURCHASE') }}</span></button></div>
    <div v-if="loading" class="loading-state surface"><div class="spinner"></div></div>
    <div v-else-if="filtered.length" class="request-list">
      <article v-for="item in filtered" :key="item.id" class="request-card surface" :class="{overdue:item.overdue}">
        <router-link :to="path(`/assets/${item.courseId}`)" class="asset-symbol">{{ categoryIcon(item.course?.category) }}</router-link>
        <div class="request-copy"><div class="title-row"><span :class="['status-badge',statusTone(item.status)]">{{ statusLabel(item.status) }}</span><span v-if="item.overdue" class="overdue-pill">연체</span></div><h3>{{ item.course?.title||`자산 #${item.courseId}` }}</h3><p>{{ item.reason }}</p><div class="meta"><span>REQ-{{ String(item.id).padStart(4,'0') }}</span><span v-if="item.requestedFrom">{{ shortDate(item.requestedFrom) }} → {{ shortDate(item.dueDate) }}</span><span>{{ dateTime(item.createdAt) }}</span><span v-if="item.course?.pickupLocation">{{ item.course.pickupLocation }}</span></div><p v-if="item.reviewComment" class="review">검토 의견 · {{ item.reviewComment }}</p></div>
        <div class="request-actions"><button v-if="item.requestType==='LOAN'&&item.status==='ACTIVE'" class="btn btn-primary btn-sm" :disabled="busyId===item.id" @click="requestReturn(item)">반납 요청</button><span v-else class="next-step">{{ nextStep(item) }}</span></div>
      </article>
    </div>
    <div v-else class="empty-state surface"><span class="empty-icon">↔</span><strong>{{ tab==='LOAN'?'대여 요청이 없습니다.':'도입 요청이 없습니다.' }}</strong><router-link :to="tab==='LOAN'?path('/assets'):path('/acquisitions/new')" class="btn btn-outline">새 요청 시작</router-link></div>
  </div></main></div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { enrollmentApi } from '@/api/enrollment.js'
import { categoryIcon } from '@/store/course.js'
import { useGroupStore } from '@/store/group.js'
const route=useRoute(),groupStore=useGroupStore(),groupId=computed(()=>Number(route.params.groupId)),items=ref([]),loading=ref(true),tab=ref('LOAN'),busyId=ref(null),error=ref(''),message=ref('')
const filtered=computed(()=>items.value.filter(item=>item.requestType===tab.value)),activeCount=computed(()=>items.value.filter(item=>item.requestType==='LOAN'&&['ACTIVE','RETURN_REQUESTED'].includes(item.status)).length),overdueCount=computed(()=>items.value.filter(item=>item.overdue).length)
const path=suffix=>`/groups/${groupId.value}${suffix}`,countStatus=status=>items.value.filter(item=>item.status===status).length,countType=type=>items.value.filter(item=>item.requestType===type).length
const labels={PENDING:'검토 대기',GROUP_APPROVED:'그룹 승인',ACTIVE:'대여 중',RETURN_REQUESTED:'반납 확인 대기',RETURNED:'반납 완료',BUDGET_APPROVED:'예산 승인',RECEIVED:'입고 완료',REJECTED:'반려',CANCELLED:'취소'}
const statusLabel=status=>labels[status]||status,statusTone=status=>['ACTIVE','RETURNED','RECEIVED','BUDGET_APPROVED'].includes(status)?'status-active':status==='REJECTED'?'status-rejected':['PENDING','GROUP_APPROVED','RETURN_REQUESTED'].includes(status)?'status-pending':'status-muted'
const shortDate=value=>value?new Intl.DateTimeFormat('ko-KR',{month:'short',day:'numeric'}).format(new Date(`${value}T00:00:00`)):'-',dateTime=value=>value?new Intl.DateTimeFormat('ko-KR',{dateStyle:'medium'}).format(new Date(value)):'-'
function nextStep(item){return({PENDING:'관리자 검토 중',GROUP_APPROVED:'학교 예산 검토 중',RETURN_REQUESTED:'장비 전달 후 확인',BUDGET_APPROVED:'입고 대기',RETURNED:'완료',RECEIVED:'자산 전환 완료',REJECTED:'요청 종료'}[item.status]||'')}
async function load(){loading.value=true;try{await groupStore.loadGroup(groupId.value);const res=await enrollmentApi.getMyEnrollments(groupId.value);items.value=(res.data?.data??[]).sort((a,b)=>new Date(b.createdAt)-new Date(a.createdAt))}catch(cause){error.value=cause.response?.data?.message||'요청 목록을 불러오지 못했습니다.'}finally{loading.value=false}}
async function requestReturn(item){busyId.value=item.id;error.value='';try{const res=await enrollmentApi.requestReturn(item.id);Object.assign(item,res.data?.data);message.value='반납 요청을 보냈습니다. 장비를 수령 장소에 전달해 주세요.'}catch(cause){error.value=cause.response?.data?.message||cause.response?.data?.error||'반납 요청에 실패했습니다.'}finally{busyId.value=null}}
onMounted(load)
</script>

<style scoped>
.summary-value.danger{color:var(--color-danger)}.feedback{margin-bottom:14px}.tabs{display:flex;gap:8px;margin-bottom:14px}.tabs button{display:flex;align-items:center;gap:7px;padding:9px 14px;color:var(--color-text-secondary);background:#fff;border:1px solid var(--color-border);border-radius:10px;font-size:11px;font-weight:700}.tabs button.active{color:#fff;background:var(--color-primary);border-color:var(--color-primary)}.tabs span{opacity:.7}.request-list{display:flex;flex-direction:column;gap:11px}.request-card{display:grid;grid-template-columns:54px minmax(0,1fr) 155px;align-items:center;gap:17px;padding:19px}.request-card.overdue{border-left:4px solid var(--color-danger)}.asset-symbol{width:54px;height:54px;display:grid;place-items:center;color:var(--color-primary);background:var(--color-primary-light);border-radius:14px;font-size:22px}.title-row{display:flex;gap:6px}.overdue-pill{padding:4px 8px;color:#fff;background:var(--color-danger);border-radius:999px;font-size:9px;font-weight:800}.request-copy h3{margin-top:6px;color:var(--color-navy);font-size:15px}.request-copy>p{margin-top:4px;color:var(--color-text-secondary);font-size:10px}.meta{display:flex;flex-wrap:wrap;gap:11px;margin-top:8px;color:var(--color-text-muted);font-size:9px}.request-copy .review{margin-top:8px;padding:7px 9px;color:var(--color-danger);background:var(--color-danger-light);border-radius:7px}.request-actions{text-align:right}.next-step{color:var(--color-text-muted);font-size:9px;font-weight:650}@media(max-width:700px){.request-card{grid-template-columns:44px 1fr}.asset-symbol{width:44px;height:44px}.request-actions{grid-column:2;text-align:left}}
</style>
