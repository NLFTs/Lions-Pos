import { onMounted, onUnmounted } from 'vue'
import { gsap, ScrollTrigger } from '@/lib/gsap'
import { initLenis, destroyLenis } from '@/lib/lenis'

export function useGsap(callback) {
  onMounted(() => {
    // Initialize Smooth Scroll
    const lenis = initLenis()

    // Execute GSAP Animations
    if (callback && typeof callback === 'function') {
      callback(gsap, ScrollTrigger)
    }

    // Refresh ScrollTrigger on load
    ScrollTrigger.refresh()
  })

  onUnmounted(() => {
    // Kill all ScrollTriggers to prevent memory leaks and ghost animations
    const allTriggers = ScrollTrigger.getAll()
    allTriggers.forEach(trigger => trigger.kill())
    
    // Kill all active GSAP animations
    gsap.killTweensOf('*')

    // Destroy Lenis
    destroyLenis()
  })
}
