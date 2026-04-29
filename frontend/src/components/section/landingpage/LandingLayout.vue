<template>
  <div class="min-h-screen bg-zinc-50 dark:bg-zinc-950 flex justify-center text-foreground selection:bg-primary/10 transition-colors duration-300">
    <!-- Main Container with Side Borders -->
    <div class="w-full max-w-7xl border-x-[3px] border-border min-h-screen flex flex-col bg-background transition-colors duration-300">
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
