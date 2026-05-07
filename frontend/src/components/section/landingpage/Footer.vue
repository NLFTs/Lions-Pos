<script setup>
import { Zap, Github, Twitter, Instagram, Linkedin } from 'lucide-vue-next'

const currentYear = new Date().getFullYear()

const footerLinks = [
  {
    title: 'Produk',
    links: [
      { name: 'Fitur', href: '#features' },
      { name: 'Harga', href: '#pricing' },
      { name: 'Demo', href: '#' },
      { name: 'Integrasi', href: '#' }
    ]
  },
  {
    title: 'Perusahaan',
    links: [
      { name: 'Tentang Kami', href: '/about' },
      { name: 'Karir', href: '#' },
      { name: 'Blog', href: '#' },
      { name: 'Kontak', href: '#' }
    ]
  },
  {
    title: 'Bantuan',
    links: [
      { name: 'Pusat Bantuan', href: '#' },
      { name: 'Dokumentasi', href: '#' },
      { name: 'Status Sistem', href: '#' },
      { name: 'Komunitas', href: '#' }
    ]
  },
  {
    title: 'Legal',
    links: [
      { name: 'Privasi', href: '#' },
      { name: 'Ketentuan', href: '#' },
      { name: 'Cookie', href: '#' }
    ]
  }
]
</script>

<template>
  <footer class="pt-24 pb-12 px-6 border-t border-white/5 bg-black">
    <div class="max-w-7xl mx-auto">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-6 gap-12 mb-16">
        <!-- Brand Info -->
        <div class="lg:col-span-2">
          <div class="flex items-center gap-2 mb-6">
            <div class="w-8 h-8 bg-zinc-100 rounded-lg flex items-center justify-center">
              <Zap class="h-5 w-5 text-zinc-900" />
            </div>
            <span class="text-xl font-bold tracking-tight text-white uppercase">gaptek</span>
          </div>
          <p class="text-zinc-400 text-sm leading-relaxed max-w-xs mb-8">
            Platform operasional tercanggih untuk transformasi digital bisnis retail dan F&B masa depan.
          </p>
          <div class="flex gap-4">
            <a href="#" class="w-10 h-10 rounded-full bg-zinc-900 flex items-center justify-center text-zinc-400 hover:text-white hover:bg-zinc-800 transition-all">
              <Twitter class="w-5 h-5" />
            </a>
            <a href="#" class="w-10 h-10 rounded-full bg-zinc-900 flex items-center justify-center text-zinc-400 hover:text-white hover:bg-zinc-800 transition-all">
              <Instagram class="w-5 h-5" />
            </a>
            <a href="#" class="w-10 h-10 rounded-full bg-zinc-900 flex items-center justify-center text-zinc-400 hover:text-white hover:bg-zinc-800 transition-all">
              <Linkedin class="w-5 h-5" />
            </a>
            <a href="#" class="w-10 h-10 rounded-full bg-zinc-900 flex items-center justify-center text-zinc-400 hover:text-white hover:bg-zinc-800 transition-all">
              <Github class="w-5 h-5" />
            </a>
          </div>
        </div>

        <!-- Links -->
        <div v-for="group in footerLinks" :key="group.title" class="lg:col-span-1">
          <h4 class="text-white font-semibold mb-6">{{ group.title }}</h4>
          <ul class="space-y-4">
            <li v-for="link in group.links" :key="link.name">
              <a :href="link.href" class="text-zinc-400 text-sm hover:text-primary transition-colors">
                {{ link.name }}
              </a>
            </li>
          </ul>
        </div>
      </div>

      <!-- Bottom Bar -->
      <div class="pt-8 border-t border-white/5 flex flex-col md:flex-row justify-between items-center gap-4">
        <p class="text-zinc-500 text-xs">
          © {{ currentYear }} Gaptek Technology. Seluruh hak cipta dilindungi.
        </p>
        <div class="flex items-center gap-6">
          <span class="flex items-center gap-2 text-zinc-500 text-xs">
            <span class="w-2 h-2 bg-emerald-500 rounded-full animate-pulse"></span>
            Semua sistem operasional
          </span>
        </div>
      </div>
    </div>
  </footer>
</template>
