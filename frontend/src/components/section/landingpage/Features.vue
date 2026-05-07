<script setup>
import { 
  Package, 
  Store, 
  BarChart3, 
  CreditCard, 
  Users, 
  ShieldCheck 
} from 'lucide-vue-next'

const features = [
  {
    title: 'Manajemen Stok Real-time',
    description: 'Pantau stok produk Anda secara akurat di berbagai lokasi secara instan.',
    icon: Package,
    color: 'bg-blue-500/10 text-blue-500'
  },
  {
    title: 'Multi-Lokasi & Cabang',
    description: 'Kelola banyak toko atau gudang dalam satu dashboard terpusat.',
    icon: Store,
    color: 'bg-emerald-500/10 text-emerald-500'
  },
  {
    title: 'Analitik Mendalam',
    description: 'Dapatkan laporan penjualan dan performa bisnis yang komprehensif.',
    icon: BarChart3,
    color: 'bg-purple-500/10 text-purple-500'
  },
  {
    title: 'Sistem Kasir (POS) Modern',
    description: 'Transaksi cepat dan mudah dengan antarmuka yang intuitif.',
    icon: CreditCard,
    color: 'bg-orange-500/10 text-orange-500'
  },
  {
    title: 'Manajemen Pelanggan',
    description: 'Bangun loyalitas pelanggan dengan sistem CRM yang terintegrasi.',
    icon: Users,
    color: 'bg-pink-500/10 text-pink-500'
  },
  {
    title: 'Keamanan & Izin Akses',
    description: 'Kontrol penuh atas siapa yang bisa mengakses data bisnis Anda.',
    icon: ShieldCheck,
    color: 'bg-cyan-500/10 text-cyan-500'
  }
]
</script>

<template>
  <section id="features" class="py-24 px-6 relative overflow-hidden">
    <!-- Section Header -->
    <div class="max-w-4xl mx-auto text-center mb-20">
      <h2 class="text-4xl md:text-5xl font-medium tracking-tight text-white mb-6">
        Solusi Lengkap untuk <span class="text-primary italic">Bisnis Anda</span>
      </h2>
      <p class="text-lg text-zinc-400 max-w-2xl mx-auto">
        Gaptek hadir dengan fitur-fitur canggih yang dirancang khusus untuk mempercepat pertumbuhan bisnis Retail dan F&B Anda.
      </p>
    </div>

    <!-- Features Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-7xl mx-auto">
      <div 
        v-for="(feature, index) in features" 
        :key="index"
        class="group p-8 rounded-3xl bg-zinc-900/50 border border-white/5 hover:border-primary/30 transition-all duration-300 hover:-translate-y-1"
      >
        <div :class="['w-12 h-12 rounded-2xl flex items-center justify-center mb-6 transition-transform duration-500 group-hover:scale-110', feature.color]">
          <component :is="feature.icon" class="w-6 h-6" />
        </div>
        <h3 class="text-xl font-semibold text-white mb-3">{{ feature.title }}</h3>
        <p class="text-zinc-400 leading-relaxed">
          {{ feature.description }}
        </p>
      </div>
    </div>
    
    <!-- Background Decoration -->
    <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-primary/5 rounded-full blur-[120px] pointer-events-none -z-10"></div>
  </section>
</template>
