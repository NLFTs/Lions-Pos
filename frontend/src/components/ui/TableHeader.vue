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
  <thead v-bind="delegatedProps" :class="cn('[&_tr]:border-b', props.class)">
    <slot />
  </thead>
</template>
