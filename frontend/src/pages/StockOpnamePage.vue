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
  Plus, Loader2, ArrowLeft, PackageSearch, Eye, Check,
  Warehouse, Building2, Calendar, Package, RefreshCw
} from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.isAdmin)
const isSuperAdmin = computed(() => authStore.isSuperAdmin)

const opnames = ref([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('all')
const page = ref(1)
const pageSize = ref(10)

const statusOptions = [
  { value: 'all', label: 'Semua Status' },
  { value: 'draft', label: 'Draf' },
  { value: 'counting', label: 'Menghitung' },
  { value: 'reviewed', label: 'Ditinjau' },
  { value: 'approved', label: 'Disetujui' }
]

const locations = ref([])
const products = ref([])

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

const showForm = ref(false)
const formMode = ref('create')
const saving = ref(false)
const formError = ref(null)
const selectedOpname = ref(null)

const emptyForm = () => ({
  location: 'warehouse',
  locationId: '',
  date: new Date().toISOString().slice(0, 16),
  notes: '',
  items: []
})

const form = ref(emptyForm())

async function fetchData() {
  loading.value = true
  try {
    const url = isAdmin.value ? '/api/v1/stock-opnames/admin' : '/api/v1/stock-opnames'
    const res = await api.get(url)
    const dataO = Array.isArray(res.data) ? res.data : (res.data?.data || [])
    opnames.value = dataO.map(o => ({
      ...o,
      locationType: o.location || o.locationType || o.location_type,
      locationId: o.location_id || o.locationId,
      locationName: o.locationName || o.location_name || `${o.location || ''} #${o.location_id || o.locationId || ''}`,
    }))
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
    const [resB, resW] = await Promise.all([api.get(urlB), api.get(urlW)])
    const brRaw = isAdmin.value ? resB.data : (resB.data?.data || [])
    const brArr = Array.isArray(brRaw) ? brRaw : (brRaw?.content || [])
    const whRaw = resW.data?.data
    const whArr = whRaw && !Array.isArray(whRaw) && whRaw.content ? whRaw.content : (Array.isArray(whRaw) ? whRaw : [])
    locations.value = [
      ...brArr.map(x => ({ ...x, type: 'branch' })),
      ...whArr.map(x => ({ ...x, type: 'warehouse' }))
    ]
  } catch (err) {
    toast.error('Gagal memuat opsi lokasi.')
  }
}

async function openCreate() {
  form.value = emptyForm()
  formMode.value = 'create'
  formError.value = null
  showForm.value = true
  await loadFormOptions()
}

async function fetchLocationStock() {
  if (!form.value.locationId) { toast.warning('Pilih lokasi terlebih dahulu.'); return }
  loading.value = true
  try {
    const urlStock = isAdmin.value ? '/api/v1/stock-balances/admin' : '/api/v1/stock-balances'
    const res = await api.get(`${urlStock}?locationType=${form.value.location}&locationId=${form.value.locationId}`)
    const stocks = res.data.data?.content || res.data.data || []
    if (stocks.length === 0) toast.info('Tidak ada stok terdaftar di lokasi ini.')
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

function openDetail(opname) {
  selectedOpname.value = opname
  formMode.value = 'detail'
  showForm.value = true
}

function closeForm() { showForm.value = false }

async function saveOpname() {
  if (!form.value.locationId) { formError.value = 'Lokasi wajib diisi.'; return }
  if (form.value.items.length === 0) { formError.value = 'Belum ada item. Gunakan "Tarik Stok" terlebih dahulu.'; return }
  saving.value = true
  formError.value = null
  try {
    await api.post('/api/v1/stock-opnames', form.value)
    toast.success('Stock Opname berhasil disimpan!')
    showForm.value = false
    fetchData()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Gagal menyimpan stock opname.'
  } finally {
    saving.value = false
  }
}

async function approveOpname(o) {
  try {
    await api.patch(`/api/v1/stock-opnames/${o.id}/status`, { status: 'ADJUSTED' })
    toast.success('Opname berhasil disetujui dan stok disesuaikan!')
    fetchData()
    showForm.value = false
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menyetujui opname.')
  }
}

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
  const m = { draft: 'Draf', counting: 'Menghitung', reviewed: 'Ditinjau', approved: 'Disetujui' }
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
      <Transition name="fade" mode="out-in">

        <!-- ─── Table View ─── -->
        <div v-if="!showForm" key="table-view" class="flex flex-col gap-6">
          <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
            <div>
              <h1 class="text-2xl font-bold tracking-tight">Pengecekan Stok</h1>
              <p class="text-muted-foreground text-sm mt-1">Audit dan rekonsiliasi stok barang secara berkala.</p>
            </div>
            <div class="flex items-center gap-3 w-full md:w-auto">
              <div class="w-full sm:w-72">
                <DataTableSearch v-model="searchQuery" placeholder="Cari lokasi atau catatan..." />
              </div>
              <CustomSelect v-model="statusFilter" :options="statusOptions" class="w-full sm:w-44" />
              <Button v-if="can('stock_opname.store') && !isSuperAdmin" @click="openCreate" size="sm" class="flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
                <Plus class="h-4 w-4" /><span>Buat Pengecekan</span>
              </Button>
            </div>
          </div>

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
                <Button v-if="can('stock_opname.store') && !isSuperAdmin && !searchQuery" size="sm" class="mt-4" @click="openCreate">
                  <Plus class="h-3.5 w-3.5 mr-1.5" />Mulai Opname Baru
                </Button>
              </div>
              <div v-else>
                <!-- Mobile -->
                <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
                  <div v-for="o in paginatedOpnames" :key="o.id" class="p-4 flex flex-col gap-2 hover:bg-zinc-50 dark:hover:bg-zinc-900/40 cursor-pointer transition-colors" @click="openDetail(o)">
                    <div class="flex justify-between items-center">
                      <span class="text-xs font-bold text-zinc-900 dark:text-zinc-100">{{ o.locationName }}</span>
                      <Badge :class="['text-[9px] uppercase tracking-wider', statusColor(o.status)]" variant="outline">{{ statusLabel(o.status) }}</Badge>
                    </div>
                    <div class="flex justify-between items-end text-[10px] text-muted-foreground">
                      <div>
                        <div class="flex items-center gap-1"><Calendar class="h-3 w-3" /> {{ formatDate(o.date) }}</div>
                        <div class="flex items-center gap-1 mt-1"><Package class="h-3 w-3" /> {{ o.items?.length || 0 }} Item</div>
                      </div>
                      <div class="flex flex-col items-end">
                        <span class="text-[9px] uppercase font-bold text-zinc-400">Auditor</span>
                        <span>{{ o.createdBy?.username || '-' }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- Desktop -->
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
                        <td class="py-4 text-xs text-muted-foreground">{{ formatDate(o.date) }}</td>
                        <td class="py-4 text-center">
                          <div class="inline-flex items-center justify-center px-2 py-1 rounded bg-zinc-100 dark:bg-zinc-800 text-[10px] font-bold uppercase">{{ o.items?.length || 0 }} Item</div>
                        </td>
                        <td class="py-4 text-center">
                          <Badge :class="['text-[9px] uppercase tracking-widest font-bold', statusColor(o.status)]" variant="outline">{{ statusLabel(o.status) }}</Badge>
                        </td>
                        <td class="py-4 text-xs font-medium">{{ o.createdBy?.username || '-' }}</td>
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
              <DataTablePagination v-if="filteredOpnames.length > 0 && !loading" :page="page" :page-size="pageSize" :total="filteredOpnames.length" @update:page="page = $event" @update:page-size="pageSize = $event; page = 1" />
            </CardContent>
          </Card>
        </div>

        <!-- ─── Inline Form / Detail View ─── -->
        <div v-else key="form-view" class="flex flex-col gap-6">

          <!-- Header -->
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-zinc-200 dark:border-zinc-800 pb-4">
            <div class="flex items-center gap-3">
              <Button variant="outline" size="icon" @click="closeForm" :disabled="saving" class="h-9 w-9 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800">
                <ArrowLeft class="h-4 w-4" />
              </Button>
              <div>
                <h2 class="text-xl font-bold tracking-tight flex items-center gap-2">
                  <span>{{ formMode === 'create' ? 'Input Hasil Pengecekan' : 'Detail Pengecekan Stok' }}</span>
                  <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-primary/10 text-primary border border-primary/20 uppercase tracking-wider">
                    {{ formMode === 'create' ? 'Baru' : 'Detail' }}
                  </span>
                </h2>
                <p class="text-xs text-muted-foreground mt-0.5">Rekonsiliasi perbedaan stok fisik dan sistem.</p>
              </div>
            </div>
            <div v-if="formMode === 'create'" class="flex items-center gap-3 w-full sm:w-auto">
              <Button variant="outline" @click="closeForm" :disabled="saving" class="flex-1 sm:flex-none">Batal</Button>
              <Button @click="saveOpname" :disabled="saving || form.items.length === 0" class="flex-1 sm:flex-none bg-primary text-primary-foreground hover:bg-primary/95 shadow-md shadow-primary/20">
                <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
                <span>Simpan Draf Pengecekan</span>
              </Button>
            </div>
            <div v-else class="flex items-center gap-3 w-full sm:w-auto">
              <Button v-if="selectedOpname?.status === 'approved' && can('stock_opname.update') && !isSuperAdmin" @click="approveOpname(selectedOpname)" class="flex-1 sm:flex-none bg-primary text-primary-foreground font-bold">
                <Check class="h-4 w-4 mr-2" /> Setujui &amp; Rekonsiliasi
              </Button>
              <Button variant="outline" @click="closeForm" class="flex-1 sm:flex-none">Tutup</Button>
            </div>
          </div>

          <!-- CREATE FORM -->
          <template v-if="formMode === 'create'">
            <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">

              <!-- LEFT -->
              <div class="lg:col-span-5 space-y-6">
                <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                  <CardContent class="p-5 space-y-4">
                    <h3 class="text-sm font-semibold border-b pb-2 border-zinc-100 dark:border-zinc-800 flex items-center gap-2">
                      <Warehouse class="h-4 w-4 text-primary" /><span>Pilih Lokasi Audit</span>
                    </h3>
                    <div class="grid grid-cols-2 gap-2">
                      <button type="button" @click="form.location = 'warehouse'; form.locationId = ''; form.items = []"
                        :class="['flex items-center justify-center gap-2 h-9 rounded-lg border text-[10px] font-bold transition-all', form.location === 'warehouse' ? 'bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 border-zinc-900' : 'bg-white dark:bg-zinc-900 border-zinc-200 dark:border-zinc-700 hover:bg-zinc-50 dark:hover:bg-zinc-800']">
                        <Warehouse class="h-3.5 w-3.5" />GUDANG
                      </button>
                      <button type="button" @click="form.location = 'branch'; form.locationId = ''; form.items = []"
                        :class="['flex items-center justify-center gap-2 h-9 rounded-lg border text-[10px] font-bold transition-all', form.location === 'branch' ? 'bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 border-zinc-900' : 'bg-white dark:bg-zinc-900 border-zinc-200 dark:border-zinc-700 hover:bg-zinc-50 dark:hover:bg-zinc-800']">
                        <Building2 class="h-3.5 w-3.5" />CABANG
                      </button>
                    </div>
                    <select v-model="form.locationId" @change="form.items = []" class="w-full h-10 rounded-lg border border-input bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-primary/20">
                      <option value="" disabled>Pilih lokasi...</option>
                      <option v-for="l in locations.filter(x => x.type === form.location)" :key="l.id" :value="l.id">{{ l.name }}</option>
                    </select>
                  </CardContent>
                </Card>

                <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                  <CardContent class="p-5 space-y-3">
                    <h3 class="text-sm font-semibold border-b pb-2 border-zinc-100 dark:border-zinc-800 flex items-center gap-2">
                      <Calendar class="h-4 w-4 text-primary" /><span>Waktu Audit</span>
                    </h3>
                    <Input v-model="form.date" type="datetime-local" class="h-10 rounded-lg" />
                  </CardContent>
                </Card>

                <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                  <CardContent class="p-5 space-y-3">
                    <Label class="text-xs font-semibold">Catatan Audit</Label>
                    <textarea v-model="form.notes" rows="3" class="w-full rounded-lg border border-input bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-primary/20 resize-none" placeholder="Contoh: Barang rusak ditemukan, stok tertukar, dll." />
                  </CardContent>
                </Card>
              </div>

              <!-- RIGHT -->
              <div class="lg:col-span-7">
                <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                  <CardContent class="p-5 space-y-4">
                    <div class="flex items-center justify-between border-b pb-3 border-zinc-100 dark:border-zinc-800">
                      <h3 class="text-sm font-semibold flex items-center gap-2">
                        <Package class="h-4 w-4 text-primary" /><span>Daftar Item &amp; Hasil Hitung</span>
                      </h3>
                      <Button variant="outline" size="sm" class="h-8 text-[10px] font-bold bg-primary/5 border-primary/20 text-primary hover:bg-primary/10" @click="fetchLocationStock" :disabled="!form.locationId || loading">
                        <RefreshCw class="h-3 w-3 mr-1.5" :class="{ 'animate-spin': loading }" />Tarik Stok Sistem
                      </Button>
                    </div>

                    <div v-if="form.items.length === 0" class="flex flex-col items-center justify-center py-12 border-2 border-dashed border-zinc-200 dark:border-zinc-800 rounded-xl bg-zinc-50/50 dark:bg-zinc-900/20">
                      <Package class="h-8 w-8 text-zinc-300 dark:text-zinc-600 mb-2" />
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
                            <div class="h-8 flex items-center px-3 bg-zinc-100 dark:bg-zinc-800 rounded-md text-xs font-bold border border-zinc-200 dark:border-zinc-700">{{ item.qtySystem }} PCS</div>
                          </div>
                          <div class="space-y-1">
                            <Label class="text-[10px] uppercase text-primary font-bold">Fisik Terhitung</Label>
                            <Input v-model.number="item.qtyPhysical" type="number" class="h-8 text-xs font-bold rounded-lg border-primary/30" />
                          </div>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </div>
          </template>

          <!-- DETAIL VIEW -->
          <template v-else-if="formMode === 'detail' && selectedOpname">
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
              <div class="lg:col-span-4 space-y-6">
                <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                  <CardContent class="p-5 space-y-4">
                    <div class="flex items-center justify-between bg-primary/5 p-4 rounded-xl border border-primary/10">
                      <div>
                        <span class="text-[10px] font-bold text-primary uppercase tracking-widest block mb-1">{{ selectedOpname.locationType }}</span>
                        <h4 class="text-base font-bold leading-none">{{ selectedOpname.locationName }}</h4>
                      </div>
                      <Badge :class="['text-[10px] uppercase tracking-widest font-bold px-3 py-1', statusColor(selectedOpname.status)]" variant="outline">{{ statusLabel(selectedOpname.status) }}</Badge>
                    </div>
                    <div class="grid grid-cols-2 gap-3">
                      <div class="p-3 rounded-lg border border-zinc-100 dark:border-zinc-800 bg-card">
                        <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-1">Auditor</p>
                        <p class="font-medium text-xs">{{ selectedOpname.createdBy?.username || '-' }}</p>
                      </div>
                      <div class="p-3 rounded-lg border border-zinc-100 dark:border-zinc-800 bg-card">
                        <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-1">Waktu</p>
                        <p class="font-medium text-xs">{{ formatDate(selectedOpname.date) }}</p>
                      </div>
                    </div>
                    <div v-if="selectedOpname.notes" class="bg-zinc-50 dark:bg-zinc-900 p-3 rounded-lg border border-zinc-100 dark:border-zinc-800">
                      <p class="text-[10px] font-bold text-zinc-400 uppercase tracking-wider mb-1">Catatan</p>
                      <p class="text-xs italic">{{ selectedOpname.notes }}</p>
                    </div>
                  </CardContent>
                </Card>
              </div>

              <div class="lg:col-span-8">
                <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                  <CardContent class="p-5 space-y-4">
                    <h3 class="text-sm font-semibold border-b pb-3 border-zinc-100 dark:border-zinc-800 flex items-center gap-2">
                      <Package class="h-4 w-4 text-primary" /><span>Rincian Hasil Audit</span>
                      <span class="ml-auto text-[10px] font-bold text-muted-foreground">{{ selectedOpname.items?.length || 0 }} Item</span>
                    </h3>
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
                  </CardContent>
                </Card>
              </div>
            </div>
          </template>

        </div>
      </Transition>
    </div>
  </AppLayout>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
