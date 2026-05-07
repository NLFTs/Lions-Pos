<script setup>
import { 
  UserPlus, 
  Settings2, 
  Rocket 
} from 'lucide-vue-next'

const steps = [
  {
    number: '01',
    title: 'Daftar Akun',
    description: 'Buat akun bisnis Anda dalam hitungan menit dan lengkapi profil toko.',
    icon: UserPlus
  },
  {
    number: '02',
    title: 'Konfigurasi Sistem',
    description: 'Atur kategori, produk, lokasi, dan hak akses tim sesuai kebutuhan operasional.',
    icon: Settings2
  },
  {
    number: '03',
    title: 'Siap Beroperasi',
    description: 'Mulai kelola stok, transaksi, dan pantau pertumbuhan bisnis Anda secara real-time.',
    icon: Rocket
  }
]
</script>

<template>
  <section class="py-24 px-6 relative">
    <div class="max-w-7xl mx-auto">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
        <!-- Left Side: Content -->
        <div>
          <h2 class="text-4xl md:text-5xl font-medium tracking-tight text-white mb-8 leading-tight">
            Mulai Transformasi Digital <br />
            Hanya dalam <span class="text-primary italic">3 Langkah Mudah</span>
          </h2>
          <p class="text-lg text-zinc-400 mb-12 max-w-xl">
            Gaptek dirancang agar mudah digunakan oleh siapa saja, tanpa perlu latar belakang teknis yang mendalam.
          </p>

          <div class="space-y-12">
            <div v-for="step in steps" :key="step.number" class="flex gap-6">
              <div class="shrink-0 w-12 h-12 rounded-2xl bg-zinc-900 border border-white/5 flex items-center justify-center text-primary font-bold text-lg">
                {{ step.number }}
              </div>
              <div>
                <h3 class="text-xl font-semibold text-white mb-2 flex items-center gap-2">
                  <component :is="step.icon" class="w-5 h-5 text-zinc-500" />
                  {{ step.title }}
                </h3>
                <p class="text-zinc-400 leading-relaxed max-w-md">
                  {{ step.description }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Side: Decorative Image/Mockup -->
        <div class="relative group">
          <div class="absolute inset-0 bg-primary/20 rounded-[3rem] blur-3xl opacity-20 group-hover:opacity-40 transition-opacity duration-500"></div>
          <div class="relative aspect-square rounded-[3rem] bg-gradient-to-br from-zinc-800 to-zinc-950 border border-white/10 overflow-hidden shadow-2xl flex items-center justify-center p-12">
            <!-- Simulated UI element -->
            <div class="w-full h-full bg-black/40 rounded-2xl border border-white/5 backdrop-blur-sm p-6 flex flex-col gap-4">
              <div class="h-8 w-1/3 bg-zinc-800 rounded-lg"></div>
              <div class="grid grid-cols-2 gap-4 h-full">
                <div class="bg-zinc-900/80 rounded-xl border border-white/5 p-4 flex flex-col justify-between">
                  <div class="w-8 h-8 rounded-full bg-primary/20"></div>
                  <div class="space-y-2">
                    <div class="h-2 w-full bg-zinc-800 rounded"></div>
                    <div class="h-2 w-2/3 bg-zinc-800 rounded"></div>
                  </div>
                </div>
                <div class="bg-zinc-900/80 rounded-xl border border-white/5 p-4 flex flex-col justify-between">
                  <div class="w-8 h-8 rounded-full bg-blue-500/20"></div>
                  <div class="space-y-2">
                    <div class="h-2 w-full bg-zinc-800 rounded"></div>
                    <div class="h-2 w-2/3 bg-zinc-800 rounded"></div>
                  </div>
                </div>
                <div class="col-span-2 bg-zinc-900/80 rounded-xl border border-white/5 p-4 space-y-4">
                  <div class="flex justify-between items-center">
                    <div class="h-3 w-1/4 bg-zinc-800 rounded"></div>
                    <div class="h-3 w-1/6 bg-primary/40 rounded"></div>
                  </div>
                  <div class="h-20 w-full bg-zinc-950/50 rounded-lg border border-white/5 flex items-end p-2 gap-2">
                    <div class="w-full bg-primary/30 h-1/2 rounded-t-sm"></div>
                    <div class="w-full bg-primary/30 h-3/4 rounded-t-sm"></div>
                    <div class="w-full bg-primary h-full rounded-t-sm"></div>
                    <div class="w-full bg-primary/30 h-2/3 rounded-t-sm"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
