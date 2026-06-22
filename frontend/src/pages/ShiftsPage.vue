<script setup>
import { ref, computed, onMounted } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import { useToast } from '@/composables/useToast'
import api from '@/lib/api'
import {
  Clock, Users, TrendingUp, ReceiptText, ChevronDown,
  CheckCircle2, Circle, Loader2, Search, Filter
} from 'lucide-vue-next'

const { toast } = useToast()

// ─── State ───────────────────────────────────────────────────────────────────
const shifts = ref([])
const loading = ref(false)
const searchQuery = ref('')
const filterStatus = ref('ALL') // ALL | OPEN | CLOSED

// ─── Fetch ───────────────────────────────────────────────────────────────────
async function fetchShifts() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/shifts')
    shifts.value = res.data?.data || []
  } catch (err) {
    toast.error('Gagal memuat data shift.')
  } finally {
    loading.value = false
  }
}

onMounted(fetchShifts)

// ─── Computed ─────────────────────────────────────────────────────────────────
const filteredShifts = computed(() => {
  let list = shifts.value
  if (filterStatus.value !== 'ALL') {
    list = list.filter(s => s.status === filterStatus.value)
  }
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(s =>
      (s.cashierUsername || '').toLowerCase().includes(q) ||
      (s.cashierFullname || '').toLowerCase().includes(q) ||
      (s.branchName || '').toLowerCase().includes(q)
    )
  }
  return list
})

// ─── Summary per kasir ────────────────────────────────────────────────────────
const cashierSummary = computed(() => {
  const map = {}
  shifts.value.filter(s => s.status === 'CLOSED').forEach(s => {
    const key = s.cashierUsername || s.cashierId
    if (!map[key]) {
      map[key] = {
        cashierUsername: s.cashierUsername,
        cashierFullname: s.cashierFullname,
        branchName: s.branchName,
        totalRevenue: 0,
        totalTransactions: 0,
        totalShifts: 0,
      }
    }
    map[key].totalRevenue += Number(s.totalRevenue || 0)
    map[key].totalTransactions += Number(s.totalTransactions || 0)
    map[key].totalShifts++
  })
  return Object.values(map).sort((a, b) => b.totalRevenue - a.totalRevenue)
})

// ─── Grand totals ─────────────────────────────────────────────────────────────
const grandTotal = computed(() => {
  const closed = shifts.value.filter(s => s.status === 'CLOSED')
  return {
    revenue: closed.reduce((s, x) => s + Number(x.totalRevenue || 0), 0),
    transactions: closed.reduce((s, x) => s + Number(x.totalTransactions || 0), 0),
    shiftCount: closed.length,
    openCount: shifts.value.filter(s => s.status === 'OPEN').length,
  }
})

// ─── Helpers ─────────────────────────────────────────────────────────────────
function formatCurrency(v) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(v || 0)
}

function formatDateTime(val) {
  if (!val) return '-'
  return new Date(val).toLocaleString('id-ID', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

function shiftDuration(s) {
  if (!s.startedAt || !s.endedAt) return '-'
  const ms = new Date(s.endedAt) - new Date(s.startedAt)
  const h = Math.floor(ms / 3600000)
  const m = Math.floor((ms % 3600000) / 60000)
  return `${h}j ${m}m`
}

function initials(name) {
  if (!name) return '?'
  return name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2)
}
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6">

      <!-- ─── Summary Cards ─────────────────────────────────────────────────── -->
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-white dark:bg-zinc-900 rounded-2xl border border-zinc-200 dark:border-zinc-800 p-4 flex flex-col gap-1">
          <p class="text-[11px] font-bold text-zinc-400 uppercase tracking-wider flex items-center gap-1.5">
            <TrendingUp class="h-3.5 w-3.5 text-emerald-500" /> Total Omzet
          </p>
          <p class="text-lg font-black text-emerald-600 dark:text-emerald-400 leading-tight">{{ formatCurrency(grandTotal.revenue) }}</p>
          <p class="text-[11px] text-zinc-400">dari shift yang selesai</p>
        </div>
        <div class="bg-white dark:bg-zinc-900 rounded-2xl border border-zinc-200 dark:border-zinc-800 p-4 flex flex-col gap-1">
          <p class="text-[11px] font-bold text-zinc-400 uppercase tracking-wider flex items-center gap-1.5">
            <ReceiptText class="h-3.5 w-3.5 text-blue-500" /> Total Transaksi
          </p>
          <p class="text-lg font-black text-zinc-900 dark:text-white leading-tight">{{ grandTotal.transactions }}</p>
          <p class="text-[11px] text-zinc-400">order PAID terkonfirmasi</p>
        </div>
        <div class="bg-white dark:bg-zinc-900 rounded-2xl border border-zinc-200 dark:border-zinc-800 p-4 flex flex-col gap-1">
          <p class="text-[11px] font-bold text-zinc-400 uppercase tracking-wider flex items-center gap-1.5">
            <CheckCircle2 class="h-3.5 w-3.5 text-zinc-500" /> Shift Selesai
          </p>
          <p class="text-lg font-black text-zinc-900 dark:text-white leading-tight">{{ grandTotal.shiftCount }}</p>
          <p class="text-[11px] text-zinc-400">shift telah ditutup</p>
        </div>
        <div class="bg-white dark:bg-zinc-900 rounded-2xl border border-zinc-200 dark:border-zinc-800 p-4 flex flex-col gap-1">
          <p class="text-[11px] font-bold text-zinc-400 uppercase tracking-wider flex items-center gap-1.5">
            <Circle class="h-3.5 w-3.5 text-emerald-400" /> Shift Aktif
          </p>
          <p class="text-lg font-black text-zinc-900 dark:text-white leading-tight">{{ grandTotal.openCount }}</p>
          <p class="text-[11px] text-zinc-400">shift sedang berjalan</p>
        </div>
      </div>

      <!-- ─── Omzet Per Kasir ────────────────────────────────────────────────── -->
      <div v-if="cashierSummary.length > 0" class="bg-white dark:bg-zinc-900 rounded-2xl border border-zinc-200 dark:border-zinc-800 overflow-hidden">
        <div class="px-5 py-4 border-b border-zinc-100 dark:border-zinc-800">
          <h2 class="text-sm font-black text-zinc-900 dark:text-white flex items-center gap-2">
            <Users class="h-4 w-4 text-primary" /> Rekapitulasi Omzet Per Kasir
          </h2>
        </div>
        <div class="divide-y divide-zinc-100 dark:divide-zinc-800">
          <div v-for="(c, i) in cashierSummary" :key="c.cashierUsername"
            class="flex items-center gap-4 px-5 py-3.5">
            <div class="w-8 h-8 rounded-full flex items-center justify-center text-[11px] font-black shrink-0"
              :style="{ background: ['#dbeafe','#dcfce7','#fef9c3','#fce7f3','#ede9fe'][i % 5],
                        color: ['#1d4ed8','#15803d','#a16207','#be185d','#6d28d9'][i % 5] }">
              {{ initials(c.cashierFullname || c.cashierUsername) }}
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-[13px] font-bold text-zinc-900 dark:text-zinc-100 truncate">{{ c.cashierFullname || c.cashierUsername }}</p>
              <p class="text-[11px] text-zinc-400">{{ c.branchName }} · {{ c.totalShifts }} shift · {{ c.totalTransactions }} transaksi</p>
            </div>
            <div class="text-right shrink-0">
              <p class="text-[14px] font-black text-emerald-600 dark:text-emerald-400">{{ formatCurrency(c.totalRevenue) }}</p>
              <p class="text-[10px] text-zinc-400 font-medium">omzet total</p>
            </div>
          </div>
        </div>
      </div>

      <!-- ─── Shift List ─────────────────────────────────────────────────────── -->
      <div class="bg-white dark:bg-zinc-900 rounded-2xl border border-zinc-200 dark:border-zinc-800 overflow-hidden">
        <div class="px-5 py-4 border-b border-zinc-100 dark:border-zinc-800 flex flex-wrap items-center justify-between gap-3">
          <h2 class="text-sm font-black text-zinc-900 dark:text-white flex items-center gap-2">
            <Clock class="h-4 w-4 text-primary" /> Riwayat Shift
          </h2>
          <div class="flex items-center gap-2">
            <!-- Search -->
            <div class="flex items-center gap-2 h-8 px-3 rounded-lg bg-zinc-100 dark:bg-zinc-800 border border-zinc-200 dark:border-zinc-700">
              <Search class="h-3.5 w-3.5 text-zinc-400 shrink-0" />
              <input v-model="searchQuery" placeholder="Cari kasir/cabang..."
                class="bg-transparent border-none outline-none text-[12px] font-medium text-zinc-700 dark:text-zinc-200 placeholder:text-zinc-400 w-36" />
            </div>
            <!-- Filter -->
            <div class="flex rounded-lg overflow-hidden border border-zinc-200 dark:border-zinc-700 text-[11px] font-bold">
              <button v-for="opt in ['ALL','OPEN','CLOSED']" :key="opt"
                @click="filterStatus = opt"
                :class="['px-3 py-1.5 transition-colors',
                  filterStatus === opt
                    ? 'bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900'
                    : 'bg-white dark:bg-zinc-900 text-zinc-500 hover:bg-zinc-50 dark:hover:bg-zinc-800']">
                {{ opt === 'ALL' ? 'Semua' : opt === 'OPEN' ? 'Aktif' : 'Selesai' }}
              </button>
            </div>
            <button @click="fetchShifts" class="h-8 w-8 flex items-center justify-center rounded-lg border border-zinc-200 dark:border-zinc-700 text-zinc-400 hover:text-zinc-700 hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors">
              <Loader2 :class="['h-3.5 w-3.5', loading ? 'animate-spin' : '']" />
            </button>
          </div>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="flex flex-col items-center justify-center py-16 text-zinc-400 gap-3">
          <Loader2 class="h-8 w-8 animate-spin opacity-30" />
          <p class="text-[13px] font-medium">Memuat data shift...</p>
        </div>

        <!-- Empty -->
        <div v-else-if="filteredShifts.length === 0" class="flex flex-col items-center justify-center py-16 text-zinc-400 gap-2">
          <Clock class="h-10 w-10 opacity-20" />
          <p class="text-[13px] font-medium">Belum ada data shift</p>
        </div>

        <!-- Table Desktop -->
        <div v-else class="hidden md:block overflow-x-auto">
          <table class="w-full text-[13px]">
            <thead>
              <tr class="bg-zinc-50 dark:bg-zinc-800/60">
                <th class="text-left px-5 py-3 font-bold text-zinc-500 text-[11px] uppercase tracking-wider">Kasir</th>
                <th class="text-left px-4 py-3 font-bold text-zinc-500 text-[11px] uppercase tracking-wider">Cabang</th>
                <th class="text-left px-4 py-3 font-bold text-zinc-500 text-[11px] uppercase tracking-wider">Mulai</th>
                <th class="text-left px-4 py-3 font-bold text-zinc-500 text-[11px] uppercase tracking-wider">Selesai</th>
                <th class="text-left px-4 py-3 font-bold text-zinc-500 text-[11px] uppercase tracking-wider">Durasi</th>
                <th class="text-right px-4 py-3 font-bold text-zinc-500 text-[11px] uppercase tracking-wider">Modal Awal</th>
                <th class="text-right px-4 py-3 font-bold text-zinc-500 text-[11px] uppercase tracking-wider">Omzet</th>
                <th class="text-right px-4 py-3 font-bold text-zinc-500 text-[11px] uppercase tracking-wider">Transaksi</th>
                <th class="text-center px-4 py-3 font-bold text-zinc-500 text-[11px] uppercase tracking-wider">Status</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-zinc-100 dark:divide-zinc-800">
              <tr v-for="s in filteredShifts" :key="s.id"
                class="hover:bg-zinc-50/60 dark:hover:bg-zinc-800/30 transition-colors">
                <td class="px-5 py-3.5">
                  <div class="flex items-center gap-2.5">
                    <div class="w-7 h-7 rounded-full flex items-center justify-center text-[10px] font-black shrink-0 bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-300">
                      {{ initials(s.cashierFullname || s.cashierUsername) }}
                    </div>
                    <div class="min-w-0">
                      <p class="font-bold text-zinc-900 dark:text-zinc-100 truncate">{{ s.cashierFullname || s.cashierUsername }}</p>
                      <p class="text-[11px] text-zinc-400">@{{ s.cashierUsername }}</p>
                    </div>
                  </div>
                </td>
                <td class="px-4 py-3.5 text-zinc-600 dark:text-zinc-300 font-medium">{{ s.branchName || '-' }}</td>
                <td class="px-4 py-3.5 text-zinc-600 dark:text-zinc-300">{{ formatDateTime(s.startedAt) }}</td>
                <td class="px-4 py-3.5 text-zinc-600 dark:text-zinc-300">{{ s.endedAt ? formatDateTime(s.endedAt) : '—' }}</td>
                <td class="px-4 py-3.5 text-zinc-500 font-mono text-[12px]">{{ shiftDuration(s) }}</td>
                <td class="px-4 py-3.5 text-right text-zinc-600 dark:text-zinc-300">{{ formatCurrency(s.startingCash) }}</td>
                <td class="px-4 py-3.5 text-right font-black" :class="s.status === 'CLOSED' ? 'text-emerald-600 dark:text-emerald-400' : 'text-zinc-400 italic'">
                  {{ s.status === 'CLOSED' ? formatCurrency(s.totalRevenue) : 'Belum dihitung' }}
                </td>
                <td class="px-4 py-3.5 text-right font-bold text-zinc-700 dark:text-zinc-300">
                  {{ s.status === 'CLOSED' ? s.totalTransactions : '—' }}
                </td>
                <td class="px-4 py-3.5 text-center">
                  <span :class="['text-[10px] font-black px-2.5 py-1 rounded-full',
                    s.status === 'OPEN'
                      ? 'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-600 dark:text-emerald-400'
                      : 'bg-zinc-100 dark:bg-zinc-800 text-zinc-500']">
                    {{ s.status === 'OPEN' ? '● Aktif' : 'Selesai' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Cards Mobile -->
        <div class="md:hidden divide-y divide-zinc-100 dark:divide-zinc-800">
          <div v-for="s in filteredShifts" :key="s.id" class="p-4 flex flex-col gap-2">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2.5">
                <div class="w-8 h-8 rounded-full bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-[11px] font-black text-zinc-600 dark:text-zinc-300">
                  {{ initials(s.cashierFullname || s.cashierUsername) }}
                </div>
                <div>
                  <p class="text-[13px] font-bold text-zinc-900 dark:text-zinc-100">{{ s.cashierFullname || s.cashierUsername }}</p>
                  <p class="text-[11px] text-zinc-400">{{ s.branchName }}</p>
                </div>
              </div>
              <span :class="['text-[10px] font-black px-2 py-0.5 rounded-full',
                s.status === 'OPEN' ? 'bg-emerald-50 text-emerald-600 dark:bg-emerald-900/20 dark:text-emerald-400' : 'bg-zinc-100 text-zinc-500 dark:bg-zinc-800']">
                {{ s.status === 'OPEN' ? '● Aktif' : 'Selesai' }}
              </span>
            </div>
            <div class="grid grid-cols-2 gap-2 text-[12px]">
              <div class="bg-zinc-50 dark:bg-zinc-800/60 rounded-lg px-3 py-2">
                <p class="text-zinc-400 text-[10px] font-bold uppercase tracking-wider">Mulai</p>
                <p class="font-semibold text-zinc-700 dark:text-zinc-200 mt-0.5">{{ formatDateTime(s.startedAt) }}</p>
              </div>
              <div class="bg-zinc-50 dark:bg-zinc-800/60 rounded-lg px-3 py-2">
                <p class="text-zinc-400 text-[10px] font-bold uppercase tracking-wider">Omzet</p>
                <p class="font-black mt-0.5" :class="s.status === 'CLOSED' ? 'text-emerald-600 dark:text-emerald-400' : 'text-zinc-400 italic'">
                  {{ s.status === 'CLOSED' ? formatCurrency(s.totalRevenue) : '—' }}
                </p>
              </div>
            </div>
            <div v-if="s.status === 'CLOSED'" class="text-[11px] text-zinc-400 flex items-center gap-3">
              <span>{{ s.totalTransactions }} transaksi</span>
              <span>·</span>
              <span>Durasi {{ shiftDuration(s) }}</span>
              <span>·</span>
              <span>Modal {{ formatCurrency(s.startingCash) }}</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </AppLayout>
</template>
