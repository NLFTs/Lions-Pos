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
const brandName = 'Spravel'

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
        <Zap class="h-6 w-6 text-white" />
      </template>

      <template #actions>
        <!-- Theme Toggle -->
        <Button 
          variant="ghost" 
          size="icon" 
          class="text-muted-foreground hover:text-primary transition-colors rounded-full mr-2" 
          @click="themeStore.toggleDark()"
        >
          <Sun v-if="!themeStore.isDark" class="h-5 w-5" />
          <Moon v-else class="h-5 w-5" />
        </Button>

        <Button variant="ghost" class="font-bold text-muted-foreground hover:text-primary transition-colors" @click="navigateToLogin">
          Masuk
        </Button>
        <Button class="bg-primary hover:bg-primary/90 text-white font-bold px-6 rounded-xl shadow-lg shadow-primary/20 transition-all active:scale-95" @click="navigateToLogin">
          Mulai Gratis
        </Button>
      </template>
    </Navbar>

    <!-- Content Sections -->
    <main class="flex-grow flex flex-col items-center justify-center text-center px-8 py-32">
      <div class="space-y-8 max-w-4xl mx-auto">
        <h1 class="text-5xl md:text-7xl font-black font-lexend tracking-tighter text-foreground leading-[1.1]">
          Ready to Build <br class="md:hidden" />
          <span class="text-primary inline-block">Something Great?</span>
        </h1>
        <p class="text-lg md:text-xl text-muted-foreground font-medium max-w-2xl mx-auto leading-relaxed">
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
