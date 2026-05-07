<script setup>
import { ref, computed, onMounted, watch } from 'vue'
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
import { Plus, Pencil, Trash2, Loader2, X, Truck, Phone, Mail } from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()

const suppliers = ref([])
const loading = ref(false)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

const MOCK = [
  { id: '1', name: 'PT Textile Jaya', phone: '021-5551234', email: 'info@textilejaya.com', address: 'Jl. Industri No. 10, Bandung', notes: 'Supplier kain utama' },
  { id: '2', name: 'CV Sepatu Makmur', phone: '031-7778899', email: 'order@sepatumakmur.id', address: 'Surabaya', notes: '' },
  { id: '3', name: 'UD Aksesoris Lengkap', phone: '0812-3456-7890', email: null, address: 'Jakarta Barat', notes: 'COD only' },
]

const filteredSuppliers = computed(() => {
  if (!searchQuery.value) return suppliers.value
  const q = searchQuery.value.toLowerCase()
  return suppliers.value.filter(s => s.name.toLowerCase().includes(q) || s.phone?.toLowerCase().includes(q) || s.email?.toLowerCase().includes(q))
})
const paginatedSuppliers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredSuppliers.value.slice(start, start + pageSize.value)
})

const showDrawer = ref(false)
const modalMode = ref('create')
const saving = ref(false)
const formError = ref(null)
const emptyForm = () => ({ id: null, name: '', phone: '', email: '', address: '', notes: '' })
const form = ref(emptyForm())

const deleteModal = ref({ show: false, supplier: null, confirmText: '' })
const deleting = ref(false)

function fetchSuppliers() { loading.value = true; setTimeout(() => { suppliers.value = [...MOCK]; loading.value = false }, 300) }
function openCreate() { form.value = emptyForm(); formError.value = null; modalMode.value = 'create'; showDrawer.value = true }
function openEdit(s) { form.value = { ...s }; formError.value = null; modalMode.value = 'edit'; showDrawer.value = true }

function saveSupplier() {
  if (!form.value.name) { formError.value = 'Nama supplier wajib diisi.'; return }
  saving.value = true
  setTimeout(() => {
    if (modalMode.value === 'create') {
      suppliers.value.push({ ...form.value, id: 's-' + Date.now() })
      toast.success('Supplier berhasil ditambahkan!')
    } else {
      const idx = suppliers.value.findIndex(s => s.id === form.value.id)
      if (idx !== -1) suppliers.value[idx] = { ...form.value }
      toast.success('Supplier berhasil diperbarui!')
    }
    showDrawer.value = false; saving.value = false
  }, 400)
}

function doDelete(s) { deleteModal.value = { show: true, supplier: s, confirmText: '' } }
function closeDeleteModal() { deleteModal.value.show = false }
function confirmDelete() {
  if (deleteModal.value.confirmText !== deleteModal.value.supplier?.name) return
  deleting.value = true
  setTimeout(() => {
    suppliers.value = suppliers.value.filter(s => s.id !== deleteModal.value.supplier.id)
    toast.success('Supplier berhasil dihapus!')
    closeDeleteModal(); deleting.value = false
  }, 400)
}

onMounted(fetchSuppliers)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Supplier</h1>
          <p class="text-muted-foreground text-sm mt-1">Kelola data supplier dan pemasok.</p>
        </div>
        <div class="flex items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-72"><DataTableSearch v-model="searchQuery" placeholder="Cari supplier..." /></div>
          <Button v-if="can('supplier.store')" @click="openCreate" size="sm" class="flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
            <Plus class="h-4 w-4" /><span>Tambah</span>
          </Button>
        </div>
      </div>

      <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm overflow-hidden">
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-24"><Loader2 class="h-7 w-7 animate-spin text-primary/50" /></div>
          <div v-else-if="filteredSuppliers.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
            <Truck class="h-10 w-10 mb-3 opacity-20" /><p class="text-sm">Belum ada data supplier.</p>
          </div>
          <div v-else>
            <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
              <div v-for="s in paginatedSuppliers" :key="s.id" class="p-4 flex flex-col gap-2">
                <div class="flex items-start justify-between">
                  <div>
                    <h4 class="font-medium text-sm">{{ s.name }}</h4>
                    <div v-if="s.phone" class="flex items-center gap-1.5 text-xs text-muted-foreground mt-1"><Phone class="h-3 w-3" />{{ s.phone }}</div>
                  </div>
                  <div class="flex gap-1">
                    <Button variant="ghost" size="icon" class="h-8 w-8" @click="openEdit(s)"><Pencil class="h-3.5 w-3.5" /></Button>
                    <Button variant="ghost" size="icon" class="h-8 w-8 text-destructive" @click="doDelete(s)"><Trash2 class="h-3.5 w-3.5" /></Button>
                  </div>
                </div>
              </div>
            </div>
            <div class="hidden md:block overflow-x-auto">
              <table class="w-full text-sm">
                <thead><tr class="border-b border-zinc-100 dark:border-zinc-800">
                  <th class="pl-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Nama</th>
                  <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Telepon</th>
                  <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Email</th>
                  <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Alamat</th>
                  <th class="pr-5 py-3 text-right"></th>
                </tr></thead>
                <tbody>
                  <tr v-for="s in paginatedSuppliers" :key="s.id" class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40">
                    <td class="pl-5 py-3 font-medium">{{ s.name }}</td>
                    <td class="py-3 text-xs text-muted-foreground">{{ s.phone || '-' }}</td>
                    <td class="py-3 text-xs text-muted-foreground">{{ s.email || '-' }}</td>
                    <td class="py-3 text-xs text-muted-foreground max-w-[200px] truncate">{{ s.address || '-' }}</td>
                    <td class="pr-5 py-3 text-right">
                      <div class="flex justify-end gap-1">
                        <Button v-if="can('supplier.update')" variant="ghost" size="icon" class="h-7 w-7" @click="openEdit(s)"><Pencil class="h-3.5 w-3.5" /></Button>
                        <Button v-if="can('supplier.delete')" variant="ghost" size="icon" class="h-7 w-7 text-destructive" @click="doDelete(s)"><Trash2 class="h-3.5 w-3.5" /></Button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <DataTablePagination v-if="filteredSuppliers.length > 0 && !loading" :page="page" :page-size="pageSize" :total="filteredSuppliers.length" @update:page="page = $event" @update:page-size="pageSize = $event; page = 1" />
        </CardContent>
      </Card>
    </div>

    <Teleport to="body">
      <Transition name="fade"><div v-if="showDrawer" class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm" @click="showDrawer = false" /></Transition>
      <Transition name="slide-right">
        <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[420px] bg-card shadow-2xl sm:border-l overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
            <div><h3 class="font-semibold text-base">{{ modalMode === 'create' ? 'Tambah Supplier' : 'Edit Supplier' }}</h3></div>
            <Button variant="ghost" size="icon" @click="showDrawer = false"><X class="h-4 w-4" /></Button>
          </div>
          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-4">
            <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>
            <div class="space-y-1.5"><Label>Nama Supplier *</Label><Input v-model="form.name" placeholder="PT Example" /></div>
            <div class="space-y-1.5"><Label>Telepon</Label><Input v-model="form.phone" placeholder="021-xxx" /></div>
            <div class="space-y-1.5"><Label>Email</Label><Input v-model="form.email" type="email" placeholder="email@example.com" /></div>
            <div class="space-y-1.5"><Label>Alamat</Label><textarea v-model="form.address" rows="3" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm" placeholder="Alamat lengkap..." /></div>
            <div class="space-y-1.5"><Label>Catatan</Label><textarea v-model="form.notes" rows="2" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm" placeholder="Catatan tambahan..." /></div>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4 border-t bg-muted/30">
            <Button variant="outline" @click="showDrawer = false">Batal</Button>
            <Button @click="saveSupplier" :disabled="saving"><Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />Simpan</Button>
          </div>
        </div>
      </Transition>

      <Transition name="fade"><div v-if="deleteModal.show" class="fixed inset-0 z-[60] bg-black/40 backdrop-blur-sm" @click="closeDeleteModal" /></Transition>
      <Transition name="scale">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none">
          <div class="bg-card rounded-xl shadow-2xl w-full max-w-md border border-border pointer-events-auto p-6">
            <h3 class="text-lg font-semibold text-destructive flex items-center gap-2"><Trash2 class="h-5 w-5" />Hapus Supplier</h3>
            <p class="text-sm text-muted-foreground mt-2">Ketik <span class="font-bold bg-muted px-1.5 py-0.5 rounded">{{ deleteModal.supplier?.name }}</span> untuk konfirmasi.</p>
            <Input v-model="deleteModal.confirmText" class="mt-3" placeholder="Nama supplier" />
            <div class="flex justify-end gap-3 mt-4">
              <Button variant="outline" @click="closeDeleteModal">Batal</Button>
              <Button variant="destructive" :disabled="deleteModal.confirmText !== deleteModal.supplier?.name || deleting" @click="confirmDelete">
                <Loader2 v-if="deleting" class="h-4 w-4 mr-2 animate-spin" />Hapus
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
