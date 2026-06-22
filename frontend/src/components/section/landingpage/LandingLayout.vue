<template>
  <div class="dark">
    <div class="min-h-screen bg-black flex justify-center text-zinc-100 selection:bg-primary/10 transition-colors duration-300">
      <div class="w-full max-w-[1728px] min-h-screen flex flex-col bg-black border-x transition-colors duration-300 relative side-border">
        <slot />
        <AppFooter />
      </div>
    </div>
  </div>
  </template>

<script setup>
import AppFooter from '@/components/ui/AppFooter.vue';
</script>

<style scoped>
/* Garis kiri & kanan — ungu gelap, terlihat tapi tidak mencolok */
.side-border {
  border-color: hsl(270, 55%, 22%);
  /* Tambahkan subtle glow agar semakin terasa */
  box-shadow:
    -1px 0 12px 0 hsla(270, 70%, 30%, 0.25),
     1px 0 12px 0 hsla(270, 70%, 30%, 0.25);
}
</style>
