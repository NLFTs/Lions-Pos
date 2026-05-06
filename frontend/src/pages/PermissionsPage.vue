<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
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
import { Plus, Pencil, Trash2, Loader2, X, KeyRound, LayoutGrid, ChevronDown, Filter, Check } from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()

// ─── State ────────────────────────────────────────────────────────────────────
const permissions  = ref([])
const modules      = ref([])
const loading      = ref(false)
const error        = ref(null)
const filterModule = ref('')
const searchQuery  = ref('')
const page         = ref(1)
const pageSize     = ref(10)
const showFilter   = ref(false)
const filterRef    = ref(null)

const activeFilterCount = computed(() => {
  return filterModule.value ? 1 : 0
})

function handleOutsideClick(e) {
  if (filterRef.value && !filterRef.value.contains(e.target)) {
    showFilter.value = false
  }
}

onMounted(() => {
  fetchPermissions()
  fetchModules()
  document.addEventListener('click', handleOutsideClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
})
const showDrawer   = ref(false)
const modalMode    = ref('create')
const saving    = ref(false)
const formError = ref(null)
const form      = ref({ id: null, slug: '', name: '', moduleId: null })

// ─── Computed ─────────────────────────────────────────────────────────────────
const filtered = computed(() => {
  let result = permissions.value
  if (filterModule.value) {
    result = result.filter((p) => p.moduleSlug === filterModule.value)
  }
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(p => 
      p.name.toLowerCase().includes(q) || 
      p.slug.toLowerCase().includes(q)
    )
  }
  return result
})

const paginatedPermissions = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filtered.value.slice(start, start + pageSize.value)
})

watch([searchQuery, filterModule], () => {
  page.value = 1
})

// ─── Fetch ────────────────────────────────────────────────────────────────────
async function fetchPermissions() {
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/api/v1/permissions')
    permissions.value = res.data.data
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load permissions.'
  } finally {
    loading.value = false
  }
}

async function fetchModules() {
  try {
    const res = await api.get('/api/v1/modules')
    modules.value = res.data.data
  } catch { /* non-critical */ }
}



// ─── Modal ────────────────────────────────────────────────────────────────────
function openCreate() {
  modalMode.value = 'create'
  form.value = { id: null, slug: '', name: '', moduleId: null }
  formError.value = null
  showDrawer.value = true
}

function openEdit(perm) {
  modalMode.value = 'edit'
  const mod = modules.value.find((m) => m.slug === perm.moduleSlug)
  form.value = { id: perm.id, slug: perm.slug, name: perm.name, moduleId: mod?.id ?? null }
  formError.value = null
  showDrawer.value = true
}

function closeDrawer() {
  showDrawer.value = false
}

function onNameInput() {
  if (modalMode.value !== 'create') return
  const selected = modules.value.find((m) => m.id === form.value.moduleId)
  const mod = selected?.slug ?? ''
  const name = form.value.name.trim().toLowerCase()
  let action = ''
  if (name.startsWith('view all'))                              action = 'index'
  else if (name.startsWith('view') || name.startsWith('show')) action = 'show'
  else if (name.startsWith('create'))                          action = 'store'
  else if (name.startsWith('update') || name.startsWith('edit')) action = 'update'
  else if (name.startsWith('delete') || name.startsWith('remove')) action = 'delete'
  form.value.slug = mod && action ? `${mod}.${action}` : mod ? `${mod}.` : ''
}

function onModuleSelect() {
  onNameInput()
}

async function savePermission() {
  formError.value = null
  saving.value = true
  try {
    if (modalMode.value === 'create') {
      await api.post('/api/v1/permissions', {
        slug:     form.value.slug,
        name:     form.value.name,
        moduleId: form.value.moduleId,
      })
      toast.success('Permission created!')
    } else {
      await api.put(`/api/v1/permissions/${form.value.id}`, {
        name:     form.value.name,
        moduleId: form.value.moduleId,
      })
      toast.success('Permission updated!')
    }
    showDrawer.value = false
    fetchPermissions()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Failed to save permission.'
  } finally {
    saving.value = false
  }
}

// ─── Delete ───────────────────────────────────────────────────────────────────
async function doDelete(perm) {
  const ok = await confirm({
    title: 'Delete Permission',
    description: `Are you sure you want to delete "${perm.slug}"? Roles with this permission will lose it immediately.`,
  })
  if (!ok) return

  try {
    await api.delete(`/api/v1/permissions/${perm.id}`)
    toast.success('Permission deleted!')
    fetchPermissions()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Failed to delete permission.')
  }
}
</script>

<template>
  <AppLayout>
    <div class="pb-6">
      <!-- Page Header -->
      <div class="mb-6">
        <h1 class="text-xl font-bold tracking-tight text-zinc-900">Permission Management</h1>
        <p class="text-xs text-zinc-500 mt-0.5">
          Manage granular system permissions.
        </p>
      </div>

      <!-- Table Controls -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5">
        <DataTableSearch
          v-model="searchQuery"
          placeholder="Search permissions..."
          class="w-full max-w-sm"
          input-class="h-9 text-xs"
        />
        <div class="flex items-center gap-2 w-full sm:w-auto">
          <!-- Filter Dropdown -->
          <div ref="filterRef" class="relative flex-1 sm:flex-none">
            <button
              @click="showFilter = !showFilter"
              class="w-full flex items-center justify-center gap-2 h-9 px-3 rounded-md border border-border bg-background hover:bg-accent text-foreground text-sm font-medium transition-colors"
            >
              <Filter class="h-3.5 w-3.5" />
              <span>Filter</span>
              <!-- Active count badge -->
              <span v-if="activeFilterCount > 0" class="inline-flex items-center justify-center h-4.5 min-w-[18px] px-1 rounded-full bg-primary text-primary-foreground text-[10px] font-bold leading-none">
                {{ activeFilterCount }}
              </span>
              <ChevronDown class="h-3 w-3 text-muted-foreground" :class="showFilter ? 'rotate-180' : ''" style="transition: transform 0.2s" />
            </button>

            <!-- Dropdown Panel -->
            <Transition name="fade">
              <div
                v-if="showFilter"
                class="absolute left-0 sm:left-auto sm:right-0 top-full mt-1 z-30 w-[calc(100vw-2.5rem)] sm:w-64 max-w-[280px] sm:max-w-none bg-card border border-border rounded-lg shadow-xl overflow-hidden"
              >
                <!-- Header -->
                <div class="flex items-center justify-between px-3 py-2.5 border-b border-border">
                  <span class="text-xs font-semibold text-foreground uppercase tracking-wide">Filter</span>
                  <button
                    v-if="activeFilterCount > 0"
                    @click="filterModule = ''"
                    class="text-xs text-red-500 hover:text-red-600 font-medium transition-colors"
                  >Clear all</button>
                </div>

                <!-- Module Section -->
                <div class="px-3 pt-3 pb-3">
                  <p class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2">Modul</p>
                  <div class="space-y-1 max-h-[240px] overflow-y-auto pr-1 custom-scrollbar">
                    <button
                      @click="filterModule = ''"
                      class="w-full flex items-center justify-between px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors outline-none text-left"
                    >
                      <span class="text-sm font-medium text-foreground select-none">Semua Modul</span>
                      <Check v-if="filterModule === ''" class="h-4 w-4 text-foreground" />
                    </button>
                    <button
                      v-for="mod in modules"
                      :key="mod.slug"
                      @click="filterModule = mod.slug"
                      class="w-full flex items-center justify-between px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors outline-none text-left"
                    >
                      <span class="text-sm font-medium text-foreground select-none capitalize">{{ mod.name }}</span>
                      <Check v-if="filterModule === mod.slug" class="h-4 w-4 text-foreground" />
                    </button>
                  </div>
                </div>
              </div>
            </Transition>
          </div>
          <!-- /Filter Dropdown -->

          <Button v-if="can('permission.store')" @click="openCreate" size="sm" class="flex-1 sm:flex-none bg-primary hover:bg-primary/90 flex items-center justify-center gap-2">
            <Plus class="h-4 w-4" />
            <span>Add Permission</span>
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

        <div v-else-if="filtered.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
          <KeyRound class="h-10 w-10 mb-3 opacity-40" />
          <p class="text-sm">No permissions found.</p>
        </div>

        <div v-else>
          <!-- ─── Mobile List View ─── -->
          <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
            <div
              v-for="perm in paginatedPermissions"
              :key="'mobile-' + perm.id"
              class="p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
            >
              <div class="flex items-start justify-between gap-3">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-lg bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-500 font-bold shrink-0 border border-zinc-200 dark:border-zinc-800/50">
                    <KeyRound class="h-5 w-5" />
                  </div>
                  <div>
                    <h4 class="font-medium text-sm text-zinc-900 dark:text-zinc-100 leading-tight">{{ perm.name }}</h4>
                    <span class="font-mono text-[10px] text-primary bg-primary/5 px-1.5 py-0.5 rounded border border-primary/10 mt-1.5 block w-fit tracking-tight">{{ perm.slug }}</span>
                  </div>
                </div>
                
                <div class="flex items-center gap-1 shrink-0">
                  <Button
                    v-if="can('permission.update')"
                    variant="ghost"
                    size="icon"
                    class="h-8 w-8 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 bg-zinc-50 dark:bg-zinc-800/50"
                    @click="openEdit(perm)"
                  >
                    <Pencil class="h-3.5 w-3.5" />
                  </Button>
                  <Button
                    v-if="can('permission.delete')"
                    variant="ghost"
                    size="icon"
                    class="h-8 w-8 text-zinc-400 hover:text-destructive bg-zinc-50 dark:bg-zinc-800/50"
                    @click="doDelete(perm)"
                  >
                    <Trash2 class="h-3.5 w-3.5" />
                  </Button>
                </div>
              </div>
              <div class="flex items-center justify-between mt-1">
                <span class="text-[10px] text-zinc-400 uppercase tracking-wider font-semibold">Modul</span>
                <span class="inline-block rounded-full bg-zinc-100 dark:bg-zinc-800/80 px-2 py-0.5 text-[10px] capitalize font-medium text-zinc-600 dark:text-zinc-400 border border-zinc-200/50 dark:border-zinc-800/50">
                  {{ perm.moduleSlug }}
                </span>
              </div>
            </div>
          </div>

          <!-- ─── Desktop Table ─── -->
          <div class="hidden md:block overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="border-b bg-muted/40">
                  <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Slug</th>
                  <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Nama Permission</th>
                  <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Modul</th>
                  <th class="px-5 py-3 text-right font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="perm in paginatedPermissions" :key="perm.id" class="border-b last:border-0 hover:bg-muted/30 transition-colors">
                  <td class="px-5 py-3 font-mono text-xs text-primary">{{ perm.slug }}</td>
                  <td class="px-5 py-3 font-medium text-zinc-900 dark:text-zinc-100">{{ perm.name }}</td>
                  <td class="px-5 py-3 text-xs">
                    <span class="inline-block rounded-full bg-muted px-2.5 py-0.5 text-xs capitalize text-zinc-600 dark:text-zinc-400 font-medium">
                      {{ perm.moduleSlug }}
                    </span>
                  </td>
                  <td class="px-5 py-3 text-right">
                    <div class="flex justify-end gap-1">
                      <Button v-if="can('permission.update')" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-zinc-700" @click="openEdit(perm)">
                        <Pencil class="h-3.5 w-3.5" />
                      </Button>
                      <Button v-if="can('permission.delete')" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-destructive" @click="doDelete(perm)">
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
          v-if="filtered.length > 0 && !loading"
          :page="page"
          :page-size="pageSize"
          :total="filtered.length"
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
                {{ modalMode === 'create' ? 'Tambah Permission' : 'Edit Permission' }}
              </h3>
              <p class="text-xs text-muted-foreground mt-0.5">
                {{ modalMode === 'create' ? 'Isi detail permission baru.' : 'Perbarui informasi permission.' }}
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
              <Label for="permModule">Modul</Label>
              <select 
                id="permModule" 
                v-model="form.moduleId" 
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50" 
                @change="onModuleSelect"
                :disabled="saving"
              >
                <option :value="null" disabled> Pilih modul </option>
                <option v-for="mod in modules" :key="mod.id" :value="mod.id">{{ mod.name }} ({{ mod.slug }})</option>
              </select>
            </div>

            <div class="space-y-1.5">
              <Label for="permName">Nama Permission <span class="text-destructive">*</span></Label>
              <Input 
                id="permName" 
                v-model="form.name" 
                placeholder="Contoh: View All Posts" 
                @input="onNameInput" 
                :disabled="saving"
              />
            </div>

            <div class="space-y-1.5">
              <Label for="permSlug">
                Slug 
                <span class="ml-1 text-[10px] text-muted-foreground font-normal">
                  {{ modalMode === 'create' ? '(otomatis, format: modul.aksi)' : '(tidak dapat diubah)' }}
                </span>
              </Label>
              <Input 
                id="permSlug" 
                v-model="form.slug" 
                :disabled="modalMode === 'edit' || saving" 
                placeholder="Contoh: post.index" 
                class="font-mono text-xs" 
                :class="modalMode === 'edit' ? 'bg-muted' : ''" 
              />
            </div>
          </div>

          <!-- Footer -->
          <div class="flex justify-end gap-3 px-6 py-4 border-t shrink-0 bg-muted/30">
            <Button variant="outline" @click="closeDrawer" :disabled="saving">Batal</Button>
            <Button @click="savePermission" :disabled="saving">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              {{ modalMode === 'create' ? 'Simpan Permission' : 'Simpan Perubahan' }}
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
