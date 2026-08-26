<template>
  <div class="page-shell"><AppHeader /><main class="page-main"><div class="container narrow">
    <div class="page-heading"><div><span class="eyebrow">INVENTORY MANAGEMENT</span><h1 class="page-title">보유 자산 등록</h1><p class="page-subtitle">실제로 입고되어 바로 대여할 수 있는 자산과 수량을 등록합니다.</p></div></div>
    <section v-if="isManager" class="form-surface surface"><form @submit.prevent="submit"><div class="form-grid">
      <label class="field field-full"><span>자산명</span><input v-model.trim="form.title" class="form-input" placeholder="예: MacBook Pro 14 M3" /></label>
      <label class="field field-full"><span>설명</span><textarea v-model.trim="form.description" class="form-textarea" placeholder="사양, 구성품과 주의사항"></textarea></label>
      <label class="field"><span>카테고리</span><select v-model="form.category" class="form-select"><option disabled value="">선택하세요</option><option v-for="item in categories" :key="item.value" :value="item.value">{{ item.label }}</option></select></label>
      <label class="field"><span>보유 수량</span><input v-model.number="form.totalQuantity" type="number" min="1" class="form-input" /></label>
      <label class="field"><span>대여 범위</span><select v-model="form.visibility" class="form-select"><option value="GROUP">{{ group?.name }} 전용</option><option v-if="auth.isInstructor" value="ORGANIZATION">학교 전체 공용</option></select></label>
      <label class="field"><span>최대 대여일</span><input v-model.number="form.maxLoanDays" type="number" min="1" max="60" class="form-input" /></label>
      <label class="field"><span>수령·반납 장소</span><input v-model.trim="form.pickupLocation" class="form-input" placeholder="예: 공학관 301호" /></label>
      <label class="field"><span>취득 단가</span><input v-model.number="form.price" type="number" min="0" class="form-input" /></label>
      <label class="field field-full"><span>참고 구매 링크 <small class="field-hint">선택</small></span><input v-model.trim="form.purchaseUrl" type="url" class="form-input" placeholder="https://..." /></label>
    </div><div v-if="error" class="error-box feedback">{{ error }}</div><div class="form-actions"><router-link :to="path('/assets')" class="btn btn-ghost">취소</router-link><button class="btn btn-primary" :disabled="busy">{{ busy?'등록 중...':'자산 등록' }}</button></div></form></section>
    <div v-else class="empty-state surface"><strong>그룹 관리자만 자산을 등록할 수 있습니다.</strong><router-link :to="path('')" class="btn btn-outline">그룹 홈</router-link></div>
  </div></main></div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { courseApi } from '@/api/course.js'
import { useAuthStore } from '@/store/auth.js'
import { categoryOptions } from '@/store/course.js'
import { useGroupStore } from '@/store/group.js'
const route=useRoute(),router=useRouter(),auth=useAuthStore(),groupStore=useGroupStore(),groupId=computed(()=>Number(route.params.groupId)),group=computed(()=>groupStore.currentGroup)
const isManager=computed(()=>auth.isInstructor||group.value?.currentRole==='MANAGER'),categories=categoryOptions.filter(item=>item.value!=='ALL'),busy=ref(false),error=ref('')
const form=reactive({title:'',description:'',category:'',totalQuantity:1,visibility:'GROUP',maxLoanDays:7,pickupLocation:'',price:0,purchaseUrl:''})
const path=suffix=>`/groups/${groupId.value}${suffix}`
async function submit(){error.value='';if(!form.title||!form.category||!form.pickupLocation){error.value='자산명, 카테고리, 수령 장소를 입력해 주세요.';return}busy.value=true;try{await courseApi.create({...form,itemType:'OWNED',ownerGroupId:form.visibility==='GROUP'?groupId.value:null,purchaseUrl:form.purchaseUrl||null});router.push(path('/assets'))}catch(cause){error.value=cause.response?.data?.message||cause.response?.data?.error||'자산 등록에 실패했습니다.'}finally{busy.value=false}}
onMounted(()=>groupStore.loadGroup(groupId.value).catch(()=>{}))
</script>

<style scoped>
.narrow{max-width:850px}.form-surface{padding:30px}.feedback{margin-top:18px}@media(max-width:620px){.form-surface{padding:20px}}
</style>
