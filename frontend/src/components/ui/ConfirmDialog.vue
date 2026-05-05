<script setup>
import Button from '@/components/ui/Button.vue'
import { Loader2 } from 'lucide-vue-next'

// Vue 3.5+: destructuring props dengan default values
const {
  title = 'Are you sure?',
  description = 'This action cannot be undone.',
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  variant = 'destructive',
  loading = false,
} = defineProps({
  title: String,
  description: String,
  confirmLabel: String,
  cancelLabel: String,
  variant: String,
  loading: Boolean,
})

const emit = defineEmits(['confirm', 'cancel'])
</script>

<template>
  <Teleport to="body">
    <div class="fixed inset-0 z-[90] flex items-center justify-center">
      <div class="absolute inset-0 bg-black/50" @click="!loading && emit('cancel')" />
      <div class="relative z-10 w-full max-w-sm mx-4 bg-card rounded-lg shadow-xl border p-6 space-y-4">
        <h3 class="font-semibold text-lg">{{ title }}</h3>
        <p class="text-sm text-muted-foreground">{{ description }}</p>
        <div class="flex justify-end gap-3">
          <Button variant="outline" @click="emit('cancel')" :disabled="loading">{{ cancelLabel }}</Button>
          <Button :variant="variant" @click="emit('confirm')" :disabled="loading">
            <Loader2 v-if="loading" class="h-4 w-4 mr-2 animate-spin" />
            {{ confirmLabel }}
          </Button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
