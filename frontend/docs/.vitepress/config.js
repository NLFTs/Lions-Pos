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
      { text: 'Panduan', link: '/guide/' },
      { text: 'Services', link: '/services/' }
    ],

    logo: '/gaptek-hit.png',

    socialLinks: [
      { icon: 'github', link: 'https://github.com' },
      { icon: 'discord', link: 'https://discord.com' }
    ],

    sidebar: [
      {
        text: 'Introduction',
        collapsed: false,
        items: [
          { text: 'Pengenalan', link: '/guide/' },
          { text: 'Instalasi', link: '/guide/installation' },
          { text: 'Mulai', link: '/guide/getting-started' },
        ]
      },
      {
        text: 'Services',
        collapsed: false,
        items: [
          { text: 'Overview', link: '/services/' },
          { text: 'Product', link: '/services/product' },
          { text: 'Showcase', link: '/services/showcase' },
        ]
      },      
      {
        text: 'Legal',
        collapsed: false,
        items: [
          { text: 'Syarat dan ketentuan', link: '/terms-of-services' },
          { text: 'Privasi dan kebijakan data', link: '/privacy-policy' },
        ]
      },
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
