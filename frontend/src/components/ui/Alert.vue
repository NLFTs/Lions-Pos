<script setup>
import { cn } from '@/lib/utils'

const { variant = 'default', class: className } = defineProps({
  variant: String,
  class: String,
})

const variants = {
  default: 'bg-background text-foreground border',
  destructive: 'border-destructive/50 text-destructive dark:border-destructive',
}
</script>

<template>
  <div role="alert" :class="cn('relative w-full rounded-lg border p-4 [&>svg~*]:pl-7 [&>svg]:absolute [&>svg]:left-4 [&>svg]:top-4', variants[variant], className)">
    <slot />
  </div>
</template>
