<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import AppLayout from '@/components/AppLayout.vue'
import api from '@/lib/api'
import { jsPDF } from 'jspdf'
import autoTable from 'jspdf-autotable' // 🌟 Baris impor tabel yang bersih
import { 
  TrendingUp, 
  TrendingDown, 
  Calendar,
  Download,
  BarChart3,
  Table,
  FileText as FileIcon,
  ShoppingBag,
  Trophy,
  ArrowUpRight,
  Loader2
} from 'lucide-vue-next'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'
import { Line, Bar } from 'vue-chartjs'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler
)

// --- Filter State ---
const activeFilter = ref('30 hari')
const filters = ['7 hari', '14 hari', '30 hari']

// --- Stats State ---
const stats = ref([
  { label: 'TOTAL PENDAPATAN', value: 'Rp 0', trend: '0%', trendType: 'up' },
  { label: 'TOTAL PESANAN', value: '0', trend: '0%', trendType: 'up' },
  { label: 'RATA-RATA ORDER', value: 'Rp 0', trend: '0%', trendType: 'up' },
  { label: 'PRODUK AKTIF', value: '0', trend: '0%', trendType: 'up' }
])

const loading = ref(true)
const rawOrders = ref([])
const rawProducts = ref([])

const auth = useAuthStore()
const { isAdmin } = storeToRefs(auth)

// --- Chart Data States ---
const lineChartData = ref({
  labels: [],
  datasets: [
    {
      label: 'Pendapatan',
      data: [],
      borderColor: '#0f172a',
      backgroundColor: 'transparent',
      fill: false,
      tension: 0.4,
      pointRadius: 4,
      borderWidth: 2,
    },
    {
      label: 'Pesanan',
      data: [],
      borderColor: '#10b981',
      backgroundColor: 'transparent',
      fill: false,
      tension: 0.4,
      pointRadius: 4,
      borderWidth: 2,
    }
  ]
})

const barChartData = ref({
  labels: [],
  datasets: [{
    label: 'Penjualan',
    data: [],
    backgroundColor: ['#0f172a', '#10b981', '#f59e0b', '#3b82f6', '#8b5cf6'],
    borderRadius: 4,
  }]
})

const bestSellers = ref([])
const paymentDist = ref([
  { name: 'Tunai', percentage: 0, color: 'bg-zinc-800' },
  { name: 'Transfer', percentage: 0, color: 'bg-blue-500' }
])

// ==========================================
// 🛠️ TAMBAHAN: FUNGSI EKSPOR DATA CSV
// ==========================================
const exportToCSV = () => {
  const days = parseInt(activeFilter.value)
  const now = new Date()
  const filterDate = new Date()
  filterDate.setDate(now.getDate() - days)

  // Ambil data order yang terfilter sesuai pilihan hari dashboard
  const filteredOrders = rawOrders.value.filter(o => new Date(o.createdAt) >= filterDate)

  // Buat Baris Header CSV
  const headers = ['ID Order', 'Tanggal', 'Metode Pembayaran', 'Total Transaksi (Rp)', 'Status']
  
  // Mapping isi data transaksinya
  const rows = filteredOrders.map(o => [
    `"${o.id || '-'}"`,
    `"${o.createdAt ? o.createdAt.split('T')[0] : '-'}"`,
    `"${o.payment?.method || '-'}"`,
    o.total || 0,
    `"${o.status || 'COMPLETED'}"`
  ])

  // Gabungkan semua baris menjadi string CSV rapi
  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.join(','))
  ].join('\n')

  // Download File via Browser
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', `GAPTEK_Laporan_${activeFilter.value.replace(' ', '_')}_${new Date().toISOString().split('T')[0]}.csv`)
  link.click()
}
const exportToPDF = () => {
  const doc = new jsPDF({
    orientation: 'portrait',
    unit: 'mm',
    format: 'a4'
  })

  const tanggalSekarang = new Date().toISOString().split('T')[0]
  const filterHari = activeFilter.value

  // 1. Banner Atas Modern (Warna Hitam/Zinc Gelap Premium)
  doc.setFillColor(15, 23, 42) 
  doc.rect(0, 0, 210, 38, 'F')  

  doc.setFont('helvetica', 'bold')
  doc.setFontSize(22)
  doc.setTextColor(255, 255, 255) 
  doc.text('GAPTEK TERMINAL POS', 14, 16)
  
  doc.setFont('helvetica', 'normal')
  doc.setFontSize(11)
  doc.setTextColor(148, 163, 184) 
  doc.text(`Laporan Analisis Performa Bisnis — Periode ${filterHari}`, 14, 24)
  
  doc.setFontSize(9)
  doc.setTextColor(203, 213, 225)
  doc.text(`Diekspor pada: ${tanggalSekarang}`, 14, 31)

  // 2. Tabel Ringkasan Data Kunci
  doc.setFont('helvetica', 'bold')
  doc.setFontSize(13)
  doc.setTextColor(15, 23, 42) 
  doc.text('I. RINGKASAN DATA KUNCI', 14, 50)

  const statsRows = stats.value.map(stat => [stat.label, stat.value])

  autoTable(doc, {
    startY: 54,
    margin: { left: 14, right: 14 },
    theme: 'plain', 
    styles: { fontSize: 10, cellPadding: 2.5 },
    columnStyles: {
      0: { fontStyle: 'normal', textColor: [100, 116, 139], width: 60 },
      1: { fontStyle: 'bold', textColor: [15, 23, 42], halign: 'left' }
    },
    body: statsRows,
  })

  // 3. Tabel Produk Terlaris Berkualitas Tinggi (Striped)
  const currentY = doc.lastAutoTable.finalY + 10
  
  doc.setFont('helvetica', 'bold')
  doc.setFontSize(13)
  doc.setTextColor(15, 23, 42)
  doc.text('II. DAFTAR 5 PRODUK PALING LARIS', 14, currentY)

  const productHeaders = [['Nama Produk', 'Kategori', 'Terjual', 'Pendapatan']]
  let productRows = []

  if (bestSellers.value.length === 0) {
    productRows = [['Belum ada data penjualan produk di periode ini.', '', '', '']]
  } else {
    productRows = bestSellers.value.map(p => [p.name, p.category, `${p.sold} Unit`, p.revenue])
  }

  autoTable(doc, {
    startY: currentY + 4,
    margin: { left: 14, right: 14 },
    theme: 'striped', 
    headStyles: { 
      fillColor: [15, 23, 42], 
      textColor: [255, 255, 255], 
      fontStyle: 'bold',
      fontSize: 9.5 
    },
    bodyStyles: { fontSize: 9, textColor: [51, 65, 85] },
    columnStyles: {
      0: { width: 75 },
      1: { width: 40 },
      2: { halign: 'center', width: 25 },
      3: { halign: 'right', fontStyle: 'bold', width: 42 }
    },
    head: productHeaders,
    body: productRows,
  })

  // 4. Unduh Berkas Otomatis
  doc.save(`GAPTEK_Laporan_${filterHari.replace(' ', '_')}_${tanggalSekarang}.pdf`)
}
async function fetchData() {
  loading.value = true
  try {
    const urlOrders = isAdmin.value ? '/api/v1/orders/admin' : '/api/v1/orders'
    const urlProducts = isAdmin.value ? '/api/v1/products/admin' : '/api/v1/products'
    
    const [ordersRes, productsRes] = await Promise.all([
      api.get(urlOrders),
      api.get(urlProducts)
    ])

    rawOrders.value = ordersRes.data.data || []
    rawProducts.value = Array.isArray(productsRes.data.data) ? productsRes.data.data : (productsRes.data.data.content || [])
    
    processData()
  } catch (err) {
    console.error('Failed to fetch report data', err)
  } finally {
    loading.value = false
  }
}
function processData() {
  const days = parseInt(activeFilter.value)
  const now = new Date()
  const filterDate = new Date()
  filterDate.setDate(now.getDate() - days)

  // 🌟 Fungsi pembersih angka yang jauh lebih aman & fleksibel
  const safeNumber = (val) => {
    if (val === undefined || val === null) return 0
    if (typeof val === 'number') return val // Jika sudah angka murni, langsung kembalikan
    
    // Jika bentuknya string, bersihkan dulu teks Rp dan titiknya
    const clean = val.toString().replace(/[^0-9.-]/g, '')
    return parseFloat(clean) || 0
  }

  // Filter pesanan berdasarkan tanggal (Logika asli kamu yang sudah work)
  const filteredOrders = rawOrders.value.filter(o => {
    if (!o.createdAt) return false
    return new Date(o.createdAt) >= filterDate
  })
  
  // 1. Hitung Statistik Utama
  const totalRevenue = filteredOrders.reduce((sum, o) => sum + safeNumber(o.total), 0)
  const totalOrders = filteredOrders.length
  const avgOrder = totalOrders > 0 ? totalRevenue / totalOrders : 0
  
  // 🌟 Perbaikan pembacaan PRODUK AKTIF agar tidak 0
  const activeProducts = rawProducts.value.filter(p => {
    return p.isActive === true || 
           p.isActive === 1 || 
           p.is_active === true || 
           p.is_active === 1 ||
           p.status?.toLowerCase() === 'active' ||
           p.status?.toLowerCase() === 'aktif'
  }).length

  stats.value[0].value = formatCurrency(totalRevenue)
  stats.value[1].value = totalOrders.toString()
  stats.value[2].value = formatCurrency(avgOrder)
  stats.value[3].value = activeProducts.toString()

  // 2. Pemetaan Grafik Tren / Line Chart
  const dateLabels = []
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date()
    d.setDate(now.getDate() - i)
    dateLabels.push(d.toISOString().split('T')[0])
  }

  lineChartData.value.labels = dateLabels.map(d => d.split('-').slice(1).reverse().join('/'))
  lineChartData.value.datasets[0].data = dateLabels.map(date => 
    filteredOrders.filter(o => o.createdAt?.startsWith(date)).reduce((sum, o) => sum + safeNumber(o.total), 0) / 1000000
  )
  lineChartData.value.datasets[1].data = dateLabels.map(date => 
    filteredOrders.filter(o => o.createdAt?.startsWith(date)).length
  )

  // 3. Distribusi Metode Pembayaran
  const cashCount = filteredOrders.filter(o => {
    const m = o.payment?.method?.toLowerCase()
    return m === 'cash' || m === 'tunai'
  }).length
  const transferCount = filteredOrders.filter(o => o.payment?.method?.toLowerCase() === 'transfer').length
  const totalWithPayment = cashCount + transferCount
  
  if (totalWithPayment > 0) {
    paymentDist.value[0].percentage = Math.round((cashCount / totalWithPayment) * 100)
    paymentDist.value[1].percentage = Math.round((transferCount / totalWithPayment) * 100)
  } else {
    paymentDist.value[0].percentage = 0
    paymentDist.value[1].percentage = 0
  }

  // 4. Hitung Ulang Daftar 5 Produk Terlaris
  const productSales = {}
  filteredOrders.forEach(order => {
    if (order.items) {
      order.items.forEach(item => {
        const pid = item.product?.id || item.productId || item.product_id
        if (!pid) return
        if (!productSales[pid]) {
          productSales[pid] = { 
            name: item.product?.name || item.name || 'Produk', 
            category: item.product?.category?.name || item.category || 'General', 
            sold: 0, 
            revenue: 0 
          }
        }
        productSales[pid].sold += (parseInt(item.qty) || 0)
        productSales[pid].revenue += safeNumber(item.subtotal || item.price * item.qty)
      })
    }
  })

  bestSellers.value = Object.values(productSales)
    .sort((a, b) => b.sold - a.sold)
    .slice(0, 5)
    .map(p => ({
      ...p,
      revenue: formatCurrency(p.revenue)
    }))

  // 5. Grafik Batang / Bar Chart Kategori
  const categorySales = {}
  filteredOrders.forEach(order => {
    if (order.items) {
      order.items.forEach(item => {
        const cat = item.product?.category?.name || item.category || 'Lainnya'
        categorySales[cat] = (categorySales[cat] || 0) + safeNumber(item.subtotal || item.price * item.qty)
      })
    }
  })

  const sortedCats = Object.entries(categorySales)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5)

  barChartData.value.labels = sortedCats.map(c => c[0])
  barChartData.value.datasets[0].data = sortedCats.map(c => c[1] / 1000000)
}

function formatCurrency(v) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(v)
}

watch(activeFilter, processData)
onMounted(fetchData)

const lineChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    y: { beginAtZero: true, ticks: { callback: (v) => `Rp ${v}jt` } }
  }
}

const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    y: { ticks: { callback: (v) => `Rp ${v}jt` } }
  }
}
</script>

<template>
  <AppLayout>
    <div v-if="loading" class="flex flex-col items-center justify-center min-h-[60vh]">
      <Loader2 class="w-10 h-10 animate-spin text-primary/40" />
      <p class="text-sm text-muted-foreground mt-4 font-medium italic">Menghubungkan ke sistem...</p>
    </div>
   <div v-else id="report-content" class="space-y-8 pb-10 animate-in fade-in duration-500 bg-white dark:bg-zinc-950 p-4 rounded-2xl">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h3 class="text-[10px] font-bold text-muted-foreground/60 uppercase tracking-[0.2em] mb-1">WAWASAN & ANALISIS DATA</h3>
          <h1 class="text-3xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50">Laporan</h1>
          <p class="text-sm text-muted-foreground mt-1.5">
            Analisis performa bisnis berdasarkan data transaksi Terminal POS.
          </p>
        </div>
        <div class="flex flex-wrap items-center gap-3">
          <div class="flex items-center bg-zinc-100/80 dark:bg-zinc-900/80 p-1 rounded-xl border border-zinc-200 dark:border-zinc-800 backdrop-blur-sm">
            <button 
              v-for="filter in filters" 
              :key="filter"
              @click="activeFilter = filter"
              class="px-4 py-1.5 text-[11px] font-bold rounded-lg transition-all"
              :class="activeFilter === filter 
                ? 'bg-white dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 shadow-sm border border-zinc-200/50 dark:border-zinc-700/50' 
                : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100'"
            >
              {{ filter }}
            </button>
          </div>

          <div class="flex items-center gap-2">
            <button @click="exportToCSV" class="flex items-center gap-2 px-4 py-2 text-[11px] font-bold bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-xl hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-all shadow-sm group">
              <Table class="w-4 h-4 text-zinc-400 group-hover:text-emerald-500 transition-colors" />
              CSV
            </button>
            <button @click="exportToPDF" class="flex items-center gap-2 px-4 py-2 text-[11px] font-bold bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-xl hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-all shadow-sm group">
              <FileIcon class="w-4 h-4 text-zinc-400 group-hover:text-rose-500 transition-colors" />
              PDF
            </button>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div v-for="stat in stats" :key="stat.label" class="bg-card border border-zinc-100 dark:border-zinc-800/60 rounded-2xl p-6 shadow-sm hover:shadow-md transition-all duration-300 group">
          <div class="flex items-center justify-between mb-4">
            <span class="text-[10px] font-bold text-muted-foreground/70 uppercase tracking-widest group-hover:text-primary transition-colors">{{ stat.label }}</span>
          </div>
          <div class="flex flex-col gap-1.5">
            <h2 class="text-3xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50">{{ stat.value }}</h2>
            <div class="flex items-center gap-2">
              <div class="flex items-center gap-1 bg-zinc-50 dark:bg-zinc-900 px-1.5 py-0.5 rounded-md">
                <TrendingUp class="w-3 h-3 text-emerald-500" />
                <span class="text-[11px] font-bold text-emerald-500">{{ stat.trend }}</span>
              </div>
              <span class="text-[10px] text-muted-foreground font-medium whitespace-nowrap">vs periode sebelumnya</span>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-card border border-zinc-100 dark:border-zinc-800/60 rounded-2xl p-8 shadow-sm">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
          <div>
            <h3 class="text-[10px] font-bold text-muted-foreground/60 uppercase tracking-[0.2em] mb-1.5">TREN PERFORMA</h3>
            <h2 class="text-xl font-bold text-zinc-900 dark:text-zinc-50">Pendapatan & Pesanan</h2>
          </div>
          <div class="flex items-center gap-6">
            <div class="flex items-center gap-2.5">
              <div class="w-2.5 h-2.5 rounded-full bg-zinc-900 dark:bg-zinc-50"></div>
              <span class="text-xs font-bold text-zinc-600 dark:text-zinc-400">Pendapatan (juta)</span>
            </div>
            <div class="flex items-center gap-2.5">
              <div class="w-2.5 h-2.5 rounded-full bg-emerald-500"></div>
              <span class="text-xs font-bold text-zinc-600 dark:text-zinc-400">Pesanan</span>
            </div>
          </div>
        </div>
        <div class="h-[350px] w-full">
          <Line :data="lineChartData" :options="lineChartOptions" />
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="bg-card border border-zinc-100 dark:border-zinc-800/60 rounded-2xl p-8 shadow-sm">
          <div class="flex items-center justify-between mb-8">
            <div>
              <h3 class="text-[10px] font-bold text-muted-foreground/60 uppercase tracking-[0.2em] mb-1.5">PER KATEGORI</h3>
              <h2 class="text-xl font-bold text-zinc-900 dark:text-zinc-50">Penjualan Terbanyak</h2>
            </div>
            <button class="p-2 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors">
              <BarChart3 class="w-4 h-4 text-muted-foreground" />
            </button>
          </div>
          <div class="h-[300px] w-full">
            <Bar :data="barChartData" :options="barChartOptions" />
          </div>
        </div>

        <div class="bg-card border border-zinc-100 dark:border-zinc-800/60 rounded-2xl p-8 shadow-sm">
          <div class="flex items-center justify-between mb-8">
            <div>
              <h3 class="text-[10px] font-bold text-muted-foreground/60 uppercase tracking-[0.2em] mb-1.5">METODE PEMBAYARAN</h3>
              <h2 class="text-xl font-bold text-zinc-900 dark:text-zinc-50">Distribusi Transaksi</h2>
            </div>
            <button class="p-2 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors">
              <Download class="w-4 h-4 text-muted-foreground" />
            </button>
          </div>
          <div class="space-y-6 py-4">
            <div v-for="item in paymentDist" :key="item.name" class="space-y-2">
              <div class="flex items-center justify-between">
                <span class="text-sm font-bold text-zinc-700 dark:text-zinc-300 tracking-tight">{{ item.name }}</span>
                <span class="text-sm font-black text-zinc-900 dark:text-zinc-50">{{ item.percentage }}%</span>
              </div>
              <div class="h-2.5 w-full bg-zinc-100 dark:bg-zinc-900 rounded-full overflow-hidden">
                <div 
                  class="h-full rounded-full transition-all duration-1000 ease-out shadow-sm" 
                  :class="item.color"
                  :style="{ width: `${item.percentage}%` }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-card border border-zinc-100 dark:border-zinc-800/60 rounded-2xl p-8 shadow-sm">
        <div class="flex items-center justify-between mb-8">
          <div>
            <h3 class="text-[10px] font-bold text-muted-foreground/60 uppercase tracking-[0.2em] mb-1.5">TERLARIS</h3>
            <h2 class="text-xl font-bold text-zinc-900 dark:text-zinc-50">Produk Paling Laris</h2>
          </div>
          <button class="p-2 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors">
            <Trophy class="w-4 h-4 text-muted-foreground" />
          </button>
        </div>
        
        <div v-if="bestSellers.length === 0" class="py-12 text-center text-muted-foreground">
           <ShoppingBag class="w-10 h-10 mx-auto opacity-20 mb-3" />
           <p class="text-sm font-medium">Belum ada data penjualan produk di periode ini.</p>
        </div>

        <div v-else>
          <div class="md:hidden space-y-4">
            <div v-for="product in bestSellers" :key="'mobile-' + product.name" class="p-4 rounded-xl border border-zinc-100 dark:border-zinc-800/60 bg-zinc-50/30 dark:bg-zinc-900/20">
              <div class="flex items-center gap-3 mb-3">
                <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center text-primary shrink-0">
                  <ShoppingBag class="w-5 h-5" />
                </div>
                <div class="min-w-0">
                  <h4 class="font-bold text-zinc-900 dark:text-zinc-50 truncate">{{ product.name }}</h4>
                  <span class="text-[10px] font-medium px-2 py-0.5 rounded-full bg-zinc-100 dark:bg-zinc-800 text-muted-foreground uppercase tracking-wider">
                    {{ product.category }}
                  </span>
                </div>
              </div>
              <div class="flex items-center justify-between pt-3 border-t border-zinc-100 dark:border-zinc-800/60">
                <div class="flex flex-col">
                  <span class="text-[10px] text-muted-foreground font-bold uppercase tracking-widest">TERJUAL</span>
                  <span class="text-sm font-bold text-zinc-900 dark:text-zinc-50">{{ product.sold }} unit</span>
                </div>
                <div class="flex flex-col text-right">
                  <span class="text-[10px] text-muted-foreground font-bold uppercase tracking-widest">PENDAPATAN</span>
                  <span class="text-sm font-bold text-primary">{{ product.revenue }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="hidden md:block overflow-x-auto">
            <table class="w-full text-sm text-left">
              <thead class="text-xs text-muted-foreground uppercase bg-zinc-50/50 dark:bg-zinc-900/50 border-b border-zinc-100 dark:border-zinc-800/60">
                <tr>
                  <th scope="col" class="px-6 py-4 font-semibold rounded-tl-lg">Produk</th>
                  <th scope="col" class="px-6 py-4 font-semibold">Kategori</th>
                  <th scope="col" class="px-6 py-4 font-semibold text-center">Terjual</th>
                  <th scope="col" class="px-6 py-4 font-semibold text-right rounded-tr-lg">Pendapatan</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-zinc-100 dark:divide-zinc-800/60">
                <tr v-for="product in bestSellers" :key="product.name" class="hover:bg-zinc-50 dark:hover:bg-zinc-900/40 transition-colors group">
                  <td class="px-6 py-4 font-medium text-zinc-900 dark:text-zinc-50">
                    <div class="flex items-center gap-3">
                      <div class="w-8 h-8 rounded bg-primary/10 flex items-center justify-center text-primary group-hover:scale-110 transition-transform">
                        <ShoppingBag class="w-4 h-4" />
                      </div>
                      {{ product.name }}
                    </div>
                  </td>
                  <td class="px-6 py-4 text-muted-foreground">{{ product.category }}</td>
                  <td class="px-6 py-4 text-center">
                    <span class="inline-flex items-center justify-center bg-zinc-100 dark:bg-zinc-800 px-2.5 py-1 rounded-md text-xs font-semibold">
                      {{ product.sold }}
                    </span>
                  </td>
                  <td class="px-6 py-4 text-right font-semibold text-zinc-900 dark:text-zinc-50">
                    {{ product.revenue }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        
        <div class="mt-6 flex justify-center">
          <RouterLink to="/dashboard/products" class="flex items-center gap-2 text-sm font-medium text-primary hover:text-primary/80 transition-colors">
            Analisis Produk Lebih Dalam
            <ArrowUpRight class="w-4 h-4" />
          </RouterLink>
        </div>
      </div>
    </div>
  </AppLayout>
</template>


<style scoped>
canvas {
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.02));
}
</style>