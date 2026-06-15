<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import { useAuthStore } from '@/stores/auth'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Alert from '@/components/ui/Alert.vue'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'
import api from '@/lib/api'
import { Plus, Pencil, Trash2, Loader2, Truck, Phone, Mail, MapPin, FileText, ArrowLeft } from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()
const authStore = useAuthStore()
const isSuperAdmin = computed(() => authStore.isSuperAdmin)

// ─── Selection State ──────────────────────────────────────────────────────────
const selectedIds = ref([])

const isAllSelected = computed(() => {
  const visible = paginatedSuppliers.value
  if (visible.length === 0) return false
  return visible.every(s => selectedIds.value.includes(s.id))
})

function toggleSelectAll() {
  const visible = paginatedSuppliers.value
  if (isAllSelected.value) {
    const visibleIds = visible.map(s => s.id)
    selectedIds.value = selectedIds.value.filter(id => !visibleIds.includes(id))
  } else {
    visible.forEach(s => {
      if (!selectedIds.value.includes(s.id)) selectedIds.value.push(s.id)
    })
  }
}

function toggleSelect(id) {
  const index = selectedIds.value.indexOf(id)
  if (index === -1) selectedIds.value.push(id)
  else selectedIds.value.splice(index, 1)
}

async function bulkDelete() {
  const count = selectedIds.value.length
  if (count === 0) return
  const ok = await confirm({
    title: 'Hapus Supplier Terpilih',
    description: `Apakah Anda yakin ingin menghapus ${count} supplier terpilih secara permanen?`,
    confirmLabel: 'Hapus', cancelLabel: 'Batal',
  })
  if (!ok) return
  loading.value = true
  try {
    await Promise.all(selectedIds.value.map(id => api.delete(`/api/v1/suppliers/${id}`)))
    toast.success(`${count} supplier berhasil dihapus!`)
    selectedIds.value = []
    fetchSuppliers()
  } catch (err) {
    toast.error(err.response?.data?.data?.message || err.response?.data?.message || 'Gagal menghapus beberapa supplier.')
    fetchSuppliers()
  } finally {
    loading.value = false
  }
}

// ─── State ───────────────────────────────────────────────────────────────────
const suppliers = ref([])
const loading = ref(false)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

const filteredSuppliers = computed(() => {
  if (!searchQuery.value) return suppliers.value
  const q = searchQuery.value.toLowerCase()
  return suppliers.value.filter(s =>
    (s.name && s.name.toLowerCase().includes(q)) ||
    (s.phone && s.phone.toLowerCase().includes(q)) ||
    (s.email && s.email.toLowerCase().includes(q))
  )
})

const paginatedSuppliers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredSuppliers.value.slice(start, start + pageSize.value)
})

watch([searchQuery, page, pageSize], () => { selectedIds.value = [] })

// ─── Form State ───────────────────────────────────────────────────────────────
const showForm = ref(false)
const modalMode = ref('create')
const saving = ref(false)
const formError = ref(null)

const emptyForm = () => ({ id: null, name: '', phone: '', email: '', address: '', notes: '' })
const form = ref(emptyForm())

const formErrors = ref({ name: '' })

// ─── Fetch ────────────────────────────────────────────────────────────────────
async function fetchSuppliers() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/suppliers')
    suppliers.value = Array.isArray(res.data) ? res.data : (res.data?.data || [])
  } catch (err) {
    toast.error(err.response?.data?.data?.message || err.response?.data?.message || 'Gagal memuat data supplier.')
  } finally {
    loading.value = false
  }
}

// ─── Create / Edit ────────────────────────────────────────────────────────────
function openCreate() {
  form.value = emptyForm()
  formErrors.value = { name: '' }
  formError.value = null
  modalMode.value = 'create'
  showForm.value = true
}

function openEdit(s) {
  form.value = { ...s }
  formErrors.value = { name: '' }
  formError.value = null
  modalMode.value = 'edit'
  showForm.value = true
}

function closeForm() {
  showForm.value = false
}

function validateForm() {
  formErrors.value = { name: '' }
  if (!form.value.name?.trim()) {
    formErrors.value.name = 'Nama supplier wajib diisi'
    return false
  }
  if (form.value.name.trim().length < 2) {
    formErrors.value.name = 'Nama supplier minimal 2 karakter'
    return false
  }
  return true
}

async function saveSupplier() {
  formError.value = null
  if (!validateForm()) return

  saving.value = true
  try {
    if (modalMode.value === 'create') {
      await api.post('/api/v1/suppliers', form.value)
      toast.success('Supplier berhasil ditambahkan!')
    } else {
      await api.put(`/api/v1/suppliers/${form.value.id}`, form.value)
      toast.success('Supplier berhasil diperbarui!')
    }
    showForm.value = false
    fetchSuppliers()
  } catch (err) {
    formError.value = err.response?.data?.data?.message || err.response?.data?.message || 'Gagal menyimpan supplier.'
  } finally {
    saving.value = false
  }
}

// ─── Delete ───────────────────────────────────────────────────────────────────
const deleteModal = ref({ show: false, supplier: null, confirmText: '' })
const deleting = ref(false)

function doDelete(s) {
  deleteModal.value = { show: true, supplier: s, confirmText: '' }
}

function closeDeleteModal() {
  deleteModal.value.show = false
  setTimeout(() => { deleteModal.value.supplier = null; deleteModal.value.confirmText = '' }, 300)
}

async function confirmDelete() {
  if (deleteModal.value.confirmText !== deleteModal.value.supplier?.name) return
  deleting.value = true
  try {
    await api.delete(`/api/v1/suppliers/${deleteModal.value.supplier.id}`)
    toast.success('Supplier berhasil dihapus!')
    fetchSuppliers()
    closeDeleteModal()
  } catch (err) {
    toast.error(err.response?.data?.data?.message || err.response?.data?.message || 'Gagal menghapus supplier.')
    closeDeleteModal()
  } finally {
    deleting.value = false
  }
}

// ─── Lifecycle ────────────────────────────────────────────────────────────────
onMounted(fetchSuppliers)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <Transition name="fade" mode="out-in">

        <!-- ─── Main Table View ─── -->
        <div v-if="!showForm" key="table-view" class="flex flex-col gap-6">
          <!-- Page Header -->
          <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
            <div>
              <h1 class="text-2xl font-bold tracking-tight">Agen/Distrbutor</h1>
              <p class="text-muted-foreground text-sm mt-1">Kelola data Agen dan distributor Anda.</p>
            </div>
            <div class="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto">
              <div class="w-full sm:w-72">
                <DataTableSearch v-model="searchQuery" placeholder="Cari Agen/Distributor..." />
              </div>
              <Button
                v-if="can('supplier.store') && !isSuperAdmin"
                @click="openCreate"
                size="sm"
                class="flex-1 sm:flex-none flex items-center justify-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground"
              >
                <Plus class="h-4 w-4" />
                <span>Tambah Supplier</span>
              </Button>
            </div>
          </div>

          <!-- Table Card -->
          <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
            <CardContent class="p-0">
              <div v-if="loading" class="flex flex-col items-center justify-center py-24 gap-3">
                <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
                <p class="text-xs text-muted-foreground">Memuat data...</p>
              </div>

              <div v-else-if="filteredSuppliers.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
                <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
                  <Truck class="h-7 w-7 opacity-40" />
                </div>
                <p class="text-sm font-medium">Belum ada data supplier</p>
                <p class="text-xs text-muted-foreground/70 mt-1">Mulai dengan menambahkan supplier pertama Anda.</p>
                <Button v-if="can('supplier.store') && !isSuperAdmin && !searchQuery" size="sm" class="mt-4" @click="openCreate">
                  <Plus class="h-3.5 w-3.5 mr-1.5" />
                  Tambah Supplier
                </Button>
              </div>

              <div v-else>
                <!-- Mobile List -->
                <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
                  <div
                    v-for="s in paginatedSuppliers"
                    :key="'mobile-' + s.id"
                    class="p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
                  >
                    <div class="flex items-start justify-between gap-3">
                      <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-lg bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-sm font-bold text-zinc-500 shrink-0 border border-zinc-200 dark:border-zinc-800/50">
                          {{ s.name?.charAt(0).toUpperCase() }}
                        </div>
                        <div>
                          <h4 class="font-medium text-sm text-zinc-900 dark:text-zinc-100">{{ s.name }}</h4>
                          <div class="flex flex-col gap-0.5 mt-0.5">
                            <div v-if="s.phone" class="flex items-center gap-1 text-[10px] text-zinc-500">
                              <Phone class="h-2.5 w-2.5" /> {{ s.phone }}
                            </div>
                            <div v-if="s.email" class="flex items-center gap-1 text-[10px] text-zinc-500">
                              <Mail class="h-2.5 w-2.5" /> {{ s.email }}
                            </div>
                          </div>
                        </div>
                      </div>
                      <div class="flex items-center gap-1 shrink-0">
                        <Button v-if="can('supplier.update') && !isSuperAdmin" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 bg-zinc-50 dark:bg-zinc-800/50" @click="openEdit(s)">
                          <Pencil class="h-3.5 w-3.5" />
                        </Button>
                        <Button v-if="can('supplier.delete') && !isSuperAdmin" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-destructive bg-zinc-50 dark:bg-zinc-800/50" @click="doDelete(s)">
                          <Trash2 class="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </div>
                    <div v-if="s.address" class="text-[11px] text-zinc-500 bg-zinc-50 dark:bg-zinc-900/50 p-2 rounded border border-zinc-100 dark:border-zinc-800/60 flex items-start gap-1.5">
                      <MapPin class="h-3 w-3 shrink-0 mt-0.5" /> {{ s.address }}
                    </div>
                  </div>
                </div>

                <!-- Desktop Table -->
                <div class="hidden md:block overflow-x-auto relative">
                  <!-- Selection Banner -->
                  <Transition name="fade">
                    <div v-if="selectedIds.length > 0" class="flex items-center justify-between px-5 py-3 bg-primary/5 dark:bg-primary/10 border-b border-border">
                      <div class="flex items-center gap-2">
                        <span class="text-xs font-semibold text-primary px-2 py-0.5 rounded bg-primary/10">{{ selectedIds.length }} Terpilih</span>
                        <span class="text-xs text-muted-foreground">Baris terpilih dalam tabel ini.</span>
                      </div>
                      <Button v-if="can('supplier.delete') && !isSuperAdmin" size="sm" variant="destructive" class="h-8 text-xs gap-1" @click="bulkDelete">
                        <Trash2 class="h-3.5 w-3.5" /> Hapus
                      </Button>
                    </div>
                  </Transition>

                  <table class="w-full text-sm">
                    <thead>
                      <tr class="border-b border-zinc-100 dark:border-zinc-800 bg-muted/40">
                        <th class="w-12 pl-5 py-3 text-left">
                          <input type="checkbox" class="rounded border-zinc-300 dark:border-zinc-700 text-primary focus:ring-primary h-4 w-4 cursor-pointer" :checked="isAllSelected" @change="toggleSelectAll" />
                        </th>
                        <th class="pl-2 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Nama Supplier</th>
                        <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Kontak</th>
                        <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Alamat</th>
                        <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Catatan</th>
                        <th class="pr-5 py-3 text-right"></th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr
                        v-for="s in paginatedSuppliers"
                        :key="s.id"
                        class="group table-lift-row border-b border-zinc-100 dark:border-zinc-800/60 odd:bg-background even:bg-zinc-50/40 dark:even:bg-zinc-900/10 hover:bg-zinc-100/60 dark:hover:bg-zinc-800/40 transition-colors cursor-pointer"
                        @click="can('supplier.update') && !isSuperAdmin && openEdit(s)"
                      >
                        <td class="w-12 pl-5 py-3" @click.stop>
                          <input type="checkbox" class="rounded border-zinc-300 dark:border-zinc-700 text-primary focus:ring-primary h-4 w-4 cursor-pointer" :checked="selectedIds.includes(s.id)" @change="toggleSelect(s.id)" />
                        </td>
                        <td class="pl-2 py-3">
                          <div class="flex items-center gap-3">
                            <div class="w-8 h-8 rounded-lg bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-xs font-bold text-zinc-500 shrink-0 border border-zinc-200 dark:border-zinc-700/50">
                              {{ s.name?.charAt(0).toUpperCase() }}
                            </div>
                            <span class="font-medium text-zinc-900 dark:text-zinc-100">{{ s.name }}</span>
                          </div>
                        </td>
                        <td class="py-3">
                          <div class="flex flex-col gap-0.5">
                            <div v-if="s.phone" class="text-xs flex items-center gap-1.5"><Phone class="h-3 w-3 text-muted-foreground" /> {{ s.phone }}</div>
                            <div v-if="s.email" class="text-xs flex items-center gap-1.5 text-muted-foreground italic"><Mail class="h-3 w-3" /> {{ s.email }}</div>
                            <span v-if="!s.phone && !s.email" class="text-xs text-zinc-300 dark:text-zinc-600">—</span>
                          </div>
                        </td>
                        <td class="py-3 max-w-[200px] truncate text-xs text-zinc-500">{{ s.address || '-' }}</td>
                        <td class="py-3 max-w-[150px] truncate text-[11px] text-zinc-400 italic">{{ s.notes || '' }}</td>
                        <td class="pr-5 py-3 text-right" @click.stop>
                          <div class="flex justify-end gap-1">
                            <Button v-if="can('supplier.delete') && !isSuperAdmin" variant="ghost" size="icon" class="h-7 w-7 text-zinc-400 hover:text-destructive hover:bg-destructive/10 rounded-md opacity-0 group-hover:opacity-100 transition-opacity duration-200" @click="doDelete(s)">
                              <Trash2 class="h-3.5 w-3.5" />
                            </Button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <DataTablePagination
                v-if="filteredSuppliers.length > 0 && !loading"
                :page="page" :page-size="pageSize" :total="filteredSuppliers.length"
                @update:page="page = $event" @update:page-size="pageSize = $event; page = 1"
              />
            </CardContent>
          </Card>
        </div>

        <!-- ─── Inline Form View ─── -->
        <div v-else key="form-view" class="flex flex-col gap-6">
          <!-- Form Header -->
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-zinc-200 dark:border-zinc-800 pb-4">
            <div class="flex items-center gap-3">
              <Button variant="outline" size="icon" @click="closeForm" :disabled="saving" class="h-9 w-9 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors">
                <ArrowLeft class="h-4 w-4" />
              </Button>
              <div>
                <h2 class="text-xl font-bold tracking-tight flex items-center gap-2">
                  <span>{{ modalMode === 'create' ? 'Tambah Supplier' : 'Edit Supplier' }}</span>
                  <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-primary/10 text-primary border border-primary/20 uppercase tracking-wider">
                    {{ modalMode === 'create' ? 'Baru' : 'Modifikasi' }}
                  </span>
                </h2>
                <p class="text-xs text-muted-foreground mt-0.5">
                  {{ modalMode === 'create' ? 'Lengkapi detail untuk menambahkan supplier baru.' : 'Perbarui informasi supplier.' }}
                </p>
              </div>
            </div>
            <div class="flex items-center gap-3 w-full sm:w-auto">
              <Button variant="outline" @click="closeForm" :disabled="saving" class="flex-1 sm:flex-none">Batal</Button>
              <Button @click="saveSupplier" :disabled="saving" class="flex-1 sm:flex-none bg-primary text-primary-foreground hover:bg-primary/95 shadow-md shadow-primary/20">
                <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
                <span>{{ modalMode === 'create' ? 'Simpan Supplier' : 'Perbarui' }}</span>
              </Button>
            </div>
          </div>

          <!-- Form Error -->
          <Alert v-if="formError" variant="destructive">
            <p class="text-sm font-medium">{{ formError }}</p>
          </Alert>

          <!-- Form Grid -->
          <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">

            <!-- LEFT: Preview Card -->
            <div class="lg:col-span-4 space-y-4">
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <div class="flex items-center justify-between border-b pb-3 border-zinc-100 dark:border-zinc-800">
                    <div>
                      <Label class="text-sm font-semibold text-zinc-950 dark:text-zinc-50">Pratinjau Supplier</Label>
                      <p class="text-[10px] text-muted-foreground mt-0.5">Tampilan data supplier secara visual.</p>
                    </div>
                  </div>
                  <div class="flex flex-col items-center justify-center py-6 gap-4">
                    <div class="w-20 h-20 rounded-2xl bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-3xl font-bold text-zinc-600 dark:text-zinc-300 shadow-lg border-2 border-zinc-200 dark:border-zinc-700 select-none transition-all duration-300">
                      {{ (form.name || 'S').charAt(0).toUpperCase() }}
                    </div>
                    <div class="text-center space-y-1">
                      <p class="font-semibold text-base text-zinc-900 dark:text-zinc-100">{{ form.name || 'Nama Supplier' }}</p>
                      <div v-if="form.phone" class="flex items-center justify-center gap-1 text-xs text-zinc-500">
                        <Phone class="h-3 w-3" /> {{ form.phone }}
                      </div>
                      <div v-if="form.email" class="flex items-center justify-center gap-1 text-xs text-zinc-500">
                        <Mail class="h-3 w-3" /> {{ form.email }}
                      </div>
                    </div>
                  </div>
                  <div class="grid grid-cols-2 gap-3 pt-2 border-t border-zinc-100 dark:border-zinc-800">
                    <div class="rounded-lg bg-zinc-50 dark:bg-zinc-900/30 border border-zinc-100 dark:border-zinc-800 p-3 text-center">
                      <p class="text-[10px] font-semibold text-muted-foreground uppercase tracking-wide">Karakter</p>
                      <p class="text-lg font-bold text-zinc-800 dark:text-zinc-200 mt-1">{{ form.name.length }}<span class="text-xs font-normal text-muted-foreground">/100</span></p>
                    </div>
                    <div class="rounded-lg bg-zinc-50 dark:bg-zinc-900/30 border border-zinc-100 dark:border-zinc-800 p-3 text-center">
                      <p class="text-[10px] font-semibold text-muted-foreground uppercase tracking-wide">Kontak</p>
                      <p class="text-lg font-bold text-zinc-800 dark:text-zinc-200 mt-1">{{ (form.phone || form.email) ? 'Ada' : 'Kosong' }}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>

            <!-- RIGHT: Form Fields -->
            <div class="lg:col-span-8 space-y-6">

              <!-- Card 1: Informasi Utama -->
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <h3 class="text-sm font-semibold text-zinc-950 dark:text-zinc-50 border-b pb-2 border-zinc-100 dark:border-zinc-800 flex items-center gap-2">
                    <Truck class="h-4 w-4 text-primary" />
                    <span>Informasi Utama</span>
                  </h3>

                  <div class="space-y-1.5">
                    <Label for="s-name" class="text-xs font-semibold">Nama Supplier <span class="text-destructive">*</span></Label>
                    <Input
                      id="s-name"
                      v-model="form.name"
                      placeholder="Contoh: PT Sumber Makmur"
                      :disabled="saving"
                      class="h-10 rounded-lg"
                      :class="{ 'border-destructive ring-destructive/20': formErrors.name }"
                    />
                    <p v-if="formErrors.name" class="text-xs text-destructive font-semibold">{{ formErrors.name }}</p>
                  </div>

                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div class="space-y-1.5">
                      <Label for="s-phone" class="text-xs font-semibold">
                        Nomor Telepon <span class="text-muted-foreground text-[10px] font-normal">(opsional)</span>
                      </Label>
                      <div class="relative">
                        <Phone class="absolute left-3 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-muted-foreground" />
                        <Input id="s-phone" v-model="form.phone" placeholder="021-xxxx atau 08xx" :disabled="saving" class="h-10 rounded-lg pl-9" />
                      </div>
                    </div>
                    <div class="space-y-1.5">
                      <Label for="s-email" class="text-xs font-semibold">
                        Alamat Email <span class="text-muted-foreground text-[10px] font-normal">(opsional)</span>
                      </Label>
                      <div class="relative">
                        <Mail class="absolute left-3 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-muted-foreground" />
                        <Input id="s-email" v-model="form.email" type="email" placeholder="supplier@example.com" :disabled="saving" class="h-10 rounded-lg pl-9" />
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <!-- Card 2: Alamat & Catatan -->
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <h3 class="text-sm font-semibold text-zinc-950 dark:text-zinc-50 border-b pb-2 border-zinc-100 dark:border-zinc-800 flex items-center gap-2">
                    <MapPin class="h-4 w-4 text-primary" />
                    <span>Alamat & Catatan</span>
                  </h3>

                  <div class="space-y-1.5">
                    <Label for="s-address" class="text-xs font-semibold">
                      Alamat Lengkap <span class="text-muted-foreground text-[10px] font-normal">(opsional)</span>
                    </Label>
                    <textarea
                      id="s-address"
                      v-model="form.address"
                      rows="3"
                      :disabled="saving"
                      placeholder="Alamat kantor atau gudang supplier..."
                      class="flex min-h-[80px] w-full rounded-lg border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 resize-none transition-colors"
                    />
                  </div>

                  <div class="space-y-1.5">
                    <Label for="s-notes" class="text-xs font-semibold">
                      Catatan Tambahan <span class="text-muted-foreground text-[10px] font-normal">(opsional)</span>
                    </Label>
                    <div class="relative">
                      <FileText class="absolute left-3 top-3 h-3.5 w-3.5 text-muted-foreground" />
                      <textarea
                        id="s-notes"
                        v-model="form.notes"
                        rows="2"
                        :disabled="saving"
                        placeholder="Syarat pembayaran, jadwal pengiriman, dll..."
                        class="flex min-h-[60px] w-full rounded-lg border border-input bg-background pl-9 pr-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 resize-none transition-colors"
                      />
                    </div>
                  </div>
                </CardContent>
              </Card>

            </div>
          </div>
        </div>

      </Transition>
    </div>

    <!-- ─── Delete Confirmation Modal ─── -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[60] bg-black/40 backdrop-blur-sm" @click="closeDeleteModal" />
      </Transition>
      <Transition name="scale">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none">
          <div class="relative bg-card rounded-xl shadow-2xl w-full max-w-md overflow-hidden border border-border pointer-events-auto">
            <div class="p-6">
              <h3 class="text-lg font-semibold text-destructive flex items-center gap-2">
                <Trash2 class="h-5 w-5" />
                Hapus Supplier
              </h3>
              <p class="text-sm text-muted-foreground mt-2">
                Tindakan ini tidak dapat dibatalkan. Hapus supplier
                <span class="font-semibold text-foreground">{{ deleteModal.supplier?.name }}</span> secara permanen.
              </p>
              <div class="mt-4">
                <Label class="text-sm font-medium">
                  Ketik <span class="font-bold bg-muted px-1.5 py-0.5 rounded text-foreground">{{ deleteModal.supplier?.name }}</span> untuk konfirmasi.
                </Label>
                <Input v-model="deleteModal.confirmText" class="mt-2" placeholder="Masukkan nama supplier" @keyup.enter="confirmDelete" />
              </div>
            </div>
            <div class="flex items-center justify-end gap-3 px-6 py-4 bg-muted/30 border-t">
              <Button variant="outline" @click="closeDeleteModal">Batal</Button>
              <Button variant="destructive" :disabled="deleteModal.confirmText !== deleteModal.supplier?.name || deleting" @click="confirmDelete">
                <Loader2 v-if="deleting" class="h-4 w-4 mr-2 animate-spin" />
                Hapus Sekarang
              </Button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.scale-enter-active, .scale-leave-active { transition: opacity 0.25s ease, transform 0.25s cubic-bezier(0.16, 1, 0.3, 1); }
.scale-enter-from, .scale-leave-to { opacity: 0; transform: scale(0.96); }
.table-lift-row { transition: box-shadow 0.15s ease, background-color 0.15s ease; }
.table-lift-row:hover { box-shadow: 0 2px 8px 0 rgba(0,0,0,0.06); }
</style>
