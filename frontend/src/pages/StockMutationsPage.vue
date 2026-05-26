<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'
import api from '@/lib/api'
import { 
  Loader2, 
  Repeat2, 
  Package, 
  Warehouse, 
  Building2, 
  ArrowUpRight, 
  ArrowDownLeft,
  Calendar,
  History
} from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.isAdmin)

// ─── State ───────────────────────────────────────────────────────────────────
const mutations = ref([])
const loading = ref(false)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

// ─── Actions ──────────────────────────────────────────────────────────────────
async function fetchData() {
  loading.value = true
  try {
    // Admin: /admin returns ResData<Page>, partner: /api/v1/stock-mutations returns ResData<List>
    const url = isAdmin.value ? '/api/v1/stock-mutations/admin?page=0&size=500' : '/api/v1/stock-mutations'
    const res = await api.get(url)
    const data = res.data?.data
    mutations.value = Array.isArray(data) ? data : (data?.content || [])
  } catch (err) {
    toast.error('Gagal memuat riwayat mutasi stok.')
  } finally {
    loading.value = false
  }
}

// ─── Computed ─────────────────────────────────────────────────────────────────
const filteredMutations = computed(() => {
  let r = mutations.value
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    r = r.filter(m => 
      (m.product?.name && m.product.name.toLowerCase().includes(q)) ||
      (m.type && m.type.toLowerCase().includes(q)) ||
      (m.reference_type && m.reference_type.toLowerCase().includes(q)) ||
      (m.notes && m.notes.toLowerCase().includes(q))
    )
  }
  return r
})

const paginatedMutations = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredMutations.value.slice(start, start + pageSize.value)
})

// ─── Helpers ──────────────────────────────────────────────────────────────────
function formatDate(dt) {
  return dt ? new Date(dt).toLocaleDateString('id-ID', { 
    day: '2-digit', 
    month: 'short', 
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }) : '-'
}

// Tentukan apakah mutasi ini masuk (+) atau keluar (-) berdasarkan type
function isIncoming(m) {
  const t = (m.type || '').toUpperCase()
  return t === 'PURCHASE_IN' || t === 'RETURN' || 
         (t === 'TRANSFER' && m.to_location_type != null) ||
         (t === 'ADJUSTMENT' && (m.qty || 0) > 0)
}

// Lokasi yang relevan untuk ditampilkan
function getLocationDisplay(m) {
  const t = (m.type || '').toUpperCase()
  if (t === 'SALE_OUT') return { type: m.from_location_type, id: m.from_location_id }
  if (t === 'PURCHASE_IN' || t === 'RETURN') return { type: m.to_location_type, id: m.to_location_id }
  if (t === 'TRANSFER') return { from: m.from_location_type, to: m.to_location_type }
  return { type: m.to_location_type || m.from_location_type, id: m.to_location_id || m.from_location_id }
}

function typeLabel(t) {
  const m = {
    SALE_OUT: 'Penjualan',
    PURCHASE_IN: 'Pembelian',
    TRANSFER: 'Transfer',
    ADJUSTMENT: 'Koreksi',
    RETURN: 'Retur'
  }
  return m[(t || '').toUpperCase()] || t
}

function typeColor(t) {
  const m = {
    SALE_OUT: 'bg-red-50 text-red-700 border-red-200 dark:bg-red-900/20 dark:text-red-400 dark:border-red-800/50',
    PURCHASE_IN: 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-900/20 dark:text-emerald-400 dark:border-emerald-800/50',
    TRANSFER: 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-900/20 dark:text-blue-400 dark:border-blue-800/50',
    ADJUSTMENT: 'bg-zinc-100 text-zinc-700 border-zinc-200 dark:bg-zinc-800 dark:text-zinc-400',
    RETURN: 'bg-violet-50 text-violet-700 border-violet-200 dark:bg-violet-900/20 dark:text-violet-400 dark:border-violet-800/50'
  }
  return m[(t || '').toUpperCase()] || 'bg-zinc-100 text-zinc-700 border-zinc-200'
}

onMounted(fetchData)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <!-- Header -->
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight text-zinc-900 dark:text-white">Riwayat Mutasi</h1>
          <p class="text-muted-foreground text-sm mt-1">Audit log pergerakan stok barang secara real-time.</p>
        </div>
        <div class="flex items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-80">
            <DataTableSearch v-model="searchQuery" placeholder="Cari produk, tipe, atau referensi..." />
          </div>
        </div>
      </div>

      <!-- Table Card -->
      <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm overflow-hidden bg-white dark:bg-zinc-950">
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-24">
            <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
          </div>

          <div v-else-if="filteredMutations.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
            <div class="w-14 h-14 rounded-full bg-zinc-50 dark:bg-zinc-900 flex items-center justify-center mb-4">
              <History class="h-7 w-7 opacity-20" />
            </div>
            <p class="text-sm font-medium">Belum ada riwayat mutasi stok.</p>
          </div>

          <div v-else>
            <!-- Mobile List -->
            <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
              <div v-for="m in paginatedMutations" :key="m.id" class="p-4 flex flex-col gap-3">
                <div class="flex justify-between items-start">
                  <div class="flex items-center gap-2">
                    <div class="w-8 h-8 rounded-lg bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center shrink-0">
                      <ArrowUpRight v-if="isIncoming(m)" class="h-4 w-4 text-emerald-500" />
                      <ArrowDownLeft v-else class="h-4 w-4 text-red-500" />
                    </div>
                    <div>
                      <p class="text-xs font-bold text-zinc-900 dark:text-white leading-none mb-1">{{ m.product?.name || '-' }}</p>
                      <p class="text-[10px] font-mono text-muted-foreground">{{ m.reference_type || 'NO-REF' }} #{{ m.reference_id || '-' }}</p>
                    </div>
                  </div>
                  <Badge :class="['text-[9px] uppercase tracking-widest font-bold', typeColor(m.type)]" variant="outline">
                    {{ typeLabel(m.type) }}
                  </Badge>
                </div>
                <div class="flex justify-between items-center bg-zinc-50 dark:bg-zinc-900/50 p-2 rounded-lg border border-zinc-100 dark:border-zinc-800">
                  <div class="flex flex-col">
                    <span class="text-[9px] text-muted-foreground uppercase font-black tracking-tighter">Lokasi</span>
                    <span class="text-[10px] font-bold">
                      <template v-if="m.type === 'TRANSFER'">
                        {{ (m.from_location_type || '').toUpperCase() }} → {{ (m.to_location_type || '').toUpperCase() }}
                      </template>
                      <template v-else>
                        {{ (m.to_location_type || m.from_location_type || '-').toUpperCase() }}
                      </template>
                    </span>
                  </div>
                  <div class="text-right">
                    <span class="text-[9px] text-muted-foreground uppercase font-black tracking-tighter">Qty</span>
                    <p :class="['text-sm font-black', isIncoming(m) ? 'text-emerald-600' : 'text-red-600']">
                      {{ isIncoming(m) ? '+' : '-' }}{{ m.qty }}
                    </p>
                  </div>
                </div>
                <div class="flex items-center gap-1.5 text-[10px] text-muted-foreground">
                  <Calendar class="h-3 w-3" /> {{ formatDate(m.created_at) }}
                </div>
              </div>
            </div>

            <!-- Desktop Table -->
            <div class="hidden md:block overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="border-b border-zinc-100 dark:border-zinc-800">
                    <th class="pl-5 py-4 text-left text-xs font-bold uppercase tracking-widest text-zinc-400">Waktu</th>
                    <th class="py-4 text-left text-xs font-bold uppercase tracking-widest text-zinc-400">Produk</th>
                    <th class="py-4 text-left text-xs font-bold uppercase tracking-widest text-zinc-400">Dari → Ke</th>
                    <th class="py-4 text-center text-xs font-bold uppercase tracking-widest text-zinc-400">Tipe</th>
                    <th class="py-4 text-right text-xs font-bold uppercase tracking-widest text-zinc-400">Qty</th>
                    <th class="pr-5 py-4 text-right text-xs font-bold uppercase tracking-widest text-zinc-400">Referensi</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="m in paginatedMutations" :key="m.id" class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/50 dark:hover:bg-zinc-900/30 transition-colors group">
                    <td class="pl-5 py-4 whitespace-nowrap">
                      <div class="flex items-center gap-2">
                        <Calendar class="h-3.5 w-3.5 text-zinc-400" />
                        <span class="text-xs font-medium">{{ formatDate(m.created_at) }}</span>
                      </div>
                    </td>
                    <td class="py-4">
                      <div class="flex items-center gap-3">
                        <div class="w-8 h-8 rounded-lg bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center shrink-0 border border-zinc-200 dark:border-zinc-700">
                          <Package class="h-4 w-4 text-zinc-500" />
                        </div>
                        <div class="flex flex-col">
                          <span class="text-sm font-bold text-zinc-800 dark:text-zinc-200">{{ m.product?.name || '-' }}</span>
                          <span class="text-[10px] font-mono text-muted-foreground">{{ m.product?.sku || '-' }}</span>
                        </div>
                      </div>
                    </td>
                    <td class="py-4">
                      <div class="flex items-center gap-1.5 text-xs">
                        <template v-if="m.from_location_type">
                          <component :is="m.from_location_type === 'WAREHOUSE' ? Warehouse : Building2" class="h-3.5 w-3.5 text-zinc-400" />
                          <span class="text-muted-foreground">{{ m.from_location_type }}</span>
                          <span class="text-zinc-300">→</span>
                        </template>
                        <component :is="(m.to_location_type || '').toUpperCase() === 'WAREHOUSE' ? Warehouse : Building2" class="h-3.5 w-3.5 text-primary" />
                        <span class="font-medium">{{ m.to_location_type || '-' }}</span>
                      </div>
                    </td>
                    <td class="py-4 text-center">
                      <Badge :class="['text-[9px] uppercase tracking-widest font-black px-2 py-0.5', typeColor(m.type)]" variant="outline">
                        {{ typeLabel(m.type) }}
                      </Badge>
                    </td>
                    <td class="py-4 text-right">
                      <div class="flex items-center justify-end gap-1.5">
                        <ArrowUpRight v-if="isIncoming(m)" class="h-3 w-3 text-emerald-500" />
                        <ArrowDownLeft v-else class="h-3 w-3 text-red-500" />
                        <span :class="['text-sm font-black', isIncoming(m) ? 'text-emerald-600' : 'text-red-600']">
                          {{ isIncoming(m) ? '+' : '-' }}{{ m.qty }}
                        </span>
                      </div>
                    </td>
                    <td class="pr-5 py-4 text-right font-mono text-[10px] text-muted-foreground group-hover:text-primary transition-colors">
                      {{ m.reference_type ? `${m.reference_type} #${m.reference_id || ''}` : '-' }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <DataTablePagination 
            v-if="filteredMutations.length > 0 && !loading" 
            :page="page" 
            :page-size="pageSize" 
            :total="filteredMutations.length" 
            @update:page="page = $event" 
            @update:page-size="pageSize = $event; page = 1" 
          />
        </CardContent>
      </Card>
    </div>
  </AppLayout>
</template>
