<script setup>
import { useRouter } from 'vue-router'
import LandingLayout from '@/components/section/landingpage/LandingLayout.vue'
import Navbar from '@/components/section/landingpage/Navbar.vue'
import Button from '@/components/ui/Button.vue'
import { Zap, Sun, Moon } from 'lucide-vue-next'
import { useGsap } from '@/hooks/useGsap'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const themeStore = useThemeStore()

// GSAP Structure
useGsap((gsap, ScrollTrigger) => {
  // GSAP animations can be added here
  // For example:
  // gsap.from('.hero-title', { opacity: 0, y: 30, duration: 1, ease: 'power3.out' })
})

// Configuration for Landing Page
const brandName = 'GAPETK'

const navigationItems = [
  { name: 'Home', path: '/' },
  { name: 'Features', path: '#features' },
  { name: 'About', path: '#about' },
  { name: 'Pricing', path: '#pricing' }
]

const handleNavigation = (path) => {
  if (path.startsWith('#')) {
    const el = document.querySelector(path)
    if (el) el.scrollIntoView({ behavior: 'smooth' })
  } else {
    router.push(path)
  }
}

const navigateToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <LandingLayout>
    <!-- Navbar Section -->
    <Navbar 
      :brand-name="brandName" 
      :nav-items="navigationItems"
      @navigate="handleNavigation"
    >
      <template #logo>
        <Zap class="h-6 w-6 text-primary-foreground transition-colors duration-300" />
      </template>

      <template #actions>
        <!-- Theme Toggle -->
        <Button 
          variant="ghost" 
          size="icon" 
          class="text-zinc-600 dark:text-zinc-300 hover:text-primary transition-colors duration-300 rounded-full sm:mr-2 h-8 w-8 sm:h-10 sm:w-10" 
          @click="themeStore.toggleDark()"
        >
          <Sun v-if="!themeStore.isDark" class="h-4 w-4 sm:h-5 sm:w-5" />
          <Moon v-else class="h-4 w-4 sm:h-5 sm:w-5" />
        </Button>

        <Button variant="ghost" class="hidden sm:inline-flex font-bold text-zinc-600 dark:text-zinc-300 hover:text-primary transition-colors duration-300" @click="navigateToLogin">
          Masuk
        </Button>
        <Button class="font-bold px-3 sm:px-6 py-1.5 sm:py-2 text-xs sm:text-base rounded-xl shadow-lg shadow-primary/20 transition-all active:scale-95" @click="navigateToLogin">
          Mulai Gratis
        </Button>
      </template>
    </Navbar>

    <!-- Content Sections -->
    <main class="flex-grow flex flex-col items-center justify-center text-center px-8 py-32">
      <div class="space-y-8 max-w-4xl mx-auto">
        <h1 class="text-5xl md:text-7xl font-black font-lexend tracking-tighter text-zinc-900 dark:text-zinc-100 leading-[1.1] transition-colors duration-300">
          Ready to Build <br class="md:hidden" />
          <span class="text-primary inline-block transition-colors duration-300">Something Great?</span>
        </h1>
        <p class="text-lg md:text-xl text-zinc-500 dark:text-zinc-400 font-medium max-w-2xl mx-auto leading-relaxed transition-colors duration-300">
          Struktur layout, GSAP, dan Smooth Scroll telah siap. <br />
          Silakan tambahkan section berikutnya sesuai kebutuhan.
        </p>
      </div>
    </main>
  </LandingLayout>
</template>

<style scoped>
/* No styles here as per requirement */
</style>
