<template>
  <div class="dark">
    <div class="min-h-screen bg-zinc-950 flex justify-center text-zinc-100 selection:bg-primary/10 transition-colors duration-300">
      <div class="w-full min-h-screen flex flex-col bg-zinc-950 transition-colors duration-300">
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
