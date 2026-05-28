  <script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { storeToRefs } from 'pinia'
import AppLayout from '@/components/AppLayout.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import DashboardCard from '@/components/dashboard/Card.vue'
import ActivityTimeline from '@/components/dashboard/ActivityTimeline.vue'
import MobileSummaryTabs from '@/components/dashboard/MobileSummaryTabs.vue'
import AboutModal from '@/components/dashboard/AboutModal.vue'
import OnboardingChecklist from '@/components/dashboard/OnboardingChecklist.vue'
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
  ShoppingBag,
  BarChart3,
} from 'lucide-vue-next'

const auth = useAuthStore()
const { user, isAdmin } = storeToRefs(auth)
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
    const productsUrl = isAdmin.value ? '/api/v1/products/admin' : '/api/v1/products'
    const [usersRes, postsRes, categoriesRes, rolesRes, permsRes, modulesRes] = await Promise.allSettled([
      api.get('/api/v1/users'),
      api.get(productsUrl),
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
  if (isAdmin.value || can('user.index')) {
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
  if (isAdmin.value || can('produk.index')) {
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
  if (isAdmin.value || can('category.index')) {
    cards.push({
      label: 'Kategori',
      value: stats.value.totalCategories,
      icon: Boxes,
      to: '/dashboard/categories',
      color: 'text-violet-600',
      bg: 'bg-violet-50 dark:bg-violet-950/30',
      description: 'Kategori produk',
    })
  }
  if (isAdmin.value || can('role.index')) {
    cards.push({
      label: 'Role',
      value: stats.value.totalRoles,
      icon: ShieldCheck,
      to: '/dashboard/roles',
      color: 'text-amber-600',
      bg: 'bg-amber-50 dark:bg-amber-950/30',
      description: 'Role akses sistem',
    })
  }
  if (isAdmin.value || can('permission.index')) {
    cards.push({
      label: 'Permission',
      value: stats.value.totalPermissions,
      icon: KeyRound,
      to: '/dashboard/permissions',
      color: 'text-rose-600',
      bg: 'bg-rose-50 dark:bg-rose-950/30',
      description: 'Hak akses terdaftar',
    })
  }
  if (isAdmin.value || can('module.index')) {
    cards.push({
      label: 'Modul',
      value: stats.value.totalModules,
      icon: Zap,
      to: '/dashboard/modules',
      color: 'text-orange-600',
      bg: 'bg-orange-50 dark:bg-orange-950/30',
      description: 'Modul sistem aktif',
    })
  }
  if (isAdmin.value || can('produk.index')) {
    cards.push({
      label: 'Produk Aktif',
      value: stats.value.activeProducts,
      icon: BarChart3,
      to: '/dashboard/products',
      color: 'text-teal-600',
      bg: 'bg-teal-50 dark:bg-teal-950/30',
      description: 'Produk yang aktif dijual',
    })
  }
  if (isAdmin.value || can('produk.index')) {
    cards.push({
      label: 'Produk Nonaktif',
      value: stats.value.inactiveProducts,
      icon: Package,
      to: '/dashboard/products',
      color: 'text-zinc-500',
      bg: 'bg-zinc-100 dark:bg-zinc-800/40',
      description: 'Produk tidak aktif',
    })
  }
  return cards
})

const createMenuItems = computed(() => {
  const isAdminUser = isAdmin.value
  // Menu yang TIDAK ditampilkan di "Buat Baru"
  const EXCLUDED_LABELS = ['Riwayat Order', 'Mutasi Stok', 'Roles', 'Laporan', 'Audit Log', 'Dashboard']

  const items = []

  // Semua menu dari MENU_GROUPS yang bisa di-index, filter permission, exclude tertentu
  const MENU_GROUPS_FLAT = [
    { label: 'Kasir',           icon: ShoppingBag,    to: '/dashboard/kasir',            permission: null },
    { label: 'Produk',          icon: Package,         to: '/dashboard/products',         permission: 'produk.index' },
    { label: 'Saldo Stok',      icon: Package,         to: '/dashboard/stock-balances',   permission: 'stock_balance.index' },
    { label: 'Kategori',        icon: Boxes,           to: '/dashboard/categories',       permission: 'category.index' },
    { label: 'Transfer Stok',   icon: ArrowRightLeft,  to: '/dashboard/transfer-requests',permission: 'transfer_request.index' },
    { label: 'Stock Opname',    icon: Package,         to: '/dashboard/stock-opname',     permission: 'stock_opname.index' },
    { label: 'Supplier',        icon: Handshake,       to: '/dashboard/suppliers',        permission: 'supplier.index' },
    { label: 'Purchase Order',  icon: Package,         to: '/dashboard/purchase-orders',  anyPermission: ['purchase_order.index', 'purchase_receipt.store'] },
    { label: 'User Management', icon: Users,           to: '/dashboard/users',            permission: 'user.index' },
    { label: 'Partner',         icon: Handshake,       to: '/dashboard/partners',         permission: 'partner.index' },
    { label: 'Lokasi',          icon: MapPin,          to: '/dashboard/locations',        permission: 'branch.index' },
    { label: 'Voucer',          icon: Ticket,          to: '/dashboard/vouchers',         permission: 'voucher.index' },
    { label: 'Permissions',     icon: ShieldCheck,     to: '/dashboard/permissions',      permission: 'permission.index' },
    { label: 'Modul',           icon: Zap,             to: '/dashboard/modules',          permission: 'module.index' },
  ]

  for (const item of MENU_GROUPS_FLAT) {
    if (EXCLUDED_LABELS.includes(item.label)) continue
    if (item.anyPermission) {
      if (!isAdminUser && !item.anyPermission.some(p => can(p))) continue
    } else if (item.permission && !isAdminUser && !can(item.permission)) {
      continue
    }
    items.push({ label: item.label, icon: item.icon, to: item.to })
  }

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
      <div class="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Selamat datang, {{ user?.fullname || user?.username || 'User' }}! 👋</h1>
          <p class="text-sm text-muted-foreground mt-1">
            Ringkasan aktivitas operasional Gaptek POS hari ini.
          </p>
        </div>
      </div>

      <!-- ── Onboarding Checklist ── -->
      <OnboardingChecklist :stats="stats" />

      <!-- Toolbar -->
      <div class="flex items-center justify-between gap-3">
        <span class="text-xs text-muted-foreground hidden sm:inline">
          <Clock class="w-3 h-3 inline mr-1" />
          {{ lastRefresh ? formatTime(lastRefresh) : 'Belum dimuat' }}
        </span>

        <div class="flex items-center gap-2 ml-auto">
          <!-- Refresh -->
          <button
            @click="fetchStats"
            :disabled="loading"
            class="h-10 px-5 rounded-md border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-900 text-sm font-semibold hover:bg-zinc-50 dark:hover:bg-zinc-800 disabled:opacity-50 transition-colors flex items-center gap-2"
          >
            <Loader2 v-if="loading" class="w-4 h-4 animate-spin" />
            <Eye v-else class="w-4 h-4" />
            Refresh
          </button>

          <!-- Buat Baru dropdown -->
          <div class="relative shrink-0" ref="dropdownRef">
            <button
              @click.stop="dropdownOpen = !dropdownOpen"
              class="h-10 px-5 rounded-md bg-zinc-900 dark:bg-white text-white dark:text-zinc-900 text-sm font-bold flex items-center gap-2 hover:bg-zinc-700 dark:hover:bg-zinc-200 transition-colors"
            >
              <Plus class="w-4 h-4" />
              <span class="hidden sm:inline">Buat Baru</span>
              <ChevronDown class="w-4 h-4 transition-transform" :class="{ 'rotate-180': dropdownOpen }" />
            </button>
            <div
              v-if="dropdownOpen"
              class="absolute right-0 top-full mt-1.5 w-52 rounded-md border border-zinc-100 dark:border-zinc-800 bg-white dark:bg-zinc-900 shadow-xl z-50 overflow-hidden py-1 animate-in fade-in slide-in-from-top-2"
            >
              <RouterLink
                v-for="item in createMenuItems"
                :key="item.label"
                :to="item.to"
                @click="dropdownOpen = false"
                class="flex items-center gap-3 px-4 py-2.5 text-sm font-medium hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors"
              >
                <component :is="item.icon" class="w-4 h-4 text-zinc-400" />
                {{ item.label }}
              </RouterLink>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div v-if="loading && !lastRefresh" class="flex flex-col items-center justify-center py-24 gap-3">
        <Loader2 class="w-10 h-10 animate-spin text-primary/30" />
        <p class="text-xs text-muted-foreground italic">Menyiapkan dashboard Anda...</p>
      </div>

      <div v-else class="space-y-6">
        <!-- ── DESKTOP: info cards kiri (flex) + stat panel kanan (fixed) ── -->
        <div class="hidden lg:flex gap-5 items-start">

          <!-- KIRI: 3 info cards, mengisi sisa ruang -->
          <div class="flex-1 min-w-0 space-y-5">
            <DashboardCard title="Aktivitas Terbaru" :icon="Activity">
              <ActivityTimeline :items="recentActivities" />
            </DashboardCard>

            <DashboardCard title="Produk Terbaru" :icon="Package" action-label="Semua" action-to="/dashboard/products">
              <div v-if="recentProducts.length" class="space-y-2">
                <div
                  v-for="p in recentProducts"
                  :key="p.id"
                  class="flex items-center justify-between px-3 py-2.5 rounded-lg border border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30 hover:border-primary/20 transition-colors"
                >
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-semibold truncate">{{ p.name }}</p>
                    <p class="text-[10px] text-muted-foreground uppercase tracking-wider font-medium">{{ p.sku || 'NO SKU' }}</p>
                  </div>
                  <Badge
                    :class="p.isActive ? 'bg-emerald-50 text-emerald-600 border-emerald-200' : 'bg-zinc-100 text-zinc-500 border-zinc-200'"
                    variant="outline"
                    class="text-[9px] px-1.5 h-5 ml-3 shrink-0"
                  >{{ p.isActive ? 'Aktif' : 'Off' }}</Badge>
                </div>
              </div>
              <div v-else class="py-8 text-center text-muted-foreground text-xs italic">Belum ada data produk</div>
            </DashboardCard>

            <DashboardCard title="User Terbaru" :icon="Users" action-label="Semua" action-to="/dashboard/users">
              <div v-if="recentUsers.length" class="space-y-2">
                <div
                  v-for="u in recentUsers"
                  :key="u.id"
                  class="flex items-center gap-3 px-3 py-2.5 rounded-lg border border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30 hover:border-primary/20 transition-colors"
                >
                  <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary text-sm font-bold shrink-0">
                    {{ (u.fullname || u.username || '?').charAt(0).toUpperCase() }}
                  </div>
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-semibold truncate">{{ u.fullname || u.username }}</p>
                    <p class="text-[11px] text-muted-foreground">@{{ u.username }}</p>
                  </div>
                </div>
              </div>
              <div v-else class="py-8 text-center text-muted-foreground text-xs italic">Belum ada data user</div>
            </DashboardCard>
          </div>

          <!-- KANAN: Stat Panel (fixed 300px) -->
          <div class="w-[300px] shrink-0 space-y-3">

            <!-- Header panel: search + toggle view -->
            <div class="flex items-center gap-2">
              <div class="relative flex-1">
                <Search class="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-muted-foreground" />
                <input
                  v-model="searchQuery"
                  type="text"
                  placeholder="Cari..."
                  class="w-full h-8 rounded-md bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 pl-8 pr-3 text-xs focus:outline-none focus:ring-2 focus:ring-primary/20 transition-all"
                />
              </div>
              <!-- Toggle grid / list -->
              <div class="flex items-center border border-zinc-200 dark:border-zinc-800 rounded-md overflow-hidden bg-white dark:bg-zinc-900">
                <button
                  @click="statsViewMode = 'grid'"
                  :class="statsViewMode === 'grid' ? 'bg-zinc-900 dark:bg-white text-white dark:text-zinc-900' : 'text-zinc-500 hover:bg-zinc-50 dark:hover:bg-zinc-800'"
                  class="p-1.5 transition-colors"
                  title="Grid"
                >
                  <LayoutGrid class="w-3.5 h-3.5" />
                </button>
                <button
                  @click="statsViewMode = 'list'"
                  :class="statsViewMode === 'list' ? 'bg-zinc-900 dark:bg-white text-white dark:text-zinc-900' : 'text-zinc-500 hover:bg-zinc-50 dark:hover:bg-zinc-800'"
                  class="p-1.5 transition-colors"
                  title="List"
                >
                  <List class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>

            <!-- MODE LIST: satu card besar dengan divider -->
            <div
              v-if="statsViewMode === 'list'"
              class="rounded-lg border border-border/60 bg-card shadow-sm overflow-hidden divide-y divide-border/40"
            >
              <RouterLink
                v-for="card in filteredStatCards"
                :key="card.label"
                :to="card.to || '#'"
                class="flex items-center gap-3 px-4 py-3 hover:bg-zinc-50 dark:hover:bg-zinc-900/60 transition-colors"
              >
                <div :class="['w-8 h-8 rounded-md flex items-center justify-center shrink-0', card.bg]">
                  <component :is="card.icon" :class="['w-4 h-4', card.color]" />
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-[11px] text-muted-foreground leading-none mb-1">{{ card.label }}</p>
                  <p class="text-sm font-bold leading-none">{{ card.value }}</p>
                </div>
                <span v-if="card.trend" class="text-[10px] font-medium text-muted-foreground shrink-0">{{ card.trend }}</span>
              </RouterLink>
              <div v-if="filteredStatCards.length === 0" class="px-4 py-6 text-center text-xs text-muted-foreground italic">
                Tidak ada data
              </div>
            </div>

            <!-- MODE GRID: 2 kolom kotak sama sisi -->
            <div
              v-else
              class="grid grid-cols-2 gap-2"
            >
              <RouterLink
                v-for="card in filteredStatCards"
                :key="card.label"
                :to="card.to || '#'"
                class="flex flex-col gap-2 p-3 rounded-lg border border-border/60 bg-card shadow-sm hover:border-primary/30 hover:shadow-md transition-all"
              >
                <div :class="['w-8 h-8 rounded-md flex items-center justify-center shrink-0', card.bg]">
                  <component :is="card.icon" :class="['w-4 h-4', card.color]" />
                </div>
                <div>
                  <p class="text-lg font-bold leading-none">{{ card.value }}</p>
                  <p class="text-[10px] text-muted-foreground mt-0.5 leading-tight">{{ card.label }}</p>
                </div>
                <span v-if="card.trend" class="text-[9px] font-medium text-muted-foreground">{{ card.trend }}</span>
              </RouterLink>
              <div v-if="filteredStatCards.length === 0" class="col-span-2 py-6 text-center text-xs text-muted-foreground italic">
                Tidak ada data
              </div>
            </div>

          </div>
        </div>

        <!-- Mobile View -->
        <div class="lg:hidden space-y-4">
          <div class="grid grid-cols-2 gap-3">
            <StatCard v-for="card in filteredStatCards" :key="card.label" v-bind="card" />
          </div>
          <MobileSummaryTabs :recent-products="recentProducts" :recent-users="recentUsers" :recent-activities="recentActivities" :format-date="formatDate" />
        </div>
      </div>
    </div>

    <AboutModal :is-open="isAboutModalOpen" @close="isAboutModalOpen = false" />
  </AppLayout>
</template>
