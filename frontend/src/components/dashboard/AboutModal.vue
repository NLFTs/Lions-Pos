<script setup>
import { X, Zap, Heart, Shield, Rocket, Globe } from 'lucide-vue-next'

defineProps({
  isOpen: Boolean
})

const emit = defineEmits(['close'])

const close = () => {
  emit('close')
}
</script>

<template>
  <Transition
    enter-active-class="transition duration-300 ease-out"
    enter-from-class="opacity-0 scale-95"
    enter-to-class="opacity-100 scale-100"
    leave-active-class="transition duration-200 ease-in"
    leave-from-class="opacity-100 scale-100"
    leave-to-class="opacity-0 scale-95"
  >
    <div v-if="isOpen" class="fixed inset-0 z-[100] flex items-center justify-center p-4 sm:p-6">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-zinc-950/60 backdrop-blur-md" @click="close"></div>

      <!-- Modal Content -->
      <div class="relative w-full max-w-2xl overflow-hidden rounded-3xl bg-white dark:bg-zinc-900 shadow-2xl border border-white/20 dark:border-zinc-800">
        <!-- Decoration Gradients -->
        <div class="absolute -top-24 -right-24 w-64 h-64 bg-zinc-500/10 rounded-full blur-3xl"></div>
        <div class="absolute -bottom-24 -left-24 w-64 h-64 bg-zinc-500/10 rounded-full blur-3xl"></div>

        <!-- Header -->
        <div class="relative flex items-center justify-between p-6 border-b border-border/50">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-primary rounded-xl flex items-center justify-center shadow-lg shadow-primary/20">
              <Zap class="w-6 h-6 text-primary-foreground" />
            </div>
            <div>
              <h2 class="text-xl font-bold tracking-tight">Tentang Gaptek</h2>
              <p class="text-xs text-muted-foreground">Versi 1.0.0 • Premium Dashboard</p>
            </div>
          </div>
          <button 
            @click="close"
            class="p-2 rounded-full hover:bg-muted transition-colors text-muted-foreground hover:text-foreground"
          >
            <X class="w-5 h-5" />
          </button>
        </div>

        <!-- Body -->
        <div class="relative p-8 space-y-8 overflow-y-auto max-h-[70vh]">
          <!-- Hero Text -->
          <div class="space-y-4">
            <h3 class="text-3xl font-bold leading-tight text-white">
              Masa Depan Manajemen Bisnis Ada di Tangan Anda.
            </h3>
            <p class="text-zinc-600 dark:text-zinc-400 leading-relaxed text-lg">
              Gaptek bukan sekadar aplikasi manajemen. Kami adalah partner strategis yang membantu Anda mengubah kompleksitas operasional menjadi kemudahan yang elegan. Dirancang untuk kecepatan, dibangun untuk pertumbuhan.
            </p>
          </div>

          <!-- Features Grid -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="p-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800/50 border border-border/50 flex gap-4">
              <div class="w-12 h-12 rounded-xl bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center shrink-0">
                <Rocket class="w-6 h-6 text-zinc-600 dark:text-zinc-400" />
              </div>
              <div>
                <h4 class="font-bold text-sm">Performa Maksimal</h4>
                <p class="text-xs text-muted-foreground mt-1">Kecepatan akses data real-time tanpa kompromi.</p>
              </div>
            </div>
            <div class="p-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800/50 border border-border/50 flex gap-4">
              <div class="w-12 h-12 rounded-xl bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center shrink-0">
                <Heart class="w-6 h-6 text-zinc-600 dark:text-zinc-400" />
              </div>
              <div>
                <h4 class="font-bold text-sm">User Centric</h4>
                <p class="text-xs text-muted-foreground mt-1">Antarmuka intuitif yang memanjakan mata.</p>
              </div>
            </div>
            <div class="p-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800/50 border border-border/50 flex gap-4">
              <div class="w-12 h-12 rounded-xl bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center shrink-0">
                <Shield class="w-6 h-6 text-zinc-600 dark:text-zinc-400" />
              </div>
              <div>
                <h4 class="font-bold text-sm">Keamanan Ketat</h4>
                <p class="text-xs text-muted-foreground mt-1">Data Anda terlindungi dengan enkripsi standar industri.</p>
              </div>
            </div>
            <div class="p-4 rounded-2xl bg-zinc-50 dark:bg-zinc-800/50 border border-border/50 flex gap-4">
              <div class="w-12 h-12 rounded-xl bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center shrink-0">
                <Globe class="w-6 h-6 text-zinc-600 dark:text-zinc-400" />
              </div>
              <div>
                <h4 class="font-bold text-sm">Akses Dimanapun</h4>
                <p class="text-xs text-muted-foreground mt-1">Pantau bisnis Anda kapan saja, di mana saja.</p>
              </div>
            </div>
          </div>

          <!-- Footer Quote -->
          <div class="pt-4 text-center">
            <p class="text-sm font-medium italic text-muted-foreground">
              "Building the bridge between technology and your business success."
            </p>
            <div class="mt-6 flex justify-center gap-4">
              <div class="px-4 py-2 rounded-full bg-zinc-100 dark:bg-zinc-800 text-[10px] font-bold uppercase tracking-wider text-zinc-500">
                #ModernERP
              </div>
              <div class="px-4 py-2 rounded-full bg-zinc-100 dark:bg-zinc-800 text-[10px] font-bold uppercase tracking-wider text-zinc-500">
                #Efficiency
              </div>
              <div class="px-4 py-2 rounded-full bg-zinc-100 dark:bg-zinc-800 text-[10px] font-bold uppercase tracking-wider text-zinc-500">
                #Growth
              </div>
            </div>
          </div>
        </div>

        <!-- Action Footer -->
        <div class="p-6 bg-zinc-50 dark:bg-zinc-800/30 border-t border-border/50 flex justify-end">
          <button 
            @click="close"
            class="px-6 py-2.5 rounded-xl bg-zinc-900 dark:bg-white text-white dark:text-zinc-900 font-bold text-sm hover:scale-105 transition-transform active:scale-95 shadow-lg shadow-black/10"
          >
            Tutup & Lanjutkan
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>
