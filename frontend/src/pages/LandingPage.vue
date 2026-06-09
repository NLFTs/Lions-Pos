<script setup>
import { useRouter } from 'vue-router'
import LandingLayout from '@/components/section/landingpage/LandingLayout.vue'
import Navbar from '@/components/section/landingpage/Navbar.vue'
import Button from '@/components/ui/Button.vue'
import { Zap, Check } from 'lucide-vue-next'
import { useGsap } from '@/hooks/useGsap'
import GradientBlinds from '@/components/ui/background/GradientBlinds.vue'
import { onMounted, ref, defineAsyncComponent } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'

// Async components for better performance
const Steps = defineAsyncComponent(() => import('@/components/section/landingpage/Steps.vue'))
const Footer = defineAsyncComponent(() => import('@/components/section/landingpage/Footer.vue'))

const router = useRouter()
const authStore = useAuthStore()
const { isAuthenticated } = storeToRefs(authStore)
const showCookieBanner = ref(false)

onMounted(() => {
  const hasAccepted = localStorage.getItem('gaptek_cookies_accepted')
  if (!hasAccepted) {
    showCookieBanner.value = true
  }
})

const hideCookieBanner = () => {
  localStorage.setItem('gaptek_cookies_accepted', 'true')
  showCookieBanner.value = false
}

// Configuration for Landing Page
const brandName = 'gaptek'

const navigationItems = [
  { name: 'Fitur', path: '/features' },
  { name: 'Harga', path: '/pricing' },
  { name: 'Tentang Gaptek', path: '/about' }
]

const handleNavigation = (path) => {
  router.push(path)
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
        <div class="h-full w-full bg-zinc-100 flex items-center justify-center">
          <Zap class="h-4 w-4 text-zinc-900" />
        </div>
      </template>

      <template #actions>
        <Button class="font-bold px-3 sm:px-6 py-1.5 sm:py-2 text-xs sm:text-base rounded-xl bg-zinc-100 text-zinc-900 hover:bg-white transition-all active:scale-95" @click="navigateToLogin">
          Masuk
        </Button>
      </template>
    </Navbar>

    <!-- Hero Section -->
    <main class="relative flex-grow flex flex-col items-center pt-24 pb-32 px-6 overflow-hidden">
      <!-- Background Gradient Blinds -->
      <div class="absolute inset-0 z-0 opacity-100 pointer-events-none">
        <GradientBlinds
          :gradient-colors="['#1EA03F', '#182FFF']"
          :angle="0"
          :noise="0.3"
          :blind-count="12"
          :blind-min-width="50"
          :spotlight-radius="0.5"
          :spotlight-softness="1"
          :spotlight-opacity="1"
          :mouse-dampening="0.15"
          :distort-amount="0"
          shine-direction="left"
          mix-blend-mode="screen"
        />
      </div>
      
      <div class="relative z-10 space-y-10 max-w-5xl mx-auto text-center">
        <!-- Headline -->
        <h1 class="text-6xl md:text-8xl font-medium font-serif tracking-tight text-white leading-[1.05]">
          Platform All-in-One<br /> 
          untuk <span class="italic text-primary">Retail & F&B </span> modern
        </h1>

        <!-- Subheadline -->
        <p class="text-lg md:text-xl text-zinc-400 font-medium max-w-2xl mx-auto leading-relaxed">
          Manajemen proyek bertenaga AI yang membantu Anda <br class="hidden md:block" />
          membangun aplikasi impian lebih cepat dan terorganisir.
        </p>

        <!-- CTA Buttons -->
        <div class="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
          <Button 
            class="h-12 px-8 rounded-full bg-white text-zinc-900 hover:scale-105 transition-transform duration-300 font-medium"
            @click="navigateToLogin"
          >
            Masuk
          </Button>
          <Button 
            variant="ghost"
            class="h-12 px-8 rounded-full border border-zinc-800 text-white hover:bg-zinc-900 transition-colors duration-300"
          >
            Lihat Demo
          </Button>
        </div>
      </div>


    </main>

    <!-- Content Sections -->

    <Steps />

    <!-- Footer -->
    <Footer />

    
    <!-- Cookie Banner -->
    <Transition
      enter-active-class="transition duration-500 ease-out"
      enter-from-class="transform translate-y-20 opacity-0"
      enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-300 ease-in"
      leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform translate-y-20 opacity-0"
    >
      <div v-if="showCookieBanner" class="fixed bottom-8 right-8 z-[60] max-w-sm">
        <div class="bg-white text-zinc-900 p-5 rounded-2xl shadow-2xl flex items-center justify-between gap-8 backdrop-blur-xl bg-opacity-95 border border-black/5">
          <div class="space-y-1">
            <p class="text-[11px] leading-relaxed font-semibold">Kebijakan Cookie</p>
            <p class="text-[10px] leading-relaxed opacity-70">
              Kami menggunakan cookie untuk mempersonalisasi konten, menjalankan iklan, dan menganalisis lalu lintas.
            </p>
          </div>
          <button 
            @click="hideCookieBanner"
            class="shrink-0 px-5 py-2 bg-zinc-900 text-white rounded-xl text-[10px] font-bold hover:bg-black transition-all active:scale-95 shadow-lg shadow-black/10"
          >
            Oke
          </button>
        </div>
      </div>
    </Transition>


  </LandingLayout>
</template>

<style scoped>
/* No styles here as per requirement */
</style>


