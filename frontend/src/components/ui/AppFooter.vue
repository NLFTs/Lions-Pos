<template>
  <footer class="bg-[#0a0a0a] border-t border-zinc-800 pt-20 pb-10 px-6">
    <div class="max-w-6xl mx-auto flex flex-col md:flex-row justify-between gap-16">
      
      <div class="flex-shrink-0 flex flex-col gap-4">
        <a href="/" class="flex items-center gap-2 text-white transition-opacity hover:opacity-80">
          <img src="/logo-gaptek.svg" alt="Gaptek Logo" class="w-8 h-8">
          <span class="font-semibold tracking-tight">Gaptek</span>
        </a>
        <p class="text-zinc-500 text-sm max-w-[200px]">
          Membangun masa depan web lebih cepat dan efisien.
        </p>
      </div>

      <div class="grid grid-cols-2 md:grid-cols-4 gap-12 text-sm">
        <div v-for="section in footerLinks" :key="section.title" class="flex flex-col gap-4">
          <h3 class="font-medium text-white">{{ section.title }}</h3>
          <ul class="flex flex-col gap-3">
            <li v-for="link in section.items" :key="link.name">
              <a 
                :href="link.url" 
                class="text-zinc-400 hover:text-white transition-colors duration-200"
              >
                {{ link.name }}
              </a>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <div class="max-w-6xl mx-auto mt-20 pt-10 border-t border-zinc-900 flex flex-col md:flex-row justify-between items-center gap-4 text-[13px] text-zinc-500">
      <p>© {{ new Date().getFullYear() }} <a href="https://github.com/vuxilabs" class="text-white hover:underline transition-colors">Vuxilabs</a> All rights reserved.</p>
      
      <div class="flex gap-8">
        <a href="/docs/introduction" class="hover:text-white hover:underline transition-colors">kebijakan Privasi</a>
        <RouterLink to="/docs/terms-of-service" class="hover:text-white hover:underline transition-colors">Syarat Penggunaan (ToS)</RouterLink>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { RouterLink } from 'vue-router';  
const footerLinks = [
  {
    title: 'Product',
    items: [
      { name: 'Dashboard', url: '#' },
      { name: 'Features', url: '#' },
      { name: 'Pricing', url: '#' }
    ]
  },
  {
    title: 'Company',
    items: [
      { name: 'About', url: '#' },
      { name: 'Careers', url: '#' },
      { name: 'Blog', url: '#' }
    ]
  },
  {
    title: 'Resources',
    items: [
      { name: 'Docs', url: '/docs' },
      { name: 'Changelog', url: '#' },
      { name: 'Support', url: '#' }
    ]
  },
  {
    title: 'Legal',
    items: [
      { name: 'Privacy', url: '#' },
      { name: 'Terms', url: '#' },
      { name: 'Security', url: '#' }
    ]
  }
]
</script>