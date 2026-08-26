<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link :to="auth.isAuthenticated ? '/groups' : '/'" class="brand" aria-label="GearHub Campus 홈">
        <span class="brand-mark">G</span>
        <span class="brand-copy"><strong>GearHub</strong><small>CAMPUS</small></span>
      </router-link>

      <nav v-if="auth.isAuthenticated" class="nav-links">
        <router-link to="/groups" class="nav-link">그룹</router-link>
        <template v-if="groupId">
          <router-link :to="groupPath('')" class="nav-link" exact-active-class="router-link-active">홈</router-link>
          <router-link :to="groupPath('/assets')" class="nav-link">자산 찾기</router-link>
          <router-link :to="groupPath('/loans')" class="nav-link">내 요청</router-link>
          <router-link v-if="isManager" :to="groupPath('/admin')" class="nav-link">운영 데스크</router-link>
          <router-link v-if="isManager" :to="groupPath('/analytics')" class="nav-link ai-link">AI 수요예측</router-link>
        </template>
      </nav>

      <div class="header-actions">
        <select v-if="auth.isAuthenticated && groups.length" :value="groupId || ''" class="group-select" @change="changeGroup">
          <option value="">그룹 선택</option>
          <option v-for="group in groups" :key="group.id" :value="group.id">{{ group.name }}</option>
        </select>
        <template v-if="auth.isAuthenticated">
          <router-link to="/mypage" class="profile-link">
            <span class="role-pill">{{ auth.isInstructor ? '학교 관리자' : (isManager ? '그룹 관리자' : '구성원') }}</span>
            <span class="avatar">{{ auth.user?.name?.charAt(0) || '?' }}</span>
          </router-link>
          <button class="logout-btn" @click="handleLogout">로그아웃</button>
        </template>
        <router-link v-else to="/login" class="btn btn-primary btn-sm">로그인</router-link>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'
import { useGroupStore } from '@/store/group.js'

const auth = useAuthStore()
const groupStore = useGroupStore()
const route = useRoute()
const router = useRouter()
const groupId = computed(() => Number(route.params.groupId) || null)
const groups = computed(() => groupStore.groups)
const isManager = computed(() => auth.isInstructor || groupStore.currentGroup?.currentRole === 'MANAGER')

const groupPath = suffix => `/groups/${groupId.value}${suffix}`

async function syncGroup(id) {
  if (!auth.isAuthenticated) return
  if (!groupStore.groups.length) await groupStore.fetchGroups().catch(() => {})
  if (id) await groupStore.loadGroup(id).catch(() => {})
}

function changeGroup(event) {
  const id = Number(event.target.value)
  if (id) router.push(`/groups/${id}`)
  else router.push('/groups')
}

function handleLogout() {
  groupStore.clear()
  auth.logout(false)
  router.push('/')
}

onMounted(() => syncGroup(groupId.value))
watch(groupId, id => syncGroup(id))
</script>

<style scoped>
.app-header { position: sticky; top: 0; z-index: 100; background: rgba(255,255,255,.95); border-bottom: 1px solid rgba(213,225,220,.9); backdrop-filter: blur(14px); }
.header-inner { width: min(1220px, calc(100% - 36px)); height: 68px; margin: 0 auto; display: flex; align-items: center; gap: 22px; }
.brand { display: flex; align-items: center; gap: 9px; flex-shrink: 0; }
.brand-mark { width: 38px; height: 38px; display: grid; place-items: center; color: #fff; background: linear-gradient(145deg, #0b6b57, #064e42); border-radius: 12px 12px 5px 12px; font-size: 17px; font-weight: 800; box-shadow: 0 7px 16px rgba(11,107,87,.2); }
.brand-copy { display: flex; flex-direction: column; line-height: 1; }
.brand-copy strong { color: var(--color-navy); font-size: 15px; letter-spacing: -.02em; }
.brand-copy small { margin-top: 4px; color: var(--color-primary); font-size: 8px; font-weight: 800; letter-spacing: .16em; }
.nav-links { display: flex; align-items: center; gap: 2px; flex: 1; }
.nav-link { padding: 8px 9px; border-radius: 9px; color: var(--color-text-secondary); font-size: 12px; font-weight: 650; white-space: nowrap; }
.nav-link:hover, .nav-link.router-link-active { color: var(--color-primary); background: var(--color-primary-light); }
.ai-link::before { content: '✦'; margin-right: 4px; color: #8a5bd1; }
.header-actions { margin-left: auto; display: flex; align-items: center; gap: 8px; }
.group-select { max-width: 155px; height: 34px; padding: 0 28px 0 9px; color: var(--color-text-secondary); background: #f5f8f6; border: 1px solid var(--color-border); border-radius: 9px; font-size: 10px; font-weight: 650; }
.profile-link { display: flex; align-items: center; gap: 7px; }
.role-pill { color: var(--color-primary); background: var(--color-primary-light); border-radius: 999px; padding: 3px 7px; font-size: 9px; font-weight: 750; }
.avatar { width: 31px; height: 31px; display: grid; place-items: center; color: #fff; background: var(--color-navy); border-radius: 50%; font-size: 12px; font-weight: 700; }
.logout-btn { color: var(--color-text-muted); background: transparent; border: 0; font-size: 10px; }
.logout-btn:hover { color: var(--color-danger); }
@media (max-width: 940px) { .brand-copy, .role-pill, .logout-btn { display: none; } .header-inner { gap: 10px; } .nav-links { overflow-x: auto; } .group-select { display: none; } }
@media (max-width: 620px) { .nav-link:first-child, .nav-link:nth-child(2) { display: none; } .header-inner { width: calc(100% - 22px); } }
</style>
