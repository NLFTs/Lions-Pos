<script setup>
import { onMounted, ref } from 'vue'
import { gsap } from '@/lib/gsap'
import { Icon } from '@iconify/vue'

const containerRef = ref(null)

onMounted(() => {
  const ctx = gsap.context(() => {
    // 1. Data Packets Flow
    gsap.to('.data-packet', {
      x: 320,
      opacity: 0,
      duration: 2.5,
      repeat: -1,
      stagger: 0.6,
      ease: "none",
      onRepeat: function() {
        // Change color when "hitting" the shield area
        gsap.set(this.targets(), { x: 0, opacity: 0.4 })
      }
    })

    // 2. Shield Pulse
    gsap.to('.shield-glow', {
      scale: 1.2,
      opacity: 0.1,
      duration: 2,
      repeat: -1,
      yoyo: true,
      ease: "sine.inOut"
    })

    // 3. Scanner Grid
    gsap.fromTo('.scanner-bar', 
      { x: -50 }, 
      { x: 350, duration: 4, repeat: -1, ease: "linear" }
    )

  }, containerRef.value)
})
</script>

<template>
  <div ref="containerRef" class="w-full h-full relative overflow-hidden flex items-center justify-center bg-transparent">
    
    <!-- Background Grid -->
    <div class="absolute inset-0 opacity-[0.05]" 
         style="background-image: radial-gradient(circle, white 1px, transparent 1px); background-size: 20px 20px;"></div>

    <!-- Security Hub -->
    <div class="relative w-80 h-80 flex items-center justify-center">
      
      <!-- Firewall Line -->
      <div class="absolute left-1/4 h-48 w-px bg-white/10"></div>

      <!-- Shield Centerpiece -->
      <div class="relative z-20 w-40 h-48 flex items-center justify-center">
        <!-- Shield Glow -->
        <div class="shield-glow absolute w-32 h-32 bg-blue-500/20 rounded-full blur-3xl"></div>
        
        <!-- Main Shield UI -->
        <div class="relative w-28 h-32 bg-zinc-900 border border-white/10 rounded-2xl flex flex-col items-center justify-center shadow-2xl backdrop-blur-xl">
           <Icon icon="solar:shield-keyhole-bold-duotone" class="w-16 h-16 text-blue-500 mb-2" />
           <div class="flex gap-1">
             <div class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
             <span class="text-[8px] font-mono text-zinc-500 uppercase tracking-widest">Protected</span>
           </div>
        </div>

        <!-- Scanning Bar -->
        <div class="scanner-bar absolute h-full w-px bg-blue-400/30 shadow-[0_0_10px_rgba(96,165,250,0.5)] z-30"></div>
      </div>

      <!-- Incoming Data Packets (Threats being filtered) -->
      <div class="absolute left-0 w-full h-48 pointer-events-none overflow-hidden">
        <div v-for="i in 6" :key="i" class="data-packet absolute" 
             :style="{ top: (15 + i*12) + '%', left: '-20px' }">
           <Icon icon="solar:lock-keyhole-minimalistic-bold-duotone" class="w-4 h-4 text-white/30" />
        </div>
      </div>

      <!-- Floating Verified Nodes -->
      <div class="absolute -top-4 -right-4 p-2 bg-green-500/10 border border-green-500/20 rounded-lg backdrop-blur-md opacity-40">
        <Icon icon="solar:check-read-bold-duotone" class="w-4 h-4 text-green-400" />
      </div>
      <div class="absolute -bottom-4 -right-8 p-2 bg-blue-500/10 border border-blue-500/20 rounded-lg backdrop-blur-md opacity-40">
        <Icon icon="solar:key-bold-duotone" class="w-4 h-4 text-blue-400" />
      </div>

    </div>

    <!-- HUD Label -->
    <div class="absolute bottom-10 left-1/2 -translate-x-1/2">
       <div class="px-4 py-1 bg-black/40 border border-white/5 rounded-full backdrop-blur-md">
          <p class="text-[9px] font-mono text-zinc-500 tracking-[0.3em] uppercase italic">Encrypted_Data_Vault</p>
       </div>
    </div>

  </div>
</template>

<style scoped>
/* Grounded and serious aesthetics */
</style>

