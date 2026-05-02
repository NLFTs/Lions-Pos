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
import { Plus, Pencil, Trash2, Loader2, X, MapPin, Search } from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()

// ─── State ───────────────────────────────────────────────────────────────────
const locations = ref([])
const loading = ref(false)
const error = ref(null)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

const filteredLocations = computed(() => {
  if (!searchQuery.value) return locations.value
  const q = searchQuery.value.toLowerCase()
  return locations.value.filter(l => 
    l.name.toLowerCase().includes(q) || 
    l.type?.toLowerCase().includes(q) ||
    l.address?.toLowerCase().includes(q)
  )
})

const paginatedLocations = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredLocations.value.slice(start, start + pageSize.value)
})

// ─── Form State ───────────────────────────────────────────────────────────────
const showDrawer = ref(false)
const modalMode = ref('create')
const saving = ref(false)
const formError = ref(null)

const emptyForm = () => ({
  id: null,
  name: '',
  type: 'warehouse', // 'warehouse' | 'branch'
  address: '',
})

const form = ref(emptyForm())

// ─── Delete ───────────────────────────────────────────────────────────────────
const deleteModal = ref({
  show: false,
  location: null,
  confirmText: ''
})
const deleting = ref(false)

function doDelete(location) {
  deleteModal.value = {
    show: true,
    location,
    confirmText: ''
  }
}

function closeDeleteModal() {
  deleteModal.value.show = false
  setTimeout(() => {
    deleteModal.value.location = null
    deleteModal.value.confirmText = ''
  }, 300)
}

async function confirmDelete() {
  if (deleteModal.value.confirmText !== deleteModal.value.location?.name) return

  deleting.value = true
  try {
    await api.delete(`/api/v1/locations/${deleteModal.value.location.id}`)
    toast.success('Lokasi berhasil dihapus!')
    fetchLocations()
    closeDeleteModal()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menghapus lokasi.')
  } finally {
    deleting.value = false
  }
}

// ─── Actions ──────────────────────────────────────────────────────────────────
async function fetchLocations() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/locations')
    locations.value = res.data.data
  } catch (err) {
    if (import.meta.env.DEV) {
      locations.value = [
        { id: '1', name: 'Gudang Utama', type: 'warehouse', address: 'Jl. Industri No. 1' },
        { id: '2', name: 'Cabang Jakarta', type: 'branch', address: 'Jakarta Selatan' },
      ]
    } else {
      error.value = 'Gagal memuat data lokasi.'
    }
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

function openEdit(l) {
  form.value = { ...l }
  formError.value = null
  modalMode.value = 'edit'
  showDrawer.value = true
}

async function saveLocation() {
  saving.value = true
  formError.value = null
  try {
    if (modalMode.value === 'create') {
      await api.post('/api/v1/locations', form.value)
      toast.success('Lokasi berhasil ditambahkan!')
    } else {
      await api.put(`/api/v1/locations/${form.value.id}`, form.value)
      toast.success('Lokasi berhasil diperbarui!')
    }
    showDrawer.value = false
    fetchLocations()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Gagal menyimpan lokasi.'
  } finally {
    saving.value = false
  }
}



onMounted(fetchLocations)
</script>

<template>
  <AppLayout>
    <div class="pb-6">
      <div class="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100">Manajemen Lokasi</h1>
          <p class="text-xs text-zinc-500 mt-0.5">Kelola data gudang dan cabang.</p>
        </div>
        <Button @click="openCreate" class="bg-primary hover:bg-primary/90 flex items-center gap-2">
          <Plus class="h-4 w-4" />
          <span>Tambah Lokasi</span>
        </Button>
      </div>

      <div class="flex flex-col sm:flex-row items-center justify-between gap-4 mb-5">
        <DataTableSearch v-model="searchQuery" placeholder="Cari lokasi..." class="w-full sm:max-w-sm" />
      </div>

      <Card>
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-20">
            <Loader2 class="h-6 w-6 animate-spin text-muted-foreground" />
          </div>

          <div v-else-if="filteredLocations.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
            <MapPin class="h-10 w-10 mb-3 opacity-20" />
            <p class="text-sm">Belum ada data lokasi.</p>
          </div>

          <div v-else class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="bg-muted/40 border-b">
                  <th class="px-4 py-3 text-left font-medium text-muted-foreground">Nama Lokasi</th>
                  <th class="px-4 py-3 text-left font-medium text-muted-foreground">Tipe</th>
                  <th class="px-4 py-3 text-left font-medium text-muted-foreground">Alamat</th>
                  <th class="px-4 py-3 text-right font-medium text-muted-foreground">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="l in paginatedLocations" :key="l.id" class="border-b last:border-0 hover:bg-muted/30 transition-colors">
                  <td class="px-4 py-3 font-medium">{{ l.name }}</td>
                  <td class="px-4 py-3 capitalize text-xs">
                    <Badge :variant="l.type === 'warehouse' ? 'default' : 'secondary'">{{ l.type }}</Badge>
                  </td>
                  <td class="px-4 py-3 text-xs text-muted-foreground max-w-[250px] truncate">
                    {{ l.address }}
                  </td>
                  <td class="px-4 py-3 text-right">
                    <div class="flex justify-end gap-2">
                      <Button variant="ghost" size="icon" @click="openEdit(l)">
                        <Pencil class="h-4 w-4" />
                      </Button>
                      <Button variant="ghost" size="icon" class="text-destructive" @click="doDelete(l)">
                        <Trash2 class="h-4 w-4" />
                      </Button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <DataTablePagination
            v-if="filteredLocations.length > 0 && !loading"
            :page="page"
            :page-size="pageSize"
            :total="filteredLocations.length"
            @update:page="page = $event"
            @update:page-size="pageSize = $event; page = 1"
          />
        </CardContent>
      </Card>
    </div>

    <!-- Drawer -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showDrawer" class="fixed inset-0 z-[100] bg-black/40 backdrop-blur-sm" @click="showDrawer = false" />
      </Transition>

      <Transition name="slide-right">
        <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[101] flex flex-col w-full sm:max-w-[420px] h-full bg-card shadow-2xl sm:border-l overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
            <div>
              <h3 class="font-semibold text-base">{{ modalMode === 'create' ? 'Tambah Lokasi' : 'Edit Lokasi' }}</h3>
              <p class="text-xs text-muted-foreground mt-0.5">Kelola data gudang atau cabang.</p>
            </div>
            <Button variant="ghost" size="icon" @click="showDrawer = false">
              <X class="h-4 w-4" />
            </Button>
          </div>

          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-4">
            <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>

            <div class="space-y-1.5">
              <Label for="name">Nama Lokasi <span class="text-destructive">*</span></Label>
              <Input id="name" v-model="form.name" placeholder="Gudang A / Cabang B..." />
            </div>

            <div class="space-y-1.5">
              <Label for="type">Tipe Lokasi</Label>
              <select id="type" v-model="form.type" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm">
                <option value="warehouse">Warehouse (Gudang)</option>
                <option value="branch">Branch (Cabang)</option>
              </select>
            </div>

            <div class="space-y-1.5">
              <Label for="address">Alamat</Label>
              <textarea id="address" v-model="form.address" rows="4" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring" placeholder="Alamat lengkap lokasi..." />
            </div>
          </div>

          <div class="flex justify-end gap-3 px-6 py-4 border-t bg-muted/30">
            <Button variant="outline" @click="showDrawer = false" :disabled="saving">Batal</Button>
            <Button @click="saveLocation" :disabled="saving">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              Simpan Lokasi
            </Button>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Delete Confirmation Modal -->
    <Teleport to="body">
      <!-- Backdrop -->
      <Transition name="fade">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[110] bg-black/40 backdrop-blur-sm" @click="closeDeleteModal"></div>
      </Transition>
      
      <!-- Modal Content -->
      <Transition name="scale">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[111] flex items-center justify-center p-4 pointer-events-none">
          <div class="relative bg-card rounded-xl shadow-2xl w-full max-w-md overflow-hidden border border-border pointer-events-auto">
            <div class="p-6">
              <h3 class="text-lg font-semibold text-destructive flex items-center gap-2">
                <Trash2 class="h-5 w-5" />
                Hapus Lokasi
              </h3>
              <p class="text-sm text-muted-foreground mt-2">
                Tindakan ini tidak dapat dibatalkan. Hal ini akan menghapus lokasi 
                <span class="font-semibold text-foreground">{{ deleteModal.location?.name }}</span> secara permanen.
              </p>
              
              <div class="mt-4">
                <Label class="text-sm font-medium">
                  Ketik <span class="font-bold select-all bg-muted px-1.5 py-0.5 rounded text-foreground">{{ deleteModal.location?.name }}</span> untuk mengonfirmasi.
                </Label>
                <Input
                  v-model="deleteModal.confirmText"
                  class="mt-2"
                  placeholder="Masukkan nama lokasi"
                  @keyup.enter="confirmDelete"
                />
              </div>
            </div>
            <div class="flex items-center justify-end gap-3 px-6 py-4 bg-muted/30 border-t">
              <Button variant="outline" @click="closeDeleteModal">Batal</Button>
              <Button
                variant="destructive"
                :disabled="deleteModal.confirmText !== deleteModal.location?.name || deleting"
                @click="confirmDelete"
              >
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

.scale-enter-active, .scale-leave-active {
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.scale-enter-from, .scale-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
