import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'Gaptek Docs',
  description: 'Dokumentasi Minimalis Gaptek',
  base: '/docs/',
  outDir: '../public/docs',
  cleanUrls: true,
  
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Panduan', link: '/introduction' }
    ],

    logo: '/gaptek-hit.png',

    socialLinks: [
      { icon: 'github', link: 'https://github.com' },
      { icon: 'discord', link: 'https://discord.com' }
    ],
    
    sidebar: [
      {
        text: 'Dokumentasi',
        items: [
          { text: 'Pengenalan', link: '/introduction' },
        ]
      }
    ],

    search: {
      provider: 'local'
    },

    outline: {
      level: [2, 3],
      label: 'Di Halaman Ini'
    },

    docFooter: {
      prev: 'Sebelumnya',
      next: 'Selanjutnya'
    },

    darkModeSwitchLabel: 'Mode Gelap',
    lightModeSwitchLabel: 'Mode Terang',
    sidebarMenuLabel: 'Menu',
    returnToTopLabel: 'Kembali ke Atas'
  }
})
