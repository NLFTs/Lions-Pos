<script setup>
import { cn } from '@/lib/utils'
import { computed } from 'vue'
import { badgeVariants } from './index'

const props = defineProps({
  variant: { type: String, default: 'default' },
  class: { type: String, default: '' },
})

const delegatedProps = computed(() => {
  const { class: _, ...rest } = props
  return rest
})
</script>

<template>
  <div v-bind="delegatedProps" :class="cn(badgeVariants({ variant }), props.class)">
    <slot />
  </div>
</template>
