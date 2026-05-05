<script setup>
import { cn } from '@/lib/utils'
import { computed } from 'vue'

const props = defineProps({
  class: { type: String, default: '' },
})

const delegatedProps = computed(() => {
  const { class: _, ...rest } = props
  return rest
})
</script>

<template>
  <div v-bind="delegatedProps" :class="cn('rounded-md border border-border bg-card text-card-foreground shadow-sm', props.class)">
    <slot />
  </div>
</template>
