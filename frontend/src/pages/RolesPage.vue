<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Alert from '@/components/ui/Alert.vue'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import api from '@/lib/api'
import { Plus, Pencil, Trash2, ShieldCheck, Loader2, X, LayoutGrid, ChevronDown, Check } from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()

// ─── State ────────────────────────────────────────────────────────────────────
const roles            = ref([])
const permissionsMap   = ref({})   // { module: [PermissionResponse, ...] }
const loading          = ref(false)
const error            = ref(null)
const searchQuery      = ref('')
const page             = ref(1)
const pageSize         = ref(10)

const filteredRoles = computed(() => {
  if (!searchQuery.value) return roles.value
  const q = searchQuery.value.toLowerCase()
  return roles.value.filter(r => 
    r.name.toLowerCase().includes(q) || 
    r.slug.toLowerCase().includes(q)
  )
})

const paginatedRoles = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredRoles.value.slice(start, start + pageSize.value)
})

watch(searchQuery, () => {
  page.value = 1
})

// Drawer state
const showDrawer = ref(false)
const modalMode  = ref('create')   // 'create' | 'edit'
const saving     = ref(false)
const formError  = ref(null)

const form = ref({ id: null, name: '', slug: '' })
const selectedPermIds = ref(new Set())

// Ordered actions for matrix header columns
const ACTIONS = ['index', 'show', 'store', 'update', 'delete']

// Sorted module keys
const modules = computed(() => Object.keys(permissionsMap.value).sort())

// ─── Fetch ────────────────────────────────────────────────────────────────────
async function fetchRoles() {
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/api/v1/roles')
    roles.value = res.data.data
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load roles.'
  } finally {
    loading.value = false
  }
}

async function fetchPermissions() {
  try {
    const res = await api.get('/api/v1/roles/permissions')
    permissionsMap.value = res.data.data
  } catch (err) {
    console.error('Failed to load permissions', err)
  }
}

onMounted(() => {
  fetchRoles()
  fetchPermissions()
})

// ─── Helpers ──────────────────────────────────────────────────────────────────
function getPermission(module, action) {
  const list = permissionsMap.value[module] || []
  return list.find((p) => p.slug === `${module}.${action}`) || null
}

function isChecked(module, action) {
  const p = getPermission(module, action)
  return p ? selectedPermIds.value.has(p.id) : false
}

function togglePerm(module, action) {
  const p = getPermission(module, action)
  if (!p) return
  const next = new Set(selectedPermIds.value)
  if (next.has(p.id)) {
    next.delete(p.id)
  } else {
    next.add(p.id)
  }
  selectedPermIds.value = next
}

function toggleAllInModule(module) {
  const list = permissionsMap.value[module] || []
  const allChecked = list.every((p) => selectedPermIds.value.has(p.id))
  const next = new Set(selectedPermIds.value)
  if (allChecked) {
    list.forEach((p) => next.delete(p.id))
  } else {
    list.forEach((p) => next.add(p.id))
  }
  selectedPermIds.value = next
}

function isModuleAllChecked(module) {
  const list = permissionsMap.value[module] || []
  return list.length > 0 && list.every((p) => selectedPermIds.value.has(p.id))
}

function isModuleSomeChecked(module) {
  const list = permissionsMap.value[module] || []
  return list.some((p) => selectedPermIds.value.has(p.id)) && !isModuleAllChecked(module)
}

function permCountFor(role) {
  return role.permissions?.length ?? 0
}

// ─── Modal ───────────────────────────────────────────────────────────────────
function openCreate() {
  modalMode.value = 'create'
  form.value = { id: null, name: '', slug: '' }
  selectedPermIds.value = new Set()
  formError.value = null
  showDrawer.value = true
}

function openEdit(role) {
  modalMode.value = 'edit'
  form.value = { id: role.id, name: role.name, slug: role.slug }
  selectedPermIds.value = new Set((role.permissions || []).map((p) => p.id))
  formError.value = null
  showDrawer.value = true
}

function closeDrawer() {
  showDrawer.value = false
}

function onNameInput() {
  if (modalMode.value === 'create') {
    form.value.slug = form.value.name
      .toLowerCase()
      .replace(/\s+/g, '-')
      .replace(/[^a-z0-9-]/g, '')
  }
}

async function saveRole() {
  formError.value = null
  saving.value = true
  try {
    let roleId = form.value.id

    if (modalMode.value === 'create') {
      const payload = {
        name: form.value.name,
        slug: form.value.slug,
        permissionIds: [...selectedPermIds.value],
      }
      const res = await api.post('/api/v1/roles', payload)
      roleId = res.data.data.id
      toast.success('Role created!')
    } else {
      await api.put(`/api/v1/roles/${roleId}`, {
        name: form.value.name,
        slug: form.value.slug,
      })
      await api.put(`/api/v1/roles/${roleId}/permissions`, {
        permissionIds: [...selectedPermIds.value],
      })
      toast.success('Role updated!')
    }

    showDrawer.value = false
    fetchRoles()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Failed to save role.'
  } finally {
    saving.value = false
  }
}

// ─── Delete ───────────────────────────────────────────────────────────────────
async function doDelete(role) {
  const ok = await confirm({
    title: 'Delete Role',
    description: `Are you sure you want to delete "${role.name}"? This cannot be undone.`,
  })
  if (!ok) return

  try {
    await api.delete(`/api/v1/roles/${role.id}`)
    toast.success('Role deleted!')
    fetchRoles()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Failed to delete role.')
  }
}
</script>

<template>
  <AppLayout>
    <div class="pb-6">
      <!-- Page Header -->
      <div class="mb-6">
        <h1 class="text-xl font-bold tracking-tight text-zinc-900">Role Management</h1>
        <p class="text-xs text-zinc-500 mt-0.5">
          Manage system roles and their permissions.
        </p>
      </div>

      <!-- Table Controls -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5">
        <DataTableSearch
          v-model="searchQuery"
          placeholder="Search roles..."
          class="w-full max-w-sm"
          input-class="h-9 text-xs"
        />
        <div class="flex items-center gap-2 w-full sm:w-auto">
          <Button variant="outline" size="sm" class="flex-1 sm:flex-none flex items-center justify-center gap-2 border-zinc-200">
            <LayoutGrid class="h-3.5 w-3.5" />
            <span>Customize Columns</span>
            <ChevronDown class="h-3 w-3 text-zinc-400" />
          </Button>
          <Button v-if="can('role.store')" @click="openCreate" size="sm" class="flex-1 sm:flex-none bg-primary hover:bg-primary/90 flex items-center justify-center gap-2">
            <Plus class="h-4 w-4" />
            <span>Add Role</span>
          </Button>
        </div>
      </div>

      <!-- Error -->
      <Alert v-if="error" variant="destructive" class="mb-4">{{ error }}</Alert>

    <!-- Roles table -->
    <Card>
      <CardContent class="p-0">
        <div v-if="loading" class="flex items-center justify-center py-20">
          <Loader2 class="h-6 w-6 animate-spin text-muted-foreground" />
        </div>

        <div v-else-if="filteredRoles.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
          <ShieldCheck class="h-10 w-10 mb-3 opacity-40" />
          <p class="text-sm">No roles yet.</p>
        </div>

        <div v-else>
          <!-- ─── Mobile List View ─── -->
          <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
            <div
              v-for="role in paginatedRoles"
              :key="'mobile-' + role.id"
              class="p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
            >
              <div class="flex items-start justify-between gap-3">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-lg bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-500 font-bold shrink-0 border border-zinc-200 dark:border-zinc-800/50">
                    <ShieldCheck class="h-5 w-5" />
                  </div>
                  <div>
                    <h4 class="font-medium text-sm text-zinc-900 dark:text-zinc-100 leading-tight">{{ role.name }}</h4>
                    <span class="font-mono text-[10px] text-zinc-400 mt-1 block">{{ role.slug }}</span>
                  </div>
                </div>
                
                <div class="flex items-center gap-1 shrink-0">
                  <Button
                    v-if="can('role.update')"
                    variant="ghost"
                    size="icon"
                    class="h-8 w-8 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 bg-zinc-50 dark:bg-zinc-800/50"
                    @click="openEdit(role)"
                  >
                    <Pencil class="h-3.5 w-3.5" />
                  </Button>
                  <Button
                    v-if="can('role.delete')"
                    variant="ghost"
                    size="icon"
                    class="h-8 w-8 text-zinc-400 hover:text-destructive bg-zinc-50 dark:bg-zinc-800/50"
                    @click="doDelete(role)"
                  >
                    <Trash2 class="h-3.5 w-3.5" />
                  </Button>
                </div>
              </div>
              <div class="flex items-center justify-between mt-1">
                <span class="text-[10px] text-zinc-400 uppercase tracking-wider font-semibold">Hak Akses</span>
                <span class="inline-flex items-center gap-1 rounded-full bg-primary/10 text-primary px-2.5 py-0.5 text-[10px] font-semibold border border-primary/10">
                  <ShieldCheck class="h-3 w-3" />
                  {{ permCountFor(role) }} Permission
                </span>
              </div>
            </div>
          </div>

          <!-- ─── Desktop Table ─── -->
          <div class="hidden md:block overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="border-b bg-muted/40">
                  <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Nama Role</th>
                  <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Slug</th>
                  <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Permissions</th>
                  <th class="px-5 py-3 text-right font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="role in paginatedRoles"
                  :key="role.id"
                  class="border-b last:border-0 hover:bg-muted/30 transition-colors"
                >
                  <td class="px-5 py-3 font-medium text-zinc-900 dark:text-zinc-100">{{ role.name }}</td>
                  <td class="px-5 py-3 font-mono text-xs text-muted-foreground">{{ role.slug }}</td>
                  <td class="px-5 py-3">
                    <span class="inline-flex items-center gap-1 rounded-full bg-primary/10 text-primary px-2.5 py-0.5 text-xs font-medium border border-primary/10">
                      <ShieldCheck class="h-3 w-3" />
                      {{ permCountFor(role) }} permission{{ permCountFor(role) !== 1 ? 's' : '' }}
                    </span>
                  </td>
                  <td class="px-5 py-3 text-right">
                    <div class="flex justify-end gap-1">
                      <Button v-if="can('role.update')" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-zinc-700" @click="openEdit(role)">
                        <Pencil class="h-3.5 w-3.5" />
                      </Button>
                      <Button
                        v-if="can('role.delete')"
                        variant="ghost"
                        size="icon"
                        class="h-8 w-8 text-zinc-400 hover:text-destructive"
                        @click="doDelete(role)"
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
          v-if="filteredRoles.length > 0 && !loading"
          :page="page"
          :page-size="pageSize"
          :total="filteredRoles.length"
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
          class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[500px] h-full bg-card shadow-2xl sm:border-l overflow-hidden"
        >
          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
            <div>
              <h3 class="font-semibold text-base">
                {{ modalMode === 'create' ? 'Tambah Role' : 'Edit Role' }}
              </h3>
              <p class="text-xs text-muted-foreground mt-0.5">
                {{ modalMode === 'create' ? 'Isi detail role dan pilih permission.' : 'Perbarui informasi role dan permission.' }}
              </p>
            </div>
            <Button variant="ghost" size="icon" @click="closeDrawer">
              <X class="h-4 w-4" />
            </Button>
          </div>

          <!-- Body (scrollable) -->
          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-6">
            <Alert v-if="formError" variant="destructive">
              <p class="text-sm">{{ formError }}</p>
            </Alert>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <Label for="roleName">Nama Role <span class="text-destructive">*</span></Label>
                <Input id="roleName" v-model="form.name" placeholder="Contoh: Editor" @input="onNameInput" :disabled="saving" />
              </div>
              <div class="space-y-1.5">
                <Label for="roleSlug">Slug</Label>
                <Input id="roleSlug" v-model="form.slug" placeholder="contoh-role" class="font-mono text-xs" :disabled="saving" />
              </div>
            </div>

            <!-- Permission Matrix -->
            <div class="space-y-3">
              <div class="flex items-center justify-between">
                <Label class="text-sm font-semibold">Hak Akses (Permissions)</Label>
                <span class="text-[10px] text-muted-foreground bg-muted px-1.5 py-0.5 rounded">
                  {{ selectedPermIds.size }} dipilih
                </span>
              </div>

              <div v-if="modules.length === 0" class="flex items-center justify-center py-10 border rounded-lg bg-muted/20">
                <Loader2 class="h-5 w-5 animate-spin text-muted-foreground/50 mr-2" />
                <span class="text-sm text-muted-foreground">Memuat data hak akses…</span>
              </div>

              <div v-else class="rounded-lg border border-zinc-200 dark:border-zinc-800 overflow-hidden">
                <div class="overflow-x-auto">
                  <table class="w-full text-xs">
                    <thead>
                      <tr class="bg-muted/50 border-b border-zinc-200 dark:border-zinc-800">
                        <th class="px-4 py-2.5 text-left font-semibold text-zinc-600 dark:text-zinc-400">Modul</th>
                        <th class="px-2 py-2.5 text-center font-semibold text-zinc-600 dark:text-zinc-400 capitalize" v-for="action in ACTIONS" :key="action">
                          {{ action }}
                        </th>
                        <th class="px-3 py-2.5 text-center font-semibold text-zinc-600 dark:text-zinc-400">Semua</th>
                      </tr>
                    </thead>
                    <tbody class="divide-y divide-zinc-100 dark:divide-zinc-800/60">
                      <tr v-for="module in modules" :key="module" class="hover:bg-zinc-50 dark:hover:bg-zinc-900/40 transition-colors">
                        <td class="px-4 py-3 font-medium capitalize text-zinc-900 dark:text-zinc-100">{{ module }}</td>
                        <td v-for="action in ACTIONS" :key="action" class="px-2 py-4 text-center">
                          <div v-if="getPermission(module, action)" class="flex justify-center">
                            <button 
                              type="button"
                              @click="togglePerm(module, action)"
                              class="h-6 w-6 rounded-md flex items-center justify-center transition-all duration-200"
                              :class="isChecked(module, action) 
                                ? 'bg-primary/10 border-primary shadow-sm' 
                                : 'bg-transparent border-zinc-200 dark:border-zinc-800 hover:border-zinc-400 dark:hover:border-zinc-600'"
                            >
                              <div 
                                class="h-4 w-4 rounded-sm border flex items-center justify-center transition-all"
                                :class="isChecked(module, action) ? 'border-primary bg-white dark:bg-zinc-900' : 'border-zinc-300 dark:border-zinc-700'"
                              >
                                <Check v-if="isChecked(module, action)" class="h-3 w-3 text-primary stroke-[4]" />
                              </div>
                            </button>
                          </div>
                          <div v-else class="text-zinc-200 dark:text-zinc-800 select-none text-[10px]">—</div>
                        </td>
                        <td class="px-3 py-4 text-center bg-zinc-50/30 dark:bg-zinc-900/10">
                          <div class="flex justify-center">
                            <button 
                              type="button"
                              @click="toggleAllInModule(module)"
                              class="h-6 w-6 rounded-md flex items-center justify-center transition-all duration-200"
                              :class="isModuleAllChecked(module) 
                                ? 'bg-primary/10 border-primary shadow-sm' 
                                : 'bg-transparent border-zinc-200 dark:border-zinc-800 hover:border-zinc-400 dark:hover:border-zinc-600'"
                            >
                              <div 
                                class="h-4 w-4 rounded-sm border flex items-center justify-center transition-all"
                                :class="isModuleAllChecked(module) ? 'border-primary bg-white dark:bg-zinc-900' : 'border-zinc-300 dark:border-zinc-700'"
                              >
                                <Check v-if="isModuleAllChecked(module)" class="h-3 w-3 text-primary stroke-[4]" />
                              </div>
                            </button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>

          <!-- Footer -->
          <div class="flex justify-end gap-3 px-6 py-4 border-t shrink-0 bg-muted/30">
            <Button variant="outline" @click="closeDrawer" :disabled="saving">Batal</Button>
            <Button @click="saveRole" :disabled="saving">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              {{ modalMode === 'create' ? 'Simpan Role' : 'Simpan Perubahan' }}
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
