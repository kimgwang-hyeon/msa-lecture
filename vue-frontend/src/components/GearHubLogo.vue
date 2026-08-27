<template>
  <span class="gearhub-logo" :class="{ 'symbol-only': !showWordmark }" aria-hidden="true">
    <span class="gearhub-mark">
      <svg viewBox="0 0 48 48" fill="none">
        <path class="network-edge" d="m24 24-13-11m13 11 14-12M24 24l16 8M24 24l5 15M24 24 9 36M11 13l27-1m2 20L29 39M9 36l20 3" />
        <circle class="network-node" cx="11" cy="13" r="3.5" />
        <circle class="network-node" cx="38" cy="12" r="3.5" />
        <circle class="network-node" cx="40" cy="32" r="3.5" />
        <circle class="network-node" cx="29" cy="39" r="3.5" />
        <circle class="network-node" cx="9" cy="36" r="3.5" />
        <circle class="network-core" cx="24" cy="24" r="6" />
        <circle class="network-core-dot" cx="24" cy="24" r="2" />
      </svg>
    </span>
    <span v-if="showWordmark" class="gearhub-wordmark">
      <strong>GearHub</strong>
      <small>CAMPUS</small>
    </span>
  </span>
</template>

<script setup>
defineProps({
  showWordmark: { type: Boolean, default: true }
})
</script>

<style scoped>
.gearhub-logo {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  color: var(--color-primary, #356d63);
  line-height: 1;
}
.gearhub-mark {
  width: 39px;
  height: 39px;
  flex: 0 0 39px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, .82);
  border: 1px solid color-mix(in srgb, var(--color-primary, #356d63) 22%, transparent);
  border-radius: 12px;
  box-shadow: 0 7px 20px rgba(40, 70, 92, .08);
}
.gearhub-mark svg {
  width: 29px;
  height: 29px;
  overflow: visible;
}
.network-edge {
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
  opacity: .68;
}
.network-node {
  fill: #fff;
  stroke: currentColor;
  stroke-width: 2;
}
.network-core {
  fill: #fff;
  stroke: currentColor;
  stroke-width: 2.2;
}
.network-core-dot { fill: currentColor; }
.gearhub-wordmark {
  display: flex;
  flex-direction: column;
}
.gearhub-wordmark strong {
  color: var(--color-navy, #202632);
  font-family: var(--font-display, 'Plus Jakarta Sans', 'Noto Sans KR', sans-serif);
  font-size: 15px;
  font-weight: 700;
  letter-spacing: -.025em;
}
.gearhub-wordmark small {
  margin-top: 4px;
  color: var(--color-primary, #356d63);
  font-family: var(--font-display, 'Plus Jakarta Sans', 'Noto Sans KR', sans-serif);
  font-size: 9px;
  font-weight: 800;
  letter-spacing: .16em;
}
.symbol-only { gap: 0; }

@media (max-width: 390px) {
  .gearhub-wordmark { display: none; }
}
</style>
