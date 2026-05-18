<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { storeToRefs } from 'pinia'
import AppLayout from '@/components/AppLayout.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import DashboardCard from '@/components/dashboard/Card.vue'
import ActivityTimeline from '@/components/dashboard/ActivityTimeline.vue'
import MobileSummaryTabs from '@/components/dashboard/MobileSummaryTabs.vue'
import AboutModal from '@/components/dashboard/AboutModal.vue'
import Badge from '@/components/ui/badge/Badge.vue' // Added missing import
import { useAuthStore } from '@/stores/auth'
import { usePermission } from '@/composables/usePermission'
import api from '@/lib/api'
import {
  Users,
  Boxes,
  ShieldCheck,
  KeyRound,
  Zap,
  Activity,
  UserPlus,
  Loader2,
  Clock,
  ArrowRight,
  Plus,
  Eye,
  Search,
  LayoutGrid,
  List,
  ChevronDown,
  Package,
  ArrowRightLeft,
  Handshake,
  MapPin,
  Ticket,
  ShoppingBag, // Added missing import
} from 'lucide-vue-next'

const auth = useAuthStore()
const { user } = storeToRefs(auth)
const { can } = usePermission()

const loading = ref(true)
const error = ref(null)
const lastRefresh = ref(null)

// Stats toolbar state
const searchQuery = ref('')
const statsViewMode = ref('grid') // 'grid' | 'list'

// About Modal state
const isAboutModalOpen = ref(false)

// Dropdown "Buat Baru"
const dropdownOpen = ref(false)
const dropdownRef = ref(null)
function handleOutsideClick(e) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    dropdownOpen.value = false
  }
}
onMounted(() => { fetchStats(); document.addEventListener('click', handleOutsideClick) })
onUnmounted(() => document.removeEventListener('click', handleOutsideClick))

// Stats from backend
const stats = ref({
  totalUsers: 0,
  totalProducts: 0,
  totalCategories: 0,
  totalRoles: 0,
  totalPermissions: 0,
  totalModules: 0,
  activeProducts: 0,
  inactiveProducts: 0,
})

const recentUsers = ref([])
const recentProducts = ref([])
const recentActivities = ref([])

async function fetchStats() {
  loading.value = true
  error.value = null
  try {
    const [usersRes, postsRes, categoriesRes, rolesRes, permsRes, modulesRes] = await Promise.allSettled([
      api.get('/api/v1/users'),
      api.get('/api/v1/products'),
      api.get('/api/v1/categories'),
      api.get('/api/v1/roles'),
      api.get('/api/v1/permissions'),
      api.get('/api/v1/modules'),
    ])

    if (usersRes.status === 'fulfilled' && usersRes.value.data?.data) {
      const users = usersRes.value.data.data
      recentUsers.value = Array.isArray(users) ? users.slice(0, 5) : []
      stats.value.totalUsers = Array.isArray(users) ? users.length : 0
    }
    
    if (postsRes.status === 'fulfilled' && postsRes.value.data?.data) {
      const productsData = postsRes.value.data.data
      const productsList = Array.isArray(productsData) ? productsData : (productsData.content || [])
      stats.value.totalProducts = Array.isArray(productsData) ? productsData.length : (productsData.totalElements || 0)
      stats.value.activeProducts = productsList.filter(p => p.isActive).length
      stats.value.inactiveProducts = productsList.filter(p => !p.isActive).length
      recentProducts.value = productsList.slice(0, 5)
    }

    if (categoriesRes.status === 'fulfilled' && categoriesRes.value.data?.data) {
      const cats = categoriesRes.value.data.data
      stats.value.totalCategories = Array.isArray(cats) ? cats.length : 0
    }
    if (rolesRes.status === 'fulfilled' && rolesRes.value.data?.data) {
      const roles = rolesRes.value.data.data
      stats.value.totalRoles = Array.isArray(roles) ? roles.length : 0
    }
    if (permsRes.status === 'fulfilled' && permsRes.value.data?.data) {
      const perms = permsRes.value.data.data
      stats.value.totalPermissions = Array.isArray(perms) ? perms.length : 0
    }
    if (modulesRes.status === 'fulfilled' && modulesRes.value.data?.data) {
      const mods = modulesRes.value.data.data
      stats.value.totalModules = Array.isArray(mods) ? mods.length : 0
    }

    generateRecentActivities()
    lastRefresh.value = new Date()
  } catch (err) {
    console.error('Dashboard Error:', err)
    error.value = 'Gagal memuat beberapa data dashboard.'
  } finally {
    loading.value = false
  }
}

function generateRecentActivities() {
  const activities = []
  
  if (Array.isArray(recentUsers.value)) {
    recentUsers.value.slice(0, 3).forEach(u => {
      if (!u) return
      activities.push({
        title: `User baru: ${u.fullname || u.username || 'Unknown'}`,
        description: `@${u.username || 'anon'} bergabung`,
        icon: UserPlus,
        color: 'bg-blue-50 text-blue-600 dark:bg-blue-950/40 dark:text-blue-400',
        time: formatDate(u.createdAt),
        initials: (u.fullname || u.username || '?').charAt(0).toUpperCase(),
      })
    })
  }

  if (Array.isArray(recentProducts.value)) {
    recentProducts.value.slice(0, 2).forEach(p => {
      if (!p) return
      activities.push({
        title: `Produk: ${p.name || 'Unnamed'}`,
        description: `Status: ${p.isActive ? 'Aktif' : 'Nonaktif'}`,
        icon: Package,
        color: 'bg-emerald-50 text-emerald-600 dark:bg-emerald-950/40 dark:text-emerald-400',
        time: formatDate(p.createdAt),
      })
    })
  }

  recentActivities.value = activities.slice(0, 5)
}

const statCards = computed(() => {
  const cards = []
  if (can('user.index')) {
    cards.push({
      label: 'Total User',
      value: stats.value.totalUsers,
      icon: Users,
      to: '/dashboard/users',
      color: 'text-blue-600',
      bg: 'bg-blue-50 dark:bg-blue-950/30',
      trend: '+',
      description: 'Daftar pengguna sistem',
    })
  }
  if (can('produk.index')) {
    cards.push({
      label: 'Total Produk',
      value: stats.value.totalProducts,
      icon: Package,
      to: '/dashboard/products',
      color: 'text-emerald-600',
      bg: 'bg-emerald-50 dark:bg-emerald-950/30',
      trend: `${stats.value.activeProducts} aktif`,
      description: 'Katalog produk aktif',
    })
  }
  if (can('category.index')) {
    cards.push({
      label: 'Kategori',
      value: stats.value.totalCategories,
      icon: Boxes,
      to: '/dashboard/categories',
      color: 'text-violet-600',
      bg: 'bg-violet-50 dark:bg-violet-950/30',
    })
  }
  if (can('role.index')) {
    cards.push({
      label: 'Role',
      value: stats.value.totalRoles,
      icon: ShieldCheck,
      to: '/dashboard/roles',
      color: 'text-amber-600',
      bg: 'bg-amber-50 dark:bg-amber-950/30',
    })
  }
  return cards
})

const createMenuItems = computed(() => {
  const items = []
  if (can('produk.store'))          items.push({ label: 'Produk',          icon: Package,        to: '/dashboard/products' })
  if (can('order.store') || true)   items.push({ label: 'Transaksi POS',   icon: ShoppingBag,    to: '/dashboard/kasir' })
  if (can('stock-mutation.index'))  items.push({ label: 'Mutasi Stok',      icon: ArrowRightLeft, to: '/dashboard/stock-mutations' })
  return items
})

const filteredStatCards = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return statCards.value
  return statCards.value.filter(c => c.label.toLowerCase().includes(q))
})

function formatDate(dt) {
  if (!dt) return '-'
  try {
    const date = new Date(dt)
    const now = new Date()
    const diff = now - date
    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)

    if (minutes < 1) return 'Baru saja'
    if (minutes < 60) return `${minutes} menit lalu`
    if (hours < 24) return `${hours} jam lalu`
    if (days < 7) return `${days} hari lalu`
    return date.toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' })
  } catch {
    return '-'
  }
}

function formatTime(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <AppLayout>
    <div class="space-y-6 min-h-[50vh]">
      <!-- Welcome Section -->
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Selamat datang, {{ user?.fullname || user?.username || 'User' }}! 👋</h1>
          <p class="text-sm text-muted-foreground mt-1">
            Ringkasan aktivitas operasional Gaptek POS hari ini.
          </p>
        </div>
        <div class="flex items-center gap-2">
          <span class="text-xs text-muted-foreground hidden sm:inline">
            <Clock class="w-3 h-3 inline mr-1" />
            {{ lastRefresh ? formatTime(lastRefresh) : '-' }}
          </span>
          <button @click="fetchStats" :disabled="loading" class="h-9 px-4 rounded-xl bg-primary text-primary-foreground text-xs font-bold hover:opacity-90 disabled:opacity-50 transition-all flex items-center gap-2 shadow-lg shadow-primary/20">
            <Loader2 v-if="loading" class="w-3 h-3 animate-spin" />
            <Eye v-else class="w-3 h-3" />
            Refresh
          </button>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="flex items-center gap-3">
        <div class="relative flex-1 group">
          <Search class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground group-focus-within:text-primary transition-colors" />
          <input v-model="searchQuery" type="text" placeholder="Cari statistik..." class="w-full h-11 rounded-2xl bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 pl-11 pr-4 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 transition-all shadow-sm" />
        </div>

        <div class="relative shrink-0" ref="dropdownRef">
          <button @click.stop="dropdownOpen = !dropdownOpen" class="h-11 px-5 rounded-2xl bg-zinc-900 dark:bg-white text-white dark:text-zinc-900 text-sm font-bold flex items-center gap-2 shadow-xl hover:scale-[1.02] active:scale-[0.98] transition-all">
            <Plus class="w-4 h-4" />
            <span class="hidden sm:inline">Buat Baru</span>
            <ChevronDown class="w-3.5 h-3.5 transition-transform" :class="{ 'rotate-180': dropdownOpen }" />
          </button>
          <div v-if="dropdownOpen" class="absolute right-0 top-full mt-2 w-48 rounded-2xl border border-zinc-100 dark:border-zinc-800 bg-white dark:bg-zinc-900 shadow-2xl z-50 overflow-hidden py-1.5 animate-in fade-in slide-in-from-top-2">
            <RouterLink v-for="item in createMenuItems" :key="item.label" :to="item.to" @click="dropdownOpen = false" class="flex items-center gap-3 px-4 py-2.5 text-sm font-medium hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors">
              <component :is="item.icon" class="w-4 h-4 text-zinc-400" />
              {{ item.label }}
            </RouterLink>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div v-if="loading && !lastRefresh" class="flex flex-col items-center justify-center py-24 gap-3">
        <Loader2 class="w-10 h-10 animate-spin text-primary/30" />
        <p class="text-xs text-muted-foreground italic">Menyiapkan dashboard Anda...</p>
      </div>

      <div v-else class="space-y-6">
        <!-- Summary Cards -->
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard v-for="card in filteredStatCards" :key="card.label" v-bind="card" />
        </div>

        <!-- Desktop Grid -->
        <div class="hidden lg:grid grid-cols-3 gap-6">
          <div class="col-span-2 space-y-6">
            <DashboardCard title="Aktivitas Terbaru" :icon="Activity">
              <ActivityTimeline :items="recentActivities" />
            </DashboardCard>
          </div>
          <div class="space-y-6">
             <DashboardCard title="Produk Populer" :icon="Package" action-label="Semua" action-to="/dashboard/products">
                <div v-if="recentProducts.length" class="space-y-3">
                   <div v-for="p in recentProducts" :key="p.id" class="flex items-center justify-between p-3 rounded-xl border border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30">
                      <div class="flex-1 min-w-0">
                         <p class="text-sm font-bold truncate">{{ p.name }}</p>
                         <p class="text-[10px] text-muted-foreground uppercase font-black tracking-tighter">{{ p.sku || 'NO SKU' }}</p>
                      </div>
                      <Badge :class="p.isActive ? 'bg-emerald-50 text-emerald-600' : 'bg-zinc-100 text-zinc-500'" variant="outline" class="text-[9px] px-1.5 h-5">{{ p.isActive ? 'Aktif' : 'Off' }}</Badge>
                   </div>
                </div>
                <div v-else class="py-10 text-center text-muted-foreground text-xs italic">Belum ada data produk</div>
             </DashboardCard>
          </div>
        </div>

        <!-- Mobile View -->
        <div class="lg:hidden">
          <MobileSummaryTabs :recent-products="recentProducts" :recent-users="recentUsers" :recent-activities="recentActivities" :format-date="formatDate" />
        </div>
      </div>
    </div>

    <AboutModal :is-open="isAboutModalOpen" @close="isAboutModalOpen = false" />
  </AppLayout>
</template>
