<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import LandingLayout from '@/components/section/landingpage/LandingLayout.vue'
import Navbar from '@/components/section/landingpage/Navbar.vue'
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import { 
  Zap, 
  Check, 
  X, 
  Crown, 
  Store, 
  Building2, 
  Sparkles,
  Users,
  Database,
  Headphones,
  TrendingUp,
  Package,
  Truck,
  BarChart3,
  Shield,
  Gift,
  ArrowRight
} from 'lucide-vue-next'

const router = useRouter()

const navigationItems = [
  { name: 'Beranda', path: '/' },
  { name: 'Fitur', path: '/#features' },
  { name: 'Testimoni', path: '/#testimonials' },
  { name: 'Tentang', path: '/about' }
]

const selectedBilling = ref('monthly') // 'monthly' or 'yearly'
const hoveredPlan = ref(null)

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

const plans = [
  {
    id: 'basic',
    name: 'Basic',
    icon: Store,
    description: 'Cocok untuk toko retail kecil yang baru memulai digitalisasi.',
    price: {
      monthly: 199000,
      yearly: 1990000
    },
    featured: false,
    buttonText: 'Pilih Paket',
    buttonVariant: 'outline',
    features: [
      { name: 'Maksimal 1 cabang', included: true },
      { name: 'Maksimal 3 pengguna', included: true },
      { name: 'Manajemen produk (1000 SKU)', included: true },
      { name: 'Transaksi kasir', included: true },
      { name: 'Laporan penjualan dasar', included: true },
      { name: 'Manajemen stok', included: true },
      { name: 'Supplier management', included: true },
      { name: 'Purchase order', included: false },
      { name: 'Manajemen karyawan', included: false },
      { name: 'Akses API', included: false },
      { name: 'Support 24/7', included: false },
      { name: 'Custom branding', included: false }
    ]
  },
  {
    id: 'pro',
    name: 'Pro',
    icon: Building2,
    description: 'Solusi lengkap untuk bisnis dengan multi-cabang dan tim besar.',
    price: {
      monthly: 499000,
      yearly: 4990000
    },
    featured: true,
    buttonText: 'Pilih Paket',
    buttonVariant: 'default',
    badge: 'Paling Populer',
    features: [
      { name: 'Maksimal 10 cabang', included: true },
      { name: 'Maksimal 20 pengguna', included: true },
      { name: 'Manajemen produk (10.000 SKU)', included: true },
      { name: 'Transaksi kasir', included: true },
      { name: 'Laporan penjualan lanjutan', included: true },
      { name: 'Manajemen stok real-time', included: true },
      { name: 'Supplier management', included: true },
      { name: 'Purchase order', included: true },
      { name: 'Manajemen karyawan', included: true },
      { name: 'Akses API', included: true },
      { name: 'Support 24/7 prioritas', included: true },
      { name: 'Custom branding', included: false }
    ]
  },
  {
    id: 'enterprise',
    name: 'Enterprise',
    icon: Crown,
    description: 'Untuk korporasi besar dengan kebutuhan khusus dan skalabilitas tinggi.',
    price: {
      monthly: null,
      yearly: null
    },
    featured: false,
    buttonText: 'Hubungi Sales',
    buttonVariant: 'outline',
    customPrice: true,
    features: [
      { name: 'Unlimited cabang', included: true },
      { name: 'Unlimited pengguna', included: true },
      { name: 'Manajemen produk unlimited', included: true },
      { name: 'Transaksi kasir', included: true },
      { name: 'Laporan kustom', included: true },
      { name: 'Manajemen stok real-time', included: true },
      { name: 'Supplier management', included: true },
      { name: 'Purchase order', included: true },
      { name: 'Manajemen karyawan', included: true },
      { name: 'Akses API premium', included: true },
      { name: 'Dedicated support 24/7', included: true },
      { name: 'Custom branding & white label', included: true }
    ]
  }
]

const formatPrice = (price) => {
  if (!price) return 'Kustom'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(price)
}

const getYearlyDiscount = (monthlyPrice, yearlyPrice) => {
  if (!monthlyPrice || !yearlyPrice) return null
  const monthlyTotal = monthlyPrice * 12
  const discount = monthlyTotal - yearlyPrice
  const discountPercent = Math.round((discount / monthlyTotal) * 100)
  return discountPercent
}

const handleSelectPlan = (plan) => {
  if (plan.id === 'enterprise') {
    window.location.href = 'mailto:sales@gaptek.com'
  } else {
    router.push({
      path: '/register',
      query: { plan: plan.id, billing: selectedBilling.value }
    })
  }
}

// Smooth scroll ke pricing section
onMounted(() => {
  if (window.location.hash === '#pricing') {
    setTimeout(() => {
      const el = document.querySelector('#pricing-section')
      if (el) el.scrollIntoView({ behavior: 'smooth' })
    }, 300)
  }
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
        <div class="h-full w-full bg-primary flex items-center justify-center rounded-lg">
          <Zap class="h-4 w-4 text-white" />
        </div>
      </template>
      <template #actions>
        <Button 
          variant="ghost" 
          class="font-medium px-5 py-2 rounded-xl text-zinc-300 hover:text-white hover:bg-white/10 transition-all"
          @click="router.push('/login')"
        >
          Masuk
        </Button>
        <Button 
          class="font-bold px-6 py-2 rounded-xl bg-primary text-white hover:bg-primary/90 transition-all active:scale-95"
          @click="router.push('/register')"
        >
          Daftar Gratis
        </Button>
      </template>
    </Navbar>

    <main class="relative flex-grow flex flex-col items-center pt-20 pb-24 px-6 overflow-hidden">
      <div class="relative z-10 max-w-6xl mx-auto w-full">
        
        <!-- Hero Section -->
        <div class="text-center space-y-6 mb-16">
          <div class="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 border border-primary/20">
            <Sparkles class="w-4 h-4 text-primary" />
            <span class="text-xs font-medium text-primary tracking-wide">Pilih Paket Sesuai Kebutuhan</span>
          </div>
          
          <h1 class="text-5xl md:text-6xl lg:text-7xl font-bold tracking-tight text-white leading-[1.1]">
            Pilih <span class="text-primary">Plan</span> yang Tepat
            <br class="hidden md:block" />
            untuk Bisnis Anda
          </h1>
          
          <p class="text-lg md:text-xl text-zinc-400 max-w-2xl mx-auto">
            Mulai dari toko kecil hingga enterprise besar, Gaptek punya solusi untuk semua skala bisnis.
          </p>
        </div>

        <!-- Billing Toggle -->
        <div class="flex justify-center mb-12">
          <div class="inline-flex items-center gap-2 p-1 rounded-2xl bg-white/5 border border-white/10">
            <button
              @click="selectedBilling = 'monthly'"
              :class="[
                'px-6 py-2.5 rounded-xl font-semibold text-sm transition-all duration-200',
                selectedBilling === 'monthly' 
                  ? 'bg-primary text-white shadow-lg' 
                  : 'text-zinc-400 hover:text-white'
              ]"
            >
              Bulanan
            </button>
            <button
              @click="selectedBilling = 'yearly'"
              :class="[
                'px-6 py-2.5 rounded-xl font-semibold text-sm transition-all duration-200 flex items-center gap-2',
                selectedBilling === 'yearly' 
                  ? 'bg-primary text-white shadow-lg' 
                  : 'text-zinc-400 hover:text-white'
              ]"
            >
              Tahunan
              <span class="text-[10px] bg-emerald-500/20 text-emerald-400 px-1.5 py-0.5 rounded-full">Hemat 20%</span>
            </button>
          </div>
        </div>

        <!-- Pricing Cards -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mb-24">
          <div 
            v-for="plan in plans" 
            :key="plan.id"
            @mouseenter="hoveredPlan = plan.id"
            @mouseleave="hoveredPlan = null"
            :class="[
              'relative rounded-2xl transition-all duration-300',
              plan.featured 
                ? 'bg-gradient-to-b from-white/10 to-white/5 border-2 border-primary/50 shadow-2xl shadow-primary/10 scale-105 md:scale-105' 
                : 'bg-white/5 border border-white/10 hover:border-white/30 hover:scale-[1.02]'
            ]"
          >
            <!-- Badge untuk Pro Plan -->
            <div v-if="plan.badge" class="absolute -top-3 left-1/2 -translate-x-1/2">
              <div class="bg-gradient-to-r from-primary to-primary/80 text-white text-xs font-bold px-4 py-1.5 rounded-full shadow-lg">
                {{ plan.badge }}
              </div>
            </div>

            <div class="p-8">
              <!-- Plan Icon & Name -->
              <div class="flex items-center gap-3 mb-4">
                <div :class="[
                  'w-12 h-12 rounded-xl flex items-center justify-center',
                  plan.featured ? 'bg-primary/20' : 'bg-white/10'
                ]">
                  <component :is="plan.icon" class="w-6 h-6 text-primary" />
                </div>
                <h3 class="text-xl font-bold text-white">{{ plan.name }}</h3>
              </div>

              <!-- Description -->
              <p class="text-zinc-400 text-sm mb-6 leading-relaxed">{{ plan.description }}</p>

              <!-- Price -->
              <div class="mb-6">
                <template v-if="plan.customPrice">
                  <div class="text-2xl font-bold text-white">Hubungi Kami</div>
                  <p class="text-xs text-zinc-500 mt-1">Solusi kustom untuk kebutuhan Anda</p>
                </template>
                <template v-else>
                  <div class="flex items-baseline gap-1">
                    <span class="text-3xl font-bold text-white">{{ formatPrice(plan.price[selectedBilling]) }}</span>
                    <span class="text-zinc-500 text-sm">/{{ selectedBilling === 'monthly' ? 'bulan' : 'tahun' }}</span>
                  </div>
                  <div v-if="selectedBilling === 'yearly' && getYearlyDiscount(plan.price.monthly, plan.price.yearly)" class="mt-2">
                    <span class="text-xs text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-full">
                      Hemat {{ getYearlyDiscount(plan.price.monthly, plan.price.yearly) }}%
                    </span>
                  </div>
                </template>
              </div>

              <!-- CTA Button -->
              <Button 
                :variant="plan.buttonVariant"
                :class="[
                  'w-full mb-8 font-semibold py-6 rounded-xl transition-all',
                  plan.featured 
                    ? 'bg-primary hover:bg-primary/90 text-white shadow-lg shadow-primary/25' 
                    : 'border-white/20 text-white hover:bg-white/10'
                ]"
                @click="handleSelectPlan(plan)"
              >
                {{ plan.buttonText }}
                <ArrowRight class="w-4 h-4 ml-2" />
              </Button>

              <!-- Features List -->
              <div class="space-y-3 pt-4 border-t border-white/10">
                <p class="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-3">
                  Yang Anda Dapatkan:
                </p>
                <div 
                  v-for="feature in plan.features" 
                  :key="feature.name"
                  class="flex items-center gap-3 text-sm"
                >
                  <Check v-if="feature.included" class="w-4 h-4 text-emerald-500 shrink-0" />
                  <X v-else class="w-4 h-4 text-zinc-600 shrink-0" />
                  <span :class="feature.included ? 'text-zinc-300' : 'text-zinc-500 line-through'">
                    {{ feature.name }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Feature Comparison Table (Mobile Friendly) -->
        <div class="mt-16 mb-24">
          <div class="text-center mb-12">
            <h2 class="text-2xl md:text-3xl font-bold text-white mb-4">
              Bandingkan Semua Fitur
            </h2>
            <p class="text-zinc-400 max-w-xl mx-auto">
              Lihat lebih detail apa saja yang Anda dapatkan di setiap paket
            </p>
          </div>

          <Card class="bg-white/5 border-white/10 overflow-hidden">
            <CardContent class="p-0 overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="border-b border-white/10 bg-white/5">
                    <th class="px-6 py-4 text-left text-zinc-400 font-semibold">Fitur</th>
                    <th class="px-6 py-4 text-center text-zinc-400 font-semibold">Basic</th>
                    <th class="px-6 py-4 text-center text-primary font-semibold">Pro</th>
                    <th class="px-6 py-4 text-center text-zinc-400 font-semibold">Enterprise</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="feature in plans[0].features" :key="feature.name" class="border-b border-white/5">
                    <td class="px-6 py-3 text-white font-medium">{{ feature.name }}</td>
                    <td class="px-6 py-3 text-center">
                      <Check v-if="plans[0].features.find(f => f.name === feature.name)?.included" class="w-5 h-5 text-emerald-500 mx-auto" />
                      <X v-else class="w-5 h-5 text-zinc-600 mx-auto" />
                    </td>
                    <td class="px-6 py-3 text-center">
                      <Check v-if="plans[1].features.find(f => f.name === feature.name)?.included" class="w-5 h-5 text-emerald-500 mx-auto" />
                      <X v-else class="w-5 h-5 text-zinc-600 mx-auto" />
                    </td>
                    <td class="px-6 py-3 text-center">
                      <Check v-if="plans[2].features.find(f => f.name === feature.name)?.included" class="w-5 h-5 text-emerald-500 mx-auto" />
                      <X v-else class="w-5 h-5 text-zinc-600 mx-auto" />
                    </td>
                  </tr>
                </tbody>
              </table>
            </CardContent>
          </Card>
        </div>

        <!-- FAQ / CTA Section -->
        <div class="text-center py-16 rounded-3xl bg-gradient-to-r from-primary/10 via-primary/5 to-transparent border border-primary/20">
          <h2 class="text-3xl font-bold text-white mb-4">
            Masih Ragu?
          </h2>
          <p class="text-zinc-400 max-w-md mx-auto mb-8">
            Konsultasikan kebutuhan bisnis Anda dengan tim kami secara gratis.
          </p>
          <div class="flex flex-col sm:flex-row gap-4 justify-center">
            <Button 
              variant="outline"
              class="border-white/20 text-white hover:bg-white/10 px-8 py-6 rounded-xl"
              @click="router.push('/contact')"
            >
              <Headphones class="w-4 h-4 mr-2" />
              Hubungi Kami
            </Button>
            <Button 
              class="bg-white text-zinc-900 hover:bg-white/90 px-8 py-6 rounded-xl font-semibold"
              @click="router.push('/register')"
            >
              <Gift class="w-4 h-4 mr-2" />
              Coba Gratis 14 Hari
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
/* Animasi smooth untuk card */
.grid > div {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

/* Custom scrollbar untuk tabel */
.overflow-x-auto::-webkit-scrollbar {
  height: 6px;
}
.overflow-x-auto::-webkit-scrollbar-track {
  background: rgba(255,255,255,0.05);
  border-radius: 10px;
}
.overflow-x-auto::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.2);
  border-radius: 10px;
}
</style>