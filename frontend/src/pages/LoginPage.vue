<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

import Input from '@/components/ui/Input.vue'
import Button from '@/components/ui/Button.vue'
import Label from '@/components/ui/Label.vue'
import Alert from '@/components/ui/Alert.vue'

import {
  Zap,
  User,
  Lock,
  Eye,
  EyeOff,
  LogIn,
  Loader2
} from 'lucide-vue-next'

const auth = useAuthStore()
const router = useRouter()

const form = ref({
  username: '',
  password: ''
})

const loading = ref(false)
const error = ref('')
const showPassword = ref(false)

/* =========================
   VALIDATION
========================= */

const usernameError = computed(() => {
  const username = form.value.username.trim()

  if (!username) {
    return 'Username wajib diisi'
  }

  if (username.length < 3) {
    return 'Username minimal 3 karakter'
  }

  if (username.length > 20) {
    return 'Username maksimal 20 karakter'
  }

  if (!/^[a-zA-Z0-9_]+$/.test(username)) {
    return 'Username hanya boleh huruf, angka, dan underscore'
  }

  return ''
})

const passwordError = computed(() => {
  const password = form.value.password

  if (!password.trim()) {
    return 'Password wajib diisi'
  }

  if (password.length < 6) {
    return 'Password minimal 6 karakter'
  }

  return ''
})

const isFormValid = computed(() => {
  return !usernameError.value && !passwordError.value
})

/* =========================
   LOGIN
========================= */

async function handleLogin() {
  if (loading.value) return

  error.value = ''

  if (!isFormValid.value) {
    return
  }

  loading.value = true

  try {
    await auth.login(
      form.value.username.trim(),
      form.value.password
    )

    router.push('/dashboard')

  } catch (err) {

    if (!err.response) {
      error.value = 'Tidak dapat terhubung ke server'
    } else {
      error.value =
        err.response?.data?.message ||
        'Login gagal. Periksa kembali kredensial Anda.'
    }

  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen bg-white dark:bg-zinc-950 transition-colors duration-300">

    <!-- LEFT -->
    <div class="flex w-full flex-col justify-between px-8 py-10 sm:px-12 md:w-1/2 lg:px-16 xl:px-24">

      <!-- Logo -->
        <a href="/" class="flex items-center gap-2.5 group cursor-pointer hover:opacity-80 transition-opacity">
          <div
            class="flex h-9 w-9 items-center justify-center rounded-xl  shadow-md shadow-primary/30 transition-transform group-hover:scale-105"
          >
            <img 
            src="/logo-gaptek.svg" 
            alt="Logo Gaptek" 
            class="h-8 w-8 object-contain"
          />
          </div>

          <span class="text-base font-bold tracking-tight text-zinc-900 dark:text-zinc-100">
            GapTek
          </span>
        </a>

      <!-- FORM -->
      <div class="mx-auto w-full max-w-sm space-y-7">

        <!-- Heading -->
        <div class="space-y-1">
          <h1
            class="text-2xl font-extrabold tracking-tight text-zinc-900 dark:text-zinc-100"
          >
            Masuk ke Akun Anda
          </h1>

          <p class="text-sm text-zinc-500 dark:text-zinc-400">
            Silakan masukkan username dan password Anda.
          </p>
        </div>

        <!-- Global Error -->
        <Alert
          v-if="error"
          variant="destructive"
          class="animate-in zoom-in-95 duration-200 py-2.5"
        >
          <p class="text-xs font-medium">
            {{ error }}
          </p>
        </Alert>

        <!-- FORM -->
        <form
          class="space-y-4"
          @submit.prevent="handleLogin"
        >

          <!-- USERNAME -->
          <div class="space-y-1.5">

            <Label
              for="username"
              class="text-sm font-semibold text-zinc-700 dark:text-zinc-300"
            >
              Username
            </Label>

            <div class="group relative">

              <div
                class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5"
              >
                <User
                  class="h-4 w-4 text-zinc-400 transition-colors group-focus-within:text-primary"
                />
              </div>

              <Input
                id="username"
                v-model="form.username"
                type="text"
                autocomplete="username"
                placeholder="masukan username"
                :disabled="loading"
                required
                class="h-11 pl-10 border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900 focus:bg-white dark:focus:bg-zinc-950 text-zinc-900 dark:text-zinc-100 text-sm shadow-sm transition-all focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary"
              />
            </div>

            <!-- Username Error -->
            <p
              v-if="usernameError"
              class="text-xs font-medium text-red-500"
            >
              {{ usernameError }}
            </p>

          </div>

          <!-- PASSWORD -->
          <div class="space-y-1.5">

            <Label
              for="password"
              class="text-sm font-semibold text-zinc-700 dark:text-zinc-300"
            >
              Password
            </Label>

            <div class="group relative">

              <div
                class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5"
              >
                <Lock
                  class="h-4 w-4 text-zinc-400 transition-colors group-focus-within:text-primary"
                />
              </div>

              <Input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                placeholder="Masukkan password"
                :disabled="loading"
                required
                class="h-11 pl-10 pr-10 border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900 focus:bg-white dark:focus:bg-zinc-950 text-zinc-900 dark:text-zinc-100 text-sm shadow-sm transition-all focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary"
              />

              <!-- Toggle Password -->
              <button
                type="button"
                :aria-label="showPassword ? 'Sembunyikan password' : 'Tampilkan password'"
                class="absolute inset-y-0 right-0 flex items-center pr-3.5 text-zinc-400 hover:text-zinc-600 dark:hover:text-zinc-300 transition-colors"
                @click="showPassword = !showPassword"
              >
                <Eye
                  v-if="!showPassword"
                  class="h-4 w-4"
                />

                <EyeOff
                  v-else
                  class="h-4 w-4"
                />
              </button>

            </div>

            <!-- Password Error -->
            <p
              v-if="passwordError"
              class="text-xs font-medium text-red-500"
            >
              {{ passwordError }}
            </p>

          </div>

          <!-- SUBMIT -->
          <Button
            type="submit"
            :disabled="loading || !isFormValid"
            class="mt-2 w-full h-11 text-sm font-bold shadow-md hover:shadow-lg active:scale-[0.98] transition-all duration-200 disabled:cursor-not-allowed disabled:opacity-60"
          >

            <Loader2
              v-if="loading"
              class="mr-2 h-4 w-4 animate-spin"
            />

            <LogIn
              v-else
              class="mr-2 h-4 w-4"
            />

            <span>
              {{ loading ? 'Memproses...' : 'Masuk' }}
            </span>

          </Button>

        </form>

        <!-- HELP -->
        <p class="text-center text-xs text-zinc-500 dark:text-zinc-400">
          Butuh bantuan?

          <a
            href="#"
            class="font-semibold text-primary hover:underline underline-offset-2 decoration-2 transition-all"
          >
            Hubungi IT Support
          </a>
        </p>

      </div>

      <!-- FOOTER -->
      <p class="text-xs text-zinc-400 dark:text-zinc-600">
        &copy; {{ new Date().getFullYear() }} GapTek.
        Hak cipta dilindungi.
      </p>

    </div>

    <!-- RIGHT IMAGE -->
    <div
      class="hidden md:block md:w-1/2 relative overflow-hidden bg-zinc-100 dark:bg-zinc-900"
    >

      <img
        src="/login-image.jpg"
        alt="Login visual"
        class="absolute inset-0 h-full w-full object-cover object-left-top"
      />

      <!-- Overlay -->
      <div class="absolute inset-0 bg-black/10 dark:bg-black/30" />

    </div>

  </div>
</template>