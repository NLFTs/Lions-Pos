<script setup>
import { onMounted, ref } from 'vue'
import { gsap } from '@/lib/gsap'
import { Icon } from '@iconify/vue'

const containerRef = ref(null)

onMounted(() => {
  const ctx = gsap.context(() => {
    // 1. Cards Floating
    gsap.to('.membership-card', {
      y: -10,
      duration: 2,
      repeat: -1,
      yoyo: true,
      ease: "sine.inOut",
      stagger: 0.3
    })

    // 2. Progress Bar Fill
    gsap.to('.loyalty-progress', {
      width: '85%',
      duration: 3,
      repeat: -1,
      yoyo: true,
      ease: "power2.inOut"
    })

    // 3. Floating Icons
    gsap.to('.reward-icon', {
      y: -15,
      opacity: 0.6,
      duration: 2,
      repeat: -1,
      yoyo: true,
      stagger: 0.4
    })

  }, containerRef.value)
})
</script>

<template>
  <div ref="containerRef" class="w-full h-full relative overflow-hidden flex items-center justify-center bg-transparent">
    
    <!-- Background Gradient -->
    <div class="absolute inset-0 bg-gradient-to-tr from-green-500/5 via-transparent to-blue-500/5"></div>

    <!-- Main Content Container -->
    <div class="relative w-72 h-72 flex flex-col items-center justify-center">
      
      <!-- Membership Cards Stack -->
      <div class="relative w-48 h-32">
        <!-- Platinum Card (Top) -->
        <div class="membership-card absolute top-0 left-0 w-full h-full bg-gradient-to-br from-zinc-800 to-zinc-950 border border-white/20 rounded-xl shadow-2xl p-4 z-30 transform -rotate-3">
          <div class="flex justify-between items-start mb-4">
            <Icon icon="solar:star-bold-duotone" class="w-6 h-6 text-yellow-500" />
            <div class="text-[8px] font-mono text-zinc-500 tracking-[0.2em]">PLATINUM_MEMBER</div>
          </div>
          <div class="space-y-3">
            <div class="w-24 h-2 bg-white/5 rounded-full"></div>
            <div class="flex items-center gap-3">
              <div class="flex-1 h-1.5 bg-white/10 rounded-full overflow-hidden">
                <div class="loyalty-progress h-full bg-green-500 w-0"></div>
              </div>
              <span class="text-[8px] font-mono text-green-400">85%</span>
            </div>
          </div>
        </div>

        <!-- Gold Card (Middle) -->
        <div class="membership-card absolute top-2 left-4 w-full h-full bg-zinc-900 border border-white/10 rounded-xl shadow-xl p-4 z-20 transform rotate-2 opacity-60"></div>
        
        <!-- Silver Card (Bottom) -->
        <div class="membership-card absolute top-4 left-8 w-full h-full bg-zinc-900 border border-white/5 rounded-xl shadow-lg p-4 z-10 transform rotate-6 opacity-30"></div>
      </div>

      <!-- Floating Reward Icons -->
      <div class="reward-icon absolute -top-4 -right-4 p-3 bg-zinc-900/80 border border-white/10 rounded-xl shadow-2xl backdrop-blur-md">
        <Icon icon="solar:gift-bold-duotone" class="w-6 h-6 text-purple-400" />
      </div>
      <div class="reward-icon absolute -bottom-4 -left-4 p-3 bg-zinc-900/80 border border-white/10 rounded-xl shadow-2xl backdrop-blur-md">
        <Icon icon="solar:ticket-bold-duotone" class="w-6 h-6 text-blue-400" />
      </div>

    </div>

    <!-- HUD Elements -->
    <div class="absolute bottom-8 left-8 flex flex-col gap-1 opacity-20">
      <div class="w-32 h-1 bg-white/10 rounded-full"></div>
      <div class="w-20 h-1 bg-white/10 rounded-full"></div>
    </div>

    <div class="absolute top-8 right-8 text-right opacity-30">
      <p class="text-[10px] font-mono text-white tracking-widest uppercase">Loyalty Engine</p>
      <p class="text-[8px] font-mono text-green-500">Tier: Platinum_Active</p>
    </div>

  </div>
</template>

<style scoped>
.vertical-text {
  writing-mode: vertical-rl;
  text-orientation: mixed;
}
</style>


<style scoped>
.crm-ray {
  stroke-dasharray: 4 4;
}
</style>

