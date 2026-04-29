import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  // Dev mode: base '/' agar URL tanpa prefix (localhost:5173/login)
  // Prod build: base '/_/' agar Spring Boot serve dengan prefix (localhost:8090/_/login)
  const base = mode === 'production' ? '/_/' : '/'

  return {
    base,
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    optimizeDeps: {
      include: ['vue', 'vue-router', 'pinia', 'axios'],
    },
    build: {
      outDir: '../src/main/resources/static',
      emptyOutDir: true,
      target: 'es2022',
      rollupOptions: {
        output: {
          entryFileNames: 'assets/index.js',
          chunkFileNames: 'assets/[name]-[hash].js',
          assetFileNames: 'assets/[name][extname]',
        },
      },
    },
    server: {
      port: parseInt(env.VITE_DEV_PORT) || 5173,
      proxy: {
        '/api': 'http://localhost:8090',
      },
    },
  }
})
