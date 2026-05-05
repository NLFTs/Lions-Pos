<template>
  <nav class="h-20 flex items-center justify-between px-6 md:px-12 sticky top-0 bg-white/50 dark:bg-zinc-950/50 backdrop-blur-xl z-50 transition-colors duration-300 w-full">
    <!-- Logo Section -->
    <div class="flex items-center gap-2 cursor-pointer group" @click="$emit('navigate', '/')">
      <div class="h-8 w-8 bg-white dark:bg-zinc-100 rounded-full flex items-center justify-center overflow-hidden border border-zinc-200 dark:border-zinc-800">
        <slot name="logo">
          <Zap class="h-4 w-4 text-zinc-950" />
        </slot>
      </div>
      <span class="text-xl font-medium font-serif tracking-tight text-zinc-900 dark:text-zinc-100">{{ brandName.toLowerCase() }}</span>
    </div>

    <!-- Navigation Links -->
    <div class="hidden md:flex items-center gap-8">
      <div 
        v-for="item in navItems" 
        :key="item.path" 
        class="flex items-center gap-1 cursor-pointer group"
        @click="$emit('navigate', item.path)"
      >
        <span class="text-sm font-medium text-zinc-600 dark:text-zinc-400 group-hover:text-zinc-900 dark:group-hover:text-zinc-100 transition-colors">
          {{ item.name }}
        </span>
        <ChevronDown v-if="item.hasDropdown" class="h-3.5 w-3.5 text-zinc-400 group-hover:text-zinc-600 dark:group-hover:text-zinc-200 transition-colors" />
      </div>
    </div>

    <!-- Actions -->
    <div class="flex items-center gap-4">
      <slot name="actions"></slot>
    </div>
  </nav>
</template>

<script setup>
import { Zap, ChevronDown } from 'lucide-vue-next'

defineProps({
  brandName: {
    type: String,
    default: 'obsidian'
  },
  navItems: {
    type: Array,
    required: true,
    default: () => [
      { name: 'What we offer', path: '#offer', hasDropdown: true },
      { name: "Who's it for", path: '#target', hasDropdown: true },
      { name: 'Pricing', path: '#pricing' },
      { name: 'About', path: '#about' }
    ]
  }
})

defineEmits(['navigate'])
</script>

<style scoped>
/* Scoped styles for Navbar if needed */
</style>
