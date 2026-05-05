<script setup>
import { AvatarImage as AvatarImageRoot } from 'radix-vue'
import { cn } from '@/lib/utils'
import { computed } from 'vue'

const props = defineProps({
  src: { type: String, required: true },
  alt: { type: String, default: '' },
  class: { type: String, default: '' },
})

const delegatedProps = computed(() => {
  const { class: _, ...rest } = props
  return rest
})
</script>

<template>
  <AvatarImageRoot v-bind="delegatedProps" :class="cn('aspect-square h-full w-full', props.class)" />
</template>
