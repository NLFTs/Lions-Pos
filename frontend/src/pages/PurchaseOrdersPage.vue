<script setup>
import { ref, computed, onMounted, watch } from 'vue'
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
  ClipboardList, 
  Eye, 
  Trash2, 
  Search,
  Calendar,
  Truck,
  MapPin,
  Package
} from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()

// ─── State ───────────────────────────────────────────────────────────────────
const pos = ref([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('all')
const page = ref(1)
const pageSize = ref(10)

const statusOptions = [
  { value: 'all', label: 'Semua Status' },
  { value: 'draft', label: 'Draft' },
  { value: 'ordered', label: 'Dipesan' },
  { value: 'received', label: 'Diterima' },
  { value: 'cancelled', label: 'Batal' }
]

const locationTypeOptions = [
  { value: 'warehouse', label: 'Warehouse' },
  { value: 'branch', label: 'Branch' }
]

// ─── Form Data for Selects ───────────────────────────────────────────────────
const suppliers = ref([])
const locations = ref([])
const products = ref([])

// ─── Computed ─────────────────────────────────────────────────────────────────
const filteredPOs = computed(() => {
  let r = pos.value
  if (statusFilter.value !== 'all') r = r.filter(p => p.status === statusFilter.value)
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    r = r.filter(p => 
      (p.poNumber && p.poNumber.toLowerCase().includes(q)) || 
      (p.supplier?.name && p.supplier.name.toLowerCase().includes(q))
    )
  }
  return r
})

const paginatedPOs = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredPOs.value.slice(start, start + pageSize.value)
})

// ─── Drawer State ────────────────────────────────────────────────────────────
const showDrawer = ref(false)
const drawerMode = ref('detail') // 'create' | 'detail'
const saving = ref(false)
const formError = ref(null)
const selectedPO = ref(null)

const emptyForm = () => ({
  supplierId: '',
  locationId: '',
  locationType: 'warehouse',
  orderDate: new Date().toISOString().slice(0, 10),
  expectedDate: '',
  notes: '',
  items: [{ productId: '', qtyOrdered: 1, unitCost: 0 }]
})

const form = ref(emptyForm())

// ─── Actions ──────────────────────────────────────────────────────────────────
async function fetchData() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/purchase-orders')
    pos.value = res.data.data || []
  } catch (err) {
    toast.error('Gagal memuat data Purchase Order.')
  } finally {
    loading.value = false
  }
}

async function loadFormOptions() {
  try {
    const [resS, resB, resW, resP] = await Promise.all([
      api.get('/api/v1/suppliers'),
      api.get('/api/v1/branches'),
      api.get('/api/v1/warehouses'),
      api.get('/api/v1/products')
    ])
    suppliers.value = resS.data.data || []
    
    const branches = (resB.data.data || []).map(x => ({ ...x, type: 'branch' }))
    const warehouses = (resW.data.data || []).map(x => ({ ...x, type: 'warehouse' }))
    locations.value = [...branches, ...warehouses]
    
    products.value = resP.data.data || []
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

async function openDetail(po) {
  selectedPO.value = po
  drawerMode.value = 'detail'
  showDrawer.value = true
}

function addItem() {
  form.value.items.push({ productId: '', qtyOrdered: 1, unitCost: 0 })
}

function removeItem(index) {
  form.value.items.splice(index, 1)
}

const formTotal = computed(() => {
  return form.value.items.reduce((sum, item) => sum + (item.qtyOrdered * item.unitCost), 0)
})

async function savePO() {
  if (!form.value.supplierId || !form.value.locationId) {
    formError.value = 'Supplier dan Lokasi wajib diisi.'
    return
  }
  
  if (form.value.items.some(i => !i.productId || i.qtyOrdered <= 0)) {
    formError.value = 'Semua item harus memiliki produk dan jumlah yang valid.'
    return
  }

  saving.value = true
  formError.value = null
  try {
    await api.post('/api/v1/purchase-orders', form.value)
    toast.success('Purchase Order berhasil dibuat!')
    showDrawer.value = false
    fetchData()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Gagal menyimpan Purchase Order.'
  } finally {
    saving.value = false
  }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
function formatCurrency(v) {
  return v == null ? '-' : new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(v)
}

function formatDate(dt) {
  return dt ? new Date(dt).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' }) : '-'
}

function statusColor(s) {
  const map = {
    received: 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-900/20 dark:text-emerald-400 dark:border-emerald-800/50',
    ordered: 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-900/20 dark:text-blue-400 dark:border-blue-800/50',
    partial: 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-900/20 dark:text-amber-400 dark:border-amber-800/50',
    cancelled: 'bg-red-50 text-red-700 border-red-200 dark:bg-red-900/20 dark:text-red-400 dark:border-red-800/50'
  }
  return map[s] || 'bg-zinc-100 text-zinc-700 border-zinc-200 dark:bg-zinc-800 dark:text-zinc-400 dark:border-zinc-700'
}

function statusLabel(s) {
  const m = { draft: 'Draft', ordered: 'Dipesan', partial: 'Sebagian', received: 'Diterima', cancelled: 'Batal' }
  return m[s] || s
}

function getLocationName(type, id) {
  // Try to find in loaded locations or use the ones from PO detail
  const loc = locations.value.find(l => l.type === type && l.id === id)
  return loc ? loc.name : `Lokasi #${id}`
}

onMounted(fetchData)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <!-- Header -->
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Purchase Order</h1>
          <p class="text-muted-foreground text-sm mt-1">Kelola pesanan pembelian barang ke supplier.</p>
        </div>
        <div class="flex items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-72">
            <DataTableSearch v-model="searchQuery" placeholder="Cari No. PO atau Supplier..." />
          </div>
          <CustomSelect v-model="statusFilter" :options="statusOptions" class="w-full sm:w-44" />
          <Button v-if="can('purchase-order.store')" @click="openCreate" size="sm" class="flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
            <Plus class="h-4 w-4" />
            <span>Buat PO</span>
          </Button>
        </div>
      </div>

      <!-- Table Card -->
      <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm overflow-hidden">
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-24">
            <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
          </div>

          <div v-else-if="filteredPOs.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
            <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
              <ClipboardList class="h-7 w-7 opacity-40" />
            </div>
            <p class="text-sm font-medium">Belum ada Purchase Order.</p>
            <Button v-if="can('purchase-order.store') && !searchQuery" size="sm" class="mt-4" @click="openCreate">
              <Plus class="h-3.5 w-3.5 mr-1.5" />
              Buat PO Pertama
            </Button>
          </div>

          <div v-else>
            <!-- Mobile List -->
            <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
              <div v-for="po in paginatedPOs" :key="po.id" class="p-4 flex flex-col gap-2 hover:bg-zinc-50 dark:hover:bg-zinc-900/40 cursor-pointer transition-colors" @click="openDetail(po)">
                <div class="flex justify-between items-center">
                  <span class="font-mono text-[11px] font-bold text-primary">{{ po.poNumber }}</span>
                  <Badge :class="['text-[9px] uppercase tracking-wider', statusColor(po.status)]" variant="outline">
                    {{ statusLabel(po.status) }}
                  </Badge>
                </div>
                <div class="flex justify-between items-end mt-1">
                  <div>
                    <div class="text-sm font-medium">{{ po.supplier?.name }}</div>
                    <div class="text-[10px] text-muted-foreground flex items-center gap-1 mt-0.5">
                      <Calendar class="h-3 w-3" /> {{ formatDate(po.createdAt) }}
                    </div>
                  </div>
                  <div class="text-sm font-bold">{{ formatCurrency(po.total) }}</div>
                </div>
              </div>
            </div>

            <!-- Desktop Table -->
            <div class="hidden md:block overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="border-b border-zinc-100 dark:border-zinc-800">
                    <th class="pl-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">No. PO</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Supplier</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Tujuan</th>
                    <th class="py-3 text-right text-xs font-semibold uppercase tracking-wide text-zinc-500">Total</th>
                    <th class="py-3 text-center text-xs font-semibold uppercase tracking-wide text-zinc-500">Status</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Tanggal</th>
                    <th class="pr-5 py-3 text-right"></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="po in paginatedPOs" :key="po.id" class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors">
                    <td class="pl-5 py-4">
                      <span class="font-mono text-[11px] font-bold text-primary bg-primary/5 px-1.5 py-0.5 rounded border border-primary/10">
                        {{ po.poNumber }}
                      </span>
                    </td>
                    <td class="py-4">
                      <div class="font-medium text-zinc-900 dark:text-zinc-100">{{ po.supplier?.name }}</div>
                      <div class="text-[10px] text-zinc-400 italic">{{ po.supplier?.phone || '' }}</div>
                    </td>
                    <td class="py-4">
                      <div class="flex items-center gap-1.5 text-xs">
                        <Warehouse v-if="po.locationType === 'warehouse'" class="h-3.5 w-3.5 opacity-50" />
                        <Building2 v-else class="h-3.5 w-3.5 opacity-50" />
                        {{ po.locationName || `ID: ${po.locationId}` }}
                      </div>
                    </td>
                    <td class="py-4 text-right font-bold text-zinc-900 dark:text-zinc-100">
                      {{ formatCurrency(po.total) }}
                    </td>
                    <td class="py-4 text-center">
                      <Badge :class="['text-[9px] uppercase tracking-widest font-bold', statusColor(po.status)]" variant="outline">
                        {{ statusLabel(po.status) }}
                      </Badge>
                    </td>
                    <td class="py-4 text-xs text-muted-foreground">
                      {{ formatDate(po.createdAt) }}
                    </td>
                    <td class="pr-5 py-4 text-right">
                      <Button variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-zinc-700" @click="openDetail(po)">
                        <Eye class="h-4 w-4" />
                      </Button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <DataTablePagination 
            v-if="filteredPOs.length > 0 && !loading" 
            :page="page" 
            :page-size="pageSize" 
            :total="filteredPOs.length" 
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
        <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[550px] bg-card shadow-2xl sm:border-l overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0 bg-muted/20">
            <div>
              <h3 class="font-semibold text-base">{{ drawerMode === 'create' ? 'Buat Purchase Order' : 'Detail Purchase Order' }}</h3>
              <p class="text-xs text-muted-foreground mt-0.5">Kelola pesanan barang ke supplier secara efisien.</p>
            </div>
            <Button variant="ghost" size="icon" @click="showDrawer = false">
              <X class="h-4 w-4" />
            </Button>
          </div>

          <!-- CREATE MODE -->
          <template v-if="drawerMode === 'create'">
            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-6">
              <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>
              
              <!-- Section: Info Dasar -->
              <div class="space-y-4">
                <h4 class="text-[10px] font-bold uppercase tracking-widest text-primary flex items-center gap-2">
                  <Truck class="h-3 w-3" /> Informasi Dasar
                </h4>
                <div class="space-y-1.5">
                  <Label>Pilih Supplier <span class="text-destructive">*</span></Label>
                  <select v-model="form.supplierId" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm focus:ring-2 focus:ring-primary/20 outline-none">
                    <option value="" disabled>Pilih supplier...</option>
                    <option v-for="s in suppliers" :key="s.id" :value="s.id">{{ s.name }}</option>
                  </select>
                </div>

                <div class="grid grid-cols-2 gap-4">
                  <div class="space-y-1.5">
                    <Label>Tipe Tujuan</Label>
                    <div class="grid grid-cols-2 gap-2">
                      <button 
                        type="button"
                        @click="form.locationType = 'warehouse'; form.locationId = ''"
                        :class="['flex items-center justify-center gap-2 h-9 rounded-md border text-[11px] font-medium transition-all', form.locationType === 'warehouse' ? 'bg-primary text-primary-foreground border-primary shadow-sm' : 'bg-background border-input hover:bg-muted']"
                      >
                        <Warehouse class="h-3.5 w-3.5" /> Gudang
                      </button>
                      <button 
                        type="button"
                        @click="form.locationType = 'branch'; form.locationId = ''"
                        :class="['flex items-center justify-center gap-2 h-9 rounded-md border text-[11px] font-medium transition-all', form.locationType === 'branch' ? 'bg-primary text-primary-foreground border-primary shadow-sm' : 'bg-background border-input hover:bg-muted']"
                      >
                        <Building2 class="h-3.5 w-3.5" /> Cabang
                      </button>
                    </div>
                  </div>
                  <div class="space-y-1.5">
                    <Label>Pilih Lokasi <span class="text-destructive">*</span></Label>
                    <select v-model="form.locationId" class="w-full h-9 rounded-md border border-input bg-background px-3 text-sm outline-none">
                      <option value="" disabled>Pilih...</option>
                      <option v-for="l in locations.filter(x => x.type === form.locationType)" :key="l.id" :value="l.id">
                        {{ l.name }}
                      </option>
                    </select>
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-4">
                  <div class="space-y-1.5">
                    <Label>Tanggal Order</Label>
                    <Input v-model="form.orderDate" type="date" class="h-9" />
                  </div>
                  <div class="space-y-1.5">
                    <Label>Estimasi Tiba</Label>
                    <Input v-model="form.expectedDate" type="date" class="h-9" />
                  </div>
                </div>
              </div>

              <!-- Section: Item Pesanan -->
              <div class="space-y-4">
                <div class="flex items-center justify-between">
                  <h4 class="text-[10px] font-bold uppercase tracking-widest text-primary flex items-center gap-2">
                    <Package class="h-3 w-3" /> Item Pesanan
                  </h4>
                  <Button variant="outline" size="sm" class="h-7 text-[10px] font-bold" @click="addItem">
                    <Plus class="h-3 w-3 mr-1" /> Tambah Item
                  </Button>
                </div>

                <div class="space-y-3">
                  <div v-for="(item, i) in form.items" :key="i" class="relative p-4 rounded-xl border border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30 group animate-in slide-in-from-right-2 duration-200">
                    <Button 
                      v-if="form.items.length > 1" 
                      variant="ghost" 
                      size="icon" 
                      class="absolute -top-2 -right-2 h-6 w-6 rounded-full bg-destructive text-white hover:bg-destructive/90 opacity-0 group-hover:opacity-100 transition-opacity shadow-lg" 
                      @click="removeItem(i)"
                    >
                      <X class="h-3 w-3" />
                    </Button>
                    
                    <div class="grid grid-cols-1 gap-3">
                      <div class="space-y-1">
                        <Label class="text-[10px] text-zinc-500">Pilih Produk</Label>
                        <select v-model="item.productId" class="w-full h-8 rounded-md border border-input bg-background px-2 text-xs outline-none">
                          <option value="" disabled>Pilih produk...</option>
                          <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }} ({{ p.sku }})</option>
                        </select>
                      </div>
                      <div class="grid grid-cols-2 gap-3">
                        <div class="space-y-1">
                          <Label class="text-[10px] text-zinc-500">Kuantitas</Label>
                          <Input v-model.number="item.qtyOrdered" type="number" min="1" class="h-8 text-xs" />
                        </div>
                        <div class="space-y-1">
                          <Label class="text-[10px] text-zinc-500">Harga Satuan (HPP)</Label>
                          <Input v-model.number="item.unitCost" type="number" min="0" class="h-8 text-xs" />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Section: Catatan -->
              <div class="space-y-1.5">
                <Label>Catatan Tambahan</Label>
                <textarea v-model="form.notes" rows="3" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:ring-2 focus:ring-primary/20 outline-none" placeholder="Catatan untuk supplier..." />
              </div>
            </div>

            <!-- Footer Summary -->
            <div class="px-6 py-4 border-t bg-zinc-50/80 dark:bg-zinc-900/50">
              <div class="flex items-center justify-between mb-4">
                <span class="text-xs text-muted-foreground uppercase tracking-widest font-bold">Total Estimasi</span>
                <span class="text-xl font-black text-primary">{{ formatCurrency(formTotal) }}</span>
              </div>
              <div class="flex gap-3">
                <Button variant="outline" class="flex-1" @click="showDrawer = false" :disabled="saving">Batal</Button>
                <Button class="flex-1" @click="savePO" :disabled="saving">
                  <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
                  Simpan Order
                </Button>
              </div>
            </div>
          </template>

          <!-- DETAIL MODE -->
          <template v-else-if="selectedPO">
            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-6">
              <div class="flex items-center justify-between bg-primary/5 p-4 rounded-xl border border-primary/10">
                <div>
                  <span class="text-[10px] font-bold text-primary uppercase tracking-widest block mb-1">Purchase Order</span>
                  <h4 class="font-mono text-lg font-bold leading-none">{{ selectedPO.poNumber }}</h4>
                </div>
                <Badge :class="['text-[10px] uppercase tracking-widest font-bold px-3 py-1', statusColor(selectedPO.status)]" variant="outline">
                  {{ statusLabel(selectedPO.status) }}
                </Badge>
              </div>

              <div class="grid grid-cols-2 gap-6">
                <div class="space-y-1">
                  <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Supplier</p>
                  <p class="text-sm font-semibold">{{ selectedPO.supplier?.name }}</p>
                  <p class="text-[11px] text-zinc-500">{{ selectedPO.supplier?.address || '-' }}</p>
                </div>
                <div class="space-y-1">
                  <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Tujuan</p>
                  <div class="flex items-center gap-1.5 text-sm font-semibold">
                    <Warehouse v-if="selectedPO.locationType === 'warehouse'" class="h-3.5 w-3.5 opacity-50" />
                    <Building2 v-else class="h-3.5 w-3.5 opacity-50" />
                    {{ selectedPO.locationName || `Lokasi #${selectedPO.locationId}` }}
                  </div>
                  <Badge variant="outline" class="text-[9px] h-4 px-1.5 uppercase tracking-tighter">{{ selectedPO.locationType }}</Badge>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-6 pt-2">
                <div class="space-y-1">
                  <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Tanggal Order</p>
                  <p class="text-sm">{{ formatDate(selectedPO.orderDate || selectedPO.createdAt) }}</p>
                </div>
                <div class="space-y-1">
                  <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Estimasi Tiba</p>
                  <p class="text-sm">{{ formatDate(selectedPO.expectedDate) }}</p>
                </div>
              </div>

              <div class="space-y-3">
                <h4 class="text-[10px] font-bold uppercase tracking-widest text-zinc-500 border-b pb-2">Item Pesanan</h4>
                <div class="space-y-2">
                  <div v-for="(item, i) in selectedPO.items" :key="i" class="flex justify-between items-center p-3 rounded-lg border border-zinc-100 dark:border-zinc-800 bg-zinc-50/30 dark:bg-zinc-900/20">
                    <div class="flex items-center gap-3">
                      <div class="w-8 h-8 rounded bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-400">
                        <Package class="h-4 w-4" />
                      </div>
                      <div>
                        <p class="text-sm font-semibold">{{ item.product?.name || `Produk #${item.productId}` }}</p>
                        <p class="text-[10px] text-muted-foreground">
                          {{ item.qtyOrdered }} pcs × {{ formatCurrency(item.unitCost) }}
                          <span v-if="item.qtyReceived > 0" class="ml-2 text-emerald-600 font-bold">• Diterima: {{ item.qtyReceived }}</span>
                        </p>
                      </div>
                    </div>
                    <span class="text-sm font-bold">{{ formatCurrency(item.qtyOrdered * item.unitCost) }}</span>
                  </div>
                </div>
              </div>

              <div v-if="selectedPO.notes" class="space-y-1 bg-amber-50/50 dark:bg-amber-900/10 p-3 rounded-lg border border-amber-100/50 dark:border-amber-800/30">
                <p class="text-[10px] font-bold text-amber-600 uppercase tracking-wider">Catatan</p>
                <p class="text-xs italic text-amber-800/80 dark:text-amber-200/80">{{ selectedPO.notes }}</p>
              </div>
            </div>

            <div class="px-6 py-5 border-t bg-zinc-50/80 dark:bg-zinc-900/50 mt-auto">
              <div class="flex items-center justify-between mb-2">
                <span class="text-xs text-muted-foreground font-bold">SUBTOTAL</span>
                <span class="text-sm font-medium">{{ formatCurrency(selectedPO.total) }}</span>
              </div>
              <div class="flex items-center justify-between mb-4">
                <span class="text-xs text-muted-foreground font-bold uppercase tracking-widest">Grand Total</span>
                <span class="text-2xl font-black text-primary">{{ formatCurrency(selectedPO.total) }}</span>
              </div>
              <div class="flex gap-2">
                <Button v-if="selectedPO.status === 'draft'" class="flex-1">Kirim PO</Button>
                <Button v-if="selectedPO.status === 'ordered'" class="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white">Terima Barang</Button>
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
