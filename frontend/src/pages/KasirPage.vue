<script setup>
import { ref, computed, onMounted, watch, onBeforeUnmount } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'
import { 
  Search, ShoppingCart, Plus, Minus, Trash2, ShoppingBag,
  Banknote, ArrowRightLeft, X, Ticket, Check, Scan,
  Loader2, Building2, Printer, ReceiptText, ChevronDown, MapPin, Tag, Percent, AlertTriangle,
  Clock, Pause, Play, LogIn, LogOut, UserCheck, MessageSquare, Star, Target, TrendingUp,
  Scissors
} from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'
import api from '@/lib/api'
import StrukPrint from '@/components/StrukPrint.vue'
import ShiftModal from '@/components/ShiftModal.vue'

const { toast } = useToast()
const authStore = useAuthStore()

// ─── State ───────────────────────────────────────────────────────────────────
const products = ref([])
const categories = ref([])
const vouchers = ref([])
const branches = ref([])
const stockBalances = ref({})
const selectedBranchId = ref(null)
const showBranchDropdown = ref(false)
const branchDropdownRef = ref(null)

const selectedBranch = computed(() => branches.value.find(b => b.id === selectedBranchId.value))

function selectBranch(branch) {
  selectedBranchId.value = branch.id
  showBranchDropdown.value = false
}

function handleBranchOutsideClick(e) {
  if (branchDropdownRef.value && !branchDropdownRef.value.contains(e.target)) {
    showBranchDropdown.value = false
  }
}
const loading = ref(false)
const processingCheckout = ref(false)
const lastOrder = ref(null)
const showReceipt = ref(false)
const struk = ref(null)

const searchQuery = ref('')
const activeCategory = ref('Semua')
const cart = ref([])

// ─── Produk Favorit ──────────────────────────────────────────────────────────
const topProducts = ref([])
const loadingTop = ref(false)
const showFavorites = ref(false)

async function fetchTopProducts() {
  loadingTop.value = true
  try {
    const res = await api.get('/api/v1/orders')
    const data = res.data?.data ?? res.data
    const all = Array.isArray(data) ? data : (data?.content || [])
    const freq = {}
    all.filter(o => o.status?.toUpperCase() === 'PAID').forEach(o => {
      (o.items || []).forEach(i => {
        const key = i.productId || i.product?.id
        if (!key) return
        if (!freq[key]) freq[key] = { id: key, name: i.productName, count: 0 }
        freq[key].count += (i.qty || 1)
      })
    })
    topProducts.value = Object.values(freq)
      .sort((a, b) => b.count - a.count)
      .slice(0, 8)
  } catch {
    topProducts.value = []
  } finally {
    loadingTop.value = false
  }
}

// ─── Sales Target ─────────────────────────────────────────────────────────────
const salesTarget = ref(Number(localStorage.getItem('pos_sales_target') || '0'))
const showTargetEdit = ref(false)
const targetInput = ref('')
const currentMonthRevenue = ref(0)

const targetProgress = computed(() => {
  if (!salesTarget.value || salesTarget.value <= 0) return 0
  return Math.min(100, Math.round((currentMonthRevenue.value / salesTarget.value) * 100))
})

async function fetchMonthRevenue() {
  try {
    const res = await api.get('/api/v1/orders')
    const data = res.data?.data ?? res.data
    const all = Array.isArray(data) ? data : (data?.content || [])
    const now = new Date()
    const revenue = all
      .filter(o => {
        if (o.status?.toUpperCase() !== 'PAID') return false
        const d = o.createdAt ? new Date(o.createdAt) : null
        return d && d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
      })
      .reduce((s, o) => s + (Number(o.total) || 0), 0)
    currentMonthRevenue.value = revenue
  } catch { /* silent */ }
}

function saveTarget() {
  const v = Number(targetInput.value.replace(/\D/g, ''))
  salesTarget.value = v
  localStorage.setItem('pos_sales_target', String(v))
  showTargetEdit.value = false
  targetInput.value = ''
}

// ─── Note per item ───────────────────────────────────────────────────────────
const openItemNoteId = ref(null)

function toggleItemNote(item) {
  openItemNoteId.value = openItemNoteId.value === item._uid ? null : item._uid
}

// ─── Shift State ─────────────────────────────────────────────────────────────
const activeShift = ref(null)
const loadingShift = ref(false)
const showShiftModal = ref(false)
const shiftModalMode = ref('open')
const closedShiftResult = ref(null)

async function fetchActiveShift() {
  if (!selectedBranchId.value) return
  loadingShift.value = true
  try {
    const res = await api.get(`/api/v1/shifts/active?branchId=${selectedBranchId.value}`)
    activeShift.value = res.data?.data || null
  } catch {
    activeShift.value = null
  } finally {
    loadingShift.value = false
  }
}

function openShiftModal(mode) {
  shiftModalMode.value = mode
  showShiftModal.value = true
}

async function handleShiftSubmitted(form) {
  if (!selectedBranchId.value) { toast.error('Pilih cabang terlebih dahulu.'); return }
  loadingShift.value = true
  try {
    if (shiftModalMode.value === 'open') {
      const res = await api.post('/api/v1/shifts/open', {
        branchId: selectedBranchId.value,
        startingCash: form.startingCash ?? 0,
        notes: form.notes || null,
      })
      activeShift.value = res.data?.data
      toast.success('Shift berhasil dibuka!')
      showShiftModal.value = false
    } else {
      const res = await api.patch(`/api/v1/shifts/${activeShift.value.id}/close`, {
        closingNotes: form.closingNotes || null,
      })
      const d = res.data?.data
      activeShift.value = null
      closedShiftResult.value = d
      toast.success('Shift ditutup. Omzet berhasil dihitung!')
    }
  } catch (err) {
    const msg = err.response?.data?.data?.message || err.response?.data?.message || err.message || 'Gagal proses shift.'
    toast.error(msg)
  } finally {
    loadingShift.value = false
  }
}

// ─── Data Fetching ────────────────────────────────────────────────────────────
async function fetchData() {
  loading.value = true
  try {
    const isAdmin = authStore.isAdmin
    const userBranchId = authStore.user?.branchId
    const shouldFetchBranches = isAdmin || !userBranchId

    const requests = [
      api.get('/api/v1/products?page=0&size=500'),
      api.get('/api/v1/categories'),
      shouldFetchBranches
        ? api.get(isAdmin ? '/api/v1/branches/admin' : '/api/v1/branches')
        : Promise.resolve(null),
    ]

    const [resP, resC, resB] = await Promise.all(requests)

    const pData = resP.data?.data
    const pArr = pData?.content ? pData.content : (Array.isArray(pData) ? pData : [])
    products.value = pArr.filter(p => p.is_active !== false)

    const cData = resC.data?.data
    categories.value = Array.isArray(cData) ? cData : (cData?.content || [])

    if (userBranchId) {
      branches.value = [{ id: userBranchId, name: authStore.user?.branchName || 'Cabang Aktif' }]
      selectedBranchId.value = userBranchId
      await fetchStockBalances(userBranchId)
    } else {
      const bData = resB?.data?.data
      const bArr = Array.isArray(bData) ? bData : (bData?.content || [])
      const seen = new Set()
      branches.value = bArr.filter(b => {
        if (seen.has(b.name)) return false
        seen.add(b.name)
        return true
      })
      if (branches.value.length > 0) {
        selectedBranchId.value = branches.value[0].id
        await fetchStockBalances(branches.value[0].id)
      }
    }
    
  } catch (err) {
    console.error('[POS] fetchData error:', err.response?.data || err.message)
    toast.error('Gagal memuat data POS')
  } finally {
    loading.value = false
  }

  try {
    const resV = await api.get('/api/v1/vouchers')
    vouchers.value = Array.isArray(resV.data) ? resV.data : (resV.data?.data || [])
  } catch {
    vouchers.value = []
  }
}

onMounted(() => {
  fetchData()
  fetchTodayOrders()
  fetchTopProducts()
  fetchMonthRevenue()
  document.addEventListener('click', handleBranchOutsideClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleBranchOutsideClick)
})

watch(selectedBranchId, (newId) => {
  if (newId) {
    fetchStockBalances(newId)
    fetchActiveShift()
    showStockWarning.value = true
  }
})

async function fetchStockBalances(branchId) {
  try {
    const res = await api.get(`/api/v1/stock-balances/branch/${branchId}`)
    const list = res.data?.data || []
    const map = {}
    list.forEach(sb => {
      if (sb.product?.id) map[sb.product.id] = sb.qty ?? 0
    })
    stockBalances.value = map
  } catch {
    stockBalances.value = {}
  }
}

function getStock(productId) {
  return stockBalances.value[String(productId)] ?? null
}

const LOW_STOCK_THRESHOLD = 5

const lowStockProducts = computed(() => {
  return products.value.filter(p => {
    const stock = getStock(p.id)
    return stock !== null && stock > 0 && stock <= LOW_STOCK_THRESHOLD
  })
})

const outOfStockProducts = computed(() => {
  return products.value.filter(p => {
    const stock = getStock(p.id)
    return stock !== null && stock <= 0
  })
})

const showStockWarning = ref(true)

const showTodayPanel = ref(false)
const todayOrders = ref([])
const loadingToday = ref(false)

async function fetchTodayOrders() {
  loadingToday.value = true
  try {
    const res = await api.get('/api/v1/orders')
    const data = res.data?.data ?? res.data
    const all = Array.isArray(data) ? data : (data?.content || [])
    const today = new Date().toDateString()
    todayOrders.value = all.filter(o => {
      const d = o.createdAt ? new Date(o.createdAt).toDateString() : null
      return d === today
    }).sort((a, b) => (b.id || 0) - (a.id || 0))
  } catch {
    todayOrders.value = []
  } finally {
    loadingToday.value = false
  }
}

const todaySummary = computed(() => {
  const paid = todayOrders.value.filter(o => o.status?.toUpperCase() === 'PAID')
  const total = paid.reduce((s, o) => s + (Number(o.total) || 0), 0)
  const cash = paid.filter(o => o.payments?.some(p => p.method === 'CASH')).length
  const transfer = paid.filter(o => o.payments?.some(p => p.method === 'TRANSFER')).length
  return { count: paid.length, total, cash, transfer }
})

function toggleTodayPanel() {
  showTodayPanel.value = !showTodayPanel.value
  if (showTodayPanel.value) fetchTodayOrders()
}

const uniqueCategories = computed(() => {
  const names = categories.value.map(c => c.name).filter(Boolean).sort()
  return ['Semua', ...new Set(names)]
})

const filteredProducts = computed(() => {
  let result = products.value
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(p => p.name.toLowerCase().includes(q) || (p.sku && p.sku.toLowerCase().includes(q)))
  }
  if (activeCategory.value !== 'Semua') {
    result = result.filter(p => {
      const catName = p.category_id?.name || p.categoryId?.name || p.category?.name || ''
      return catName === activeCategory.value
    })
  }
  return result
})

// ─── Cart Logic ───────────────────────────────────────────────────────────────
let _uidCounter = Date.now()
function genUid() { return ++_uidCounter }

function addToCart(product) {
  const stock = getStock(product.id)
  // Hitung total qty semua baris produk yang sama
  const totalQty = cart.value.filter(i => i.id === product.id).reduce((s, i) => s + i.qty, 0)

  if (stock !== null && totalQty >= stock) {
    toast.error(`Stok ${product.name} tidak mencukupi. Tersisa: ${stock}`)
    return
  }

  // Tambahkan ke baris terakhir yang ada (baris pertama ditemukan)
  const existing = cart.value.find(item => item.id === product.id)
  if (existing) {
    existing.qty++
  } else {
    cart.value.push({
      ...product,
      qty: 1,
      _uid: genUid(),
      itemDiscountType: 'FLAT',
      itemDiscountValue: 0,
      itemNote: '',
    })
  }
}

// ─── Split Cart Item ──────────────────────────────────────────────────────────
function splitCartItem(item) {
  if (item.qty < 2) return
  const idx = cart.value.findIndex(i => i._uid === item._uid)
  if (idx === -1) return

  const half1 = Math.floor(item.qty / 2)
  const half2 = item.qty - half1

  // Update qty baris asli
  cart.value[idx] = { ...item, qty: half1 }

  // Sisipkan baris baru tepat setelah baris asli
  const newItem = {
    ...item,
    qty: half2,
    _uid: genUid(),
    itemDiscountType: 'FLAT',
    itemDiscountValue: 0,
    itemNote: '',
  }
  cart.value.splice(idx + 1, 0, newItem)

  toast.success(`"${item.name}" dipisah menjadi 2 baris`)
}

// Hitung total qty semua baris produk yang sama (untuk badge di card produk)
function getCartQty(id) {
  return cart.value.filter(i => i.id === id).reduce((s, i) => s + i.qty, 0)
}

function increaseQty(item) {
  const stock = getStock(item.id)
  const totalQty = cart.value.filter(i => i.id === item.id).reduce((s, i) => s + i.qty, 0)
  if (stock !== null && totalQty >= stock) {
    toast.error(`Stok ${item.name} tidak mencukupi. Tersisa: ${stock}`)
    return
  }
  item.qty++
}

function decreaseQty(item) {
  if (item.qty > 1) { item.qty-- } else { removeFromCart(item) }
}

function setQty(item, value) {
  const num = parseInt(value, 10)
  if (isNaN(num) || num < 1) {
    item.qty = 1
    return
  }
  const stock = getStock(item.id)
  // Hitung qty baris lain produk yang sama (tidak termasuk baris ini)
  const otherQty = cart.value.filter(i => i.id === item.id && i._uid !== item._uid).reduce((s, i) => s + i.qty, 0)
  if (stock !== null && (otherQty + num) > stock) {
    toast.error(`Stok ${item.name} tidak mencukupi. Tersisa: ${stock}`)
    item.qty = Math.max(1, stock - otherQty)
    return
  }
  item.qty = num
}

function removeFromCart(item) {
  const idx = cart.value.findIndex(i => i._uid === item._uid)
  if (idx !== -1) cart.value.splice(idx, 1)
}

function formatCurrency(value) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(value)
}

// ─── Diskon Per Item ──────────────────────────────────────────────────────────
function getItemUnitPrice(item) {
  return Number(item.base_price || item.basePrice || item.price || 0)
}

function getItemDiscountPerUnit(item) {
  const unitPrice = getItemUnitPrice(item)
  const val = Number(item.itemDiscountValue) || 0
  if (val <= 0) return 0
  if (item.itemDiscountType === 'PERCENT') {
    const pct = Math.min(val, 100)
    return Math.round(unitPrice * pct / 100)
  }
  return Math.min(val, unitPrice)
}

function getItemDiscountTotal(item) {
  return getItemDiscountPerUnit(item) * item.qty
}

function getItemNetUnitPrice(item) {
  return Math.max(0, getItemUnitPrice(item) - getItemDiscountPerUnit(item))
}

const totalItemDiscount = computed(() => {
  return cart.value.reduce((s, i) => s + getItemDiscountTotal(i), 0)
})

const totalItems = computed(() => cart.value.reduce((s, i) => s + i.qty, 0))

const subtotal = computed(() => {
  return cart.value.reduce((s, i) => {
    const price = getItemUnitPrice(i)
    return s + price * i.qty
  }, 0)
})

// ─── Voucher Logic ───────────────────────────────────────────────────────────
const voucherCode = ref('')
const appliedVoucher = ref(null)
const showVoucherDropdown = ref(false)
const voucherInputRef = ref(null)
const focusedVoucherIndex = ref(-1)

function handleVoucherBlur() {
  setTimeout(() => {
    showVoucherDropdown.value = false
    focusedVoucherIndex.value = -1
  }, 200)
}

const filteredVouchers = computed(() => {
  if (!voucherCode.value) return []
  const q = voucherCode.value.toLowerCase()
  return vouchers.value.filter(v => {
    const active = v.isActive ?? v.is_active ?? true
    if (!active) return false
    return (
      v.code.toLowerCase().includes(q) || 
      (v.name && v.name.toLowerCase().includes(q))
    )
  })
})

watch(filteredVouchers, () => {
  focusedVoucherIndex.value = -1
})

function selectVoucher(code) {
  voucherCode.value = code
  showVoucherDropdown.value = false
  focusedVoucherIndex.value = -1
  applyVoucher()
  if (voucherInputRef.value) {
    const el = voucherInputRef.value.$el?.querySelector('input') || voucherInputRef.value
    if (el && typeof el.blur === 'function') el.blur()
  }
}

function handleVoucherKeyDown(e) {
  if (!showVoucherDropdown.value || filteredVouchers.value.length === 0) return
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    focusedVoucherIndex.value = focusedVoucherIndex.value < filteredVouchers.value.length - 1
      ? focusedVoucherIndex.value + 1 : 0
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    focusedVoucherIndex.value = focusedVoucherIndex.value > 0
      ? focusedVoucherIndex.value - 1 : filteredVouchers.value.length - 1
  } else if (e.key === 'Enter') {
    if (focusedVoucherIndex.value >= 0 && focusedVoucherIndex.value < filteredVouchers.value.length) {
      e.preventDefault()
      selectVoucher(filteredVouchers.value[focusedVoucherIndex.value].code)
    }
  } else if (e.key === 'Escape') {
    showVoucherDropdown.value = false
    focusedVoucherIndex.value = -1
  }
}

// ─── Diskon Manual (Keseluruhan) ─────────────────────────────────────────────
const manualDiscountType = ref('FLAT')
const manualDiscountValue = ref('')
const manualDiscountNote = ref('')
const manualDiscountDisplay = ref('')

function onManualDiscountInput(e) {
  const raw = e.target.value.replace(/\D/g, '')
  manualDiscountValue.value = raw
  manualDiscountDisplay.value = raw ? Number(raw).toLocaleString('id-ID') : ''
  e.target.value = manualDiscountDisplay.value
}

function onManualDiscountTypeChange(type) {
  manualDiscountType.value = type
  manualDiscountValue.value = ''
  manualDiscountDisplay.value = ''
}

const manualDiscountAmount = computed(() => {
  const val = Number(manualDiscountValue.value) || 0
  if (val <= 0) return 0
  if (manualDiscountType.value === 'PERCENT') {
    const pct = Math.min(val, 100)
    return Math.round(subtotal.value * pct / 100)
  }
  return Math.min(val, subtotal.value)
})

function applyVoucher() {
  if (!voucherCode.value) return
  const v = vouchers.value.find(v => v.code === voucherCode.value.toUpperCase())
  if (!v) { toast.error('Kode voucher tidak valid.'); appliedVoucher.value = null; return }
  const isActive = v.isActive ?? v.is_active ?? true
  if (!isActive) { toast.error('Voucher ini sudah tidak aktif.'); return }
  const quota = v.quota ?? null
  const usedCount = v.usedCount ?? v.used_count ?? 0
  if (quota !== null && quota > 0 && usedCount >= quota) { toast.error('Kuota voucher sudah habis.'); return }
  const minPurchase = Number(v.minPurchase ?? v.min_purchase ?? 0)
  if (minPurchase > 0 && subtotal.value < minPurchase) {
    toast.error(`Min. pembelian untuk voucher ini adalah ${formatCurrency(minPurchase)}`)
    return
  }
  appliedVoucher.value = v
  toast.success(`Voucher "${v.name}" diterapkan!`)
}
function removeVoucher() { appliedVoucher.value = null; voucherCode.value = '' }

const discountAmount = computed(() => {
  let d = 0
  if (appliedVoucher.value) {
    const v = appliedVoucher.value
    const type = (v.discountType || v.discount_type || '').toString().toUpperCase()
    const rawValue = v.discountValue ?? v.discount_value ?? 0
    const value = Number(rawValue)
    const minPurchase = Number(v.minPurchase ?? v.min_purchase ?? 0)
    if (!(minPurchase > 0 && subtotal.value < minPurchase)) {
      if (type === 'PERCENT') {
        d = subtotal.value * value / 100
        const maxDiscount = Number(v.maxDiscount ?? v.max_discount ?? 0)
        if (maxDiscount > 0) d = Math.min(d, maxDiscount)
      } else {
        d = value
      }
      d = Math.round(d)
    }
  }
  d += manualDiscountAmount.value
  return Math.min(d, subtotal.value)
})

const total = computed(() => {
  return Math.max(0, Math.round(subtotal.value - totalItemDiscount.value - discountAmount.value))
})

// ─── Diskon Per Item: state UI popover ───────────────────────────────────────
const openItemDiscountId = ref(null)

function toggleItemDiscount(item) {
  openItemDiscountId.value = openItemDiscountId.value === item._uid ? null : item._uid
}

function onItemDiscountTypeChange(item, type) {
  item.itemDiscountType = type
  item.itemDiscountValue = 0
}

function onItemDiscountInput(item, e) {
  if (item.itemDiscountType === 'PERCENT') {
    let num = parseFloat(e.target.value)
    if (isNaN(num) || num < 0) num = 0
    if (num > 100) num = 100
    item.itemDiscountValue = num
  } else {
    const raw = e.target.value.replace(/\D/g, '')
    item.itemDiscountValue = raw ? Number(raw) : 0
    e.target.value = raw ? Number(raw).toLocaleString('id-ID') : ''
  }
}

const showPayment = ref(false)
const payMethod = ref('cash')
const cashTendered = ref('')
const cashTenderedDisplay = ref('')
const bankName = ref('')
const referenceNo = ref('')
const buyerName = ref('')

const changeDue = computed(() => Math.max(0, Math.round((Number(cashTendered.value) || 0) - total.value)))

const isSplitPayment = ref(false)
const splitCashAmount = ref('')
const splitTransferAmount = ref('')
const splitBankName = ref('')
const splitReferenceNo = ref('')

const splitCashNum = computed(() => Number(splitCashAmount.value) || 0)
const splitTransferNum = computed(() => Number(splitTransferAmount.value) || 0)
const splitTotal = computed(() => splitCashNum.value + splitTransferNum.value)
const splitValid = computed(() => splitTotal.value >= total.value && splitCashNum.value > 0 && splitTransferNum.value > 0)
const splitChange = computed(() => Math.max(0, splitTotal.value - total.value))

function onCashTenderedInput(e) {
  const raw = e.target.value.replace(/\D/g, '')
  cashTendered.value = raw
  cashTenderedDisplay.value = raw ? Number(raw).toLocaleString('id-ID') : ''
  e.target.value = cashTenderedDisplay.value
}

function setCashTendered(amount) {
  cashTendered.value = String(amount)
  cashTenderedDisplay.value = amount ? Number(amount).toLocaleString('id-ID') : ''
}

function openPayment() { 
  if (!cart.value.length) return
  if (!selectedBranchId.value) { toast.warning('Pilih cabang terlebih dahulu.'); return }
  showPayment.value = true
  payMethod.value = 'cash'
  cashTendered.value = ''
  cashTenderedDisplay.value = ''
  bankName.value = ''
  referenceNo.value = ''
  buyerName.value = ''
  manualDiscountType.value = 'FLAT'
  manualDiscountValue.value = ''
  manualDiscountDisplay.value = ''
  manualDiscountNote.value = ''
  isSplitPayment.value = false
  splitCashAmount.value = ''
  splitTransferAmount.value = ''
  splitBankName.value = ''
  splitReferenceNo.value = ''
}

function closePayment() { showPayment.value = false }

function closeReceipt() {
  showReceipt.value = false
  lastOrder.value = null
  confirmingTransfer.value = false
  if (selectedBranchId.value) fetchStockBalances(selectedBranchId.value)
}

// ─── Hold Order ───────────────────────────────────────────────────────────────
const heldOrders = ref(JSON.parse(localStorage.getItem('pos_held_orders') || '[]'))
const showHeldPanel = ref(false)

function saveHeldToStorage() {
  localStorage.setItem('pos_held_orders', JSON.stringify(heldOrders.value))
}

function holdOrder() {
  if (!cart.value.length) { toast.error('Keranjang kosong, tidak ada yang bisa ditahan.'); return }
  const held = {
    id: Date.now(),
    label: `Order #${heldOrders.value.length + 1}`,
    cart: JSON.parse(JSON.stringify(cart.value)),
    voucherCode: voucherCode.value,
    appliedVoucher: appliedVoucher.value ? JSON.parse(JSON.stringify(appliedVoucher.value)) : null,
    buyerName: buyerName.value,
    savedAt: new Date().toISOString(),
    branchId: selectedBranchId.value,
  }
  heldOrders.value.push(held)
  saveHeldToStorage()
  cart.value = []
  appliedVoucher.value = null
  voucherCode.value = ''
  buyerName.value = ''
  toast.success(`Order ditahan. Lanjutkan kapan saja.`)
}

function restoreHeldOrder(held) {
  if (cart.value.length > 0) {
    const ok = window.confirm('Keranjang aktif akan digantikan oleh order yang ditahan. Lanjutkan?')
    if (!ok) return
  }
  // Pastikan setiap item yang dipulihkan punya _uid
  cart.value = held.cart.map(item => ({ ...item, _uid: item._uid || genUid() }))
  voucherCode.value = held.voucherCode || ''
  appliedVoucher.value = held.appliedVoucher || null
  buyerName.value = held.buyerName || ''
  deleteHeldOrder(held.id)
  showHeldPanel.value = false
  toast.success(`Order "${held.label}" dipulihkan.`)
}

function deleteHeldOrder(id) {
  heldOrders.value = heldOrders.value.filter(h => h.id !== id)
  saveHeldToStorage()
}

const confirmingTransfer = ref(false)

async function confirmTransferFromReceipt() {
  const paymentId = lastOrder.value?.payments?.[0]?.id
  if (!paymentId) { toast.error('ID pembayaran tidak ditemukan.'); return }
  confirmingTransfer.value = true
  try {
    await api.patch(`/api/v1/payments/${paymentId}/verify`)
    if (lastOrder.value?.payments?.[0]) lastOrder.value.payments[0].status = 'VERIFIED'
    toast.success('Transfer berhasil dikonfirmasi!')
  } catch (err) {
    toast.error(err.response?.data?.message || 'Gagal mengkonfirmasi transfer.')
  } finally {
    confirmingTransfer.value = false
  }
}

function printReceipt() { struk.value?.doPrint() }

function fmt(v) {
  if (v == null) return 'Rp 0'
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(v)
}

function copyOrderSummary() {
  if (!lastOrder.value) return
  const o = lastOrder.value
  const pay = o.payments?.[0]
  const items = (o.items || []).map(i => `  - ${i.productName || 'Produk'} x${i.qty} = ${formatCurrency(i.subtotal)}`).join('\n')
  const text = [
    `📋 Catatan Transaksi`,
    `No. Order : ${o.orderNumber}`,
    `Cabang    : ${o.branchName || '-'}`,
    `Kasir     : ${o.cashierName || '-'}`,
    ``,
    `Item:`,
    items || '  -',
    ``,
    `Subtotal  : ${formatCurrency(o.subtotal)}`,
    o.discountAmount > 0 ? `Diskon    : -${formatCurrency(o.discountAmount)}` : null,
    `Total     : ${formatCurrency(o.total)}`,
    `Metode    : ${pay?.method === 'CASH' ? 'Tunai' : 'Transfer Bank'}`,
    pay?.method === 'CASH' ? `Kembalian : ${formatCurrency(pay.changeDue)}` : `Status    : Menunggu Konfirmasi`,
  ].filter(Boolean).join('\n')

  navigator.clipboard.writeText(text).then(() => {
    toast.success('Catatan berhasil disalin!')
  }).catch(() => {
    toast.error('Gagal menyalin catatan.')
  })
}

async function checkout() {
  if (isSplitPayment.value) {
    if (splitCashNum.value <= 0) { toast.error('Nominal tunai untuk split payment wajib diisi.'); return }
    if (splitTransferNum.value <= 0) { toast.error('Nominal transfer untuk split payment wajib diisi.'); return }
    if (!splitReferenceNo.value) { toast.error('Nomor referensi transfer wajib diisi.'); return }
    if (splitTotal.value < total.value) { toast.error('Total pembayaran kurang dari total tagihan.'); return }
  } else {
    if (payMethod.value === 'cash' && (Number(cashTendered.value) || 0) < total.value) {
      toast.error('Uang yang diberikan kurang!'); return
    }
    if (payMethod.value === 'transfer' && !referenceNo.value) {
      toast.error('Nomor referensi wajib diisi!'); return
    }
  }

  // Validasi stok: hitung total qty per productId dari semua baris
  const qtyByProduct = {}
  for (const item of cart.value) {
    qtyByProduct[item.id] = (qtyByProduct[item.id] || 0) + item.qty
  }
  for (const [productId, qty] of Object.entries(qtyByProduct)) {
    const stock = getStock(productId)
    const productName = cart.value.find(i => i.id == productId)?.name || 'Produk'
    if (stock !== null && qty > stock) {
      toast.error(`Stok ${productName} tidak mencukupi. Diminta: ${qty}, Tersisa: ${stock}`)
      return
    }
    if (stock !== null && stock <= 0) {
      toast.error(`Stok ${productName} sudah habis.`)
      return
    }
  }

  processingCheckout.value = true
  try {
    const basePayload = {
      branchId: selectedBranchId.value,
      orderNumber: `ORD-${Date.now()}`,
      voucherId: appliedVoucher.value?.id,
      notes: null,
      buyerName: buyerName.value?.trim() || null,
      manualDiscountType: manualDiscountValue.value && Number(manualDiscountValue.value) > 0 ? manualDiscountType.value : null,
      manualDiscountValue: manualDiscountValue.value && Number(manualDiscountValue.value) > 0 ? Number(manualDiscountValue.value) : null,
      manualDiscountNote: manualDiscountNote.value?.trim() || null,
      // Kirim setiap baris cart sebagai item terpisah ke backend
      items: cart.value.map(item => ({
        productId: item.id,
        qty: item.qty,
        unitPrice: getItemUnitPrice(item),
        itemDiscountType: getItemDiscountPerUnit(item) > 0 ? item.itemDiscountType : null,
        itemDiscountValue: getItemDiscountPerUnit(item) > 0 ? Number(item.itemDiscountValue) : null,
        itemDiscountAmount: getItemDiscountTotal(item),
        itemNote: item.itemNote?.trim() || null,
      })),
    }

    const payload = isSplitPayment.value
      ? {
          ...basePayload,
          payments: [
            { method: 'CASH', amount: splitCashNum.value, cashTendered: splitCashNum.value, changeDue: splitChange.value },
            { method: 'TRANSFER', amount: splitTransferNum.value, bankName: splitBankName.value, referenceNo: splitReferenceNo.value }
          ]
        }
      : {
          ...basePayload,
          payment: {
            method: payMethod.value.toUpperCase(),
            amount: total.value,
            cashTendered: payMethod.value === 'cash' ? Number(cashTendered.value) : total.value,
            changeDue: payMethod.value === 'cash' ? changeDue.value : 0,
            bankName: bankName.value,
            referenceNo: referenceNo.value
          }
        }

    const response = await api.post('/api/v1/orders', payload)

    lastOrder.value = response.data?.data || {
      orderNumber: payload.orderNumber,
      total: total.value,
      payments: isSplitPayment.value ? payload.payments : [payload.payment]
    }

    cart.value = []
    appliedVoucher.value = null
    voucherCode.value = ''
    manualDiscountValue.value = ''
    manualDiscountDisplay.value = ''
    manualDiscountNote.value = ''
    cashTendered.value = ''
    cashTenderedDisplay.value = ''
    showPayment.value = false
    showReceipt.value = true

    // Update stok lokal berdasarkan total qty per produk
    for (const [productId, qty] of Object.entries(qtyByProduct)) {
      const key = String(productId)
      const current = stockBalances.value[key]
      if (current !== undefined && current !== null) {
        stockBalances.value[key] = Math.max(0, Number(current) - qty)
      }
    }

    fetchTodayOrders()
  } catch (err) {
    console.error('[Checkout Error]', err.response?.data || err.message)
    const msg = err.response?.data?.data?.message || err.response?.data?.message || err.message || 'Gagal memproses transaksi.'
    toast.error(msg)
  } finally {
    processingCheckout.value = false
  }
}

const AVATAR_COLORS = [
  { bg: '#ede9fe', color: '#6d28d9' }, { bg: '#dbeafe', color: '#1d4ed8' },
  { bg: '#d1fae5', color: '#065f46' }, { bg: '#fef3c7', color: '#92400e' },
  { bg: '#fee2e2', color: '#991b1b' }, { bg: '#fce7f3', color: '#9d174d' },
  { bg: '#e0f2fe', color: '#0369a1' }, { bg: '#f3f4f6', color: '#374151' },
]
function avatarStyle(name = '') {
  const c = AVATAR_COLORS[name.charCodeAt(0) % AVATAR_COLORS.length]
  return { backgroundColor: c.bg, color: c.color }
}
</script>

<template>
  <AppLayout>
    <div class="pos-root">
      
      <div class="flex flex-col bg-white dark:bg-[#09090b] flex-1 min-w-0 min-h-0">
        
        <div class="px-3 pt-3 lg:px-5 lg:pt-5 shrink-0 z-10">
          <div class="flex items-center justify-between mb-4">
            <div class="hidden lg:block">
              <h1 class="text-xl font-black text-zinc-900 dark:text-white tracking-tight">Terminal Kasir</h1>
              <p class="text-xs text-zinc-500 font-medium mt-0.5">Point of Sale System v2.0</p>
            </div>
            
            <div class="flex items-center gap-2">
              <div v-if="loadingShift" class="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-zinc-100 dark:bg-zinc-800 text-zinc-400 text-[11px] font-bold">
                <Loader2 class="h-3 w-3 animate-spin" /> Memuat shift...
              </div>
              <template v-else>
                <div v-if="activeShift" class="flex items-center gap-2">
                  <div class="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200/60 dark:border-emerald-800/30">
                    <UserCheck class="h-3.5 w-3.5 text-emerald-500" />
                    <span class="text-[11px] font-bold text-emerald-700 dark:text-emerald-400">{{ activeShift.cashierUsername }}</span>
                    <span class="text-[10px] text-emerald-500/70 font-medium">· shift aktif</span>
                  </div>
                  <button
                    @click="openShiftModal('close')"
                    class="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-red-50 dark:bg-red-900/20 border border-red-200/60 dark:border-red-800/30 text-red-600 dark:text-red-400 text-[11px] font-bold hover:bg-red-100 dark:hover:bg-red-900/40 transition-colors"
                  >
                    <LogOut class="h-3.5 w-3.5" /> Tutup Shift
                  </button>
                </div>
                <button
                  v-else
                  @click="openShiftModal('open')"
                  class="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-[11px] font-bold transition-colors shadow-sm shadow-emerald-500/30"
                >
                  <LogIn class="h-3.5 w-3.5" /> Buka Shift
                </button>
              </template>

              <div ref="branchDropdownRef" class="relative">
                <div 
                  v-if="authStore.user?.branchId"
                  class="flex items-center gap-2 bg-emerald-50 dark:bg-emerald-950/30 px-3.5 py-2 rounded-xl border border-emerald-200/50 dark:border-emerald-800/30"
                >
                  <Building2 class="h-4 w-4 text-emerald-500 shrink-0" />
                  <span class="text-[12px] font-black text-emerald-700 dark:text-emerald-400 uppercase tracking-wider">
                    📍 {{ selectedBranch?.name || authStore.user?.branchName || 'Cabang Aktif' }}
                  </span>
                </div>

                <button
                  v-else
                  @click.stop="showBranchDropdown = !showBranchDropdown"
                  class="flex items-center gap-2 bg-zinc-100 dark:bg-zinc-800/60 px-3 py-2 rounded-xl border border-zinc-200/50 dark:border-zinc-700/50 hover:bg-zinc-200/60 dark:hover:bg-zinc-700/60 transition-colors min-w-[160px]"
                >
                  <MapPin class="h-3.5 w-3.5 text-primary shrink-0" />
                  <span class="text-[12px] font-black text-zinc-700 dark:text-zinc-200 flex-1 text-left truncate">
                    {{ selectedBranch?.name || 'Pilih Cabang' }}
                  </span>
                  <ChevronDown class="h-3.5 w-3.5 text-zinc-400 shrink-0 transition-transform duration-200" :class="showBranchDropdown ? 'rotate-180' : ''" />
                </button>

                <Transition name="dropdown">
                  <div
                    v-if="showBranchDropdown && !authStore.user?.branch"
                    class="absolute top-full right-0 mt-1.5 z-50 min-w-[200px] bg-white dark:bg-zinc-900 rounded-xl shadow-xl border border-zinc-200 dark:border-zinc-800 overflow-hidden"
                  >
                    <div class="p-1">
                      <button
                        v-for="b in branches"
                        :key="b.id"
                        @click="selectBranch(b)"
                        class="w-full flex items-center gap-2.5 px-3 py-2.5 rounded-lg text-left transition-colors text-sm"
                        :class="selectedBranchId === b.id
                          ? 'bg-primary/10 text-primary font-bold'
                          : 'text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800 font-medium'"
                      >
                        <MapPin class="h-3.5 w-3.5 shrink-0 opacity-60" />
                        <span class="truncate">{{ b.name }}</span>
                        <Check v-if="selectedBranchId === b.id" class="h-3.5 w-3.5 ml-auto shrink-0 text-primary" />
                      </button>
                    </div>
                  </div>
                </Transition>
              </div>
            </div>
          </div>
          
          <div class="flex items-center bg-zinc-100 dark:bg-zinc-800/60 rounded-[1rem] p-1 mb-3">
            <div class="flex-1 flex items-center pl-3">
              <Search class="h-[18px] w-[18px] text-zinc-400" />
              <Input v-model="searchQuery" placeholder="Cari produk atau SKU..."
                class="w-full bg-transparent border-none shadow-none text-[13px] font-medium focus-visible:ring-0 px-2 h-9" />
            </div>
            <Button size="sm" variant="outline" class="h-[34px] rounded-xl px-3 bg-white dark:bg-zinc-700 shadow-sm border-none font-bold gap-1.5 mr-0.5 text-zinc-700 dark:text-zinc-200 hover:bg-zinc-50">
              <Scan class="h-3.5 w-3.5" /> Scan
            </Button>
          </div>

          <Transition name="slide-down">
            <div v-if="showStockWarning && (lowStockProducts.length > 0 || outOfStockProducts.length > 0)"
              class="mb-2 rounded-xl border overflow-hidden"
              :class="outOfStockProducts.length > 0
                ? 'border-red-200 dark:border-red-800/40 bg-red-50 dark:bg-red-900/20'
                : 'border-amber-200 dark:border-amber-800/40 bg-amber-50 dark:bg-amber-900/20'">
              <div class="flex items-start gap-2.5 px-3.5 py-2.5">
                <AlertTriangle :class="['h-4 w-4 mt-0.5 shrink-0',
                  outOfStockProducts.length > 0 ? 'text-red-500' : 'text-amber-500']" />
                <div class="flex-1 min-w-0">
                  <p :class="['text-[12px] font-bold',
                    outOfStockProducts.length > 0 ? 'text-red-700 dark:text-red-400' : 'text-amber-700 dark:text-amber-400']">
                    <span v-if="outOfStockProducts.length > 0">{{ outOfStockProducts.length }} produk habis</span>
                    <span v-if="outOfStockProducts.length > 0 && lowStockProducts.length > 0">, </span>
                    <span v-if="lowStockProducts.length > 0">{{ lowStockProducts.length }} produk hampir habis (≤ {{ LOW_STOCK_THRESHOLD }})</span>
                  </p>
                  <div class="flex flex-wrap gap-1 mt-1">
                    <span v-for="p in [...outOfStockProducts, ...lowStockProducts].slice(0, 5)" :key="p.id"
                      :class="['text-[10px] font-semibold px-1.5 py-0.5 rounded-md truncate max-w-[140px]',
                        getStock(p.id) <= 0
                          ? 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
                          : 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400']">
                      {{ p.name }}
                      <span class="opacity-70">({{ getStock(p.id) <= 0 ? 'Habis' : getStock(p.id) }})</span>
                    </span>
                    <span v-if="(outOfStockProducts.length + lowStockProducts.length) > 5"
                      class="text-[10px] font-semibold px-1.5 py-0.5 rounded-md bg-zinc-100 text-zinc-500">
                      +{{ (outOfStockProducts.length + lowStockProducts.length) - 5 }} lainnya
                    </span>
                  </div>
                </div>
                <button @click="showStockWarning = false"
                  class="shrink-0 p-0.5 rounded text-zinc-400 hover:text-zinc-600 transition-colors">
                  <X class="h-3.5 w-3.5" />
                </button>
              </div>
            </div>
          </Transition>

          <!-- Sales Target Widget -->
          <div v-if="salesTarget > 0 || showTargetEdit" class="mb-3 bg-white dark:bg-zinc-900 rounded-2xl border border-zinc-200 dark:border-zinc-800 px-4 py-3">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-1.5">
                <Target class="h-3.5 w-3.5 text-primary shrink-0" />
                <span class="text-[11px] font-bold text-zinc-700 dark:text-zinc-200">Target Bulan Ini</span>
              </div>
              <button @click="showTargetEdit = !showTargetEdit; targetInput = salesTarget ? String(salesTarget) : ''"
                class="text-[10px] font-bold text-zinc-400 hover:text-primary transition-colors px-2 py-0.5 rounded-md hover:bg-zinc-100 dark:hover:bg-zinc-800">
                {{ showTargetEdit ? 'Batal' : 'Ubah' }}
              </button>
            </div>
            <template v-if="showTargetEdit">
              <div class="flex gap-2">
                <div class="relative flex-1">
                  <span class="absolute left-2.5 top-1/2 -translate-y-1/2 text-[11px] font-black text-zinc-400">Rp</span>
                  <input 
                    v-model="targetInput" 
                    type="text" 
                    inputmode="numeric" 
                    placeholder="0"
                    @input="e => {
                      const raw = e.target.value.replace(/\D/g, '');
                      targetInput = raw ? Number(raw).toLocaleString('id-ID') : '';
                    }"
                    class="pl-8 h-8 w-full text-[12px] font-bold rounded-lg border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 outline-none focus:ring-2 focus:ring-primary/20" 
                  />
                </div>
                <button @click="saveTarget" class="h-8 px-3 rounded-lg bg-primary text-primary-foreground text-[11px] font-bold">Simpan</button>
              </div>
            </template>
            <template v-else>
              <div class="flex items-end justify-between mb-1.5">
                <span class="text-[13px] font-black text-zinc-900 dark:text-white">{{ formatCurrency(currentMonthRevenue) }}</span>
                <span class="text-[11px] font-bold" :class="targetProgress >= 100 ? 'text-emerald-500' : 'text-zinc-400'">
                  {{ targetProgress }}% dari {{ formatCurrency(salesTarget) }}
                </span>
              </div>
              <div class="h-2 bg-zinc-100 dark:bg-zinc-800 rounded-full overflow-hidden">
                <div class="h-full rounded-full transition-all duration-700"
                  :class="targetProgress >= 100 ? 'bg-emerald-500' : targetProgress >= 75 ? 'bg-primary' : targetProgress >= 50 ? 'bg-amber-400' : 'bg-red-400'"
                  :style="{ width: targetProgress + '%' }"></div>
              </div>
              <p v-if="targetProgress >= 100" class="text-[10px] font-bold text-emerald-500 mt-1">🎉 Target bulan ini tercapai!</p>
            </template>
          </div>
          <button v-else @click="showTargetEdit = true"
            class="mb-3 w-full flex items-center justify-center gap-1.5 h-8 rounded-xl border border-dashed border-zinc-300 dark:border-zinc-700 text-[11px] font-bold text-zinc-400 hover:text-primary hover:border-primary transition-colors">
            <Target class="h-3.5 w-3.5" /> Set Target Omzet Bulan Ini
          </button>

          <div class="flex gap-2 overflow-x-auto no-scrollbar pb-2">
            <button
              :class="['px-4 py-1.5 rounded-full text-[13px] font-bold whitespace-nowrap transition-all border flex items-center gap-1.5',
                showFavorites
                  ? 'bg-amber-500 border-amber-500 text-white shadow-md shadow-amber-500/20'
                  : 'bg-white border-zinc-200 text-zinc-600 dark:bg-zinc-900 dark:border-zinc-800 dark:text-zinc-400 hover:bg-zinc-50']"
              @click="showFavorites = !showFavorites; if(showFavorites && topProducts.length === 0) fetchTopProducts()">
              <Star class="h-3 w-3" /> Favorit
            </button>
            <button v-for="cat in uniqueCategories" :key="cat"
              class="px-4 py-1.5 rounded-full text-[13px] font-bold whitespace-nowrap transition-all border"
              :class="!showFavorites && activeCategory === cat ? 'bg-zinc-900 border-zinc-900 text-white dark:bg-zinc-100 dark:border-zinc-100 dark:text-zinc-900 shadow-md shadow-zinc-900/20' : 'bg-white border-zinc-200 text-zinc-600 dark:bg-zinc-900 dark:border-zinc-800 dark:text-zinc-400 hover:bg-zinc-50'"
              @click="showFavorites = false; activeCategory = cat">
              {{ cat }}
            </button>
          </div>
        </div>

        <div class="flex-1 min-h-0 overflow-y-auto no-scrollbar">
          <div v-if="loading" class="h-full flex flex-col items-center justify-center text-zinc-400 gap-3">
            <Loader2 class="h-10 w-10 animate-spin opacity-20 mb-3 text-primary" />
            <p class="text-[13px] font-medium italic">Menghubungkan ke sistem...</p>
          </div>
          <div v-else-if="showFavorites">
            <div v-if="loadingTop" class="h-full flex flex-col items-center justify-center py-16 text-zinc-400 gap-3">
              <Loader2 class="h-8 w-8 animate-spin opacity-20" />
              <p class="text-[13px] font-medium">Memuat produk terlaris...</p>
            </div>
            <div v-else-if="topProducts.length === 0" class="flex flex-col items-center justify-center py-16 text-zinc-400 gap-2">
              <Star class="h-10 w-10 opacity-20" />
              <p class="text-[13px] font-medium">Belum ada data penjualan untuk analisis</p>
            </div>
            <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-3 xl:grid-cols-4 gap-3 p-3 lg:p-5">
              <div v-for="(top, idx) in topProducts" :key="top.id"
                class="relative flex flex-col bg-white dark:bg-zinc-900/80 rounded-[20px] overflow-hidden border border-amber-200/60 dark:border-amber-800/30 shadow-sm hover:shadow-md transition-all cursor-pointer active:scale-[0.98]"
                @click="addToCart(products.find(p => p.id == top.id) || { id: top.id, name: top.name, base_price: 0 })">
                <div class="absolute top-2 left-2 z-10 w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-black"
                  :class="idx === 0 ? 'bg-amber-400 text-white' : idx === 1 ? 'bg-zinc-300 text-zinc-700' : idx === 2 ? 'bg-amber-600 text-white' : 'bg-zinc-100 text-zinc-500'">
                  {{ idx + 1 }}
                </div>
                <div v-if="getCartQty(top.id) > 0" class="absolute top-2 right-2 bg-primary text-primary-foreground text-[11px] font-black px-2.5 py-0.5 rounded-full shadow-md z-10">
                  {{ getCartQty(top.id) }}
                </div>
                <div class="aspect-square bg-amber-50 dark:bg-amber-900/20 flex items-center justify-center">
                  <span class="text-2xl font-black text-amber-600 dark:text-amber-400 opacity-80 uppercase">{{ top.name.charAt(0) }}</span>
                </div>
                <div class="p-3 flex flex-col gap-1">
                  <h4 class="text-[13px] font-bold text-zinc-800 dark:text-zinc-200 line-clamp-2 leading-tight">{{ top.name }}</h4>
                  <p class="text-[10px] font-bold text-amber-600 dark:text-amber-400">{{ top.count }}× terjual</p>
                  <p class="text-[13px] font-black text-zinc-900 dark:text-white mt-auto">
                    {{ formatCurrency(products.find(p => p.id == top.id)?.base_price || products.find(p => p.id == top.id)?.basePrice || products.find(p => p.id == top.id)?.price || 0) }}
                  </p>
                </div>
              </div>
            </div>
          </div>
          <div v-else-if="filteredProducts.length === 0" class="h-full flex flex-col items-center justify-center text-zinc-400">
            <ShoppingBag class="h-12 w-12 opacity-20 mb-3" />
            <p class="text-[13px] font-medium">Produk tidak ditemukan.</p>
          </div>
          <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-3 xl:grid-cols-4 gap-3 p-3 lg:p-5">
            <div v-for="p in filteredProducts" :key="p.id"
              class="relative flex flex-col bg-white dark:bg-zinc-900/80 rounded-[20px] overflow-hidden transition-transform active:scale-[0.98] border border-zinc-200/60 dark:border-zinc-800 shadow-sm hover:shadow-md"
              :class="[
                getStock(p.id) !== null && getStock(p.id) <= 0
                  ? 'opacity-50 cursor-not-allowed'
                  : 'cursor-pointer',
                { 'ring-2 ring-primary ring-offset-2 dark:ring-offset-zinc-900': getCartQty(p.id) > 0 }
              ]"
              @click="getStock(p.id) !== null && getStock(p.id) <= 0 ? null : addToCart(p)">
              
              <div v-if="getCartQty(p.id) > 0" class="absolute top-2.5 right-2.5 bg-primary text-primary-foreground text-[11px] font-black px-2.5 py-0.5 rounded-full shadow-md z-10">
                {{ getCartQty(p.id) }}
              </div>
              
              <div class="aspect-[4/3] lg:aspect-square bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center overflow-hidden shrink-0">
                <img v-if="p.imageUrl" :src="p.imageUrl" class="w-full h-full object-cover" />
                <div v-else class="w-full h-full flex items-center justify-center text-3xl font-black opacity-80 uppercase" :style="avatarStyle(p.name)">{{ p.name.charAt(0) }}</div>
              </div>
              
              <div class="p-3 flex flex-col gap-1.5 flex-1">
                <div class="flex items-center justify-between">
                  <span class="text-[9px] font-bold text-zinc-400 uppercase tracking-widest">{{ p.sku || 'N/A' }}</span>
                </div>
                <h4 class="text-[13px] font-bold text-zinc-800 dark:text-zinc-200 line-clamp-2 leading-tight">{{ p.name }}</h4>
                <div class="mt-auto pt-1 flex items-end justify-between">
                  <span class="text-[15px] font-black text-zinc-900 dark:text-white">{{ formatCurrency(p.base_price || p.basePrice || p.price) }}</span>
                  <span :class="['text-[10px] font-bold px-1.5 py-0.5 rounded-md',
                    getStock(p.id) === null ? 'text-zinc-400' :
                    getStock(p.id) <= 0 ? 'bg-red-50 text-red-500 dark:bg-red-900/20' :
                    getStock(p.id) <= 5 ? 'bg-amber-50 text-amber-600 dark:bg-amber-900/20' :
                    'bg-emerald-50 text-emerald-600 dark:bg-emerald-900/20']">
                    {{ getStock(p.id) === null ? '' : getStock(p.id) <= 0 ? 'Habis' : `Stok: ${getStock(p.id)}` }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ─── Panel Kanan: Pesanan ─────────────────────────────────────────── -->
      <div class="flex flex-col bg-white dark:bg-[#09090b] border-t lg:border-t-0 lg:border-l border-zinc-200 dark:border-zinc-800 max-h-[42vh] lg:max-h-none lg:w-[380px] shrink-0 shadow-[0_-10px_40px_-15px_rgba(0,0,0,0.05)] lg:shadow-none z-20 rounded-t-[24px] lg:rounded-none">
        
        <div class="flex items-center justify-between px-4 py-3.5 lg:p-5 border-b border-zinc-100 dark:border-zinc-800/60 shrink-0">
          <div class="flex items-center gap-2">
            <h2 class="text-[15px] lg:text-lg font-black text-zinc-900 dark:text-white">Pesanan</h2>
            <span class="text-[10px] font-black bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-300 px-2.5 py-1 rounded-full">{{ totalItems }}</span>
          </div>
          <div class="flex items-center gap-1.5">
            <button v-if="cart.length && !showTodayPanel"
              @click="holdOrder"
              class="text-[11px] font-bold px-2.5 py-1 rounded-md bg-amber-50 dark:bg-amber-900/20 text-amber-600 dark:text-amber-400 hover:bg-amber-100 transition-colors flex items-center gap-1">
              <Pause class="h-3 w-3" /> Tahan
            </button>
            <button v-if="heldOrders.length > 0"
              @click="showHeldPanel = !showHeldPanel; showTodayPanel = false"
              :class="['relative text-[11px] font-bold px-2.5 py-1 rounded-md transition-colors flex items-center gap-1',
                showHeldPanel ? 'bg-amber-500 text-white' : 'bg-amber-50 dark:bg-amber-900/20 text-amber-600 dark:text-amber-400 hover:bg-amber-100']">
              <Clock class="h-3 w-3" />
              <span>{{ heldOrders.length }}</span>
            </button>
            <button @click="toggleTodayPanel(); showHeldPanel = false"
              :class="['text-[11px] font-bold px-2.5 py-1 rounded-md transition-colors flex items-center gap-1',
                showTodayPanel ? 'bg-primary/10 text-primary' : 'text-zinc-500 hover:text-zinc-700 bg-zinc-100 dark:bg-zinc-800']">
              <ReceiptText class="h-3.5 w-3.5" />
            </button>
            <button v-if="cart.length && !showTodayPanel && !showHeldPanel" class="text-[11px] font-bold text-red-500 hover:text-red-600 bg-red-50 dark:bg-red-500/10 px-2.5 py-1 rounded-md" @click="cart = []">Kosongkan</button>
          </div>
        </div>

        <div class="flex-1 min-h-0 overflow-y-auto no-scrollbar bg-zinc-50/50 dark:bg-[#09090b]/50 p-3 lg:p-5">

          <template v-if="showTodayPanel">
            <div v-if="loadingToday" class="h-full flex items-center justify-center">
              <Loader2 class="h-6 w-6 animate-spin text-primary/40" />
            </div>
            <div v-else class="flex flex-col gap-3">
              <div class="grid grid-cols-2 gap-2">
                <div class="bg-white dark:bg-zinc-900 rounded-[14px] p-3 border border-zinc-200/60 dark:border-zinc-800">
                  <p class="text-[10px] font-bold text-zinc-400 uppercase tracking-wider">Total Order</p>
                  <p class="text-xl font-black text-zinc-900 dark:text-white mt-0.5">{{ todaySummary.count }}</p>
                </div>
                <div class="bg-emerald-50 dark:bg-emerald-900/20 rounded-[14px] p-3 border border-emerald-100 dark:border-emerald-800/40">
                  <p class="text-[10px] font-bold text-emerald-600 uppercase tracking-wider">Omzet</p>
                  <p class="text-[13px] font-black text-emerald-700 dark:text-emerald-400 mt-0.5 leading-tight">{{ formatCurrency(todaySummary.total) }}</p>
                </div>
                <div class="bg-white dark:bg-zinc-900 rounded-[14px] p-3 border border-zinc-200/60 dark:border-zinc-800 col-span-2 flex items-center justify-between">
                  <div class="flex items-center gap-2 text-[11px] font-semibold text-zinc-500">
                    <Banknote class="h-3.5 w-3.5" /> Tunai: <span class="text-zinc-800 dark:text-zinc-200">{{ todaySummary.cash }} order</span>
                  </div>
                  <div class="flex items-center gap-2 text-[11px] font-semibold text-zinc-500">
                    <ArrowRightLeft class="h-3.5 w-3.5" /> Transfer: <span class="text-zinc-800 dark:text-zinc-200">{{ todaySummary.transfer }} order</span>
                  </div>
                </div>
              </div>
              <p class="text-[10px] font-bold text-zinc-400 uppercase tracking-wider px-1">Riwayat</p>
              <div v-if="todayOrders.length === 0" class="flex flex-col items-center justify-center py-8 text-zinc-400 gap-2">
                <ShoppingBag class="h-8 w-8 opacity-30" />
                <p class="text-[12px] font-medium">Belum ada transaksi hari ini</p>
              </div>
              <div v-for="o in todayOrders" :key="o.id"
                class="bg-white dark:bg-zinc-900 rounded-[14px] p-3 border border-zinc-200/60 dark:border-zinc-800 flex items-center justify-between gap-2">
                <div class="min-w-0">
                  <p class="text-[11px] font-mono font-bold text-primary truncate">{{ o.orderNumber }}</p>
                  <p class="text-[10px] text-zinc-400 mt-0.5">{{ o.buyerName || o.cashierName || '-' }} · {{ new Date(o.createdAt).toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }) }}</p>
                </div>
                <div class="text-right shrink-0">
                  <p class="text-[12px] font-black text-zinc-900 dark:text-zinc-100">{{ formatCurrency(o.total) }}</p>
                  <span :class="['text-[9px] font-bold px-1.5 py-0.5 rounded',
                    o.status === 'PAID' ? 'bg-emerald-50 text-emerald-600 dark:bg-emerald-900/20 dark:text-emerald-400' :
                    o.status === 'RETURN' ? 'bg-violet-50 text-violet-600 dark:bg-violet-900/20' :
                    'bg-zinc-100 text-zinc-500']">
                    {{ o.status === 'PAID' ? 'Lunas' : o.status === 'RETURN' ? 'Retur' : o.status }}
                  </span>
                </div>
              </div>
            </div>
          </template>

          <template v-else-if="showHeldPanel">
            <div class="flex flex-col gap-3">
              <p class="text-[10px] font-bold text-amber-600 dark:text-amber-400 uppercase tracking-wider px-1 flex items-center gap-1.5">
                <Clock class="h-3.5 w-3.5" /> Order Ditahan ({{ heldOrders.length }})
              </p>
              <div v-if="heldOrders.length === 0" class="flex flex-col items-center justify-center py-8 text-zinc-400 gap-2">
                <Clock class="h-8 w-8 opacity-30" />
                <p class="text-[12px] font-medium">Belum ada order yang ditahan</p>
              </div>
              <div v-for="held in heldOrders" :key="held.id"
                class="bg-white dark:bg-zinc-900 rounded-[14px] border border-amber-200/60 dark:border-amber-800/30 p-3 flex flex-col gap-2">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-[12px] font-black text-zinc-900 dark:text-zinc-100">{{ held.label }}</p>
                    <p class="text-[10px] text-zinc-400">{{ held.cart.length }} item · {{ new Date(held.savedAt).toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }) }}</p>
                  </div>
                  <button @click="deleteHeldOrder(held.id)" class="p-1.5 text-zinc-300 hover:text-red-400 transition-colors rounded-lg hover:bg-red-50 dark:hover:bg-red-900/20">
                    <X class="h-3.5 w-3.5" />
                  </button>
                </div>
                <div class="flex flex-wrap gap-1">
                  <span v-for="item in held.cart.slice(0, 3)" :key="item._uid || item.id"
                    class="text-[9px] font-medium px-1.5 py-0.5 rounded bg-zinc-100 dark:bg-zinc-800 text-zinc-500 truncate max-w-[100px]">
                    {{ item.name }} ×{{ item.qty }}
                  </span>
                  <span v-if="held.cart.length > 3" class="text-[9px] font-medium px-1.5 py-0.5 rounded bg-zinc-100 dark:bg-zinc-800 text-zinc-400">
                    +{{ held.cart.length - 3 }} lainnya
                  </span>
                </div>
                <button @click="restoreHeldOrder(held)"
                  class="w-full h-8 rounded-xl bg-amber-500 hover:bg-amber-600 text-white text-[11px] font-black flex items-center justify-center gap-1.5 transition-colors">
                  <Play class="h-3 w-3" /> Lanjutkan Order Ini
                </button>
              </div>
            </div>
          </template>

          <template v-else>
            <div v-if="!cart.length" class="h-full flex flex-col items-center justify-center text-zinc-400 gap-3">
              <div class="w-14 h-14 rounded-full bg-zinc-100 dark:bg-zinc-800/80 flex items-center justify-center"><ShoppingCart class="h-6 w-6 opacity-40" /></div>
              <p class="text-[13px] font-semibold">Belum ada pesanan</p>
            </div>
            <div v-else class="flex flex-col gap-2.5">
              <TransitionGroup name="list">
                <div v-for="item in cart" :key="item._uid" class="bg-white dark:bg-zinc-900 border border-zinc-200/60 dark:border-zinc-800 rounded-[16px] p-3 flex flex-col gap-3 shadow-sm hover:border-zinc-300 transition-colors">
                  <div class="flex items-start justify-between gap-3">
                    <div class="flex-1 min-w-0 pt-0.5">
                      <h4 class="text-[13px] font-bold text-zinc-800 dark:text-zinc-200 leading-snug truncate">{{ item.name }}</h4>
                      <div class="flex items-center gap-1.5 mt-0.5">
                        <p class="text-[11px] font-bold text-primary">{{ formatCurrency(item.base_price || item.basePrice || item.price) }}</p>
                        <span v-if="getStock(item.id) !== null && getStock(item.id) <= LOW_STOCK_THRESHOLD && getStock(item.id) > 0"
                          class="text-[9px] font-bold px-1.5 py-0.5 rounded bg-amber-50 text-amber-600 dark:bg-amber-900/20 dark:text-amber-400 shrink-0">
                          Sisa {{ getStock(item.id) }}
                        </span>
                      </div>
                    </div>

                    <div class="flex items-center gap-1 shrink-0">
                      <!-- Tombol Pisah: muncul jika qty >= 2 -->
                      <button
                        v-if="item.qty >= 2"
                        @click="splitCartItem(item)"
                        class="p-1.5 rounded-lg text-zinc-300 hover:text-violet-500 hover:bg-violet-50 dark:hover:bg-violet-900/20 transition-colors"
                        title="Pisah jadi 2 baris">
                        <Scissors class="h-3.5 w-3.5" />
                      </button>
                      <button
                        @click="toggleItemDiscount(item)"
                        :class="['p-1.5 rounded-lg transition-colors',
                          openItemDiscountId === item._uid
                            ? 'bg-orange-500 text-white'
                            : getItemDiscountPerUnit(item) > 0
                              ? 'bg-orange-100 text-orange-500 dark:bg-orange-900/20 dark:text-orange-400'
                              : 'text-zinc-300 hover:text-orange-400 hover:bg-orange-50 dark:hover:bg-orange-900/20']"
                        title="Diskon per item">
                        <Tag class="h-3.5 w-3.5" />
                      </button>
                      <button
                        @click="toggleItemNote(item)"
                        :class="['p-1.5 rounded-lg transition-colors',
                          openItemNoteId === item._uid
                            ? 'bg-blue-500 text-white'
                            : item.itemNote
                              ? 'bg-blue-100 text-blue-500 dark:bg-blue-900/20 dark:text-blue-400'
                              : 'text-zinc-300 hover:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-900/20']"
                        title="Catatan item">
                        <MessageSquare class="h-3.5 w-3.5" />
                      </button>
                      <button
                        @click="removeFromCart(item)"
                        class="p-1.5 rounded-lg text-zinc-300 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
                        title="Hapus item">
                        <Trash2 class="h-3.5 w-3.5" />
                      </button>
                    </div>
                  </div>

                  <div v-if="openItemNoteId === item._uid" class="bg-blue-50/60 dark:bg-blue-900/10 border border-blue-100 dark:border-blue-800/30 rounded-xl p-2.5">
                    <textarea
                      v-model="item.itemNote"
                      rows="2"
                      placeholder="Catatan khusus item ini... (cth: tanpa bawang, extra pedas)"
                      class="w-full text-[12px] font-medium rounded-lg border border-blue-200 dark:border-blue-800/40 bg-white dark:bg-zinc-800 px-2.5 py-2 outline-none focus:ring-2 focus:ring-blue-300/40 resize-none font-sans"
                    />
                    <p v-if="item.itemNote" class="text-[10px] text-blue-600 dark:text-blue-400 font-medium mt-1">
                      ✓ Catatan akan tercetak di struk
                    </p>
                  </div>

                  <div v-if="openItemDiscountId === item._uid" class="bg-orange-50/60 dark:bg-orange-900/10 border border-orange-100 dark:border-orange-800/30 rounded-xl p-2.5 flex flex-col gap-2">
                    <div class="flex rounded-[10px] bg-white dark:bg-zinc-800 p-0.5 border border-orange-100 dark:border-orange-800/30">
                      <button @click="onItemDiscountTypeChange(item, 'FLAT')"
                        :class="['flex-1 py-1 text-[11px] font-bold rounded-[8px] transition-all', item.itemDiscountType === 'FLAT' ? 'bg-orange-500 text-white shadow-sm' : 'text-zinc-500']">
                        Rp / unit
                      </button>
                      <button @click="onItemDiscountTypeChange(item, 'PERCENT')"
                        :class="['flex-1 py-1 text-[11px] font-bold rounded-[8px] transition-all', item.itemDiscountType === 'PERCENT' ? 'bg-orange-500 text-white shadow-sm' : 'text-zinc-500']">
                        % / unit
                      </button>
                    </div>
                    <div class="relative">
                      <span class="absolute left-2.5 top-1/2 -translate-y-1/2 text-[12px] font-black text-zinc-400 select-none">
                        {{ item.itemDiscountType === 'FLAT' ? 'Rp' : '%' }}
                      </span>
                      <input
                        :value="item.itemDiscountType === 'FLAT' ? (item.itemDiscountValue ? Number(item.itemDiscountValue).toLocaleString('id-ID') : '') : item.itemDiscountValue"
                        @input="e => onItemDiscountInput(item, e)"
                        :inputmode="item.itemDiscountType === 'FLAT' ? 'numeric' : 'decimal'"
                        placeholder="0"
                        class="pl-8 h-8 w-full text-[12px] font-bold rounded-[8px] border border-orange-200 dark:border-orange-800/40 bg-white dark:bg-zinc-800 outline-none focus:ring-2 focus:ring-orange-300/40"
                      />
                    </div>
                    <p v-if="getItemDiscountTotal(item) > 0" class="text-[10px] font-bold text-orange-600 dark:text-orange-400">
                      Potongan baris ini: -{{ formatCurrency(getItemDiscountTotal(item)) }}
                    </p>
                  </div>

                  <div class="flex items-center justify-between">
                    <div class="flex items-center bg-zinc-100 dark:bg-zinc-800 rounded-lg p-0.5">
                      <button class="w-[28px] h-[28px] flex items-center justify-center text-zinc-600 dark:text-zinc-300 rounded-md hover:bg-white dark:hover:bg-zinc-700 shadow-sm transition-all" @click="decreaseQty(item)"><Minus class="h-3 w-3" /></button>
                      <input
                        type="number"
                        min="1"
                        :max="getStock(item.id) ?? undefined"
                        :value="item.qty"
                        @change="e => setQty(item, e.target.value)"
                        @focus="e => e.target.select()"
                        class="w-10 text-center text-[13px] font-black bg-transparent border-none outline-none focus:ring-0 [-moz-appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                      />
                      <button class="w-[28px] h-[28px] flex items-center justify-center text-zinc-600 dark:text-zinc-300 rounded-md hover:bg-white dark:hover:bg-zinc-700 shadow-sm transition-all" @click="increaseQty(item)"><Plus class="h-3 w-3" /></button>
                    </div>
                    <span class="text-[14px] font-black text-zinc-900 dark:text-white">{{ formatCurrency(getItemNetUnitPrice(item) * item.qty) }}</span>
                  </div>
                </div>
              </TransitionGroup>
            </div>
          </template>
        </div>

        <div class="p-4 lg:p-5 bg-white dark:bg-[#09090b] border-t border-zinc-100 dark:border-zinc-800 shrink-0 z-10 relative">
          <div class="flex items-center justify-between">
            <div class="flex flex-col">
              <span class="text-[11px] font-bold text-zinc-400 uppercase tracking-wider">Total Tagihan</span>
              <span class="text-[20px] lg:text-[24px] font-black text-primary leading-none mt-1">{{ formatCurrency(total) }}</span>
            </div>
            <Button class="h-11 lg:h-12 px-7 lg:px-8 rounded-[14px] font-bold text-[14px] shadow-lg shadow-primary/25 hover:shadow-primary/40 transition-all active:scale-95" :disabled="cart.length === 0" @click="openPayment">
              Bayar
            </Button>
          </div>
        </div>
      </div>

    </div>

    <!-- ─── Modal Pembayaran ──────────────────────────────────────────────────── -->
    <Teleport to="body">
      <Transition name="fade"><div v-if="showPayment" class="fixed inset-0 z-[60] bg-black/40 backdrop-blur-sm" @click="closePayment" /></Transition>
      <Transition name="scale">
        <div v-if="showPayment" class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none">
          <div class="bg-white dark:bg-zinc-900 rounded-[24px] shadow-2xl w-full max-w-md border border-zinc-200 dark:border-zinc-800 pointer-events-auto overflow-hidden flex flex-col">
            
            <div class="flex items-center justify-between px-6 py-4 border-b border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/50">
              <h3 class="font-black text-[15px]">Detail Pembayaran</h3>
              <button @click="closePayment" class="p-1.5 rounded-full hover:bg-zinc-200 dark:hover:bg-zinc-800 text-zinc-500 transition-colors"><X class="h-4 w-4" /></button>
            </div>
            
            <div class="px-6 py-5 flex flex-col gap-5 overflow-y-auto max-h-[70vh] no-scrollbar">
              <div class="flex flex-col gap-3">
                <div class="flex items-center justify-between text-[14px] font-bold text-zinc-700 dark:text-zinc-300">
                  <span>Subtotal Pesanan</span>
                  <span>{{ formatCurrency(subtotal) }}</span>
                </div>

                <div v-if="totalItemDiscount > 0" class="flex items-center justify-between text-[12px] font-bold text-orange-600 dark:text-orange-400">
                  <span class="flex items-center gap-1.5"><Tag class="h-3 w-3" /> Diskon Per Item</span>
                  <span>-{{ formatCurrency(totalItemDiscount) }}</span>
                </div>
                
                <div class="bg-zinc-50 dark:bg-zinc-800/60 rounded-[16px] p-3 border border-zinc-200/60 dark:border-zinc-700/50">
                  <div v-if="appliedVoucher" class="flex items-center justify-between mb-2">
                    <div class="flex items-center gap-3">
                      <div class="bg-emerald-100 text-emerald-600 p-2 rounded-xl"><Ticket class="w-[18px] h-[18px]" /></div>
                      <div class="flex flex-col">
                        <span class="text-[13px] font-black text-emerald-600">{{ appliedVoucher.code }}</span>
                        <span class="text-[10px] font-bold text-zinc-500">{{ appliedVoucher.name }}</span>
                      </div>
                    </div>
                    <div class="flex items-center gap-3">
                      <span class="text-[13px] font-black text-red-500">-{{ formatCurrency(discountAmount - manualDiscountAmount) }}</span>
                      <button class="p-1.5 text-zinc-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors" @click="removeVoucher"><X class="w-[14px] h-[14px]" /></button>
                    </div>
                  </div>
                    
                  <div class="flex gap-2 relative">
                    <div class="relative flex-1">
                      <Input 
                        ref="voucherInputRef"
                        v-model="voucherCode" 
                        placeholder="Masukkan kode voucer..." 
                        class="w-full h-[38px] text-[13px] font-semibold bg-white dark:bg-zinc-900 border-none shadow-sm rounded-xl px-3 placeholder:font-medium" 
                        @focus="showVoucherDropdown = true"
                        @blur="handleVoucherBlur"
                        @keydown="handleVoucherKeyDown"
                        @keyup.enter="focusedVoucherIndex === -1 ? applyVoucher() : null"
                      />

                      <Transition name="dropdown">
                        <div 
                          v-if="showVoucherDropdown && filteredVouchers.length > 0" 
                          class="absolute left-0 right-0 z-50 top-[calc(100%+4px)] bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 shadow-xl rounded-xl max-h-[150px] overflow-y-auto p-1 flex flex-col gap-0.5 no-scrollbar"
                        >
                          <button
                            v-for="(v, index) in filteredVouchers"
                            :key="v.id"
                            type="button"
                            class="w-full text-left px-3 py-2 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800/80 transition-colors flex justify-between items-center group"
                            :class="{ 'bg-zinc-100 dark:bg-zinc-800 text-primary': index === focusedVoucherIndex }"
                            @mousedown="selectVoucher(v.code)"  
                          >
                            <div class="flex flex-col min-w-0">
                              <span class="text-[12px] font-black text-zinc-800 dark:text-zinc-200 uppercase group-hover:text-primary transition-colors">{{ v.code }}</span>
                              <span class="text-[10px] text-zinc-400 dark:text-zinc-500 truncate max-w-[140px]">{{ v.name }}</span>
                            </div>
                            <span class="text-[11px] font-bold text-emerald-600 bg-emerald-50 dark:bg-emerald-950/30 px-1.5 py-0.5 rounded-md shrink-0">
                              {{ v.discountType === 'PERCENT' || v.discount_type === 'PERCENT' ? `${v.discountValue ?? v.discount_value}%` : `-${formatCurrency(v.discountValue ?? v.discount_value)}` }}
                            </span>
                          </button>
                        </div>
                      </Transition>
                    </div>

                    <Button variant="secondary" class="h-[38px] font-bold px-4 rounded-xl text-[13px] shadow-sm shrink-0" @click="applyVoucher">Pakai</Button>
                  </div>
                </div>

                <div class="flex items-center justify-between text-[18px] font-black text-zinc-900 dark:text-white pt-2 border-t border-zinc-100 dark:border-zinc-800">
                  <span>Total Tagihan</span>
                  <span class="text-primary">{{ formatCurrency(total) }}</span>
                </div>
              </div>

              <div class="flex flex-col gap-2.5">
                <div class="flex items-center gap-2">
                  <Tag class="h-[13px] w-[13px] text-orange-500" />
                  <label class="text-[11px] font-bold text-zinc-400 uppercase tracking-wider">Diskon Manual <span class="normal-case font-normal">(opsional)</span></label>
                </div>
                <div class="flex rounded-[12px] bg-zinc-100 dark:bg-zinc-800/80 p-0.5">
                  <button @click="onManualDiscountTypeChange('FLAT')"
                    :class="['flex-1 py-1.5 text-[12px] font-bold flex items-center justify-center gap-1.5 rounded-[10px] transition-all',
                      manualDiscountType === 'FLAT'
                        ? 'bg-white dark:bg-zinc-700 text-zinc-900 dark:text-white shadow-sm'
                        : 'text-zinc-500 hover:text-zinc-700']">
                    <Tag class="h-[11px] w-[11px]" /> Nominal (Rp)
                  </button>
                  <button @click="onManualDiscountTypeChange('PERCENT')"
                    :class="['flex-1 py-1.5 text-[12px] font-bold flex items-center justify-center gap-1.5 rounded-[10px] transition-all',
                      manualDiscountType === 'PERCENT'
                        ? 'bg-white dark:bg-zinc-700 text-zinc-900 dark:text-white shadow-sm'
                        : 'text-zinc-500 hover:text-zinc-700']">
                    <Percent class="h-[11px] w-[11px]" /> Persentase (%)
                  </button>
                </div>
                <div class="relative">
                  <span class="absolute left-3.5 top-1/2 -translate-y-1/2 text-[14px] font-black text-zinc-400 select-none">
                    {{ manualDiscountType === 'FLAT' ? 'Rp' : '%' }}
                  </span>
                  <input
                    v-if="manualDiscountType === 'FLAT'"
                    :value="manualDiscountDisplay"
                    @input="onManualDiscountInput"
                    inputmode="numeric"
                    placeholder="0"
                    class="pl-10 h-11 w-full text-base font-bold rounded-[12px] border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 shadow-sm outline-none focus:ring-2 focus:ring-primary/20 tracking-wide"
                  />
                  <Input
                    v-else
                    v-model="manualDiscountValue"
                    type="number"
                    min="0"
                    max="100"
                    class="pl-10 h-11 text-base font-bold rounded-[12px] border-zinc-200 shadow-sm"
                    placeholder="0 — 100"
                  />
                </div>
                <div v-if="manualDiscountAmount > 0"
                  class="flex items-center justify-between px-3 py-2.5 rounded-xl bg-orange-50 dark:bg-orange-900/20 border border-orange-100 dark:border-orange-800/40">
                  <span class="text-[12px] font-bold text-orange-600 dark:text-orange-400 flex items-center gap-1.5">
                    <Tag class="h-3.5 w-3.5" /> Potongan Diskon
                  </span>
                  <span class="text-[14px] font-black text-red-500">-{{ formatCurrency(manualDiscountAmount) }}</span>
                </div>
                <Input
                  v-model="manualDiscountNote"
                  placeholder="Keterangan (opsional, cth: diskon pelanggan tetap)"
                  class="h-9 text-[12px] rounded-[10px] border-zinc-200"
                />
              </div>

              <div class="flex flex-col gap-2">
                <label class="text-[11px] font-bold text-zinc-400 uppercase tracking-wider">Nama Pembeli <span class="normal-case font-normal">(opsional)</span></label>
                <Input v-model="buyerName" placeholder="Contoh: Budi Santoso" class="h-11 rounded-[12px]" />
              </div>

              <div class="flex flex-col gap-2">
                <label class="text-[11px] font-bold text-zinc-400 uppercase tracking-wider">Metode Pembayaran</label>
                <div class="flex rounded-[14px] bg-zinc-100 dark:bg-zinc-800/80 p-1">
                  <button @click="isSplitPayment = false; payMethod = 'cash'" :class="['flex-1 py-2.5 text-[13px] font-bold flex items-center justify-center gap-2 rounded-[10px] transition-all', !isSplitPayment && payMethod === 'cash' ? 'bg-white dark:bg-zinc-700 text-zinc-900 dark:text-white shadow-sm' : 'text-zinc-500 hover:text-zinc-700']">
                    <Banknote class="h-[14px] w-[14px]" />Tunai
                  </button>
                  <button @click="isSplitPayment = false; payMethod = 'transfer'" :class="['flex-1 py-2.5 text-[13px] font-bold flex items-center justify-center gap-2 rounded-[10px] transition-all', !isSplitPayment && payMethod === 'transfer' ? 'bg-white dark:bg-zinc-700 text-zinc-900 dark:text-white shadow-sm' : 'text-zinc-500 hover:text-zinc-700']">
                    <ArrowRightLeft class="h-[14px] w-[14px]" />Transfer
                  </button>
                  <button @click="isSplitPayment = true" :class="['flex-1 py-2.5 text-[11px] font-bold flex items-center justify-center gap-1.5 rounded-[10px] transition-all', isSplitPayment ? 'bg-white dark:bg-zinc-700 text-zinc-900 dark:text-white shadow-sm' : 'text-zinc-500 hover:text-zinc-700']">
                    <Plus class="h-[13px] w-[13px]" />Split
                  </button>
                </div>
              </div>

              <div v-if="isSplitPayment" class="flex flex-col gap-3 animate-in fade-in slide-in-from-bottom-2 duration-200">
                <div class="flex items-center justify-between px-3 py-2 rounded-xl bg-blue-50 dark:bg-blue-900/20 border border-blue-100 dark:border-blue-800/40 text-[11px]">
                  <span class="font-bold text-blue-700 dark:text-blue-400">Total Tagihan</span>
                  <span class="font-black text-blue-700 dark:text-blue-400">{{ formatCurrency(total) }}</span>
                </div>
                <div class="space-y-1.5">
                  <label class="text-[12px] font-bold text-zinc-600 flex items-center gap-1.5"><Banknote class="h-3.5 w-3.5" /> Nominal Tunai</label>
                  <div class="relative">
                    <span class="absolute left-3.5 top-1/2 -translate-y-1/2 text-[14px] font-black text-zinc-400">Rp</span>
                    <input v-model="splitCashAmount" type="number" min="0" placeholder="0"
                      class="pl-10 h-11 w-full text-base font-bold rounded-[12px] border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 shadow-sm outline-none focus:ring-2 focus:ring-primary/20" />
                  </div>
                </div>
                <div class="space-y-1.5">
                  <label class="text-[12px] font-bold text-zinc-600 flex items-center gap-1.5"><ArrowRightLeft class="h-3.5 w-3.5" /> Nominal Transfer</label>
                  <div class="relative">
                    <span class="absolute left-3.5 top-1/2 -translate-y-1/2 text-[14px] font-black text-zinc-400">Rp</span>
                    <input v-model="splitTransferAmount" type="number" min="0" placeholder="0"
                      class="pl-10 h-11 w-full text-base font-bold rounded-[12px] border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 shadow-sm outline-none focus:ring-2 focus:ring-primary/20" />
                  </div>
                </div>
                <div class="grid grid-cols-2 gap-2">
                  <div class="space-y-1">
                    <label class="text-[11px] font-semibold text-zinc-500">Bank</label>
                    <input v-model="splitBankName" placeholder="BCA, Mandiri..." class="h-9 w-full rounded-[10px] border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 px-3 text-sm outline-none focus:ring-2 focus:ring-primary/20" />
                  </div>
                  <div class="space-y-1">
                    <label class="text-[11px] font-semibold text-zinc-500">No. Referensi *</label>
                    <input v-model="splitReferenceNo" placeholder="Nomor transfer" class="h-9 w-full rounded-[10px] border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 px-3 text-sm outline-none focus:ring-2 focus:ring-primary/20" />
                  </div>
                </div>
                <div v-if="splitTotal > 0" :class="['flex items-center justify-between px-3 py-2 rounded-xl text-[12px]',
                  splitValid ? 'bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-100 dark:border-emerald-800/40' : 'bg-red-50 dark:bg-red-900/20 border border-red-100 dark:border-red-800/40']">
                  <span :class="splitValid ? 'text-emerald-700 dark:text-emerald-400 font-bold' : 'text-red-600 font-bold'">
                    {{ splitValid ? (splitChange > 0 ? `Kembalian` : 'Pas') : 'Kurang' }}
                  </span>
                  <span :class="splitValid ? 'font-black text-emerald-700 dark:text-emerald-400' : 'font-black text-red-600'">
                    {{ splitValid ? (splitChange > 0 ? formatCurrency(splitChange) : '✓') : formatCurrency(total - splitTotal) }}
                  </span>
                </div>
              </div>

              <div v-if="!isSplitPayment" class="flex flex-col gap-4 animate-in fade-in slide-in-from-bottom-2 duration-200">
                <template v-if="payMethod === 'cash'">
                  <div class="space-y-2">
                    <label class="text-[13px] font-bold text-zinc-700 dark:text-zinc-300">Uang Diterima</label>
                    <div class="relative">
                      <span class="absolute left-3.5 top-1/2 -translate-y-1/2 text-[15px] font-black text-zinc-400">Rp</span>
                      <input
                        :value="cashTenderedDisplay"
                        @input="onCashTenderedInput"
                        inputmode="numeric"
                        placeholder="0"
                        class="pl-10 h-12 w-full text-lg font-black rounded-[14px] border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 shadow-sm outline-none focus:ring-2 focus:ring-primary/20"
                      />
                    </div>
                  </div>
                  
                  <div class="flex flex-wrap gap-2 pt-1">
                    <button v-for="amt in [total, Math.ceil(total/10000)*10000, Math.ceil(total/50000)*50000, Math.ceil(total/100000)*100000].filter((v,i,a) => a.indexOf(v) === i)" :key="amt" @click="setCashTendered(amt)"
                      class="px-3.5 py-2 text-[13px] font-bold rounded-xl border border-zinc-200 dark:border-zinc-700 hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors">
                      {{ formatCurrency(amt) }}
                    </button>
                  </div>

                  <div v-if="Number(cashTendered) >= total" class="flex items-center justify-between p-4 rounded-[16px] bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-100 dark:border-emerald-800/40 mt-2">
                    <span class="text-[13px] font-bold text-emerald-700 dark:text-emerald-400">Kembalian</span>
                    <span class="text-xl font-black text-emerald-700 dark:text-emerald-400">{{ formatCurrency(changeDue) }}</span>
                  </div>
                </template>
                <template v-else>
                  <div class="space-y-2">
                    <label class="text-[13px] font-bold text-zinc-700">Nama Bank</label>
                    <Input v-model="bankName" placeholder="BCA, Mandiri, BNI..." class="h-11 rounded-[12px]" />
                  </div>
                  <div class="space-y-2">
                    <label class="text-[13px] font-bold text-zinc-700">No. Referensi *</label>
                    <Input v-model="referenceNo" placeholder="Nomor referensi transfer" class="h-11 rounded-[12px]" />
                  </div>
                </template>
              </div>
            </div>

            <div class="px-6 py-5 border-t border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/50 mt-auto">
              <Button class="w-full h-12 font-bold text-[14px] rounded-[14px] shadow-lg shadow-primary/20" @click="checkout"
                :disabled="processingCheckout || (isSplitPayment && !splitValid)">
                <Loader2 v-if="processingCheckout" class="h-4 w-4 mr-2 animate-spin" />
                <Check v-else class="h-[18px] w-[18px] mr-2" /> Konfirmasi Pembayaran
              </Button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ─── Modal Struk ───────────────────────────────────────────────────────── -->
    <Teleport to="body">
      <Transition name="fade"><div v-if="showReceipt" class="fixed inset-0 z-[70] bg-black/50 backdrop-blur-sm" @click="closeReceipt" /></Transition>
      <Transition name="scale">
        <div v-if="showReceipt && lastOrder" class="fixed inset-0 z-[70] flex items-center justify-center p-4 pointer-events-none">
          <div class="bg-white dark:bg-zinc-900 rounded-[24px] shadow-2xl w-full max-w-sm border border-zinc-200 dark:border-zinc-800 pointer-events-auto overflow-hidden flex flex-col relative">
            <div class="flex flex-col items-center px-6 pt-8 pb-5 border-b border-dashed border-zinc-200 dark:border-zinc-700">
              <div class="absolute top-4 right-4">
                <button @click="closeReceipt" class="p-1.5 rounded-full hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-400 transition-colors">
                  <X class="h-4 w-4" />
                </button>
              </div>
              <div :class="['w-14 h-14 rounded-full flex items-center justify-center mb-4',
                lastOrder.payments?.[0]?.method === 'TRANSFER' && lastOrder.payments[0].status !== 'VERIFIED'
                  ? 'bg-amber-100 dark:bg-amber-900/30'
                  : 'bg-emerald-100 dark:bg-emerald-900/30']">
                <Check :class="['h-7 w-7',
                  lastOrder.payments?.[0]?.method === 'TRANSFER' && lastOrder.payments[0].status !== 'VERIFIED'
                    ? 'text-amber-600 dark:text-amber-400'
                    : 'text-emerald-600 dark:text-emerald-400']" />
              </div>
              <h3 class="text-lg font-black text-zinc-900 dark:text-white">
                {{ lastOrder.payments?.[0]?.method === 'TRANSFER' && lastOrder.payments[0].status !== 'VERIFIED'
                  ? 'Menunggu Konfirmasi'
                  : 'Transaksi Berhasil!' }}
              </h3>
              <p class="text-xs text-zinc-500 font-mono mt-1">{{ lastOrder.orderNumber }}</p>
            </div>
            <div class="px-6 py-5 flex flex-col gap-3">
              <div class="flex justify-between items-center text-sm">
                <span class="text-zinc-500 font-medium">Total Dibayar</span>
                <span class="font-black text-lg text-zinc-900 dark:text-white">{{ formatCurrency(lastOrder.total) }}</span>
              </div>
              <template v-if="lastOrder.payments?.[0]?.method === 'CASH'">
                <div class="flex justify-between items-center text-sm">
                  <span class="text-zinc-500 font-medium">Uang Diterima</span>
                  <span class="font-bold">{{ formatCurrency(lastOrder.payments[0].cashTendered) }}</span>
                </div>
                <div class="flex justify-between items-center text-sm p-3 rounded-xl bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-100 dark:border-emerald-800/40">
                  <span class="text-emerald-700 dark:text-emerald-400 font-bold">Kembalian</span>
                  <span class="font-black text-emerald-700 dark:text-emerald-400">{{ formatCurrency(lastOrder.payments[0].changeDue) }}</span>
                </div>
              </template>
              <template v-if="lastOrder.payments?.[0]?.method === 'TRANSFER'">
                <div v-if="lastOrder.payments[0].status === 'VERIFIED'"
                  class="flex justify-between items-center text-sm p-3 rounded-xl bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-100 dark:border-emerald-800/40">
                  <span class="text-emerald-700 dark:text-emerald-400 font-bold">Status Transfer</span>
                  <span class="font-black text-emerald-700 dark:text-emerald-400">✓ Terkonfirmasi</span>
                </div>
                <div v-else
                  class="flex justify-between items-center text-sm p-3 rounded-xl bg-amber-50 dark:bg-amber-900/20 border border-amber-100 dark:border-amber-800/40">
                  <span class="text-amber-700 dark:text-amber-400 font-bold">Status Transfer</span>
                  <span class="font-black text-amber-700 dark:text-amber-400">Menunggu Konfirmasi</span>
                </div>
              </template>
            </div>
            <div class="px-6 pb-6 flex flex-col gap-2.5">
              <button
                v-if="lastOrder.payments?.[0]?.method === 'TRANSFER' && lastOrder.payments[0].status !== 'VERIFIED'"
                @click="confirmTransferFromReceipt"
                :disabled="confirmingTransfer"
                class="w-full h-11 font-bold rounded-[14px] bg-blue-600 hover:bg-blue-700 text-white flex items-center justify-center gap-2 transition-colors disabled:opacity-50 text-sm"
              >
                <Loader2 v-if="confirmingTransfer" class="h-4 w-4 animate-spin" />
                <Check v-else class="h-4 w-4" />
                Konfirmasi Pembayaran Transfer
              </button>

              <button @click="printReceipt" class="w-full h-11 font-bold rounded-[14px] border border-zinc-200 dark:border-zinc-700 flex items-center justify-center gap-2 hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors text-sm">
                <Printer class="h-4 w-4" /> Cetak Struk
              </button>
              <Button class="w-full h-11 font-bold rounded-[14px] gap-2" @click="copyOrderSummary">
                <ReceiptText class="h-4 w-4" /> Salin Catatan Riwayat
              </Button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <StrukPrint
      ref="struk"
      v-if="lastOrder"
      :order="lastOrder"
      :branch-name="selectedBranch?.name"
      store-name="Gaptek"
    />

    <ShiftModal
      :show="showShiftModal"
      :mode="shiftModalMode"
      :branch-id="selectedBranchId"
      :branch-name="selectedBranch?.name || authStore.user?.branchName"
      :active-shift="activeShift"
      :closed-shift="closedShiftResult"
      @close="showShiftModal = false; closedShiftResult = null"
      @submitted="handleShiftSubmitted"
    />
  </AppLayout>
</template>

<style scoped>
.pos-root {
  display: flex;
  flex-direction: column;
  margin: -20px;
  height: calc(100dvh - 48px);
  overflow: hidden;
  background: #f4f4f5;
}
@media (min-width: 1024px) {
  .pos-root { flex-direction: row; background: #09090b; }
}
:root.dark .pos-root { background: #09090b; }

.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }

.dropdown-enter-active, .dropdown-leave-active {
  transition: all 0.15s cubic-bezier(0.16, 1, 0.3, 1);
}
.dropdown-enter-from, .dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.97);
}

.list-enter-active, .list-leave-active { transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
.list-enter-from, .list-leave-to { opacity: 0; transform: translateY(10px) scale(0.98); }

.slide-down-enter-active, .slide-down-leave-active { transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1); }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-8px); max-height: 0; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.scale-enter-active, .scale-leave-active { transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1); }
.scale-enter-from, .scale-leave-to { opacity: 0; transform: scale(0.95) translateY(10px); }
</style>   
