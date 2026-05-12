<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Badge from '@/components/ui/Badge.vue'
import Alert from '@/components/ui/Alert.vue'
import api from '@/lib/api'
import { Plus, Pencil, Trash2, Loader2, X, Users, Search, ChevronDown, Check, Globe } from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()

// ─── State ───────────────────────────────────────────────────────────────────
const partners = ref([])
const loading = ref(false)
const error = ref(null)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

const filteredPartners = computed(() => {
  if (!searchQuery.value) return partners.value
  const q = searchQuery.value.toLowerCase()
  return partners.value.filter(p => 
    p.name.toLowerCase().includes(q) || 
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

const emptyForm = () => ({
  id: null,
  name: '',
  slug: '',
  plan: 'basic', // 'basic' | 'pro' | 'enterprise'
  is_active: true,
})

const form = ref(emptyForm())

// ─── Delete ───────────────────────────────────────────────────────────────────
const deleteModal = ref({
  show: false,
  partner: null,
  confirmText: ''
})
const deleting = ref(false)

function doDelete(partner) {
  deleteModal.value = {
    show: true,
    partner,
    confirmText: ''
  }
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

// ─── Actions ──────────────────────────────────────────────────────────────────
async function fetchPartners() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/partners')
    partners.value = res.data.data
  } catch (err) {
    error.value = 'Gagal memuat data partner.'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = emptyForm()
  formError.value = null
  modalMode.value = 'create'
  showDrawer.value = true
}

function openEdit(p) {
  form.value = { ...p }
  formError.value = null
  modalMode.value = 'edit'
  showDrawer.value = true
}

async function savePartner() {
  saving.value = true
  formError.value = null
  try {
    const payload = { ...form.value }
    if (modalMode.value === 'create') {
      await api.post('/api/v1/partners', payload)
      toast.success('Partner berhasil ditambahkan!')
    } else {
      await api.put(`/api/v1/partners/${form.value.id}`, payload)
      toast.success('Partner berhasil diperbarui!')
    }
    showDrawer.value = false
    fetchPartners()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Gagal menyimpan partner.'
  } finally {
    saving.value = false
  }
}

// Auto-generate slug from name
watch(() => form.value.name, (newName) => {
  if (modalMode.value === 'create') {
    form.value.slug = (newName || '')
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/(^-|-$)/g, '')
  }
})

function getPlanBadgeVariant(plan) {
  if (plan === 'enterprise') return 'default'
  if (plan === 'pro') return 'secondary'
  return 'outline'
}



onMounted(fetchPartners)
</script>

<template>
  <AppLayout>
    <div class="pb-6">
      <div class="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100">Manajemen Partner</h1>
          <p class="text-xs text-zinc-500 mt-0.5">Kelola data partner dan lisensi mereka.</p>
        </div>
        <Button @click="openCreate" class="bg-primary hover:bg-primary/90 flex items-center gap-2">
          <Plus class="h-4 w-4" />
          <span>Tambah Partner</span>
        </Button>
      </div>

      <div class="flex flex-col sm:flex-row items-center justify-between gap-4 mb-5">
        <DataTableSearch v-model="searchQuery" placeholder="Cari partner..." class="w-full sm:max-w-sm" />
      </div>

      <Card>
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-20">
            <Loader2 class="h-6 w-6 animate-spin text-muted-foreground" />
          </div>

          <div v-else-if="filteredPartners.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
            <Users class="h-10 w-10 mb-3 opacity-20" />
            <p class="text-sm">Belum ada data partner.</p>
          </div>

          <div v-else>
            <!-- ─── Mobile List View ─── -->
            <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
              <div
                v-for="p in paginatedPartners"
                :key="'mobile-' + p.id"
                class="p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
              >
                <div class="flex items-start justify-between gap-3">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-lg bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-500 font-bold shrink-0 border border-zinc-200 dark:border-zinc-800/50">
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
                    <Button
                      variant="ghost"
                      size="icon"
                      class="h-8 w-8 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 bg-zinc-50 dark:bg-zinc-800/50"
                      @click="openEdit(p)"
                    >
                      <Pencil class="h-3.5 w-3.5" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      class="h-8 w-8 text-zinc-400 hover:text-destructive bg-zinc-50 dark:bg-zinc-800/50"
                      @click="doDelete(p)"
                    >
                      <Trash2 class="h-3.5 w-3.5" />
                    </Button>
                  </div>
                </div>
                <div class="flex items-center justify-between mt-1">
                  <Badge :variant="getPlanBadgeVariant(p.plan)" class="capitalize text-[10px] px-1.5 py-0">
                    {{ p.plan }}
                  </Badge>
                  <div class="flex items-center gap-2">
                    <div :class="['h-2 w-2 rounded-full', p.is_active ? 'bg-emerald-500' : 'bg-zinc-300 dark:bg-zinc-700']" />
                    <span class="text-[10px]">{{ p.is_active ? 'Aktif' : 'Nonaktif' }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- ─── Desktop Table ─── -->
            <div class="hidden md:block overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="bg-muted/40 border-b">
                    <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Nama Partner</th>
                    <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Slug / Subdomain</th>
                    <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Plan</th>
                    <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Status</th>
                    <th class="px-5 py-3 text-right font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Aksi</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="p in paginatedPartners" :key="p.id" class="border-b last:border-0 hover:bg-muted/30 transition-colors">
                    <td class="px-5 py-3">
                      <div class="font-medium text-zinc-900 dark:text-zinc-100">{{ p.name }}</div>
                    </td>
                    <td class="px-5 py-3">
                      <div class="flex items-center gap-1.5 text-xs text-muted-foreground">
                        <Globe class="h-3 w-3" />
                        <span>{{ p.slug }}</span>
                      </div>
                    </td>
                    <td class="px-5 py-3">
                      <Badge :variant="getPlanBadgeVariant(p.plan)" class="capitalize text-[10px] px-1.5 py-0">
                        {{ p.plan }}
                      </Badge>
                    </td>
                    <td class="px-5 py-3">
                      <div class="flex items-center gap-2">
                        <div :class="['h-2 w-2 rounded-full', p.is_active ? 'bg-emerald-500' : 'bg-zinc-300 dark:bg-zinc-700']" />
                        <span class="text-xs">{{ p.is_active ? 'Aktif' : 'Nonaktif' }}</span>
                      </div>
                    </td>
                    <td class="px-5 py-3 text-right">
                      <div class="flex justify-end gap-1">
                        <Button variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-zinc-700" @click="openEdit(p)">
                          <Pencil class="h-3.5 w-3.5" />
                        </Button>
                        <Button variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-destructive" @click="doDelete(p)">
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

    <!-- Drawer -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showDrawer" class="fixed inset-0 z-[100] bg-black/40 backdrop-blur-sm" @click="showDrawer = false" />
      </Transition>

      <Transition name="slide-right">
        <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[101] flex flex-col w-full sm:max-w-[420px] h-full bg-card shadow-2xl sm:border-l overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
            <div>
              <h3 class="font-semibold text-base">{{ modalMode === 'create' ? 'Tambah Partner' : 'Edit Partner' }}</h3>
              <p class="text-xs text-muted-foreground mt-0.5">Kelola detail partner dan lisensi.</p>
            </div>
            <Button variant="ghost" size="icon" @click="showDrawer = false">
              <X class="h-4 w-4" />
            </Button>
          </div>

          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-4">
            <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>

            <div class="space-y-1.5">
              <Label for="name">Nama Partner <span class="text-destructive">*</span></Label>
              <Input id="name" v-model="form.name" placeholder="Contoh: Gaptek Solution" />
            </div>

            <div class="space-y-1.5">
              <Label for="slug">Slug / Subdomain <span class="text-destructive">*</span></Label>
              <div class="relative">
                <Globe class="absolute left-3 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-muted-foreground" />
                <Input id="slug" v-model="form.slug" class="pl-9" placeholder="partner-slug" />
              </div>
              <p class="text-[10px] text-muted-foreground italic">Digunakan sebagai identitas URL unik.</p>
            </div>

            <div class="space-y-1.5">
              <Label for="plan">Tipe Plan <span class="text-destructive">*</span></Label>
              <select id="plan" v-model="form.plan" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring transition-all">
                <option value="free">Basic</option>
                <option value="pro">Pro</option>
                <option value="enterprise">Enterprise</option>
              </select>
            </div>

            <div class="pt-2">
              <label class="flex items-center gap-3 p-3 rounded-lg border border-border bg-muted/30 cursor-pointer hover:bg-muted/50 transition-colors">
                <div class="relative flex items-center">
                  <input type="checkbox" v-model="form.is_active" class="peer h-5 w-5 opacity-0 absolute cursor-pointer" />
                  <div class="h-5 w-5 rounded border border-input bg-background peer-checked:bg-primary peer-checked:border-primary transition-all flex items-center justify-center">
                    <Check v-if="form.is_active" class="h-3.5 w-3.5 text-primary-foreground" />
                  </div>
                </div>
                <div>
                  <div class="text-sm font-medium">Partner Aktif</div>
                  <div class="text-xs text-muted-foreground">Izinkan partner untuk mengakses sistem.</div>
                </div>
              </label>
            </div>
          </div>

          <div class="flex justify-end gap-3 px-6 py-4 border-t bg-muted/30">
            <Button variant="outline" @click="showDrawer = false" :disabled="saving">Batal</Button>
            <Button @click="savePartner" :disabled="saving">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              Simpan Partner
            </Button>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Delete Confirmation Modal -->
    <Teleport to="body">
      <!-- Backdrop -->
      <Transition name="fade">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[110] bg-black/40 backdrop-blur-sm" @click="closeDeleteModal"></div>
      </Transition>
      
      <!-- Modal Content -->
      <Transition name="scale">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[111] flex items-center justify-center p-4 pointer-events-none">
          <div class="relative bg-card rounded-xl shadow-2xl w-full max-w-md overflow-hidden border border-border pointer-events-auto">
            <div class="p-6">
              <h3 class="text-lg font-semibold text-destructive flex items-center gap-2">
                <Trash2 class="h-5 w-5" />
                Hapus Partner
              </h3>
              <p class="text-sm text-muted-foreground mt-2">
                Tindakan ini tidak dapat dibatalkan. Hal ini akan menghapus partner 
                <span class="font-semibold text-foreground">{{ deleteModal.partner?.name }}</span> secara permanen.
              </p>
              
              <div class="mt-4">
                <Label class="text-sm font-medium">
                  Ketik <span class="font-bold select-all bg-muted px-1.5 py-0.5 rounded text-foreground">{{ deleteModal.partner?.name }}</span> untuk mengonfirmasi.
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

.scale-enter-active, .scale-leave-active {
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.scale-enter-from, .scale-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
