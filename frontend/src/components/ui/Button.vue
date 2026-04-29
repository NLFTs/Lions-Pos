<script setup>
import { cn } from '@/lib/utils'

// Vue 3.5+: destructuring props dengan default values — tetap reaktif
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
  type: String,
})

const variants = {
  default: 'bg-primary text-primary-foreground hover:bg-primary/90',
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

// cn() dipanggil langsung di template — tidak perlu computed karena props
// string tidak berubah di tengah render cycle untuk kasus ini
function getClasses() {
  return cn(
    'inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50',
    variants[variant],
    sizes[size],
    className,
  )
}
</script>

<template>
  <button :class="getClasses()" :type="type" :disabled="disabled">
    <slot />
  </button>
</template>
