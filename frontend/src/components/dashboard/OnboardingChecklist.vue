<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { usePermission } from '@/composables/usePermission'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import api from '@/lib/api'
import {
  Boxes,
  Package,
  Warehouse,
  Building2,
  Handshake,
  ClipboardList,
  ShoppingCart,
  UserPlus,
  ShieldCheck,
  MapPin,
  Ticket,
  BarChart3,
  PackageCheck,
  ChevronRight,
  CheckCircle2,
  Circle,
  X,
  Sparkles,
  ChevronDown,
  ChevronUp,
  Loader2,
  RefreshCw,
} from 'lucide-vue-next'

const router = useRouter()
const { can, canAny } = usePermission()
const auth = useAuthStore()
const { isAdmin } = storeToRefs(auth)

const props = defineProps({
  stats: { type: Object, default: () => ({}) },
})

const dismissed = ref(false)
const collapsed = ref(true)
const expandedTask = ref(null)

// ─── Data real dari API ───────────────────────────────────────────────────────
const checkLoading = ref(true)
const hasOrders    = ref(false)
const hasBranch    = ref(false)
const hasWarehouse = ref(false)
const hasSupplier  = ref(false)
const hasPO        = ref(false)
const hasStock     = ref(false)
const hasVoucher   = ref(false)

async function fetchChecks() {
  checkLoading.value = true
  try {
    const calls = [
      // 0 - Orders (POS sudah dicoba)
      isAdmin.value ? api.get('/api/v1/orders/admin') : api.get('/api/v1/orders'),
      // 1 - Branches
      api.get(isAdmin.value ? '/api/v1/branches/admin' : '/api/v1/branches'),
      // 2 - Warehouses
      api.get(isAdmin.value ? '/api/v1/warehouses/admin' : '/api/v1/warehouses'),
      // 3 - Suppliers
      api.get('/api/v1/suppliers'),
      // 4 - Purchase Orders
      api.get('/api/v1/purchase-orders'),
      // 5 - Stock Balances
      api.get(isAdmin.value ? '/api/v1/stock-balances/admin' : '/api/v1/stock-balances'),
      // 6 - Vouchers
      api.get('/api/v1/vouchers'),
    ]

    const results = await Promise.allSettled(calls)

    // 0 - Orders
    if (results[0].status === 'fulfilled') {
      const d = results[0].value.data
      const list = Array.isArray(d) ? d : (d?.data?.content || d?.data || [])
      hasOrders.value = Array.isArray(list) && list.length > 0
    }
    // 1 - Branches
    if (results[1].status === 'fulfilled') {
      const d = results[1].value.data
      const list = Array.isArray(d) ? d : (d?.data || [])
      hasBranch.value = Array.isArray(list) && list.length > 0
    }
    // 2 - Warehouses
    if (results[2].status === 'fulfilled') {
      const d = results[2].value.data
      const raw = d?.data
      const list = raw && !Array.isArray(raw) && raw.content
        ? raw.content
        : (Array.isArray(raw) ? raw : (Array.isArray(d) ? d : []))
      hasWarehouse.value = list.length > 0
    }
    // 3 - Suppliers
    if (results[3].status === 'fulfilled') {
      const d = results[3].value.data
      const list = Array.isArray(d) ? d : (d?.data || [])
      hasSupplier.value = Array.isArray(list) && list.length > 0
    }
    // 4 - Purchase Orders
    if (results[4].status === 'fulfilled') {
      const d = results[4].value.data
      const list = Array.isArray(d) ? d : (d?.data || [])
      hasPO.value = Array.isArray(list) && list.length > 0
    }
    // 5 - Stock Balances
    if (results[5].status === 'fulfilled') {
      const d = results[5].value.data
      const list = Array.isArray(d) ? d : (d?.data || [])
      // Dianggap ada stok jika minimal 1 record dengan qty > 0
      hasStock.value = Array.isArray(list) && list.some(b => (b.qty ?? b.quantity ?? 0) > 0)
    }
    // 6 - Vouchers
    if (results[6].status === 'fulfilled') {
      const d = results[6].value.data
      const list = Array.isArray(d) ? d : (d?.data || [])
      hasVoucher.value = Array.isArray(list) && list.length > 0
    }
  } catch {
    // silent — checklist tetap tampil
  } finally {
    checkLoading.value = false
  }
}

onMounted(fetchChecks)

// ─── Definisi semua task ──────────────────────────────────────────────────────
// Urutan mengikuti alur bisnis yang logis
const ALL_TASKS = computed(() => [

  // ══════════════════════════════════════════════════════════════════
  // GRUP 1 — Setup Produk
  // Fondasi: tanpa kategori & produk, tidak ada yang bisa dijual
  // ══════════════════════════════════════════════════════════════════
  {
    id: 'create_category',
    group: 'Setup Produk',
    groupIcon: Boxes,
    step: 1,
    title: 'Buat Kategori Produk',
    desc: 'Kelompokkan produk sebelum mulai menambahkan item.',
    detail: 'Kategori adalah fondasi katalog produk Anda. Dengan kategori (misal: Minuman, Makanan, Elektronik), pencarian di kasir jadi lebih cepat dan laporan penjualan bisa difilter per kelompok produk. Buat kategori dulu sebelum menambahkan produk.',
    icon: Boxes,
    to: '/dashboard/categories',
    canView: isAdmin.value || can('category.index') || can('category.store'),
    isDone: (props.stats.totalCategories ?? 0) > 0,
  },
  {
    id: 'add_product',
    group: 'Setup Produk',
    groupIcon: Boxes,
    step: 2,
    title: 'Tambah Produk Pertama',
    desc: 'Daftarkan produk yang akan dijual atau dikelola stoknya.',
    detail: 'Produk adalah item yang bisa dijual di kasir atau dikelola stoknya di gudang. Setiap produk memiliki SKU unik, harga jual, dan bisa dikaitkan ke kategori. Tanpa produk, kasir tidak bisa beroperasi dan Purchase Order tidak bisa dibuat.',
    icon: Package,
    to: '/dashboard/products',
    canView: isAdmin.value || can('produk.index') || can('produk.store'),
    isDone: (props.stats.totalProducts ?? 0) > 0,
  },

  // ══════════════════════════════════════════════════════════════════
  // GRUP 2 — Infrastruktur
  // Cabang & gudang harus ada sebelum stok bisa ditempatkan
  // ══════════════════════════════════════════════════════════════════
  {
    id: 'create_branch',
    group: 'Infrastruktur',
    groupIcon: Building2,
    step: 3,
    title: 'Buat Cabang',
    desc: 'Daftarkan lokasi toko atau outlet Anda.',
    detail: 'Cabang adalah unit bisnis fisik (toko, outlet, kantor). Setiap transaksi POS, laporan penjualan, dan stok bisa dipisah per cabang. Kasir harus terhubung ke cabang agar transaksi tercatat di lokasi yang benar.',
    icon: Building2,
    to: '/dashboard/locations',
    canView: isAdmin.value || can('branch.index') || can('branch.store'),
    isDone: hasBranch.value,
  },
  {
    id: 'create_warehouse',
    group: 'Infrastruktur',
    groupIcon: Building2,
    step: 4,
    title: 'Buat Gudang',
    desc: 'Siapkan gudang sebagai tempat penyimpanan stok.',
    detail: 'Gudang adalah tempat penyimpanan barang. Anda bisa punya beberapa gudang per cabang. Stok masuk dari Purchase Order, transfer antar gudang, dan stock opname semuanya berbasis gudang. Tanpa gudang, Purchase Order tidak bisa diarahkan ke mana pun.',
    icon: Warehouse,
    to: '/dashboard/locations',
    canView: isAdmin.value || can('warehouse.index') || can('warehouse.store'),
    isDone: hasWarehouse.value,
  },

  // ══════════════════════════════════════════════════════════════════
  // GRUP 3 — Rantai Pasok
  // Supplier → PO → Terima barang → Stok terisi
  // ══════════════════════════════════════════════════════════════════
  {
    id: 'add_supplier',
    group: 'Rantai Pasok',
    groupIcon: Handshake,
    step: 5,
    title: 'Daftarkan Supplier',
    desc: 'Tambahkan pemasok yang menyediakan produk Anda.',
    detail: 'Supplier adalah pemasok atau vendor yang mengirimkan barang ke gudang Anda. Dengan mendaftarkan supplier, Anda bisa membuat Purchase Order (PO) untuk meminta pengiriman stok secara terstruktur, terlacak, dan bisa diaudit.',
    icon: Handshake,
    to: '/dashboard/suppliers',
    canView: isAdmin.value || can('supplier.index') || can('supplier.store'),
    isDone: hasSupplier.value,
  },
  {
    id: 'create_po',
    group: 'Rantai Pasok',
    groupIcon: Handshake,
    step: 6,
    title: 'Buat Purchase Order',
    desc: 'Minta supplier mengisi stok produk ke gudang.',
    detail: 'Purchase Order (PO) adalah dokumen resmi permintaan pembelian ke supplier. Setelah PO dibuat dan dikirim ke supplier, Anda melakukan penerimaan barang (receipt) yang otomatis menambah stok di gudang. Ini cara yang benar untuk mengisi stok secara terlacak.',
    icon: ClipboardList,
    to: '/dashboard/purchase-orders',
    canView: isAdmin.value || canAny('purchase_order.index', 'purchase_order.store', 'purchase_receipt.store'),
    isDone: hasPO.value,
  },
  {
    id: 'check_stock',
    group: 'Rantai Pasok',
    groupIcon: Handshake,
    step: 7,
    title: 'Verifikasi Stok Awal',
    desc: 'Pastikan stok produk sudah masuk ke gudang setelah PO diterima.',
    detail: 'Setelah Purchase Order diterima (receipt), stok otomatis bertambah di gudang tujuan. Buka halaman Saldo Stok untuk memverifikasi bahwa jumlah stok sudah sesuai. Jika perlu koreksi manual, gunakan fitur "Tambah Stok" di halaman yang sama.',
    icon: PackageCheck,
    to: '/dashboard/stock-balances',
    canView: isAdmin.value || can('stock_balance.index'),
    isDone: hasStock.value,
  },

  // ══════════════════════════════════════════════════════════════════
  // GRUP 4 — Operasional
  // Siap jual: voucher (opsional) → coba POS → pantau laporan
  // ══════════════════════════════════════════════════════════════════
  {
    id: 'create_voucher',
    group: 'Operasional',
    groupIcon: ShoppingCart,
    step: 8,
    title: 'Buat Voucher Diskon (Opsional)',
    desc: 'Siapkan promo untuk menarik pelanggan pertama.',
    detail: 'Voucher adalah kode promo yang bisa digunakan pelanggan saat checkout di kasir. Anda bisa mengatur diskon persentase atau nominal tetap, minimum pembelian, kuota penggunaan, dan masa berlaku. Langkah ini opsional — bisa dilakukan kapan saja.',
    icon: Ticket,
    to: '/dashboard/vouchers',
    canView: isAdmin.value || can('voucher.index') || can('voucher.store'),
    isDone: hasVoucher.value,
  },
  {
    id: 'try_pos',
    group: 'Operasional',
    groupIcon: ShoppingCart,
    step: 9,
    title: 'Coba Point of Sales',
    desc: 'Lakukan transaksi penjualan pertama Anda.',
    detail: 'Point of Sales (POS) adalah antarmuka kasir untuk mencatat penjualan secara real-time. Mendukung pencarian produk cepat, scan barcode, diskon, voucher, pembayaran tunai/transfer, dan cetak struk. Setelah transaksi, stok otomatis berkurang dan laporan penjualan terupdate.',
    icon: ShoppingCart,
    to: '/dashboard/kasir',
    canView: true, // semua user bisa akses kasir
    isDone: hasOrders.value,
  },
  {
    id: 'check_report',
    group: 'Operasional',
    groupIcon: ShoppingCart,
    step: 10,
    title: 'Pantau Laporan Penjualan',
    desc: 'Lihat performa penjualan dan analitik bisnis Anda.',
    detail: 'Halaman Laporan menampilkan ringkasan penjualan, tren pendapatan, distribusi metode pembayaran, dan produk terlaris per kategori. Data bisa diekspor ke CSV atau PDF. Pantau laporan secara rutin untuk mengambil keputusan bisnis yang tepat.',
    icon: BarChart3,
    to: '/dashboard/reports',
    canView: true, // semua user bisa akses laporan
    isDone: hasOrders.value, // jika sudah ada order, laporan sudah bisa dilihat
  },

  // ══════════════════════════════════════════════════════════════════
  // GRUP 5 — Tim & Akses
  // Hanya tampil jika user punya permission manajemen
  // ══════════════════════════════════════════════════════════════════
  {
    id: 'invite_user',
    group: 'Tim & Akses',
    groupIcon: UserPlus,
    step: 11,
    title: 'Undang Anggota Tim',
    desc: 'Tambahkan pengguna lain ke sistem.',
    detail: 'Dengan menambahkan anggota tim, setiap orang bisa login dan menggunakan sistem sesuai peran mereka — kasir hanya akses POS, manajer gudang akses stok, admin akses semua modul. Ini menjaga keamanan dan akuntabilitas data operasional.',
    icon: UserPlus,
    to: '/dashboard/users',
    canView: isAdmin.value || can('user.index') || can('user.store'),
    isDone: (props.stats.totalUsers ?? 0) > 1,
  },
  {
    id: 'setup_roles',
    group: 'Tim & Akses',
    groupIcon: UserPlus,
    step: 12,
    title: 'Atur Role & Izin Akses',
    desc: 'Tentukan hak akses setiap anggota tim.',
    detail: 'Role adalah kumpulan izin akses yang diberikan ke user. Misalnya role "Kasir" hanya bisa akses POS dan melihat produk, sedangkan "Manajer Gudang" bisa kelola stok dan transfer. Pengaturan role yang tepat mencegah akses tidak sah ke data sensitif.',
    icon: ShieldCheck,
    to: '/dashboard/roles',
    // Hanya super admin / yang punya akses penuh role management
    canView: isAdmin.value || (can('role.index') && can('role.store')),
    isDone: (props.stats.totalRoles ?? 0) > 0,
  },
])

// Filter task yang relevan untuk user ini
const visibleTasks = computed(() => ALL_TASKS.value.filter(t => t.canView))

// Kelompokkan per group, pertahankan urutan step
const groups = computed(() => {
  const map = new Map()
  for (const task of visibleTasks.value) {
    if (!map.has(task.group)) {
      map.set(task.group, { name: task.group, icon: task.groupIcon, tasks: [] })
    }
    map.get(task.group).tasks.push(task)
  }
  return [...map.values()]
})

const totalDone   = computed(() => visibleTasks.value.filter(t => t.isDone).length)
const totalTasks  = computed(() => visibleTasks.value.length)
const allDone     = computed(() => totalTasks.value > 0 && totalDone.value === totalTasks.value)
const progressPct = computed(() =>
  totalTasks.value === 0 ? 0 : Math.round((totalDone.value / totalTasks.value) * 100)
)

function toggleDetail(id) {
  expandedTask.value = expandedTask.value === id ? null : id
}
function navigate(to) {
  router.push(to)
}
</script>

<template>
  <!-- Dismissed: tombol kecil untuk membuka kembali -->
  <div v-if="dismissed" class="flex justify-end">
    <button
      @click="dismissed = false"
      class="inline-flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground transition-colors border border-border/60 rounded-full px-3 py-1 bg-card"
    >
      <Sparkles class="w-3 h-3" />
      Panduan Memulai
    </button>
  </div>

  <!-- Main Card -->
  <div v-else class="rounded-xl border border-border/70 bg-card shadow-sm overflow-hidden">

    <!-- ── Header ── -->
    <div class="flex items-start justify-between px-5 py-4 border-b border-border/60 bg-gradient-to-r from-card to-muted/20">
      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2 mb-0.5">
          <Sparkles class="w-4 h-4 text-primary shrink-0" />
          <h2 class="text-base font-bold tracking-tight">
            {{ allDone ? 'Setup selesai — sistem siap beroperasi! 🎉' : 'Panduan memulai — ikuti alur ini' }}
          </h2>
        </div>
        <p class="text-xs text-muted-foreground ml-6">
          {{ allDone
            ? 'Semua langkah telah diselesaikan. Selamat menggunakan Gaptek POS!'
            : 'Selesaikan langkah-langkah berikut sesuai urutan untuk memastikan sistem berjalan optimal.' }}
        </p>

        <!-- Progress bar -->
        <div class="ml-6 mt-3 flex items-center gap-3">
          <div v-if="checkLoading" class="flex items-center gap-2">
            <Loader2 class="w-3.5 h-3.5 animate-spin text-muted-foreground" />
            <span class="text-[11px] text-muted-foreground">Memeriksa status...</span>
          </div>
          <template v-else>
            <div class="flex-1 h-1.5 rounded-full bg-muted overflow-hidden max-w-xs">
              <div
                class="h-full rounded-full transition-all duration-700"
                :class="allDone ? 'bg-emerald-500' : 'bg-primary'"
                :style="{ width: progressPct + '%' }"
              />
            </div>
            <span class="text-[11px] font-semibold text-muted-foreground shrink-0">
              {{ totalDone }}/{{ totalTasks }} selesai
            </span>
            <!-- Refresh button -->
            <button
              @click="fetchChecks"
              class="p-1 rounded hover:bg-muted transition-colors text-muted-foreground"
              title="Refresh status"
            >
              <RefreshCw class="w-3 h-3" />
            </button>
          </template>
        </div>
      </div>

      <!-- Controls -->
      <div class="flex items-center gap-1 ml-4 shrink-0">
        <button
          @click="collapsed = !collapsed"
          class="p-1.5 rounded-md hover:bg-muted transition-colors text-muted-foreground"
          :title="collapsed ? 'Perluas' : 'Ciutkan'"
        >
          <ChevronUp v-if="!collapsed" class="w-4 h-4" />
          <ChevronDown v-else class="w-4 h-4" />
        </button>
        <button
          @click="dismissed = true"
          class="p-1.5 rounded-md hover:bg-muted transition-colors text-muted-foreground"
          title="Tutup panduan"
        >
          <X class="w-4 h-4" />
        </button>
      </div>
    </div>

    <!-- ── Body ── -->
    <div v-if="!collapsed">

      <!-- Loading skeleton -->
      <div v-if="checkLoading" class="px-5 py-8 flex items-center justify-center gap-2 text-muted-foreground">
        <Loader2 class="w-4 h-4 animate-spin" />
        <span class="text-sm">Memeriksa status setup...</span>
      </div>

      <div v-else class="divide-y divide-border/40">
        <div v-for="group in groups" :key="group.name">

          <!-- Group header -->
          <div class="flex items-center gap-2 px-5 py-2 bg-muted/30">
            <component :is="group.icon" class="w-3.5 h-3.5 text-muted-foreground" />
            <span class="text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
              {{ group.name }}
            </span>
            <!-- Jumlah selesai di grup ini -->
            <span class="ml-auto text-[10px] text-muted-foreground">
              {{ group.tasks.filter(t => t.isDone).length }}/{{ group.tasks.length }}
            </span>
          </div>

          <!-- Tasks grid -->
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-0 divide-y sm:divide-y-0 sm:divide-x divide-border/30">
            <div v-for="task in group.tasks" :key="task.id" class="relative">

              <!-- Task card -->
              <div
                class="flex flex-col gap-2 px-4 py-3.5 cursor-pointer transition-colors"
                :class="task.isDone ? 'opacity-60 hover:opacity-80' : 'hover:bg-muted/40'"
                @click="toggleDetail(task.id)"
              >
                <!-- Step badge + icon + status -->
                <div class="flex items-start justify-between gap-2">
                  <div class="flex items-center gap-2">
                    <span
                      class="inline-flex items-center justify-center w-5 h-5 rounded-full text-[10px] font-bold shrink-0"
                      :class="task.isDone
                        ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400'
                        : 'bg-primary/10 text-primary'"
                    >{{ task.step }}</span>
                    <div
                      class="w-7 h-7 rounded-lg flex items-center justify-center shrink-0"
                      :class="task.isDone
                        ? 'bg-emerald-50 dark:bg-emerald-950/30 text-emerald-600'
                        : 'bg-primary/10 text-primary'"
                    >
                      <component :is="task.icon" class="w-3.5 h-3.5" />
                    </div>
                  </div>
                  <CheckCircle2 v-if="task.isDone" class="w-5 h-5 text-emerald-500 shrink-0 mt-0.5" />
                  <Circle v-else class="w-5 h-5 text-muted-foreground/30 shrink-0 mt-0.5" />
                </div>

                <!-- Title & desc -->
                <div>
                  <p
                    class="text-sm font-semibold leading-snug"
                    :class="task.isDone ? 'line-through text-muted-foreground' : ''"
                  >
                    {{ task.title }}
                  </p>
                  <p class="text-[11px] text-muted-foreground mt-0.5 leading-relaxed">
                    {{ task.desc }}
                  </p>
                </div>

                <!-- CTA -->
                <button
                  v-if="!task.isDone"
                  @click.stop="navigate(task.to)"
                  class="inline-flex items-center gap-1 text-xs font-semibold text-primary hover:text-primary/80 transition-colors mt-auto w-fit"
                >
                  Mulai sekarang
                  <ChevronRight class="w-3.5 h-3.5" />
                </button>
                <span v-else class="text-[11px] text-emerald-600 dark:text-emerald-400 font-medium mt-auto">
                  ✓ Sudah selesai
                </span>
              </div>

              <!-- Expandable detail (klik task untuk expand) -->
              <div
                v-if="expandedTask === task.id"
                class="px-4 pb-3.5 border-t border-border/40 bg-muted/20"
              >
                <div class="flex items-start gap-2 pt-3">
                  <div class="w-0.5 self-stretch rounded-full bg-primary/30 shrink-0" />
                  <div>
                    <p class="text-xs font-semibold text-foreground mb-1">Mengapa langkah ini penting?</p>
                    <p class="text-[11px] text-muted-foreground leading-relaxed">{{ task.detail }}</p>
                    <button
                      v-if="!task.isDone"
                      @click="navigate(task.to)"
                      class="mt-2.5 inline-flex items-center gap-1.5 text-xs font-semibold bg-primary text-primary-foreground px-3 py-1.5 rounded-md hover:bg-primary/90 transition-colors"
                    >
                      <component :is="task.icon" class="w-3.5 h-3.5" />
                      Buka {{ task.title }}
                    </button>
                    <span v-else class="mt-2 inline-flex items-center gap-1 text-[11px] text-emerald-600 dark:text-emerald-400 font-medium">
                      <CheckCircle2 class="w-3.5 h-3.5" /> Langkah ini sudah selesai
                    </span>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>

      <!-- All done banner -->
      <div
        v-if="!checkLoading && allDone"
        class="px-5 py-3 bg-emerald-50 dark:bg-emerald-950/20 border-t border-emerald-200 dark:border-emerald-900/40 flex items-center gap-2"
      >
        <CheckCircle2 class="w-4 h-4 text-emerald-600 shrink-0" />
        <p class="text-xs text-emerald-700 dark:text-emerald-400 font-medium">
          Semua langkah setup telah selesai. Sistem Anda siap beroperasi penuh!
        </p>
      </div>
    </div>
  </div>
</template>
