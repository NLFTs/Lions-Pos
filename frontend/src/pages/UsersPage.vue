<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import DataTable from '@/components/ui/DataTable.vue'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import { Button } from '@/components/ui/button'
import api from '@/lib/api'
import {
  Plus, Pencil, Trash2, MoreVertical, X, Shield, Eye, EyeOff, ChevronDown, Check,
} from 'lucide-vue-next'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

const { can } = usePermission()
const { toast } = useToast()

// ─── State ──────────────────────────────────────────────────────────
const allUsers = ref([])
const roles = ref([])
const loading = ref(false)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

// Drawer (Sekarang bertindak sebagai Center Modal)
const showDrawer = ref(false)
const drawerMode = ref('create')
const saving = ref(false)
const formError = ref(null)
const showPassword = ref(false)
const form = ref({ id: null, username: '', fullname: '', email: '', password: '', roleIds: [] })
const fieldErrors = ref({ username: '', email: '', password: '' })

function clearFieldErrors() {
  fieldErrors.value = { username: '', email: '', password: '' }
}

function validateForm() {
  clearFieldErrors()
  let valid = true

  const uname = form.value.username.trim()
  if (!uname) {
    fieldErrors.value.username = 'Username wajib diisi.'
    valid = false
  } else if (uname.length < 3) {
    fieldErrors.value.username = 'Username minimal 3 karakter.'
    valid = false
  } else if (uname.length > 30) {
    fieldErrors.value.username = 'Username maksimal 30 karakter.'
    valid = false
  } else if (!/^[a-zA-Z0-9_]+$/.test(uname)) {
    fieldErrors.value.username = 'Username hanya boleh huruf, angka, dan underscore.'
    valid = false
  }

  const email = form.value.email.trim()
  if (!email) {
    fieldErrors.value.email = 'Email wajib diisi.'
    valid = false
  } else if (!email.includes('@') || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    fieldErrors.value.email = 'Masukkan alamat email yang valid (contoh: nama@domain.com).'
    valid = false
  }

  const pwd = form.value.password
  if (drawerMode.value === 'create' && !pwd) {
    fieldErrors.value.password = 'Password wajib diisi.'
    valid = false
  } else if (pwd && pwd.length < 6) {
    fieldErrors.value.password = 'Password minimal 6 karakter.'
    valid = false
  }

  return valid
}
const avatarPreview = ref(null)
const avatarFile = ref(null)
const avatarInputRef = ref(null)

function handleAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  avatarFile.value = file
  const reader = new FileReader()
  reader.onload = (ev) => { avatarPreview.value = ev.target.result }
  reader.readAsDataURL(file)
}

function removeAvatar() {
  avatarPreview.value = null
  avatarFile.value = null
  if (avatarInputRef.value) avatarInputRef.value.value = ''
}

// Delete confirmation dialog
const showDeleteDialog = ref(false)
const deletingUser = ref(null)
const deleteAdminUsername = ref('')
const deleteAdminPassword = ref('')
const showDeletePassword = ref(false)
const deleteError = ref('')

// ─── Columns ────────────────────────────────────────────────────────
const columns = computed(() => [
  { key: 'username', label: 'Pengguna', sortable: true },
  { key: 'email', label: 'Email', sortable: true },
  { key: 'roles', label: 'Role' },
  { key: 'createdAt', label: 'Bergabung', sortable: true },
])

// ─── Filter State ─────────────────────────────────────────────────────
const filterOpen = ref(false)
const activeFilters = ref({
  roles: [],       // array of role slugs
})

// Get unique roles from loaded data
const availableRoles = computed(() => {
  const map = {}
  allUsers.value.forEach(u => {
    ;(u.roles || []).forEach(r => {
      if (r.slug) map[r.slug] = r.name
    })
  })
  return Object.entries(map).map(([slug, name]) => ({ slug, name }))
})

const activeFilterCount = computed(() => {
  return activeFilters.value.roles.length
})

function toggleRoleFilter(slug) {
  const idx = activeFilters.value.roles.indexOf(slug)
  if (idx === -1) activeFilters.value.roles.push(slug)
  else activeFilters.value.roles.splice(idx, 1)
  page.value = 1
}

function clearFilters() {
  activeFilters.value = { roles: [] }
  page.value = 1
}

// ─── Filtered + Paginated Data ──────────────────────────────────────
const filteredUsers = computed(() => {
  let list = Array.isArray(allUsers.value) ? allUsers.value : []

  // Search query
  const q = searchQuery.value.trim().toLowerCase()
  if (q) {
    list = list.filter(u =>
      (u.fullname || '').toLowerCase().includes(q) ||
      (u.username || '').toLowerCase().includes(q) ||
      (u.email || '').toLowerCase().includes(q) ||
      (u.roles || []).some(r => r.name.toLowerCase().includes(q))
    )
  }

  // Role filter (multi-select)
  if (activeFilters.value.roles.length > 0) {
    list = list.filter(u =>
      (u.roles || []).some(r => activeFilters.value.roles.includes(r.slug))
    )
  }

  return list
})

const pagedUsers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredUsers.value.slice(start, start + pageSize.value)
})

const total = computed(() => filteredUsers.value.length)

watch(searchQuery, () => { page.value = 1 })
watch(pageSize, () => { page.value = 1 })
watch(activeFilters, () => { page.value = 1 }, { deep: true })

// ─── Fetch ──────────────────────────────────────────────────────────
async function fetchUsers() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/users?page=0&size=999')
    const data = res.data?.data
    if (!data) {
      allUsers.value = []
      return
    }
    allUsers.value = data.content || data || []
    if (!Array.isArray(allUsers.value)) {
      allUsers.value = []
    }
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal memuat data pengguna.')
  } finally {
    loading.value = false
  }
}

async function fetchRoles() {
  try {
    const res = await api.get('/api/v1/roles')
    roles.value = res.data.data
  } catch { roles.value = [] }
}

onMounted(() => { 
  fetchUsers()
  fetchRoles()
  document.addEventListener('click', handleOutsideClick)
})
onBeforeUnmount(() => { document.removeEventListener('click', handleOutsideClick) })

const filterRef = ref(null)
function handleOutsideClick(e) {
  if (filterRef.value && !filterRef.value.contains(e.target)) {
    filterOpen.value = false
  }
}

// ─── Drawer helpers ─────────────────────────────────────────────────
function openCreate() {
  form.value = { id: null, username: '', fullname: '', email: '', password: '', roleIds: [] }
  avatarPreview.value = null
  avatarFile.value = null
  formError.value = null
  clearFieldErrors()
  showPassword.value = false
  drawerMode.value = 'create'
  showDrawer.value = true
}

function openEdit(user) {
  form.value = {
    id: user.id,
    username: user.username,
    fullname: user.fullname || '',
    email: user.email || '',
    password: '',
    roleIds: (user.roles || []).map(r => r.id),
  }
  avatarPreview.value = user.avatar || null
  avatarFile.value = null
  formError.value = null
  clearFieldErrors()
  showPassword.value = false
  drawerMode.value = 'edit'
  showDrawer.value = true
}

async function saveUser() {
  formError.value = null
  if (!validateForm()) return
  saving.value = true
  try {
    let avatarUrl = undefined
    if (avatarFile.value) {
      const formData = new FormData()
      formData.append('file', avatarFile.value)
      const uploadRes = await api.post('/api/v1/upload/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      avatarUrl = uploadRes.data.data.url
    } else if (avatarPreview.value === null) {
      avatarUrl = ''
    }

    const payload = { 
      username: form.value.username, 
      fullname: form.value.fullname || null, 
      email: form.value.email,
      roleIds: form.value.roleIds 
    }
    if (avatarUrl !== undefined) {
      payload.avatar = avatarUrl
    }
    
    if (drawerMode.value === 'create') {
      payload.password = form.value.password
      await api.post('/api/v1/users', payload)
      toast.success('Pengguna berhasil ditambahkan!')
    } else {
      if (form.value.password) payload.password = form.value.password
      await api.put(`/api/v1/users/${form.value.id}`, payload)
      toast.success('Pengguna berhasil diperbarui!')
    }
    showDrawer.value = false
    fetchUsers()
  } catch (err) {
    formError.value = err.response?.data?.data?.message || err.response?.data?.message || 'Gagal menyimpan pengguna.'
  } finally {
    saving.value = false
  }
}

function toggleRole(roleId) {
  const idx = form.value.roleIds.indexOf(roleId)
  if (idx === -1) form.value.roleIds.push(roleId)
  else form.value.roleIds.splice(idx, 1)
}

// ─── Delete ─────────────────────────────────────────────────────────
function openDelete(user) {
  deletingUser.value = user
  deleteAdminUsername.value = ''
  deleteAdminPassword.value = ''
  deleteError.value = ''
  showDeletePassword.value = false
  showDeleteDialog.value = true
}

function cancelDelete() {
  showDeleteDialog.value = false
  deletingUser.value = null
}

async function confirmDelete() {
  if (!deleteAdminUsername.value || !deleteAdminPassword.value) {
    deleteError.value = 'Masukkan username dan password admin.'
    return
  }
  try {
    deleteError.value = ''
    await api.delete(`/api/v1/users/${deletingUser.value.id}`)
    toast.success(`Pengguna "${deletingUser.value.username}" berhasil dihapus.`)
    showDeleteDialog.value = false
    deletingUser.value = null
    fetchUsers()
  } catch (err) {
    deleteError.value = err.response?.data?.message || 'Gagal menghapus pengguna.'
  }
}

// ─── Utils ──────────────────────────────────────────────────────────
function getRoleName(user) {
  const r = (user.roles || [])[0]
  return r?.name || '-'
}

// Perbaikan kontras Badge warna teks gelap di dark mode, Mip
function getRoleBadgeClass(roleName) {
  if (!roleName || roleName === '-') return 'bg-zinc-100 dark:bg-zinc-800 text-zinc-500 dark:text-zinc-400'
  const n = roleName.toLowerCase()
  if (n === 'admin') return 'bg-zinc-950 dark:bg-zinc-100 text-white dark:text-zinc-950 border-transparent shadow-sm'
  return 'bg-zinc-100 dark:bg-zinc-800/80 text-zinc-600 dark:text-zinc-300 border-zinc-200 dark:border-zinc-700/60'
}

function formatDate(d) {
  if (!d) return '-'
  return new Date(d).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
}

function getUserInitials(user) {
  const name = user.fullname || user.username
  return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
}

const avatarColors = ['bg-zinc-700', 'bg-zinc-600', 'bg-zinc-500', 'bg-zinc-800', 'bg-zinc-900']
function getAvatarColor(user) {
  const idStr = String(user.id || '')
  const hash = idStr.split('').reduce((a, c) => a + c.charCodeAt(0), 0)
  return avatarColors[hash % avatarColors.length]
}
</script>

<template>
  <AppLayout>
    <div class="pb-6">
      <div class="mb-6">
        <h1 class="text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100">Manajemen Pengguna</h1>
        <p class="text-xs text-zinc-500 dark:text-zinc-400 mt-0.5">Kelola akun pengguna, role, dan hak akses.</p>
      </div>

      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5">
        <DataTableSearch v-model="searchQuery" placeholder="Cari nama, username, atau role…" class="w-full max-w-sm" />
        <div class="flex items-center gap-2 w-full sm:w-auto">

          <div ref="filterRef" class="relative flex-1 sm:flex-none">
            <button
              @click="filterOpen = !filterOpen"
              class="w-full flex items-center justify-center gap-2 h-9 px-3 rounded-md border border-border bg-background hover:bg-accent text-foreground text-sm font-medium transition-colors"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg>
              <span>Filter</span>
              <span v-if="activeFilterCount > 0" class="inline-flex items-center justify-center h-4.5 min-w-[18px] px-1 rounded-full bg-primary text-primary-foreground text-[10px] font-bold leading-none">
                {{ activeFilterCount }}
              </span>
              <ChevronDown class="h-3 w-3 text-muted-foreground transition-transform duration-200" :class="filterOpen ? 'rotate-180' : ''" />
            </button>

            <Transition name="fade">
              <div
                v-if="filterOpen"
                class="absolute left-0 sm:left-auto sm:right-0 top-full mt-1 z-30 w-[calc(100vw-2.5rem)] sm:w-64 max-w-[280px] sm:max-w-none bg-card border border-border rounded-lg shadow-xl overflow-hidden"
              >
                <div class="flex items-center justify-between px-3 py-2.5 border-b border-border">
                  <span class="text-xs font-semibold text-foreground uppercase tracking-wide">Filter</span>
                  <button
                    v-if="activeFilterCount > 0"
                    @click="clearFilters"
                    class="text-xs text-red-500 hover:text-red-600 font-medium transition-colors"
                  >Hapus semua</button>
                </div>

                <div class="px-3 pt-3 pb-2">
                  <p class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2">Role</p>
                  <div class="space-y-1">
                    <button
                      v-for="role in availableRoles"
                      :key="role.slug"
                      @click="toggleRoleFilter(role.slug)"
                      class="w-full flex items-center justify-between px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors outline-none text-left"
                    >
                      <span class="text-sm font-medium text-foreground select-none">{{ role.name }}</span>
                      <Check v-if="activeFilters.roles.includes(role.slug)" class="h-4 w-4 text-foreground" />
                    </button>
                    <p v-if="availableRoles.length === 0" class="text-xs text-muted-foreground px-2 py-1">Belum ada role.</p>
                  </div>
                </div>
              </div>
            </Transition>
          </div>

          <Button v-if="can('user.store')" @click="openCreate" size="sm" class="flex-1 sm:flex-none flex items-center justify-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
            <Plus class="h-4 w-4" />
            <span>Tambah User</span>
          </Button>
        </div>
      </div>

      <Card class="border-border shadow-sm overflow-hidden">
        <CardContent class="p-0">
          <DataTable
            :data="pagedUsers"
            :columns="columns"
            :page="page"
            :page-size="pageSize"
            :total="total"
            :loading="loading"
            :sortable="true"
            empty-message="Belum ada pengguna."
            @update:page="page = $event"
            @update:page-size="pageSize = $event"
          >
            <template #cell-username="{ item }">
              <div class="flex items-center gap-3">
                <div class="relative shrink-0">
                  <img
                    v-if="item.avatar"
                    :src="item.avatar"
                    :alt="item.fullname || item.username"
                    class="w-9 h-9 rounded-full border border-border bg-muted object-cover"
                    @error="(e) => { e.target.style.display='none'; e.target.nextSibling.style.display='flex' }"
                  />
                  <div
                    :class="['w-9 h-9 rounded-full flex items-center justify-center text-white text-xs font-bold shrink-0', getAvatarColor(item)]"
                    :style="item.avatar ? 'display:none' : ''"
                  >
                    {{ getUserInitials(item) }}
                  </div>
                </div>
                <div class="flex flex-col min-w-0">
                  <span class="font-semibold text-foreground text-sm truncate">{{ item.fullname || item.username }}</span>
                  <span class="text-xs text-muted-foreground truncate">@{{ item.username }}</span>
                </div>
              </div>
            </template>

            <template #cell-email="{ item }">
              <span class="text-sm text-muted-foreground">{{ item.email || '-' }}</span>
            </template>

            <template #cell-roles="{ item }">
              <span :class="['inline-flex items-center gap-1 px-2 py-0.5 rounded-md border text-[10px] font-bold uppercase tracking-wide', getRoleBadgeClass(getRoleName(item))]">
                <Shield class="h-3 w-3 opacity-70" />
                {{ getRoleName(item) }}
              </span>
            </template>

            <template #cell-createdAt="{ item }">
              <span class="text-sm text-muted-foreground font-medium">{{ formatDate(item.createdAt) }}</span>
            </template>

            <template #actions="{ item }">
              <DropdownMenu>
                <DropdownMenuTrigger as-child>
                  <button class="p-1.5 hover:bg-accent rounded-md transition-colors outline-none">
                    <MoreVertical class="h-4 w-4 text-muted-foreground" />
                  </button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" class="w-40">
                  <DropdownMenuItem @click="openEdit(item)" class="gap-2 cursor-pointer text-foreground">
                    <Pencil class="h-3.5 w-3.5" /> Edit
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem @click="openDelete(item)" class="gap-2 cursor-pointer text-red-600 dark:text-red-400 focus:text-red-600 dark:focus:text-red-400">
                    <Trash2 class="h-3.5 w-3.5" /> Hapus
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </template>
          </DataTable>
        </CardContent>
      </Card>
    </div>

    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showDrawer" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm" @click="showDrawer = false" />
      </Transition>

      <Transition name="scale">
        <div v-if="showDrawer" class="fixed inset-0 z-50 flex items-center justify-center p-4 pointer-events-none">
          
          <div class="relative flex flex-col w-full max-w-2xl max-h-[90vh] bg-card border border-border rounded-xl shadow-2xl overflow-hidden pointer-events-auto animate-in fade-in zoom-in-95 duration-150">
            
            <div class="flex items-center justify-between px-6 py-4 border-b border-border bg-muted/20 shrink-0">
              <div>
                <h3 class="font-semibold text-base text-foreground">{{ drawerMode === 'create' ? 'Tambah Pengguna' : 'Edit Pengguna' }}</h3>
                <p class="text-xs text-muted-foreground mt-0.5">{{ drawerMode === 'create' ? 'Buat akun pengguna baru sistem.' : 'Perbarui informasi kredensial pengguna.' }}</p>
              </div>
              <button @click="showDrawer = false" class="p-2 rounded-md hover:bg-accent transition-colors text-muted-foreground hover:text-foreground">
                <X class="h-4 w-4" />
              </button>
            </div>

            <div class="flex-1 overflow-y-auto px-6 py-5 space-y-4">
              <div v-if="formError" class="rounded-md border border-red-200 dark:border-red-900 bg-red-50 dark:bg-red-950/30 p-3">
                <p class="text-sm text-red-600 dark:text-red-400">{{ formError }}</p>
              </div>

              <div class="flex items-center gap-4 p-4 rounded-xl bg-muted/40 border border-border">
                <div class="relative shrink-0 group">
                  <img
                    v-if="avatarPreview"
                    :src="avatarPreview"
                    alt="Avatar preview"
                    class="w-16 h-16 rounded-full object-cover border-2 border-border shadow-sm"
                  />
                  <div
                    v-else
                    :class="['w-16 h-16 rounded-full flex items-center justify-center text-white text-xl font-bold border-2 border-border shadow-sm', form.id ? getAvatarColor({ id: form.id }) : 'bg-zinc-700']"
                  >
                    {{ form.fullname || form.username ? getUserInitials({ fullname: form.fullname, username: form.username || '?' }) : '?' }}
                  </div>
                  <button
                    type="button"
                    @click="avatarInputRef?.click()"
                    class="absolute inset-0 rounded-full bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center cursor-pointer"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                  </button>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="font-semibold text-sm text-foreground truncate">{{ form.fullname || form.username || 'Akun Baru' }}</p>
                  <p class="text-xs text-muted-foreground mb-2">@{{ form.username || 'username' }}</p>
                  <div class="flex items-center gap-2">
                    <button
                      type="button"
                      @click="avatarInputRef?.click()"
                      class="text-xs font-semibold px-2.5 py-1.5 rounded-md border border-border bg-background hover:bg-accent text-foreground transition-colors cursor-pointer"
                    >Unggah Foto</button>
                    <button
                      v-if="avatarPreview"
                      type="button"
                      @click="removeAvatar"
                      class="text-xs font-semibold px-2.5 py-1.5 rounded-md text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/20 transition-colors cursor-pointer"
                    >Hapus</button>
                  </div>
                </div>
                <input ref="avatarInputRef" type="file" accept="image/*" class="hidden" @change="handleAvatarChange" />
              </div>

              <div class="space-y-1.5">
                <label class="text-xs font-bold text-foreground uppercase tracking-wide" for="dw-username">Username <span class="text-red-500">*</span></label>
                <input id="dw-username" v-model="form.username" type="text" placeholder="budi_santoso" :disabled="saving"
                  :class="['flex h-10 w-full rounded-md border px-3 py-2 text-sm text-foreground bg-background placeholder:text-muted-foreground outline-none transition disabled:opacity-50', fieldErrors.username ? 'border-red-400 focus:ring-2 focus:ring-red-500/10' : 'border-input focus:ring-2 focus:ring-primary/20']" />
                <p v-if="fieldErrors.username" class="text-xs text-red-500 flex items-center gap-1 mt-1 font-medium">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  {{ fieldErrors.username }}
                </p>
              </div>

              <div class="space-y-1.5">
                <label class="text-xs font-bold text-foreground uppercase tracking-wide" for="dw-fullname">Nama Lengkap</label>
                <input id="dw-fullname" v-model="form.fullname" type="text" placeholder="Budi Santoso" :disabled="saving"
                  class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:ring-2 focus:ring-primary/20 outline-none transition disabled:opacity-50" />
              </div>

              <div class="space-y-1.5">
                <label class="text-xs font-bold text-foreground uppercase tracking-wide" for="dw-email">Email <span class="text-red-500">*</span></label>
                <input id="dw-email" v-model="form.email" type="text" placeholder="budi@example.com" :disabled="saving"
                  :class="['flex h-10 w-full rounded-md border px-3 py-2 text-sm text-foreground bg-background placeholder:text-muted-foreground outline-none transition disabled:opacity-50', fieldErrors.email ? 'border-red-400 focus:ring-2 focus:ring-red-500/10' : 'border-input focus:ring-2 focus:ring-primary/20']" />
                <p v-if="fieldErrors.email" class="text-xs text-red-500 flex items-center gap-1 mt-1 font-medium">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  {{ fieldErrors.email }}
                </p>
              </div>

              <div class="space-y-1.5">
                <label class="text-xs font-bold text-foreground uppercase tracking-wide" for="dw-password">
                  Password
                  <span v-if="drawerMode === 'create'" class="text-red-500">*</span>
                  <span v-else class="text-muted-foreground text-[10px] font-normal lowercase italic"> (kosongkan jika tidak diganti)</span>
                </label>
                <div class="relative">
                  <input id="dw-password" v-model="form.password" :type="showPassword ? 'text' : 'password'"
                    :placeholder="drawerMode === 'create' ? 'Masukkan password akun' : 'Masukkan password baru'" :disabled="saving"
                    :class="['flex h-10 w-full rounded-md border px-3 py-2 pr-10 text-sm text-foreground bg-background placeholder:text-muted-foreground outline-none transition disabled:opacity-50', fieldErrors.password ? 'border-red-400 focus:ring-2 focus:ring-red-500/10' : 'border-input focus:ring-2 focus:ring-primary/20']" />
                  <button type="button" @click="showPassword = !showPassword" class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors cursor-pointer">
                    <EyeOff v-if="showPassword" class="h-4 w-4" />
                    <Eye v-else class="h-4 w-4" />
                  </button>
                </div>
                <p v-if="fieldErrors.password" class="text-xs text-red-500 flex items-center gap-1 mt-1 font-medium">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  {{ fieldErrors.password }}
                </p>
              </div>

              <div class="space-y-1.5">
                <label class="text-xs font-bold text-foreground uppercase tracking-wide">Tetapkan Hak Akses (Role)</label>
                <div class="rounded-lg border border-border overflow-hidden bg-background">
                  <div class="max-h-36 overflow-y-auto divide-y divide-border">
                    <button v-for="role in roles" :key="role.id"
                      type="button"
                      @click="toggleRole(role.id)"
                      :disabled="saving"
                      class="w-full flex items-center justify-between px-3 py-2.5 hover:bg-muted/50 cursor-pointer transition-colors outline-none text-left disabled:opacity-50">
                      <div class="flex items-center gap-3">
                        <Shield class="h-4 w-4 text-muted-foreground shrink-0" />
                        <div class="min-w-0">
                          <p class="font-semibold text-xs text-foreground truncate">{{ role.name }}</p>
                          <p class="text-[10px] text-muted-foreground font-mono truncate">{{ role.slug }}</p>
                        </div>
                      </div>
                      <Check v-if="form.roleIds.includes(role.id)" class="h-4 w-4 text-primary shrink-0" />
                    </button>
                  </div>
                  <div v-if="roles.length === 0" class="p-3 text-xs text-muted-foreground text-center">Belum ada role tersedia di server.</div>
                </div>
              </div>
            </div>

            <div class="flex justify-end gap-3 px-6 py-4 border-t border-border bg-muted/30 shrink-0">
              <Button variant="outline" size="sm" @click="showDrawer = false" :disabled="saving" class="text-foreground border-border">Batal</Button>
              <Button size="sm" @click="saveUser" :disabled="saving" class="bg-primary hover:bg-primary/90 text-primary-foreground font-semibold shadow-sm">
                <span v-if="saving" class="flex items-center gap-2"><span class="h-3.5 w-3.5 animate-spin rounded-full border-2 border-current border-t-transparent" />Menyimpan…</span>
                <span v-else>{{ drawerMode === 'create' ? 'Buat Pengguna' : 'Simpan Perubahan' }}</span>
              </Button>
            </div>
          </div>

        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showDeleteDialog" class="fixed inset-0 z-[60] bg-black/50 backdrop-blur-sm" @click="cancelDelete" />
      </Transition>
      <Transition name="scale">
        <div v-if="showDeleteDialog" class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none">
          
          <div class="relative z-10 w-full max-w-2xl bg-card border border-border rounded-xl shadow-2xl overflow-hidden pointer-events-auto">
            <div class="px-6 py-5 border-b border-border bg-muted/10">
              <div class="flex items-start gap-4">
                <div class="w-10 h-10 rounded-full bg-red-100 dark:bg-red-950/40 flex items-center justify-center shrink-0">
                  <Trash2 class="h-5 w-5 text-red-600 dark:text-red-400" />
                </div>
                <div>
                  <h3 class="font-bold text-foreground text-base">Hapus Pengguna</h3>
                  <p class="text-xs text-muted-foreground mt-1 leading-relaxed">
                    Anda akan menghapus akun <span class="font-bold text-foreground">"{{ deletingUser?.fullname || deletingUser?.username }}"</span>. Semua data mutasi log bersangkutan akan terpengaruh. Tindakan ini permanen.
                  </p>
                </div>
              </div>
            </div>

            <div class="px-6 py-5 space-y-4">
              <p class="text-xs font-bold uppercase tracking-wider text-muted-foreground">Kredensial Verifikasi Admin</p>

              <div v-if="deleteError" class="rounded-md border border-red-200 dark:border-red-900 bg-red-50 dark:bg-red-950/30 p-3">
                <p class="text-sm text-red-600 dark:text-red-400 font-medium">{{ deleteError }}</p>
              </div>

              <div class="space-y-1.5">
                <label class="text-[11px] font-bold text-foreground uppercase tracking-wide" for="del-username">Username Admin</label>
                <input id="del-username" v-model="deleteAdminUsername" type="text" placeholder="Username admin penanggung jawab"
                  class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:ring-2 focus:ring-red-500/10 focus:border-red-400 outline-none transition" />
              </div>

              <div class="space-y-1.5">
                <label class="text-[11px] font-bold text-foreground uppercase tracking-wide" for="del-password">Password Admin</label>
                <div class="relative">
                  <input id="del-password" v-model="deleteAdminPassword" :type="showDeletePassword ? 'text' : 'password'" placeholder="Password konfirmasi otoritas"
                    class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 pr-10 text-sm text-foreground placeholder:text-muted-foreground focus:ring-2 focus:ring-red-500/10 focus:border-red-400 outline-none transition" />
                  <button type="button" @click="showDeletePassword = !showDeletePassword" class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors cursor-pointer">
                    <EyeOff v-if="showDeletePassword" class="h-4 w-4" />
                    <Eye v-else class="h-4 w-4" />
                  </button>
                </div>
              </div>
            </div>

            <div class="flex justify-end gap-3 px-6 py-4 border-t border-border bg-muted/30">
              <Button variant="outline" size="sm" @click="cancelDelete" class="text-foreground border-border">Batal</Button>
              <Button size="sm" @click="confirmDelete" class="bg-red-600 hover:bg-red-700 text-white font-bold shadow-sm">
                <Trash2 class="h-3.5 w-3.5 mr-1.5" /> Hapus Pengguna
              </Button>
            </div>
          </div>

        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>

<style scoped>
/* ─── FADE ANTIMASI: UNTUK BACKGROUND BACKDROP TRANSPARAN ─── */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ─── SCALE ANIMASI: EFFECT POP-UP DI TENGAH LAYAR MONITOR ─── */
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