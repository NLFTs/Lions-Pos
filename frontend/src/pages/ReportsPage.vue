<script setup>
import { ref } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
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
  ArrowUpRight
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

// --- Stats Data ---
const stats = [
  { 
    label: 'TOTAL PENDAPATAN', 
    value: 'Rp 45.7jt', 
    trend: '+18.4%', 
    trendType: 'up'
  },
  { 
    label: 'TOTAL PESANAN', 
    value: '814', 
    trend: '+9.7%', 
    trendType: 'up'
  },
  { 
    label: 'RATA-RATA ORDER', 
    value: 'Rp 56.158', 
    trend: '+3.2%', 
    trendType: 'up'
  },
  { 
    label: 'TINGKAT PEMBATALAN', 
    value: '12.5%', 
    trend: '-1.4%', 
    trendType: 'down'
  }
]

// --- Line Chart Data (Pendapatan & Pesanan) ---
const lineChartData = {
  labels: ['24 Apr', '25 Apr', '26 Apr', '27 Apr', '28 Apr', '29 Apr', '30 Apr', '01 Mei', '02 Mei', '03 Mei', '04 Mei', '05 Mei', '06 Mei', '07 Mei'],
  datasets: [
    {
      label: 'Pendapatan',
      data: [3.8, 3.2, 2.8, 3.1, 3.2, 3.4, 3.6, 3.2, 3.0, 2.8, 3.4, 3.8, 3.6, 3.4],
      borderColor: '#0f172a',
      backgroundColor: 'transparent',
      fill: false,
      tension: 0.4,
      pointRadius: 4,
      pointHoverRadius: 6,
      borderWidth: 2,
    },
    {
      label: 'Pesanan',
      data: [3.2, 4.2, 4.8, 3.8, 4.4, 4.2, 5.2, 5.4, 4.8, 4.2, 5.2, 5.4, 5.6, 6.2],
      borderColor: '#10b981',
      backgroundColor: 'transparent',
      fill: false,
      tension: 0.4,
      pointRadius: 4,
      pointHoverRadius: 6,
      borderWidth: 2,
    }
  ]
}

const lineChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      mode: 'index',
      intersect: false,
      backgroundColor: '#fff',
      titleColor: '#000',
      bodyColor: '#666',
      borderColor: '#eee',
      borderWidth: 1,
      padding: 12,
      displayColors: true,
      boxWidth: 8,
      boxHeight: 8,
      usePointStyle: true,
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      grid: {
        display: true,
        drawBorder: false,
        color: 'rgba(0,0,0,0.03)',
        borderDash: [5, 5]
      },
      ticks: {
        callback: (value) => value === 0 ? 'Rp 0' : `Rp ${value}jt`,
        color: '#94a3b8',
        font: { size: 10 }
      }
    },
    x: {
      grid: { display: false },
      ticks: { color: '#94a3b8', font: { size: 10 } }
    }
  }
}

// --- Bar Chart Data (Penjualan Kategori) ---
const barChartData = {
  labels: ['Pakaian', 'Alas Kaki', 'Aksesori', 'Tas', 'Lainnya'],
  datasets: [
    {
      label: 'Penjualan',
      data: [25.4, 12.8, 8.5, 6.2, 2.1],
      backgroundColor: ['#0f172a', '#10b981', '#f59e0b', '#3b82f6', '#8b5cf6'],
      borderRadius: 4,
      barThickness: 45,
    }
  ]
}

const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    y: {
      beginAtZero: true,
      grid: {
        display: true,
        drawBorder: false,
        color: 'rgba(0,0,0,0.03)',
        borderDash: [5, 5]
      },
      ticks: { color: '#94a3b8', font: { size: 10 } }
    },
    x: {
      grid: { display: false },
      ticks: { color: '#94a3b8', font: { size: 10 } }
    }
  }
}

// --- Transaction Distribution Data ---
const paymentDist = [
  { name: 'QRIS', percentage: 38, color: 'bg-emerald-500' },
  { name: 'Tunai', percentage: 24, color: 'bg-zinc-800 dark:bg-zinc-200' },
  { name: 'Kartu Debit', percentage: 18, color: 'bg-blue-500' },
  { name: 'Kartu Kredit', percentage: 12, color: 'bg-violet-500' },
  { name: 'GoPay', percentage: 8, color: 'bg-purple-500' }
]

// --- Best Sellers Data ---
const bestSellers = [
  { id: 1, name: 'Kaos Polos Putih', category: 'Pakaian', sold: 420, revenue: 'Rp 35.700.000' },
  { id: 2, name: 'Celana Chino Beige', category: 'Pakaian', sold: 315, revenue: 'Rp 61.425.000' },
  { id: 3, name: 'Sepatu Sneakers Hitam', category: 'Alas Kaki', sold: 280, revenue: 'Rp 126.000.000' },
  { id: 4, name: 'Jaket Bomber Olive', category: 'Pakaian', sold: 210, revenue: 'Rp 67.200.000' },
  { id: 5, name: 'Dompet Kulit Minimalis', category: 'Aksesori', sold: 195, revenue: 'Rp 32.175.000' },
  { id: 6, name: 'Tas Selempang Canvas', category: 'Tas', sold: 154, revenue: 'Rp 20.790.000' },
  { id: 7, name: 'Kemeja Flannel Kotak', category: 'Pakaian', sold: 128, revenue: 'Rp 26.880.000' },
  { id: 8, name: 'Topi Baseball Biru Navy', category: 'Aksesori', sold: 95, revenue: 'Rp 7.125.000' }
]

const activeFilter = ref('14 hari')
const filters = ['7 hari', '14 hari', '30 hari']
</script>

<template>
  <AppLayout>
    <div class="space-y-8 pb-10">
      <!-- Header -->
      <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h3 class="text-[10px] font-bold text-muted-foreground/60 uppercase tracking-[0.2em] mb-1">INSIGHT & ANALITIK</h3>
          <h1 class="text-3xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50">Laporan</h1>
          <p class="text-sm text-muted-foreground mt-1.5">
            Analisis performa bisnis dengan filter periode fleksibel.
          </p>
        </div>
        <div class="flex flex-wrap items-center gap-3">
          <!-- Period Filter -->
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
            <button class="px-4 py-1.5 text-[11px] font-bold rounded-lg transition-all text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100 flex items-center gap-1.5">
              <Calendar class="w-3.5 h-3.5" />
              Kustom
            </button>
          </div>

          <!-- Export Buttons -->
          <div class="flex items-center gap-2">
            <button class="flex items-center gap-2 px-4 py-2 text-[11px] font-bold bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-xl hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-all shadow-sm group">
              <Table class="w-4 h-4 text-zinc-400 group-hover:text-emerald-500 transition-colors" />
              CSV
            </button>
            <button class="flex items-center gap-2 px-4 py-2 text-[11px] font-bold bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-xl hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-all shadow-sm group">
              <FileIcon class="w-4 h-4 text-zinc-400 group-hover:text-rose-500 transition-colors" />
              PDF
            </button>
          </div>
        </div>
      </div>

      <!-- Stat Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div v-for="stat in stats" :key="stat.label" class="bg-card border border-zinc-100 dark:border-zinc-800/60 rounded-2xl p-6 shadow-sm hover:shadow-md transition-all duration-300 group">
          <div class="flex items-center justify-between mb-4">
            <span class="text-[10px] font-bold text-muted-foreground/70 uppercase tracking-widest group-hover:text-primary transition-colors">{{ stat.label }}</span>
          </div>
          <div class="flex flex-col gap-1.5">
            <h2 class="text-3xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50">{{ stat.value }}</h2>
            <div class="flex items-center gap-2">
              <div class="flex items-center gap-1 bg-zinc-50 dark:bg-zinc-900 px-1.5 py-0.5 rounded-md">
                <component 
                  :is="stat.trendType === 'up' ? TrendingUp : TrendingDown" 
                  class="w-3 h-3" 
                  :class="stat.trendType === 'up' ? 'text-emerald-500' : 'text-rose-500'" 
                />
                <span class="text-[11px] font-bold" :class="stat.trendType === 'up' ? 'text-emerald-500' : 'text-rose-500'">{{ stat.trend }}</span>
              </div>
              <span class="text-[10px] text-muted-foreground font-medium whitespace-nowrap">vs periode sebelumnya</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Line Chart -->
      <div class="bg-card border border-zinc-100 dark:border-zinc-800/60 rounded-2xl p-8 shadow-sm">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
          <div>
            <h3 class="text-[10px] font-bold text-muted-foreground/60 uppercase tracking-[0.2em] mb-1.5">TREN PERFORMA</h3>
            <h2 class="text-xl font-bold text-zinc-900 dark:text-zinc-50">Pendapatan & Pesanan</h2>
          </div>
          <div class="flex items-center gap-6">
            <div class="flex items-center gap-2.5">
              <div class="w-2.5 h-2.5 rounded-full bg-zinc-900 dark:bg-zinc-50"></div>
              <span class="text-xs font-bold text-zinc-600 dark:text-zinc-400">Pendapatan</span>
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

      <!-- Charts Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Bar Chart: Category Sales -->
        <div class="bg-card border border-zinc-100 dark:border-zinc-800/60 rounded-2xl p-8 shadow-sm">
          <div class="flex items-center justify-between mb-8">
            <div>
              <h3 class="text-[10px] font-bold text-muted-foreground/60 uppercase tracking-[0.2em] mb-1.5">PER KATEGORI</h3>
              <h2 class="text-xl font-bold text-zinc-900 dark:text-zinc-50">Penjualan kategori</h2>
            </div>
            <button class="p-2 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors">
              <BarChart3 class="w-4 h-4 text-muted-foreground" />
            </button>
          </div>
          <div class="h-[450px] w-full">
            <Bar :data="barChartData" :options="barChartOptions" />
          </div>
        </div>

        <!-- Payment Method Distribution -->
        <div class="bg-card border border-zinc-100 dark:border-zinc-800/60 rounded-2xl p-8 shadow-sm">
          <div class="flex items-center justify-between mb-8">
            <div>
              <h3 class="text-[10px] font-bold text-muted-foreground/60 uppercase tracking-[0.2em] mb-1.5">METODE PEMBAYARAN</h3>
              <h2 class="text-xl font-bold text-zinc-900 dark:text-zinc-50">Distribusi transaksi</h2>
            </div>
            <button class="p-2 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors">
              <Download class="w-4 h-4 text-muted-foreground" />
            </button>
          </div>
          <div class="space-y-6">
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

      <!-- Best Sellers -->
      <div class="bg-card border border-zinc-100 dark:border-zinc-800/60 rounded-2xl p-8 shadow-sm">
        <div class="flex items-center justify-between mb-8">
          <div>
            <h3 class="text-[10px] font-bold text-muted-foreground/60 uppercase tracking-[0.2em] mb-1.5">BEST SELLERS</h3>
            <h2 class="text-xl font-bold text-zinc-900 dark:text-zinc-50">Produk paling laris</h2>
          </div>
          <button class="p-2 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors">
            <Trophy class="w-4 h-4 text-muted-foreground" />
          </button>
        </div>
        
        <div class="overflow-x-auto">
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
              <tr v-for="product in bestSellers" :key="product.id" class="hover:bg-zinc-50 dark:hover:bg-zinc-900/40 transition-colors group">
                <td class="px-6 py-4 font-medium text-zinc-900 dark:text-zinc-50">
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded bg-primary/10 flex items-center justify-center text-primary">
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
        
        <div class="mt-6 flex justify-center">
          <RouterLink to="/dashboard/products" class="flex items-center gap-2 text-sm font-medium text-primary hover:text-primary/80 transition-colors">
            Lihat semua produk
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
