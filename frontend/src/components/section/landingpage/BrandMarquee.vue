<script setup>
const brands = [
  'RetailPro', 'FoodieHub', 'MarketX', 'ShopWave', 'ZestStore', 'CloudDine', 'SmartCart'
]
</script>

<template>
  <div class="py-12 border-y border-white/5 bg-zinc-950 overflow-hidden">
    <div class="flex items-center gap-16 animate-marquee whitespace-nowrap">
      <div v-for="i in 4" :key="i" class="flex items-center gap-16">
        <span 
          v-for="brand in brands" 
          :key="brand" 
          class="text-2xl font-bold text-zinc-700 hover:text-zinc-500 transition-colors cursor-default"
        >
          {{ brand }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-marquee {
  display: flex;
  width: max-content;
  animation: marquee 30s linear infinite;
}

@keyframes marquee {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}
</style>
