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

    // Initial jump
    state.progress = 10

    timer = setInterval(() => {
      if (state.progress < 90) {
        // Slow down as it gets closer to 90
        const diff = Math.random() * (90 - state.progress) * 0.1
        state.progress += diff
      }
    }, 200)
  }

  const finish = () => {
    if (timer) clearInterval(timer)
    state.progress = 100
    
    setTimeout(() => {
      state.active = false
      // Reset after transition finishes
      setTimeout(() => {
        state.progress = 0
      }, 400)
    }, 400)
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
