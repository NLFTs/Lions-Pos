<script setup>
import { useToastStore } from '@/stores/toast'
import { cn } from '@/lib/utils'
import { X, CheckCircle2, AlertCircle, Info } from 'lucide-vue-next'

const store = useToastStore()

const icons = {
  success: CheckCircle2,
  error: AlertCircle,
  info: Info,
}

const variants = {
  success: 'border-green-300 text-green-800 bg-green-50',
  error: 'border-destructive/50 text-destructive bg-destructive/10',
  info: 'border-blue-300 text-blue-800 bg-blue-50',
}
</script>

<template>
  <div class="fixed top-4 right-4 z-[100] flex flex-col gap-2 w-80 pointer-events-none">
    <TransitionGroup
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="translate-x-full opacity-0"
      enter-to-class="translate-x-0 opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="translate-x-0 opacity-100"
      leave-to-class="translate-x-full opacity-0"
    >
      <div
        v-for="t in store.toasts"
        :key="t.id"
        :class="cn('pointer-events-auto rounded-lg border p-3 text-sm shadow-lg flex items-start gap-2', variants[t.type] || variants.info)"
      >
        <component :is="icons[t.type] || icons.info" class="h-4 w-4 mt-0.5 shrink-0" />
        <span class="flex-1">{{ t.message }}</span>
        <button @click="store.remove(t.id)" class="shrink-0 opacity-60 hover:opacity-100 transition-opacity">
          <X class="h-3.5 w-3.5" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>
