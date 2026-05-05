<script setup>
import { cn } from '@/lib/utils'

// Vue 3.5+: destructuring props dengan default values
const {
  class: className,
  type = 'text',
  placeholder,
  disabled = false,
  modelValue,
} = defineProps({
  class: String,
  type: String,
  placeholder: String,
  disabled: Boolean,
  modelValue: [String, Number],
})

const emit = defineEmits(['update:modelValue'])
</script>

<template>
  <input
    :class="cn(
      'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
      className
    )"
    :type="type"
    :placeholder="placeholder"
    :disabled="disabled"
    :value="modelValue"
    @input="emit('update:modelValue', $event.target.value)"
  />
</template>
