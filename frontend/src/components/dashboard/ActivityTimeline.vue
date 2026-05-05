<script setup>
import { computed } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps({
  items: { type: Array, required: true },
  // Each item: { title, description, icon, color?, time?, avatar?, initials? }
  class: { type: String, default: '' },
})

const defaultColor = 'text-zinc-500 dark:text-zinc-400'
</script>

<template>
  <div :class="cn('space-y-0', props.class)">
    <div
      v-for="(item, index) in items"
      :key="index"
      class="flex gap-3 group"
    >
      <!-- Timeline line -->
      <div class="flex flex-col items-center shrink-0">
        <div
          class="w-8 h-8 rounded-full flex items-center justify-center shrink-0 transition-colors"
          :class="item.color || defaultColor"
        >
          <component v-if="item.icon" :is="item.icon" class="w-4 h-4" />
          <span v-else-if="item.initials" class="text-xs font-bold">{{ item.initials }}</span>
        </div>
        <div
          v-if="index < items.length - 1"
          class="w-px flex-1 bg-border group-last:hidden"
        />
      </div>

      <!-- Content -->
      <div class="flex-1 min-w-0 py-0.5">
        <div class="flex items-start justify-between gap-2">
          <p class="text-sm font-medium leading-snug">{{ item.title }}</p>
          <span v-if="item.time" class="text-[11px] text-muted-foreground shrink-0 mt-0.5">
            {{ item.time }}
          </span>
        </div>
        <p v-if="item.description" class="text-xs text-muted-foreground mt-0.5 leading-relaxed">
          {{ item.description }}
        </p>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="items.length === 0" class="py-8 text-center">
      <p class="text-sm text-muted-foreground">Belum ada aktivitas.</p>
    </div>
  </div>
</template>
