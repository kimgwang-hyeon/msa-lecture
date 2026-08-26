<template>
  <div class="login-page">
    <div class="login-layout">
      <!-- 좌측 브랜딩 -->
      <div class="login-left">
        <div class="brand">
          <span class="brand-logo">G</span>
          <span class="brand-name">GearHub Campus</span>
        </div>
        <div class="brand-content">
          <h2>조직 장비 운영을<br>더 간단하게</h2>
          <p>학교와 소속 그룹의 자산을 찾고 대여·반납·도입 현황을 확인하세요.</p>
          <ul class="feature-list">
            <li v-for="f in features" :key="f">
              <span class="dot"></span>{{ f }}
            </li>
          </ul>
        </div>
      </div>

      <!-- 우측 -->
      <div class="login-right">
        <div class="login-box fade-in-up">
          <router-link to="/" class="back-link">← 홈으로</router-link>

          <!-- 로그인 영역 -->
          <div v-if="!showRegister" class="section">
            <h3 class="section-title">로그인</h3>
            <p class="section-desc">학교 계정으로 로그인해 소속 그룹을 선택합니다.</p>
            <div v-if="expired" class="session-notice" role="status">
              안전을 위해 로그인 시간이 만료되었습니다. 다시 로그인해 주세요.
            </div>
            <button class="btn btn-primary btn-full" @click="handleOAuth">로그인</button>
            <div class="switch-link">
              계정이 없으신가요?
              <button class="text-btn" @click="showRegister = true">회원가입</button>
            </div>
          </div>

          <!-- 회원가입 영역 -->
          <div v-else class="section">
            <h3 class="section-title">회원가입</h3>
            <form @submit.prevent="handleRegister" class="form">
              <div class="form-group">
                <label class="form-label">이름</label>
                <input v-model="registerForm.name" type="text" class="form-input" placeholder="홍길동" required />
              </div>
              <div class="form-group">
                <label class="form-label">이메일</label>
                <input v-model="registerForm.email" type="email" class="form-input" placeholder="user@example.com" required />
              </div>
              <div class="form-group">
                <label class="form-label">비밀번호</label>
                <input v-model="registerForm.password" type="password" class="form-input" placeholder="8자 이상" required />
              </div>
              <div class="form-group">
                <label class="form-label">역할</label>
                <select v-model="registerForm.role" class="form-input">
                  <option value="STUDENT">구성원(교육생)</option>
                  <option value="INSTRUCTOR">자산 운영자(운영진)</option>
                </select>
              </div>
              <div v-if="error" class="error-msg">{{ error }}</div>
              <div v-if="success" class="success-msg">{{ success }}</div>
              <button type="submit" class="btn btn-primary btn-full" :disabled="loading">
                <span v-if="loading">가입 중...</span>
                <span v-else>회원가입</span>
              </button>
            </form>
            <div class="switch-link">
              이미 계정이 있으신가요?
              <button class="text-btn" @click="showRegister = false">로그인</button>
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'
import { authApi } from '@/api/auth.js'

const auth = useAuthStore()
const route = useRoute()

const showRegister = ref(false)
const loading = ref(false)
const error = ref('')
const success = ref('')
const expired = computed(() => route.query.expired === '1')

const registerForm = ref({ name: '', email: '', password: '', role: 'STUDENT' })

const features = ['학교 공용·그룹 전용 자산 조회', '대여 승인과 반납 확인', '관리자용 AI 수요예측']

function handleOAuth() {
  auth.redirectToLogin(route.query.redirect || '/groups')
}

async function handleRegister() {
  error.value = ''
  success.value = ''
  loading.value = true
  try {
    await authApi.register(registerForm.value)
    success.value = '회원가입 완료! 로그인 페이지로 이동합니다.'
    registerForm.value = { name: '', email: '', password: '', role: 'STUDENT' }
    setTimeout(() => {
      showRegister.value = false
      success.value = ''
    }, 2000)
  } catch (e) {
    error.value = e.response?.data?.message || '회원가입에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: stretch;
}
.session-notice {
  margin-bottom: 16px;
  padding: 12px 14px;
  color: var(--color-warning);
  background: var(--color-warning-light);
  border: 1px solid #efd5a3;
  border-radius: var(--radius-md);
  font-size: 13px;
  line-height: 1.55;
}
.login-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  width: 100%;
  min-height: 100vh;
}
.login-left {
  background: linear-gradient(160deg, #102a43 0%, #0b4f49 58%, #0b6b57 100%);
  padding: 48px;
  display: flex;
  flex-direction: column;
  gap: 48px;
}
.brand { display: flex; align-items: center; gap: 10px; }
.brand-logo { width: 40px; height: 40px; display: grid; place-items: center; color: #0b6b57; background: #fff; border-radius: 12px 12px 5px 12px; font-size: 18px; font-weight: 800; }
.brand-name { font-size: 18px; font-weight: 700; color: #fff; }
.brand-content h2 {
  font-size: 32px; font-weight: 700; color: #fff;
  line-height: 1.35; margin-bottom: 14px;
}
.brand-content p { font-size: 15px; color: rgba(255,255,255,0.75); margin-bottom: 28px; }
.feature-list { list-style: none; display: flex; flex-direction: column; gap: 12px; }
.feature-list li { display: flex; align-items: center; gap: 10px; font-size: 14px; color: rgba(255,255,255,0.85); }
.dot { width: 7px; height: 7px; border-radius: 50%; background: rgba(255,255,255,0.6); flex-shrink: 0; }

.login-right {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: var(--color-bg-primary);
}
.login-box { width: 100%; max-width: 400px; }
.back-link {
  display: inline-block;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 32px;
  transition: var(--transition);
}
.back-link:hover { color: var(--color-primary); }

.section { display: flex; flex-direction: column; gap: 16px; }
.section-title { font-size: 22px; font-weight: 700; color: var(--color-text-primary); margin-bottom: 4px; }
.section-desc { font-size: 14px; color: var(--color-text-secondary); margin-bottom: 4px; }

.form { display: flex; flex-direction: column; gap: 14px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-label { font-size: 13px; font-weight: 500; color: var(--color-text-secondary); }
.form-input {
  padding: 10px 14px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  font-family: var(--font-sans);
  color: var(--color-text-primary);
  background: var(--color-bg-primary);
  transition: var(--transition);
  outline: none;
}
.form-input:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px var(--color-primary-light); }
.btn-full { width: 100%; padding: 12px; font-size: 15px; justify-content: center; margin-top: 4px; }

.switch-link {
  text-align: center;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}
.text-btn {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  padding: 0 2px;
  text-decoration: underline;
}
.error-msg {
  padding: 10px 14px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-md);
  font-size: 13px;
  color: #dc2626;
}
.success-msg {
  padding: 10px 14px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: var(--radius-md);
  font-size: 13px;
  color: #16a34a;
}
</style>
