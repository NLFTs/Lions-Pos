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
import Button from '@/components/ui/button/Button.vue'
import api from '@/lib/api'
import {
  Loader2, Package, Building2, AlertTriangle, ShieldAlert,
  CheckCircle2, Trash2, X, History,
} from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.isAdmin)
const canUpdateStock = computed(() => authStore.isAdmin || can('stock_balance.update'))

// ─── State ───────────────────────────────────────────────────────────────────
const balances = ref([])
const loading = ref(false)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)
const branchFilter = ref('all')
const branches = ref([])

// ─── Computed ─────────────────────────────────────────────────────────────────
const filteredBalances = computed(() => {
  let r = balances.value
  if (branchFilter.value !== 'all') {
    r = r.filter(b => Number(b.locationId || b.location_id) === Number(branchFilter.value))
  }
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

const totalQty = computed(() => balances.value.reduce((sum, b) => sum + Number(b.qty || 0), 0))

function getBranchName(id) {
  const branch = branches.value.find(b => Number(b.id) === Number(id))
  return branch ? branch.name : `Cabang #${id}`
}

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('id-ID', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

// ─── Fetch ────────────────────────────────────────────────────────────────────
async function fetchQuarantine() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/stock-balances/quarantine')
    const data = res.data?.data || []
    balances.value = data.map(b => ({
      ...b,
      locationType: b.locationType || b.location_type,
      location_type: b.locationType || b.location_type,
      locationId: b.locationId || b.location_id,
      location_id: b.locationId || b.location_id,
      updatedAt: b.updatedAt || b.updated_at,
      updated_at: b.updatedAt || b.updated_at,
      updatedBy: b.updatedBy || b.updated_by,
      updated_by: b.updatedBy || b.updated_by,
    }))
  } catch (err) {
    toast.error('Gagal memuat data stok karantina.')
    console.error(err)
  } finally {
    loading.value = false
  }
}

async function fetchBranches() {
  try {
    const url = isAdmin.value ? '/api/v1/branches/admin' : '/api/v1/branches'
    const res = await api.get(url)
    const raw = res.data?.data || res.data
    branches.value = Array.isArray(raw) ? raw : (raw?.content || [])
  } catch (err) {
    console.error('Gagal memuat cabang:', err)
  }
}

// ─── Dispose ──────────────────────────────────────────────────────────────────
const showDisposeModal = ref(false)
const disposeTarget = ref(null)
const disposeQty = ref('')
const disposeNotes = ref('')
const disposeSaving = ref(false)

function openDispose(b) {
  disposeTarget.value = b
  disposeQty.value = b.qty?.toString() || ''
  disposeNotes.value = ''
  showDisposeModal.value = true
}

async function submitDispose() {
  if (!disposeTarget.value) return
  const qty = Number(disposeQty.value)
  if (!qty || qty <= 0) { toast.error('Jumlah dispose harus lebih dari 0.'); return }
  if (qty > disposeTarget.value.qty) { toast.error(`Maksimal qty: ${disposeTarget.value.qty}.`); return }
  disposeSaving.value = true
  try {
    await api.post(`/api/v1/stock-balances/${disposeTarget.value.id}/dispose`, null, {
      params: { qty, notes: disposeNotes.value?.trim() || null }
    })
    toast.success(`${qty} pcs berhasil di-dispose.`)
    showDisposeModal.value = false
    fetchQuarantine()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal dispose.')
  } finally {
    disposeSaving.value = false
  }
}

// ─── Approve ──────────────────────────────────────────────────────────────────
const showApproveModal = ref(false)
const approveTarget = ref(null)
const approveQty = ref('')
const approveNotes = ref('')
const approveSaving = ref(false)

function openApprove(b) {
  approveTarget.value = b
  approveQty.value = b.qty?.toString() || ''
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
    toast.success(`${qty} pcs berhasil approve ke stok aktif cabang.`)
    showApproveModal.value = false
    fetchQuarantine()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal approve.')
  } finally {
    approveSaving.value = false
  }
}

onMounted(async () => {
  await fetchBranches()
  await fetchQuarantine()
})
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <!-- Header -->
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-red-100 dark:bg-red-900/30 flex items-center justify-center shrink-0">
            <ShieldAlert class="w-5 h-5 text-red-600 dark:text-red-400" />
          </div>
          <div>
            <h1 class="text-2xl font-bold tracking-tight">Stok Karantina</h1>
            <p class="text-xs text-muted-foreground mt-0.5">
              {{ balances.length }} produk — {{ totalQty }} pcs menunggu tindakan
            </p>
          </div>
        </div>
        <div class="flex items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-64">
            <DataTableSearch v-model="searchQuery" placeholder="Cari produk atau SKU..." />
          </div>
          <select v-model="branchFilter"
            class="h-9 px-3 text-xs font-semibold border border-zinc-200 dark:border-zinc-800 bg-background rounded-md focus:outline-none focus:ring-2 focus:ring-primary/40 text-zinc-700 dark:text-zinc-300">
            <option value="all">Semua Cabang</option>
            <option v-for="b in branches" :key="b.id" :value="b.id">{{ b.name }}</option>
          </select>
          <button @click="fetchQuarantine"
            class="flex items-center gap-2 h-9 px-3 text-xs font-semibold border border-zinc-200 dark:border-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-900 rounded-md transition-colors">
            <History class="w-3.5 h-3.5" /> Segarkan
          </button>
        </div>
      </div>

      <!-- Warning Banner -->
      <div class="flex items-start gap-3 px-4 py-3 rounded-xl bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800/40 text-xs text-amber-700 dark:text-amber-400">
        <AlertTriangle class="h-4 w-4 shrink-0 mt-0.5" />
        <span>
          Stok karantina adalah barang yang <strong>belum siap jual</strong>. Gunakan <strong>Approve</strong> untuk memindahkan ke stok aktif cabang, atau <strong>Dispose</strong> untuk mengembalikan ke supplier.
        </span>
      </div>

      <!-- Stats -->
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <div class="rounded-xl border border-zinc-200 dark:border-zinc-800 bg-card p-4">
          <p class="text-[10px] font-bold text-zinc-400 uppercase tracking-wider mb-1">Total Produk</p>
          <p class="text-2xl font-black tracking-tight text-foreground">{{ balances.length }}</p>
        </div>
        <div class="rounded-xl border border-zinc-200 dark:border-zinc-800 bg-card p-4">
          <p class="text-[10px] font-bold text-zinc-400 uppercase tracking-wider mb-1">Total Stok</p>
          <p class="text-2xl font-black tracking-tight text-red-600 dark:text-red-400">{{ totalQty }} <span class="text-xs font-bold text-muted-foreground">PCS</span></p>
        </div>
        <div class="rounded-xl border border-zinc-200 dark:border-zinc-800 bg-card p-4">
          <p class="text-[10px] font-bold text-zinc-400 uppercase tracking-wider mb-1">Cabang Terkait</p>
          <p class="text-2xl font-black tracking-tight text-foreground">{{ new Set(balances.map(b => b.locationId || b.location_id)).size }}</p>
        </div>
      </div>

      <!-- Table -->
      <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-24">
            <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
          </div>
          <div v-else-if="filteredBalances.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
            <div class="w-14 h-14 rounded-full bg-zinc-100 dark:bg-zinc-900 flex items-center justify-center mb-3">
              <ShieldAlert class="h-7 w-7 opacity-40" />
            </div>
            <p class="text-sm font-medium">Tidak ada stok karantina.</p>
            <p class="text-xs mt-1">Semua stok sudah ditindaklanjuti.</p>
          </div>
          <div v-else>
            <div class="overflow-x-auto">
              <table class="w-full text-xs">
                <thead>
                  <tr class="border-b border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30 text-zinc-500">
                    <th class="pl-5 py-3 text-left font-semibold uppercase tracking-wider">Produk</th>
                    <th class="py-3 text-left font-semibold uppercase tracking-wider">Cabang</th>
                    <th class="py-3 text-center font-semibold uppercase tracking-wider">Stok</th>
                    <th class="py-3 text-left font-semibold uppercase tracking-wider">Diperbarui</th>
                    <th class="pr-5 py-3 text-right font-semibold uppercase tracking-wider">Aksi</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="b in paginatedBalances" :key="b.id"
                    class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors">
                    <td class="pl-5 py-3.5">
                      <div class="flex items-center gap-3">
                        <div class="w-8 h-8 rounded-md bg-red-100 dark:bg-red-900/30 flex items-center justify-center shrink-0 border border-red-200/50 dark:border-red-800/50">
                          <AlertTriangle class="h-4 w-4 text-red-500" />
                        </div>
                        <div>
                          <div class="font-bold text-zinc-900 dark:text-zinc-100">{{ b.product?.name }}</div>
                          <div class="text-[9px] font-mono text-muted-foreground">{{ b.product?.sku }}</div>
                        </div>
                      </div>
                    </td>
                    <td class="py-3.5">
                      <div class="flex items-center gap-1.5">
                        <Building2 class="h-3.5 w-3.5 text-zinc-400 shrink-0" />
                        <span class="font-semibold text-zinc-700 dark:text-zinc-300">{{ getBranchName(b.locationId || b.location_id) }}</span>
                      </div>
                    </td>
                    <td class="py-3.5 text-center">
                      <div class="inline-flex flex-col items-center">
                        <span class="text-sm font-black text-red-600 dark:text-red-400">{{ b.qty }}</span>
                        <span class="text-[8px] text-muted-foreground uppercase font-bold tracking-tight">PCS</span>
                      </div>
                    </td>
                    <td class="py-3.5">
                      <span class="text-zinc-500">{{ formatDate(b.updatedAt || b.updated_at) }}</span>
                      <div v-if="b.updatedBy || b.updated_by" class="text-[9px] text-muted-foreground mt-0.5">
                        Oleh: {{ (b.updatedBy || b.updated_by).username }}
                      </div>
                    </td>
                    <td class="pr-5 py-3.5 text-right">
                      <div class="flex justify-end gap-1.5">
                        <Button v-if="canUpdateStock" variant="ghost" size="sm"
                          class="h-7 px-2.5 text-[11px] font-bold text-emerald-600 hover:text-emerald-700 hover:bg-emerald-50 dark:hover:bg-emerald-900/20 gap-1.5"
                          @click="openApprove(b)">
                          <CheckCircle2 class="h-3.5 w-3.5" /> Approve
                        </Button>
                        <Button variant="ghost" size="sm"
                          class="h-7 px-2.5 text-[11px] font-bold text-red-500 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-900/20 gap-1.5"
                          @click="openDispose(b)">
                          <Trash2 class="h-3.5 w-3.5" /> Dispose
                        </Button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <DataTablePagination v-if="filteredBalances.length > 0" :page="page" :page-size="pageSize" :total="filteredBalances.length"
              @update:page="page = $event" @update:page-size="pageSize = $event; page = 1" />
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- ─── MODAL DISPOSE ─── -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showDisposeModal" class="fixed inset-0 z-[60] bg-black/50 backdrop-blur-sm" @click="showDisposeModal = false" />
      </Transition>
      <Transition name="scale">
        <div v-if="showDisposeModal && disposeTarget" class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none">
          <div class="bg-white dark:bg-zinc-900 rounded-[24px] shadow-2xl w-full max-w-sm border border-red-200 dark:border-red-800/40 pointer-events-auto overflow-hidden">
            <div class="flex items-center gap-3 px-6 py-4 border-b border-red-100 dark:border-red-900/40 bg-red-50/50 dark:bg-red-950/20">
              <div class="w-9 h-9 rounded-xl bg-red-100 dark:bg-red-900/30 flex items-center justify-center shrink-0">
                <AlertTriangle class="h-5 w-5 text-red-600 dark:text-red-400" />
              </div>
              <div>
                <h3 class="font-black text-[15px] text-zinc-900 dark:text-zinc-100">Dispose ke Supplier</h3>
                <p class="text-[11px] text-muted-foreground mt-0.5">Stok karantina akan dihapus dari sistem</p>
              </div>
              <button @click="showDisposeModal = false" class="ml-auto p-1.5 rounded-full hover:bg-red-100 dark:hover:bg-red-900/30 text-zinc-400">
                <X class="h-4 w-4" />
              </button>
            </div>
            <div class="px-6 py-5 space-y-4">
              <div class="p-3 rounded-xl bg-zinc-50 dark:bg-zinc-800/50 border border-zinc-100 dark:border-zinc-800">
                <p class="text-[10px] font-bold text-zinc-400 uppercase tracking-wider mb-1">Produk</p>
                <p class="text-sm font-bold text-zinc-900 dark:text-zinc-100">{{ disposeTarget.product?.name }}</p>
                <p class="text-[11px] font-mono text-muted-foreground">{{ disposeTarget.product?.sku }}</p>
                <div class="flex items-center justify-between mt-2 pt-2 border-t border-zinc-100 dark:border-zinc-800">
                  <span class="text-[11px] text-zinc-500">Stok Karantina</span>
                  <span class="text-sm font-black text-red-600 dark:text-red-400">{{ disposeTarget.qty }} pcs</span>
                </div>
              </div>
              <div class="space-y-1.5">
                <label class="text-[11px] font-bold text-zinc-500 uppercase tracking-wider">Jumlah Dispose</label>
                <div class="relative">
                  <input v-model="disposeQty" type="number" min="1" :max="disposeTarget.qty" placeholder="0"
                    class="w-full h-11 rounded-xl border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 px-3 pr-12 text-base font-bold outline-none focus:ring-2 focus:ring-red-400/30" />
                  <span class="absolute right-3 top-1/2 -translate-y-1/2 text-[11px] font-bold text-zinc-400">PCS</span>
                </div>
                <button type="button" @click="disposeQty = disposeTarget.qty.toString()"
                  class="text-[11px] font-semibold text-primary hover:underline">Dispose semua ({{ disposeTarget.qty }} pcs)</button>
              </div>
              <div class="space-y-1.5">
                <label class="text-[11px] font-bold text-zinc-500 uppercase tracking-wider">Keterangan</label>
                <input v-model="disposeNotes" placeholder="cth: dikembalikan ke supplier karena expired"
                  class="w-full h-9 rounded-xl border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 px-3 text-sm outline-none focus:ring-2 focus:ring-red-400/30" />
              </div>
              <div class="flex items-start gap-2 px-3 py-2.5 rounded-xl bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800/40 text-[11px] text-amber-700 dark:text-amber-400">
                <AlertTriangle class="h-3.5 w-3.5 shrink-0 mt-0.5" />
                <span>Tindakan ini <strong>tidak dapat dibatalkan</strong>.</span>
              </div>
            </div>
            <div class="px-6 pb-5 flex gap-2.5">
              <button @click="showDisposeModal = false" :disabled="disposeSaving"
                class="flex-1 h-11 rounded-[14px] border border-zinc-200 dark:border-zinc-700 font-bold text-sm hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors disabled:opacity-50">Batal</button>
              <button @click="submitDispose" :disabled="disposeSaving"
                class="flex-1 h-11 rounded-[14px] bg-red-600 hover:bg-red-700 text-white font-bold text-sm flex items-center justify-center gap-2 transition-colors disabled:opacity-50">
                <Loader2 v-if="disposeSaving" class="h-4 w-4 animate-spin" />
                <Trash2 v-else class="h-4 w-4" /> Dispose
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ─── MODAL APPROVE ─── -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showApproveModal" class="fixed inset-0 z-[60] bg-black/50 backdrop-blur-sm" @click="showApproveModal = false" />
      </Transition>
      <Transition name="scale">
        <div v-if="showApproveModal && approveTarget" class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none">
          <div class="bg-white dark:bg-zinc-900 rounded-[24px] shadow-2xl w-full max-w-sm border border-emerald-200 dark:border-emerald-800/40 pointer-events-auto overflow-hidden">
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
                  <span class="text-[11px] font-bold text-zinc-700 dark:text-zinc-300">{{ getBranchName(approveTarget.locationId || approveTarget.location_id) }}</span>
                </div>
              </div>
              <div class="space-y-1.5">
                <label class="text-[11px] font-bold text-zinc-500 uppercase tracking-wider">Jumlah Approve</label>
                <div class="relative">
                  <input v-model="approveQty" type="number" min="1" :max="approveTarget.qty" placeholder="0"
                    class="w-full h-11 rounded-xl border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 px-3 pr-12 text-base font-bold outline-none focus:ring-2 focus:ring-emerald-400/30" />
                  <span class="absolute right-3 top-1/2 -translate-y-1/2 text-[11px] font-bold text-zinc-400">PCS</span>
                </div>
                <button type="button" @click="approveQty = approveTarget.qty.toString()"
                  class="text-[11px] font-semibold text-primary hover:underline">Approve semua ({{ approveTarget.qty }} pcs)</button>
              </div>
              <div class="space-y-1.5">
                <label class="text-[11px] font-bold text-zinc-500 uppercase tracking-wider">Catatan</label>
                <input v-model="approveNotes" placeholder="cth: barang sudah dicek, kondisi baik"
                  class="w-full h-9 rounded-xl border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 px-3 text-sm outline-none focus:ring-2 focus:ring-emerald-400/30" />
              </div>
              <div class="flex items-start gap-2 px-3 py-2.5 rounded-xl bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-200 dark:border-emerald-800/40 text-[11px] text-emerald-700 dark:text-emerald-400">
                <CheckCircle2 class="h-3.5 w-3.5 shrink-0 mt-0.5" />
                <span>Stok akan langsung masuk ke saldo aktif cabang dan <strong>bisa dijual di kasir</strong>.</span>
              </div>
            </div>
            <div class="px-6 pb-5 flex gap-2.5">
              <button @click="showApproveModal = false" :disabled="approveSaving"
                class="flex-1 h-11 rounded-[14px] border border-zinc-200 dark:border-zinc-700 font-bold text-sm hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors disabled:opacity-50">Batal</button>
              <button @click="submitApprove" :disabled="approveSaving"
                class="flex-1 h-11 rounded-[14px] bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm flex items-center justify-center gap-2 transition-colors disabled:opacity-50">
                <Loader2 v-if="approveSaving" class="h-4 w-4 animate-spin" />
                <CheckCircle2 v-else class="h-4 w-4" /> Setujui
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.scale-enter-active, .scale-leave-active { transition: opacity 0.25s ease, transform 0.25s cubic-bezier(0.16, 1, 0.3, 1); }
.scale-enter-from, .scale-leave-to { opacity: 0; transform: scale(0.96); }
</style>
