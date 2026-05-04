<script setup>
import { useRouter } from 'vue-router'
import LandingLayout from '@/components/section/landingpage/LandingLayout.vue'
import Navbar from '@/components/section/landingpage/Navbar.vue'
import Button from '@/components/ui/Button.vue'
import { Zap, Heart, Shield, Rocket, Globe, ArrowLeft, Users, Sparkles, Trophy } from 'lucide-vue-next'
import GradientBlinds from '@/components/ui/background/GradientBlinds.vue'

const router = useRouter()

const navigationItems = [
  { name: 'Layanan Kami', path: '/#offer', hasDropdown: true },
  { name: 'Untuk Siapa', path: '/#target', hasDropdown: true },
  { name: 'Harga', path: '/#pricing' },
  { name: 'Tentang Kami', path: '/about' }
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

const navigateBack = () => {
  router.back()
}
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
        <Button variant="ghost" class="hidden sm:inline-flex font-bold text-zinc-300 hover:text-white transition-colors duration-300" @click="router.push('/login')">
          Masuk
        </Button>
        <Button class="font-bold px-6 py-2 rounded-xl bg-zinc-100 text-zinc-900 hover:bg-white transition-all active:scale-95" @click="router.push('/login')">
          Mulai Gratis
        </Button>
      </template>
    </Navbar>

    <main class="relative flex-grow flex flex-col items-center pt-32 pb-24 px-6 overflow-hidden">
      <!-- Background -->
      <div class="absolute inset-0 z-0 opacity-40 pointer-events-none">
        <GradientBlinds
          :gradient-colors="['#1EA03F', '#182FFF']"
          :angle="0"
          :noise="0.2"
          :blind-count="10"
          mix-blend-mode="screen"
        />
      </div>

      <div class="relative z-10 max-w-4xl mx-auto space-y-24">
        <!-- Hero Section -->
        <div class="text-center space-y-8">
          <button 
            @click="navigateBack"
            class="inline-flex items-center gap-2 text-sm font-medium text-zinc-500 hover:text-white transition-colors mb-4 group"
          >
            <ArrowLeft class="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
            Kembali
          </button>
          
          <h1 class="text-5xl md:text-7xl font-serif italic tracking-tight text-white leading-tight">
            Kami Membangun <span class="text-primary not-italic">Masa Depan</span> Retail.
          </h1>
          
          <p class="text-xl text-zinc-400 font-medium max-w-2xl mx-auto leading-relaxed">
            Gaptek hadir sebagai solusi revolusioner untuk membantu pengusaha retail dan F&B naik kelas melalui teknologi otomasi dan kecerdasan data yang elegan.
          </p>
        </div>

        <!-- Vision Section -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
          <div class="space-y-6">
            <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary/10 border border-primary/20 text-primary text-xs font-bold uppercase tracking-widest">
              <Sparkles class="w-3 h-3" />
              Visi Kami
            </div>
            <h2 class="text-3xl font-bold text-white tracking-tight">Menghapus Batas Antara Ide dan Eksekusi.</h2>
            <p class="text-zinc-400 leading-relaxed">
              Kami percaya bahwa setiap pemilik bisnis layak mendapatkan alat yang sama kuatnya dengan perusahaan raksasa. Misi kami adalah mendemokratisasi teknologi enterprise dan mengemasnya dalam antarmuka yang sangat mudah digunakan oleh siapa saja.
            </p>
            <div class="flex gap-4">
              <div class="text-center p-4 rounded-2xl bg-zinc-900/50 border border-white/5 flex-1">
                <div class="text-2xl font-bold text-white">500+</div>
                <div class="text-[10px] text-zinc-500 uppercase font-bold tracking-widest mt-1">Mitra Bisnis</div>
              </div>
              <div class="text-center p-4 rounded-2xl bg-zinc-900/50 border border-white/5 flex-1">
                <div class="text-2xl font-bold text-white">99.9%</div>
                <div class="text-[10px] text-zinc-500 uppercase font-bold tracking-widest mt-1">Uptime</div>
              </div>
            </div>
          </div>
          <div class="relative group">
            <div class="absolute -inset-4 bg-primary/20 blur-3xl rounded-full opacity-0 group-hover:opacity-100 transition-opacity duration-700"></div>
            <div class="relative aspect-square rounded-3xl bg-zinc-900 border border-white/10 overflow-hidden flex items-center justify-center shadow-2xl">
               <Zap class="w-32 h-32 text-primary opacity-20 animate-pulse" />
               <div class="absolute inset-0 flex items-center justify-center p-8">
                 <div class="text-center space-y-4">
                   <Users class="w-12 h-12 text-white mx-auto mb-4" />
                   <p class="text-sm font-medium text-zinc-300 italic">"Teknologi yang hebat adalah teknologi yang tidak terasa seperti teknologi—ia hanya bekerja."</p>
                 </div>
               </div>
            </div>
          </div>
        </div>

        <!-- Core Values -->
        <div class="space-y-12">
          <div class="text-center space-y-4">
            <h2 class="text-3xl font-bold text-white">Nilai Inti Kami</h2>
            <p class="text-zinc-400 max-w-xl mx-auto">Prinsip yang membimbing kami dalam membangun setiap baris kode di Gaptek.</p>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div class="p-8 rounded-3xl bg-zinc-900 border border-white/5 hover:border-primary/30 transition-all group">
              <div class="w-12 h-12 rounded-2xl bg-primary/10 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform">
                <Rocket class="w-6 h-6 text-primary" />
              </div>
              <h3 class="text-lg font-bold text-white mb-2">Inovasi Tanpa Henti</h3>
              <p class="text-sm text-zinc-500 leading-relaxed">Kami tidak pernah puas dengan status quo. Setiap minggu ada fitur baru yang lahir dari masukan Anda.</p>
            </div>
            
            <div class="p-8 rounded-3xl bg-zinc-900 border border-white/5 hover:border-rose-500/30 transition-all group">
              <div class="w-12 h-12 rounded-2xl bg-rose-500/10 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform">
                <Heart class="w-6 h-6 text-rose-500" />
              </div>
              <h3 class="text-lg font-bold text-white mb-2">Empati Pengguna</h3>
              <p class="text-sm text-zinc-500 leading-relaxed">Kami membangun solusi berdasarkan masalah nyata yang dihadapi oleh pemilik toko dan manajer gudang.</p>
            </div>

            <div class="p-8 rounded-3xl bg-zinc-900 border border-white/5 hover:border-blue-500/30 transition-all group">
              <div class="w-12 h-12 rounded-2xl bg-blue-500/10 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform">
                <Shield class="w-6 h-6 text-blue-500" />
              </div>
              <h3 class="text-lg font-bold text-white mb-2">Kepercayaan & Keamanan</h3>
              <p class="text-sm text-zinc-500 leading-relaxed">Data Anda adalah aset paling berharga. Kami melindunginya dengan standar keamanan militer.</p>
            </div>
          </div>
        </div>

        <!-- CTA -->
        <div class="relative p-12 rounded-[3rem] bg-zinc-900 border border-white/10 overflow-hidden text-center space-y-8">
          <div class="absolute inset-0 bg-gradient-to-b from-primary/5 to-transparent"></div>
          <Trophy class="w-16 h-16 text-primary mx-auto relative z-10" />
          <h2 class="text-4xl font-bold text-white relative z-10">Siap Bergabung dengan Revolusi?</h2>
          <p class="text-zinc-400 max-w-lg mx-auto relative z-10">Ribuan pebisnis telah beralih ke Gaptek untuk menyederhanakan hidup mereka. Sekarang giliran Anda.</p>
          <div class="relative z-10">
            <Button 
              class="h-14 px-10 rounded-full bg-white text-zinc-900 hover:scale-105 transition-transform duration-300 font-bold text-lg"
              @click="router.push('/login')"
            >
              Mulai Perjalanan Anda
            </Button>
          </div>
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
