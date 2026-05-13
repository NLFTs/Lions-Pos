<script setup>
import { 
  PlayCircle, 
  ChevronRight
} from 'lucide-vue-next'
import DashboardReplica from './DashboardReplica.vue'
</script>

<template>
  <section class="py-24 lg:py-48 px-6 bg-[#020202] relative overflow-x-hidden z-10">
    <!-- Neutral Background Glow -->
    <div class="absolute top-1/2 right-0 -translate-y-1/2 w-[1200px] h-[800px] bg-white/[0.02] blur-[200px] rounded-full pointer-events-none"></div>

    <div class="max-w-[1600px] mx-auto relative z-10">
      <div class="grid grid-cols-1 lg:grid-cols-[0.7fr_1.3fr] gap-16 lg:gap-0 items-center">
        
        <!-- Left Side: Typography & CTAs -->
        <div class="space-y-10 lg:pr-12 z-20">
          <div class="space-y-6">
            <h2 class="text-5xl md:text-8xl font-medium tracking-tighter leading-[0.95] text-white">
              The <span class="text-emerald-500 underline decoration-emerald-500/30 decoration-4 underline-offset-8">All-in-One</span> <br />
              POS System.
            </h2>
            <p class="text-lg md:text-xl text-neutral-400 max-w-xl leading-relaxed">
              Membangun, mengelola, dan menskalakan bisnis ritel Anda dengan infrastruktur modern. Dari kasir hingga analitik cerdas — semua dalam satu dasbor.
            </p>
          </div>

          <div class="flex flex-col sm:flex-row items-center gap-4">
             <button class="h-12 px-8 rounded-xl bg-emerald-500 text-white font-bold hover:scale-105 transition-transform active:scale-95 shadow-lg shadow-emerald-500/20">
                Mulai Sekarang
             </button>
             <button class="h-12 px-8 rounded-xl bg-neutral-900 border border-white/10 text-white font-medium flex items-center gap-2 hover:bg-neutral-800 transition-colors">
                <PlayCircle class="w-5 h-5 text-neutral-500" />
                Gaptek dalam 100 detik
             </button>
          </div>
        </div>

        <!-- Right Side: Expansive Bleeding Window -->
        <div class="relative lg:h-[750px] w-full flex items-center mt-16 lg:mt-0">
          <!-- Window wrapper that allows bleeding -->
          <div class="relative w-full lg:w-[140%] min-w-0 lg:min-w-[1000px] h-[450px] sm:h-[600px] lg:h-full lg:translate-x-24 group">
            
            <!-- Glow behind window -->
            <div class="absolute inset-0 bg-white/5 rounded-[2rem] lg:rounded-[3rem] blur-3xl opacity-20 group-hover:opacity-40 transition-opacity"></div>
            
            <div class="relative h-full w-full bg-[#050505] border border-white/10 rounded-[2rem] lg:rounded-[3rem] shadow-[0_50px_100px_-20px_rgba(0,0,0,1)] overflow-hidden flex flex-col group-hover:border-white/20 transition-all duration-1000">
              
              <!-- Window Title Bar -->
              <div class="h-16 lg:h-20 border-b border-white/5 flex items-center px-6 lg:px-10 gap-6 lg:gap-12 bg-[#0A0A0A]/80 backdrop-blur-xl shrink-0">
                 <!-- Window Dots -->
                 <div class="flex gap-2 lg:gap-3 shrink-0">
                    <div class="w-3 h-3 lg:w-4 lg:h-4 rounded-full bg-neutral-800 border border-neutral-700"></div>
                    <div class="w-3 h-3 lg:w-4 lg:h-4 rounded-full bg-neutral-800 border border-neutral-700"></div>
                    <div class="w-3 h-3 lg:w-4 lg:h-4 rounded-full bg-neutral-800 border border-neutral-700"></div>
                 </div>

                 <!-- System Identifier -->
                 <div class="flex items-center gap-4 px-4 lg:px-6 py-2 rounded-2xl bg-white/5 border border-white/5">
                    <div class="w-2 h-2 rounded-full bg-white animate-pulse"></div>
                    <span class="text-[8px] lg:text-[10px] font-bold uppercase tracking-[0.3em] text-neutral-400">GAPTEK_CORE_v4.0</span>
                 </div>
              </div>

              <!-- Content Area (Dashboard Mock) -->
              <div class="flex-1 bg-white dark:bg-black relative overflow-hidden">
                  <DashboardReplica />
                  
                  <!-- Bleed Gradient for aesthetic -->
                  <div class="absolute inset-y-0 right-0 w-32 bg-gradient-to-l from-black/20 to-transparent pointer-events-none"></div>
              </div>

            </div>
          </div>
        </div>

      </div>
    </div>
  </section>
</template>


<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
.animate-float {
  animation: float 5s ease-in-out infinite;
}
</style>

