<script setup>
import { useRouter } from 'vue-router'
import LandingLayout from '@/components/section/landingpage/LandingLayout.vue'
import Navbar from '@/components/section/landingpage/Navbar.vue'
import Button from '@/components/ui/Button.vue'
import { Zap } from 'lucide-vue-next'
import GradientBlinds from '@/components/ui/background/GradientBlinds.vue'
import { onMounted, ref, nextTick, defineAsyncComponent } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'

const Features = defineAsyncComponent(() => import('@/components/section/landingpage/Features.vue'))
const Testimonials = defineAsyncComponent(() => import('@/components/section/landingpage/Testimonials.vue'))
const Pricing  = defineAsyncComponent(() => import('@/components/section/landingpage/Pricing.vue'))
const Footer   = defineAsyncComponent(() => import('@/components/section/landingpage/Footer.vue'))

const router = useRouter()
const authStore = useAuthStore()
const { isAuthenticated } = storeToRefs(authStore)
const showCookieBanner = ref(false)
const headlineRef = ref(null)

const brandName = 'gaptek'
const navigationItems = [
  { name: 'Fitur',         path: '#features' },
  { name: 'Harga',         path: '#pricing' },
  { name: 'Tentang Gaptek', path: '/about' }
]

const handleNavigation = (path) => {
  if (path.startsWith('#')) {
    const el = document.querySelector(path)
    if (el) el.scrollIntoView({ behavior: 'smooth' })
  } else {
    router.push(path)
  }
}

const navigateToLogin = () => router.push('/login')

const hideCookieBanner = () => {
  localStorage.setItem('gaptek_cookies_accepted', 'true')
  showCookieBanner.value = false
}

/**
 * Split teks plain (tanpa child element) menjadi <span> per karakter
 * dengan outer wrapper sebagai clip-mask (overflow:hidden)
 */
function splitPlainToChars(el) {
  const text = el.innerText.trim()
  el.innerHTML = ''
  el.setAttribute('aria-label', text)

  const chars = []
  text.split('').forEach((char) => {
    // Outer: clip-mask, karakter tersembunyi di bawahnya
    const outer = document.createElement('span')
    outer.style.cssText = 'display:inline-block; overflow:hidden; vertical-align:bottom; line-height:1.15;'

    // Inner: elemen yang bergerak
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
 * Wrap setiap child node di line2 (text node & element) dengan clip-mask
 * agar kata dan span italic bisa dianimasikan tanpa merusak styling
 */
function wrapLine2Words(el) {
  const items = []
  // Snapshot dulu karena kita akan mengubah DOM
  const nodes = Array.from(el.childNodes)
  nodes.forEach((node) => {
    if (node.nodeType === Node.TEXT_NODE) {
      const text = node.textContent
      if (!text.trim()) return // skip whitespace-only

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
  const hasAccepted = localStorage.getItem('gaptek_cookies_accepted')
  if (!hasAccepted) showCookieBanner.value = true

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

    // ✅ Sekarang tampilkan h1 — char sudah siap di posisi awal GSAP (y:110%, clipped)
    h1.style.opacity = '1'

    // ── Timeline ────────────────────────────────────────────
    const tl = gsap.timeline()

    // Baris 1: tiap karakter naik dari bawah clip-mask, stagger kiri→kanan
    tl.from(chars1, {
      y: '110%',
      rotationZ: 6,
      duration: 0.9,
      stagger: { each: 0.03, from: 'start' },
      ease: 'expo.out'
      // Tidak pakai immediateRender:false — GSAP langsung set y:110% lalu animasi ✅
    })

    // Baris 2: kata/span masuk dari bawah dengan skew, stagger kiri→kanan
    .from(words2, {
      y: '110%',
      skewX: -10,
      duration: 0.85,
      stagger: { each: 0.12, from: 'start' },
      ease: 'expo.out'
    }, '-=0.55')

    // Setelah masuk: wave float halus di baris 1 (loop)
    tl.to(chars1, {
      y: (i) => Math.sin(i * 1.0) * 3,
      duration: 3,
      stagger: { each: 0.06, repeat: -1, yoyo: true },
      ease: 'sine.inOut'
    }, '+=0.3')

  } catch (err) {
    // GSAP gagal: pastikan h1 tetap terlihat
    h1.style.opacity = '1'
    console.warn('[Hero] GSAP gagal, tampilan tetap normal.', err)
  }
})
</script>

<template>
  <LandingLayout>
    <!-- Navbar Section -->
    <Navbar 
      :brand-name="brandName" 
      :nav-items="navigationItems"
      @navigate="handleNavigation"
    >
      <template #logo>
        <div class="h-full w-full bg-zinc-100 flex items-center justify-center">
          <Zap class="h-4 w-4 text-zinc-900" />
        </div>
      </template>

      <template #actions>
        <Button class="font-bold px-3 sm:px-6 py-1.5 sm:py-2 text-xs sm:text-base rounded-xl bg-zinc-100 text-zinc-900 hover:bg-white transition-all active:scale-95" @click="navigateToLogin">
          Masuk
        </Button>
      </template>
    </Navbar>

    <!-- Hero Section -->
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

        <!-- Headline — hanya ini yang dianimasikan GSAP -->
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

        <!-- Subheadline — STATIS, tidak dianimasikan -->
        <p class="text-lg md:text-xl text-zinc-400 font-medium max-w-2xl mx-auto leading-relaxed">
          Manajemen bertenaga AI yang membantu Anda <br class="hidden md:block" />
          membangun bisnis lebih cepat dan terorganisir.
        </p>

        <!-- CTA Buttons — STATIS -->
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

    <!-- Content Sections -->

    <Features />
    <Testimonials />
    <Pricing />

    <!-- Footer -->
    <Footer />

    
    <!-- Cookie Banner -->
    <Transition
      enter-active-class="transition duration-500 ease-out"
      enter-from-class="transform translate-y-20 opacity-0"
      enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-300 ease-in"
      leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform translate-y-20 opacity-0"
    >
      <div v-if="showCookieBanner" class="fixed bottom-8 right-8 z-[60] max-w-sm">
        <div class="bg-white text-zinc-900 p-5 rounded-2xl shadow-2xl flex items-center justify-between gap-8 backdrop-blur-xl bg-opacity-95 border border-black/5">
          <div class="space-y-1">
            <p class="text-[11px] leading-relaxed font-semibold">Kebijakan Cookie</p>
            <p class="text-[10px] leading-relaxed opacity-70">
              Kami menggunakan cookie untuk mempersonalisasi konten, menjalankan iklan, dan menganalisis lalu lintas.
            </p>
          </div>
          <button 
            @click="hideCookieBanner"
            class="shrink-0 px-5 py-2 bg-zinc-900 text-white rounded-xl text-[10px] font-bold hover:bg-black transition-all active:scale-95 shadow-lg shadow-black/10"
          >
            Oke
          </button>
        </div>
      </div>
    </Transition>


  </LandingLayout>
</template>

<style scoped>
/*
  h1 tersembunyi di awal — GSAP yang akan reveal via opacity:1
  Jika GSAP gagal, catch block set opacity:1 secara manual
  Ini mencegah "flash" teks sebelum animasi jalan
*/

/* Performance hint untuk karakter yang dianimasikan */
.hero-char,
.hero-word {
  will-change: transform;
}
</style>


