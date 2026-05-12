<script setup>
import { 
  Package, 
  Store, 
  BarChart3, 
  CreditCard, 
  Users, 
  ShieldCheck,
  Zap,
  TrendingUp,
  Activity,
  Maximize2,
  Lock,
  Share2
} from 'lucide-vue-next'
import { ref } from 'vue'

// Import High-Detail Illustrations
import StockIllustration from '@/components/ui/features/StockIllustration.vue'
import AnalyticsIllustration from '@/components/ui/features/AnalyticsIllustration.vue'
import LocationIllustration from '@/components/ui/features/LocationIllustration.vue'
import POSIllustration from '@/components/ui/features/POSIllustration.vue'
import CRMIllustration from '@/components/ui/features/CRMIllustration.vue'
import SecurityIllustration from '@/components/ui/features/SecurityIllustration.vue'

const features = [
  {
    id: 'analytics',
    category: 'Insights',
    title: 'Analitik Data',
    description: 'Wawasan prediktif berbasis AI untuk strategi pertumbuhan yang terukur.',
    icon: BarChart3,
    component: AnalyticsIllustration,
    span: 'md:col-span-1 md:row-span-1',
    accent: 'from-green-500/20'
  },
  {
    id: 'dashboard',
    category: 'Overview',
    title: 'Multi-Lokasi',
    description: 'Kendali operasional terpusat untuk ribuan titik cabang tanpa batas.',
    icon: Store,
    component: LocationIllustration,
    span: 'md:col-span-1 md:row-span-1',
    accent: 'from-blue-500/20'
  },
  {
    id: 'collaboration',
    category: 'Teamwork',
    title: 'Sistem Kasir',
    description: 'Ekosistem POS masa depan dengan performa transaksi ultra-cepat.',
    icon: CreditCard,
    component: POSIllustration,
    span: 'md:col-span-2 md:row-span-2',
    accent: 'from-purple-500/20'
  },
  {
    id: 'automation',
    category: 'Efficiency',
    title: 'Manajemen Stok',
    description: 'Otomasi inventaris cerdas dengan sinkronisasi multi-gudang real-time.',
    icon: Package,
    component: StockIllustration,
    span: 'md:col-span-2 md:row-span-2',
    accent: 'from-orange-500/20'
  },
  {
    id: 'integration',
    category: 'Connectivity',
    title: 'Loyalitas',
    description: 'Personalisasi pengalaman pelanggan melalui algoritma CRM canggih.',
    icon: Users,
    component: CRMIllustration,
    span: 'md:col-span-1 md:row-span-1',
    accent: 'from-emerald-500/20'
  },
  {
    id: 'security',
    category: 'Protection',
    title: 'Keamanan',
    description: 'Infrastruktur enkripsi militer untuk perlindungan aset digital Anda.',
    icon: ShieldCheck,
    component: SecurityIllustration,
    span: 'md:col-span-1 md:row-span-1',
    accent: 'from-red-500/20'
  }
]

const containerRef = ref(null)
</script>

<template>
  <section 
    ref="containerRef"
    id="features" 
    class="min-h-screen py-24 px-6 bg-[#020202] text-white relative overflow-hidden flex flex-col justify-center"
  >
    <!-- Background Elements -->
    <div class="absolute inset-0 pointer-events-none opacity-20">
       <div class="absolute top-0 left-0 w-full h-full bg-[radial-gradient(circle_at_50%_50%,#111,transparent)]"></div>
    </div>

    <div class="max-w-[1400px] mx-auto w-full relative z-10 flex flex-col gap-16 lg:gap-20">
      
      <!-- Premium Header (Minimalized) -->
      <div class="space-y-4">
        <div class="flex items-center gap-2">
           <div class="w-8 h-[1px] bg-green-500"></div>
           <span class="text-[10px] font-bold uppercase tracking-[0.4em] text-green-500">Core Features</span>
        </div>
        <h2 class="text-5xl md:text-7xl font-medium tracking-tighter leading-[0.9]">
          Platform Pintar Untuk <br />
          <span class="text-zinc-600">Skalabilitas Tanpa Batas.</span>
        </h2>
      </div>

      <!-- Bento Mosaic Grid -->
      <div class="grid grid-cols-1 md:grid-cols-4 md:grid-rows-3 gap-4 h-auto md:h-[800px]">
        
        <div v-for="(f, i) in features" :key="i" 
             :class="[
               f.span, 
               'feature-card group relative rounded-[2.5rem] bg-[#0A0A0A] border border-white/5 overflow-hidden transition-all duration-700 hover:border-white/10 hover:bg-[#0D0D0D]'
             ]"
        >
          <!-- Corner Accent Glow (Like image) -->
          <div v-if="f.id === 'analytics'" class="absolute -top-12 -left-12 w-32 h-32 bg-green-500/20 blur-3xl opacity-60 group-hover:opacity-100 transition-opacity"></div>

          <!-- Background Illustration -->
          <div class="absolute inset-0 z-0">
             <component :is="f.component" />
          </div>

          <!-- Content: Top Category -->
          <div class="absolute top-8 left-8 z-20">
             <span class="text-xs font-medium text-zinc-400 tracking-tight">{{ f.category }}</span>
          </div>

          <!-- Content: Bottom Info -->
          <div class="absolute bottom-8 left-8 right-8 z-20">
             <h3 class="text-xl md:text-2xl font-bold tracking-tight mb-1">{{ f.title }}</h3>
             <p class="text-xs md:text-sm text-zinc-500 group-hover:text-zinc-300 transition-colors">
               {{ f.description }}
             </p>
          </div>

          <!-- Optional Icon (Top Right) -->
          <div class="absolute top-8 right-8 opacity-0 group-hover:opacity-100 transition-opacity duration-500">
             <component :is="f.icon" class="w-5 h-5 text-zinc-600" />
          </div>

          <!-- Subtle Gradient Overlay -->
          <div class="absolute inset-0 bg-gradient-to-t from-black via-black/20 to-transparent opacity-60"></div>
        </div>

      </div>

    </div>
  </section>
</template>

<style scoped>
#features {
  font-family: 'Inter', sans-serif;
}

.feature-card {
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

h2, h3 {
  letter-spacing: -0.05em;
}

@media (min-height: 900px) {
  #features {
    height: 100vh;
  }
}

.animate-shimmer {
  animation: shimmer 2s infinite linear;
}

@keyframes shimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}

@media (max-width: 768px) {
  .grid {
    display: flex;
    flex-direction: column;
  }
  .feature-card {
    min-height: 300px;
  }
}
</style>
