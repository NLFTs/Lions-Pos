<script setup>
import { useRouter } from 'vue-router'
import LandingLayout from '@/components/section/landingpage/LandingLayout.vue'
import Navbar from '@/components/section/landingpage/Navbar.vue'
import Hero from '@/components/section/landingpage/Hero.vue'
import Button from '@/components/ui/Button.vue'
import { Zap } from 'lucide-vue-next'

const router = useRouter()

const brandName = 'gaptek'
const navigationItems = [
  { name: 'Tentang Gaptek', path: '/about' }
]

const handleNavigation = (path) => {
  router.push(path)
}

const navigateToLogin = () => router.push('/login')
</script>

<template>
  <LandingLayout>
    <!-- Navbar Section -->
    <Navbar 
      :brand-name="brandName" 
      :nav-items="navigationItems"
      @navigate="handleNavigation"
    >
      <template #logo>
        <div class="h-full w-full bg-zinc-100 flex items-center justify-center">
          <Zap class="h-4 w-4 text-zinc-900" />
        </div>
      </template>

      <template #actions>
        <Button class="font-bold px-3 sm:px-6 py-1.5 sm:py-2 text-xs sm:text-base rounded-xl bg-zinc-100 text-zinc-900 hover:bg-white transition-all active:scale-95" @click="navigateToLogin">
          Masuk
        </Button>
      </template>
    </Navbar>

    <!-- Hero Section -->
    <Hero />
  </LandingLayout>
</template>

<style scoped>
</style>
