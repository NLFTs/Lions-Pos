<script setup>
import { computed } from 'vue'
import { cn } from '@/lib/utils'
import { ChevronRight } from 'lucide-vue-next'

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  icon: { type: [Object, Function], default: null },
  actionLabel: { type: String, default: '' },
  actionTo: { type: String, default: '' },
  class: { type: String, default: '' },
})

const emit = defineEmits(['action-click'])

const hasHeader = computed(() => props.title || props.actionLabel)
</script>

<template>
  <div :class="cn('rounded-xl border border-border/60 bg-card shadow-sm overflow-hidden', props.class)">
    <!-- Header -->
    <div v-if="hasHeader" class="flex items-center justify-between px-5 py-3.5 border-b border-border">
      <div class="flex items-center gap-2">
        <component v-if="icon" :is="icon" class="w-4 h-4 text-muted-foreground" />
        <h3 class="text-sm font-semibold">{{ title }}</h3>
      </div>
      <div class="flex items-center gap-2">
        <p v-if="subtitle" class="text-xs text-muted-foreground">{{ subtitle }}</p>
        <RouterLink
          v-if="actionLabel && actionTo"
          :to="actionTo"
          class="inline-flex items-center gap-1 text-xs font-medium text-primary hover:text-primary/80 transition-colors"
        >
          {{ actionLabel }}
          <ChevronRight class="w-3.5 h-3.5" />
        </RouterLink>
        <button
          v-else-if="actionLabel"
          @click="emit('action-click')"
          class="inline-flex items-center gap-1 text-xs font-medium text-primary hover:text-primary/80 transition-colors"
        >
          {{ actionLabel }}
          <ChevronRight class="w-3.5 h-3.5" />
        </button>
      </div>
    </div>

    <!-- Content -->
    <div class="p-5">
      <slot />
    </div>
  </div>
</template>
