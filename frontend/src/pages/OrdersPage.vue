<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/Input.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'
import CustomSelect from '@/components/ui/CustomSelect.vue'
import { Loader2, X, Eye, ShoppingBag, Banknote, ArrowRightLeft, Printer, RotateCcw, Ban, CheckCircle2, Building2, Warehouse } from 'lucide-vue-next'
import api from '@/lib/api'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()
const auth = useAuthStore()
const { isAdmin } = storeToRefs(auth)

        // Cek apakah user adalah owner (bisa konfirmasi transfer)
const isOwner = computed(() => {
  const roles = auth.user?.roles || []
  return isAdmin.value || roles.some(r => {
    const slug = typeof r === 'string' ? r : (r.slug || r.name || '')
    return slug.toLowerCase() === 'owner'
  })
})

// ─── State ───────────────────────────────────────────────────────────────────
const orders = ref([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('all')
const branchFilter = ref('all')
const branches = ref([])
const page = ref(1)
const pageSize = ref(10)
const detailDrawer = ref({ show: false, order: null })
const actionLoading = ref(false)

// Return modal
const showReturnModal = ref(false)
const returnItems = ref([])
const returnReason = ref('')
const submittingReturn = ref(false)
// Return location
const returnLocationType = ref('BRANCH') // 'BRANCH' atau 'WAREHOUSE'
const returnLocationId = ref(null)
const warehouses = ref([])
const branchesList = ref([])
const loadingLocations = ref(false)

// ─── Actions ──────────────────────────────────────────────────────────────────
async function fetchOrders() {
  loading.value = true
  try {
    const url = isAdmin.value ? '/api/v1/orders/admin' : '/api/v1/orders'
    const res = await api.get(url)
    const data = res.data?.data ?? res.data
    orders.value = Array.isArray(data) ? data : (data?.content || [])
  } catch (err) {
    toast.error('Gagal memuat data order')
  } finally {
    loading.value = false
  }
}

// ─── Computed ─────────────────────────────────────────────────────────────────
const filteredOrders = computed(() => {
  let result = [...orders.value].sort((a, b) => {
    // Terbaru di atas — sort by id descending (id selalu increment)
    return (b.id || 0) - (a.id || 0)
  })
  if (statusFilter.value !== 'all') {
    result = result.filter(o => {
      const hasPendingTransfer = o.payments?.some(p => p.method === 'TRANSFER' && p.status === 'PENDING')
      if (statusFilter.value === 'pending_transfer') return hasPendingTransfer
      return o.status?.toLowerCase() === statusFilter.value.toLowerCase()
    })
  }
  if (branchFilter.value !== 'all') {
    result = result.filter(o => {
      const bId = o.branchId || o.branch?.id
      return String(bId) === String(branchFilter.value)
    })
  }
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(o =>
      o.orderNumber.toLowerCase().includes(q) ||
      (o.createdBy?.username && o.createdBy.username.toLowerCase().includes(q)) ||
      (o.cashierName && o.cashierName.toLowerCase().includes(q)) ||
      ((o.branchName || o.branch?.name || '').toLowerCase().includes(q))
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

async function cancelOrder(order) {
  const ok = await confirm({ title: 'Batalkan Order?', description: `Order ${order.orderNumber} akan dibatalkan dan stok dikembalikan.` })
  if (!ok) return
  actionLoading.value = true
  try {
    await api.patch(`/api/v1/orders/${order.id}/cancel`)
    toast.success('Order berhasil dibatalkan.')
    await fetchOrders()
    closeDetail()
  } catch (err) {
    toast.error(err.response?.data?.data?.message || err.response?.data?.message || 'Gagal membatalkan order.')
  } finally { actionLoading.value = false }
}

async function verifyTransfer(paymentId) {
  actionLoading.value = true
  try {
    await api.patch(`/api/v1/payments/${paymentId}/verify`)
    toast.success('Transfer berhasil dikonfirmasi.')
    await fetchOrders()
    closeDetail()
  } catch (err) {
    toast.error(err.response?.data?.data?.message || err.response?.data?.message || 'Gagal mengkonfirmasi transfer.')
  } finally { actionLoading.value = false }
}

function openReturnModal(order) {
  returnItems.value = (order.items || []).map(i => ({ ...i, returnQty: 0 }))
  returnReason.value = ''
  // Default ke branch asal order
  returnLocationType.value = 'BRANCH'
  returnLocationId.value = order.branch?.id || order.branchId || null
  showReturnModal.value = true
  fetchLocations()
}
function closeReturnModal() { showReturnModal.value = false }

async function fetchLocations() {
  loadingLocations.value = true
  try {
    const [whRes, brRes] = await Promise.all([
      api.get('/api/v1/warehouses'),
      api.get('/api/v1/branches')
    ])
    const whRaw = whRes.data?.data
    warehouses.value = Array.isArray(whRaw) ? whRaw : (whRaw?.content || [])
    const brRaw = brRes.data?.data
    branchesList.value = Array.isArray(brRaw) ? brRaw : (brRaw?.content || [])
  } catch (err) {
    console.error('Gagal memuat lokasi:', err)
  } finally {
    loadingLocations.value = false
  }
}

async function submitReturn() {
  const toReturn = returnItems.value.filter(i => i.returnQty > 0)
  if (!toReturn.length) { toast.error('Pilih minimal satu item untuk diretur.'); return }
  if (returnLocationType.value === 'WAREHOUSE' && !returnReason.value?.trim()) {
    toast.error('Alasan retur wajib diisi saat mengembalikan ke gudang.'); return
  }
  if (!returnLocationId.value) { toast.error('Pilih tujuan lokasi pengembalian stok.'); return }
  submittingReturn.value = true
  try {
    await api.post(`/api/v1/orders/${detailDrawer.value.order.id}/return`, {
      items: toReturn.map(i => ({ orderItemId: i.id, qtyReturn: i.returnQty, reason: returnReason.value || null })),
      returnLocationType: returnLocationType.value,
      returnLocationId: returnLocationId.value
    })
    const dest = returnLocationType.value === 'WAREHOUSE' ? 'gudang' : 'cabang'
    toast.success(`Retur berhasil diproses. Stok dikembalikan ke ${dest}.`)
    closeReturnModal()
    await fetchOrders()
    closeDetail()
  } catch (err) {
    const msg = err.response?.data?.data?.message
      || err.response?.data?.message
      || err.message
      || 'Gagal memproses retur.'
    toast.error(msg)
  } finally { submittingReturn.value = false }
}

function printReceipt(order) {
  window.print()
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
function formatCurrency(v) {
  if (v == null) return '-'
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(v)
}
function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}
function statusColor(s, payments) {
  const st = String(s).toLowerCase()
  // Canceled selalu merah, tidak peduli payment
  if (st === 'cancelled' || st === 'canceled') return 'bg-red-50 text-red-700 border-red-200 dark:bg-red-900/20 dark:text-red-400 dark:border-red-800/50'
  if (st === 'return') return 'bg-violet-50 text-violet-700 border-violet-200 dark:bg-violet-900/20 dark:text-violet-400 dark:border-violet-800/50'
  // Jika ada payment transfer yang masih pending
  if (payments?.length && payments.some(p => p.method === 'TRANSFER' && p.status === 'PENDING')) {
    return 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-900/20 dark:text-amber-400 dark:border-amber-800/50'
  }
  if (st === 'paid') return 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-900/20 dark:text-emerald-400 dark:border-emerald-800/50'
  if (st === 'draft') return 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-900/20 dark:text-amber-400 dark:border-amber-800/50'
  return 'bg-zinc-100 text-zinc-700 border-zinc-200 dark:bg-zinc-800 dark:text-zinc-400'
}
function statusLabel(s, payments) {
  const st = String(s).toLowerCase()
  // Canceled selalu "Dibatalkan", tidak peduli payment
  if (st === 'cancelled' || st === 'canceled') return 'Dibatalkan'
  if (st === 'return') return 'Retur'
  // Jika ada payment transfer yang masih pending
  if (payments?.length && payments.some(p => p.method === 'TRANSFER' && p.status === 'PENDING')) {
    return 'Menunggu Konfirmasi'
  }
  if (st === 'paid') return 'Lunas'
  if (st === 'draft') return 'Menunggu Pembayaran'
  return s
}

function statusDotClass(s, payments) {
  const st = String(s || '').toLowerCase()
  if (st === 'cancelled' || st === 'canceled') return 'bg-red-500 shadow-[0_0_8px_#ef4444]'
  if (st === 'return') return 'bg-violet-500 shadow-[0_0_8px_#8b5cf6]'
  if (payments?.length && payments.some(p => p.method === 'TRANSFER' && p.status === 'PENDING')) {
    return 'bg-amber-500 shadow-[0_0_8px_#f59e0b]'
  }
  if (st === 'paid') return 'bg-emerald-500 shadow-[0_0_8px_#10b981]'
  if (st === 'draft') return 'bg-amber-500 shadow-[0_0_8px_#f59e0b]'
  return 'bg-zinc-400 shadow-[0_0_8px_#a1a1aa]'
}

function paymentStatusLabel(status, orderStatus) {
  // Jika order dibatalkan, payment juga dianggap batal
  const os = String(orderStatus || '').toUpperCase()
  if (os === 'CANCELED' || os === 'CANCELLED') return 'Dibatalkan'
  const st = String(status).toUpperCase()
  if (st === 'VERIFIED') return 'Terverifikasi'
  if (st === 'PENDING') return 'Menunggu Konfirmasi'
  if (st === 'FAILED') return 'Gagal'
  return status
}

function paymentStatusColor(status, orderStatus) {
  const os = String(orderStatus || '').toUpperCase()
  if (os === 'CANCELED' || os === 'CANCELLED') return 'bg-red-50 text-red-700 border-red-200 dark:bg-red-900/20 dark:text-red-400'
  const st = String(status).toUpperCase()
  if (st === 'VERIFIED') return 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-900/20 dark:text-emerald-400'
  if (st === 'PENDING') return 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-900/20 dark:text-amber-400'
  if (st === 'FAILED') return 'bg-red-50 text-red-700 border-red-200 dark:bg-red-900/20 dark:text-red-400'
  return 'bg-zinc-100 text-zinc-600 border-zinc-200'
}

const pendingTransferPayment = computed(() => {
  const order = detailDrawer.value.order
  if (!order?.payments?.length) return null
  return order.payments.find(p =>
    p.method?.toUpperCase() === 'TRANSFER' && p.status?.toUpperCase() === 'PENDING'
  ) || null
})

async function fetchBranches() {
  try {
    const url = isAdmin.value ? '/api/v1/branches/admin' : '/api/v1/branches'
    const res = await api.get(url)
    const raw = res.data?.data
    branches.value = Array.isArray(raw) ? raw : (raw?.content || [])
  } catch {
  }
}

watch([statusFilter, branchFilter, searchQuery], () => { page.value = 1 })

// Filter Cabang
const statusOptions = computed(() => [
  { value: 'all', label: 'Semua Status' },
  { value: 'paid', label: 'Lunas' },
  { value: 'pending_transfer', label: 'Menunggu Konfirmasi' },
  { value: 'draft', label: 'Draf' },
  { value: 'return', label: 'Retur' },
  { value: 'canceled', label: 'Batal' },
])

const branchOptions = computed(() => [
  { value: 'all', label: 'Semua Cabang' },
  ...branches.value.map(b => ({ value: b.id, label: b.name }))
])

onMounted(() => {
  fetchOrders()
  fetchBranches()
})
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <!-- Header -->
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Riwayat Pesanan</h1>
          <p class="text-muted-foreground text-sm mt-1">Lihat riwayat transaksi penjualan dari Terminal POS.</p>
        </div>
        <div class="flex items-center gap-2 w-full md:w-auto">
          <div class="w-full sm:w-64">
            <DataTableSearch v-model="searchQuery" placeholder="Cari pesanan, kasir atau cabang..." />
          </div>
          <CustomSelect
            v-model="branchFilter"
            :options="branchOptions"
            placeholder="Semua Cabang"
            :show-icon="false"
            class="w-full sm:w-40"
          />
          <CustomSelect
            v-model="statusFilter"
            :options="statusOptions"
            placeholder="Semua Status"
            :show-icon="false"
            class="w-full sm:w-40"
          />
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
                  <div class="inline-flex items-center gap-1.5 text-xs font-semibold text-zinc-700 dark:text-zinc-300">
                    <span :class="['w-1.5 h-1.5 rounded-full animate-pulse shrink-0', statusDotClass(o.status, o.payments)]"></span>
                    <span>{{ statusLabel(o.status, o.payments) }}</span>
                  </div>                </div>
                <div class="flex justify-between items-end">
                  <div>
                    <p class="text-[10px] text-muted-foreground uppercase font-bold tracking-tighter">{{ o.branchName || o.branch?.name || '-' }}</p>
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
                    <th class="pl-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">No. Pesanan</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Tgl &amp; Waktu</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Nama Pembeli</th>
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
                    <td class="py-3 text-xs text-muted-foreground whitespace-nowrap">{{ formatDate(o.createdAt) }}</td>
                    <td class="py-3 text-xs font-medium">{{ o.buyerName || '-' }}</td>
                    <td class="py-3 text-xs font-medium">{{ o.branchName || o.branch?.name || '-' }}</td>
                    <td class="py-3 text-xs">{{ formatCurrency(o.subtotal) }}</td>
                    <td class="py-3 text-xs text-red-500">{{ o.discountAmount > 0 ? '-' + formatCurrency(o.discountAmount) : '-' }}</td>
                    <td class="py-3 text-sm font-black">{{ formatCurrency(o.total) }}</td>
                    <td class="py-3 text-center">
                      <div class="inline-flex items-center gap-1.5 text-xs font-semibold text-zinc-700 dark:text-zinc-300">
                        <span :class="['w-2 h-2 rounded-full animate-pulse shrink-0', statusDotClass(o.status, o.payments)]"></span>
                        <span>{{ statusLabel(o.status, o.payments) }}</span>
                      </div>
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
              <div class="inline-flex items-center gap-2 text-sm font-semibold text-zinc-700 dark:text-zinc-300">
                <span :class="['w-2.5 h-2.5 rounded-full animate-pulse shrink-0', statusDotClass(detailDrawer.order.status, detailDrawer.order.payments)]"></span>
                <span>{{ statusLabel(detailDrawer.order.status, detailDrawer.order.payments) }}</span>
              </div>
              <span class="text-xs text-muted-foreground">{{ formatDate(detailDrawer.order.createdAt) }}</span>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="p-3 rounded-xl border border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30">
                <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-1">Kasir / User</p>
                <p class="text-sm font-semibold">{{ detailDrawer.order.cashierName || detailDrawer.order.createdBy?.username || '-' }}</p>
              </div>
              <div class="p-3 rounded-xl border border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30">
                <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-1">Lokasi Cabang</p>
                <p class="text-sm font-semibold">{{ detailDrawer.order.branchName || detailDrawer.order.branch?.name || '-' }}</p>
              </div>
              <div class="p-3 rounded-xl border border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30">
                <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-1">Nama Pembeli</p>
                <p class="text-sm font-semibold">{{ detailDrawer.order.buyerName || '-' }}</p>
              </div>
              <div class="p-3 rounded-xl border border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30">
                <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-1">Waktu Pembelian</p>
                <p class="text-sm font-semibold">{{ formatDate(detailDrawer.order.createdAt) }}</p>
              </div>
            </div>

            <!-- Items -->
            <div class="space-y-3">
              <h4 class="text-[10px] font-bold uppercase tracking-widest text-zinc-500 border-b pb-2">Item Belanja</h4>
              <div class="space-y-2">
                <div v-for="(item, idx) in detailDrawer.order.items" :key="idx" class="flex justify-between items-center p-3 rounded-xl border border-zinc-100 dark:border-zinc-800 bg-white dark:bg-zinc-900/50">
                  <div>
                    <p class="text-sm font-bold">{{ item.productName || item.product?.name || 'Produk' }}</p>
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

            <!-- Payment Info -->
            <div v-if="detailDrawer.order.payments?.length" class="space-y-2">
              <h4 class="text-[10px] font-bold uppercase tracking-widest text-zinc-500 border-b pb-2">Pembayaran</h4>
              <div v-for="(pay, idx) in detailDrawer.order.payments" :key="idx"
                class="flex flex-col gap-2.5 p-3 rounded-xl border border-zinc-100 dark:border-zinc-800 bg-white dark:bg-zinc-900/50">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2.5">
                    <div class="p-2 rounded-lg bg-zinc-100 dark:bg-zinc-800">
                      <Banknote v-if="pay.method?.toUpperCase() === 'CASH'" class="h-4 w-4 text-zinc-500" />
                      <ArrowRightLeft v-else class="h-4 w-4 text-zinc-500" />
                    </div>
                    <div>
                      <p class="text-sm font-bold">{{ pay.method === 'CASH' ? 'Tunai' : 'Transfer' }}</p>
                      <p class="text-[11px] text-muted-foreground">{{ formatCurrency(pay.amount) }}</p>
                    </div>
                  </div>
                  <Badge :class="['text-[9px] font-bold uppercase tracking-wider px-2 py-0.5', paymentStatusColor(pay.status, detailDrawer.order.status)]" variant="outline">
                    {{ paymentStatusLabel(pay.status, detailDrawer.order.status) }}
                  </Badge>
                </div>
                <template v-if="pay.method?.toUpperCase() === 'CASH' && pay.cashTendered != null">
                  <div class="border-t border-zinc-100 dark:border-zinc-800 pt-2.5 space-y-1.5">
                    <div class="flex items-center justify-between text-xs">
                      <span class="text-zinc-500">Uang Diterima</span>
                      <span class="font-semibold text-zinc-800 dark:text-zinc-200">{{ formatCurrency(pay.cashTendered) }}</span>
                    </div>
                    <div class="flex items-center justify-between text-xs px-2 py-1.5 rounded-lg"
                      :class="pay.changeDue > 0
                        ? 'bg-emerald-50 dark:bg-emerald-900/20'
                        : 'bg-zinc-50 dark:bg-zinc-800/40'">
                      <span :class="pay.changeDue > 0 ? 'text-emerald-700 dark:text-emerald-400 font-bold' : 'text-zinc-500'">
                        Kembalian
                      </span>
                      <span :class="pay.changeDue > 0 ? 'font-black text-emerald-700 dark:text-emerald-400' : 'font-semibold text-zinc-500'">
                        {{ formatCurrency(pay.changeDue ?? 0) }}
                      </span>
                    </div>
                  </div>
                </template>
                <template v-if="pay.method?.toUpperCase() === 'TRANSFER'">
                  <div v-if="pay.bankName || pay.referenceNo" class="border-t border-zinc-100 dark:border-zinc-800 pt-2.5 space-y-1.5">
                    <div v-if="pay.bankName" class="flex items-center justify-between text-xs">
                      <span class="text-zinc-500">Bank</span>
                      <span class="font-semibold">{{ pay.bankName }}</span>
                    </div>
                    <div v-if="pay.referenceNo" class="flex items-center justify-between text-xs">
                      <span class="text-zinc-500">No. Referensi</span>
                      <span class="font-mono font-semibold">{{ pay.referenceNo }}</span>
                    </div>
                  </div>
                </template>
              </div>
            </div>

            <!-- Payment Info (If available via relationship) -->
            <div v-if="detailDrawer.order.notes" class="bg-primary/5 p-3 rounded-lg border border-primary/10">
              <p class="text-[10px] font-bold text-primary uppercase tracking-wider mb-1">Tipe Pesanan / Catatan</p>
              <p class="text-xs font-medium">{{ detailDrawer.order.notes }}</p>
            </div>

            <!-- Info Retur -->
            <div v-if="detailDrawer.order.returnInfo" class="space-y-3">
              <h4 class="text-[10px] font-bold uppercase tracking-widest text-violet-500 border-b border-violet-100 dark:border-violet-900/40 pb-2 flex items-center gap-1.5">
                <RotateCcw class="h-3.5 w-3.5" /> Detail Retur
              </h4>
              <div class="bg-violet-50/60 dark:bg-violet-950/20 rounded-xl border border-violet-100 dark:border-violet-900/40 p-4 space-y-3">
                <div class="flex justify-between text-xs">
                  <span class="text-muted-foreground font-medium">Waktu Retur</span>
                  <span class="font-semibold">{{ formatDate(detailDrawer.order.returnInfo.returnedAt) }}</span>
                </div>
                <div class="space-y-2">
                  <p class="text-[10px] font-bold text-violet-600 dark:text-violet-400 uppercase tracking-wider">Item Diretur</p>
                  <div v-for="(item, idx) in detailDrawer.order.returnInfo.items" :key="idx"
                    class="flex items-start justify-between p-2.5 rounded-lg border border-violet-100 dark:border-violet-900/30 bg-white/60 dark:bg-zinc-900/40 gap-2">
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-bold text-zinc-900 dark:text-zinc-100 truncate">{{ item.productName }}</p>
                      <p class="text-[11px] text-muted-foreground mt-0.5">Qty: {{ item.qtyReturn }} pcs</p>
                      <p v-if="item.reason" class="text-[11px] text-violet-600 dark:text-violet-400 mt-1 italic">
                        Alasan: {{ item.reason }}
                      </p>
                    </div>
                    <span class="text-sm font-black text-violet-700 dark:text-violet-300 shrink-0">{{ formatCurrency(item.refundAmount) }}</span>
                  </div>
                </div>
                <div class="flex justify-between items-center pt-2 border-t border-violet-100 dark:border-violet-900/40">
                  <span class="text-xs font-bold text-violet-600 dark:text-violet-400 uppercase tracking-wider">Total Refund</span>
                  <span class="text-base font-black text-violet-700 dark:text-violet-300">{{ formatCurrency(detailDrawer.order.returnInfo.totalRefund) }}</span>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex flex-col gap-2 pt-1">
              <!-- Cetak Struk -->
              <button @click="printReceipt(detailDrawer.order)"
                class="w-full h-10 rounded-xl border border-zinc-200 dark:border-zinc-700 flex items-center justify-center gap-2 text-sm font-bold hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors">
                <Printer class="h-4 w-4" /> Cetak Struk
              </button>

              <!-- Konfirmasi Transfer — hanya owner -->
              <button v-if="pendingTransferPayment && isOwner"
                :disabled="actionLoading"
                @click="verifyTransfer(pendingTransferPayment.id)"
                class="w-full h-10 rounded-xl bg-blue-600 hover:bg-blue-700 text-white flex items-center justify-center gap-2 text-sm font-bold transition-colors disabled:opacity-50">
                <CheckCircle2 class="h-4 w-4" /> Konfirmasi Transfer
              </button>

              <!-- Retur Barang — hanya jika PAID dan tidak ada transfer pending -->
              <button v-if="detailDrawer.order.status?.toUpperCase() === 'PAID' && !pendingTransferPayment"
                :disabled="actionLoading"
                @click="openReturnModal(detailDrawer.order)"
                class="w-full h-10 rounded-xl border border-purple-300 dark:border-purple-700 text-purple-700 dark:text-purple-400 flex items-center justify-center gap-2 text-sm font-bold hover:bg-purple-50 dark:hover:bg-purple-900/20 transition-colors disabled:opacity-50">
                <RotateCcw class="h-4 w-4" /> Retur Barang
              </button>

              <!-- Batalkan Order -->
              <button v-if="['DRAFT','PAID'].includes(detailDrawer.order.status?.toUpperCase())"
                :disabled="actionLoading"
                @click="cancelOrder(detailDrawer.order)"
                class="w-full h-10 rounded-xl border border-red-300 dark:border-red-700 text-red-600 dark:text-red-400 flex items-center justify-center gap-2 text-sm font-bold hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors disabled:opacity-50">
                <Ban class="h-4 w-4" /> Batalkan Order
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
    <!-- Return Modal -->
    <Teleport to="body">
      <Transition name="fade"><div v-if="showReturnModal" class="fixed inset-0 z-[60] bg-black/50 backdrop-blur-sm" @click="closeReturnModal" /></Transition>
      <Transition name="scale">
        <div v-if="showReturnModal" class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none">
          <div class="bg-white dark:bg-zinc-900 rounded-[24px] shadow-2xl w-full max-w-md border border-zinc-200 dark:border-zinc-800 pointer-events-auto overflow-hidden flex flex-col max-h-[90vh]">
            <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
              <div>
                <h3 class="font-black text-[15px]">Form Retur Barang</h3>
                <p class="text-xs text-muted-foreground mt-0.5">Pilih item dan jumlah yang akan diretur</p>
              </div>
              <button @click="closeReturnModal" class="p-1.5 rounded-full hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-500">
                <X class="h-4 w-4" />
              </button>
            </div>
            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-4">
              <div class="space-y-2">
                <p class="text-[11px] font-bold text-zinc-400 uppercase tracking-wider">Item Pesanan</p>
                <div v-for="(item, idx) in returnItems" :key="idx"
                  class="flex items-center justify-between p-3 rounded-xl border border-zinc-100 dark:border-zinc-800 bg-white dark:bg-zinc-900/50 gap-3">
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-bold truncate">{{ item.productName || 'Produk' }}</p>
                    <p class="text-[11px] text-muted-foreground">Qty order: {{ item.qty }}</p>
                  </div>
                  <div class="flex items-center gap-2 shrink-0">
                    <span class="text-[11px] font-bold text-zinc-500">Retur:</span>
                    <input v-model.number="item.returnQty" type="number" min="0" :max="item.qty"
                      class="w-16 h-8 text-center text-sm font-bold rounded-lg border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 outline-none focus:ring-2 focus:ring-primary/20" />
                  </div>
                </div>
              </div>

              <!-- ── Tujuan Pengembalian Stok ── -->
              <div class="space-y-3">
                <p class="text-[11px] font-bold text-zinc-400 uppercase tracking-wider">Tujuan Pengembalian Stok <span class="text-destructive normal-case">*</span></p>
                <!-- Toggle BRANCH / WAREHOUSE -->
                <div class="grid grid-cols-2 gap-2">
                  <button type="button"
                    @click="returnLocationType = 'BRANCH'; returnLocationId = null"
                    :class="[
                      'h-12 rounded-xl border-2 text-sm font-bold flex flex-col items-center justify-center gap-0.5 transition-all',
                      returnLocationType === 'BRANCH'
                        ? 'border-blue-500 bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-400 dark:border-blue-500'
                        : 'border-zinc-200 dark:border-zinc-700 text-zinc-500 hover:border-zinc-300 dark:hover:border-zinc-600'
                    ]">
                    <Building2 class="h-4 w-4" />
                    <span class="text-[11px]">Cabang</span>
                  </button>
                  <button type="button"
                    @click="returnLocationType = 'WAREHOUSE'; returnLocationId = null"
                    :class="[
                      'h-12 rounded-xl border-2 text-sm font-bold flex flex-col items-center justify-center gap-0.5 transition-all',
                      returnLocationType === 'WAREHOUSE'
                        ? 'border-amber-500 bg-amber-50 text-amber-700 dark:bg-amber-900/20 dark:text-amber-400 dark:border-amber-500'
                        : 'border-zinc-200 dark:border-zinc-700 text-zinc-500 hover:border-zinc-300 dark:hover:border-zinc-600'
                    ]">
                    <Warehouse class="h-4 w-4" />
                    <span class="text-[11px]">Gudang</span>
                  </button>
                </div>

                <!-- Info tooltip -->
                <div :class="[
                  'flex items-start gap-2 px-3 py-2.5 rounded-xl text-[11px]',
                  returnLocationType === 'WAREHOUSE'
                    ? 'bg-amber-50 text-amber-700 dark:bg-amber-900/20 dark:text-amber-400'
                    : 'bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-400'
                ]">
                  <component :is="returnLocationType === 'WAREHOUSE' ? Warehouse : Building2" class="h-3.5 w-3.5 mt-0.5 shrink-0" />
                  <span v-if="returnLocationType === 'WAREHOUSE'">
                    Stok akan masuk ke <strong>Gudang</strong>. Barang tidak akan muncul di stok kasir sampai ditransfer ke cabang.
                  </span>
                  <span v-else>
                    Stok akan masuk ke <strong>Cabang</strong> dan langsung tersedia di stok kasir.
                  </span>
                </div>

                <!-- Pilih Gudang -->
                <div v-if="returnLocationType === 'WAREHOUSE'" class="space-y-1.5">
                  <p class="text-[11px] font-medium text-zinc-500">Pilih Gudang</p>
                  <div v-if="loadingLocations" class="flex items-center gap-2 text-xs text-zinc-400 py-2">
                    <Loader2 class="h-3.5 w-3.5 animate-spin" /> Memuat data gudang...
                  </div>
                  <select v-else v-model="returnLocationId"
                    class="w-full h-10 rounded-xl border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 px-3 text-sm outline-none focus:ring-2 focus:ring-amber-400/30">
                    <option :value="null" disabled>-- Pilih Gudang --</option>
                    <option v-for="wh in warehouses" :key="wh.id" :value="wh.id">{{ wh.name }}</option>
                  </select>
                </div>

                <!-- Pilih Cabang -->
                <div v-if="returnLocationType === 'BRANCH'" class="space-y-1.5">
                  <p class="text-[11px] font-medium text-zinc-500">Pilih Cabang</p>
                  <div v-if="loadingLocations" class="flex items-center gap-2 text-xs text-zinc-400 py-2">
                    <Loader2 class="h-3.5 w-3.5 animate-spin" /> Memuat data cabang...
                  </div>
                  <select v-else v-model="returnLocationId"
                    class="w-full h-10 rounded-xl border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 px-3 text-sm outline-none focus:ring-2 focus:ring-blue-400/30">
                    <option :value="null" disabled>-- Pilih Cabang --</option>
                    <option v-for="br in branchesList" :key="br.id" :value="br.id">{{ br.name }}</option>
                  </select>
                </div>
              </div>

              <div class="space-y-2">
                <p class="text-[11px] font-bold text-zinc-400 uppercase tracking-wider">
                  Alasan Retur
                  <span v-if="returnLocationType === 'WAREHOUSE'" class="text-destructive normal-case"> *</span>
                  <span v-else class="normal-case font-normal text-zinc-400"> (opsional)</span>
                </p>
                <textarea v-model="returnReason" rows="3" placeholder="Contoh: Expired, Barang rusak, Salah produk, Kemasan bocor..."
                  class="w-full rounded-xl border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 px-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-primary/20 resize-none" />
                <p class="text-[10px] text-muted-foreground">
                  <span v-if="returnLocationType === 'WAREHOUSE'">Alasan wajib diisi saat mengembalikan ke gudang.</span>
                  <span v-else>Alasan bersifat opsional saat mengembalikan ke cabang.</span>
                </p>
              </div>
            </div>
            <div class="px-6 py-4 border-t shrink-0 flex gap-2.5">
              <button @click="closeReturnModal" class="flex-1 h-11 rounded-[14px] border border-zinc-200 dark:border-zinc-700 font-bold text-sm hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors">
                Batal
              </button>
              <button @click="submitReturn" :disabled="submittingReturn"
                class="flex-1 h-11 rounded-[14px] bg-purple-600 hover:bg-purple-700 text-white font-bold text-sm flex items-center justify-center gap-2 transition-colors disabled:opacity-50">
                <Loader2 v-if="submittingReturn" class="h-4 w-4 animate-spin" />
                <RotateCcw v-else class="h-4 w-4" />
                Proses Retur
              </button>
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
.scale-enter-active, .scale-leave-active { transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1); }
.scale-enter-from, .scale-leave-to { opacity: 0; transform: scale(0.95) translateY(10px); }
</style>
