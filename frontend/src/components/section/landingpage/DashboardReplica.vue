<script setup>
import { 
  Users, 
  Package, 
  Boxes, 
  ShieldCheck, 
  KeyRound,
  Zap,
  Activity, 
  UserPlus,
  Search,
  Plus,
  LayoutGrid,
  List,
  Eye,
  Clock,
  ArrowRight
} from 'lucide-vue-next'

const stats = [
  { label: 'Total User', value: '12', icon: Users, color: 'text-neutral-600', bg: 'bg-neutral-100 dark:bg-neutral-800/30', trend: '+3' },
  { label: 'Total Produk', value: '145', icon: Package, color: 'text-emerald-600', bg: 'bg-emerald-50 dark:bg-emerald-950/30', trend: '120 aktif' },
  { label: 'Kategori', value: '8', icon: Boxes, color: 'text-violet-600', bg: 'bg-violet-50 dark:bg-violet-950/30' },
  { label: 'Role', value: '4', icon: ShieldCheck, color: 'text-amber-600', bg: 'bg-amber-50 dark:bg-amber-950/30' }
]

const recentProducts = [
  { name: 'Espresso Arabica 250g', status: 'Aktif', time: '2 jam lalu' },
  { name: 'Milk Frother Pro', status: 'Aktif', time: '5 jam lalu' },
  { name: 'Paper Filter V60', status: 'Nonaktif', time: '1 hari lalu' }
]

const activities = [
  { title: 'User baru: Sarah Wilson', desc: '@sarah bergabung', time: 'Baru saja', initial: 'S', color: 'bg-neutral-600' },
  { title: 'Produk: Roasted Beans', desc: 'Status: Aktif', time: '10 menit lalu', icon: Package, color: 'bg-emerald-500' }
]
</script>

<template>
  <div class="w-full h-full bg-white dark:bg-[#09090b] text-foreground flex flex-col overflow-hidden font-sans">
    
    <!-- Navbar Mock -->
    <div class="h-16 border-b border-border bg-white/50 dark:bg-card/50 backdrop-blur-md px-8 flex items-center justify-between shrink-0">
      <div class="flex items-center gap-4">
        <div class="w-8 h-8 rounded-lg bg-zinc-900 dark:bg-white flex items-center justify-center">
          <Zap class="w-5 h-5 text-white dark:text-zinc-900" />
        </div>
        <div class="h-4 w-px bg-border mx-2"></div>
        <span class="text-sm font-semibold text-muted-foreground">Dashboard</span>
      </div>
      <div class="flex items-center gap-4">
        <div class="w-8 h-8 rounded-full bg-zinc-200 dark:bg-zinc-800 border border-border"></div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex-1 overflow-y-auto p-8 space-y-8 no-scrollbar">
      
      <!-- Welcome Section -->
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight text-foreground">Selamat datang, Admin! 👋</h1>
          <p class="text-sm text-muted-foreground mt-1">Berikut ringkasan data dan aktivitas aplikasi hari ini.</p>
        </div>
        <div class="flex items-center gap-4">
          <span class="text-xs text-muted-foreground"><Clock class="w-3 h-3 inline mr-1" /> 14:20</span>
          <div class="px-3 py-1.5 bg-zinc-900 dark:bg-white text-white dark:text-zinc-900 rounded-lg text-xs font-medium flex items-center gap-2">
            <Eye class="w-3.5 h-3.5" /> Refresh
          </div>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="flex items-center gap-2">
        <div class="relative flex-1">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <input type="text" placeholder="Cari statistik..." class="w-full bg-card border border-border rounded-lg pl-10 pr-4 py-2 text-sm" readonly />
        </div>
        <div class="flex gap-1 p-1 bg-card border border-border rounded-lg">
          <div class="p-1.5 bg-zinc-900 dark:bg-white text-white dark:text-zinc-900 rounded-md"><LayoutGrid class="w-4 h-4" /></div>
          <div class="p-1.5 text-muted-foreground"><List class="w-4 h-4" /></div>
        </div>
        <div class="px-4 py-2 bg-zinc-900 dark:bg-white text-white dark:text-zinc-900 rounded-lg text-sm font-medium flex items-center gap-2">
          <Plus class="w-4 h-4" /> Buat Baru
        </div>
      </div>

      <!-- Content Grid -->
      <div class="grid grid-cols-3 gap-8">
        
        <!-- Left: Recent Cards -->
        <div class="col-span-1 space-y-6">
          <div class="bg-card border border-border rounded-xl overflow-hidden shadow-sm">
            <div class="px-5 py-3.5 border-b border-border flex items-center justify-between">
              <div class="flex items-center gap-2">
                <Package class="w-4 h-4 text-muted-foreground" />
                <h3 class="text-sm font-semibold">Produk Terbaru</h3>
              </div>
              <span class="text-xs text-zinc-600 dark:text-zinc-400 font-medium">Lihat Semua</span>
            </div>
            <div class="p-4 space-y-3">
              <div v-for="p in recentProducts" :key="p.name" class="flex items-center justify-between p-3 rounded-lg border border-border/40 bg-muted/20">
                <div class="min-w-0 flex-1">
                  <p class="text-sm font-medium truncate">{{ p.name }}</p>
                  <div class="flex items-center gap-2 mt-1">
                    <span class="px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400 text-[10px] font-bold">{{ p.status }}</span>
                    <span class="text-[10px] text-muted-foreground">{{ p.time }}</span>
                  </div>
                </div>
                <ArrowRight class="w-4 h-4 text-muted-foreground" />
              </div>
            </div>
          </div>

          <div class="bg-card border border-border rounded-xl overflow-hidden shadow-sm">
            <div class="px-5 py-3.5 border-b border-border flex items-center gap-2">
              <Activity class="w-4 h-4 text-muted-foreground" />
              <h3 class="text-sm font-semibold">Aktivitas Terbaru</h3>
            </div>
            <div class="p-5 space-y-6">
              <div v-for="act in activities" :key="act.title" class="flex gap-4">
                <div class="relative">
                  <div class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold border border-border" :class="act.color + ' text-white'">
                    {{ act.initial || '' }}
                    <component v-if="act.icon" :is="act.icon" class="w-4 h-4" />
                  </div>
                  <div class="absolute top-8 left-4 w-px h-6 bg-border last:hidden"></div>
                </div>
                <div class="flex-1">
                  <p class="text-sm font-medium">{{ act.title }}</p>
                  <p class="text-xs text-muted-foreground">{{ act.desc }}</p>
                  <p class="text-[10px] text-muted-foreground mt-1 uppercase tracking-wider">{{ act.time }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right: Stats Grid -->
        <div class="col-span-2 space-y-6">
          <h2 class="text-lg font-semibold">Statistik & Data</h2>
          <div class="grid grid-cols-2 gap-4">
            <div v-for="s in stats" :key="s.label" class="p-5 bg-card border border-border rounded-xl shadow-sm relative overflow-hidden group">
              <div class="absolute -top-6 -right-6 w-20 h-20 rounded-full opacity-5" :class="s.bg"></div>
              <div class="flex justify-between items-start">
                <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="s.bg + ' ' + s.color">
                  <component :is="s.icon" class="w-5 h-5" />
                </div>
                <span v-if="s.trend" class="px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400 text-[10px] font-bold">{{ s.trend }}</span>
              </div>
              <div class="mt-4">
                <p class="text-2xl font-bold tracking-tight">{{ s.value }}</p>
                <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mt-1">{{ s.label }}</p>
              </div>
            </div>
          </div>
          
          <!-- Mock Chart -->
          <div class="p-6 bg-card border border-border rounded-xl shadow-sm h-48 flex items-end gap-2">
             <div v-for="i in 24" :key="i" class="flex-1 bg-neutral-500/10 rounded-t-sm hover:bg-neutral-500/30 transition-all cursor-pointer" :style="{ height: Math.random() * 80 + 20 + '%' }"></div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>


<style scoped>
/* Performance optimized terminal look */
</style>
