<script setup>
import { computed } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps({
  actions: { type: Array, required: true },
  // Each action: { label, icon, to?, variant?, onClick? }
  class: { type: String, default: '' },
})

const variants = {
  default: 'bg-primary text-primary-foreground hover:bg-primary/90',
  secondary: 'bg-secondary text-secondary-foreground hover:bg-secondary/80',
  outline: 'border border-border bg-card hover:bg-accent hover:text-accent-foreground',
  ghost: 'hover:bg-accent hover:text-accent-foreground',
  destructive: 'bg-destructive text-destructive-foreground hover:bg-destructive/90',
}
</script>

<template>
  <div :class="cn('flex flex-wrap gap-2', props.class)">
    <component
      v-for="(action, index) in actions"
      :key="index"
      :is="action.to ? 'RouterLink' : 'button'"
      :to="action.to"
      @click="action.onClick?.()"
      :class="cn(
        'inline-flex items-center gap-2 rounded-lg px-3.5 py-2 text-sm font-medium transition-all duration-150 shadow-sm hover:shadow',
        variants[action.variant || 'default']
      )"
    >
      <component v-if="action.icon" :is="action.icon" class="w-4 h-4" />
      {{ action.label }}
    </component>
  </div>
</template>
