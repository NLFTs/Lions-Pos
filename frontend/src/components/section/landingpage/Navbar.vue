<template>
  <nav class="h-20 border-b-[3px] border-border flex items-center justify-between px-8 sticky top-0 bg-background/80 backdrop-blur-md z-50 transition-colors duration-300">
    <!-- Logo Section -->
    <div class="flex items-center gap-2 cursor-pointer" @click="$emit('navigate', '/')">
      <div class="h-10 w-10 bg-primary rounded-xl flex items-center justify-center shadow-lg shadow-primary/20">
        <slot name="logo">
          <span class="text-white font-bold text-xl">S</span>
        </slot>
      </div>
      <span class="text-xl font-bold font-lexend tracking-tight text-foreground">{{ brandName }}</span>
    </div>

    <!-- Navigation Links -->
    <div class="hidden md:flex items-center gap-8">
      <a 
        v-for="item in navItems" 
        :key="item.path" 
        href="javascript:void(0)"
        @click="$emit('navigate', item.path)"
        class="text-sm font-bold text-muted-foreground hover:text-primary transition-all duration-300 relative group"
      >
        {{ item.name }}
        <span class="absolute -bottom-1 left-0 w-0 h-0.5 bg-primary transition-all duration-300 group-hover:w-full"></span>
      </a>
    </div>

    <!-- Actions -->
    <div class="flex items-center gap-4">
      <slot name="actions"></slot>
    </div>
  </nav>
</template>

<script setup>
defineProps({
  brandName: {
    type: String,
    default: 'Spravel'
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
