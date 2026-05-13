<script setup>
import { onMounted, ref } from 'vue'
import { gsap } from '@/lib/gsap'
import { 
  TrendingUp, 
  TrendingDown, 
  Activity, 
  DollarSign, 
  BarChart3,
  ArrowUpRight
} from 'lucide-vue-next'

const containerRef = ref(null)

onMounted(() => {
  const ctx = gsap.context(() => {
    // 1. Candlestick "live" effect
    gsap.to('.candle', {
      scaleY: () => 0.8 + Math.random() * 0.4,
      duration: 1,
      repeat: -1,
      yoyo: true,
      ease: "sine.inOut",
      stagger: 0.1
    })

    // 2. Order book scrolling
    gsap.to('.order-row', {
      y: -20,
      duration: 1,
      repeat: -1,
      ease: "none",
      stagger: 0.2,
      onRepeat: function() {
        gsap.set(this.targets(), { y: 0 })
      }
    })

    // 3. Price Ticker pulse
    gsap.to('.price-text', {
      opacity: 0.7,
      duration: 0.5,
      repeat: -1,
      yoyo: true,
      ease: "power1.inOut"
    })

    // 4. Line chart drawing
    gsap.fromTo('.main-trend-path', 
      { strokeDashoffset: 400 },
      { strokeDashoffset: 0, duration: 4, repeat: -1, ease: "none" }
    )

  }, containerRef.value)
})
</script>

<template>
  <div ref="containerRef" class="w-full h-full relative overflow-hidden flex items-center justify-center bg-[#050505]">
    
    <!-- Dashboard Main Frame -->
    <div class="relative w-[90%] h-[80%] bg-[#0A0A0A] border border-white/5 rounded-xl shadow-2xl overflow-hidden flex flex-col">
      
      <!-- Top Toolbar -->
      <div class="h-10 border-b border-white/5 flex items-center px-4 justify-between bg-zinc-900/50">
        <div class="flex items-center gap-3">
          <div class="flex gap-1.5">
            <div class="w-2.5 h-2.5 rounded-full bg-zinc-600"></div>
            <div class="w-2.5 h-2.5 rounded-full bg-zinc-700"></div>
            <div class="w-2.5 h-2.5 rounded-full bg-zinc-800"></div>
          </div>
          <div class="h-4 w-px bg-white/10 mx-1"></div>
          <span class="text-[10px] font-mono text-zinc-400 uppercase tracking-widest">BTC/USDT <span class="text-zinc-500 ml-1">Live</span></span>
        </div>
        <div class="flex gap-2">
          <div class="w-12 h-4 bg-white/5 rounded text-[8px] flex items-center justify-center text-zinc-500 font-mono">1M</div>
          <div class="w-12 h-4 bg-zinc-500/20 rounded text-[8px] flex items-center justify-center text-zinc-400 font-mono">5M</div>
          <div class="w-12 h-4 bg-white/5 rounded text-[8px] flex items-center justify-center text-zinc-500 font-mono">1H</div>
        </div>
      </div>

      <!-- Main Layout -->
      <div class="flex-1 flex overflow-hidden">
        
        <!-- Sidebar: Order Book -->
        <div class="w-32 border-r border-white/5 p-3 flex flex-col gap-2 bg-black/20">
          <div class="text-[8px] font-mono text-zinc-500 mb-1">Order Book</div>
          <div class="flex flex-col gap-1.5 overflow-hidden">
            <div v-for="i in 8" :key="'ask'+i" class="order-row flex justify-between">
              <span class="text-[9px] font-mono text-zinc-600/70">64.{{ 200 + i*12 }}</span>
              <span class="text-[9px] font-mono text-zinc-800">{{ (Math.random()*0.5).toFixed(3) }}</span>
            </div>
            <div class="h-px bg-white/5 my-1"></div>
            <div v-for="i in 8" :key="'bid'+i" class="order-row flex justify-between">
              <span class="text-[9px] font-mono text-zinc-500/70">64.{{ 100 - i*12 }}</span>
              <span class="text-[9px] font-mono text-zinc-800">{{ (Math.random()*0.5).toFixed(3) }}</span>
            </div>
          </div>
        </div>

        <!-- Center: Chart Area -->
        <div class="flex-1 relative flex flex-col p-4">
          <!-- Price Header -->
          <div class="mb-4 flex items-baseline gap-3">
             <h2 class="price-text text-2xl font-mono font-bold text-white tracking-tighter">64,241.80</h2>
             <span class="text-zinc-500 text-xs font-mono flex items-center gap-0.5">
               <ArrowUpRight class="w-3 h-3" /> +2.45%
             </span>
          </div>

          <!-- Candlestick Chart Area -->
          <div class="flex-1 flex items-end justify-between gap-1 pb-12 relative">
             <!-- Grid Lines -->
             <div class="absolute inset-0 flex flex-col justify-between opacity-10 pointer-events-none">
               <div v-for="i in 4" :key="i" class="w-full h-px bg-white"></div>
             </div>
             
             <!-- Candles -->
             <div v-for="i in 20" :key="i" class="flex-1 flex flex-col items-center">
                <div class="w-[1px] h-12 bg-zinc-700"></div>
                <div class="candle w-full max-w-[8px] rounded-sm" 
                     :class="i % 3 === 0 ? 'bg-zinc-600/40 border border-zinc-600/50' : 'bg-zinc-400/40 border border-zinc-400/50'"
                     :style="{ height: (20 + Math.random()*50) + 'px' }"></div>
             </div>

             <!-- Floating Trend Line -->
             <svg class="absolute inset-0 w-full h-full pointer-events-none" viewBox="0 0 400 200" preserveAspectRatio="none">
               <path class="main-trend-path" d="M0 150 Q 50 120, 100 140 T 200 100 T 300 130 T 400 80" 
                     fill="none" stroke="#71717a" stroke-width="2" stroke-dasharray="400" />
             </svg>
          </div>

          <!-- Volume Bars (Bottom) -->
          <div class="absolute bottom-4 left-4 right-4 h-8 flex items-end gap-0.5 opacity-30">
            <div v-for="i in 40" :key="i" class="flex-1 bg-white/20" :style="{ height: Math.random()*100 + '%' }"></div>
          </div>
        </div>

      </div>

    </div>

    <!-- HUD Background Decor -->
    <div class="absolute inset-0 pointer-events-none overflow-hidden opacity-5">
      <div class="absolute -top-1/4 -right-1/4 w-[600px] h-[600px] bg-zinc-800 rounded-full blur-[120px]"></div>
    </div>

    <!-- Bottom HUD -->
    <div class="absolute bottom-4 left-0 w-full text-center px-12 flex justify-between items-center opacity-30">
      <div class="flex items-center gap-2">
        <Activity class="w-3 h-3" />
        <span class="text-[8px] font-mono uppercase tracking-[0.3em]">Network Latency: 12ms</span>
      </div>
      <span class="text-[8px] font-mono uppercase tracking-[0.3em]">Gaptek Finance Engine v4.0</span>
    </div>
  </div>
</template>

<style scoped>
.main-trend-path {
  filter: drop-shadow(0 0 5px rgba(113, 113, 122, 0.5));
}
</style>

