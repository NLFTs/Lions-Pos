<template>
  <div class="min-h-screen bg-zinc-50 dark:bg-zinc-950 flex justify-center text-zinc-900 dark:text-zinc-100 selection:bg-primary/10 transition-colors duration-300">
    <!-- Main Container with Side Borders -->
    <div class="w-full md:w-[calc(100%-2rem)] max-w-[2560px] border-x-0 md:border-x-[3px] border-zinc-200 dark:border-zinc-800 min-h-screen flex flex-col bg-white dark:bg-zinc-900 transition-colors duration-300">
      <slot />
    </div>
  </div>
</template>

<script setup>
// Layout component for the landing page
</script>

<style scoped>
/* Zinc-200 provides a clean, visible but not overwhelming border color */
</style>
