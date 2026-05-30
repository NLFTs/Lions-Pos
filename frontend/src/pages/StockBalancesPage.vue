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
import Badge from '@/components/ui/badge/Badge.vue'
import Alert from '@/components/ui/Alert.vue'
import api from '@/lib/api'
import { 
  Package, 
  Warehouse, 
  Building2, 
  Search, 
  Loader2, 
  Plus, 
  History, 
  ArrowUpRight,
  ArrowUpDown,
  Filter,
  X,
  ChevronDown,
} from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'
import CustomSelect from '@/components/ui/CustomSelect.vue'

const { can } = usePermission()
const { toast } = useToast()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.isAdmin)
const isSuperAdmin = computed(() => authStore.isSuperAdmin)

// ─── State ───────────────────────────────────────────────────────────────────
const balances = ref([])
const loading = ref(false)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

// Filters
const locationFilter = ref('all')
const typeFilter = ref('all')
const locations = ref([]) 
const products = ref([])  

// ─── Computed ─────────────────────────────────────────────────────────────────
const filteredBalances = computed(() => {
  let r = balances.value
  if (locationFilter.value !== 'all') {
    r = r.filter(b => b.locationId === parseInt(locationFilter.value))
  }
  if (typeFilter.value !== 'all') {
    r = r.filter(b => b.locationType === typeFilter.value)
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

const locationOptions = computed(() => {
  const opts = [{ value: 'all', label: 'Semua Lokasi' }]
  locations.value.forEach(l => {
    opts.push({ value: l.id.toString(), label: `${l.name} (${l.type})` })
  })
  return opts
})

const typeOptions = [
  { value: 'all', label: 'Semua Tipe' },
  { value: 'warehouse', label: 'Warehouse' },
  { value: 'branch', label: 'Branch' }
]

// ─── Actions: Fetch Master Data ───────────────────────────────────────────────
async function fetchData() {
  loading.value = true
  try {
    // Admin: /admin returns List (no ResData), partner: returns ResData<List>
    const url = isAdmin.value ? '/api/v1/stock-balances/admin' : '/api/v1/stock-balances'
    const urlBranches = isAdmin.value ? '/api/v1/branches/admin' : '/api/v1/branches'
    const urlWarehouses = isAdmin.value ? '/api/v1/warehouses/admin' : '/api/v1/warehouses'
    
    const [resB, resBr, resWh] = await Promise.all([
      api.get(url),
      api.get(urlBranches),
      api.get(urlWarehouses)
    ])
    
    // Admin stock-balances: plain List (no ResData wrapper)
    // Partner stock-balances: ResData<List>
    let rawBalances
    if (isAdmin.value) {
      rawBalances = Array.isArray(resB.data) ? resB.data : (resB.data?.data || [])
    } else {
      const dataB = resB.data?.data
      rawBalances = Array.isArray(dataB) ? dataB : (dataB?.content || [])
    }
    balances.value = rawBalances.map(b => ({
      ...b,
      locationType: b.locationType || b.location_type,
      locationId: b.locationId || b.location_id,
      updatedAt: b.updatedAt || b.updated_at,
      updatedBy: b.updatedBy || b.updated_by
    }))
    
    const brRaw = isAdmin.value ? resBr.data : (resBr.data?.data || [])
    const brArr = Array.isArray(brRaw) ? brRaw : (brRaw?.content || [])
    const whRaw = resWh.data?.data
    const whArr = whRaw && !Array.isArray(whRaw) && whRaw.content ? whRaw.content : (Array.isArray(whRaw) ? whRaw : [])
    
    locations.value = [
      ...brArr.map(x => ({ ...x, type: 'branch' })),
      ...whArr.map(x => ({ ...x, type: 'warehouse' }))
    ]
  } catch (err) {
    toast.error('Gagal memuat data inventaris.')
  } finally {
    loading.value = false
  }
}

async function ensureProductsLoaded() {
  if (products.value.length === 0) {
    try {
      const urlProducts = isAdmin.value ? '/api/v1/products/admin' : '/api/v1/products'
      const res = await api.get(urlProducts)
      const pRaw = res.data?.data
      products.value = pRaw && pRaw.content ? pRaw.content : (Array.isArray(pRaw) ? pRaw : [])
    } catch (err) {
      toast.error('Gagal memuat produk.')
    }
  }
}

// ─── STOK AWAL / KOREKSI DRAWER CONTROL ───────────────────────────────────────
const showDrawer = ref(false)
const saving = ref(false)
const formError = ref(null)
const form = ref({ productId: '', locationId: '', locationType: 'warehouse', qty: 0 })

async function openAdjustment() {
  form.value = { productId: '', locationId: '', locationType: 'warehouse', qty: 0 }
  formError.value = null
  showDrawer.value = true
  await ensureProductsLoaded()
}

async function saveAdjustment() {
  if (!form.value.productId || !form.value.locationId || form.value.qty == null) {
    formError.value = 'Semua field wajib diisi.'
    return
  }
  saving.value = true
  formError.value = null
  try {
    await api.post('/api/v1/stock-balances', {
      product: form.value.productId,
      locationType: form.value.locationType,
      locationId: form.value.locationId,
      qty: form.value.qty
    })
    toast.success('Stok berhasil disesuaikan!')
    showDrawer.value = false
    fetchData()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Gagal menyimpan penyesuaian stok.'
  } finally {
    saving.value = false
  }
}

// ─── TRANSFER STOK DRAWER CONTROL ───────────────────────────────────────────
const showTransferDrawer = ref(false)
const transferSaving = ref(false)
const transferFormError = ref(null)
const transferForm = ref({ fromLocationType: 'warehouse', fromLocationId: '', toLocationType: 'branch', toLocationId: '', productId: '', qty: 1 })

const transferFromOptions = computed(() => locations.value.filter(x => x.type === transferForm.value.fromLocationType))
const transferToOptions = computed(() => locations.value.filter(x => x.type === transferForm.value.toLocationType))

async function openTransfer() {
  transferForm.value = { fromLocationType: 'warehouse', fromLocationId: '', toLocationType: 'branch', toLocationId: '', productId: '', qty: 1 }
  transferFormError.value = null
  showTransferDrawer.value = true
  await ensureProductsLoaded()
}

function swapTransferRoute() {
  const tempType = transferForm.value.fromLocationType
  const tempId = transferForm.value.fromLocationId
  transferForm.value.fromLocationType = transferForm.value.toLocationType
  transferForm.value.fromLocationId = transferForm.value.toLocationId
  transferForm.value.toLocationType = tempType
  transferForm.value.toLocationId = tempId
}

async function saveTransfer() {
  if (!transferForm.value.fromLocationId || !transferForm.value.toLocationId || !transferForm.value.productId || !transferForm.value.qty) {
    transferFormError.value = 'Semua field wajib diisi.'
    return
  }
  if (transferForm.value.fromLocationType === transferForm.value.toLocationType && transferForm.value.fromLocationId === transferForm.value.toLocationId) {
    transferFormError.value = 'Lokasi asal dan tujuan tidak boleh sama persis!'
    return
  }
  if (transferForm.value.qty <= 0) {
    transferFormError.value = 'Jumlah qty transfer minimal 1 Pcs.'
    return
  }

  transferSaving.value = true
  transferFormError.value = null
  try {
    // 💡 DI SINI UBAH JADI SNAKE_CASE SESUAI SELERANYA JACKSON SPRING BOOT LU MIP:
    await api.post('/api/v1/stock-balances/transfer', {
      product_id: parseInt(transferForm.value.productId),
      qty: parseInt(transferForm.value.qty),
      from_location_type: transferForm.value.fromLocationType.toUpperCase(),
      from_location_id: parseInt(transferForm.value.fromLocationId),
      to_location_type: transferForm.value.toLocationType.toUpperCase(),
      to_location_id: parseInt(transferForm.value.toLocationId)
    })
    
    toast.success('Stok berhasil dimutasi antar lokasi!')
    showTransferDrawer.value = false
    fetchData()
  } catch (err) {
    transferFormError.value = err.response?.data?.message || 'Gagal melakukan transfer stok.'
  } finally {
    transferSaving.value = false
  }
}

function getLocationName(type, id) {
  if (!type) return `Unknown (#${id})`
  const loc = locations.value.find(l => l.type.toLowerCase() === type.toLowerCase() && l.id === id)
  return loc ? loc.name : `Unknown (${type} #${id})`
}

onMounted(fetchData)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <!-- Header -->
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Saldo Stok</h1>
          <p class="text-muted-foreground text-sm mt-1">Pantau ketersediaan barang di seluruh lokasi.</p>
        </div>
        <div class="flex items-center gap-2">
          <Button v-if="can('stock_balance.store') && !isSuperAdmin" @click="openAdjustment" size="sm" class="flex items-center gap-2 bg-primary hover:bg-primary/90">
            <Plus class="h-4 w-4" />
            <span>Stok Awal / Koreksi</span>
          </Button>
        </div>
      </div>

      <!-- Filters -->
      <Card class="border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/20">
        <CardContent class="p-4">
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div class="space-y-1.5">
              <Label class="text-[10px] uppercase tracking-wider text-muted-foreground font-bold">Cari Produk</Label>
              <DataTableSearch v-model="searchQuery" placeholder="Nama atau SKU..." />
            </div>
            <div class="space-y-1.5">
              <Label class="text-[10px] uppercase tracking-wider text-muted-foreground font-bold">Tipe Lokasi</Label>
              <CustomSelect v-model="typeFilter" :options="typeOptions" class="w-full h-10" />
            </div>
            <div class="space-y-1.5">
              <Label class="text-[10px] uppercase tracking-wider text-muted-foreground font-bold">Lokasi Spesifik</Label>
              <CustomSelect v-model="locationFilter" :options="locationOptions" class="w-full h-10" />
            </div>
            <div class="flex items-end">
              <Button variant="outline" class="w-full h-10" @click="fetchData">
                <History class="h-4 w-4 mr-2" /> Segarkan
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Main Table -->
      <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm overflow-visible">
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-24">
            <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
          </div>
          <div v-else-if="filteredBalances.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
            <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
              <Package class="h-7 w-7 opacity-40" />
            </div>
            <p class="text-sm font-medium">Data stok tidak ditemukan.</p>
          </div>

          <div v-else>
            <div class="overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="border-b border-zinc-100 dark:border-zinc-800">
                    <th class="pl-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Produk</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Lokasi</th>
                    <th class="py-3 text-center text-xs font-semibold uppercase tracking-wide text-zinc-500">Saldo</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Update Terakhir</th>
                    <th class="pr-5 py-3 text-right"></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="b in paginatedBalances" :key="b.id" class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors">
                    <td class="pl-5 py-4">
                      <div class="flex items-center gap-3">
                        <div class="w-9 h-9 rounded-md bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-400 shrink-0 border border-zinc-200 dark:border-zinc-800/50">
                          <Package class="h-4.5 w-4.5" />
                        </div>
                        <div>
                          <div class="font-medium text-zinc-900 dark:text-zinc-100">{{ b.product?.name }}</div>
                          <div class="text-[10px] font-mono text-muted-foreground">{{ b.product?.sku }}</div>
                        </div>
                      </div>
                    </td>
                    <td class="py-4">
                      <div class="flex flex-col gap-1">
                        <div class="flex items-center gap-1.5 font-medium text-zinc-700 dark:text-zinc-300">
                          <Warehouse v-if="b.locationType === 'warehouse'" class="h-3.5 w-3.5 opacity-50" />
                          <Building2 v-else class="h-3.5 w-3.5 opacity-50" />
                          {{ getLocationName(b.locationType, b.locationId) }}
                        </div>
                        <Badge variant="outline" class="w-fit text-[9px] px-1.5 py-0 uppercase tracking-widest font-bold">
                          {{ b.locationType }}
                        </Badge>
                      </div>
                    </td>
                    <td class="py-4 text-center">
                      <div class="inline-flex flex-col items-center">
                        <span class="text-base font-bold" :class="b.qty <= 0 ? 'text-destructive' : 'text-zinc-900 dark:text-zinc-100'">
                          {{ b.qty }}
                        </span>
                        <span class="text-[10px] text-muted-foreground uppercase tracking-tight">PCS</span>
                      </div>
                    </td>
                    <td class="py-4">
                      <div class="text-xs text-zinc-500">
                        {{ b.updatedAt ? new Date(b.updatedAt).toLocaleString('id-ID', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }) : '-' }}
                      </div>
                      <div v-if="b.updatedBy" class="text-[10px] text-muted-foreground mt-0.5">Oleh: {{ b.updatedBy.username }}</div>
                    </td>
                    <td class="pr-5 py-4 text-right">
                      <Button variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-primary transition-colors">
                        <ArrowUpRight class="h-4 w-4" />
                      </Button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <!-- 💡 FIX AMAN: Mengubah filteredLocations menjadi filteredBalances -->
          <DataTablePagination 
            v-if="filteredBalances.length > 0 && !loading" 
            :page="page" 
            :page-size="pageSize" 
            :total="filteredBalances.length" 
            @update:page="page = $event" 
            @update:page-size="pageSize = $event; page = 1" 
          />
        </CardContent>
      </Card>
    </div>

    <!-- ─── 🛡️ PIXEL PERFECT DRAWER ADJUSTMENT (KOREKSI STOK) sesuai image_0b509d.png ─── -->
    <Teleport to="body">
        <Transition name="fade">
          <div v-if="showDrawer" class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm" @click="showDrawer = false" />
        </Transition>
    
        <Transition name="scale">
          <div v-if="showDrawer" class="fixed inset-0 z-[50] flex items-center justify-center p-4 pointer-events-none">
            
            <div class="relative flex flex-col w-full max-w-2xl max-h-[90vh] bg-background shadow-2xl border border-zinc-200 dark:border-zinc-800 rounded-xl overflow-hidden pointer-events-auto">
              
              <div class="flex items-center justify-between px-6 py-4 border-b shrink-0 border-zinc-100 dark:border-zinc-800">
                <div>
                  <h3 class="font-bold text-zinc-950 dark:text-zinc-50 text-base">Stok Awal / Koreksi</h3>
                  <p class="text-xs text-zinc-500 dark:text-zinc-400 mt-1">Atur saldo stok secara langsung.</p>
                </div>
                <Button variant="ghost" size="icon" @click="showDrawer = false" class="h-8 w-8 text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-900 rounded-md transition-colors">
                  <X class="h-4 w-4" />
                </Button>
              </div>
    
              <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5">
                <Alert v-if="formError" variant="destructive" class="text-xs py-2.5">{{ formError }}</Alert>
                
                <div class="space-y-1.5">
                  <Label class="text-sm font-semibold text-zinc-900 dark:text-zinc-100">Pilih Produk *</Label>
                  <div class="relative">
                    <select v-model="form.productId" class="w-full h-10 pl-3 pr-10 text-sm rounded-md border border-zinc-200 dark:border-zinc-800 bg-background outline-none appearance-none focus:ring-2 focus:ring-zinc-950 dark:focus:ring-zinc-300 transition-all cursor-pointer text-zinc-900 dark:text-zinc-100">
                      <option value="" disabled>Pilih produk...</option>
                      <option v-for="p in products" :key="p.id" :value="p.id">
                        {{ p.name }} ({{ p.sku }})
                      </option>
                    </select>
                    <div class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-zinc-400">
                      <ChevronDown class="h-4 w-4" />
                    </div>
                  </div>
                </div>
    
                <div class="space-y-1.5">
                  <Label class="text-sm font-semibold text-zinc-900 dark:text-zinc-100">Tipe Lokasi</Label>
                  <div class="grid grid-cols-2 gap-2">
                    <button 
                      type="button" 
                      @click="form.locationType = 'warehouse'; form.locationId = ''" 
                      :class="[
                        'flex items-center justify-center gap-2 h-10 rounded-md border text-sm font-medium transition-all shadow-sm', 
                        form.locationType === 'warehouse' 
                          ? 'bg-[#18181b] text-white border-[#18181b] dark:bg-zinc-50 dark:text-zinc-900 dark:border-zinc-50' 
                          : 'bg-background border-zinc-200 text-zinc-900 hover:bg-zinc-50 dark:border-zinc-800 dark:text-zinc-100 dark:hover:bg-zinc-900'
                      ]"
                    >
                      <Warehouse class="h-4 w-4" /> Warehouse
                    </button>
                    <button 
                      type="button" 
                      @click="form.locationType = 'branch'; form.locationId = ''" 
                      :class="[
                        'flex items-center justify-center gap-2 h-10 rounded-md border text-sm font-medium transition-all shadow-sm', 
                        form.locationType === 'branch' 
                          ? 'bg-[#18181b] text-white border-[#18181b] dark:bg-zinc-50 dark:text-zinc-900 dark:border-zinc-50' 
                          : 'bg-background border-zinc-200 text-zinc-900 hover:bg-zinc-50 dark:border-zinc-800 dark:text-zinc-100 dark:hover:bg-zinc-900'
                      ]"
                    >
                      <Building2 class="h-4 w-4" /> Branch
                    </button>
                  </div>
                </div>
    
                <div class="space-y-1.5">
                  <Label class="text-sm font-semibold text-zinc-900 dark:text-zinc-100">Pilih Lokasi *</Label>
                  <div class="relative">
                    <select v-model="form.locationId" class="w-full h-10 pl-3 pr-10 text-sm rounded-md border border-zinc-200 dark:border-zinc-800 bg-background outline-none appearance-none focus:ring-2 focus:ring-zinc-950 dark:focus:ring-zinc-300 transition-all cursor-pointer text-zinc-900 dark:text-zinc-100">
                      <option value="" disabled>Pilih lokasi...</option>
                      <option v-for="l in locations.filter(x => x.type === form.locationType)" :key="l.id" :value="l.id">
                        {{ l.name }}
                      </option>
                    </select>
                    <div class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-zinc-400">
                      <ChevronDown class="h-4 w-4" />
                    </div>
                  </div>
                </div>
    
                <div class="space-y-1.5">
                  <Label for="qty" class="text-sm font-semibold text-zinc-900 dark:text-zinc-100">Jumlah Stok *</Label>
                  <div class="relative flex items-center">
                    <Input 
                      id="qty" 
                      v-model.number="form.qty" 
                      type="number" 
                      placeholder="0" 
                      class="h-10 pr-12 pl-3 w-full border border-zinc-200 dark:border-zinc-800 focus-visible:ring-2 focus-visible:ring-zinc-950 dark:focus-visible:ring-zinc-300 rounded-md bg-background text-sm shadow-sm" 
                    />
                    <div class="absolute inset-y-0 right-0 flex items-center pr-4 pointer-events-none text-[10px] font-bold text-zinc-400 tracking-wide">
                      PCS
                    </div>
                  </div>
                </div>
              </div>
    
              <div class="flex justify-end gap-3 px-6 py-4 border-t bg-background border-zinc-100 dark:border-zinc-800 shrink-0">
                <Button variant="outline" @click="showDrawer = false" :disabled="saving" class="h-9 px-4 text-sm font-medium rounded-md border border-zinc-200 dark:border-zinc-800 bg-background text-zinc-900 dark:text-zinc-100 hover:bg-zinc-50 dark:hover:bg-zinc-900">
                  Batal
                </Button>
                <Button @click="saveAdjustment" :disabled="saving" class="h-9 px-4 text-sm font-medium rounded-md bg-[#18181b] hover:bg-[#18181b]/90 text-white dark:bg-zinc-50 dark:text-zinc-900 dark:hover:bg-zinc-200 shadow-sm">
                  <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
                  Simpan Saldo
                </Button>
              </div>
    
            </div>
          </div>
        </Transition>
      </Teleport>
  </AppLayout>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
 
.scale-enter-active,
.scale-leave-active {
  transition: opacity 0.25s ease, transform 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}
.scale-enter-from,
.scale-leave-to {
  opacity: 0;
  transform: scale(0.96);
}</style>