<script setup>
import { useRouter } from 'vue-router'
import LandingLayout from '@/components/section/landingpage/LandingLayout.vue'
import Navbar from '@/components/section/landingpage/Navbar.vue'
import Button from '@/components/ui/Button.vue'
import Aurora from '@/components/Aurora.vue'
import { Zap } from 'lucide-vue-next'
import { useGsap } from '@/hooks/useGsap'

const router = useRouter()

const navigationItems = [
  { name: 'Harga', path: '/#pricing' },
  { name: 'Tentang Gaptek', path: '/about' }
]

const handleNavigation = (path) => {
  if (path.startsWith('/#')) {
    router.push('/').then(() => {
      setTimeout(() => {
        const el = document.querySelector(path.substring(1))
        if (el) el.scrollIntoView({ behavior: 'smooth' })
      }, 100)
    })
  } else {
    router.push(path)
  }
}

useGsap((gsap) => {
  gsap.from('.reveal', {
    y: 40,
    opacity: 0,
    duration: 1.2,
    stagger: 0.2,
    ease: 'power4.out'
  })
})
</script>

<template>
  <LandingLayout>
    <Navbar 
      brand-name="gaptek" 
      :nav-items="navigationItems"
      @navigate="handleNavigation"
    >
      <template #logo>
        <div class="h-full w-full bg-zinc-100 flex items-center justify-center">
          <Zap class="h-4 w-4 text-zinc-900" />
        </div>
      </template>
      <template #actions>
        <Button class="font-bold px-6 py-2 rounded-xl bg-zinc-100 text-zinc-900 hover:bg-white transition-all active:scale-95" @click="router.push('/login')">
          Masuk
        </Button>
      </template>
    </Navbar>

    <!-- Container dibuat full-width dengan margin yang lebih tipis agar hero terlihat lebih besar -->
    <main class="relative flex-grow flex flex-col items-center pt-8 pb-16 px-2 font-roobert">
      <div class="relative w-full max-w-[95rem] min-h-[600px] overflow-hidden bg-black text-white flex flex-col items-center justify-center py-24 rounded-3xl border border-white/10">
    
        <!-- Aurora Background Effect - positioned absolutely to fill container -->
        <div class="absolute inset-0 rounded-3xl overflow-hidden z-0">
          <Aurora 
            :color-stops="['#3A29FF', '#FF94B4', '#FF3232']"
            :blend="0.5"
            :amplitude="1.0"
            :speed="0.5"
          />
        </div>

        <!-- Konten Utama: Ukuran font diperkecil untuk kesan lebih rapi -->
        <div class="relative z-10 text-center px-6 max-w-4xl">
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full border border-gray-800 bg-gray-900/30 mb-6">
            <span class="text-[9px] font-semibold tracking-widest uppercase text-gray-500" style="font-family: 'Roobert', sans-serif; font-weight: 600;">Workflows</span>
            <span class="text-[9px] bg-purple-600 px-2 py-0.5 rounded-full font-bold" style="font-family: 'Roobert', sans-serif; font-weight: 700;">BETA</span>
          </div>

          <h1 class="text-4xl md:text-6xl font-bold mb-6 leading-tight tracking-tight" style="font-family: 'Roobert', sans-serif; font-weight: 300;">
            Long-running jobs for<br />apps and agents
          </h1>
          
          <p class="text-base md:text-lg text-gray-400 mb-8 max-w-2xl mx-auto" style="font-family: 'Roobert', sans-serif; font-weight: 400;">
            Deploy reliable application logic and parallel workloads without managing queues, worker pools, or custom retry logic.
          </p>

          <div class="flex items-center justify-center gap-4">
            <button class="px-5 py-2 bg-white text-black font-medium rounded-lg hover:bg-gray-200 transition-colors flex items-center gap-2 text-xs" style="font-family: 'Roobert', sans-serif; font-weight: 600;">
              Get started <span>&rarr;</span>
            </button>
            <button class="px-5 py-2 border border-gray-700 rounded-lg hover:border-gray-500 transition-colors text-xs" style="font-family: 'Roobert', sans-serif; font-weight: 500;">
              Read the docs
            </button>
          </div>
        </div>

        <!-- Bagian Bawah -->
        <div class="mt-20 w-full text-center">
          <p class="text-base md:text-lg text-gray-400" style="font-family: 'Roobert', sans-serif; font-weight: 400;">
            A better way to build reliable 
            <span class="inline-block border-b border-purple-500 text-purple-400 px-1" style="font-family: 'Roobert', sans-serif; font-weight: 600;">data pipelines</span>
          </p>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <footer class="py-12 border-t border-white/5 text-center">
      <div class="flex items-center justify-center gap-2 mb-4">
        <Zap class="w-5 h-5 text-zinc-500" />
        <span class="text-lg font-bold text-white tracking-tighter uppercase">Gaptek</span>
      </div>
      <p class="text-xs text-zinc-600">© 2026 Gaptek Technology Inc. Semua hak dilindungi undang-undang.</p>
    </footer>
  </LandingLayout>
</template>