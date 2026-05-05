<script setup>
import { AvatarRoot } from 'radix-vue'
import { cn } from '@/lib/utils'
import { computed } from 'vue'
import { avatarVariant } from './index'

const props = defineProps({
  size: { type: String, default: 'default' },
  variant: { type: String, default: 'default' },
  class: { type: String, default: '' },
})

const delegatedProps = computed(() => {
  const { class: _, ...rest } = props
  return rest
})
</script>

<template>
  <AvatarRoot v-bind="delegatedProps" :class="cn(avatarVariant({ size, variant }), props.class)">
    <slot />
  </AvatarRoot>
</template>
