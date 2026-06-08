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
  Plus, Trash2, Loader2, ArrowLeft, Warehouse,
  MapPin, Shield, Check, Users, ArrowRightLeft, KeyRound,
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

// ─── View State ────────────────────────────────────────────────────────────────
const view = ref('list') // 'list' | 'create' | 'detail'

// ─── State ────────────────────────────────────────────────────────────────────
const warehouses       = ref([])
const roles            = ref([])
const allUsers         = ref([])
const warehouseUsers   = ref([])
const loading          = ref(false)
const error            = ref(null)
const searchQuery      = ref('')
const page             = ref(1)
const pageSize         = ref(10)
const selectedIds      = ref([])
const selectedWarehouse = ref(null)
const detailLoading    = ref(false)

// ─── Computed ─────────────────────────────────────────────────────────────────
const filteredWarehouses = computed(() => {
  if (!searchQuery.value) return warehouses.value
  const q = searchQuery.value.toLowerCase()
  return warehouses.value.filter(w =>
    (w.name && w.name.toLowerCase().includes(q)) ||
    (w.address && w.address.toLowerCase().includes(q))
  )
})
const paginatedWarehouses = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredWarehouses.value.slice(start, start + pageSize.value)
})
const isAllSelected = computed(() => {
  const v = paginatedWarehouses.value
  return v.length > 0 && v.every(w => selectedIds.value.includes(w.id))
})
function isWarehouseManager(u) {
  return (u?.roles || []).some(r => {
    const slug = r.slug?.toLowerCase() || ''
    return slug === 'pengelola-gudang' || slug === 'warehouse-manager'
  })
}

const currentManager = computed(() => {
  if (!selectedWarehouse.value) return null
  return warehouseUsers.value.find(u =>
    Number(u.warehouseId) === Number(selectedWarehouse.value.id) && isWarehouseManager(u)
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

const availableForTransfer = computed(() => {
  return allUsers.value.filter(u => {
    if (isPrivilegedUser(u)) return false
    if (isManagerRoleUser(u)) return false
    if (u.warehouseId === selectedWarehouse.value?.id) return false
    if (u.warehouseId && u.warehouseId !== selectedWarehouse.value?.id) return false
    if (u.branchId) return false
    return true
  })
})

watch([searchQuery, page, pageSize], () => { selectedIds.value = [] })

// ─── Fetch ────────────────────────────────────────────────────────────────────
async function fetchWarehouses() {
  loading.value = true; error.value = null
  try {
    const url = isAdmin.value ? '/api/v1/warehouses/admin' : '/api/v1/warehouses'
    const res = await api.get(url)
    const raw = res.data?.data
    warehouses.value = Array.isArray(raw) ? raw : (raw?.content || [])
  } catch { error.value = 'Gagal memuat data gudang.' }
  finally { loading.value = false }
}
async function fetchRoles() {
  try { const res = await api.get('/api/v1/roles'); roles.value = res.data?.data || res.data || [] } catch (_) {}
}
async function fetchAllUsers() {
  try { const res = await api.get('/api/v1/users'); allUsers.value = res.data?.data || [] } catch (_) {}
}
async function fetchWarehouseUsers(whId) {
  try {
    const res = await api.get(`/api/v1/warehouses/${whId}/users`)
    warehouseUsers.value = res.data?.data || []
  } catch (err) {
    console.error('[Warehouse] gagal fetch users:', err.response?.data || err.message)
    warehouseUsers.value = []
  }
}

// ─── Selection ────────────────────────────────────────────────────────────────
function toggleSelectAll() {
  if (isAllSelected.value) {
    const ids = paginatedWarehouses.value.map(w => w.id)
    selectedIds.value = selectedIds.value.filter(id => !ids.includes(id))
  } else {
    paginatedWarehouses.value.forEach(w => { if (!selectedIds.value.includes(w.id)) selectedIds.value.push(w.id) })
  }
}
function toggleSelect(id) {
  const idx = selectedIds.value.indexOf(id)
  if (idx === -1) selectedIds.value.push(id)
  else selectedIds.value.splice(idx, 1)
}

// ─── Bulk Delete ──────────────────────────────────────────────────────────────
async function bulkDelete() {
  const count = selectedIds.value.length; if (!count) return
  const ok = await confirm({ title: 'Hapus Gudang Terpilih', description: `Hapus ${count} gudang secara permanen?`, confirmLabel: 'Hapus', cancelLabel: 'Batal' })
  if (!ok) return
  loading.value = true
  try {
    await Promise.all(selectedIds.value.map(id => api.delete(`/api/v1/warehouses/${id}`)))
    toast.success(`${count} gudang berhasil dihapus!`)
    selectedIds.value = []; fetchWarehouses()
  } catch (err) { toast.error(err.response?.data?.message || 'Gagal menghapus.'); fetchWarehouses() }
  finally { loading.value = false }
}

// ─── Open Detail ──────────────────────────────────────────────────────────────
async function openDetail(w) {
  selectedWarehouse.value = { ...w }
  view.value = 'detail'
  detailLoading.value = true
  editForm.value = { name: w.name, address: w.address || '' }
  editErrors.value = {}; editError.value = null
  credForm.value = { username: '', password: '' }; credError.value = null
  transferUserId.value = null
  addStaffId.value = null
  addStaffError.value = null
  activePanel.value = 'info'
  await Promise.all([fetchWarehouseUsers(w.id), fetchAllUsers(), fetchRoles()])
  detailLoading.value = false
}

// ─── Panels ───────────────────────────────────────────────────────────────────
const activePanel = ref('info')

// ─── Edit Info ────────────────────────────────────────────────────────────────
const editForm = ref({ name: '', address: '' })
const editErrors = ref({})
const editError = ref(null)
const editSaving = ref(false)
async function saveInfo() {
  editErrors.value = {}; editError.value = null
  if (!editForm.value.name.trim()) { editErrors.value.name = 'Nama wajib diisi'; return }
  editSaving.value = true
  try {
    const res = await api.put(`/api/v1/warehouses/${selectedWarehouse.value.id}`, { name: editForm.value.name, address: editForm.value.address })
    toast.success('Gudang berhasil diperbarui!')
    selectedWarehouse.value = { ...selectedWarehouse.value, ...res.data?.data }
    fetchWarehouses()
  } catch (err) { editError.value = err.response?.data?.message || 'Gagal menyimpan.' }
  finally { editSaving.value = false }
}

// ─── Credentials ─────────────────────────────────────────────────────────────
const credForm = ref({ username: '', password: '' })
const credError = ref(null)
const credSaving = ref(false)
async function saveCredentials() {
  credError.value = null
  if (!credForm.value.username.trim() && !credForm.value.password) { credError.value = 'Isi username atau password baru.'; return }
  if (credForm.value.password && credForm.value.password.length < 6) { credError.value = 'Password minimal 6 karakter.'; return }
  if (!currentManager.value) { credError.value = 'Tidak ada pengelola aktif.'; return }
  credSaving.value = true
  try {
    const payload = {}
    if (credForm.value.username.trim()) payload.username = credForm.value.username.trim()
    if (credForm.value.password) payload.password = credForm.value.password
    await api.put(`/api/v1/users/${currentManager.value.id}`, payload)
    toast.success('Kredensial pengelola berhasil diperbarui!')
    credForm.value = { username: '', password: '' }
    await fetchWarehouseUsers(selectedWarehouse.value.id)
  } catch (err) { credError.value = err.response?.data?.message || 'Gagal.' }
  finally { credSaving.value = false }
}

// ─── Transfer ─────────────────────────────────────────────────────────────────
const transferUserId = ref(null)
const transferSaving = ref(false)
const transferError = ref(null)
async function doTransfer() {
  if (!transferUserId.value) return
  transferError.value = null; transferSaving.value = true
  try {
    await api.put(`/api/v1/warehouses/${selectedWarehouse.value.id}/manager`, { userId: transferUserId.value })
    toast.success('Pengelola gudang berhasil dipindahkan!')
    transferUserId.value = null
    await fetchWarehouseUsers(selectedWarehouse.value.id)
    await fetchAllUsers()
  } catch (err) { transferError.value = err.response?.data?.message || 'Gagal transfer pengelola.' }
  finally { transferSaving.value = false }
}

// ─── Create Form ──────────────────────────────────────────────────────────────
const createForm   = ref({ name: '', address: '', username: '', password: '', roleIds: [] })
const createErrors = ref({})
const createError  = ref(null)
const createSaving = ref(false)

function openCreate() {
  createForm.value = { name: '', address: '', username: '', password: '', roleIds: [] }
  createErrors.value = {}; createError.value = null
  view.value = 'create'; fetchRoles()
}
function toggleRole(id) {
  const idx = createForm.value.roleIds.indexOf(id)
  if (idx === -1) createForm.value.roleIds.push(id)
  else createForm.value.roleIds.splice(idx, 1)
}
function validateCreate() {
  createErrors.value = {}; let ok = true
  if (!createForm.value.name.trim()) { createErrors.value.name = 'Nama gudang wajib diisi'; ok = false }
  if (createForm.value.username.trim()) {
    if (!createForm.value.password || createForm.value.password.length < 6) { createErrors.value.password = 'Password minimal 6 karakter'; ok = false }
    if (createForm.value.roleIds.length === 0) { createErrors.value.roles = 'Pilih minimal satu role'; ok = false }
  }
  return ok
}
async function saveWarehouse() {
  if (!validateCreate()) return
  createSaving.value = true; createError.value = null
  try {
    const payload = { name: createForm.value.name, address: createForm.value.address }
    if (createForm.value.username.trim()) {
      payload.username = createForm.value.username
      payload.password = createForm.value.password
      payload.roleIds = createForm.value.roleIds
    }
    await api.post('/api/v1/warehouses', payload)
    toast.success('Gudang berhasil ditambahkan!')
    view.value = 'list'; fetchWarehouses()
  } catch (err) { createError.value = err.response?.data?.message || 'Gagal menyimpan gudang.' }
  finally { createSaving.value = false }
}

// ─── Add Staff to Warehouse ───────────────────────────────────────────────────
// User tersedia untuk ditambahkan ke gudang:
// - Bukan owner/admin
// - Belum di gudang manapun dan belum di cabang manapun
// - Karyawan cabang (punya role karyawan-cabang) tidak bisa masuk gudang
const staffAvailable = computed(() => {
  return allUsers.value.filter(u => {
    const isOwner = (u.roles || []).some(r =>
      ['owner', 'admin-partner', 'admin', 'super-admin'].includes(r.slug?.toLowerCase())
    )
    if (isOwner) return false
    if (u.warehouseId === selectedWarehouse.value?.id) return false // sudah di sini
    if (u.branchId || u.warehouseId) return false // di lokasi lain
    // Karyawan cabang tidak bisa ditambah ke gudang
    const hasBranchRole = (u.roles || []).some(r =>
      ['karyawan-cabang', 'pengelola-cabang', 'branch-manager', 'kasir'].includes(r.slug?.toLowerCase())
    )
    if (hasBranchRole) return false
    return true
  })
})

const warehouseStaff = computed(() =>
  warehouseUsers.value.filter(u =>
    Number(u.warehouseId) === Number(selectedWarehouse.value?.id) && !isWarehouseManager(u)
  )
)

const addStaffId     = ref(null)
const addStaffSaving = ref(false)
const addStaffError  = ref(null)

async function doAddStaff() {
  if (!addStaffId.value) return
  addStaffError.value = null; addStaffSaving.value = true
  try {
    await api.put(`/api/v1/users/${addStaffId.value}`, { warehouseId: selectedWarehouse.value.id })
    toast.success('Karyawan berhasil ditambahkan ke gudang!')
    addStaffId.value = null
    await fetchWarehouseUsers(selectedWarehouse.value.id)
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
    description: 'Karyawan akan dilepas dari gudang ini. Role karyawan gudang juga akan dihapus.',
    confirmLabel: 'Lepas',
    cancelLabel: 'Batal',
  })
  if (!ok) return
  try {
    await api.put(`/api/v1/users/${userId}`, { clearWarehouse: true })
    toast.success('Karyawan berhasil dilepas dari gudang.')
    await fetchWarehouseUsers(selectedWarehouse.value.id)
    await fetchAllUsers()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal melepas karyawan.')
  }
}

// Lepas pengelola aktif dari gudang (tanpa transfer ke orang lain)
async function releaseManager() {
  if (!currentManager.value) return
  const ok = await confirm({
    title: 'Lepas Pengelola',
    description: `"${currentManager.value.fullname || currentManager.value.username}" akan dilepas dari jabatan pengelola gudang ini. Role pengelola-gudang akan dihapus.`,
    confirmLabel: 'Lepas Jabatan',
    cancelLabel: 'Batal',
  })
  if (!ok) return
  try {
    await api.put(`/api/v1/users/${currentManager.value.id}`, { clearWarehouse: true })
    toast.success('Pengelola berhasil dilepas dari gudang.')
    await fetchWarehouseUsers(selectedWarehouse.value.id)
    await fetchAllUsers()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal melepas pengelola.')
  }
}

// ─── Delete ───────────────────────────────────────────────────────────────────
const deleteModal = ref({ show: false, warehouse: null, confirmText: '' })
const deleting    = ref(false)
function doDelete(w, e) { if (e) e.stopPropagation(); deleteModal.value = { show: true, warehouse: w, confirmText: '' } }
function closeDeleteModal() { deleteModal.value.show = false }
async function confirmDelete() {
  if (deleteModal.value.confirmText !== deleteModal.value.warehouse?.name) return
  deleting.value = true
  try {
    await api.delete(`/api/v1/warehouses/${deleteModal.value.warehouse.id}`)
    toast.success('Gudang berhasil dihapus!')
    if (view.value === 'detail') view.value = 'list'
    fetchWarehouses(); closeDeleteModal()
  } catch (err) { toast.error(err.response?.data?.message || 'Gagal menghapus.') }
  finally { deleting.value = false }
}

// ─── Utils ────────────────────────────────────────────────────────────────────
const COLORS = [
  { bg: '#ffedd5', color: '#c2410c' }, { bg: '#fef9c3', color: '#a16207' },
  { bg: '#dcfce7', color: '#15803d' }, { bg: '#cffafe', color: '#0e7490' },
  { bg: '#ede9fe', color: '#6d28d9' }, { bg: '#fce7f3', color: '#be185d' },
]
function avatarStyle(name = '') {
  const idx = (name.charCodeAt(0) || 0) % COLORS.length
  return { backgroundColor: COLORS[idx].bg, color: COLORS[idx].color }
}
function userInitial(u) { return (u?.fullname || u?.username || '?').charAt(0).toUpperCase() }

onMounted(fetchWarehouses)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <Transition name="fade" mode="out-in">

        <!-- ══════════ LIST ══════════ -->
        <div v-if="view === 'list'" key="list" class="flex flex-col gap-6">
          <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
            <div>
              <h1 class="text-2xl font-bold tracking-tight">Gudang</h1>
              <p class="text-muted-foreground text-sm mt-1">Kelola data gudang beserta akun pengelolanya.</p>
            </div>
            <div class="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto">
              <div class="w-full sm:w-72"><DataTableSearch v-model="searchQuery" placeholder="Cari gudang..." /></div>
              <Button v-if="can('warehouse.store') && !isSuperAdmin" @click="openCreate" size="sm" class="w-full sm:w-auto flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
                <Plus class="h-4 w-4" /><span>Tambah Gudang</span>
              </Button>
            </div>
          </div>

          <Alert v-if="error" variant="destructive">{{ error }}</Alert>

          <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
            <CardContent class="p-0">
              <div v-if="loading" class="flex flex-col items-center justify-center py-24 gap-3">
                <Loader2 class="h-7 w-7 animate-spin text-primary/50" /><p class="text-xs text-muted-foreground">Memuat data...</p>
              </div>
              <div v-else-if="filteredWarehouses.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
                <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4"><Warehouse class="h-7 w-7 opacity-40" /></div>
                <p class="text-sm font-medium">Belum ada data gudang</p>
                <Button v-if="can('warehouse.store') && !isSuperAdmin" size="sm" class="mt-4" @click="openCreate"><Plus class="h-3.5 w-3.5 mr-1.5" /> Tambah Gudang</Button>
              </div>
              <div v-else>
                <Transition name="fade">
                  <div v-if="selectedIds.length > 0" class="flex items-center justify-between px-5 py-3 bg-primary/5 border-b border-border">
                    <span class="text-xs font-semibold text-primary px-2 py-0.5 rounded bg-primary/10">{{ selectedIds.length }} Terpilih</span>
                    <Button v-if="can('warehouse.delete')" size="sm" variant="destructive" class="h-8 text-xs gap-1.5" @click="bulkDelete"><Trash2 class="h-3.5 w-3.5" /> Hapus</Button>
                  </div>
                </Transition>

                <!-- Mobile -->
                <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
                  <div v-for="w in paginatedWarehouses" :key="w.id" class="p-4 flex items-center gap-3" @click="openDetail(w)">
                    <div class="w-10 h-10 rounded-lg flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(w.name)">{{ w.name?.charAt(0).toUpperCase() }}</div>
                    <div class="flex-1 min-w-0">
                      <p class="font-semibold text-sm">{{ w.name }}</p>
                      <p class="text-xs text-zinc-400 truncate">{{ w.address || 'Belum ada alamat' }}</p>
                    </div>
                    <Button variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-destructive shrink-0" @click="doDelete(w, $event)"><Trash2 class="h-3.5 w-3.5" /></Button>
                  </div>
                </div>

                <!-- Desktop -->
                <div class="hidden md:block overflow-x-auto">
                  <table class="w-full text-sm">
                    <thead>
                      <tr class="border-b border-zinc-100 dark:border-zinc-800 bg-muted/40">
                        <th class="w-12 pl-5 py-3 text-left"><input type="checkbox" class="rounded border-zinc-300 dark:border-zinc-700 text-primary focus:ring-primary h-4 w-4 cursor-pointer" :checked="isAllSelected" @change="toggleSelectAll" /></th>
                        <th class="pl-2 py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Nama Gudang</th>
                        <th class="py-3 text-left text-xs font-semibold uppercase tracking-wide text-zinc-500">Alamat</th>
                        <th class="pr-5 py-3 text-right"></th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="w in paginatedWarehouses" :key="w.id"
                        class="group border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors cursor-pointer"
                        @click="openDetail(w)">
                        <td class="w-12 pl-5 py-3" @click.stop><input type="checkbox" class="rounded border-zinc-300 dark:border-zinc-700 text-primary focus:ring-primary h-4 w-4 cursor-pointer" :checked="selectedIds.includes(w.id)" @change="toggleSelect(w.id)" /></td>
                        <td class="pl-2 py-3">
                          <div class="flex items-center gap-3">
                            <div class="w-9 h-9 rounded-lg flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(w.name)">{{ w.name?.charAt(0).toUpperCase() }}</div>
                            <div>
                              <p class="font-semibold text-[13px] leading-tight">{{ w.name }}</p>
                              <p class="text-[10px] text-zinc-400">ID #{{ w.id }}</p>
                            </div>
                          </div>
                        </td>
                        <td class="py-3 max-w-[300px]">
                          <div v-if="w.address" class="flex items-center gap-1.5 text-xs text-zinc-500"><MapPin class="h-3 w-3 shrink-0 opacity-60" /><span class="truncate">{{ w.address }}</span></div>
                          <span v-else class="text-xs text-zinc-300 dark:text-zinc-600 italic">Belum ada alamat</span>
                        </td>
                        <td class="pr-5 py-3 text-right" @click.stop>
                          <div class="flex justify-end gap-1 opacity-0 group-hover:opacity-100 transition-all">
                            <Button v-if="can('warehouse.delete') && !isSuperAdmin" variant="ghost" size="icon" class="h-7 w-7 text-zinc-400 hover:text-destructive" @click="doDelete(w, $event)"><Trash2 class="h-3.5 w-3.5" /></Button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <DataTablePagination v-if="filteredWarehouses.length > 0 && !loading" :page="page" :page-size="pageSize" :total="filteredWarehouses.length" @update:page="page = $event" @update:page-size="pageSize = $event; page = 1" />
              </div>
            </CardContent>
          </Card>
        </div>

        <!-- ══════════ CREATE ══════════ -->
        <div v-else-if="view === 'create'" key="create" class="flex flex-col gap-6 max-w-2xl">
          <div class="flex items-center gap-4">
            <Button variant="ghost" size="icon" class="h-9 w-9 shrink-0" @click="view = 'list'"><ArrowLeft class="h-4 w-4" /></Button>
            <div>
              <h1 class="text-xl font-bold tracking-tight">Tambah Gudang</h1>
              <p class="text-muted-foreground text-sm">Akun pengelola bersifat opsional.</p>
            </div>
          </div>
          <Alert v-if="createError" variant="destructive">{{ createError }}</Alert>
          <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
            <CardContent class="p-6 space-y-5">
              <div class="space-y-1.5">
                <Label>Nama Gudang <span class="text-destructive">*</span></Label>
                <Input v-model="createForm.name" placeholder="Contoh: Gudang Sentral Jakarta" :disabled="createSaving" />
                <p v-if="createErrors.name" class="text-xs text-destructive">{{ createErrors.name }}</p>
              </div>
              <div class="space-y-1.5">
                <Label>Alamat</Label>
                <textarea v-model="createForm.address" rows="3" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring resize-none" placeholder="Alamat lengkap gudang..." :disabled="createSaving" />
              </div>

              <div class="border-t border-border pt-5">
                <div class="flex items-center gap-2 mb-4">
                  <div class="w-7 h-7 rounded-md bg-amber-500/10 flex items-center justify-center"><Users class="h-4 w-4 text-amber-600" /></div>
                  <div>
                    <p class="text-sm font-semibold">Akun Pengelola Gudang <span class="text-muted-foreground font-normal text-xs">(opsional)</span></p>
                    <p class="text-xs text-muted-foreground">Kosongkan username jika tidak perlu akun tersendiri.</p>
                  </div>
                </div>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div class="space-y-1.5">
                    <Label>Username</Label>
                    <Input v-model="createForm.username" placeholder="nairha" :disabled="createSaving" autocomplete="off" />
                  </div>
                  <div class="space-y-1.5">
                    <Label>Password</Label>
                    <Input type="password" v-model="createForm.password" placeholder="Min. 6 karakter" :disabled="createSaving" autocomplete="new-password" />
                    <p v-if="createErrors.password" class="text-xs text-destructive">{{ createErrors.password }}</p>
                  </div>
                </div>
              </div>

              <!-- Roles — hanya tampil jika username diisi -->
              <Transition name="fade">
                <div v-if="createForm.username?.trim()" class="space-y-2">
                  <div class="flex items-center gap-2"><Shield class="h-4 w-4 text-muted-foreground" /><Label class="text-sm font-semibold">Assign Role <span class="text-destructive">*</span></Label></div>
                  <div class="rounded-lg border border-input overflow-hidden">
                    <div class="max-h-52 overflow-y-auto divide-y divide-border">
                      <button v-for="role in roles" :key="role.id" type="button" @click="toggleRole(role.id)" :disabled="createSaving"
                        class="w-full flex items-center justify-between px-4 py-3 hover:bg-muted/50 transition-colors outline-none text-left"
                        :class="createForm.roleIds.includes(role.id) ? 'bg-primary/5' : ''">
                        <div class="flex items-center gap-3">
                          <div class="w-7 h-7 rounded-md flex items-center justify-center" :class="createForm.roleIds.includes(role.id) ? 'bg-primary/10' : 'bg-muted'">
                            <Shield class="h-3.5 w-3.5" :class="createForm.roleIds.includes(role.id) ? 'text-primary' : 'text-muted-foreground'" />
                          </div>
                          <div><p class="font-medium text-sm">{{ role.name }}</p><p class="text-[10px] text-muted-foreground font-mono">{{ role.slug }}</p></div>
                        </div>
                        <div class="h-4 w-4 rounded border flex items-center justify-center" :class="createForm.roleIds.includes(role.id) ? 'bg-primary border-primary' : 'border-zinc-300 dark:border-zinc-600'">
                          <Check v-if="createForm.roleIds.includes(role.id)" class="h-2.5 w-2.5 text-white" />
                        </div>
                      </button>
                      <div v-if="roles.length === 0" class="p-4 text-xs text-muted-foreground italic text-center">Tidak ada role.</div>
                    </div>
                  </div>
                  <p v-if="createErrors.roles" class="text-xs text-destructive">{{ createErrors.roles }}</p>
                </div>
              </Transition>
            </CardContent>
          </Card>
          <div class="flex justify-end gap-3">
            <Button variant="outline" @click="view = 'list'" :disabled="createSaving">Batal</Button>
            <Button @click="saveWarehouse" :disabled="createSaving" class="bg-primary hover:bg-primary/90 min-w-[120px]">
              <Loader2 v-if="createSaving" class="h-4 w-4 mr-2 animate-spin" /> Simpan Gudang
            </Button>
          </div>
        </div>

        <!-- ══════════ DETAIL ══════════ -->
        <div v-else-if="view === 'detail'" key="detail" class="flex flex-col gap-6">
          <div class="flex items-start gap-4">
            <Button variant="ghost" size="icon" class="h-9 w-9 shrink-0 mt-0.5" @click="view = 'list'"><ArrowLeft class="h-4 w-4" /></Button>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-3 flex-wrap">
                <div class="w-10 h-10 rounded-xl flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(selectedWarehouse?.name)">{{ selectedWarehouse?.name?.charAt(0).toUpperCase() }}</div>
                <div>
                  <h1 class="text-xl font-bold tracking-tight">{{ selectedWarehouse?.name }}</h1>
                  <p class="text-xs text-muted-foreground">ID #{{ selectedWarehouse?.id }} · {{ selectedWarehouse?.address || 'Belum ada alamat' }}</p>
                </div>
              </div>
            </div>
            <Button v-if="can('warehouse.delete') && !isSuperAdmin" variant="outline" size="sm" class="text-destructive border-destructive/30 hover:bg-destructive/5 shrink-0" @click="doDelete(selectedWarehouse, $event)">
              <Trash2 class="h-3.5 w-3.5 mr-1.5" /> Hapus
            </Button>
          </div>

          <!-- Tabs -->
          <div class="flex gap-1 p-1 bg-muted/50 dark:bg-zinc-900/50 rounded-lg w-fit border border-border overflow-x-auto">
            <button v-for="tab in [{id:'info',label:'Info'},{id:'staff',label:'Karyawan'},{id:'transfer',label:'Transfer Pengelola'},{id:'credentials',label:'Ubah Kredensial'}]"
              :key="tab.id" @click="activePanel = tab.id"
              class="px-3 py-1.5 rounded-md text-sm font-medium transition-all whitespace-nowrap"
              :class="activePanel === tab.id ? 'bg-background shadow-sm text-foreground' : 'text-muted-foreground hover:text-foreground'">
              {{ tab.label }}
            </button>
          </div>

          <div v-if="detailLoading" class="flex items-center justify-center py-16"><Loader2 class="h-7 w-7 animate-spin text-primary/50" /></div>

          <template v-else>
            <!-- TAB INFO -->
            <Card v-if="activePanel === 'info'" class="border-zinc-200 dark:border-zinc-800 shadow-sm max-w-xl">
              <CardContent class="p-6 space-y-5">
                <Alert v-if="editError" variant="destructive">{{ editError }}</Alert>
                <div class="space-y-1.5"><Label>Nama Gudang <span class="text-destructive">*</span></Label><Input v-model="editForm.name" :disabled="editSaving" /><p v-if="editErrors.name" class="text-xs text-destructive">{{ editErrors.name }}</p></div>
                <div class="space-y-1.5"><Label>Alamat</Label><textarea v-model="editForm.address" rows="3" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring resize-none" :disabled="editSaving" /></div>
                <div class="flex justify-end"><Button @click="saveInfo" :disabled="editSaving" class="bg-primary hover:bg-primary/90"><Loader2 v-if="editSaving" class="h-4 w-4 mr-2 animate-spin" /> Simpan Perubahan</Button></div>
              </CardContent>
            </Card>

            <!-- TAB KARYAWAN -->
            <div v-else-if="activePanel === 'staff'" class="flex flex-col gap-4 max-w-2xl">
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5">
                  <p class="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Pengelola Gudang</p>
                  <div v-if="currentManager" class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(currentManager.fullname || currentManager.username)">{{ userInitial(currentManager) }}</div>
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
                  <div v-else class="flex items-center gap-2 text-zinc-400"><UserX class="h-4 w-4" /><p class="text-sm">Belum ada pengelola aktif</p></div>
                </CardContent>
              </Card>

              <div class="flex items-center justify-between"><p class="text-sm font-semibold">Karyawan</p><Badge variant="secondary">{{ warehouseStaff.length }} orang</Badge></div>
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-0">
                  <div v-if="warehouseStaff.length === 0" class="flex flex-col items-center justify-center py-10 text-muted-foreground"><Users class="h-7 w-7 opacity-30 mb-2" /><p class="text-sm">Belum ada karyawan di gudang ini</p></div>
                  <div v-else class="divide-y divide-zinc-100 dark:divide-zinc-800/60">
                    <div v-for="u in warehouseStaff" :key="u.id" class="flex items-center gap-3 px-4 py-3">
                      <div class="w-9 h-9 rounded-full flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(u.fullname || u.username)">{{ userInitial(u) }}</div>
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

              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <p class="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Tambah Karyawan</p>
                  <Alert v-if="addStaffError" variant="destructive">{{ addStaffError }}</Alert>
                  <div v-if="staffAvailable.length === 0" class="text-sm text-muted-foreground italic py-2">Tidak ada user tersedia.</div>
                  <div v-else class="rounded-lg border border-input overflow-hidden max-h-60 overflow-y-auto">
                    <div class="divide-y divide-border">
                      <button v-for="u in staffAvailable" :key="u.id" type="button" @click="addStaffId = addStaffId === u.id ? null : u.id"
                        class="w-full flex items-center gap-3 px-4 py-3 hover:bg-muted/50 transition-colors text-left outline-none"
                        :class="addStaffId === u.id ? 'bg-primary/5' : ''">
                        <div class="w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs shrink-0" :style="avatarStyle(u.fullname || u.username)">{{ userInitial(u) }}</div>
                        <div class="flex-1 min-w-0"><p class="font-medium text-sm">{{ u.fullname || u.username }}</p><p class="text-xs text-muted-foreground">@{{ u.username }}</p></div>
                        <div class="h-4 w-4 rounded-full border-2 flex items-center justify-center" :class="addStaffId === u.id ? 'border-primary' : 'border-zinc-300 dark:border-zinc-600'">
                          <div v-if="addStaffId === u.id" class="w-2 h-2 rounded-full bg-primary" />
                        </div>
                      </button>
                    </div>
                  </div>
                  <div class="flex justify-end">
                    <Button @click="doAddStaff" :disabled="!addStaffId || addStaffSaving" class="bg-primary hover:bg-primary/90 gap-2">
                      <Loader2 v-if="addStaffSaving" class="h-4 w-4 animate-spin" /><Plus v-else class="h-4 w-4" /> Tambah ke Gudang
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </div>

            <!-- TAB TRANSFER -->
            <div v-else-if="activePanel === 'transfer'" class="flex flex-col gap-4 max-w-xl">
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5">
                  <p class="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Pengelola Saat Ini</p>
                  <div v-if="currentManager" class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(currentManager.fullname || currentManager.username)">{{ userInitial(currentManager) }}</div>
                    <div><p class="font-semibold text-sm">{{ currentManager.fullname || currentManager.username }}</p><p class="text-xs text-muted-foreground">@{{ currentManager.username }}</p></div>
                    <UserCheck class="h-4 w-4 text-emerald-500 ml-auto" />
                  </div>
                  <div v-else class="flex items-center gap-2 text-zinc-400"><UserX class="h-4 w-4" /><p class="text-sm">Belum ada pengelola aktif</p></div>
                </CardContent>
              </Card>
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <p class="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Transfer ke Pengelola Baru</p>
                  <Alert v-if="transferError" variant="destructive">{{ transferError }}</Alert>
                  <div v-if="availableForTransfer.length === 0" class="text-sm text-muted-foreground italic py-2">Tidak ada user yang tersedia. User harus bebas dari pengelolaan cabang/gudang lain.</div>
                  <div v-else class="rounded-lg border border-input overflow-hidden max-h-60 overflow-y-auto">
                    <div class="divide-y divide-border">
                      <button v-for="u in availableForTransfer" :key="u.id" type="button" @click="transferUserId = transferUserId === u.id ? null : u.id"
                        class="w-full flex items-center gap-3 px-4 py-3 hover:bg-muted/50 transition-colors text-left outline-none"
                        :class="transferUserId === u.id ? 'bg-primary/5' : ''">
                        <div class="w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs shrink-0" :style="avatarStyle(u.fullname || u.username)">{{ userInitial(u) }}</div>
                        <div class="flex-1 min-w-0"><p class="font-medium text-sm">{{ u.fullname || u.username }}</p><p class="text-xs text-muted-foreground">@{{ u.username }}</p></div>
                        <div class="h-4 w-4 rounded-full border-2 flex items-center justify-center" :class="transferUserId === u.id ? 'border-primary' : 'border-zinc-300 dark:border-zinc-600'">
                          <div v-if="transferUserId === u.id" class="w-2 h-2 rounded-full bg-primary" />
                        </div>
                      </button>
                    </div>
                  </div>
                  <div class="flex justify-end">
                    <Button @click="doTransfer" :disabled="!transferUserId || transferSaving" class="bg-primary hover:bg-primary/90 gap-2">
                      <Loader2 v-if="transferSaving" class="h-4 w-4 animate-spin" /><ArrowRightLeft v-else class="h-4 w-4" /> Transfer Pengelola
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </div>

            <!-- TAB CREDENTIALS -->
            <div v-else-if="activePanel === 'credentials'" class="flex flex-col gap-4 max-w-xl">
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <div v-if="!currentManager" class="flex items-center gap-2 text-zinc-400 py-2"><UserX class="h-4 w-4" /><p class="text-sm">Tidak ada pengelola aktif.</p></div>
                  <template v-else>
                    <div class="flex items-center gap-3 p-3 rounded-lg bg-muted/50 border border-border">
                      <div class="w-9 h-9 rounded-full flex items-center justify-center font-bold text-sm shrink-0" :style="avatarStyle(currentManager.fullname || currentManager.username)">{{ userInitial(currentManager) }}</div>
                      <div><p class="font-semibold text-sm">{{ currentManager.fullname || currentManager.username }}</p><p class="text-xs text-muted-foreground">@{{ currentManager.username }}</p></div>
                      <KeyRound class="h-4 w-4 text-muted-foreground ml-auto" />
                    </div>
                    <Alert v-if="credError" variant="destructive">{{ credError }}</Alert>
                    <div class="space-y-1.5"><Label>Username Baru <span class="text-xs text-muted-foreground">(kosongkan jika tidak diganti)</span></Label><Input v-model="credForm.username" :placeholder="currentManager.username" :disabled="credSaving" autocomplete="off" /></div>
                    <div class="space-y-1.5"><Label>Password Baru <span class="text-xs text-muted-foreground">(min. 6 karakter)</span></Label><Input type="password" v-model="credForm.password" placeholder="Password baru..." :disabled="credSaving" autocomplete="new-password" /></div>
                    <div class="flex justify-end">
                      <Button @click="saveCredentials" :disabled="credSaving" class="bg-primary hover:bg-primary/90 gap-2">
                        <Loader2 v-if="credSaving" class="h-4 w-4 animate-spin" /><KeyRound v-else class="h-4 w-4" /> Perbarui Kredensial
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
              <h3 class="text-lg font-bold">Hapus Gudang</h3>
            </div>
            <p class="text-sm text-muted-foreground mb-4">Anda akan menghapus <span class="font-bold text-foreground">{{ deleteModal.warehouse?.name }}</span> secara permanen.</p>
            <div class="space-y-2 mb-6">
              <Label class="text-xs text-muted-foreground">Ketik nama gudang untuk konfirmasi:</Label>
              <Input v-model="deleteModal.confirmText" :placeholder="deleteModal.warehouse?.name" class="border-destructive/30" />
            </div>
            <div class="flex justify-end gap-3">
              <Button variant="outline" @click="closeDeleteModal" :disabled="deleting">Batal</Button>
              <Button variant="destructive" :disabled="deleteModal.confirmText !== deleteModal.warehouse?.name || deleting" @click="confirmDelete">
                <Loader2 v-if="deleting" class="h-4 w-4 mr-2 animate-spin" /> Hapus Permanen
              </Button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>
