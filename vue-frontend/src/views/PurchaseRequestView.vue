<template>
  <div class="page-shell"><AppHeader /><main class="page-main"><div class="container">
    <div class="page-heading"><div><span class="eyebrow">ACQUISITION REQUEST</span><h1 class="page-title">미보유 장비 도입 요청</h1><p class="page-subtitle">대여할 자산이 없을 때 필요성과 예상 비용을 제출합니다.</p></div></div>
    <div class="request-layout"><section class="form-surface surface"><div class="workflow"><span class="active">1 요청</span><i></i><span>2 그룹 검토</span><i></i><span>3 학교 예산</span><i></i><span>4 입고·자산화</span></div><form @submit.prevent="submit"><div class="form-grid">
      <label class="field field-full"><span>장비명</span><input v-model.trim="form.title" class="form-input" placeholder="예: Sony A7 IV 카메라" /></label>
      <label class="field"><span>카테고리</span><select v-model="form.category" class="form-select"><option disabled value="">선택하세요</option><option v-for="item in categories" :key="item.value" :value="item.value">{{ item.label }}</option></select></label>
      <label class="field"><span>필요 수량</span><input v-model.number="form.quantity" type="number" min="1" class="form-input" /></label>
      <label class="field"><span>예상 단가</span><input v-model.number="form.unitPrice" type="number" min="1" class="form-input" /></label>
      <div class="field"><span>예상 총액</span><div class="total-field">{{ money(totalAmount) }}</div></div>
      <label class="field field-full"><span>구매 참고 링크</span><input v-model.trim="form.purchaseUrl" type="url" class="form-input" placeholder="https://..." /></label>
      <label class="field field-full"><span>필요 사양</span><textarea v-model.trim="form.description" class="form-textarea" placeholder="모델, 사양, 함께 필요한 구성품"></textarea></label>
      <label class="field field-full"><span>도입 필요성 <small class="field-hint">관리자 판단의 핵심 근거</small></span><textarea v-model.trim="form.reason" class="form-textarea" placeholder="기존 자산으로 해결할 수 없는 이유, 예상 사용자와 활용 일정"></textarea></label>
    </div><div v-if="error" class="error-box feedback">{{ error }}</div><div class="form-actions"><router-link :to="path('/assets')" class="btn btn-ghost">취소</router-link><button class="btn btn-primary" :disabled="busy">{{ busy?'제출 중...':'도입 요청 제출' }}</button></div></form></section>
      <aside class="guide-card surface"><span class="guide-mark">₩</span><h3>AI는 이 단계에서 판단하지 않습니다</h3><p>요청자는 이미 필요한 장비를 알고 있습니다. AI는 관리자 화면에서 누적 수요와 재고 부족을 비교하는 데 사용합니다.</p><ul><li>그룹 관리자가 필요성 검토</li><li>학교 관리자가 예산 검토</li><li>입고 확인 후 대여 자산 전환</li><li>실제 결제 기능은 포함하지 않음</li></ul></aside>
    </div>
  </div></main></div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { enrollmentApi } from '@/api/enrollment.js'
import { categoryOptions } from '@/store/course.js'
import { useGroupStore } from '@/store/group.js'
const route=useRoute(),router=useRouter(),groupStore=useGroupStore(),groupId=computed(()=>Number(route.params.groupId)),categories=categoryOptions.filter(item=>item.value!=='ALL')
const form=reactive({title:'',category:'',quantity:1,unitPrice:null,purchaseUrl:'',description:'',reason:''}),busy=ref(false),error=ref(''),totalAmount=computed(()=>Number(form.unitPrice||0)*Number(form.quantity||0)),path=suffix=>`/groups/${groupId.value}${suffix}`,money=value=>`${Number(value||0).toLocaleString()}원`
function validate(){if(!form.title||!form.category||!form.purchaseUrl||!form.reason)return'장비명, 카테고리, 구매 링크와 도입 필요성을 입력해 주세요.';if(Number(form.quantity)<1||Number(form.unitPrice)<=0)return'수량과 단가를 올바르게 입력해 주세요.';if(!/^https?:\/\//i.test(form.purchaseUrl))return'구매 링크는 http:// 또는 https://로 시작해야 합니다.';return''}
async function submit(){error.value=validate();if(error.value)return;busy.value=true;try{await enrollmentApi.requestPurchase({...form,groupId:groupId.value,unitPrice:Number(form.unitPrice),quantity:Number(form.quantity),alternativeChecked:false});router.push(path('/loans'))}catch(cause){error.value=cause.response?.data?.message||cause.response?.data?.error||'도입 요청 제출에 실패했습니다.'}finally{busy.value=false}}
onMounted(()=>groupStore.loadGroup(groupId.value).catch(()=>{}))
</script>

<style scoped>
.request-layout{display:grid;grid-template-columns:minmax(0,1fr) 285px;gap:22px;align-items:start}.form-surface{padding:28px}.workflow{display:flex;align-items:center;gap:7px;margin-bottom:24px;color:var(--color-text-muted);font-size:9px;font-weight:700}.workflow span{padding:5px 8px;border:1px solid var(--color-border);border-radius:999px}.workflow span.active{color:#fff;background:var(--color-primary);border-color:var(--color-primary)}.workflow i{height:1px;flex:1;background:var(--color-border)}.total-field{height:46px;display:flex;align-items:center;padding:0 13px;color:var(--color-primary);background:var(--color-primary-light);border-radius:var(--radius-md);font-weight:800}.feedback{margin-top:18px}.guide-card{position:sticky;top:91px;padding:23px}.guide-mark{width:42px;height:42px;display:grid;place-items:center;color:var(--color-warning);background:var(--color-warning-light);border-radius:12px;font-size:18px;font-weight:800}.guide-card h3{margin-top:16px;color:var(--color-navy);font-size:15px}.guide-card p{margin-top:8px;color:var(--color-text-secondary);font-size:10px;line-height:1.8}.guide-card ul{margin-top:17px;padding-top:15px;border-top:1px solid var(--color-border);list-style:none}.guide-card li{padding:5px 0;color:var(--color-text-secondary);font-size:9px}.guide-card li:before{content:'✓';margin-right:7px;color:var(--color-primary);font-weight:800}@media(max-width:880px){.request-layout{grid-template-columns:1fr}.guide-card{position:static}}@media(max-width:560px){.form-surface{padding:19px}.workflow i{display:none}.workflow{flex-wrap:wrap}}
</style>
