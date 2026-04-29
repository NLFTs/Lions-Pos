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
import { Plus, Pencil, Trash2, ShieldCheck, Loader2, X, LayoutGrid, ChevronDown } from 'lucide-vue-next'
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

// Modal state
const showModal  = ref(false)
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
  if (selectedPermIds.value.has(p.id)) {
    selectedPermIds.value.delete(p.id)
  } else {
    selectedPermIds.value.add(p.id)
  }
}

function toggleAllInModule(module) {
  const list = permissionsMap.value[module] || []
  const allChecked = list.every((p) => selectedPermIds.value.has(p.id))
  if (allChecked) {
    list.forEach((p) => selectedPermIds.value.delete(p.id))
  } else {
    list.forEach((p) => selectedPermIds.value.add(p.id))
  }
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
  showModal.value = true
}

function openEdit(role) {
  modalMode.value = 'edit'
  form.value = { id: role.id, name: role.name, slug: role.slug }
  selectedPermIds.value = new Set((role.permissions || []).map((p) => p.id))
  formError.value = null
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

function onNameInput() {
  if (modalMode.value === 'create') {
    form.value.slug = form.value.name
      .toLowerCase()
      .replace(/\s+/g, '_')
      .replace(/[^a-z0-9_-]/g, '')
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

    showModal.value = false
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

        <div v-else class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b bg-muted/40">
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">Name</th>
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">Slug</th>
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">Permissions</th>
                <th class="px-4 py-3 text-right font-medium text-muted-foreground">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="role in paginatedRoles"
                :key="role.id"
                class="border-b last:border-0 hover:bg-muted/30 transition-colors"
              >
                <td class="px-4 py-3 font-medium">{{ role.name }}</td>
                <td class="px-4 py-3 font-mono text-xs text-muted-foreground">{{ role.slug }}</td>
                <td class="px-4 py-3">
                  <span class="inline-flex items-center gap-1 rounded-full bg-primary/10 text-primary px-2.5 py-0.5 text-xs font-medium">
                    <ShieldCheck class="h-3 w-3" />
                    {{ permCountFor(role) }} permission{{ permCountFor(role) !== 1 ? 's' : '' }}
                  </span>
                </td>
                <td class="px-4 py-3 text-right">
                  <div class="flex justify-end gap-2">
                    <Button v-if="can('role.update')" variant="ghost" size="icon" @click="openEdit(role)">
                      <Pencil class="h-4 w-4" />
                    </Button>
                    <Button
                      v-if="can('role.delete')"
                      variant="ghost"
                      size="icon"
                      class="text-destructive hover:text-destructive"
                      @click="doDelete(role)"
                    >
                      <Trash2 class="h-4 w-4" />
                    </Button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
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

    <!-- ─── Create / Edit Modal ──────────────────────────────────────────────── -->
    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="closeModal" />

        <div class="relative z-10 w-full max-w-2xl max-h-[90vh] overflow-y-auto rounded-lg bg-card shadow-xl border">
          <div class="flex items-center justify-between border-b px-6 py-4">
            <h3 class="font-semibold text-lg">
              {{ modalMode === 'create' ? 'New Role' : 'Edit Role' }}
            </h3>
            <button @click="closeModal" class="text-muted-foreground hover:text-foreground transition-colors">
              <X class="h-5 w-5" />
            </button>
          </div>

          <div class="px-6 py-4 space-y-5">
            <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <Label for="roleName">Name</Label>
                <Input id="roleName" v-model="form.name" placeholder="e.g. Editor" @input="onNameInput" />
              </div>
              <div class="space-y-1.5">
                <Label for="roleSlug">Slug <span class="ml-1 text-xs text-muted-foreground font-normal">(lowercase, a-z 0-9 _ -)</span></Label>
                <Input id="roleSlug" v-model="form.slug" placeholder="e.g. editor" class="font-mono" />
              </div>
            </div>

            <!-- Permission Matrix -->
            <div>
              <p class="text-sm font-medium mb-3">Permissions</p>

              <div v-if="modules.length === 0" class="text-sm text-muted-foreground py-4 text-center">
                Loading permissions…
              </div>

              <div v-else class="rounded-md border overflow-hidden">
                <table class="w-full text-sm">
                  <thead>
                    <tr class="bg-muted/40 border-b">
                      <th class="px-4 py-2 text-left font-medium text-muted-foreground w-32">Module</th>
                      <th class="px-3 py-2 text-center font-medium text-muted-foreground capitalize w-20" v-for="action in ACTIONS" :key="action">
                        {{ action }}
                      </th>
                      <th class="px-3 py-2 text-center font-medium text-muted-foreground w-16">All</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="module in modules" :key="module" class="border-b last:border-0 hover:bg-muted/20">
                      <td class="px-4 py-3 font-medium capitalize">{{ module }}</td>
                      <td v-for="action in ACTIONS" :key="action" class="px-3 py-3 text-center">
                        <template v-if="getPermission(module, action)">
                          <input type="checkbox" :checked="isChecked(module, action)" @change="togglePerm(module, action)" class="h-4 w-4 cursor-pointer accent-primary" />
                        </template>
                        <template v-else>
                          <span class="text-muted-foreground/40 text-xs">—</span>
                        </template>
                      </td>
                      <td class="px-3 py-3 text-center">
                        <input type="checkbox" :checked="isModuleAllChecked(module)" @change="toggleAllInModule(module)" class="h-4 w-4 cursor-pointer accent-primary" />
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div class="flex justify-end gap-3 border-t px-6 py-4">
            <Button variant="outline" @click="closeModal">Cancel</Button>
            <Button :disabled="saving" @click="saveRole">
              <Loader2 v-if="saving" class="h-4 w-4 mr-1 animate-spin" />
              {{ modalMode === 'create' ? 'Create Role' : 'Save Changes' }}
            </Button>
          </div>
        </div>
      </div>
    </Teleport>
  </AppLayout>
</template>
