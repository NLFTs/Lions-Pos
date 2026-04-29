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
  <td v-bind="delegatedProps" :class="cn('px-4 py-2.5 align-middle text-xs [&:has([role=checkbox])]:pr-0', props.class)">
    <slot />
  </td>
</template>
