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
    <Navbar brand-name="gaptek" :nav-items="navigationItems" @navigate="handleNavigation">
      <template #logo>
        <div class="h-8 w-8 bg-white flex items-center justify-center rounded-md">
          <Zap class="h-4 w-4 text-black" />
        </div>
      </template>
      <template #actions>
        <Button variant="ghost" class="text-xs font-medium text-zinc-400 hover:text-white transition-colors">
          Masuk
        </Button>
        <Button class="text-xs font-medium bg-white text-black hover:bg-zinc-200 px-4 py-2 rounded-md transition-all">
          Daftar
        </Button>
      </template>
    </Navbar>

    <main class="min-h-screen bg-black text-white py-20 px-6">
      <div class="max-w-5xl mx-auto">
        
        <div class="text-center space-y-4 mb-16">
          <h1 class="text-5xl md:text-6xl font-medium tracking-tighter">
            Pilih plan yang tepat.
          </h1>
          <p class="text-lg text-zinc-400 max-w-xl mx-auto">
            Solusi yang dirancang untuk skala bisnis Anda. Dari pemula hingga enterprise.
          </p>
        </div>

        <div class="flex justify-center mb-16">
          <div class="inline-flex items-center p-1 rounded-lg bg-zinc-900 border border-zinc-800">
            <button @click="selectedBilling = 'monthly'" 
              :class="['px-6 py-1.5 rounded text-sm transition-all', selectedBilling === 'monthly' ? 'bg-white text-black' : 'text-zinc-400 hover:text-white']">
              Bulanan
            </button>
            <button @click="selectedBilling = 'yearly'" 
              :class="['px-6 py-1.5 rounded text-sm transition-all', selectedBilling === 'yearly' ? 'bg-white text-black' : 'text-zinc-400 hover:text-white']">
              Tahunan
            </button>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-24">
          <div 
            v-for="plan in plans" 
            :key="plan.id"
            class="flex flex-col p-8 rounded-xl border border-zinc-800 bg-black hover:border-zinc-600 transition-colors"
          >
            <h3 class="text-sm font-semibold text-white mb-2">{{ plan.name }}</h3>
            <div class="text-4xl font-semibold tracking-tighter mb-4">
              {{ plan.customPrice ? 'Custom' : formatPrice(plan.price[selectedBilling]) }}
              <span v-if="!plan.customPrice" class="text-sm font-normal text-zinc-500">/{{ selectedBilling === 'monthly' ? 'mo' : 'yr' }}</span>
            </div>
            <p class="text-sm text-zinc-400 mb-8 flex-grow">{{ plan.description }}</p>
            
            <Button :class="[plan.featured ? 'bg-white text-black hover:bg-zinc-200' : 'bg-zinc-900 text-white hover:bg-zinc-800', 'w-full py-2 rounded-md font-medium text-sm transition-all mb-8']">
              {{ plan.buttonText }}
            </Button>

            <div class="space-y-3 pt-6 border-t border-zinc-900">
              <div v-for="feature in plan.features" :key="feature.name" class="flex items-center gap-3 text-sm">
                <Check v-if="feature.included" class="w-4 h-4 text-white" />
                <span :class="feature.included ? 'text-zinc-300' : 'text-zinc-600'">{{ feature.name }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="mt-20">
          <h2 class="text-xl font-medium mb-8">Perbandingan fitur</h2>
          <div class="border border-zinc-800 rounded-xl overflow-hidden">
            <table class="w-full text-sm">
              <thead class="bg-zinc-900/50">
                <tr class="border-b border-zinc-800">
                  <th class="px-6 py-4 text-left font-medium text-zinc-400">Fitur</th>
                  <th class="px-6 py-4 text-center font-medium text-zinc-400">Basic</th>
                  <th class="px-6 py-4 text-center font-medium text-zinc-400">Pro</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="feature in plans[0].features" :key="feature.name" class="border-b border-zinc-900 last:border-0">
                  <td class="px-6 py-4">{{ feature.name }}</td>
                  <td class="px-6 py-4 text-center text-zinc-500">{{ feature.included ? '✓' : '—' }}</td>
                  <td class="px-6 py-4 text-center text-zinc-500">{{ plans[1].features.find(f => f.name === feature.name)?.included ? '✓' : '—' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </main>

    <footer class="py-12 border-t border-zinc-900 text-center">
      <p class="text-xs text-zinc-600">© 2026 Gaptek Inc.</p>
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