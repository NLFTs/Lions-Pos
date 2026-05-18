<script setup>

import { ref, computed, onMounted, watch } from 'vue'
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

onMounted(() => fetchCategories())

// ─── Create / Edit ────────────────────────────────────────────────────────────
function openCreate() {
  form.value = { id: null, name: '', description: '' }
  formError.value = null
  modalMode.value = 'create'
  showDrawer.value = true
}

function closeDrawer() {
  showDrawer.value = false
}

function openEdit(cat) {
  form.value = { id: cat.id, name: cat.name, description: cat.description || '' }
  formError.value = null
  modalMode.value = 'edit'
  showDrawer.value = true
}

async function saveCategory() {
  formError.value = null
  saving.value = true
  try {
    if (modalMode.value === 'create') {
      await api.post('/api/v1/categories', { name: form.value.name, description: form.value.description })
      toast.success('Category created!')
    } else {
      await api.put(`/api/v1/categories/${form.value.id}`, { name: form.value.name, description: form.value.description })
      toast.success('Category updated!')
    }
    showDrawer.value = false
    fetchCategories()
  } catch (err) {
    formError.value = err.response?.data?.data?.message
      || err.response?.data?.message
      || 'Failed to save category.'
  } finally {
    saving.value = false
  }
}

// ─── Delete ───────────────────────────────────────────────────────────────────
async function doDelete(cat) {
  const ok = await confirm({
    title: 'Delete Category',
    description: `Are you sure you want to delete "${cat.name}"? Posts in this category will be uncategorized.`,
  })
  if (!ok) return

  try {
    await api.delete(`/api/v1/categories/${cat.id}`)
    toast.success('Category deleted!')
    fetchCategories()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Failed to delete category.')
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
        <h1 class="text-xl font-bold tracking-tight text-zinc-900">Category Management</h1>
        <p class="text-xs text-zinc-500 mt-0.5">
          Manage categories for content grouping.
        </p>
      </div>

      <!-- Table Controls -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5">
        <DataTableSearch
          v-model="searchQuery"
          placeholder="Search categories..."
          class="w-full max-w-sm"
          input-class="h-9 text-xs"
        />
        <div class="flex items-center gap-2 w-full sm:w-auto">
          <Button variant="outline" size="sm" class="flex-1 sm:flex-none flex items-center justify-center gap-2 border-zinc-200">
            <LayoutGrid class="h-3.5 w-3.5" />
            <span>Customize Columns</span>
            <ChevronDown class="h-3 w-3 text-zinc-400" />
          </Button>
          <Button v-if="can('category.store')" @click="openCreate" size="sm" class="flex-1 sm:flex-none bg-primary hover:bg-primary/90 flex items-center justify-center gap-2">
            <Plus class="h-4 w-4" />
            <span>Add Category</span>
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
          <p class="text-sm">No categories yet.</p>
          <Button v-if="can('category.store') && !searchQuery" variant="outline" class="mt-3" @click="openCreate">Create your first category</Button>
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
                    v-if="can('category.update')"
                    variant="ghost"
                    size="icon"
                    class="h-8 w-8 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 bg-zinc-50 dark:bg-zinc-800/50"
                    @click="openEdit(cat)"
                  >
                    <Pencil class="h-3.5 w-3.5" />
                  </Button>
                  <Button
                    v-if="can('category.delete')"
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
                  <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px] w-16">#</th>
                  <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Nama Kategori</th>
                  <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Deskripsi</th>
                  <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Dibuat</th>
                  <th class="px-5 py-3 text-right font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="cat in paginatedCategories"
                  :key="cat.id"
                  class="border-b last:border-0 hover:bg-muted/30 transition-colors"
                >
                  <td class="px-5 py-3 text-muted-foreground font-mono text-xs">{{ cat.id }}</td>
                  <td class="px-5 py-3 font-medium text-zinc-900 dark:text-zinc-100">{{ cat.name }}</td>
                  <td class="px-5 py-3 text-muted-foreground max-w-[400px] truncate">{{ cat.description || '-' }}</td>
                  <td class="px-5 py-3 text-muted-foreground text-xs">{{ formatDate(cat.createdAt) }}</td>
                  <td class="px-5 py-3 text-right">
                    <div class="flex justify-end gap-1">
                      <Button v-if="can('category.update')" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-zinc-700" @click="openEdit(cat)">
                        <Pencil class="h-3.5 w-3.5" />
                      </Button>
                      <Button v-if="can('category.delete')" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-destructive" @click="doDelete(cat)">
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
      <Transition name="slide-right">
        <div
          v-if="showDrawer"
          class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[420px] h-full bg-card shadow-2xl sm:border-l overflow-hidden"
        >
          <!-- Header -->
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
              <X class="h-4 w-4" />
            </Button>
          </div>

          <!-- Body (scrollable) -->
          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5">
            <Alert v-if="formError" variant="destructive">
              <p class="text-sm">{{ formError }}</p>
            </Alert>

            <div class="space-y-1.5">
              <Label for="c-name">Nama Kategori <span class="text-destructive">*</span></Label>
              <Input id="c-name" v-model="form.name" placeholder="Nama kategori" :disabled="saving" />
            </div>

            <div class="space-y-1.5">
              <Label for="c-desc">Deskripsi</Label>
              <textarea
                id="c-desc"
                v-model="form.description"
                rows="4"
                :disabled="saving"
                placeholder="Deskripsi opsional..."
                class="flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 resize-none"
              />
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
      </Transition>
    </Teleport>
  </AppLayout>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-right-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
}
</style>
