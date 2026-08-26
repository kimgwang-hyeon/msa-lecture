<template>
  <div class="landing">
    <AppHeader />

    <main>
      <section class="hero">
        <div class="container hero-grid">
          <div class="hero-copy fade-in-up">
            <span class="eyebrow"><i></i> CAMPUS ASSET NETWORK</span>
            <h1>학교와 그룹의 장비를<br><em>함께 빌리고 운영하세요.</em></h1>
            <p>학과·연구실·동아리마다 별도 워크스페이스를 만들고, 학교 공용 자산과 그룹 전용 자산의 대여·반납·도입을 연결합니다.</p>
            <div class="hero-actions">
              <router-link :to="auth.isAuthenticated ? '/groups' : '/login'" class="btn btn-primary btn-lg">
                {{ auth.isAuthenticated ? '내 그룹 들어가기' : 'GearHub 시작하기' }}
              </router-link>
              <router-link v-if="auth.isAuthenticated" to="/groups" class="btn btn-outline btn-lg">그룹 선택</router-link>
            </div>
            <div class="trust-row">
              <span>✓ 그룹별 권한</span><span>✓ 대여·반납 추적</span><span>✓ AI 수요예측</span>
            </div>
          </div>

          <div class="workflow-card fade-in-up">
            <div class="workflow-head">
              <div><small>오늘의 GearHub</small><strong>조직 자산 운영 현황</strong></div>
              <span class="live-dot">운영 중</span>
            </div>
            <div class="workflow-list">
              <div v-for="(item, index) in workflow" :key="item.title" class="workflow-item">
                <span class="step">0{{ index + 1 }}</span>
                <span class="workflow-icon">{{ item.icon }}</span>
                <div><strong>{{ item.title }}</strong><small>{{ item.desc }}</small></div>
                <span class="arrow">→</span>
              </div>
            </div>
            <div class="workflow-foot"><span>MSA 기반 자산 흐름</span><strong>5 services connected</strong></div>
          </div>
        </div>
      </section>

      <section class="category-section">
        <div class="container">
          <div class="section-heading">
            <div><span class="eyebrow">RESOURCE CATEGORIES</span><h2>팀과 전공을 넘나드는 업무·교육 장비</h2></div>
            <router-link :to="auth.isAuthenticated ? '/groups' : '/login'" class="text-link">그룹에서 자산 보기 →</router-link>
          </div>
          <div class="category-grid">
            <article v-for="item in categories" :key="item.title" class="category-card">
              <span>{{ item.icon }}</span><div><h3>{{ item.title }}</h3><p>{{ item.desc }}</p></div>
            </article>
          </div>
        </div>
      </section>

      <section class="why-section">
        <div class="container why-grid">
          <div class="why-copy">
            <span class="eyebrow">WHY GEARHUB</span>
            <h2>감이 아니라 데이터로,<br>다음 4주의 장비 수요를.</h2>
            <p>18개월 요청 이력을 학습하고 단순 이동평균과 scikit-learn 모델을 비교해 관리자에게 부족 재고와 그룹 간 이동 대안을 보여줍니다.</p>
          </div>
          <div class="benefit-list">
            <div v-for="benefit in benefits" :key="benefit.title" class="benefit">
              <span>{{ benefit.number }}</span><div><strong>{{ benefit.title }}</strong><p>{{ benefit.desc }}</p></div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <footer><div class="container"><strong>GearHub Campus</strong><span>Multi-group Asset Operations · Agile MSA · Applied ML</span></div></footer>
  </div>
</template>

<script setup>
import AppHeader from '@/components/AppHeader.vue'
import { useAuthStore } from '@/store/auth.js'

const auth = useAuthStore()
const workflow = [
  { icon: '⌂', title: '그룹 워크스페이스', desc: '학과·연구실·동아리별 공간' },
  { icon: '↔', title: '대여와 반납', desc: '기간·승인·재고를 함께 추적' },
  { icon: '✦', title: '관리자 수요예측', desc: '4주 부족 수량과 이동 대안' }
]
const categories = [
  { icon: '▣', title: '스마트기기', desc: 'iPhone, Android, 태블릿' },
  { icon: '⌘', title: '컴퓨터', desc: '노트북, 워크스테이션' },
  { icon: '⌁', title: '전자·IoT', desc: '센서, Arduino, Raspberry Pi' },
  { icon: '△', title: '메이커·건축', desc: '측정 도구, 제작 장비' }
]
const benefits = [
  { number: '01', title: '재고가 보이면 바로 대여', desc: '자산 운영자가 보유 수량과 신청을 같은 흐름에서 관리합니다.' },
  { number: '02', title: '도입 요청은 사람의 근거로', desc: '구성원이 필요성을 설명하고 그룹 관리자와 학교 관리자가 순서대로 검토합니다.' },
  { number: '03', title: 'AI는 관리자 의사결정에', desc: '기준선과 모델 성능을 공개하고 수요·재고·대여기간을 함께 비교합니다.' }
]
</script>

<style scoped>
.landing { background: #fff; }
.hero { position: relative; overflow: hidden; padding: 86px 0 82px; background: linear-gradient(135deg, #f2f8f5 0%, #fff 58%, #fff8eb 100%); }
.hero::after { content: ''; position: absolute; width: 430px; height: 430px; right: -170px; top: -210px; border: 70px solid rgba(11,107,87,.055); border-radius: 50%; }
.hero-grid { position: relative; z-index: 1; display: grid; grid-template-columns: 1.08fr .92fr; align-items: center; gap: 70px; }
.eyebrow i { width: 7px; height: 7px; background: var(--color-accent); border-radius: 50%; }
.hero h1 { color: var(--color-navy); font-size: clamp(40px, 4.6vw, 59px); line-height: 1.16; letter-spacing: -.06em; word-break: keep-all; }
.hero h1 em { color: var(--color-primary); font-style: normal; }
.hero-copy > p { max-width: 570px; margin-top: 22px; color: var(--color-text-secondary); font-size: 16px; line-height: 1.8; }
.hero-actions { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 30px; }
.trust-row { display: flex; flex-wrap: wrap; gap: 18px; margin-top: 27px; color: #60746c; font-size: 11px; font-weight: 600; }
.workflow-card { padding: 26px; background: rgba(255,255,255,.92); border: 1px solid #d9e6e0; border-radius: 26px; box-shadow: var(--shadow-lg); }
.workflow-head, .workflow-foot { display: flex; align-items: center; justify-content: space-between; }
.workflow-head div { display: flex; flex-direction: column; }
.workflow-head small { color: var(--color-text-muted); font-size: 10px; }
.workflow-head strong { margin-top: 2px; color: var(--color-navy); font-size: 17px; }
.live-dot { color: var(--color-success); background: var(--color-success-light); border-radius: 999px; padding: 5px 9px; font-size: 10px; font-weight: 700; }
.workflow-list { display: flex; flex-direction: column; gap: 10px; margin: 23px 0; }
.workflow-item { display: grid; grid-template-columns: 26px 38px 1fr auto; align-items: center; gap: 11px; padding: 13px; background: #f7f9f8; border: 1px solid #e6ece9; border-radius: 13px; }
.step { color: #a0aaa5; font-size: 9px; font-weight: 800; }
.workflow-icon { width: 36px; height: 36px; display: grid; place-items: center; color: var(--color-primary); background: var(--color-primary-light); border-radius: 10px; font-size: 17px; }
.workflow-item div { display: flex; flex-direction: column; }
.workflow-item strong { font-size: 12px; }
.workflow-item small { color: var(--color-text-muted); font-size: 10px; }
.arrow { color: #9caaa4; }
.workflow-foot { padding-top: 15px; border-top: 1px solid var(--color-border); color: var(--color-text-muted); font-size: 9px; }
.workflow-foot strong { color: var(--color-primary); text-transform: uppercase; letter-spacing: .05em; }
.category-section { padding: 80px 0; }
.section-heading { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 28px; }
.section-heading h2, .why-copy h2 { color: var(--color-navy); font-size: clamp(27px, 3vw, 38px); line-height: 1.25; letter-spacing: -.04em; }
.text-link { color: var(--color-primary); font-size: 12px; font-weight: 700; }
.category-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.category-card { display: flex; align-items: center; gap: 15px; padding: 21px; background: #f7f9f8; border: 1px solid var(--color-border); border-radius: 16px; }
.category-card > span { width: 43px; height: 43px; flex: 0 0 43px; display: grid; place-items: center; color: var(--color-primary); background: #fff; border-radius: 12px; box-shadow: var(--shadow-sm); font-size: 21px; }
.category-card h3 { font-size: 13px; }
.category-card p { margin-top: 3px; color: var(--color-text-muted); font-size: 10px; line-height: 1.5; }
.why-section { padding: 82px 0; color: #fff; background: var(--color-navy); }
.why-grid { display: grid; grid-template-columns: .85fr 1.15fr; gap: 90px; align-items: center; }
.why-copy .eyebrow { color: #72dbc0; }
.why-copy h2 { color: #fff; }
.why-copy p { margin-top: 18px; color: #aebfcb; font-size: 14px; line-height: 1.8; }
.benefit-list { display: flex; flex-direction: column; }
.benefit { display: grid; grid-template-columns: 42px 1fr; gap: 16px; padding: 19px 0; border-bottom: 1px solid rgba(255,255,255,.11); }
.benefit > span { color: #72dbc0; font-size: 11px; font-weight: 800; }
.benefit strong { font-size: 14px; }
.benefit p { margin-top: 4px; color: #aebfcb; font-size: 11px; }
footer { padding: 24px 0; background: #0b1f30; color: #8fa3b2; }
footer .container { display: flex; justify-content: space-between; font-size: 10px; }
footer strong { color: #fff; font-size: 12px; }
@media (max-width: 850px) {
  .hero { padding: 56px 0; }
  .hero-grid, .why-grid { grid-template-columns: 1fr; gap: 42px; }
  .category-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 520px) {
  .category-grid { grid-template-columns: 1fr; }
  .section-heading { align-items: flex-start; flex-direction: column; gap: 12px; }
}
</style>
