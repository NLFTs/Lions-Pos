<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/Input.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'
import { Loader2, X, Eye, ShoppingBag, Banknote, ArrowRightLeft } from 'lucide-vue-next'
import api from '@/lib/api'

const { can } = usePermission()
const { toast } = useToast()

// ─── State ───────────────────────────────────────────────────────────────────
const orders = ref([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('all')
const page = ref(1)
const pageSize = ref(10)
const detailDrawer = ref({ show: false, order: null })

// ─── Actions ──────────────────────────────────────────────────────────────────
async function fetchOrders() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/orders')
    const data = res.data.data
    orders.value = Array.isArray(data) ? data : (data.content || [])
  } catch (err) {
    toast.error('Gagal memuat data order')
  } finally {
    loading.value = false
  }
}

// ─── Computed ─────────────────────────────────────────────────────────────────
const filteredOrders = computed(() => {
  let result = orders.value
  if (statusFilter.value !== 'all') {
    result = result.filter(o => o.status?.toLowerCase() === statusFilter.value.toLowerCase())
  }
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(o => 
      o.orderNumber.toLowerCase().includes(q) || 
      (o.createdBy?.username && o.createdBy.username.toLowerCase().includes(q)) ||
      (o.branch?.name && o.branch.name.toLowerCase().includes(q))
    )
  }
  return result
})

const paginatedOrders = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredOrders.value.slice(start, start + pageSize.value)
})

function openDetail(order) { detailDrawer.value = { show: true, order } }
function closeDetail() { detailDrawer.value.show = false }

// ─── Helpers ──────────────────────────────────────────────────────────────────
function formatCurrency(v) {
  if (v == null) return '-'
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(v)
}
function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}
function statusColor(s) {
  const st = String(s).toLowerCase()
  if (st === 'paid') return 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-400 border-emerald-200 dark:border-emerald-800/40'
  if (st === 'cancelled' || st === 'canceled') return 'bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-400 border-red-200 dark:border-red-800/40'
  return 'bg-amber-50 text-amber-700 dark:bg-amber-900/20 dark:text-amber-400 border-amber-200 dark:border-amber-800/40'
}
function statusLabel(s) {
  const st = String(s).toLowerCase()
  if (st === 'paid') return 'Lunas'
  if (st === 'cancelled' || st === 'canceled') return 'Batal'
  return 'Draft'
}

onMounted(fetchOrders)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <!-- Header -->
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Riwayat Order</h1>
          <p class="text-muted-foreground text-sm mt-1">Lihat riwayat transaksi penjualan dari Terminal POS.</p>
        </div>
        <div class="flex items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-72">
            <DataTableSearch v-model="searchQuery" placeholder="Cari order, kasir atau cabang..." />
          </div>
          <select v-model="statusFilter" class="h-9 rounded-md border border-border bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-primary/20">
            <option value="all">Semua Status</option>
            <option value="paid">Lunas</option>
            <option value="draft">Draft</option>
            <option value="canceled">Batal</option>
          </select>
        </div>
      </div>

      <!-- Table Card -->
      <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm overflow-hidden">
        <CardContent class="p-0">
          <div v-if="loading" class="flex flex-col items-center justify-center py-24 gap-3">
            <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
            <p class="text-xs text-muted-foreground italic">Menghubungkan ke sistem...</p>
          </div>
          <div v-else-if="filteredOrders.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
            <ShoppingBag class="h-10 w-10 mb-3 opacity-20" />
            <p class="text-sm font-medium">Belum ada data order.</p>
          </div>
          <div v-else>
            <!-- Mobile -->
            <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
              <div v-for="o in paginatedOrders" :key="o.id" class="p-4 flex flex-col gap-2 hover:bg-zinc-50 dark:hover:bg-zinc-900/40 cursor-pointer" @click="openDetail(o)">
                <div class="flex items-center justify-between">
                  <span class="font-mono text-xs font-semibold text-zinc-700 dark:text-zinc-300">{{ o.orderNumber }}</span>
                  <Badge :class="['text-[9px] uppercase tracking-wider', statusColor(o.status)]" variant="outline">{{ statusLabel(o.status) }}</Badge>
                </div>
                <div class="flex justify-between items-end">
                  <div>
                    <p class="text-[10px] text-muted-foreground uppercase font-bold tracking-tighter">{{ o.branch?.name || '-' }}</p>
                    <p class="text-sm font-bold">{{ formatCurrency(o.total) }}</p>
                  </div>
                  <span class="text-[10px] text-muted-foreground">{{ formatDate(o.createdAt) }}</span>
                </div>
              </div>
            </div>
            <!-- Desktop -->
            <div class="hidden md:block overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="border-b border-zinc-100 dark:border-zinc-800">
                    <th class="pl-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">No. Order</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Tanggal</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Cabang</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Subtotal</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Diskon</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Total</th>
                    <th class="py-3 text-center text-xs font-semibold uppercase tracking-wide text-zinc-500">Status</th>
                    <th class="pr-5 py-3 text-right"></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="o in paginatedOrders" :key="o.id" class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors">
                    <td class="pl-5 py-3 font-mono text-xs font-bold text-primary">{{ o.orderNumber }}</td>
                    <td class="py-3 text-xs text-muted-foreground">{{ formatDate(o.createdAt) }}</td>
                    <td class="py-3 text-xs font-medium">{{ o.branch?.name || '-' }}</td>
                    <td class="py-3 text-xs">{{ formatCurrency(o.subtotal) }}</td>
                    <td class="py-3 text-xs text-red-500">{{ o.discountAmount > 0 ? '-' + formatCurrency(o.discountAmount) : '-' }}</td>
                    <td class="py-3 text-sm font-black">{{ formatCurrency(o.total) }}</td>
                    <td class="py-3 text-center">
                      <Badge :class="['text-[9px] uppercase tracking-widest font-bold', statusColor(o.status)]" variant="outline">{{ statusLabel(o.status) }}</Badge>
                    </td>
                    <td class="pr-5 py-3 text-right">
                      <Button variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-zinc-900" @click="openDetail(o)"><Eye class="h-4 w-4" /></Button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <DataTablePagination v-if="filteredOrders.length > 0 && !loading" :page="page" :page-size="pageSize" :total="filteredOrders.length" @update:page="page = $event" @update:page-size="pageSize = $event; page = 1" />
        </CardContent>
      </Card>
    </div>

    <!-- Detail Drawer -->
    <Teleport to="body">
      <Transition name="fade"><div v-if="detailDrawer.show" class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm" @click="closeDetail" /></Transition>
      <Transition name="slide-right">
        <div v-if="detailDrawer.show" class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[460px] bg-card shadow-2xl sm:border-l overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0 bg-muted/20">
            <div>
              <h3 class="font-semibold text-base">Detail Transaksi</h3>
              <p class="text-xs text-muted-foreground font-mono mt-0.5">{{ detailDrawer.order?.orderNumber }}</p>
            </div>
            <Button variant="ghost" size="icon" @click="closeDetail"><X class="h-4 w-4" /></Button>
          </div>
          
          <div v-if="detailDrawer.order" class="flex-1 overflow-y-auto px-6 py-5 space-y-6">
            <div class="flex items-center justify-between">
              <Badge :class="['text-[10px] uppercase tracking-widest px-3 py-1 font-bold', statusColor(detailDrawer.order.status)]" variant="outline">
                {{ statusLabel(detailDrawer.order.status) }}
              </Badge>
              <span class="text-xs text-muted-foreground">{{ formatDate(detailDrawer.order.createdAt) }}</span>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="p-3 rounded-xl border border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30">
                <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-1">Kasir / User</p>
                <p class="text-sm font-semibold">{{ detailDrawer.order.createdBy?.username || '-' }}</p>
              </div>
              <div class="p-3 rounded-xl border border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30">
                <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-1">Lokasi Cabang</p>
                <p class="text-sm font-semibold">{{ detailDrawer.order.branch?.name || '-' }}</p>
              </div>
            </div>

            <!-- Items -->
            <div class="space-y-3">
              <h4 class="text-[10px] font-bold uppercase tracking-widest text-zinc-500 border-b pb-2">Item Belanja</h4>
              <div class="space-y-2">
                <div v-for="(item, idx) in detailDrawer.order.items" :key="idx" class="flex justify-between items-center p-3 rounded-xl border border-zinc-100 dark:border-zinc-800 bg-white dark:bg-zinc-900/50">
                  <div>
                    <p class="text-sm font-bold">{{ item.product?.name || 'Produk' }}</p>
                    <p class="text-[11px] text-muted-foreground">{{ item.qty }} x {{ formatCurrency(item.unitPrice) }}</p>
                  </div>
                  <span class="text-sm font-black">{{ formatCurrency(item.subtotal) }}</span>
                </div>
              </div>
            </div>

            <!-- Summary -->
            <div class="p-4 rounded-xl bg-zinc-100/50 dark:bg-zinc-800/50 space-y-2.5">
              <div class="flex justify-between text-xs"><span class="text-muted-foreground">Subtotal</span><span class="font-medium">{{ formatCurrency(detailDrawer.order.subtotal) }}</span></div>
              <div v-if="detailDrawer.order.discountAmount > 0" class="flex justify-between text-xs text-red-500"><span>Diskon</span><span class="font-medium">-{{ formatCurrency(detailDrawer.order.discountAmount) }}</span></div>
              <div class="flex justify-between text-lg font-black border-t border-zinc-200 dark:border-zinc-700 pt-2"><span>Total</span><span class="text-primary">{{ formatCurrency(detailDrawer.order.total) }}</span></div>
            </div>

            <!-- Payment Info (If available via relationship) -->
            <div v-if="detailDrawer.order.notes" class="bg-primary/5 p-3 rounded-lg border border-primary/10">
              <p class="text-[10px] font-bold text-primary uppercase tracking-wider mb-1">Tipe Pesanan / Catatan</p>
              <p class="text-xs font-medium">{{ detailDrawer.order.notes }}</p>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-right-enter-active, .slide-right-leave-active { transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
.slide-right-enter-from, .slide-right-leave-to { transform: translateX(100%); }
</style>
