<script setup>
import { computed } from 'vue'
import { cn } from '@/lib/utils'
const props = defineProps({ class: String })
const classes = computed(() => cn('text-2xl font-semibold leading-none tracking-tight', props.class))
</script>
<template><h3 :class="classes"><slot /></h3></template>
