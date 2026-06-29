<script setup>
import { onMounted, watch, ref } from 'vue'
import { useRoute } from 'vue-router'
import NotificationProvider from '@/components/ui/NotificationProvider.vue'
import LoadingBar from '@/components/ui/LoadingBar.vue'
import { initLenis, getLenis, destroyLenis } from '@/lib/lenis'

const route = useRoute()
const isTransitioning = ref(false)
const transitionTimeout = ref(null)

const handleLenis = (path) => {
  const isDashboard = path.startsWith('/dashboard')
  const existingLenis = getLenis()

  if (isDashboard) {
    if (existingLenis) {
      destroyLenis()
    }
    // Ensure html and body are not locked by Lenis
    document.documentElement.classList.remove('lenis')
    document.body.classList.remove('lenis')
  } else {
    if (!existingLenis) {
      initLenis()
    }
  }
}

onMounted(() => {
  handleLenis(route.path)
})

// Handle lenis on route change (scroll library for landing pages)
watch(() => route.path, (newPath) => {
  // Start transition state
  isTransitioning.value = true
  
  if (transitionTimeout.value) clearTimeout(transitionTimeout.value)
  transitionTimeout.value = setTimeout(() => {
    isTransitioning.value = false
  }, 400) // Match transition duration
  
  handleLenis(newPath)

  const lenis = getLenis()
  if (lenis) {
    lenis.scrollTo(0, { immediate: true })
  } else {
    // For dashboard pages, use requestAnimationFrame for smooth scroll reset
    requestAnimationFrame(() => {
      const mainEl = document.querySelector('main.overflow-y-auto')
      if (mainEl) {
        mainEl.scrollTop = 0
      }
    })
  }
})
</script>

<template>
  <LoadingBar />
  <!-- Dark background container to prevent white flash -->
  <div class="app-bg-container">
    <!-- PENTING: Jangan gunakan :key="$route.fullPath" di sini karena akan
         menyebabkan seluruh komponen halaman di-destroy & recreate setiap
         navigasi → layar hitam / blank setiap pindah menu. -->
    <Transition name="page-fade" mode="out-in">
      <RouterView />
    </Transition>
  </div>
  <NotificationProvider />
</template>

<style scoped>
.app-bg-container {
  background-color: rgb(3, 7, 18) !important;
  min-height: 100vh;
  min-width: 100vw;
  display: flex;
  flex-direction: column;
}

/* Smooth page transition - longer duration */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}

/* Force dark background during transition */
:deep(div:has(> .fade-enter-active)) {
  background-color: rgb(3, 7, 18) !important;
}
</style>
