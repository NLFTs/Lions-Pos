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
import {
  Plus, Pencil, Trash2, Loader2, X, LayoutGrid, ChevronDown,
  ArrowLeft, LayoutList, ChevronRight, FolderOpen, Folder, Check, User
} from 'lucide-vue-next'
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
    (c.description || '').toLowerCase().includes(q) ||
    (c.parent?.name || '').toLowerCase().includes(q)
  )
})

const paginatedCategories = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredCategories.value.slice(start, start + pageSize.value)
})

watch(searchQuery, () => {
  page.value = 1
})

// ─── Selection State ──────────────────────────────────────────────────────────
const selectedIds = ref([])

const isAllSelected = computed(() => {
  const visible = paginatedCategories.value
  if (visible.length === 0) return false
  return visible.every(c => selectedIds.value.includes(c.id))
})

function toggleSelectAll() {
  const visible = paginatedCategories.value
  if (isAllSelected.value) {
    const visibleIds = visible.map(c => c.id)
    selectedIds.value = selectedIds.value.filter(id => !visibleIds.includes(id))
  } else {
    visible.forEach(c => {
      if (!selectedIds.value.includes(c.id)) {
        selectedIds.value.push(c.id)
      }
    })
  }
}

function toggleSelect(id) {
  const index = selectedIds.value.indexOf(id)
  if (index === -1) {
    selectedIds.value.push(id)
  } else {
    selectedIds.value.splice(index, 1)
  }
}

async function bulkDelete() {
  const count = selectedIds.value.length
  if (count === 0) return

  const ok = await confirm({
    title: 'Hapus Kategori Terpilih',
    description: `Apakah Anda yakin ingin menghapus ${count} kategori terpilih? Produk dalam kategori tersebut akan menjadi tidak berkategori.`,
    confirmLabel: 'Hapus',
    cancelLabel: 'Batal',
  })
  if (!ok) return

  loading.value = true
  try {
    await Promise.all(
      selectedIds.value.map(id => api.delete(`/api/v1/categories/${id}`))
    )
    toast.success(`${count} kategori berhasil dihapus!`)
    selectedIds.value = []
    fetchCategories()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menghapus beberapa kategori.')
    fetchCategories()
  } finally {
    loading.value = false
  }
}

watch([searchQuery, page, pageSize], () => {
  selectedIds.value = []
})

// ─── Inline Form State ────────────────────────────────────────────────────────
const showForm = ref(false)
const modalMode = ref('create')
const saving = ref(false)
const formError = ref(null)
const lastSubmitTime = ref(0)
const SUBMIT_COOLDOWN_MS = 1000

const emptyForm = () => ({ id: null, name: '', description: '', parentId: null, sortOrder: null })
const form = ref(emptyForm())

const formErrors = ref({ name: '', description: '' })

// ─── Customize Columns ────────────────────────────────────────────────────────
const showColumnMenu = ref(false)
const columnMenuRef = ref(null)
const availableColumns = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: 'Nama Kategori' },
  { key: 'parent', label: 'Parent' },
  { key: 'description', label: 'Deskripsi' },
  { key: 'createdBy', label: 'Dibuat Oleh' },
  { key: 'createdAt', label: 'Dibuat' },
]
const visibleColumns = ref(['id', 'name', 'parent', 'description', 'createdBy', 'createdAt'])

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

// ─── Parent Category Picker ───────────────────────────────────────────────────
const showParentPicker = ref(false)
const parentPickerRef = ref(null)
const parentSearchQuery = ref('')

const parentCandidates = computed(() => {
  // Cannot pick self as parent when editing
  let list = categories.value.filter(c => c.id !== form.value.id)
  if (parentSearchQuery.value) {
    const q = parentSearchQuery.value.toLowerCase()
    list = list.filter(c => c.name.toLowerCase().includes(q))
  }
  return list
})

const selectedParentLabel = computed(() => {
  if (!form.value.parentId) return null
  const cat = categories.value.find(c => c.id === form.value.parentId)
  return cat ? cat.name : null
})

function selectParent(cat) {
  form.value.parentId = cat ? cat.id : null
  parentSearchQuery.value = ''
  showParentPicker.value = false
}

function handleParentPickerOutside(e) {
  if (parentPickerRef.value && !parentPickerRef.value.contains(e.target)) {
    showParentPicker.value = false
  }
}

// ─── Lifecycle ────────────────────────────────────────────────────────────────
onMounted(() => {
  fetchCategories()
  document.addEventListener('click', handleColumnMenuOutside)
  document.addEventListener('click', handleParentPickerOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleColumnMenuOutside)
  document.removeEventListener('click', handleParentPickerOutside)
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
    error.value = err.response?.data?.message || 'Gagal memuat data kategori.'
  } finally {
    loading.value = false
  }
}

// ─── Create / Edit ────────────────────────────────────────────────────────────
function openCreate() {
  form.value = emptyForm()
  formErrors.value = { name: '', description: '' }
  formError.value = null
  parentSearchQuery.value = ''
  modalMode.value = 'create'
  showForm.value = true
}

function closeForm() {
  showForm.value = false
}

function openEdit(cat) {
  form.value = {
    id: cat.id,
    name: cat.name,
    description: cat.description || '',
    parentId: cat.parent?.id || null,
    sortOrder: cat.sortOrder || null,
  }
  formErrors.value = { name: '', description: '' }
  formError.value = null
  parentSearchQuery.value = ''
  modalMode.value = 'edit'
  showForm.value = true
}

async function saveCategory() {
  formError.value = null
  if (!validateCategoryForm()) {
    const firstErrorField = document.querySelector('.form-error')
    if (firstErrorField) firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' })
    return
  }

  const now = Date.now()
  if (now - lastSubmitTime.value < SUBMIT_COOLDOWN_MS) {
    toast.warning('Tunggu sebentar sebelum menyimpan lagi')
    return
  }
  lastSubmitTime.value = now

  saving.value = true
  try {
    const payload = {
      name: form.value.name,
      description: form.value.description || null,
      parent_id: form.value.parentId || null,
      sort_order: form.value.sortOrder || null,
    }
    if (modalMode.value === 'create') {
      await api.post('/api/v1/categories', payload)
      toast.success('Kategori berhasil ditambahkan!')
    } else {
      await api.put(`/api/v1/categories/${form.value.id}`, payload)
      toast.success('Kategori berhasil diperbarui!')
    }
    showForm.value = false
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
  formErrors.value = { name: '', description: '' }
  let isValid = true

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
    toast.error(err.response?.data?.data?.message || err.response?.data?.message || 'Gagal menghapus kategori.')
  }
}

// ─── Utils ────────────────────────────────────────────────────────────────────
function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' })
}

const AVATAR_COLORS = [
  { bg: '#ede9fe', color: '#6d28d9' },
  { bg: '#dbeafe', color: '#1d4ed8' },
  { bg: '#d1fae5', color: '#065f46' },
  { bg: '#fef3c7', color: '#92400e' },
  { bg: '#fee2e2', color: '#991b1b' },
  { bg: '#fce7f3', color: '#9d174d' },
  { bg: '#e0f2fe', color: '#0369a1' },
  { bg: '#f3f4f6', color: '#374151' },
]
function categoryAvatarStyle(name = '') {
  const idx = (name.charCodeAt(0) || 0) % AVATAR_COLORS.length
  const c = AVATAR_COLORS[idx]
  return { backgroundColor: c.bg, color: c.color }
}
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
              <h1 class="text-2xl font-bold tracking-tight">Kategori</h1>
              <p class="text-muted-foreground text-sm mt-1">Kelola pengelompokan produk dengan hierarki.</p>
            </div>

            <div class="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto">
              <div class="w-full sm:w-72">
                <DataTableSearch v-model="searchQuery" placeholder="Cari kategori..." />
              </div>
              <div class="flex items-center gap-2 w-full sm:w-auto">
                <!-- Column Toggle -->
                <div ref="columnMenuRef" class="relative flex-1 sm:flex-none">
                  <button
                    @click="showColumnMenu = !showColumnMenu"
                    class="w-full flex items-center justify-center gap-2 h-9 px-3 rounded-md border border-border bg-background hover:bg-accent text-foreground text-sm font-medium transition-colors"
                  >
                    <LayoutGrid class="h-3.5 w-3.5" />
                    <span>Kolom</span>
                    <ChevronDown class="h-3 w-3 text-muted-foreground" :class="showColumnMenu ? 'rotate-180' : ''" style="transition: transform 0.2s" />
                  </button>
                  <Transition name="fade">
                    <div v-if="showColumnMenu" class="absolute right-0 top-full mt-1 z-30 w-52 bg-card border border-border rounded-lg shadow-xl overflow-hidden">
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
                            <Check v-if="isColumnVisible(col.key)" class="h-2.5 w-2.5 text-white" />
                          </div>
                        </button>
                      </div>
                    </div>
                  </Transition>
                </div>

                <Button
                  v-if="can('category.store') && !isSuperAdmin"
                  @click="openCreate"
                  size="sm"
                  class="flex-1 sm:flex-none flex items-center justify-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground"
                >
                  <Plus class="h-4 w-4" />
                  <span>Tambah Kategori</span>
                </Button>
              </div>
            </div>
          </div>

          <Alert v-if="error" variant="destructive" class="mb-4">{{ error }}</Alert>

          <!-- Table card -->
          <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
            <CardContent class="p-0">
              <!-- Loading -->
              <div v-if="loading" class="flex flex-col items-center justify-center py-24 gap-3">
                <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
                <p class="text-xs text-muted-foreground">Memuat data...</p>
              </div>

              <!-- Empty -->
              <div v-else-if="filteredCategories.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
                <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
                  <FolderOpen class="h-7 w-7 opacity-40" />
                </div>
                <p class="text-sm font-medium">Belum ada kategori</p>
                <p class="text-xs text-muted-foreground/70 mt-1">Mulai dengan membuat kategori pertama Anda.</p>
                <Button v-if="can('category.store') && !isSuperAdmin && !searchQuery" size="sm" class="mt-4" @click="openCreate">
                  <Plus class="h-3.5 w-3.5 mr-1.5" />
                  Buat Kategori
                </Button>
              </div>

              <div v-else>
                <!-- Mobile List View -->
                <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
                  <div
                    v-for="cat in paginatedCategories"
                    :key="'mobile-' + cat.id"
                    class="p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
                  >
                    <div class="flex items-start justify-between gap-3">
                      <div class="flex items-center gap-3">
                        <div
                          class="w-10 h-10 rounded-lg flex items-center justify-center text-sm font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-800/50"
                          :style="categoryAvatarStyle(cat.name)"
                        >
                          {{ cat.name?.charAt(0).toUpperCase() }}
                        </div>
                        <div>
                          <div class="flex items-center gap-1.5 flex-wrap">
                            <h4 class="font-medium text-sm text-zinc-900 dark:text-zinc-100">{{ cat.name }}</h4>
                            <!-- Parent badge (mobile) -->
                            <span v-if="cat.parent?.name" class="inline-flex items-center gap-1 text-[10px] font-medium px-1.5 py-0.5 rounded-full bg-violet-50 text-violet-700 dark:bg-violet-900/20 dark:text-violet-300 border border-violet-100 dark:border-violet-800/40">
                              <ChevronRight class="h-2.5 w-2.5" />{{ cat.parent.name }}
                            </span>
                            <span v-else class="inline-flex items-center gap-1 text-[10px] font-bold px-1.5 py-0.5 rounded-full bg-amber-50 text-amber-700 dark:bg-amber-900/20 dark:text-amber-300 border border-amber-200 dark:border-amber-800/40">
                              Induk
                            </span>
                          </div>
                          <p class="text-xs text-zinc-500 line-clamp-1 mt-0.5">{{ cat.description || '-' }}</p>
                          <p v-if="(cat.createdBy || cat.created_by)?.username" class="text-[10px] text-zinc-400 mt-0.5 flex items-center gap-1">
                            <User class="h-2.5 w-2.5" />{{ (cat.createdBy || cat.created_by).username }}
                          </p>
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
                      <span class="text-[10px] text-zinc-400">{{ formatDate(cat.createdAt || cat.created_at) }}</span>
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
                      <div class="flex items-center gap-2">
                        <Button
                          v-if="can('category.delete') && !isSuperAdmin"
                          size="sm"
                          variant="destructive"
                          class="h-8 text-xs gap-1"
                          @click="bulkDelete"
                        >
                          <Trash2 class="h-3.5 w-3.5" />
                          Hapus
                        </Button>
                      </div>
                    </div>
                  </Transition>

                  <table class="w-full text-sm">
                    <thead>
                      <tr class="border-b bg-muted/40">
                        <th class="w-12 pl-5 py-3 text-left">
                          <input
                            type="checkbox"
                            class="rounded border-zinc-300 dark:border-zinc-700 text-primary focus:ring-primary h-4 w-4 cursor-pointer"
                            :checked="isAllSelected"
                            @change="toggleSelectAll"
                          />
                        </th>
                        <th v-if="isColumnVisible('name')" class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Nama Kategori</th>
                        <th v-if="isColumnVisible('parent')" class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Tipe Kategori</th>
                        <th v-if="isColumnVisible('description')" class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Deskripsi</th>
                        <th v-if="isColumnVisible('createdBy')" class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Dibuat Oleh</th>
                        <th v-if="isColumnVisible('createdAt')" class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Dibuat</th>
                        <th class="px-5 py-3 text-right font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Aksi</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr
                        v-for="cat in paginatedCategories"
                        :key="cat.id"
                        class="group table-lift-row border-b last:border-0 odd:bg-background even:bg-zinc-50/40 dark:even:bg-zinc-900/10 hover:bg-zinc-100/60 dark:hover:bg-zinc-800/40 transition-colors cursor-pointer"
                        @click="can('category.update') && !isSuperAdmin && openEdit(cat)"
                      >
                        <td class="w-12 pl-5 py-3 text-left" @click.stop>
                          <input
                            type="checkbox"
                            class="rounded border-zinc-300 dark:border-zinc-700 text-primary focus:ring-primary h-4 w-4 cursor-pointer"
                            :checked="selectedIds.includes(cat.id)"
                            @change="toggleSelect(cat.id)"
                          />
                        </td>
                        <td v-if="isColumnVisible('name')" class="px-5 py-3">
                          <div class="flex items-center gap-3">
                            <div
                              class="w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-700/50"
                              :style="categoryAvatarStyle(cat.name)"
                            >
                              {{ cat.name?.charAt(0).toUpperCase() }}
                            </div>
                            <span class="font-medium text-sm text-zinc-900 dark:text-zinc-100">{{ cat.name }}</span>
                          </div>
                        </td>
                        <td v-if="isColumnVisible('parent')" class="px-5 py-3">
                          <!-- Has parent: show purple badge with parent name -->
                          <span
                            v-if="cat.parent?.name"
                            class="inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full bg-violet-50 dark:bg-violet-900/20 text-violet-700 dark:text-violet-300 border border-violet-100 dark:border-violet-800/40"
                          >
                            <Folder class="h-3 w-3" />
                            {{ cat.parent.name }}
                          </span>
                          <!-- Root category: show yellow "Induk" badge -->
                          <span v-else class="inline-flex items-center gap-1 text-xs font-bold px-2 py-1 rounded-full bg-amber-50 dark:bg-amber-900/20 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-800/40">
                            Induk
                          </span>
                        </td>
                        <td v-if="isColumnVisible('description')" class="px-5 py-3 text-muted-foreground max-w-[360px] truncate text-sm">{{ cat.description || '—' }}</td>
                        <td v-if="isColumnVisible('createdBy')" class="px-5 py-3">
                          <div v-if="(cat.createdBy || cat.created_by)?.username" class="flex items-center gap-1.5">
                            <div class="w-5 h-5 rounded-full bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center">
                              <User class="h-3 w-3 text-zinc-500" />
                            </div>
                            <span class="text-xs text-zinc-700 dark:text-zinc-300 font-medium">{{ (cat.createdBy || cat.created_by).username }}</span>
                          </div>
                          <span v-else class="text-zinc-300 dark:text-zinc-600 text-sm">—</span>
                        </td>
                        <td v-if="isColumnVisible('createdAt')" class="px-5 py-3 text-xs text-zinc-400 dark:text-zinc-500">{{ formatDate(cat.createdAt || cat.created_at) }}</td>
                        <td class="px-5 py-3 text-right" @click.stop>
                          <div class="flex justify-end gap-1">
                            <Button
                              v-if="can('category.delete') && !isSuperAdmin"
                              variant="ghost"
                              size="icon"
                              class="h-7 w-7 text-zinc-400 hover:text-destructive hover:bg-destructive/10 rounded-md opacity-0 group-hover:opacity-100 transition-opacity duration-200"
                              @click="doDelete(cat)"
                            >
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

        <!-- ─── Premium Inline Form View ─── -->
        <div v-else key="form-view" class="flex flex-col gap-6">
          <!-- Form Header -->
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-zinc-200 dark:border-zinc-800 pb-4">
            <div class="flex items-center gap-3">
              <Button
                variant="outline"
                size="icon"
                @click="closeForm"
                :disabled="saving"
                class="h-9 w-9 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors"
              >
                <ArrowLeft class="h-4 w-4" />
              </Button>
              <div>
                <h2 class="text-xl font-bold tracking-tight flex items-center gap-2">
                  <span>{{ modalMode === 'create' ? 'Tambah Kategori' : 'Edit Kategori' }}</span>
                  <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-primary/10 text-primary border border-primary/20 uppercase tracking-wider">
                    {{ modalMode === 'create' ? 'Baru' : 'Modifikasi' }}
                  </span>
                </h2>
                <p class="text-xs text-muted-foreground mt-0.5">
                  {{ modalMode === 'create' ? 'Lengkapi detail untuk menambahkan kategori baru.' : 'Perbarui informasi kategori.' }}
                </p>
              </div>
            </div>
            <div class="flex items-center gap-3 w-full sm:w-auto">
              <Button variant="outline" @click="closeForm" :disabled="saving" class="flex-1 sm:flex-none">Batal</Button>
              <Button @click="saveCategory" :disabled="saving" class="flex-1 sm:flex-none bg-primary text-primary-foreground hover:bg-primary/95 shadow-md shadow-primary/20">
                <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
                <span>{{ modalMode === 'create' ? 'Simpan Kategori' : 'Perbarui' }}</span>
              </Button>
            </div>
          </div>

          <!-- Form Error Alert -->
          <Alert v-if="formError" variant="destructive">
            <p class="text-sm font-medium">{{ formError }}</p>
          </Alert>

          <!-- Form Grid -->
          <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">

            <!-- LEFT: Category Icon Preview -->
            <div class="lg:col-span-4 space-y-4">
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <div class="flex items-center justify-between border-b pb-3 border-zinc-100 dark:border-zinc-800">
                    <div>
                      <Label class="text-sm font-semibold text-zinc-950 dark:text-zinc-50">Pratinjau Kategori</Label>
                      <p class="text-[10px] text-muted-foreground mt-0.5">Tampilan kategori secara visual.</p>
                    </div>
                  </div>

                  <!-- Category Preview Card -->
                  <div class="flex flex-col items-center justify-center py-6 gap-4">
                    <div
                      class="w-20 h-20 rounded-2xl flex items-center justify-center text-3xl font-bold select-none shadow-lg border-2 transition-all duration-300"
                      :style="categoryAvatarStyle(form.name || 'K')"
                    >
                      {{ (form.name || 'K').charAt(0).toUpperCase() }}
                    </div>
                    <div class="text-center space-y-1">
                      <p class="font-semibold text-base text-zinc-900 dark:text-zinc-100 transition-all">
                        {{ form.name || 'Nama Kategori' }}
                      </p>
                      <div v-if="selectedParentLabel" class="inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full bg-violet-50 text-violet-700 dark:bg-violet-900/20 dark:text-violet-300 border border-violet-100 dark:border-violet-800/40">
                        <Folder class="h-3 w-3" />
                        {{ selectedParentLabel }}
                      </div>
                      <p class="text-xs text-muted-foreground line-clamp-2 mt-1 max-w-[200px]">{{ form.description || 'Deskripsi kategori...' }}</p>
                    </div>
                  </div>

                  <!-- Stats summary -->
                  <div class="grid grid-cols-2 gap-3 pt-2 border-t border-zinc-100 dark:border-zinc-800">
                    <div class="rounded-lg bg-zinc-50 dark:bg-zinc-900/30 border border-zinc-100 dark:border-zinc-800 p-3 text-center">
                      <p class="text-[10px] font-semibold text-muted-foreground uppercase tracking-wide">Karakter</p>
                      <p class="text-lg font-bold text-zinc-800 dark:text-zinc-200 mt-1">{{ form.name.length }}<span class="text-xs text-muted-foreground font-normal">/100</span></p>
                    </div>
                    <div class="rounded-lg bg-zinc-50 dark:bg-zinc-900/30 border border-zinc-100 dark:border-zinc-800 p-3 text-center">
                      <p class="text-[10px] font-semibold text-muted-foreground uppercase tracking-wide">Hierarki</p>
                      <p class="text-lg font-bold text-zinc-800 dark:text-zinc-200 mt-1">{{ selectedParentLabel ? 'Sub' : 'Root' }}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>

            <!-- RIGHT: Form Fields -->
            <div class="lg:col-span-8 space-y-6">

              <!-- Card 1: Info Utama -->
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <h3 class="text-sm font-semibold text-zinc-950 dark:text-zinc-50 border-b pb-2 border-zinc-100 dark:border-zinc-800 flex items-center gap-2">
                    <FolderOpen class="h-4 w-4 text-primary" />
                    <span>Informasi Utama</span>
                  </h3>

                  <!-- Nama Kategori -->
                  <div class="space-y-1.5">
                    <Label for="c-name" class="text-xs font-semibold">Nama Kategori <span class="text-destructive">*</span></Label>
                    <Input
                      id="c-name"
                      v-model="form.name"
                      placeholder="Contoh: Elektronik, Pakaian, Makanan"
                      :disabled="saving"
                      class="h-10 rounded-lg"
                      :class="{ 'border-destructive ring-destructive/20': formErrors.name }"
                    />
                    <p v-if="formErrors.name" class="text-xs text-destructive form-error font-semibold">{{ formErrors.name }}</p>
                  </div>

                  <!-- Deskripsi -->
                  <div class="space-y-1.5">
                    <Label for="c-desc" class="text-xs font-semibold">
                      Deskripsi <span class="text-muted-foreground text-[10px] font-normal">(opsional)</span>
                    </Label>
                    <textarea
                      id="c-desc"
                      v-model="form.description"
                      rows="4"
                      :disabled="saving"
                      placeholder="Jelaskan kategori ini secara singkat..."
                      :class="[
                        'flex min-h-[100px] w-full rounded-lg border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 resize-none transition-colors',
                        formErrors.description ? 'border-destructive ring-destructive/20' : ''
                      ]"
                    />
                    <div class="flex items-center justify-between">
                      <p v-if="formErrors.description" class="text-xs text-destructive form-error font-semibold">{{ formErrors.description }}</p>
                      <p class="text-[10px] text-muted-foreground ml-auto">{{ form.description.length }} / 255</p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <!-- Card 2: Hierarki & Pengaturan -->
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <h3 class="text-sm font-semibold text-zinc-950 dark:text-zinc-50 border-b pb-2 border-zinc-100 dark:border-zinc-800 flex items-center gap-2">
                    <LayoutList class="h-4 w-4 text-primary" />
                    <span>Hierarki & Pengaturan</span>
                  </h3>

                  <!-- Parent Category Picker -->
                  <div class="space-y-1.5">
                    <Label class="text-xs font-semibold">
                      Kategori Parent <span class="text-muted-foreground text-[10px] font-normal">(opsional – biarkan kosong untuk kategori utama)</span>
                    </Label>

                    <div ref="parentPickerRef" class="relative">
                      <!-- Trigger button -->
                      <button
                        type="button"
                        :disabled="saving"
                        @click="showParentPicker = !showParentPicker"
                        class="w-full flex items-center justify-between h-10 px-3 rounded-lg border border-input bg-background text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 transition-colors hover:border-primary/40"
                      >
                        <span v-if="selectedParentLabel" class="flex items-center gap-2 text-zinc-800 dark:text-zinc-200 font-medium">
                          <Folder class="h-4 w-4 text-violet-500" />
                          {{ selectedParentLabel }}
                        </span>
                        <span v-else class="text-muted-foreground">— Tidak ada (Root) —</span>
                        <ChevronDown class="h-4 w-4 text-muted-foreground shrink-0" :class="showParentPicker ? 'rotate-180' : ''" style="transition: transform 0.2s" />
                      </button>

                      <!-- Dropdown -->
                      <Transition name="fade">
                        <div
                          v-if="showParentPicker"
                          class="absolute left-0 top-full mt-1 z-30 w-full bg-card border border-border rounded-lg shadow-xl overflow-hidden"
                        >
                          <!-- Search inside picker -->
                          <div class="p-2 border-b border-border">
                            <input
                              v-model="parentSearchQuery"
                              type="text"
                              placeholder="Cari kategori..."
                              class="w-full h-8 px-3 rounded-md border border-input bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring placeholder:text-muted-foreground"
                            />
                          </div>

                          <div class="max-h-52 overflow-y-auto py-1">
                            <!-- None option -->
                            <button
                              type="button"
                              @click="selectParent(null)"
                              class="w-full flex items-center gap-2 px-3 py-2 hover:bg-muted/50 text-sm text-left transition-colors"
                              :class="!form.parentId ? 'text-primary font-semibold bg-primary/5' : 'text-muted-foreground'"
                            >
                              <X class="h-3.5 w-3.5 shrink-0" />
                              Tidak ada (Root)
                              <Check v-if="!form.parentId" class="h-3.5 w-3.5 ml-auto text-primary" />
                            </button>

                            <!-- Category options -->
                            <button
                              v-for="cat in parentCandidates"
                              :key="cat.id"
                              type="button"
                              @click="selectParent(cat)"
                              class="w-full flex items-center gap-2 px-3 py-2 hover:bg-muted/50 text-sm text-left transition-colors"
                              :class="form.parentId === cat.id ? 'text-primary font-semibold bg-primary/5' : 'text-foreground'"
                            >
                              <Folder class="h-3.5 w-3.5 shrink-0 text-violet-500" />
                              <span class="flex-1 truncate">{{ cat.name }}</span>
                              <span v-if="cat.parent?.name" class="text-[10px] text-muted-foreground truncate max-w-[80px]">{{ cat.parent.name }}</span>
                              <Check v-if="form.parentId === cat.id" class="h-3.5 w-3.5 shrink-0 text-primary" />
                            </button>

                            <p v-if="parentCandidates.length === 0 && parentSearchQuery" class="text-xs text-muted-foreground text-center py-4">
                              Tidak ada kategori ditemukan.
                            </p>
                          </div>
                        </div>
                      </Transition>
                    </div>

                    <!-- Info about hierarchy -->
                    <div class="flex items-start gap-2 p-3 rounded-lg bg-violet-50/50 dark:bg-violet-900/10 border border-violet-100 dark:border-violet-800/30">
                      <ChevronRight class="h-3.5 w-3.5 text-violet-500 mt-0.5 shrink-0" />
                      <p class="text-[11px] text-violet-700 dark:text-violet-400 leading-relaxed">
                        Pilih parent untuk menjadikan kategori ini <strong>sub-kategori</strong>. Contoh: <em>"Smartphone"</em> di bawah <em>"Elektronik"</em>.
                      </p>
                    </div>
                  </div>

                  <!-- Sort Order (optional) -->
                  <div class="space-y-1.5">
                    <Label for="c-sort" class="text-xs font-semibold">
                      Urutan <span class="text-muted-foreground text-[10px] font-normal">(opsional)</span>
                    </Label>
                    <Input
                      id="c-sort"
                      v-model.number="form.sortOrder"
                      type="number"
                      min="0"
                      placeholder="0 – biarkan untuk urutan otomatis"
                      :disabled="saving"
                      class="h-10 rounded-lg"
                    />
                    <p class="text-[10px] text-muted-foreground">Nilai lebih kecil akan ditampilkan lebih awal. Biarkan kosong untuk urutan otomatis.</p>
                  </div>
                </CardContent>
              </Card>

            </div>
          </div>
        </div>

      </Transition>
    </div>
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

.table-lift-row {
  transition: box-shadow 0.15s ease, background-color 0.15s ease;
}
.table-lift-row:hover {
  box-shadow: 0 2px 8px 0 rgba(0,0,0,0.06);
}
</style>
