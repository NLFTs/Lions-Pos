<script setup>
import { onMounted, ref } from 'vue'
import { gsap } from '@/lib/gsap'
import { Icon } from '@iconify/vue'

const containerRef = ref(null)

onMounted(() => {
  const ctx = gsap.context(() => {
    const tl = gsap.timeline({ repeat: -1, repeatDelay: 1 })

    // 1. Scan animation
    tl.fromTo('.scanner-beam', 
      { y: 0, opacity: 0 }, 
      { y: 100, opacity: 1, duration: 1.5, ease: "power1.inOut" }
    )
    .to('.scanner-beam', { opacity: 0, duration: 0.2 })

    // 2. Item appears on screen
    tl.fromTo('.pos-item', 
      { x: -20, opacity: 0 }, 
      { x: 0, opacity: 1, duration: 0.5, stagger: 0.2, ease: "back.out(1.7)" },
      "-=0.5"
    )

    // 3. Payment process
    tl.to('.payment-card', {
      y: -20,
      opacity: 1,
      duration: 0.8,
      ease: "power2.out"
    })
    .to('.payment-status', {
      scale: 1,
      opacity: 1,
      duration: 0.5,
      ease: "elastic.out(1, 0.5)"
    })

    // 4. Receipt prints
    tl.to('.receipt-paper', {
      height: 80,
      duration: 1,
      ease: "power1.inOut"
    })

    // Floating elements
    gsap.to('.floating-icon', {
      y: -10,
      duration: 2,
      repeat: -1,
      yoyo: true,
      ease: "sine.inOut",
      stagger: 0.3
    })

  }, containerRef.value)
})
</script>

<template>
  <div ref="containerRef" class="w-full h-full relative overflow-hidden flex items-center justify-center bg-transparent">
    <!-- Background Decor -->
    <div class="absolute inset-0 flex items-center justify-center pointer-events-none opacity-[0.03]">
      <Icon icon="solar:globus-bold-duotone" class="w-[500px] h-[500px] animate-pulse" />
    </div>

    <!-- Main POS Assembly -->
    <div class="relative w-64 h-64 flex flex-col items-center justify-center scale-110 lg:scale-125">
      
      <!-- Tablet / Monitor -->
      <div class="relative w-48 h-36 bg-zinc-900 border-4 border-zinc-800 rounded-xl shadow-2xl overflow-hidden flex flex-col">
        <!-- Screen Header -->
        <div class="h-6 bg-zinc-800 flex items-center px-2 justify-between">
          <div class="flex gap-1">
            <div class="w-1.5 h-1.5 rounded-full bg-red-500/50"></div>
            <div class="w-1.5 h-1.5 rounded-full bg-yellow-500/50"></div>
            <div class="w-1.5 h-1.5 rounded-full bg-green-500/50"></div>
          </div>
          <div class="text-[8px] text-zinc-500 font-mono tracking-tighter">TERMINAL_01</div>
        </div>

        <!-- Screen Content (Simulated UI) -->
        <div class="flex-1 p-3 flex flex-col gap-2">
          <div class="pos-item w-full h-2 bg-white/10 rounded-full"></div>
          <div class="pos-item w-full h-2 bg-white/10 rounded-full"></div>
          <div class="pos-item w-2/3 h-2 bg-white/10 rounded-full"></div>
          
          <div class="mt-auto flex justify-between items-end border-t border-white/5 pt-2">
            <div class="flex flex-col gap-1">
              <div class="w-8 h-1.5 bg-white/5 rounded-full"></div>
              <div class="w-12 h-2.5 bg-green-500/20 rounded-full border border-green-500/30"></div>
            </div>
            <div class="payment-status opacity-0 scale-50">
              <Icon icon="solar:check-circle-bold-duotone" class="w-5 h-5 text-green-500" />
            </div>
          </div>
        </div>

        <!-- Scanning Glow -->
        <div class="scanner-beam absolute top-0 left-0 w-full h-0.5 bg-green-400 shadow-[0_0_10px_rgba(74,222,128,0.8)] opacity-0"></div>
      </div>

      <!-- Stand -->
      <div class="w-20 h-8 bg-zinc-800/80 rounded-t-lg -mt-1 transform perspective-[100px] rotateX(20deg)"></div>

      <!-- Peripheral Elements -->
      
      <!-- Receipt Printer -->
      <div class="absolute -right-4 bottom-12 w-16 h-12 bg-zinc-800 rounded-md shadow-lg border border-white/5 flex flex-col items-center">
        <div class="w-12 h-1 bg-zinc-950 mt-2 rounded-full"></div>
        <div class="receipt-paper w-10 h-0 bg-white/90 shadow-sm mt-0 relative overflow-hidden">
          <div class="p-1 flex flex-col gap-0.5">
            <div v-for="i in 5" :key="i" class="w-full h-[1px] bg-zinc-300"></div>
          </div>
        </div>
      </div>

      <!-- Floating Payment Card -->
      <div class="payment-card absolute -left-8 top-1/2 opacity-0 -translate-y-1/2">
        <div class="w-12 h-8 bg-gradient-to-br from-zinc-700 to-zinc-900 border border-white/10 rounded-md shadow-xl flex items-center justify-center">
          <Icon icon="solar:card-2-bold-duotone" class="w-6 h-6 text-white/40" />
        </div>
      </div>

      <!-- Floating Icons -->
      <div class="floating-icon absolute -top-4 -right-2 p-2 bg-green-500/10 border border-green-500/20 rounded-lg backdrop-blur-md">
        <Icon icon="solar:box-minimalistic-bold-duotone" class="w-5 h-5 text-green-400" />
      </div>
      <div class="floating-icon absolute -bottom-4 -left-2 p-2 bg-blue-500/10 border border-blue-500/20 rounded-lg backdrop-blur-md">
        <Icon icon="solar:scanner-bold-duotone" class="w-5 h-5 text-blue-400" />
      </div>

    </div>

    <!-- HUD Text -->
    <div class="absolute bottom-8 left-0 w-full text-center">
      <p class="text-[10px] font-mono text-white/20 tracking-widest uppercase">
        Gaptek Transaction Engine // POS_SYS_v4
      </p>
    </div>
  </div>
</template>

<style scoped>
/* Glassmorphism effects if needed */
</style>

