<script setup>
import { Check } from 'lucide-vue-next'
import Button from '@/components/ui/Button.vue'

const plans = [
  {
    name: 'Starter',
    price: 'Rp 299rb',
    period: '/bulan',
    description: 'Cocok untuk UMKM yang baru memulai.',
    features: [
      '1 Lokasi Cabang',
      'Hingga 500 Produk',
      'Laporan Dasar',
      'Support Email'
    ],
    highlight: false
  },
  {
    name: 'Business',
    price: 'Rp 799rb',
    period: '/bulan',
    description: 'Pilihan terbaik untuk bisnis yang berkembang.',
    features: [
      'Hingga 5 Lokasi Cabang',
      'Produk Tidak Terbatas',
      'Analitik Lanjutan',
      'Support 24/7 Chat',
      'Integrasi Marketplace'
    ],
    highlight: true
  },
  {
    name: 'Enterprise',
    price: 'Custom',
    period: '',
    description: 'Solusi skala besar untuk korporasi.',
    features: [
      'Lokasi Tidak Terbatas',
      'Custom Development',
      'Account Manager Khusus',
      'SLA Garansi 99.9%',
      'On-premise Deployment'
    ],
    highlight: false
  }
]
</script>

<template>
  <section id="pricing" class="py-24 px-6 bg-zinc-950/50">
    <div class="max-w-7xl mx-auto">
      <!-- Section Header -->
      <div class="text-center mb-16">
        <h2 class="text-4xl md:text-5xl font-medium tracking-tight text-white mb-6">
          Pilih Paket yang <span class="text-white italic">Tepat</span>
        </h2>
        <p class="text-lg text-zinc-400 max-w-2xl mx-auto">
          Mulai gratis dan tingkatkan seiring pertumbuhan bisnis Anda. Tidak ada biaya tersembunyi.
        </p>
      </div>

      <!-- Pricing Cards -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div 
          v-for="(plan, index) in plans" 
          :key="index"
          :class="[
            'relative p-8 rounded-[2.5rem] border transition-all duration-500 flex flex-col',
            plan.highlight 
              ? 'bg-zinc-900 border-white/20 shadow-[0_0_40px_-15px_rgba(255,255,255,0.1)] scale-105 z-10' 
              : 'bg-zinc-900/40 border-white/5 hover:border-white/10'
          ]"
        >
          <div v-if="plan.highlight" class="absolute -top-4 left-1/2 -translate-x-1/2 bg-white text-black text-[10px] font-bold px-4 py-1 rounded-full uppercase tracking-wider">
            Paling Populer
          </div>

          <div class="mb-8">
            <h3 class="text-xl font-semibold text-white mb-2">{{ plan.name }}</h3>
            <p class="text-zinc-400 text-sm">{{ plan.description }}</p>
          </div>

          <div class="mb-8 flex items-baseline gap-1">
            <span class="text-4xl font-bold text-white">{{ plan.price }}</span>
            <span class="text-zinc-500 text-sm">{{ plan.period }}</span>
          </div>

          <div class="space-y-4 mb-10 flex-grow">
            <div v-for="feature in plan.features" :key="feature" class="flex items-center gap-3">
              <div class="shrink-0 w-5 h-5 rounded-full bg-zinc-800 flex items-center justify-center">
                <Check class="w-3 h-3 text-white" />
              </div>
              <span class="text-zinc-300 text-sm">{{ feature }}</span>
            </div>
          </div>

          <Button 
            :variant="plan.highlight ? 'default' : 'outline'"
            class="w-full h-12 rounded-2xl font-semibold transition-all duration-300"
            :class="plan.highlight ? 'bg-white hover:bg-zinc-200 text-black' : 'border-zinc-700 hover:bg-zinc-800 text-white'"
          >
            Pilih Paket
          </Button>
        </div>
      </div>
    </div>
  </section>
</template>
