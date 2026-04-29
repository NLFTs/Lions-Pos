import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useConfirmStore = defineStore('confirm', () => {
  const isOpen = ref(false)
  const config = ref({
    title: '',
    description: '',
    confirmLabel: 'Confirm',
    cancelLabel: 'Cancel',
    variant: 'destructive',
  })
  const loading = ref(false)
  let resolver = null

  function confirm(opts = {}) {
    return new Promise((resolve) => {
      resolver = resolve
      config.value = {
        title: opts.title || 'Are you sure?',
        description: opts.description || 'This action cannot be undone.',
        confirmLabel: opts.confirmLabel || 'Confirm',
        cancelLabel: opts.cancelLabel || 'Cancel',
        variant: opts.variant || 'destructive',
      }
      loading.value = false
      isOpen.value = true
    })
  }

  function onConfirm() {
    if (resolver) resolver(true)
    resolver = null
    isOpen.value = false
    loading.value = false
  }

  function onCancel() {
    if (resolver) resolver(false)
    resolver = null
    isOpen.value = false
    loading.value = false
  }

  return { isOpen, config, loading, confirm, onConfirm, onCancel }
})
