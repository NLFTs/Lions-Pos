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
import Badge from '@/components/ui/Badge.vue'
import Alert from '@/components/ui/Alert.vue'
import api from '@/lib/api'
import { Plus, Pencil, Trash2, Loader2, X, Ticket, Search, Calendar, Users } from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()

// ─── State ───────────────────────────────────────────────────────────────────
const vouchers = ref([])
const partners = ref([])
const loading = ref(false)
const error = ref(null)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

const filteredVouchers = computed(() => {
  if (!searchQuery.value) return vouchers.value
  const q = searchQuery.value.toLowerCase()
  return vouchers.value.filter(v => 
    v.name.toLowerCase().includes(q) || 
    v.code?.toLowerCase().includes(q)
  )
})

const paginatedVouchers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredVouchers.value.slice(start, start + pageSize.value)
})

// ─── Form State ───────────────────────────────────────────────────────────────
const showDrawer = ref(false)
const modalMode = ref('create')
const saving = ref(false)
const formError = ref(null)

const emptyForm = () => ({
  id: null,
  partner_id: '',
  code: '',
  name: '',
  discount_type: 'percent',
  discount_value: 0,
  min_purchase: 0,
  max_discount: null,
  quota: null,
  valid_from: '',
  valid_until: '',
  is_active: true,
})

const form = ref(emptyForm())

// ─── Delete ───────────────────────────────────────────────────────────────────
const deleteModal = ref({
  show: false,
  voucher: null,
  confirmText: ''
})
const deleting = ref(false)

function doDelete(voucher) {
  deleteModal.value = {
    show: true,
    voucher,
    confirmText: ''
  }
}

function closeDeleteModal() {
  deleteModal.value.show = false
  setTimeout(() => {
    deleteModal.value.voucher = null
    deleteModal.value.confirmText = ''
  }, 300)
}

async function confirmDelete() {
  if (deleteModal.value.confirmText !== deleteModal.value.voucher?.code) return

  deleting.value = true
  try {
    await api.delete(`/api/v1/vouchers/${deleteModal.value.voucher.id}`)
    toast.success('Voucer berhasil dihapus!')
    fetchVouchers()
    closeDeleteModal()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menghapus voucer.')
  } finally {
    deleting.value = false
  }
}

// ─── Actions ──────────────────────────────────────────────────────────────────
async function fetchVouchers() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/vouchers')
    vouchers.value = res.data.data
  } catch (err) {
    if (import.meta.env.DEV) {
      vouchers.value = [
        { 
          id: '1', 
          partner_id: '1', 
          code: 'DISCOUNT10', 
          name: 'Promo Merdeka', 
          discount_type: 'percent', 
          discount_value: 10, 
          min_purchase: 50000, 
          max_discount: 10000, 
          quota: 100, 
          used_count: 5, 
          valid_from: '2026-05-01', 
          valid_until: '2026-05-31', 
          is_active: true 
        },
        { 
          id: '2', 
          partner_id: '2', 
          code: 'FIXED50K', 
          name: 'Voucher Belanja', 
          discount_type: 'fixed', 
          discount_value: 50000, 
          min_purchase: 200000, 
          max_discount: null, 
          quota: 50, 
          used_count: 50, 
          valid_from: '2026-04-01', 
          valid_until: '2026-04-30', 
          is_active: false 
        },
      ]
    } else {
      error.value = 'Gagal memuat data voucer.'
    }
  } finally {
    loading.value = false
  }
}

async function fetchPartners() {
  try {
    const res = await api.get('/api/v1/partners')
    partners.value = res.data.data
  } catch (err) {
    if (import.meta.env.DEV) {
      partners.value = [
        { id: '1', name: 'Supplier Utama' },
        { id: '2', name: 'Customer Retail' },
      ]
    }
  }
}

function openCreate() {
  form.value = emptyForm()
  formError.value = null
  modalMode.value = 'create'
  showDrawer.value = true
}

function openEdit(v) {
  form.value = { ...v }
  // Format dates for input type="date"
  if (form.value.valid_from) form.value.valid_from = form.value.valid_from.split('T')[0]
  if (form.value.valid_until) form.value.valid_until = form.value.valid_until.split('T')[0]
  formError.value = null
  modalMode.value = 'edit'
  showDrawer.value = true
}

async function saveVoucher() {
  saving.value = true
  formError.value = null
  try {
    if (modalMode.value === 'create') {
      await api.post('/api/v1/vouchers', form.value)
      toast.success('Voucer berhasil ditambahkan!')
    } else {
      await api.put(`/api/v1/vouchers/${form.value.id}`, form.value)
      toast.success('Voucer berhasil diperbarui!')
    }
    showDrawer.value = false
    fetchVouchers()
  } catch (err) {
    formError.value = err.response?.data?.message || 'Gagal menyimpan voucer.'
  } finally {
    saving.value = false
  }
}

function formatCurrency(val) {
  if (val === null || val === undefined) return '-'
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(val)
}

function getPartnerName(id) {
  return partners.value.find(p => p.id === id)?.name || 'Unknown Partner'
}

onMounted(() => {
  fetchVouchers()
  fetchPartners()
})
</script>

<template>
  <AppLayout>
    <div class="pb-6">
      <div class="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100">Manajemen Voucer</h1>
          <p class="text-xs text-zinc-500 mt-0.5">Kelola kode promo dan diskon belanja.</p>
        </div>
        <Button @click="openCreate" class="bg-primary hover:bg-primary/90 flex items-center gap-2">
          <Plus class="h-4 w-4" />
          <span>Tambah Voucer</span>
        </Button>
      </div>

      <div class="flex flex-col sm:flex-row items-center justify-between gap-4 mb-5">
        <DataTableSearch v-model="searchQuery" placeholder="Cari kode atau nama voucer..." class="w-full sm:max-w-sm" />
      </div>

      <Card>
        <CardContent class="p-0">
          <div v-if="loading" class="flex items-center justify-center py-20">
            <Loader2 class="h-6 w-6 animate-spin text-muted-foreground" />
          </div>

          <div v-else-if="filteredVouchers.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
            <Ticket class="h-10 w-10 mb-3 opacity-20" />
            <p class="text-sm">Belum ada data voucer.</p>
          </div>

          <div v-else class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="bg-muted/40 border-b">
                  <th class="px-4 py-3 text-left font-medium text-muted-foreground">Voucer</th>
                  <th class="px-4 py-3 text-left font-medium text-muted-foreground">Diskon</th>
                  <th class="px-4 py-3 text-left font-medium text-muted-foreground">Kuota / Terpakai</th>
                  <th class="px-4 py-3 text-left font-medium text-muted-foreground">Masa Berlaku</th>
                  <th class="px-4 py-3 text-left font-medium text-muted-foreground">Status</th>
                  <th class="px-4 py-3 text-right font-medium text-muted-foreground">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="v in paginatedVouchers" :key="v.id" class="border-b last:border-0 hover:bg-muted/30 transition-colors">
                  <td class="px-4 py-3">
                    <div class="flex flex-col">
                      <span class="font-mono text-xs font-bold text-primary">{{ v.code }}</span>
                      <span class="font-medium">{{ v.name }}</span>
                      <span class="text-[10px] text-muted-foreground">{{ getPartnerName(v.partner_id) }}</span>
                    </div>
                  </td>
                  <td class="px-4 py-3">
                    <div class="flex flex-col">
                      <span class="font-semibold">
                        {{ v.discount_type === 'percent' ? v.discount_value + '%' : formatCurrency(v.discount_value) }}
                      </span>
                      <span v-if="v.min_purchase > 0" class="text-[10px] text-muted-foreground">
                        Min. {{ formatCurrency(v.min_purchase) }}
                      </span>
                    </div>
                  </td>
                  <td class="px-4 py-3">
                    <div class="flex flex-col">
                      <span>{{ v.quota || '∞' }} / {{ v.used_count || 0 }}</span>
                      <div class="w-24 h-1.5 bg-muted rounded-full mt-1 overflow-hidden">
                        <div 
                          class="h-full bg-primary" 
                          :style="{ width: v.quota ? Math.min((v.used_count / v.quota) * 100, 100) + '%' : '0%' }"
                        />
                      </div>
                    </div>
                  </td>
                  <td class="px-4 py-3 text-xs">
                    <div class="flex flex-col gap-0.5">
                      <div class="flex items-center gap-1">
                        <Calendar class="h-3 w-3 text-muted-foreground" />
                        <span>{{ v.valid_from || '-' }}</span>
                      </div>
                      <div class="flex items-center gap-1">
                        <Calendar class="h-3 w-3 text-muted-foreground" />
                        <span>{{ v.valid_until || '-' }}</span>
                      </div>
                    </div>
                  </td>
                  <td class="px-4 py-3">
                    <Badge :variant="v.is_active ? 'default' : 'secondary'">
                      {{ v.is_active ? 'Aktif' : 'Nonaktif' }}
                    </Badge>
                  </td>
                  <td class="px-4 py-3 text-right">
                    <div class="flex justify-end gap-2">
                      <Button variant="ghost" size="icon" @click="openEdit(v)">
                        <Pencil class="h-4 w-4" />
                      </Button>
                      <Button variant="ghost" size="icon" class="text-destructive" @click="doDelete(v)">
                        <Trash2 class="h-4 w-4" />
                      </Button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <DataTablePagination
            v-if="filteredVouchers.length > 0 && !loading"
            :page="page"
            :page-size="pageSize"
            :total="filteredVouchers.length"
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
        <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[101] flex flex-col w-full sm:max-w-[480px] h-full bg-card shadow-2xl sm:border-l overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
            <div>
              <h3 class="font-semibold text-base">{{ modalMode === 'create' ? 'Tambah Voucer' : 'Edit Voucer' }}</h3>
              <p class="text-xs text-muted-foreground mt-0.5">Atur kode promo dan detail diskon.</p>
            </div>
            <Button variant="ghost" size="icon" @click="showDrawer = false">
              <X class="h-4 w-4" />
            </Button>
          </div>

          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-4 custom-scrollbar">
            <Alert v-if="formError" variant="destructive">{{ formError }}</Alert>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <Label for="code">Kode Voucer <span class="text-destructive">*</span></Label>
                <Input id="code" v-model="form.code" placeholder="CONTOH: PROMO10" class="font-mono uppercase" />
              </div>
              <div class="space-y-1.5">
                <Label for="partner">Partner <span class="text-destructive">*</span></Label>
                <select id="partner" v-model="form.partner_id" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm">
                  <option value="" disabled>Pilih Partner</option>
                  <option v-for="p in partners" :key="p.id" :value="p.id">{{ p.name }}</option>
                </select>
              </div>
            </div>

            <div class="space-y-1.5">
              <Label for="name">Nama Voucer <span class="text-destructive">*</span></Label>
              <Input id="name" v-model="form.name" placeholder="Nama promo..." />
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <Label for="discount_type">Tipe Diskon</Label>
                <select id="discount_type" v-model="form.discount_type" class="w-full h-10 rounded-md border border-input bg-background px-3 text-sm">
                  <option value="percent">Persentase (%)</option>
                  <option value="fixed">Nominal Tetap (Rp)</option>
                </select>
              </div>
              <div class="space-y-1.5">
                <Label for="discount_value">Nilai Diskon <span class="text-destructive">*</span></Label>
                <Input id="discount_value" type="number" v-model="form.discount_value" />
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <Label for="min_purchase">Min. Pembelian</Label>
                <Input id="min_purchase" type="number" v-model="form.min_purchase" />
              </div>
              <div class="space-y-1.5">
                <Label for="max_discount">Maks. Diskon (Cap)</Label>
                <Input id="max_discount" type="number" v-model="form.max_discount" placeholder="Kosongkan jika tidak ada cap" />
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <Label for="quota">Kuota</Label>
                <Input id="quota" type="number" v-model="form.quota" placeholder="Kosongkan jika tidak terbatas" />
              </div>
              <div class="space-y-1.5 flex flex-col justify-end">
                <div class="flex items-center space-x-2 h-10">
                  <input type="checkbox" id="is_active" v-model="form.is_active" class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary" />
                  <Label for="is_active" class="cursor-pointer">Status Aktif</Label>
                </div>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4 pt-2">
              <div class="space-y-1.5">
                <Label for="valid_from">Berlaku Dari</Label>
                <Input id="valid_from" type="date" v-model="form.valid_from" />
              </div>
              <div class="space-y-1.5">
                <Label for="valid_until">Berlaku Sampai</Label>
                <Input id="valid_until" type="date" v-model="form.valid_until" />
              </div>
            </div>
          </div>

          <div class="flex justify-end gap-3 px-6 py-4 border-t bg-muted/30">
            <Button variant="outline" @click="showDrawer = false" :disabled="saving">Batal</Button>
            <Button @click="saveVoucher" :disabled="saving">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              Simpan Voucer
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
                Hapus Voucer
              </h3>
              <p class="text-sm text-muted-foreground mt-2">
                Tindakan ini tidak dapat dibatalkan. Hal ini akan menghapus voucer 
                <span class="font-semibold text-foreground font-mono">{{ deleteModal.voucher?.code }}</span> secara permanen.
              </p>
              
              <div class="mt-4">
                <Label class="text-sm font-medium">
                  Ketik <span class="font-bold select-all bg-muted px-1.5 py-0.5 rounded text-foreground">{{ deleteModal.voucher?.code }}</span> untuk mengonfirmasi.
                </Label>
                <Input
                  v-model="deleteModal.confirmText"
                  class="mt-2"
                  placeholder="Masukkan kode voucer"
                  @keyup.enter="confirmDelete"
                />
              </div>
            </div>
            <div class="flex items-center justify-end gap-3 px-6 py-4 bg-muted/30 border-t">
              <Button variant="outline" @click="closeDeleteModal">Batal</Button>
              <Button
                variant="destructive"
                :disabled="deleteModal.confirmText !== deleteModal.voucher?.code || deleting"
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

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: hsl(var(--muted-foreground) / 0.15);
  border-radius: 99px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: hsl(var(--muted-foreground) / 0.25);
}
</style>
