<script setup>
import { cn } from '@/lib/utils'
const { class: className, for: htmlFor } = defineProps({ class: String, for: String })
</script>
<template>
  <label :class="cn('text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70', className)" :for="htmlFor"><slot /></label>
</template>
