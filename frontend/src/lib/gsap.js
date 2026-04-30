import gsap from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'

// Register GSAP plugins
gsap.registerPlugin(ScrollTrigger)

// Configuration
gsap.config({
  nullTargetWarn: false,
})

export { gsap, ScrollTrigger }
