<script setup>
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import NotificationProvider from '@/components/ui/NotificationProvider.vue'
import LoadingBar from '@/components/ui/LoadingBar.vue'
import { initLenis, getLenis } from '@/lib/lenis'

const route = useRoute()

onMounted(() => {
  // Initialize Lenis globally
  initLenis()
})

// Scroll to top on route change (Lenis way)
watch(() => route.fullPath, () => {
  const lenis = getLenis()
  if (lenis) {
    lenis.scrollTo(0, { immediate: true })
  }
})
</script>

<template>
  <LoadingBar />
  <RouterView :key="$route.fullPath" />
  <NotificationProvider />
</template>
