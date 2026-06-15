<template>
  <nav class="sticky top-0 z-50 w-full border-b border-zinc-800 bg-black text-white h-16 flex items-center px-6">
    <div class="max-w-7xl w-full mx-auto flex items-center justify-between">
      
      <div class="flex items-center gap-6">
        <a href="#" class="flex items-center gap-2 cursor-pointer mr-4" @click.prevent="$emit('navigate', '/')">
          <img 
            src="/logo-G-zinc.svg" 
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
            <a 
              :href="item.hasDropdown ? '#' : item.url || '#'"
              :class="[
                'px-3 py-1.5 text-sm transition-all duration-200 flex items-center gap-1 rounded-full',
                activeDropdown === item.name ? 'bg-zinc-800' : 'text-zinc-400 hover:text-white'
              ]"
            >
              {{ item.name }}
              <ChevronDown v-if="item.hasDropdown" :class="['w-3 h-3 transition-transform duration-200', activeDropdown === item.name ? 'rotate-180' : '']" />
            </a>

            <div 
              v-if="item.hasDropdown && activeDropdown === item.name"
              class="absolute top-[32px] left-0 w-full h-[20px] z-40"
            ></div>

            <div 
              v-if="item.hasDropdown && activeDropdown === item.name"
              class="absolute top-[52px] left-0 w-[600px] bg-black border border-zinc-800 rounded-xl p-6 shadow-2xl grid grid-cols-3 gap-8 animate-in fade-in slide-in-from-top-2 duration-200 z-50"
            >
              <div v-for="(col, index) in item.menu" :key="index">
                <h4 class="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-4">{{ col.title }}</h4>
                <div class="flex flex-col gap-4">
                  <a v-for="link in col.links" :key="link.name" href="#" class="group flex items-start gap-3 hover:opacity-80 transition-opacity">
                    <component :is="link.icon" class="w-5 h-5 text-zinc-400 group-hover:text-white" />
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
</template>

<script setup>
import { ref } from 'vue'
import { ChevronDown, Warehouse, Code, Receipt, MapPinHouse, Users, Ticket, ChartBarBig, History, ArrowLeftRight  } from 'lucide-vue-next'

const activeDropdown = ref(null)

const navItems = ref([
  { 
    name: 'Solutions', 
    hasDropdown: true,
    menu: [
      { 
        title: 'Fitur', 
        links: [
          { name: 'Retail', desc: 'Menejemen Kasir dan Inventaris', icon: Receipt},
          { name: 'Gudang', desc: 'Kelola gudang di berbagai lokasi', icon: Warehouse },
          { name: 'Cabang', desc: 'Kelola lebih banyak cabang', icon: MapPinHouse },
          { name: 'Voucher', desc: 'Kelola voucher dan promosi', icon: Ticket },
        ]
      },
      { 
        title: 'Tools', 
        links: [
          { name: 'Laporan', desc: 'Buat laporan bisnis yang lengkap', icon: ChartBarBig },
          { name: 'Riwayat Transaksi', desc: 'Pantau semua transaksi di berbagai lokasi', icon: History },
          { name: 'Riwayat Mutasi Stok', desc: 'Pantau semua mutasi stok di berbagai lokasi', icon: ArrowLeftRight },
        ]
      },
      { 
        title: 'Users', 
        links: [
          { name: 'Karyawan', desc: 'Kelola banyak pengguna dalam 1', icon: Users }
        ]
      }
    ]
  },
  { name: 'Enterprise', url: '#' },
  { name: 'Pricing', url: '#' }
])
</script>