<template>
  <div class="dark">
    <div class="min-h-screen bg-black flex justify-center text-zinc-100 selection:bg-primary/10 transition-colors duration-300">
      <div class="w-full max-w-[1728px] min-h-screen flex flex-col bg-black border-x border-white/20 transition-colors duration-300 relative">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
// Layout component for the landing page
</script>

<style scoped>
/* Zinc-200 provides a clean, visible but not overwhelming border color */
</style>
