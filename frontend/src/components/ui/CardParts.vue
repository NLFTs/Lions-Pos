<script setup>
import { computed } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps({ class: String })
const hClasses = computed(() => cn('flex flex-col space-y-1.5 p-6', props.class))
const tClasses = computed(() => cn('text-2xl font-semibold leading-none tracking-tight'))
const dClasses = computed(() => cn('text-sm text-muted-foreground'))
const cClasses = computed(() => cn('p-6 pt-0'))
const fClasses = computed(() => cn('flex items-center p-6 pt-0'))
</script>

<template>
  <slot />
</template>

<!-- CardHeader -->
<script>
export const CardHeader = { template: `<div :class="cn('flex flex-col space-y-1.5 p-6', $props.class)"><slot/></div>`, props: ['class'] }
export const CardTitle = { template: `<h3 :class="cn('text-2xl font-semibold leading-none tracking-tight', $props.class)"><slot/></h3>`, props: ['class'] }
export const CardDescription = { template: `<p :class="cn('text-sm text-muted-foreground', $props.class)"><slot/></p>`, props: ['class'] }
export const CardContent = { template: `<div :class="cn('p-6 pt-0', $props.class)"><slot/></div>`, props: ['class'] }
export const CardFooter = { template: `<div :class="cn('flex items-center p-6 pt-0', $props.class)"><slot/></div>`, props: ['class'] }
</script>
