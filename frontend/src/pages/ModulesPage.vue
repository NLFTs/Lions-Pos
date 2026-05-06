<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Alert from '@/components/ui/Alert.vue'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import api from '@/lib/api'
import { Plus, Pencil, Trash2, Loader2, X, Boxes, LayoutGrid, ChevronDown } from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()

// ─── State ────────────────────────────────────────────────────────────────────
const modules    = ref([])
const loading    = ref(false)
const error      = ref(null)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

const filteredModules = computed(() => {
  if (!searchQuery.value) return modules.value
  const q = searchQuery.value.toLowerCase()
  return modules.value.filter(m => 
    m.name.toLowerCase().includes(q) || 
    m.slug.toLowerCase().includes(q)
  )
})

const paginatedModules = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredModules.value.slice(start, start + pageSize.value)
})

watch(searchQuery, () => {
  page.value = 1
})

// Drawer
const showDrawer = ref(false)
const modalMode = ref('create')
const saving    = ref(false)
const formError = ref(null)
const form      = ref({ id: null, slug: '', name: '', description: '' })

// ─── Fetch ────────────────────────────────────────────────────────────────────
async function fetchModules() {
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/api/v1/modules')
    modules.value = res.data.data
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load modules.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchModules)

// ─── Modal ────────────────────────────────────────────────────────────────────
function openCreate() {
  modalMode.value = 'create'
  form.value = { id: null, slug: '', name: '', description: '' }
  formError.value = null
  showDrawer.value = true
}

function openEdit(mod) {
  modalMode.value = 'edit'
  form.value = { id: mod.id, slug: mod.slug, name: mod.name, description: mod.description ?? '' }
  formError.value = null
  showDrawer.value = true
}

function closeDrawer() {
  showDrawer.value = false
}

// Bagian yang diubah: Menggunakan '-' (dash)
function onNameInput() {
  if (modalMode.value !== 'create') return
  form.value.slug = form.value.name
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-') // Mengganti karakter non-alfanumerik dengan dash
    .replace(/^-+|-+$/g, '')    // Menghapus dash di awal atau akhir
}

async function saveModule() {
  formError.value = null
  saving.value = true
  try {
    if (modalMode.value === 'create') {
      await api.post('/api/v1/modules', {
        slug:        form.value.slug,
        name:        form.value.name,
        description: form.value.description || null,
      })
      toast.success('Module created!')
    } else {
      await api.put(`/api/v1/modules/${form.value.id}`, {
        name:        form.value.name,
        description: form.value.description || null,
      })
      toast.success('Module updated!')
    }
    showDrawer.value = false
    fetchModules()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Failed to save module.'
  } finally {
    saving.value = false
  }
}

// ─── Delete ───────────────────────────────────────────────────────────────────
async function doDelete(mod) {
  const ok = await confirm({
    title: 'Delete Module',
    description: `Are you sure you want to delete "${mod.slug}"? All permissions in this module will lose their module reference.`,
  })
  if (!ok) return

  try {
    await api.delete(`/api/v1/modules/${mod.id}`)
    toast.success('Module deleted!')
    fetchModules()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Failed to delete module.')
  }
}
</script>

<template>
  <AppLayout>
    <div class="pb-6">
      <!-- Page Header -->
      <div class="mb-6">
        <h1 class="text-xl font-bold tracking-tight text-zinc-900">Module Management</h1>
        <p class="text-xs text-zinc-500 mt-0.5">
          Manage system modules for access control.
        </p>
      </div>

      <!-- Table Controls -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5">
        <DataTableSearch
          v-model="searchQuery"
          placeholder="Search modules..."
          class="w-full max-sm"
          input-class="h-9 text-xs"
        />
        <div class="flex items-center gap-2 w-full sm:w-auto">
          <Button variant="outline" size="sm" class="flex-1 sm:flex-none flex items-center justify-center gap-2 border-zinc-200">
            <LayoutGrid class="h-3.5 w-3.5" />
            <span>Customize Columns</span>
            <ChevronDown class="h-3 w-3 text-zinc-400" />
          </Button>
          <Button v-if="can('module.store')" @click="openCreate" size="sm" class="flex-1 sm:flex-none bg-primary hover:bg-primary/90 flex items-center justify-center gap-2">
            <Plus class="h-4 w-4" />
            <span>Add Module</span>
          </Button>
        </div>
      </div>

      <Alert v-if="error" variant="destructive" class="mb-4">{{ error }}</Alert>

      <!-- Table -->
      <Card>
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-20">
            <Loader2 class="h-6 w-6 animate-spin text-muted-foreground" />
          </div>

          <div v-else-if="filteredModules.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
          <Boxes class="h-10 w-10 mb-3 opacity-40" />
          <p class="text-sm">No modules found.</p>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b bg-muted/40">
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">Slug</th>
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">Name</th>
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">Description</th>
                <th class="px-4 py-3 text-right font-medium text-muted-foreground">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="mod in paginatedModules" :key="mod.id" class="border-b last:border-0 hover:bg-muted/30 transition-colors">
                <td class="px-4 py-3 font-mono text-xs">{{ mod.slug }}</td>
                <td class="px-4 py-3 font-medium">{{ mod.name }}</td>
                <td class="px-4 py-3 text-muted-foreground text-xs">{{ mod.description ?? '—' }}</td>
                <td class="px-4 py-3 text-right">
                  <div class="flex justify-end gap-2">
                    <Button v-if="can('module.update')" variant="ghost" size="icon" @click="openEdit(mod)">
                      <Pencil class="h-4 w-4" />
                    </Button>
                    <Button v-if="can('module.delete')" variant="ghost" size="icon" class="text-destructive hover:text-destructive" @click="doDelete(mod)">
                      <Trash2 class="h-4 w-4" />
                    </Button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <DataTablePagination
          v-if="filteredModules.length > 0 && !loading"
          :page="page"
          :page-size="pageSize"
          :total="filteredModules.length"
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
                {{ modalMode === 'create' ? 'Tambah Modul' : 'Edit Modul' }}
              </h3>
              <p class="text-xs text-muted-foreground mt-0.5">
                {{ modalMode === 'create' ? 'Isi detail modul baru.' : 'Perbarui informasi modul.' }}
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
              <Label for="modName">Nama Modul <span class="text-destructive">*</span></Label>
              <Input id="modName" v-model="form.name" placeholder="Contoh: Post" @input="onNameInput" :disabled="saving" />
            </div>

            <div class="space-y-1.5">
              <Label for="modSlug">
                Slug 
                <span class="ml-1 text-[10px] text-muted-foreground font-normal">
                  {{ modalMode === 'create' ? '(otomatis, huruf kecil/angka/-)' : '(tidak dapat diubah)' }}
                </span>
              </Label>
              <Input id="modSlug" v-model="form.slug" :disabled="modalMode === 'edit' || saving" placeholder="contoh-modul" class="font-mono text-xs" :class="modalMode === 'edit' ? 'bg-muted' : ''" />
            </div>

            <div class="space-y-1.5">
              <Label for="modDesc">Deskripsi <span class="text-xs text-muted-foreground font-normal">(opsional)</span></Label>
              <textarea
                id="modDesc"
                v-model="form.description"
                rows="4"
                :disabled="saving"
                placeholder="Deskripsi singkat modul..."
                class="flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 resize-none"
              />
            </div>
          </div>

          <!-- Footer -->
          <div class="flex justify-end gap-3 px-6 py-4 border-t shrink-0 bg-muted/30">
            <Button variant="outline" @click="closeDrawer" :disabled="saving">Batal</Button>
            <Button @click="saveModule" :disabled="saving">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              {{ modalMode === 'create' ? 'Simpan Modul' : 'Simpan Perubahan' }}
            </Button>
          </div>
        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>