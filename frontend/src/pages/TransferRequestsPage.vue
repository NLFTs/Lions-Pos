<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
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
  Plus, Loader2, Repeat2, Eye, Check, Building2,
  ArrowRight, ArrowLeft, Package, Calendar, Trash2, Send, ChevronDown
} from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.isAdmin)
const currentBranchId = computed(() => authStore.user?.branchId || authStore.user?.branch?.id || null)

// Pengguna dianggap sebagai pengelola cabang jika memiliki ID cabang yang ditetapkan
const isBranchManager = computed(() => {
  return !!currentBranchId.value || authStore.user?.roles?.includes('pengelola-cabang')
})

const isOwner = computed(() => {
  const roles = authStore.user?.roles || []
  return roles.includes('owner') || roles.includes('admin-partners') || (!currentBranchId.value && !roles.includes('pengelola-cabang'))
})

const isSuperAdmin = computed(() => authStore.isSuperAdmin)

const userLocationName = computed(() => {
  if (currentBranchId.value) return authStore.user.branchName || authStore.user?.branch?.name || ''
  return ''
})

// Logika kelayakan penerima barang di cabang tujuan (harus pengelola cabang tujuan)
const isTargetReceiver = computed(() => {
  if (!selectedTR.value) return false
  const toId = String(selectedTR.value.toLocationId || '')
  const myBranchId = String(currentBranchId.value || '')
  return isBranchManager.value && myBranchId === toId
})

// Logika kelayakan pengirim barang dari cabang asal (harus pengelola cabang asal)
const isSourceSender = computed(() => {
  if (!selectedTR.value) return false
  const fromId = String(selectedTR.value.fromLocationId || '')
  const myBranchId = String(currentBranchId.value || '')
  return isBranchManager.value && myBranchId === fromId
})

const transfers = ref([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('all')
const page = ref(1)
const pageSize = ref(10)

const statusOptions = [
  { value: 'all', label: 'Semua Status' },
  { value: 'pending', label: 'Menunggu Persetujuan' },
  { value: 'approved', label: 'Disetujui' },
  { value: 'in_transit', label: 'Dalam Pengiriman' },
  { value: 'received', label: 'Diterima' },
  { value: 'cancelled', label: 'Dibatalkan' }
]

const branches = ref([])
const products = ref([])

// State kontrol dropdown kustom
const showFromDropdown = ref(false)
const showToDropdown = ref(false)
const showProductDropdowns = ref([])

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
  return [...r].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
})

const paginatedTRs = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredTRs.value.slice(start, start + pageSize.value)
})

const showForm = ref(false)
const formMode = ref('detail')
const saving = ref(false)
const formError = ref(null)
const selectedTR = ref(null)
const updatingStatus = ref(false)

const emptyForm = () => ({
  fromLocationType: 'branch',
  fromLocationId: '',
  toLocationType: 'branch',
  toLocationId: '',
  notes: '',
  items: [{ productId: '', qtyRequested: 1 }]
})

const form = ref(emptyForm())

function extractArray(response) {
  if (!response) return []
  if (Array.isArray(response)) return response
  if (response.data && Array.isArray(response.data)) return response.data
  const innerData = response.data?.data || response.data
  if (Array.isArray(innerData)) return innerData
  if (innerData && Array.isArray(innerData.content)) return innerData.content
  return []
}

// Menyaring daftar cabang asal yang bisa dipilih
const availableFromLocations = computed(() => {
  const targetId = form.value.toLocationId
  if (!targetId) return branches.value
  return branches.value.filter(loc => String(loc.id) !== String(targetId))
})

// Menyaring daftar cabang tujuan yang bisa dipilih
const availableToLocations = computed(() => {
  const sourceId = form.value.fromLocationId
  if (!sourceId) return branches.value
  return branches.value.filter(loc => String(loc.id) !== String(sourceId))
})

async function fetchData() {
  loading.value = true
  try {
    const url = isAdmin.value ? '/api/v1/transfer-requests/admin' : '/api/v1/transfer-requests'
    const res = await api.get(url)
    const data = res.data.data
    const raw = Array.isArray(data) ? data : (data?.content || [])
    transfers.value = raw.map(t => ({
      ...t,
      fromLocationType: 'branch',
      fromLocationId: t.fromLocationId || t.from_location_id,
      fromLocationName: t.fromLocationName || t.from_location_name || `Cabang #${t.fromLocationId || ''}`,
      toLocationType: 'branch',
      toLocationId: t.toLocationId || t.to_location_id,
      toLocationName: t.toLocationName || t.to_location_name || `Cabang #${t.toLocationId || ''}`,
      status: t.status ? t.status.toLowerCase() : t.status,
      createdAt: t.createdAt || t.created_at,
      createdBy: t.createdBy || t.created_by,
      requestedBy: t.createdBy || t.created_by || t.requestedBy || t.requested_by,
      items: (t.items || []).map(item => ({
        ...item,
        qtyRequested: item.qtyRequested ?? item.qty_requested,
        qtyReceived: item.qtyReceived ?? item.qty_received,
      }))
    }))
  } catch (err) {
    toast.error('Gagal memuat data transfer stok.')
  } finally {
    loading.value = false
  }
}

async function loadFormOptions() {
  try {
    const urlB = isAdmin.value ? '/api/v1/branches/admin' : '/api/v1/branches'
    const urlP = isAdmin.value ? '/api/v1/products/admin' : '/api/v1/products'
    
    const [resB, resP] = await Promise.all([
      api.get(urlB),
      api.get(urlP)
    ])

    branches.value = extractArray(resB)
    products.value = extractArray(resP)
  } catch (err) {
    toast.error('Gagal memuat pilihan formulir.')
  }
}

async function openCreate() {
  form.value = emptyForm()
  
  if (currentBranchId.value) {
    form.value.toLocationType = 'branch'
    form.value.toLocationId = currentBranchId.value
    form.value.fromLocationType = 'branch'
    form.value.fromLocationId = ''
  }

  formMode.value = 'create'
  formError.value = null
  showForm.value = true
  await loadFormOptions()
}

function openDetail(tr) {
  selectedTR.value = tr
  formMode.value = 'detail'
  showForm.value = true
}

function closeForm() {
  showForm.value = false
}

function addItem() {
  form.value.items.push({ productId: '', qtyRequested: 1 })
}

function removeItem(index) {
  form.value.items.splice(index, 1)
}

async function saveTR() {
  if (!form.value.fromLocationId || !form.value.toLocationId) { 
    formError.value = 'Cabang Asal dan Tujuan wajib diisi.'; 
    return 
  }
  
  if (String(form.value.fromLocationId) === String(form.value.toLocationId)) {
    formError.value = 'Cabang asal dan tujuan tidak boleh sama.'; 
    return
  }

  if (!form.value.items.length || form.value.items.some(i => !i.productId)) {
    formError.value = 'Harap pilih setidaknya satu produk.'; 
    return
  }

  saving.value = true
  formError.value = null
  try {
    const payload = {
      fromLocationType: 'BRANCH',
      from_location_type: 'BRANCH',
      fromLocationId: Number(form.value.fromLocationId),
      from_location_id: Number(form.value.fromLocationId),
      toLocationType: 'BRANCH',
      to_location_type: 'BRANCH',
      toLocationId: Number(form.value.toLocationId),
      to_location_id: Number(form.value.toLocationId),
      notes: form.value.notes,
      items: form.value.items.map(i => ({ 
        productId: Number(i.productId), 
        product_id: Number(i.productId),
        qtyRequested: Number(i.qtyRequested),
        qty_requested: Number(i.qtyRequested)
      }))
    }

    await api.post('/api/v1/transfer-requests', payload)
    toast.success('Permintaan transfer stok berhasil dikirim!')
    showForm.value = false
    fetchData()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Gagal mengirim permintaan transfer.'
  } finally {
    saving.value = false
  }
}

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
  const m = { pending: 'Menunggu Persetujuan', approved: 'Disetujui', in_transit: 'Dalam Pengiriman', received: 'Diterima', cancelled: 'Dibatalkan' }
  return m[s] || s
}

async function updateTRStatus(id, newStatus) {
  updatingStatus.value = true
  try {
    if (newStatus.toLowerCase() === 'received') {
      
      const currentItems = selectedTR.value?.items || []
      
      console.log("🔍 [DEBUG INTERNALS] Isi raw items dari selectedTR:", currentItems)

      const payloadItems = currentItems.map(item => {
        const idTerdeteksi = 
          item.product?.id || 
          item.productId || 
          item.product_id || 
          item.idProduct;
        
        const qty = Number(item.qtyRequested || item.qty_requested || item.qty || 0);
        return {
          productId: idTerdeteksi ? Number(idTerdeteksi) : null,
          product_id: idTerdeteksi ? Number(idTerdeteksi) : null,
          
          qtyRequested: qty,
          qty_requested: qty,
          
          qtyReceived: qty,
          qty_received: qty,
          notes: item.notes || ''
        }
      })

      console.log(" [FINAL PAYLOAD TO JAVA]:", JSON.stringify(payloadItems, null, 2))

      if (payloadItems.some(i => i.productId === null || isNaN(i.productId))) {
        toast.error('Gagal: ID Produk tidak terdeteksi. Silakan buka F12 Console untuk cek struktur data.')
        updatingStatus.value = false
        return
      }

      await api.patch(`/api/v1/transfer-requests/${id}/receive`, payloadItems)
    } else {
      await api.patch(`/api/v1/transfer-requests/${id}/status?status=${newStatus.toUpperCase()}`)
    }
    toast.success('Status transfer stok berhasil diperbarui!')
    showForm.value = false
    fetchData()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal memperbarui status transfer.')
  } finally {
    updatingStatus.value = false
  }
}

const handleOutsideClick = (e) => {
  if (!e.target.closest('.custom-select-from')) showFromDropdown.value = false
  if (!e.target.closest('.custom-select-to')) showToDropdown.value = false
  form.value.items.forEach((_, i) => {
    if (!e.target.closest(`.custom-select-prod-${i}`)) {
      showProductDropdowns.value[i] = false
    }
  })
}

onMounted(() => {
  fetchData()
  window.addEventListener('click', handleOutsideClick)
})

onUnmounted(() => {
  window.removeEventListener('click', handleOutsideClick)
})
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <Transition name="fade" mode="out-in">

        <!-- ─── Tampilan Utama: Daftar Permintaan Transfer ─── -->
        <div v-if="!showForm" key="table-view" class="flex flex-col gap-6">
          <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
            <div>
              <h1 class="text-2xl font-bold tracking-tight">Transfer Stok</h1>
              <p class="text-muted-foreground text-sm mt-1">Kelola pengiriman dan keluar-masuk barang antar cabang toko Anda.</p>
            </div>
            <div class="flex items-center gap-3 w-full md:w-auto">
              <div class="w-full sm:w-72">
                <DataTableSearch v-model="searchQuery" placeholder="Cari cabang asal/tujuan" />
              </div>
              <CustomSelect v-model="statusFilter" :options="statusOptions" class="w-full sm:w-48" />
              <Button v-if="can('transfer_request.store') && !isSuperAdmin" @click="openCreate" size="sm" class="flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
                <Plus class="h-4 w-4" /><span>Minta Transfer</span>
              </Button>
            </div>
          </div>

          <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
            <CardContent class="p-0">
              <div v-if="loading" class="flex items-center justify-center py-24">
                <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
              </div>
              <div v-else-if="filteredTRs.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
                <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
                  <Repeat2 class="h-7 w-7 opacity-40" />
                </div>
                <p class="text-sm font-medium">Belum ada riwayat transfer stok</p>
                <Button v-if="can('transfer_request.store') && !isSuperAdmin && !searchQuery" size="sm" class="mt-4" @click="openCreate">
                  <Plus class="h-3.5 w-3.5 mr-1.5" />Buat Permintaan
                </Button>
              </div>

              <div v-else>
                <!-- Versi Mobile -->
                <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
                  <div v-for="tr in paginatedTRs" :key="tr.id" class="p-4 flex flex-col gap-3 hover:bg-zinc-50 dark:hover:bg-zinc-900/40 cursor-pointer transition-colors" @click="openDetail(tr)">
                    <div class="flex justify-between items-center">
                      <div class="flex items-center gap-2">
                        <span class="text-xs font-bold text-zinc-900 dark:text-zinc-100">{{ tr.fromLocationName }}</span>
                        <ArrowRight class="h-3 w-3 text-zinc-400" />
                        <span class="text-xs font-bold text-zinc-900 dark:text-zinc-100">{{ tr.toLocationName }}</span>
                      </div>
                      <Badge :class="['text-[9px] uppercase tracking-wider', statusColor(tr.status)]" variant="outline">{{ statusLabel(tr.status) }}</Badge>
                    </div>
                    <div class="flex justify-between items-center text-[10px] text-muted-foreground">
                      <div class="flex items-center gap-1.5"><Package class="h-3.5 w-3.5" /> {{ tr.items?.length || 0 }} Jenis Item</div>
                      <div class="flex items-center gap-1.5"><Calendar class="h-3.5 w-3.5" /> {{ formatDate(tr.createdAt) }}</div>
                    </div>
                  </div>
                </div>

                <!-- Versi Desktop (Tabel) -->
                <div class="hidden md:block overflow-x-auto">
                  <table class="w-full text-sm">
                    <thead>
                      <tr class="border-b border-zinc-100 dark:border-zinc-800">
                        <th class="pl-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Cabang Pengirim (Asal) → Penerima (Tujuan)</th>
                        <th class="py-3 text-center text-xs font-semibold uppercase tracking-wide text-zinc-500">Jumlah Barang</th>
                        <th class="py-3 text-center text-xs font-semibold uppercase tracking-wide text-zinc-500">Status</th>
                        <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Dibuat Oleh</th>
                        <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Tanggal Permintaan</th>
                        <th class="pr-5 py-3 text-right">Aksi</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="tr in paginatedTRs" :key="tr.id" class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors">
                        <td class="pl-5 py-4">
                          <div class="flex items-center gap-3">
                            <div class="flex flex-col items-center gap-0.5 shrink-0">
                              <Building2 class="h-4 w-4 text-zinc-400" />
                              <div class="w-px h-3.5 bg-zinc-200 dark:bg-zinc-700"></div>
                              <Building2 class="h-4 w-4 text-primary" />
                            </div>
                            <div class="flex flex-col gap-0.5">
                              <span class="text-xs font-medium text-muted-foreground">{{ tr.fromLocationName }}</span>
                              <span class="text-xs font-bold text-zinc-900 dark:text-zinc-100">{{ tr.toLocationName }}</span>
                            </div>
                          </div>
                        </td>
                        <td class="py-4 text-center">
                          <div class="inline-flex items-center justify-center px-2.5 py-1 rounded-md bg-zinc-100 dark:bg-zinc-800 text-[10px] font-bold">{{ tr.items?.length || 0 }} ITEM</div>
                        </td>
                        <td class="py-4 text-center">
                          <Badge :class="['text-[9px] uppercase tracking-widest font-bold px-2 py-1', statusColor(tr.status)]" variant="outline">{{ statusLabel(tr.status) }}</Badge>
                        </td>
                        <td class="py-4 text-xs font-medium">{{ tr.requestedBy?.username || '-' }}</td>
                        <td class="py-4 text-xs text-muted-foreground">{{ formatDate(tr.createdAt) }}</td>
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

              <DataTablePagination v-if="filteredTRs.length > 0 && !loading" :page="page" :page-size="pageSize" :total="filteredTRs.length" @update:page="page = $event" @update:page-size="pageSize = $event; page = 1" />
            </CardContent>
          </Card>
        </div>

        <!-- ─── Tampilan Formulir: Detail / Tambah Permintaan ─── -->
        <div v-else key="form-view" class="flex flex-col gap-6">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-zinc-200 dark:border-zinc-800 pb-4">
            <div class="flex items-center gap-3">
              <Button variant="outline" size="icon" @click="closeForm" :disabled="saving || updatingStatus" class="h-9 w-9 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800">
                <ArrowLeft class="h-4 w-4" />
              </Button>
              <div>
                <h2 class="text-xl font-bold tracking-tight flex items-center gap-2">
                  <span>{{ formMode === 'create' ? 'Permintaan Transfer Stok' : 'Atur Perpindahan Stok' }}</span>
                </h2>
                <p class="text-xs text-muted-foreground mt-0.5">{{ formMode === 'create' ? 'Atur keluar-masuk atau perpindahan stok antar cabang.' : 'Periksa informasi dan perbarui status alur pemindahan.' }}</p>
              </div>
            </div>
            <div v-if="formMode === 'create'" class="flex items-center gap-3 w-full sm:w-auto">
              <Button variant="outline" @click="closeForm" :disabled="saving" class="flex-1 sm:flex-none">Batal</Button>
              <Button @click="saveTR" :disabled="saving" class="flex-1 sm:flex-none bg-primary text-primary-foreground hover:bg-primary/95 shadow-md shadow-primary/20">
                <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
                <span>Kirim</span>
              </Button>
            </div>
            <div v-else class="flex items-center gap-3 w-full sm:w-auto">
              <Button variant="outline" @click="closeForm" :disabled="updatingStatus" class="flex-1 sm:flex-none">Kembali</Button>
            </div>
          </div>

          <!-- FORMULIR TAMBAH TRANSFER STOK BARU (Antar Cabang) -->
          <template v-if="formMode === 'create'">
            <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
              
              <div class="lg:col-span-7 space-y-4">
                <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                  <CardContent class="p-5 space-y-5">
                    <div class="flex items-center gap-2 border-b pb-3 border-zinc-100 dark:border-zinc-800">
                      <ArrowRight class="h-4 w-4 text-primary" />
                      <h4 class="text-sm font-bold uppercase tracking-widest text-primary">Rute Pengiriman</h4>
                    </div>

                    <!-- Cabang Asal (Sumber Stok) -->
                    <div class="space-y-2">
                      <Label class="text-xs font-bold">Cabang Pengirim (Sumber Stok) <span class="text-destructive">*</span></Label>
                      <div class="relative custom-select-from">
                        <button type="button" @click="showFromDropdown = !showFromDropdown" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-primary/20 flex items-center justify-between text-left transition-all duration-150">
                          <span :class="form.fromLocationId ? 'text-zinc-900 dark:text-zinc-100 font-medium' : 'text-muted-foreground'">
                            {{ branches.find(b => b.id === form.fromLocationId)?.name || 'Pilih cabang pengirim asal...' }}
                          </span>
                          <ChevronDown class="h-4 w-4 text-zinc-400 transition-transform duration-200" :class="{ 'rotate-180': showFromDropdown }" />
                        </button>
                        <Transition name="dropdown">
                          <div v-if="showFromDropdown" class="absolute z-50 mt-1.5 w-full bg-background border border-input rounded-md shadow-lg max-h-60 overflow-y-auto py-1">
                            <div v-if="availableFromLocations.length === 0" class="px-3 py-2.5 text-xs text-muted-foreground text-center">
                              Tidak ada cabang tersedia
                            </div>
                            <button v-for="l in availableFromLocations" :key="l.id" type="button" @click="form.fromLocationId = l.id; showFromDropdown = false" class="w-full px-3 py-2 text-left text-sm hover:bg-zinc-100 dark:hover:bg-zinc-800/80 transition-colors flex items-center justify-between">
                              <span>{{ l.name }}</span>
                              <Check v-if="form.fromLocationId === l.id" class="h-4 w-4 text-primary" />
                            </button>
                          </div>
                        </Transition>
                      </div>
                    </div>

                    <div class="flex justify-center -my-2">
                      <div class="bg-white dark:bg-zinc-900 p-1.5 rounded-full border border-zinc-200 dark:border-zinc-800">
                        <Repeat2 class="h-4 w-4 text-primary animate-pulse" />
                      </div>
                    </div>

                    <!-- Cabang Tujuan (Penerima) -->
                    <div class="space-y-2">
                      <Label class="text-xs font-bold">Cabang Penerima (Tujuan) <span class="text-destructive">*</span></Label>
                      <!-- Jika login sebagai pengelola cabang, otomatis isi cabang pengguna sendiri -->
                      <div v-if="isBranchManager" class="flex items-center gap-3 p-3 rounded-lg border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 shadow-sm">
                        <div class="p-2 rounded-md bg-zinc-100 dark:bg-zinc-900 text-zinc-500"><Building2 class="h-4 w-4" /></div>
                        <div class="flex flex-col">
                          <span class="text-xs font-bold text-zinc-900 dark:text-zinc-100">{{ userLocationName }}</span>
                          <span class="text-[9px] text-zinc-400 font-bold uppercase tracking-wider mt-0.5">Lokasi Cabang Anda</span>
                        </div>
                      </div>
                      <div v-else>
                        <div class="relative custom-select-to">
                          <button type="button" @click="showToDropdown = !showToDropdown" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-primary/20 flex items-center justify-between text-left transition-all duration-150">
                            <span :class="form.toLocationId ? 'text-zinc-900 dark:text-zinc-100 font-medium' : 'text-muted-foreground'">
                              {{ branches.find(b => b.id === form.toLocationId)?.name || 'Pilih cabang penerima tujuan...' }}
                            </span>
                            <ChevronDown class="h-4 w-4 text-zinc-400 transition-transform duration-200" :class="{ 'rotate-180': showToDropdown }" />
                          </button>
                          <Transition name="dropdown">
                            <div v-if="showToDropdown" class="absolute z-50 mt-1.5 w-full bg-background border border-input rounded-md shadow-lg max-h-60 overflow-y-auto py-1">
                              <div v-if="availableToLocations.length === 0" class="px-3 py-2.5 text-xs text-muted-foreground text-center">
                                Tidak ada cabang tersedia
                              </div>
                              <button v-for="l in availableToLocations" :key="l.id" type="button" @click="form.toLocationId = l.id; showToDropdown = false" class="w-full px-3 py-2 text-left text-sm hover:bg-zinc-100 dark:hover:bg-zinc-800/80 transition-colors flex items-center justify-between">
                                <span>{{ l.name }}</span>
                                <Check v-if="form.toLocationId === l.id" class="h-4 w-4 text-primary" />
                              </button>
                            </div>
                          </Transition>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                  <CardContent class="p-5 space-y-3">
                    <Label class="text-xs font-bold uppercase tracking-widest text-zinc-500">Catatan / Keterangan</Label>
                    <textarea v-model="form.notes" rows="3" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:ring-2 focus:ring-primary/20 outline-none resize-none" placeholder="Tulis alasan pemindahan stok atau instruksi logistik..." />
                  </CardContent>
                </Card>
              </div>

              <!-- Kolom Item Produk -->
              <div class="lg:col-span-5">
                <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                  <CardContent class="p-5 space-y-4">
                    <div class="flex items-center justify-between border-b pb-3 border-zinc-100 dark:border-zinc-800">
                      <div class="flex items-center gap-2">
                        <Package class="h-4 w-4 text-zinc-400" />
                        <h4 class="text-xs font-bold uppercase tracking-widest text-zinc-500">Daftar Barang</h4>
                      </div>
                      <Button variant="outline" size="sm" class="h-7 text-[10px] font-bold" @click="addItem">
                        <Plus class="h-3 w-3 mr-1" /> Tambah Item
                      </Button>
                    </div>

                    <div class="space-y-3">
                      <div v-for="(item, i) in form.items" :key="i" class="relative p-3 rounded-xl border border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30 group">
                        <Button v-if="form.items.length > 1" variant="ghost" size="icon" class="absolute -top-2 -right-2 h-6 w-6 rounded-full bg-destructive text-white hover:bg-destructive/90 opacity-0 group-hover:opacity-100 transition-opacity shadow-lg" @click="removeItem(i)">
                          <Trash2 class="h-3 w-3" />
                        </Button>
                        <div class="grid grid-cols-4 gap-3">
                          <div class="col-span-3 space-y-1">
                            <Label class="text-[10px] text-zinc-500">Nama Produk</Label>
                            <div :class="`relative custom-select-prod-${i}`">
                              <button type="button" @click="showProductDropdowns[i] = !showProductDropdowns[i]" class="w-full h-8 rounded-md border border-input bg-background px-2 text-xs outline-none flex items-center justify-between text-left transition-all duration-150">
                                <span :class="item.productId ? 'text-zinc-900 dark:text-zinc-100 font-medium' : 'text-muted-foreground'">
                                  {{ products.find(p => p.id === item.productId)?.name || 'Pilih produk...' }}
                                </span>
                                <ChevronDown class="h-3.5 w-3.5 text-zinc-400" />
                              </button>
                              <Transition name="dropdown">
                                <div v-if="showProductDropdowns[i]" class="absolute z-50 mt-1 w-full bg-background border border-input rounded-md shadow-lg max-h-48 overflow-y-auto py-1">
                                  <div v-if="products.length === 0" class="px-2 py-1.5 text-[10px] text-muted-foreground text-center">
                                    Tidak ada produk terdaftar
                                  </div>
                                  <button v-for="p in products" :key="p.id" type="button" @click="item.productId = p.id; showProductDropdowns[i] = false" class="w-full px-2 py-1.5 text-left text-xs hover:bg-zinc-100 dark:hover:bg-zinc-800/80 transition-colors flex items-center justify-between">
                                    <span>{{ p.name }}</span>
                                    <Check v-if="item.productId === p.id" class="h-3.5 w-3.5 text-primary" />
                                  </button>
                                </div>
                              </Transition>
                            </div>
                          </div>
                          <div class="col-span-1 space-y-1">
                            <Label class="text-[10px] text-zinc-500">Jumlah</Label>
                            <Input v-model.number="item.qtyRequested" type="number" min="1" class="h-8 text-xs text-center" />
                          </div>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </div>
          </template>

          <!-- TAMPILAN DETAIL PERMINTAAN YANG SUDAH ADA -->
          <template v-else-if="selectedTR">
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
              
              <div class="lg:col-span-7 space-y-4">
                <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                  <CardContent class="p-5 space-y-4">
                    <div class="flex items-center justify-between bg-primary/5 p-4 rounded-xl border border-primary/10">
                      <div class="flex items-center gap-4">
                        <div class="flex flex-col items-center">
                          <Badge variant="outline" class="text-[9px] h-4 mb-1 uppercase bg-white dark:bg-zinc-950">Asal</Badge>
                          <span class="text-xs font-bold max-w-[120px] truncate">{{ selectedTR.fromLocationName }}</span>
                        </div>
                        <ArrowRight class="h-4 w-4 text-primary shrink-0" />
                        <div class="flex flex-col items-center">
                          <Badge variant="outline" class="text-[9px] h-4 mb-1 uppercase bg-white dark:bg-zinc-950">Tujuan</Badge>
                          <span class="text-xs font-bold max-w-[120px] truncate">{{ selectedTR.toLocationName }}</span>
                        </div>
                      </div>
                      <Badge :class="['text-[9px] uppercase tracking-widest font-bold px-2 py-1', statusColor(selectedTR.status)]" variant="outline">{{ statusLabel(selectedTR.status) }}</Badge>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                      <div class="p-3 rounded-lg border border-zinc-100 dark:border-zinc-800 bg-card">
                        <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-1">Diajukan Oleh</p>
                        <p class="font-semibold text-xs">{{ selectedTR.requestedBy?.username || '-' }}</p>
                      </div>
                      <div class="p-3 rounded-lg border border-zinc-100 dark:border-zinc-800 bg-card">
                        <p class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-1">Tanggal Permintaan</p>
                        <p class="font-semibold text-xs">{{ formatDate(selectedTR.createdAt) }}</p>
                      </div>
                    </div>

                    <div v-if="selectedTR.notes" class="bg-zinc-50 dark:bg-zinc-900 p-3 rounded-lg border border-zinc-100 dark:border-zinc-800">
                      <p class="text-[10px] font-bold text-zinc-400 uppercase tracking-wider mb-1">Catatan</p>
                      <p class="text-xs italic text-zinc-700 dark:text-zinc-300">"{{ selectedTR.notes }}"</p>
                    </div>
                  </CardContent>
                </Card>

                <!-- Tombol Aksi Alur Status -->
                <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                  <CardContent class="p-5 space-y-3">
                    <p class="text-[10px] font-bold text-zinc-500 uppercase tracking-widest border-b pb-2 border-zinc-100 dark:border-zinc-800">Aksi Persetujuan & Logistik</p>
                    <div class="flex flex-col gap-2">
                      
                      <!-- 1. Owner/Admin-Partners menyetujui permintaan transfer -->
                      <Button v-if="selectedTR.status === 'pending' && isOwner && can('transfer_request.update') && !isSuperAdmin" class="w-full bg-primary text-primary-foreground font-bold hover:bg-primary/90" :disabled="updatingStatus" @click="updateTRStatus(selectedTR.id, 'approved')">
                        <Loader2 v-if="updatingStatus" class="h-4 w-4 mr-2 animate-spin" />
                        <Check v-else class="h-4 w-4 mr-2" />Setujui Transfer (Owner / Admin-Partners)
                      </Button>

                      <!-- 2. Pengelola cabang asal mengirim barang ke tujuan -->
                      <Button v-if="selectedTR.status === 'approved' && isBranchManager && isSourceSender && can('transfer_request.update') && !isSuperAdmin" class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold" :disabled="updatingStatus" @click="updateTRStatus(selectedTR.id, 'in_transit')">
                        <Loader2 v-if="updatingStatus" class="h-4 w-4 mr-2 animate-spin" />
                        <Send v-else class="h-4 w-4 mr-2" />Mulai Pengiriman (Cabang Asal)
                      </Button>

                      <!-- 3. Pengelola cabang tujuan mengkonfirmasi penerimaan stok -->
                      <Button v-if="selectedTR.status === 'in_transit' && isBranchManager && isTargetReceiver && can('transfer_request.update') && !isSuperAdmin" class="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold" :disabled="updatingStatus" @click="updateTRStatus(selectedTR.id, 'RECEIVED')">
                        <Loader2 v-if="updatingStatus" class="h-4 w-4 mr-2 animate-spin" />
                        <Check v-else class="h-4 w-4 mr-2" />Konfirmasi Penerimaan Stok (Cabang Tujuan)
                      </Button>

                      <!-- Notifikasi Status Saat Ini -->
                      <div v-if="selectedTR.status === 'pending' && isBranchManager" class="w-full text-center py-3 text-xs font-bold text-amber-600 bg-amber-50/60 dark:bg-amber-900/10 rounded-md border border-amber-200/50">
                        ⌛ Menunggu persetujuan dari Owner Pusat...
                      </div>
                      <div v-if="selectedTR.status === 'approved' && isBranchManager && isTargetReceiver" class="w-full text-center py-3 text-xs font-bold text-blue-600 bg-blue-50/60 dark:bg-blue-900/10 rounded-md border border-blue-200/50">
                        📦 Sudah disetujui! Menunggu cabang asal memulai pengiriman barang...
                      </div>
                      <div v-if="selectedTR.status === 'in_transit' && isBranchManager && !isTargetReceiver" class="w-full text-center py-3 text-xs font-bold text-violet-600 bg-violet-50/60 dark:bg-violet-900/10 rounded-md border border-violet-200/50">
                        🚚 Barang sedang dalam perjalanan menuju cabang tujuan...
                      </div>
                      <div v-if="selectedTR.status === 'cancelled'" class="w-full text-center py-3 text-xs font-bold text-red-600 bg-red-50 dark:bg-red-900/10 rounded-md border border-red-200/50">
                        ❌ Permintaan transfer ini telah dibatalkan.
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>

              <!-- Daftar Barang di Sebelah Kanan -->
              <div class="lg:col-span-5">
                <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                  <CardContent class="p-5 space-y-4">
                    <div class="flex items-center gap-2 border-b pb-3 border-zinc-100 dark:border-zinc-800">
                      <Package class="h-4 w-4 text-zinc-400" />
                      <h4 class="text-xs font-bold uppercase tracking-widest text-zinc-500">Rincian Barang</h4>
                    </div>
                    <div class="space-y-2">
                      <div v-for="(item, i) in selectedTR.items" :key="i" class="flex justify-between items-center p-3 rounded-lg border border-zinc-100 dark:border-zinc-800 bg-zinc-50/30 dark:bg-zinc-900/20">
                        <div class="flex items-center gap-3">
                          <Package class="h-4.5 w-4.5 text-zinc-400" />
                          <div>
                            <p class="text-xs font-semibold text-zinc-800 dark:text-zinc-200">{{ item.product?.name || `Produk #${item.productId}` }}</p>
                            <p class="text-[9px] text-muted-foreground font-bold uppercase">SKU: {{ item.product?.sku || '-' }}</p>
                          </div>
                        </div>
                        <div class="text-right shrink-0">
                          <p class="text-xs font-bold">{{ item.qtyRequested }} <span class="text-[9px] text-muted-foreground font-normal">PCS</span></p>
                          <p v-if="item.qtyReceived != null" class="text-[9px] text-emerald-600 font-bold">Diterima: {{ item.qtyReceived }} PCS</p>
                        </div>
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

.dropdown-enter-active, .dropdown-leave-active {
  transition: opacity 0.15s cubic-bezier(0.16, 1, 0.3, 1), transform 0.15s cubic-bezier(0.16, 1, 0.3, 1);
}
.dropdown-enter-from, .dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>