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
import api from '@/lib/api'
import { Plus, Pencil, Trash2, Loader2, X, Truck, Phone, Mail } from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()

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

// ─── Form State ───────────────────────────────────────────────────────────────
const showDrawer = ref(false)
const modalMode = ref('create')
const saving = ref(false)
const formError = ref(null)

const emptyForm = () => ({ 
  id: null, 
  name: '', 
  phone: '', 
  email: '', 
  address: '', 
  notes: '' 
})

const form = ref(emptyForm())

// ─── Actions ──────────────────────────────────────────────────────────────────
async function fetchSuppliers() {
  loading.value = true
  try {
    // Admin: /admin/all returns plain List; Partner: / returns plain List
    const url = '/api/v1/suppliers'
    const res = await api.get(url)
    suppliers.value = Array.isArray(res.data) ? res.data : (res.data?.data || [])
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal memuat data supplier.')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = emptyForm()
  formError.value = null
  modalMode.value = 'create'
  showDrawer.value = true
}

function openEdit(s) {
  form.value = { ...s }
  formError.value = null
  modalMode.value = 'edit'
  showDrawer.value = true
}

async function saveSupplier() {
  if (!form.value.name) {
    formError.value = 'Nama supplier wajib diisi.'
    return
  }
  
  saving.value = true
  formError.value = null
  try {
    if (modalMode.value === 'create') {
      await api.post('/api/v1/suppliers', form.value)
      toast.success('Supplier berhasil ditambahkan!')
    } else {
      await api.put(`/api/v1/suppliers/${form.value.id}`, form.value)
      toast.success('Supplier berhasil diperbarui!')
    }
    showDrawer.value = false
    fetchSuppliers()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Gagal menyimpan supplier.'
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
    toast.error(err.response?.data?.message || 'Gagal menghapus supplier.')
  } finally {
    deleting.value = false
  }
}

onMounted(fetchSuppliers)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <!-- Header -->
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Supplier</h1>
          <p class="text-muted-foreground text-sm mt-1">Kelola data supplier dan pemasok Anda.</p>
        </div>
        <div class="flex items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-72">
            <DataTableSearch v-model="searchQuery" placeholder="Cari supplier..." />
          </div>
          <Button v-if="can('supplier.store')" @click="openCreate" size="sm" class="flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
            <Plus class="h-4 w-4" />
            <span>Tambah</span>
          </Button>
        </div>
      </div>

      <!-- Table Card -->
      <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm overflow-hidden">
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-24">
            <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
          </div>

          <div v-else-if="filteredSuppliers.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
            <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
              <Truck class="h-7 w-7 opacity-40" />
            </div>
            <p class="text-sm font-medium">Belum ada data supplier.</p>
            <Button v-if="can('supplier.store') && !searchQuery" size="sm" class="mt-4" @click="openCreate">
              <Plus class="h-3.5 w-3.5 mr-1.5" />
              Tambah Supplier
            </Button>
          </div>

          <div v-else>
            <!-- Mobile List -->
            <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
              <div v-for="s in paginatedSuppliers" :key="s.id" class="p-4 flex flex-col gap-3">
                <div class="flex items-start justify-between">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-lg bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-500 border border-zinc-200 dark:border-zinc-800/50">
                      {{ s.name?.charAt(0).toUpperCase() }}
                    </div>
                    <div>
                      <h4 class="font-medium text-sm text-zinc-900 dark:text-zinc-100">{{ s.name }}</h4>
                      <div class="flex flex-col gap-1 mt-1">
                        <div v-if="s.phone" class="flex items-center gap-1.5 text-[10px] text-muted-foreground">
                          <Phone class="h-3 w-3" /> {{ s.phone }}
                        </div>
                        <div v-if="s.email" class="flex items-center gap-1.5 text-[10px] text-muted-foreground">
                          <Mail class="h-3 w-3" /> {{ s.email }}
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="flex gap-1 shrink-0">
                    <Button v-if="can('supplier.update')" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400" @click="openEdit(s)">
                      <Pencil class="h-3.5 w-3.5" />
                    </Button>
                    <Button v-if="can('supplier.delete')" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-destructive" @click="doDelete(s)">
                      <Trash2 class="h-3.5 w-3.5" />
                    </Button>
                  </div>
                </div>
                <div v-if="s.address" class="text-[11px] text-zinc-500 bg-zinc-50 dark:bg-zinc-900/50 p-2 rounded border border-zinc-100 dark:border-zinc-800/60">
                  {{ s.address }}
                </div>
              </div>
            </div>

            <!-- Desktop Table -->
            <div class="hidden md:block overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="border-b border-zinc-100 dark:border-zinc-800">
                    <th class="pl-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Nama Supplier</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Kontak</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Alamat</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Catatan</th>
                    <th class="pr-5 py-3 text-right"></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="s in paginatedSuppliers" :key="s.id" class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors">
                    <td class="pl-5 py-3">
                      <div class="flex items-center gap-3">
                        <div class="w-8 h-8 rounded bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-xs font-bold text-zinc-500 shrink-0">
                          {{ s.name?.charAt(0).toUpperCase() }}
                        </div>
                        <span class="font-medium text-zinc-900 dark:text-zinc-100">{{ s.name }}</span>
                      </div>
                    </td>
                    <td class="py-3">
                      <div class="flex flex-col gap-0.5">
                        <div v-if="s.phone" class="text-xs flex items-center gap-1.5"><Phone class="h-3 w-3 text-muted-foreground" /> {{ s.phone }}</div>
                        <div v-if="s.email" class="text-xs flex items-center gap-1.5 text-muted-foreground italic"><Mail class="h-3 w-3" /> {{ s.email }}</div>
                        <span v-if="!s.phone && !s.email" class="text-xs text-zinc-300">—</span>
                      </div>
                    </td>
                    <td class="py-3 max-w-[200px] truncate text-xs text-zinc-500">
                      {{ s.address || '-' }}
                    </td>
                    <td class="py-3 max-w-[150px] truncate text-[11px] text-zinc-400 italic">
                      {{ s.notes || '' }}
                    </td>
                    <td class="pr-5 py-3 text-right">
                      <div class="flex justify-end gap-1">
                        <Button v-if="can('supplier.update')" variant="ghost" size="icon" class="h-7 w-7 text-zinc-400 hover:text-zinc-700" @click="openEdit(s)">
                          <Pencil class="h-3.5 w-3.5" />
                        </Button>
                        <Button v-if="can('supplier.delete')" variant="ghost" size="icon" class="h-7 w-7 text-zinc-400 hover:text-destructive" @click="doDelete(s)">
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
            :page="page" 
            :page-size="pageSize" 
            :total="filteredSuppliers.length" 
            @update:page="page = $event" 
            @update:page-size="pageSize = $event; page = 1" 
          />
        </CardContent>
      </Card>
    </div>

    <!-- Right Side Drawer -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showDrawer" class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm" @click="showDrawer = false" />
      </Transition>
      <Transition name="slide-right">
        <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[420px] bg-card shadow-2xl sm:border-l overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
            <div>
              <h3 class="font-semibold text-base">{{ modalMode === 'create' ? 'Tambah Supplier' : 'Edit Supplier' }}</h3>
              <p class="text-xs text-muted-foreground mt-0.5">Lengkapi data pemasok barang Anda.</p>
            </div>
            <Button variant="ghost" size="icon" @click="showDrawer = false">
              <X class="h-4 w-4" />
            </Button>
          </div>
          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-4">
            <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>
            <div class="space-y-1.5">
              <Label for="name">Nama Supplier <span class="text-destructive">*</span></Label>
              <Input id="name" v-model="form.name" placeholder="Contoh: PT Sumber Rejeki" :disabled="saving" />
            </div>
            <div class="space-y-1.5">
              <Label for="phone">Nomor Telepon</Label>
              <Input id="phone" v-model="form.phone" placeholder="021-xxxx atau 08xx" :disabled="saving" />
            </div>
            <div class="space-y-1.5">
              <Label for="email">Alamat Email</Label>
              <Input id="email" v-model="form.email" type="email" placeholder="supplier@example.com" :disabled="saving" />
            </div>
            <div class="space-y-1.5">
              <Label for="address">Alamat Lengkap</Label>
              <textarea 
                id="address" 
                v-model="form.address" 
                rows="3" 
                class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring" 
                placeholder="Alamat kantor atau gudang supplier..."
                :disabled="saving"
              />
            </div>
            <div class="space-y-1.5">
              <Label for="notes">Catatan Tambahan</Label>
              <textarea 
                id="notes" 
                v-model="form.notes" 
                rows="2" 
                class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring" 
                placeholder="Catatan khusus, syarat pembayaran, dll..."
                :disabled="saving"
              />
            </div>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4 border-t bg-muted/30">
            <Button variant="outline" @click="showDrawer = false" :disabled="saving">Batal</Button>
            <Button @click="saveSupplier" :disabled="saving" class="bg-primary hover:bg-primary/90">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              {{ modalMode === 'create' ? 'Simpan Supplier' : 'Perbarui' }}
            </Button>
          </div>
        </div>
      </Transition>

      <!-- Delete Modal -->
      <Transition name="fade">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[60] bg-black/40 backdrop-blur-sm" @click="closeDeleteModal" />
      </Transition>
      <Transition name="scale">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none">
          <div class="bg-card rounded-xl shadow-2xl w-full max-w-md border border-border pointer-events-auto p-6 overflow-hidden">
            <div class="flex items-center gap-3 text-destructive mb-3">
              <div class="w-10 h-10 rounded-full bg-destructive/10 flex items-center justify-center">
                <Trash2 class="h-5 w-5" />
              </div>
              <h3 class="text-lg font-bold">Hapus Supplier</h3>
            </div>
            <p class="text-sm text-muted-foreground mb-4 leading-relaxed">
              Apakah Anda yakin ingin menghapus supplier <span class="font-bold text-foreground">{{ deleteModal.supplier?.name }}</span>? Data yang sudah dihapus tidak dapat dikembalikan.
            </p>
            <div class="space-y-2 mb-6">
              <Label class="text-xs">Ketik nama supplier untuk konfirmasi:</Label>
              <Input v-model="deleteModal.confirmText" placeholder="Nama supplier" class="border-destructive/30 focus:ring-destructive/20" />
            </div>
            <div class="flex justify-end gap-3">
              <Button variant="outline" @click="closeDeleteModal" :disabled="deleting">Batal</Button>
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
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-right-enter-active, .slide-right-leave-active { transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
.slide-right-enter-from, .slide-right-leave-to { transform: translateX(100%); }
.scale-enter-active, .scale-leave-active { transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1); }
.scale-enter-from, .scale-leave-to { opacity: 0; transform: scale(0.95); }
</style>
