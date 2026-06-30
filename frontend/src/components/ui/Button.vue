<script setup>
import { cn } from '@/lib/utils'

const {
  variant = 'default',
  size = 'default',
  class: className,
  disabled = false,
  type = 'button',
} = defineProps({
  variant: String,
  size: String,
  class: String,
  disabled: Boolean,
  type: Boolean,
})

const variants = {
  // Pastikan background default adalah zinc-100 agar transisi terlihat jelas
  default: 'bg-zinc-100 text-zinc-900', 
  destructive: 'bg-destructive text-destructive-foreground hover:bg-destructive/90',
  outline: 'border border-input bg-background hover:bg-accent hover:text-accent-foreground',
  secondary: 'bg-secondary text-secondary-foreground hover:bg-secondary/80',
  ghost: 'hover:bg-accent hover:text-accent-foreground',
  link: 'text-primary underline-offset-4 hover:underline',
}

const sizes = {
  default: 'h-9 px-4 py-2',
  sm: 'h-8 px-3 text-xs',
  lg: 'h-11 rounded-md px-8',
  icon: 'h-9 w-9',
}

function getClasses() {
  return cn(
    // Base classes: group, relative, overflow-hidden, transisi warna teks default, dan scale-down saat klik
    'group relative overflow-hidden inline-flex items-center justify-center gap-2 whitespace-nowrap text-sm font-bold transition-colors duration-300 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 active:scale-95',
    variants[variant],
    sizes[size],
    className,
  )
}
</script>

<template>
  <button :class="getClasses()" :type="type" :disabled="disabled">
    <!-- Layer Background Animasi -->
    <span 
      class="absolute inset-0 z-0 bg-indigo-500 scale-x-0 transition-transform duration-500 ease-in-out origin-right group-hover:scale-x-100 group-hover:origin-left"
    ></span>

    <!-- Slot Teks dengan transisi warna agar tetap kontras -->
    <span class="relative z-10 inline-flex items-center gap-2 transition-colors duration-300 group-hover:text-white">
      <slot />
    </span>
  </button>
</template>