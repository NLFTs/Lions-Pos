<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { Send, Facebook, Youtube, Linkedin, ArrowUpCircle } from 'lucide-vue-next'

const currentYear = new Date().getFullYear()
const footerRef = ref(null)

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

let scrollTriggerInstance = null

onMounted(async () => {
  await nextTick()
  if (!footerRef.value) return

  // GSAP hanya sebagai progressive enhancement
  // Jika gagal, elemen tetap visible karena CSS tidak menyembunyikannya
  try {
    const { gsap, ScrollTrigger } = await import('@/lib/gsap')

    const container = footerRef.value
    const letters = Array.from(container.querySelectorAll('.gaptek-letter'))
    const headline = container.querySelector('.footer-headline')
    const contact = container.querySelector('.footer-contact')
    const bottom = container.querySelector('.footer-bottom')

    // Pastikan semua elemen dalam state visible sebelum animasi apapun
    gsap.set([headline, contact, ...letters, bottom], {
      clearProps: 'opacity,transform,y,rotation,visibility'
    })

    // Buat timeline dengan immediateRender: false
    // Ini mencegah GSAP menyembunyikan elemen saat timeline dibuat tapi belum diplay
    const tl = gsap.timeline({ paused: true })

    tl.from(headline, {
      y: 40,
      opacity: 0,
      duration: 0.8,
      ease: 'power3.out',
      immediateRender: false  // ← KUNCI: tidak sembunyikan saat mount
    })
    .from(contact, {
      y: 25,
      opacity: 0,
      duration: 0.6,
      ease: 'power2.out',
      immediateRender: false
    }, '-=0.4')
    .from(letters, {
      y: 120,
      opacity: 0,
      rotation: (i) => (i % 2 === 0 ? 8 : -8),
      duration: 1.0,
      stagger: 0.08,
      ease: 'elastic.out(1, 0.75)',
      immediateRender: false
    }, '-=0.3')
    .from(bottom, {
      y: 15,
      opacity: 0,
      duration: 0.6,
      ease: 'power2.out',
      immediateRender: false
    }, '-=0.7')

    // Cek apakah footer sudah terlihat saat halaman di-load
    const rect = container.getBoundingClientRect()
    if (rect.top < window.innerHeight * 0.95) {
      // Footer sudah visible, langsung play tanpa scroll trigger
      tl.play()
    } else {
      // Footer di luar viewport, pakai ScrollTrigger
      scrollTriggerInstance = ScrollTrigger.create({
        trigger: container,
        start: 'top 92%',
        once: true,
        onEnter: () => tl.play()
      })

      ScrollTrigger.refresh()
    }
  } catch (err) {
    // GSAP gagal dimuat — tidak masalah, elemen sudah visible via CSS
    console.warn('[Footer] GSAP tidak tersedia, tampilan tetap normal.', err)
  }
})

onUnmounted(() => {
  try {
    if (scrollTriggerInstance) {
      scrollTriggerInstance.kill()
    }
    const { gsap } = require('@/lib/gsap')
    if (footerRef.value) gsap.killTweensOf(footerRef.value)
  } catch (_) {}
})
</script>

<template>
  <footer
    ref="footerRef"
    class="bg-black pt-24 pb-8 px-6 text-white border-t border-white/5 font-sans overflow-hidden"
  >
    <div class="max-w-[1400px] mx-auto flex flex-col">

      <!-- Top Text: selalu visible via CSS -->
      <div class="mb-20">
        <h2 class="footer-headline text-4xl md:text-5xl lg:text-7xl font-medium tracking-tight max-w-4xl leading-tight">
          Ada ide bisnis luar biasa yang <span class="text-zinc-600">sedang terbayang di benak Anda?</span>
        </h2>
      </div>

      <!-- Contact & Links -->
      <div class="footer-contact flex flex-col md:flex-row justify-between items-start md:items-end gap-8 mb-8">
        <div class="flex items-center gap-3 group cursor-pointer">
          <div class="p-2 bg-white text-black rounded-full group-hover:scale-110 transition-transform duration-300">
            <Send class="w-5 h-5 -rotate-45" fill="currentColor" />
          </div>
          <a href="mailto:team@gaptek.org" class="text-xl md:text-3xl font-medium hover:text-zinc-300 transition-colors tracking-tight">
            team@gaptek.org
          </a>
        </div>

        <nav class="flex flex-wrap items-center gap-6 md:gap-8">
          <a href="/about" class="text-sm font-medium hover:text-zinc-400 transition-colors">Tentang Kami</a>
          <a href="#" class="text-sm font-medium hover:text-zinc-400 transition-colors">Layanan</a>
          <a href="#" class="text-sm font-medium hover:text-zinc-400 transition-colors">Hubungi Kami</a>
          <a href="#" class="text-sm font-medium hover:text-zinc-400 transition-colors">Artikel Blog</a>
        </nav>
      </div>

      <!-- Divider -->
      <div class="w-full h-px bg-white/10 mb-12"></div>

      <!-- ✅ Tulisan GAPTEK Besar — SELALU VISIBLE via CSS (tidak bergantung GSAP) -->
      <div class="w-full flex justify-between items-center mb-16 select-none gaptek-container">
        <span class="gaptek-letter font-gaptek text-[18vw] leading-[0.75] font-black tracking-tighter letter-hover text-outline">G</span>
        <span class="gaptek-letter font-gaptek text-[18vw] leading-[0.75] font-black tracking-tighter letter-hover text-solid">A</span>
        <span class="gaptek-letter font-gaptek text-[18vw] leading-[0.75] font-black tracking-tighter letter-hover text-outline">P</span>
        <span class="gaptek-letter font-gaptek text-[18vw] leading-[0.75] font-black tracking-tighter letter-hover text-solid">T</span>
        <span class="gaptek-letter font-gaptek text-[18vw] leading-[0.75] font-black tracking-tighter letter-hover text-outline">E</span>
        <span class="gaptek-letter font-gaptek text-[18vw] leading-[0.75] font-black tracking-tighter letter-hover text-solid">K</span>
      </div>

      <!-- Bottom Bar -->
      <div class="footer-bottom flex flex-col md:flex-row justify-between items-center gap-6 text-xs text-zinc-500 font-medium mt-8">
        <!-- Socials -->
        <div class="flex items-center gap-6">
          <a href="#" class="hover:text-white transition-colors"><Facebook class="w-4 h-4" /></a>
          <a href="#" class="hover:text-white transition-colors"><Youtube class="w-4 h-4" /></a>
          <a href="#" class="hover:text-white transition-colors"><Linkedin class="w-4 h-4" /></a>
          <!-- X (Twitter) -->
          <a href="#" class="hover:text-white transition-colors">
            <svg viewBox="0 0 24 24" class="w-4 h-4 fill-current" aria-hidden="true"><path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/></svg>
          </a>
          <!-- TikTok -->
          <a href="#" class="hover:text-white transition-colors">
            <svg viewBox="0 0 24 24" class="w-4 h-4 fill-current" aria-hidden="true"><path d="M19.59 6.69a4.83 4.83 0 0 1-3.77-4.25V2h-3.45v13.67a2.89 2.89 0 0 1-5.2 1.74 2.89 2.89 0 0 1 2.31-4.64 2.93 2.93 0 0 1 .88.13V9.4a6.84 6.84 0 0 0-1-.05A6.33 6.33 0 0 0 5 20.1a6.34 6.34 0 0 0 10.86-4.43v-7a8.16 8.16 0 0 0 4.77 1.52v-3.4a4.85 4.85 0 0 1-1-.1z"/></svg>
          </a>
        </div>

        <!-- Copyright & Legal -->
        <div class="flex flex-wrap items-center justify-center gap-4 md:gap-8">
          <span>&copy; {{ currentYear }} Gaptek. Seluruh hak cipta dilindungi.</span>
          <a href="#" class="hover:text-white transition-colors">Kebijakan Privasi</a>
          <a href="#" class="hover:text-white transition-colors">Syarat & Ketentuan</a>
        </div>

        <!-- Scroll Top -->
        <button @click="scrollToTop" class="flex items-center gap-2 hover:text-white transition-colors uppercase font-bold tracking-widest text-[10px]">
          KEMBALI KE ATAS
          <ArrowUpCircle class="w-4 h-4" />
        </button>
      </div>
    </div>
  </footer>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@700;900&display=swap');

/* ✅ FONT */
.font-gaptek {
  font-family: 'Space Grotesk', sans-serif;
}

/* ✅ FALLBACK UTAMA: semua elemen selalu visible dari awal
   GSAP tidak bisa menyembunyikannya kecuali animasi berjalan */
.footer-headline,
.footer-contact,
.footer-bottom {
  opacity: 1 !important;
  transform: none !important;
  visibility: visible !important;
}

.gaptek-letter {
  /* Huruf selalu terlihat — GSAP hanya animasi masuknya saja */
  opacity: 1 !important;
  visibility: visible !important;
  display: inline-block;
  cursor: crosshair;
  transition: transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275),
              color 0.3s ease,
              text-shadow 0.3s ease;
}

/* Warna huruf */
.text-outline {
  color: transparent;
  -webkit-text-stroke: 3px rgba(255, 255, 255, 0.18);
}

.text-solid {
  color: #ffffff;
}

/* Hover individual huruf */
.gaptek-letter:hover {
  transform: translateY(-20px) scale(1.05) !important;
  color: #ffffff !important;
  -webkit-text-stroke: 0px transparent !important;
  text-shadow: 0 0 60px rgba(255, 255, 255, 0.5);
}

/* Hover container: balik semua huruf ke outline, kecuali yang dihover */
.gaptek-container:hover .text-solid {
  color: transparent;
  -webkit-text-stroke: 3px rgba(255, 255, 255, 0.18);
}

.gaptek-container:hover .gaptek-letter:hover {
  color: #ffffff !important;
  -webkit-text-stroke: 0px transparent !important;
  z-index: 10;
  position: relative;
}
</style>
