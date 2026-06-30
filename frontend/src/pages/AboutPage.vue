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
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { ArrowUpRight, ChevronDown } from 'lucide-vue-next'
// coreFeatures tetap pakai array kamu yang sudah ada (icon, title, desc)

const expandedIndex = ref(null)
const spotlight = reactive({})
const currentBusinessType = ref('Retail')
const businessTypeText = ref(null)
const statCards = ref([])
const activeFeatureIndex = ref(0)
const featureAutoHoverEnabled = ref(true)
const activeClientIndex = ref(0)
const clientProgressKey = ref(0)
let featureAutoHoverTimer = null
let clientRotationTimer = null

const businessTypes = [
  'Retail',
  'F&B',
  'Jasa',
  'Apotek',
  'Minimarket',
  'Restoran',
  'Kafe',
  'Salon',
  'Bengkel',
  'Laundry',
  'Fashion',
  'Grosir'
]

function handleMove(e, i) {
  const rect = e.currentTarget.getBoundingClientRect()
  spotlight[i] = { x: e.clientX - rect.left, y: e.clientY - rect.top }
}

function spotlightStyle(i) {
  const p = spotlight[i]
  if (!p) return { opacity: 0 }
  return {
    opacity: 1,
    background: `radial-gradient(260px circle at ${p.x}px ${p.y}px, rgba(167,139,250,0.16), transparent 70%)`
  }
}

function toggleExpand(i) {
  expandedIndex.value = expandedIndex.value === i ? null : i
}

function setStatCard(el, i) {
  if (el) statCards.value[i] = el
}

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
  },
  {
    icon: Globe,
    title: 'Multi-Outlet',
    desc: 'Kelola banyak cabang dari satu akun. Harga, stok, promo, dan laporan setiap outlet tetap tersinkron rapi.'
  },
  {
    icon: Lock,
    title: 'Kontrol Akses',
    desc: 'Batasi akses kasir, supervisor, dan owner sesuai peran. Aktivitas penting tercatat agar operasional lebih aman.'
  },

]

const featureColors = [
  'hover:bg-emerald-500',
  'hover:bg-violet-500',
  'hover:bg-blue-500',
  'hover:bg-pink-500',
  'hover:bg-orange-500',
  'hover:bg-cyan-500',
  'hover:bg-lime-500',
  'hover:bg-rose-500'
]

const featureActiveStyles = [
  { backgroundColor: '#10b981' },
  { backgroundColor: '#8b5cf6' },
  { backgroundColor: '#3b82f6' },
  { backgroundColor: '#ec4899' },
  { backgroundColor: '#f97316' },
  { backgroundColor: '#06b6d4' },
  { backgroundColor: '#84cc16' },
  { backgroundColor: '#f43f5e' }
]

function pauseAutoFeatureHover() {
  featureAutoHoverEnabled.value = false
  activeFeatureIndex.value = null
}

function resumeAutoFeatureHover() {
  featureAutoHoverEnabled.value = true
}

function scheduleClientRotation() {
  if (clientRotationTimer) clearTimeout(clientRotationTimer)
  clientRotationTimer = setTimeout(showNextClient, 5000)
}

function setActiveClient(index) {
  activeClientIndex.value = index
  clientProgressKey.value += 1
  scheduleClientRotation()
}

function showNextClient() {
  setActiveClient((activeClientIndex.value + 1) % clientTestimonials.length)
}

onMounted(() => {
  featureAutoHoverTimer = setInterval(() => {
    if (!featureAutoHoverEnabled.value) return
    activeFeatureIndex.value = (activeFeatureIndex.value + 1) % coreFeatures.length
  }, 3000)

  scheduleClientRotation()
})

onUnmounted(() => {
  if (featureAutoHoverTimer) clearInterval(featureAutoHoverTimer)
  if (clientRotationTimer) clearTimeout(clientRotationTimer)
})

const clientTestimonials = [
  {
    name: 'NLFTs',
    img: null,
    link: 'https://www.vuemastery.com/',
    quote: 'Gaptek gives our team a cleaner operational backbone: transactions, inventory, and reports stay connected without adding extra admin work.',
    author: 'NLFTs TEAM'
  },
  {
    name: '',
    img: 'https://upload.wikimedia.org/wikipedia/commons/9/9e/ALFAMART_LOGO_BARU.png',
    link: 'https://vehikl.com/',
    quote: 'The platform keeps daily operations readable across outlets, so teams can focus on service quality instead of chasing scattered records.',
    author: 'Alfamart Team'
  },
  {
    name: '',
    img: 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/9d/Logo_Indomaret.png/1920px-Logo_Indomaret.png',
    link: 'https://vuejs.amsterdam/',
    quote: 'Gaptek helps businesses move faster with a POS foundation that feels organized, reliable, and ready for multi-branch growth.',
    author: 'Indomaret team'
  },
  {
    name: '',
    img: 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c4/Alfamidi_2015.svg/1920px-Alfamidi_2015.svg.png',
    link: 'https://www.storyblok.com',
    quote: 'From front counter workflows to back-office visibility, Gaptek makes operational data easier to trust and act on.',
    author: 'Alfamidi team'
  },
  {
    name: '',
    img: 'https://upload.wikimedia.org/wikipedia/commons/f/f3/Logo_Borma_Toserba.jpg',
    link: 'https://opencollective.com/2021-frameworks-fund',
    quote: 'Reliable infrastructure matters when teams scale. Gaptek gives business owners a practical way to keep data flowing.',
    author: 'Borma Team'
  }
]

const stats = [
  { value: '2.500+', label: 'Outlet Aktif' },
  { value: '99.9%', label: 'Uptime SLA' },
  { value: '<150ms', label: 'Response Time' },
  { value: '12 Juta+', label: 'Transaksi/Bulan' },
  { value: '1.000+', label: 'Fitur & Integrasi' },
  { value: '24/7', label: 'Dukungan Teknis' } ,
  { value: '100%', label: 'Migrasi Gratis' },
  { value: '0%', label: 'Biaya Setup' }
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

useGsap((gsap, ScrollTrigger) => {
  gsap.from('.reveal', {
    y: 40,
    opacity: 0,
    duration: 1.2,
    stagger: 0.2,
    ease: 'power4.out'
  })

  const businessTl = gsap.timeline({ repeat: -1 })
  const rotatingBusinessTypes = [...businessTypes.slice(1), businessTypes[0]]

  businessTl.to({}, { duration: 1 })

  rotatingBusinessTypes.forEach((type) => {
    businessTl
      .call(() => {
        currentBusinessType.value = type || 'Retail'
      })
      .fromTo(
        businessTypeText.value,
        { opacity: 0.82 },
        {
          opacity: 1,
          duration: 0.2,
          ease: 'power2.out'
        }
      )
      .to({}, { duration: 0.8 })
  })

  gsap.set(statCards.value, { x: 90, y: 28, opacity: 0 })

  gsap.to(statCards.value, {
    x: 0,
    y: 0,
    opacity: 1,
    duration: 0.7,
    stagger: {
      each: 0.12,
      from: 'end'
    },
    ease: 'power3.out',
    scrollTrigger: {
      trigger: '.stats-strip',
      start: 'top 80%',
      once: true,
      toggleActions: 'play none none none'
    }
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
            <span class="text-[15px] font-semibold tracking-widest uppercase text-light" style="font-family: 'Roobert', sans-serif; font-weight: 600;">Tentang</span>
            <span class="none "></span>
            <span class="text-[16px] bg-purple-600 px-2 py-0.5 rounded-full font-bold" style="font-family: 'Roobert', sans-serif; font-weight: 700;">BETA</span>

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
    <section class="stats-strip border-b border-white/8 bg-black text-white overflow-x-auto">
      <div class="min-w-full inline-grid grid-cols-8 gap-0">
        <div
          v-for="(stat, i) in stats"
          :key="i"
          :ref="(el) => setStatCard(el, i)"
          class="stat-card py-8 px-4 border-r border-white/8 last:border-r-0 flex flex-col items-center justify-center text-center"
        >
          <div class="text-2xl md:text-3xl font-semibold tracking-tight text-white mb-2">{{ stat.value }}</div>
          <div class="text-xs text-zinc-500 tracking-wide uppercase whitespace-nowrap">{{ stat.label }}</div>
        </div>
      </div>
    </section>

    <!-- ─── MISI ─────────────────────────────────────────────────── -->
    <section class="flex flex-col md:flex-row w-full h-[600px] overflow-hidden border-b border-[#1a1a1a]">
        
        <!-- Left Side: Content -->
        <div class="bg-[#00e676] p-12 md:p-24 flex flex-col justify-center w-full md:w-1/2 text-black">
            <div>
                <p class="text-xs tracking-widest uppercase font-bold mb-6 opacity-70">Misi Kami</p>
                <h2 class="text-4xl md:text-5xl font-medium leading-[1.1] tracking-tight">
                    Membuat bisnis <span ref="businessTypeText" class="inline-block min-w-[8.5rem] align-baseline text-left">{{ currentBusinessType || 'Retail' }}</span> Indonesia mampu bersaing dengan infrastruktur kelas dunia.
                </h2>
            </div>
            
            <div class="mt-8 space-y-6 text-black/80 max-w-md font-medium">
                <p>
                    Pemilik outlet seharusnya fokus pada pelanggan dan produk, bukan terjebak merekap data dari tiga sistem berbeda setiap malam.
                </p>
            </div>
        </div>

        <div class="flex w-full md:w-1/2 h-full">
            <!-- Grid Divider -->
            <div class="w-20 grid-pattern border-l border-r border-[#1a1a1a] hidden md:block flex-shrink-0"></div>
            
            <!-- Image Area: Using h-full and object-cover to contain sizing -->
            <div class="flex-1 h-full w-full bg-zinc-900 overflow-hidden">
                <img 
                    src="/lintang.webp" 
                    alt="Team working" 
                    class="w-full h-full object-cover object-center"
                    onerror="this.style.display='none'; this.parentElement.innerHTML='<div class=\'w-full h-full flex items-center justify-center text-zinc-600\'>Image not found</div>'"
                >
            </div>
        </div>
    </section>

    <!-- ─── FITUR UTAMA ──────────────────────────────────────────── -->
    <section class="border-b border-white/8 py-20 px-6 bg-black text-white relative overflow-hidden">
      <!-- Background Grid Pattern -->
      <div class="absolute inset-0 grid-background"></div>
      
      <div class="max-w-5xl mx-auto relative z-10">
        <div class="mb-14">
          <p class="text-[10px] tracking-widest uppercase text-zinc-600 mb-3">Fitur Platform</p>
          <h2 class="text-2xl md:text-3xl font-light text-white">
            Satu platform, semua yang Anda butuhkan.
          </h2>
        </div>

        <!-- Table-like Layout -->
        <div class="border border-white/8">
          <div class="grid grid-cols-4 gap-0">
            <div
              v-for="(feat, i) in coreFeatures"
              :key="i"
              class="border-r border-b border-white/8 p-5 transition-all duration-200 cursor-pointer"
              :class="[
                featureColors[i],
                'bg-zinc-950',
                { 'border-r-0': (i + 1) % 4 === 0, 'border-b-0': i >= 4 }
              ]"
              :style="featureAutoHoverEnabled && activeFeatureIndex === i ? featureActiveStyles[i] : null"
              @mouseenter="pauseAutoFeatureHover"
              @mouseleave="resumeAutoFeatureHover"
            >
              <div class="flex flex-col h-full">
                <div class="flex items-center gap-3 mb-4">
                  <div class="w-8 h-8 bg-white/5 border border-white/10 flex items-center justify-center flex-shrink-0">
                    <component :is="feat.icon" class="w-4 h-4 text-white" />
                  </div>
                  <h3 class="text-sm font-medium text-white">{{ feat.title }}</h3>
                </div>
                <p class="text-white text-sm leading-relaxed flex-grow">{{ feat.desc }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ─── KLIEN ───────────────────────────────────────── -->
    <section class="border-y border-white/8 py-24 px-6 bg-black text-white">
      <div class="max-w-7xl mx-auto rounded-[2rem] border border-white/10 bg-[#080808] px-6 py-12 md:px-12 md:py-16 shadow-2xl shadow-black/40">
        <div class="grid grid-cols-2 md:grid-cols-5 gap-x-6 gap-y-8 mb-16 md:mb-20">
          <button
            v-for="(client, i) in clientTestimonials"
            :key="client.name"
            class="group relative pb-7 text-left transition-colors"
            :class="activeClientIndex === i ? 'text-white' : 'text-zinc-500 hover:text-zinc-300'"
            type="button"
            @click="setActiveClient(i)"
          >
            <div
              class="flex min-h-12 items-center"
              :class="client.name === 'NLFTs' ? 'justify-center' : 'gap-3'"
            >
              <span v-if="client.name === 'NLFTs'" class="text-3xl md:text-4xl font-bold tracking-tight text-white">
                NLFTs
              </span>
              <template v-else>
                <img
                  :src="client.img"
                  :alt="client.name"
                  class="max-h-14 max-w-[8rem] object-contain opacity-55 grayscale transition duration-200 group-hover:opacity-90"
                  :class="activeClientIndex === i ? 'opacity-100 grayscale-0' : ''"
                >
              </template>
            </div>
            <div class="absolute bottom-0 left-0 h-px w-full bg-white/10 overflow-hidden">
              <span
                v-if="activeClientIndex === i"
                :key="`${i}-${clientProgressKey}`"
                class="client-progress-bar block h-full bg-white"
              ></span>
            </div>
          </button>
        </div>

        <Transition name="client-slide" mode="out-in">
          <div :key="activeClientIndex">
            <blockquote class="max-w-6xl text-2xl md:text-4xl leading-tight tracking-tight text-white">
              &quot;{{ clientTestimonials[activeClientIndex].quote }}&quot;
            </blockquote>
            <a
              :href="clientTestimonials[activeClientIndex].link"
              target="_blank"
              rel="noreferrer"
              class="mt-10 inline-block bg-blue-700 px-1 font-mono text-sm md:text-base font-bold uppercase tracking-wider text-white hover:bg-blue-600"
            >
              {{ clientTestimonials[activeClientIndex].author }}
            </a>
          </div>
        </Transition>
      </div>
    </section>

    <!-- Section Gabungan: Cara Kerja, Perjalanan, dan Keunggulan -->
    <section class="flex flex-col md:flex-row w-full h-[600px] overflow-hidden border-b border-[#1a1a1a] bg-black">
        <div class="flex w-full md:w-1/2 h-full">
            <div class="flex-1 h-full w-full bg-zinc-900 overflow-hidden">
                <img 
                    src="/login-gam.webp"
                    alt="POS team collaboration" 
                    class="w-full h-full object-cover object-center"
                    onerror="this.style.display='none'; this.parentElement.innerHTML='<div class=\'w-full h-full flex items-center justify-center text-zinc-600\'>Image not found</div>'"
                >
            </div>
            <div class="w-20 grid-pattern border-l border-r border-[#1a1a1a] hidden md:block flex-shrink-0"></div>
        </div>

        <div class="bg-[#00e676] p-12 md:p-24 flex flex-col justify-center w-full md:w-1/2 text-black">
            <div>
                <p class="text-xs tracking-widest uppercase font-bold mb-6 opacity-70">Langkah Berikutnya</p>
                <h2 class="text-4xl md:text-5xl font-medium leading-[1.1] tracking-tight">
                    Operasional yang rapi dimulai dari sistem POS yang bisa dipercaya setiap hari.
                </h2>
            </div>
            
            <div class="mt-8 space-y-6 text-black/80 max-w-md font-medium">
                <p>
                    Dari transaksi pertama sampai laporan akhir bulan, Gaptek membantu tim bergerak cepat tanpa kehilangan kontrol atas data toko.
                </p>
            </div>
        </div>
    </section>

    <!-- CTA -->
    <section class="py-20 px-6 bg-black text-white border-b border-white/8">
      <div class="max-w-6xl mx-auto rounded-2xl border border-white/10 bg-[#1c1d23] px-6 py-9 md:px-10 md:py-10">
        <div class="grid lg:grid-cols-[1fr_0.42fr] gap-8 lg:gap-10 items-center">
          <div>
            <h2 class="text-2xl md:text-4xl font-light leading-tight tracking-tight text-zinc-400">
              <span class="font-semibold text-white">Siap mendigitalkan operasional ritel Anda?</span> Mulai iterasi Retail Anda sekarang. Hubungi tim kami untuk mendiskusikan kebutuhan infrastruktur skala 
              <span class="text-blue-500">Pro</span> atau tingkat <span class="text-violet-500">Enterprise</span> perusahaan Anda.
            </h2>
            <div class="mt-7 flex flex-wrap items-center gap-3">
              <button class="px-5 py-2.5 bg-[#f0efe7] text-black text-xs font-semibold rounded-md hover:bg-white transition-colors">
                Hubungi Kami
              </button>
              <button class="px-5 py-2.5 border border-white/10 text-zinc-400 text-xs font-semibold rounded-md hover:border-white/20 hover:text-white transition-colors">
                Konsultasikan Ide
              </button>
            </div>
          </div>

          <div class="lg:border-l border-white/10 lg:pl-10">
            <p class="text-base leading-relaxed text-zinc-400">
              <span class="font-semibold text-white">Jelajahi Opsi Enterprise</span> dengan jaminan performa kustom, isolasi pipeline data, serta dukungan Service Level Agreement (SLA) khusus.
            </p>
            <button class="mt-5 px-5 py-2.5 border border-white/10 text-white text-xs font-semibold rounded-md hover:border-white/25 hover:bg-white/5 transition-colors">
              Solusi Korporat
            </button>
          </div>
        </div>
      </div>
    </section>
  </LandingLayout>
</template>

<style scoped>
.about-root {
  font-family: 'Roobert', 'Inter', sans-serif;
}

.client-slide-enter-active,
.client-slide-leave-active {
  transition: opacity 0.35s ease, transform 0.35s ease;
}

.client-slide-enter-from {
  opacity: 0;
  transform: translateX(28px);
}

.client-slide-leave-to {
  opacity: 0;
  transform: translateX(-28px);
}

.client-progress-bar {
  animation: client-progress 5s linear forwards;
  transform-origin: left center;
}

@keyframes client-progress {
  from {
    transform: scaleX(0);
  }
  to {
    transform: scaleX(1);
  }
}

.grid-background {
  background-image: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 99px,
    rgba(255, 255, 255, 0.1) 99px,
    rgba(255, 255, 255, 0.1) 100px
  ),
  repeating-linear-gradient(
    90deg,
    transparent,
    transparent 99px,
    rgba(255, 255, 255, 0.1) 99px,
    rgba(255, 255, 255, 0.1) 100px
  );
  background-size: 100px 100px;
  background-repeat: repeat;
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