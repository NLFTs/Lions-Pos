import { ref } from 'vue'

const notifications = ref([])

export function useNotification() {
  function notify({ title, message, type = 'default', duration = 3000 }) {
    const id = Date.now().toString() + Math.random().toString(36).substr(2, 5)
    notifications.value.push({ id, title, message, type })

    if (duration > 0) {
      setTimeout(() => {
        remove(id)
      }, duration)
    }
  }

  function remove(id) {
    const index = notifications.value.findIndex((n) => n.id === id)
    if (index !== -1) {
      notifications.value.splice(index, 1)
    }
  }

  return {
    notifications,
    notify,
    remove,
  }
}
