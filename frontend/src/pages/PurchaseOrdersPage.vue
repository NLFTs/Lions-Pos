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
import { Plus, Loader2, X, ClipboardList, Eye, Trash2 } from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()
const pos = ref([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('all')
const page = ref(1)
const pageSize = ref(10)
const showDrawer = ref(false)
const drawerMode = ref('detail') // 'create' | 'detail'
const saving = ref(false)
const formError = ref(null)
const selectedPO = ref(null)

const MOCK = [
  { id: 'po1', poNumber: 'PO-20260501-0001', supplierName: 'PT Textile Jaya', locationName: 'Gudang Utama', locationType: 'warehouse', status: 'received', orderDate: '2026-05-01', total: 1700000, createdAt: '2026-05-01T08:00:00Z',
    items: [{ productName: 'Kaos Polos Putih', qtyOrdered: 20, qtyReceived: 20, unitCost: 50000, subtotal: 1000000 }, { productName: 'Celana Chino Beige', qtyOrdered: 5, qtyReceived: 5, unitCost: 140000, subtotal: 700000 }] },
  { id: 'po2', poNumber: 'PO-20260502-0001', supplierName: 'CV Sepatu Makmur', locationName: 'Cabang Jakarta', locationType: 'branch', status: 'ordered', orderDate: '2026-05-02', total: 2250000, createdAt: '2026-05-02T10:00:00Z',
    items: [{ productName: 'Sepatu Sneakers Hitam', qtyOrdered: 10, qtyReceived: 0, unitCost: 225000, subtotal: 2250000 }] },
  { id: 'po3', poNumber: 'PO-20260503-0001', supplierName: 'UD Aksesoris Lengkap', locationName: 'Gudang Utama', locationType: 'warehouse', status: 'draft', orderDate: null, total: 490000, createdAt: '2026-05-03T14:00:00Z',
    items: [{ productName: 'Sabuk Kulit Coklat', qtyOrdered: 5, qtyReceived: 0, unitCost: 98000, subtotal: 490000 }] },
]

const emptyForm = () => ({ supplierName: '', locationName: '', locationType: 'warehouse', notes: '', items: [{ productName: '', qtyOrdered: 1, unitCost: 0 }] })
const form = ref(emptyForm())

const filteredPOs = computed(() => {
  let r = pos.value
  if (statusFilter.value !== 'all') r = r.filter(p => p.status === statusFilter.value)
  if (searchQuery.value) { const q = searchQuery.value.toLowerCase(); r = r.filter(p => p.poNumber.toLowerCase().includes(q) || p.supplierName?.toLowerCase().includes(q)) }
  return r
})
const paginatedPOs = computed(() => { const s = (page.value - 1) * pageSize.value; return filteredPOs.value.slice(s, s + pageSize.value) })

function fetchPOs() { loading.value = true; setTimeout(() => { pos.value = [...MOCK]; loading.value = false }, 300) }
function openCreate() { form.value = emptyForm(); drawerMode.value = 'create'; formError.value = null; showDrawer.value = true }
function openDetail(po) { selectedPO.value = po; drawerMode.value = 'detail'; showDrawer.value = true }

function addItem() { form.value.items.push({ productName: '', qtyOrdered: 1, unitCost: 0 }) }
function removeItem(i) { form.value.items.splice(i, 1) }
const formTotal = computed(() => form.value.items.reduce((s, i) => s + (i.qtyOrdered * i.unitCost), 0))

function savePO() {
  if (!form.value.supplierName) { formError.value = 'Supplier wajib diisi.'; return }
  saving.value = true
  setTimeout(() => {
    const num = `PO-${new Date().toISOString().slice(0,10).replace(/-/g,'')}-${String(pos.value.length + 1).padStart(4,'0')}`
    pos.value.unshift({ id: 'po-' + Date.now(), poNumber: num, ...form.value, status: 'draft', total: formTotal.value, createdAt: new Date().toISOString(),
      items: form.value.items.map(i => ({ ...i, qtyReceived: 0, subtotal: i.qtyOrdered * i.unitCost })) })
    toast.success('Purchase Order berhasil dibuat!'); showDrawer.value = false; saving.value = false
  }, 400)
}

function formatCurrency(v) { return v == null ? '-' : new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(v) }
function formatDate(dt) { return dt ? new Date(dt).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' }) : '-' }
function statusColor(s) {
  const map = { received: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-400', ordered: 'bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-400', partial: 'bg-amber-50 text-amber-700 dark:bg-amber-900/20 dark:text-amber-400', cancelled: 'bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-400' }
  return map[s] || 'bg-zinc-100 text-zinc-700 dark:bg-zinc-800 dark:text-zinc-400'
}
function statusLabel(s) { const m = { draft: 'Draft', ordered: 'Dipesan', partial: 'Sebagian', received: 'Diterima', cancelled: 'Batal' }; return m[s] || s }

onMounted(fetchPOs)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div><h1 class="text-2xl font-bold tracking-tight">Purchase Order</h1><p class="text-muted-foreground text-sm mt-1">Kelola pesanan pembelian barang.</p></div>
        <div class="flex items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-72"><DataTableSearch v-model="searchQuery" placeholder="Cari PO..." /></div>
          <select v-model="statusFilter" class="h-9 rounded-md border border-border bg-background px-3 text-sm">
            <option value="all">Semua</option><option value="draft">Draft</option><option value="ordered">Dipesan</option><option value="received">Diterima</option><option value="cancelled">Batal</option>
          </select>
          <Button v-if="can('purchase-order.store')" @click="openCreate" size="sm" class="flex items-center gap-2 bg-primary text-primary-foreground"><Plus class="h-4 w-4" /><span>Buat PO</span></Button>
        </div>
      </div>
      <Card class="shadow-sm overflow-hidden"><CardContent class="p-0">
        <div v-if="loading" class="flex items-center justify-center py-24"><Loader2 class="h-7 w-7 animate-spin text-primary/50" /></div>
        <div v-else-if="filteredPOs.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground"><ClipboardList class="h-10 w-10 mb-3 opacity-20" /><p class="text-sm">Belum ada PO.</p></div>
        <div v-else>
          <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
            <div v-for="po in paginatedPOs" :key="po.id" class="p-4 flex flex-col gap-2" @click="openDetail(po)">
              <div class="flex justify-between"><span class="font-mono text-xs font-semibold">{{ po.poNumber }}</span><span :class="['text-[10px] font-semibold px-2 py-0.5 rounded-full border', statusColor(po.status)]">{{ statusLabel(po.status) }}</span></div>
              <div class="flex justify-between"><span class="text-xs text-muted-foreground">{{ po.supplierName }}</span><span class="text-sm font-bold">{{ formatCurrency(po.total) }}</span></div>
            </div>
          </div>
          <div class="hidden md:block overflow-x-auto">
            <table class="w-full text-sm"><thead><tr class="border-b border-zinc-100 dark:border-zinc-800">
              <th class="pl-5 py-3 text-left text-xs font-semibold uppercase text-zinc-500">No. PO</th>
              <th class="py-3 text-left text-xs font-semibold uppercase text-zinc-500">Supplier</th>
              <th class="py-3 text-left text-xs font-semibold uppercase text-zinc-500">Tujuan</th>
              <th class="py-3 text-left text-xs font-semibold uppercase text-zinc-500">Total</th>
              <th class="py-3 text-center text-xs font-semibold uppercase text-zinc-500">Status</th>
              <th class="py-3 text-left text-xs font-semibold uppercase text-zinc-500">Tanggal</th>
              <th class="pr-5 py-3 text-right"></th>
            </tr></thead><tbody>
              <tr v-for="po in paginatedPOs" :key="po.id" class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40">
                <td class="pl-5 py-3 font-mono text-xs font-semibold">{{ po.poNumber }}</td>
                <td class="py-3 text-xs">{{ po.supplierName }}</td>
                <td class="py-3 text-xs text-muted-foreground">{{ po.locationName }}</td>
                <td class="py-3 text-sm font-semibold">{{ formatCurrency(po.total) }}</td>
                <td class="py-3 text-center"><span :class="['text-[10px] font-semibold px-2 py-0.5 rounded-full border', statusColor(po.status)]">{{ statusLabel(po.status) }}</span></td>
                <td class="py-3 text-xs text-muted-foreground">{{ formatDate(po.createdAt) }}</td>
                <td class="pr-5 py-3 text-right"><Button variant="ghost" size="icon" class="h-7 w-7" @click="openDetail(po)"><Eye class="h-3.5 w-3.5" /></Button></td>
              </tr>
            </tbody></table>
          </div>
        </div>
        <DataTablePagination v-if="filteredPOs.length > 0 && !loading" :page="page" :page-size="pageSize" :total="filteredPOs.length" @update:page="page = $event" @update:page-size="pageSize = $event; page = 1" />
      </CardContent></Card>
    </div>

    <Teleport to="body">
      <Transition name="fade"><div v-if="showDrawer" class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm" @click="showDrawer = false" /></Transition>
      <Transition name="slide-right">
        <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[500px] bg-card shadow-2xl sm:border-l overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
            <h3 class="font-semibold text-base">{{ drawerMode === 'create' ? 'Buat Purchase Order' : 'Detail PO' }}</h3>
            <Button variant="ghost" size="icon" @click="showDrawer = false"><X class="h-4 w-4" /></Button>
          </div>
          <!-- CREATE -->
          <template v-if="drawerMode === 'create'">
            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-4">
              <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>
              <div class="space-y-1.5"><Label>Supplier *</Label><Input v-model="form.supplierName" placeholder="Nama supplier" /></div>
              <div class="grid grid-cols-2 gap-3">
                <div class="space-y-1.5"><Label>Tujuan Lokasi</Label><Input v-model="form.locationName" placeholder="Gudang/Cabang" /></div>
                <div class="space-y-1.5"><Label>Tipe</Label><select v-model="form.locationType" class="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"><option value="warehouse">Warehouse</option><option value="branch">Branch</option></select></div>
              </div>
              <div>
                <div class="flex items-center justify-between mb-2"><Label>Item Pesanan</Label><Button variant="outline" size="sm" @click="addItem"><Plus class="h-3 w-3 mr-1" />Item</Button></div>
                <div class="space-y-2">
                  <div v-for="(item, i) in form.items" :key="i" class="flex gap-2 items-end p-3 rounded-lg border border-border bg-muted/20">
                    <div class="flex-1 space-y-1"><Label class="text-[10px]">Produk</Label><Input v-model="item.productName" placeholder="Nama produk" class="h-8 text-xs" /></div>
                    <div class="w-16 space-y-1"><Label class="text-[10px]">Qty</Label><Input v-model.number="item.qtyOrdered" type="number" min="1" class="h-8 text-xs" /></div>
                    <div class="w-24 space-y-1"><Label class="text-[10px]">Harga</Label><Input v-model.number="item.unitCost" type="number" min="0" class="h-8 text-xs" /></div>
                    <Button v-if="form.items.length > 1" variant="ghost" size="icon" class="h-8 w-8 text-destructive shrink-0" @click="removeItem(i)"><Trash2 class="h-3 w-3" /></Button>
                  </div>
                </div>
                <div class="mt-3 text-right text-sm font-bold">Total: {{ formatCurrency(formTotal) }}</div>
              </div>
            </div>
            <div class="flex justify-end gap-3 px-6 py-4 border-t bg-muted/30">
              <Button variant="outline" @click="showDrawer = false">Batal</Button>
              <Button @click="savePO" :disabled="saving"><Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />Simpan PO</Button>
            </div>
          </template>
          <!-- DETAIL -->
          <template v-else-if="selectedPO">
            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5">
              <div class="flex items-center justify-between">
                <span class="font-mono text-sm font-bold">{{ selectedPO.poNumber }}</span>
                <span :class="['text-xs font-semibold px-2.5 py-1 rounded-full border', statusColor(selectedPO.status)]">{{ statusLabel(selectedPO.status) }}</span>
              </div>
              <div class="space-y-1.5 text-sm">
                <div class="flex justify-between"><span class="text-muted-foreground">Supplier</span><span class="font-medium">{{ selectedPO.supplierName }}</span></div>
                <div class="flex justify-between"><span class="text-muted-foreground">Tujuan</span><span>{{ selectedPO.locationName }} ({{ selectedPO.locationType }})</span></div>
                <div class="flex justify-between"><span class="text-muted-foreground">Tanggal</span><span>{{ formatDate(selectedPO.createdAt) }}</span></div>
              </div>
              <div><h4 class="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Item</h4>
                <div class="space-y-2">
                  <div v-for="(item, i) in selectedPO.items" :key="i" class="flex justify-between p-3 rounded-lg border border-border bg-muted/20">
                    <div><p class="text-sm font-medium">{{ item.productName }}</p><p class="text-xs text-muted-foreground">{{ item.qtyOrdered }} pcs × {{ formatCurrency(item.unitCost) }} | Diterima: {{ item.qtyReceived }}</p></div>
                    <span class="text-sm font-semibold">{{ formatCurrency(item.subtotal) }}</span>
                  </div>
                </div>
              </div>
              <div class="border-t pt-3 flex justify-between text-base font-bold"><span>Total</span><span class="text-primary">{{ formatCurrency(selectedPO.total) }}</span></div>
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
