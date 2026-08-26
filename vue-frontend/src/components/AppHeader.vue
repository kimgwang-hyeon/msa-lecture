<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="brand" aria-label="SKALA GearHub 홈">
        <span class="brand-mark">S</span>
        <span class="brand-copy">
          <strong>GearHub</strong>
          <small>SKALA</small>
        </span>
      </router-link>

      <nav v-if="auth.isAuthenticated" class="nav-links">
        <router-link to="/courses" class="nav-link">교보재 찾기</router-link>
        <template v-if="!isOperator">
          <router-link to="/requests/new" class="nav-link">신규 교보재 신청</router-link>
          <router-link to="/enrollments" class="nav-link">내 신청</router-link>
        </template>
        <template v-else>
          <router-link to="/courses/new" class="nav-link">교보재 등록</router-link>
          <router-link to="/admin/approvals" class="nav-link">승인 관리</router-link>
        </template>
      </nav>

      <div class="header-actions">
        <template v-if="auth.isAuthenticated">
          <router-link to="/mypage" class="profile-link">
            <span class="role-pill">{{ isOperator ? '자산 운영자' : '구성원' }}</span>
            <span class="avatar">{{ auth.user?.name?.charAt(0) || '?' }}</span>
            <span class="profile-name">{{ auth.user?.name || '사용자' }}</span>
          </router-link>
          <button class="logout-btn" @click="handleLogout">로그아웃</button>
        </template>
        <router-link v-else to="/login" class="btn btn-primary btn-sm">로그인</router-link>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'

const auth = useAuthStore()
const router = useRouter()
const isOperator = computed(() => auth.user?.role === 'INSTRUCTOR')

function handleLogout() {
  auth.logout(false)
  router.push('/')
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255,255,255,.94);
  border-bottom: 1px solid rgba(213,225,220,.9);
  backdrop-filter: blur(14px);
}
.header-inner {
  width: min(1160px, calc(100% - 40px));
  height: 68px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 28px;
}
.brand { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.brand-mark {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  color: #fff;
  background: var(--color-primary);
  border-radius: 12px 12px 5px 12px;
  font-size: 18px;
  font-weight: 800;
  box-shadow: 0 7px 16px rgba(11,107,87,.2);
}
.brand-copy { display: flex; flex-direction: column; line-height: 1; }
.brand-copy strong { color: var(--color-navy); font-size: 16px; letter-spacing: -.02em; }
.brand-copy small { margin-top: 4px; color: var(--color-primary); font-size: 9px; font-weight: 800; letter-spacing: .18em; }
.nav-links { display: flex; align-items: center; gap: 3px; flex: 1; }
.nav-link { padding: 8px 11px; border-radius: 9px; color: var(--color-text-secondary); font-size: 13px; font-weight: 600; }
.nav-link:hover, .nav-link.router-link-active { color: var(--color-primary); background: var(--color-primary-light); }
.header-actions { margin-left: auto; display: flex; align-items: center; gap: 10px; }
.profile-link { display: flex; align-items: center; gap: 7px; }
.role-pill { color: var(--color-primary); background: var(--color-primary-light); border-radius: 999px; padding: 3px 7px; font-size: 10px; font-weight: 700; }
.avatar { width: 31px; height: 31px; display: grid; place-items: center; color: #fff; background: var(--color-navy); border-radius: 50%; font-size: 12px; font-weight: 700; }
.profile-name { max-width: 88px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 12px; font-weight: 700; }
.logout-btn { color: var(--color-text-muted); background: transparent; border: 0; font-size: 11px; }
.logout-btn:hover { color: var(--color-danger); }
@media (max-width: 820px) {
  .header-inner { width: calc(100% - 28px); gap: 12px; }
  .brand-copy, .profile-name, .role-pill, .logout-btn { display: none; }
  .nav-links { overflow-x: auto; }
  .nav-link { white-space: nowrap; padding: 7px 9px; font-size: 12px; }
}
</style>
