<template>
   <div class="w-full flex flex-col">
    <!-- Banner Iklan -->
    <div 
      v-if="showBanner" 
      class="w-full bg-gradient-to-r from-[#232b38] via-[#3a9681] to-[#8fcea4] px-4 py-2.5 flex items-center justify-center relative z-50"
    >
      <div class="flex flex-col md:flex-row items-center gap-2 md:gap-4 pr-8">
        <p class="text-xs md:text-sm font-medium text-white text-center">
          Butuh Mengelola Retail dengan cepat? dapatkan penawaran khusus 50%.
        </p>
        <a href="#" class="bg-black text-white text-xs md:text-sm px-4 py-1 rounded hover:bg-zinc-800 transition-colors whitespace-nowrap">
          Apply now >
        </a>
      </div>
      <button 
        @click="showBanner = false" 
        class="absolute right-4 p-1 rounded-full bg-white/20 hover:bg-white/30 text-white transition-colors flex items-center justify-center"
        aria-label="Close banner"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>

    <!-- Navbar -->
    <nav class="sticky top-0 z-40 w-full border-b border-white bg-black text-white h-16 flex items-center px-6">
      <div class="w-full flex items-center justify-between">
        
        <div class="flex items-center gap-6">
          <a href="#" class="flex items-center gap-2 cursor-pointer mr-4" @click.prevent="$emit('navigate', '/')">
            <img 
              src="/logo-gaptek.svg" 
              alt="Logo Gaptek" 
              class="h-8 w-8 object-contain"
            />
            <span class="text-lg font-bold tracking-tight">Gaptek</span>
          </a>

          <div class="hidden md:flex items-center gap-1">
            <div 
              v-for="item in navItems" 
              :key="item.name" 
              class="relative"
              @mouseenter="activeDropdown = item.name"
              @mouseleave="activeDropdown = null"
            >
              <!-- Tombol menu dengan efek kotak tegas -->
              <a 
                :href="item.hasDropdown ? '#' : item.url || '#'"
                class="relative px-3 py-1.5 text-sm font-medium transition-all duration-300 flex items-center gap-1 overflow-hidden group"
              >
                <!-- Background Layer stabilo kotak tegas -->
                <span class="absolute inset-0 bg-indigo-600 scale-x-0 group-hover:scale-x-100 transition-transform duration-500 origin-left z-0"></span>
                
                <span class="relative z-10 flex items-center gap-1">
                  {{ item.name }}
                  <ChevronDown v-if="item.hasDropdown" :class="['w-3 h-3 transition-transform duration-200', activeDropdown === item.name ? 'rotate-180' : '']" />
                </span>
              </a>

              <div 
                v-if="item.hasDropdown && activeDropdown === item.name"
                class="absolute top-[32px] left-0 w-full h-[20px] z-40"
              ></div>

              <div 
                v-if="item.hasDropdown && activeDropdown === item.name"
                class="absolute top-[52px] left-0 w-[600px] bg-black border border-zinc-800 p-6 shadow-2xl grid grid-cols-3 gap-8 animate-in fade-in slide-in-from-top-2 duration-200 z-50"
              >
                <div v-for="(col, index) in item.menu" :key="index">
                  <h4 class="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-4">{{ col.title }}</h4>
                  <div class="flex flex-col gap-4">
                    <a v-for="link in col.links" :key="link.name" :href="link.url" class="group/link flex items-start gap-3 hover:opacity-80 transition-opacity">
                      <component :is="link.icon" class="w-5 h-5 text-zinc-400 group-hover/link:text-white" />
                      <div>
                        <div class="text-sm font-medium text-white">{{ link.name }}</div>
                        <div class="text-xs text-zinc-400">{{ link.desc }}</div>
                      </div>
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="flex items-center">
          <a 
            href="#"
            @click.prevent="$emit('navigate', '/login')"
            class="text-sm font-medium text-zinc-400 hover:text-white transition-colors px-3 py-1.5"
          >
            Log In
          </a>
        </div>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ChevronDown, Warehouse, Code, Receipt, MapPinHouse, Users, Ticket, ChartBarBig, History, ArrowLeftRight  } from 'lucide-vue-next'

const activeDropdown = ref(null)
const bannerText = ref('Switching clouds? Get up to $10K in credits + hands-on help.')

//contol banner iklan
const showBanner = ref(true);

const navItems = ref([
  { 
    name: 'Solutions', 
    url: '',
    hasDropdown: true,
    menu: [
      { 
        title: 'Fitur', 
        links: [
          { name: 'Retail', desc: 'Menejemen Kasir dan Inventaris', icon: Receipt, url: '/docs/retail' },
          { name: 'Gudang', desc: 'Kelola gudang di berbagai lokasi', icon: Warehouse, url: '/docs/warehouse' },
          { name: 'Cabang', desc: 'Kelola lebih banyak cabang', icon: MapPinHouse, url: '/docs/branches' },
          { name: 'Voucher', desc: 'Kelola voucher dan promosi', icon: Ticket, url: '/docs/vouchers' },
        ]
      },
      { 
        title: 'Tools', 
        links: [
          { name: 'Laporan', desc: 'Buat laporan bisnis yang lengkap', icon: ChartBarBig, url: '/docs/reports' },
          { name: 'Riwayat Transaksi', desc: 'Pantau semua transaksi di berbagai lokasi', icon: History, url: '/docs/transaction-history' },
          { name: 'Riwayat Mutasi Stok', desc: 'Pantau semua mutasi stok di berbagai lokasi', icon: ArrowLeftRight, url: '/docs/inventory-changes' },
        ]
      },
      { 
        title: 'Users', 
        links: [
          { name: 'Karyawan', desc: 'Kelola banyak pengguna dalam 1', icon: Users, url: '/docs/users' }
        ]
      }
    ]
  },
  { name: 'Tentang', url: '/about' },
  { name: 'Plan', url: '/pricing' }
])
</script>