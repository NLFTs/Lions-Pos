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
        <Button class="font-bold px-6 py-2 rounded-xl bg-zinc-100 text-zinc-900 hover:bg-white transition-all active:scale-95" @click="goHome">
          Kembali ke Beranda
        </Button>
      </template>
    </Navbar>

    <main class="relative flex-grow flex flex-col items-center justify-center text-center px-6 py-24">
      <div class="max-w-3xl mx-auto space-y-10">
        <div class="inline-flex items-center justify-center gap-3 text-primary text-sm font-semibold uppercase tracking-[0.3em] mb-6">
          <Zap class="w-5 h-5" />
          404 - Halaman Tidak Ditemukan
        </div>

        <h1 class="text-6xl md:text-7xl font-bold text-white tracking-tight leading-tight">
          Ups, sepertinya alamat yang dicari tidak ada.
        </h1>

        <p class="text-lg text-zinc-400 max-w-2xl mx-auto leading-relaxed">
          Halaman yang Anda buka mungkin telah dipindahkan, salah ketik, atau belum dibuat.
          Kembali ke beranda untuk melanjutkan tanpa perlu login.
        </p>

        <div class="flex flex-col sm:flex-row items-center justify-center gap-4 pt-6">
          <Button class="h-14 px-8 rounded-2xl bg-white text-zinc-900 hover:bg-zinc-100 transition-all font-semibold" @click="goHome">
            Ke Beranda
          </Button>
          <Button class="h-14 px-8 rounded-2xl bg-zinc-900/70 text-white border border-white/10 hover:bg-zinc-800 transition-all font-semibold" @click="router.push('/about')">
            Pelajari Gaptek
          </Button>
        </div>
      </div>
    </main>
  </LandingLayout>
</template>

<style scoped>
</style>
