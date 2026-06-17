<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'
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
  Package,
  Warehouse,
  Building2,
  CheckCircle2,
  Send,
  Copy,
  ClipboardCheck
} from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()
const authStore = useAuthStore()

const canReceive = computed(() => !authStore.isAdmin && authStore.user?.roles?.length > 0)
const isSuperAdmin = computed(() => authStore.isSuperAdmin)

// ─── State ───────────────────────────────────────────────────────────────────
const pos = ref([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('all')
const page = ref(1)
const pageSize = ref(10)

const statusOptions = [
  { value: 'all', label: 'Semua Status', class: 'my-0.5 text-zinc-900 dark:text-zinc-100 bg-zinc-100/70 dark:bg-zinc-800/40 border border-zinc-200 dark:border-zinc-700 focus:bg-zinc-200/60 dark:focus:bg-zinc-800/80' },
  { value: 'draft', label: 'Draf', class: 'my-0.5 text-zinc-900 dark:text-zinc-100 bg-orange-100/70 dark:bg-orange-950/30 border border-orange-200 dark:border-orange-900/40 focus:bg-orange-200/50 dark:focus:bg-orange-950/55' },
  { value: 'ordered', label: 'Dipesan', class: 'my-0.5 text-zinc-900 dark:text-zinc-100 bg-emerald-100/70 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-900/40 focus:bg-emerald-200/50 dark:focus:bg-emerald-950/55' },
  { value: 'received', label: 'Diterima', class: 'my-0.5 text-zinc-900 dark:text-zinc-100 bg-teal-100/70 dark:bg-teal-950/30 border border-teal-200 dark:border-teal-900/40 focus:bg-teal-200/50 dark:focus:bg-teal-950/55' },
  { value: 'cancelled', label: 'Batal', class: 'my-0.5 text-zinc-900 dark:text-zinc-100 bg-red-100/70 dark:bg-red-950/30 border border-red-200 dark:border-red-900/40 focus:bg-red-200/50 dark:focus:bg-red-950/55' }
]

const locationTypeOptions = [
  { value: 'warehouse', label: 'Gudang' },
  { value: 'branch', label: 'Cabang' }
]

// ─── Form Data for Selects ───────────────────────────────────────────────────
const suppliers = ref([])
const locations = ref([])
const products = ref([])

// ─── Computed ─────────────────────────────────────────────────────────────────
const filteredPOs = computed(() => {
  let r = pos.value
  if (statusFilter.value !== 'all') {
    r = r.filter(p => p.status?.toLowerCase() === statusFilter.value.toLowerCase())
  }
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

// ─── Receipt Drawer State ─────────────────────────────────────────────────────
const showReceiptDrawer = ref(false)
const receiptSaving = ref(false)
const receiptError = ref(null)
const receiptForm = ref({
  purchaseOrderId: null,
  receivedDate: new Date().toISOString().slice(0, 16),
  notes: '',
  items: []
})

async function openReceiptDrawer(po) {
  receiptError.value = null
  
  // Ambil data items dari cache objek atau tembak langsung ke API jika belum ada
  let poItems = po.items
  
  if (!poItems || poItems.length === 0) {
    try {
      // Kita tarik langsung datanya via API berdasarkan ID Purchase Order-nya
      const res = await api.get(`/api/v1/purchase-orders/${po.id}/items`)
      poItems = res.data || []
      
      // Cadangkan juga ke objek po-nya biar datanya sinkron
      po.items = poItems
    } catch (err) {
      console.error('Gagal mengambil item untuk penerimaan barang:', err)
      toast.error('Gagal memuat daftar produk dari server.')
      return // Stop eksekusi kalau API bermasalah
    }
  }

  // Setelah dipastikan poItems terisi penuh, baru kita set ke receiptForm
  receiptForm.value = {
    purchaseOrderId: po.id,
    receivedDate: new Date().toISOString().slice(0, 16),
    notes: '',
    items: poItems.map(item => ({
      purchaseOrderItemId: item.id,
      productId: item.product?.id || item.productId,
      productName: item.product?.name || `Produk #${item.productId}`,
      qtyOrdered: item.qtyOrdered,
      qtyReceived: item.qtyOrdered, // default: terima semua
      unitCost: item.unitCost,
      notes: ''
    }))
  }
  
  showReceiptDrawer.value = true
} 

async function submitReceipt() {
  receiptError.value = null
  if (receiptForm.value.items.some(i => !i.qtyReceived || i.qtyReceived <= 0)) {
    receiptError.value = 'Semua item harus memiliki jumlah yang valid.'
    return
  }
  receiptSaving.value = true
  try {
    await api.post('/api/v1/purchase-receipts', {
      purchaseOrderId: receiptForm.value.purchaseOrderId,
      receivedDate: receiptForm.value.receivedDate,
      notes: receiptForm.value.notes || null,
      items: receiptForm.value.items.map(i => ({
        purchaseOrderItemId: i.purchaseOrderItemId,
        productId: i.productId,
        qtyReceived: Number(i.qtyReceived),
        notes: i.notes || null
      }))
    })
    toast.success('Barang berhasil diterima! Stok telah diperbarui.')
    showReceiptDrawer.value = false
    showDrawer.value = false
    fetchData()
  } catch (err) {
    receiptError.value = err.response?.data?.message || err.response?.data?.data?.message || 'Gagal memproses penerimaan barang.'
  } finally {
    receiptSaving.value = false
  }
}
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
const locationsLoaded = ref(false)
const updatingPoId = ref(null)

async function loadLocations() {
  if (locationsLoaded.value) return
  try {
    const [resB, resW] = await Promise.all([
      api.get('/api/v1/branches'),
      api.get('/api/v1/warehouses')
    ])
    const brData = resB.data?.data || []
    const branches = (Array.isArray(brData) ? brData : (brData.content || [])).map(x => ({ ...x, type: 'branch' }))
    
    const whRaw = resW.data?.data
    const whArr = whRaw && !Array.isArray(whRaw) && whRaw.content ? whRaw.content : (Array.isArray(whRaw) ? whRaw : [])
    const warehouses = whArr.map(x => ({ ...x, type: 'warehouse' }))
    locations.value = [...branches, ...warehouses]
    locationsLoaded.value = true
  } catch (err) {
    console.error('Gagal memuat lokasi:', err)
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/purchase-orders')
    pos.value = Array.isArray(res.data) ? res.data : (res.data?.data || [])
  } catch (err) {
    toast.error('Gagal memuat data Purchase Order.')
  } finally {
    loading.value = false
  }
}

async function loadFormOptions() {
  try {
    const [resS, resP] = await Promise.all([
      api.get('/api/v1/suppliers'),
      api.get('/api/v1/products')
    ])
    suppliers.value = Array.isArray(resS.data) ? resS.data : (resS.data?.data || [])
    
    const pData = resP.data?.data
    products.value = pData && pData.content ? pData.content : (Array.isArray(pData) ? pData : [])
    
    await loadLocations()
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
  try {
    const res = await api.get(`/api/v1/purchase-orders/${po.id}/items`)
    selectedPO.value.items = res.data
  } catch (err) {
    console.error('Gagal memuat item Purchase Order:', err)
  }
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
  if (!s) return 'bg-zinc-100 text-zinc-700 border-zinc-200 dark:bg-zinc-800 dark:text-zinc-400 dark:border-zinc-700'
  const key = s.toLowerCase()
  const map = {
    draft: 'bg-orange-50 text-orange-700 border-orange-200 dark:bg-orange-900/20 dark:text-orange-400 dark:border-orange-800/50',
    ordered: 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-900/20 dark:text-emerald-400 dark:border-emerald-800/50',
    received: 'bg-teal-50 text-teal-700 border-teal-200 dark:bg-teal-900/20 dark:text-teal-400 dark:border-teal-800/50',
    partial: 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-900/20 dark:text-blue-400 dark:border-blue-800/50',
    cancelled: 'bg-red-50 text-red-700 border-red-200 dark:bg-red-900/20 dark:text-red-400 dark:border-red-800/50'
  }
  return map[key] || 'bg-zinc-100 text-zinc-700 border-zinc-200 dark:bg-zinc-800 dark:text-zinc-400 dark:border-zinc-700'
}

function statusLabel(s) {
  if (!s) return '-'
  const m = { draft: 'Draf', ordered: 'Dipesan', partial: 'Sebagian', received: 'Diterima', cancelled: 'Batal' }
  return m[s.toLowerCase()] || s
}

function statusDotClass(s) {
  if (!s) return 'bg-zinc-400 dark:bg-zinc-500 shadow-[0_0_8px_#a1a1aa]'
  const key = s.toLowerCase()
  const map = {
    draft: 'bg-orange-500 shadow-[0_0_8px_#f97316] dark:shadow-[0_0_8px_#ea580c]',
    ordered: 'bg-emerald-500 shadow-[0_0_8px_#10b981] dark:shadow-[0_0_8px_#059669]',
    received: 'bg-teal-500 shadow-[0_0_8px_#14b8a6] dark:shadow-[0_0_8px_#0d9488]',
    partial: 'bg-blue-500 shadow-[0_0_8px_#3b82f6] dark:shadow-[0_0_8px_#2563eb]',
    cancelled: 'bg-red-500 shadow-[0_0_8px_#ef4444] dark:shadow-[0_0_8px_#dc2626]'
  }
  return map[key] || 'bg-zinc-400 dark:bg-zinc-500 shadow-[0_0_8px_#a1a1aa]'
}

function getLocationName(type, id) {
  if (!type || id == null) return '-'
  const loc = locations.value.find(l => l.type?.toLowerCase() === type.toLowerCase() && String(l.id) === String(id))
  if (loc) return loc.name
  const typeLabel = type.toLowerCase() === 'warehouse' ? 'Gudang' : 'Cabang'
  return `${typeLabel} #${id}`
}

// ─── Copy Helpers ─────────────────────────────────────────────────────────────
const copiedCode = ref(false)
const copiedAll = ref(false)

function copyPOCode() {
  if (!selectedPO.value?.poNumber) return
  navigator.clipboard.writeText(selectedPO.value.poNumber).then(() => {
    copiedCode.value = true
    setTimeout(() => { copiedCode.value = false }, 2000)
  })
}

function copyAllInfo() {
  if (!selectedPO.value) return
  const po = selectedPO.value
  const lines = [
    `Purchase Order: ${po.poNumber}`,
    `Status: ${statusLabel(po.status)}`,
    ``,
    `Supplier: ${po.supplier?.name || '-'}`,
    `Alamat Supplier: ${po.supplier?.address || '-'}`,
    `Telepon: ${po.supplier?.phone || '-'}`,
    ``,
    `Tujuan: ${getLocationName(po.locationType, po.locationId)} (${po.locationType})`,
    `Tanggal Order: ${formatDate(po.orderDate || po.createdAt)}`,
    `Estimasi Tiba: ${formatDate(po.expectedDate)}`,
    ``,
    `Item Pesanan:`,
    ...(po.items || []).map((item, i) => 
      `  ${i + 1}. ${item.product?.name || `Produk #${item.productId}`} — ${item.qtyOrdered} pcs × ${formatCurrency(item.unitCost)} = ${formatCurrency(item.qtyOrdered * item.unitCost)}`
    ),
    ``,
    `Total: ${formatCurrency(po.total)}`,
    ...(po.notes ? [``, `Catatan: ${po.notes}`] : [])
  ]
  navigator.clipboard.writeText(lines.join('\n')).then(() => {
    copiedAll.value = true
    setTimeout(() => { copiedAll.value = false }, 2000)
  })
}

// ─── Status Update ──────────────────────────────────────────────────────────────────
const updatingStatus = ref(false)

async function updatePOStatus(id, newStatus) {
  updatingPoId.value = id
  updatingStatus.value = true
  try {
    if (newStatus?.toLowerCase() === 'received') {
      const itemsRes = await api.get(`/api/v1/purchase-orders/${id}/items`)
      const poItems = itemsRes.data || []
      
      if (poItems.length === 0) {
        throw new Error('Purchase Order tidak memiliki item.')
      }

      const payload = {
        purchaseOrderId: id,
        receivedDate: new Date().toISOString(),
        notes: 'Penerimaan otomatis dari sistem',
        items: poItems.map(item => ({
          purchaseOrderItemId: item.id,
          productId: item.product?.id || item.productId,
          qtyReceived: item.qtyOrdered - (item.qtyReceived || 0)
        })).filter(item => item.qtyReceived > 0)
      }

      if (payload.items.length === 0) {
        throw new Error('Semua item dalam Purchase Order ini sudah diterima.')
      }

      await api.post('/api/v1/purchase-receipts', payload)
      toast.success('Barang berhasil diterima dan stok telah diperbarui!')
    } else {
      await api.patch(`/api/v1/purchase-orders/${id}/status?status=${newStatus}`)
      toast.success(`Status PO berhasil diubah ke ${statusLabel(newStatus)}!`)
    }
    showDrawer.value = false
    fetchData()
  } catch (err) {
    console.error('Gagal memproses status PO:', err)
    toast.error(err.response?.data?.message || err.message || 'Gagal memproses status PO.')
  } finally {
    updatingStatus.value = false
    updatingPoId.value = null
  }
}

onMounted(async () => {
  fetchData()
  await loadLocations()
})
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Pembelian</h1>
          <p class="text-muted-foreground text-sm mt-1">Kelola pesanan pembelian barang ke distributor.</p>
        </div>
        <div class="flex items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-72">
            <DataTableSearch v-model="searchQuery" placeholder="Cari No. Pembelian atau Distributor..." />
          </div>
          <CustomSelect v-model="statusFilter" :options="statusOptions" class="w-full sm:w-44" />
          <Button v-if="can('purchase_order.store') && !isSuperAdmin" @click="openCreate" size="sm" class="flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
            <Plus class="h-4 w-4" />
            <span>Buat Pembelian</span>
          </Button>
        </div>
      </div>

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
            <Button v-if="can('purchase_order.store') && !isSuperAdmin && !searchQuery" size="sm" class="mt-4" @click="openCreate">
              <Plus class="h-3.5 w-3.5 mr-1.5" />
              Buat PO Pertama
            </Button>
          </div>

          <div v-else>
            <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
              <div v-for="po in paginatedPOs" :key="po.id" class="p-4 flex flex-col gap-3 hover:bg-zinc-50 dark:hover:bg-zinc-900/40 cursor-pointer transition-colors" @click="openDetail(po)">
                <div class="flex justify-between items-center">
                  <span class="font-mono text-[11px] font-bold text-primary bg-primary/5 px-1.5 py-0.5 rounded border border-primary/10">{{ po.poNumber }}</span>
                  <div class="inline-flex items-center gap-1.5 text-xs font-semibold text-zinc-700 dark:text-zinc-300">
                    <span :class="['w-1.5 h-1.5 rounded-full animate-pulse shrink-0', statusDotClass(po.status)]"></span>
                    <span>{{ statusLabel(po.status) }}</span>
                  </div>
                </div>
                <div class="flex justify-between items-start mt-0.5">
                  <div>
                    <div class="text-sm font-semibold text-zinc-900 dark:text-zinc-100">{{ po.supplier?.name }}</div>
                    <div class="text-[10px] text-zinc-400 italic mb-1">{{ po.supplier?.phone || '' }}</div>
                    <div class="text-[10px] text-muted-foreground flex items-center gap-1.5 mt-1">
                      <Warehouse class="h-3 w-3 opacity-60" />
                      <span>{{ getLocationName(po.locationType, po.locationId) }}</span>
                    </div>
                    <div class="text-[10px] text-muted-foreground flex flex-col gap-0.5 mt-1.5">
                      <div class="flex items-center gap-1">
                        <Calendar class="h-3 w-3 opacity-60" /> <span class="font-medium text-zinc-600 dark:text-zinc-400">Pesan:</span> {{ formatDate(po.orderDate || po.createdAt) }}
                      </div>
                      <div class="flex items-center gap-1">
                        <Truck class="h-3 w-3 opacity-60" /> <span class="font-medium text-zinc-600 dark:text-zinc-400">Tiba:</span> {{ formatDate(po.expectedDate) }}
                      </div>
                    </div>
                  </div>
                  <div class="text-sm font-bold text-right text-zinc-900 dark:text-zinc-100">{{ formatCurrency(po.total) }}</div>
                </div>
                
                <div v-if="['draft', 'ordered'].includes(po.status?.toLowerCase()) && can('purchase_order.update') && !isSuperAdmin" class="flex justify-end gap-2 mt-1 pt-2 border-t border-zinc-100/60 dark:border-zinc-800/40">
                  <Button 
                    v-if="po.status?.toLowerCase() === 'draft'"
                    size="sm"
                    variant="outline"
                    class="h-7 w-28 text-[11px] flex items-center justify-center gap-1 bg-white hover:bg-zinc-50 border-zinc-200 text-zinc-700 shrink-0"
                    :disabled="updatingStatus"
                    @click.stop="updatePOStatus(po.id, 'ordered')"
                  >
                    <Loader2 v-if="updatingPoId === po.id" class="h-3 w-3 animate-spin" />
                    <Send v-else class="h-3 w-3 text-primary" />
                    <span>Kirim PO</span>
                  </Button>
                  
                  <Button 
                    v-if="po.status?.toLowerCase() === 'ordered' && canReceive"
                    size="sm"
                    class="h-7 w-28 text-[11px] flex items-center justify-center gap-1 bg-emerald-600 hover:bg-emerald-700 text-white shrink-0"
                    @click.stop="openReceiptDrawer(po)"
                  >
                    <CheckCircle2 class="h-3 w-3 text-white" />
                    <span>Terima Barang</span>
                  </Button>
                </div>
              </div>
            </div>

            <div class="hidden md:block overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="border-b border-zinc-100 dark:border-zinc-800">
                    <th class="pl-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">No.Pembelian</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Distributor</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Tujuan</th>
                    <th class="py-3 text-right text-xs font-semibold uppercase tracking-wide text-zinc-500">Total</th>
                    <th class="py-3 text-center text-xs font-semibold uppercase tracking-wide text-zinc-500">Status</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Tgl Dipesan</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Estimasi Tiba</th>
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
                      <div class="flex items-center gap-1.5 text-xs text-zinc-700 dark:text-zinc-300">
                        <Warehouse v-if="po.locationType?.toLowerCase() === 'warehouse'" class="h-3.5 w-3.5 opacity-60" />
                        <Building2 v-else class="h-3.5 w-3.5 opacity-60" />
                        <span>{{ getLocationName(po.locationType, po.locationId) }}</span>
                      </div>
                    </td>
                    <td class="py-4 text-right font-bold text-zinc-900 dark:text-zinc-100">
                      {{ formatCurrency(po.total) }}
                    </td>
                    <td class="py-4 text-center">
                      <div class="inline-flex items-center gap-1.5 text-xs font-semibold text-zinc-700 dark:text-zinc-300">
                        <span :class="['w-2 h-2 rounded-full animate-pulse shrink-0', statusDotClass(po.status)]"></span>
                        <span>{{ statusLabel(po.status) }}</span>
                      </div>
                    </td>
                    <td class="py-4 text-xs text-muted-foreground">
                      {{ formatDate(po.orderDate || po.createdAt) }}
                    </td>
                    <td class="py-4 text-xs text-muted-foreground">
                      {{ formatDate(po.expectedDate) }}
                    </td>
                    <td class="pr-5 py-4 text-right">
                      <div class="flex items-center justify-end gap-2">
                        <Button 
                          v-if="po.status?.toLowerCase() === 'draft' && can('purchase_order.update') && !isSuperAdmin"
                          size="sm"
                          variant="outline"
                          class="h-7 w-28 text-xs flex items-center justify-center gap-1 bg-white hover:bg-zinc-50 border-zinc-200 text-zinc-700 hover:text-zinc-900 shrink-0"
                          :disabled="updatingStatus"
                          @click.stop="updatePOStatus(po.id, 'ordered')"
                        >
                          <Loader2 v-if="updatingPoId === po.id" class="h-3.5 w-3.5 animate-spin" />
                          <Send v-else class="h-3.5 w-3.5 text-primary" />
                          <span>Kirim PO</span>
                        </Button>
                        
                        <Button 
                          v-if="po.status?.toLowerCase() === 'ordered' && can('purchase_order.update') && canReceive && !isSuperAdmin"
                          size="sm"
                          class="h-7 w-28 text-xs flex items-center justify-center gap-1 bg-emerald-600 hover:bg-emerald-700 text-white border-0 shadow-sm shrink-0"
                          @click.stop="openReceiptDrawer(po)"
                        >
                          <CheckCircle2 class="h-3.5 w-3.5 text-white" />
                          <span>Terima Barang</span>
                        </Button>
                        
                        <Button variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-zinc-700" @click="openDetail(po)">
                          <Eye class="h-4 w-4" />
                        </Button>
                      </div>
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

    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showDrawer" class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm" @click="showDrawer = false" />
      </Transition>
      <Transition name="scale">
        <div v-if="showDrawer" class="fixed inset-0 z-[50] flex items-center justify-center p-4 pointer-events-none">
          
          <div class="relative flex flex-col w-full max-w-2xl max-h-[90vh] bg-card shadow-2xl border border-border rounded-xl overflow-hidden pointer-events-auto">
            
            <div class="flex items-center justify-between px-6 py-4 border-b shrink-0 bg-muted/20">
              <div>
                <h3 class="font-semibold text-base">{{ drawerMode === 'create' ? 'Buat Purchase Order' : 'Detail Pembelian' }}</h3>
                <p class="text-xs text-muted-foreground mt-0.5">Kelola pesanan barang ke distributor secara efisien.</p>
              </div>
              <Button variant="ghost" size="icon" @click="showDrawer = false">
                <X class="h-4 w-4" />
              </Button>
            </div>

            <template v-if="drawerMode === 'create'">
              <div class="flex-1 overflow-y-auto px-6 py-5 space-y-6">
                <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>
                
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
                    <div v-for="(item, i) in form.items" :key="i" class="relative p-4 rounded-xl border border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30 group">
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
                            <Input v-model.number="item.qtyOrdered" type="number" min="1" class="h-8 text-xs text-center" />
                          </div>
                          <div class="space-y-1">
                            <Label class="text-[10px] text-zinc-500">Harga Satuan </Label>
                            <Input v-model.number="item.unitCost" type="number" min="0" class="h-8 text-xs" />
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="space-y-1.5">
                  <Label>Catatan Tambahan</Label>
                  <textarea v-model="form.notes" rows="3" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:ring-2 focus:ring-primary/20 outline-none resize-none" placeholder="Catatan untuk supplier..." />
                </div>
              </div>

              <div class="px-6 py-4 border-t bg-zinc-50/80 dark:bg-zinc-900/50 shrink-0">
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

            <template v-else-if="selectedPO">
              <div class="flex-1 overflow-y-auto px-6 py-5 space-y-6">
                
                <div class="bg-primary/5 p-4 rounded-xl border border-primary/10 space-y-3">
                  <div class="flex items-center justify-between">
                    <div>
                      <span class="text-[10px] font-bold text-primary uppercase tracking-widest block mb-1">Pembelian</span>
                      <h4 class="font-mono text-lg font-bold leading-none">{{ selectedPO.poNumber }}</h4>
                    </div>
                    <Badge :class="['text-[10px] uppercase tracking-widest font-bold px-3 py-1', statusColor(selectedPO.status)]" variant="outline">
                      {{ statusLabel(selectedPO.status) }}
                    </Badge>
                  </div>
                  
                  <div class="flex items-center gap-2 pt-1 border-t border-primary/10">
                    <button
                      @click="copyPOCode"
                      :class="[
                        'flex items-center gap-1.5 px-3 py-1.5 rounded-md text-[11px] font-semibold transition-all duration-200',
                        copiedCode
                          ? 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border border-emerald-400/30'
                          : 'bg-background border border-primary/20 text-primary hover:bg-primary/10'
                      ]"
                    >
                      <ClipboardCheck v-if="copiedCode" class="h-3.5 w-3.5" />
                      <Copy v-else class="h-3.5 w-3.5" />
                      {{ copiedCode ? 'Tersalin!' : 'Salin Kode PO' }}
                    </button>
                    <button
                      @click="copyAllInfo"
                      :class="[
                        'flex items-center gap-1.5 px-3 py-1.5 rounded-md text-[11px] font-semibold transition-all duration-200',
                        copiedAll
                          ? 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border border-emerald-400/30'
                          : 'bg-background border border-zinc-200 dark:border-zinc-700 text-zinc-600 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800'
                      ]"
                    >
                      <ClipboardCheck v-if="copiedAll" class="h-3.5 w-3.5" />
                      <ClipboardList v-else class="h-3.5 w-3.5" />
                      {{ copiedAll ? 'Tersalin!' : 'Salin Semua Info' }}
                    </button>
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-6">
                  <div class="space-y-1">
                    <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Distributor</p>
                    <p class="text-sm font-semibold text-zinc-900 dark:text-zinc-100">{{ selectedPO.supplier?.name || '-' }}</p>
                    <p class="text-[11px] text-zinc-500 leading-relaxed">{{ selectedPO.supplier?.address || '-' }}</p>
                  </div>
                  <div class="space-y-1">
                    <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Tujuan Penerima</p>
                    <div class="flex items-center gap-1.5 text-sm font-semibold text-zinc-900 dark:text-zinc-100">
                      <Warehouse v-if="selectedPO.locationType?.toLowerCase() === 'warehouse'" class="h-3.5 w-3.5 opacity-50" />
                      <Building2 v-else class="h-3.5 w-3.5 opacity-50" />
                      {{ getLocationName(selectedPO.locationType, selectedPO.locationId) }}
                    </div>
                    <Badge variant="outline" class="text-[9px] h-4 px-1.5 uppercase tracking-tighter mt-0.5">{{ selectedPO.locationType }}</Badge>
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-6 pt-1">
                  <div class="space-y-1">
                    <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Tanggal Pemesanan</p>
                    <p class="text-xs font-medium text-zinc-800 dark:text-zinc-200">{{ formatDate(selectedPO.orderDate || selectedPO.createdAt) }}</p>
                  </div>
                  <div class="space-y-1">
                    <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Estimasi Barang Tiba</p>
                    <p class="text-xs font-medium text-zinc-800 dark:text-zinc-200">{{ formatDate(selectedPO.expectedDate) }}</p>
                  </div>
                </div>

                <div class="space-y-3">
                  <h4 class="text-[10px] font-bold uppercase tracking-widest text-zinc-500 border-b pb-2">Daftar Item Pesanan</h4>
                  <div class="space-y-2">
                    <div v-for="(item, i) in selectedPO.items" :key="i" class="flex justify-between items-center p-3 rounded-lg border border-zinc-100 dark:border-zinc-800 bg-zinc-50/30 dark:bg-zinc-900/20">
                      <div class="flex items-center gap-3">
                        <div class="w-8 h-8 rounded bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-400">
                          <Package class="h-4 w-4" />
                        </div>
                        <div>
                          <p class="text-sm font-semibold text-zinc-900 dark:text-zinc-100">{{ item.product?.name || `Produk #${item.productId}` }}</p>
                          <p class="text-[10px] text-muted-foreground mt-0.5">
                            {{ item.qtyOrdered }} PCS × {{ formatCurrency(item.unitCost) }}
                            <span v-if="item.qtyReceived > 0" class="ml-2 text-emerald-600 font-bold dark:text-emerald-400">• Berhasil Masuk: {{ item.qtyReceived }}</span>
                          </p>
                        </div>
                      </div>
                      <span class="text-sm font-bold text-zinc-900 dark:text-zinc-100">{{ formatCurrency(item.qtyOrdered * item.unitCost) }}</span>
                    </div>
                  </div>
                </div>

                <div v-if="selectedPO.notes" class="space-y-1 bg-amber-50/50 dark:bg-amber-900/10 p-3 rounded-lg border border-amber-100/50 dark:border-amber-800/30">
                  <p class="text-[10px] font-bold text-amber-600 uppercase tracking-wider">Catatan</p>
                  <p class="text-xs italic text-amber-800/80 dark:text-amber-200/80">{{ selectedPO.notes }}</p>
                </div>
              </div>

              <div class="px-6 py-5 border-t bg-zinc-50/80 dark:bg-zinc-900/50 mt-auto shrink-0">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-xs text-muted-foreground font-bold">SUBTOTAL COST</span>
                  <span class="text-sm font-medium text-zinc-700 dark:text-zinc-300">{{ formatCurrency(selectedPO.total) }}</span>
                </div>
                <div class="flex items-center justify-between mb-4">
                  <span class="text-xs text-muted-foreground font-bold uppercase tracking-widest">Grand Total</span>
                  <span class="text-2xl font-black text-primary">{{ formatCurrency(selectedPO.total) }}</span>
                </div>
                
                <div class="flex gap-2">
                  <Button 
                    v-if="selectedPO.status?.toLowerCase() === 'draft' && can('purchase_order.update') && !isSuperAdmin"
                    class="flex-1"
                    :disabled="updatingStatus"
                    @click="updatePOStatus(selectedPO.id, 'ordered')"
                  >
                    <Loader2 v-if="updatingStatus" class="h-4 w-4 mr-2 animate-spin" />
                    <Send v-else class="h-4 w-4 mr-2" />
                    Kirim PO
                  </Button>
                  <Button 
                    v-if="selectedPO.status?.toLowerCase() === 'ordered' && canReceive && !isSuperAdmin"
                    class="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white font-bold"
                    @click="openReceiptDrawer(selectedPO)"
                  >
                    <CheckCircle2 class="h-4 w-4 mr-2" />
                    Terima Barang
                  </Button>
                  <Button 
                    v-if="['draft', 'ordered'].includes(selectedPO.status?.toLowerCase()) && can('purchase_order.update') && !isSuperAdmin"
                    variant="destructive"
                    class="flex-1 bg-red-50 text-red-600 hover:bg-red-100 hover:text-red-700 border border-red-200"
                    :disabled="updatingStatus"
                    @click="updatePOStatus(selectedPO.id, 'cancelled')"
                  >
                    <X class="h-4 w-4 mr-2" />
                    Batalkan PO
                  </Button>
                  <Button variant="outline" class="flex-1" @click="showDrawer = false">Tutup</Button>
                </div>
              </div>
            </template>
          </div>

        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showReceiptDrawer" class="fixed inset-0 z-[60] bg-black/50 backdrop-blur-sm" @click="showReceiptDrawer = false" />
      </Transition>
      <Transition name="scale">
        <div v-if="showReceiptDrawer" class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none">
          
          <div class="relative flex flex-col w-full max-w-2xl max-h-[90vh] bg-card shadow-2xl border border-border rounded-xl overflow-hidden pointer-events-auto">
            
            <div class="flex items-center justify-between px-6 py-4 border-b shrink-0 bg-emerald-50/60 dark:bg-emerald-950/20">
              <div>
                <h3 class="font-semibold text-base text-emerald-800 dark:text-emerald-300">Terima Barang Masuk</h3>
                <p class="text-xs text-emerald-600/80 dark:text-emerald-400/70 mt-0.5">Konfirmasi penerimaan produk fisik dan update kartu stok.</p>
              </div>
              <Button variant="ghost" size="icon" @click="showReceiptDrawer = false">
                <X class="h-4 w-4" />
              </Button>
            </div>

            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5">
              <div v-if="receiptError" class="rounded-md border border-red-200 dark:border-red-900 bg-red-50 dark:bg-red-950/30 p-3">
                <p class="text-sm text-red-600 dark:text-red-400">{{ receiptError }}</p>
              </div>

              <div class="space-y-1.5">
                <Label>Tanggal & Waktu Terima <span class="text-destructive">*</span></Label>
                <Input v-model="receiptForm.receivedDate" type="datetime-local" class="h-9" :disabled="receiptSaving" />
              </div>

              <div class="space-y-1.5">
                <Label>Catatan Log Penerimaan</Label>
                <textarea
                  v-model="receiptForm.notes"
                  rows="2"
                  :disabled="receiptSaving"
                  placeholder="Instruksi tambahan atau kondisi pengiriman barang..."
                  class="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 resize-none"
                />
              </div>

              <div class="space-y-3">
                <h4 class="text-[10px] font-bold uppercase tracking-widest text-zinc-500 border-b pb-2">Rincian Jumlah Kuantitas Tiba</h4>
                
                <div v-for="(item, i) in receiptForm.items" :key="i" class="p-4 rounded-xl border border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30 space-y-3">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <div class="w-7 h-7 rounded bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center">
                        <Package class="h-3.5 w-3.5 text-zinc-400" />
                      </div>
                      <span class="text-sm font-semibold text-zinc-900 dark:text-zinc-100">{{ item.productName }}</span>
                    </div>
                    <span class="text-[10px] text-zinc-400 font-bold bg-zinc-100 dark:bg-zinc-800 px-2 py-0.5 rounded">Tag PO: {{ item.qtyOrdered }} PCS</span>
                  </div>
                  
                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    <div class="space-y-1">
                      <Label class="text-[10px] text-zinc-500">Qty Fisik Diterima <span class="text-destructive">*</span></Label>
                      <Input
                        v-model.number="item.qtyReceived"
                        type="number"
                        min="1"
                        :max="item.qtyOrdered"
                        class="h-8 text-sm text-center font-bold"
                        :disabled="receiptSaving"
                      />
                    </div>
                    <div class="space-y-1">
                      <Label class="text-[10px] text-zinc-500">Catatan Item Masuk</Label>
                      <Input v-model="item.notes" placeholder="Contoh: Aman, Box Robek" class="h-8 text-sm" :disabled="receiptSaving" />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="flex gap-3 px-6 py-4 border-t shrink-0 bg-muted/30">
              <Button variant="outline" class="flex-1" @click="showReceiptDrawer = false" :disabled="receiptSaving">Batal</Button>
              <Button class="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white font-bold" @click="submitReceipt" :disabled="receiptSaving">
                <Loader2 v-if="receiptSaving" class="h-4 w-4 mr-2 animate-spin" />
                <CheckCircle2 v-else class="h-4 w-4 mr-2" />
                Konfirmasi Terima
              </Button>
            </div>
          </div>

        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>

<style scoped>
/* ─── FADE EFFECT BACKGROUND BACKDROP ─── */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ─── SCALE EFFECT DI TENGAH MONITOR LU ─── */
.scale-enter-active,
.scale-leave-active {
  transition: opacity 0.25s ease, transform 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}
.scale-enter-from,
.scale-leave-to {
  opacity: 0;
  transform: scale(0.96);
}
</style>