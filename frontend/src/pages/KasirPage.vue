<script setup>
import { ref, computed, onMounted } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/Input.vue'
import { Search, ShoppingCart, Plus, Minus, Trash2, CreditCard, ShoppingBag, ArrowLeft, Banknote, ArrowRightLeft, X, Ticket, Check, Scan } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import api from '@/lib/api'

const { toast } = useToast()

const products = ref([])
const categories = ref([])
const vouchers = ref([])
const loading = ref(false)

async function fetchProducts() {
  loading.value = true
  try {
    const res = await api.get('/api/v1/products')
    const data = res.data.data
    // Backend returns a List if not using Pageable properly
    products.value = (Array.isArray(data) ? data : (data.content || [])).filter(p => p.isActive)
  } catch (err) {
    toast.error('Gagal memuat produk')
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const res = await api.get('/api/v1/categories')
    categories.value = res.data.data || []
  } catch (_) {}
}

async function fetchVouchers() {
  try {
    const res = await api.get('/api/v1/vouchers')
    vouchers.value = res.data.data || []
  } catch (_) {}
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
  fetchVouchers()
})

const searchQuery = ref('')
const activeCategory = ref('Semua')
const orderType = ref('Dine In')

const uniqueCategories = computed(() => {
  return ['Semua', ...categories.value.map(c => c.name).sort()]
})

const filteredProducts = computed(() => {
  let result = products.value
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(p => p.name.toLowerCase().includes(q) || (p.sku && p.sku.toLowerCase().includes(q)))
  }
  if (activeCategory.value !== 'Semua') {
    result = result.filter(p => (p.categoryName || p.category?.name || 'Lainnya') === activeCategory.value)
  }
  return result
})

const cart = ref([])

function addToCart(product) {
  const existing = cart.value.find(item => item.id === product.id)
  if (existing) {
    existing.qty++
  } else {
    cart.value.push({ ...product, qty: 1 })
  }
}

function getCartQty(id) {
  const item = cart.value.find(i => i.id === id)
  return item ? item.qty : 0
}

function increaseQty(item) {
  item.qty++
}
function decreaseQty(item) { if (item.qty > 1) { item.qty-- } else { removeFromCart(item) } }
function removeFromCart(item) {
  const idx = cart.value.findIndex(i => i.id === item.id)
  if (idx !== -1) cart.value.splice(idx, 1)
}

function formatCurrency(value) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(value)
}

const totalItems = computed(() => cart.value.reduce((s, i) => s + i.qty, 0))
const subtotal   = computed(() => cart.value.reduce((s, i) => s + (i.price || i.basePrice || 0) * i.qty, 0))

// Voucher
const voucherCode = ref('')
const appliedVoucher = ref(null)

function applyVoucher() {
  const v = vouchers.value.find(v => v.code === voucherCode.value.toUpperCase())
  if (v) { appliedVoucher.value = v; toast.success(`Voucher "${v.name}" diterapkan!`) }
  else { toast.error('Kode voucher tidak valid.'); appliedVoucher.value = null }
}
function removeVoucher() { appliedVoucher.value = null; voucherCode.value = '' }

const discountAmount = computed(() => {
  if (!appliedVoucher.value) return 0
  const v = appliedVoucher.value
  if (v.discountType === 'percent') { const d = subtotal.value * v.discountValue / 100; return v.maxDiscount ? Math.min(d, v.maxDiscount) : d }
  return v.discountValue
})
const total = computed(() => Math.max(0, subtotal.value - discountAmount.value))

// Payment Dialog
const showPayment = ref(false)
const payMethod = ref('cash')
const cashTendered = ref('')
const bankName = ref('')
const referenceNo = ref('')
const changeDue = computed(() => Math.max(0, (Number(cashTendered.value) || 0) - total.value))

function openPayment() { if (!cart.value.length) return; showPayment.value = true; payMethod.value = 'cash'; cashTendered.value = ''; bankName.value = ''; referenceNo.value = '' }
function closePayment() { showPayment.value = false }

function checkout() {
  if (payMethod.value === 'cash' && (Number(cashTendered.value) || 0) < total.value) { toast.error('Uang yang diberikan kurang!'); return }
  if (payMethod.value === 'transfer' && !referenceNo.value) { toast.error('Nomor referensi wajib diisi!'); return }
  const orderNum = `ORD-${new Date().toISOString().slice(0,10).replace(/-/g,'')}-${String(Math.floor(Math.random()*9999)+1).padStart(4,'0')}`
  toast.success(`Pembayaran berhasil! ${orderNum} — ${formatCurrency(total.value)}`)
  cart.value = []; appliedVoucher.value = null; voucherCode.value = ''; showPayment.value = false
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
      
      <!-- ── Product panel ── -->
      <div class="flex flex-col bg-white dark:bg-[#09090b] flex-1 min-w-0 min-h-0">
        
        <!-- Header -->
        <div class="px-3 pt-3 lg:px-5 lg:pt-5 shrink-0 z-10">
          <div class="hidden lg:block mb-4">
            <h1 class="text-xl font-black text-zinc-900 dark:text-white tracking-tight">Kasir</h1>
            <p class="text-xs text-zinc-500 font-medium mt-0.5">Sistem Point of Sale</p>
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

          <div class="flex bg-zinc-100 dark:bg-zinc-800/60 p-1 rounded-xl mb-3 lg:mb-4">
            <button v-for="type in ['Dine In', 'Take Away', 'Delivery']" :key="type"
              class="flex-1 text-[13px] font-bold py-2 rounded-[10px] transition-all"
              :class="orderType === type ? 'bg-white dark:bg-zinc-700 text-zinc-900 dark:text-white shadow-sm' : 'text-zinc-500 dark:text-zinc-400 hover:text-zinc-700'"
              @click="orderType = type">
              {{ type }}
            </button>
          </div>

          <div class="flex gap-2 overflow-x-auto no-scrollbar pb-2">
            <button v-for="cat in uniqueCategories" :key="cat"
              class="px-4 py-1.5 rounded-full text-[13px] font-bold whitespace-nowrap transition-all border"
              :class="activeCategory === cat ? 'bg-zinc-900 border-zinc-900 text-white dark:bg-zinc-100 dark:border-zinc-100 dark:text-zinc-900 shadow-md shadow-zinc-900/20' : 'bg-white border-zinc-200 text-zinc-600 dark:bg-zinc-900 dark:border-zinc-800 dark:text-zinc-400 hover:bg-zinc-50'"
              @click="activeCategory = cat">
              {{ cat }}
            </button>
          </div>
        </div>

        <!-- Grid -->
        <div class="flex-1 min-h-0 overflow-y-auto no-scrollbar">
          <div v-if="loading" class="h-full flex flex-col items-center justify-center text-zinc-400 gap-3">
             <Loader2 class="h-10 w-10 animate-spin opacity-20 mb-3" />
             <p class="text-[13px] font-medium italic">Menghubungkan ke sistem...</p>
          </div>
          <div v-else-if="filteredProducts.length === 0" class="h-full flex flex-col items-center justify-center text-zinc-400">
            <ShoppingBag class="h-12 w-12 opacity-20 mb-3" />
            <p class="text-[13px] font-medium">Produk tidak ditemukan.</p>
          </div>
          <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-3 xl:grid-cols-4 gap-3 p-3 lg:p-5">
            <div v-for="p in filteredProducts" :key="p.id"
              class="relative flex flex-col bg-white dark:bg-zinc-900/80 rounded-[20px] overflow-hidden cursor-pointer transition-transform active:scale-[0.98] border border-zinc-200/60 dark:border-zinc-800 shadow-sm hover:shadow-md"
              :class="{ 'ring-2 ring-primary ring-offset-2 dark:ring-offset-zinc-900': getCartQty(p.id) > 0, 'opacity-60 grayscale-[0.5]': p.stock <= 0 }"
              @click="p.stock > 0 && addToCart(p)">
              
              <div v-if="getCartQty(p.id) > 0" class="absolute top-2.5 right-2.5 bg-primary text-primary-foreground text-[11px] font-black px-2.5 py-0.5 rounded-full shadow-md z-10">
                {{ getCartQty(p.id) }}
              </div>
              
              <div class="aspect-[4/3] lg:aspect-square bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center overflow-hidden shrink-0">
                <img v-if="p.imageUrl" :src="p.imageUrl" class="w-full h-full object-cover" />
                <div v-else class="w-full h-full flex items-center justify-center text-3xl font-black opacity-80" :style="avatarStyle(p.name)">{{ p.name.charAt(0) }}</div>
              </div>
              
              <div class="p-3 flex flex-col gap-1.5 flex-1">
                <div class="flex items-center justify-between">
                  <span class="text-[9px] font-bold text-zinc-400 uppercase tracking-widest">{{ p.sku || 'N/A' }}</span>
                  <span class="text-[9px] font-black px-1.5 py-0.5 rounded-md" :class="p.stock > 0 ? 'bg-emerald-100/50 text-emerald-600 dark:bg-emerald-900/30' : 'bg-red-100/50 text-red-600 dark:bg-red-900/30'">
                    {{ p.stock > 0 ? `STOK ${p.stock}` : 'HABIS' }}
                  </span>
                </div>
                <h4 class="text-[13px] font-bold text-zinc-800 dark:text-zinc-200 line-clamp-2 leading-tight">{{ p.name }}</h4>
                <div class="mt-auto pt-1 text-[15px] font-black text-zinc-900 dark:text-white">{{ formatCurrency(p.price) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ── Cart panel ── -->
      <div class="flex flex-col bg-white dark:bg-[#09090b] border-t lg:border-t-0 lg:border-l border-zinc-200 dark:border-zinc-800 max-h-[42vh] lg:max-h-none lg:w-[380px] shrink-0 shadow-[0_-10px_40px_-15px_rgba(0,0,0,0.05)] lg:shadow-none z-20 rounded-t-[24px] lg:rounded-none">
        
        <div class="flex items-center justify-between px-4 py-3.5 lg:p-5 border-b border-zinc-100 dark:border-zinc-800/60 shrink-0">
          <div class="flex items-center gap-2">
            <h2 class="text-[15px] lg:text-lg font-black text-zinc-900 dark:text-white">Pesanan</h2>
            <span class="text-[10px] font-black bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-300 px-2.5 py-1 rounded-full">{{ totalItems }}</span>
          </div>
          <button v-if="cart.length" class="text-[11px] font-bold text-red-500 hover:text-red-600 bg-red-50 dark:bg-red-500/10 px-2.5 py-1 rounded-md" @click="cart = []">Kosongkan</button>
        </div>

        <div class="flex-1 min-h-0 overflow-y-auto no-scrollbar bg-zinc-50/50 dark:bg-[#09090b]/50 p-3 lg:p-5">
          <div v-if="!cart.length" class="h-full flex flex-col items-center justify-center text-zinc-400 gap-3">
             <div class="w-14 h-14 rounded-full bg-zinc-100 dark:bg-zinc-800/80 flex items-center justify-center"><ShoppingCart class="h-6 w-6 opacity-40" /></div>
             <p class="text-[13px] font-semibold">Belum ada pesanan</p>
          </div>
          <div v-else class="flex flex-col gap-2.5">
            <TransitionGroup name="list">
              <div v-for="item in cart" :key="item.id" class="bg-white dark:bg-zinc-900 border border-zinc-200/60 dark:border-zinc-800 rounded-[16px] p-3 flex flex-col gap-3 shadow-sm hover:border-zinc-300 transition-colors">
                <div class="flex items-start justify-between gap-3">
                  <div class="flex-1 min-w-0 pt-0.5">
                    <h4 class="text-[13px] font-bold text-zinc-800 dark:text-zinc-200 leading-snug truncate">{{ item.name }}</h4>
                    <p class="text-[11px] font-bold text-primary mt-0.5">{{ formatCurrency(item.price) }}</p>
                  </div>
                  <button class="shrink-0 text-zinc-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 p-1.5 rounded-lg transition-colors" @click="removeFromCart(item)">
                    <Trash2 class="h-[14px] w-[14px]" />
                  </button>
                </div>
                <div class="flex items-center justify-between">
                  <div class="flex items-center bg-zinc-100 dark:bg-zinc-800 rounded-lg p-0.5">
                    <button class="w-[28px] h-[28px] flex items-center justify-center text-zinc-600 dark:text-zinc-300 rounded-md hover:bg-white dark:hover:bg-zinc-700 shadow-sm transition-all" @click="decreaseQty(item)"><Minus class="h-3 w-3" /></button>
                    <span class="w-8 text-center text-[13px] font-black">{{ item.qty }}</span>
                    <button class="w-[28px] h-[28px] flex items-center justify-center text-zinc-600 dark:text-zinc-300 rounded-md hover:bg-white dark:hover:bg-zinc-700 shadow-sm transition-all" @click="increaseQty(item)"><Plus class="h-3 w-3" /></button>
                  </div>
                  <span class="text-[14px] font-black text-zinc-900 dark:text-white">{{ formatCurrency(item.price * item.qty) }}</span>
                </div>
              </div>
            </TransitionGroup>
          </div>
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

    <!-- Payment Dialog -->
    <Teleport to="body">
      <Transition name="fade"><div v-if="showPayment" class="fixed inset-0 z-[60] bg-black/40 backdrop-blur-sm" @click="closePayment" /></Transition>
      <Transition name="scale">
        <div v-if="showPayment" class="fixed inset-0 z-[60] flex items-center justify-center p-4 pointer-events-none">
          <div class="bg-white dark:bg-zinc-900 rounded-[24px] shadow-2xl w-full max-w-md border border-zinc-200 dark:border-zinc-800 pointer-events-auto overflow-hidden flex flex-col">
            
            <!-- Modal Header -->
            <div class="flex items-center justify-between px-6 py-4 border-b border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/50">
              <h3 class="font-black text-[15px]">Detail Pembayaran</h3>
              <button @click="closePayment" class="p-1.5 rounded-full hover:bg-zinc-200 dark:hover:bg-zinc-800 text-zinc-500 transition-colors"><X class="h-4 w-4" /></button>
            </div>
            
            <div class="px-6 py-5 flex flex-col gap-5 overflow-y-auto max-h-[70vh] no-scrollbar">
              
              <!-- Bill Summary & Voucher -->
              <div class="flex flex-col gap-3">
                <div class="flex items-center justify-between text-[14px] font-bold text-zinc-700 dark:text-zinc-300">
                  <span>Subtotal Pesanan</span>
                  <span>{{ formatCurrency(subtotal) }}</span>
                </div>
                
                <!-- Voucher Block -->
                <div class="bg-zinc-50 dark:bg-zinc-800/60 rounded-[16px] p-3 border border-zinc-200/60 dark:border-zinc-700/50">
                  <div v-if="appliedVoucher" class="flex items-center justify-between">
                    <div class="flex items-center gap-3">
                      <div class="bg-emerald-100 text-emerald-600 p-2 rounded-xl"><Ticket class="w-[18px] h-[18px]" /></div>
                      <div class="flex flex-col">
                        <span class="text-[13px] font-black text-emerald-600">{{ appliedVoucher.code }}</span>
                        <span class="text-[10px] font-bold text-zinc-500">{{ appliedVoucher.name }}</span>
                      </div>
                    </div>
                    <div class="flex items-center gap-3">
                      <span class="text-[13px] font-black text-red-500">-{{ formatCurrency(discountAmount) }}</span>
                      <button class="p-1.5 text-zinc-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors" @click="removeVoucher"><X class="w-[14px] h-[14px]" /></button>
                    </div>
                  </div>
                  <div v-else class="flex gap-2">
                    <Input v-model="voucherCode" placeholder="Masukkan kode voucher..." class="h-[38px] text-[13px] font-semibold bg-white dark:bg-zinc-900 border-none shadow-sm rounded-xl px-3 placeholder:font-medium" @keyup.enter="applyVoucher" />
                    <Button variant="secondary" class="h-[38px] font-bold px-4 rounded-xl text-[13px] shadow-sm" @click="applyVoucher">Pakai</Button>
                  </div>
                </div>

                <div class="flex items-center justify-between text-[18px] font-black text-zinc-900 dark:text-white pt-2 border-t border-zinc-100 dark:border-zinc-800">
                  <span>Total Tagihan</span>
                  <span class="text-primary">{{ formatCurrency(total) }}</span>
                </div>
              </div>

              <!-- Payment Methods -->
              <div class="flex flex-col gap-2">
                <label class="text-[11px] font-bold text-zinc-400 uppercase tracking-wider">Metode Pembayaran</label>
                <div class="flex rounded-[14px] bg-zinc-100 dark:bg-zinc-800/80 p-1">
                  <button @click="payMethod = 'cash'" :class="['flex-1 py-2.5 text-[13px] font-bold flex items-center justify-center gap-2 rounded-[10px] transition-all', payMethod === 'cash' ? 'bg-white dark:bg-zinc-700 text-zinc-900 dark:text-white shadow-sm' : 'text-zinc-500 hover:text-zinc-700']">
                    <Banknote class="h-[14px] w-[14px]" />Tunai
                  </button>
                  <button @click="payMethod = 'transfer'" :class="['flex-1 py-2.5 text-[13px] font-bold flex items-center justify-center gap-2 rounded-[10px] transition-all', payMethod === 'transfer' ? 'bg-white dark:bg-zinc-700 text-zinc-900 dark:text-white shadow-sm' : 'text-zinc-500 hover:text-zinc-700']">
                    <ArrowRightLeft class="h-[14px] w-[14px]" />Transfer
                  </button>
                </div>
              </div>

              <div class="flex flex-col gap-4 animate-in fade-in slide-in-from-bottom-2 duration-200">
                <template v-if="payMethod === 'cash'">
                  <div class="space-y-2">
                    <label class="text-[13px] font-bold text-zinc-700 dark:text-zinc-300">Uang Diterima</label>
                    <div class="relative">
                      <span class="absolute left-3.5 top-1/2 -translate-y-1/2 text-[15px] font-black text-zinc-400">Rp</span>
                      <Input v-model="cashTendered" type="number" min="0" class="pl-10 h-12 text-lg font-black rounded-[14px] border-zinc-200 shadow-sm" placeholder="0" />
                    </div>
                  </div>
                  
                  <div class="flex flex-wrap gap-2 pt-1">
                    <button v-for="amt in [total, Math.ceil(total/10000)*10000, Math.ceil(total/50000)*50000, Math.ceil(total/100000)*100000].filter((v,i,a) => a.indexOf(v) === i)" :key="amt" @click="cashTendered = amt"
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
              <Button class="w-full h-12 font-bold text-[14px] rounded-[14px] shadow-lg shadow-primary/20" @click="checkout">
                <Check class="h-[18px] w-[18px] mr-2" /> Konfirmasi Pembayaran
              </Button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>

<style scoped>
.pos-root {
  display: flex;
  flex-direction: column;
  margin: -20px; /* Counteract AppLayout p-5 padding */
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

/* Transitions */
.list-enter-active, .list-leave-active { transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
.list-enter-from, .list-leave-to { opacity: 0; transform: translateY(10px) scale(0.98); }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.scale-enter-active, .scale-leave-active { transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1); }
.scale-enter-from, .scale-leave-to { opacity: 0; transform: scale(0.95) translateY(10px); }
</style>

