<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Alert from '@/components/ui/Alert.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'
import CustomSelect from '@/components/ui/CustomSelect.vue'
import api from '@/lib/api'
import { 
  Plus, 
  Loader2, 
  X, 
  PackageSearch, 
  Eye, 
  Check, 
  AlertTriangle,
  Warehouse,
  Building2,
  Calendar,
  Trash2,
  Package,
  RefreshCw
} from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.isAdmin)

// ─── State ───────────────────────────────────────────────────────────────────
const opnames = ref([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('all')
const page = ref(1)
const pageSize = ref(10)

const statusOptions = [
  { value: 'all', label: 'Semua Status' },
  { value: 'draft', label: 'Draft' },
  { value: 'counting', label: 'Counting' },
  { value: 'reviewed', label: 'Reviewed' },
  { value: 'approved', label: 'Approved' }
]

const locationTypeOptions = [
  { value: 'warehouse', label: 'Warehouse' },
  { value: 'branch', label: 'Branch' }
]

// ─── Form Options ────────────────────────────────────────────────────────────
const locations = ref([])
const products = ref([])

// ─── Computed ─────────────────────────────────────────────────────────────────
const filteredOpnames = computed(() => {
  let r = opnames.value
  if (statusFilter.value !== 'all') r = r.filter(o => o.status === statusFilter.value)
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    r = r.filter(o => (o.locationName && o.locationName.toLowerCase().includes(q)) || (o.notes && o.notes.toLowerCase().includes(q)))
  }
  return r
})

const paginatedOpnames = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredOpnames.value.slice(start, start + pageSize.value)
})

// ─── Drawer State ────────────────────────────────────────────────────────────
const showDrawer = ref(false)
const drawerMode = ref('detail')
const saving = ref(false)
const formError = ref(null)
const selectedOpname = ref(null)

const emptyForm = () => ({
  location: 'warehouse',
  locationId: '',
  date: new Date().toISOString().slice(0, 16), // datetime-local format
  notes: '',
  items: []
})

const form = ref(emptyForm())

// ─── Actions ──────────────────────────────────────────────────────────────────
async function fetchData() {
  loading.value = true
  try {
    const url = isAdmin.value ? '/api/v1/stock-opnames/admin' : '/api/v1/stock-opnames'
    const res = await api.get(url)
    opnames.value = res.data.data || []
  } catch (err) {
    toast.error('Gagal memuat data stock opname.')
  } finally {
    loading.value = false
  }
}

async function loadFormOptions() {
  try {
    const urlB = isAdmin.value ? '/api/v1/branches/admin' : '/api/v1/branches'
    const urlW = isAdmin.value ? '/api/v1/warehouses/admin' : '/api/v1/warehouses'
    const urlP = isAdmin.value ? '/api/v1/products/admin' : '/api/v1/products'

    const [resB, resW, resP] = await Promise.all([
      api.get(urlB),
      api.get(urlW),
      api.get(urlP)
    ])
    const branches = (resB.data.data || []).map(x => ({ ...x, type: 'branch' }))
    const warehouses = (resW.data.data || []).map(x => ({ ...x, type: 'warehouse' }))
    locations.value = [...branches, ...warehouses]
    products.value = resP.data.data || []
  } catch (err) {
    toast.error('Gagal memuat opsi lokasi/produk.')
  }
}

async function openCreate() {
  form.value = emptyForm()
  drawerMode.value = 'create'
  formError.value = null
  showDrawer.value = true
  await loadFormOptions()
}

async function fetchLocationStock() {
  if (!form.value.locationId) {
    toast.warning('Pilih lokasi terlebih dahulu.')
    return
  }
  
  loading.value = true
  try {
    const urlStock = isAdmin.value ? '/api/v1/stock-balances/admin' : '/api/v1/stock-balances'
    const res = await api.get(`${urlStock}?locationType=${form.value.location}&locationId=${form.value.locationId}`)
    const stocks = res.data.data?.content || res.data.data || []
    
    if (stocks.length === 0) {
      toast.info('Tidak ada stok terdaftar di lokasi ini.')
    }
    
    form.value.items = stocks.map(s => ({
      productId: s.product?.id,
      productName: s.product?.name,
      sku: s.product?.sku,
      qtySystem: s.qty,
      qtyPhysical: s.qty,
      notes: ''
    }))
  } catch (err) {
    toast.error('Gagal menarik data stok lokasi.')
  } finally {
    loading.value = false
  }
}

async function openDetail(opname) {
  selectedOpname.value = opname
  drawerMode.value = 'detail'
  showDrawer.value = true
}

async function saveOpname() {
  if (!form.value.locationId) {
    formError.value = 'Lokasi wajib diisi.'
    return
  }
  if (form.value.items.length === 0) {
    formError.value = 'Belum ada item untuk di-opname. Gunakan "Tarik Stok" atau tambah manual.'
    return
  }

  saving.value = true
  formError.value = null
  try {
    // Backend expects 'date' as LocalDateTime (string)
    await api.post('/api/v1/stock-opnames', form.value)
    toast.success('Stock Opname berhasil disimpan!')
    showDrawer.value = false
    fetchData()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Gagal menyimpan stock opname.'
  } finally {
    saving.value = false
  }
}

async function approveOpname(o) {
  try {
    await api.post(`/api/v1/stock-opnames/${o.id}/approve`)
    toast.success('Opname berhasil disetujui dan stok disesuaikan!')
    fetchData()
    showDrawer.value = false
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menyetujui opname.')
  }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
function formatDate(dt) {
  return dt ? new Date(dt).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : '-'
}

function statusColor(s) {
  const m = { 
    approved: 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-900/20 dark:text-emerald-400 dark:border-emerald-800/50', 
    reviewed: 'bg-violet-50 text-violet-700 border-violet-200 dark:bg-violet-900/20 dark:text-violet-400 dark:border-violet-800/50', 
    counting: 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-900/20 dark:text-amber-400 dark:border-amber-800/50' 
  }
  return m[s] || 'bg-zinc-100 text-zinc-700 border-zinc-200 dark:bg-zinc-800 dark:text-zinc-400'
}

function statusLabel(s) {
  const m = { draft: 'Draft', counting: 'Counting', reviewed: 'Reviewed', approved: 'Approved' }
  return m[s] || s
}

function diffColor(d) {
  if (d == null || d === 0) return 'text-zinc-500'
  return d < 0 ? 'text-destructive font-bold' : 'text-emerald-600 font-bold'
}

onMounted(fetchData)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <!-- Header -->
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Stock Opname</h1>
          <p class="text-muted-foreground text-sm mt-1">Audit dan rekonsiliasi stok barang secara berkala.</p>
        </div>
        <div class="flex items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-72">
            <DataTableSearch v-model="searchQuery" placeholder="Cari lokasi atau catatan..." />
          </div>
          <CustomSelect v-model="statusFilter" :options="statusOptions" class="w-full sm:w-44" />
          <Button v-if="can('stock-opname.store')" @click="openCreate" size="sm" class="flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
            <Plus class="h-4 w-4" />
            <span>Buat Opname</span>
          </Button>
        </div>
      </div>

      <!-- Table Card -->
      <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm overflow-hidden">
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-24">
            <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
          </div>

          <div v-else-if="filteredOpnames.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
            <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
              <PackageSearch class="h-7 w-7 opacity-40" />
            </div>
            <p class="text-sm font-medium">Belum ada data Stock Opname.</p>
            <Button v-if="can('stock-opname.store') && !searchQuery" size="sm" class="mt-4" @click="openCreate">
              <Plus class="h-3.5 w-3.5 mr-1.5" />
              Mulai Opname Baru
            </Button>
          </div>

          <div v-else>
            <!-- Mobile List -->
            <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
              <div v-for="o in paginatedOpnames" :key="o.id" class="p-4 flex flex-col gap-2 hover:bg-zinc-50 dark:hover:bg-zinc-900/40 cursor-pointer transition-colors" @click="openDetail(o)">
                <div class="flex justify-between items-center">
                  <span class="text-xs font-bold text-zinc-900 dark:text-zinc-100">{{ o.locationName }}</span>
                  <Badge :class="['text-[9px] uppercase tracking-wider', statusColor(o.status)]" variant="outline">
                    {{ statusLabel(o.status) }}
                  </Badge>
                </div>
                <div class="flex justify-between items-end text-[10px] text-muted-foreground">
                  <div>
                    <div class="flex items-center gap-1"><Calendar class="h-3 w-3" /> {{ formatDate(o.date) }}</div>
                    <div class="flex items-center gap-1 mt-1"><Package class="h-3 w-3" /> {{ o.items?.length || 0 }} Item</div>
                  </div>
                  <div class="flex flex-col items-end">
                    <span class="text-[9px] uppercase font-bold text-zinc-400">Auditor</span>
                    <span class="text-zinc-700 dark:text-zinc-300">{{ o.createdBy?.username || '-' }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Desktop Table -->
            <div class="hidden md:block overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="border-b border-zinc-100 dark:border-zinc-800">
                    <th class="pl-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Lokasi</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Tanggal</th>
                    <th class="py-3 text-center text-xs font-semibold uppercase tracking-wide text-zinc-500">Item</th>
                    <th class="py-3 text-center text-xs font-semibold uppercase tracking-wide text-zinc-500">Status</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Auditor</th>
                    <th class="pr-5 py-3 text-right"></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="o in paginatedOpnames" :key="o.id" class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors">
                    <td class="pl-5 py-4">
                      <div class="flex items-center gap-3">
                        <div class="w-8 h-8 rounded bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-400">
                          <Warehouse v-if="o.locationType === 'warehouse'" class="h-4 w-4" />
                          <Building2 v-else class="h-4 w-4" />
                        </div>
                        <div>
                          <div class="font-medium text-zinc-900 dark:text-zinc-100">{{ o.locationName }}</div>
                          <Badge variant="outline" class="text-[9px] h-4 px-1 py-0 uppercase">{{ o.locationType }}</Badge>
                        </div>
                      </div>
                    </td>
                    <td class="py-4 text-xs text-muted-foreground">
                      {{ formatDate(o.date) }}
                    </td>
                    <td class="py-4 text-center">
                      <div class="inline-flex items-center justify-center px-2 py-1 rounded bg-zinc-100 dark:bg-zinc-800 text-[10px] font-bold uppercase">
                        {{ o.items?.length || 0 }} Items
                      </div>
                    </td>
                    <td class="py-4 text-center">
                      <Badge :class="['text-[9px] uppercase tracking-widest font-bold', statusColor(o.status)]" variant="outline">
                        {{ statusLabel(o.status) }}
                      </Badge>
                    </td>
                    <td class="py-4 text-xs font-medium">
                      {{ o.createdBy?.username || '-' }}
                    </td>
                    <td class="pr-5 py-4 text-right">
                      <Button variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-zinc-700" @click="openDetail(o)">
                        <Eye class="h-4 w-4" />
                      </Button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <DataTablePagination 
            v-if="filteredOpnames.length > 0 && !loading" 
            :page="page" 
            :page-size="pageSize" 
            :total="filteredOpnames.length" 
            @update:page="page = $event" 
            @update:page-size="pageSize = $event; page = 1" 
          />
        </CardContent>
      </Card>
    </div>

    <!-- Drawer -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showDrawer" class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm" @click="showDrawer = false" />
      </Transition>
      <Transition name="slide-right">
        <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[600px] bg-card shadow-2xl sm:border-l overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0 bg-muted/20">
            <div>
              <h3 class="font-semibold text-base">{{ drawerMode === 'create' ? 'Input Hasil Opname' : 'Detail Stock Opname' }}</h3>
              <p class="text-xs text-muted-foreground mt-0.5">Rekonsiliasi perbedaan stok fisik dan sistem.</p>
            </div>
            <Button variant="ghost" size="icon" @click="showDrawer = false">
              <X class="h-4 w-4" />
            </Button>
          </div>

          <!-- CREATE MODE -->
          <template v-if="drawerMode === 'create'">
            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-6">
              <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>
              
              <!-- Header Info -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div class="space-y-4">
                  <div class="space-y-1.5">
                    <Label class="text-xs font-bold uppercase tracking-tight">Pilih Lokasi Audit</Label>
                    <div class="grid grid-cols-2 gap-2">
                      <button type="button" @click="form.location = 'warehouse'; form.locationId = ''; form.items = []" :class="['flex items-center justify-center gap-2 h-9 rounded-md border text-[10px] font-bold transition-all', form.location === 'warehouse' ? 'bg-zinc-900 text-white border-zinc-900' : 'bg-white border-zinc-200 hover:bg-zinc-50']">
                        WAREHOUSE
                      </button>
                      <button type="button" @click="form.location = 'branch'; form.locationId = ''; form.items = []" :class="['flex items-center justify-center gap-2 h-9 rounded-md border text-[10px] font-bold transition-all', form.location === 'branch' ? 'bg-zinc-900 text-white border-zinc-900' : 'bg-white border-zinc-200 hover:bg-zinc-50']">
                        BRANCH
                      </button>
                    </div>
                    <select v-model="form.locationId" @change="form.items = []" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm outline-none">
                      <option value="" disabled>Pilih lokasi...</option>
                      <option v-for="l in locations.filter(x => x.type === form.location)" :key="l.id" :value="l.id">{{ l.name }}</option>
                    </select>
                  </div>
                </div>
                <div class="space-y-4">
                  <div class="space-y-1.5">
                    <Label class="text-xs font-bold uppercase tracking-tight">Waktu Audit</Label>
                    <Input v-model="form.date" type="datetime-local" class="h-10" />
                  </div>
                </div>
              </div>

              <!-- Item Section -->
              <div class="space-y-4">
                <div class="flex items-center justify-between">
                  <h4 class="text-[10px] font-bold uppercase tracking-widest text-primary flex items-center gap-2">
                    <Package class="h-3 w-3" /> Daftar Item & Hasil Hitung
                  </h4>
                  <Button variant="outline" size="sm" class="h-8 text-[10px] font-bold bg-primary/5 border-primary/20 text-primary hover:bg-primary/10" @click="fetchLocationStock" :disabled="!form.locationId || loading">
                    <RefreshCw class="h-3 w-3 mr-1.5" :class="{'animate-spin': loading}" /> Tarik Stok Sistem
                  </Button>
                </div>

                <div v-if="form.items.length === 0" class="flex flex-col items-center justify-center py-12 border-2 border-dashed border-zinc-200 dark:border-zinc-800 rounded-xl bg-zinc-50/50">
                  <Package class="h-8 w-8 text-zinc-300 mb-2" />
                  <p class="text-xs text-muted-foreground">Gunakan tombol "Tarik Stok Sistem" untuk memuat data.</p>
                </div>

                <div v-else class="space-y-3">
                  <div v-for="(item, i) in form.items" :key="i" class="p-4 rounded-xl border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900/50">
                    <div class="flex justify-between items-start mb-3">
                      <div>
                        <p class="text-sm font-bold">{{ item.productName }}</p>
                        <p class="text-[10px] font-mono text-muted-foreground">{{ item.sku }}</p>
                      </div>
                      <div class="text-right">
                        <p class="text-[10px] uppercase font-bold text-zinc-400">Selisih</p>
                        <span :class="['text-xs font-black', diffColor(item.qtyPhysical - item.qtySystem)]">
                          {{ (item.qtyPhysical - item.qtySystem) > 0 ? '+' : '' }}{{ item.qtyPhysical - item.qtySystem }}
                        </span>
                      </div>
                    </div>
                    
                    <div class="grid grid-cols-2 gap-4">
                      <div class="space-y-1">
                        <Label class="text-[10px] uppercase text-zinc-500 font-bold">Stok Sistem</Label>
                        <div class="h-8 flex items-center px-3 bg-zinc-100 dark:bg-zinc-800 rounded-md text-xs font-bold border border-zinc-200 dark:border-zinc-700">
                          {{ item.qtySystem }} PCS
                        </div>
                      </div>
                      <div class="space-y-1">
                        <Label class="text-[10px] uppercase text-primary font-bold">Fisik Terhitung</Label>
                        <Input v-model.number="item.qtyPhysical" type="number" class="h-8 text-xs font-bold border-primary/30 focus:ring-primary/20" />
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="space-y-1.5">
                <Label>Catatan Audit</Label>
                <textarea v-model="form.notes" rows="3" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-primary/20" placeholder="Contoh: Barang rusak ditemukan, stok tertukar, dll." />
              </div>
            </div>

            <div class="px-6 py-4 border-t bg-zinc-50/80 dark:bg-zinc-900/50">
              <div class="flex gap-3">
                <Button variant="outline" class="flex-1" @click="showDrawer = false" :disabled="saving">Batal</Button>
                <Button class="flex-1" @click="saveOpname" :disabled="saving || form.items.length === 0">
                  <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
                  Simpan Draft Opname
                </Button>
              </div>
            </div>
          </template>

          <!-- DETAIL MODE -->
          <template v-else-if="selectedOpname">
            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-6">
              <div class="flex items-center justify-between bg-primary/5 p-4 rounded-xl border border-primary/10">
                <div>
                  <span class="text-[10px] font-bold text-primary uppercase tracking-widest block mb-1">{{ selectedOpname.locationType }}</span>
                  <h4 class="text-base font-bold leading-none">{{ selectedOpname.locationName }}</h4>
                </div>
                <Badge :class="['text-[10px] uppercase tracking-widest font-bold px-3 py-1', statusColor(selectedOpname.status)]" variant="outline">
                  {{ statusLabel(selectedOpname.status) }}
                </Badge>
              </div>

              <div class="grid grid-cols-2 gap-4 text-sm">
                <div class="p-3 rounded-lg border border-zinc-100 dark:border-zinc-800">
                  <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-1">Auditor</p>
                  <p class="font-medium">{{ selectedOpname.createdBy?.username || '-' }}</p>
                </div>
                <div class="p-3 rounded-lg border border-zinc-100 dark:border-zinc-800">
                  <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-1">Waktu</p>
                  <p class="font-medium">{{ formatDate(selectedOpname.date) }}</p>
                </div>
              </div>

              <div class="space-y-3">
                <h4 class="text-[10px] font-bold uppercase tracking-widest text-zinc-500 border-b pb-2">Rincian Hasil Audit</h4>
                <div class="space-y-2">
                  <div v-for="(item, i) in selectedOpname.items" :key="i" class="p-3 rounded-lg border border-zinc-100 dark:border-zinc-800 bg-zinc-50/30 dark:bg-zinc-900/20">
                    <div class="flex justify-between items-start mb-2">
                      <div class="flex items-center gap-2">
                        <Package class="h-3.5 w-3.5 text-zinc-400" />
                        <span class="text-sm font-bold">{{ item.product?.name || `Produk #${item.productId}` }}</span>
                      </div>
                      <div :class="['text-xs font-black', diffColor(item.qtyPhysical - item.qtySystem)]">
                        {{ (item.qtyPhysical - item.qtySystem) > 0 ? '+' : '' }}{{ item.qtyPhysical - item.qtySystem }}
                      </div>
                    </div>
                    <div class="flex gap-4 text-[11px]">
                      <span class="text-muted-foreground uppercase tracking-tighter">Sistem: <b>{{ item.qtySystem }}</b></span>
                      <span class="text-primary uppercase tracking-tighter">Fisik: <b>{{ item.qtyPhysical }}</b></span>
                    </div>
                    <p v-if="item.notes" class="text-[10px] italic text-muted-foreground mt-1.5 border-l-2 border-zinc-200 pl-2">{{ item.notes }}</p>
                  </div>
                </div>
              </div>

              <div v-if="selectedOpname.notes" class="space-y-1 bg-zinc-50 dark:bg-zinc-900 p-3 rounded-lg border border-zinc-100 dark:border-zinc-800">
                <p class="text-[10px] font-bold text-zinc-400 uppercase tracking-wider">Catatan Tambahan</p>
                <p class="text-xs italic">{{ selectedOpname.notes }}</p>
              </div>
            </div>

            <div class="px-6 py-5 border-t bg-zinc-50/80 dark:bg-zinc-900/50 mt-auto">
              <div class="flex gap-2">
                <Button v-if="selectedOpname.status === 'reviewed' && can('stock-opname.update')" class="flex-1 bg-primary" @click="approveOpname(selectedOpname)">
                  <Check class="h-4 w-4 mr-2" /> Setujui & Rekonsiliasi
                </Button>
                <Button variant="outline" class="flex-1" @click="showDrawer = false">Tutup</Button>
              </div>
            </div>
          </template>
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
