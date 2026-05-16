<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Badge from '@/components/ui/Badge.vue'
import Alert from '@/components/ui/Alert.vue'
import api from '@/lib/api'
import {
  Plus, Pencil, Trash2, Loader2, X, Users, Globe, Check,
  Eye, EyeOff, Shield, Building2, UserPlus
} from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()

// ─── State ────────────────────────────────────────────────────────────────────
const partners = ref([])
const loading = ref(false)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

const filteredPartners = computed(() => {
  if (!searchQuery.value) return partners.value
  const q = searchQuery.value.toLowerCase()
  return partners.value.filter(p =>
    p.name?.toLowerCase().includes(q) ||
    p.slug?.toLowerCase().includes(q) ||
    p.plan?.toLowerCase().includes(q)
  )
})

const paginatedPartners = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredPartners.value.slice(start, start + pageSize.value)
})

// ─── Form State ───────────────────────────────────────────────────────────────
const showDrawer = ref(false)
const modalMode = ref('create')
const saving = ref(false)
const formError = ref(null)
const showAdminPassword = ref(false)

// Create form — includes admin user
const emptyCreateForm = () => ({
  // Partner fields
  name: '',
  plan: 'BASIC',
  // Admin user fields
  admin_username: '',
  admin_email: '',
  admin_password: '',
})

// Edit form — only partner meta
const emptyEditForm = () => ({
  id: null,
  name: '',
  plan: 'BASIC',
  is_active: true,
})

const createForm = ref(emptyCreateForm())
const editForm = ref(emptyEditForm())

// Computed slug preview for create
const slugPreview = computed(() => {
  return (createForm.value.name || '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '')
})

// ─── Fetch ────────────────────────────────────────────────────────────────────
async function fetchPartners() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/partners')
    partners.value = Array.isArray(res.data.data) ? res.data.data : []
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal memuat data partner.')
  } finally {
    loading.value = false
  }
}

// ─── Open Drawers ─────────────────────────────────────────────────────────────
function openCreate() {
  createForm.value = emptyCreateForm()
  formError.value = null
  showAdminPassword.value = false
  modalMode.value = 'create'
  showDrawer.value = true
}

function openEdit(p) {
  editForm.value = {
    id: p.id,
    name: p.name,
    plan: p.plan || 'BASIC',
    is_active: p.is_active !== false,
  }
  formError.value = null
  modalMode.value = 'edit'
  showDrawer.value = true
}

function closeDrawer() {
  showDrawer.value = false
  formError.value = null
}

// ─── Save ─────────────────────────────────────────────────────────────────────
async function savePartner() {
  saving.value = true
  formError.value = null
  try {
    if (modalMode.value === 'create') {
      // Validasi frontend
      if (!createForm.value.name.trim()) {
        formError.value = 'Nama partner wajib diisi.'
        return
      }
      if (!createForm.value.admin_username.trim()) {
        formError.value = 'Username admin wajib diisi.'
        return
      }
      if (!createForm.value.admin_email.trim()) {
        formError.value = 'Email admin wajib diisi.'
        return
      }
      if (!createForm.value.admin_password || createForm.value.admin_password.length < 6) {
        formError.value = 'Password admin minimal 6 karakter.'
        return
      }

      const payload = {
        name: createForm.value.name.trim(),
        plan: createForm.value.plan,
        admin: {
          username: createForm.value.admin_username.trim(),
          email: createForm.value.admin_email.trim(),
          password: createForm.value.admin_password,
        }
      }
      await api.post('/api/v1/partners', payload)
      toast.success('Partner & admin user berhasil dibuat!')
    } else {
      const payload = {
        name: editForm.value.name,
        plan: editForm.value.plan,
        isActive: editForm.value.is_active,
      }
      await api.put(`/api/v1/partners/${editForm.value.id}`, payload)
      toast.success('Partner berhasil diperbarui!')
    }
    closeDrawer()
    fetchPartners()
  } catch (err) {
    formError.value = err.response?.data?.message || err.response?.data?.data?.message || 'Gagal menyimpan partner.'
  } finally {
    saving.value = false
  }
}

// ─── Delete ───────────────────────────────────────────────────────────────────
const deleteModal = ref({ show: false, partner: null, confirmText: '' })
const deleting = ref(false)

function doDelete(partner) {
  deleteModal.value = { show: true, partner, confirmText: '' }
}

function closeDeleteModal() {
  deleteModal.value.show = false
  setTimeout(() => {
    deleteModal.value.partner = null
    deleteModal.value.confirmText = ''
  }, 300)
}

async function confirmDelete() {
  if (deleteModal.value.confirmText !== deleteModal.value.partner?.name) return
  deleting.value = true
  try {
    await api.delete(`/api/v1/partners/${deleteModal.value.partner.id}`)
    toast.success('Partner berhasil dihapus!')
    fetchPartners()
    closeDeleteModal()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menghapus partner.')
  } finally {
    deleting.value = false
  }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
function getPlanBadgeVariant(plan) {
  if (plan === 'ENTERPRISE') return 'default'
  if (plan === 'PRO') return 'secondary'
  return 'outline'
}

function getPlanLabel(plan) {
  const map = { BASIC: 'Basic', PRO: 'Pro', ENTERPRISE: 'Enterprise' }
  return map[plan] || plan || '-'
}

onMounted(fetchPartners)
</script>

<template>
  <AppLayout>
    <div class="pb-6">
      <div class="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100">Manajemen Partner</h1>
          <p class="text-xs text-zinc-500 mt-0.5">Kelola data mitra bisnis & akun admin mereka.</p>
        </div>
        <Button v-if="can('partner.store')" @click="openCreate" class="bg-primary hover:bg-primary/90 flex items-center gap-2">
          <Plus class="h-4 w-4" />
          <span>Tambah Partner</span>
        </Button>
      </div>

      <div class="flex items-center justify-between gap-4 mb-5">
        <DataTableSearch v-model="searchQuery" placeholder="Cari partner..." class="w-full sm:max-w-sm" />
      </div>

      <Card>
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-20">
            <Loader2 class="h-6 w-6 animate-spin text-muted-foreground" />
          </div>

          <div v-else-if="filteredPartners.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
            <Building2 class="h-10 w-10 mb-3 opacity-20" />
            <p class="text-sm">Belum ada data partner.</p>
          </div>

          <div v-else>
            <!-- Mobile -->
            <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
              <div
                v-for="p in paginatedPartners"
                :key="'m-' + p.id"
                class="p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
              >
                <div class="flex items-start justify-between gap-3">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-lg bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-500 font-bold shrink-0 border border-zinc-200 dark:border-zinc-700">
                      {{ p.name?.charAt(0).toUpperCase() }}
                    </div>
                    <div>
                      <h4 class="font-medium text-sm text-zinc-900 dark:text-zinc-100">{{ p.name }}</h4>
                      <div class="flex items-center gap-1.5 text-[10px] text-muted-foreground mt-0.5">
                        <Globe class="h-3 w-3" />
                        <span>{{ p.slug }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="flex items-center gap-1 shrink-0">
                    <Button v-if="can('partner.update')" variant="ghost" size="icon" class="h-8 w-8" @click="openEdit(p)">
                      <Pencil class="h-3.5 w-3.5" />
                    </Button>
                    <Button v-if="can('partner.delete')" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-destructive" @click="doDelete(p)">
                      <Trash2 class="h-3.5 w-3.5" />
                    </Button>
                  </div>
                </div>
                <div class="flex items-center justify-between">
                  <Badge :variant="getPlanBadgeVariant(p.plan)" class="capitalize text-[10px] px-1.5 py-0">
                    {{ getPlanLabel(p.plan) }}
                  </Badge>
                  <div class="flex items-center gap-2">
                    <div :class="['h-2 w-2 rounded-full', p.is_active ? 'bg-emerald-500' : 'bg-zinc-300 dark:bg-zinc-700']" />
                    <span class="text-[10px]">{{ p.is_active ? 'Aktif' : 'Nonaktif' }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Desktop Table -->
            <div class="hidden md:block overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="bg-muted/40 border-b">
                    <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Nama Partner</th>
                    <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Slug</th>
                    <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Plan</th>
                    <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Status</th>
                    <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Dibuat Oleh</th>
                    <th class="px-5 py-3 text-right font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Aksi</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="p in paginatedPartners" :key="p.id" class="border-b last:border-0 hover:bg-muted/30 transition-colors">
                    <td class="px-5 py-3">
                      <div class="flex items-center gap-3">
                        <div class="w-8 h-8 rounded-lg bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-600 dark:text-zinc-300 font-bold text-sm border border-zinc-200 dark:border-zinc-700 shrink-0">
                          {{ p.name?.charAt(0).toUpperCase() }}
                        </div>
                        <span class="font-medium text-zinc-900 dark:text-zinc-100">{{ p.name }}</span>
                      </div>
                    </td>
                    <td class="px-5 py-3">
                      <div class="flex items-center gap-1.5 text-xs text-muted-foreground">
                        <Globe class="h-3 w-3" />
                        <span>{{ p.slug }}</span>
                      </div>
                    </td>
                    <td class="px-5 py-3">
                      <Badge :variant="getPlanBadgeVariant(p.plan)" class="capitalize text-[10px] px-1.5 py-0">
                        {{ getPlanLabel(p.plan) }}
                      </Badge>
                    </td>
                    <td class="px-5 py-3">
                      <div class="flex items-center gap-2">
                        <div :class="['h-2 w-2 rounded-full', p.is_active ? 'bg-emerald-500' : 'bg-zinc-300 dark:bg-zinc-700']" />
                        <span class="text-xs">{{ p.is_active ? 'Aktif' : 'Nonaktif' }}</span>
                      </div>
                    </td>
                    <td class="px-5 py-3">
                      <span class="text-xs text-muted-foreground">{{ p.created_by?.username || '-' }}</span>
                    </td>
                    <td class="px-5 py-3 text-right">
                      <div class="flex justify-end gap-1">
                        <Button v-if="can('partner.update')" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-zinc-700" @click="openEdit(p)">
                          <Pencil class="h-3.5 w-3.5" />
                        </Button>
                        <Button v-if="can('partner.delete')" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-destructive" @click="doDelete(p)">
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
            v-if="filteredPartners.length > 0 && !loading"
            :page="page"
            :page-size="pageSize"
            :total="filteredPartners.length"
            @update:page="page = $event"
            @update:page-size="pageSize = $event; page = 1"
          />
        </CardContent>
      </Card>
    </div>

    <!-- ─── DRAWER ─────────────────────────────────────────────────────────── -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showDrawer" class="fixed inset-0 z-[100] bg-black/40 backdrop-blur-sm" @click="closeDrawer" />
      </Transition>
      <Transition name="slide-right">
        <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[101] flex flex-col w-full sm:max-w-[460px] h-full bg-card shadow-2xl sm:border-l overflow-hidden">
          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
            <div>
              <h3 class="font-semibold text-base flex items-center gap-2">
                <Building2 class="h-4 w-4 text-muted-foreground" />
                {{ modalMode === 'create' ? 'Tambah Partner Baru' : 'Edit Partner' }}
              </h3>
              <p class="text-xs text-muted-foreground mt-0.5">
                {{ modalMode === 'create' ? 'Buat partner beserta akun admin yang mengelolanya.' : 'Ubah detail partner.' }}
              </p>
            </div>
            <Button variant="ghost" size="icon" @click="closeDrawer">
              <X class="h-4 w-4" />
            </Button>
          </div>

          <!-- Body -->
          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5">
            <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>

            <!-- ══ CREATE MODE ══ -->
            <template v-if="modalMode === 'create'">

              <!-- Section: Info Partner -->
              <div class="space-y-1 pb-1">
                <p class="text-xs font-semibold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                  <Building2 class="h-3.5 w-3.5" /> Informasi Partner
                </p>
                <div class="h-px bg-border" />
              </div>

              <div class="space-y-1.5">
                <Label for="c-name">Nama Partner <span class="text-destructive">*</span></Label>
                <Input id="c-name" v-model="createForm.name" placeholder="Contoh: Toko Maju Jaya" />
              </div>

              <div class="space-y-1.5">
                <Label>Slug (auto-generate)</Label>
                <div class="flex items-center h-10 px-3 rounded-md border border-dashed border-muted-foreground/40 bg-muted/30 gap-2">
                  <Globe class="h-3.5 w-3.5 text-muted-foreground shrink-0" />
                  <span class="text-sm text-muted-foreground font-mono">{{ slugPreview || 'slug-akan-muncul-di-sini' }}</span>
                </div>
                <p class="text-[10px] text-muted-foreground italic">Dihasilkan otomatis dari nama partner.</p>
              </div>

              <div class="space-y-1.5">
                <Label for="c-plan">Tipe Plan <span class="text-destructive">*</span></Label>
                <select id="c-plan" v-model="createForm.plan" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring transition-all">
                  <option value="BASIC">Basic</option>
                  <option value="PRO">Pro</option>
                  <option value="ENTERPRISE">Enterprise</option>
                </select>
              </div>

              <!-- Section: Admin User -->
              <div class="space-y-1 pb-1 pt-2">
                <p class="text-xs font-semibold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                  <UserPlus class="h-3.5 w-3.5" /> Akun Admin Partner
                </p>
                <div class="h-px bg-border" />
              </div>

              <div class="rounded-lg border border-amber-200 dark:border-amber-900/50 bg-amber-50 dark:bg-amber-950/20 p-3">
                <p class="text-xs text-amber-700 dark:text-amber-400 flex items-start gap-2">
                  <Shield class="h-3.5 w-3.5 shrink-0 mt-0.5" />
                  Akun ini akan menjadi <strong>Admin</strong> yang mengelola partner dan dapat login ke sistem.
                </p>
              </div>

              <div class="space-y-1.5">
                <Label for="c-admin-username">Username Admin <span class="text-destructive">*</span></Label>
                <Input id="c-admin-username" v-model="createForm.admin_username" placeholder="Contoh: admin_majujaya" />
              </div>

              <div class="space-y-1.5">
                <Label for="c-admin-email">Email Admin <span class="text-destructive">*</span></Label>
                <Input id="c-admin-email" v-model="createForm.admin_email" type="email" placeholder="admin@majujaya.com" />
              </div>

              <div class="space-y-1.5">
                <Label for="c-admin-password">Password Admin <span class="text-destructive">*</span></Label>
                <div class="relative">
                  <Input
                    id="c-admin-password"
                    v-model="createForm.admin_password"
                    :type="showAdminPassword ? 'text' : 'password'"
                    placeholder="Min. 6 karakter"
                    class="pr-10"
                  />
                  <button
                    type="button"
                    @click="showAdminPassword = !showAdminPassword"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                  >
                    <EyeOff v-if="showAdminPassword" class="h-4 w-4" />
                    <Eye v-else class="h-4 w-4" />
                  </button>
                </div>
              </div>
            </template>

            <!-- ══ EDIT MODE ══ -->
            <template v-else>
              <div class="space-y-1.5">
                <Label for="e-name">Nama Partner <span class="text-destructive">*</span></Label>
                <Input id="e-name" v-model="editForm.name" placeholder="Nama partner" />
              </div>

              <div class="space-y-1.5">
                <Label for="e-plan">Tipe Plan <span class="text-destructive">*</span></Label>
                <select id="e-plan" v-model="editForm.plan" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring transition-all">
                  <option value="BASIC">Basic</option>
                  <option value="PRO">Pro</option>
                  <option value="ENTERPRISE">Enterprise</option>
                </select>
              </div>

              <div class="pt-1">
                <label class="flex items-center gap-3 p-3 rounded-lg border border-border bg-muted/30 cursor-pointer hover:bg-muted/50 transition-colors">
                  <div class="relative flex items-center">
                    <input type="checkbox" v-model="editForm.is_active" class="peer h-5 w-5 opacity-0 absolute cursor-pointer" />
                    <div class="h-5 w-5 rounded border border-input bg-background peer-checked:bg-primary peer-checked:border-primary transition-all flex items-center justify-center">
                      <Check v-if="editForm.is_active" class="h-3.5 w-3.5 text-primary-foreground" />
                    </div>
                  </div>
                  <div>
                    <div class="text-sm font-medium">Partner Aktif</div>
                    <div class="text-xs text-muted-foreground">Izinkan partner mengakses sistem.</div>
                  </div>
                </label>
              </div>
            </template>
          </div>

          <!-- Footer -->
          <div class="flex justify-end gap-3 px-6 py-4 border-t bg-muted/30 shrink-0">
            <Button variant="outline" @click="closeDrawer" :disabled="saving">Batal</Button>
            <Button @click="savePartner" :disabled="saving" class="bg-primary hover:bg-primary/90">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              {{ modalMode === 'create' ? 'Buat Partner & Admin' : 'Simpan Perubahan' }}
            </Button>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ─── DELETE MODAL ───────────────────────────────────────────────────── -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[110] bg-black/40 backdrop-blur-sm" @click="closeDeleteModal" />
      </Transition>
      <Transition name="scale">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[111] flex items-center justify-center p-4 pointer-events-none">
          <div class="relative bg-card rounded-xl shadow-2xl w-full max-w-md overflow-hidden border border-border pointer-events-auto">
            <div class="p-6">
              <h3 class="text-lg font-semibold text-destructive flex items-center gap-2">
                <Trash2 class="h-5 w-5" /> Hapus Partner
              </h3>
              <p class="text-sm text-muted-foreground mt-2">
                Tindakan ini tidak dapat dibatalkan. Ini akan menghapus partner
                <span class="font-semibold text-foreground">{{ deleteModal.partner?.name }}</span> secara permanen.
              </p>
              <div class="mt-4">
                <Label class="text-sm font-medium">
                  Ketik <span class="font-bold bg-muted px-1.5 py-0.5 rounded text-foreground">{{ deleteModal.partner?.name }}</span> untuk konfirmasi.
                </Label>
                <Input
                  v-model="deleteModal.confirmText"
                  class="mt-2"
                  placeholder="Masukkan nama partner"
                  @keyup.enter="confirmDelete"
                />
              </div>
            </div>
            <div class="flex items-center justify-end gap-3 px-6 py-4 bg-muted/30 border-t">
              <Button variant="outline" @click="closeDeleteModal">Batal</Button>
              <Button
                variant="destructive"
                :disabled="deleteModal.confirmText !== deleteModal.partner?.name || deleting"
                @click="confirmDelete"
              >
                <Loader2 v-if="deleting" class="h-4 w-4 mr-2 animate-spin" />
                Hapus Sekarang
              </Button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-right-enter-active, .slide-right-leave-active { transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
.slide-right-enter-from, .slide-right-leave-to { transform: translateX(100%); }
.scale-enter-active, .scale-leave-active { transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1); }
.scale-enter-from, .scale-leave-to { opacity: 0; transform: scale(0.95); }
</style>
