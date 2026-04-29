<script setup>
import { ref, computed, onMounted, watch } from 'vue'
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

// Modal
const showModal = ref(false)
const modalMode = ref('create')
const saving = ref(false)
const formError = ref(null)
const form = ref({ id: null, name: '', description: '' })

// ─── Fetch ────────────────────────────────────────────────────────────────────
async function fetchCategories() {
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/api/v1/categories')
    categories.value = res.data.data
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
  showModal.value = true
}

function openEdit(cat) {
  form.value = { id: cat.id, name: cat.name, description: cat.description || '' }
  formError.value = null
  modalMode.value = 'edit'
  showModal.value = true
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
    showModal.value = false
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

        <div v-else class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b bg-muted/40">
                <th class="px-4 py-3 text-left font-medium text-muted-foreground w-12">#</th>
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">Name</th>
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">Description</th>
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">Created</th>
                <th class="px-4 py-3 text-right font-medium text-muted-foreground">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="cat in paginatedCategories"
                :key="cat.id"
                class="border-b last:border-0 hover:bg-muted/30 transition-colors"
              >
                <td class="px-4 py-3 text-muted-foreground">{{ cat.id }}</td>
                <td class="px-4 py-3 font-medium">{{ cat.name }}</td>
                <td class="px-4 py-3 text-muted-foreground max-w-[320px] truncate">{{ cat.description || '-' }}</td>
                <td class="px-4 py-3 text-muted-foreground">{{ formatDate(cat.createdAt) }}</td>
                <td class="px-4 py-3 text-right">
                  <div class="flex justify-end gap-2">
                    <Button v-if="can('category.update')" variant="ghost" size="icon" @click="openEdit(cat)">
                      <Pencil class="h-4 w-4" />
                    </Button>
                    <Button v-if="can('category.delete')" variant="ghost" size="icon" class="text-destructive hover:text-destructive" @click="doDelete(cat)">
                      <Trash2 class="h-4 w-4" />
                    </Button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
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

    <!-- ─── Create / Edit Modal ─── -->
    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/50" @click="showModal = false" />
        <div class="relative z-10 w-full max-w-md mx-4 bg-card rounded-lg shadow-xl border">
          <div class="flex items-center justify-between p-6 border-b">
            <h3 class="font-semibold text-lg">{{ modalMode === 'create' ? 'New Category' : 'Edit Category' }}</h3>
            <Button variant="ghost" size="icon" @click="showModal = false">
              <X class="h-4 w-4" />
            </Button>
          </div>

          <div class="p-6 space-y-4">
            <Alert v-if="formError" variant="destructive">
              <p class="text-sm">{{ formError }}</p>
            </Alert>

            <div class="space-y-2">
              <Label for="c-name">Name <span class="text-destructive">*</span></Label>
              <Input id="c-name" v-model="form.name" placeholder="Category name" :disabled="saving" />
            </div>

            <div class="space-y-2">
              <Label for="c-desc">Description</Label>
              <textarea
                id="c-desc"
                v-model="form.description"
                rows="3"
                :disabled="saving"
                placeholder="Optional description..."
                class="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 resize-none"
              />
            </div>
          </div>

          <div class="flex justify-end gap-3 p-6 border-t">
            <Button variant="outline" @click="showModal = false" :disabled="saving">Cancel</Button>
            <Button @click="saveCategory" :disabled="saving">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              {{ modalMode === 'create' ? 'Create' : 'Save Changes' }}
            </Button>
          </div>
        </div>
      </div>
    </Teleport>
  </AppLayout>
</template>
