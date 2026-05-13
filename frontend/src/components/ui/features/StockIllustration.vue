<script setup>
import { onMounted, ref } from 'vue'
import { gsap } from '@/lib/gsap'

const svgRef = ref(null)

onMounted(() => {
  const ctx = gsap.context(() => {
    // Animate robotic arms
    gsap.to('.robot-arm', {
      rotation: 15,
      duration: 3,
      repeat: -1,
      yoyo: true,
      ease: 'sine.inOut',
      transformOrigin: 'bottom center'
    })

    // Animate boxes on conveyor
    const conveyorItems = ['.conveyor-1', '.conveyor-2', '.conveyor-3']
    conveyorItems.forEach((item, i) => {
      gsap.to(item, {
        x: 400,
        y: 200,
        duration: 8,
        repeat: -1,
        delay: i * 2.5,
        ease: 'none',
        onRepeat: () => gsap.set(item, { x: 0, y: 0 })
      })
    })

    // Floating UI labels
    gsap.to('.stock-label', {
      y: -5,
      opacity: 1,
      duration: 2,
      repeat: -1,
      yoyo: true,
      stagger: 0.5
    })
  }, svgRef.value)
})
</script>

<template>
  <div class="w-full h-full relative overflow-hidden flex items-center justify-center">
    <svg ref="svgRef" viewBox="0 0 800 500" class="w-[120%] h-[120%] opacity-40 group-hover:opacity-80 transition-opacity duration-1000">
      <defs>
        <linearGradient id="boxGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#ffffff" stop-opacity="0.2" />
          <stop offset="100%" stop-color="#ffffff" stop-opacity="0.05" />
        </linearGradient>
        
        <g id="detailed-box">
          <path d="M0 0 L30 -15 L60 0 L30 15 Z" fill="url(#boxGrad)" stroke="white" stroke-width="0.3" />
          <path d="M0 0 L0 30 L30 45 L30 15 Z" fill="white" fill-opacity="0.1" stroke="white" stroke-width="0.3" />
          <path d="M30 15 L30 45 L60 30 L60 0 Z" fill="white" fill-opacity="0.05" stroke="white" stroke-width="0.3" />
          <rect x="25" y="0" width="10" height="2" fill="white" opacity="0.2" transform="rotate(-26.5)" />
        </g>

        <g id="rack-section">
          <use xlink:href="#detailed-box" x="0" y="0" />
          <use xlink:href="#detailed-box" x="0" y="-45" />
          <use xlink:href="#detailed-box" x="60" y="30" />
          <use xlink:href="#detailed-box" x="60" y="-15" />
        </g>
      </defs>

      <!-- Isometric Floor Grid -->
      <g stroke="white" stroke-width="0.2" opacity="0.1">
        <path v-for="i in 20" :key="'h'+i" :d="`M ${-200 + i*100} 500 L ${800 + i*100} 0`" />
        <path v-for="i in 20" :key="'v'+i" :d="`M ${1000 - i*100} 500 L ${0 - i*100} 0`" />
      </g>

      <!-- Racks -->
      <g transform="translate(450, 150)">
        <use xlink:href="#rack-section" x="0" y="0" />
        <use xlink:href="#rack-section" x="120" y="60" />
        <use xlink:href="#rack-section" x="-120" y="-60" />
      </g>

      <!-- Conveyor Belt System -->
      <path d="M 0 350 L 400 150 L 800 350" stroke="white" stroke-width="2" fill="none" opacity="0.1" />
      <g class="conveyor-1"><use xlink:href="#detailed-box" x="0" y="320" /></g>
      <g class="conveyor-2"><use xlink:href="#detailed-box" x="-100" y="370" /></g>
      <g class="conveyor-3"><use xlink:href="#detailed-box" x="-200" y="420" /></g>

      <!-- Robotic Arms -->
      <g transform="translate(300, 200)" class="robot-arm">
         <rect x="-2" y="0" width="4" height="60" fill="white" opacity="0.4" />
         <circle r="5" fill="white" opacity="0.6" />
         <g transform="translate(0, 60)">
            <rect x="-10" y="0" width="20" height="2" fill="white" />
         </g>
      </g>

      <!-- HUD Labels -->
      <g font-family="monospace" font-size="10" fill="white" opacity="0">
        <g class="stock-label" transform="translate(500, 100)">
          <text>SKU_ID: 9402-AX</text>
          <rect x="-5" y="-12" width="100" height="18" stroke="white" stroke-width="0.5" fill="none" />
        </g>
        <g class="stock-label" transform="translate(100, 300)">
          <text>STATUS: PROCESSING</text>
          <rect x="-5" y="-12" width="120" height="18" stroke="white" stroke-width="0.5" fill="none" />
        </g>
      </g>
    </svg>
  </div>
</template>
