<script setup>
import { useNotification } from '@/composables/useNotification'
import { X, CheckCircle, AlertCircle, Info } from 'lucide-vue-next'

const { notifications, remove } = useNotification()

function getIcon(type) {
  if (type === 'success') return CheckCircle
  if (type === 'error') return AlertCircle
  return Info
}

function getIconClass(type) {
  if (type === 'success') return 'text-emerald-500'
  if (type === 'error') return 'text-red-500'
  return 'text-zinc-500'
}
</script>

<template>
  <Teleport to="body">
    <div class="fixed bottom-4 right-4 z-[100] flex flex-col gap-2 max-w-[380px] w-full px-4 sm:px-0 pointer-events-none">
      <TransitionGroup
        enter-active-class="transition-all duration-300 ease-out"
        enter-from-class="opacity-0 translate-y-4 sm:translate-y-0 sm:translate-x-4 scale-95"
        enter-to-class="opacity-100 translate-y-0 sm:translate-x-0 scale-100"
        leave-active-class="transition-all duration-200 ease-in"
        leave-from-class="opacity-100 scale-100"
        leave-to-class="opacity-0 scale-95"
      >
        <div
          v-for="item in notifications"
          :key="item.id"
          class="flex items-start gap-3 w-full bg-white dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 shadow-[0_8px_30px_rgba(0,0,0,0.12)] rounded-lg p-3.5 pointer-events-auto"
        >
          <component :is="getIcon(item.type)" class="w-4 h-4 shrink-0 mt-0.5" :class="getIconClass(item.type)" />
          <div class="flex-1 min-w-0">
            <h4 v-if="item.title" class="text-[13px] font-semibold text-zinc-900 dark:text-zinc-100">{{ item.title }}</h4>
            <p v-if="item.message" class="text-[12px] text-zinc-500 dark:text-zinc-400 mt-0.5 leading-snug">{{ item.message }}</p>
          </div>
          <button @click="remove(item.id)" class="text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100 transition-colors shrink-0">
            <X class="w-3.5 h-3.5" />
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>
