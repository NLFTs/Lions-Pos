<script setup>

import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Alert from '@/components/ui/Alert.vue'
import api from '@/lib/api'
import { Plus, Pencil, Trash2, Loader2, X, LayoutGrid, ChevronDown } from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.isAdmin)
const isSuperAdmin = computed(() => authStore.isSuperAdmin)

// ─── State ───────────────────────────────────────────────────────────────────
const categories = ref([])
const loading = ref(false)
const error = ref(null)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

const filteredCategories = computed(() => {
  if (!searchQuery.value) return categories.value
  const q = searchQuery.value.toLowerCase()
  return categories.value.filter(c => 
    c.name.toLowerCase().includes(q) || 
    (c.description || '').toLowerCase().includes(q)
  )
})

const paginatedCategories = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredCategories.value.slice(start, start + pageSize.value)
})

watch(searchQuery, () => {
  page.value = 1
})

// Drawer
const showDrawer = ref(false)
const modalMode = ref('create')
const saving = ref(false)
const formError = ref(null)
const form = ref({ id: null, name: '', description: '' })

// ─── Validasi frontend ───────────────────────────────────────────────────────
const formErrors = ref({
  name: '',
  description: ''
})

// Cooldown untuk submit (opsional, mencegah spam klik)
const lastSubmitTime = ref(0)
const SUBMIT_COOLDOWN_MS = 1000 // 1 detik

// ─── Customize Columns ────────────────────────────────────────────────────────
const showColumnMenu = ref(false)
const columnMenuRef = ref(null)
const availableColumns = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: 'Nama Kategori' },
  { key: 'description', label: 'Deskripsi' },
  { key: 'createdAt', label: 'Dibuat' },
]
const visibleColumns = ref(['id', 'name', 'description', 'createdAt'])

function toggleColumn(key) {
  const idx = visibleColumns.value.indexOf(key)
  if (idx === -1) visibleColumns.value.push(key)
  else if (visibleColumns.value.length > 1) visibleColumns.value.splice(idx, 1)
}

function isColumnVisible(key) {
  return visibleColumns.value.includes(key)
}

function handleColumnMenuOutside(e) {
  if (columnMenuRef.value && !columnMenuRef.value.contains(e.target)) {
    showColumnMenu.value = false
  }
}

onMounted(() => {
  fetchCategories()
  document.addEventListener('click', handleColumnMenuOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleColumnMenuOutside)
})

// ─── Fetch ────────────────────────────────────────────────────────────────────
async function fetchCategories() {
  loading.value = true
  error.value = null
  try {
    const url = isAdmin.value ? '/api/v1/categories/admin' : '/api/v1/categories'
    const res = await api.get(url)
    const data = res.data.data
    
    if (Array.isArray(data)) {
      categories.value = data
    } else {
      categories.value = data.content || []
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load categories.'
  } finally {
    loading.value = false
  }
}

// ─── Create / Edit ────────────────────────────────────────────────────────────
function openCreate() {
  form.value = { id: null, name: '', description: '' }
  formErrors.value = { name: '', description: '' }
  formError.value = null
  modalMode.value = 'create'
  showDrawer.value = true
}

function closeDrawer() {
  showDrawer.value = false
}

function openEdit(cat) {
  form.value = { id: cat.id, name: cat.name, description: cat.description || '' }
  formErrors.value = { name: '', description: '' }
  formError.value = null
  modalMode.value = 'edit'
  showDrawer.value = true
}

async function saveCategory() {
  formError.value = null
  
  // Validasi frontend
  if (!validateCategoryForm()) {
    const firstErrorField = document.querySelector('.form-error')
    if (firstErrorField) firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' })
    return
  }
  
  // Cooldown untuk mencegah spam submit
  const now = Date.now()
  if (now - lastSubmitTime.value < SUBMIT_COOLDOWN_MS) {
    toast.warning('Tunggu sebentar sebelum menyimpan lagi')
    return
  }
  lastSubmitTime.value = now
  
  saving.value = true
  try {
    if (modalMode.value === 'create') {
      await api.post('/api/v1/categories', { name: form.value.name, description: form.value.description })
      toast.success('Kategori berhasil ditambahkan!')
    } else {
      await api.put(`/api/v1/categories/${form.value.id}`, { name: form.value.name, description: form.value.description })
      toast.success('Kategori berhasil diperbarui!')
    }
    showDrawer.value = false
    fetchCategories()
  } catch (err) {
    formError.value = err.response?.data?.data?.message
      || err.response?.data?.message
      || 'Gagal menyimpan kategori.'
  } finally {
    saving.value = false
  }
}

function validateCategoryForm() {
  // Reset errors
  formErrors.value = { name: '', description: '' }
  
  let isValid = true
  
  // 1. Nama kategori
  if (!form.value.name || form.value.name.trim() === '') {
    formErrors.value.name = 'Nama kategori wajib diisi'
    isValid = false
  } else if (form.value.name.length < 3) {
    formErrors.value.name = 'Nama kategori minimal 3 karakter'
    isValid = false
  } else if (form.value.name.length > 100) {
    formErrors.value.name = 'Nama kategori maksimal 100 karakter'
    isValid = false
  }
  
  // 2. Deskripsi (opsional, batasi panjang)
  if (form.value.description && form.value.description.length > 255) {
    formErrors.value.description = 'Deskripsi maksimal 255 karakter'
    isValid = false
  }
  
  return isValid
}

// ─── Delete ───────────────────────────────────────────────────────────────────
async function doDelete(cat) {
  const ok = await confirm({
    title: 'Hapus Kategori',
    description: `Yakin ingin menghapus "${cat.name}"? Produk dalam kategori ini akan menjadi tidak berkategori.`,
  })
  if (!ok) return

  try {
    await api.delete(`/api/v1/categories/${cat.id}`)
    toast.success('Kategori berhasil dihapus!')
    fetchCategories()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menghapus kategori.')
  }
}

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' })
}
</script>

<template>
  <AppLayout>
    <div class="pb-6">
      <!-- Page Header -->
      <div class="mb-6">
        <h1 class="text-xl font-bold tracking-tight text-zinc-900">Manajemen Kategori</h1>
        <p class="text-xs text-zinc-500 mt-0.5">
          Kelola kategori untuk pengelompokan produk.
        </p>
      </div>

      <!-- Table Controls -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5">
        <DataTableSearch
          v-model="searchQuery"
          placeholder="Cari kategori..."
          class="w-full max-w-sm"
          input-class="h-9 text-xs"
        />
        <div class="flex items-center gap-2 w-full sm:w-auto">
          <div ref="columnMenuRef" class="relative flex-1 sm:flex-none">
            <Button variant="outline" size="sm" class="w-full flex items-center justify-center gap-2 border-zinc-200" @click="showColumnMenu = !showColumnMenu">
              <LayoutGrid class="h-3.5 w-3.5" />
              <span>Kolom</span>
              <ChevronDown class="h-3 w-3 text-zinc-400" :class="showColumnMenu ? 'rotate-180' : ''" style="transition: transform 0.2s" />
            </Button>
            <Transition name="fade">
              <div v-if="showColumnMenu" class="absolute right-0 top-full mt-1 z-30 w-48 bg-card border border-border rounded-lg shadow-xl overflow-hidden">
                <div class="px-3 py-2.5 border-b border-border">
                  <span class="text-xs font-semibold text-foreground uppercase tracking-wide">Tampilkan Kolom</span>
                </div>
                <div class="px-2 py-2 space-y-0.5">
                  <button
                    v-for="col in availableColumns"
                    :key="col.key"
                    @click="toggleColumn(col.key)"
                    class="w-full flex items-center justify-between px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors outline-none"
                  >
                    <span class="text-sm font-medium text-foreground select-none">{{ col.label }}</span>
                    <div :class="['w-4 h-4 rounded border flex items-center justify-center transition-colors', isColumnVisible(col.key) ? 'bg-primary border-primary' : 'border-zinc-300 dark:border-zinc-600']">
                      <svg v-if="isColumnVisible(col.key)" xmlns="http://www.w3.org/2000/svg" class="h-2.5 w-2.5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
                    </div>
                  </button>
                </div>
              </div>
            </Transition>
          </div>
          <Button v-if="can('category.store') && !isSuperAdmin" @click="openCreate" size="sm" class="flex-1 sm:flex-none bg-primary hover:bg-primary/90 flex items-center justify-center gap-2">
            <Plus class="h-4 w-4" />
            <span>Tambah Kategori</span>
          </Button>
        </div>
      </div>

      <Alert v-if="error" variant="destructive" class="mb-4">{{ error }}</Alert>

      <Card>
      <CardContent class="p-0">
        <div v-if="loading" class="flex items-center justify-center py-20">
          <Loader2 class="h-6 w-6 animate-spin text-muted-foreground" />
        </div>

        <div v-else-if="filteredCategories.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
          <p class="text-sm">Belum ada kategori.</p>
          <Button v-if="can('category.store') && !isSuperAdmin && !searchQuery" variant="outline" class="mt-3" @click="openCreate">Buat kategori pertama</Button>
        </div>

        <div v-else>
          <!-- ─── Mobile List View ─── -->
          <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
            <div
              v-for="cat in paginatedCategories"
              :key="'mobile-' + cat.id"
              class="p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
            >
              <div class="flex items-start justify-between gap-3">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-lg bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-500 font-bold shrink-0 border border-zinc-200 dark:border-zinc-800/50">
                    {{ cat.name?.charAt(0).toUpperCase() }}
                  </div>
                  <div>
                    <h4 class="font-medium text-sm text-zinc-900 dark:text-zinc-100">{{ cat.name }}</h4>
                    <p class="text-xs text-zinc-500 line-clamp-1 mt-0.5">{{ cat.description || '-' }}</p>
                  </div>
                </div>
                
                <div class="flex items-center gap-1 shrink-0">
                  <Button
                    v-if="can('category.update') && !isSuperAdmin"
                    variant="ghost"
                    size="icon"
                    class="h-8 w-8 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 bg-zinc-50 dark:bg-zinc-800/50"
                    @click="openEdit(cat)"
                  >
                    <Pencil class="h-3.5 w-3.5" />
                  </Button>
                  <Button
                    v-if="can('category.delete') && !isSuperAdmin"
                    variant="ghost"
                    size="icon"
                    class="h-8 w-8 text-zinc-400 hover:text-destructive bg-zinc-50 dark:bg-zinc-800/50"
                    @click="doDelete(cat)"
                  >
                    <Trash2 class="h-3.5 w-3.5" />
                  </Button>
                </div>
              </div>
              <div class="flex items-center justify-between mt-1">
                <span class="text-[10px] text-zinc-400 uppercase tracking-wider font-semibold">ID: {{ cat.id }}</span>
                <span class="text-[10px] text-zinc-400">{{ formatDate(cat.createdAt) }}</span>
              </div>
            </div>
          </div>

          <!-- ─── Desktop Table ─── -->
          <div class="hidden md:block overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="border-b bg-muted/40">
                  <th v-if="isColumnVisible('id')" class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px] w-16">#</th>
                  <th v-if="isColumnVisible('name')" class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Nama Kategori</th>
                  <th v-if="isColumnVisible('description')" class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Deskripsi</th>
                  <th v-if="isColumnVisible('createdAt')" class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Dibuat</th>
                  <th class="px-5 py-3 text-right font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="cat in paginatedCategories"
                  :key="cat.id"
                  class="border-b last:border-0 hover:bg-muted/30 transition-colors"
                >
                  <td v-if="isColumnVisible('id')" class="px-5 py-3 text-muted-foreground font-mono text-xs">{{ cat.id }}</td>
                  <td v-if="isColumnVisible('name')" class="px-5 py-3 font-medium text-zinc-900 dark:text-zinc-100">{{ cat.name }}</td>
                  <td v-if="isColumnVisible('description')" class="px-5 py-3 text-muted-foreground max-w-[400px] truncate">{{ cat.description || '-' }}</td>
                  <td v-if="isColumnVisible('createdAt')" class="px-5 py-3 text-muted-foreground text-xs">{{ formatDate(cat.createdAt) }}</td>
                  <td class="px-5 py-3 text-right">
                    <div class="flex justify-end gap-1">
                      <Button v-if="can('category.update') && !isSuperAdmin" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-zinc-700" @click="openEdit(cat)">
                        <Pencil class="h-3.5 w-3.5" />
                      </Button>
                      <Button v-if="can('category.delete') && !isSuperAdmin" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-destructive" @click="doDelete(cat)">
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
          v-if="filteredCategories.length > 0 && !loading"
          :page="page"
          :page-size="pageSize"
          :total="filteredCategories.length"
          @update:page="page = $event"
          @update:page-size="pageSize = $event; page = 1"
        />
      </CardContent>
    </Card>
    </div>

    <!-- ─── Right-side Drawer ─── -->
    <Teleport to="body">
      <!-- Backdrop -->
      <Transition name="fade">
        <div
          v-if="showDrawer"
          class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm"
          @click="closeDrawer"
        />
      </Transition>

      <!-- Panel -->
      <Transition name="scale">
        <div
        v-if="showDrawer"
        class="fixed inset-0 z-[50] flex items-center justify-center p-4 pointer-events-none"
        >
          <!-- Header -->
            <div 
                class="relative flex flex-col w-full max-w-2xl max-h-[90vh] bg-card shadow-2xl border border-border rounded-xl overflow-hidden pointer-events-auto"
            >            
            <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
                <div>
                    <h3 class="font-semibold text-base">
                      {{ modalMode === 'create' ? 'Tambah Kategori' : 'Edit Kategori' }}
                    </h3>
                    <p class="text-xs text-muted-foreground mt-0.5">
                      {{ modalMode === 'create' ? 'Isi detail kategori baru.' : 'Perbarui informasi kategori.' }}
                    </p>
                </div>
                <Button variant="ghost" size="icon" @click="closeDrawer">
                <X class="h-4 w-5" />
                </Button>
            </div>

            <!-- Body (scrollable) -->
            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5">
              <Alert v-if="formError" variant="destructive">
                <p class="text-sm">{{ formError }}</p>
              </Alert>

            <div class="space-y-1.5">
              <Label for="c-name">Nama Kategori <span class="text-destructive">*</span></Label>
              <Input 
                id="c-name" 
                v-model="form.name" 
                placeholder="Nama kategori" 
                :disabled="saving"
                :class="{ 'border-destructive ring-destructive/20': formErrors.name }"
              />
              <p v-if="formErrors.name" class="text-xs text-destructive form-error">{{ formErrors.name }}</p>
            </div>

            <div class="space-y-1.5">
              <Label for="c-desc">Deskripsi</Label>
              <textarea
                id="c-desc"
                v-model="form.description"
                rows="4"
                :disabled="saving"
                placeholder="Deskripsi opsional..."
                :class="[
                  'flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 resize-none',
                  formErrors.description ? 'border-destructive ring-destructive/20' : ''
                ]"
              />
              <p v-if="formErrors.description" class="text-xs text-destructive form-error">{{ formErrors.description }}</p>
            </div>
          </div>

          <!-- Footer -->
          <div class="flex justify-end gap-3 px-6 py-4 border-t shrink-0 bg-muted/30">
            <Button variant="outline" @click="closeDrawer" :disabled="saving">Batal</Button>
            <Button @click="saveCategory" :disabled="saving">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              {{ modalMode === 'create' ? 'Simpan Kategori' : 'Simpan Perubahan' }}
            </Button>
          </div>
        </div>
        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

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
