<script setup>
import { useRouter } from 'vue-router'
import LandingLayout from '@/components/section/landingpage/LandingLayout.vue'
import Navbar from '@/components/section/landingpage/Navbar.vue'
import Features from '@/components/section/landingpage/Features.vue'
import Footer from '@/components/section/landingpage/Footer.vue'
import Button from '@/components/ui/Button.vue'
import { Zap } from 'lucide-vue-next'

const router = useRouter()

const navigationItems = [
  { name: 'Fitur', path: '/features' },
  { name: 'Harga', path: '/pricing' },
  { name: 'Tentang Gaptek', path: '/about' }
]

const handleNavigation = (path) => {
  router.push(path)
}
</script>

<template>
  <LandingLayout>
    <Navbar 
      brand-name="gaptek" 
      :nav-items="navigationItems"
      @navigate="handleNavigation"
    >
      <template #logo>
        <div class="h-full w-full bg-zinc-100 flex items-center justify-center">
          <Zap class="h-4 w-4 text-zinc-900" />
        </div>
      </template>
      <template #actions>
        <Button class="font-bold px-6 py-2 rounded-xl bg-zinc-100 text-zinc-900 hover:bg-white transition-all active:scale-95" @click="router.push('/login')">
          Masuk
        </Button>
      </template>
    </Navbar>

    <main class="relative flex-grow pt-12 pb-24 overflow-hidden">
      <Features />
    </main>

    <Footer />
  </LandingLayout>
</template>
