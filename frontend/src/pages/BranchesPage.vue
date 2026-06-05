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
import Badge from '@/components/ui/badge/Badge.vue'
import Alert from '@/components/ui/Alert.vue'
import api from '@/lib/api'
import {
  Plus, Pencil, Trash2, Loader2, X, Building2,
  MapPin, ArrowLeft, Shield, Check, ChevronDown, Filter, Users
} from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.isAdmin)
const isSuperAdmin = computed(() => authStore.isSuperAdmin)

// ─── State ────────────────────────────────────────────────────────────────────
const branches   = ref([])
const roles      = ref([])
const loading    = ref(false)
const error      = ref(null)
const searchQuery = ref('')
const page       = ref(1)
const pageSize   = ref(10)

// ─── Selection ────────────────────────────────────────────────────────────────
const selectedIds = ref([])

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
  const visible = paginatedBranches.value
  return visible.length > 0 && visible.every(b => selectedIds.value.includes(b.id))
})

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
  } catch (err) {
    error.value = 'Gagal memuat data cabang.'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

async function fetchRoles() {
  try {
    const res = await api.get('/api/v1/roles')
    roles.value = res.data?.data || res.data || []
  } catch (_) {}
}

// ─── Bulk Delete ──────────────────────────────────────────────────────────────
async function bulkDelete() {
  const count = selectedIds.value.length
  if (!count) return
  const ok = await confirm({
    title: 'Hapus Cabang Terpilih',
    description: `Hapus ${count} cabang secara permanen? Data stok di cabang ini mungkin terpengaruh.`,
    confirmLabel: 'Hapus',
    cancelLabel: 'Batal',
  })
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

// ─── Form / Drawer ────────────────────────────────────────────────────────────
const showDrawer   = ref(false)
const modalMode    = ref('create')
const saving       = ref(false)
const formError    = ref(null)

const emptyForm = () => ({
  id: null,
  name: '',
  address: '',
  username: '',
  password: '',
  roleIds: [],
})

const form       = ref(emptyForm())
const formErrors = ref({ name: '', username: '', password: '' })

function openCreate() {
  form.value = emptyForm()
  formErrors.value = { name: '', username: '', password: '' }
  formError.value = null
  modalMode.value = 'create'
  showDrawer.value = true
  fetchRoles()
}

function openEdit(b) {
  form.value = { id: b.id, name: b.name, address: b.address || '', username: '', password: '', roleIds: [] }
  formErrors.value = { name: '', username: '', password: '' }
  formError.value = null
  modalMode.value = 'edit'
  showDrawer.value = true
}

function closeDrawer() { showDrawer.value = false }

function toggleRole(id) {
  const idx = form.value.roleIds.indexOf(id)
  if (idx === -1) form.value.roleIds.push(id)
  else form.value.roleIds.splice(idx, 1)
}

function validate() {
  formErrors.value = { name: '', username: '', password: '' }
  let ok = true
  if (!form.value.name.trim()) { formErrors.value.name = 'Nama cabang wajib diisi'; ok = false }
  if (modalMode.value === 'create') {
    if (!form.value.username.trim()) { formErrors.value.username = 'Username wajib diisi'; ok = false }
    if (!form.value.password || form.value.password.length < 6) {
      formErrors.value.password = 'Password minimal 6 karakter'; ok = false
    }
  }
  return ok
}

async function saveBranch() {
  if (!validate()) return
  saving.value = true
  formError.value = null
  try {
    const payload = {
      name: form.value.name,
      address: form.value.address,
      ...(modalMode.value === 'create' && {
        username: form.value.username,
        password: form.value.password,
        roleIds: form.value.roleIds,
      })
    }
    if (modalMode.value === 'create') {
      await api.post('/api/v1/branches', payload)
      toast.success('Cabang berhasil ditambahkan!')
    } else {
      await api.put(`/api/v1/branches/${form.value.id}`, payload)
      toast.success('Cabang berhasil diperbarui!')
    }
    closeDrawer()
    fetchBranches()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Gagal menyimpan cabang.'
  } finally {
    saving.value = false
  }
}

// ─── Delete Modal ─────────────────────────────────────────────────────────────
const deleteModal = ref({ show: false, branch: null, confirmText: '' })
const deleting    = ref(false)

function doDelete(b) { deleteModal.value = { show: true, branch: b, confirmText: '' } }
function closeDeleteModal() { deleteModal.value.show = false }

async function confirmDelete() {
  if (deleteModal.value.confirmText !== deleteModal.value.branch?.name) return
  deleting.value = true
  try {
    await api.delete(`/api/v1/branches/${deleteModal.value.branch.id}`)
    toast.success('Cabang berhasil dihapus!')
    fetchBranches()
    closeDeleteModal()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menghapus cabang.')
  } finally {
    deleting.value = false
  }
}

// ─── Utils ────────────────────────────────────────────────────────────────────
const AVATAR_COLORS = [
  { bg: '#dbeafe', color: '#1d4ed8' },
  { bg: '#dcfce7', color: '#15803d' },
  { bg: '#fef9c3', color: '#a16207' },
  { bg: '#fce7f3', color: '#be185d' },
  { bg: '#ede9fe', color: '#6d28d9' },
  { bg: '#ffedd5', color: '#c2410c' },
]
function avatarStyle(name = '') {
  const idx = name.charCodeAt(0) % AVATAR_COLORS.length
  return { backgroundColor: AVATAR_COLORS[idx].bg, color: AVATAR_COLORS[idx].color }
}

onMounted(fetchBranches)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <Transition name="fade" mode="out-in">

        <!-- ══════════ TABLE VIEW ══════════ -->
        <div v-if="!showDrawer" key="table" class="flex flex-col gap-6">

          <!-- Header -->
          <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
            <div>
              <h1 class="text-2xl font-bold tracking-tight">Cabang</h1>
              <p class="text-muted-foreground text-sm mt-1">Kelola data cabang beserta akun penggunanya.</p>
            </div>
            <div class="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto">
              <div class="w-full sm:w-72">
                <DataTableSearch v-model="searchQuery" placeholder="Cari cabang..." />
              </div>
              <Button
                v-if="can('branch.store') && !isSuperAdmin"
                @click="openCreate"
                size="sm"
                class="w-full sm:w-auto flex items-center justify-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground"
              >
                <Plus class="h-4 w-4" />
                <span>Tambah Cabang</span>
              </Button>
            </div>
          </div>

          <Alert v-if="error" variant="destructive">{{ error }}</Alert>

          <!-- Table Card -->
          <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
            <CardContent class="p-0">

              <!-- Loading -->
              <div v-if="loading" class="flex flex-col items-center justify-center py-24 gap-3">
                <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
                <p class="text-xs text-muted-foreground">Memuat data...</p>
              </div>

              <!-- Empty -->
              <div v-else-if="filteredBranches.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
                <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
                  <Building2 class="h-7 w-7 opacity-40" />
                </div>
                <p class="text-sm font-medium">Belum ada data cabang</p>
                <p class="text-xs text-muted-foreground/70 mt-1">Mulai dengan menambahkan cabang pertama Anda.</p>
                <Button v-if="can('branch.store') && !isSuperAdmin" size="sm" class="mt-4" @click="openCreate">
                  <Plus class="h-3.5 w-3.5 mr-1.5" /> Tambah Cabang
                </Button>
              </div>

              <div v-else>
                <!-- Selection Banner -->
                <Transition name="fade">
                  <div v-if="selectedIds.length > 0" class="flex items-center justify-between px-5 py-3 bg-primary/5 border-b border-border">
                    <div class="flex items-center gap-2">
                      <span class="text-xs font-semibold text-primary px-2 py-0.5 rounded bg-primary/10">{{ selectedIds.length }} Terpilih</span>
                      <span class="text-xs text-muted-foreground">Baris terpilih.</span>
                    </div>
                    <Button v-if="can('branch.delete')" size="sm" variant="destructive" class="h-8 text-xs gap-1.5" @click="bulkDelete">
                      <Trash2 class="h-3.5 w-3.5" /> Hapus
                    </Button>
                  </div>
                </Transition>

                <!-- Mobile Cards -->
                <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
                  <div v-for="b in paginatedBranches" :key="b.id" class="p-4 flex flex-col gap-3">
                    <div class="flex items-start justify-between">
                      <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-lg flex items-center justify-center font-bold text-sm shrink-0 border border-zinc-200/50 dark:border-zinc-800/50" :style="avatarStyle(b.name)">
                          {{ b.name?.charAt(0).toUpperCase() }}
                        </div>
                        <div>
                          <p class="font-semibold text-sm text-zinc-900 dark:text-zinc-100">{{ b.name }}</p>
                          <p v-if="b.address" class="text-xs text-zinc-400 truncate max-w-[160px]">{{ b.address }}</p>
                        </div>
                      </div>
                      <div class="flex gap-1 shrink-0">
                        <Button v-if="can('branch.update') && !isSuperAdmin" variant="ghost" size="icon" class="h-8 w-8" @click="openEdit(b)">
                          <Pencil class="h-3.5 w-3.5" />
                        </Button>
                        <Button v-if="can('branch.delete') && !isSuperAdmin" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-destructive" @click="doDelete(b)">
                          <Trash2 class="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </div>
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
                      <tr
                        v-for="b in paginatedBranches"
                        :key="b.id"
                        class="group border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors cursor-pointer"
                        @click="can('branch.update') && !isSuperAdmin && openEdit(b)"
                      >
                        <td class="w-12 pl-5 py-3" @click.stop>
                          <input type="checkbox" class="rounded border-zinc-300 dark:border-zinc-700 text-primary focus:ring-primary h-4 w-4 cursor-pointer" :checked="selectedIds.includes(b.id)" @change="toggleSelect(b.id)" />
                        </td>
                        <td class="pl-2 py-3">
                          <div class="flex items-center gap-3">
                            <div class="w-9 h-9 rounded-lg flex items-center justify-center font-bold text-sm shrink-0 border border-zinc-200/50 dark:border-zinc-800/50" :style="avatarStyle(b.name)">
                              {{ b.name?.charAt(0).toUpperCase() }}
                            </div>
                            <div>
                              <p class="font-semibold text-zinc-900 dark:text-zinc-100 text-[13px] leading-tight">{{ b.name }}</p>
                              <p class="text-[10px] text-zinc-400 mt-0.5">ID #{{ b.id }}</p>
                            </div>
                          </div>
                        </td>
                        <td class="py-3 max-w-[320px]">
                          <div v-if="b.address" class="flex items-center gap-1.5 text-xs text-zinc-500">
                            <MapPin class="h-3 w-3 shrink-0 opacity-60" />
                            <span class="truncate">{{ b.address }}</span>
                          </div>
                          <span v-else class="text-xs text-zinc-300 dark:text-zinc-600 italic">Belum ada alamat</span>
                        </td>
                        <td class="pr-5 py-3 text-right" @click.stop>
                          <div class="flex justify-end gap-1">
                            <Button
                              v-if="can('branch.update') && !isSuperAdmin"
                              variant="ghost" size="icon"
                              class="h-7 w-7 text-zinc-400 hover:text-primary opacity-0 group-hover:opacity-100 transition-all"
                              @click="openEdit(b)"
                            >
                              <Pencil class="h-3.5 w-3.5" />
                            </Button>
                            <Button
                              v-if="can('branch.delete') && !isSuperAdmin"
                              variant="ghost" size="icon"
                              class="h-7 w-7 text-zinc-400 hover:text-destructive opacity-0 group-hover:opacity-100 transition-all"
                              @click="doDelete(b)"
                            >
                              <Trash2 class="h-3.5 w-3.5" />
                            </Button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <DataTablePagination
                  v-if="filteredBranches.length > 0 && !loading"
                  :page="page"
                  :page-size="pageSize"
                  :total="filteredBranches.length"
                  @update:page="page = $event"
                  @update:page-size="pageSize = $event; page = 1"
                />
              </div>
            </CardContent>
          </Card>
        </div>

        <!-- ══════════ FORM VIEW ══════════ -->
        <div v-else key="form" class="flex flex-col gap-6 max-w-2xl">
          <!-- Form Header -->
          <div class="flex items-center gap-4">
            <Button variant="ghost" size="icon" class="h-9 w-9 shrink-0" @click="closeDrawer">
              <ArrowLeft class="h-4 w-4" />
            </Button>
            <div>
              <h1 class="text-xl font-bold tracking-tight">
                {{ modalMode === 'create' ? 'Tambah Cabang' : 'Edit Cabang' }}
              </h1>
              <p class="text-muted-foreground text-sm mt-0.5">
                {{ modalMode === 'create' ? 'Buat cabang baru beserta akun penggunanya.' : 'Perbarui informasi cabang.' }}
              </p>
            </div>
          </div>

          <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>

          <!-- Form Card -->
          <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
            <CardContent class="p-6 space-y-6">

              <!-- Nama Cabang -->
              <div class="space-y-1.5">
                <Label for="br-name">Nama Cabang <span class="text-destructive">*</span></Label>
                <Input id="br-name" v-model="form.name" placeholder="Contoh: Cabang Sudirman" :disabled="saving" />
                <p v-if="formErrors.name" class="text-xs text-destructive mt-1">{{ formErrors.name }}</p>
              </div>

              <!-- Alamat -->
              <div class="space-y-1.5">
                <Label for="br-address">Alamat</Label>
                <textarea
                  id="br-address"
                  v-model="form.address"
                  rows="3"
                  class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring placeholder:text-muted-foreground resize-none"
                  placeholder="Alamat lengkap cabang..."
                  :disabled="saving"
                />
              </div>

              <!-- Akun Pengguna (hanya saat create) -->
              <template v-if="modalMode === 'create'">
                <div class="border-t border-border pt-6">
                  <div class="flex items-center gap-2 mb-4">
                    <div class="w-7 h-7 rounded-md bg-primary/10 flex items-center justify-center">
                      <Users class="h-4 w-4 text-primary" />
                    </div>
                    <div>
                      <p class="text-sm font-semibold text-foreground">Akun Pengguna Cabang</p>
                      <p class="text-xs text-muted-foreground">Akan dibuat otomatis saat cabang disimpan.</p>
                    </div>
                  </div>
                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div class="space-y-1.5">
                      <Label for="br-username">Username <span class="text-destructive">*</span></Label>
                      <Input id="br-username" v-model="form.username" placeholder="username_cabang" :disabled="saving" autocomplete="off" />
                      <p v-if="formErrors.username" class="text-xs text-destructive">{{ formErrors.username }}</p>
                    </div>
                    <div class="space-y-1.5">
                      <Label for="br-password">Password <span class="text-destructive">*</span></Label>
                      <Input id="br-password" type="password" v-model="form.password" placeholder="Min. 6 karakter" :disabled="saving" autocomplete="new-password" />
                      <p v-if="formErrors.password" class="text-xs text-destructive">{{ formErrors.password }}</p>
                    </div>
                  </div>
                </div>

                <!-- Assign Roles -->
                <div class="space-y-2">
                  <div class="flex items-center gap-2 mb-1">
                    <Shield class="h-4 w-4 text-muted-foreground" />
                    <Label class="text-sm font-semibold">Assign Role <span class="text-destructive">*</span></Label>
                  </div>
                  <div class="rounded-lg border border-input overflow-hidden">
                    <div class="max-h-52 overflow-y-auto divide-y divide-border">
                      <button
                        v-for="role in roles"
                        :key="role.id"
                        type="button"
                        @click="toggleRole(role.id)"
                        :disabled="saving"
                        class="w-full flex items-center justify-between px-4 py-3 hover:bg-muted/50 transition-colors outline-none text-left"
                        :class="form.roleIds.includes(role.id) ? 'bg-primary/5' : ''"
                      >
                        <div class="flex items-center gap-3">
                          <div class="w-7 h-7 rounded-md flex items-center justify-center" :class="form.roleIds.includes(role.id) ? 'bg-primary/10' : 'bg-muted'">
                            <Shield class="h-3.5 w-3.5" :class="form.roleIds.includes(role.id) ? 'text-primary' : 'text-muted-foreground'" />
                          </div>
                          <div>
                            <p class="font-medium text-sm text-foreground">{{ role.name }}</p>
                            <p class="text-[10px] text-muted-foreground font-mono">{{ role.slug }}</p>
                          </div>
                        </div>
                        <div class="h-4 w-4 rounded border flex items-center justify-center transition-all"
                          :class="form.roleIds.includes(role.id) ? 'bg-primary border-primary' : 'border-zinc-300 dark:border-zinc-600'">
                          <Check v-if="form.roleIds.includes(role.id)" class="h-2.5 w-2.5 text-white" />
                        </div>
                      </button>
                      <div v-if="roles.length === 0" class="p-4 text-xs text-muted-foreground italic text-center">
                        Tidak ada role tersedia.
                      </div>
                    </div>
                  </div>
                  <p v-if="form.roleIds.length === 0" class="text-xs text-destructive">Wajib pilih minimal satu role.</p>
                </div>
              </template>
            </CardContent>
          </Card>

          <!-- Form Actions -->
          <div class="flex justify-end gap-3">
            <Button variant="outline" @click="closeDrawer" :disabled="saving">Batal</Button>
            <Button @click="saveBranch" :disabled="saving" class="bg-primary hover:bg-primary/90 min-w-[120px]">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              {{ modalMode === 'create' ? 'Simpan Cabang' : 'Perbarui' }}
            </Button>
          </div>
        </div>

      </Transition>
    </div>

    <!-- ─── Delete Modal ─── -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[60] bg-black/40 backdrop-blur-sm" @click="closeDeleteModal" />
      </Transition>
      <Transition name="scale">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none">
          <div class="bg-card rounded-xl shadow-2xl w-full max-w-md border border-border pointer-events-auto p-6">
            <div class="flex items-center gap-3 text-destructive mb-3">
              <div class="w-10 h-10 rounded-full bg-destructive/10 flex items-center justify-center shrink-0">
                <Trash2 class="h-5 w-5" />
              </div>
              <h3 class="text-lg font-bold">Hapus Cabang</h3>
            </div>
            <p class="text-sm text-muted-foreground mb-4 leading-relaxed">
              Anda akan menghapus <span class="font-bold text-foreground">{{ deleteModal.branch?.name }}</span> secara permanen. Data stok di cabang ini mungkin terpengaruh.
            </p>
            <div class="space-y-2 mb-6">
              <Label class="text-xs text-muted-foreground">Ketik nama cabang untuk konfirmasi:</Label>
              <Input
                v-model="deleteModal.confirmText"
                :placeholder="deleteModal.branch?.name"
                class="border-destructive/30 focus:ring-destructive/20"
              />
            </div>
            <div class="flex justify-end gap-3">
              <Button variant="outline" @click="closeDeleteModal" :disabled="deleting">Batal</Button>
              <Button
                variant="destructive"
                :disabled="deleteModal.confirmText !== deleteModal.branch?.name || deleting"
                @click="confirmDelete"
              >
                <Loader2 v-if="deleting" class="h-4 w-4 mr-2 animate-spin" />
                Hapus Permanen
              </Button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>
