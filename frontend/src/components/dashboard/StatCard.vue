<script setup>
import { computed } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps({
  label: { type: String, required: true },
  value: { type: [Number, String], required: true },
  icon: { type: [Object, Function], required: true },
  to: { type: String, default: null },
  color: { type: String, default: 'text-blue-600' },
  bg: { type: String, default: 'bg-blue-50 dark:bg-blue-950/30' },
  trend: { type: String, default: null },
  description: { type: String, default: null },
  horizontal: { type: Boolean, default: false }, // list view mode
  class: { type: String, default: '' },
})

const emit = defineEmits(['click'])

const wrapperClass = computed(() => {
  const base = 'group relative overflow-hidden rounded-xl border border-border/60 bg-card shadow-sm hover:shadow-md hover:border-primary/30 transition-all duration-200 cursor-pointer'
  const padding = props.horizontal ? 'px-4 py-3' : 'p-5'
  return cn(base, padding, props.class)
})
</script>

<template>
  <component
    :is="to ? 'RouterLink' : 'div'"
    :to="to"
    :class="wrapperClass"
    @click="emit('click')"
  >
    <!-- Decorative gradient blob -->
    <div class="absolute -top-8 -right-8 w-24 h-24 rounded-full opacity-10" :class="bg" />

    <!-- ── HORIZONTAL layout (list view) ── -->
    <div v-if="horizontal" class="relative flex items-center gap-4">
      <!-- Icon -->
      <div :class="['w-10 h-10 rounded-lg flex items-center justify-center shrink-0', bg, color]">
        <component :is="icon" class="w-5 h-5" />
      </div>

      <!-- Label + Value side by side -->
      <div class="flex-1 min-w-0">
        <p class="text-xs font-medium text-muted-foreground uppercase tracking-wide">{{ label }}</p>
        <span class="text-xl font-bold tracking-tight">{{ value }}</span>
        <p v-if="description" class="text-[11px] text-muted-foreground leading-relaxed mt-0.5">{{ description }}</p>
      </div>

      <!-- Trend badge — right side -->
      <div v-if="trend" class="shrink-0">
        <span
          class="inline-flex items-center gap-0.5 rounded-full px-2 py-0.5 text-[11px] font-semibold"
          :class="trend.startsWith('+')
            ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400'
            : 'bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-400'"
        >
          {{ trend }}
        </span>
      </div>
    </div>

    <!-- ── VERTICAL layout (default / grid view) ── -->
    <template v-else>
      <div class="relative flex items-start justify-between gap-3">
        <!-- Icon -->
        <div :class="['w-11 h-11 rounded-lg flex items-center justify-center shrink-0', bg, color]">
          <component :is="icon" class="w-5 h-5" />
        </div>

        <!-- Trend badge -->
        <div v-if="trend" class="shrink-0">
          <span
            class="inline-flex items-center gap-0.5 rounded-full px-2 py-0.5 text-[11px] font-semibold"
            :class="trend.startsWith('+')
              ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400'
              : 'bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-400'"
          >
            {{ trend }}
          </span>
        </div>
      </div>

      <!-- Value & Label -->
      <div class="mt-3">
        <span class="text-2xl font-bold tracking-tight">{{ value }}</span>
        <p class="text-xs font-medium text-muted-foreground mt-0.5 uppercase tracking-wide">{{ label }}</p>
      </div>

      <!-- Description -->
      <p v-if="description" class="text-[11px] text-muted-foreground mt-2 leading-relaxed">
        {{ description }}
      </p>
    </template>
  </component>
</template>
