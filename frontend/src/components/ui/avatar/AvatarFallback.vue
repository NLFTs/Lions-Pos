<script setup>
import { AvatarFallback as AvatarFallbackRoot } from 'radix-vue'
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
  <AvatarFallbackRoot
    v-bind="delegatedProps"
    :class="cn(
      'flex h-full w-full items-center justify-center rounded-full bg-muted',
      props.class,
    )"
  >
    <slot />
  </AvatarFallbackRoot>
</template>
