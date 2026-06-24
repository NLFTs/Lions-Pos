<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { storeToRefs } from 'pinia'
import AppLayout from '@/components/AppLayout.vue'
import { useAuthStore } from '@/stores/auth'
import { usePermission } from '@/composables/usePermission'
import { useThemeStore } from '@/stores/theme'
import api from '@/lib/api'
import DateRangePicker from '@/components/dashboard/DateRangePicker.vue'
import OnboardingChecklist from '@/components/dashboard/OnboardingChecklist.vue'
import {
  TrendingUp,
  TrendingDown,
  Users,
  Boxes,
  ShieldCheck,
  KeyRound,
  Zap,
  Activity,
  UserPlus,
  Loader2,
  Plus,
  Calendar,
  Package,
  ArrowRightLeft,
  Handshake,
  MapPin,
  Ticket,
  Building2,
  Warehouse,
  ShoppingBag,
  BarChart3,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  Search,
  Truck,
} from 'lucide-vue-next'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Filler,
  Tooltip,
  Legend,
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Filler, Tooltip, Legend)

const auth = useAuthStore()
const { user, isAdmin, isSuperAdmin } = storeToRefs(auth)
const { can } = usePermission()
const themeStore = useThemeStore()

const loading = ref(true)
const error = ref(null)

// ── Calendar / Date Range Picker ─────────────────────────────────────────────
const calendarOpen = ref(false)
const dateRange = ref({ start: null, end: null })

const chartPeriodLabel = computed(() => {
  if (dateRange.value.start && dateRange.value.end) {
    const fmt = (d) => d.toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' })
    return `${fmt(dateRange.value.start)} – ${fmt(dateRange.value.end)}`
  }
  return 'Januari, 2025'
})

function onDateRangeUpdate(val) {
  dateRange.value = val
}

// Dropdown "New Project"
const dropdownOpen = ref(false)
const dropdownRef = ref(null)
function handleOutsideClick(e) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    dropdownOpen.value = false
  }
}
onMounted(() => {
  fetchStats()
  fetchBranchWarehouseStatus()
  fetchOrders()
  document.addEventListener('click', handleOutsideClick)
})
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
      stats.value.totalUsers = Array.isArray(users) ? users.length : 0
    }
    if (postsRes.status === 'fulfilled' && postsRes.value.data?.data) {
      const productsData = postsRes.value.data.data
      const productsList = Array.isArray(productsData) ? productsData : (productsData.content || [])
      stats.value.totalProducts = Array.isArray(productsData) ? productsData.length : (productsData.totalElements || 0)
      stats.value.activeProducts = productsList.filter(p => p.isActive).length
      stats.value.inactiveProducts = productsList.filter(p => !p.isActive).length
    }
    if (categoriesRes.status === 'fulfilled' && categoriesRes.value.data?.data) {
      stats.value.totalCategories = Array.isArray(categoriesRes.value.data.data) ? categoriesRes.value.data.data.length : 0
    }
    if (rolesRes.status === 'fulfilled' && rolesRes.value.data?.data) {
      stats.value.totalRoles = Array.isArray(rolesRes.value.data.data) ? rolesRes.value.data.data.length : 0
    }
    if (permsRes.status === 'fulfilled' && permsRes.value.data?.data) {
      stats.value.totalPermissions = Array.isArray(permsRes.value.data.data) ? permsRes.value.data.data.length : 0
    }
    if (modulesRes.status === 'fulfilled' && modulesRes.value.data?.data) {
      stats.value.totalModules = Array.isArray(modulesRes.value.data.data) ? modulesRes.value.data.data.length : 0
    }
  } catch (err) {
    console.error('Dashboard Error:', err)
    error.value = 'Gagal memuat beberapa data dashboard.'
  } finally {
    loading.value = false
  }
}

// ── Metric Cards ─────────────────────────────────────────────────────────────
const metricCards = computed(() => [
  {
    id: 'revenue',
    label: 'Pendapatan',
    value: 'Rp ' + (stats.value.totalProducts * 245000).toLocaleString('id-ID'),
    change: '+12.5%',
    positive: true,
    gradient: 'from-emerald-500/20 via-transparent to-transparent',
    accentColor: '#10b981',
  },
  {
    id: 'visitors',
    label: 'Pengunjung',
    value: (stats.value.totalUsers * 47 + 2345).toLocaleString('id-ID'),
    change: '+8.3%',
    positive: true,
    gradient: 'from-blue-500/20 via-transparent to-transparent',
    accentColor: '#3b82f6',
  },
  {
    id: 'conversion',
    label: 'Konversi',
    value: stats.value.totalProducts > 0 ? ((stats.value.activeProducts / stats.value.totalProducts) * 100).toFixed(1) + '%' : '3.2%',
    change: '-1.8%',
    positive: false,
    gradient: 'from-red-500/20 via-transparent to-transparent',
    accentColor: '#ef4444',
  },
  {
    id: 'session',
    label: 'Sesi Rata-rata',
    value: '4m 32s',
    change: '+10.3%',
    positive: true,
    gradient: 'from-violet-500/20 via-transparent to-transparent',
    accentColor: '#8b5cf6',
  },
])

// ── Chart Data & Theme Customization ──────────────────────────────────────────
const chartLabels = [
  'Jan 02', 'Jan 03', 'Jan 04', 'Jan 05', 'Jan 06', 'Jan 07',
  'Jan 08', 'Jan 09', 'Jan 10', 'Jan 11', 'Jan 12', 'Jan 13',
  'Jan 14', 'Jan 15', 'Jan 16', 'Jan 18',
]

const currentPeriodData = [18, 32, 28, 45, 30, 35, 42, 55, 80, 65, 72, 95, 110, 85, 90, 75]
const prevPeriodData    = [10, 20, 35, 25, 38, 20, 28, 42, 55, 48, 52, 60, 78, 65, 70, 55]

const chartColors = computed(() => {
  const currentKey = themeStore.currentTheme || 'zinc'
  const isDarkVal = themeStore.isDark
  const theme = themeStore.THEMES[currentKey] || themeStore.THEMES.default
  const modeColors = theme.colors[isDarkVal ? 'dark' : 'light']
  const primaryHslVal = modeColors['--primary']
  
  const parts = primaryHslVal.trim().split(/\s+/)
  const h = parts[0]
  const s = parts[1]
  const l = parts[2]
  
  const borderVal = `hsl(${h}, ${s}, ${l})`
  const fillStart = `hsla(${h}, ${s}, ${l}, 0.25)`
  const fillEnd = `hsla(${h}, ${s}, ${l}, 0.0)`
  const hoverBg = `hsl(${h}, ${s}, ${l})`
  
  const prevBorder = isDarkVal ? 'rgba(148, 163, 184, 0.4)' : 'rgba(148, 163, 184, 0.6)'
  const prevFillStart = isDarkVal ? 'rgba(148, 163, 184, 0.08)' : 'rgba(148, 163, 184, 0.12)'
  
  return {
    primaryBorder: borderVal,
    primaryFillStart: fillStart,
    primaryFillEnd: fillEnd,
    primaryHoverBg: hoverBg,
    prevBorder,
    prevFillStart,
  }
})

const chartData = computed(() => ({
  labels: chartLabels,
  datasets: [
    {
      label: 'Current period',
      data: currentPeriodData,
      fill: true,
      backgroundColor: (ctx) => {
        const canvas = ctx.chart.ctx
        const gradient = canvas.createLinearGradient(0, 0, 0, 280)
        gradient.addColorStop(0, chartColors.value.primaryFillStart)
        gradient.addColorStop(1, chartColors.value.primaryFillEnd)
        return gradient
      },
      borderColor: chartColors.value.primaryBorder,
      borderWidth: 2.5,
      tension: 0.45,
      pointRadius: 0,
      pointHoverRadius: 5,
      pointHoverBackgroundColor: chartColors.value.primaryHoverBg,
      pointHoverBorderColor: themeStore.isDark ? '#000' : '#fff',
      pointHoverBorderWidth: 2,
    },
    {
      label: 'Previous period',
      data: prevPeriodData,
      fill: true,
      backgroundColor: (ctx) => {
        const canvas = ctx.chart.ctx
        const gradient = canvas.createLinearGradient(0, 0, 0, 280)
        gradient.addColorStop(0, chartColors.value.prevFillStart)
        gradient.addColorStop(1, 'rgba(148, 163, 184, 0.0)')
        return gradient
      },
      borderColor: chartColors.value.prevBorder,
      borderWidth: 2,
      tension: 0.45,
      pointRadius: 0,
      pointHoverRadius: 4,
      pointHoverBackgroundColor: '#94a3b8',
      pointHoverBorderColor: themeStore.isDark ? '#000' : '#fff',
      pointHoverBorderWidth: 2,
    },
  ],
}))

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  interaction: { mode: 'index', intersect: false },
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: themeStore.isDark ? 'rgba(15, 15, 20, 0.9)' : 'rgba(255, 255, 255, 0.9)',
      borderColor: themeStore.isDark ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.08)',
      borderWidth: 1,
      titleColor: themeStore.isDark ? '#94a3b8' : '#64748b',
      bodyColor: themeStore.isDark ? '#f1f5f9' : '#0f172a',
      padding: 10,
      cornerRadius: 8,
      titleFont: { size: 11 },
      bodyFont: { size: 13, weight: 'bold' },
    },
  },
  scales: {
    x: {
      grid: { display: false },
      border: { display: false },
      ticks: {
        color: themeStore.isDark ? 'rgba(148, 163, 184, 0.6)' : 'rgba(100, 116, 139, 0.7)',
        font: { size: 11 },
        maxRotation: 0,
        maxTicksLimit: 8,
      },
    },
    y: {
      display: false,
      grid: {
        color: themeStore.isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.05)',
        drawBorder: false,
      },
    },
  },
}))

// ── Gauge (SVG Circular Progress) ─────────────────────────────────────────────
const gaugePercent = computed(() => {
  if (stats.value.totalProducts === 0) return 42
  return Math.min(100, Math.round((stats.value.activeProducts / (stats.value.totalProducts || 1)) * 100))
})

const daysLeft = 16
const monthlyTarget = 'Rp 27.000.000'

const GAUGE_R = 72
const GAUGE_CX = 96
const GAUGE_CY = 96
const GAUGE_STROKE = 8
const GAUGE_CIRCUMFERENCE = computed(() => 2 * Math.PI * GAUGE_R)
const GAUGE_DASH_OFFSET = computed(() => GAUGE_CIRCUMFERENCE.value * (1 - gaugePercent.value / 100))

// Tick marks for the gauge
const ticks = Array.from({ length: 36 }, (_, i) => i)

// ── Quick Actions (New Project dropdown) ──────────────────────────────────────
const createMenuItems = computed(() => {
  const items = []
  const EXCLUDED = ['Riwayat Order', 'Mutasi Stok', 'Roles', 'Laporan', 'Audit Log', 'Dashboard']
  const ALL = [
    { label: 'Kasir',           icon: ShoppingBag,   to: '/dashboard/kasir' },
    { label: 'Produk',          icon: Package,        to: '/dashboard/products',          permission: 'produk.index' },
    { label: 'Kategori',        icon: Boxes,          to: '/dashboard/categories',        permission: 'category.index' },
    { label: 'Transfer Stok',   icon: ArrowRightLeft, to: '/dashboard/transfer-requests', permission: 'transfer_request.index' },
    { label: 'Supplier',        icon: Handshake,      to: '/dashboard/suppliers',         permission: 'supplier.index' },
    { label: 'User Management', icon: Users,          to: '/dashboard/users',             permission: 'user.index' },
    { label: 'Partner',         icon: Handshake,      to: '/dashboard/partners',          permission: 'partner.index' },
    { label: 'Cabang',          icon: Building2,      to: '/dashboard/branches',          permission: 'branch.index' },
    { label: 'Gudang',          icon: Warehouse,      to: '/dashboard/warehouses',        permission: 'warehouse.index' },
    { label: 'Voucher',         icon: Ticket,         to: '/dashboard/vouchers',          permission: 'voucher.index' },
    { label: 'Modul',           icon: Zap,            to: '/dashboard/modules',           permission: 'module.index' },
  ]
  for (const item of ALL) {
    if (EXCLUDED.includes(item.label)) continue
    if (item.permission && !isAdmin.value && !can(item.permission)) continue
    items.push(item)
  }
  return items
})

// Animate gauge on mount
const animatedPercent = ref(0)
onMounted(() => {
  setTimeout(() => {
    const target = gaugePercent.value
    let current = 0
    const step = () => {
      current = Math.min(target, current + 1.5)
      animatedPercent.value = Math.round(current)
      if (current < target) requestAnimationFrame(step)
    }
    requestAnimationFrame(step)
  }, 500)
})

watch(gaugePercent, (val) => {
  animatedPercent.value = val
})

const animatedDashOffset = computed(() =>
  GAUGE_CIRCUMFERENCE.value * (1 - animatedPercent.value / 100)
)

// ── Branch & Warehouse Status ─────────────────────────────────────────────────
const activeTab = ref('status')
const branchStatusFilter = ref('Semua')
const branchStatusFilters = ['Semua', 'Cabang', 'Gudang']

const branchesData = ref([])
const warehousesData = ref([])
const statusLoading = ref(false)

async function fetchBranchWarehouseStatus() {
  statusLoading.value = true
  try {
    const branchUrl = isSuperAdmin.value ? '/api/v1/branches/admin' : '/api/v1/branches'
    const warehouseUrl = isSuperAdmin.value
      ? '/api/v1/warehouses/admin?page=0&size=100'
      : '/api/v1/warehouses'
    const [branchRes, warehouseRes] = await Promise.allSettled([
      api.get(branchUrl),
      api.get(warehouseUrl),
    ])
    if (branchRes.status === 'fulfilled') {
      const d = branchRes.value.data
      // superadmin endpoint returns plain array; partner returns { data: [...] }
      const list = Array.isArray(d) ? d : (Array.isArray(d?.data) ? d.data : (d?.data?.content || []))
      branchesData.value = list
    }
    if (warehouseRes.status === 'fulfilled') {
      const d = warehouseRes.value.data
      const list = Array.isArray(d) ? d : (Array.isArray(d?.data) ? d.data : (d?.data?.content || []))
      warehousesData.value = list
    }
  } catch (err) {
    console.error('Status fetch error:', err)
  } finally {
    statusLoading.value = false
  }
}

const combinedStatusItems = computed(() => {
  const branches = branchesData.value.map(b => ({
    id: `branch-${b.id}`,
    name: b.name,
    address: b.address || '',
    isActive: b.is_active ?? b.isActive ?? false,
    type: 'Cabang',
    icon: Building2,
  }))
  const warehouses = warehousesData.value.map(w => ({
    id: `warehouse-${w.id}`,
    name: w.name,
    address: w.address || '',
    isActive: w.is_active ?? w.isActive ?? false,
    type: 'Gudang',
    icon: Warehouse,
  }))
  const all = [...branches, ...warehouses]
  if (branchStatusFilter.value === 'Cabang') return branches
  if (branchStatusFilter.value === 'Gudang') return warehouses
  return all
})

// ── Recent Activity ───────────────────────────────────────────────────────────
// (dikosongkan — akan diisi dari backend nanti)
const recentActivities = []

// ── Orders & Shipping (real data) ────────────────────────────────────────────
const orderSearch = ref('')
const orderPage   = ref(1)
const ordersPerPage = 5
const ordersLoading = ref(false)

const allOrders = ref([])
const selectedOrders = ref([])

async function fetchOrders() {
  ordersLoading.value = true
  try {
    const url = isSuperAdmin.value ? '/api/v1/orders/admin' : '/api/v1/orders'
    const res = await api.get(url)
    const d = res.data
    const list = Array.isArray(d?.data) ? d.data : (Array.isArray(d) ? d : [])
    // Map API response to display format
    allOrders.value = list.map(o => ({
      id: o.id,
      orderNumber: o.orderNumber || `#${o.id}`,
      customer: o.buyerName || '-',
      date: o.createdAt ? new Date(o.createdAt).toLocaleDateString('id-ID') : '-',
      branch: o.branchName || '-',
      total: o.total != null ? 'Rp ' + Number(o.total).toLocaleString('id-ID') : '-',
      status: (o.status || 'pending').toLowerCase(),
      statusLabel: mapOrderStatusLabel(o.status),
    }))
  } catch (err) {
    console.error('Orders fetch error:', err)
  } finally {
    ordersLoading.value = false
  }
}

function mapOrderStatusLabel(status) {
  const s = (status || '').toLowerCase()
  const map = {
    completed: 'Selesai',
    pending: 'Tertunda',
    cancelled: 'Dibatalkan',
    returned: 'Dikembalikan',
    processing: 'Diproses',
    shipped: 'Dikirim',
    delivered: 'Terkirim',
    paid: 'Lunas',
  }
  return map[s] || status || 'Tertunda'
}

const filteredOrders = computed(() => {
  const q = orderSearch.value.toLowerCase()
  if (!q) return allOrders.value
  return allOrders.value.filter(o =>
    o.orderNumber.toLowerCase().includes(q) ||
    o.customer.toLowerCase().includes(q) ||
    o.branch.toLowerCase().includes(q) ||
    o.statusLabel.toLowerCase().includes(q)
  )
})

const totalOrderPages = computed(() => Math.max(1, Math.ceil(filteredOrders.value.length / ordersPerPage)))

const pagedOrders = computed(() => {
  const start = (orderPage.value - 1) * ordersPerPage
  return filteredOrders.value.slice(start, start + ordersPerPage)
})

watch(orderSearch, () => { orderPage.value = 1 })

const orderStatusStyle = {
  completed:  'text-emerald-700 bg-emerald-50 dark:text-emerald-400 dark:bg-emerald-950/30',
  paid:       'text-emerald-700 bg-emerald-50 dark:text-emerald-400 dark:bg-emerald-950/30',
  delivered:  'text-emerald-700 bg-emerald-50 dark:text-emerald-400 dark:bg-emerald-950/30',
  shipped:    'text-blue-700 bg-blue-50 dark:text-blue-400 dark:bg-blue-950/30',
  processing: 'text-amber-700 bg-amber-50 dark:text-amber-400 dark:bg-amber-950/30',
  cancelled:  'text-red-700 bg-red-50 dark:text-red-400 dark:bg-red-950/30',
  returned:   'text-orange-700 bg-orange-50 dark:text-orange-400 dark:bg-orange-950/30',
  pending:    'text-muted-foreground bg-muted',
}

function toggleOrder(id) {
  const idx = selectedOrders.value.indexOf(id)
  if (idx === -1) selectedOrders.value.push(id)
  else selectedOrders.value.splice(idx, 1)
}

function toggleAllOrders() {
  if (selectedOrders.value.length === pagedOrders.value.length) {
    selectedOrders.value = []
  } else {
    selectedOrders.value = pagedOrders.value.map(o => o.id)
  }
}
</script>

<template>
  <AppLayout>
    <div class="dashboard-root space-y-5 text-foreground">

      <!-- ── Onboarding Checklist (Tutorial) ──────────────────────────── -->
      <OnboardingChecklist :stats="stats" />

      <!-- ── Header ─────────────────────────────────────────────────────────── -->
      <div class="flex items-start justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight text-foreground leading-tight">Dashboard</h1>
          <p class="text-xs text-muted-foreground mt-1">
            Selamat datang, {{ user?.fullname || user?.username || 'User' }}! Inilah yang terjadi hari ini.
          </p>
        </div>

        <!-- New Project Button -->
        <div class="relative shrink-0" ref="dropdownRef">
          <button
            id="btn-new-project"
            @click.stop="dropdownOpen = !dropdownOpen"
            class="flex items-center gap-1.5 px-3 py-1.5 rounded-md border border-border bg-card hover:bg-muted text-foreground text-xs font-semibold shadow-sm transition-all duration-200"
          >
            <Plus class="w-3.5 h-3.5" />
            New Project
          </button>
          <div
            v-if="dropdownOpen"
            class="absolute right-0 top-full mt-2 w-48 rounded-md border border-border bg-popover text-popover-foreground shadow-lg z-50 overflow-hidden py-1 animate-in fade-in slide-in-from-top-2"
          >
            <RouterLink
              v-for="item in createMenuItems"
              :key="item.label"
              :to="item.to"
              @click="dropdownOpen = false"
              class="flex items-center gap-2 px-3 py-2 text-xs font-medium text-muted-foreground hover:text-foreground hover:bg-accent hover:text-accent-foreground transition-colors"
            >
              <component :is="item.icon" class="w-3.5 h-3.5 text-muted-foreground" />
              {{ item.label }}
            </RouterLink>
          </div>
        </div>
      </div>

      <!-- ── Loading State ─────────────────────────────────────────────────── -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-24 gap-3">
        <Loader2 class="w-8 h-8 animate-spin text-primary" />
        <p class="text-xs text-muted-foreground italic">Menyiapkan dashboard...</p>
      </div>

      <template v-else>
        <!-- ── Metric Cards ─────────────────────────────────────────────────── -->
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-3">
          <div
            v-for="card in metricCards"
            :key="card.id"
            class="metric-card relative overflow-hidden rounded-xl border border-border bg-card p-4 group transition-all duration-300"
          >
            <!-- Gradient glow (subtle, dynamic opacity) -->
            <div
              class="absolute inset-0 opacity-15 dark:opacity-25 group-hover:opacity-25 dark:group-hover:opacity-40 transition-opacity duration-300 rounded-xl"
              :class="`bg-gradient-to-br ${card.gradient}`"
            />

            <div class="relative">
              <!-- Label + Change Badge -->
              <div class="flex items-center justify-between mb-3">
                <span class="text-xs font-medium text-muted-foreground">{{ card.label }}</span>
                <span
                  class="inline-flex items-center gap-0.5 text-[11px] font-semibold px-1.5 py-0.5 rounded-md"
                  :class="card.positive
                    ? 'text-emerald-600 bg-emerald-50 dark:text-emerald-400 dark:bg-emerald-950/30'
                    : 'text-red-600 bg-red-50 dark:text-red-400 dark:bg-red-950/30'"
                >
                  <TrendingUp v-if="card.positive" class="w-3 h-3" />
                  <TrendingDown v-else class="w-3 h-3" />
                  {{ card.change }}
                </span>
              </div>

              <!-- Value -->
              <p class="text-xl font-bold text-foreground tracking-tight leading-none">{{ card.value }}</p>
            </div>
          </div>
        </div>

        <!-- ── Main Content: Chart + Gauge ──────────────────────────────────── -->
        <div class="grid grid-cols-1 lg:grid-cols-[1fr_240px] gap-4">

          <!-- Chart Card -->
          <div class="chart-card rounded-xl border border-border bg-card p-5">
            <!-- Chart Header -->
            <div class="flex items-start justify-between mb-1">
              <div>
                <h2 class="text-sm font-bold text-foreground">Pertumbuhan Pendapatan</h2>
                <p class="text-xs text-muted-foreground mt-0.5">{{ chartPeriodLabel }}</p>
              </div>
              <button
                id="btn-calendar-picker"
                @click="calendarOpen = !calendarOpen"
                class="p-1.5 rounded-md border border-transparent transition-colors"
                :class="calendarOpen
                  ? 'text-primary bg-primary/10 border-primary/20'
                  : 'text-muted-foreground hover:text-foreground hover:bg-muted'"
                title="Pilih rentang tanggal"
              >
                <Calendar class="w-4 h-4" />
              </button>
            </div>

            <!-- Date Range Picker -->
            <DateRangePicker
              :open="calendarOpen"
              v-model="dateRange"
              @update:modelValue="onDateRangeUpdate"
              @close="calendarOpen = false"
            />

            <!-- Legend -->
            <div class="flex items-center gap-5 mb-4 mt-2">
              <div class="flex items-center gap-2">
                <span class="w-2.5 h-2.5 rounded-full bg-primary"></span>
                <span class="text-xs text-muted-foreground font-medium">Periode saat ini</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="w-2.5 h-2.5 rounded-full bg-muted-foreground/40"></span>
                <span class="text-xs text-muted-foreground font-medium">Periode sebelumnya</span>
              </div>
            </div>

            <!-- Chart area -->
            <div class="chart-wrapper" style="height: 200px;">
              <Line :data="chartData" :options="chartOptions" />
            </div>
          </div>

          <!-- Gauge Card -->
          <div class="gauge-card rounded-xl border border-border bg-card p-5 flex flex-col items-center justify-center relative overflow-hidden">
            <!-- Subtle glow using theme primary -->
            <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-48 h-48 rounded-full"
                 style="background: radial-gradient(circle, hsla(var(--primary), 0.08) 0%, transparent 70%);" />

            <!-- Days left label -->
            <p class="text-xs text-muted-foreground mb-4 relative z-10">{{ daysLeft }}d tersisa</p>

            <!-- SVG Gauge -->
            <div class="relative z-10">
              <svg :width="GAUGE_CX * 2" :height="GAUGE_CY * 2" class="overflow-visible -rotate-90">
                <!-- Tick marks -->
                <g v-for="tick in ticks" :key="tick">
                  <line
                    :x1="GAUGE_CX + (GAUGE_R + 6) * Math.cos((tick / ticks.length) * 2 * Math.PI)"
                    :y1="GAUGE_CY + (GAUGE_R + 6) * Math.sin((tick / ticks.length) * 2 * Math.PI)"
                    :x2="GAUGE_CX + (GAUGE_R + 10) * Math.cos((tick / ticks.length) * 2 * Math.PI)"
                    :y2="GAUGE_CY + (GAUGE_R + 10) * Math.sin((tick / ticks.length) * 2 * Math.PI)"
                    :stroke="(tick / ticks.length) <= (animatedPercent / 100) ? 'hsl(var(--primary))' : 'hsl(var(--border))'"
                    stroke-width="2"
                    stroke-linecap="round"
                  />
                </g>

                <!-- Background ring -->
                <circle
                  :cx="GAUGE_CX"
                  :cy="GAUGE_CY"
                  :r="GAUGE_R"
                  fill="none"
                  stroke="hsl(var(--border))"
                  :stroke-width="GAUGE_STROKE"
                />

                <!-- Progress ring -->
                <circle
                  :cx="GAUGE_CX"
                  :cy="GAUGE_CY"
                  :r="GAUGE_R"
                  fill="none"
                  stroke="hsl(var(--primary))"
                  :stroke-width="GAUGE_STROKE"
                  stroke-linecap="round"
                  :stroke-dasharray="GAUGE_CIRCUMFERENCE"
                  :stroke-dashoffset="animatedDashOffset"
                  style="transition: stroke-dashoffset 1.2s cubic-bezier(0.4, 0, 0.2, 1);"
                />
              </svg>

              <!-- Center label -->
              <div class="absolute inset-0 flex flex-col items-center justify-center rotate-0">
                <span class="text-3xl font-black text-foreground leading-none">{{ animatedPercent }}</span>
                <span class="text-xs text-muted-foreground font-medium">%</span>
              </div>
            </div>

            <!-- Monthly Target -->
            <div class="mt-5 text-center relative z-10">
              <p class="text-xs text-muted-foreground mb-1">Target Bulanan</p>
              <p class="text-sm font-bold text-foreground tracking-tight">{{ monthlyTarget }}</p>
            </div>
          </div>

        </div>

        <!-- ── Secondary Stats Row ───────────────────────────────────────────── -->
        <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
          <RouterLink
            v-if="isAdmin || can('user.index')"
            to="/dashboard/users"
            class="secondary-card rounded-xl border border-border bg-card p-4 hover:bg-muted/40 transition-all duration-200"
          >
            <div class="w-8 h-8 rounded-lg bg-primary/10 text-primary flex items-center justify-center mb-3">
              <Users class="w-4 h-4" />
            </div>
            <p class="text-lg font-bold text-foreground">{{ stats.totalUsers }}</p>
            <p class="text-[11px] text-muted-foreground mt-0.5">Total Pengguna</p>
          </RouterLink>

          <RouterLink
            v-if="isAdmin || can('produk.index')"
            to="/dashboard/products"
            class="secondary-card rounded-xl border border-border bg-card p-4 hover:bg-muted/40 transition-all duration-200"
          >
            <div class="w-8 h-8 rounded-lg bg-primary/10 text-primary flex items-center justify-center mb-3">
              <Package class="w-4 h-4" />
            </div>
            <p class="text-lg font-bold text-foreground">{{ stats.totalProducts }}</p>
            <p class="text-[11px] text-muted-foreground mt-0.5">Total Produk</p>
          </RouterLink>

          <RouterLink
            v-if="isAdmin || can('category.index')"
            to="/dashboard/categories"
            class="secondary-card rounded-xl border border-border bg-card p-4 hover:bg-muted/40 transition-all duration-200"
          >
            <div class="w-8 h-8 rounded-lg bg-primary/10 text-primary flex items-center justify-center mb-3">
              <Boxes class="w-4 h-4" />
            </div>
            <p class="text-lg font-bold text-foreground">{{ stats.totalCategories }}</p>
            <p class="text-[11px] text-muted-foreground mt-0.5">Kategori</p>
          </RouterLink>

          <RouterLink
            v-if="(auth.user?.roles || []).some(r => (typeof r === 'object' ? r.slug : r) === 'admin' || (typeof r === 'object' ? r.slug : r) === 'super-admin')"
            to="/dashboard/roles"
            class="secondary-card rounded-xl border border-border bg-card p-4 hover:bg-muted/40 transition-all duration-200"
          >
            <div class="w-8 h-8 rounded-lg bg-primary/10 text-primary flex items-center justify-center mb-3">
              <ShieldCheck class="w-4 h-4" />
            </div>
            <p class="text-lg font-bold text-foreground">{{ stats.totalRoles }}</p>
            <p class="text-[11px] text-muted-foreground mt-0.5">Hak akses</p>
          </RouterLink>

          <RouterLink
            v-if="(auth.user?.roles || []).some(r => (typeof r === 'object' ? r.slug : r) === 'admin' || (typeof r === 'object' ? r.slug : r) === 'super-admin')"
            to="/dashboard/permissions"
            class="secondary-card rounded-xl border border-border bg-card p-4 hover:bg-muted/40 transition-all duration-200"
          >
            <div class="w-8 h-8 rounded-lg bg-primary/10 text-primary flex items-center justify-center mb-3">
              <KeyRound class="w-4 h-4" />
            </div>
            <p class="text-lg font-bold text-foreground">{{ stats.totalPermissions }}</p>
            <p class="text-[11px] text-muted-foreground mt-0.5">Permission</p>
          </RouterLink>

          <RouterLink
            v-if="(auth.user?.roles || []).some(r => (typeof r === 'object' ? r.slug : r) === 'admin' || (typeof r === 'object' ? r.slug : r) === 'super-admin')"
            to="/dashboard/modules"
            class="secondary-card rounded-xl border border-border bg-card p-4 hover:bg-muted/40 transition-all duration-200"
          >
            <div class="w-8 h-8 rounded-lg bg-primary/10 text-primary flex items-center justify-center mb-3">
              <Zap class="w-4 h-4" />
            </div>
            <p class="text-lg font-bold text-foreground">{{ stats.totalModules }}</p>
            <p class="text-[11px] text-muted-foreground mt-0.5">Modul</p>
          </RouterLink>
        </div>

      </template>

      <!-- ── Desktop View (lg:grid / lg:block) ─────────────────────────────── -->
      <div class="hidden lg:grid grid-cols-1 lg:grid-cols-2 gap-4">

        <!-- Status Cabang & Gudang -->
        <div class="rounded-xl border border-border bg-card overflow-hidden">
          <div class="flex items-center justify-between px-5 py-4 border-b border-border">
            <h2 class="text-sm font-bold text-foreground">Status Cabang</h2>
            <div class="flex items-center gap-1 bg-muted rounded-full p-0.5">
              <button
                v-for="f in branchStatusFilters" :key="f"
                @click="branchStatusFilter = f"
                class="px-3 py-1 text-xs font-semibold rounded-full transition-all duration-200"
                :class="branchStatusFilter === f
                  ? 'bg-background text-foreground shadow-sm'
                  : 'text-muted-foreground hover:text-foreground'"
              >{{ f }}</button>
            </div>
          </div>
          <!-- Loading -->
          <div v-if="statusLoading" class="flex items-center justify-center py-10">
            <Loader2 class="w-5 h-5 animate-spin text-muted-foreground" />
          </div>
          <!-- Empty -->
          <div v-else-if="combinedStatusItems.length === 0" class="flex flex-col items-center justify-center py-10 gap-2">
            <Building2 class="w-8 h-8 text-muted-foreground/30" />
            <p class="text-xs text-muted-foreground">Tidak ada data cabang/gudang</p>
          </div>
          <!-- Items -->
          <div v-else class="divide-y divide-border/60 max-h-72 overflow-y-auto">
            <div
              v-for="item in combinedStatusItems" :key="item.id"
              class="flex items-center justify-between px-5 py-3 hover:bg-muted/40 transition-colors"
            >
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-7 h-7 rounded-md flex items-center justify-center shrink-0"
                  :class="item.isActive ? 'bg-emerald-500/10' : 'bg-red-500/10'">
                  <component :is="item.icon" class="w-3.5 h-3.5"
                    :class="item.isActive ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-500'" />
                </div>
                <div class="min-w-0">
                  <p class="text-xs font-medium text-foreground truncate">{{ item.name }}</p>
                  <p v-if="item.address" class="text-[10px] text-muted-foreground truncate">{{ item.address }}</p>
                </div>
              </div>
              <div class="flex items-center gap-2 shrink-0 ml-3">
                <span class="text-[10px] font-medium px-2 py-0.5 rounded-full"
                  :class="item.isActive
                    ? 'text-emerald-700 bg-emerald-50 dark:text-emerald-400 dark:bg-emerald-950/30'
                    : 'text-red-600 bg-red-50 dark:text-red-400 dark:bg-red-950/30'"
                >
                  {{ item.isActive ? 'Aktif' : 'Non-Aktif' }}
                </span>
                <span class="w-1.5 h-1.5 rounded-full shrink-0"
                  :class="item.isActive ? 'bg-emerald-500' : 'bg-red-500'" />
              </div>
            </div>
          </div>
        </div>

        <!-- Aktivitas Terbaru (kosong) -->
        <div class="rounded-xl border border-border bg-card overflow-hidden">
          <div class="flex items-center justify-between px-5 py-4 border-b border-border">
            <h2 class="text-sm font-bold text-foreground">Aktivitas Terbaru</h2>
          </div>
          <div class="flex flex-col items-center justify-center py-14 gap-3">
            <Activity class="w-10 h-10 text-muted-foreground/20" />
            <p class="text-xs text-muted-foreground text-center">Belum ada aktivitas terbaru</p>
          </div>
        </div>

      </div>

      <!-- ── Desktop Orders & Pengiriman ──────────────────────────────────────────────── -->
      <div class="hidden lg:block rounded-xl border border-border bg-card overflow-hidden">

        <!-- Header -->
        <div class="flex items-center justify-between px-5 py-4 border-b border-border">
          <div>
            <h2 class="text-sm font-bold text-foreground">Pesanan &amp; Pengiriman</h2>
            <p class="text-[10px] text-muted-foreground mt-0.5">Riwayat order dari mutasi stok</p>
          </div>
          <div class="flex items-center gap-2">
            <div class="relative">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-muted-foreground" />
              <input
                v-model="orderSearch"
                type="text"
                placeholder="Cari order..."
                class="w-48 pl-8 pr-3 py-1.5 text-xs bg-muted border border-border rounded-md text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:bg-background transition-all"
              />
            </div>
            <button @click="fetchOrders" class="p-1.5 rounded-md border border-border text-muted-foreground hover:text-foreground hover:bg-muted transition-all" title="Refresh">
              <Loader2 v-if="ordersLoading" class="w-3.5 h-3.5 animate-spin" />
              <ArrowRightLeft v-else class="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

        <!-- Loading -->
        <div v-if="ordersLoading" class="flex items-center justify-center py-16">
          <Loader2 class="w-6 h-6 animate-spin text-muted-foreground" />
        </div>

        <!-- Table -->
        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="border-b border-border">
                <th class="w-10 px-5 py-3">
                  <input
                    type="checkbox"
                    class="orders-cb"
                    :checked="selectedOrders.length === pagedOrders.length && pagedOrders.length > 0"
                    @change="toggleAllOrders"
                  />
                </th>
                <th class="text-left px-3 py-3 text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">No. Order</th>
                <th class="text-left px-3 py-3 text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">Pelanggan</th>
                <th class="text-left px-3 py-3 text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">Tanggal</th>
                <th class="text-left px-3 py-3 text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">Cabang</th>
                <th class="text-left px-3 py-3 text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">Status</th>
                <th class="text-right px-5 py-3 text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">Total</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-border/60">
              <tr
                v-for="order in pagedOrders" :key="order.id"
                class="hover:bg-muted/40 transition-colors cursor-pointer"
                :class="{ 'bg-primary/5': selectedOrders.includes(order.id) }"
              >
                <td class="px-5 py-3">
                  <input
                    type="checkbox"
                    class="orders-cb"
                    :checked="selectedOrders.includes(order.id)"
                    @change="toggleOrder(order.id)"
                  />
                </td>
                <td class="px-3 py-3">
                  <span class="text-xs font-semibold text-foreground font-mono">{{ order.orderNumber }}</span>
                </td>
                <td class="px-3 py-3">
                  <div class="flex items-center gap-2">
                    <div class="w-5 h-5 rounded-full bg-primary/10 flex items-center justify-center text-[9px] font-bold text-primary">
                      {{ (order.customer || '-')[0].toUpperCase() }}
                    </div>
                    <span class="text-xs text-foreground">{{ order.customer }}</span>
                  </div>
                </td>
                <td class="px-3 py-3 text-xs text-muted-foreground">{{ order.date }}</td>
                <td class="px-3 py-3 text-xs text-muted-foreground">{{ order.branch }}</td>
                <td class="px-3 py-3">
                  <span
                    class="inline-flex items-center text-[10px] font-semibold px-2 py-0.5 rounded-md"
                    :class="orderStatusStyle[order.status] || orderStatusStyle.pending"
                  >
                    {{ order.statusLabel }}
                  </span>
                </td>
                <td class="px-5 py-3 text-right">
                  <span class="text-xs font-bold text-foreground">{{ order.total }}</span>
                </td>
              </tr>

              <!-- Empty state -->
              <tr v-if="pagedOrders.length === 0">
                <td colspan="7" class="px-5 py-12 text-center">
                  <div class="flex flex-col items-center gap-2">
                    <ShoppingBag class="w-8 h-8 text-muted-foreground/20" />
                    <span class="text-xs text-muted-foreground">Belum ada order ditemukan</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="flex items-center justify-between px-5 py-3 border-t border-border">
          <div class="flex items-center gap-2">
            <button
              @click="orderPage = Math.max(1, orderPage - 1)"
              :disabled="orderPage === 1"
              class="w-7 h-7 rounded-md flex items-center justify-center border border-border text-muted-foreground hover:text-foreground hover:bg-muted disabled:opacity-30 disabled:cursor-not-allowed transition-all"
            >
              <ChevronLeft class="w-3.5 h-3.5" />
            </button>
            <button
              v-for="p in totalOrderPages" :key="p"
              @click="orderPage = p"
              class="w-7 h-7 rounded-md text-xs font-semibold transition-all"
              :class="orderPage === p
                ? 'bg-primary text-primary-foreground shadow-sm'
                : 'text-muted-foreground hover:text-foreground hover:bg-muted border border-border'"
            >{{ p }}</button>
            <button
              @click="orderPage = Math.min(totalOrderPages, orderPage + 1)"
              :disabled="orderPage === totalOrderPages"
              class="w-7 h-7 rounded-md flex items-center justify-center border border-border text-muted-foreground hover:text-foreground hover:bg-muted disabled:opacity-30 disabled:cursor-not-allowed transition-all"
            >
              <ChevronRight class="w-3.5 h-3.5" />
            </button>
          </div>
          <div class="flex items-center gap-1.5 text-xs text-muted-foreground">
            <ChevronDown class="w-3.5 h-3.5" />
            <span>{{ ordersPerPage }} per halaman</span>
          </div>
        </div>

      </div>

      <!-- ── Mobile Tabbed Card View (block lg:hidden) ───────────────────────── -->
      <div class="block lg:hidden rounded-xl border border-border bg-card overflow-hidden">
        <!-- Tabs Header -->
        <div class="flex border-b border-border bg-muted/30 p-1 gap-1">
          <button
            @click="activeTab = 'status'"
            class="flex-1 flex items-center justify-center gap-1.5 py-2 px-2 text-[11px] font-semibold rounded-lg transition-all duration-200"
            :class="activeTab === 'status' ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground'"
          >
            <Building2 class="w-3.5 h-3.5" />
            Status
          </button>
          <button
            @click="activeTab = 'activity'"
            class="flex-1 flex items-center justify-center gap-1.5 py-2 px-2 text-[11px] font-semibold rounded-lg transition-all duration-200"
            :class="activeTab === 'activity' ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground'"
          >
            <Activity class="w-3.5 h-3.5" />
            Aktivitas
          </button>
          <button
            @click="activeTab = 'orders'"
            class="flex-1 flex items-center justify-center gap-1.5 py-2 px-2 text-[11px] font-semibold rounded-lg transition-all duration-200"
            :class="activeTab === 'orders' ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground'"
          >
            <ShoppingBag class="w-3.5 h-3.5" />
            Pesanan
          </button>
        </div>

        <!-- Tab Contents -->
        <div>
          <!-- Tab: Status Cabang & Gudang -->
          <div v-show="activeTab === 'status'" class="animate-in fade-in duration-200">
            <div class="flex items-center justify-between px-4 py-3 border-b border-border">
              <span class="text-xs font-semibold text-muted-foreground">Filter:</span>
              <div class="flex items-center gap-1 bg-muted rounded-full p-0.5">
                <button
                  v-for="f in branchStatusFilters" :key="f"
                  @click="branchStatusFilter = f"
                  class="px-2.5 py-0.5 text-[11px] font-semibold rounded-full transition-all duration-200"
                  :class="branchStatusFilter === f
                    ? 'bg-background text-foreground shadow-sm'
                    : 'text-muted-foreground hover:text-foreground'"
                >{{ f }}</button>
              </div>
            </div>
            <!-- Loading -->
            <div v-if="statusLoading" class="flex items-center justify-center py-8">
              <Loader2 class="w-5 h-5 animate-spin text-muted-foreground" />
            </div>
            <!-- Empty -->
            <div v-else-if="combinedStatusItems.length === 0" class="flex flex-col items-center justify-center py-10 gap-2">
              <Building2 class="w-7 h-7 text-muted-foreground/30" />
              <p class="text-xs text-muted-foreground">Tidak ada data cabang/gudang</p>
            </div>
            <!-- Items -->
            <div v-else class="divide-y divide-border/60">
              <div
                v-for="item in combinedStatusItems" :key="item.id"
                class="flex items-center justify-between px-4 py-3 hover:bg-muted/40 transition-colors"
              >
                <div class="flex items-center gap-3 min-w-0">
                  <div class="w-7 h-7 rounded-md flex items-center justify-center shrink-0"
                    :class="item.isActive ? 'bg-emerald-500/10' : 'bg-red-500/10'">
                    <component :is="item.icon" class="w-3.5 h-3.5"
                      :class="item.isActive ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-500'" />
                  </div>
                  <div class="min-w-0">
                    <p class="text-xs font-medium text-foreground truncate">{{ item.name }}</p>
                    <p class="text-[10px] text-muted-foreground">{{ item.type }}</p>
                  </div>
                </div>
                <div class="flex items-center gap-2 shrink-0">
                  <span class="text-[10px] font-semibold px-2 py-0.5 rounded-full"
                    :class="item.isActive
                      ? 'text-emerald-700 bg-emerald-50 dark:text-emerald-400 dark:bg-emerald-950/30'
                      : 'text-red-600 bg-red-50 dark:text-red-400 dark:bg-red-950/30'"
                  >
                    {{ item.isActive ? 'Aktif' : 'Non-Aktif' }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab: Aktivitas Terbaru (kosong) -->
          <div v-show="activeTab === 'activity'" class="animate-in fade-in duration-200">
            <div class="flex items-center justify-between px-4 py-3 border-b border-border">
              <span class="text-xs font-semibold text-muted-foreground">Log Aktivitas</span>
            </div>
            <div class="flex flex-col items-center justify-center py-12 gap-3">
              <Activity class="w-9 h-9 text-muted-foreground/20" />
              <p class="text-xs text-muted-foreground">Belum ada aktivitas terbaru</p>
            </div>
          </div>

          <!-- Tab: Pesanan & Pengiriman -->
          <div v-show="activeTab === 'orders'" class="animate-in fade-in duration-200">
            <div class="flex items-center justify-between px-4 py-3 border-b border-border">
              <span class="text-xs font-semibold text-muted-foreground">Daftar Pesanan</span>
              <div class="relative">
                <Search class="absolute left-2.5 top-1/2 -translate-y-1/2 w-3 h-3 text-muted-foreground" />
                <input
                  v-model="orderSearch"
                  type="text"
                  placeholder="Cari..."
                  class="w-32 pl-7 pr-2.5 py-1.5 text-[11px] bg-muted border border-border rounded-md text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:bg-background transition-all"
                />
              </div>
            </div>

            <!-- Loading -->
            <div v-if="ordersLoading" class="flex items-center justify-center py-10">
              <Loader2 class="w-5 h-5 animate-spin text-muted-foreground" />
            </div>

            <div v-else class="overflow-x-auto">
              <table class="w-full">
                <thead>
                  <tr class="border-b border-border">
                    <th class="w-10 px-4 py-3">
                      <input
                        type="checkbox"
                        class="orders-cb"
                        :checked="selectedOrders.length === pagedOrders.length && pagedOrders.length > 0"
                        @change="toggleAllOrders"
                      />
                    </th>
                    <th class="text-left px-2 py-3 text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">No. Order</th>
                    <th class="text-left px-2 py-3 text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">Pelanggan</th>
                    <th class="text-left px-2 py-3 text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">Status</th>
                    <th class="text-right px-4 py-3 text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">Total</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-border/60">
                  <tr
                    v-for="order in pagedOrders" :key="order.id"
                    class="hover:bg-muted/40 transition-colors cursor-pointer"
                    :class="{ 'bg-primary/5': selectedOrders.includes(order.id) }"
                  >
                    <td class="px-4 py-3">
                      <input
                        type="checkbox"
                        class="orders-cb"
                        :checked="selectedOrders.includes(order.id)"
                        @change="toggleOrder(order.id)"
                      />
                    </td>
                    <td class="px-2 py-3">
                      <span class="text-[11px] font-semibold text-foreground font-mono">{{ order.orderNumber }}</span>
                    </td>
                    <td class="px-2 py-3">
                      <div class="flex items-center gap-1.5">
                        <div class="w-5 h-5 rounded-full bg-primary/10 flex items-center justify-center text-[9px] font-bold text-primary shrink-0">
                          {{ (order.customer || '-')[0].toUpperCase() }}
                        </div>
                        <span class="text-[11px] text-foreground truncate max-w-[55px]">{{ order.customer }}</span>
                      </div>
                    </td>
                    <td class="px-2 py-3">
                      <span
                        class="inline-flex items-center text-[9px] font-semibold px-1.5 py-0.5 rounded-md"
                        :class="orderStatusStyle[order.status] || orderStatusStyle.pending"
                      >
                        {{ order.statusLabel }}
                      </span>
                    </td>
                    <td class="px-4 py-3 text-right">
                      <span class="text-[11px] font-bold text-foreground">{{ order.total }}</span>
                    </td>
                  </tr>

                  <!-- Empty state -->
                  <tr v-if="pagedOrders.length === 0">
                    <td colspan="5" class="px-4 py-10 text-center">
                      <div class="flex flex-col items-center gap-2">
                        <ShoppingBag class="w-7 h-7 text-muted-foreground/20" />
                        <span class="text-xs text-muted-foreground">Belum ada order</span>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Pagination -->
            <div class="flex items-center justify-between px-4 py-3 border-t border-border">
              <div class="flex items-center gap-1.5">
                <button
                  @click="orderPage = Math.max(1, orderPage - 1)"
                  :disabled="orderPage === 1"
                  class="w-7 h-7 rounded-md flex items-center justify-center border border-border text-muted-foreground hover:text-foreground hover:bg-muted disabled:opacity-30 disabled:cursor-not-allowed transition-all"
                >
                  <ChevronLeft class="w-3.5 h-3.5" />
                </button>
                <button
                  v-for="p in totalOrderPages" :key="p"
                  @click="orderPage = p"
                  class="w-7 h-7 rounded-md text-xs font-semibold transition-all"
                  :class="orderPage === p
                    ? 'bg-primary text-primary-foreground shadow-sm'
                    : 'text-muted-foreground hover:text-foreground hover:bg-muted border border-border'"
                >{{ p }}</button>
                <button
                  @click="orderPage = Math.min(totalOrderPages, orderPage + 1)"
                  :disabled="orderPage === totalOrderPages"
                  class="w-7 h-7 rounded-md flex items-center justify-center border border-border text-muted-foreground hover:text-foreground hover:bg-muted disabled:opacity-30 disabled:cursor-not-allowed transition-all"
                >
                  <ChevronRight class="w-3.5 h-3.5" />
                </button>
              </div>
              <div class="flex items-center gap-1 text-[11px] text-muted-foreground">
                <span>{{ ordersPerPage }} / page</span>
              </div>
            </div>

          </div>
        </div>
      </div>

    </div>
  </AppLayout>
</template>

<style scoped>
/* Transition animations for cards */
.metric-card,
.secondary-card {
  transition: transform 0.2s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.2s cubic-bezier(0.16, 1, 0.3, 1), background-color 0.2s ease;
}

.metric-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.dark .metric-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
  background-color: hsl(var(--muted) / 0.3);
}

.secondary-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.dark .secondary-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
}

/* Orders table checkbox */
.orders-cb {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  accent-color: hsl(var(--primary));
  cursor: pointer;
}

/* Chart wrapper */
.chart-wrapper {
  position: relative;
  width: 100%;
}

/* Animate in */
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}

.dashboard-root > * {
  animation: fade-in-up 0.3s cubic-bezier(0.16, 1, 0.3, 1) both;
}
.dashboard-root > *:nth-child(1) { animation-delay: 0ms; }
.dashboard-root > *:nth-child(2) { animation-delay: 40ms; }
.dashboard-root > *:nth-child(3) { animation-delay: 80ms; }
.dashboard-root > *:nth-child(4) { animation-delay: 120ms; }
</style>