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
  <tbody v-bind="delegatedProps" :class="cn('[&_tr:last-child]:border-0', props.class)">
    <slot />
  </tbody>
</template>
