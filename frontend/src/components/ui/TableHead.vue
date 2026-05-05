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
  <th v-bind="delegatedProps" :class="cn('h-8 px-4 text-left align-middle font-semibold text-zinc-500 text-[11px] uppercase tracking-wider [&:has([role=checkbox])]:pr-0', props.class)">
    <slot />
  </th>
</template>
