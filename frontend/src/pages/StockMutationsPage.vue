<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Badge from '@/components/ui/Badge.vue'
import Alert from '@/components/ui/Alert.vue'
import api from '@/lib/api'
import { 
  Plus, 
  ArrowLeftRight, 
  ArrowUpRight, 
  ArrowDownLeft, 
  Settings2, 
  RotateCcw, 
  Loader2, 
  X, 
  Filter, 
  History,
  Search,
  ChevronDown
} from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()

// ─── State ───────────────────────────────────────────────────────────────────
const mutations = ref([])
const products = ref([])
const partners = ref([])
const locations = ref([
  { id: 'wh-1', name: 'Gudang Utama', type: 'warehouse' },
  { id: 'br-1', name: 'Cabang Jakarta', type: 'branch' },
  { id: 'br-2', name: 'Cabang Bandung', type: 'branch' },
])

const loading = ref(false)
const error = ref(null)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

const mutationTypes = [
  { value: 'sale_out', label: 'Penjualan (Out)', icon: ArrowUpRight, color: 'text-red-500 bg-red-50' },
  { value: 'purchase_in', label: 'Pembelian (In)', icon: ArrowDownLeft, color: 'text-emerald-500 bg-emerald-50' },
  { value: 'transfer', label: 'Transfer Stok', icon: ArrowLeftRight, color: 'text-blue-500 bg-blue-50' },
  { value: 'adjustment', label: 'Penyesuaian', icon: Settings2, color: 'text-amber-500 bg-amber-50' },
  { value: 'return', label: 'Retur', icon: RotateCcw, color: 'text-purple-500 bg-purple-50' },
]

const referenceTypes = [
  { value: 'order', label: 'Pesanan (Order)' },
  { value: 'transfer_request', label: 'Permintaan Transfer' },
  { value: 'stock_opname', label: 'Stock Opname' },
]

// ─── Form State ───────────────────────────────────────────────────────────────
const showDrawer = ref(false)
const saving = ref(false)
const formError = ref(null)

const emptyForm = () => ({
  id: null,
  partner_id: '',
  product_id: '',
  type: 'sale_out',
  from_location_type: '',
  from_location_id: '',
  to_location_type: '',
  to_location_id: '',
  qty: 1,
  reference_type: '',
  reference_id: '',
  notes: '',
})

const form = ref(emptyForm())

// ─── Computed ────────────────────────────────────────────────────────────────
const filteredMutations = computed(() => {
  if (!searchQuery.value) return mutations.value
  const q = searchQuery.value.toLowerCase()
  return mutations.value.filter(m => 
    m.productName?.toLowerCase().includes(q) || 
    m.notes?.toLowerCase().includes(q) ||
    m.type?.toLowerCase().includes(q)
  )
})

const paginatedMutations = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredMutations.value.slice(start, start + pageSize.value)
})

// ─── Actions ──────────────────────────────────────────────────────────────────
async function fetchMutations() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/stock-mutations')
    mutations.value = res.data.data
  } catch (err) {
    if (import.meta.env.DEV) {
      // Mock data for dev
      mutations.value = Array.from({ length: 5 }).map((_, i) => ({
        id: `m-${i}`,
        productName: 'Kaos Polos Putih',
        type: i % 2 === 0 ? 'sale_out' : 'purchase_in',
        qty: 10 + i,
        notes: 'Mutasi stok harian',
        createdAt: new Date().toISOString(),
        createdBy: 'Admin',
      }))
    } else {
      error.value = 'Gagal memuat data mutasi.'
    }
  } finally {
    loading.value = false
  }
}

async function fetchProducts() {
  try {
    const res = await api.get('/api/v1/products?size=100')
    products.value = res.data.data.content
  } catch (_) {}
}

async function fetchPartners() {
  // Mocking partners for now
  partners.value = [
    { id: 'p1', name: 'Supplier A' },
    { id: 'p2', name: 'Customer B' },
  ]
}

function openCreate() {
  form.value = emptyForm()
  formError.value = null
  showDrawer.value = true
}

async function saveMutation() {
  saving.value = true
  formError.value = null
  try {
    await api.post('/api/v1/stock-mutations', form.value)
    toast.success('Mutasi stok berhasil dicatat!')
    showDrawer.value = false
    fetchMutations()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Gagal menyimpan mutasi.'
  } finally {
    saving.value = false
  }
}

function getTypeInfo(type) {
  return mutationTypes.find(t => t.value === type) || mutationTypes[0]
}

onMounted(() => {
  fetchMutations()
  fetchProducts()
  fetchPartners()
})
</script>

<template>
  <AppLayout>
    <div class="pb-6">
      <!-- Header -->
      <div class="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100">Mutasi Stok</h1>
          <p class="text-xs text-zinc-500 mt-0.5">Pantau dan kelola pergerakan stok barang.</p>
        </div>
        <Button @click="openCreate" class="bg-primary hover:bg-primary/90 flex items-center gap-2">
          <Plus class="h-4 w-4" />
          <span>Tambah Mutasi</span>
        </Button>
      </div>

      <!-- Controls -->
      <div class="flex flex-col sm:flex-row items-center justify-between gap-4 mb-5">
        <DataTableSearch v-model="searchQuery" placeholder="Cari mutasi..." class="w-full sm:max-w-sm" />
      </div>

      <!-- Table -->
      <Card>
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-20">
            <Loader2 class="h-6 w-6 animate-spin text-muted-foreground" />
          </div>

          <div v-else-if="filteredMutations.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
            <History class="h-10 w-10 mb-3 opacity-20" />
            <p class="text-sm">Belum ada data mutasi stok.</p>
          </div>

          <div v-else>
            <!-- ─── Mobile List View ─── -->
            <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
              <div
                v-for="m in paginatedMutations"
                :key="'mobile-' + m.id"
                class="p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
              >
                <div class="flex items-start justify-between gap-3">
                  <div class="flex items-center gap-3">
                    <div :class="['p-2 rounded-lg shrink-0', getTypeInfo(m.type).color]">
                      <component :is="getTypeInfo(m.type).icon" class="h-4 w-4" />
                    </div>
                    <div>
                      <h4 class="font-medium text-sm text-zinc-900 dark:text-zinc-100">{{ m.productName }}</h4>
                      <span class="text-[10px] text-zinc-500">{{ new Date(m.createdAt).toLocaleString('id-ID') }}</span>
                    </div>
                  </div>
                  <div class="text-right">
                    <div class="text-sm font-bold" :class="m.type.endsWith('_in') ? 'text-emerald-600' : 'text-red-600'">
                      {{ m.type.endsWith('_in') ? '+' : '-' }}{{ m.qty }}
                    </div>
                    <span class="text-[10px] font-medium text-zinc-400 uppercase tracking-tighter">{{ getTypeInfo(m.type).label }}</span>
                  </div>
                </div>
                <div v-if="m.notes" class="text-xs text-zinc-500 bg-zinc-50 dark:bg-zinc-800/50 p-2 rounded border border-zinc-100 dark:border-zinc-800/60 line-clamp-2">
                  {{ m.notes }}
                </div>
                <div class="flex items-center justify-between text-[10px] text-zinc-400">
                  <span>Oleh: {{ m.createdBy }}</span>
                  <span>ID: {{ m.id }}</span>
                </div>
              </div>
            </div>

            <!-- ─── Desktop Table ─── -->
            <div class="hidden md:block overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="bg-muted/40 border-b">
                    <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Tanggal</th>
                    <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Produk</th>
                    <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Tipe</th>
                    <th class="px-5 py-3 text-right font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Qty</th>
                    <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Keterangan</th>
                    <th class="px-5 py-3 text-right font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Oleh</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="m in paginatedMutations" :key="m.id" class="border-b last:border-0 hover:bg-muted/30 transition-colors">
                    <td class="px-5 py-3 whitespace-nowrap text-xs text-muted-foreground">
                      {{ new Date(m.createdAt).toLocaleString('id-ID') }}
                    </td>
                    <td class="px-5 py-3 font-medium text-zinc-900 dark:text-zinc-100">{{ m.productName }}</td>
                    <td class="px-5 py-3">
                      <div class="flex items-center gap-2">
                        <div :class="['p-1 rounded-md', getTypeInfo(m.type).color]">
                          <component :is="getTypeInfo(m.type).icon" class="h-3 w-3" />
                        </div>
                        <span class="text-xs font-medium">{{ getTypeInfo(m.type).label }}</span>
                      </div>
                    </td>
                    <td class="px-5 py-3 text-right font-semibold" :class="m.type.endsWith('_in') ? 'text-emerald-600' : 'text-red-600'">
                      {{ m.type.endsWith('_in') ? '+' : '-' }}{{ m.qty }}
                    </td>
                    <td class="px-5 py-3 text-xs text-muted-foreground max-w-[200px] truncate">
                      {{ m.notes || '—' }}
                    </td>
                    <td class="px-5 py-3 text-right text-xs text-muted-foreground">
                      {{ m.createdBy }}
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

    <!-- ─── Drawer ─── -->
    <Teleport to="body">
      <!-- Backdrop -->
      <Transition name="fade">
        <div v-if="showDrawer" class="fixed inset-0 z-[100] bg-black/40 backdrop-blur-sm" @click="showDrawer = false" />
      </Transition>

      <!-- Panel -->
      <Transition name="slide-right">
        <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[101] flex flex-col w-full sm:max-w-[480px] h-full bg-card shadow-2xl sm:border-l overflow-hidden">
          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
            <div>
              <h3 class="font-semibold text-base">Tambah Mutasi Stok</h3>
              <p class="text-xs text-muted-foreground mt-0.5">Catat pergerakan stok baru.</p>
            </div>
            <Button variant="ghost" size="icon" @click="showDrawer = false">
              <X class="h-4 w-4" />
            </Button>
          </div>

          <!-- Body -->
          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5">
            <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>

            <!-- Tipe Mutasi -->
            <div class="space-y-1.5">
              <Label>Tipe Mutasi <span class="text-destructive">*</span></Label>
              <div class="grid grid-cols-2 gap-2">
                <button 
                  v-for="t in mutationTypes" 
                  :key="t.value"
                  @click="form.type = t.value"
                  class="flex flex-col items-center gap-2 p-3 rounded-lg border transition-all text-center"
                  :class="form.type === t.value ? 'border-primary bg-primary/5 text-primary ring-1 ring-primary' : 'border-zinc-200 dark:border-zinc-800 hover:bg-zinc-50 dark:hover:bg-zinc-900'"
                >
                  <component :is="t.icon" class="h-5 w-5" />
                  <span class="text-[10px] font-semibold leading-tight">{{ t.label }}</span>
                </button>
              </div>
            </div>

            <!-- Produk -->
            <div class="space-y-1.5">
              <Label for="product">Produk <span class="text-destructive">*</span></Label>
              <select id="product" v-model="form.product_id" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring">
                <option value="" disabled>Pilih Produk...</option>
                <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }} ({{ p.sku }})</option>
              </select>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <!-- Partner -->
              <div class="space-y-1.5">
                <Label for="partner">Partner (Supplier/Cust)</Label>
                <select id="partner" v-model="form.partner_id" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm">
                  <option value="">None</option>
                  <option v-for="p in partners" :key="p.id" :value="p.id">{{ p.name }}</option>
                </select>
              </div>
              <!-- Qty -->
              <div class="space-y-1.5">
                <Label for="qty">Jumlah <span class="text-destructive">*</span></Label>
                <Input id="qty" type="number" v-model="form.qty" min="1" step="0.01" />
              </div>
            </div>

            <hr class="border-zinc-100 dark:border-zinc-800" />

            <!-- Locations -->
            <div class="space-y-4">
              <div class="grid grid-cols-2 gap-4">
                <div class="space-y-1.5">
                  <Label>Asal (From)</Label>
                  <select v-model="form.from_location_id" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm">
                    <option value="">None</option>
                    <option v-for="l in locations" :key="l.id" :value="l.id">[{{ l.type }}] {{ l.name }}</option>
                  </select>
                </div>
                <div class="space-y-1.5">
                  <Label>Tujuan (To)</Label>
                  <select v-model="form.to_location_id" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm">
                    <option value="">None</option>
                    <option v-for="l in locations" :key="l.id" :value="l.id">[{{ l.type }}] {{ l.name }}</option>
                  </select>
                </div>
              </div>
            </div>

            <!-- Reference -->
            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <Label>Tipe Referensi</Label>
                <select v-model="form.reference_type" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm">
                  <option value="">None</option>
                  <option v-for="r in referenceTypes" :key="r.value" :value="r.value">{{ r.label }}</option>
                </select>
              </div>
              <div class="space-y-1.5">
                <Label>ID Referensi</Label>
                <Input v-model="form.reference_id" placeholder="ID order/req..." />
              </div>
            </div>

            <!-- Notes -->
            <div class="space-y-1.5">
              <Label for="notes">Catatan</Label>
              <textarea 
                id="notes" 
                v-model="form.notes" 
                rows="3" 
                class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                placeholder="Tambahkan catatan mutasi..."
              />
            </div>
          </div>

          <!-- Footer -->
          <div class="flex justify-end gap-3 px-6 py-4 border-t bg-muted/30">
            <Button variant="outline" @click="showDrawer = false" :disabled="saving">Batal</Button>
            <Button @click="saveMutation" :disabled="saving">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              Simpan Mutasi
            </Button>
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
