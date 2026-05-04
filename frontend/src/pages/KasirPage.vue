<script setup>
import { ref, computed } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/Input.vue'
import { Search, ShoppingCart, Plus, Minus, Trash2, CreditCard, ShoppingBag, ArrowLeft } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'

const { toast } = useToast()

const MOCK_PRODUCTS = [
  { id: '1', name: 'Kaos Polos Putih', sku: 'KPP-001', price: 85000, categoryName: 'Pakaian', isActive: true, imageUrl: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=300&q=80' },
  { id: '2', name: 'Celana Chino Beige', sku: 'CCB-002', price: 195000, categoryName: 'Pakaian', isActive: true, imageUrl: 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=300&q=80' },
  { id: '3', name: 'Sepatu Sneakers Hitam', sku: 'SSH-003', price: 450000, categoryName: 'Alas Kaki', isActive: true, imageUrl: null },
  { id: '5', name: 'Jaket Bomber Olive', sku: 'JBO-005', price: 320000, categoryName: 'Pakaian', isActive: true, imageUrl: null },
  { id: '6', name: 'Tas Selempang Canvas', sku: 'TSC-006', price: 135000, categoryName: 'Tas', isActive: true, imageUrl: null },
  { id: '7', name: 'Kemeja Flannel Kotak', sku: 'KFK-007', price: 210000, categoryName: 'Pakaian', isActive: true, imageUrl: null },
  { id: '8', name: 'Kaos Kaki Sport (3 pcs)', sku: 'KKS-008', price: 45000, categoryName: 'Aksesoris', isActive: true, imageUrl: null },
  { id: '9', name: 'Sabuk Kulit Coklat', sku: 'SKC-009', price: 98000, categoryName: 'Aksesoris', isActive: true, imageUrl: null },
]

const searchQuery = ref('')
const showCartMobile = ref(false)

const groupedProducts = computed(() => {
  let result = MOCK_PRODUCTS.filter(p => p.isActive)

  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(p => p.name.toLowerCase().includes(q) || (p.sku && p.sku.toLowerCase().includes(q)))
  }

  const groups = {}
  result.forEach(p => {
    const cat = p.categoryName || 'Lainnya'
    if (!groups[cat]) groups[cat] = []
    groups[cat].push(p)
  })

  return Object.keys(groups).map(key => ({
    category: key,
    products: groups[key]
  })).sort((a, b) => a.category.localeCompare(b.category))
})

const cart = ref([])

function addToCart(product) {
  const existing = cart.value.find(item => item.id === product.id)
  if (existing) {
    existing.qty++
  } else {
    cart.value.push({
      ...product,
      qty: 1
    })
  }
}

function getCartQty(id) {
  const item = cart.value.find(i => i.id === id)
  return item ? item.qty : 0
}

function increaseQty(item) {
  item.qty++
}

function decreaseQty(item) {
  if (item.qty > 1) {
    item.qty--
  } else {
    removeFromCart(item)
  }
}

function removeFromCart(item) {
  const index = cart.value.findIndex(i => i.id === item.id)
  if (index !== -1) {
    cart.value.splice(index, 1)
  }
}

function formatCurrency(value) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(value)
}

const subtotal = computed(() => cart.value.reduce((sum, item) => sum + (item.price * item.qty), 0))
const tax = computed(() => subtotal.value * 0.11) // 11% tax example
const total = computed(() => subtotal.value + tax.value)

function checkout() {
  if (cart.value.length === 0) return
  
  toast.success(`Pembayaran berhasil! Total: ${formatCurrency(total.value)}`)
  cart.value = []
  showCartMobile.value = false
}

// Avatar Colors logic
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
    <div class="h-[calc(100dvh-3rem)] p-2 sm:p-4 pb-2 lg:pb-4 flex flex-col lg:flex-row gap-2 sm:gap-4 bg-zinc-50/50 dark:bg-zinc-950 overflow-hidden">
      
      <!-- ─── Left Panel: Products ─── -->
      <div class="flex-1 flex-col min-w-0 min-h-0 bg-white dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-xl shadow-sm overflow-hidden lg:flex" :class="showCartMobile ? 'hidden' : 'flex'">
        
        <!-- Header & Filters -->
        <div class="p-4 border-b border-zinc-200 dark:border-zinc-800 shrink-0">
          <div class="flex flex-col sm:flex-row gap-3 justify-between items-start sm:items-center">
            <div>
              <h1 class="text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100">Kasir</h1>
              <p class="text-xs text-muted-foreground mt-0.5">Sistem Point of Sale</p>
            </div>
            <div class="relative w-full sm:w-64">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400" />
              <Input v-model="searchQuery" placeholder="Cari produk..." class="pl-9 bg-zinc-100 dark:bg-zinc-900 border-transparent focus:bg-white dark:focus:bg-zinc-950" />
            </div>
          </div>
        </div>

        <!-- Product Grid -->
        <div class="flex-1 overflow-y-auto p-4 custom-scrollbar bg-zinc-50/30 dark:bg-zinc-950/50">
          <div v-if="groupedProducts.length === 0" class="h-full flex flex-col items-center justify-center text-muted-foreground">
            <ShoppingBag class="h-12 w-12 opacity-20 mb-3" />
            <p class="text-sm">Produk tidak ditemukan.</p>
          </div>
          <div v-else class="space-y-6">
            <div v-for="group in groupedProducts" :key="group.category">
              <h2 class="text-[13px] font-bold text-zinc-800 dark:text-zinc-200 mb-3 uppercase tracking-wider flex items-center gap-3">
                <span>{{ group.category }}</span>
                <div class="h-px bg-zinc-200 dark:bg-zinc-800 flex-1 mt-0.5"></div>
              </h2>
              <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 xl:grid-cols-5 gap-3">
                <div
                  v-for="product in group.products"
                  :key="product.id"
                  @click="addToCart(product)"
                  class="group relative flex flex-col bg-white dark:bg-zinc-900 border rounded-lg overflow-hidden cursor-pointer transition-all duration-200"
                  :class="getCartQty(product.id) > 0 ? 'border-primary ring-1 ring-primary/20 shadow-sm' : 'border-zinc-200 dark:border-zinc-800 hover:border-primary/50 hover:bg-primary/5'"
                >
                  <!-- Qty Badge -->
                  <div v-if="getCartQty(product.id) > 0" class="absolute top-1.5 right-1.5 z-10 bg-primary text-primary-foreground text-[10px] font-bold min-w-[20px] h-[20px] flex items-center justify-center rounded-full shadow-md border border-primary-foreground/20 px-1">
                    {{ getCartQty(product.id) }}
                  </div>
                  
                  <div class="h-24 bg-zinc-100 dark:bg-zinc-800 relative overflow-hidden flex items-center justify-center shrink-0 border-b border-zinc-100 dark:border-zinc-800" :class="getCartQty(product.id) > 0 ? 'opacity-90' : ''">
                    <img v-if="product.imageUrl" :src="product.imageUrl" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />
                    <div v-else class="w-full h-full flex items-center justify-center text-2xl font-bold select-none opacity-80" :style="productAvatarStyle(product.name)">
                      {{ product.name.charAt(0).toUpperCase() }}
                    </div>
                  </div>
                  <div class="p-2.5 flex flex-col flex-1 justify-between gap-1.5">
                    <h3 class="font-medium text-xs text-zinc-900 dark:text-zinc-100 line-clamp-2 leading-snug group-hover:text-primary transition-colors">{{ product.name }}</h3>
                    <div class="flex items-end justify-between mt-auto pt-1">
                      <span class="text-[9px] text-zinc-400 font-mono">{{ product.sku || '' }}</span>
                      <span class="text-[11px] font-bold text-zinc-800 dark:text-zinc-200">{{ formatCurrency(product.price) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Mobile Cart Toggle Button -->
        <div v-if="cart.length > 0" class="lg:hidden p-3 border-t border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 shrink-0 shadow-[0_-4px_10px_rgba(0,0,0,0.05)]">
          <Button @click="showCartMobile = true" class="w-full h-12 flex justify-between items-center text-[13px] font-bold shadow-md">
            <div class="flex items-center gap-2">
              <ShoppingCart class="h-4 w-4" />
              <span>{{ cart.reduce((sum, item) => sum + item.qty, 0) }} Item</span>
            </div>
            <span>{{ formatCurrency(total) }}</span>
          </Button>
        </div>
      </div>

      <!-- ─── Right Panel: Cart ─── -->
      <div class="w-full lg:w-[300px] xl:w-[340px] flex-col shrink-0 bg-white dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-xl shadow-sm lg:h-auto overflow-hidden lg:flex" :class="showCartMobile ? 'flex flex-1' : 'hidden'">
        
        <!-- Cart Header -->
        <div class="p-3 border-b border-zinc-200 dark:border-zinc-800 shrink-0 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <button @click="showCartMobile = false" class="lg:hidden p-1 -ml-1 mr-1 text-zinc-500 hover:bg-zinc-100 dark:hover:bg-zinc-800 rounded-md transition-colors">
              <ArrowLeft class="h-5 w-5" />
            </button>
            <ShoppingCart class="h-5 w-5 text-primary hidden lg:block" />
            <h2 class="font-bold text-lg">Pesanan</h2>
            <span class="bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 text-[10px] font-bold px-2 py-0.5 rounded-full ml-1">
              {{ cart.reduce((sum, item) => sum + item.qty, 0) }} Item
            </span>
          </div>
          <button v-if="cart.length > 0" @click="cart = []" class="text-[11px] font-medium text-red-500 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-500/10 px-2 py-1 rounded transition-colors">
            Kosongkan
          </button>
        </div>

        <!-- Cart Items -->
        <div class="flex-1 overflow-y-auto p-2 sm:p-3 custom-scrollbar bg-zinc-50/50 dark:bg-zinc-950/50">
          <div v-if="cart.length === 0" class="h-full flex flex-col items-center justify-center text-muted-foreground space-y-3">
            <div class="w-16 h-16 rounded-full bg-zinc-100 dark:bg-zinc-900 flex items-center justify-center">
              <ShoppingCart class="h-8 w-8 opacity-20" />
            </div>
            <p class="text-sm">Keranjang masih kosong</p>
          </div>
          
          <div v-else class="space-y-3">
            <TransitionGroup name="list">
              <div v-for="item in cart" :key="item.id" class="flex flex-col bg-white dark:bg-zinc-900 border border-zinc-100 dark:border-zinc-800/80 p-2 rounded-lg shadow-sm">
                <div class="flex items-start justify-between gap-1.5">
                  <div class="flex-1 min-w-0">
                    <h4 class="font-semibold text-[11px] text-zinc-900 dark:text-zinc-100 line-clamp-2 leading-tight">{{ item.name }}</h4>
                    <p class="text-[9px] text-primary font-semibold mt-0.5">{{ formatCurrency(item.price) }}</p>
                  </div>
                  <button @click="removeFromCart(item)" class="p-1 text-zinc-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 rounded-md transition-colors shrink-0">
                    <Trash2 class="h-3 w-3" />
                  </button>
                </div>
                
                <div class="flex items-center justify-between mt-1.5">
                  <div class="flex items-center bg-zinc-100 dark:bg-zinc-800/80 rounded p-0.5 border border-zinc-200 dark:border-zinc-700/50">
                    <button @click="decreaseQty(item)" class="w-5 h-5 flex items-center justify-center rounded-[3px] hover:bg-white dark:hover:bg-zinc-700 text-zinc-600 dark:text-zinc-300 shadow-sm transition-colors">
                      <Minus class="h-[10px] w-[10px]" />
                    </button>
                    <span class="w-5 text-center text-[10px] font-bold text-zinc-900 dark:text-zinc-100">{{ item.qty }}</span>
                    <button @click="increaseQty(item)" class="w-5 h-5 flex items-center justify-center rounded-[3px] hover:bg-white dark:hover:bg-zinc-700 text-zinc-600 dark:text-zinc-300 shadow-sm transition-colors">
                      <Plus class="h-[10px] w-[10px]" />
                    </button>
                  </div>
                  <span class="font-bold text-[11px] text-zinc-900 dark:text-zinc-100">{{ formatCurrency(item.price * item.qty) }}</span>
                </div>
              </div>
            </TransitionGroup>
          </div>
        </div>

        <!-- Checkout Summary -->
        <div class="p-2 sm:p-3 border-t border-zinc-200 dark:border-zinc-800 shrink-0 bg-zinc-50/50 dark:bg-zinc-900/30">
          <div class="space-y-1 mb-2">
            <div class="flex justify-between text-xs">
              <span class="text-zinc-500 font-medium">Subtotal</span>
              <span class="font-semibold text-zinc-800 dark:text-zinc-200">{{ formatCurrency(subtotal) }}</span>
            </div>
            <div class="flex justify-between text-xs">
              <span class="text-zinc-500 font-medium">Pajak (11%)</span>
              <span class="font-semibold text-zinc-800 dark:text-zinc-200">{{ formatCurrency(tax) }}</span>
            </div>
            <div class="h-px w-full bg-zinc-200 dark:bg-zinc-800 my-1.5"></div>
            <div class="flex justify-between items-center">
              <span class="text-[13px] font-bold text-zinc-900 dark:text-zinc-100">Total</span>
              <span class="text-lg font-black text-primary">{{ formatCurrency(total) }}</span>
            </div>
          </div>
          
          <Button 
            @click="checkout" 
            class="w-full h-9 text-[13px] font-bold shadow-md hover:shadow-lg transition-all" 
            :disabled="cart.length === 0"
          >
            <CreditCard class="h-4 w-4 mr-1.5" />
            Bayar Pesanan
          </Button>
        </div>
        
      </div>
      
    </div>
  </AppLayout>
</template>

<style scoped>
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
