import Lenis from 'lenis'
import { gsap, ScrollTrigger } from './gsap'

let lenisInstance = null

export const initLenis = () => {
  if (typeof window === 'undefined') return

  lenisInstance = new Lenis({
    duration: 1.2,
    easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
    orientation: 'vertical',
    gestureOrientation: 'vertical',
    smoothWheel: true,
    wheelMultiplier: 1,
    smoothTouch: false,
    touchMultiplier: 2,
    infinite: false,
  })

  // Synchronize Lenis with GSAP ScrollTrigger
  lenisInstance.on('scroll', ScrollTrigger.update)

  gsap.ticker.add((time) => {
    lenisInstance.raf(time * 1000)
  })

  gsap.ticker.lagSmoothing(0)

  return lenisInstance
}

export const getLenis = () => lenisInstance

export const destroyLenis = () => {
  if (lenisInstance) {
    lenisInstance.destroy()
    lenisInstance = null
  }
}
