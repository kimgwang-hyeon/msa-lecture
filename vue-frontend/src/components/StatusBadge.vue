<template>
  <span :class="['request-status-badge', `tone-${meta.tone}`]">
    <span class="status-dot" aria-hidden="true"></span>
    {{ meta.label }}
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { statusMeta } from '@/utils/requestStatus.js'

const props = defineProps({
  status: { type: String, default: '' }
})

const meta = computed(() => statusMeta(props.status))
</script>

<style scoped>
.request-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  width: fit-content;
  padding: 5px 9px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 750;
  line-height: 1;
}
.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}
.tone-pending { color: var(--color-warning); background: var(--color-warning-light); }
.tone-active, .tone-complete { color: var(--color-success); background: var(--color-success-light); }
.tone-info { color: var(--color-info); background: var(--color-info-light); }
.tone-rejected { color: var(--color-danger); background: var(--color-danger-light); }
.tone-muted { color: #64717b; background: #eef1f3; }
</style>
