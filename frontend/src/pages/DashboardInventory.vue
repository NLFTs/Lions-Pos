<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { useToast } from '@/composables/useToast'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import api from '@/lib/api'
import { 
  Package, 
  Warehouse, 
  Building2, 
  Search, 
  Loader2, 
  ArrowUpRight,
  TrendingUp,
  TrendingDown,
  AlertTriangle,
  History
} from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'
import { Button } from '@/components/ui/button'
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

const route = useRoute()
const { toast } = useToast()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const isAdmin = computed(() => authStore.isAdmin)

// ─── State ───────────────────────────────────────────────────────────────────
const loading = ref(true)
const stats = ref({
  total_products: 0,
  damaged_products: 10,
  incoming_products: 0,
  outgoing_products: 0,
  chart_data: []
})
const balances = ref([])
const locations = ref([])
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

// Resolved Page Title
const dashboardTitle = computed(() => {
  const type = route.query.locationType
  const id = route.query.locationId
  if (type && id) {
    return `Dashboard Inventory - ${getLocationName(type, id)}`
  }
  return 'Dashboard Inventory'
})

// ─── Computed: Filter & Paginate Table ──────────────────────────────────────
const filteredBalances = computed(() => {
  let r = balances.value
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    r = r.filter(b => 
      (b.product?.name && b.product.name.toLowerCase().includes(q)) ||
      (b.product?.sku && b.product.sku.toLowerCase().includes(q))
    )
  }
  return r
})

const paginatedBalances = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredBalances.value.slice(start, start + pageSize.value)
})

// ─── Computed: Chart Setup ──────────────────────────────────────────────────
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
  
  return {
    primaryBorder: `hsl(${h}, ${s}, ${l})`,
    primaryFillStart: `hsla(${h}, ${s}, ${l}, 0.25)`,
    primaryFillEnd: `hsla(${h}, ${s}, ${l}, 0.0)`,
    primaryHoverBg: `hsl(${h}, ${s}, ${l})`,
    outgoingBorder: '#ef4444',
    outgoingFillStart: 'rgba(239, 68, 68, 0.15)',
    outgoingFillEnd: 'rgba(239, 68, 68, 0.0)'
  }
})

const chartLabels = computed(() => {
  return stats.value.chart_data.map(item => item.date)
})

const chartData = computed(() => ({
  labels: chartLabels.value,
  datasets: [
    {
      label: 'Produk Masuk',
      data: stats.value.chart_data.map(item => item.incoming),
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
      tension: 0.4,
      pointRadius: 2,
      pointHoverRadius: 6,
      pointHoverBackgroundColor: chartColors.value.primaryHoverBg,
      pointHoverBorderColor: themeStore.isDark ? '#000' : '#fff',
      pointHoverBorderWidth: 2,
    },
    {
      label: 'Produk Keluar',
      data: stats.value.chart_data.map(item => item.outgoing),
      fill: true,
      backgroundColor: (ctx) => {
        const canvas = ctx.chart.ctx
        const gradient = canvas.createLinearGradient(0, 0, 0, 280)
        gradient.addColorStop(0, chartColors.value.outgoingFillStart)
        gradient.addColorStop(1, chartColors.value.outgoingFillEnd)
        return gradient
      },
      borderColor: chartColors.value.outgoingBorder,
      borderWidth: 2,
      tension: 0.4,
      pointRadius: 2,
      pointHoverRadius: 5,
      pointHoverBackgroundColor: '#ef4444',
      pointHoverBorderColor: themeStore.isDark ? '#000' : '#fff',
      pointHoverBorderWidth: 2,
    }
  ]
}))

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  interaction: { mode: 'index', intersect: false },
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: themeStore.isDark ? 'rgba(15, 15, 20, 0.95)' : 'rgba(255, 255, 255, 0.95)',
      borderColor: themeStore.isDark ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.08)',
      borderWidth: 1,
      titleColor: themeStore.isDark ? '#94a3b8' : '#64748b',
      bodyColor: themeStore.isDark ? '#f1f5f9' : '#0f172a',
      padding: 10,
      cornerRadius: 8,
      titleFont: { size: 11 },
      bodyFont: { size: 12, weight: 'bold' }
    }
  },
  scales: {
    x: {
      grid: { display: false },
      ticks: {
        color: themeStore.isDark ? 'rgba(148, 163, 184, 0.6)' : 'rgba(100, 116, 139, 0.7)',
        font: { size: 10 },
        maxTicksLimit: 8,
      }
    },
    y: {
      grid: {
        color: themeStore.isDark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)',
      },
      ticks: {
        color: themeStore.isDark ? 'rgba(148, 163, 184, 0.6)' : 'rgba(100, 116, 139, 0.7)',
        font: { size: 10 },
      }
    }
  }
}))

// ─── Actions: Fetch Data ─────────────────────────────────────────────────────
async function fetchDashboardData() {
  loading.value = true
  const type = route.query.locationType
  const id = route.query.locationId
  
  try {
    // 1. Fetch Stats & Chart
    const statsParams = {}
    if (type && id) {
      statsParams.locationType = type
      statsParams.locationId = id
    }
    const resStats = await api.get('/api/v1/stock-balances/stats', { params: statsParams })
    stats.value = resStats.data?.data || { total_products: 0, damaged_products: 10, incoming_products: 0, outgoing_products: 0, chart_data: [] }
    
    // 2. Fetch Stock Balances List
    let resBalances
    if (type && id) {
      resBalances = await api.get('/api/v1/stock-balances/location', { params: { locationType: type, locationId: id } })
    } else {
      const url = isAdmin.value ? '/api/v1/stock-balances/admin' : '/api/v1/stock-balances'
      resBalances = await api.get(url)
    }

    let rawBalances
    if (isAdmin.value && !(type && id)) {
      rawBalances = Array.isArray(resBalances.data) ? resBalances.data : (resBalances.data?.data || [])
    } else {
      const dataB = resBalances.data?.data
      rawBalances = Array.isArray(dataB) ? dataB : (dataB?.content || [])
    }

    balances.value = rawBalances.map(b => ({
      ...b,
      locationType: b.locationType || b.location_type,
      locationId: b.locationId || b.location_id,
      updatedAt: b.updatedAt || b.updated_at,
      updatedBy: b.updatedBy || b.updated_by
    }))

  } catch (err) {
    toast.error('Gagal memuat data dashboard inventory.')
    console.error(err)
  } finally {
    loading.value = false
  }
}

async function fetchLocations() {
  try {
    const urlBranches = isAdmin.value ? '/api/v1/branches/admin' : '/api/v1/branches'
    const urlWarehouses = isAdmin.value ? '/api/v1/warehouses/admin' : '/api/v1/warehouses'
    
    const [resBr, resWh] = await Promise.all([
      api.get(urlBranches),
      api.get(urlWarehouses)
    ])
    
    const brRaw = isAdmin.value ? resBr.data : (resBr.data?.data || [])
    const brArr = Array.isArray(brRaw) ? brRaw : (brRaw?.content || [])
    const whRaw = resWh.data?.data
    const whArr = whRaw && !Array.isArray(whRaw) && whRaw.content ? whRaw.content : (Array.isArray(whRaw) ? whRaw : [])
    
    locations.value = [
      ...brArr.map(x => ({ ...x, type: 'branch' })),
      ...whArr.map(x => ({ ...x, type: 'warehouse' }))
    ]
  } catch (err) {
    console.error('Failed to load branches and warehouses for mapping', err)
  }
}

function getLocationName(type, id) {
  if (!type) return `Unknown (#${id})`
  const loc = locations.value.find(l => l.type.toLowerCase() === type.toLowerCase() && l.id === Number(id))
  return loc ? loc.name : `Unknown (${type} #${id})`
}

watch(
  () => [route.query.locationType, route.query.locationId],
  () => {
    page.value = 1
    fetchDashboardData()
  }
)

onMounted(async () => {
  await fetchLocations()
  await fetchDashboardData()
})
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <!-- Header -->
      <div class="flex flex-col gap-2">
        <h1 class="text-2xl font-bold tracking-tight text-foreground">{{ dashboardTitle }}</h1>
        <p class="text-xs text-muted-foreground">Ringkasan stok, grafik keluar masuk barang, dan kondisi saldo stok realtime.</p>
      </div>

      <!-- Loading State -->
      <div v-if="loading && balances.length === 0" class="flex flex-col items-center justify-center py-32 gap-3">
        <Loader2 class="h-8 w-8 animate-spin text-primary/50" />
        <p class="text-xs text-muted-foreground italic">Menghitung statistik inventaris realtime...</p>
      </div>

      <template v-else>
        <!-- 4 Kotak Stats Utama (Realtime dari Java) -->
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-3">
          <!-- Total Semua Produk -->
          <div class="relative overflow-hidden rounded-xl border border-zinc-200 dark:border-zinc-800 bg-card p-4 group hover:shadow-md transition-all duration-300">
            <div class="absolute inset-0 opacity-5 dark:opacity-10 group-hover:opacity-10 dark:group-hover:opacity-20 bg-gradient-to-br from-blue-500 via-transparent to-transparent rounded-xl" />
            <div class="relative flex flex-col justify-between h-full">
              <div class="flex items-center justify-between mb-3">
                <span class="text-[11px] font-semibold text-zinc-500 dark:text-zinc-400">Total Semua Product</span>
                <div class="w-7 h-7 rounded-lg bg-blue-500/10 text-blue-500 flex items-center justify-center">
                  <Package class="w-4 h-4" />
                </div>
              </div>
              <div>
                <p class="text-2xl font-bold text-foreground tracking-tight leading-none">
                  {{ stats.total_products }}
                </p>
                <span class="text-[10px] text-zinc-400 dark:text-zinc-500 mt-1 block">Produk aktif terdaftar</span>
              </div>
            </div>
          </div>

          <!-- Product Rusak (Mock 10) -->
          <div class="relative overflow-hidden rounded-xl border border-zinc-200 dark:border-zinc-800 bg-card p-4 group hover:shadow-md transition-all duration-300">
            <div class="absolute inset-0 opacity-5 dark:opacity-10 group-hover:opacity-10 dark:group-hover:opacity-20 bg-gradient-to-br from-red-500 via-transparent to-transparent rounded-xl" />
            <div class="relative flex flex-col justify-between h-full">
              <div class="flex items-center justify-between mb-3">
                <span class="text-[11px] font-semibold text-zinc-500 dark:text-zinc-400">Product Rusak</span>
                <div class="w-7 h-7 rounded-lg bg-red-500/10 text-red-500 flex items-center justify-center">
                  <AlertTriangle class="w-4 h-4" />
                </div>
              </div>
              <div>
                <p class="text-2xl font-bold text-foreground tracking-tight leading-none">
                  {{ stats.damaged_products }}
                </p>
                <span class="text-[10px] text-zinc-400 dark:text-zinc-500 mt-1 block">Butuh tindak lanjut</span>
              </div>
            </div>
          </div>

          <!-- Product Masuk -->
          <div class="relative overflow-hidden rounded-xl border border-zinc-200 dark:border-zinc-800 bg-card p-4 group hover:shadow-md transition-all duration-300">
            <div class="absolute inset-0 opacity-5 dark:opacity-10 group-hover:opacity-10 dark:group-hover:opacity-20 bg-gradient-to-br from-emerald-500 via-transparent to-transparent rounded-xl" />
            <div class="relative flex flex-col justify-between h-full">
              <div class="flex items-center justify-between mb-3">
                <span class="text-[11px] font-semibold text-zinc-500 dark:text-zinc-400">Product Masuk</span>
                <div class="w-7 h-7 rounded-lg bg-emerald-500/10 text-emerald-500 flex items-center justify-center">
                  <TrendingUp class="w-4 h-4" />
                </div>
              </div>
              <div>
                <p class="text-2xl font-bold text-foreground tracking-tight leading-none">
                  {{ stats.incoming_products }}
                </p>
                <span class="text-[10px] text-zinc-400 dark:text-zinc-500 mt-1 block">Pcs akumulatif masuk</span>
              </div>
            </div>
          </div>

          <!-- Product Keluar -->
          <div class="relative overflow-hidden rounded-xl border border-zinc-200 dark:border-zinc-800 bg-card p-4 group hover:shadow-md transition-all duration-300">
            <div class="absolute inset-0 opacity-5 dark:opacity-10 group-hover:opacity-10 dark:group-hover:opacity-20 bg-gradient-to-br from-rose-500 via-transparent to-transparent rounded-xl" />
            <div class="relative flex flex-col justify-between h-full">
              <div class="flex items-center justify-between mb-3">
                <span class="text-[11px] font-semibold text-zinc-500 dark:text-zinc-400">Product Keluar</span>
                <div class="w-7 h-7 rounded-lg bg-rose-500/10 text-rose-500 flex items-center justify-center">
                  <TrendingDown class="w-4 h-4" />
                </div>
              </div>
              <div>
                <p class="text-2xl font-bold text-foreground tracking-tight leading-none">
                  {{ stats.outgoing_products }}
                </p>
                <span class="text-[10px] text-zinc-400 dark:text-zinc-500 mt-1 block">Pcs akumulatif keluar</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Grafik Realtime dengan Chart.js -->
        <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
          <CardContent class="p-5">
            <div class="flex items-center justify-between mb-4">
              <div>
                <h2 class="text-sm font-bold text-foreground">Aktivitas Mutasi Barang (15 Hari Terakhir)</h2>
                <p class="text-[10px] text-muted-foreground mt-0.5">Perbandingan grafik masuk vs keluar</p>
              </div>
              <!-- Legend manual -->
              <div class="flex items-center gap-4 text-xs font-semibold">
                <div class="flex items-center gap-1.5">
                  <span class="w-2.5 h-2.5 rounded-full" :style="{ backgroundColor: chartColors.primaryBorder }" />
                  <span class="text-zinc-600 dark:text-zinc-400">Masuk</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <span class="w-2.5 h-2.5 rounded-full" :style="{ backgroundColor: chartColors.outgoingBorder }" />
                  <span class="text-zinc-600 dark:text-zinc-400">Keluar</span>
                </div>
              </div>
            </div>

            <!-- Chart wrapper -->
            <div class="w-full relative" style="height: 250px;">
              <Line :data="chartData" :options="chartOptions" />
            </div>
          </CardContent>
        </Card>

        <!-- Tabel Saldo Stok Realtime -->
        <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
          <CardContent class="p-0">
            <!-- Search & Refresh -->
            <div class="p-4 flex items-center gap-4 justify-between border-b border-zinc-100 dark:border-zinc-800">
              <div class="w-72">
                <DataTableSearch v-model="searchQuery" placeholder="Cari nama produk atau SKU..." />
              </div>
              <button @click="fetchDashboardData" class="flex items-center gap-2 h-9 px-3 text-xs font-semibold border border-zinc-200 dark:border-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-900 rounded-md transition-colors">
                <History class="w-3.5 h-3.5" /> Segarkan
              </button>
            </div>

            <!-- Table -->
            <div v-if="filteredBalances.length === 0" class="flex flex-col items-center justify-center py-16 text-muted-foreground">
              <div class="w-12 h-12 rounded-full bg-zinc-100 dark:bg-zinc-900 flex items-center justify-center mb-3">
                <Package class="h-6 w-6 opacity-40" />
              </div>
              <p class="text-xs font-medium">Saldo stok tidak ditemukan.</p>
            </div>

            <div v-else>
              <div class="overflow-x-auto">
                <table class="w-full text-xs">
                  <thead>
                    <tr class="border-b border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30 text-zinc-500">
                      <th class="pl-5 py-3 text-left font-semibold uppercase tracking-wider">Produk</th>
                      <th class="py-3 text-left font-semibold uppercase tracking-wider">Lokasi</th>
                      <th class="py-3 text-center font-semibold uppercase tracking-wider">Saldo Stok</th>
                      <th class="py-3 text-left font-semibold uppercase tracking-wider">Terakhir Diperbarui</th>
                      <th class="pr-5 py-3 text-right"></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr 
                      v-for="b in paginatedBalances" 
                      :key="b.id" 
                      class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
                    >
                      <td class="pl-5 py-3.5">
                        <div class="flex items-center gap-3">
                          <div class="w-8 h-8 rounded-md bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-400 shrink-0 border border-zinc-200/50 dark:border-zinc-800/50">
                            <Package class="h-4 w-4" />
                          </div>
                          <div>
                            <div class="font-bold text-zinc-900 dark:text-zinc-100">{{ b.product?.name }}</div>
                            <div class="text-[9px] font-mono text-muted-foreground">{{ b.product?.sku }}</div>
                          </div>
                        </div>
                      </td>
                      <td class="py-3.5">
                        <div class="flex flex-col gap-0.5">
                          <div class="flex items-center gap-1 font-semibold text-zinc-700 dark:text-zinc-300">
                            <Warehouse v-if="b.locationType === 'WAREHOUSE'" class="h-3 w-3 opacity-55" />
                            <Building2 v-else class="h-3 w-3 opacity-55" />
                            <span>{{ getLocationName(b.locationType, b.locationId) }}</span>
                          </div>
                          <span class="text-[8px] uppercase font-bold tracking-widest text-zinc-400">
                            {{ b.locationType }}
                          </span>
                        </div>
                      </td>
                      <td class="py-3.5 text-center">
                        <div class="inline-flex flex-col items-center">
                          <span class="text-sm font-black" :class="b.qty <= 0 ? 'text-red-500' : 'text-zinc-900 dark:text-zinc-100'">
                            {{ b.qty }}
                          </span>
                          <span class="text-[8px] text-muted-foreground uppercase font-bold tracking-tight">PCS</span>
                        </div>
                      </td>
                      <td class="py-3.5">
                        <div class="text-zinc-500">
                          {{ b.updatedAt ? new Date(b.updatedAt).toLocaleString('id-ID', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }) : '-' }}
                        </div>
                        <div v-if="b.updatedBy" class="text-[9px] text-muted-foreground mt-0.5">Oleh: {{ b.updatedBy.username }}</div>
                      </td>
                      <td class="pr-5 py-3.5 text-right">
                        <Button variant="ghost" size="icon" class="h-7 w-7 text-zinc-400 hover:text-primary transition-colors">
                          <ArrowUpRight class="h-3.5 w-3.5" />
                        </Button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <!-- Pagination -->
              <DataTablePagination 
                v-if="filteredBalances.length > 0" 
                :page="page" 
                :page-size="pageSize" 
                :total="filteredBalances.length" 
                @update:page="page = $event" 
                @update:page-size="pageSize = $event; page = 1" 
              />
            </div>
          </CardContent>
        </Card>
      </template>
    </div>
  </AppLayout>
</template>
