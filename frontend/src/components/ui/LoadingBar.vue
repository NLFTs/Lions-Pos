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
      
      <!-- Bright glow effect for better visibility -->
      <div 
        class="loading-bar-glow"
        :style="{ left: `${state.progress}%` }"
      ></div>
    </div>
  </Transition>
  
  <!-- Subtle overlay to dim page during transition -->
  <Transition name="overlay-fade">
    <div 
      v-if="state.active && state.progress < 99"
      class="loading-overlay"
    ></div>
  </Transition>
</template>

<style scoped>
.loading-bar-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  z-index: 9998;
  pointer-events: none;
  background: rgba(0, 0, 0, 0.05);
}

.loading-bar {
  height: 100%;
  background: linear-gradient(90deg, hsl(var(--primary)), hsl(var(--primary) / 0.8));
  transition: width 0.3s cubic-bezier(0.1, 0.5, 0.5, 1);
  box-shadow: 0 0 15px hsl(var(--primary) / 0.8), 0 0 8px hsl(var(--primary) / 0.5);
}

.loading-bar.is-error {
  background: hsl(var(--destructive));
  box-shadow: 0 0 15px hsl(var(--destructive) / 0.8), 0 0 8px hsl(var(--destructive) / 0.5);
}

.loading-bar-glow {
  position: absolute;
  top: -2px;
  width: 150px;
  height: 8px;
  background: linear-gradient(to right, transparent, hsl(var(--primary) / 0.5), transparent);
  filter: blur(4px);
  transform: translateX(-150px);
  transition: left 0.3s cubic-bezier(0.1, 0.5, 0.5, 1);
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9997;
  background: rgba(0, 0, 0, 0.02);
  backdrop-filter: blur(0.5px);
  pointer-events: none;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.overlay-fade-enter-active,
.overlay-fade-leave-active {
  transition: opacity 0.25s ease;
}

.overlay-fade-enter-from,
.overlay-fade-leave-to {
  opacity: 0;
}
</style>
