<script setup>
import { ref } from 'vue'

const testimonials = [
  {
    quote: "Saya sudah menggunakan Gaptek selama hampir satu dekade dan belum pernah terpikir untuk beralih ke sistem lain. Sangat stabil.",
    author: "davingm",
    role: "founder NLFTs ltd",
    avatar: "https://avatars.githubusercontent.com/u/228851591?v=4",
    logo: "NLFTs",
    isTall: true
  },
  {
    quote: "Gaptek adalah fondasi utama operasional kami. Membantu pengelolaan stok di puluhan cabang menjadi jauh lebih mudah dan transparan.",
    author: "miftah aufar",
    role: "Owner, Mart 24 Jam",
    avatar: "https://avatars.githubusercontent.com/u/232498018?v=4"
  },
  {
    quote: "Gaptek benar-benar menghilangkan kerumitan dalam membangun sistem manajemen toko yang modern dan scalable.",
    author: "Lintang Nur Wijaya",
    role: "Founder, Warung Madura",
    avatar: "https://avatars.githubusercontent.com/u/216552062?v=4"
  },
  {
    quote: "Kecepatan dan performa Gaptek dalam menangani ribuan transaksi harian benar-benar tidak tertandingi di kelasnya.",
    author: "Gunawan",
    role: "CEO Roti Goreng Garut",
    avatar: "https://avatars.githubusercontent.com/u/228840854?v=4"
  },
  {
    quote: "Ekosistem Gaptek telah menjadi kunci sukses pertumbuhan bisnis kami. Sistem ini memungkinkan kami untuk ekspansi cabang dengan sangat cepat.",
    author: "Salsa",
    role: "Founder, Fashion Hub",
    avatar: "https://i.pravatar.cc/150?u=dewi",
    logo: "gaptek analytics",
    isTall: true
  },
  {
    quote: "Gaptek memberikan angin segar bagi pengusaha di Indonesia dengan sistem yang intuitif dan dukungan komunitas yang luar biasa.",
    author: "Siti Nur Fadila",
    role: "Creator, Retail Lab",
    avatar: "https://i.pravatar.cc/150?u=maya"
  },
  {
    quote: "Fitur analitik, manajemen stok, dan integrasi kasirnya—ini adalah paket lengkap yang paling sempurna untuk bisnis retail.",
    author: "Gak tau siapa",
    role: "CEO, Solusi Dagang",
    avatar: "https://i.pravatar.cc/150?u=hendra"
  }
]
</script>

<template>
  <section id="testimonials" class="py-32 px-6 bg-[#000000] relative overflow-hidden z-10">
    <!-- Subtle Noise/Grain Overlay -->
    <div class="absolute inset-0 opacity-[0.02] pointer-events-none" 
         style="background-image: url('https://grainy-gradients.vercel.app/noise.svg');">
    </div>

    <div class="max-w-[1200px] mx-auto relative z-10">
      
      <!-- Bento Grid Testimonials -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
        
        <!-- Column 1 -->
        <div class="flex flex-col gap-5">
          <div v-for="t in [testimonials[0], testimonials[5]]" :key="t.author" class="testimonial-card" :class="{ 'is-tall': t.isTall }">
            <div class="card-content">
              <!-- Logo Section -->
              <div v-if="t.logo" class="logo-container">
                <template v-if="t.logo === 'NLFTs'">
                  <div class="flex items-center gap-2">
                    <div class="w-7 h-7 bg-white rounded-md flex items-center justify-center">
                      <span class="text-black font-black text-lg">N</span>
                    </div>
                    <span class="text-white font-bold tracking-tight text-xl">NLFTs</span>
                  </div>
                </template>
                <template v-if="t.logo === 'gaptek'">
                  <div class="flex items-center gap-2">
                    <div class="w-6 h-6 bg-white rounded flex items-center justify-center">
                      <svg class="h-4 w-4 text-black" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
                    </div>
                    <span class="text-white font-bold tracking-tight text-xl">gaptek</span>
                  </div>
                </template>
                <template v-if="t.logo === 'gaptek analytics'">
                  <div class="flex flex-col">
                    <span class="text-white text-xl font-bold leading-[0.9]">gaptek</span>
                    <span class="text-white text-xl font-light leading-[0.9]">analytics/</span>
                  </div>
                </template>
              </div>

              <p class="quote-text" :class="{ 'is-tall': t.isTall }">
                &ldquo;{{ t.quote }}&rdquo;
              </p>
            </div>

            <div class="card-footer">
              <div class="author-info">
                <h4 class="author-name">{{ t.author }}</h4>
                <p class="author-role">{{ t.role }}</p>
              </div>
              <img :src="t.avatar" :alt="t.author" class="author-avatar" />
            </div>
          </div>
        </div>

        <!-- Column 2 -->
        <div class="flex flex-col gap-5">
          <div v-for="t in [testimonials[1], testimonials[3], testimonials[6]]" :key="t.author" class="testimonial-card">
            <div class="card-content">
              <p class="quote-text">
                &ldquo;{{ t.quote }}&rdquo;
              </p>
            </div>

            <div class="card-footer">
              <div class="author-info">
                <h4 class="author-name">{{ t.author }}</h4>
                <p class="author-role">{{ t.role }}</p>
              </div>
              <img :src="t.avatar" :alt="t.author" class="author-avatar" />
            </div>
          </div>
        </div>

        <!-- Column 3 -->
        <div class="flex flex-col gap-5">
          <div v-for="t in [testimonials[2], testimonials[4]]" :key="t.author" class="testimonial-card" :class="{ 'is-tall': t.isTall }">
            <div class="card-content">
              <!-- Logo Section -->
              <div v-if="t.logo" class="logo-container">
                <template v-if="t.logo === 'gaptek analytics'">
                  <div class="flex flex-col">
                    <span class="text-white text-xl font-bold leading-[0.9]">gaptek</span>
                    <span class="text-white text-xl font-light leading-[0.9]">analytics/</span>
                  </div>
                </template>
              </div>

              <p class="quote-text" :class="{ 'is-tall': t.isTall }">
                &ldquo;{{ t.quote }}&rdquo;
              </p>
            </div>

            <div class="card-footer">
              <div class="author-info">
                <h4 class="author-name">{{ t.author }}</h4>
                <p class="author-role">{{ t.role }}</p>
              </div>
              <img :src="t.avatar" :alt="t.author" class="author-avatar" />
            </div>
          </div>
        </div>

      </div>

    </div>
  </section>
</template>

<style scoped>
.testimonial-card {
  @apply relative p-8 rounded-xl bg-[#111111] border border-white/[0.08] flex flex-col justify-between transition-all duration-300;
  min-height: 200px;
}

.testimonial-card:hover {
  @apply border-white/[0.15] bg-[#141414];
}

.testimonial-card.is-tall {
  @apply md:flex-1;
  min-height: 480px;
}

.logo-container {
  @apply flex items-center gap-2 mb-8;
}

.quote-text {
  @apply font-medium leading-[1.4] text-zinc-100 text-[16px];
}

.quote-text.is-tall {
  @apply text-[26px] tracking-tight leading-[1.2] mt-4;
}

.card-footer {
  @apply mt-10 flex items-end justify-between;
}

.author-info {
  @apply space-y-0.5;
}

.author-name {
  @apply text-[15px] font-bold text-white;
}

.author-role {
  @apply text-[13px] text-zinc-500 font-medium;
}

.author-avatar {
  @apply w-11 h-11 rounded-lg object-cover;
}

/* Ensure Column 3 stretches correctly if tall card is present */
.md\:flex-1 {
  flex: 1 1 0%;
}
</style>
