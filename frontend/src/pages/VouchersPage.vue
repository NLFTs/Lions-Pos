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
import { Plus, Pencil, Trash2, Loader2, Ticket, Calendar, ArrowLeft, Tag, Settings2 } from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()

const selectedIds = ref([])
const vouchers = ref([])
const loading = ref(false)
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)
const showForm = ref(false)
const modalMode = ref('create')
const saving = ref(false)
const formError = ref(null)
const deleting = ref(false)
const deleteModal = ref({ show: false, voucher: null, confirmText: '' })

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

const isAllSelected = computed(() => {
  const visible = paginatedVouchers.value
  if (visible.length === 0) return false
  return visible.every(v => selectedIds.value.includes(v.id))
})

watch([searchQuery, page, pageSize], () => { selectedIds.value = [] })

const emptyForm = () => ({
  id: null, code: '', name: '', discountType: 'percent',
  discountValue: 0, minPurchase: 0, maxDiscount: null,
  quota: null, validFrom: '', validUntil: '', isActive: true,
})
const form = ref(emptyForm())

function toggleSelectAll() {
  const visible = paginatedVouchers.value
  if (isAllSelected.value) {
    const ids = visible.map(v => v.id)
    selectedIds.value = selectedIds.value.filter(id => !ids.includes(id))
  } else {
    visible.forEach(v => { if (!selectedIds.value.includes(v.id)) selectedIds.value.push(v.id) })
  }
}

function toggleSelect(id) {
  const i = selectedIds.value.indexOf(id)
  if (i === -1) selectedIds.value.push(id)
  else selectedIds.value.splice(i, 1)
}

async function bulkDelete() {
  const count = selectedIds.value.length
  if (!count) return
  const ok = await confirm({ title: 'Hapus Voucer Terpilih', description: `Hapus ${count} voucer terpilih secara permanen?`, confirmLabel: 'Hapus', cancelLabel: 'Batal' })
  if (!ok) return
  loading.value = true
  try {
    await Promise.all(selectedIds.value.map(id => api.delete(`/api/v1/vouchers/${id}`)))
    toast.success(`${count} voucer berhasil dihapus!`)
    selectedIds.value = []
    fetchVouchers()
  } catch (err) {
    toast.error(err.response?.data?.data?.message || err.response?.data?.message || 'Gagal menghapus.')
    fetchVouchers()
  } finally { loading.value = false }
}

async function fetchVouchers() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/vouchers')
    vouchers.value = Array.isArray(res.data) ? res.data : (res.data?.data || [])
  } catch (err) {
    toast.error('Gagal memuat data voucer.')
  } finally { loading.value = false }
}

function openCreate() {
  form.value = emptyForm()
  formError.value = null
  modalMode.value = 'create'
  showForm.value = true
}

function openEdit(v) {
  form.value = {
    ...v,
    discountType: v.discountType || v.discount_type || 'percent',
    discountValue: v.discountValue ?? v.discount_value ?? 0,
    minPurchase: v.minPurchase ?? v.min_purchase ?? 0,
    maxDiscount: v.maxDiscount ?? v.max_discount ?? null,
    isActive: v.isActive ?? v.is_active ?? true,
    validFrom: (v.validFrom || v.valid_from || '').split('T')[0],
    validUntil: (v.validUntil || v.valid_until || '').split('T')[0],
  }
  formError.value = null
  modalMode.value = 'edit'
  showForm.value = true
}

async function saveVoucher() {
  formError.value = null
  if (!form.value.code?.trim()) { formError.value = 'Kode voucer wajib diisi.'; return }
  if (!form.value.name?.trim()) { formError.value = 'Nama voucer wajib diisi.'; return }
  if (!form.value.discountValue || Number(form.value.discountValue) <= 0) { formError.value = 'Nilai diskon harus lebih dari 0.'; return }
  if (modalMode.value === 'create') {
    const exists = vouchers.value.some(v => v.code?.toUpperCase() === form.value.code.trim().toUpperCase())
    if (exists) { formError.value = `Kode "${form.value.code.toUpperCase()}" sudah digunakan.`; return }
  }
  saving.value = true
  try {
    const payload = {
      code: form.value.code,
      name: form.value.name,
      discount_type: form.value.discountType?.toUpperCase(),
      discount_value: form.value.discountValue,
      min_purchase: form.value.minPurchase,
      max_discount: form.value.maxDiscount,
      quota: form.value.quota,
      is_active: form.value.isActive,
      valid_from: form.value.validFrom ? form.value.validFrom + 'T00:00:00' : null,
      valid_until: form.value.validUntil ? form.value.validUntil + 'T23:59:59' : null,
    }
    if (modalMode.value === 'create') {
      await api.post('/api/v1/vouchers', payload)
      toast.success('Voucer berhasil ditambahkan!')
    } else {
      await api.put(`/api/v1/vouchers/${form.value.id}`, payload)
      toast.success('Voucer berhasil diperbarui!')
    }
    showForm.value = false
    fetchVouchers()
  } catch (err) {
    formError.value = err.response?.data?.data?.message || err.response?.data?.message || 'Gagal menyimpan voucer.'
  } finally { saving.value = false }
}

function doDelete(v) { deleteModal.value = { show: true, voucher: v, confirmText: '' } }
function closeDeleteModal() {
  deleteModal.value.show = false
  setTimeout(() => { deleteModal.value.voucher = null; deleteModal.value.confirmText = '' }, 300)
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
    toast.error(err.response?.data?.data?.message || err.response?.data?.message || 'Gagal menghapus voucer.')
    closeDeleteModal()
  } finally { deleting.value = false }
}

function formatCurrency(val) {
  if (val === null || val === undefined) return '-'
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(val)
}
function discountDisplay(v) {
  const type = (v.discountType || v.discount_type || '').toUpperCase()
  const val = v.discountValue ?? v.discount_value ?? 0
  return type === 'PERCENT' ? `${val}%` : formatCurrency(val)
}
function getValidUntil(v) { const r = v.validUntil || v.valid_until || null; return r ? r.split('T')[0] : null }
function getValidFrom(v) { const r = v.validFrom || v.valid_from || null; return r ? r.split('T')[0] : null }
function getMinPurchase(v) { return v.minPurchase ?? v.min_purchase ?? 0 }
function getQuota(v) { return v.quota ?? null }
function getUsedCount(v) { return v.usedCount ?? v.used_count ?? 0 }
function getIsActive(v) { return v.isActive ?? v.is_active ?? true }

onMounted(fetchVouchers)
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <Transition name="fade" mode="out-in">

        <!-- ─── Table View ─── -->
        <div v-if="!showForm" key="table-view" class="flex flex-col gap-6">
          <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
            <div>
              <h1 class="text-2xl font-bold tracking-tight">Voucer</h1>
              <p class="text-muted-foreground text-sm mt-1">Kelola kode promo dan diskon belanja.</p>
            </div>
            <div class="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto">
              <div class="w-full sm:w-72">
                <DataTableSearch v-model="searchQuery" placeholder="Cari kode atau nama voucer..." />
              </div>
              <Button v-if="can('voucher.store')" @click="openCreate" size="sm" class="flex-1 sm:flex-none flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
                <Plus class="h-4 w-4" /><span>Tambah Voucer</span>
              </Button>
            </div>
          </div>

          <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
            <CardContent class="p-0">
              <div v-if="loading" class="flex flex-col items-center justify-center py-24 gap-3">
                <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
                <p class="text-xs text-muted-foreground">Memuat data...</p>
              </div>

              <div v-else-if="filteredVouchers.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
                <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
                  <Ticket class="h-7 w-7 opacity-40" />
                </div>
                <p class="text-sm font-medium">Belum ada data voucer</p>
                <Button v-if="can('voucher.store') && !searchQuery" size="sm" class="mt-4" @click="openCreate">
                  <Plus class="h-3.5 w-3.5 mr-1.5" />Buat Voucer
                </Button>
              </div>

              <div v-else>
                <!-- Mobile -->
                <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
                  <div v-for="v in paginatedVouchers" :key="'m-' + v.id" class="p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors">
                    <div class="flex items-start justify-between gap-3">
                      <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center text-primary shrink-0 border border-primary/20">
                          <Ticket class="h-5 w-5" />
                        </div>
                        <div>
                          <span class="font-mono text-xs font-bold text-primary block tracking-wider">{{ v.code }}</span>
                          <h4 class="font-medium text-sm text-zinc-900 dark:text-zinc-100 mt-0.5">{{ v.name }}</h4>
                        </div>
                      </div>
                      <div class="flex items-center gap-1 shrink-0">
                        <Button variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-zinc-700 bg-zinc-50 dark:bg-zinc-800/50" @click="openEdit(v)">
                          <Pencil class="h-3.5 w-3.5" />
                        </Button>
                        <Button v-if="can('voucher.delete')" variant="ghost" size="icon" class="h-8 w-8 text-zinc-400 hover:text-destructive bg-zinc-50 dark:bg-zinc-800/50" @click="doDelete(v)">
                          <Trash2 class="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </div>
                    <div class="grid grid-cols-2 gap-y-1 gap-x-4 text-xs text-zinc-600 dark:text-zinc-400">
                      <div><span class="font-bold">Diskon:</span> {{ discountDisplay(v) }}</div>
                      <div><span class="font-bold">Kuota:</span> {{ getQuota(v) || '∞' }} / {{ getUsedCount(v) }}</div>
                      <div class="col-span-2"><span class="font-bold">Berlaku:</span> {{ getValidFrom(v) || '-' }} s.d {{ getValidUntil(v) || 'Selamanya' }}</div>
                    </div>
                    <div class="flex justify-end">
                      <Badge :variant="getIsActive(v) ? 'default' : 'secondary'" class="text-[9px]">{{ getIsActive(v) ? 'Aktif' : 'Nonaktif' }}</Badge>
                    </div>
                  </div>
                </div>

                <!-- Desktop -->
                <div class="hidden md:block overflow-x-auto">
                  <Transition name="fade">
                    <div v-if="selectedIds.length > 0" class="flex items-center justify-between px-5 py-3 bg-primary/5 dark:bg-primary/10 border-b border-border">
                      <span class="text-xs font-semibold text-primary px-2 py-0.5 rounded bg-primary/10">{{ selectedIds.length }} Terpilih</span>
                      <Button v-if="can('voucher.delete')" size="sm" variant="destructive" class="h-8 text-xs gap-1" @click="bulkDelete">
                        <Trash2 class="h-3.5 w-3.5" />Hapus
                      </Button>
                    </div>
                  </Transition>
                  <table class="w-full text-sm">
                    <thead>
                      <tr class="bg-muted/40 border-b border-zinc-100 dark:border-zinc-800">
                        <th class="w-12 pl-5 py-3 text-left">
                          <input type="checkbox" class="rounded border-zinc-300 dark:border-zinc-700 text-primary h-4 w-4 cursor-pointer" :checked="isAllSelected" @change="toggleSelectAll" />
                        </th>
                        <th class="pl-2 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Voucer</th>
                        <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Diskon</th>
                        <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Kuota / Terpakai</th>
                        <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Masa Berlaku</th>
                        <th class="px-5 py-3 text-left font-semibold text-muted-foreground uppercase tracking-wider text-[11px]">Status</th>
                        <th class="px-5 py-3 text-right"></th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr
                        v-for="v in paginatedVouchers" :key="v.id"
                        class="group border-b last:border-0 border-zinc-100 dark:border-zinc-800/60 odd:bg-background even:bg-zinc-50/40 dark:even:bg-zinc-900/10 hover:bg-zinc-100/60 dark:hover:bg-zinc-800/40 transition-colors cursor-pointer"
                        @click="can('voucher.update') && openEdit(v)"
                      >
                        <td class="w-12 pl-5 py-3" @click.stop>
                          <input type="checkbox" class="rounded border-zinc-300 dark:border-zinc-700 text-primary h-4 w-4 cursor-pointer" :checked="selectedIds.includes(v.id)" @change="toggleSelect(v.id)" />
                        </td>
                        <td class="pl-2 py-3">
                          <span class="font-mono text-xs font-bold text-primary block">{{ v.code }}</span>
                          <span class="font-medium text-zinc-900 dark:text-zinc-100 text-sm">{{ v.name }}</span>
                        </td>
                        <td class="px-5 py-3">
                          <span class="font-semibold text-zinc-800 dark:text-zinc-200 block">{{ discountDisplay(v) }}</span>
                          <span v-if="getMinPurchase(v) > 0" class="text-[10px] text-muted-foreground">Min. {{ formatCurrency(getMinPurchase(v)) }}</span>
                        </td>
                        <td class="px-5 py-3">
                          <span class="text-xs block">{{ getQuota(v) || '∞' }} / {{ getUsedCount(v) }}</span>
                          <div class="w-24 h-1 bg-muted rounded-full mt-1.5 overflow-hidden">
                            <div class="h-full bg-primary" :style="{ width: getQuota(v) ? Math.min((getUsedCount(v) / getQuota(v)) * 100, 100) + '%' : '0%' }" />
                          </div>
                        </td>
                        <td class="px-5 py-3 text-xs">
                          <div class="flex items-center gap-1 text-muted-foreground"><Calendar class="h-3 w-3" />{{ getValidFrom(v) || '-' }}</div>
                          <div class="flex items-center gap-1 text-muted-foreground mt-0.5"><Calendar class="h-3 w-3" />{{ getValidUntil(v) || '-' }}</div>
                        </td>
                        <td class="px-5 py-3">
                          <Badge :variant="getIsActive(v) ? 'default' : 'secondary'" class="text-[10px]">{{ getIsActive(v) ? 'Aktif' : 'Nonaktif' }}</Badge>
                        </td>
                        <td class="px-5 py-3 text-right" @click.stop>
                          <Button v-if="can('voucher.delete')" variant="ghost" size="icon" class="h-7 w-7 text-zinc-400 hover:text-destructive hover:bg-destructive/10 opacity-0 group-hover:opacity-100 transition-opacity" @click="doDelete(v)">
                            <Trash2 class="h-3.5 w-3.5" />
                          </Button>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <DataTablePagination
                v-if="filteredVouchers.length > 0 && !loading"
                :page="page" :page-size="pageSize" :total="filteredVouchers.length"
                @update:page="page = $event" @update:page-size="pageSize = $event; page = 1"
              />
            </CardContent>
          </Card>
        </div>

        <!-- ─── Inline Form View ─── -->
        <div v-else key="form-view" class="flex flex-col gap-6">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-zinc-200 dark:border-zinc-800 pb-4">
            <div class="flex items-center gap-3">
              <Button variant="outline" size="icon" @click="showForm = false" :disabled="saving" class="h-9 w-9 rounded-lg">
                <ArrowLeft class="h-4 w-4" />
              </Button>
              <div>
                <h2 class="text-xl font-bold tracking-tight flex items-center gap-2">
                  <span>{{ modalMode === 'create' ? 'Tambah Voucer' : 'Edit Voucer' }}</span>
                  <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-primary/10 text-primary border border-primary/20 uppercase tracking-wider">
                    {{ modalMode === 'create' ? 'Baru' : 'Modifikasi' }}
                  </span>
                </h2>
                <p class="text-xs text-muted-foreground mt-0.5">{{ modalMode === 'create' ? 'Lengkapi detail kode promo baru.' : 'Perbarui informasi voucer.' }}</p>
              </div>
            </div>
            <div class="flex items-center gap-3 w-full sm:w-auto">
              <Button variant="outline" @click="showForm = false" :disabled="saving" class="flex-1 sm:flex-none">Batal</Button>
              <Button @click="saveVoucher" :disabled="saving" class="flex-1 sm:flex-none bg-primary text-primary-foreground hover:bg-primary/95 shadow-md shadow-primary/20">
                <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
                <span>{{ modalMode === 'create' ? 'Simpan Voucer' : 'Perbarui' }}</span>
              </Button>
            </div>
          </div>

          <Alert v-if="formError" variant="destructive">
            <p class="text-sm font-medium">{{ formError }}</p>
          </Alert>

          <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">

            <!-- LEFT: Preview -->
            <div class="lg:col-span-4">
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <Label class="text-sm font-semibold border-b pb-3 border-zinc-100 dark:border-zinc-800 block">Pratinjau Voucer</Label>
                  <div class="flex flex-col items-center justify-center py-6 gap-4">
                    <div class="w-20 h-20 rounded-2xl bg-primary/10 border-2 border-primary/20 flex items-center justify-center shadow-lg">
                      <Ticket class="h-10 w-10 text-primary" />
                    </div>
                    <div class="text-center space-y-1">
                      <p class="font-mono font-bold text-primary tracking-widest text-lg">{{ form.code || 'KODE' }}</p>
                      <p class="font-semibold text-base text-zinc-900 dark:text-zinc-100">{{ form.name || 'Nama Voucer' }}</p>
                      <p class="text-sm font-bold text-emerald-600 dark:text-emerald-400">
                        {{ form.discountType === 'percent' ? `${form.discountValue || 0}%` : formatCurrency(form.discountValue || 0) }}
                      </p>
                    </div>
                  </div>
                  <div class="grid grid-cols-2 gap-3 pt-2 border-t border-zinc-100 dark:border-zinc-800">
                    <div class="rounded-lg bg-zinc-50 dark:bg-zinc-900/30 border border-zinc-100 dark:border-zinc-800 p-3 text-center">
                      <p class="text-[10px] font-semibold text-muted-foreground uppercase">Kuota</p>
                      <p class="text-lg font-bold text-zinc-800 dark:text-zinc-200 mt-1">{{ form.quota || '∞' }}</p>
                    </div>
                    <div class="rounded-lg bg-zinc-50 dark:bg-zinc-900/30 border border-zinc-100 dark:border-zinc-800 p-3 text-center">
                      <p class="text-[10px] font-semibold text-muted-foreground uppercase">Status</p>
                      <p class="text-sm font-bold mt-1" :class="form.isActive ? 'text-emerald-600' : 'text-zinc-400'">{{ form.isActive ? 'Aktif' : 'Off' }}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>

            <!-- RIGHT: Fields -->
            <div class="lg:col-span-8 space-y-6">

              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <h3 class="text-sm font-semibold border-b pb-2 border-zinc-100 dark:border-zinc-800 flex items-center gap-2">
                    <Ticket class="h-4 w-4 text-primary" />Identitas Voucer
                  </h3>
                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div class="space-y-1.5">
                      <Label for="v-code" class="text-xs font-semibold">Kode Voucer <span class="text-destructive">*</span></Label>
                      <Input id="v-code" v-model="form.code" placeholder="PROMO10" :disabled="saving" class="h-10 rounded-lg font-mono uppercase tracking-wider" />
                    </div>
                    <div class="space-y-1.5">
                      <Label for="v-name" class="text-xs font-semibold">Nama Voucer <span class="text-destructive">*</span></Label>
                      <Input id="v-name" v-model="form.name" placeholder="Diskon 10% Semua Produk" :disabled="saving" class="h-10 rounded-lg" />
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <h3 class="text-sm font-semibold border-b pb-2 border-zinc-100 dark:border-zinc-800 flex items-center gap-2">
                    <Tag class="h-4 w-4 text-primary" />Konfigurasi Diskon
                  </h3>
                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div class="space-y-1.5">
                      <Label for="v-type" class="text-xs font-semibold">Tipe Diskon</Label>
                      <select id="v-type" v-model="form.discountType" :disabled="saving" class="flex h-10 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50">
                        <option value="percent">Persentase (%)</option>
                        <option value="fixed">Nominal Tetap (Rp)</option>
                      </select>
                    </div>
                    <div class="space-y-1.5">
                      <Label for="v-val" class="text-xs font-semibold">Nilai Diskon <span class="text-destructive">*</span></Label>
                      <Input id="v-val" type="number" v-model="form.discountValue" :disabled="saving" class="h-10 rounded-lg" />
                    </div>
                    <div class="space-y-1.5">
                      <Label for="v-min" class="text-xs font-semibold">Min. Pembelian <span class="text-muted-foreground text-[10px] font-normal">(opsional)</span></Label>
                      <Input id="v-min" type="number" v-model="form.minPurchase" :disabled="saving" class="h-10 rounded-lg" />
                    </div>
                    <div class="space-y-1.5">
                      <Label for="v-max" class="text-xs font-semibold">Maks. Diskon (Cap) <span class="text-muted-foreground text-[10px] font-normal">(opsional)</span></Label>
                      <Input id="v-max" type="number" v-model="form.maxDiscount" placeholder="Kosongkan jika tidak ada cap" :disabled="saving" class="h-10 rounded-lg" />
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <h3 class="text-sm font-semibold border-b pb-2 border-zinc-100 dark:border-zinc-800 flex items-center gap-2">
                    <Settings2 class="h-4 w-4 text-primary" />Masa Berlaku &amp; Pengaturan
                  </h3>
                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div class="space-y-1.5">
                      <Label for="v-from" class="text-xs font-semibold">Berlaku Dari <span class="text-muted-foreground text-[10px] font-normal">(opsional)</span></Label>
                      <Input id="v-from" type="date" v-model="form.validFrom" :disabled="saving" class="h-10 rounded-lg" />
                    </div>
                    <div class="space-y-1.5">
                      <Label for="v-until" class="text-xs font-semibold">Berlaku Sampai <span class="text-muted-foreground text-[10px] font-normal">(opsional)</span></Label>
                      <Input id="v-until" type="date" v-model="form.validUntil" :disabled="saving" class="h-10 rounded-lg" />
                    </div>
                    <div class="space-y-1.5">
                      <Label for="v-quota" class="text-xs font-semibold">Kuota <span class="text-muted-foreground text-[10px] font-normal">(opsional)</span></Label>
                      <Input id="v-quota" type="number" v-model="form.quota" placeholder="Kosongkan jika tidak terbatas" :disabled="saving" class="h-10 rounded-lg" />
                    </div>
                    <div class="flex items-center justify-between rounded-xl border p-4 bg-zinc-50/50 dark:bg-zinc-900/30">
                      <div>
                        <Label class="text-xs font-semibold">Status Aktif</Label>
                        <p class="text-[10px] text-muted-foreground">Aktifkan agar voucer dapat digunakan.</p>
                      </div>
                      <button
                        type="button" :disabled="saving"
                        class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors disabled:opacity-50"
                        :class="form.isActive ? 'bg-primary' : 'bg-zinc-200 dark:bg-zinc-700'"
                        @click="form.isActive = !form.isActive"
                      >
                        <span class="pointer-events-none block h-5 w-5 rounded-full bg-background shadow-lg transition-transform" :class="form.isActive ? 'translate-x-5' : 'translate-x-0'" />
                      </button>
                    </div>
                  </div>
                </CardContent>
              </Card>

            </div>
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
          <div class="relative bg-card rounded-xl shadow-2xl w-full max-w-md overflow-hidden border border-border pointer-events-auto">
            <div class="p-6">
              <h3 class="text-lg font-semibold text-destructive flex items-center gap-2">
                <Trash2 class="h-5 w-5" />Hapus Voucer
              </h3>
              <p class="text-sm text-muted-foreground mt-2">
                Hapus voucer <span class="font-semibold text-foreground font-mono">{{ deleteModal.voucher?.code }}</span> secara permanen.
              </p>
              <div class="mt-4">
                <Label class="text-sm font-medium">Ketik <span class="font-bold bg-muted px-1.5 py-0.5 rounded">{{ deleteModal.voucher?.code }}</span> untuk konfirmasi.</Label>
                <Input v-model="deleteModal.confirmText" class="mt-2" placeholder="Masukkan kode voucer" @keyup.enter="confirmDelete" />
              </div>
            </div>
            <div class="flex items-center justify-end gap-3 px-6 py-4 bg-muted/30 border-t">
              <Button variant="outline" @click="closeDeleteModal">Batal</Button>
              <Button variant="destructive" :disabled="deleteModal.confirmText !== deleteModal.voucher?.code || deleting" @click="confirmDelete">
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
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.scale-enter-active, .scale-leave-active { transition: opacity 0.25s ease, transform 0.25s cubic-bezier(0.16, 1, 0.3, 1); }
.scale-enter-from, .scale-leave-to { opacity: 0; transform: scale(0.96); }
</style>
