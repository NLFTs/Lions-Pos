import { defineStore } from 'pinia'
import { ref } from 'vue'

let nextId = 1

export const useToastStore = defineStore('toast', () => {
  const toasts = ref([])

  function add(message, type = 'default', duration = 3000) {
    const id = nextId++
    toasts.value.push({ id, message, type })
    if (duration > 0) {
      setTimeout(() => remove(id), duration)
    }
    return id
  }

  function remove(id) {
    const idx = toasts.value.findIndex((t) => t.id === id)
    if (idx !== -1) toasts.value.splice(idx, 1)
  }

  function success(message, duration = 3000) {
    return add(message, 'success', duration)
  }

  function error(message, duration = 5000) {
    return add(message, 'error', duration)
  }

  function info(message, duration = 3000) {
    return add(message, 'info', duration)
  }

  return { toasts, add, remove, success, error, info }
})
