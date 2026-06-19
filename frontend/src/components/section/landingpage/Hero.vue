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

    // Wave float removed to keep characters upright and still

  } catch (err) {
    h1.style.opacity = '1'
    console.warn('[Hero] GSAP gagal, tampilan tetap normal.', err)
  }
})
</script>

<template>
  <main class="relative flex-grow flex items-center pt-24 pb-48 px-6 overflow-hidden z-10">
  <!-- Background -->
  <div class="absolute inset-0 z-0 pointer-events-none">
    
    <!-- Layer 1 -->
    <div
      class="absolute inset-0"
      style="
        background:
          radial-gradient(
            circle at top left,
            rgba(28,1,54,0.9) 0%,
            rgba(28,1,54,0.45) 20%,
            rgba(0,0,0,1) 50%
          );
      "
    ></div>

    <!-- Layer 2 -->
    <div
      class="absolute inset-0 bg-center bg-cover opacity-40"
      style="
        background-image: url('/box-bg.png');
      "
    ></div>

    <!-- Overlay -->
    <div class="absolute inset-0 bg-black/30"></div>
  </div>

  <div class="relative z-10 max-w-5xl w-full mx-auto">
    <div class="space-y-8 max-w-3xl text-left">
      
      <!-- Headline -->
      <h1
        ref="headlineRef"
        class="text-5xl md:text-7xl font-medium font-serif tracking-tight text-white leading-[1.05]"
        style="opacity: 0;"
      >
        <span id="hero-line1" class="block">
          Platform All-in-One
        </span>

        <span id="hero-line2" class="block">
          <span class="italic text-white">
            Retail &amp; F&amp;B <span class="text-indigo-500">modern</span>
            </span>
          </span>
      </h1>

      <!-- Subheadline -->
      <p
        class="text-base md:text-lg text-zinc-400 font-medium max-w-xl leading-relaxed"
      >
        Manajemen bertenaga AI yang membantu Anda
        membangun bisnis lebih cepat dan terorganisir.
      </p>

      <!-- CTA Buttons -->
      <div class="flex flex-col sm:flex-row items-start gap-4 pt-2">
        <Button
          class="h-12 px-8 bg-white text-zinc-900 hover:scale-105 transition-transform duration-300 font-medium"
          @click="navigateToLogin"
        >
          Masuk
        </Button>

        <Button
          variant="ghost"
          class="h-12 px-8 border border-zinc-800 text-white hover:bg-zinc-900 transition-colors duration-300"
        >
          Lihat Demo
        </Button>
      </div>

    </div>
  </div>
</main>
</template>

<style scoped>
.hero-char,
.hero-word {
  will-change: transform;
}

main {
  font-family: 'Roobert', system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

h1 {
  font-family: 'Roobert', system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  font-weight: 300 !important;
  font-size: 80px !important;
  line-height: 1.02 !important;
}
</style>
