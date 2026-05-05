<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import Card from '@/components/ui/Card.vue'
import Input from '@/components/ui/Input.vue'
import Button from '@/components/ui/Button.vue'
import Label from '@/components/ui/Label.vue'
import Alert from '@/components/ui/Alert.vue'
import { Zap, User, Lock, Eye, EyeOff, LogIn, Loader2 } from 'lucide-vue-next'

const auth = useAuthStore()
const router = useRouter()

const form = ref({ username: '', password: '' })
const loading = ref(false)
const error = ref(null)
const showPassword = ref(false)

async function handleLogin() {
  error.value = null
  loading.value = true
  try {
    await auth.login(form.value.username, form.value.password)
    router.push('/dashboard')
  } catch (err) {
    error.value = err.response?.data?.message || 'Login failed. Check your credentials.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-zinc-50 dark:bg-zinc-950 selection:bg-primary/20 selection:text-primary transition-colors duration-300 p-4 sm:p-6">
    
    <!-- 
      KONTEN KOTAK (SQUARE)
      Menggunakan aspect-square dan rounded-3xl. 
      justify-center dihilangkan dari parent agar tidak memotong layar atas saat overflow.
    -->
    <div class="w-full max-w-[420px] aspect-square bg-white dark:bg-zinc-900 rounded-3xl shadow-2xl border border-zinc-200 dark:border-zinc-800 flex flex-col overflow-y-auto overflow-x-hidden p-6 sm:p-8 animate-in fade-in zoom-in-95 duration-500">
      
      <!-- m-auto akan menengahkan konten dengan aman tanpa clipping -->
      <div class="m-auto w-full flex flex-col space-y-6">
        
        <!-- Branding Header: Dibuat menyamping (horizontal) agar hemat tempat di layar kecil -->
        <div class="flex items-center justify-center gap-4">
          <div class="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-primary shadow-lg shadow-primary/30 transition-colors duration-300">
            <Zap class="h-6 w-6 text-primary-foreground transition-colors duration-300" />
          </div>
          <div class="text-left">
            <h1 class="text-xl sm:text-2xl font-extrabold tracking-tight text-zinc-950 dark:text-zinc-100 transition-colors duration-300">Selamat Datang</h1>
            <p class="text-xs sm:text-sm font-medium text-zinc-500 dark:text-zinc-400 transition-colors duration-300">
              Silakan masuk ke akun Anda
            </p>
          </div>
        </div>

        <!-- Error Alert -->
        <Alert v-if="error" variant="destructive" class="animate-in zoom-in-95 duration-200 py-2.5">
          <p class="text-xs font-medium">{{ error }}</p>
        </Alert>

        <!-- Login Form -->
        <form @submit.prevent="handleLogin" class="space-y-3.5 sm:space-y-4">
          <div class="space-y-1.5">
            <Label for="username" class="text-xs sm:text-sm font-semibold text-zinc-700 dark:text-zinc-300 transition-colors duration-300">Username</Label>
            <div class="group relative">
              <div class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none">
                <User class="h-4 w-4 text-zinc-400 group-focus-within:text-primary transition-colors" />
              </div>
              <Input
                id="username"
                v-model="form.username"
                placeholder="Masukkan username"
                class="h-10 sm:h-11 pl-10 border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-950/50 focus:bg-white dark:focus:bg-zinc-900 text-zinc-900 dark:text-zinc-100 transition-all ring-offset-0 focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary shadow-sm text-sm"
                :disabled="loading"
                required
              />
            </div>
          </div>

          <div class="space-y-1.5">
            <div class="flex items-center justify-between">
              <Label for="password" class="text-xs sm:text-sm font-semibold text-zinc-700 dark:text-zinc-300 transition-colors duration-300">Password</Label>
            </div>
            <div class="group relative">
              <div class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none">
                <Lock class="h-4 w-4 text-zinc-400 group-focus-within:text-primary transition-colors" />
              </div>
              <Input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="Masukkan password"
                class="h-10 sm:h-11 pl-10 pr-10 border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-950/50 focus:bg-white dark:focus:bg-zinc-900 text-zinc-900 dark:text-zinc-100 transition-all ring-offset-0 focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary shadow-sm text-sm"
                :disabled="loading"
                required
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute inset-y-0 right-0 flex items-center pr-3.5 text-zinc-400 hover:text-zinc-600 focus:outline-none transition-colors"
                tabindex="-1"
              >
                <Eye v-if="!showPassword" class="h-4 w-4" />
                <EyeOff v-else class="h-4 w-4" />
              </button>
            </div>
          </div>

          <div class="pt-2 sm:pt-4">
            <Button
              type="submit"
              class="w-full h-10 sm:h-11 text-sm font-bold shadow-md hover:shadow-lg active:scale-[0.98] transition-all duration-200"
              :disabled="loading"
            >
              <Loader2 v-if="loading" class="mr-2 h-4 w-4 animate-spin" />
              <LogIn v-else class="mr-2 h-4 w-4" />
              <span>Masuk</span>
            </Button>
          </div>
        </form>

        <p class="text-center text-xs font-medium text-zinc-500 dark:text-zinc-400 transition-colors duration-300">
          Butuh bantuan? <a href="#" class="text-primary hover:underline font-bold transition-all decoration-2 underline-offset-2">Hubungi IT Support</a>
        </p>
      </div>
      
    </div>
  </div>
</template>