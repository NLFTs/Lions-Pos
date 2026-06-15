<script setup>
import { onMounted, ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import GradientBlinds from '@/components/ui/background/GradientBlinds.vue'
import Button from '@/components/ui/Button.vue'

const router = useRouter()
const headlineRef = ref(null)

const navigateToLogin = () => router.push('/login')

/**
 * Split teks plain menjadi <span> per karakter
 * dengan outer wrapper sebagai clip-mask
 */
function splitPlainToChars(el) {
  const text = el.innerText.trim()
  el.innerHTML = ''
  el.setAttribute('aria-label', text)

  const chars = []
  text.split('').forEach((char) => {
    const outer = document.createElement('span')
    outer.style.cssText = 'display:inline-block; overflow:hidden; vertical-align:bottom; line-height:1.15;'

    const inner = document.createElement('span')
    inner.style.cssText = 'display:inline-block; will-change:transform;'
    inner.textContent = char === ' ' ? '\u00A0' : char

    outer.appendChild(inner)
    el.appendChild(outer)
    chars.push(inner)
  })
  return chars
}

/**
 * Wrap setiap child node di line2 dengan clip-mask
 */
function wrapLine2Words(el) {
  const items = []
  const nodes = Array.from(el.childNodes)
  nodes.forEach((node) => {
    if (node.nodeType === Node.TEXT_NODE) {
      const text = node.textContent
      if (!text.trim()) return

      const outer = document.createElement('span')
      outer.style.cssText = 'display:inline-block; overflow:hidden; vertical-align:bottom; line-height:1.15;'
      const inner = document.createElement('span')
      inner.style.cssText = 'display:inline-block; will-change:transform;'
      inner.textContent = text
      outer.appendChild(inner)
      node.replaceWith(outer)
      items.push(inner)
    } else if (node.nodeType === Node.ELEMENT_NODE) {
      const outer = document.createElement('span')
      outer.style.cssText = 'display:inline-block; overflow:hidden; vertical-align:bottom; line-height:1.15;'
      const inner = node.cloneNode(true)
      inner.style.cssText = (inner.getAttribute('style') || '') + '; display:inline-block; will-change:transform;'
      outer.appendChild(inner)
      node.replaceWith(outer)
      items.push(inner)
    }
  })
  return items
}

onMounted(async () => {
  await nextTick()

  const h1 = headlineRef.value
  if (!h1) return

  try {
    const { gsap } = await import('@/lib/gsap')

    const line1El = h1.querySelector('#hero-line1')
    const line2El = h1.querySelector('#hero-line2')

    // Split DOM
    const chars1  = splitPlainToChars(line1El)
    const words2  = wrapLine2Words(line2El)

    h1.style.opacity = '1'

    const tl = gsap.timeline()

    // Baris 1: naik dari bawah clip-mask
    tl.from(chars1, {
      y: '110%',
      rotationZ: 6,
      duration: 0.9,
      stagger: { each: 0.03, from: 'start' },
      ease: 'expo.out'
    })
    // Baris 2: masuk dari bawah dengan skew
    .from(words2, {
      y: '110%',
      skewX: -10,
      duration: 0.85,
      stagger: { each: 0.12, from: 'start' },
      ease: 'expo.out'
    }, '-=0.55')

    // Wave float halus di baris 1
    tl.to(chars1, {
      y: (i) => Math.sin(i * 1.0) * 3,
      duration: 3,
      stagger: { each: 0.06, repeat: -1, yoyo: true },
      ease: 'sine.inOut'
    }, '+=0.3')

  } catch (err) {
    h1.style.opacity = '1'
    console.warn('[Hero] GSAP gagal, tampilan tetap normal.', err)
  }
})
</script>

<template>
  <main class="relative flex-grow flex flex-col items-center pt-24 pb-48 px-6 overflow-hidden z-10">
    <!-- Background Gradient Blinds -->
    <div class="absolute inset-0 z-0 opacity-100 pointer-events-none">
      <GradientBlinds
        :gradient-colors="['#1EA03F', '#182FFF']"
        :angle="0"
        :noise="0.3"
        :blind-count="12"
        :blind-min-width="50"
        :spotlight-radius="0.5"
        :spotlight-softness="1"
        :spotlight-opacity="1"
        :mouse-dampening="0.15"
        :distort-amount="0"
        shine-direction="left"
        mix-blend-mode="screen"
      />
    </div>
    
    <div class="relative z-10 space-y-10 max-w-5xl mx-auto text-center">
      <!-- Headline -->
      <h1
        ref="headlineRef"
        class="text-6xl md:text-8xl font-medium font-serif tracking-tight text-white leading-[1.05]"
        style="opacity: 0;"
      >
        <span id="hero-line1" class="block">Platform All-in-One</span>
        <span id="hero-line2" class="block">
          untuk <span class="italic text-white">Retail &amp; F&amp;B</span> modern
        </span>
      </h1>

      <!-- Subheadline -->
      <p class="text-lg md:text-xl text-zinc-400 font-medium max-w-2xl mx-auto leading-relaxed">
        Manajemen bertenaga AI yang membantu Anda <br class="hidden md:block" />
        membangun bisnis lebih cepat dan terorganisir.
      </p>

      <!-- CTA Buttons -->
      <div class="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
        <Button
          class="h-12 px-8 rounded-full bg-white text-zinc-900 hover:scale-105 transition-transform duration-300 font-medium"
          @click="navigateToLogin"
        >
          Masuk
        </Button>
        <Button
          variant="ghost"
          class="h-12 px-8 rounded-full border border-zinc-800 text-white hover:bg-zinc-900 transition-colors duration-300"
        >
          Lihat Demo
        </Button>
      </div>
    </div>
  </main>
</template>

<style scoped>
.hero-char,
.hero-word {
  will-change: transform;
}
</style>
