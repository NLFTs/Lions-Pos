<script setup>
import { useLoadingBar } from '@/composables/useLoadingBar'

const { state } = useLoadingBar()
</script>

<template>
  <Transition name="fade">
    <div 
      v-if="state.active"
      class="loading-bar-container"
    >
      <div 
        class="loading-bar"
        :class="{ 'is-error': state.error }"
        :style="{ width: `${state.progress}%` }"
      ></div>
      
      <!-- Subtle glow effect ala Youtube -->
      <div 
        class="loading-bar-glow"
        :style="{ left: `${state.progress}%` }"
      ></div>
    </div>
  </Transition>
</template>

<style scoped>
.loading-bar-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  z-index: 9999;
  pointer-events: none;
}

.loading-bar {
  height: 100%;
  background: hsl(var(--primary));
  transition: width 0.4s cubic-bezier(0.1, 0.5, 0.5, 1);
  box-shadow: 0 0 10px hsl(var(--primary) / 0.5);
}

.loading-bar.is-error {
  background: hsl(var(--destructive));
  box-shadow: 0 0 10px hsl(var(--destructive) / 0.5);
}

.loading-bar-glow {
  position: absolute;
  top: 0;
  width: 100px;
  height: 100%;
  background: linear-gradient(to right, transparent, hsl(var(--primary) / 0.3), transparent);
  transform: translateX(-100%);
  transition: left 0.4s cubic-bezier(0.1, 0.5, 0.5, 1);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
