<template>
  <nav class="h-16 sm:h-20 border-b-[3px] border-zinc-200 dark:border-zinc-800 flex items-center justify-between px-4 sm:px-8 sticky top-0 bg-white/80 dark:bg-zinc-900/80 backdrop-blur-md z-50 transition-colors duration-300 w-full overflow-hidden">
    <!-- Logo Section -->
    <div class="flex items-center gap-2 cursor-pointer shrink-0" @click="$emit('navigate', '/')">
      <div class="h-8 w-8 sm:h-10 sm:w-10 bg-primary rounded-xl flex items-center justify-center shadow-lg shadow-primary/20">
        <slot name="logo">
          <span class="text-primary-foreground font-bold text-lg sm:text-xl transition-colors duration-300">S</span>
        </slot>
      </div>
      <span class="text-lg sm:text-xl font-bold font-lexend tracking-tight text-zinc-900 dark:text-zinc-100 transition-colors duration-300">{{ brandName }}</span>
    </div>

    <!-- Navigation Links -->
    <div class="hidden md:flex items-center gap-8">
      <a 
        v-for="item in navItems" 
        :key="item.path" 
        href="javascript:void(0)"
        @click="$emit('navigate', item.path)"
        class="text-sm font-bold text-zinc-500 dark:text-zinc-400 hover:text-primary transition-all duration-300 relative group"
      >
        {{ item.name }}
        <span class="absolute -bottom-1 left-0 w-0 h-0.5 bg-primary transition-all duration-300 group-hover:w-full"></span>
      </a>
    </div>

    <!-- Actions -->
    <div class="flex items-center gap-2 sm:gap-4 shrink-0">
      <slot name="actions"></slot>
    </div>
  </nav>
</template>

<script setup>
defineProps({
  brandName: {
    type: String,
    default: 'GAPETK'
  },
  navItems: {
    type: Array,
    required: true,
    default: () => []
  }
})

defineEmits(['navigate'])
</script>

<style scoped>
/* Scoped styles for Navbar if needed */
</style>
