<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Alert from '@/components/ui/Alert.vue'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'
import CustomSelect from '@/components/ui/CustomSelect.vue'
import { Plus, Loader2, X, PackageSearch, Eye, Check, AlertTriangle } from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()
const opnames = ref([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('all')
const statusOptions = [
  { value: 'all', label: 'Semua Status' },
  { value: 'draft', label: 'Draft' },
  { value: 'counting', label: 'Counting' },
  { value: 'approved', label: 'Approved' }
]
const locationTypeOptions = [
  { value: 'warehouse', label: 'Warehouse' },
  { value: 'branch', label: 'Branch' }
]
const page = ref(1)
const pageSize = ref(10)
const showDrawer = ref(false)
const drawerMode = ref('detail')
const saving = ref(false)
const formError = ref(null)
const selectedOpname = ref(null)

const MOCK = [
  { id: 'so1', locationName: 'Gudang Utama', locationType: 'warehouse', opnameDate: '2026-05-01', status: 'approved', notes: 'Opname bulanan', createdBy: 'Admin', createdAt: '2026-05-01T08:00:00Z',
    items: [
      { productName: 'Kaos Polos Putih', qtySystem: 50, qtyPhysical: 48, qtyDifference: -2, notes: '2 pcs rusak' },
      { productName: 'Celana Chino Beige', qtySystem: 30, qtyPhysical: 30, qtyDifference: 0, notes: '' },
      { productName: 'Sepatu Sneakers Hitam', qtySystem: 15, qtyPhysical: 17, qtyDifference: 2, notes: 'Ada barang belum tercatat' },
    ] },
  { id: 'so2', locationName: 'Cabang Jakarta', locationType: 'branch', opnameDate: '2026-05-03', status: 'counting', notes: '', createdBy: 'Admin', createdAt: '2026-05-03T10:00:00Z',
    items: [
      { productName: 'Jaket Bomber Olive', qtySystem: 10, qtyPhysical: null, qtyDifference: null, notes: '' },
      { productName: 'Kemeja Flannel Kotak', qtySystem: 8, qtyPhysical: 7, qtyDifference: -1, notes: '' },
    ] },
  { id: 'so3', locationName: 'Cabang Bandung', locationType: 'branch', opnameDate: '2026-05-05', status: 'draft', notes: 'Persiapan audit', createdBy: 'Admin', createdAt: '2026-05-05T14:00:00Z',
    items: [] },
]

const emptyForm = () => ({ locationName: '', locationType: 'warehouse', opnameDate: new Date().toISOString().slice(0, 10), notes: '' })
const form = ref(emptyForm())

const filteredOpnames = computed(() => {
  let r = opnames.value
  if (statusFilter.value !== 'all') r = r.filter(o => o.status === statusFilter.value)
  if (searchQuery.value) { const q = searchQuery.value.toLowerCase(); r = r.filter(o => o.locationName.toLowerCase().includes(q)) }
  return r
})
const paginatedOpnames = computed(() => { const s = (page.value - 1) * pageSize.value; return filteredOpnames.value.slice(s, s + pageSize.value) })

function fetchOpnames() { loading.value = true; setTimeout(() => { opnames.value = [...MOCK]; loading.value = false }, 300) }
function openCreate() { form.value = emptyForm(); drawerMode.value = 'create'; formError.value = null; showDrawer.value = true }
function openDetail(opname) { selectedOpname.value = opname; drawerMode.value = 'detail'; showDrawer.value = true }

function saveOpname() {
  if (!form.value.locationName) { formError.value = 'Lokasi wajib diisi.'; return }
  saving.value = true
  setTimeout(() => {
    opnames.value.unshift({ id: 'so-' + Date.now(), ...form.value, status: 'draft', createdBy: 'Admin', createdAt: new Date().toISOString(), items: [] })
    toast.success('Stock Opname berhasil dibuat!'); showDrawer.value = false; saving.value = false
  }, 400)
}

function approveOpname(o) { o.status = 'approved'; toast.success('Opname disetujui!') }

function formatDate(dt) { return dt ? new Date(dt).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' }) : '-' }
function statusColor(s) {
  const m = { approved: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-400', adjusted: 'bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-400', reviewed: 'bg-violet-50 text-violet-700 dark:bg-violet-900/20 dark:text-violet-400', counting: 'bg-amber-50 text-amber-700 dark:bg-amber-900/20 dark:text-amber-400' }
  return m[s] || 'bg-zinc-100 text-zinc-700 dark:bg-zinc-800 dark:text-zinc-400'
}
function statusLabel(s) { const m = { draft: 'Draft', counting: 'Counting', reviewed: 'Reviewed', approved: 'Approved', adjusted: 'Adjusted' }; return m[s] || s }
function diffColor(d) { if (d == null) return ''; if (d < 0) return 'text-red-600 dark:text-red-400'; if (d > 0) return 'text-emerald-600 dark:text-emerald-400'; return 'text-zinc-500' }

onMounted(fetchOpnames)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div><h1 class="text-2xl font-bold tracking-tight">Stock Opname</h1><p class="text-muted-foreground text-sm mt-1">Hitung dan rekonsiliasi stok fisik.</p></div>
        <div class="flex items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-72"><DataTableSearch v-model="searchQuery" placeholder="Cari opname..." /></div>
          <CustomSelect v-model="statusFilter" :options="statusOptions" class="w-full sm:w-44" label="Filter Status" />
          <Button v-if="can('stock-opname.store')" @click="openCreate" size="sm" class="flex items-center gap-2 bg-primary text-primary-foreground"><Plus class="h-4 w-4" />Buat</Button>
        </div>
      </div>
      <Card class="shadow-sm overflow-hidden"><CardContent class="p-0">
        <div v-if="loading" class="flex items-center justify-center py-24"><Loader2 class="h-7 w-7 animate-spin text-primary/50" /></div>
        <div v-else-if="filteredOpnames.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground"><PackageSearch class="h-10 w-10 mb-3 opacity-20" /><p class="text-sm">Belum ada stock opname.</p></div>
        <div v-else>
          <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
            <div v-for="o in paginatedOpnames" :key="o.id" class="p-4 flex flex-col gap-2" @click="openDetail(o)">
              <div class="flex justify-between"><span class="text-sm font-medium">{{ o.locationName }}</span><span :class="['text-[10px] font-semibold px-2 py-0.5 rounded-full border', statusColor(o.status)]">{{ statusLabel(o.status) }}</span></div>
              <div class="flex justify-between"><span class="text-xs text-muted-foreground">{{ o.items.length }} item</span><span class="text-xs text-muted-foreground">{{ formatDate(o.opnameDate) }}</span></div>
            </div>
          </div>
          <div class="hidden md:block overflow-x-auto">
            <table class="w-full text-sm"><thead><tr class="border-b border-zinc-100 dark:border-zinc-800">
              <th class="pl-5 py-3 text-left text-xs font-semibold uppercase text-zinc-500">Lokasi</th>
              <th class="py-3 text-left text-xs font-semibold uppercase text-zinc-500">Tipe</th>
              <th class="py-3 text-left text-xs font-semibold uppercase text-zinc-500">Tanggal</th>
              <th class="py-3 text-center text-xs font-semibold uppercase text-zinc-500">Item</th>
              <th class="py-3 text-center text-xs font-semibold uppercase text-zinc-500">Status</th>
              <th class="pr-5 py-3 text-right"></th>
            </tr></thead><tbody>
              <tr v-for="o in paginatedOpnames" :key="o.id" class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40">
                <td class="pl-5 py-3 font-medium">{{ o.locationName }}</td>
                <td class="py-3 text-xs capitalize">{{ o.locationType }}</td>
                <td class="py-3 text-xs text-muted-foreground">{{ formatDate(o.opnameDate) }}</td>
                <td class="py-3 text-center text-xs">{{ o.items.length }}</td>
                <td class="py-3 text-center"><span :class="['text-[10px] font-semibold px-2 py-0.5 rounded-full border', statusColor(o.status)]">{{ statusLabel(o.status) }}</span></td>
                <td class="pr-5 py-3 text-right"><Button variant="ghost" size="icon" class="h-7 w-7" @click="openDetail(o)"><Eye class="h-3.5 w-3.5" /></Button></td>
              </tr>
            </tbody></table>
          </div>
        </div>
        <DataTablePagination v-if="filteredOpnames.length > 0 && !loading" :page="page" :page-size="pageSize" :total="filteredOpnames.length" @update:page="page = $event" @update:page-size="pageSize = $event; page = 1" />
      </CardContent></Card>
    </div>

    <Teleport to="body">
      <Transition name="fade"><div v-if="showDrawer" class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm" @click="showDrawer = false" /></Transition>
      <Transition name="slide-right">
        <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[500px] bg-card shadow-2xl sm:border-l overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
            <h3 class="font-semibold text-base">{{ drawerMode === 'create' ? 'Buat Stock Opname' : 'Detail Opname' }}</h3>
            <Button variant="ghost" size="icon" @click="showDrawer = false"><X class="h-4 w-4" /></Button>
          </div>
          <template v-if="drawerMode === 'create'">
            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-4">
              <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>
              <div class="grid grid-cols-2 gap-3">
                <div class="space-y-1.5"><Label>Lokasi *</Label><Input v-model="form.locationName" placeholder="Gudang/Cabang" /></div>
                <div class="space-y-1.5"><Label>Tipe</Label>
                  <CustomSelect v-model="form.locationType" :options="locationTypeOptions" :show-icon="false" align="start" class="w-full h-10" />
                </div>
              </div>
              <div class="space-y-1.5"><Label>Tanggal Opname</Label><Input v-model="form.opnameDate" type="date" /></div>
              <div class="space-y-1.5"><Label>Catatan</Label><textarea v-model="form.notes" rows="3" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm" /></div>
              <p class="text-xs text-muted-foreground">Item akan ditambahkan setelah opname dibuat.</p>
            </div>
            <div class="flex justify-end gap-3 px-6 py-4 border-t bg-muted/30">
              <Button variant="outline" @click="showDrawer = false">Batal</Button>
              <Button @click="saveOpname" :disabled="saving"><Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />Simpan</Button>
            </div>
          </template>
          <template v-else-if="selectedOpname">
            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5">
              <div class="flex items-center justify-between">
                <span :class="['text-xs font-semibold px-2.5 py-1 rounded-full border', statusColor(selectedOpname.status)]">{{ statusLabel(selectedOpname.status) }}</span>
                <span class="text-xs text-muted-foreground">{{ formatDate(selectedOpname.opnameDate) }}</span>
              </div>
              <div class="space-y-1.5 text-sm">
                <div class="flex justify-between"><span class="text-muted-foreground">Lokasi</span><span class="font-medium">{{ selectedOpname.locationName }} ({{ selectedOpname.locationType }})</span></div>
                <div class="flex justify-between"><span class="text-muted-foreground">Dibuat oleh</span><span>{{ selectedOpname.createdBy }}</span></div>
                <div v-if="selectedOpname.notes" class="flex justify-between"><span class="text-muted-foreground">Catatan</span><span>{{ selectedOpname.notes }}</span></div>
              </div>
              <div v-if="selectedOpname.items.length > 0">
                <h4 class="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Hasil Counting</h4>
                <div class="space-y-2">
                  <div v-for="(item, i) in selectedOpname.items" :key="i" class="p-3 rounded-lg border border-border bg-muted/20">
                    <div class="flex justify-between items-start">
                      <p class="text-sm font-medium">{{ item.productName }}</p>
                      <span v-if="item.qtyDifference != null && item.qtyDifference !== 0" class="flex items-center gap-1 text-xs font-bold" :class="diffColor(item.qtyDifference)">
                        <AlertTriangle v-if="item.qtyDifference < 0" class="h-3 w-3" />
                        {{ item.qtyDifference > 0 ? '+' : '' }}{{ item.qtyDifference }}
                      </span>
                    </div>
                    <div class="flex gap-4 mt-1 text-xs text-muted-foreground">
                      <span>Sistem: <b>{{ item.qtySystem }}</b></span>
                      <span>Fisik: <b :class="diffColor(item.qtyDifference)">{{ item.qtyPhysical ?? '-' }}</b></span>
                    </div>
                    <p v-if="item.notes" class="text-[10px] text-muted-foreground mt-1 italic">{{ item.notes }}</p>
                  </div>
                </div>
              </div>
              <div v-else class="text-center py-8 text-muted-foreground"><PackageSearch class="h-8 w-8 mx-auto opacity-20 mb-2" /><p class="text-sm">Belum ada item.</p></div>
              <div v-if="selectedOpname.status === 'reviewed'" class="flex gap-2">
                <Button class="flex-1" @click="approveOpname(selectedOpname)"><Check class="h-4 w-4 mr-1" />Approve</Button>
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
