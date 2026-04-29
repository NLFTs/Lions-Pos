<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import Card from '@/components/ui/Card.vue'
import Input from '@/components/ui/Input.vue'
import Button from '@/components/ui/Button.vue'
import Label from '@/components/ui/Label.vue'
import Alert from '@/components/ui/Alert.vue'
import { Zap, User, Lock, Eye, EyeOff, LogIn, Loader2, CheckCircle2 } from 'lucide-vue-next'

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

const features = [
  'Enterprise Ready Framework',
  'Real-time Data Monitoring',
  'Advanced Access Control',
  'Modular System Architecture'
]
</script>

<template>
  <div class="flex min-h-screen bg-white selection:bg-primary/20 selection:text-primary">
    <!-- Left side: Login Form -->
    <div class="flex flex-1 flex-col justify-center px-6 py-12 lg:flex-none lg:px-20 xl:px-24">
      <div class="mx-auto w-full max-w-sm lg:w-96 animate-in fade-in slide-in-from-left duration-700">
        <!-- Branding Header -->
        <div class="flex items-center gap-3 mb-12">
          <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-primary shadow-lg shadow-primary/30">
            <Zap class="h-6 w-6 text-white" />
          </div>
          <span class="text-2xl font-bold tracking-tight text-zinc-900 border-none">Spravel</span>
        </div>

        <div class="mb-10">
          <h1 class="text-3xl font-extrabold tracking-tight text-zinc-950">Selamat Datang Kembali</h1>
          <p class="mt-3 text-sm font-medium text-zinc-500">
            Silakan masukkan kredensial untuk mengakses akun Anda
          </p>
        </div>

        <Alert v-if="error" variant="destructive" class="mb-6 animate-in zoom-in-95 duration-200">
          <p class="text-sm">{{ error }}</p>
        </Alert>

        <form @submit.prevent="handleLogin" class="space-y-6">
          <div class="space-y-2">
            <Label for="username" class="text-sm font-semibold text-zinc-700">Username</Label>
            <div class="group relative">
              <div class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none">
                <User class="h-4 w-4 text-zinc-400 group-focus-within:text-primary transition-colors" />
              </div>
              <Input
                id="username"
                v-model="form.username"
                placeholder="Masukkan username"
                class="h-11 pl-10 border-zinc-200 bg-zinc-50/30 focus:bg-white transition-all ring-offset-0 focus-visible:ring-1 focus-visible:ring-primary shadow-sm"
                :disabled="loading"
                required
              />
            </div>
          </div>

          <div class="space-y-2">
            <div class="flex items-center justify-between">
              <Label for="password" class="text-sm font-semibold text-zinc-700">Password</Label>
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
                class="h-11 pl-10 pr-10 border-zinc-200 bg-zinc-50/30 focus:bg-white transition-all ring-offset-0 focus-visible:ring-1 focus-visible:ring-primary shadow-sm"
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

          <div class="pt-2">
            <Button
              type="submit"
              class="w-full h-11 text-base font-bold shadow-none active:scale-[0.98] transition-all bg-primary hover:bg-primary/90"
              :disabled="loading"
            >
              <Loader2 v-if="loading" class="mr-2 h-4 w-4 animate-spin" />
              <LogIn v-else class="mr-2 h-4 w-4" />
              <span>Masuk</span>
            </Button>
          </div>
        </form>

        <p class="mt-10 text-center text-sm font-medium text-zinc-500">
          Butuh bantuan? <a href="#" class="text-primary hover:underline font-bold transition-all decoration-2 underline-offset-2">Hubungi IT Support</a>
        </p>
      </div>
    </div>

    <!-- Right side: Content/Visual -->
    <div class="relative hidden flex-1 lg:block overflow-hidden group">
      <!-- Gradient Background -->
      <div class="absolute inset-0 bg-primary">
         <!-- Abstract Pattern Overlay -->
         <div class="absolute inset-0 opacity-[0.08]" style="background-image: radial-gradient(#fff 1px, transparent 1px); background-size: 30px 30px;"></div>
         <div class="absolute inset-0 bg-gradient-to-br from-primary via-primary/95 to-primary/80"></div>
         <!-- Subtle Animated Light Effect -->
         <div class="absolute top-1/4 left-1/4 w-[600px] h-[600px] bg-white/10 rounded-full blur-[120px] animate-pulse"></div>
      </div>

      <div class="relative flex h-full flex-col items-center justify-center p-16 text-center text-white">
        <div class="max-w-md w-full animate-in fade-in slide-in-from-right duration-700">
          <div class="mb-10 inline-flex items-center justify-center w-20 h-20 rounded-3xl bg-white/10 border border-white/20 shadow-2xl backdrop-blur-xl">
            <Zap class="h-10 w-10 text-white" />
          </div>

          <h2 class="text-4xl font-extrabold tracking-tight leading-[1.1] mb-6">
            Sistem Manajemen <br/> Kredit Digital
          </h2>
          <p class="text-lg font-medium text-primary-foreground/80 mb-12">
            Platform terintegrasi untuk mengelola seluruh proses bisnis Anda dengan efisien, aman, dan transparan.
          </p>

          <!-- Value Props -->
          <div class="grid gap-4 mx-auto w-max text-left">
            <div v-for="item in features" :key="item" class="flex items-center gap-3.5 group/item">
              <div class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-white/20 border border-white/30 backdrop-blur-sm group-hover/item:bg-white/30 transition-colors">
                <CheckCircle2 class="h-4 w-4 text-white" />
              </div>
              <span class="text-base font-semibold tracking-wide">{{ item }}</span>
            </div>
          </div>
        </div>
        
        <!-- Bottom branding/info -->
        <div class="absolute bottom-10 text-xs font-semibold tracking-widest text-white/40 uppercase">
          © 2026 Spravel Enterprise Edition
        </div>
      </div>
    </div>
  </div>
</template>
