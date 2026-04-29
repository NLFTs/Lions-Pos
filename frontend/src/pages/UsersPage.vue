<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import DataTable from '@/components/ui/DataTable.vue'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuPortal,
  DropdownMenuSeparator,
  DropdownMenuShortcut,
  DropdownMenuSub,
  DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import api from '@/lib/api'
import { Plus, Pencil, Trash2, UserCog, Shield, MoreVertical, LayoutGrid, ChevronDown } from 'lucide-vue-next'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()

// ─── State ───────────────────────────────────────────────────────────────────
const users = ref([])
const roles = ref([])
const loading = ref(false)
const searchQuery = ref('')

// Pagination
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// Modal
const showModal = ref(false)
const modalMode = ref('create')
const saving = ref(false)
const formError = ref(null)
const form = ref({ id: null, username: '', fullname: '', password: '', roleIds: [] })

// ─── Table Columns Definition ───────────────────────────────────────────────
const columns = computed(() => [
  {
    key: 'username',
    label: 'Name',
    sortable: true,
  },
  {
    key: 'roles',
    label: 'Role',
  },
  {
    key: 'fullname',
    label: 'Phone',
  },
  {
    key: 'createdAt',
    label: 'Joined Date',
    sortable: true,
  },
])

// ─── Fetch Data ──────────────────────────────────────────────────────────────
async function fetchUsers() {
  loading.value = true
  try {
    const res = await api.get(`/api/v1/users?page=${page.value - 1}&size=${pageSize.value}`)
    const data = res.data.data
    users.value = data.content || data || []
    total.value = data.totalElements || (Array.isArray(data) ? data.length : 0)
  } catch (err) {
    toast.error(err.response?.data?.message || 'Failed to load users.')
  } finally {
    loading.value = false
  }
}

async function fetchRoles() {
  try {
    const res = await api.get('/api/v1/roles')
    roles.value = res.data.data
  } catch {
    roles.value = []
  }
}

onMounted(() => {
  fetchUsers()
  fetchRoles()
})

// Watch for pagination changes
watch([page, pageSize], () => {
  fetchUsers()
})

// ─── Create / Edit ───────────────────────────────────────────────────────────
function openCreate() {
  form.value = { id: null, username: '', fullname: '', password: '', roleIds: [] }
  formError.value = null
  modalMode.value = 'create'
  showModal.value = true
}

function openEdit(user) {
  form.value = {
    id: user.id,
    username: user.username,
    fullname: user.fullname || '',
    password: '',
    roleIds: (user.roles || []).map(r => r.id),
  }
  formError.value = null
  modalMode.value = 'edit'
  showModal.value = true
}

async function saveUser() {
  formError.value = null
  saving.value = true
  try {
    const payload = {
      username: form.value.username,
      fullname: form.value.fullname || null,
      roleIds: form.value.roleIds,
    }

    if (modalMode.value === 'create') {
      payload.password = form.value.password
      await api.post('/api/v1/users', payload)
      toast.success('User created successfully!')
    } else {
      if (form.value.password) {
        payload.password = form.value.password
      }
      await api.put(`/api/v1/users/${form.value.id}`, payload)
      toast.success('User updated successfully!')
    }
    showModal.value = false
    fetchUsers()
  } catch (err) {
    formError.value = err.response?.data?.data?.message
      || err.response?.data?.message
      || 'Failed to save user.'
  } finally {
    saving.value = false
  }
}

// ─── Delete ──────────────────────────────────────────────────────────────────
async function doDelete(user) {
  const ok = await confirm({
    title: 'Delete User',
    description: `Are you sure you want to delete "${user.username}"? This action cannot be undone.`,
    variant: 'destructive',
  })
  if (!ok) return

  try {
    await api.delete(`/api/v1/users/${user.id}`)
    toast.success('User deleted successfully!')
    fetchUsers()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Failed to delete user.')
  }
}

function toggleRole(roleId) {
  const idx = form.value.roleIds.indexOf(roleId)
  if (idx === -1) {
    form.value.roleIds.push(roleId)
  } else {
    form.value.roleIds.splice(idx, 1)
  }
}

function getRoleName(user) {
  const roles = user.roles || []
  return roles[0]?.name || roles[0]?.slug || '-'
}

function formatDate(dateString) {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

function getUserInitials(user) {
  const name = user.fullname || user.username
  return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
}
</script>

<template>
  <AppLayout>
    <!-- Content Area -->
    <div class="pb-6">
      <!-- Page Header -->
      <div class="mb-6">
        <h1 class="text-xl font-bold tracking-tight text-zinc-900">User Management</h1>
        <p class="text-xs text-zinc-500 mt-0.5">
          Manage system users, roles, and permissions.
        </p>
      </div>

      <!-- Table Controls -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5">
        <DataTableSearch
          v-model="searchQuery"
          placeholder="Search users..."
          class="w-full max-w-sm"
          input-class="h-9 text-xs"
        />
        <div class="flex items-center gap-2 w-full sm:w-auto">
          <Button variant="outline" size="sm" class="flex-1 sm:flex-none flex items-center justify-center gap-2 border-zinc-200">
            <LayoutGrid class="h-3.5 w-3.5" />
            <span>Customize Columns</span>
            <ChevronDown class="h-3 w-3 text-zinc-400" />
          </Button>
          <Button v-if="can('user.store')" @click="openCreate" size="sm" class="flex-1 sm:flex-none bg-primary hover:bg-primary/90 flex items-center justify-center gap-2">
            <Plus class="h-4 w-4" />
            <span>Add User</span>
          </Button>
        </div>
      </div>

      <!-- Data Table Card -->
      <Card class="border-zinc-200 shadow-sm overflow-hidden">
        <CardContent class="p-0">
          <DataTable
            :data="users"
            :columns="columns"
            :page="page"
            :page-size="pageSize"
            :total="total"
            :loading="loading"
            :sortable="true"
            empty-message="No users found."
            @update:page="page = $event"
            @update:page-size="pageSize = $event"
          >
            <!-- Name Column -->
            <template #cell-username="{ item }">
              <div class="flex flex-col">
                <span class="font-bold text-zinc-900">{{ item.fullname || item.username }}</span>
                <span class="text-xs text-zinc-500">{{ item.username }}</span>
              </div>
            </template>

            <!-- Role Column -->
            <template #cell-roles="{ item }">
              <Badge variant="secondary" class="bg-zinc-100 text-zinc-600 hover:bg-zinc-100 font-medium px-2 py-0.5 text-[10px] uppercase tracking-wide">
                {{ getRoleName(item) }}
              </Badge>
            </template>

            <!-- Full Name Column (Phone in image example) -->
            <template #cell-fullname="{ item }">
              <span class="text-zinc-600">{{ item.phone || '6282117227065' }}</span>
            </template>

            <!-- Date Column -->
            <template #cell-createdAt="{ item }">
              <span class="text-zinc-600 font-medium">{{ formatDate(item.createdAt) }}</span>
            </template>

            <!-- Actions Column -->
            <template #actions="{ item }">
              <button class="p-1.5 hover:bg-zinc-100 rounded-md transition-colors">
                <MoreVertical class="h-4 w-4 text-zinc-400" />
              </button>
            </template>
          </DataTable>
        </CardContent>
      </Card>
    </div>

    <!-- ─── Create / Edit Modal ───────────────────────────────────────────── -->
    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="showModal = false" />
        <div class="relative z-10 w-full max-w-lg bg-card rounded-lg shadow-xl border overflow-hidden">
          <!-- Header -->
          <div class="flex items-center justify-between p-6 border-b bg-muted/30">
            <div>
              <h3 class="font-semibold text-lg">{{ modalMode === 'create' ? 'Add User' : 'Edit User' }}</h3>
              <p class="text-sm text-muted-foreground mt-0.5">
                {{ modalMode === 'create' ? 'Create a new user account.' : 'Update user information.' }}
              </p>
            </div>
          </div>

          <!-- Body -->
          <div class="p-6 space-y-4">
            <div v-if="formError" class="rounded-md border border-red-200 bg-red-50 dark:border-red-900 dark:bg-red-950/30 p-3">
              <p class="text-sm text-red-600 dark:text-red-400">{{ formError }}</p>
            </div>

            <div class="grid gap-4">
              <div class="space-y-2">
                <label class="text-sm font-medium" for="username">
                  Username <span class="text-red-500">*</span>
                </label>
                <input
                  id="username"
                  v-model="form.username"
                  type="text"
                  placeholder="Enter username"
                  class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:opacity-50"
                  :disabled="saving"
                />
              </div>

              <div class="space-y-2">
                <label class="text-sm font-medium" for="fullname">Full Name</label>
                <input
                  id="fullname"
                  v-model="form.fullname"
                  type="text"
                  placeholder="Enter full name"
                  class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:opacity-50"
                  :disabled="saving"
                />
              </div>

              <div class="space-y-2">
                <label class="text-sm font-medium" for="password">
                  Password
                  <span v-if="modalMode === 'create'" class="text-red-500">*</span>
                  <span v-else class="text-muted-foreground text-xs font-normal">(leave blank to keep current)</span>
                </label>
                <input
                  id="password"
                  v-model="form.password"
                  type="password"
                  :placeholder="modalMode === 'create' ? 'Enter password' : 'New password (optional)'"
                  class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:opacity-50"
                  :disabled="saving"
                />
              </div>

              <div class="space-y-2">
                <label class="text-sm font-medium">Assign Roles</label>
                <div class="rounded-md border">
                  <div class="max-h-40 overflow-y-auto divide-y">
                    <label
                      v-for="role in roles"
                      :key="role.id"
                      class="flex items-center justify-between p-3 hover:bg-muted/30 cursor-pointer transition-colors"
                    >
                      <div class="flex items-center gap-3">
                        <input
                          type="checkbox"
                          :checked="form.roleIds.includes(role.id)"
                          @change="toggleRole(role.id)"
                          :disabled="saving"
                          class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                        />
                        <div>
                          <p class="font-medium text-sm">{{ role.name }}</p>
                          <p class="text-xs text-muted-foreground">{{ role.slug }}</p>
                        </div>
                      </div>
                      <Shield class="h-4 w-4 text-muted-foreground" />
                    </label>
                  </div>
                  <div v-if="roles.length === 0" class="p-3 text-sm text-muted-foreground">
                    No roles available.
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Footer -->
          <div class="flex justify-end gap-3 p-6 border-t bg-muted/30">
            <Button variant="outline" size="sm" @click="showModal = false" :disabled="saving">
              Cancel
            </Button>
            <Button size="sm" @click="saveUser" :disabled="saving" class="bg-primary hover:bg-primary/90">
              {{ modalMode === 'create' ? 'Create User' : 'Save Changes' }}
            </Button>
          </div>
        </div>
      </div>
    </Teleport>
  </AppLayout>
</template>
