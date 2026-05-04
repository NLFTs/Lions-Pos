<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePermission } from '@/composables/usePermission'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import Alert from '@/components/ui/Alert.vue'
import api from '@/lib/api'
import { Plus, Pencil, Trash2, Loader2, X, Users, Search, ChevronDown } from 'lucide-vue-next'
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
    p.code?.toLowerCase().includes(q) ||
    p.type?.toLowerCase().includes(q)
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
  code: '',
  name: '',
  type: 'supplier', // 'supplier' | 'customer' | 'both'
  phone: '',
  email: '',
  address: '',
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
    if (import.meta.env.DEV) {
      partners.value = [
        { id: '1', code: 'SUP-001', name: 'Supplier Utama', type: 'supplier', phone: '0812345678', email: 'sup@mail.com' },
        { id: '2', code: 'CUS-001', name: 'Customer Retail', type: 'customer', phone: '0812999999', email: 'cus@mail.com' },
      ]
    } else {
      error.value = 'Gagal memuat data partner.'
    }
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
    if (modalMode.value === 'create') {
      await api.post('/api/v1/partners', form.value)
      toast.success('Partner berhasil ditambahkan!')
    } else {
      await api.put(`/api/v1/partners/${form.value.id}`, form.value)
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



onMounted(fetchPartners)
</script>

<template>
  <AppLayout>
    <div class="pb-6">
      <div class="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100">Manajemen Partner</h1>
          <p class="text-xs text-zinc-500 mt-0.5">Kelola data supplier dan customer.</p>
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

          <div v-else class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="bg-muted/40 border-b">
                  <th class="px-4 py-3 text-left font-medium text-muted-foreground">Kode</th>
                  <th class="px-4 py-3 text-left font-medium text-muted-foreground">Nama</th>
                  <th class="px-4 py-3 text-left font-medium text-muted-foreground">Tipe</th>
                  <th class="px-4 py-3 text-left font-medium text-muted-foreground">Kontak</th>
                  <th class="px-4 py-3 text-right font-medium text-muted-foreground">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in paginatedPartners" :key="p.id" class="border-b last:border-0 hover:bg-muted/30 transition-colors">
                  <td class="px-4 py-3 font-mono text-xs">{{ p.code }}</td>
                  <td class="px-4 py-3 font-medium">{{ p.name }}</td>
                  <td class="px-4 py-3 capitalize text-xs">
                    <Badge variant="outline">{{ p.type }}</Badge>
                  </td>
                  <td class="px-4 py-3 text-xs text-muted-foreground">
                    <div>{{ p.phone }}</div>
                    <div>{{ p.email }}</div>
                  </td>
                  <td class="px-4 py-3 text-right">
                    <div class="flex justify-end gap-2">
                      <Button variant="ghost" size="icon" @click="openEdit(p)">
                        <Pencil class="h-4 w-4" />
                      </Button>
                      <Button variant="ghost" size="icon" class="text-destructive" @click="doDelete(p)">
                        <Trash2 class="h-4 w-4" />
                      </Button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
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
              <p class="text-xs text-muted-foreground mt-0.5">Kelola data supplier/customer.</p>
            </div>
            <Button variant="ghost" size="icon" @click="showDrawer = false">
              <X class="h-4 w-4" />
            </Button>
          </div>

          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-4">
            <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>

            <div class="space-y-1.5">
              <Label for="name">Nama Partner <span class="text-destructive">*</span></Label>
              <Input id="name" v-model="form.name" placeholder="Nama supplier/customer..." />
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <Label for="code">Kode</Label>
                <Input id="code" v-model="form.code" placeholder="SUP-001" />
              </div>
              <div class="space-y-1.5">
                <Label for="type">Tipe</Label>
                <select id="type" v-model="form.type" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm">
                  <option value="supplier">Supplier</option>
                  <option value="customer">Customer</option>
                  <option value="both">Both</option>
                </select>
              </div>
            </div>

            <div class="space-y-1.5">
              <Label for="phone">No. Telepon</Label>
              <Input id="phone" v-model="form.phone" placeholder="081..." />
            </div>

            <div class="space-y-1.5">
              <Label for="email">Email</Label>
              <Input id="email" type="email" v-model="form.email" placeholder="partner@mail.com" />
            </div>

            <div class="space-y-1.5">
              <Label for="address">Alamat</Label>
              <textarea id="address" v-model="form.address" rows="3" class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring" placeholder="Alamat lengkap..." />
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
