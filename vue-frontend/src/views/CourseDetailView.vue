<template>
  <div class="page-shell"><AppHeader /><main class="page-main"><div class="container">
    <router-link :to="path('/assets')" class="back-link">← 자산 목록</router-link>
    <div v-if="loading" class="loading-state surface"><div class="spinner"></div></div>
    <div v-else-if="course" class="detail-grid fade-in-up">
      <section class="detail-main"><div class="gear-visual"><span>{{ categoryIcon(course.categoryCode||course.category) }}</span><div class="asset-number">ASSET #{{ String(course.id).padStart(4,'0') }}</div></div><div class="detail-copy surface"><div class="tag-row"><span class="badge">{{ course.category }}</span><span class="scope">{{ course.visibility==='ORGANIZATION'?'학교 공용':'그룹 전용' }}</span></div><h1>{{ course.title }}</h1><p>{{ course.description||'수업, 연구와 그룹 활동에 사용할 수 있는 자산입니다.' }}</p><div class="spec-grid"><div><small>전체 수량</small><strong>{{ course.totalQuantity }}개</strong></div><div><small>가용 수량</small><strong :class="{danger:available===0}">{{ available }}개</strong></div><div><small>수령 장소</small><strong>{{ course.pickupLocation||'그룹 운영실' }}</strong></div><div><small>최대 대여</small><strong>{{ course.maxLoanDays||7 }}일</strong></div></div></div></section>
      <aside class="request-panel surface"><div class="panel-label">LOAN REQUEST</div><h2>대여 신청</h2><div class="stock-line"><span>현재 상태</span><strong :class="{out:available===0}">{{ available>0?'대여 가능':'재고 없음' }}</strong></div>
        <div v-if="openRequest" :class="['request-status',statusTone(openRequest.status)]"><strong>{{ statusLabel(openRequest.status) }}</strong><span>{{ statusDescription(openRequest.status) }}</span></div>
        <template v-else><div class="date-grid"><label class="field"><span>대여 시작일</span><input v-model="form.requestedFrom" type="date" :min="today" class="form-input" /></label><label class="field"><span>반납 예정일</span><input v-model="form.dueDate" type="date" :min="form.requestedFrom" :max="maxDueDate" class="form-input" /></label></div><label class="field reason-field"><span>사용 목적</span><textarea v-model.trim="form.reason" class="form-textarea" placeholder="수업·연구·행사 등 구체적인 사용 목적" maxlength="500"></textarea></label></template>
        <div v-if="error" class="error-box">{{ error }}</div><button class="btn btn-primary btn-block" :disabled="submitting||available===0||!!openRequest" @click="submitLoan">{{ submitting?'신청 중...':(openRequest?'요청 처리 중':'대여 신청하기') }}</button><p class="helper">그룹 관리자가 승인하면 재고가 배정됩니다. 반납 시에도 관리자 확인이 필요합니다.</p>
      </aside>
    </div>
    <div v-else class="empty-state surface"><strong>자산 정보를 찾을 수 없습니다.</strong></div>
  </div></main></div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { enrollmentApi } from '@/api/enrollment.js'
import { categoryIcon, useCourseStore } from '@/store/course.js'
const route=useRoute(),courseStore=useCourseStore(),error=ref(''),submitting=ref(false),requests=ref([])
const groupId=computed(()=>Number(route.params.groupId)),course=computed(()=>courseStore.selectedCourse),loading=computed(()=>courseStore.loading),available=computed(()=>Number(course.value?.availableQuantity||0))
const asIso=date=>{const local=new Date(date.getTime()-date.getTimezoneOffset()*60000);return local.toISOString().slice(0,10)}
const today=asIso(new Date()),form=reactive({requestedFrom:today,dueDate:today,reason:''})
const openRequest=computed(()=>requests.value.find(item=>Number(item.courseId)===Number(course.value?.id)&&['PENDING','ACTIVE','RETURN_REQUESTED'].includes(item.status)))
const maxDueDate=computed(()=>{const start=new Date(`${form.requestedFrom}T00:00:00`);start.setDate(start.getDate()+Math.max(1,Number(course.value?.maxLoanDays||7))-1);return asIso(start)})
const path=suffix=>`/groups/${groupId.value}${suffix}`
const statusLabel=status=>({PENDING:'승인 대기',ACTIVE:'대여 중',RETURN_REQUESTED:'반납 확인 대기'}[status]||status)
const statusDescription=status=>({PENDING:'그룹 관리자가 요청을 검토하고 있습니다.',ACTIVE:'대여가 승인되었습니다. 내 요청에서 반납할 수 있습니다.',RETURN_REQUESTED:'장비를 반납 장소에 전달하면 관리자가 확인합니다.'}[status]||'')
const statusTone=status=>status==='ACTIVE'?'active':'pending'
function explain(cause){return cause.response?.data?.message||cause.response?.data?.detail||cause.response?.data?.error||'대여 신청에 실패했습니다.'}
async function submitLoan(){error.value='';if(!form.reason){error.value='사용 목적을 입력해 주세요.';return}if(form.dueDate>maxDueDate.value){error.value=`최대 ${course.value.maxLoanDays||7}일까지 대여할 수 있습니다.`;return}submitting.value=true;try{const res=await enrollmentApi.enroll({courseId:course.value.id,groupId:groupId.value,...form});requests.value.unshift(res.data?.data);form.reason=''}catch(cause){error.value=explain(cause)}finally{submitting.value=false}}
onMounted(async()=>{await courseStore.fetchCourse(route.params.id);const days=Math.min(7,Number(course.value?.maxLoanDays||7));const due=new Date();due.setDate(due.getDate()+days-1);form.dueDate=asIso(due);try{const res=await enrollmentApi.getMyEnrollments(groupId.value);requests.value=res.data?.data??[]}catch{requests.value=[]}})
</script>

<style scoped>
.back-link{display:inline-block;margin-bottom:20px;color:var(--color-text-secondary);font-size:11px;font-weight:600}.detail-grid{display:grid;grid-template-columns:1fr 370px;gap:22px;align-items:start}.detail-main{display:flex;flex-direction:column;gap:17px}.gear-visual{position:relative;height:270px;display:grid;place-items:center;color:rgba(16,42,67,.72);background:linear-gradient(145deg,#e8f5f1,#edf2fb);border-radius:var(--radius-xl);overflow:hidden}.gear-visual:before,.gear-visual:after{content:'';position:absolute;border:1px solid rgba(16,42,67,.08);border-radius:50%}.gear-visual:before{width:310px;height:310px}.gear-visual:after{width:190px;height:190px}.gear-visual>span{z-index:1;font-size:94px}.asset-number{position:absolute;left:20px;bottom:17px;font-size:9px;font-weight:800;letter-spacing:.15em}.detail-copy{padding:28px}.tag-row{display:flex;align-items:center;gap:8px}.scope{color:var(--color-text-muted);font-size:9px}.detail-copy h1{margin-top:12px;color:var(--color-navy);font-size:29px;letter-spacing:-.04em}.detail-copy>p{margin-top:12px;color:var(--color-text-secondary);font-size:13px;line-height:1.8}.spec-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:10px;margin-top:25px;padding-top:20px;border-top:1px solid var(--color-border)}.spec-grid div{display:flex;flex-direction:column}.spec-grid small{color:var(--color-text-muted);font-size:9px}.spec-grid strong{margin-top:3px;font-size:12px}.danger{color:var(--color-danger)}.request-panel{position:sticky;top:90px;padding:25px}.panel-label{color:var(--color-primary);font-size:9px;font-weight:800;letter-spacing:.15em}.request-panel h2{margin-top:6px;color:var(--color-navy);font-size:21px}.stock-line{display:flex;align-items:center;justify-content:space-between;margin:18px 0;padding:11px 0;border-top:1px solid var(--color-border);border-bottom:1px solid var(--color-border);font-size:11px}.stock-line strong{color:var(--color-success)}.stock-line .out{color:var(--color-danger)}.date-grid{display:grid;grid-template-columns:1fr 1fr;gap:9px}.field span{font-size:10px;font-weight:700}.date-grid .form-input{padding:0 8px;font-size:10px}.reason-field{margin:11px 0}.request-status{display:flex;flex-direction:column;gap:3px;margin-bottom:15px;padding:13px;border-radius:11px}.request-status strong{font-size:12px}.request-status span{font-size:9px}.request-status.pending{color:var(--color-warning);background:var(--color-warning-light)}.request-status.active{color:var(--color-success);background:var(--color-success-light)}.error-box{margin-bottom:10px}.helper{margin-top:12px;color:var(--color-text-muted);font-size:9px;text-align:center}@media(max-width:850px){.detail-grid{grid-template-columns:1fr}.request-panel{position:static}}@media(max-width:560px){.spec-grid{grid-template-columns:repeat(2,1fr)}.date-grid{grid-template-columns:1fr}}
</style>
