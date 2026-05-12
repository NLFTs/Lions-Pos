<script setup>
import { onMounted, ref, onUnmounted } from 'vue'
import { gsap } from '@/lib/gsap'

const containerRef = ref(null)
const canvasRef = ref(null)

onMounted(() => {
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  let width = canvas.offsetWidth
  let height = canvas.offsetHeight
  
  const dpr = window.devicePixelRatio || 1
  canvas.width = width * dpr
  canvas.height = height * dpr
  ctx.scale(dpr, dpr)

  const radius = 130
  const points = []
  const pointCount = 3000

  const getIslandType = (lon, lat) => {
    if (lon > 95 && lon < 106 && lat > -6 && lat < 6) return 'ID'
    if (lon > 105 && lon < 115 && lat > -9 && lat < -6) return 'ID'
    if (lon > 108 && lon < 119 && lat > -4 && lat < 7) return 'ID'
    if (lon > 118 && lon < 128 && lat > -7 && lat < 3) return 'ID'
    if (lon > 130 && lon < 141 && lat > -9 && lat < 1) return 'ID'
    if (Math.random() > 0.85) return 'WORLD'
    return 'SEA'
  }

  for (let i = 0; i < pointCount; i++) {
    const phi = Math.acos(-1 + (2 * i) / pointCount)
    const theta = Math.sqrt(pointCount * Math.PI) * phi
    const x = Math.cos(theta) * Math.sin(phi)
    const y = Math.sin(theta) * Math.sin(phi)
    const z = Math.cos(phi)
    const lon = (theta % (2 * Math.PI)) * (180 / Math.PI)
    const lat = (phi - Math.PI / 2) * (180 / Math.PI)
    const type = getIslandType(lon, lat)
    if (type !== 'SEA') {
      points.push({ x, y, z, type })
    }
  }

  const branchNodes = []
  for (let i = 0; i < 25; i++) {
    const phi = Math.random() * Math.PI
    const theta = Math.random() * 2 * Math.PI
    branchNodes.push({
      x: Math.cos(theta) * Math.sin(phi),
      y: Math.sin(theta) * Math.sin(phi),
      z: Math.cos(phi)
    })
  }

  let rotation = 0
  let animationFrame;

  const render = () => {
    ctx.clearRect(0, 0, width, height)
    rotation += 0.004
    const cosR = Math.cos(rotation)
    const sinR = Math.sin(rotation)

    // --- Subtle Sphere Definition (To make the ball visible) ---
    // Edge Glow (Rim light)
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.08)'
    ctx.lineWidth = 0.5
    ctx.beginPath()
    ctx.arc(width/2, height/2, radius, 0, Math.PI * 2)
    ctx.stroke()
    
    // Very faint surface depth
    const surface = ctx.createRadialGradient(width/2, height/2, radius * 0.8, width/2, height/2, radius)
    surface.addColorStop(0, 'transparent')
    surface.addColorStop(1, 'rgba(255, 255, 255, 0.03)')
    ctx.fillStyle = surface
    ctx.beginPath()
    ctx.arc(width/2, height/2, radius, 0, Math.PI * 2)
    ctx.fill()

    // Render Base Points
    points.forEach(p => {
      const rx = p.x * cosR - p.z * sinR
      const rz = p.x * sinR + p.z * cosR
      const ry = p.y
      if (rz < 0) {
        const perspective = 500 / (500 + rz)
        const px = rx * radius * perspective + width / 2
        const py = ry * radius * perspective + height / 2
        const opacity = Math.max(0.1, (rz + 1) / 2)
        if (p.type === 'ID') {
           ctx.fillStyle = `rgba(255, 255, 255, ${opacity * 0.4})`
           ctx.beginPath(); ctx.arc(px, py, 1.5, 0, Math.PI * 2); ctx.fill()
        } else {
           ctx.fillStyle = `rgba(255, 255, 255, ${opacity * 0.1})`
           ctx.beginPath(); ctx.arc(px, py, 0.8, 0, Math.PI * 2); ctx.fill()
        }
      }
    })

    // Render "Super Bright" Random Nodes
    branchNodes.forEach(node => {
      const rx = node.x * cosR - node.z * sinR
      const rz = node.x * sinR + node.z * cosR
      const ry = node.y
      if (rz < 0) {
        const perspective = 500 / (500 + rz)
        const px = rx * radius * perspective + width / 2
        const py = ry * radius * perspective + height / 2
        const opacity = Math.max(0.1, (rz + 1) / 2)
        const pulse = Math.sin(Date.now() * 0.003 + node.x * 100) * 0.5 + 0.5
        
        ctx.shadowBlur = 10 * pulse + 5
        ctx.shadowColor = '#ffffff'
        ctx.fillStyle = `rgba(255, 255, 255, ${opacity * (0.3 + 0.3 * pulse)})`
        ctx.beginPath(); ctx.arc(px, py, 2.5 * pulse + 1, 0, Math.PI * 2); ctx.fill()
        
        ctx.shadowBlur = 0
        ctx.fillStyle = `rgba(255, 255, 255, ${opacity * pulse})`
        ctx.beginPath(); ctx.arc(px, py, 1, 0, Math.PI * 2); ctx.fill()
      }
    })

    animationFrame = requestAnimationFrame(render)
  }

  render()
  onUnmounted(() => cancelAnimationFrame(animationFrame))
})
</script>

<template>
  <div ref="containerRef" class="w-full h-full relative overflow-hidden flex items-center justify-center bg-black group">
    <canvas ref="canvasRef" class="w-full h-full pointer-events-none z-0 transition-all duration-1000 group-hover:scale-105"></canvas>
    
    <div class="absolute inset-0 p-12 flex flex-col justify-between pointer-events-none z-10">
      <div class="flex justify-between items-start">
        <div class="text-[10px] font-bold text-zinc-600 uppercase tracking-[0.4em]"></div>
        <div class="flex items-center gap-2">
           <div class="h-2 w-2 rounded-full bg-zinc-600 animate-pulse"></div>
        </div>
      </div>
      <div class="h-[1px] w-full bg-white/5 relative overflow-hidden">
        <div class="absolute inset-0 bg-white/10 w-1/3 animate-shimmer"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(300%); }
}
.animate-shimmer {
  animation: shimmer 6s infinite linear;
}
</style>
