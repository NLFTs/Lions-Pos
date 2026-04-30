import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import fs from 'node:fs'
import path from 'node:path'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const outDirPath = path.resolve(__dirname, '../src/main/resources/static')

  /**
   * LOGIKA PEMBERSIHAN MANUAL
   * Hanya berjalan saat mode production (npm run build)
   */
  if (mode === 'production' && fs.existsSync(outDirPath)) {
    const files = fs.readdirSync(outDirPath)
    files.forEach(file => {
      // Tentukan pengecualian di sini. Folder 'scalar-ui' tidak akan dihapus.
      if (file !== 'scalar-ui') {
        const fullPath = path.join(outDirPath, file)
        fs.rmSync(fullPath, { recursive: true, force: true })
      }
    })
    console.log('✅ Cleaned static folder (preserved: scalar-ui)')
  }

  return {
    base: mode === 'production' ? '/_/' : '/',
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    build: {
      outDir: '../src/main/resources/static',
      // PENTING: Set ke false agar Vite tidak menghapus scalar-ui yang sudah kita amankan di atas
      emptyOutDir: false, 
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