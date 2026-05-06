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
  return Object.keys(groups).map(key => ({ category: key, products: groups[key] })).sort((a, b) => a.category.localeCompare(b.category))
})

const cart = ref([])

function addToCart(product) {
  const existing = cart.value.find(item => item.id === product.id)
  if (existing) { existing.qty++ } else { cart.value.push({ ...product, qty: 1 }) }
}

function getCartQty(id) {
  const item = cart.value.find(i => i.id === id)
  return item ? item.qty : 0
}

function increaseQty(item) { item.qty++ }
function decreaseQty(item) { if (item.qty > 1) { item.qty-- } else { removeFromCart(item) } }
function removeFromCart(item) {
  const idx = cart.value.findIndex(i => i.id === item.id)
  if (idx !== -1) cart.value.splice(idx, 1)
}

function formatCurrency(value) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(value)
}

const totalItems = computed(() => cart.value.reduce((s, i) => s + i.qty, 0))
const total      = computed(() => cart.value.reduce((s, i) => s + i.price * i.qty, 0))

function checkout() {
  if (!cart.value.length) return
  toast.success(`Pembayaran berhasil! Total: ${formatCurrency(total.value)}`)
  cart.value = []
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
      <div class="pos-panel">

        <!-- Header -->
        <div class="pos-header">
          <div class="pos-header-row">
            <div>
              <h1 class="pos-title">Kasir</h1>
              <p class="pos-sub">Sistem Point of Sale</p>
            </div>
          </div>
          <div class="pos-search">
            <Search class="pos-search-ic" />
            <Input v-model="searchQuery" placeholder="Cari produk..."
              class="pl-9 bg-zinc-100 dark:bg-zinc-900 border-transparent focus:bg-white dark:focus:bg-zinc-950" />
          </div>
        </div>

        <!-- Grid -->
        <div class="pos-grid-wrap custom-scrollbar">
          <div v-if="!groupedProducts.length" class="pos-empty">
            <ShoppingBag class="h-12 w-12 opacity-20 mb-3" />
            <p class="text-sm">Produk tidak ditemukan.</p>
          </div>
          <div v-else class="pos-groups">
            <div v-for="g in groupedProducts" :key="g.category" class="pos-group">
              <h2 class="pos-cat">
                <span>{{ g.category }}</span>
                <div class="pos-cat-line" />
              </h2>
              <div class="pos-grid">
                <div
                  v-for="p in g.products" :key="p.id"
                  class="pos-card" :class="{ 'pos-card--on': getCartQty(p.id) > 0 }"
                  @click="addToCart(p)"
                >
                  <div v-if="getCartQty(p.id) > 0" class="pos-badge">{{ getCartQty(p.id) }}</div>
                  <div class="pos-thumb">
                    <img v-if="p.imageUrl" :src="p.imageUrl" class="pos-thumb-img" />
                    <div v-else class="pos-thumb-av" :style="avatarStyle(p.name)">{{ p.name.charAt(0).toUpperCase() }}</div>
                  </div>
                  <div class="pos-info">
                    <p class="pos-name">{{ p.name }}</p>
                    <span class="pos-price">{{ formatCurrency(p.price) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ── Cart panel ── -->
      <div class="pos-cart">

        <!-- Cart header -->
        <div class="pos-cart-hd">
          <div class="pos-cart-hd-left">
            <h2 class="pos-cart-title">Pesanan</h2>
            <span class="pos-cart-count">{{ totalItems }} Item</span>
          </div>
          <button v-if="cart.length" class="pos-clear" @click="cart = []">Kosongkan</button>
        </div>

        <!-- Cart items -->
        <div class="pos-cart-body custom-scrollbar">
          <div v-if="!cart.length" class="pos-cart-empty">
            <div class="pos-cart-empty-ic">
              <ShoppingCart class="h-8 w-8 opacity-20" />
            </div>
            <p class="text-sm">Keranjang masih kosong</p>
          </div>
          <div v-else class="pos-items">
            <TransitionGroup name="list">
              <div v-for="item in cart" :key="item.id" class="pos-item">
                <div class="pos-item-top">
                  <div class="pos-item-info">
                    <h4 class="pos-item-name">{{ item.name }}</h4>
                    <p class="pos-item-unit">{{ formatCurrency(item.price) }}</p>
                  </div>
                  <button class="pos-item-del" @click="removeFromCart(item)">
                    <Trash2 class="h-3.5 w-3.5" />
                    <span>Hapus</span>
                  </button>
                </div>
                <div class="pos-item-bot">
                  <div class="pos-qty">
                    <button class="pos-qty-btn" @click="decreaseQty(item)"><Minus class="h-[10px] w-[10px]" /></button>
                    <span class="pos-qty-val">{{ item.qty }}</span>
                    <button class="pos-qty-btn" @click="increaseQty(item)"><Plus class="h-[10px] w-[10px]" /></button>
                  </div>
                  <span class="pos-item-sub">{{ formatCurrency(item.price * item.qty) }}</span>
                </div>
              </div>
            </TransitionGroup>
          </div>
        </div>

        <!-- Summary & checkout -->
        <div v-if="cart.length > 0" class="pos-cart-ft">
          <div class="pos-sum-total">
            <span class="pos-sum-lbl">Total</span>
            <span class="pos-sum-total-val">{{ formatCurrency(total) }}</span>
          </div>
          <Button class="pos-pay-btn" :disabled="!cart.length" @click="checkout">
            <CreditCard class="h-4 w-4 mr-1.5" />
            Bayar
          </Button>
        </div>
      </div>

    </div>
  </AppLayout>
</template>

<style scoped>
/* Root */
.pos-root {
  display: flex;
  flex-direction: column;
  margin: -20px; /* Counteract AppLayout p-5 padding */
  height: calc(100dvh - 48px);
  overflow: hidden;
  background: #f9f9f9;
}
@media (min-width: 1024px) {
  .pos-root { 
    flex-direction: row; 
  }
}
:root.dark .pos-root { background: #09090b; }

/* ── Product panel ── */
.pos-panel {
  display: flex;
  flex-direction: column;
  background: white;
  flex: 1;
  min-height: 0;
}
@media (min-width: 1024px) {
  .pos-panel {
    min-width: 0;
  }
}
:root.dark .pos-panel { background: #09090b; }

.pos-header {
  padding: 12px 16px 10px;
  border-bottom: 1px solid #e4e4e7;
  flex-shrink: 0;
}
:root.dark .pos-header { border-color: #27272a; }
.pos-header-row { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px; }
.pos-title { font-size: 1.15rem; font-weight: 800; letter-spacing: -0.025em; color: #18181b; }
:root.dark .pos-title { color: #f4f4f5; }
.pos-sub { font-size: 11px; color: #a1a1aa; margin-top: 1px; }
.pos-search { position: relative; }
.pos-search-ic { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); height: 15px; width: 15px; color: #a1a1aa; z-index: 1; }

.pos-grid-wrap { padding: 12px 16px; flex: 1; overflow-y: auto; }
.pos-empty { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #a1a1aa; padding: 40px 0; }
.pos-groups { display: flex; flex-direction: column; gap: 20px; }
.pos-cat {
  font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.08em;
  color: #71717a; display: flex; align-items: center; gap: 10px; margin-bottom: 10px;
}
:root.dark .pos-cat { color: #a1a1aa; }
.pos-cat-line { flex: 1; height: 1px; background: #e4e4e7; }
:root.dark .pos-cat-line { background: #27272a; }

.pos-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
@media (min-width: 480px)  { .pos-grid { grid-template-columns: repeat(3, 1fr); } }
@media (min-width: 768px)  { .pos-grid { grid-template-columns: repeat(4, 1fr); } }
@media (min-width: 1024px) { .pos-grid { grid-template-columns: repeat(5, 1fr); } }
@media (min-width: 1280px) { .pos-grid { grid-template-columns: repeat(6, 1fr); } }

.pos-card {
  position: relative; display: flex; flex-direction: column;
  background: white; border: 1.5px solid #e4e4e7;
  border-radius: 10px; overflow: hidden; cursor: pointer;
  transition: all 0.18s ease;
}
:root.dark .pos-card { background: #18181b; border-color: #27272a; }
.pos-card:active { transform: scale(0.96); }
.pos-card--on { border-color: hsl(var(--primary)); box-shadow: 0 0 0 2px hsl(var(--primary)/0.15); }

.pos-badge {
  position: absolute; top: 6px; right: 6px; z-index: 2;
  background: hsl(var(--primary)); color: hsl(var(--primary-foreground));
  font-size: 10px; font-weight: 700; min-width: 20px; height: 20px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 999px; padding: 0 5px;
}

.pos-thumb {
  height: 72px; background: #f4f4f5;
  display: flex; align-items: center; justify-content: center;
  overflow: hidden; flex-shrink: 0;
}
:root.dark .pos-thumb { background: #27272a; }
.pos-thumb-img { width: 100%; height: 100%; object-fit: cover; }
.pos-thumb-av {
  width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
  font-size: 1.4rem; font-weight: 700; opacity: 0.85;
}

.pos-info { padding: 6px 8px 8px; display: flex; flex-direction: column; gap: 3px; flex: 1; }
.pos-name {
  font-size: 11px; font-weight: 600; line-height: 1.3; color: #18181b;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
:root.dark .pos-name { color: #f4f4f5; }
.pos-price { font-size: 10px; font-weight: 700; color: hsl(var(--primary)); margin-top: auto; }

/* ── Cart panel ── */
.pos-cart {
  display: flex; flex-direction: column;
  background: white;
  border-top: 1px solid #e4e4e7;
  max-height: 38vh;
  flex-shrink: 0;
}
@media (min-width: 1024px) {
  .pos-cart {
    width: 340px;
    height: 100%;
    max-height: none;
    border-top: none;
    border-left: 1px solid #e4e4e7;
  }
}
:root.dark .pos-cart { background: #09090b; border-color: #27272a; }

.pos-cart-hd {
  padding: 12px 16px;
  border-bottom: 1px solid #e4e4e7;
  display: flex; align-items: center; justify-content: space-between;
  flex-shrink: 0;
}
:root.dark .pos-cart-hd { border-color: #27272a; }
.pos-cart-hd-left { display: flex; align-items: center; gap: 8px; }
.pos-cart-title { font-size: 1.1rem; font-weight: 800; color: #18181b; }
:root.dark .pos-cart-title { color: #f4f4f5; }
.pos-cart-count {
  background: #f4f4f5; color: #71717a;
  font-size: 10px; font-weight: 700;
  padding: 2px 8px; border-radius: 999px;
}
:root.dark .pos-cart-count { background: #27272a; color: #a1a1aa; }
.pos-clear {
  font-size: 11px; font-weight: 600; color: #ef4444;
  background: none; border: none; cursor: pointer;
  padding: 4px 8px; border-radius: 6px;
}
.pos-clear:hover { background: #fef2f2; }
:root.dark .pos-clear:hover { background: rgba(239,68,68,0.1); }

.pos-cart-body { padding: 12px 16px; background: #fafafa; flex: 1; overflow-y: auto; }
:root.dark .pos-cart-body { background: #09090b; }
.pos-cart-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; color: #a1a1aa; padding: 20px 0; }
.pos-cart-empty-ic { width: 48px; height: 48px; border-radius: 50%; background: #f4f4f5; display: flex; align-items: center; justify-content: center; }
:root.dark .pos-cart-empty-ic { background: #18181b; }

.pos-items { display: flex; flex-direction: column; gap: 10px; }
.pos-item {
  background: white; border: 1px solid #e4e4e7;
  border-radius: 10px; padding: 10px 12px;
  display: flex; flex-direction: column; gap: 8px;
}
:root.dark .pos-item { background: #18181b; border-color: #27272a; }
.pos-item-top { display: flex; align-items: flex-start; justify-content: space-between; gap: 8px; }
.pos-item-info { flex: 1; min-width: 0; }
.pos-item-name { font-size: 12px; font-weight: 600; color: #18181b; line-height: 1.3; }
:root.dark .pos-item-name { color: #f4f4f5; }
.pos-item-unit { font-size: 10px; color: hsl(var(--primary)); font-weight: 600; margin-top: 2px; }
.pos-item-del {
  display: flex; align-items: center; gap: 4px;
  padding: 4px 8px; border-radius: 6px; background: #fef2f2; border: none; cursor: pointer;
  color: #ef4444; flex-shrink: 0; font-size: 11px; font-weight: 600;
}
:root.dark .pos-item-del { background: rgba(239,68,68,0.1); color: #f87171; }
.pos-item-del:hover { background: #fee2e2; }
:root.dark .pos-item-del:hover { background: rgba(239,68,68,0.2); }

.pos-item-bot { display: flex; align-items: center; justify-content: space-between; }
.pos-qty {
  display: flex; align-items: center;
  background: #f4f4f5; border: 1px solid #e4e4e7;
  border-radius: 6px; overflow: hidden;
}
:root.dark .pos-qty { background: #27272a; border-color: #3f3f46; }
.pos-qty-btn {
  width: 26px; height: 26px; display: flex; align-items: center; justify-content: center;
  background: none; border: none; cursor: pointer; color: #52525b;
}
:root.dark .pos-qty-btn { color: #d4d4d8; }
.pos-qty-btn:hover { background: #e4e4e7; }
:root.dark .pos-qty-btn:hover { background: #3f3f46; }
.pos-qty-val { width: 28px; text-align: center; font-size: 11px; font-weight: 700; color: #18181b; }
:root.dark .pos-qty-val { color: #f4f4f5; }
.pos-item-sub { font-size: 12px; font-weight: 700; color: #18181b; }
:root.dark .pos-item-sub { color: #f4f4f5; }

/* Summary */
.pos-cart-ft {
  padding: 10px 16px; border-top: 1px solid #e4e4e7; flex-shrink: 0;
  background: white;
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
}
:root.dark .pos-cart-ft { border-color: #27272a; background: #09090b; }
.pos-sum-total { display: flex; flex-direction: column; line-height: 1.2; }
.pos-sum-lbl { font-size: 11px; font-weight: 700; color: #71717a; text-transform: uppercase; }
:root.dark .pos-sum-lbl { color: #a1a1aa; }
.pos-sum-total-val { font-size: 1.15rem; font-weight: 900; color: hsl(var(--primary)); }
.pos-pay-btn { height: 40px; padding: 0 20px; font-size: 14px; font-weight: 700; border-radius: 10px; flex-shrink: 0; }

@media (min-width: 1024px) {
  .pos-cart-ft {
    flex-direction: column; align-items: stretch; gap: 12px;
    padding: 16px;
  }
  .pos-sum-total { flex-direction: row; justify-content: space-between; align-items: center; }
  .pos-sum-lbl { font-size: 13px; text-transform: none; }
  .pos-sum-total-val { font-size: 1.25rem; }
  .pos-pay-btn { height: 44px; width: 100%; }
}

/* Transitions */
.list-enter-active, .list-leave-active { transition: all 0.25s ease; }
.list-enter-from, .list-leave-to { opacity: 0; transform: translateX(20px); }

/* Scrollbar */
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #e4e4e7; border-radius: 10px; }
:root.dark .custom-scrollbar::-webkit-scrollbar-thumb { background: #3f3f46; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #d4d4d8; }
:root.dark .custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #52525b; }
</style>

