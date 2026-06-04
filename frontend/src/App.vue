<script setup>
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import NotificationProvider from '@/components/ui/NotificationProvider.vue'
import LoadingBar from '@/components/ui/LoadingBar.vue'
import { initLenis, getLenis, destroyLenis } from '@/lib/lenis'

const route = useRoute()

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
  handleLenis(newPath)

  const lenis = getLenis()
  if (lenis) {
    lenis.scrollTo(0, { immediate: true })
  } else {
    // For dashboard pages, scroll the main content area
    requestAnimationFrame(() => {
      const mainEl = document.querySelector('main.overflow-y-auto')
      if (mainEl) mainEl.scrollTop = 0
    })
  }
})
</script>

<template>
  <LoadingBar />
  <!-- PENTING: Jangan gunakan :key="$route.fullPath" di sini karena akan
       menyebabkan seluruh komponen halaman di-destroy & recreate setiap
       navigasi → layar hitam / blank setiap pindah menu. -->
  <RouterView />
  <NotificationProvider />
</template>
