import { onMounted, onUnmounted } from 'vue'
import { gsap, ScrollTrigger } from '@/lib/gsap'

export function useGsap(callback) {
  let context = null

  onMounted(() => {
    // Create a GSAP Context for easy cleanup
    context = gsap.context(() => {
      if (callback && typeof callback === 'function') {
        callback(gsap, ScrollTrigger)
      }
    })

    // Refresh ScrollTrigger on load
    ScrollTrigger.refresh()
  })

  onUnmounted(() => {
    if (context) {
      context.revert() // This kills all animations and ScrollTriggers created within the context
    }
  })
}
