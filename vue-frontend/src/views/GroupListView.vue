<template>
  <div class="page-shell">
    <AppHeader />
    <main class="page-main"><div class="container">
      <div class="page-heading">
        <div><span class="eyebrow">CAMPUS WORKSPACES</span><h1 class="page-title">내 그룹</h1><p class="page-subtitle">학과·연구실·동아리별 자산 공간으로 들어가세요.</p></div>
        <button class="btn btn-ghost" :disabled="loading" @click="load">↻ 새로고침</button>
      </div>

      <div class="group-layout">
        <section>
          <div v-if="loading" class="loading-state surface"><div class="spinner"></div></div>
          <div v-else-if="groups.length" class="group-grid fade-in-up">
            <router-link v-for="(group, index) in groups" :key="group.id" :to="`/groups/${group.id}`" class="group-card surface">
              <div class="group-symbol" :class="`tone-${index % 4}`">{{ group.name.charAt(0) }}</div>
              <div><span class="role">{{ group.currentRole === 'MANAGER' ? 'MANAGER' : 'MEMBER' }}</span><h2>{{ group.name }}</h2><p>{{ group.description || '그룹 장비와 대여 요청을 한곳에서 관리합니다.' }}</p></div>
              <span class="arrow">→</span>
            </router-link>
          </div>
          <div v-else class="empty-state surface"><span class="empty-icon">⌂</span><strong>아직 참여한 그룹이 없습니다.</strong><p>운영자에게 받은 초대코드로 참여해 주세요.</p></div>
        </section>

        <aside class="join-panel surface">
          <span class="panel-icon">#</span><h3>초대코드로 참여</h3><p>그룹 관리자에게 받은 8자리 코드를 입력하세요.</p>
          <form @submit.prevent="join"><input v-model.trim="inviteCode" class="form-input code-input" maxlength="12" placeholder="예: LAB2026A" /><button class="btn btn-primary btn-block" :disabled="busy || !inviteCode">그룹 참여</button></form>
          <template v-if="auth.isInstructor"><div class="divider"><span>학교 관리자</span></div><button class="btn btn-outline btn-block" @click="showCreate = !showCreate">＋ 새 그룹 만들기</button>
            <form v-if="showCreate" class="create-form" @submit.prevent="create"><input v-model.trim="newGroup.name" class="form-input" placeholder="그룹 이름" /><textarea v-model.trim="newGroup.description" class="form-textarea" placeholder="그룹 설명"></textarea><button class="btn btn-primary btn-block" :disabled="busy || !newGroup.name">생성</button></form>
          </template>
          <div v-if="error" class="error-box feedback">{{ error }}</div><div v-if="message" class="success-box feedback">{{ message }}</div>
        </aside>
      </div>
    </div></main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { groupApi } from '@/api/group.js'
import { useAuthStore } from '@/store/auth.js'
import { useGroupStore } from '@/store/group.js'

const auth = useAuthStore(), groupStore = useGroupStore(), router = useRouter()
const groups = computed(() => groupStore.groups), loading = computed(() => groupStore.loading)
const inviteCode = ref(''), showCreate = ref(false), busy = ref(false), error = ref(''), message = ref('')
const newGroup = reactive({ name: '', description: '' })
const explain = cause => cause.response?.data?.message || cause.response?.data?.detail || cause.response?.data?.error || '요청을 처리하지 못했습니다.'
async function load() { await groupStore.fetchGroups().catch(cause => { error.value = explain(cause) }) }
async function join() { busy.value = true; error.value = ''; try { const res = await groupApi.join(inviteCode.value); const group = res.data?.data; await groupStore.fetchGroups(); message.value = `${group.name}에 참여했습니다.`; router.push(`/groups/${group.id}`) } catch (cause) { error.value = explain(cause) } finally { busy.value = false } }
async function create() { busy.value = true; error.value = ''; try { const res = await groupApi.create(newGroup); const group = res.data?.data; await groupStore.fetchGroups(); router.push(`/groups/${group.id}`) } catch (cause) { error.value = explain(cause) } finally { busy.value = false } }
onMounted(load)
</script>

<style scoped>
.group-layout { display: grid; grid-template-columns: minmax(0,1fr) 310px; gap: 22px; align-items: start; }.group-grid { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 14px; }.group-card { position: relative; display: grid; grid-template-columns: 52px 1fr; gap: 15px; min-height: 145px; padding: 21px; transition: var(--transition); }.group-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-md); border-color: #a9c9bd; }.group-symbol { width: 52px; height: 52px; display: grid; place-items: center; color: #fff; background: #0b6b57; border-radius: 15px; font-size: 20px; font-weight: 800; }.tone-1{background:#38598b}.tone-2{background:#8a5b35}.tone-3{background:#6c4f8f}.role { color: var(--color-primary); font-size: 8px; font-weight: 800; letter-spacing: .12em; }.group-card h2 { margin-top: 3px; color: var(--color-navy); font-size: 17px; }.group-card p { margin-top: 5px; color: var(--color-text-secondary); font-size: 11px; }.arrow { position: absolute; right: 18px; bottom: 15px; color: var(--color-primary); }.join-panel { position: sticky; top: 90px; padding: 24px; }.panel-icon { width: 40px; height: 40px; display: grid; place-items: center; color: var(--color-primary); background: var(--color-primary-light); border-radius: 11px; font-weight: 800; }.join-panel h3 { margin-top: 14px; color: var(--color-navy); font-size: 16px; }.join-panel > p { margin: 5px 0 15px; color: var(--color-text-secondary); font-size: 11px; }.join-panel form { display: flex; flex-direction: column; gap: 9px; }.code-input { text-transform: uppercase; letter-spacing: .12em; }.divider { display: flex; align-items: center; gap: 8px; margin: 20px 0 12px; color: var(--color-text-muted); font-size: 9px; }.divider::before,.divider::after{content:'';height:1px;flex:1;background:var(--color-border)}.create-form { margin-top: 10px; }.create-form .form-textarea { min-height: 76px; }.feedback { margin-top: 11px; }.empty-state { min-height: 330px; }
@media(max-width:850px){.group-layout{grid-template-columns:1fr}.join-panel{position:static}.group-grid{grid-template-columns:1fr}}@media(max-width:520px){.group-card{grid-template-columns:42px 1fr;padding:17px}.group-symbol{width:42px;height:42px}}
</style>
