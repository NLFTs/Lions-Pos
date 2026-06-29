<script setup>
import { useRouter } from 'vue-router'
import LandingLayout from '@/components/section/landingpage/LandingLayout.vue'
import Navbar from '@/components/section/landingpage/Navbar.vue'
import Button from '@/components/ui/Button.vue'
import Aurora from '@/components/Aurora.vue'
import {
  Zap,
  ShoppingCart,
  BarChart3,
  Package,
  Receipt,
  Users,
  ArrowRight,
  CheckCircle2,
  Globe,
  Lock,
  Layers,
  Cpu,
  TrendingUp,
  Clock,
  Database,
  Workflow
} from 'lucide-vue-next'
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

const coreFeatures = [
  {
    icon: ShoppingCart,
    title: 'Kasir Cerdas',
    desc: 'Proses transaksi lebih cepat dengan antarmuka yang intuitif. Mendukung barcode scanner, multi-payment, dan split bill tanpa hambatan.'
  },
  {
    icon: Package,
    title: 'Manajemen Stok',
    desc: 'Pantau stok real-time di semua cabang. Notifikasi otomatis saat stok mendekati batas minimum, tanpa perlu cek manual.'
  },
  {
    icon: Receipt,
    title: 'Laporan & Akuntansi',
    desc: 'Dari data kasir langsung ke laporan laba rugi. Tidak ada input ganda, tidak ada rekap manual—semua tersinkron otomatis.'
  },
  {
    icon: Users,
    title: 'Manajemen Tim',
    desc: 'Atur hak akses per peran, lacak performa kasir, dan kelola shift karyawan dari satu dasbor terpusat.'
  },
  {
    icon: BarChart3,
    title: 'Analitik Bisnis',
    desc: 'Visualisasi tren penjualan, produk terlaris, dan waktu ramai pelanggan. Data yang membantu keputusan, bukan sekadar angka.'
  },
  {
    icon: Workflow,
    title: 'Otomatisasi Workflow',
    desc: 'Buat aturan bisnis sekali, jalankan selamanya. Reorder stok otomatis, diskon terjadwal, dan laporan terkirim sendiri.'
  }
]

const principles = [
  {
    icon: Layers,
    label: 'Modular',
    desc: 'Mulai dari modul yang Anda butuhkan, tambahkan fitur seiring bisnis tumbuh.'
  },
  {
    icon: Lock,
    label: 'Aman',
    desc: 'Enkripsi data end-to-end, audit log lengkap, dan kontrol akses berbasis peran.'
  },
  {
    icon: Globe,
    label: 'Multi-Cabang',
    desc: 'Satu akun untuk semua cabang. Data tersentralisasi, laporan konsolidasi instan.'
  },
  {
    icon: Cpu,
    label: 'Ringan & Cepat',
    desc: 'Dibangun dengan teknologi modern. Berjalan lancar di tablet, PC, bahkan koneksi lambat.'
  }
]

const stats = [
  { value: '2.500+', label: 'Outlet Aktif' },
  { value: '99.9%', label: 'Uptime SLA' },
  { value: '<150ms', label: 'Response Time' },
  { value: '12 Juta+', label: 'Transaksi/Bulan' }
]

const timeline = [
  { year: '2022', event: 'Gaptek lahir dari frustrasi pemilik retail yang lelah merekap data secara manual setiap malam.' },
  { year: '2023', event: 'Modul akuntansi otomatis diluncurkan—pertama di kelasnya untuk pasar retail Indonesia.' },
  { year: '2024', event: 'Ekspansi ke manajemen multi-cabang. Ratusan jaringan toko bergabung dalam satu platform.' },
  { year: '2025', event: 'Workflows engine diluncurkan. Otomatisasi end-to-end dari stok ke pembukuan.' },
  { year: '2026', event: 'Gaptek melayani 2.500+ outlet aktif di seluruh Indonesia dengan uptime 99.9%.' }
]

const checks = [
  'Tidak ada biaya setup tersembunyi',
  'Migrasi data gratis dari sistem lama',
  'Onboarding 1:1 bersama tim kami',
  'Dukungan teknis 7 hari seminggu',
  'Kontrak fleksibel, tanpa lock-in jangka panjang'
]

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
            Sistem Jangka Panjang Untuk <br />Retail dan Outlet
          </h1>
          
          <p class="text-base md:text-lg text-gray-400 mb-8 max-w-2xl mx-auto" style="font-family: 'Roobert', sans-serif; font-weight: 400;">
            Mulai dari kasir, ke stok, ke laporan, ke akuntansi—semua data mengalir otomatis. Gak perlu input ulang, gak perlu rekap manual. Data yang akurat, keputusan yang tepat.  
          </p>

          <div class="flex items-center justify-center gap-4">
            <button class="px-5 py-2 bg-white text-black font-medium rounded-lg hover:bg-gray-200 transition-colors flex items-center gap-2 text-xs" style="font-family: 'Roobert', sans-serif; font-weight: 600;">
              Ayo Memulai <span>&rarr;</span>
            </button>
            <button class="px-5 py-2 border border-gray-700 rounded-lg hover:border-gray-500 transition-colors text-xs" style="font-family: 'Roobert', sans-serif; font-weight: 500;">
              Baca Panduan
            </button>
          </div>
        </div>

        <!-- Bagian Bawah -->
        <div class="mt-20 w-full text-center">
          <p class="text-base md:text-lg text-gray-400" style="font-family: 'Roobert', sans-serif; font-weight: 400;">
            Cara yang lebih baik untuk membangun
            <span class="inline-block border-b border-purple-500 text-purple-400 px-1" style="font-family: 'Roobert', sans-serif; font-weight: 600;">data pipelines yang andal</span>
          </p>
        </div>
      </div>
    </main>

    <!-- ─── STATS STRIP ──────────────────────────────────────────── -->
    <section class="border-b border-white/8 bg-black text-white">
      <div class="max-w-4xl mx-auto grid grid-cols-2 md:grid-cols-4">
        <div
          v-for="(stat, i) in stats"
          :key="i"
          class="py-10 px-6 border-r border-white/8 last:border-r-0 md:[&:nth-child(2)]:border-r md:[&:nth-child(3)]:border-l-0"
        >
          <div class="text-3xl md:text-4xl font-semibold tracking-tight text-white mb-1">{{ stat.value }}</div>
          <div class="text-xs text-zinc-500 tracking-wide uppercase">{{ stat.label }}</div>
        </div>
      </div>
    </section>

    <!-- ─── MISI ─────────────────────────────────────────────────── -->
    <section class="border-b border-white/8 py-20 px-6 bg-black text-white">
      <div class="max-w-4xl mx-auto grid md:grid-cols-2 gap-16 items-start">
        <div>
          <p class="text-[10px] tracking-widest uppercase text-zinc-600 mb-4">Misi Kami</p>
          <h2 class="text-2xl md:text-3xl font-light leading-snug text-white">
            Membuat bisnis retail Indonesia mampu bersaing dengan infrastruktur kelas dunia.
          </h2>
        </div>
        <div class="space-y-5 text-zinc-400 text-sm leading-relaxed pt-1">
          <p>
            Pemilik outlet seharusnya fokus pada pelanggan dan produk, bukan terjebak merekap data dari tiga sistem berbeda setiap malam. Kami melihat kesenjangan ini dan membangun solusinya.
          </p>
          <p>
            Gaptek dirancang sebagai sistem jangka panjang—bukan alat sekali pakai. Setiap fitur dibangun untuk tumbuh bersama bisnis Anda, dari satu gerai hingga ratusan cabang.
          </p>
          <p>
            Data yang akurat menghasilkan keputusan yang tepat. Prinsip ini menjadi fondasi setiap baris kode yang kami tulis.
          </p>
        </div>
      </div>
    </section>

    <!-- ─── FITUR UTAMA ──────────────────────────────────────────── -->
    <section class="border-b border-white/8 py-20 px-6 bg-black text-white">
      <div class="max-w-4xl mx-auto">
        <div class="mb-14">
          <p class="text-[10px] tracking-widest uppercase text-zinc-600 mb-3">Fitur Platform</p>
          <h2 class="text-2xl md:text-3xl font-light text-white">
            Satu platform, semua yang Anda butuhkan.
          </h2>
        </div>

        <div class="grid md:grid-cols-2 gap-0 border border-white/8 rounded-2xl overflow-hidden">
          <div
            v-for="(feat, i) in coreFeatures"
            :key="i"
            class="feature-card p-7 border-b border-r border-white/8 group transition-colors duration-200 hover:bg-white/3"
            :class="{
              'border-r-0': (i + 1) % 2 === 0,
              'border-b-0': i >= coreFeatures.length - 2
            }"
          >
            <div class="flex items-center gap-3 mb-4">
              <div class="w-8 h-8 rounded-lg bg-white/5 border border-white/10 flex items-center justify-center flex-shrink-0 group-hover:border-purple-500/40 transition-colors">
                <component :is="feat.icon" class="w-4 h-4 text-zinc-300" />
              </div>
              <h3 class="text-sm font-medium text-white">{{ feat.title }}</h3>
            </div>
            <p class="text-zinc-500 text-sm leading-relaxed">{{ feat.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ─── PRINSIP DESAIN ───────────────────────────────────────── -->
    <section class="border-b border-white/8 py-20 px-6 bg-black text-white">
      <div class="max-w-4xl mx-auto">
        <p class="text-[10px] tracking-widest uppercase text-zinc-600 mb-14">Dibangun dengan Prinsip</p>

        <div class="grid md:grid-cols-4 gap-8">
          <div
            v-for="(p, i) in principles"
            :key="i"
            class="space-y-3"
          >
            <component :is="p.icon" class="w-5 h-5 text-zinc-400 mb-4" />
            <div class="text-white font-medium text-sm">{{ p.label }}</div>
            <p class="text-zinc-500 text-xs leading-relaxed">{{ p.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ─── ALUR KERJA ───────────────────────────────────────────── -->
    <section class="border-b border-white/8 py-20 px-6 bg-black text-white">
      <div class="max-w-4xl mx-auto grid md:grid-cols-2 gap-16 items-start">
        <div>
          <p class="text-[10px] tracking-widest uppercase text-zinc-600 mb-4">Bagaimana Ini Bekerja</p>
          <h2 class="text-2xl md:text-3xl font-light leading-snug text-white mb-6">
            Data mengalir, bukan berpindah manual.
          </h2>
          <p class="text-zinc-400 text-sm leading-relaxed">
            Setiap transaksi di kasir secara otomatis memperbarui stok, mencatat pendapatan, dan masuk ke pembukuan. Tidak ada jeda, tidak ada rekonsiliasi akhir bulan yang menyiksa.
          </p>
        </div>

        <!-- Pipeline Visual -->
        <div class="space-y-0">
          <div
            v-for="(step, i) in [
              { icon: ShoppingCart, label: 'Kasir & Transaksi', color: 'text-purple-400' },
              { icon: Database, label: 'Stok Diperbarui Otomatis', color: 'text-blue-400' },
              { icon: TrendingUp, label: 'Laporan Penjualan Real-Time', color: 'text-green-400' },
              { icon: Receipt, label: 'Akuntansi Tersinkron', color: 'text-orange-400' }
            ]"
            :key="i"
            class="flex items-start gap-4"
          >
            <div class="flex flex-col items-center">
              <div class="w-8 h-8 rounded-lg bg-white/5 border border-white/10 flex items-center justify-center flex-shrink-0">
                <component :is="step.icon" :class="['w-4 h-4', step.color]" />
              </div>
              <div v-if="i < 3" class="w-px h-8 bg-white/8 mt-1"></div>
            </div>
            <div class="pt-1.5 pb-6">
              <div class="text-sm text-white font-medium">{{ step.label }}</div>
              <div v-if="i < 3" class="mt-1 flex items-center gap-1">
                <Clock class="w-3 h-3 text-zinc-600" />
                <span class="text-[10px] text-zinc-600">Instan, tanpa jeda</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ─── TIMELINE ─────────────────────────────────────────────── -->
    <section class="border-b border-white/8 py-20 px-6 bg-black text-white">
      <div class="max-w-4xl mx-auto">
        <p class="text-[10px] tracking-widest uppercase text-zinc-600 mb-14">Perjalanan Kami</p>

        <div class="space-y-0">
          <div
            v-for="(item, i) in timeline"
            :key="i"
            class="grid grid-cols-[80px_1fr] gap-8 pb-10 relative"
          >
            <!-- Connector line -->
            <div
              v-if="i < timeline.length - 1"
              class="absolute left-[39px] top-6 bottom-0 w-px bg-white/8"
            ></div>

            <div class="text-xs text-zinc-600 pt-0.5 font-mono">{{ item.year }}</div>
            <div class="text-sm text-zinc-300 leading-relaxed">{{ item.event }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- ─── KENAPA GAPTEK ────────────────────────────────────────── -->
    <section class="border-b border-white/8 py-20 px-6 bg-black text-white">
      <div class="max-w-4xl mx-auto">
        <div class="mb-14">
          <p class="text-[10px] tracking-widest uppercase text-zinc-600 mb-3">Mengapa Memilih Gaptek</p>
          <h2 class="text-2xl md:text-3xl font-light text-white">Tidak ada kompromi.</h2>
        </div>

        <div class="space-y-4">
          <div
            v-for="(item, i) in checks"
            :key="i"
            class="flex items-center gap-4 py-4 border-b border-white/5 last:border-b-0"
          >
            <CheckCircle2 class="w-4 h-4 text-green-500 flex-shrink-0" />
            <span class="text-sm text-zinc-300">{{ item }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ─── CTA ──────────────────────────────────────────────────── -->
    <section class="py-24 px-6 bg-black text-white border-b border-white/8">
      <div class="max-w-4xl mx-auto flex flex-col md:flex-row md:items-center justify-between gap-8">
        <div>
          <h2 class="text-2xl md:text-3xl font-light text-white mb-2">
            Siap membangun sistem yang tumbuh bersama bisnis Anda?
          </h2>
          <p class="text-zinc-500 text-sm">Mulai gratis. Tidak perlu kartu kredit.</p>
        </div>
        <div class="flex items-center gap-3 flex-shrink-0">
          <button class="inline-flex items-center gap-2 px-5 py-2.5 bg-white text-black text-xs font-semibold rounded-lg hover:bg-zinc-200 transition-colors">
            Mulai Sekarang
            <ArrowRight class="w-3.5 h-3.5" />
          </button>
          <button class="px-5 py-2.5 border border-white/10 text-zinc-400 text-xs rounded-lg hover:border-white/20 hover:text-white transition-colors">
            Lihat Demo
          </button>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="py-12 border-t border-white/5 text-center bg-black text-white">
      <div class="flex items-center justify-center gap-2 mb-4">
        <Zap class="w-5 h-5 text-zinc-500" />
        <span class="text-lg font-bold text-white tracking-tighter uppercase">Gaptek</span>
      </div>
      <p class="text-xs text-zinc-600">© 2026 Gaptek Technology Inc. Semua hak dilindungi undang-undang.</p>
    </footer>
  </LandingLayout>
</template>

<style scoped>
.about-root {
  font-family: 'Roobert', 'Inter', sans-serif;
}

/* Subtle grid border fix for md breakpoint features */
@media (max-width: 767px) {
  .feature-card {
    border-right: none !important;
  }
  .feature-card:last-child {
    border-bottom: none !important;
  }
}
</style>