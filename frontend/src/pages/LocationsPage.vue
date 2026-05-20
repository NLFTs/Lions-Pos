<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import Alert from '@/components/ui/Alert.vue'
import api from '@/lib/api'
import { Plus, Pencil, Trash2, Loader2, X, MapPin, Building2, Warehouse } from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.isAdmin)

// ─── State ───────────────────────────────────────────────────────────────────
const branches = ref([])
const warehouses = ref([])
const loading = ref(false)
const error = ref(null)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

// Unified list of locations
const allLocations = computed(() => {
  const b = branches.value.map(x => ({ ...x, type: 'branch' }))
  const w = warehouses.value.map(x => ({ ...x, type: 'warehouse' }))
  return [...b, ...w]
})

const filteredLocations = computed(() => {
  if (!searchQuery.value) return allLocations.value
  const q = searchQuery.value.toLowerCase()
  return allLocations.value.filter(l => 
    (l.name && l.name.toLowerCase().includes(q)) || 
    (l.address && l.address.toLowerCase().includes(q))
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
  username: '',
  password: '',
})

const form = ref(emptyForm())

// ─── Actions ──────────────────────────────────────────────────────────────────
async function fetchLocations() {
  loading.value = true
  error.value = null
  try {
    const urlB = isAdmin.value ? '/api/v1/branches/admin' : '/api/v1/branches'
    const urlW = isAdmin.value ? '/api/v1/warehouses/admin' : '/api/v1/warehouses'
    
    const [resB, resW] = await Promise.all([
      api.get(urlB),
      api.get(urlW)
    ])
    
    // Admin branches: returns List<BranchResponse> directly (no ResData)
    // Partner branches: returns ResData<List<BranchResponse>>
    if (isAdmin.value) {
      branches.value = Array.isArray(resB.data) ? resB.data : (resB.data?.data || [])
    } else {
      branches.value = resB.data?.data || []
    }
    
    // Admin warehouses: returns ResData<Page<WarehouseResponse>> (paginated)
    // Partner warehouses: returns ResData<List<WarehouseResponse>>
    const whData = resW.data?.data
    if (whData && !Array.isArray(whData) && whData.content) {
      warehouses.value = whData.content
    } else {
      warehouses.value = Array.isArray(whData) ? whData : []
    }
  } catch (err) {
    error.value = 'Gagal memuat data lokasi.'
    toast.error(error.value)
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
  form.value = { 
    id: l.id,
    name: l.name,
    type: l.type,
    address: l.address || ''
  }
  formError.value = null
  modalMode.value = 'edit'
  showDrawer.value = true
}

async function saveLocation() {
  if (!form.value.name.trim()) {
    formError.value = 'Nama lokasi wajib diisi.'
    return
  }

  if (form.value.type === 'branch' && modalMode.value === 'create') {
    if (!form.value.username?.trim()) {
      formError.value = 'Username wajib diisi untuk pembuatan Cabang.'
      return
    }
    if (!form.value.password || form.value.password.length < 6) {
      formError.value = 'Password wajib diisi dan minimal 6 karakter.'
      return
    }
  }
  
  saving.value = true
  formError.value = null
  try {
    const endpoint = form.value.type === 'branch' ? '/api/v1/branches' : '/api/v1/warehouses'
    
    const payload = {
      name: form.value.name,
      address: form.value.address,
    }
    if (form.value.type === 'branch') {
      payload.username = form.value.username
      payload.password = form.value.password
    }
    
    if (modalMode.value === 'create') {
      await api.post(endpoint, payload)
      toast.success(`${form.value.type === 'branch' ? 'Cabang' : 'Gudang'} berhasil ditambahkan!`)
    } else {
      await api.put(`${endpoint}/${form.value.id}`, payload)
      toast.success('Data lokasi berhasil diperbarui!')
    }
    showDrawer.value = false
    fetchLocations()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Gagal menyimpan lokasi.'
  } finally {
    saving.value = false
  }
}

// ─── Delete ───────────────────────────────────────────────────────────────────
const deleteModal = ref({ show: false, location: null, confirmText: '' })
const deleting = ref(false)

function doDelete(location) {
  deleteModal.value = { show: true, location, confirmText: '' }
}

function closeDeleteModal() {
  deleteModal.value.show = false
}

async function confirmDelete() {
  if (deleteModal.value.confirmText !== deleteModal.value.location?.name) return
  
  deleting.value = true
  try {
    const endpoint = deleteModal.value.location.type === 'branch' ? '/api/v1/branches' : '/api/v1/warehouses'
    await api.delete(`${endpoint}/${deleteModal.value.location.id}`)
    toast.success('Lokasi berhasil dihapus!')
    fetchLocations()
    closeDeleteModal()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menghapus lokasi.')
  } finally {
    deleting.value = false
  }
}

onMounted(fetchLocations)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <!-- Header -->
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Manajemen Lokasi</h1>
          <p class="text-muted-foreground text-sm mt-1">Kelola data gudang (Warehouse) dan cabang (Branch) Anda.</p>
        </div>
        <div class="flex items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-72">
            <DataTableSearch v-model="searchQuery" placeholder="Cari lokasi..." />
          </div>
          <Button @click="openCreate" size="sm" class="flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
            <Plus class="h-4 w-4" />
            <span>Tambah Lokasi</span>
          </Button>
        </div>
      </div>

      <!-- Table Card -->
      <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm overflow-hidden">
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-24">
            <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
          </div>

          <div v-else-if="filteredLocations.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
            <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
              <MapPin class="h-7 w-7 opacity-40" />
            </div>
            <p class="text-sm font-medium">Belum ada data lokasi.</p>
            <Button size="sm" class="mt-4" @click="openCreate">
              <Plus class="h-3.5 w-3.5 mr-1.5" />
              Tambah Lokasi
            </Button>
          </div>

          <div v-else>
            <!-- Mobile View -->
            <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
              <div v-for="l in paginatedLocations" :key="l.type + l.id" class="p-4 flex flex-col gap-3">
                <div class="flex items-start justify-between">
                  <div class="flex items-center gap-3">
                    <div :class="['w-10 h-10 rounded-lg flex items-center justify-center border shrink-0', l.type === 'warehouse' ? 'bg-amber-50 border-amber-100 text-amber-600 dark:bg-amber-900/10 dark:border-amber-800/50' : 'bg-blue-50 border-blue-100 text-blue-600 dark:bg-blue-900/10 dark:border-blue-800/50']">
                      <Warehouse v-if="l.type === 'warehouse'" class="h-5 w-5" />
                      <Building2 v-else class="h-5 w-5" />
                    </div>
                    <div>
                      <h4 class="font-medium text-sm text-zinc-900 dark:text-zinc-100">{{ l.name }}</h4>
                      <Badge :variant="l.type === 'warehouse' ? 'outline' : 'secondary'" class="text-[9px] px-1.5 py-0 mt-1 uppercase tracking-wider font-bold h-4">
                        {{ l.type }}
                      </Badge>
                    </div>
                  </div>
                  <div class="flex gap-1 shrink-0">
                    <Button variant="ghost" size="icon" class="h-8 w-8 text-zinc-400" @click="openEdit(l)">
                      <Pencil class="h-3.5 w-3.5" />
                    </Button>
                    <Button variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-destructive" @click="doDelete(l)">
                      <Trash2 class="h-3.5 w-3.5" />
                    </Button>
                  </div>
                </div>
                <div v-if="l.address" class="text-[11px] text-zinc-500 bg-zinc-50 dark:bg-zinc-900/50 p-2 rounded border border-zinc-100 dark:border-zinc-800/60">
                  {{ l.address }}
                </div>
              </div>
            </div>

            <!-- Desktop View -->
            <div class="hidden md:block overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="border-b border-zinc-100 dark:border-zinc-800">
                    <th class="pl-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Nama Lokasi</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Tipe</th>
                    <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Alamat</th>
                    <th class="pr-5 py-3 text-right"></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="l in paginatedLocations" :key="l.type + l.id" class="border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors">
                    <td class="pl-5 py-3">
                      <div class="flex items-center gap-3">
                        <div :class="['w-8 h-8 rounded flex items-center justify-center shrink-0', l.type === 'warehouse' ? 'bg-amber-50 text-amber-600 dark:bg-amber-900/20' : 'bg-blue-50 text-blue-600 dark:bg-blue-900/20']">
                          <Warehouse v-if="l.type === 'warehouse'" class="h-4 w-4" />
                          <Building2 v-else class="h-4 w-4" />
                        </div>
                        <span class="font-medium text-zinc-900 dark:text-zinc-100">{{ l.name }}</span>
                      </div>
                    </td>
                    <td class="py-3">
                      <Badge :variant="l.type === 'warehouse' ? 'outline' : 'secondary'" class="text-[10px] px-2 py-0 uppercase tracking-widest font-bold">
                        {{ l.type }}
                      </Badge>
                    </td>
                    <td class="py-3 max-w-[300px] truncate text-xs text-zinc-500">
                      {{ l.address || '-' }}
                    </td>
                    <td class="pr-5 py-3 text-right">
                      <div class="flex justify-end gap-1">
                        <Button variant="ghost" size="icon" class="h-7 w-7 text-zinc-400 hover:text-zinc-700" @click="openEdit(l)">
                          <Pencil class="h-3.5 w-3.5" />
                        </Button>
                        <Button variant="ghost" size="icon" class="h-7 w-7 text-zinc-400 hover:text-destructive" @click="doDelete(l)">
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
        <div v-if="showDrawer" class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm" @click="showDrawer = false" />
      </Transition>
      <Transition name="slide-right">
        <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[420px] bg-card shadow-2xl sm:border-l overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
            <div>
              <h3 class="font-semibold text-base">{{ modalMode === 'create' ? 'Tambah Lokasi' : 'Edit Lokasi' }}</h3>
              <p class="text-xs text-muted-foreground mt-0.5">Kelola data gudang atau cabang mitra.</p>
            </div>
            <Button variant="ghost" size="icon" @click="showDrawer = false">
              <X class="h-4 w-4" />
            </Button>
          </div>
          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-4">
            <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>
            
            <div class="space-y-1.5">
              <Label for="l-type">Tipe Lokasi <span class="text-destructive">*</span></Label>
              <div class="grid grid-cols-2 gap-2">
                <button 
                  type="button"
                  @click="form.type = 'warehouse'"
                  :class="['flex items-center justify-center gap-2 h-10 rounded-md border text-sm font-medium transition-all', form.type === 'warehouse' ? 'bg-primary text-primary-foreground border-primary shadow-sm' : 'bg-background border-input hover:bg-muted']"
                  :disabled="modalMode === 'edit'"
                >
                  <Warehouse class="h-4 w-4" /> Warehouse
                </button>
                <button 
                  type="button"
                  @click="form.type = 'branch'"
                  :class="['flex items-center justify-center gap-2 h-10 rounded-md border text-sm font-medium transition-all', form.type === 'branch' ? 'bg-primary text-primary-foreground border-primary shadow-sm' : 'bg-background border-input hover:bg-muted']"
                  :disabled="modalMode === 'edit'"
                >
                  <Building2 class="h-4 w-4" /> Branch
                </button>
              </div>
              <p v-if="modalMode === 'edit'" class="text-[10px] text-muted-foreground italic">Tipe lokasi tidak dapat diubah setelah dibuat.</p>
            </div>

            <div class="space-y-1.5">
              <Label for="l-name">Nama Lokasi <span class="text-destructive">*</span></Label>
              <Input id="l-name" v-model="form.name" placeholder="Contoh: Gudang Sentral / Cabang Sudirman" :disabled="saving" />
            </div>

            <div class="space-y-1.5">
              <Label for="l-address">Alamat</Label>
              <textarea 
                id="l-address" 
                v-model="form.address" 
                rows="4" 
                class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring" 
                placeholder="Alamat lengkap lokasi..."
                :disabled="saving"
              />
            </div>

            <div v-if="form.type === 'branch' && modalMode === 'create'" class="space-y-4 border-t border-border pt-4 mt-4">
              <h4 class="text-xs font-bold uppercase tracking-wider text-zinc-500 mb-2">Akun Pengguna Cabang</h4>
              
              <div class="space-y-1.5">
                <Label for="l-username">Username <span class="text-destructive">*</span></Label>
                <Input id="l-username" v-model="form.username" placeholder="Username untuk login cabang" :disabled="saving" />
              </div>

              <div class="space-y-1.5">
                <Label for="l-password">Password <span class="text-destructive">*</span></Label>
                <Input id="l-password" type="password" v-model="form.password" placeholder="Password untuk login cabang" :disabled="saving" />
              </div>
            </div>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4 border-t bg-muted/30">
            <Button variant="outline" @click="showDrawer = false" :disabled="saving">Batal</Button>
            <Button @click="saveLocation" :disabled="saving" class="bg-primary hover:bg-primary/90">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              {{ modalMode === 'create' ? 'Simpan Lokasi' : 'Perbarui' }}
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
              <h3 class="text-lg font-bold">Hapus Lokasi</h3>
            </div>
            <p class="text-sm text-muted-foreground mb-4 leading-relaxed">
              Apakah Anda yakin ingin menghapus <span class="font-bold text-foreground">{{ deleteModal.location?.name }}</span>? Data stok pada lokasi ini mungkin akan terpengaruh.
            </p>
            <div class="space-y-2 mb-6">
              <Label class="text-xs">Ketik nama lokasi untuk konfirmasi:</Label>
              <Input v-model="deleteModal.confirmText" placeholder="Nama lokasi" class="border-destructive/30 focus:ring-destructive/20" />
            </div>
            <div class="flex justify-end gap-3">
              <Button variant="outline" @click="closeDeleteModal" :disabled="deleting">Batal</Button>
              <Button variant="destructive" :disabled="deleteModal.confirmText !== deleteModal.location?.name || deleting" @click="confirmDelete">
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
