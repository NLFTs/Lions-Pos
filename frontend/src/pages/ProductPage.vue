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
import Table from '@/components/ui/Table.vue'
import TableHeader from '@/components/ui/TableHeader.vue'
import TableBody from '@/components/ui/TableBody.vue'
import TableRow from '@/components/ui/TableRow.vue'
import TableHead from '@/components/ui/TableHead.vue'
import TableCell from '@/components/ui/TableCell.vue'
import api from '@/lib/api'
import { Plus, Pencil, Trash2, Loader2, X, Filter, Package, Upload, ChevronDown, Check, PowerOff, Sparkles, ArrowLeft, Star, Image } from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { toast } = useToast()
const { confirm } = useConfirm()
const authStore = useAuthStore()

// ─── Selection State ──────────────────────────────────────────────────────────
const selectedIds = ref([])

const isAllSelected = computed(() => {
  const visible = filteredProducts.value
  if (visible.length === 0) return false
  return visible.every(p => selectedIds.value.includes(p.id))
})

function toggleSelectAll() {
  const visible = filteredProducts.value
  if (isAllSelected.value) {
    const visibleIds = visible.map(p => p.id)
    selectedIds.value = selectedIds.value.filter(id => !visibleIds.includes(id))
  } else {
    visible.forEach(p => {
      if (!selectedIds.value.includes(p.id)) {
        selectedIds.value.push(p.id)
      }
    })
  }
}

function toggleSelect(id) {
  const index = selectedIds.value.indexOf(id)
  if (index === -1) {
    selectedIds.value.push(id)
  } else {
    selectedIds.value.splice(index, 1)
  }
}

async function bulkDelete() {
  const count = selectedIds.value.length
  if (count === 0) return

  const ok = await confirm({
    title: 'Hapus Produk Terpilih',
    description: `Apakah Anda yakin ingin menghapus ${count} produk terpilih secara permanen?`,
    confirmLabel: 'Hapus',
    cancelLabel: 'Batal',
  })
  if (!ok) return

  loading.value = true
  try {
    await Promise.all(
      selectedIds.value.map(id => api.delete(`/api/v1/products/${id}`))
    )
    toast.success(`${count} produk berhasil dihapus!`)
    selectedIds.value = []
    fetchProducts(pagination.value.page)
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menghapus beberapa produk.')
    fetchProducts(pagination.value.page)
  } finally {
    loading.value = false
  }
}

async function bulkDeactivate() {
  const count = selectedIds.value.length
  if (count === 0) return

  const ok = await confirm({
    title: 'Nonaktifkan Produk Terpilih',
    description: `Apakah Anda yakin ingin menonaktifkan ${count} produk terpilih?`,
    confirmLabel: 'Nonaktifkan',
    cancelLabel: 'Batal',
  })
  if (!ok) return

  loading.value = true
  try {
    await Promise.all(
      selectedIds.value.map(id => api.patch(`/api/v1/products/${id}`, { is_active: false }))
    )
    toast.success(`${count} produk berhasil dinonaktifkan!`)
    selectedIds.value = []
    fetchProducts(pagination.value.page)
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal menonaktifkan beberapa produk.')
    fetchProducts(pagination.value.page)
  } finally {
    loading.value = false
  }
}

const isAdmin = computed(() => authStore.isAdmin)
const isSuperAdmin = computed(() => authStore.isSuperAdmin)

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
const TOGGLE_COOLDOWN_MS = 1000
const lastToggleTime = ref(0)
const isToggleOnCooldown = computed(() => {
  return Date.now() - lastToggleTime.value < TOGGLE_COOLDOWN_MS
})

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
    result = result.filter(p => p.is_active)
  } else if (filterStatus.value === 'nonaktif') {
    result = result.filter(p => !p.is_active)
  }

  // 2. Stock Filter
  if (filterStock.value === 'dilacak') {
    result = result.filter(p => p.track_stock)
  } else if (filterStock.value === 'bebas') {
    result = result.filter(p => !p.track_stock)
  }

  // 3. Search Filter
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(p =>
      (p.name && p.name.toLowerCase().includes(q)) ||
      (p.sku && p.sku.toLowerCase().includes(q)) ||
      (p.category_id?.name && p.category_id.name.toLowerCase().includes(q))
    )
  }

  // 3. Sort
  if (sortBy.value === 'harga-termahal') {
    result = [...result].sort((a, b) => (b.base_price || 0) - (a.base_price || 0))
  } else if (sortBy.value === 'harga-termurah') {
    result = [...result].sort((a, b) => (a.base_price || 0) - (b.base_price || 0))
  } else if (sortBy.value === 'terbaru') {
    result = [...result].sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime())
  }

  return result
})

// Form
const showDrawer = ref(false) // reused to toggle form and table
const modalMode = ref('create') // 'create' | 'edit'
const saving = ref(false)
const formError = ref(null)
const deletedPhotoIds = ref([])

const emptyForm = () => ({
  id: null,
  name: '',
  sku: '',
  price: '',
  categoryId: null,
  trackStock: true,
  isActive: true,
  images: [], // { id, url, file, preview, isPrimary, isExisting }
})

const form = ref(emptyForm())

// ─── IDR Price Formatting ─────────────────────────────────────────────────────
const priceDisplay = ref('')

function formatPriceDisplay(val) {
  if (val === '' || val === null || val === undefined) return ''
  // Strip non-digits
  const digits = String(val).replace(/\D/g, '')
  if (!digits) return ''
  // Add thousand separators (dots for IDR)
  return digits.replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}

function onPriceInput(e) {
  const raw = e.target.value.replace(/\./g, '').replace(/\D/g, '')
  priceDisplay.value = raw ? raw.replace(/\B(?=(\d{3})+(?!\d))/g, '.') : ''
  form.value.price = raw ? raw : ''
}

// ─── Validasi frontend ───────────────────────────────────────────────────────
const formErrors = ref({
  name: '',
  price: '',
  sku: '',
  images: ''
})

// ─── Fetch products ───────────────────────────────────────────────────────────
async function fetchProducts(page = 0) {
  loading.value = true
  error.value = null
  try {
    const url = isAdmin.value ? '/api/v1/products/admin' : '/api/v1/products'
    const res = await api.get(`${url}?page=${page}&size=${pagination.value.size}`)
    const data = res.data.data
    // Backend returns a List if not using Pageable properly in some versions, 
    // but the controller signature shows ResData<List<ProductResponse>>.
    // However, the frontend code was expecting data.content.
    // Let's adapt based on what we saw in ProductController.java (it returns a List).
    
    if (Array.isArray(data)) {
      products.value = data
      pagination.value.totalElements = data.length
      pagination.value.totalPages = 1
    } else {
      products.value = data.content || []
      pagination.value = {
        ...pagination.value,
        page: data.number || 0,
        totalPages: data.totalPages || 0,
        totalElements: data.totalElements || 0,
      }
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Gagal memuat data produk.'
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
    const url = isAdmin.value ? '/api/v1/categories/admin' : '/api/v1/categories'
    const res = await api.get(url)
    categories.value = res.data.data
  } catch (_) {
    // non-critical
  }
}

watch([searchQuery, filterStatus, filterStock, sortBy], () => {
  selectedIds.value = []
})

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
  priceDisplay.value = ''
  formErrors.value = { name: '', price: '', sku: '', images: '' }
  deletedPhotoIds.value = []
  formError.value = null
  modalMode.value = 'create'
  showDrawer.value = true
}

async function openEdit(product) {
  form.value = {
    id: product.id,
    name: product.name || '',
    sku: product.sku || '',
    price: product.base_price ?? '',
    categoryId: product.category_id?.id || null,
    trackStock: product.track_stock ?? true,
    isActive: product.is_active ?? true,
    images: []
  }
  priceDisplay.value = formatPriceDisplay(product.base_price)
  formErrors.value = { name: '', price: '', sku: '', images: '' }
  deletedPhotoIds.value = []
  formError.value = null
  modalMode.value = 'edit'
  showDrawer.value = true

  try {
    const res = await api.get(`/api/v1/product-photos/product/${product.id}`)
    const photos = res.data
    if (photos && photos.length > 0) {
      // Deduplicate: kalau ada foto dengan URL yang sama, ambil yang punya id terkecil saja
      const seen = new Map() // url → foto pertama
      for (const p of photos) {
        if (!seen.has(p.url)) {
          seen.set(p.url, p)
        }
        // foto duplikat (URL sama) akan diabaikan — akan dihapus saat save
      }
      const uniquePhotos = Array.from(seen.values())

      // Tandai duplikat untuk dihapus saat save
      const uniqueIds = new Set(uniquePhotos.map(p => p.id))
      for (const p of photos) {
        if (!uniqueIds.has(p.id)) {
          deletedPhotoIds.value.push(p.id)
        }
      }

      let primarySet = false
      form.value.images = uniquePhotos.map(p => {
        const rawPrimary = p.isPrimary ?? p.is_primary ?? false
        const isPrimary = rawPrimary && !primarySet
        if (isPrimary) primarySet = true
        return {
          id: p.id,
          url: p.url,
          file: null,
          preview: p.url,
          isPrimary,
          isExisting: true
        }
      })
      if (!primarySet && form.value.images.length > 0) {
        form.value.images[0].isPrimary = true
      }
    } else if (product.imageUrl) {
      // Tidak ada record di product_photos tapi ada imageUrl —
      // tampilkan tapi id = null agar saat save dibuat record baru
      form.value.images = [{
        id: null,
        url: product.imageUrl,
        file: null,
        preview: product.imageUrl,
        isPrimary: true,
        isExisting: false
      }]
    }
  } catch (err) {
    console.error('Gagal mengambil foto produk:', err)
    if (product.imageUrl) {
      form.value.images = [{
        id: null,
        url: product.imageUrl,
        file: null,
        preview: product.imageUrl,
        isPrimary: true,
        isExisting: false
      }]
    }
  }
  
  if (form.value.images.length > 0 && !form.value.images.some(img => img.isPrimary)) {
    form.value.images[0].isPrimary = true
  }
}

function closeDrawer() {
  showDrawer.value = false
}

function handleImagesUpload(event) {
  const files = Array.from(event.target.files)
  if (!files.length) return
  
  const currentCount = form.value.images.length
  if (currentCount + files.length > 8) {
    toast.error('Maksimal 8 gambar diperbolehkan.')
    return
  }
  
  // Cek apakah sudah ada primary sebelum loop
  const alreadyHasPrimary = form.value.images.some(img => img.isPrimary)
  let firstNew = true

  for (const file of files) {
    if (file.size > 2 * 1024 * 1024) {
      toast.error(`File ${file.name} melebihi batas 2MB.`)
      continue
    }
    
    const preview = URL.createObjectURL(file)
    // Gambar baru jadi primary hanya jika belum ada primary DAN ini yang pertama ditambahkan
    const shouldBePrimary = !alreadyHasPrimary && firstNew
    firstNew = false

    form.value.images.push({
      id: null,
      url: '',
      file: file,
      preview: preview,
      isPrimary: shouldBePrimary,
      isExisting: false
    })
  }
}

function removeImage(index) {
  const img = form.value.images[index]
  // Kalau punya id berarti sudah ada record di DB — tandai untuk dihapus
  if (img.id) {
    deletedPhotoIds.value.push(img.id)
  }
  form.value.images.splice(index, 1)
  
  if (img.isPrimary && form.value.images.length > 0) {
    form.value.images[0].isPrimary = true
  }
}

function setPrimaryImage(index) {
  form.value.images.forEach((img, i) => {
    img.isPrimary = i === index
  })
}

function generateSku() {
  if (!form.value.name || form.value.name.trim() === '') {
    toast.warning('Masukkan nama produk terlebih dahulu untuk generate SKU.')
    return
  }
  
  const cleanName = form.value.name.replace(/[^a-zA-Z]/g, '').toUpperCase()
  let prefix = 'PRO'
  if (cleanName.length >= 3) {
    prefix = cleanName.charAt(0) + cleanName.charAt(Math.floor(cleanName.length / 2)) + cleanName.charAt(cleanName.length - 1)
  } else if (cleanName.length > 0) {
    prefix = (cleanName + 'XXX').substring(0, 3)
  }
  
  const randomDigits = Math.floor(Math.random() * 900) + 100
  form.value.sku = `${prefix}-${randomDigits}`
  toast.info(`SKU berhasil dibuat: ${form.value.sku}`)
}

async function saveProduct() {
  formError.value = null
  if (!validateForm()) {
    const firstErrorField = document.querySelector('.form-error')
    if (firstErrorField) firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' })
    return
  }
  saving.value = true
  try {
    // ── Upload semua file baru ke storage terlebih dahulu ──
    await Promise.all(
      form.value.images.map(async (img) => {
        if (!img.id && img.file) {
          const formData = new FormData()
          formData.append('file', img.file)
          const uploadRes = await api.post('/api/v1/upload/product', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          })
          img.url = uploadRes.data.data.url
          img.file = null
        }
      })
    )

    const primaryImg = form.value.images.find(img => img.isPrimary) || form.value.images[0]
    const primaryUrl = primaryImg ? primaryImg.url : ''

    const payload = {
      name: form.value.name,
      sku: form.value.sku || undefined,
      base_price: parseFloat(form.value.price),
      category_id: form.value.categoryId || undefined,
      track_stock: form.value.trackStock,
      is_active: form.value.isActive,
      // Saat create: TIDAK kirim image_url agar backend tidak auto-buat ProductPhoto record
      // (semua foto diurus oleh loop di bawah secara konsisten)
      // Saat edit: kirim image_url agar flag isPrimary di DB ikut diupdate
      image_url: modalMode.value === 'edit' ? primaryUrl : undefined
    }
    
    let savedProduct;
    if (modalMode.value === 'create') {
      const res = await api.post('/api/v1/products', payload)
      savedProduct = res.data.data
    } else {
      const res = await api.put(`/api/v1/products/${form.value.id}`, payload)
      savedProduct = res.data.data
    }
    
    const productId = savedProduct.id

    // ── Hapus foto yang ditandai untuk dihapus (mode edit) ──
    if (modalMode.value === 'edit' && deletedPhotoIds.value.length > 0) {
      await Promise.all(
        deletedPhotoIds.value.map(id => api.delete(`/api/v1/product-photos/${id}`))
      )
      deletedPhotoIds.value = []
    }

    // ── Sinkronisasi foto ke DB ──
    // img.id ada   → record sudah ada di DB → PUT (update isPrimary/sortOrder)
    // img.id null  → record belum ada di DB → POST (buat baru)
    // img.url kosong → skip (file gagal upload atau tidak valid)
    console.log('[DEBUG] images to sync:', form.value.images.map(img => ({ id: img.id, url: img.url?.slice(-20), isPrimary: img.isPrimary })))
    for (let i = 0; i < form.value.images.length; i++) {
      const img = form.value.images[i]
      if (!img.url) continue  // skip gambar yang tidak punya URL

      if (img.id) {
        // Foto lama — update metadata saja
        console.log(`[DEBUG] PUT photo id=${img.id}`)
        await api.put(`/api/v1/product-photos/${img.id}`, {
          product_id: productId,
          url: img.url,
          is_primary: img.isPrimary,
          sort_order: i
        })
      } else {
        // Foto baru — buat record di DB
        console.log(`[DEBUG] POST new photo url=${img.url?.slice(-20)}`)
        await api.post('/api/v1/product-photos', {
          product_id: productId,
          url: img.url,
          is_primary: img.isPrimary,
          sort_order: i
        })
      }
    }

    toast.success(modalMode.value === 'create' ? 'Produk berhasil ditambahkan!' : 'Produk berhasil diperbarui!')
    showDrawer.value = false
    fetchProducts(pagination.value.page)
  } catch (err) {
    formError.value = err.response?.data?.data?.message || err.response?.data?.message || 'Gagal menyimpan produk.'
  } finally {
    saving.value = false
  }
}

function validateForm() {
  formErrors.value = { name: '', price: '', sku: '', images: '' }
  let isValid = true
  
  if (!form.value.name || form.value.name.trim() === '') {
    formErrors.value.name = 'Nama produk wajib diisi'
    isValid = false
  } else if (form.value.name.length < 3) {
    formErrors.value.name = 'Nama produk minimal 3 karakter'
    isValid = false
  } else if (form.value.name.length > 100) {
    formErrors.value.name = 'Nama produk maksimal 100 karakter'
    isValid = false
  }
  
  const price = parseFloat(form.value.price)
  if (form.value.price === '' || form.value.price === null || isNaN(price)) {
    formErrors.value.price = 'Harga wajib diisi'
    isValid = false
  } else if (price < 0) {
    formErrors.value.price = 'Harga tidak boleh negatif'
    isValid = false
  } else if (price === 0) {
    formErrors.value.price = 'Harga harus lebih dari 0'
    isValid = false
  }
  
  if (form.value.sku && form.value.sku.length > 50) {
    formErrors.value.sku = 'SKU maksimal 50 karakter'
    isValid = false
  }
  
  if (form.value.images.length < 2) {
    formErrors.value.images = 'Minimal memasukkan 2 gambar produk'
    isValid = false
  } else if (form.value.images.length > 8) {
    formErrors.value.images = 'Maksimal memasukkan 8 gambar produk'
    isValid = false
  }
  
  return isValid
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
    toast.error(err.response?.data?.data?.message || err.response?.data?.message || 'Gagal menghapus produk.')
  } finally {
    deleting.value = false
  }
}


async function toggleStatus(product) {
  // Cek cooldown global
  const now = Date.now()
  if (now - lastToggleTime.value < TOGGLE_COOLDOWN_MS) {
    toast.warning('Tunggu sebentar sebelum mengganti status lagi')
    return
  }
  
  if (!can('produk.update')) {
    toast.error('Anda tidak memiliki izin untuk mengubah status produk.')
    return
  }
  
  if (togglingStatus.value === product.id) return
  
  // Set cooldown
  lastToggleTime.value = now
  
  togglingStatus.value = product.id
  const originalStatus = product.is_active
  product.is_active = !product.is_active // Optimistic update
  
  console.log('DEBUG: Toggling status for product', product.id, 'to', product.is_active)
  try {
    await api.patch(`/api/v1/products/${product.id}`, {
      is_active: product.is_active
    })
    toast.success(`Status ${product.name} berhasil diperbarui menjadi ${product.is_active ? 'Aktif' : 'Nonaktif'}.`)
  } catch (err) {
    product.is_active = originalStatus // Rollback
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
      <Transition name="fade" mode="out-in">
        <!-- ─── Main Table View ─── -->
        <div v-if="!showDrawer" key="table-view" class="flex flex-col gap-6">
          <!-- Page Header -->
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
                <!-- Filter Dropdown -->
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
                      class="absolute left-0 sm:left-auto sm:right-0 top-full mt-1 z-30 w-[calc(100vw-2.5rem)] sm:w-64 max-w-[280px] sm:max-w-none bg-card border border-border rounded-lg shadow-xl overflow-hidden"
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
                          <button
                            v-for="sortOption in [{val: 'terbaru', label: 'Terbaru'}, {val: 'harga-termahal', label: 'Harga Tertinggi'}, {val: 'harga-termurah', label: 'Harga Terendah'}]"
                            :key="sortOption.val"
                            @click="sortBy = sortOption.val"
                            class="w-full flex items-center justify-between px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors outline-none"
                          >
                            <span class="text-sm font-medium text-foreground select-none">{{ sortOption.label }}</span>
                            <Check v-if="sortBy === sortOption.val" class="h-4 w-4 text-foreground" />
                          </button>
                        </div>
                      </div>

                      <div class="mx-3 border-t border-border" />

                      <!-- Status Section -->
                      <div class="px-3 pt-2 pb-2">
                        <p class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2">Status</p>
                        <div class="space-y-1">
                          <button
                            v-for="statusOption in [{val: 'all', label: 'Semua Status'}, {val: 'aktif', label: 'Aktif'}, {val: 'nonaktif', label: 'Nonaktif'}]"
                            :key="statusOption.val"
                            @click="filterStatus = statusOption.val"
                            class="w-full flex items-center justify-between px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors outline-none"
                          >
                            <span class="text-sm font-medium text-foreground select-none">{{ statusOption.label }}</span>
                            <Check v-if="filterStatus === statusOption.val" class="h-4 w-4 text-foreground" />
                          </button>
                        </div>
                      </div>

                      <div class="mx-3 border-t border-border" />

                      <!-- Stok Section -->
                      <div class="px-3 pt-2 pb-3">
                        <p class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2">Stok</p>
                        <div class="space-y-1">
                          <button
                            v-for="stockOption in [{val: 'all', label: 'Semua Stok'}, {val: 'dilacak', label: 'Dilacak'}, {val: 'bebas', label: 'Bebas'}]"
                            :key="stockOption.val"
                            @click="filterStock = stockOption.val"
                            class="w-full flex items-center justify-between px-2 py-1.5 rounded-md hover:bg-muted/50 cursor-pointer transition-colors outline-none"
                          >
                            <span class="text-sm font-medium text-foreground select-none">{{ stockOption.label }}</span>
                            <Check v-if="filterStock === stockOption.val" class="h-4 w-4 text-foreground" />
                          </button>
                        </div>
                      </div>
                    </div>
                  </Transition>
                </div>
                <!-- /Filter Dropdown -->

                <Button v-if="can('produk.store') && !isSuperAdmin" @click="openCreate" size="sm" class="flex-1 sm:flex-none flex items-center justify-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground">
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
          <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
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
                <Button v-if="can('produk.store') && !isSuperAdmin && !searchQuery" size="sm" class="mt-4" @click="openCreate">
                  <Plus class="h-3.5 w-3.5 mr-1.5" />
                  Tambah Produk
                </Button>
              </div>

              <div v-else>
                <!-- Mobile List View -->
                <div class="md:hidden flex flex-col divide-y divide-zinc-100 dark:divide-zinc-800/60">
                  <div
                    v-for="product in filteredProducts"
                    :key="'mobile-' + product.id"
                    class="p-4 flex flex-col gap-3 hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40 transition-colors"
                  >
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
                          v-if="can('produk.update') && !isSuperAdmin"
                          variant="ghost"
                          size="icon"
                          class="h-8 w-8 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 bg-zinc-50 dark:bg-zinc-800/50"
                          @click="openEdit(product)"
                        >
                          <Pencil class="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          v-if="can('produk.delete') && !isSuperAdmin"
                          variant="ghost"
                          size="icon"
                          class="h-8 w-8 text-zinc-400 hover:text-destructive bg-zinc-50 dark:bg-zinc-800/50"
                          @click="doDelete(product)"
                        >
                          <Trash2 class="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </div>

                    <div class="flex items-center justify-between mt-1">
                      <span class="text-sm font-semibold text-zinc-800 dark:text-zinc-200">{{ formatCurrency(product.base_price) }}</span>
                      <div class="flex items-center gap-2">
                        <span
                          v-if="product.category_id?.name"
                          class="text-[10px] font-medium px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40"
                        >
                          {{ product.category_id.name }}
                        </span>
                        <button
                          type="button"
                          role="switch"
                          :aria-checked="product.is_active"
                          :disabled="togglingStatus === product.id || isToggleOnCooldown"
                          class="relative inline-flex h-5 w-9 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                          :class="product.is_active ? 'bg-primary' : 'bg-zinc-200 dark:bg-zinc-700'"
                          @click.stop="toggleStatus(product)"
                        >
                        <span
                            class="pointer-events-none h-4 w-4 rounded-full bg-background shadow-lg ring-0 transition-transform flex items-center justify-center"
                            :class="product.is_active ? 'translate-x-4' : 'translate-x-0'"
                        >
                            <Loader2 v-if="togglingStatus === product.id" class="h-2.5 w-2.5 animate-spin text-primary" />
                        </span>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Desktop Table -->
                <div class="hidden md:block overflow-x-auto relative">
                  <!-- Selection Banner -->
                  <Transition name="fade">
                    <div v-if="selectedIds.length > 0" class="flex items-center justify-between px-5 py-3 bg-primary/5 dark:bg-primary/10 border-b border-border transition-all duration-200">
                      <div class="flex items-center gap-2">
                        <span class="text-xs font-semibold text-primary px-2 py-0.5 rounded bg-primary/10">
                          {{ selectedIds.length }} Terpilih
                        </span>
                        <span class="text-xs text-muted-foreground">Baris terpilih dalam tabel ini.</span>
                      </div>
                      <div class="flex items-center gap-2">
                        <Button
                          v-if="can('produk.update') && !isSuperAdmin"
                          size="sm"
                          variant="outline"
                          class="h-8 text-xs gap-1 border-primary/20 hover:bg-primary/10"
                          @click="bulkDeactivate"
                        >
                          <PowerOff class="h-3.5 w-3.5" />
                          Nonaktifkan
                        </Button>
                        <Button
                          v-if="can('produk.delete') && !isSuperAdmin"
                          size="sm"
                          variant="destructive"
                          class="h-8 text-xs gap-1"
                          @click="bulkDelete"
                        >
                          <Trash2 class="h-3.5 w-3.5" />
                          Hapus
                        </Button>
                      </div>
                    </div>
                  </Transition>

                  <Table>
                    <TableHeader>
                      <TableRow class="hover:bg-transparent border-b border-zinc-100 dark:border-zinc-800">
                        <TableHead class="w-12 pl-5 py-3">
                          <input
                            type="checkbox"
                            class="rounded border-zinc-300 dark:border-zinc-700 text-primary focus:ring-primary h-4 w-4 cursor-pointer"
                            :checked="isAllSelected"
                            @change="toggleSelectAll"
                          />
                        </TableHead>
                        <TableHead class="pl-2 py-3 text-xs font-semibold uppercase tracking-wide text-zinc-500 w-[260px]">Produk</TableHead>
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
                        class="group table-lift-row border-b border-zinc-100 dark:border-zinc-800/60 odd:bg-background even:bg-zinc-50/40 dark:even:bg-zinc-900/10 hover:bg-zinc-100/60 dark:hover:bg-zinc-800/40 transition-colors cursor-pointer"
                        @click="can('produk.update') && !isSuperAdmin && openEdit(product)"
                      >
                        <TableCell class="w-12 pl-5 py-3" @click.stop>
                          <input
                            type="checkbox"
                            class="rounded border-zinc-300 dark:border-zinc-700 text-primary focus:ring-primary h-4 w-4 cursor-pointer"
                            :checked="selectedIds.includes(product.id)"
                            @change="toggleSelect(product.id)"
                          />
                        </TableCell>

                        <TableCell class="pl-2 py-3">
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

                        <TableCell class="py-3">
                          <span v-if="product.sku" class="font-mono text-xs bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 px-2 py-1 rounded">
                            {{ product.sku }}
                          </span>
                          <span v-else class="text-zinc-300 dark:text-zinc-600 text-sm">—</span>
                        </TableCell>

                        <TableCell class="py-3">
                          <span class="text-sm font-semibold text-zinc-800 dark:text-zinc-200">{{ formatCurrency(product.base_price) }}</span>
                        </TableCell>

                        <TableCell class="py-3">
                          <span
                            v-if="product.category_id?.name"
                            class="inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 border border-blue-100 dark:border-blue-800/40"
                          >
                            {{ product.category_id.name }}
                          </span>
                          <span v-else class="text-zinc-300 dark:text-zinc-600 text-sm">—</span>
                        </TableCell>

                        <TableCell class="py-3 text-center">
                          <span
                            class="inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full"
                            :class="product.track_stock
                              ? 'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-800/40'
                              : 'bg-zinc-100 dark:bg-zinc-800 text-zinc-500 border border-zinc-200 dark:border-zinc-700'"
                          >
                            <span class="w-1.5 h-1.5 rounded-full" :class="product.track_stock ? 'bg-emerald-500' : 'bg-zinc-400'" />
                            {{ product.track_stock ? 'Dilacak' : 'Bebas' }}
                          </span>
                        </TableCell>

                        <TableCell class="py-3 text-center" @click.stop>
                          <button
                            type="button"
                            role="switch"
                            :aria-checked="product.is_active"
                            :disabled="togglingStatus === product.id"
                            class="relative inline-flex h-5 w-9 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 mx-auto"
                            :class="product.is_active ? 'bg-primary' : 'bg-zinc-200 dark:bg-zinc-700'"
                            @click="toggleStatus(product)"
                          >
                              <span
                                class="pointer-events-none h-4 w-4 rounded-full bg-background shadow-lg ring-0 transition-transform flex items-center justify-center"
                                :class="product.is_active ? 'translate-x-4' : 'translate-x-0'"
                              >
                                <Loader2 v-if="togglingStatus === product.id" class="h-2.5 w-2.5 animate-spin text-primary" />
                              </span>
                          </button>
                        </TableCell>

                        <TableCell class="py-3">
                          <span class="text-xs text-zinc-400 dark:text-zinc-500">{{ formatDate(product.created_at) }}</span>
                        </TableCell>

                        <TableCell class="pr-4 py-3 text-right" @click.stop>
                          <div class="flex justify-end gap-1">
                            <Button
                              v-if="can('produk.delete') && !isSuperAdmin"
                              variant="ghost"
                              size="icon"
                              class="h-7 w-7 text-zinc-400 hover:text-destructive hover:bg-destructive/10 rounded-md opacity-0 group-hover:opacity-100 transition-opacity duration-200"
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

        <!-- ─── Premium Inline Form View ─── -->
        <div v-else key="form-view" class="flex flex-col gap-6">
          <!-- Form Header -->
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-zinc-200 dark:border-zinc-800 pb-4">
            <div class="flex items-center gap-3">
              <Button 
                variant="outline" 
                size="icon" 
                @click="closeDrawer" 
                :disabled="saving"
                class="h-9 w-9 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors"
              >
                <ArrowLeft class="h-4 w-4" />
              </Button>
              <div>
                <h2 class="text-xl font-bold tracking-tight flex items-center gap-2">
                  <span>{{ modalMode === 'create' ? 'Tambah Profil Produk' : 'Edit Produk' }}</span>
                  <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-primary/10 text-primary border border-primary/20 uppercase tracking-wider">
                    {{ modalMode === 'create' ? 'Baru' : 'Modifikasi' }}
                  </span>
                </h2>
                <p class="text-xs text-muted-foreground mt-0.5">
                  {{ modalMode === 'create' ? 'Lengkapi detail untuk menambahkan produk baru.' : 'Perbarui informasi dan media produk Anda.' }}
                </p>
              </div>
            </div>
            <div class="flex items-center gap-3 w-full sm:w-auto">
              <Button variant="outline" @click="closeDrawer" :disabled="saving" class="flex-1 sm:flex-none">Batal</Button>
              <Button @click="saveProduct" :disabled="saving" class="flex-1 sm:flex-none bg-primary text-primary-foreground hover:bg-primary/95 shadow-md shadow-primary/20">
                <Loader2 v-if="saving" class="h-4 w-4 mr-2 animate-spin" />
                <span>{{ modalMode === 'create' ? 'Simpan Produk' : 'Perbarui' }}</span>
              </Button>
            </div>
          </div>

          <!-- Alert for Form Errors -->
          <Alert v-if="formError" variant="destructive" class="mb-4">
            <p class="text-sm font-medium">{{ formError }}</p>
          </Alert>

          <!-- Form Grid Split Layout -->
          <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
            
            <!-- LEFT COLUMN: Product Images Gallery -->
            <div class="lg:col-span-5 space-y-4">
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <div class="flex items-center justify-between border-b pb-3 border-zinc-100 dark:border-zinc-800">
                    <div>
                      <Label class="text-sm font-semibold text-zinc-950 dark:text-zinc-50">Media Galeri Produk</Label>
                      <p class="text-[10px] text-muted-foreground mt-0.5">Harus berisi 2 s.d 8 gambar produk.</p>
                    </div>
                    <span 
                      class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold"
                      :class="form.images.length >= 2 && form.images.length <= 8 
                        ? 'text-emerald-700 bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-200' 
                        : 'text-amber-700 bg-amber-50 dark:bg-amber-950/20 border border-amber-200'"
                    >
                      {{ form.images.length }} / 8 Gambar
                    </span>
                  </div>

                  <!-- Upload Dropzone if total images < 8 -->
                  <div 
                    v-if="form.images.length < 8"
                    class="group relative border-2 border-dashed border-zinc-200 dark:border-zinc-800 hover:border-primary/50 dark:hover:border-primary/50 rounded-xl bg-zinc-50/50 dark:bg-zinc-900/30 p-6 flex flex-col items-center justify-center text-center cursor-pointer transition-all duration-200"
                  >
                    <input
                      type="file"
                      multiple
                      accept="image/*"
                      class="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10"
                      @change="handleImagesUpload"
                      :disabled="saving"
                    />
                    <div class="w-12 h-12 rounded-full bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center mb-3 group-hover:scale-105 transition-transform duration-200">
                      <Upload class="h-5 w-5 text-zinc-400 dark:text-zinc-500 group-hover:text-primary transition-colors" />
                    </div>
                    <p class="text-xs font-semibold text-zinc-700 dark:text-zinc-300">Pilih atau Drag Foto</p>
                    <p class="text-[10px] text-muted-foreground mt-1">Format: JPG, PNG. Maks 2MB per file.</p>
                  </div>

                  <!-- Images Grid -->
                  <div v-if="form.images.length > 0" class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-2 gap-3 mt-4">
                    <div 
                      v-for="(img, idx) in form.images" 
                      :key="'img-' + idx"
                      class="group/item relative aspect-square rounded-xl overflow-hidden border border-zinc-200 dark:border-zinc-800 bg-zinc-100 dark:bg-zinc-900 shadow-sm"
                    >
                      <img :src="img.preview" alt="Preview" class="h-full w-full object-cover transition-transform duration-300 group-hover/item:scale-105" />
                      
                      <!-- Overlay with buttons -->
                      <div class="absolute inset-0 bg-black/60 opacity-0 group-hover/item:opacity-100 transition-opacity duration-200 flex flex-col items-center justify-between p-2">
                        
                        <!-- Top buttons (Set Primary) -->
                        <div class="w-full flex justify-end">
                          <button
                            type="button"
                            @click="setPrimaryImage(idx)"
                            class="p-1 rounded-md text-white hover:bg-white/20 transition-colors"
                            :title="img.isPrimary ? 'Gambar Utama' : 'Jadikan Gambar Utama'"
                          >
                            <Star class="h-4.5 w-4.5" :class="img.isPrimary ? 'fill-yellow-400 text-yellow-400' : 'text-white'" />
                          </button>
                        </div>
                        
                        <!-- Bottom button (Delete) -->
                        <div class="w-full flex justify-between items-center text-white">
                          <span class="text-[10px] bg-black/40 px-2 py-0.5 rounded font-medium select-none">
                            #{{ idx + 1 }}
                          </span>
                          <button
                            type="button"
                            @click="removeImage(idx)"
                            class="p-1 rounded-md text-red-400 hover:bg-red-500/20 transition-colors"
                            title="Hapus"
                          >
                            <Trash2 class="h-4.5 w-4.5" />
                          </button>
                        </div>
                      </div>

                      <!-- Primary Badge Indicator (persistent) -->
                      <div v-if="img.isPrimary" class="absolute top-2 left-2 bg-yellow-500 text-black text-[9px] font-bold px-1.5 py-0.5 rounded shadow flex items-center gap-1 select-none">
                        <Star class="h-3 w-3 fill-black text-black" />
                        <span>UTAMA</span>
                      </div>
                    </div>
                  </div>
                  
                  <div v-else class="flex flex-col items-center justify-center py-10 text-muted-foreground border border-dashed rounded-xl">
                    <Image class="h-8 w-8 opacity-40 mb-2" />
                    <p class="text-xs">Belum ada gambar terpilih.</p>
                  </div>

                  <p v-if="formErrors.images" class="text-xs text-destructive form-error text-center mt-2 font-semibold">{{ formErrors.images }}</p>
                </CardContent>
              </Card>
            </div>

            <!-- RIGHT COLUMN: Product Information Cards -->
            <div class="lg:col-span-7 space-y-6">
              
              <!-- Card 1: Informasi Utama -->
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <h3 class="text-sm font-semibold text-zinc-950 dark:text-zinc-50 border-b pb-2 border-zinc-100 dark:border-zinc-800 flex items-center gap-2">
                    <Package class="h-4 w-4 text-primary" />
                    <span>Informasi Utama</span>
                  </h3>

                  <div class="space-y-1.5">
                    <Label for="f-name" class="text-xs font-semibold">Nama Produk <span class="text-destructive">*</span></Label>
                    <Input 
                      id="f-name" 
                      v-model="form.name" 
                      placeholder="Contoh: Kaos Polos Putih Premium" 
                      :disabled="saving"
                      class="h-10 rounded-lg"
                      :class="{ 'border-destructive ring-destructive/20': formErrors.name }"
                    />
                    <p v-if="formErrors.name" class="text-xs text-destructive form-error font-semibold">{{ formErrors.name }}</p>
                  </div>

                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div class="space-y-1.5">
                      <Label for="f-price" class="text-xs font-semibold">Harga Jual <span class="text-destructive">*</span></Label>
                      <div class="relative">
                        <span class="absolute left-3 top-1/2 -translate-y-1/2 text-sm text-muted-foreground select-none font-medium">Rp</span>
                        <input
                          id="f-price"
                          type="text"
                          inputmode="numeric"
                          :value="priceDisplay"
                          @input="onPriceInput"
                          placeholder="0"
                          :disabled="saving"
                          class="flex h-10 w-full rounded-lg border border-input bg-background pl-9 pr-3 py-2 text-sm font-semibold text-zinc-800 dark:text-zinc-200 placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"
                          :class="{ 'border-destructive ring-destructive/20 focus-visible:ring-destructive/30': formErrors.price }"
                        />
                      </div>
                      <p v-if="formErrors.price" class="text-xs text-destructive form-error font-semibold">{{ formErrors.price }}</p>
                    </div>

                    <div class="space-y-1.5">
                      <Label for="f-category" class="text-xs font-semibold">Kategori <span class="text-muted-foreground text-[10px] font-normal">(opsional)</span></Label>
                      <select
                        id="f-category"
                        v-model="form.categoryId"
                        :disabled="saving"
                        class="flex h-10 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"
                      >
                        <option :value="null">— Pilih kategori —</option>
                        <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                      </select>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <!-- Card 2: SKU & Status -->
              <Card class="border-zinc-200 dark:border-zinc-800 shadow-sm">
                <CardContent class="p-5 space-y-4">
                  <h3 class="text-sm font-semibold text-zinc-950 dark:text-zinc-50 border-b pb-2 border-zinc-100 dark:border-zinc-800 flex items-center gap-2">
                    <Sparkles class="h-4 w-4 text-primary" />
                    <span>SKU & Pengaturan Tambahan</span>
                  </h3>

                  <div class="space-y-1.5">
                    <Label for="f-sku" class="text-xs font-semibold">SKU (Stock Keeping Unit) <span class="text-muted-foreground text-[10px] font-normal">(opsional)</span></Label>
                    <div class="relative flex items-center">
                      <Input 
                        id="f-sku" 
                        v-model="form.sku" 
                        placeholder="Contoh: KPP-001" 
                        :disabled="saving"
                        class="h-10 rounded-lg pr-28 uppercase font-mono tracking-wider"
                        :class="{ 'border-destructive ring-destructive/20': formErrors.sku }"
                      />
                      <button 
                        type="button"
                        @click="generateSku"
                        class="absolute right-1.5 px-3 py-1.5 h-7.5 bg-zinc-100 hover:bg-zinc-200 dark:bg-zinc-800 dark:hover:bg-zinc-700/80 text-[10px] font-bold text-zinc-800 dark:text-zinc-200 rounded-md flex items-center gap-1.5 transition-colors select-none"
                      >
                        <Sparkles class="h-3.5 w-3.5 text-primary animate-pulse" />
                        <span>GENERATE</span>
                      </button>
                    </div>
                    <p v-if="formErrors.sku" class="text-xs text-destructive form-error font-semibold">{{ formErrors.sku }}</p>
                  </div>

                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
                    <!-- Toggle Lacak Stok -->
                    <div class="flex items-center justify-between rounded-xl border p-4 border-zinc-150 bg-zinc-50/50 dark:bg-zinc-900/30">
                      <div class="space-y-0.5">
                        <Label class="text-xs font-semibold cursor-pointer" for="f-track-stock">Lacak Stok Toko</Label>
                        <p class="text-[10px] text-muted-foreground">Aktifkan untuk melacak stok produk fisik.</p>
                      </div>
                      <button
                        id="f-track-stock"
                        type="button"
                        role="switch"
                        :aria-checked="form.trackStock"
                        :disabled="saving"
                        class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                        :class="form.trackStock ? 'bg-primary' : 'bg-zinc-200 dark:bg-zinc-700'"
                        @click="form.trackStock = !form.trackStock"
                      >
                        <span
                          class="pointer-events-none block h-5 w-5 rounded-full bg-background shadow-lg ring-0 transition-transform"
                          :class="form.trackStock ? 'translate-x-5' : 'translate-x-0'"
                        />
                      </button>
                    </div>

                    <!-- Toggle Status Aktif -->
                    <div class="flex items-center justify-between rounded-xl border p-4 border-zinc-150 bg-zinc-50/50 dark:bg-zinc-900/30">
                      <div class="space-y-0.5">
                        <Label class="text-xs font-semibold cursor-pointer" for="f-is-active">Status Produk Aktif</Label>
                        <p class="text-[10px] text-muted-foreground">Aktifkan agar produk dapat dijual.</p>
                      </div>
                      <button
                        id="f-is-active"
                        type="button"
                        role="switch"
                        :aria-checked="form.isActive"
                        :disabled="saving"
                        class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                        :class="form.isActive ? 'bg-primary' : 'bg-zinc-200 dark:bg-zinc-700'"
                        @click="form.isActive = !form.isActive"
                      >
                        <span
                          class="pointer-events-none block h-5 w-5 rounded-full bg-background shadow-lg ring-0 transition-transform"
                          :class="form.isActive ? 'translate-x-5' : 'translate-x-0'"
                        />
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

    <!-- ─── Delete Confirmation Modal ─── -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[60] bg-black/40 backdrop-blur-sm" @click="closeDeleteModal"></div>
      </Transition>
      
      <Transition name="scale">
        <div v-if="deleteModal.show" class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-auto">
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
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.scale-enter-active,
.scale-leave-active {
  transition: opacity 0.25s ease, transform 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}
.scale-enter-from,
.scale-leave-to {
  opacity: 0;
  transform: scale(0.96);
}

.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-right-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
}
</style>