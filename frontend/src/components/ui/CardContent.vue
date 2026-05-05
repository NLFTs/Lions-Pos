<script setup>
import { computed } from 'vue'
import { cn } from '@/lib/utils'
const props = defineProps({ class: String })
const classes = computed(() => cn('p-6 pt-0', props.class))
</script>
<template><div :class="classes"><slot /></div></template>
