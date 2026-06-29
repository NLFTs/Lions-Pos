import { reactive, readonly } from 'vue'

const state = reactive({
  active: false,
  progress: 0,
  error: false
})

let timer = null

export const useLoadingBar = () => {
  const start = () => {
    if (timer) clearInterval(timer)
    state.active = true
    state.progress = 0
    state.error = false

    // Start at 20% immediately for quick feedback
    state.progress = 20

    timer = setInterval(() => {
      if (state.progress < 95) {
        // Accelerate loading progress perception
        const diff = Math.random() * (95 - state.progress) * 0.12
        state.progress += diff
      }
    }, 120) // More frequent updates
  }

  const finish = () => {
    if (timer) clearInterval(timer)
    state.progress = 100
    
    setTimeout(() => {
      state.active = false
      // Reset after transition finishes
      setTimeout(() => {
        state.progress = 0
      }, 350)
    }, 300) // Match with page transition timing
  }

  const fail = () => {
    state.error = true
    finish()
  }

  return {
    state: readonly(state),
    start,
    finish,
    fail
  }
}
