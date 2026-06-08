<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Alert from '@/components/ui/Alert.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import api from '@/lib/api'
import {
  Plus, Pencil, Trash2, Loader2, X, Shield, Eye, EyeOff,
  Check, ChevronDown, Filter,
  ArrowLeft, Users
} from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()
const authStore = useAuthStore()
const isAdmin = computed(() => authStore.isAdmin)

// ─── View ─────────────────────────────────────────────────────────────────────
const view = ref('list') // 'list' | 'form'
const formMode = ref('create') // 'create' | 'edit'

// ─── State ────────────────────────────────────────────────────────────────────
const allUsers    = ref([])
const roles       = ref([])
const loading     = ref(false)
const searchQuery = ref('')
const page        = ref(1)
const pageSize    = ref(10)
const filterOpen  = ref(false)
const filterRole  = ref('')
const filterRef   = ref(null)

// ─── Form ─────────────────────────────────────────────────────────────────────
const form = ref({
  id: null, username: '', fullname: '', email: '',
  password: '', roleIds: [],
})
const formErrors  = ref({})
const formError   = ref(null)
const saving      = ref(false)
const showPass    = ref(false)
const avatarPreview = ref(null)
const avatarFile  = ref(null)
const avatarInput = ref(null)

// ─── Delete ───────────────────────────────────────────────────────────────────
const deleting    = ref(false)
const deleteModal = ref({ show: false, user: null, confirmText: '' })

// ─── Computed ─────────────────────────────────────────────────────────────────
const filteredUsers = computed(() => {
  let list = allUsers.value
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(u =>
      (u.fullname || '').toLowerCase().includes(q) ||
      (u.username || '').toLowerCase().includes(q) ||
      (u.email || '').toLowerCase().includes(q)
    )
  }
  if (filterRole.value) {
    list = list.filter(u => (u.roles || []).some(r => r.slug === filterRole.value))
  }
  return list
})

const pagedUsers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredUsers.value.slice(start, start + pageSize.value)
})

const availableRoles = computed(() => {
  const map = {}
  allUsers.value.forEach(u => (u.roles || []).forEach(r => { if (r.slug) map[r.slug] = r.name }))
  return Object.entries(map).map(([slug, name]) => ({ slug, name }))
})

watch([searchQuery, filterRole], () => { page.value = 1 })

// ─── Role Label (kolom Jabatan/Role) ─────────────────────────────────────────
function isBranchManagerRole(u) {
  return (u?.roles || []).some(r => {
    const s = (r.slug || '').toLowerCase()
    return s === 'pengelola-cabang' || s === 'branch-manager' || s.includes('pengelola-cabang')
  })
}
function isWarehouseManagerRole(u) {
  return (u?.roles || []).some(r => {
    const s = (r.slug || '').toLowerCase()
    return s === 'pengelola-gudang' || s === 'warehouse-manager' || s.includes('pengelola-gudang')
  })
}

const ROLE_BADGE = {
  owner: {
    label: 'Pemilik Mitra',
    class: 'bg-amber-50 dark:bg-amber-900/20 text-amber-700 dark:text-amber-300 border-amber-200 dark:border-amber-700/50',
    dot: 'bg-amber-400 shadow-[0_0_6px_2px_rgba(251,191,36,0.5)]',
  },
  'admin-partner': {
    label: 'Admin Mitra',
    class: 'bg-orange-50 dark:bg-orange-900/20 text-orange-700 dark:text-orange-300 border-orange-200 dark:border-orange-700/50',
    dot: 'bg-orange-400',
  },
  'branch-manager': {
    label: 'Pengelola Cabang',
    class: 'bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 border-blue-200 dark:border-blue-700/50',
    dot: 'bg-blue-400',
  },
  'warehouse-manager': {
    label: 'Pengelola Gudang',
    class: 'bg-violet-50 dark:bg-violet-900/20 text-violet-700 dark:text-violet-300 border-violet-200 dark:border-violet-700/50',
    dot: 'bg-violet-400',
  },
  'staff-branch': {
    label: 'Karyawan Cabang',
    class: 'bg-sky-50 dark:bg-sky-900/20 text-sky-700 dark:text-sky-300 border-sky-200 dark:border-sky-700/50',
    dot: 'bg-sky-400',
  },
  'staff-warehouse': {
    label: 'Karyawan Gudang',
    class: 'bg-indigo-50 dark:bg-indigo-900/20 text-indigo-700 dark:text-indigo-300 border-indigo-200 dark:border-indigo-700/50',
    dot: 'bg-indigo-400',
  },
  'kasir': {
    label: 'Kasir',
    class: 'bg-teal-50 dark:bg-teal-900/20 text-teal-700 dark:text-teal-300 border-teal-200 dark:border-teal-700/50',
    dot: 'bg-teal-400',
  },
  role: {
    label: '',
    class: 'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-300 border-emerald-200 dark:border-emerald-700/50',
    dot: 'bg-emerald-400',
  },
  'no-role': {
    label: 'Tanpa Jabatan',
    class: 'bg-zinc-100 dark:bg-zinc-800 text-zinc-500 dark:text-zinc-400 border-zinc-200 dark:border-zinc-700',
    dot: 'bg-zinc-400',
  },
}

function getUserRoleBadge(u) {
  const slugs = (u.roles || []).map(r => (r.slug || '').toLowerCase())
  if (slugs.includes('owner')) return { ...ROLE_BADGE.owner, sub: null }
  if (slugs.some(s => s === 'admin-partner' || s === 'admin-partners'))
    return { ...ROLE_BADGE['admin-partner'], sub: null }
  if (isBranchManagerRole(u))
    return { ...ROLE_BADGE['branch-manager'], sub: u.branchName || null }
  if (isWarehouseManagerRole(u))
    return { ...ROLE_BADGE['warehouse-manager'], sub: u.warehouseName || null }
  // Karyawan cabang (termasuk kasir)
  if (slugs.includes('kasir'))
    return { ...ROLE_BADGE['kasir'], sub: u.branchName || null }
  if (slugs.some(s => s === 'karyawan-cabang' || s === 'staff-branch'))
    return { ...ROLE_BADGE['staff-branch'], sub: u.branchName || null }
  // Karyawan gudang
  if (slugs.some(s => s === 'karyawan-gudang' || s === 'staff-warehouse'))
    return { ...ROLE_BADGE['staff-warehouse'], sub: u.warehouseName || null }
  // Fallback: ada di cabang tapi tanpa role spesifik
  if (u.branchId && u.branchName)
    return { ...ROLE_BADGE['staff-branch'], sub: u.branchName }
  if (u.warehouseId && u.warehouseName)
    return { ...ROLE_BADGE['staff-warehouse'], sub: u.warehouseName }
  if ((u.roles || []).length > 0)
    return { ...ROLE_BADGE.role, label: u.roles[0].name, sub: null }
  return { ...ROLE_BADGE['no-role'], sub: null }
}

// ─── Status Aktif (kolom on/off) ──────────────────────────────────────────────
const togglingStatus = ref(null)

function isUserActive(u) {
  return u.is_active !== false && u.isActive !== false
}

function canToggleUser(u) {
  if (!can('user.update')) return false
  if (authStore.user?.id === u.id) return false
  return true
}

async function toggleUserActive(u, e) {
  if (e) e.stopPropagation()
  if (!canToggleUser(u) || togglingStatus.value === u.id) return
  const next = !isUserActive(u)
  togglingStatus.value = u.id
  try {
    await api.put(`/api/v1/users/${u.id}`, { isActive: next })
    u.is_active = next
    u.isActive = next
    toast.success(next ? 'User diaktifkan.' : 'User ditangguhkan — tidak bisa login.')
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal mengubah status user.')
  } finally {
    togglingStatus.value = null
  }
}

// ─── Fetch ────────────────────────────────────────────────────────────────────
async function fetchUsers() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/users')
    const data = res.data?.data
    allUsers.value = Array.isArray(data) ? data : (data?.content || [])
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal memuat pengguna.')
  } finally {
    loading.value = false
  }
}

async function fetchRoles() {
  try {
    const res = await api.get('/api/v1/roles')
    roles.value = res.data?.data || res.data || []
  } catch { roles.value = [] }
}

// ─── Form ─────────────────────────────────────────────────────────────────────
function openCreate() {
  form.value = { id: null, username: '', fullname: '', email: '', password: '', roleIds: [] }
  formErrors.value = {}; formError.value = null
  avatarPreview.value = null; avatarFile.value = null; showPass.value = false
  formMode.value = 'create'; view.value = 'form'
  fetchRoles()
}

function openEdit(u) {
  form.value = {
    id: u.id, username: u.username, fullname: u.fullname || '',
    email: u.email || '', password: '',
    roleIds: (u.roles || []).map(r => r.id),
  }
  formErrors.value = {}; formError.value = null
  avatarPreview.value = u.avatar || null; avatarFile.value = null; showPass.value = false
  formMode.value = 'edit'; view.value = 'form'
  fetchRoles()
}

function toggleRole(id) {
  const idx = form.value.roleIds.indexOf(id)
  if (idx === -1) form.value.roleIds.push(id)
  else form.value.roleIds.splice(idx, 1)
}

function handleAvatarChange(e) {
  const file = e.target.files?.[0]; if (!file) return
  avatarFile.value = file
  const reader = new FileReader()
  reader.onload = ev => { avatarPreview.value = ev.target.result }
  reader.readAsDataURL(file)
}

function validateForm() {
  formErrors.value = {}; let ok = true
  const u = form.value.username.trim()
  if (!u) { formErrors.value.username = 'Username wajib diisi'; ok = false }
  else if (u.length < 3) { formErrors.value.username = 'Minimal 3 karakter'; ok = false }
  else if (!/^[a-zA-Z0-9_]+$/.test(u)) { formErrors.value.username = 'Hanya huruf, angka, dan underscore'; ok = false }

  const e = form.value.email.trim()
  if (!e) { formErrors.value.email = 'Email wajib diisi'; ok = false }
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e)) { formErrors.value.email = 'Format email tidak valid'; ok = false }

  if (formMode.value === 'create' && !form.value.password) { formErrors.value.password = 'Password wajib diisi'; ok = false }
  else if (form.value.password && form.value.password.length < 6) { formErrors.value.password = 'Minimal 6 karakter'; ok = false }
  return ok
}

async function saveUser() {
  if (!validateForm()) return
  saving.value = true; formError.value = null
  try {
    let avatarUrl = undefined
    if (avatarFile.value) {
      const fd = new FormData()
      fd.append('file', avatarFile.value)
      const up = await api.post('/api/v1/upload/avatar', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      avatarUrl = up.data.data.url
    } else if (avatarPreview.value === null && formMode.value === 'edit') {
      avatarUrl = ''
    }

    const payload = {
      username: form.value.username,
      fullname: form.value.fullname || null,
      email: form.value.email,
      roleIds: form.value.roleIds.length > 0 ? form.value.roleIds : undefined,
    }
    if (avatarUrl !== undefined) payload.avatar = avatarUrl
    if (formMode.value === 'create') payload.password = form.value.password
    else if (form.value.password) payload.password = form.value.password

    if (formMode.value === 'create') {
      await api.post('/api/v1/users', payload)
      toast.success('Pengguna berhasil ditambahkan!')
    } else {
      await api.put(`/api/v1/users/${form.value.id}`, payload)
      toast.success('Pengguna berhasil diperbarui!')
    }
    view.value = 'list'
    fetchUsers()
  } catch (err) {
    formError.value = err.response?.data?.data?.message || err.response?.data?.message || 'Gagal menyimpan.'
  } finally {
    saving.value = false
  }
}

// ─── Delete ───────────────────────────────────────────────────────────────────
function doDelete(u, e) {
  if (e) e.stopPropagation()
  deleteModal.value = { show: true, user: u, confirmText: '' }
}
function closeDeleteModal() { deleteModal.value.show = false }

async function confirmDelete() {
  if (deleteModal.value.confirmText !== deleteModal.value.user?.username) return
  deleting.value = true
  try {
    await api.delete(`/api/v1/users/${deleteModal.value.user.id}`)
    toast.success('Pengguna berhasil dihapus!')
    if (view.value === 'form') view.value = 'list'
    fetchUsers(); closeDeleteModal()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menghapus.')
  } finally {
    deleting.value = false }
}

// ─── Utils ────────────────────────────────────────────────────────────────────
function handleOutsideClick(e) {
  if (filterRef.value && !filterRef.value.contains(e.target)) filterOpen.value = false
}
onMounted(() => { fetchUsers(); fetchRoles(); document.addEventListener('click', handleOutsideClick) })
onBeforeUnmount(() => document.removeEventListener('click', handleOutsideClick))

const PALETTE = [
  { bg: '#dbeafe', color: '#1d4ed8' }, { bg: '#dcfce7', color: '#15803d' },
  { bg: '#ede9fe', color: '#6d28d9' }, { bg: '#fef9c3', color: '#a16207' },
  { bg: '#fce7f3', color: '#be185d' }, { bg: '#ffedd5', color: '#c2410c' },
]
function avatarStyle(name = '') {
  const idx = (name.charCodeAt(0) || 0) % PALETTE.length
  return { backgroundColor: PALETTE[idx].bg, color: PALETTE[idx].color }
}
function initials(u) {
  const n = u.fullname || u.username || '?'
  return n.split(' ').map(x => x[0]).join('').toUpperCase().slice(0, 2)
}
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <Transition name="fade" mode="out-in">

        <!-- ══════════════════════ LIST VIEW ══════════════════════ -->
        <div v-if="view === 'list'" key="list" class="flex flex-col gap-6">

          <!-- Header -->
          <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
            <div>
              <h1 class="text-2xl font-bold tracking-tight">Pengguna</h1>
              <p class="text-muted-foreground text-sm mt-1">Kelola akun pengguna, role, dan penempatan lokasi.</p>
            </div>
            <div class="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto">
              <div class="w-full sm:w-72">
                <DataTableSearch v-model="searchQuery" placeholder="Cari nama, username..." />
              </div>

              <!-- Filter -->
              <div ref="filterRef" class="relative flex-1 sm:flex-none">
                <button @click="filterOpen = !filterOpen"
                  class="w-full flex items-center justify-center gap-2 h-9 px-3 rounded-md border border-border bg-background hover:bg-accent text-foreground text-sm font-medium transition-colors">
                  <Filter class="h-3.5 w-3.5" />
                  <span>Filter</span>
                  <span v-if="filterRole" class="w-1.5 h-1.5 rounded-full bg-primary" />
                  <ChevronDown class="h-3 w-3 text-muted-foreground" :class="filterOpen ? 'rotate-180' : ''" style="transition:transform .2s" />
                </button>
                <Transition name="fade">
                  <div v-if="filterOpen" class="absolute right-0 top-full mt-1 z-30 w-56 bg-card border border-border rounded-lg shadow-xl overflow-hidden">
                    <div class="flex items-center justify-between px-3 py-2.5 border-b border-border">
                      <span class="text-xs font-semibold uppercase tracking-wide">Role</span>
                      <button v-if="filterRole" @click="filterRole = ''" class="text-xs text-red-500 font-medium">Hapus</button>
                    </div>
                    <div class="p-1.5 max-h-48 overflow-y-auto">
                      <button @click="filterRole = ''" class="w-full flex items-center justify-between px-2.5 py-2 rounded-md hover:bg-muted/50 text-sm transition-colors">
                        <span>Semua</span>
                        <Check v-if="!filterRole" class="h-3.5 w-3.5 text-primary" />
                      </button>
                      <button v-for="r in availableRoles" :key="r.slug" @click="filterRole = r.slug; filterOpen = false"
                        class="w-full flex items-center justify-between px-2.5 py-2 rounded-md hover:bg-muted/50 text-sm transition-colors">
                        <span>{{ r.name }}</span>
                        <Check v-if="filterRole === r.slug" class="h-3.5 w-3.5 text-primary" />
                      </button>
                    </div>
                  </div>
                </Transition>
              </div>

              <Button v-if="can('user.store')" @click="openCreate" size="sm" class="w-full sm:w-auto flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
                <Plus class="h-4 w-4" /><span>Tambah User</span>
              </Button>
            </div>
          </div>

          <!-- Table Card -->
          <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
            <CardContent class="p-0">
              <div v-if="loading" class="flex flex-col items-center justify-center py-24 gap-3">
                <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
                <p class="text-xs text-muted-foreground">Memuat data...</p>
              </div>
              <div v-else-if="filteredUsers.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
                <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
                  <Users class="h-7 w-7 opacity-40" />
                </div>
                <p class="text-sm font-medium">Belum ada pengguna</p>
                <Button v-if="can('user.store')" size="sm" class="mt-4" @click="openCreate">
                  <Plus class="h-3.5 w-3.5 mr-1.5" /> Tambah User
                </Button>
              </div>
              <div v-else>
                <!-- Mobile Cards -->
                <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
                  <div v-for="u in pagedUsers" :key="u.id" class="p-4 flex items-center gap-3 cursor-pointer hover:bg-zinc-50 dark:hover:bg-zinc-900/40 transition-colors" @click="openEdit(u)">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(u.fullname || u.username)">
                      {{ initials(u) }}
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="font-semibold text-sm">{{ u.fullname || u.username }}</p>
                      <p class="text-xs text-muted-foreground">@{{ u.username }}</p>
                      <div class="mt-1 flex items-center gap-2">
                        <span :class="['inline-flex items-center gap-1 px-2 py-0.5 rounded-full border text-[10px] font-semibold shadow-sm', getUserRoleBadge(u).class]">
                          <span :class="['w-1.5 h-1.5 rounded-full shrink-0', getUserRoleBadge(u).dot]" />
                          {{ getUserRoleBadge(u).label }}<span v-if="getUserRoleBadge(u).sub" class="opacity-80"> · {{ getUserRoleBadge(u).sub }}</span>
                        </span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Desktop Table -->
                <div class="hidden md:block overflow-x-auto">
                  <table class="w-full text-sm">
                    <thead>
                      <tr class="border-b border-zinc-100 dark:border-zinc-800 bg-muted/40">
                        <th class="pl-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Pengguna</th>
                        <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Jabatan</th>
                        <th class="py-3 text-center text-xs font-semibold uppercase tracking-wide text-zinc-500">Status</th>
                        <th class="pr-5 py-3 text-right"></th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="u in pagedUsers" :key="u.id"
                        class="group border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors cursor-pointer"
                        @click="openEdit(u)">
                        <td class="pl-5 py-3">
                          <div class="flex items-center gap-3">
                            <div class="relative shrink-0">
                              <img v-if="u.avatar" :src="u.avatar" :alt="u.username" class="w-9 h-9 rounded-full object-cover border border-zinc-200 dark:border-zinc-700" @error="e => e.target.style.display='none'" />
                              <div class="w-9 h-9 rounded-full flex items-center justify-center font-bold text-sm" :class="u.avatar ? 'hidden' : ''" :style="avatarStyle(u.fullname || u.username)">
                                {{ initials(u) }}
                              </div>
                            </div>
                            <div>
                              <p class="font-semibold text-[13px] text-zinc-900 dark:text-zinc-100 leading-tight">{{ u.fullname || u.username }}</p>
                              <p class="text-[11px] text-zinc-400">@{{ u.username }}</p>
                            </div>
                          </div>
                        </td>
                        <td class="py-3">
                          <span :class="['inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full border text-[10px] font-semibold shadow-sm', getUserRoleBadge(u).class]">
                            <span :class="['w-1.5 h-1.5 rounded-full shrink-0', getUserRoleBadge(u).dot]" />
                            {{ getUserRoleBadge(u).label }}<span v-if="getUserRoleBadge(u).sub" class="font-normal opacity-80"> · {{ getUserRoleBadge(u).sub }}</span>
                          </span>
                        </td>
                        <td class="py-3 text-center" @click.stop>
                          <button
                            type="button"
                            role="switch"
                            :aria-checked="isUserActive(u)"
                            :disabled="!canToggleUser(u) || togglingStatus === u.id"
                            :title="authStore.user?.id === u.id ? 'Tidak bisa menonaktifkan diri sendiri' : (isUserActive(u) ? 'Nonaktifkan user' : 'Aktifkan user')"
                            class="relative inline-flex h-5 w-9 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-40"
                            :class="isUserActive(u) ? 'bg-primary' : 'bg-zinc-200 dark:bg-zinc-700'"
                            @click="toggleUserActive(u, $event)"
                          >
                            <span
                              class="pointer-events-none h-4 w-4 rounded-full bg-background shadow-lg transition-transform flex items-center justify-center"
                              :class="isUserActive(u) ? 'translate-x-4' : 'translate-x-0'"
                            >
                              <Loader2 v-if="togglingStatus === u.id" class="h-2.5 w-2.5 animate-spin text-primary" />
                            </span>
                          </button>
                        </td>
                        <td class="pr-5 py-3 text-right" @click.stop>
                          <div class="flex justify-end gap-1 opacity-0 group-hover:opacity-100 transition-all">
                            <Button v-if="can('user.delete')" variant="ghost" size="icon" class="h-7 w-7 text-zinc-400 hover:text-destructive" @click="doDelete(u, $event)">
                              <Trash2 class="h-3.5 w-3.5" />
                            </Button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <DataTablePagination
                  v-if="filteredUsers.length > 0"
                  :page="page" :page-size="pageSize" :total="filteredUsers.length"
                  @update:page="page = $event"
                  @update:page-size="pageSize = $event; page = 1"
                />
              </div>
            </CardContent>
          </Card>
        </div>

        <!-- ══════════════════════ FORM VIEW ══════════════════════ -->
        <div v-else-if="view === 'form'" key="form" class="flex flex-col gap-6 max-w-2xl">

          <!-- Header -->
          <div class="flex items-center gap-4">
            <Button variant="ghost" size="icon" class="h-9 w-9 shrink-0" @click="view = 'list'"><ArrowLeft class="h-4 w-4" /></Button>
            <div>
              <h1 class="text-xl font-bold tracking-tight">{{ formMode === 'create' ? 'Tambah Pengguna' : 'Edit Pengguna' }}</h1>
              <p class="text-muted-foreground text-sm mt-0.5">{{ formMode === 'create' ? 'Buat akun pengguna baru. Role bisa diassign kemudian.' : 'Perbarui informasi dan hak akses pengguna.' }}</p>
            </div>
            <Button v-if="formMode === 'edit' && can('user.delete')" variant="outline" size="sm" class="ml-auto text-destructive border-destructive/30 hover:bg-destructive/5 shrink-0"
              @click="doDelete(allUsers.find(u => u.id === form.id), $event)">
              <Trash2 class="h-3.5 w-3.5 mr-1.5" /> Hapus
            </Button>
          </div>

          <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>

          <!-- Avatar + Identitas -->
          <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
            <CardContent class="p-6 space-y-5">
              <!-- Avatar -->
              <div class="flex items-center gap-4 p-4 rounded-xl bg-muted/40 border border-border">
                <div class="relative shrink-0 group cursor-pointer" @click="avatarInput?.click()">
                  <img v-if="avatarPreview" :src="avatarPreview" class="w-16 h-16 rounded-full object-cover border-2 border-border" />
                  <div v-else class="w-16 h-16 rounded-full flex items-center justify-center font-bold text-xl border-2 border-border" :style="avatarStyle(form.fullname || form.username)">
                    {{ initials({ fullname: form.fullname, username: form.username || '?' }) }}
                  </div>
                  <div class="absolute inset-0 rounded-full bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                    <svg class="h-5 w-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                  </div>
                </div>
                <div class="flex-1">
                  <p class="font-semibold text-sm truncate">{{ form.fullname || form.username || 'Pengguna Baru' }}</p>
                  <p class="text-xs text-muted-foreground mb-2">@{{ form.username || 'username' }}</p>
                  <div class="flex gap-2">
                    <button type="button" @click="avatarInput?.click()" class="text-xs font-semibold px-2.5 py-1.5 rounded-md border border-border bg-background hover:bg-accent transition-colors">Unggah Foto</button>
                    <button v-if="avatarPreview" type="button" @click="avatarPreview = null; avatarFile = null" class="text-xs font-semibold px-2.5 py-1.5 rounded-md text-destructive hover:bg-destructive/5 transition-colors">Hapus</button>
                  </div>
                </div>
                <input ref="avatarInput" type="file" accept="image/*" class="hidden" @change="handleAvatarChange" />
              </div>

              <!-- Fields -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div class="space-y-1.5">
                  <Label>Username <span class="text-destructive">*</span></Label>
                  <Input v-model="form.username" placeholder="budi_santoso" :disabled="saving" autocomplete="off" />
                  <p v-if="formErrors.username" class="text-xs text-destructive">{{ formErrors.username }}</p>
                </div>
                <div class="space-y-1.5">
                  <Label>Nama Lengkap</Label>
                  <Input v-model="form.fullname" placeholder="Budi Santoso" :disabled="saving" />
                </div>
              </div>

              <div class="space-y-1.5">
                <Label>Email <span class="text-destructive">*</span></Label>
                <Input v-model="form.email" placeholder="budi@example.com" :disabled="saving" />
                <p v-if="formErrors.email" class="text-xs text-destructive">{{ formErrors.email }}</p>
              </div>

              <div class="space-y-1.5">
                <Label>
                  Password
                  <span v-if="formMode === 'create'" class="text-destructive">*</span>
                  <span v-else class="text-muted-foreground text-xs font-normal italic"> (kosongkan jika tidak diganti)</span>
                </Label>
                <div class="relative">
                  <Input :type="showPass ? 'text' : 'password'" v-model="form.password" :placeholder="formMode === 'create' ? 'Password akun' : 'Password baru...'" :disabled="saving" class="pr-10" autocomplete="new-password" />
                  <button type="button" @click="showPass = !showPass" class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors">
                    <EyeOff v-if="showPass" class="h-4 w-4" />
                    <Eye v-else class="h-4 w-4" />
                  </button>
                </div>
                <p v-if="formErrors.password" class="text-xs text-destructive">{{ formErrors.password }}</p>
              </div>
            </CardContent>
          </Card>

          <!-- Actions -->
          <div class="flex justify-end gap-3">
            <Button variant="outline" @click="view = 'list'" :disabled="saving">Batal</Button>
            <Button @click="saveUser" :disabled="saving" class="bg-primary hover:bg-primary/90 min-w-[120px]">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              {{ formMode === 'create' ? 'Buat Pengguna' : 'Simpan Perubahan' }}
            </Button>
          </div>
        </div>

      </Transition>
    </div>

    <!-- Delete Modal -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[60] bg-black/40 backdrop-blur-sm" @click="closeDeleteModal" />
      </Transition>
      <Transition name="scale">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none">
          <div class="bg-card rounded-xl shadow-2xl w-full max-w-md border border-border pointer-events-auto p-6">
            <div class="flex items-center gap-3 text-destructive mb-3">
              <div class="w-10 h-10 rounded-full bg-destructive/10 flex items-center justify-center shrink-0"><Trash2 class="h-5 w-5" /></div>
              <div>
                <h3 class="text-base font-bold">Hapus Pengguna</h3>
                <p class="text-xs text-muted-foreground">Tindakan ini tidak bisa dibatalkan.</p>
              </div>
            </div>
            <p class="text-sm text-muted-foreground mb-4">
              Anda akan menghapus akun <span class="font-bold text-foreground">{{ deleteModal.user?.fullname || deleteModal.user?.username }}</span>.
              Semua data mutasi stok dan log yang terkait akan terpengaruh.
            </p>
            <div class="space-y-2 mb-6">
              <Label class="text-xs text-muted-foreground">Ketik username untuk konfirmasi:</Label>
              <Input v-model="deleteModal.confirmText" :placeholder="deleteModal.user?.username" class="border-destructive/30" />
            </div>
            <div class="flex justify-end gap-3">
              <Button variant="outline" @click="closeDeleteModal" :disabled="deleting">Batal</Button>
              <Button variant="destructive" :disabled="deleteModal.confirmText !== deleteModal.user?.username || deleting" @click="confirmDelete">
                <Loader2 v-if="deleting" class="h-4 w-4 mr-2 animate-spin" /> Hapus Permanen
              </Button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.15s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.scale-enter-active, .scale-leave-active { transition: opacity 0.2s ease, transform 0.2s cubic-bezier(0.16,1,0.3,1); }
.scale-enter-from, .scale-leave-to { opacity: 0; transform: scale(0.96); }
</style>
