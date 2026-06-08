<script setup>
import { ref, computed, onMounted, watch } from 'vue'
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
  Plus, Pencil, Trash2, Loader2, ArrowLeft, Building2,
  MapPin, Users, ArrowRightLeft, KeyRound,
  UserCheck, UserX
} from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()
const authStore = useAuthStore()

const isAdmin    = computed(() => authStore.isAdmin)
const isSuperAdmin = computed(() => authStore.isSuperAdmin)

// ─── View State: 'list' | 'create' | 'detail' ────────────────────────────────
const view = ref('list') // 'list' | 'create' | 'detail'

// ─── State ────────────────────────────────────────────────────────────────────
const branches    = ref([])
const allUsers    = ref([]) // semua user di partner untuk transfer
const branchUsers = ref([]) // user yang ada di cabang yang dipilih
const loading     = ref(false)
const error       = ref(null)
const searchQuery = ref('')
const page        = ref(1)
const pageSize    = ref(10)
const selectedIds = ref([])

// ─── Detail State ─────────────────────────────────────────────────────────────
const selectedBranch = ref(null)
const detailLoading  = ref(false)

// ─── Computed ─────────────────────────────────────────────────────────────────
const filteredBranches = computed(() => {
  if (!searchQuery.value) return branches.value
  const q = searchQuery.value.toLowerCase()
  return branches.value.filter(b =>
    (b.name && b.name.toLowerCase().includes(q)) ||
    (b.address && b.address.toLowerCase().includes(q))
  )
})
const paginatedBranches = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredBranches.value.slice(start, start + pageSize.value)
})
const isAllSelected = computed(() => {
  const v = paginatedBranches.value
  return v.length > 0 && v.every(b => selectedIds.value.includes(b.id))
})

function isBranchManager(u) {
  return (u?.roles || []).some(r => {
    const slug = r.slug?.toLowerCase() || ''
    return slug === 'pengelola-cabang' || slug === 'branch-manager'
  })
}

// Pengelola aktif = user dengan role pengelola-cabang di cabang ini
const currentManager = computed(() => {
  if (!selectedBranch.value) return null
  return branchUsers.value.find(u =>
    Number(u.branchId) === Number(selectedBranch.value.id) && isBranchManager(u)
  ) || null
})

function isPrivilegedUser(u) {
  return (u?.roles || []).some(r => {
    const s = (r.slug || '').toLowerCase()
    return ['owner', 'admin-partner', 'admin-partners', 'admin', 'super-admin', 'superadmin'].includes(s)
  })
}

function isManagerRoleUser(u) {
  return (u?.roles || []).some(r => {
    const s = (r.slug || '').toLowerCase()
    return s === 'pengelola-cabang' || s === 'branch-manager'
        || s === 'pengelola-gudang' || s === 'warehouse-manager'
  })
}

// User tersedia untuk transfer: karyawan biasa yang belum jadi pengelola di lokasi lain
const availableForTransfer = computed(() => {
  return allUsers.value.filter(u => {
    if (isPrivilegedUser(u)) return false
    if (isManagerRoleUser(u)) return false
    if (u.branchId === selectedBranch.value?.id) return false
    if (u.branchId && u.branchId !== selectedBranch.value?.id) return false
    if (u.warehouseId) return false
    return true
  })
})

watch([searchQuery, page, pageSize], () => { selectedIds.value = [] })

// ─── Fetch ────────────────────────────────────────────────────────────────────
async function fetchBranches() {
  loading.value = true
  error.value = null
  try {
    const url = isAdmin.value ? '/api/v1/branches/admin' : '/api/v1/branches'
    const res = await api.get(url)
    const raw = isAdmin.value ? res.data : (res.data?.data || [])
    branches.value = Array.isArray(raw) ? raw : (raw?.content || [])
  } catch {
    error.value = 'Gagal memuat data cabang.'
  } finally {
    loading.value = false
  }
}

async function fetchAllUsers() {
  try {
    const res = await api.get('/api/v1/users')
    allUsers.value = res.data?.data || []
  } catch (_) {}
}

async function fetchBranchUsers(branchId) {
  try {
    const res = await api.get(`/api/v1/branches/${branchId}/users`)
    branchUsers.value = res.data?.data || []
  } catch (err) {
    console.error('[Branch] gagal fetch users:', err.response?.data || err.message)
    branchUsers.value = []
  }
}

// ─── Selection ────────────────────────────────────────────────────────────────
function toggleSelectAll() {
  if (isAllSelected.value) {
    const ids = paginatedBranches.value.map(b => b.id)
    selectedIds.value = selectedIds.value.filter(id => !ids.includes(id))
  } else {
    paginatedBranches.value.forEach(b => {
      if (!selectedIds.value.includes(b.id)) selectedIds.value.push(b.id)
    })
  }
}
function toggleSelect(id) {
  const idx = selectedIds.value.indexOf(id)
  if (idx === -1) selectedIds.value.push(id)
  else selectedIds.value.splice(idx, 1)
}

// ─── Bulk Delete ──────────────────────────────────────────────────────────────
async function bulkDelete() {
  const count = selectedIds.value.length
  if (!count) return
  const ok = await confirm({ title: 'Hapus Cabang Terpilih', description: `Hapus ${count} cabang secara permanen?`, confirmLabel: 'Hapus', cancelLabel: 'Batal' })
  if (!ok) return
  loading.value = true
  try {
    await Promise.all(selectedIds.value.map(id => api.delete(`/api/v1/branches/${id}`)))
    toast.success(`${count} cabang berhasil dihapus!`)
    selectedIds.value = []
    fetchBranches()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menghapus.')
    fetchBranches()
  } finally {
    loading.value = false
  }
}

// ─── Open Detail ──────────────────────────────────────────────────────────────
async function openDetail(branch) {
  selectedBranch.value = { ...branch }
  view.value = 'detail'
  detailLoading.value = true

  // reset form state
  editForm.value = { name: branch.name, address: branch.address || '' }
  editErrors.value = {}
  editError.value = null
  credForm.value = { username: '', password: '' }
  credError.value = null
  transferUserId.value = null
  addStaffId.value = null
  addStaffError.value = null
  activePanel.value = 'info'

  await Promise.all([fetchBranchUsers(branch.id), fetchAllUsers()])
  detailLoading.value = false
}

// ─── Detail Panels ────────────────────────────────────────────────────────────
const activePanel = ref('info') // 'info' | 'users' | 'transfer' | 'credentials'

// ─── Edit Info Form ───────────────────────────────────────────────────────────
const editForm   = ref({ name: '', address: '' })
const editErrors = ref({})
const editError  = ref(null)
const editSaving = ref(false)

async function saveInfo() {
  editErrors.value = {}
  editError.value = null
  if (!editForm.value.name.trim()) { editErrors.value.name = 'Nama wajib diisi'; return }
  editSaving.value = true
  try {
    const res = await api.put(`/api/v1/branches/${selectedBranch.value.id}`, {
      name: editForm.value.name,
      address: editForm.value.address,
    })
    toast.success('Cabang berhasil diperbarui!')
    selectedBranch.value = { ...selectedBranch.value, ...res.data?.data }
    fetchBranches()
  } catch (err) {
    editError.value = err.response?.data?.message || 'Gagal menyimpan.'
  } finally {
    editSaving.value = false
  }
}

// ─── Credentials Form ─────────────────────────────────────────────────────────
const credForm  = ref({ username: '', password: '' })
const credError = ref(null)
const credSaving = ref(false)

async function saveCredentials() {
  credError.value = null
  if (!credForm.value.username.trim() && !credForm.value.password) {
    credError.value = 'Isi username atau password baru.'; return
  }
  if (credForm.value.password && credForm.value.password.length < 6) {
    credError.value = 'Password minimal 6 karakter.'; return
  }
  if (!currentManager.value) {
    credError.value = 'Tidak ada pengelola aktif untuk diperbarui.'; return
  }
  credSaving.value = true
  try {
    const payload = {}
    if (credForm.value.username.trim()) payload.username = credForm.value.username.trim()
    if (credForm.value.password) payload.password = credForm.value.password
    await api.put(`/api/v1/users/${currentManager.value.id}`, payload)
    toast.success('Kredensial pengelola berhasil diperbarui!')
    credForm.value = { username: '', password: '' }
    await fetchBranchUsers(selectedBranch.value.id)
  } catch (err) {
    credError.value = err.response?.data?.message || 'Gagal memperbarui kredensial.'
  } finally {
    credSaving.value = false
  }
}

// ─── Transfer Manager ─────────────────────────────────────────────────────────
const transferUserId = ref(null)
const transferSaving = ref(false)
const transferError  = ref(null)

async function doTransfer() {
  if (!transferUserId.value) return
  transferError.value = null
  transferSaving.value = true
  try {
    await api.put(`/api/v1/branches/${selectedBranch.value.id}/manager`, { userId: transferUserId.value })
    toast.success('Pengelola cabang berhasil dipindahkan!')
    transferUserId.value = null
    await fetchBranchUsers(selectedBranch.value.id)
    await fetchAllUsers()
  } catch (err) {
    transferError.value = err.response?.data?.message || 'Gagal transfer pengelola.'
  } finally {
    transferSaving.value = false
  }
}

// ─── Create Form ──────────────────────────────────────────────────────────────
const createForm   = ref({ name: '', address: '', username: '', password: '' })
const createErrors = ref({})
const createError  = ref(null)
const createSaving = ref(false)

function openCreate() {
  createForm.value = { name: '', address: '', username: '', password: '' }
  createErrors.value = {}
  createError.value = null
  view.value = 'create'
}

function validateCreate() {
  createErrors.value = {}
  let ok = true
  if (!createForm.value.name.trim()) { createErrors.value.name = 'Nama cabang wajib diisi'; ok = false }
  if (!createForm.value.username.trim()) { createErrors.value.username = 'Username pengelola wajib diisi'; ok = false }
  if (!createForm.value.password || createForm.value.password.length < 6) { createErrors.value.password = 'Password minimal 6 karakter'; ok = false }
  return ok
}

async function saveBranch() {
  if (!validateCreate()) return
  createSaving.value = true
  createError.value = null
  try {
    await api.post('/api/v1/branches', {
      name: createForm.value.name,
      address: createForm.value.address,
      username: createForm.value.username,
      password: createForm.value.password,
    })
    toast.success('Cabang berhasil ditambahkan!')
    view.value = 'list'
    fetchBranches()
  } catch (err) {
    createError.value = err.response?.data?.message || 'Gagal menyimpan cabang.'
  } finally {
    createSaving.value = false
  }
}

// ─── Delete ───────────────────────────────────────────────────────────────────
const deleteModal = ref({ show: false, branch: null, confirmText: '' })
const deleting    = ref(false)

// ─── Add Staff to Branch ──────────────────────────────────────────────────────

// Karyawan cabang = user di cabang ini tanpa role pengelola
const branchStaff = computed(() =>
  branchUsers.value.filter(u =>
    Number(u.branchId) === Number(selectedBranch.value?.id) && !isBranchManager(u)
  )
)

const addStaffId     = ref(null)
const addStaffSaving = ref(false)
const addStaffError  = ref(null)

// User tersedia untuk ditambahkan ke cabang:
// - Bukan owner/admin
// - Belum di cabang manapun
// - Belum di gudang manapun
// - Karyawan gudang (punya role karyawan-gudang) tidak bisa masuk cabang
const staffAvailable = computed(() => {
  return allUsers.value.filter(u => {
    const isOwner = (u.roles || []).some(r =>
      ['owner', 'admin-partner', 'admin', 'super-admin'].includes(r.slug?.toLowerCase())
    )
    if (isOwner) return false
    if (u.branchId === selectedBranch.value?.id) return false // sudah di sini
    if (u.branchId || u.warehouseId) return false // di lokasi lain
    // Karyawan gudang tidak bisa ditambah ke cabang
    const hasWarehouseRole = (u.roles || []).some(r =>
      ['karyawan-gudang', 'pengelola-gudang', 'warehouse-manager'].includes(r.slug?.toLowerCase())
    )
    if (hasWarehouseRole) return false
    return true
  })
})

async function doAddStaff() {
  if (!addStaffId.value) return
  addStaffError.value = null; addStaffSaving.value = true
  try {
    await api.put(`/api/v1/users/${addStaffId.value}`, { branchId: selectedBranch.value.id })
    toast.success('Karyawan berhasil ditambahkan ke cabang!')
    addStaffId.value = null
    await fetchBranchUsers(selectedBranch.value.id)
    await fetchAllUsers()
  } catch (err) {
    addStaffError.value = err.response?.data?.message || 'Gagal menambahkan karyawan.'
  } finally {
    addStaffSaving.value = false
  }
}

async function removeStaff(userId) {
  const ok = await confirm({
    title: 'Lepas Karyawan',
    description: 'Karyawan akan dilepas dari cabang ini. Role karyawan cabang juga akan dihapus.',
    confirmLabel: 'Lepas',
    cancelLabel: 'Batal',
  })
  if (!ok) return
  try {
    await api.put(`/api/v1/users/${userId}`, { clearBranch: true })
    toast.success('Karyawan berhasil dilepas dari cabang.')
    await fetchBranchUsers(selectedBranch.value.id)
    await fetchAllUsers()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal melepas karyawan.')
  }
}

// Lepas pengelola aktif dari cabang (tanpa transfer ke orang lain)
async function releaseManager() {
  if (!currentManager.value) return
  const ok = await confirm({
    title: 'Lepas Pengelola',
    description: `"${currentManager.value.fullname || currentManager.value.username}" akan dilepas dari jabatan pengelola cabang ini. Role pengelola-cabang akan dihapus.`,
    confirmLabel: 'Lepas Jabatan',
    cancelLabel: 'Batal',
  })
  if (!ok) return
  try {
    await api.put(`/api/v1/users/${currentManager.value.id}`, { clearBranch: true })
    toast.success('Pengelola berhasil dilepas dari cabang.')
    await fetchBranchUsers(selectedBranch.value.id)
    await fetchAllUsers()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal melepas pengelola.')
  }
}

function doDelete(b, e) {
  if (e) e.stopPropagation()
  deleteModal.value = { show: true, branch: b, confirmText: '' }
}
function closeDeleteModal() { deleteModal.value.show = false }

async function confirmDelete() {
  if (deleteModal.value.confirmText !== deleteModal.value.branch?.name) return
  deleting.value = true
  try {
    await api.delete(`/api/v1/branches/${deleteModal.value.branch.id}`)
    toast.success('Cabang berhasil dihapus!')
    if (view.value === 'detail') view.value = 'list'
    fetchBranches()
    closeDeleteModal()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menghapus.')
  } finally {
    deleting.value = false
  }
}

// ─── Utils ────────────────────────────────────────────────────────────────────
const COLORS = [
  { bg: '#dbeafe', color: '#1d4ed8' }, { bg: '#dcfce7', color: '#15803d' },
  { bg: '#fef9c3', color: '#a16207' }, { bg: '#fce7f3', color: '#be185d' },
  { bg: '#ede9fe', color: '#6d28d9' }, { bg: '#ffedd5', color: '#c2410c' },
]
function avatarStyle(name = '') {
  const idx = (name.charCodeAt(0) || 0) % COLORS.length
  return { backgroundColor: COLORS[idx].bg, color: COLORS[idx].color }
}
function userInitial(u) {
  return (u?.fullname || u?.username || '?').charAt(0).toUpperCase()
}

onMounted(fetchBranches)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <Transition name="fade" mode="out-in">

        <!-- ══════════════════════ LIST VIEW ══════════════════════ -->
        <div v-if="view === 'list'" key="list" class="flex flex-col gap-6">
          <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
            <div>
              <h1 class="text-2xl font-bold tracking-tight">Cabang</h1>
              <p class="text-muted-foreground text-sm mt-1">Kelola data cabang beserta akun penggunanya.</p>
            </div>
            <div class="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto">
              <div class="w-full sm:w-72">
                <DataTableSearch v-model="searchQuery" placeholder="Cari cabang..." />
              </div>
              <Button v-if="can('branch.store') && !isSuperAdmin" @click="openCreate" size="sm" class="w-full sm:w-auto flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
                <Plus class="h-4 w-4" /><span>Tambah Cabang</span>
              </Button>
            </div>
          </div>

          <Alert v-if="error" variant="destructive">{{ error }}</Alert>

          <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
            <CardContent class="p-0">
              <div v-if="loading" class="flex flex-col items-center justify-center py-24 gap-3">
                <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
                <p class="text-xs text-muted-foreground">Memuat data...</p>
              </div>
              <div v-else-if="filteredBranches.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
                <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
                  <Building2 class="h-7 w-7 opacity-40" />
                </div>
                <p class="text-sm font-medium">Belum ada data cabang</p>
                <Button v-if="can('branch.store') && !isSuperAdmin" size="sm" class="mt-4" @click="openCreate">
                  <Plus class="h-3.5 w-3.5 mr-1.5" /> Tambah Cabang
                </Button>
              </div>
              <div v-else>
                <Transition name="fade">
                  <div v-if="selectedIds.length > 0" class="flex items-center justify-between px-5 py-3 bg-primary/5 border-b border-border">
                    <div class="flex items-center gap-2">
                      <span class="text-xs font-semibold text-primary px-2 py-0.5 rounded bg-primary/10">{{ selectedIds.length }} Terpilih</span>
                    </div>
                    <Button v-if="can('branch.delete')" size="sm" variant="destructive" class="h-8 text-xs gap-1.5" @click="bulkDelete">
                      <Trash2 class="h-3.5 w-3.5" /> Hapus
                    </Button>
                  </div>
                </Transition>

                <!-- Mobile -->
                <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
                  <div v-for="b in paginatedBranches" :key="b.id" class="p-4 flex items-center gap-3" @click="openDetail(b)">
                    <div class="w-10 h-10 rounded-lg flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(b.name)">
                      {{ b.name?.charAt(0).toUpperCase() }}
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="font-semibold text-sm">{{ b.name }}</p>
                      <p class="text-xs text-zinc-400 truncate">{{ b.address || 'Belum ada alamat' }}</p>
                    </div>
                    <Button variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-destructive shrink-0" @click="doDelete(b, $event)"><Trash2 class="h-3.5 w-3.5" /></Button>
                  </div>
                </div>

                <!-- Desktop Table -->
                <div class="hidden md:block overflow-x-auto">
                  <table class="w-full text-sm">
                    <thead>
                      <tr class="border-b border-zinc-100 dark:border-zinc-800 bg-muted/40">
                        <th class="w-12 pl-5 py-3 text-left">
                          <input type="checkbox" class="rounded border-zinc-300 dark:border-zinc-700 text-primary focus:ring-primary h-4 w-4 cursor-pointer" :checked="isAllSelected" @change="toggleSelectAll" />
                        </th>
                        <th class="pl-2 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Nama Cabang</th>
                        <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Alamat</th>
                        <th class="pr-5 py-3 text-right"></th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="b in paginatedBranches" :key="b.id"
                        class="group border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors cursor-pointer"
                        @click="openDetail(b)">
                        <td class="w-12 pl-5 py-3" @click.stop>
                          <input type="checkbox" class="rounded border-zinc-300 dark:border-zinc-700 text-primary focus:ring-primary h-4 w-4 cursor-pointer" :checked="selectedIds.includes(b.id)" @change="toggleSelect(b.id)" />
                        </td>
                        <td class="pl-2 py-3">
                          <div class="flex items-center gap-3">
                            <div class="w-9 h-9 rounded-lg flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(b.name)">
                              {{ b.name?.charAt(0).toUpperCase() }}
                            </div>
                            <div>
                              <p class="font-semibold text-[13px] leading-tight">{{ b.name }}</p>
                              <p class="text-[10px] text-zinc-400">ID #{{ b.id }}</p>
                            </div>
                          </div>
                        </td>
                        <td class="py-3 max-w-[300px]">
                          <div v-if="b.address" class="flex items-center gap-1.5 text-xs text-zinc-500">
                            <MapPin class="h-3 w-3 shrink-0 opacity-60" /><span class="truncate">{{ b.address }}</span>
                          </div>
                          <span v-else class="text-xs text-zinc-300 dark:text-zinc-600 italic">Belum ada alamat</span>
                        </td>
                        <td class="pr-5 py-3 text-right" @click.stop>
                          <div class="flex justify-end gap-1 opacity-0 group-hover:opacity-100 transition-all">
                            <Button v-if="can('branch.delete') && !isSuperAdmin" variant="ghost" size="icon" class="h-7 w-7 text-zinc-400 hover:text-destructive" @click="doDelete(b, $event)">
                              <Trash2 class="h-3.5 w-3.5" />
                            </Button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <DataTablePagination v-if="filteredBranches.length > 0 && !loading" :page="page" :page-size="pageSize" :total="filteredBranches.length" @update:page="page = $event" @update:page-size="pageSize = $event; page = 1" />
              </div>
            </CardContent>
          </Card>
        </div>

        <!-- ══════════════════════ CREATE VIEW ══════════════════════ -->
        <div v-else-if="view === 'create'" key="create" class="flex flex-col gap-6 max-w-2xl">
          <div class="flex items-center gap-4">
            <Button variant="ghost" size="icon" class="h-9 w-9 shrink-0" @click="view = 'list'"><ArrowLeft class="h-4 w-4" /></Button>
            <div>
              <h1 class="text-xl font-bold tracking-tight">Tambah Cabang</h1>
              <p class="text-muted-foreground text-sm">Buat cabang baru beserta akun pengelolanya.</p>
            </div>
          </div>

          <Alert v-if="createError" variant="destructive">{{ createError }}</Alert>

          <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
            <CardContent class="p-6 space-y-5">
              <div class="space-y-1.5">
                <Label>Nama Cabang <span class="text-destructive">*</span></Label>
                <Input v-model="createForm.name" placeholder="Contoh: Cabang Sudirman" :disabled="createSaving" />
                <p v-if="createErrors.name" class="text-xs text-destructive">{{ createErrors.name }}</p>
              </div>
              <div class="space-y-1.5">
                <Label>Alamat</Label>
                <textarea v-model="createForm.address" rows="3" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring placeholder:text-muted-foreground resize-none" placeholder="Alamat lengkap cabang..." :disabled="createSaving" />
              </div>

              <div class="border-t border-border pt-5">
                <div class="flex items-center gap-2 mb-4">
                  <div class="w-7 h-7 rounded-md bg-primary/10 flex items-center justify-center"><Users class="h-4 w-4 text-primary" /></div>
                  <div>
                    <p class="text-sm font-semibold">Akun Pengelola Cabang <span class="text-destructive">*</span></p>
                    <p class="text-xs text-muted-foreground">Dibuat otomatis saat cabang disimpan. Role <strong>Pengelola Cabang</strong> diberikan secara otomatis.</p>
                  </div>
                </div>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div class="space-y-1.5">
                    <Label>Username <span class="text-destructive">*</span></Label>
                    <Input v-model="createForm.username" placeholder="nairha" :disabled="createSaving" autocomplete="off" />
                    <p v-if="createErrors.username" class="text-xs text-destructive">{{ createErrors.username }}</p>
                  </div>
                  <div class="space-y-1.5">
                    <Label>Password <span class="text-destructive">*</span></Label>
                    <Input type="password" v-model="createForm.password" placeholder="Min. 6 karakter" :disabled="createSaving" autocomplete="new-password" />
                    <p v-if="createErrors.password" class="text-xs text-destructive">{{ createErrors.password }}</p>
                  </div>
                </div>
                <div class="mt-3 flex items-center gap-2 p-3 rounded-lg bg-primary/5 border border-primary/20">
                  <div class="w-5 h-5 rounded-full bg-primary/10 flex items-center justify-center shrink-0">
                    <svg class="h-3 w-3 text-primary" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
                  </div>
                  <p class="text-xs text-primary font-medium">Role <span class="font-bold">Pengelola Cabang</span> akan diberikan otomatis ke akun ini.</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <div class="flex justify-end gap-3">
            <Button variant="outline" @click="view = 'list'" :disabled="createSaving">Batal</Button>
            <Button @click="saveBranch" :disabled="createSaving" class="bg-primary hover:bg-primary/90 min-w-[120px]">
              <Loader2 v-if="createSaving" class="h-4 w-4 mr-2 animate-spin" />
              Simpan Cabang
            </Button>
          </div>
        </div>

        <!-- ══════════════════════ DETAIL VIEW ══════════════════════ -->
        <div v-else-if="view === 'detail'" key="detail" class="flex flex-col gap-6">
          <!-- Header -->
          <div class="flex items-start gap-4">
            <Button variant="ghost" size="icon" class="h-9 w-9 shrink-0 mt-0.5" @click="view = 'list'"><ArrowLeft class="h-4 w-4" /></Button>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-3 flex-wrap">
                <div class="w-10 h-10 rounded-xl flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(selectedBranch?.name)">
                  {{ selectedBranch?.name?.charAt(0).toUpperCase() }}
                </div>
                <div>
                  <h1 class="text-xl font-bold tracking-tight">{{ selectedBranch?.name }}</h1>
                  <p class="text-xs text-muted-foreground">ID #{{ selectedBranch?.id }} · {{ selectedBranch?.address || 'Belum ada alamat' }}</p>
                </div>
              </div>
            </div>
            <Button v-if="can('branch.delete') && !isSuperAdmin" variant="outline" size="sm" class="text-destructive border-destructive/30 hover:bg-destructive/5 shrink-0" @click="doDelete(selectedBranch, $event)">
              <Trash2 class="h-3.5 w-3.5 mr-1.5" /> Hapus
            </Button>
          </div>

          <!-- Tabs -->
          <div class="flex gap-1 p-1 bg-muted/50 dark:bg-zinc-900/50 rounded-lg w-fit border border-border">
            <button v-for="tab in [{id:'info',label:'Info'},{id:'staff',label:'Karyawan'},{id:'transfer',label:'Transfer Pengelola'},{id:'credentials',label:'Ubah Kredensial'}]"
              :key="tab.id" @click="activePanel = tab.id"
              class="px-3 py-1.5 rounded-md text-sm font-medium transition-all whitespace-nowrap"
              :class="activePanel === tab.id ? 'bg-background shadow-sm text-foreground' : 'text-muted-foreground hover:text-foreground'">
              {{ tab.label }}
            </button>
          </div>

          <div v-if="detailLoading" class="flex items-center justify-center py-16">
            <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
          </div>

          <template v-else>
            <!-- ─── TAB: INFO ─── -->
            <Card v-if="activePanel === 'info'" class="border-zinc-200 dark:border-zinc-800 shadow-sm max-w-xl">
              <CardContent class="p-6 space-y-5">
                <Alert v-if="editError" variant="destructive">{{ editError }}</Alert>
                <div class="space-y-1.5">
                  <Label>Nama Cabang <span class="text-destructive">*</span></Label>
                  <Input v-model="editForm.name" :disabled="editSaving" />
                  <p v-if="editErrors.name" class="text-xs text-destructive">{{ editErrors.name }}</p>
                </div>
                <div class="space-y-1.5">
                  <Label>Alamat</Label>
                  <textarea v-model="editForm.address" rows="3" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring placeholder:text-muted-foreground resize-none" :disabled="editSaving" />
                </div>
                <div class="flex justify-end">
                  <Button @click="saveInfo" :disabled="editSaving" class="bg-primary hover:bg-primary/90">
                    <Loader2 v-if="editSaving" class="h-4 w-4 mr-2 animate-spin" /> Simpan Perubahan
                  </Button>
                </div>
              </CardContent>
            </Card>

            <!-- ─── TAB: KARYAWAN ─── -->
            <div v-else-if="activePanel === 'staff'" class="flex flex-col gap-4 max-w-2xl">
              <!-- Pengelola -->
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5">
                  <p class="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Pengelola Cabang</p>
                  <div v-if="currentManager" class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(currentManager.fullname || currentManager.username)">
                      {{ userInitial(currentManager) }}
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="font-semibold text-sm">{{ currentManager.fullname || currentManager.username }}</p>
                      <p class="text-xs text-muted-foreground">@{{ currentManager.username }}</p>
                    </div>
                    <span class="text-[10px] font-semibold text-emerald-600 bg-emerald-50 dark:bg-emerald-900/20 px-2 py-0.5 rounded-full flex items-center gap-1 shrink-0">
                      <UserCheck class="h-3 w-3" /> Pengelola
                    </span>
                    <Button variant="ghost" size="sm" class="h-8 text-xs text-destructive hover:text-destructive hover:bg-destructive/5 shrink-0" @click="releaseManager">
                      <UserX class="h-3.5 w-3.5 mr-1" /> Lepas
                    </Button>
                  </div>
                  <div v-else class="flex items-center gap-2 text-zinc-400">
                    <UserX class="h-4 w-4" />
                    <p class="text-sm">Belum ada pengelola aktif</p>
                  </div>
                </CardContent>
              </Card>

              <!-- Daftar karyawan -->
              <div class="flex items-center justify-between">
                <p class="text-sm font-semibold text-foreground">Karyawan</p>
                <Badge variant="secondary">{{ branchStaff.length }} orang</Badge>
              </div>
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-0">
                  <div v-if="branchStaff.length === 0" class="flex flex-col items-center justify-center py-10 text-muted-foreground">
                    <Users class="h-7 w-7 opacity-30 mb-2" />
                    <p class="text-sm">Belum ada karyawan di cabang ini</p>
                  </div>
                  <div v-else class="divide-y divide-zinc-100 dark:divide-zinc-800/60">
                    <div v-for="u in branchStaff" :key="u.id" class="flex items-center gap-3 px-4 py-3">
                      <div class="w-9 h-9 rounded-full flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(u.fullname || u.username)">
                        {{ userInitial(u) }}
                      </div>
                      <div class="flex-1 min-w-0">
                        <p class="font-semibold text-sm">{{ u.fullname || u.username }}</p>
                        <p class="text-xs text-muted-foreground">@{{ u.username }}</p>
                        <div class="flex flex-wrap gap-1 mt-1">
                          <Badge v-for="r in u.roles" :key="r.id" variant="outline" class="text-[9px] px-1.5 py-0 h-4">{{ r.name }}</Badge>
                          <span v-if="!(u.roles || []).length" class="text-[10px] text-zinc-400 italic">Tanpa role</span>
                        </div>
                      </div>
                      <Button variant="ghost" size="sm" class="h-8 text-xs text-destructive hover:text-destructive hover:bg-destructive/5 shrink-0" @click="removeStaff(u.id)">
                        <UserX class="h-3.5 w-3.5 mr-1" /> Lepas
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <!-- Tambah karyawan -->
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <p class="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Tambah Karyawan</p>
                  <Alert v-if="addStaffError" variant="destructive">{{ addStaffError }}</Alert>
                  <div v-if="staffAvailable.length === 0" class="text-sm text-muted-foreground italic py-2">
                    Tidak ada user tersedia. Semua user sudah ditempatkan atau merupakan owner/admin.
                  </div>
                  <div v-else class="rounded-lg border border-input overflow-hidden max-h-60 overflow-y-auto">
                    <div class="divide-y divide-border">
                      <button v-for="u in staffAvailable" :key="u.id" type="button"
                        @click="addStaffId = addStaffId === u.id ? null : u.id"
                        class="w-full flex items-center gap-3 px-4 py-3 hover:bg-muted/50 transition-colors text-left outline-none"
                        :class="addStaffId === u.id ? 'bg-primary/5' : ''">
                        <div class="w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs shrink-0" :style="avatarStyle(u.fullname || u.username)">
                          {{ userInitial(u) }}
                        </div>
                        <div class="flex-1 min-w-0">
                          <p class="font-medium text-sm">{{ u.fullname || u.username }}</p>
                          <p class="text-xs text-muted-foreground">@{{ u.username }}</p>
                        </div>
                        <div class="h-4 w-4 rounded-full border-2 flex items-center justify-center transition-all" :class="addStaffId === u.id ? 'border-primary' : 'border-zinc-300 dark:border-zinc-600'">
                          <div v-if="addStaffId === u.id" class="w-2 h-2 rounded-full bg-primary" />
                        </div>
                      </button>
                    </div>
                  </div>
                  <div class="flex justify-end">
                    <Button @click="doAddStaff" :disabled="!addStaffId || addStaffSaving" class="bg-primary hover:bg-primary/90 gap-2">
                      <Loader2 v-if="addStaffSaving" class="h-4 w-4 animate-spin" />
                      <Plus v-else class="h-4 w-4" />
                      Tambah ke Cabang
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </div>

            <!-- ─── TAB: TRANSFER ─── -->
            <div v-else-if="activePanel === 'transfer'" class="flex flex-col gap-4 max-w-xl">
              <!-- Pengelola saat ini -->
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5">
                  <p class="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Pengelola Saat Ini</p>
                  <div v-if="currentManager" class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(currentManager.fullname || currentManager.username)">
                      {{ userInitial(currentManager) }}
                    </div>
                    <div>
                      <p class="font-semibold text-sm">{{ currentManager.fullname || currentManager.username }}</p>
                      <p class="text-xs text-muted-foreground">@{{ currentManager.username }}</p>
                    </div>
                    <UserCheck class="h-4 w-4 text-emerald-500 ml-auto" />
                  </div>
                  <div v-else class="flex items-center gap-2 text-zinc-400">
                    <UserX class="h-4 w-4" />
                    <p class="text-sm">Belum ada pengelola aktif</p>
                  </div>
                </CardContent>
              </Card>

              <!-- Transfer ke user lain -->
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <p class="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Transfer ke Pengelola Baru</p>
                  <Alert v-if="transferError" variant="destructive">{{ transferError }}</Alert>

                  <div v-if="availableForTransfer.length === 0" class="text-sm text-muted-foreground italic py-2">
                    Tidak ada user yang tersedia untuk ditransfer. User harus bebas dari pengelolaan cabang/gudang lain.
                  </div>
                  <div v-else class="rounded-lg border border-input overflow-hidden max-h-60 overflow-y-auto">
                    <div class="divide-y divide-border">
                      <button v-for="u in availableForTransfer" :key="u.id" type="button"
                        @click="transferUserId = transferUserId === u.id ? null : u.id"
                        class="w-full flex items-center gap-3 px-4 py-3 hover:bg-muted/50 transition-colors text-left outline-none"
                        :class="transferUserId === u.id ? 'bg-primary/5' : ''">
                        <div class="w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs shrink-0" :style="avatarStyle(u.fullname || u.username)">
                          {{ userInitial(u) }}
                        </div>
                        <div class="flex-1 min-w-0">
                          <p class="font-medium text-sm">{{ u.fullname || u.username }}</p>
                          <p class="text-xs text-muted-foreground">@{{ u.username }}</p>
                        </div>
                        <div class="h-4 w-4 rounded-full border-2 flex items-center justify-center transition-all" :class="transferUserId === u.id ? 'border-primary' : 'border-zinc-300 dark:border-zinc-600'">
                          <div v-if="transferUserId === u.id" class="w-2 h-2 rounded-full bg-primary" />
                        </div>
                      </button>
                    </div>
                  </div>

                  <div class="flex justify-end">
                    <Button @click="doTransfer" :disabled="!transferUserId || transferSaving" class="bg-primary hover:bg-primary/90 gap-2">
                      <Loader2 v-if="transferSaving" class="h-4 w-4 animate-spin" />
                      <ArrowRightLeft v-else class="h-4 w-4" />
                      Transfer Pengelola
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </div>

            <!-- ─── TAB: CREDENTIALS ─── -->
            <div v-else-if="activePanel === 'credentials'" class="flex flex-col gap-4 max-w-xl">              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <div v-if="!currentManager" class="flex items-center gap-2 text-zinc-400 py-2">
                    <UserX class="h-4 w-4" />
                    <p class="text-sm">Tidak ada pengelola aktif untuk diperbarui kredensialnya.</p>
                  </div>
                  <template v-else>
                    <div class="flex items-center gap-3 p-3 rounded-lg bg-muted/50 border border-border">
                      <div class="w-9 h-9 rounded-full flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(currentManager.fullname || currentManager.username)">
                        {{ userInitial(currentManager) }}
                      </div>
                      <div>
                        <p class="font-semibold text-sm">{{ currentManager.fullname || currentManager.username }}</p>
                        <p class="text-xs text-muted-foreground">@{{ currentManager.username }}</p>
                      </div>
                      <KeyRound class="h-4 w-4 text-muted-foreground ml-auto" />
                    </div>

                    <Alert v-if="credError" variant="destructive">{{ credError }}</Alert>

                    <div class="space-y-1.5">
                      <Label>Username Baru <span class="text-xs text-muted-foreground">(kosongkan jika tidak diganti)</span></Label>
                      <Input v-model="credForm.username" :placeholder="currentManager.username" :disabled="credSaving" autocomplete="off" />
                    </div>
                    <div class="space-y-1.5">
                      <Label>Password Baru <span class="text-xs text-muted-foreground">(min. 6 karakter)</span></Label>
                      <Input type="password" v-model="credForm.password" placeholder="Password baru..." :disabled="credSaving" autocomplete="new-password" />
                    </div>
                    <div class="flex justify-end">
                      <Button @click="saveCredentials" :disabled="credSaving" class="bg-primary hover:bg-primary/90 gap-2">
                        <Loader2 v-if="credSaving" class="h-4 w-4 animate-spin" />
                        <KeyRound v-else class="h-4 w-4" />
                        Perbarui Kredensial
                      </Button>
                    </div>
                  </template>
                </CardContent>
              </Card>
            </div>
          </template>
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
              <h3 class="text-lg font-bold">Hapus Cabang</h3>
            </div>
            <p class="text-sm text-muted-foreground mb-4">Anda akan menghapus <span class="font-bold text-foreground">{{ deleteModal.branch?.name }}</span> secara permanen.</p>
            <div class="space-y-2 mb-6">
              <Label class="text-xs text-muted-foreground">Ketik nama cabang untuk konfirmasi:</Label>
              <Input v-model="deleteModal.confirmText" :placeholder="deleteModal.branch?.name" class="border-destructive/30" />
            </div>
            <div class="flex justify-end gap-3">
              <Button variant="outline" @click="closeDeleteModal" :disabled="deleting">Batal</Button>
              <Button variant="destructive" :disabled="deleteModal.confirmText !== deleteModal.branch?.name || deleting" @click="confirmDelete">
                <Loader2 v-if="deleting" class="h-4 w-4 mr-2 animate-spin" /> Hapus Permanen
              </Button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>
