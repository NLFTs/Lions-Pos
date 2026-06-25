<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import api from '@/lib/api'
import { 
  Package, 
  Warehouse, 
  Building2, 
  Loader2, 
  ArrowUpRight,
  TrendingUp,
  TrendingDown,
  AlertTriangle,
  History,
  Trash2,
  CheckCircle2,
  X,
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
const { can } = usePermission()
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
const categories = computed(() => {
  if (!balances.value.length) return []

  return [
    ...new Set(
      balances.value
        .map((b) => b?.product?.categoryName ?? '-')
        .filter((c) => c && c !== '-' && c.trim() !== '')
    )
  ]
})
const locations = ref([])
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

// Filter state
const locationFilter = ref('all')
const typeFilter = ref('all')
const categoryFilter = ref('all')

// ─── Detektor Lokasi Kebal (SNAKE & CAMEL CASE) ──────────────────────────────
function getLocationName(type, id) {
  if (!type && !id) return '-'
  
  const targetType = String(type || '').toLowerCase().trim()
  const targetId = Number(id)

  const loc = locations.value.find(l => {
    const locType = String(l.type || '').toLowerCase().trim()
    return locType === targetType && Number(l.id) === targetId
  })

  return loc ? loc.name : `Lokasi #${targetId} (${type})`
}

// Resolved Page Title
const dashboardTitle = computed(() => {
  const type = route.query.locationType
  const id = route.query.locationId
  if (type && id) {
    return `Dashboard Inventaris - ${getLocationName(type, id)}`
  }
  return 'Dashboard Inventaris'
})

// ─── Computed: Filter & Paginate Table (BYPASS FILTER SENSITIF KAKU) ─────────
const filteredBalances = computed(() => {
  let r = balances.value.filter((b) => {
    const type = String(b.locationType || b.location_type || '').toUpperCase().trim()
    return !(type === 'QUARANTINE' && Number(b.qty || 0) <= 0)
  })

  if (locationFilter.value !== 'all') {
    r = r.filter((b) => {
      const typeMentah = String(b.locationType || b.location_type || '').toUpperCase().trim()
      return typeMentah === locationFilter.value
    })
  }

  if (typeFilter.value !== 'all') {
    const [filterType, filterId] = typeFilter.value.split('-')
    
    r = r.filter((b) => {
      const currentType = String(b.locationType || b.location_type || '').toUpperCase().trim()
      const currentId = Number(b.locationId || b.location_id)
      
      return currentType === filterType && currentId === Number(filterId)
    })
  }

  if (categoryFilter.value !== 'all') {
    r = r.filter((b) => b.product?.categoryName === categoryFilter.value)
  }

  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase().trim()
    r = r.filter((b) =>
      b.product?.name?.toLowerCase().includes(q) ||
      b.product?.sku?.toLowerCase().includes(q)
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

const currentBranchId = computed(() => authStore.user?.branch?.id || authStore.user?.branchId || null)
const isBranchManager = computed(() => !!currentBranchId.value)
const isOwner = computed(() => authStore.isOwner || (!authStore.isAdmin && !!authStore.user?.partnerId && !authStore.user?.branchId))
const canUpdateStock = computed(() => authStore.isAdmin || can('stock_balance.update'))

// ─── Actions: Fetch Data ─────────────────────────────────────────────────────
async function fetchDashboardData() {
  loading.value = true
  
  let type = route.query.locationType || (isBranchManager.value ? 'BRANCH' : null)
  let id = route.query.locationId || (isBranchManager.value ? currentBranchId.value : null)
  
  try {
    const statsParams = {}
    if (type && id) {
      statsParams.locationType = type
      statsParams.locationId = id
    }
    try {
      const resStats = await api.get('/api/v1/stock-balances/stats', { params: statsParams })
      stats.value = resStats.data?.data || { total_products: 0, damaged_products: 10, incoming_products: 0, outgoing_products: 0, chart_data: [] }
    } catch (err) {
      console.warn('[Inventory] Gagal memuat statistik lokasi, lanjut memuat tabel stok.', err.response?.data || err.message)
      stats.value = { total_products: 0, damaged_products: 10, incoming_products: 0, outgoing_products: 0, chart_data: [] }
    }
    
    const url = isAdmin.value ? '/api/v1/stock-balances/admin' : '/api/v1/stock-balances'
    
    const requests = []

    // 1. Ambil params Utama (Cabang Login)
    const primaryParams = { page: 0, size: 200 } // Ambil data agak banyak agar gabungan list aman
    if (type && id) {
      primaryParams.locationType = String(type).toUpperCase()
      primaryParams.locationId = Number(id)
    } else if (isBranchManager.value && currentBranchId.value) {
      primaryParams.locationType = 'BRANCH'
      primaryParams.locationId = Number(currentBranchId.value)
    }
    requests.push(api.get(url, { params: primaryParams }))

    const branchIdForQuarantine = type && id && String(type).toUpperCase() === 'BRANCH'
      ? Number(id)
      : (isBranchManager.value && currentBranchId.value ? Number(currentBranchId.value) : null)

    if (branchIdForQuarantine) {
      requests.push(api.get(url, {
        params: {
          page: 0,
          size: 200,
          locationType: 'QUARANTINE',
          locationId: branchIdForQuarantine
        }
      }))
    }

    // 🔥 FIX 100%: Karena endpoint /warehouses di fetchLocations() sudah memfilter branchId dari backend, 
    // maka semua data bertipe 'warehouse' yang masuk array locations sudah pasti terikat dengan cabang ini!
    if (isBranchManager.value && currentBranchId.value && (!type || type === 'BRANCH')) {
      const relatedWarehouses = locations.value.filter(l => l.type === 'warehouse')

      relatedWarehouses.forEach(wh => {
        requests.push(
          api.get(url, {
            params: {
              page: 0,
              size: 200,
              locationType: 'WAREHOUSE',
              locationId: Number(wh.id)
            }
          })
        )
      });
    }

    const responses = await Promise.allSettled(requests)
    
    let allRawBalances = []
    responses.forEach(result => {
      if (result.status !== 'fulfilled') {
        console.warn('[Inventory] Salah satu request stok gagal:', result.reason?.response?.data || result.reason?.message)
        return
      }

      let raw = result.value.data?.data || result.value.data
      if (raw && typeof raw === 'object' && 'content' in raw) {
        raw = raw.content
      }
      if (Array.isArray(raw)) {
        allRawBalances = [...allRawBalances, ...raw]
      }
    })

    // 2. Eliminasi Duplikat ID & Normalisasi Atribut
    const uniqueMap = new Map()
    allRawBalances.forEach(b => {
      if (!b || !b.id) return
      const detectedType = String(b.locationType || b.location_type || 'BRANCH').toUpperCase()
      const detectedLocId = b.locationId || b.location_id
      
      uniqueMap.set(b.id, {
        ...b,
        id: b.id,
        locationType: detectedType,
        location_type: detectedType,
        locationId: detectedLocId,
        location_id: detectedLocId,
        qty: b.qty ?? 0,
        updatedAt: b.updatedAt || b.updated_at,
        updated_at: b.updatedAt || b.updated_at,
        updatedBy: b.updatedBy || b.updated_by,
        updated_by: b.updatedBy || b.updated_by
      })
    })

    balances.value = Array.from(uniqueMap.values())
    console.log("🔥 [DUAL STOK SAKTI] Data hasil gabungan Cabang + Gudang Terikat:", balances.value)
  } catch (err) {
    toast.error('Gagal memuat data dashboard inventory.')
    console.error(err)
  } finally {
    loading.value = false
  }
}

async function fetchLocations() {
  try {
    const urlBranches = isAdmin.value
      ? '/api/v1/branches/admin' 
      : '/api/v1/branches'
    
    const urlWarehouses = isAdmin.value
      ? '/api/v1/warehouses/admin' 
      : '/api/v1/warehouses'
    
    const warehouseParams = {}
    if (!isAdmin.value && !isOwner.value && currentBranchId.value) {
      warehouseParams.branchId = Number(currentBranchId.value)
    }

    const [resBr, resWh] = await Promise.all([
      api.get(urlBranches), 
      api.get(urlWarehouses, { params: warehouseParams }) 
    ])
    
    const brRaw = isAdmin.value ? resBr.data : (resBr.data?.data || [])
    const brArr = Array.isArray(brRaw) ? brRaw : (brRaw?.content || [])
    
    let whRaw = resWh.data?.data || resWh.data
    let whArr = []
    if (whRaw && typeof whRaw === 'object' && 'content' in whRaw) {
      whArr = whRaw.content
    } else if (Array.isArray(whRaw)) {
      whArr = whRaw
    } else if (whRaw && Array.isArray(whRaw.data)) {
      whArr = whRaw.data
    }
    
    const formalBranches = (isAdmin.value || isOwner.value || !currentBranchId.value)
      ? brArr 
      : brArr.filter(b => Number(b.id) === Number(currentBranchId.value))

    locations.value = [
      ...formalBranches.map(x => ({ ...x, type: 'branch' })),
      ...whArr.map(x => ({ ...x, type: 'warehouse' }))
    ]
    console.log('Master Locations Berhasil Di-load:', locations.value)
  } catch (err) {
    console.error('Failed to load branches and warehouses for mapping', err)
  }
}

watch(
  () => [route.query.locationType, route.query.locationId],
  () => {
    page.value = 1
    locationFilter.value = 'all'
    typeFilter.value = 'all'
    fetchDashboardData()
  }
)

// DISPOSE KARANTINA 
const showDisposeModal = ref(false)
const disposeTarget = ref(null)
const disposeQty = ref('')
const disposeNotes = ref('')
const disposeSaving = ref(false)

function openDispose(balance) {
  disposeTarget.value = balance
  disposeQty.value = balance.qty?.toString() || ''
  disposeNotes.value = ''
  showDisposeModal.value = true
}

async function submitDispose() {
  if (!disposeTarget.value) return
  const qty = Number(disposeQty.value)
  if (!qty || qty <= 0) { toast.error('Jumlah dispose harus lebih dari 0.'); return }
  if (qty > disposeTarget.value.qty) { toast.error(`Maksimal qty dispose: ${disposeTarget.value.qty}.`); return }
  disposeSaving.value = true
  try {
    await api.post(`/api/v1/stock-balances/${disposeTarget.value.id}/dispose`, null, {
      params: { qty, notes: disposeNotes.value?.trim() || null }
    })
    toast.success(`${qty} pcs berhasil di-dispose ke supplier.`)
    showDisposeModal.value = false
    fetchDashboardData()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal melakukan dispose.')
  } finally {
    disposeSaving.value = false
  }
}

// APPROVE KARANTINA -> STOK AKTIF CABANG
const showApproveModal = ref(false)
const approveTarget = ref(null)
const approveQty = ref('')
const approveNotes = ref('')
const approveSaving = ref(false)

function openApprove(balance) {
  approveTarget.value = balance
  approveQty.value = balance.qty?.toString() || ''
  approveNotes.value = ''
  showApproveModal.value = true
}

async function submitApprove() {
  if (!approveTarget.value) return
  const qty = Number(approveQty.value)
  if (!qty || qty <= 0) { toast.error('Jumlah approve harus lebih dari 0.'); return }
  if (qty > approveTarget.value.qty) { toast.error(`Maksimal qty approve: ${approveTarget.value.qty}.`); return }
  approveSaving.value = true
  try {
    await api.post(`/api/v1/stock-balances/${approveTarget.value.id}/approve`, null, {
      params: { qty, notes: approveNotes.value?.trim() || null }
    })
    toast.success(`${qty} pcs berhasil masuk ke stok aktif cabang.`)
    showApproveModal.value = false
    fetchDashboardData()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal approve stok karantina.')
  } finally {
    approveSaving.value = false
  }
}

onMounted(async () => {
  // 🔥 LIFECYCLE MURNI: Ambil master data kelar dulu, baru panggil stoknya
  await fetchLocations()
  await fetchDashboardData()
})
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <div class="flex flex-col gap-2">
        <h1 class="text-2xl font-bold tracking-tight text-foreground">{{ dashboardTitle }}</h1>
        <p class="text-xs text-muted-foreground">Ringkasan stok, grafik keluar masuk barang, dan kondisi stok realtime.</p>
      </div>

      <div v-if="loading && balances.length === 0" class="flex flex-col items-center justify-center py-32 gap-3">
        <Loader2 class="h-8 w-8 animate-spin text-primary/50" />
        <p class="text-xs text-muted-foreground italic">Menghitung statistik inventaris realtime...</p>
      </div>

      <template v-else>
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-3">
          <div class="relative overflow-hidden rounded-xl border border-zinc-200 dark:border-zinc-800 bg-card p-4 group hover:shadow-md transition-all duration-300">
            <div class="absolute inset-0 opacity-5 dark:opacity-10 group-hover:opacity-10 dark:group-hover:opacity-20 bg-gradient-to-br from-blue-500 via-transparent to-transparent rounded-xl" />
            <div class="relative flex flex-col justify-between h-full">
              <div class="flex items-center justify-between mb-3">
                <span class="text-[11px] font-semibold text-zinc-500 dark:text-zinc-400">Total Semua Produk</span>
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

          <div class="relative overflow-hidden rounded-xl border border-zinc-200 dark:border-zinc-800 bg-card p-4 group hover:shadow-md transition-all duration-300">
            <div class="absolute inset-0 opacity-5 dark:opacity-10 group-hover:opacity-10 dark:group-hover:opacity-20 bg-gradient-to-br from-red-500 via-transparent to-transparent rounded-xl" />
            <div class="relative flex flex-col justify-between h-full">
              <div class="flex items-center justify-between mb-3">
                <span class="text-[11px] font-semibold text-zinc-500 dark:text-zinc-400">Produk Tidak Layak Jual</span>
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

          <div class="relative overflow-hidden rounded-xl border border-zinc-200 dark:border-zinc-800 bg-card p-4 group hover:shadow-md transition-all duration-300">
            <div class="absolute inset-0 opacity-5 dark:opacity-10 group-hover:opacity-10 dark:group-hover:opacity-20 bg-gradient-to-br from-emerald-500 via-transparent to-transparent rounded-xl" />
            <div class="relative flex flex-col justify-between h-full">
              <div class="flex items-center justify-between mb-3">
                <span class="text-[11px] font-semibold text-zinc-500 dark:text-zinc-400">Produk Masuk</span>
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

          <div class="relative overflow-hidden rounded-xl border border-zinc-200 dark:border-zinc-800 bg-card p-4 group hover:shadow-md transition-all duration-300">
            <div class="absolute inset-0 opacity-5 dark:opacity-10 group-hover:opacity-10 dark:group-hover:opacity-20 bg-gradient-to-br from-rose-500 via-transparent to-transparent rounded-xl" />
            <div class="relative flex flex-col justify-between h-full">
              <div class="flex items-center justify-between mb-3">
                <span class="text-[11px] font-semibold text-zinc-500 dark:text-zinc-400">Produk Keluar</span>
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

        <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
          <CardContent class="p-5">
            <div class="flex items-center justify-between mb-4">
              <div>
                <h2 class="text-sm font-bold text-foreground">Aktivitas Mutasi Barang (15 Hari Terakhir)</h2>
                <p class="text-[10px] text-muted-foreground mt-0.5">Perbandingan grafik masuk vs keluar</p>
              </div>
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

            <div class="w-full relative" style="height: 250px;">
              <Line :data="chartData" :options="chartOptions" />
            </div>
          </CardContent>
        </Card>

        <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
          <CardContent class="p-0">
            <div class="p-4 flex flex-wrap items-center gap-3 justify-between border-b border-zinc-100 dark:border-zinc-800">
              <div class="w-64">
                <DataTableSearch v-model="searchQuery" placeholder="Cari nama produk atau SKU..." />
              </div>
              <div class="flex items-center gap-2 flex-wrap">
                <select
                  v-model="locationFilter"
                  class="h-9 px-3 text-xs font-semibold border border-zinc-200 dark:border-zinc-800 bg-background rounded-md focus:outline-none focus:ring-2 focus:ring-primary/40 text-zinc-700 dark:text-zinc-300 transition-colors hover:bg-zinc-50 dark:hover:bg-zinc-900"
                >
                  <option value="all">Semua Tipe</option>
                  <option value="BRANCH">Cabang</option>
                  <option value="WAREHOUSE">Gudang</option>
                  <option value="QUARANTINE">Karantina</option>
                </select>

                <select
                  v-model="typeFilter"
                  class="h-9 px-3 text-xs font-semibold border border-zinc-200 dark:border-zinc-800 bg-background rounded-md focus:outline-none focus:ring-2 focus:ring-primary/40 text-zinc-700 dark:text-zinc-300 transition-colors hover:bg-zinc-50 dark:hover:bg-zinc-900"
                >
                  <option value="all">Semua Lokasi</option>
                  
                  <optgroup label="Cabang" v-if="locations.filter(l => l.type === 'branch').length">
                    <option v-for="loc in locations.filter(l => l.type === 'branch')" :key="'br-' + loc.id" :value="'BRANCH-' + loc.id">
                      {{ loc.name }}
                    </option>
                  </optgroup>
                  
                  <optgroup label="Gudang" v-if="locations.filter(l => l.type === 'warehouse').length">
                    <option v-for="loc in locations.filter(l => l.type === 'warehouse')" :key="'wh-' + loc.id" :value="'WAREHOUSE-' + loc.id">
                      {{ loc.name }}
                    </option>
                  </optgroup>
                </select>
                <select
                  v-model="categoryFilter"
                  class="h-9 px-3 text-xs font-semibold border border-zinc-200 dark:border-zinc-800 bg-background rounded-md focus:outline-none focus:ring-2 focus:ring-primary/40 text-zinc-700 dark:text-zinc-300 transition-colors hover:bg-zinc-50 dark:hover:bg-zinc-900"
                >
                  <option value="all">Semua Kategori</option>
                  <option v-for="category in categories" :key="category" :value="category">
                    {{ category }}
                  </option>
                </select>
                <button @click="fetchDashboardData" class="flex items-center gap-2 h-9 px-3 text-xs font-semibold border border-zinc-200 dark:border-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-900 rounded-md transition-colors">
                  <History class="w-3.5 h-3.5" /> Segarkan
                </button>
              </div>
            </div>
              
            <div v-if="filteredBalances.length === 0" class="flex flex-col items-center justify-center py-16 text-muted-foreground">
              <div class="w-12 h-12 rounded-full bg-zinc-100 dark:bg-zinc-900 flex items-center justify-center mb-3">
                <Package class="h-6 w-6 opacity-40" />
              </div>
              <p class="text-xs font-medium">Stok tidak ditemukan.</p>
            </div>

            <div v-else>
              <div class="overflow-x-auto">
                <table class="w-full text-xs">
                  <thead>
                    <tr class="border-b border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30 text-zinc-500">
                      <th class="pl-5 py-3 text-left font-semibold uppercase tracking-wider">Produk</th>
                      <th class="pl-5 py-3 text-left font-semibold uppercase tracking-wider">Kategori</th>
                      <th class="py-3 text-left font-semibold uppercase tracking-wider">Lokasi</th>
                      <th class="py-3 text-center font-semibold uppercase tracking-wider">Stok</th>
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
                      <td class="pl-5 py-3.5 align-middle">
                        <Badge variant="outline">
                          {{ b.product?.categoryName || '-' }}
                        </Badge>
                      </td>
                      <td class="py-3.5">
                        <div class="flex flex-col gap-0.5">
                          <div class="flex items-center gap-1 font-semibold"
                            :class="(b.locationType || b.location_type) === 'QUARANTINE' ? 'text-red-600 dark:text-red-400' : 'text-zinc-700 dark:text-zinc-300'">
                            <AlertTriangle v-if="(b.locationType || b.location_type) === 'QUARANTINE'" class="h-3 w-3 opacity-70" />
                            <Warehouse v-else-if="(b.locationType || b.location_type) === 'WAREHOUSE'" class="h-3 w-3 opacity-55" />
                            <Building2 v-else class="h-3 w-3 opacity-55" />
                            
                            <span>
                              {{ (b.locationType || b.location_type) === 'QUARANTINE' ? 'Karantina' : getLocationName(b.locationType || b.location_type, b.locationId || b.location_id) }}
                            </span>
                          </div>
                          <span class="text-[8px] uppercase font-bold tracking-widest text-zinc-400">
                            {{ b.locationType || b.location_type }}
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
                          {{ b.updatedAt || b.updated_at ? new Date(b.updatedAt || b.updated_at).toLocaleString('id-ID', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }) : '-' }}
                        </div>
                        <div v-if="b.updatedBy || b.updated_by" class="text-[9px] text-muted-foreground mt-0.5">
                          Oleh: {{ (b.updatedBy || b.updated_by).username }}
                        </div>
                      </td>
                      <td class="pr-5 py-3.5 text-right">
                        <div v-if="(b.locationType || b.location_type) === 'QUARANTINE'" class="flex justify-end gap-1.5">
                          <Button
                            v-if="canUpdateStock"
                            variant="ghost"
                            size="sm"
                            class="h-7 px-2.5 text-[11px] font-bold text-emerald-600 hover:text-emerald-700 hover:bg-emerald-50 dark:hover:bg-emerald-900/20 gap-1.5"
                            @click="openApprove(b)">
                            <CheckCircle2 class="h-3.5 w-3.5" />
                            Approve
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            class="h-7 px-2.5 text-[11px] font-bold text-red-500 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-900/20 gap-1.5"
                            @click="openDispose(b)">
                            <Trash2 class="h-3.5 w-3.5" />
                            Dispose
                          </Button>
                        </div>
                        <Button v-else variant="ghost" size="icon" class="h-7 w-7 text-zinc-400 hover:text-primary transition-colors">
                          <ArrowUpRight class="h-3.5 w-3.5" />
                        </Button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

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

    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="showApproveModal"
          class="fixed inset-0 z-[60] bg-black/50 backdrop-blur-sm"
          @click="showApproveModal = false"
        />
      </Transition>
      <Transition name="scale">
        <div
          v-if="showApproveModal && approveTarget"
          class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none"
        >
          <div class="bg-white dark:bg-zinc-900 rounded-2xl shadow-2xl w-full max-w-sm border border-emerald-200 dark:border-emerald-800/40 pointer-events-auto overflow-hidden">
            <div class="flex items-center gap-3 px-6 py-4 border-b border-emerald-100 dark:border-emerald-900/40 bg-emerald-50/50 dark:bg-emerald-950/20">
              <div class="w-9 h-9 rounded-xl bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center shrink-0">
                <CheckCircle2 class="h-5 w-5 text-emerald-600 dark:text-emerald-400" />
              </div>
              <div>
                <h3 class="font-black text-[15px] text-zinc-900 dark:text-zinc-100">Approve ke Kasir</h3>
                <p class="text-[11px] text-muted-foreground mt-0.5">Pindahkan stok karantina ke saldo aktif cabang</p>
              </div>
              <button @click="showApproveModal = false" class="ml-auto p-1.5 rounded-full hover:bg-emerald-100 dark:hover:bg-emerald-900/30 text-zinc-400">
                <X class="h-4 w-4" />
              </button>
            </div>

            <div class="px-6 py-5 space-y-4">
              <div class="p-3 rounded-xl bg-zinc-50 dark:bg-zinc-800/50 border border-zinc-100 dark:border-zinc-800">
                <p class="text-[10px] font-bold text-zinc-400 uppercase tracking-wider mb-1">Produk</p>
                <p class="text-sm font-bold text-zinc-900 dark:text-zinc-100">{{ approveTarget.product?.name }}</p>
                <p class="text-[11px] font-mono text-muted-foreground">{{ approveTarget.product?.sku }}</p>
                <div class="flex items-center justify-between mt-2 pt-2 border-t border-zinc-100 dark:border-zinc-800">
                  <span class="text-[11px] text-zinc-500">Stok Karantina</span>
                  <span class="text-sm font-black text-emerald-600 dark:text-emerald-400">{{ approveTarget.qty }} pcs</span>
                </div>
                <div class="flex items-center justify-between mt-1">
                  <span class="text-[11px] text-zinc-500">Tujuan Cabang</span>
                  <span class="text-[11px] font-bold text-zinc-700 dark:text-zinc-300">{{ getLocationName('branch', approveTarget.locationId || approveTarget.location_id) }}</span>
                </div>
              </div>

              <div class="space-y-1.5">
                <label class="text-[11px] font-bold text-zinc-500 uppercase tracking-wider">Jumlah Approve</label>
                <div class="relative">
                  <input
                    v-model="approveQty"
                    type="number"
                    min="1"
                    :max="approveTarget.qty"
                    placeholder="0"
                    class="w-full h-11 rounded-xl border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 px-3 pr-12 text-base font-bold outline-none focus:ring-2 focus:ring-emerald-400/30"
                  />
                  <span class="absolute right-3 top-1/2 -translate-y-1/2 text-[11px] font-bold text-zinc-400">PCS</span>
                </div>
                <button
                  type="button"
                  @click="approveQty = approveTarget.qty.toString()"
                  class="text-[11px] font-semibold text-primary hover:underline"
                >
                  Approve semua ({{ approveTarget.qty }} pcs)
                </button>
              </div>

              <div class="space-y-1.5">
                <label class="text-[11px] font-bold text-zinc-500 uppercase tracking-wider">Catatan</label>
                <input
                  v-model="approveNotes"
                  placeholder="cth: barang sudah dicek dan siap jual"
                  class="w-full h-9 rounded-xl border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 px-3 text-sm outline-none focus:ring-2 focus:ring-emerald-400/30"
                />
              </div>
            </div>

            <div class="px-6 pb-5 flex gap-2.5">
              <button
                @click="showApproveModal = false"
                :disabled="approveSaving"
                class="flex-1 h-11 rounded-xl border border-zinc-200 dark:border-zinc-700 font-bold text-sm hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors disabled:opacity-50"
              >
                Batal
              </button>
              <button
                @click="submitApprove"
                :disabled="approveSaving"
                class="flex-1 h-11 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm flex items-center justify-center gap-2 transition-colors disabled:opacity-50"
              >
                <Loader2 v-if="approveSaving" class="h-4 w-4 animate-spin" />
                <CheckCircle2 v-else class="h-4 w-4" />
                Setujui
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>
