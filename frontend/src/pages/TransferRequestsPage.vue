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
import { Plus, Loader2, X, Repeat2, Eye, Check, Truck } from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()
const transfers = ref([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('all')
const page = ref(1)
const pageSize = ref(10)
const showDrawer = ref(false)
const drawerMode = ref('detail')
const saving = ref(false)
const formError = ref(null)
const selectedTR = ref(null)

const MOCK = [
  { id: 'tr1', fromName: 'Gudang Utama', fromType: 'warehouse', toName: 'Cabang Jakarta', toType: 'branch', status: 'received', notes: 'Restok bulanan', createdAt: '2026-05-01T09:00:00Z', requestedBy: 'Admin',
    items: [{ productName: 'Kaos Polos Putih', qtyRequested: 10, qtyReceived: 10 }, { productName: 'Celana Chino Beige', qtyRequested: 5, qtyReceived: 4 }] },
  { id: 'tr2', fromName: 'Gudang Utama', fromType: 'warehouse', toName: 'Cabang Bandung', toType: 'branch', status: 'approved', notes: '', createdAt: '2026-05-03T11:00:00Z', requestedBy: 'Admin',
    items: [{ productName: 'Sepatu Sneakers Hitam', qtyRequested: 8, qtyReceived: null }] },
  { id: 'tr3', fromName: 'Cabang Jakarta', fromType: 'branch', toName: 'Gudang Utama', toType: 'warehouse', status: 'pending', notes: 'Retur stok berlebih', createdAt: '2026-05-05T14:00:00Z', requestedBy: 'Admin',
    items: [{ productName: 'Sabuk Kulit Coklat', qtyRequested: 3, qtyReceived: null }] },
]

const emptyForm = () => ({ fromName: '', fromType: 'warehouse', toName: '', toType: 'branch', notes: '', items: [{ productName: '', qtyRequested: 1 }] })
const form = ref(emptyForm())

const filteredTRs = computed(() => {
  let r = transfers.value
  if (statusFilter.value !== 'all') r = r.filter(t => t.status === statusFilter.value)
  if (searchQuery.value) { const q = searchQuery.value.toLowerCase(); r = r.filter(t => t.fromName.toLowerCase().includes(q) || t.toName.toLowerCase().includes(q)) }
  return r
})
const paginatedTRs = computed(() => { const s = (page.value - 1) * pageSize.value; return filteredTRs.value.slice(s, s + pageSize.value) })

function fetchTRs() { loading.value = true; setTimeout(() => { transfers.value = [...MOCK]; loading.value = false }, 300) }
function openCreate() { form.value = emptyForm(); drawerMode.value = 'create'; formError.value = null; showDrawer.value = true }
function openDetail(tr) { selectedTR.value = tr; drawerMode.value = 'detail'; showDrawer.value = true }
function addItem() { form.value.items.push({ productName: '', qtyRequested: 1 }) }
function removeItem(i) { form.value.items.splice(i, 1) }

function saveTR() {
  if (!form.value.fromName || !form.value.toName) { formError.value = 'Asal dan tujuan wajib diisi.'; return }
  saving.value = true
  setTimeout(() => {
    transfers.value.unshift({ id: 'tr-' + Date.now(), ...form.value, status: 'pending', requestedBy: 'Admin', createdAt: new Date().toISOString(),
      items: form.value.items.map(i => ({ ...i, qtyReceived: null })) })
    toast.success('Transfer request berhasil dibuat!'); showDrawer.value = false; saving.value = false
  }, 400)
}

function approveTransfer(tr) { tr.status = 'approved'; toast.success('Transfer disetujui!') }
function receiveTransfer(tr) { tr.status = 'received'; tr.items.forEach(i => { if (i.qtyReceived == null) i.qtyReceived = i.qtyRequested }); toast.success('Barang diterima!') }

function formatDate(dt) { return dt ? new Date(dt).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' }) : '-' }
function statusColor(s) {
  const m = { received: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-400', approved: 'bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-400', in_transit: 'bg-violet-50 text-violet-700 dark:bg-violet-900/20 dark:text-violet-400', cancelled: 'bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-400' }
  return m[s] || 'bg-amber-50 text-amber-700 dark:bg-amber-900/20 dark:text-amber-400'
}
function statusLabel(s) { const m = { pending: 'Pending', approved: 'Disetujui', in_transit: 'Dalam Kirim', received: 'Diterima', cancelled: 'Batal' }; return m[s] || s }

onMounted(fetchTRs)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div><h1 class="text-2xl font-bold tracking-tight">Transfer Stok</h1><p class="text-muted-foreground text-sm mt-1">Kelola permintaan transfer antar lokasi.</p></div>
        <div class="flex items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-72"><DataTableSearch v-model="searchQuery" placeholder="Cari transfer..." /></div>
          <select v-model="statusFilter" class="h-9 rounded-md border border-border bg-background px-3 text-sm">
            <option value="all">Semua</option><option value="pending">Pending</option><option value="approved">Disetujui</option><option value="received">Diterima</option>
          </select>
          <Button v-if="can('transfer-request.store')" @click="openCreate" size="sm" class="flex items-center gap-2 bg-primary text-primary-foreground"><Plus class="h-4 w-4" />Buat</Button>
        </div>
      </div>
      <Card class="shadow-sm overflow-hidden"><CardContent class="p-0">
        <div v-if="loading" class="flex items-center justify-center py-24"><Loader2 class="h-7 w-7 animate-spin text-primary/50" /></div>
        <div v-else-if="filteredTRs.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground"><Repeat2 class="h-10 w-10 mb-3 opacity-20" /><p class="text-sm">Belum ada transfer request.</p></div>
        <div v-else>
          <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
            <div v-for="tr in paginatedTRs" :key="tr.id" class="p-4 flex flex-col gap-2" @click="openDetail(tr)">
              <div class="flex justify-between"><span class="text-xs font-medium">{{ tr.fromName }} → {{ tr.toName }}</span><span :class="['text-[10px] font-semibold px-2 py-0.5 rounded-full border', statusColor(tr.status)]">{{ statusLabel(tr.status) }}</span></div>
              <div class="flex justify-between"><span class="text-xs text-muted-foreground">{{ tr.items.length }} item</span><span class="text-xs text-muted-foreground">{{ formatDate(tr.createdAt) }}</span></div>
            </div>
          </div>
          <div class="hidden md:block overflow-x-auto">
            <table class="w-full text-sm"><thead><tr class="border-b border-zinc-100 dark:border-zinc-800">
              <th class="pl-5 py-3 text-left text-xs font-semibold uppercase text-zinc-500">Asal</th>
              <th class="py-3 text-left text-xs font-semibold uppercase text-zinc-500">Tujuan</th>
              <th class="py-3 text-center text-xs font-semibold uppercase text-zinc-500">Item</th>
              <th class="py-3 text-center text-xs font-semibold uppercase text-zinc-500">Status</th>
              <th class="py-3 text-left text-xs font-semibold uppercase text-zinc-500">Tanggal</th>
              <th class="pr-5 py-3 text-right"></th>
            </tr></thead><tbody>
              <tr v-for="tr in paginatedTRs" :key="tr.id" class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40">
                <td class="pl-5 py-3 text-xs font-medium">{{ tr.fromName }}</td>
                <td class="py-3 text-xs">{{ tr.toName }}</td>
                <td class="py-3 text-center text-xs">{{ tr.items.length }}</td>
                <td class="py-3 text-center"><span :class="['text-[10px] font-semibold px-2 py-0.5 rounded-full border', statusColor(tr.status)]">{{ statusLabel(tr.status) }}</span></td>
                <td class="py-3 text-xs text-muted-foreground">{{ formatDate(tr.createdAt) }}</td>
                <td class="pr-5 py-3 text-right"><Button variant="ghost" size="icon" class="h-7 w-7" @click="openDetail(tr)"><Eye class="h-3.5 w-3.5" /></Button></td>
              </tr>
            </tbody></table>
          </div>
        </div>
        <DataTablePagination v-if="filteredTRs.length > 0 && !loading" :page="page" :page-size="pageSize" :total="filteredTRs.length" @update:page="page = $event" @update:page-size="pageSize = $event; page = 1" />
      </CardContent></Card>
    </div>

    <Teleport to="body">
      <Transition name="fade"><div v-if="showDrawer" class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm" @click="showDrawer = false" /></Transition>
      <Transition name="slide-right">
        <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[460px] bg-card shadow-2xl sm:border-l overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
            <h3 class="font-semibold text-base">{{ drawerMode === 'create' ? 'Buat Transfer' : 'Detail Transfer' }}</h3>
            <Button variant="ghost" size="icon" @click="showDrawer = false"><X class="h-4 w-4" /></Button>
          </div>
          <template v-if="drawerMode === 'create'">
            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-4">
              <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>
              <div class="grid grid-cols-2 gap-3">
                <div class="space-y-1.5"><Label>Asal *</Label><Input v-model="form.fromName" placeholder="Gudang/Cabang" /></div>
                <div class="space-y-1.5"><Label>Tipe</Label><select v-model="form.fromType" class="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"><option value="warehouse">Warehouse</option><option value="branch">Branch</option></select></div>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div class="space-y-1.5"><Label>Tujuan *</Label><Input v-model="form.toName" placeholder="Gudang/Cabang" /></div>
                <div class="space-y-1.5"><Label>Tipe</Label><select v-model="form.toType" class="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"><option value="warehouse">Warehouse</option><option value="branch">Branch</option></select></div>
              </div>
              <div class="space-y-1.5"><Label>Catatan</Label><textarea v-model="form.notes" rows="2" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm" /></div>
              <div>
                <div class="flex items-center justify-between mb-2"><Label>Item</Label><Button variant="outline" size="sm" @click="addItem"><Plus class="h-3 w-3 mr-1" />Item</Button></div>
                <div class="space-y-2">
                  <div v-for="(item, i) in form.items" :key="i" class="flex gap-2 items-end p-3 rounded-lg border border-border bg-muted/20">
                    <div class="flex-1 space-y-1"><Label class="text-[10px]">Produk</Label><Input v-model="item.productName" placeholder="Nama produk" class="h-8 text-xs" /></div>
                    <div class="w-20 space-y-1"><Label class="text-[10px]">Qty</Label><Input v-model.number="item.qtyRequested" type="number" min="1" class="h-8 text-xs" /></div>
                    <Button v-if="form.items.length > 1" variant="ghost" size="icon" class="h-8 w-8 text-destructive shrink-0" @click="removeItem(i)"><X class="h-3 w-3" /></Button>
                  </div>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-3 px-6 py-4 border-t bg-muted/30">
              <Button variant="outline" @click="showDrawer = false">Batal</Button>
              <Button @click="saveTR" :disabled="saving"><Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />Simpan</Button>
            </div>
          </template>
          <template v-else-if="selectedTR">
            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5">
              <div class="flex items-center justify-between">
                <span :class="['text-xs font-semibold px-2.5 py-1 rounded-full border', statusColor(selectedTR.status)]">{{ statusLabel(selectedTR.status) }}</span>
                <span class="text-xs text-muted-foreground">{{ formatDate(selectedTR.createdAt) }}</span>
              </div>
              <div class="space-y-1.5 text-sm">
                <div class="flex justify-between"><span class="text-muted-foreground">Dari</span><span class="font-medium">{{ selectedTR.fromName }} ({{ selectedTR.fromType }})</span></div>
                <div class="flex justify-between"><span class="text-muted-foreground">Ke</span><span class="font-medium">{{ selectedTR.toName }} ({{ selectedTR.toType }})</span></div>
                <div v-if="selectedTR.notes" class="flex justify-between"><span class="text-muted-foreground">Catatan</span><span>{{ selectedTR.notes }}</span></div>
              </div>
              <div><h4 class="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Item</h4>
                <div class="space-y-2">
                  <div v-for="(item, i) in selectedTR.items" :key="i" class="flex justify-between p-3 rounded-lg border border-border bg-muted/20">
                    <div><p class="text-sm font-medium">{{ item.productName }}</p><p class="text-xs text-muted-foreground">Diminta: {{ item.qtyRequested }} | Diterima: {{ item.qtyReceived ?? '-' }}</p></div>
                  </div>
                </div>
              </div>
              <div v-if="selectedTR.status === 'pending'" class="flex gap-2">
                <Button class="flex-1" @click="approveTransfer(selectedTR)"><Check class="h-4 w-4 mr-1" />Approve</Button>
              </div>
              <div v-if="selectedTR.status === 'approved'" class="flex gap-2">
                <Button class="flex-1" @click="receiveTransfer(selectedTR)"><Truck class="h-4 w-4 mr-1" />Terima Barang</Button>
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
