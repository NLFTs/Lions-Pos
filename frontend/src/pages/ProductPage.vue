<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
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
import Table from '@/components/ui/Table.vue'
import TableHeader from '@/components/ui/TableHeader.vue'
import TableBody from '@/components/ui/TableBody.vue'
import TableRow from '@/components/ui/TableRow.vue'
import TableHead from '@/components/ui/TableHead.vue'
import TableCell from '@/components/ui/TableCell.vue'
import api from '@/lib/api'
import { Plus, Pencil, Trash2, Loader2, X, Filter, Package, Upload, ChevronDown } from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()

// ─── State ───────────────────────────────────────────────────────────────────
const products = ref([])
const categories = ref([])
const pagination = ref({ page: 0, size: 10, totalPages: 0, totalElements: 0 })
const loading = ref(false)
const error = ref(null)
const searchQuery = ref('')
const showFilter = ref(false)
const sortBy = ref('terbaru') // 'terbaru', 'harga-termahal', 'harga-termurah'
const filterStatus = ref('all') // 'all', 'aktif', 'nonaktif'
const filterStock = ref('all') // 'all', 'dilacak', 'bebas'

const activeFilterCount = computed(() => {
  let count = 0
  if (sortBy.value !== 'terbaru') count++
  if (filterStatus.value !== 'all') count++
  if (filterStock.value !== 'all') count++
  return count
})

const togglingStatus = ref(null)

function clearFilters() {
  sortBy.value = 'terbaru'
  filterStatus.value = 'all'
  filterStock.value = 'all'
}

const filterRef = ref(null)
function handleOutsideClick(e) {
  if (filterRef.value && !filterRef.value.contains(e.target)) {
    showFilter.value = false
  }
}

const filteredProducts = computed(() => {
  let result = products.value

  // 1. Status Filter
  if (filterStatus.value === 'aktif') {
    result = result.filter(p => p.isActive)
  } else if (filterStatus.value === 'nonaktif') {
    result = result.filter(p => !p.isActive)
  }

  // 2. Stock Filter
  if (filterStock.value === 'dilacak') {
    result = result.filter(p => p.trackStock)
  } else if (filterStock.value === 'bebas') {
    result = result.filter(p => !p.trackStock)
  }

  // 3. Search Filter
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(p =>
      (p.name && p.name.toLowerCase().includes(q)) ||
      (p.sku && p.sku.toLowerCase().includes(q)) ||
      (p.categoryName && p.categoryName.toLowerCase().includes(q))
    )
  }

  // 3. Sort
  if (sortBy.value === 'harga-termahal') {
    result = [...result].sort((a, b) => b.price - a.price)
  } else if (sortBy.value === 'harga-termurah') {
    result = [...result].sort((a, b) => a.price - b.price)
  } else if (sortBy.value === 'terbaru') {
    result = [...result].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
  }

  return result
})

// Drawer
const showDrawer = ref(false)
const modalMode = ref('create') // 'create' | 'edit'
const saving = ref(false)
const formError = ref(null)

const emptyForm = () => ({
  id: null,
  name: '',
  sku: '',
  price: '',
  categoryId: null,
  trackStock: true,
  isActive: true,
  image: null,
  imagePreview: null,
})

const form = ref(emptyForm())

// ─── Mock Data (fallback dev) ─────────────────────────────────────────────────
const MOCK_PRODUCTS = [
  { id: '1', name: 'Kaos Polos Putih', sku: 'KPP-001', price: 85000, categoryName: 'Pakaian', trackStock: true, isActive: true, createdAt: '2024-11-01T08:00:00Z', imageUrl: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=150&q=80' },
  { id: '2', name: 'Celana Chino Beige', sku: 'CCB-002', price: 195000, categoryName: 'Pakaian', trackStock: true, isActive: true, createdAt: '2024-11-03T09:00:00Z', imageUrl: 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=150&q=80' },
  { id: '3', name: 'Sepatu Sneakers Hitam', sku: 'SSH-003', price: 450000, categoryName: 'Alas Kaki', trackStock: true, isActive: true, createdAt: '2024-11-05T10:00:00Z', imageUrl: null },
  { id: '4', name: 'Topi Baseball Biru Navy', sku: 'TBB-004', price: 75000, categoryName: 'Aksesori', trackStock: true, isActive: false, createdAt: '2024-11-07T11:00:00Z', imageUrl: null },
  { id: '5', name: 'Jaket Bomber Olive', sku: 'JBO-005', price: 320000, categoryName: 'Pakaian', trackStock: false, isActive: true, createdAt: '2024-11-09T12:00:00Z', imageUrl: null },
  { id: '6', name: 'Tas Selempang Canvas', sku: 'TSC-006', price: 135000, categoryName: 'Tas', trackStock: true, isActive: true, createdAt: '2024-11-11T13:00:00Z', imageUrl: null },
  { id: '7', name: 'Kemeja Flannel Kotak', sku: 'KFK-007', price: 210000, categoryName: 'Pakaian', trackStock: true, isActive: true, createdAt: '2024-11-13T14:00:00Z', imageUrl: null },
  { id: '8', name: 'Kaos Kaki Sport (3 pcs)', sku: null, price: 45000, categoryName: null, trackStock: true, isActive: true, createdAt: '2024-11-15T15:00:00Z', imageUrl: null },
  { id: '9', name: 'Sabuk Kulit Coklat', sku: 'SKC-009', price: 98000, categoryName: 'Aksesori', trackStock: false, isActive: true, createdAt: '2024-11-17T16:00:00Z', imageUrl: null },
  { id: '10', name: 'Dompet Kulit Minimalis', sku: 'DKM-010', price: 165000, categoryName: 'Aksesori', trackStock: true, isActive: false, createdAt: '2024-11-19T17:00:00Z', imageUrl: null },
]

// ─── Fetch products ───────────────────────────────────────────────────────────
async function fetchProducts(page = 0) {
  loading.value = true
  error.value = null
  try {
    const res = await api.get(`/api/v1/products?page=${page}&size=${pagination.value.size}`)
    const data = res.data.data
    products.value = data.content
    pagination.value = {
      ...pagination.value,
      page: data.number,
      totalPages: data.totalPages,
      totalElements: data.totalElements,
    }
  } catch (err) {
    // Fallback ke mock data di development
    if (import.meta.env.DEV) {
      const size = pagination.value.size
      const start = page * size
      const paged = MOCK_PRODUCTS.slice(start, start + size)
      products.value = paged
      pagination.value = {
        ...pagination.value,
        page,
        totalPages: Math.ceil(MOCK_PRODUCTS.length / size),
        totalElements: MOCK_PRODUCTS.length,
      }
    } else {
      error.value = err.response?.data?.message || 'Gagal memuat data produk.'
    }
  } finally {
    loading.value = false
  }
}

function updatePageSize(newSize) {
  pagination.value.size = newSize
  fetchProducts(0)
}

async function fetchCategories() {
  try {
    const res = await api.get('/api/v1/categories')
    categories.value = res.data.data
  } catch (_) {
    // non-critical
  }
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
  document.addEventListener('click', handleOutsideClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
})

// ─── Create / Edit Drawer ─────────────────────────────────────────────────────
function openCreate() {
  form.value = emptyForm()
  formError.value = null
  modalMode.value = 'create'
  showDrawer.value = true
}

function openEdit(product) {
  form.value = {
    id: product.id,
    name: product.name || '',
    sku: product.sku || '',
    price: product.price ?? '',
    categoryId: product.categoryId || null,
    trackStock: product.trackStock ?? true,
    isActive: product.isActive ?? true,
    image: null,
    imagePreview: product.imageUrl || null,
  }
  formError.value = null
  modalMode.value = 'edit'
  showDrawer.value = true
}

function closeDrawer() {
  showDrawer.value = false
}

function handleImageUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  
  if (file.size > 2 * 1024 * 1024) {
    toast.error('Ukuran gambar maksimal 2MB')
    return
  }
  
  form.value.image = file
  form.value.imagePreview = URL.createObjectURL(file)
}

async function saveProduct() {
  formError.value = null
  saving.value = true
  try {
    // Note: In real app, we would use FormData to upload the image file.
    // For now we send standard JSON payload as per backend expectation.
    const payload = {
      name: form.value.name,
      sku: form.value.sku || undefined,
      price: parseFloat(form.value.price),
      categoryId: form.value.categoryId || undefined,
      trackStock: form.value.trackStock,
      isActive: form.value.isActive,
    }
    
    // If backend supports multipart/form-data:
    // const formData = new FormData()
    // Object.entries(payload).forEach(([k,v]) => formData.append(k, v))
    // if (form.value.image) formData.append('image', form.value.image)
    
    if (modalMode.value === 'create') {
      await api.post('/api/v1/products', payload)
      toast.success('Produk berhasil ditambahkan!')
    } else {
      await api.put(`/api/v1/products/${form.value.id}`, payload)
      toast.success('Produk berhasil diperbarui!')
    }
    showDrawer.value = false
    fetchProducts(pagination.value.page)
  } catch (err) {
    formError.value = err.response?.data?.data?.message || err.response?.data?.message || 'Gagal menyimpan produk.'
  } finally {
    saving.value = false
  }
}

// ─── Delete ───────────────────────────────────────────────────────────────────
const deleteModal = ref({
  show: false,
  product: null,
  confirmText: ''
})
const deleting = ref(false)

function doDelete(product) {
  deleteModal.value = {
    show: true,
    product,
    confirmText: ''
  }
}

function closeDeleteModal() {
  deleteModal.value.show = false
  setTimeout(() => {
    deleteModal.value.product = null
    deleteModal.value.confirmText = ''
  }, 300)
}

async function confirmDelete() {
  if (deleteModal.value.confirmText !== deleteModal.value.product?.name) return

  deleting.value = true
  try {
    await api.delete(`/api/v1/products/${deleteModal.value.product.id}`)
    toast.success('Produk berhasil dihapus!')
    fetchProducts(pagination.value.page)
    closeDeleteModal()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menghapus produk.')
  } finally {
    deleting.value = false
  }
}

async function toggleStatus(product) {
  if (!can('produk.update')) {
    toast.error('Anda tidak memiliki izin untuk mengubah status produk.')
    return
  }
  
  if (togglingStatus.value === product.id) return
  
  togglingStatus.value = product.id
  const originalStatus = product.isActive
  product.isActive = !product.isActive // Optimistic update
  
  try {
    await api.patch(`/api/v1/products/${product.id}`, {
      isActive: product.isActive
    })
    toast.success(`Status ${product.name} berhasil diperbarui menjadi ${product.isActive ? 'Aktif' : 'Nonaktif'}.`)
  } catch (err) {
    product.isActive = originalStatus // Rollback
    toast.error(err.response?.data?.message || 'Gagal memperbarui status produk.')
  } finally {
    togglingStatus.value = null
  }
}

// ─── Utils ────────────────────────────────────────────────────────────────────
function formatCurrency(value) {
  if (value == null || value === '') return '-'
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(value)
}

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' })
}

const AVATAR_COLORS = [
  { bg: '#ede9fe', color: '#6d28d9' }, // violet
  { bg: '#dbeafe', color: '#1d4ed8' }, // blue
  { bg: '#d1fae5', color: '#065f46' }, // emerald
  { bg: '#fef3c7', color: '#92400e' }, // amber
  { bg: '#fee2e2', color: '#991b1b' }, // red
  { bg: '#fce7f3', color: '#9d174d' }, // pink
  { bg: '#e0f2fe', color: '#0369a1' }, // sky
  { bg: '#f3f4f6', color: '#374151' }, // gray
]

function productAvatarStyle(name = '') {
  const idx = name.charCodeAt(0) % AVATAR_COLORS.length
  const c = AVATAR_COLORS[idx]
  return { backgroundColor: c.bg, color: c.color }
}
</script>

<template>
  <AppLayout>
    <div class="flex flex-col gap-6 p-4 sm:p-6 pb-20">
      <!-- ─── Page Header ─── -->
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Produk</h1>
          <p class="text-muted-foreground text-sm mt-1">
            Kelola data produk Anda.
          </p>
        </div>

        <div class="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto">
          <div class="w-full sm:w-72">
            <DataTableSearch v-model="searchQuery" placeholder="Cari produk..." />
          </div>
          <div class="flex items-center gap-2 w-full sm:w-auto">
          <div ref="filterRef" class="relative flex-1 sm:flex-none">
            <button
              @click="showFilter = !showFilter"
              class="w-full flex items-center justify-center gap-2 h-9 px-3 rounded-md border border-border bg-background hover:bg-accent text-foreground text-sm font-medium transition-colors"
            >
              <Filter class="h-3.5 w-3.5" />
              <span>Filter</span>
              <!-- Active count badge -->
              <span v-if="activeFilterCount > 0" class="inline-flex items-center justify-center h-4.5 min-w-[18px] px-1 rounded-full bg-primary text-primary-foreground text-[10px] font-bold leading-none">
                {{ activeFilterCount }}
              </span>
              <ChevronDown class="h-3 w-3 text-muted-foreground" :class="showFilter ? 'rotate-180' : ''" style="transition: transform 0.2s" />
            </button>

            <!-- Dropdown Panel -->
            <Transition name="fade">
              <div
                v-if="showFilter"
                class="absolute left-0 sm:left-auto sm:right-0 top-full mt-1 z-30 w-64 bg-card border border-border rounded-lg shadow-xl overflow-hidden"
              >
                <!-- Header -->
                <div class="flex items-center justify-between px-3 py-2.5 border-b border-border">
                  <span class="text-xs font-semibold text-foreground uppercase tracking-wide">Filter</span>
                  <button
                    v-if="activeFilterCount > 0"
                    @click="clearFilters"
                    class="text-xs text-red-500 hover:text-red-600 font-medium transition-colors"
                  >Clear all</button>
                </div>

                <!-- Urutkan Section -->
                <div class="px-3 pt-3 pb-2">
                  <p class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2">Urutkan</p>
                  <div class="space-y-1">
                    <label v-for="sortOption in [{val: 'terbaru', label: 'Terbaru'}, {val: 'harga-termahal', label: 'Harga Tertinggi'}, {val: 'harga-termurah', label: 'Harga Terendah'}]" :key="sortOption.val"
                      class="flex items-center gap-2.5 px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors"
                    >
                      <div
                        class="relative h-4 w-4 shrink-0 rounded border-2 flex items-center justify-center transition-all"
                        :class="sortBy === sortOption.val ? 'bg-primary border-primary' : 'border-border bg-background'"
                        @click="sortBy = sortOption.val"
                      >
                        <svg v-if="sortBy === sortOption.val" xmlns="http://www.w3.org/2000/svg" class="h-2.5 w-2.5 text-primary-foreground" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3.5"><polyline points="20 6 9 17 4 12"/></svg>
                      </div>
                      <span class="text-sm text-foreground select-none" @click="sortBy = sortOption.val">{{ sortOption.label }}</span>
                    </label>
                  </div>
                </div>

                <div class="mx-3 border-t border-border" />

                <!-- Status Section -->
                <div class="px-3 pt-2 pb-2">
                  <p class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2">Status</p>
                  <div class="space-y-1">
                    <label v-for="statusOption in [{val: 'all', label: 'Semua Status'}, {val: 'aktif', label: 'Aktif'}, {val: 'nonaktif', label: 'Nonaktif'}]" :key="statusOption.val"
                      class="flex items-center gap-2.5 px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors"
                    >
                      <div
                        class="relative h-4 w-4 shrink-0 rounded border-2 flex items-center justify-center transition-all"
                        :class="filterStatus === statusOption.val ? 'bg-primary border-primary' : 'border-border bg-background'"
                        @click="filterStatus = statusOption.val"
                      >
                        <svg v-if="filterStatus === statusOption.val" xmlns="http://www.w3.org/2000/svg" class="h-2.5 w-2.5 text-primary-foreground" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3.5"><polyline points="20 6 9 17 4 12"/></svg>
                      </div>
                      <span class="text-sm text-foreground select-none" @click="filterStatus = statusOption.val">{{ statusOption.label }}</span>
                    </label>
                  </div>
                </div>

                <div class="mx-3 border-t border-border" />

                <!-- Stok Section -->
                <div class="px-3 pt-2 pb-3">
                  <p class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2">Stok</p>
                  <div class="space-y-1">
                    <label v-for="stockOption in [{val: 'all', label: 'Semua Stok'}, {val: 'dilacak', label: 'Dilacak'}, {val: 'bebas', label: 'Bebas'}]" :key="stockOption.val"
                      class="flex items-center gap-2.5 px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors"
                    >
                      <div
                        class="relative h-4 w-4 shrink-0 rounded border-2 flex items-center justify-center transition-all"
                        :class="filterStock === stockOption.val ? 'bg-primary border-primary' : 'border-border bg-background'"
                        @click="filterStock = stockOption.val"
                      >
                        <svg v-if="filterStock === stockOption.val" xmlns="http://www.w3.org/2000/svg" class="h-2.5 w-2.5 text-primary-foreground" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3.5"><polyline points="20 6 9 17 4 12"/></svg>
                      </div>
                      <span class="text-sm text-foreground select-none" @click="filterStock = stockOption.val">{{ stockOption.label }}</span>
                    </label>
                  </div>
                </div>
              </div>
            </Transition>
          </div>
          <!-- /Filter Dropdown -->

          <Button v-if="can('produk.store')" @click="openCreate" size="sm" class="flex-1 sm:flex-none flex items-center justify-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
            <Plus class="h-4 w-4" />
            <span>Tambah Produk</span>
          </Button>
          </div>
        </div>
      </div>

      <Alert v-if="error" variant="destructive" class="mb-4">
        {{ error }}
      </Alert>

      <!-- Table card -->
      <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm overflow-hidden">
        <CardContent class="p-0">
          <!-- Loading -->
          <div v-if="loading" class="flex flex-col items-center justify-center py-24 gap-3">
            <Loader2 class="h-7 w-7 animate-spin text-primary/50" />
            <p class="text-xs text-muted-foreground">Memuat data...</p>
          </div>

          <!-- Empty -->
          <div v-else-if="filteredProducts.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
            <div class="w-14 h-14 rounded-full bg-muted flex items-center justify-center mb-4">
              <Package class="h-7 w-7 opacity-40" />
            </div>
            <p class="text-sm font-medium">Belum ada produk</p>
            <p class="text-xs text-muted-foreground/70 mt-1">Mulai dengan menambahkan produk pertama Anda.</p>
            <Button v-if="can('produk.store') && !searchQuery" size="sm" class="mt-4" @click="openCreate">
              <Plus class="h-3.5 w-3.5 mr-1.5" />
              Tambah Produk
            </Button>
          </div>

          <div v-else>
            <!-- ─── Mobile List View ─── -->
            <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
              <div
                v-for="product in filteredProducts"
                :key="'mobile-' + product.id"
                class="p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
              >
                <!-- Header (Image, Name, SKU, Actions) -->
                <div class="flex items-start justify-between gap-3">
                  <div class="flex items-center gap-3">
                    <img v-if="product.imageUrl" :src="product.imageUrl" class="w-12 h-12 rounded-lg object-cover border border-zinc-200 dark:border-zinc-800" />
                    <div
                      v-else
                      class="w-12 h-12 rounded-lg flex items-center justify-center text-sm font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-800/50"
                      :style="productAvatarStyle(product.name)"
                    >
                      {{ product.name?.charAt(0).toUpperCase() }}
                    </div>
                    <div>
                      <h4 class="font-medium text-sm text-zinc-900 dark:text-zinc-100 line-clamp-1">{{ product.name }}</h4>
                      <span v-if="product.sku" class="font-mono text-[10px] bg-zinc-100 dark:bg-zinc-800 text-zinc-500 px-1.5 py-0.5 rounded mt-1 inline-block">
                        {{ product.sku }}
                      </span>
                    </div>
                  </div>
                  
                  <div class="flex items-center gap-1 shrink-0">
                    <Button
                      v-if="can('produk.update')"
                      variant="ghost"
                      size="icon"
                      class="h-8 w-8 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 bg-zinc-50 dark:bg-zinc-800/50"
                      @click="openEdit(product)"
                    >
                      <Pencil class="h-3.5 w-3.5" />
                    </Button>
                    <Button
                      v-if="can('produk.delete')"
                      variant="ghost"
                      size="icon"
                      class="h-8 w-8 text-zinc-400 hover:text-destructive bg-zinc-50 dark:bg-zinc-800/50"
                      @click="doDelete(product)"
                    >
                      <Trash2 class="h-3.5 w-3.5" />
                    </Button>
                  </div>
                </div>

                <!-- Footer (Price, Category, Status) -->
                <div class="flex items-center justify-between mt-1">
                  <span class="text-sm font-semibold text-zinc-800 dark:text-zinc-200">{{ formatCurrency(product.price) }}</span>
                  <div class="flex items-center gap-2">
                    <span
                      v-if="product.categoryName"
                      class="text-[10px] font-medium px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40"
                    >
                      {{ product.categoryName }}
                    </span>
                    <button
                      type="button"
                      role="switch"
                      :aria-checked="product.isActive"
                      :disabled="togglingStatus === product.id"
                      class="relative inline-flex h-5 w-9 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                      :class="product.isActive ? 'bg-primary' : 'bg-zinc-200 dark:bg-zinc-700'"
                      @click.stop="toggleStatus(product)"
                    >
                      <span
                        class="pointer-events-none block h-4 w-4 rounded-full bg-background shadow-lg ring-0 transition-transform flex items-center justify-center"
                        :class="product.isActive ? 'translate-x-4' : 'translate-x-0'"
                      >
                        <Loader2 v-if="togglingStatus === product.id" class="h-2.5 w-2.5 animate-spin text-primary" />
                      </span>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- ─── Desktop Table ─── -->
            <div class="hidden md:block overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow class="hover:bg-transparent border-b border-zinc-100 dark:border-zinc-800">
                    <TableHead class="pl-5 py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 w-[260px]">Produk</TableHead>
                    <TableHead class="py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500">SKU</TableHead>
                    <TableHead class="py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500">Harga</TableHead>
                    <TableHead class="py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500">Kategori</TableHead>
                    <TableHead class="py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center">Stok</TableHead>
                    <TableHead class="py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 text-center">Status</TableHead>
                    <TableHead class="py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500">Dibuat</TableHead>
                    <TableHead class="pr-5 py-3 text-right"></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  <TableRow
                    v-for="product in filteredProducts"
                    :key="product.id"
                    class="group border-b border-zinc-100 dark:border-zinc-800/60 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
                  >
                    <!-- Produk: Avatar/Gambar + Nama -->
                    <TableCell class="pl-5 py-3">
                      <div class="flex items-center gap-3">
                        <img v-if="product.imageUrl" :src="product.imageUrl" class="w-8 h-8 rounded-lg object-cover border border-zinc-200 dark:border-zinc-700" />
                        <div
                          v-else
                          class="w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold shrink-0 select-none border border-zinc-200 dark:border-zinc-700/50"
                          :style="productAvatarStyle(product.name)"
                        >
                          {{ product.name?.charAt(0).toUpperCase() }}
                        </div>
                        <span class="font-medium text-sm text-zinc-900 dark:text-zinc-100 truncate max-w-[170px]">{{ product.name }}</span>
                      </div>
                    </TableCell>

                    <!-- SKU -->
                    <TableCell class="py-3">
                      <span v-if="product.sku" class="font-mono text-xs bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 px-2 py-1 rounded">
                        {{ product.sku }}
                      </span>
                      <span v-else class="text-zinc-300 dark:text-zinc-600 text-sm">—</span>
                    </TableCell>

                    <!-- Harga -->
                    <TableCell class="py-3">
                      <span class="text-sm font-semibold text-zinc-800 dark:text-zinc-200">{{ formatCurrency(product.price) }}</span>
                    </TableCell>

                    <!-- Kategori -->
                    <TableCell class="py-3">
                      <span
                        v-if="product.categoryName"
                        class="inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40"
                      >
                        {{ product.categoryName }}
                      </span>
                      <span v-else class="text-zinc-300 dark:text-zinc-600 text-sm">—</span>
                    </TableCell>

                    <!-- Lacak Stok -->
                    <TableCell class="py-3 text-center">
                      <span
                        class="inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full"
                        :class="product.trackStock
                          ? 'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-800/40'
                          : 'bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700'"
                      >
                        <span class="w-1.5 h-1.5 rounded-full" :class="product.trackStock ? 'bg-emerald-500' : 'bg-zinc-400'" />
                        {{ product.trackStock ? 'Dilacak' : 'Bebas' }}
                      </span>
                    </TableCell>

                    <!-- Status Aktif -->
                    <TableCell class="py-3 text-center">
                      <button
                        type="button"
                        role="switch"
                        :aria-checked="product.isActive"
                        :disabled="togglingStatus === product.id"
                        class="relative inline-flex h-5 w-9 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 mx-auto"
                        :class="product.isActive ? 'bg-primary' : 'bg-zinc-200 dark:bg-zinc-700'"
                        @click.stop="toggleStatus(product)"
                      >
                        <span
                          class="pointer-events-none block h-4 w-4 rounded-full bg-background shadow-lg ring-0 transition-transform flex items-center justify-center"
                          :class="product.isActive ? 'translate-x-4' : 'translate-x-0'"
                        >
                          <Loader2 v-if="togglingStatus === product.id" class="h-2.5 w-2.5 animate-spin text-primary" />
                        </span>
                      </button>
                    </TableCell>

                    <!-- Tanggal -->
                    <TableCell class="py-3">
                      <span class="text-xs text-zinc-400 dark:text-zinc-500">{{ formatDate(product.createdAt) }}</span>
                    </TableCell>

                    <!-- Aksi -->
                    <TableCell class="pr-4 py-3 text-right">
                      <div class="flex justify-end gap-1 transition-opacity">
                        <Button
                          v-if="can('produk.update')"
                          variant="ghost"
                          size="icon"
                          class="h-7 w-7 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 rounded-md"
                          title="Edit"
                          @click="openEdit(product)"
                        >
                          <Pencil class="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          v-if="can('produk.delete')"
                          variant="ghost"
                          size="icon"
                          class="h-7 w-7 text-zinc-400 hover:text-destructive hover:bg-destructive/10 rounded-md"
                          title="Hapus"
                          @click="doDelete(product)"
                        >
                          <Trash2 class="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </div>
          </div>

          <DataTablePagination
            v-if="pagination.totalElements > 0 && !loading"
            :page="pagination.page + 1"
            :page-size="pagination.size"
            :total="pagination.totalElements"
            @update:page="fetchProducts($event - 1)"
            @update:page-size="updatePageSize($event)"
          />
        </CardContent>
      </Card>

    </div>

    <!-- ─── Right-side Drawer ─── -->
    <Teleport to="body">
      <!-- Backdrop -->
      <Transition name="fade">
        <div
          v-if="showDrawer"
          class="fixed inset-0 z-[50] bg-black/40 backdrop-blur-sm"
          @click="closeDrawer"
        />
      </Transition>

      <!-- Panel -->
      <Transition name="slide-right">
        <div
          v-if="showDrawer"
          class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[420px] h-full bg-card shadow-2xl sm:border-l overflow-hidden"
        >
          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b shrink-0">
            <div>
              <h3 class="font-semibold text-base">
                {{ modalMode === 'create' ? 'Tambah Produk' : 'Edit Produk' }}
              </h3>
              <p class="text-xs text-muted-foreground mt-0.5">
                {{ modalMode === 'create' ? 'Isi detail produk baru.' : 'Perbarui informasi produk.' }}
              </p>
            </div>
            <Button variant="ghost" size="icon" @click="closeDrawer">
              <X class="h-4 w-4" />
            </Button>
          </div>

          <!-- Body (scrollable) -->
          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5">
            <Alert v-if="formError" variant="destructive">
              <p class="text-sm">{{ formError }}</p>
            </Alert>

            <!-- Foto Produk -->
            <div class="space-y-1.5">
              <Label>Foto Produk</Label>
              <div class="flex items-center gap-4">
                <div class="h-20 w-20 shrink-0 overflow-hidden rounded-lg border border-dashed border-zinc-300 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-900 flex items-center justify-center relative">
                  <img v-if="form.imagePreview" :src="form.imagePreview" alt="Preview" class="h-full w-full object-cover" />
                  <Package v-else class="h-8 w-8 text-zinc-400" />
                  <input
                    type="file"
                    accept="image/*"
                    class="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    @change="handleImageUpload"
                    :disabled="saving"
                  />
                </div>
                <div class="flex flex-col gap-2">
                  <div class="relative">
                    <Button type="button" variant="outline" size="sm">
                      <Upload class="h-3.5 w-3.5 mr-2" />
                      Pilih Foto
                    </Button>
                    <input
                      type="file"
                      accept="image/*"
                      class="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                      @change="handleImageUpload"
                      :disabled="saving"
                    />
                  </div>
                  <p class="text-[10px] text-muted-foreground">Format: JPG, PNG. Maks 2MB.</p>
                </div>
              </div>
            </div>

            <!-- Nama Produk -->
            <div class="space-y-1.5">
              <Label for="f-name">Nama Produk <span class="text-destructive">*</span></Label>
              <Input id="f-name" v-model="form.name" placeholder="Contoh: Kaos Polos Putih" :disabled="saving" />
            </div>

            <!-- SKU -->
            <div class="space-y-1.5">
              <Label for="f-sku">SKU <span class="text-muted-foreground text-xs">(opsional)</span></Label>
              <Input id="f-sku" v-model="form.sku" placeholder="Contoh: KPP-001" :disabled="saving" />
            </div>

            <!-- Harga -->
            <div class="space-y-1.5">
              <Label for="f-price">Harga <span class="text-destructive">*</span></Label>
              <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-sm text-muted-foreground">Rp</span>
                <Input
                  id="f-price"
                  v-model="form.price"
                  type="number"
                  min="0"
                  step="100"
                  placeholder="0"
                  :disabled="saving"
                  class="pl-9"
                />
              </div>
            </div>

            <!-- Kategori -->
            <div class="space-y-1.5">
              <Label for="f-category">Kategori <span class="text-muted-foreground text-xs">(opsional)</span></Label>
              <select
                id="f-category"
                v-model="form.categoryId"
                :disabled="saving"
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"
              >
                <option :value="null">— Pilih kategori —</option>
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
              </select>
            </div>

            <!-- Lacak Stok -->
            <div class="flex items-center justify-between rounded-lg border p-4">
              <div class="space-y-0.5">
                <Label class="text-sm font-medium cursor-pointer" for="f-track-stock">Lacak Stok</Label>
                <p class="text-xs text-muted-foreground">Nonaktifkan untuk produk layanan.</p>
              </div>
              <button
                id="f-track-stock"
                type="button"
                role="switch"
                :aria-checked="form.trackStock"
                :disabled="saving"
                class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                :class="form.trackStock ? 'bg-primary' : 'bg-input'"
                @click="form.trackStock = !form.trackStock"
              >
                <span
                  class="pointer-events-none block h-5 w-5 rounded-full bg-background shadow-lg ring-0 transition-transform"
                  :class="form.trackStock ? 'translate-x-5' : 'translate-x-0'"
                />
              </button>
            </div>

          </div>

          <!-- Footer -->
          <div class="flex justify-end gap-3 px-6 py-4 border-t shrink-0 bg-muted/30">
            <Button variant="outline" @click="closeDrawer" :disabled="saving">Batal</Button>
            <Button @click="saveProduct" :disabled="saving">
              <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
              {{ modalMode === 'create' ? 'Simpan Produk' : 'Perbarui' }}
            </Button>
          </div>
        </div>
      </Transition>

      <!-- Delete Confirmation Modal -->
      <!-- Backdrop -->
      <Transition name="fade">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[60] bg-black/40 backdrop-blur-sm" @click="closeDeleteModal"></div>
      </Transition>
      
      <!-- Modal Content -->
      <Transition name="scale">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none">
          <div class="relative bg-card rounded-xl shadow-2xl w-full max-w-md overflow-hidden border border-border pointer-events-auto">
            <div class="p-6">
              <h3 class="text-lg font-semibold text-destructive flex items-center gap-2">
                <Trash2 class="h-5 w-5" />
                Hapus Produk
              </h3>
              <p class="text-sm text-muted-foreground mt-2">
                Tindakan ini tidak dapat dibatalkan. Hal ini akan menghapus produk 
                <span class="font-semibold text-foreground">{{ deleteModal.product?.name }}</span> secara permanen.
              </p>
              
              <div class="mt-4">
                <Label class="text-sm font-medium">
                  Ketik <span class="font-bold select-all bg-muted px-1.5 py-0.5 rounded text-foreground">{{ deleteModal.product?.name }}</span> untuk mengonfirmasi.
                </Label>
                <Input
                  v-model="deleteModal.confirmText"
                  class="mt-2"
                  placeholder="Masukkan nama produk"
                />
              </div>
            </div>
            <div class="flex items-center justify-end gap-3 px-6 py-4 bg-muted/30 border-t">
              <Button variant="outline" @click="closeDeleteModal">Batal</Button>
              <Button
                variant="destructive"
                :disabled="deleteModal.confirmText !== deleteModal.product?.name || deleting"
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
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-right-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
}

.scale-enter-active,
.scale-leave-active {
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.scale-enter-from,
.scale-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
