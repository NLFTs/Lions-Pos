<script setup>
import { useRouter } from 'vue-router'
import LandingLayout from '@/components/section/landingpage/LandingLayout.vue'
import Navbar from '@/components/section/landingpage/Navbar.vue'
import Button from '@/components/ui/Button.vue'
import { Zap, Heart, Shield, Rocket, Globe, ArrowLeft, Users, Sparkles, Trophy } from 'lucide-vue-next'
import { useGsap } from '@/hooks/useGsap'


const router = useRouter()

const navigationItems = [
  { name: 'Fitur', path: '/features' },
  { name: 'Harga', path: '/pricing' },
  { name: 'Tentang Gaptek', path: '/about' }
]

const handleNavigation = (path) => {
  router.push(path)
}

const navigateBack = () => {
  router.back()
}

useGsap((gsap, ScrollTrigger) => {
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

    <main class="relative flex-grow flex flex-col items-center pt-20 pb-24 px-6 overflow-hidden">
      <div class="relative z-10 max-w-3xl mx-auto space-y-32">
        <!-- Hero Section -->
        <div class="text-center space-y-10">
          <h1 class="reveal text-5xl md:text-7xl font-serif italic tracking-tight text-white leading-[1.1]">
            Membangun <span class="text-primary not-italic">Masa Depan</span> Retail & F&B modern.
          </h1>
          
          <p class="reveal text-lg md:text-xl text-zinc-400 font-medium max-w-2xl mx-auto leading-relaxed">
            Gaptek hadir sebagai solusi revolusioner untuk membantu pengusaha retail dan F&B naik kelas melalui teknologi otomasi yang elegan.
          </p>
        </div>

        <!-- Vision Section -->
        <div class="space-y-12 border-t border-white/10 pt-24">
          <div class="inline-flex items-center gap-2 text-primary text-xs font-bold uppercase tracking-[0.2em]">
            <Sparkles class="w-3 h-3" />
            Visi Gaptek
          </div>
          <div class="grid grid-cols-1 md:grid-cols-1 gap-12">
            <h2 class="text-4xl md:text-5xl font-medium text-white tracking-tight leading-tight">
              Menghapus Batas Antara <br class="hidden md:block" />
              Ide dan Eksekusi.
            </h2>
            <div class="space-y-8 text-xl text-zinc-400 leading-relaxed max-w-2xl">
              <p>
                Gaptek percaya bahwa setiap pemilik bisnis layak mendapatkan alat yang sama kuatnya dengan perusahaan raksasa. Misi ini adalah mendemokratisasi teknologi enterprise.
              </p>
              <p>
                Kompleksitas dikemas ke dalam antarmuka yang sangat mudah digunakan oleh siapa saja, memastikan pertumbuhan bisnis tidak terhambat oleh batasan teknis.
              </p>
            </div>
          </div>
        </div>

        <!-- Core Values (Text focused) -->
        <div class="space-y-20 border-t border-white/10 pt-24">
          <div class="text-left space-y-4 reveal">
            <h2 class="text-3xl font-bold text-white tracking-tight">Nilai Inti</h2>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-20 gap-y-16 reveal">
            <div class="space-y-4 group cursor-default">
              <h3 class="text-xl font-bold text-white group-hover:text-primary transition-colors duration-300">Inovasi Tanpa Henti</h3>
              <p class="text-lg text-zinc-500 leading-relaxed group-hover:text-zinc-300 transition-colors duration-500">Inovasi adalah jantung Gaptek. Setiap minggu ada fitur baru yang lahir dari masukan langsung para mitra.</p>
            </div>
            
            <div class="space-y-4 group cursor-default">
              <h3 class="text-xl font-bold text-white group-hover:text-rose-500 transition-colors duration-300">Empati Pengguna</h3>
              <p class="text-lg text-zinc-500 leading-relaxed group-hover:text-zinc-300 transition-colors duration-500">Solusi dibangun berdasarkan masalah nyata yang dihadapi oleh pemilik toko dan manajer di lapangan.</p>
            </div>

            <div class="space-y-4 group cursor-default">
              <h3 class="text-xl font-bold text-white group-hover:text-blue-500 transition-colors duration-300">Kepercayaan & Keamanan</h3>
              <p class="text-lg text-zinc-500 leading-relaxed group-hover:text-zinc-300 transition-colors duration-500">Data Anda adalah aset paling berharga. Data dilindungi dengan standar keamanan tertinggi industri.</p>
            </div>

            <div class="space-y-4 group cursor-default">
              <h3 class="text-xl font-bold text-white group-hover:text-primary transition-colors duration-300">Ekosistem Terbuka</h3>
              <p class="text-lg text-zinc-500 leading-relaxed group-hover:text-zinc-300 transition-colors duration-500">Teknologi Gaptek dirancang untuk terhubung dengan berbagai platform, memberikan Anda fleksibilitas penuh.</p>
            </div>
          </div>
        </div>

        <!-- Simple CTA -->
        <div class="py-32 border-t border-white/10 text-center space-y-12">
          <div class="space-y-6">
            <h2 class="text-4xl md:text-6xl font-medium text-white tracking-tight">Siap Bergabung?</h2>
            <p class="text-xl text-zinc-500 max-w-lg mx-auto">Ribuan pebisnis telah beralih ke Gaptek untuk menyederhanakan hidup mereka.</p>
          </div>
          <Button 
            class="h-16 px-12 rounded-2xl bg-white text-zinc-900 hover:scale-105 transition-transform duration-300 font-bold text-xl shadow-2xl shadow-white/10"
            @click="router.push('/login')"
          >
            Mulai Sekarang
          </Button>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <footer class="py-12 border-t border-white/5 text-center">
      <div class="flex items-center justify-center gap-2 mb-4">
        <Zap class="w-5 h-5 text-primary" />
        <span class="text-lg font-bold text-white tracking-tighter uppercase">Gaptek</span>
      </div>
      <p class="text-xs text-zinc-600">© 2026 Gaptek Technology Inc. Semua hak dilindungi undang-undang.</p>
    </footer>
  </LandingLayout>
</template>

<style scoped>
</style>
