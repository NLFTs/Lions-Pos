import { ViteSSG } from 'vite-ssg'
import { createPinia } from 'pinia'
import App from './App.vue'
import { routes, setupRouterGuards } from './router'
import './assets/index.css'
import { useThemeStore } from '@/stores/theme'

export const createApp = ViteSSG(
  App,
  { routes, base: import.meta.env.PROD ? '/_/' : '/' },
  ({ app, router, isClient }) => {
    const pinia = createPinia()
    app.use(pinia)

    if (isClient) {
      setupRouterGuards(router)
      
      // Initialize theme store and apply saved preferences
      const themeStore = useThemeStore()
      themeStore.init()
    }
  }
)
