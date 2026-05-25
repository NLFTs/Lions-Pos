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
  Repeat2, 
  Eye, 
  Check, 
  Truck,
  Warehouse,
  Building2,
  ArrowRight,
  Package,
  Calendar,
  Trash2
} from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.isAdmin)

// ─── State ───────────────────────────────────────────────────────────────────
const transfers = ref([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('all')
const page = ref(1)
const pageSize = ref(10)

const statusOptions = [
  { value: 'all', label: 'Semua Status' },
  { value: 'pending', label: 'Pending' },
  { value: 'approved', label: 'Disetujui' },
  { value: 'received', label: 'Diterima' },
  { value: 'cancelled', label: 'Batal' }
]

const locationTypeOptions = [
  { value: 'warehouse', label: 'Warehouse' },
  { value: 'branch', label: 'Branch' }
]

// ─── Form Options ────────────────────────────────────────────────────────────
const locations = ref([])
const products = ref([])

// ─── Computed ─────────────────────────────────────────────────────────────────
const filteredTRs = computed(() => {
  let r = transfers.value
  if (statusFilter.value !== 'all') r = r.filter(t => t.status === statusFilter.value)
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    r = r.filter(t => 
      (t.fromLocationName && t.fromLocationName.toLowerCase().includes(q)) || 
      (t.toLocationName && t.toLocationName.toLowerCase().includes(q))
    )
  }
  return r
})

const paginatedTRs = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredTRs.value.slice(start, start + pageSize.value)
})

// ─── Drawer State ────────────────────────────────────────────────────────────
const showDrawer = ref(false)
const drawerMode = ref('detail')
const saving = ref(false)
const formError = ref(null)
const selectedTR = ref(null)

const emptyForm = () => ({
  fromLocationType: 'warehouse',
  fromLocationId: '',
  toLocationType: 'branch',
  toLocationId: '',
  notes: '',
  items: [{ productId: '', qtyRequested: 1 }]
})

const form = ref(emptyForm())

// ─── Actions ──────────────────────────────────────────────────────────────────
async function fetchData() {
  loading.value = true
  try {
    const url = isAdmin.value ? '/api/v1/transfer-requests/admin' : '/api/v1/transfer-requests'
    const res = await api.get(url)
    const data = res.data.data
    if (Array.isArray(data)) {
      transfers.value = data
    } else {
      transfers.value = data.content || []
    }
  } catch (err) {
    toast.error('Gagal memuat data transfer stok.')
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
    
    // Admin branches: plain List (no ResData), Partner: ResData<List>
    const brRaw = isAdmin.value ? resB.data : (resB.data?.data || [])
    const brArr = Array.isArray(brRaw) ? brRaw : (brRaw?.content || [])
    
    // Warehouses: ResData<Page> for admin, ResData<List> for partner
    const whRaw = resW.data?.data
    const whArr = whRaw && !Array.isArray(whRaw) && whRaw.content ? whRaw.content : (Array.isArray(whRaw) ? whRaw : [])
    
    // Products: ResData<Page>
    const pRaw = resP.data?.data
    const pArr = pRaw && pRaw.content ? pRaw.content : (Array.isArray(pRaw) ? pRaw : [])
    
    locations.value = [
      ...brArr.map(x => ({ ...x, type: 'branch' })),
      ...whArr.map(x => ({ ...x, type: 'warehouse' }))
    ]
    products.value = pArr
  } catch (err) {
    toast.error('Gagal memuat opsi form.')
  }
}

async function openCreate() {
  form.value = emptyForm()
  drawerMode.value = 'create'
  formError.value = null
  showDrawer.value = true
  await loadFormOptions()
}

async function openDetail(tr) {
  selectedTR.value = tr
  drawerMode.value = 'detail'
  showDrawer.value = true
}

function addItem() {
  form.value.items.push({ productId: '', qtyRequested: 1 })
}

function removeItem(index) {
  form.value.items.splice(index, 1)
}

async function saveTR() {
  if (!form.value.fromLocationId || !form.value.toLocationId) {
    formError.value = 'Asal dan Tujuan wajib diisi.'
    return
  }
  
  if (form.value.fromLocationId === form.value.toLocationId && form.value.fromLocationType === form.value.toLocationType) {
    formError.value = 'Lokasi asal dan tujuan tidak boleh sama.'
    return
  }

  saving.value = true
  formError.value = null
  try {
    await api.post('/api/v1/transfer-requests', form.value)
    toast.success('Permintaan transfer berhasil dibuat!')
    showDrawer.value = false
    fetchData()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Gagal menyimpan transfer request.'
  } finally {
    saving.value = false
  }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
function formatDate(dt) {
  return dt ? new Date(dt).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' }) : '-'
}

function statusColor(s) {
  const m = { 
    received: 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-900/20 dark:text-emerald-400 dark:border-emerald-800/50', 
    approved: 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-900/20 dark:text-blue-400 dark:border-blue-800/50', 
    in_transit: 'bg-violet-50 text-violet-700 border-violet-200 dark:bg-violet-900/20 dark:text-violet-400 dark:border-violet-800/50', 
    cancelled: 'bg-red-50 text-red-700 border-red-200 dark:bg-red-900/20 dark:text-red-400 dark:border-red-800/50' 
  }
  return m[s] || 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-900/20 dark:text-amber-400 dark:border-amber-800/50'
}

function statusLabel(s) {
  const m = { pending: 'Pending', approved: 'Disetujui', in_transit: 'Dalam Kirim', received: 'Diterima', cancelled: 'Batal' }
  return m[s] || s
}

// ─── Status Update ──────────────────────────────────────────────────────────
const updatingStatus = ref(false)

async function updateTRStatus(id, newStatus) {
  updatingStatus.value = true
  try {
    await api.patch(`/api/v1/transfer-requests/${id}/status?status=${newStatus}`)
    toast.success(`Status transfer berhasil diubah!`)
    showDrawer.value = false
    fetchData()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal mengubah status transfer.')
  } finally {
    updatingStatus.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <!-- Header -->
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Transfer Stok</h1>
          <p class="text-muted-foreground text-sm mt-1">Kelola pergerakan barang antar gudang dan cabang.</p>
        </div>
        <div class="flex items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-72">
            <DataTableSearch v-model="searchQuery" placeholder="Cari lokasi asal/tujuan..." />
          </div>
          <CustomSelect v-model="statusFilter" :options="statusOptions" class="w-full sm:w-44" />
          <Button v-if="can('transfer_request.store')" @click="openCreate" size="sm" class="flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
            <Plus class="h-4 w-4" />
            <span>Buat Transfer</span>
          </Button>
        </div>
      </div>

      <!-- Table Card -->
      <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm overflow-hidden">
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-24">
            <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
          </div>

          <div v-else-if="filteredTRs.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
            <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
              <Repeat2 class="h-7 w-7 opacity-40" />
            </div>
            <p class="text-sm font-medium">Belum ada permintaan transfer.</p>
            <Button v-if="can('transfer_request.store') && !searchQuery" size="sm" class="mt-4" @click="openCreate">
              <Plus class="h-3.5 w-3.5 mr-1.5" />
              Mulai Transfer
            </Button>
          </div>

          <div v-else>
            <!-- Mobile List -->
            <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
              <div v-for="tr in paginatedTRs" :key="tr.id" class="p-4 flex flex-col gap-3 hover:bg-zinc-50 dark:hover:bg-zinc-900/40 cursor-pointer transition-colors" @click="openDetail(tr)">
                <div class="flex justify-between items-center">
                  <div class="flex items-center gap-2">
                    <span class="text-xs font-bold text-zinc-900 dark:text-zinc-100">{{ tr.fromLocationName }}</span>
                    <ArrowRight class="h-3 w-3 text-zinc-400" />
                    <span class="text-xs font-bold text-zinc-900 dark:text-zinc-100">{{ tr.toLocationName }}</span>
                  </div>
                  <Badge :class="['text-[9px] uppercase tracking-wider', statusColor(tr.status)]" variant="outline">
                    {{ statusLabel(tr.status) }}
                  </Badge>
                </div>
                <div class="flex justify-between items-center text-[10px] text-muted-foreground">
                  <div class="flex items-center gap-1.5">
                    <Package class="h-3 w-3" /> {{ tr.items?.length || 0 }} Item
                  </div>
                  <div class="flex items-center gap-1.5">
                    <Calendar class="h-3 w-3" /> {{ formatDate(tr.createdAt) }}
                  </div>
                </div>
              </div>
            </div>

            <!-- Desktop Table -->
            <div class="hidden md:block overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="border-b border-zinc-100 dark:border-zinc-800">
                    <th class="pl-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Asal → Tujuan</th>
                    <th class="py-3 text-center text-xs font-semibold uppercase tracking-wide text-zinc-500">Item</th>
                    <th class="py-3 text-center text-xs font-semibold uppercase tracking-wide text-zinc-500">Status</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Pemohon</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Tanggal</th>
                    <th class="pr-5 py-3 text-right"></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="tr in paginatedTRs" :key="tr.id" class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors">
                    <td class="pl-5 py-4">
                      <div class="flex items-center gap-3">
                        <div class="flex flex-col items-center gap-0.5 shrink-0">
                          <Warehouse v-if="tr.fromLocationType === 'warehouse'" class="h-3.5 w-3.5 text-zinc-400" />
                          <Building2 v-else class="h-3.5 w-3.5 text-zinc-400" />
                          <div class="w-px h-2 bg-zinc-200 dark:bg-zinc-800"></div>
                          <Warehouse v-if="tr.toLocationType === 'warehouse'" class="h-3.5 w-3.5 text-primary" />
                          <Building2 v-else class="h-3.5 w-3.5 text-primary" />
                        </div>
                        <div class="flex flex-col gap-1">
                          <span class="text-xs font-medium text-muted-foreground">{{ tr.fromLocationName }}</span>
                          <span class="text-xs font-bold text-zinc-900 dark:text-zinc-100">{{ tr.toLocationName }}</span>
                        </div>
                      </div>
                    </td>
                    <td class="py-4 text-center">
                      <div class="inline-flex items-center justify-center px-2 py-1 rounded bg-zinc-100 dark:bg-zinc-800 text-[10px] font-bold">
                        {{ tr.items?.length || 0 }} ITEM
                      </div>
                    </td>
                    <td class="py-4 text-center">
                      <Badge :class="['text-[9px] uppercase tracking-widest font-bold', statusColor(tr.status)]" variant="outline">
                        {{ statusLabel(tr.status) }}
                      </Badge>
                    </td>
                    <td class="py-4 text-xs font-medium">
                      {{ tr.requestedBy?.username || '-' }}
                    </td>
                    <td class="py-4 text-xs text-muted-foreground">
                      {{ formatDate(tr.createdAt) }}
                    </td>
                    <td class="pr-5 py-4 text-right">
                      <Button variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-zinc-700" @click="openDetail(tr)">
                        <Eye class="h-4 w-4" />
                      </Button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <DataTablePagination 
            v-if="filteredTRs.length > 0 && !loading" 
            :page="page" 
            :page-size="pageSize" 
            :total="filteredTRs.length" 
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
        <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[500px] bg-card shadow-2xl sm:border-l overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0 bg-muted/20">
            <div>
              <h3 class="font-semibold text-base">{{ drawerMode === 'create' ? 'Buat Transfer Stok' : 'Detail Transfer' }}</h3>
              <p class="text-xs text-muted-foreground mt-0.5">Atur mutasi barang antar lokasi bisnis.</p>
            </div>
            <Button variant="ghost" size="icon" @click="showDrawer = false">
              <X class="h-4 w-4" />
            </Button>
          </div>

          <!-- CREATE MODE -->
          <template v-if="drawerMode === 'create'">
            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-6">
              <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>
              
              <!-- Section: Rute Transfer -->
              <div class="p-4 rounded-xl border border-primary/20 bg-primary/5 space-y-5">
                <h4 class="text-[10px] font-bold uppercase tracking-widest text-primary flex items-center gap-2">
                  <ArrowRight class="h-3 w-3" /> Rute Mutasi
                </h4>
                
                <div class="grid grid-cols-1 gap-4">
                  <!-- Dari -->
                  <div class="space-y-2">
                    <Label class="text-xs font-bold">Dari (Asal) <span class="text-destructive">*</span></Label>
                    <div class="grid grid-cols-2 gap-2 mb-2">
                      <button type="button" @click="form.fromLocationType = 'warehouse'; form.fromLocationId = ''" :class="['flex items-center justify-center gap-2 h-8 rounded-md border text-[10px] font-bold transition-all', form.fromLocationType === 'warehouse' ? 'bg-zinc-900 text-white border-zinc-900' : 'bg-white border-zinc-200 hover:bg-zinc-50']">
                        <Warehouse class="h-3 w-3" /> WAREHOUSE
                      </button>
                      <button type="button" @click="form.fromLocationType = 'branch'; form.fromLocationId = ''" :class="['flex items-center justify-center gap-2 h-8 rounded-md border text-[10px] font-bold transition-all', form.fromLocationType === 'branch' ? 'bg-zinc-900 text-white border-zinc-900' : 'bg-white border-zinc-200 hover:bg-zinc-50']">
                        <Building2 class="h-3 w-3" /> BRANCH
                      </button>
                    </div>
                    <select v-model="form.fromLocationId" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-primary/20">
                      <option value="" disabled>Pilih lokasi asal...</option>
                      <option v-for="l in locations.filter(x => x.type === form.fromLocationType)" :key="l.id" :value="l.id">{{ l.name }}</option>
                    </select>
                  </div>

                  <!-- Separator Icon -->
                  <div class="flex justify-center -my-2 relative z-10">
                    <div class="bg-white dark:bg-zinc-900 p-1.5 rounded-full border border-zinc-200 dark:border-zinc-800">
                      <Repeat2 class="h-4 w-4 text-primary animate-pulse" />
                    </div>
                  </div>

                  <!-- Ke -->
                  <div class="space-y-2">
                    <Label class="text-xs font-bold">Ke (Tujuan) <span class="text-destructive">*</span></Label>
                    <div class="grid grid-cols-2 gap-2 mb-2">
                      <button type="button" @click="form.toLocationType = 'warehouse'; form.toLocationId = ''" :class="['flex items-center justify-center gap-2 h-8 rounded-md border text-[10px] font-bold transition-all', form.toLocationType === 'warehouse' ? 'bg-zinc-900 text-white border-zinc-900' : 'bg-white border-zinc-200 hover:bg-zinc-50']">
                        <Warehouse class="h-3 w-3" /> WAREHOUSE
                      </button>
                      <button type="button" @click="form.toLocationType = 'branch'; form.toLocationId = ''" :class="['flex items-center justify-center gap-2 h-8 rounded-md border text-[10px] font-bold transition-all', form.toLocationType === 'branch' ? 'bg-zinc-900 text-white border-zinc-900' : 'bg-white border-zinc-200 hover:bg-zinc-50']">
                        <Building2 class="h-3 w-3" /> BRANCH
                      </button>
                    </div>
                    <select v-model="form.toLocationId" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-primary/20">
                      <option value="" disabled>Pilih lokasi tujuan...</option>
                      <option v-for="l in locations.filter(x => x.type === form.toLocationType)" :key="l.id" :value="l.id">{{ l.name }}</option>
                    </select>
                  </div>
                </div>
              </div>

              <!-- Section: Item -->
              <div class="space-y-4">
                <div class="flex items-center justify-between">
                  <h4 class="text-[10px] font-bold uppercase tracking-widest text-zinc-500 flex items-center gap-2">
                    <Package class="h-3 w-3" /> Item Transfer
                  </h4>
                  <Button variant="outline" size="sm" class="h-7 text-[10px] font-bold" @click="addItem">
                    <Plus class="h-3 w-3 mr-1" /> Tambah Item
                  </Button>
                </div>

                <div class="space-y-3">
                  <div v-for="(item, i) in form.items" :key="i" class="relative p-3 rounded-xl border border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30 group">
                    <Button v-if="form.items.length > 1" variant="ghost" size="icon" class="absolute -top-2 -right-2 h-6 w-6 rounded-full bg-destructive text-white hover:bg-destructive/90 opacity-0 group-hover:opacity-100 transition-opacity shadow-lg" @click="removeItem(i)">
                      <Trash2 class="h-3 w-3" />
                    </Button>
                    
                    <div class="grid grid-cols-3 gap-3">
                      <div class="col-span-2 space-y-1">
                        <Label class="text-[10px] text-zinc-500">Produk</Label>
                        <select v-model="item.productId" class="w-full h-8 rounded-md border border-input bg-background px-2 text-xs outline-none">
                          <option value="" disabled>Pilih produk...</option>
                          <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
                        </select>
                      </div>
                      <div class="space-y-1">
                        <Label class="text-[10px] text-zinc-500">Qty</Label>
                        <Input v-model.number="item.qtyRequested" type="number" min="1" class="h-8 text-xs" />
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="space-y-1.5">
                <Label>Catatan</Label>
                <textarea v-model="form.notes" rows="3" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:ring-2 focus:ring-primary/20 outline-none" placeholder="Alasan transfer atau instruksi..." />
              </div>
            </div>

            <div class="px-6 py-4 border-t bg-zinc-50/80 dark:bg-zinc-900/50">
              <div class="flex gap-3">
                <Button variant="outline" class="flex-1" @click="showDrawer = false" :disabled="saving">Batal</Button>
                <Button class="flex-1" @click="saveTR" :disabled="saving">
                  <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
                  Kirim Permintaan
                </Button>
              </div>
            </div>
          </template>

          <!-- DETAIL MODE -->
          <template v-else-if="selectedTR">
            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-6">
              <div class="flex items-center justify-between bg-primary/5 p-4 rounded-xl border border-primary/10">
                <div class="flex items-center gap-3">
                  <div class="flex flex-col items-center">
                    <Badge variant="outline" class="text-[9px] h-4 mb-1">{{ selectedTR.fromLocationType }}</Badge>
                    <span class="text-xs font-bold truncate max-w-[100px]">{{ selectedTR.fromLocationName }}</span>
                  </div>
                  <ArrowRight class="h-4 w-4 text-primary shrink-0" />
                  <div class="flex flex-col items-center">
                    <Badge variant="outline" class="text-[9px] h-4 mb-1">{{ selectedTR.toLocationType }}</Badge>
                    <span class="text-xs font-bold truncate max-w-[100px]">{{ selectedTR.toLocationName }}</span>
                  </div>
                </div>
                <Badge :class="['text-[9px] uppercase tracking-widest font-bold px-2 py-1', statusColor(selectedTR.status)]" variant="outline">
                  {{ statusLabel(selectedTR.status) }}
                </Badge>
              </div>

              <div class="grid grid-cols-2 gap-4 text-sm">
                <div class="p-3 rounded-lg border border-zinc-100 dark:border-zinc-800">
                  <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-1">Diajukan Oleh</p>
                  <p class="font-medium">{{ selectedTR.requestedBy?.username || '-' }}</p>
                </div>
                <div class="p-3 rounded-lg border border-zinc-100 dark:border-zinc-800">
                  <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-1">Tanggal</p>
                  <p class="font-medium">{{ formatDate(selectedTR.createdAt) }}</p>
                </div>
              </div>

              <div class="space-y-3">
                <h4 class="text-[10px] font-bold uppercase tracking-widest text-zinc-500 border-b pb-2">Daftar Barang</h4>
                <div class="space-y-2">
                  <div v-for="(item, i) in selectedTR.items" :key="i" class="flex justify-between items-center p-3 rounded-lg border border-zinc-100 dark:border-zinc-800 bg-zinc-50/30 dark:bg-zinc-900/20">
                    <div class="flex items-center gap-3">
                      <Package class="h-4 w-4 text-zinc-400" />
                      <div>
                        <p class="text-sm font-semibold">{{ item.product?.name || `Produk #${item.productId}` }}</p>
                        <p class="text-[10px] text-muted-foreground">SKU: {{ item.product?.sku || '-' }}</p>
                      </div>
                    </div>
                    <div class="text-right">
                      <p class="text-sm font-bold">{{ item.qtyRequested }} <span class="text-[10px] text-muted-foreground font-normal">PCS</span></p>
                      <p v-if="item.qtyReceived != null" class="text-[10px] text-emerald-600 font-bold">Diterima: {{ item.qtyReceived }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="selectedTR.notes" class="space-y-1 bg-zinc-50 dark:bg-zinc-900 p-3 rounded-lg border border-zinc-100 dark:border-zinc-800">
                <p class="text-[10px] font-bold text-zinc-400 uppercase tracking-wider">Catatan</p>
                <p class="text-xs italic">{{ selectedTR.notes }}</p>
              </div>
            </div>

            <div class="px-6 py-5 border-t bg-zinc-50/80 dark:bg-zinc-900/50 mt-auto">
              <div class="flex gap-2">
                <Button 
                  v-if="selectedTR.status === 'pending' && can('transfer_request.update')"
                  class="flex-1 bg-primary"
                  :disabled="updatingStatus"
                  @click="updateTRStatus(selectedTR.id, 'approved')"
                >
                  <Loader2 v-if="updatingStatus" class="h-4 w-4 mr-2 animate-spin" />
                  <Check v-else class="h-4 w-4 mr-2" />
                  Setujui Transfer
                </Button>
                <Button 
                  v-if="selectedTR.status === 'approved' && can('transfer_request.update')"
                  class="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white"
                  :disabled="updatingStatus"
                  @click="updateTRStatus(selectedTR.id, 'received')"
                >
                  <Loader2 v-if="updatingStatus" class="h-4 w-4 mr-2 animate-spin" />
                  <Truck v-else class="h-4 w-4 mr-2" />
                  Terima Barang
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
