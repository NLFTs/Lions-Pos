<script setup>
import { useRouter } from 'vue-router'
import LandingLayout from '@/components/section/landingpage/LandingLayout.vue'
import Navbar from '@/components/section/landingpage/Navbar.vue'
import Button from '@/components/ui/Button.vue'
import { Zap, ArrowLeft } from 'lucide-vue-next'

const router = useRouter()

const navigationItems = [
  { name: 'Beranda', path: '/' },
  { name: 'Tentang', path: '/about' },
  { name: 'Harga', path: '/pricing' },
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

const goHome = () => router.push('/')
</script>

<template>
  <LandingLayout>
    <Navbar brand-name="gaptek" :nav-items="navigationItems" @navigate="handleNavigation" class="bg-black border-b border-zinc-900">
      <template #logo>
        <div class="h-6 w-6 bg-white rounded-full flex items-center justify-center">
          <Zap class="h-3 w-3 text-black" />
        </div>
      </template>
    </Navbar>

    <main class="relative flex-grow flex flex-col items-center justify-center text-center px-6 py-32 bg-black overflow-hidden">
      
      <!-- SVG 404 yang Lebih Terlihat -->
      <div class="absolute inset-0 flex items-center justify-center pointer-events-none select-none">
        <svg viewBox="0 0 400 200" class="w-[70%] max-w-3xl">
          <!-- Definisi Gradient untuk efek mengkilap -->
          <defs>
            <linearGradient id="glowGradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#ffffff;stop-opacity:0.15" />
              <stop offset="100%" style="stop-color:#4f4f4f;stop-opacity:0.05" />
            </linearGradient>
          </defs>
          
          <!-- Angka dengan Gradient dan Border -->
          <text 
            x="50%" y="50%" 
            dominant-baseline="middle" 
            text-anchor="middle" 
            font-weight="900" 
            font-size="160" 
            font-family="sans-serif"
            fill="url(#glowGradient)"
            stroke="rgba(255,255,255,0.08)"
            stroke-width="2"
          >
            404
          </text>
        </svg>
      </div>

      <!-- Content Utama -->
      <div class="relative z-10 max-w-xl mx-auto space-y-6">
        <h1 class="text-6xl font-bold text-white tracking-tighter">
          Halaman tidak ditemukan.
        </h1>
        
        <p class="text-zinc-400 text-lg max-w-md mx-auto">
          Ups, sepertinya Anda tersesat. Halaman ini mungkin telah dipindahkan atau tidak lagi tersedia.
        </p>

        <div class="flex items-center justify-center pt-8">
            <button 
                @click="goHome" 
                class="relative px-8 py-3 bg-white text-black text-sm font-semibold overflow-hidden transition-all duration-300 shadow-[0_0_25px_rgba(255,255,255,0.15)] group"
            >
                <!-- Efek Cahaya Merah Berjalan -->
                <span class="absolute inset-0 w-0 bg-red-600 transition-all duration-700 ease-out group-hover:w-full"></span>
                
                <!-- Teks Tombol -->
                <span class="relative z-10 group-hover:text-white transition-colors duration-300">
                Kembali ke Beranda
                </span>
            </button>
        </div>
      </div>
    </main>
  </LandingLayout>
</template>

<style scoped>
</style>
