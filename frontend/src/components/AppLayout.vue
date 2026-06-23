<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { usePermission } from '@/composables/usePermission'
import { useConfirmStore } from '@/stores/confirm'
// import { useNotificationStore } from '@/stores/notifications'
import { useRoute, useRouter } from 'vue-router'
import {
  LogOut,
  LayoutDashboard,
  ShieldCheck,
  KeyRound,
  ChevronDown,
  ChevronRight,
  FileText,
  ScrollText,
  Zap,
  Users,
  Activity,
  PanelLeftClose,
  PanelLeftOpen,
  Settings,
  Monitor,
  Moon,
  Sun,
  HelpCircle,
  Home,
  Pencil,
  Palette,
  Check,
  ArrowLeftRight,
  ShoppingCart,
  Search,
  Ticket,
  BarChart3,
  Package,
  Warehouse,
  ChevronLeft,
  TrendingUp,
  Building2,
  Handshake,
  Bell,
  Truck,
  ClipboardList,
  PackageSearch,
  Repeat2,
  Clock,
} from 'lucide-vue-next'
import Toast from '@/components/ui/Toast.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import AboutModal from '@/components/dashboard/AboutModal.vue'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuPortal,
  DropdownMenuSeparator,
  DropdownMenuShortcut,
  DropdownMenuSub,
  DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

const auth = useAuthStore()
const themeStore = useThemeStore()
const { user } = storeToRefs(auth)
const { can } = usePermission()
const route = useRoute()
const router = useRouter()
const confirmStore = useConfirmStore()

// Sidebar state (mobile only)
const sidebarOpen = ref(false)

// Mobile: state untuk expand warna tema di panel inline
const mobileThemeExpanded = ref(false)

// Sub-sidebar state: 'main' | 'inventory'
const activeSidebar = ref('main')

// Inventory Sub-Sidebar Menu — dengan permission filter
const INVENTORY_MENU_RAW = [
  { label: 'Dashboard Inventaris', icon: LayoutDashboard, to: '/dashboard/inventory',         permission: 'stock_balance.index' },
  { label: 'Pergerakan Stok',  icon: ArrowLeftRight,  to: '/dashboard/stock-mutations',   permission: 'stock_mutation.index' },
  { label: 'Pengecekan Stok',     icon: PackageSearch,   to: '/dashboard/stock-opname',      permission: 'stock_opname.index' },
  { label: 'Transfer Stok',       icon: Repeat2,          to: '/dashboard/transfer-requests', permission: 'transfer_request.index' },
]

// Filter inventory menu berdasarkan permission user saat ini
const INVENTORY_MENU = computed(() => {
  return INVENTORY_MENU_RAW.filter(item => {
    if (!item.permission) return true
    return auth.isAdmin || can(item.permission)
  })
})

// Routes yang termasuk inventory → auto-switch sidebar ke 'inventory'
const INVENTORY_ROUTES = [
  '/dashboard/inventory',
  '/dashboard/stock-balances',
  '/dashboard/stock-mutations',
  '/dashboard/transfer-requests',
  '/dashboard/stock-opname',
]

// About Modal state
const isAboutModalOpen = ref(false)

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

// ─── Menu Groups (dengan section header) ────────────────────────────────────
const MENU_GROUPS = [
  {
    label: 'Utama',
    items: [
      {
        label: 'Dashboard', icon: LayoutDashboard, to: '/dashboard', permission: null
      },
      { label: 'Mitra', icon: Handshake, to: '/dashboard/partners', permission: 'partner.index' },
    ]
  },
  {
    label: 'Transaksi',
    items: [
      { label: 'Kasir', icon: ShoppingCart, to: '/dashboard/kasir', permission: 'pos.index' },
      { label: 'Riwayat Pesanan', icon: ScrollText, to: '/dashboard/orders', permission: 'order.index' },
      { label: 'Riwayat Shift', icon: Clock, to: '/dashboard/shifts', permission: 'shift.index' },
    ],
  },
  {
    label: 'Inventaris',
    items: [
      { label: 'Produk', icon: ScrollText, to: '/dashboard/products', permission: 'produk.index' },
      { label: 'Data Inventaris', icon: Warehouse, to: '/dashboard/inventory', subSidebar: 'inventory', permission: null },
      { label: 'Kategori', icon: FileText, to: '/dashboard/categories', permission: 'category.index' },
    ],
  },
  {
    label: 'Pengadaan',
    items: [
      { label: 'Distributor', icon: Truck, to: '/dashboard/suppliers', permission: 'supplier.index' },
      { label: 'Pembelian', icon: ClipboardList, to: '/dashboard/purchase-orders', anyPermission: ['purchase_order.index', 'purchase_receipt.store'] },
    ],
  },
  {
    label: 'Manajemen',
    items: [
      { label: 'Manajemen Pengguna', icon: Users, to: '/dashboard/users', permission: 'user.index' },
      { label: 'Cabang',  icon: Building2, to: '/dashboard/branches',  permission: 'branch.index' },
      { label: 'Gudang',  icon: Warehouse, to: '/dashboard/warehouses', permission: 'warehouse.index' },
      { label: 'Voucer', icon: Ticket, to: '/dashboard/vouchers', permission: 'voucher.index' },
    ],
  },
  {
    label: 'Kontrol Akses',
    items: [
      { label: 'Hak akses', icon: ShieldCheck, to: '/dashboard/roles', permission: 'role.index' },
      { label: 'Perizinan', icon: KeyRound, to: '/dashboard/permissions', permission: 'permission.index' },
      { label: 'Modul', icon: Zap, to: '/dashboard/modules', permission: 'module.index' },
    ],
  },
  {
    label: 'Data Master',
    items: [
      // { label: 'Notifikasi', icon: Bell, to: '/dashboard/notifications', permission: null },
      { label: 'Log Audit', icon: Activity, to: '/dashboard/logs', permission: 'log.index' },
    ],
  },
  {
    label: 'Wawasan',
    items: [
      { label: 'Laporan', icon: BarChart3, to: '/dashboard/reports', permission: null },
    ],
  },
]

// Filter menu by permission
function filterMenu(groups) {
  const isAdmin = auth.isAdmin
  
  // Memastikan status apakah user memiliki partner atau partnerId
  const hasPartner = !!(user.value?.partner || user.value?.partnerId)

  return groups.reduce((acc, group) => {

    const filteredItems = group.items.reduce((items, item) => {
      
      // VALIDASI LEVEL ITEM (Menu Mitra): Sembunyikan jika sudah punya partner
      if (item.label === 'Mitra' && hasPartner) {
        return items
      }

      // Support anyPermission: tampilkan jika punya salah satu
      if (item.anyPermission) {
        if (!isAdmin && !item.anyPermission.some(p => can(p))) return items
      } else if (item.permission && !isAdmin && !can(item.permission)) {
        return items
      }
      
      const filtered = { ...item }
      if (filtered.children) {
        filtered.children = filterMenu([{ label: '', items: filtered.children }])[0]?.items || []
        if (filtered.children.length === 0) return items
      }
      items.push(filtered)
      return items
    }, [])

    if (filteredItems.length > 0) {
      acc.push({ label: group.label, items: filteredItems })
    }
    return acc
  }, [])
}

const filteredMenuGroups = computed(() => filterMenu(MENU_GROUPS))

// Expanded state for sub-menus
const expandedMenus = ref(new Set())

function toggleExpand(label) {
  const s = new Set(expandedMenus.value)
  if (s.has(label)) s.delete(label)
  else s.add(label)
  expandedMenus.value = s
}

function isItemActive(item) {
  if (!item.to) return item.children?.some(isItemActive) ?? false
  if (item.to === '/dashboard') return route.path === '/dashboard'
  return route.path === item.to || route.path.startsWith(item.to + '/')
}

function isItemExpanded(item) {
  if (!item.children) return false
  return expandedMenus.value.has(item.label)
}

function isGroupActive(group) {
  return group.items.some(isItemActive)
}

// Auto-expand parent menu when child is active
function expandActiveParents() {
  const s = new Set(expandedMenus.value)
  filteredMenuGroups.value.forEach(group => {
    group.items.forEach(item => {
      if (item.children && item.children.some(child => isItemActive(child))) {
        s.add(item.label)
      }
    })
  })
  expandedMenus.value = s
}

// Watch route changes: auto-switch sidebar + expand active parents
watch(() => route.path, (path) => {
  // Jika route termasuk inventory routes → buka inventory sidebar
  const isInventoryRoute = INVENTORY_ROUTES.some(
    r => path === r || path.startsWith(r + '/')
  )
  activeSidebar.value = isInventoryRoute ? 'inventory' : 'main'

  expandActiveParents()
  sidebarOpen.value = false
}, { immediate: true })

// ─── User Info ────────────────────────────────────────────────────────
const displayName = computed(() => user.value?.fullname || user.value?.username || 'User')
const displayEmail = computed(() => user.value?.username || '')
const userInitial = computed(() => {
  const name = displayName.value
  return name.charAt(0).toUpperCase()
})
const userAvatar = computed(() => user.value?.avatar || null)

// Generate warna avatar dari nama (deterministik)
const AVATAR_PALETTE = [
  { bg: '#dbeafe', color: '#1d4ed8' }, // blue
  { bg: '#dcfce7', color: '#15803d' }, // green
  { bg: '#fef9c3', color: '#a16207' }, // yellow
  { bg: '#fce7f3', color: '#be185d' }, // pink
  { bg: '#ede9fe', color: '#6d28d9' }, // violet
  { bg: '#ffedd5', color: '#c2410c' }, // orange
  { bg: '#cffafe', color: '#0e7490' }, // cyan
  { bg: '#f1f5f9', color: '#475569' }, // slate
]
const avatarStyle = computed(() => {
  const name = displayName.value
  const hash = name.split('').reduce((a, c) => a + c.charCodeAt(0), 0)
  const p = AVATAR_PALETTE[hash % AVATAR_PALETTE.length]
  return { backgroundColor: p.bg, color: p.color }
})

// ─── Plan / Upgrade ───────────────────────────────────────────────────
const userPlan = computed(() => (user.value?.plan || 'basic').toLowerCase())

// Label & warna badge plan
const planConfig = computed(() => {
  switch (userPlan.value) {
    case 'enterprise': return { label: 'Enterprise', dotClass: 'bg-amber-400 shadow-[0_0_8px_rgba(251,191,36,0.6)]' }
    case 'pro':        return { label: 'Pro',      dotClass: 'bg-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.5)]' }
    default:           return { label: 'Basic',      dotClass: 'bg-zinc-400' }
  }
})

// Upgrade button — null berarti tidak perlu ditampilkan (enterprise)
const upgradeConfig = computed(() => {
  switch (userPlan.value) {
    case 'basic':      return { label: 'Upgrade ke Pro',        to: '/pricing?plan=pro' }
    case 'pro':        return { label: 'Upgrade ke Enterprise', to: '/pricing?plan=enterprise' }
    default:           return null  // enterprise — tidak perlu upgrade
  }
})

// Theme Preference State (system | light | dark)
const themePreference = ref(typeof window !== 'undefined' && localStorage.getItem('isDark') === null ? 'system' : (themeStore.isDark ? 'dark' : 'light'))

function setThemePreference(pref) {
  themePreference.value = pref
  if (pref === 'system') {
    localStorage.removeItem('isDark')
    themeStore.init()
  } else {
    themeStore.applyTheme(themeStore.currentTheme, pref === 'dark')
  }
}

// ─── Search Functionality ──────────────────────────────────────────────────
const isSearchOpen = ref(false)
const searchQuery = ref('')
const searchInputRef = ref(null)
const activeSearchIndex = ref(0)

const searchableMenuItems = computed(() => {
  const items = []
  filteredMenuGroups.value.forEach(group => {
    group.items.forEach(item => {
      if (item.children) {
        item.children.forEach(child => {
          items.push({
            id: child.to || child.label,
            label: child.label,
            subtitle: `${group.label} / ${item.label}`,
            to: child.to,
            icon: child.icon
          })
        })
      } else {
        items.push({
          id: item.to || item.label,
          label: item.label,
          subtitle: group.label,
          to: item.to || (item.subSidebar ? '/dashboard/inventory' : null),
          icon: item.icon
        })
      }
    })
  })

  // Tambahkan semua item dari inventory sub-sidebar agar bisa ditemukan di search
  INVENTORY_MENU.value.forEach(item => {
    const alreadyAdded = items.some(i => i.to === item.to)
    if (!alreadyAdded) {
      items.push({
        id: item.to,
        label: item.label,
        subtitle: 'Inventaris / Inventory',
        to: item.to,
        icon: item.icon
      })
    }
  })

  return items
})

const filteredSearchItems = computed(() => {
  if (!searchQuery.value) return searchableMenuItems.value
  const query = searchQuery.value.toLowerCase()
  return searchableMenuItems.value.filter(item => 
    item.label.toLowerCase().includes(query) || 
    item.subtitle.toLowerCase().includes(query)
  )
})

const currentPageTitle = computed(() => {
  if (route.path === '/dashboard') return 'Dashboard'
  
  const matchedItem = searchableMenuItems.value.find(item => item.to === route.path)
  if (matchedItem) return matchedItem.label
  
  const segments = route.path.split('/').filter(Boolean)
  if (segments.length > 0) {
    const last = segments[segments.length - 1]
    return last.charAt(0).toUpperCase() + last.slice(1).replace(/-/g, ' ')
  }
  
  return 'Dasbor'
})

watch(searchQuery, () => {
  activeSearchIndex.value = 0
})

watch(sidebarOpen, (newVal) => {
  if (!newVal && isSearchOpen.value) {
    closeSearch()
  }
})

function openSearch() {
  sidebarOpen.value = true
  isSearchOpen.value = true
  searchQuery.value = ''
  activeSearchIndex.value = 0
  // Di mobile, sidebar punya animasi 300ms — tunggu sedikit lebih lama agar input sudah ter-render
  const isMobile = window.innerWidth < 1024
  setTimeout(() => {
    if (searchInputRef.value) searchInputRef.value.focus()
  }, isMobile ? 350 : 150)
}

function closeSearch() {
  isSearchOpen.value = false
}

function navigateSearch(direction) {
  const max = filteredSearchItems.value.length - 1
  if (max < 0) return
  
  activeSearchIndex.value += direction
  if (activeSearchIndex.value > max) activeSearchIndex.value = 0
  if (activeSearchIndex.value < 0) activeSearchIndex.value = max
}

function selectActiveSearch() {
  const item = filteredSearchItems.value[activeSearchIndex.value]
  if (item && item.to) {
    router.push(item.to)
    closeSearch()
    sidebarOpen.value = false
  }
}

function handleGlobalKeydown(e) {
  if (e.key.toLowerCase() === 'f') {
    const active = document.activeElement
    const isInput = active.tagName === 'INPUT' || active.tagName === 'TEXTAREA' || active.isContentEditable
    if (!isInput) {
      e.preventDefault()
      openSearch()
    }
  } else if (e.key === 'Escape' && isSearchOpen.value) {
    closeSearch()
  }
}

import api from '@/lib/api'

// ─── Role Helpers ─────────────────────────────────────────────────────────
function hasRole(roleSlug) {
  const roles = user.value?.roles || []
  return roles.some(r => {
    const name = (typeof r === 'string' ? r : (r.name || r.slug || '')).toLowerCase()
    return name === roleSlug.toLowerCase()
  })
}

// isOwnerPartner: pemilik mitra — role "owner" atau "admin-partner" dengan partnerId
const isOwnerPartner = computed(() => {
  if (!user.value?.partnerId) return false
  return hasRole('owner') || hasRole('admin-partner')
})

// Tampilkan section Akumulatif untuk: super admin (isAdmin) ATAU pemilik mitra (isOwnerPartner)
const showAccumulativeSection = computed(() => auth.isAdmin || isOwnerPartner.value)

const partnerBranches = ref([])
const partnerWarehouses = ref([])

async function fetchPartnerLocations() {
  if (!showAccumulativeSection.value) return
  try {
    const [resBr, resWh] = await Promise.all([
      api.get('/api/v1/branches'),
      api.get('/api/v1/warehouses')
    ])
    const brRaw = resBr.data?.data
    partnerBranches.value = Array.isArray(brRaw) ? brRaw : (brRaw?.content || [])
    const whRaw = resWh.data?.data
    partnerWarehouses.value = Array.isArray(whRaw) ? whRaw : (whRaw?.content || [])
  } catch (err) {
    console.error('Failed to fetch branches/warehouses', err)
  }
}

// ─── Notification Store (Mocked as store doesn't exist) ───────────────────
const showNotifPanel = ref(false)
const notifications = ref([])
const unreadCount = ref(0)

function markAllRead() {
  // Mocked
}

function clearNotifications() {
  // Mocked
}

function handleNotifClick(n) {
  // Mocked
}

onMounted(() => {
  expandActiveParents()
  window.addEventListener('keydown', handleGlobalKeydown)
  auth.fetchMe()
  fetchPartnerLocations()
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleGlobalKeydown)
})

function isLocationActive(type, id) {
  return route.path === '/dashboard/inventory' && 
         route.query.locationType === type && 
         Number(route.query.locationId) === id
}
</script>

<template>
  <div class="flex h-screen bg-white dark:bg-zinc-950 text-foreground transition-colors duration-200 overflow-hidden">
    <Toast />

    <ConfirmDialog
      v-if="confirmStore.isOpen"
      :title="confirmStore.config.title"
      :description="confirmStore.config.description"
      :confirm-label="confirmStore.config.confirmLabel"
      :cancel-label="confirmStore.config.cancelLabel"
      :variant="confirmStore.config.variant"
      :loading="confirmStore.loading"
      @confirm="confirmStore.onConfirm"
      @cancel="confirmStore.onCancel"
    />

    <!-- Invisible Backdrop for Search to click outside — desktop only -->
    <div v-if="isSearchOpen" class="fixed inset-0 z-40 hidden lg:block" @click="closeSearch"></div>

    <!-- Mobile Sidebar Overlay — hanya untuk lg ke atas jika dibutuhkan -->
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 bg-black/50 z-40 lg:hidden"
      @click="sidebarOpen = false"
    ></div>

    <!-- ═══════════════════════════════════════════════════════════ SIDEBAR ═══ -->
    <aside
      class="flex flex-col bg-white dark:bg-zinc-950 shrink-0 transition-transform duration-300 ease-in-out lg:w-[280px] w-full"
      :class="[
        'fixed inset-y-0 left-0 z-50 lg:static lg:translate-x-0',
        sidebarOpen ? 'translate-x-0' : '-translate-x-full'
      ]"
    >
      <!-- ─── HEADER: Logo ─────────────────────────────────────────────────── -->
      <div class="flex h-12 items-center px-4 shrink-0 justify-between">
        <div class="flex items-center gap-2.5 overflow-hidden">
          <div class="w-8 h-8 rounded-lg flex items-center justify-center shrink-0">
            <svg 
              viewBox="0 0 802 845" 
              xmlns="http://www.w3.org/2000/svg" 
              class="h-8 w-8 transition-colors duration-200"
              :style="{ fill: themeStore.currentTheme === 'zinc' || themeStore.currentTheme === 'default' ? '#2563eb' : 'hsl(var(--primary))' }"
            >
              <path fill-rule="evenodd" clip-rule="evenodd" d="M453.92 4.583C451.257 10.797 447.282 25.969 445.922 35.112C440.298 72.926 444.678 117.827 457.567 154.508C464.007 172.834 465.908 180.135 465.908 186.546V192.753L463.158 198.182C459.548 205.31 452.933 210.23 442.463 213.578C434.675 216.068 416.156 218.484 414.688 217.201C414.292 216.855 416.893 215.524 420.468 214.242C428.825 211.244 436.744 204.194 439.968 196.882L442.291 191.612L442.182 184.112L442.073 176.612L437.49 160.67C430.66 136.918 428.15 122.603 425.943 94.842L424.928 82.072L423.016 78.324C420.365 73.127 406.247 59.835 392.735 49.814C379.044 39.661 356.949 26.112 354.083 26.112C352.275 26.112 349.908 46.793 349.908 62.588C349.908 96.037 356.109 125.906 367.768 148.612C370.309 153.562 372.519 157.878 372.679 158.203C372.838 158.528 367.892 159.594 361.688 160.571C338.598 164.21 306.07 175.301 281.267 187.992C263.88 196.888 251.541 205.367 241.88 215.058L233.351 223.612L229.487 231.03C224.887 239.86 221.949 250.461 221.925 258.313C221.869 276.945 213.874 289.428 194.637 300.919C168.126 316.754 126.774 330.552 48.5594 349.66C20.0164 356.633 13.3514 358.792 8.14943 362.749C-4.56057 372.416 -2.01957 400.088 13.0954 416.626C15.9934 419.797 20.6094 423.284 24.8834 425.53L31.9084 429.221L31.9234 435.917C31.9784 460.721 49.9574 487.178 75.4084 499.906C79.8084 502.106 85.9304 504.421 89.0124 505.05L94.6164 506.193L95.7504 509.979C97.3374 515.277 103.985 525.161 107.836 527.95C109.612 529.236 111.257 530.096 111.491 529.862C111.726 529.628 111.465 527.051 110.913 524.137C110.36 521.223 109.908 516.263 109.908 513.115V507.391L114.658 506.81C122.264 505.881 135.74 501.621 160.908 492.188C205.849 475.345 225.911 471.112 260.784 471.112H278.558L289.983 473.153C302.425 475.375 321.826 481.546 331.377 486.318C358.773 500.007 383.519 526.828 393.685 553.851C395.183 557.833 396.671 561.095 396.992 561.101C398.071 561.121 404.772 550.667 407.486 544.73C412.24 534.331 413.998 525.732 414.61 509.88L415.179 495.147L418.196 499.88C431.08 520.091 438.173 553.913 435.859 584.112C433.203 618.781 422.353 645.745 398.839 676.112C386.555 691.976 364.324 712.407 347.503 723.291C344.255 725.393 342.005 727.12 342.503 727.13C343.001 727.14 347.683 727.807 352.908 728.612C387.477 733.94 428.825 724.146 461.158 702.972C464.443 700.82 466.908 699.745 466.908 700.463C466.908 702.446 455.616 720.842 449.22 729.277C419.343 768.683 382.601 797.386 314.883 834.224L299.692 842.488L302.05 843.414C306.033 844.979 337.784 845.471 351.577 844.181C410.86 838.638 468.954 815.478 513.908 779.464C528.593 767.699 544.566 750.627 557.231 733.162C559.803 729.614 561.908 727.051 561.908 727.466C561.908 727.881 560.749 732.358 559.333 737.416C553.129 759.571 538.207 794.062 527.097 811.926L521.926 820.241L524.167 819.559C529.533 817.925 547.084 808.164 559.23 800.058C603.424 770.566 640.879 729.078 666.612 681.112C671.069 672.805 681.908 648.951 681.908 647.45C681.908 646.924 682.575 645.621 683.39 644.553L684.873 642.612L684.411 645.612C684.158 647.262 683.235 653.787 682.361 660.112C678.008 691.622 670.835 716.672 657.2 747.978L656.605 749.345L658.601 748.276C659.699 747.689 665.28 742.317 671.003 736.339C700.859 705.153 718.36 666.863 726.411 615.112C727.379 608.892 728.278 594.921 728.617 580.836C728.932 567.759 729.391 557.261 729.636 557.507C729.882 557.753 730.473 559.452 730.95 561.283C731.427 563.114 733.201 569.337 734.894 575.112C742.095 599.688 745.044 627.443 743.208 653.362C742.75 659.824 742.626 665.112 742.933 665.112C743.871 665.112 757.577 641.091 761.298 632.924C772.456 608.436 778.914 577.93 778.905 549.756C778.897 528.411 774.854 500.802 769.295 484.133C764.235 468.96 752.347 446.499 745.606 439.377C740.383 433.857 743.449 434.548 753.637 441.187C766.853 449.797 787.2 470.048 795.72 483.07L801.908 492.529V489.995C801.908 485.959 798.069 465.644 794.938 453.112C785.219 414.214 767.81 375.279 746.648 345.112C720.222 307.441 690.475 279.269 648.409 252.076C639.494 246.313 614.774 233.112 612.898 233.112C612.498 233.112 612.113 235.616 612.043 238.676C611.888 245.376 607.906 257.748 603.694 264.612C594.565 279.491 579.881 290.84 563.754 295.484C555.848 297.761 554.822 297.523 559.426 294.48C575.122 284.108 588.966 263.651 594.562 242.56L597.202 232.612L597.241 217.612L597.279 202.612L595.177 193.541C586.397 155.655 555.504 104.245 511.995 55.112C492.102 32.648 463.591 4.312 457.388 0.840996L455.885 0L453.92 4.583ZM215.908 487.722C197.001 490.251 178.344 495.815 149.873 507.416C145.179 509.328 136.766 512.304 131.177 514.029L121.015 517.165L122.428 521.168C126.179 531.801 137.031 543.422 148.368 548.946C166.065 557.569 184.586 557.143 217.908 547.345C264.861 533.539 287.34 530.514 312.572 534.606C321.658 536.08 339.232 541.364 341.682 543.359C344.435 545.601 344.022 542.897 340.797 537.564C326.884 514.559 302.467 497.737 272.682 490.636C252.894 485.918 235.912 485.046 215.908 487.722Z" />
            </svg>
          </div>
          <span class="text-lg font-bold tracking-tight whitespace-nowrap transition-opacity duration-200">
            GAPTEK
          </span>
        </div>
        <!-- Tombol close sidebar — hanya tampil di mobile -->
        <button
          @click="sidebarOpen = false"
          class="lg:hidden p-1.5 rounded-md hover:bg-zinc-100 dark:hover:bg-zinc-900 text-zinc-500 transition-colors"
          title="Tutup menu"
        >
          <PanelLeftClose class="w-5 h-5" />
        </button>
      </div>

      <!-- ─── SEARCH COMPONENT (Outside scrollable nav) ──────────────────── -->
      <div class="px-3 pt-4 pb-2 shrink-0 relative z-[60]">
        <!-- Normal Button (when closed) -->
        <button
          v-if="!isSearchOpen"
          @click="openSearch"
          class="flex items-center gap-2 w-full px-3 py-2 text-[13px] text-zinc-500 bg-zinc-100 dark:bg-zinc-900/70 hover:bg-zinc-200 dark:hover:bg-zinc-800 rounded-lg transition-colors outline-none border border-transparent"
        >
          <Search class="w-4 h-4 shrink-0 text-zinc-400" />
          <span class="flex-1 text-left font-medium">Cari...</span>
          <kbd class="hidden lg:inline-flex items-center justify-center h-5 px-1.5 text-[10px] font-medium rounded border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-950 text-zinc-500 shadow-sm">F</kbd>
        </button>

        <!-- Opened State — DESKTOP: floating dropdown | MOBILE: inline (tidak pakai absolute) -->

        <!-- Desktop: floating modal dropdown (lg+) -->
        <div v-if="isSearchOpen" class="hidden lg:flex absolute top-4 left-3 w-[400px] max-w-[calc(100vw-24px)] bg-white dark:bg-[#09090b] rounded-xl shadow-[0_10px_40px_rgba(0,0,0,0.1)] dark:shadow-[0_10px_40px_rgba(0,0,0,0.5)] border border-zinc-200 dark:border-zinc-800 overflow-hidden flex-col z-[60] animate-in fade-in zoom-in-95 duration-200">
          <!-- Search Input -->
          <div class="flex items-center px-3 py-2.5 border-b border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-[#09090b]">
            <Search class="w-4 h-4 text-zinc-500 shrink-0" />
            <input
              ref="searchInputRef"
              v-model="searchQuery"
              type="text"
              placeholder="Cari..."
              class="flex-1 bg-transparent border-none outline-none focus:ring-0 focus:outline-none px-2.5 text-[14px] text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-500"
              @keydown.down.prevent="navigateSearch(1)"
              @keydown.up.prevent="navigateSearch(-1)"
              @keydown.enter.prevent="selectActiveSearch"
              @keydown.esc="closeSearch"
            />
            <kbd class="inline-flex items-center justify-center h-5 px-1.5 text-[10px] font-medium rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-100 dark:bg-zinc-900 text-zinc-500 cursor-pointer shadow-sm hover:bg-zinc-200 dark:hover:bg-zinc-800 transition-colors" @click="closeSearch">Esc</kbd>
          </div>
          <!-- Results List -->
          <div class="max-h-[350px] overflow-y-auto p-1.5 custom-scrollbar bg-white dark:bg-[#09090b]">
            <div v-if="filteredSearchItems.length === 0" class="py-6 text-center text-sm text-zinc-500">
              Tidak ada hasil.
            </div>
            <button
              v-for="(item, index) in filteredSearchItems"
              :key="item.id"
              @click="() => { router.push(item.to); closeSearch(); sidebarOpen = false; }"
              @mouseenter="activeSearchIndex = index"
              class="w-full flex items-center gap-3 px-2.5 py-2.5 rounded-lg text-left transition-colors outline-none"
              :class="activeSearchIndex === index ? 'bg-zinc-100 dark:bg-zinc-800/80' : 'hover:bg-zinc-50 dark:hover:bg-zinc-900/50'"
            >
              <div class="w-8 h-8 rounded-md border border-zinc-200/50 dark:border-zinc-800/50 bg-zinc-100 dark:bg-zinc-900/50 flex items-center justify-center shrink-0">
                <component :is="item.icon" class="w-4 h-4 text-zinc-600 dark:text-zinc-400" />
              </div>
              <div class="flex flex-col overflow-hidden">
                <span class="text-[13px] font-semibold text-zinc-900 dark:text-zinc-100 truncate leading-tight mb-0.5">{{ item.label }}</span>
                <span class="text-[11px] text-zinc-500 truncate leading-none">{{ item.subtitle }}</span>
              </div>
            </button>
          </div>
        </div>
      </div>

      <!-- ─── BODY: Sidebar Panels + Mobile Footer ── -->
      <div class="flex-1 flex flex-col overflow-hidden">

        <!-- Mobile: inline search panel (menggantikan nav saat search terbuka) -->
        <div v-if="isSearchOpen" class="lg:hidden flex flex-col flex-1 overflow-hidden animate-in fade-in duration-150">
        <!-- Search Input Bar -->
        <div class="flex items-center gap-2 px-3 py-2.5 border-b border-zinc-200 dark:border-zinc-800">
          <Search class="w-4 h-4 text-zinc-500 shrink-0" />
          <input
            ref="searchInputRef"
            v-model="searchQuery"
            type="text"
            placeholder="Cari..."
            class="flex-1 bg-transparent border-none outline-none focus:ring-0 focus:outline-none text-[14px] text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-500"
            @keydown.down.prevent="navigateSearch(1)"
            @keydown.up.prevent="navigateSearch(-1)"
            @keydown.enter.prevent="selectActiveSearch"
            @keydown.esc="closeSearch"
          />
          <button
            @click="closeSearch"
            class="text-[11px] font-semibold px-2 py-1 rounded border border-zinc-200 dark:border-zinc-700 bg-zinc-100 dark:bg-zinc-900 text-zinc-500 hover:bg-zinc-200 dark:hover:bg-zinc-800 transition-colors"
          >Esc</button>
        </div>

        <!-- Results List — mengisi sisa tinggi sidebar -->
        <div class="flex-1 overflow-y-auto px-2 py-2 custom-scrollbar">
          <div v-if="filteredSearchItems.length === 0" class="py-10 text-center text-sm text-zinc-500">
            Tidak ada hasil.
          </div>
          <button
            v-for="(item, index) in filteredSearchItems"
            :key="item.id"
            @click="() => { router.push(item.to); closeSearch(); sidebarOpen = false; }"
            @mouseenter="activeSearchIndex = index"
            class="w-full flex items-center gap-3 px-3 py-3 rounded-xl text-left transition-colors outline-none"
            :class="activeSearchIndex === index ? 'bg-zinc-100 dark:bg-zinc-800/80' : 'hover:bg-zinc-50 dark:hover:bg-zinc-900/50'"
          >
            <div class="w-9 h-9 rounded-lg border border-zinc-200/50 dark:border-zinc-800/50 bg-zinc-100 dark:bg-zinc-900/50 flex items-center justify-center shrink-0">
              <component :is="item.icon" class="w-4 h-4 text-zinc-600 dark:text-zinc-400" />
            </div>
            <div class="flex flex-col overflow-hidden">
              <span class="text-[14px] font-semibold text-zinc-900 dark:text-zinc-100 truncate leading-tight">{{ item.label }}</span>
              <span class="text-[12px] text-zinc-500 truncate leading-snug mt-0.5">{{ item.subtitle }}</span>
            </div>
          </button>
        </div>
      </div>

      <!-- Nav Menu -->
      <nav class="flex-1 overflow-hidden relative" :class="{ 'hidden lg:block': isSearchOpen }">

        <!-- ══════════ PANEL 1: Main Sidebar ══════════ -->
        <div
          class="sidebar-panel absolute inset-0 overflow-y-auto py-2 px-3 custom-scrollbar"
          :class="activeSidebar === 'main' ? 'sidebar-panel--active' : 'sidebar-panel--hidden'"
        >
          <template v-for="group in filteredMenuGroups" :key="group.label">
            <div class="mb-2 mt-4 px-3">
              <p class="text-xs font-semibold text-zinc-400 dark:text-zinc-500 uppercase tracking-wider">
                {{ group.label }}
              </p>
            </div>

            <div class="space-y-0.5">
              <template v-for="item in group.items" :key="item.label">

                <!-- Sub-sidebar trigger (e.g. Inventory) -->
                <button
                  v-if="item.subSidebar"
                  @click="activeSidebar = item.subSidebar; item.to && router.push(item.to)"
                  class="flex w-full items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-all"
                  :class="isItemActive(item)
                    ? 'bg-primary/10 text-primary'
                    : 'text-zinc-600 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-900 hover:text-zinc-900 dark:hover:text-zinc-200'"
                >
                  <component :is="item.icon" class="w-4 h-4 shrink-0" />
                  <span class="flex-1 text-left">{{ item.label }}</span>
                  <ChevronRight class="w-3.5 h-3.5 text-zinc-400" />
                </button>

                <!-- Regular item (no children) -->
                <template v-else-if="!item.children">
                  <RouterLink
                    :to="item.to || '#'"
                    class="flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-all"
                    :class="isItemActive(item)
                      ? 'bg-primary/10 text-primary'
                      : 'text-zinc-600 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-900 hover:text-zinc-900 dark:hover:text-zinc-200'"
                  >
                    <component :is="item.icon" class="w-4 h-4 shrink-0" />
                    <span>{{ item.label }}</span>
                  </RouterLink>
                </template>

                <!-- Expandable item (with children) -->
                <template v-else>
                  <button
                    @click="toggleExpand(item.label)"
                    class="flex w-full items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-all"
                    :class="isItemActive(item)
                      ? 'bg-zinc-100 dark:bg-zinc-900 text-zinc-900 dark:text-zinc-200'
                      : 'text-zinc-600 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-900 hover:text-zinc-900 dark:hover:text-zinc-200'"
                  >
                    <component :is="item.icon" class="w-4 h-4 shrink-0" />
                    <span class="flex-1 text-left">{{ item.label }}</span>
                    <ChevronRight
                      class="w-3.5 h-3.5 transition-transform duration-200 text-zinc-400"
                      :class="isItemExpanded(item) ? 'rotate-90' : ''"
                    />
                  </button>

                  <div
                    v-if="isItemExpanded(item)"
                    class="ml-3 mt-0.5 space-y-0.5 pl-3 border-l border-zinc-200 dark:border-zinc-800"
                  >
                    <RouterLink
                      v-for="child in item.children"
                      :key="child.to || child.label"
                      :to="child.to || '#'"
                      class="flex items-center gap-2.5 rounded-md px-3 py-1.5 text-sm font-medium transition-all"
                      :class="isItemActive(child)
                        ? 'bg-primary/10 text-primary'
                        : 'text-zinc-500 dark:text-zinc-500 hover:bg-zinc-100 dark:hover:bg-zinc-900 hover:text-zinc-900 dark:hover:text-zinc-300'"
                    >
                      <component :is="child.icon" class="w-3.5 h-3.5 shrink-0" />
                      <span>{{ child.label }}</span>
                    </RouterLink>
                  </div>
                </template>

              </template>
            </div>
          </template>

          <!-- ══ MOBILE: Profile & Settings Panel — di bawah semua menu, dalam scroll area ══ -->
          <div class="lg:hidden mt-4 pb-28">
            <div class="border-t border-zinc-200 dark:border-zinc-800 pt-3">

              <!-- Profile row -->
              <div class="flex items-center justify-between px-2 py-2 mb-1">
                <div class="flex items-center gap-2.5">
                  <div class="relative w-8 h-8 shrink-0">
                    <div class="w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs shadow-sm border border-white/20" :style="avatarStyle">
                      {{ userInitial }}
                    </div>
                    <img v-if="userAvatar" :src="userAvatar" :alt="displayName" class="w-8 h-8 rounded-full border border-border object-cover absolute inset-0" @error="(e) => e.target.style.display = 'none'" />
                  </div>
                  <div class="flex flex-col overflow-hidden">
                    <span class="text-[13px] font-semibold text-zinc-900 dark:text-zinc-100 truncate leading-tight">{{ displayName }}</span>
                    <span class="text-[11px] text-zinc-500 truncate">{{ displayEmail }}</span>
                  </div>
                </div>
                <button @click="router.push('/dashboard/profile'); sidebarOpen = false" class="p-1.5 rounded-md text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors">
                  <Settings class="w-4 h-4" />
                </button>
              </div>

              <!-- Actions -->
              <div class="space-y-0.5">

                <!-- Beranda -->
                <button @click="router.push('/'); sidebarOpen = false" class="flex w-full items-center justify-between px-3 py-2.5 rounded-lg text-sm text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors">
                  <span>Beranda</span>
                  <Home class="h-4 w-4 text-zinc-400" />
                </button>

                <!-- Warna Tema — expandable -->
                <div>
                  <button @click="mobileThemeExpanded = !mobileThemeExpanded" class="flex w-full items-center justify-between px-3 py-2.5 rounded-lg text-sm text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors">
                    <span>Warna Tema</span>
                    <ChevronDown class="h-4 w-4 text-zinc-400 transition-transform duration-200" :class="mobileThemeExpanded ? 'rotate-180' : ''" />
                  </button>
                  <div v-if="mobileThemeExpanded" class="mt-1 ml-3 pl-3 border-l border-zinc-200 dark:border-zinc-800 space-y-0.5 pb-1">
                    <button v-for="themeOption in themeStore.themeLabels" :key="themeOption.key" @click="themeStore.setTheme(themeOption.key)" class="flex w-full items-center gap-2.5 px-2 py-2 rounded-md text-sm transition-colors" :class="themeStore.currentTheme === themeOption.key ? 'text-zinc-900 dark:text-zinc-100 bg-zinc-100 dark:bg-zinc-900' : 'text-zinc-600 dark:text-zinc-400 hover:bg-zinc-50 dark:hover:bg-zinc-900/50'">
                      <div class="w-3.5 h-3.5 rounded-full shrink-0 border border-zinc-200 dark:border-zinc-700" :style="{ backgroundColor: themeOption.color }"></div>
                      <span class="flex-1 text-left">{{ themeOption.label }}</span>
                      <Check v-if="themeStore.currentTheme === themeOption.key" class="w-3.5 h-3.5 shrink-0" />
                    </button>
                  </div>
                </div>

                <!-- Mode Tampilan -->
                <div class="flex items-center justify-between px-3 py-2.5 rounded-lg">
                  <span class="text-sm text-zinc-700 dark:text-zinc-300">Mode Tampilan</span>
                  <div class="flex items-center gap-1 bg-zinc-100 dark:bg-zinc-900 p-1 rounded-lg border border-zinc-200 dark:border-zinc-800">
                    <button @click="setThemePreference('light')" class="p-1.5 rounded-md transition-all" :class="themePreference === 'light' ? 'bg-white dark:bg-zinc-800 shadow-sm text-zinc-900 dark:text-zinc-100' : 'text-zinc-500'" title="Siang"><Sun class="h-3.5 w-3.5" /></button>
                    <button @click="setThemePreference('dark')" class="p-1.5 rounded-md transition-all" :class="themePreference === 'dark' ? 'bg-white dark:bg-zinc-800 shadow-sm text-zinc-900 dark:text-zinc-100' : 'text-zinc-500'" title="Malam"><Moon class="h-3.5 w-3.5" /></button>
                    <button @click="setThemePreference('system')" class="p-1.5 rounded-md transition-all" :class="themePreference === 'system' ? 'bg-white dark:bg-zinc-800 shadow-sm text-zinc-900 dark:text-zinc-100' : 'text-zinc-500'" title="Sistem"><Monitor class="h-3.5 w-3.5" /></button>
                  </div>
                </div>

                <!-- Keluar -->
                <button @click="auth.logout()" class="flex w-full items-center justify-between px-3 py-2.5 rounded-lg text-sm text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors">
                  <span>Keluar</span>
                  <LogOut class="h-4 w-4" />
                </button>
              </div>

              <!-- Upgrade Button -->
              <div v-if="upgradeConfig" class="mt-2">
                <router-link :to="upgradeConfig.to" @click="sidebarOpen = false">
                  <Button class="w-full justify-center bg-zinc-900 text-white hover:bg-zinc-800 dark:bg-zinc-100 dark:text-zinc-900 dark:hover:bg-zinc-200 shadow-sm font-semibold h-9">
                    {{ upgradeConfig.label }}
                  </Button>
                </router-link>
              </div>

              <!-- Plan info -->
              <div class="mt-3 flex items-center justify-between px-1 py-2 border-t border-zinc-100 dark:border-zinc-800/60">
                <span class="text-[11px] font-medium text-zinc-500">Plan Saat Ini: <span class="font-semibold text-zinc-700 dark:text-zinc-300 capitalize">{{ planConfig.label }}</span></span>
                <div :class="['h-2 w-2 rounded-full', planConfig.dotClass]"></div>
              </div>

            </div>
          </div>
        </div>

        <!-- ══════════ PANEL 2: Inventory Sub-Sidebar ══════════ -->
        <div
          class="sidebar-panel absolute inset-0 overflow-y-auto py-2 px-3 custom-scrollbar"
          :class="activeSidebar === 'inventory' ? 'sidebar-panel--active' : 'sidebar-panel--hidden'"
        >
          <!-- Back button -->
          <div class="mt-3 mb-4">
            <button
              @click="activeSidebar = 'main'"
              class="flex items-center gap-2 w-full px-3 py-2 rounded-md text-sm font-semibold text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors"
            >
              <ChevronLeft class="w-4 h-4 shrink-0" />
              <span>Inventaris</span>
            </button>
            <div class="mx-3 mt-3 mb-1 border-t border-zinc-200 dark:border-zinc-800"></div>
          </div>

          <!-- Inventory menu items -->
          <div class="space-y-0.5">
            <RouterLink
              v-for="item in INVENTORY_MENU"
              :key="item.label"
              :to="item.to"
              class="flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-all"
              :class="isItemActive(item)
                ? 'bg-primary/10 text-primary'
                : 'text-zinc-600 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-900 hover:text-zinc-900 dark:hover:text-zinc-200'"
            >
              <component :is="item.icon" class="w-4 h-4 shrink-0" />
              <span>{{ item.label }}</span>
            </RouterLink>

            <!-- Separator & Stock Akumulatif -->
            <template v-if="showAccumulativeSection">
              <!-- Section Header -->
              <div class="px-3 mt-4 mb-1 flex items-center justify-between">
                <span class="text-[11px] font-bold uppercase tracking-widest text-zinc-400 dark:text-zinc-500">Akumulatif</span>
              </div>
              <div class="mx-3 mb-3 border-t border-zinc-200 dark:border-zinc-800"></div>
              
              <div class="mb-4">
                <!-- Stock Cabang -->
                <div class="mb-3">
                  <div class="px-3 py-1 flex items-center gap-2 text-xs font-semibold text-zinc-500 dark:text-zinc-400">
                    <Building2 class="w-3.5 h-3.5 opacity-60" />
                    <span>Cabang</span>
                  </div>
                  <div class="ml-3 pl-3 border-l border-zinc-200 dark:border-zinc-800 space-y-0.5">
                    <RouterLink
                      v-for="branch in partnerBranches"
                      :key="branch.id"
                      :to="`/dashboard/inventory?locationType=BRANCH&locationId=${branch.id}`"
                      class="flex items-center gap-2 rounded-md px-3 py-1.5 text-xs font-medium transition-all"
                      :class="isLocationActive('BRANCH', branch.id)
                        ? 'bg-primary/10 text-primary font-semibold'
                        : 'text-zinc-600 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-900 hover:text-zinc-900 dark:hover:text-zinc-200'"
                    >
                      <span>{{ branch.name }}</span>
                    </RouterLink>
                    <div v-if="partnerBranches.length === 0" class="px-3 py-1 text-[11px] text-zinc-400 dark:text-zinc-600 italic">
                      Tidak ada cabang.
                    </div>
                  </div>
                </div>
                
                <!-- Stock Gudang -->
                <div>
                  <div class="px-3 py-1 flex items-center gap-2 text-xs font-semibold text-zinc-500 dark:text-zinc-400">
                    <Warehouse class="w-3.5 h-3.5 opacity-60" />
                    <span>Gudang</span>
                  </div>
                  <div class="ml-3 pl-3 border-l border-zinc-200 dark:border-zinc-800 space-y-0.5">
                    <RouterLink
                      v-for="wh in partnerWarehouses"
                      :key="wh.id"
                      :to="`/dashboard/inventory?locationType=WAREHOUSE&locationId=${wh.id}`"
                      class="flex items-center gap-2 rounded-md px-3 py-1.5 text-xs font-medium transition-all"
                      :class="isLocationActive('WAREHOUSE', wh.id)
                        ? 'bg-primary/10 text-primary font-semibold'
                        : 'text-zinc-600 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-900 hover:text-zinc-900 dark:hover:text-zinc-200'"
                    >
                      <span>{{ wh.name }}</span>
                    </RouterLink>
                    <div v-if="partnerWarehouses.length === 0" class="px-3 py-1 text-[11px] text-zinc-400 dark:text-zinc-600 italic">
                      Tidak ada gudang.
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>

      </nav>

      <!-- ─── FOOTER: Profile Button — DESKTOP ONLY ───────────────────────── -->
      <div class="hidden lg:block p-2 shrink-0 relative z-30">
        <DropdownMenu>
          <DropdownMenuTrigger as-child>
            <button
              class="flex w-full items-center gap-2 rounded-md p-1.5 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors outline-none"
            >
              <div class="relative w-8 h-8 shrink-0">
                <div
                  class="w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs shadow-sm border border-white/20"
                  :style="avatarStyle"
                >
                  {{ userInitial }}
                </div>
                <img
                  v-if="userAvatar"
                  :src="userAvatar"
                  :alt="displayName"
                  class="w-8 h-8 rounded-full border border-border object-cover absolute inset-0"
                  @error="(e) => e.target.style.display = 'none'"
                />
              </div>
              <div class="flex flex-col flex-1 text-left overflow-hidden">
                <span class="text-sm font-semibold text-zinc-900 dark:text-zinc-100 truncate leading-none mb-0.5">{{ displayName }}</span>
                <span class="text-[10px] text-zinc-500 dark:text-zinc-500 font-medium tracking-tight uppercase">{{ user?.role || 'Administrator' }}</span>
              </div>
              <ChevronDown class="w-3.5 h-3.5 text-zinc-400 shrink-0" />
            </button>
          </DropdownMenuTrigger>

          <DropdownMenuContent side="top" align="start" :side-offset="8" class="w-[280px] p-0">
            <div class="flex items-center justify-between px-3 py-3 border-b border-border">
              <div class="flex flex-col space-y-0.5">
                <p class="text-sm font-semibold leading-none text-zinc-900 dark:text-zinc-100">{{ displayName }}</p>
                <p class="text-[13px] leading-none text-zinc-500">{{ displayEmail }}</p>
              </div>
              <button @click="router.push('/dashboard/profile')" class="text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100 transition-colors">
                <Settings class="h-4 w-4" />
              </button>
            </div>
            <div class="p-1">
              <DropdownMenuItem @click="router.push('/')" class="justify-between px-2 py-2 text-sm cursor-pointer">
                <span>Beranda</span>
                <Home class="h-4 w-4 text-zinc-500" />
              </DropdownMenuItem>
              <DropdownMenuSub>
                <DropdownMenuSubTrigger class="flex w-full justify-between items-center px-2 py-2 text-sm cursor-pointer outline-none">
                  <span>Warna Tema</span>
                </DropdownMenuSubTrigger>
                <DropdownMenuPortal>
                  <DropdownMenuSubContent side="right" align="start" class="min-w-[140px]">
                    <DropdownMenuItem v-for="themeOption in themeStore.themeLabels" :key="themeOption.key" @click="themeStore.setTheme(themeOption.key)" class="flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer">
                      <div class="w-3.5 h-3.5 rounded-full shrink-0 shadow-sm border border-zinc-200 dark:border-zinc-700" :style="{ backgroundColor: themeOption.color }"></div>
                      <span class="flex-1">{{ themeOption.label }}</span>
                      <Check v-if="themeStore.currentTheme === themeOption.key" class="w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto" />
                    </DropdownMenuItem>
                  </DropdownMenuSubContent>
                </DropdownMenuPortal>
              </DropdownMenuSub>
              <div class="px-2 py-2 flex items-center justify-between border-t border-border mt-1 pt-3">
                <span class="text-sm">Mode Tampilan</span>
                <div class="flex items-center gap-1 bg-zinc-100 dark:bg-zinc-900 p-1 rounded-lg border border-zinc-200 dark:border-zinc-800">
                  <button @click="setThemePreference('light')" class="p-1.5 rounded-md transition-all" :class="themePreference === 'light' ? 'bg-white dark:bg-zinc-800 shadow-sm text-zinc-900 dark:text-zinc-100' : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100'" title="Siang"><Sun class="h-3.5 w-3.5" /></button>
                  <button @click="setThemePreference('dark')" class="p-1.5 rounded-md transition-all" :class="themePreference === 'dark' ? 'bg-white dark:bg-zinc-800 shadow-sm text-zinc-900 dark:text-zinc-100' : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100'" title="Malam"><Moon class="h-3.5 w-3.5" /></button>
                  <button @click="setThemePreference('system')" class="p-1.5 rounded-md transition-all" :class="themePreference === 'system' ? 'bg-white dark:bg-zinc-800 shadow-sm text-zinc-900 dark:text-zinc-100' : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100'" title="Sistem"><Monitor class="h-3.5 w-3.5" /></button>
                </div>
              </div>
              <DropdownMenuItem @click="auth.logout()" class="justify-between px-2 py-2 text-sm cursor-pointer text-zinc-900 dark:text-zinc-100">
                <span>Keluar</span>
                <LogOut class="h-4 w-4 text-zinc-500" />
              </DropdownMenuItem>
            </div>
            <div v-if="upgradeConfig" class="px-3 pb-3">
              <router-link :to="upgradeConfig.to">
                <Button class="w-full justify-center bg-zinc-900 text-white hover:bg-zinc-800 dark:bg-zinc-100 dark:text-zinc-900 dark:hover:bg-zinc-200 shadow-sm font-semibold h-9 mt-1">{{ upgradeConfig.label }}</Button>
              </router-link>
            </div>
            <div class="border-t border-border bg-zinc-50/50 dark:bg-zinc-900/50 px-3 py-2.5 flex items-center justify-between rounded-b-md">
              <div class="flex flex-col">
                <span class="text-xs font-medium text-zinc-500">Plan Saat Ini</span>
                <span class="text-[13px] text-zinc-900 dark:text-zinc-100 font-semibold capitalize">{{ planConfig.label }}</span>
              </div>
              <div :class="['h-2.5 w-2.5 rounded-full', planConfig.dotClass]"></div>
            </div>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
      </div><!-- end: BODY wrapper -->
    </aside>

    <!-- ═══════════════════════════════════════════════════════════ MAIN ════════ -->
    <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <!-- ─── CLEAN TOP BAR ─────────────────────────────────────────────────────── -->
      <header class="relative flex h-12 shrink-0 items-center justify-between bg-white dark:bg-zinc-950 px-4">
        <!-- Left: Toggle Sidebar -->
        <div class="flex items-center gap-4 w-1/3">
        </div>

        <!-- Center: Page Title -->
        <div class="absolute left-1/2 -translate-x-1/2 text-[14px] font-semibold text-zinc-800 dark:text-zinc-200 truncate max-w-[50%] text-center pointer-events-none">
          {{ currentPageTitle }}
        </div>

        <div class="flex items-center justify-end gap-3 w-1/3">
          <div class="relative">
            <button
              @click="showNotifPanel = !showNotifPanel"
              class="relative p-1.5 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-900 text-zinc-500 transition-colors"
              title="Notifikasi"
            >
              <Bell class="w-4 h-4" :class="unreadCount > 0 ? 'text-primary' : ''" />
              <span v-if="unreadCount > 0"
                class="absolute -top-1 -right-1 min-w-[16px] h-4 px-1 rounded-full bg-red-500 text-white text-[9px] font-black flex items-center justify-center leading-none">
                {{ unreadCount > 9 ? '9+' : unreadCount }}
              </span>
            </button>

            <div v-if="showNotifPanel" class="fixed inset-0 z-40" @click="showNotifPanel = false" />

            <Transition name="notif-panel">
              <div v-if="showNotifPanel"
                class="absolute right-0 top-full mt-2 z-50 w-[360px] bg-white dark:bg-zinc-900 rounded-2xl shadow-2xl border border-zinc-200 dark:border-zinc-800 overflow-hidden">

                <!-- Header -->
                <div class="flex items-center justify-between px-4 py-3 border-b border-zinc-100 dark:border-zinc-800">
                  <div class="flex items-center gap-2">
                    <Bell class="w-3.5 h-3.5 text-zinc-500" />
                    <span class="text-[13px] font-bold">Notifikasi</span>
                    <span v-if="unreadCount > 0"
                      class="text-[10px] font-bold px-1.5 py-0.5 rounded-full bg-red-50 text-red-600 dark:bg-red-900/20 dark:text-red-400">
                      {{ unreadCount }} baru
                    </span>
                  </div>
                  <div class="flex items-center gap-2">
                    <button v-if="unreadCount > 0" @click="markAllRead"
                      class="text-[10px] font-semibold text-primary hover:underline">
                      Tandai dibaca
                    </button>
                    <button v-if="notifications.length > 0" @click="clearNotifications"
                      class="text-[10px] font-semibold text-zinc-400 hover:text-red-500 transition-colors">
                      Hapus
                    </button>
                  </div>
                </div>

                <!-- List -->
                <div class="max-h-[420px] overflow-y-auto custom-scrollbar">
                  <!-- Empty state -->
                  <div v-if="notifications.length === 0"
                    class="flex flex-col items-center justify-center py-12 text-zinc-400 gap-2">
                    <Bell class="w-8 h-8 opacity-20" />
                    <p class="text-xs font-medium">Tidak ada notifikasi</p>
                  </div>

                  <!-- Items -->
                  <div
                    v-for="n in notifications"
                    :key="n.id"
                    @click="handleNotifClick(n)"
                    :class="[
                      'flex items-start gap-3 px-4 py-3 cursor-pointer transition-colors border-b border-zinc-100 dark:border-zinc-800/60 last:border-0',
                      n.isRead
                        ? 'hover:bg-zinc-50 dark:hover:bg-zinc-800/40'
                        : 'bg-amber-50/40 dark:bg-amber-900/10 hover:bg-amber-50/70 dark:hover:bg-amber-900/20'
                    ]"
                  >
                    <!-- Icon badge -->
                    <div :class="[
                      'w-8 h-8 rounded-lg flex items-center justify-center shrink-0 mt-0.5',
                      n.severity === 'ERROR'   ? 'bg-red-100 dark:bg-red-900/30' :
                      n.severity === 'WARNING' ? 'bg-amber-100 dark:bg-amber-900/30' :
                                                 'bg-blue-100 dark:bg-blue-900/30'
                    ]">
                      <!-- ERROR / WARNING -->
                      <svg v-if="n.severity === 'ERROR' || n.severity === 'WARNING'"
                        class="w-4 h-4"
                        :class="n.severity === 'ERROR' ? 'text-red-500' : 'text-amber-500'"
                        fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round"
                          d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
                      </svg>
                      <!-- INFO -->
                      <Bell v-else class="w-4 h-4 text-blue-500" />
                    </div>

                    <!-- Content -->
                    <div class="flex-1 min-w-0">
                      <p class="text-[12px] font-bold text-zinc-900 dark:text-zinc-100 leading-snug">{{ n.title }}</p>
                      <p class="text-[11px] text-zinc-500 mt-0.5 leading-snug line-clamp-2">{{ n.message }}</p>
                      <p class="text-[10px] text-zinc-400 mt-1">
                        {{ n.createdAt ? new Date(n.createdAt).toLocaleString('id-ID', { day:'2-digit', month:'short', hour:'2-digit', minute:'2-digit' }) : '' }}
                      </p>
                    </div>

                    <!-- Unread dot -->
                    <div v-if="!n.isRead" class="w-2 h-2 rounded-full bg-primary shrink-0 mt-2" />
                  </div>
                </div>

                <!-- Footer -->
                <div class="px-4 py-2.5 border-t border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/50 flex items-center justify-between">
                  <p class="text-[10px] text-zinc-400">Diperbarui otomatis setiap 1 menit</p>
                  <button
                    @click="router.push('/dashboard/notifications'); showNotifPanel = false"
                    class="text-[10px] font-semibold text-primary hover:underline"
                  >
                    Lihat semua →
                  </button>
                </div>
              </div>
            </Transition>
          </div>
        </div>
      </header>

      <!-- ─── PAGE CONTENT ────────────────────────────────────────────────── -->
      <main class="flex-1 overflow-y-auto custom-scrollbar bg-zinc-100 dark:bg-zinc-900 rounded-tl-2xl scroll-smooth overscroll-contain shadow-[inset_1px_1px_0_rgba(255,255,255,0.04)]">
        <!-- Page content wrapper -->
        <div class="p-5 pb-24 lg:pb-5">
          <slot />
        </div>
      </main>
    </div>

    <!-- About Modal -->
    <AboutModal 
      :is-open="isAboutModalOpen" 
      @close="isAboutModalOpen = false" 
    />

    <!-- ═══════════════════════════════════════════════════════════ MOBILE FAB ═══ -->
    <!-- Floating Action Bar — hanya tampil di mobile (hidden di lg+) -->
    <div class="fixed bottom-6 left-1/2 -translate-x-1/2 z-[60] lg:hidden">
      <div class="flex items-center gap-0 bg-zinc-900 dark:bg-zinc-800 rounded-2xl shadow-2xl px-1 py-1 border border-zinc-700/60">
        <!-- Search Button -->
        <button
          @click="openSearch"
          class="flex items-center gap-2 px-4 py-2.5 rounded-xl text-zinc-300 hover:text-white hover:bg-zinc-700/50 transition-all"
        >
          <Search class="w-4 h-4 shrink-0" />
          <span class="text-[13px] font-medium">Cari...</span>
        </button>

        <!-- Divider -->
        <div class="w-px h-5 bg-zinc-600/60 mx-1"></div>

        <!-- Menu / Sidebar Toggle Button -->
        <button
          @click="toggleSidebar"
          class="flex items-center justify-center p-2.5 rounded-xl text-zinc-300 hover:text-white hover:bg-zinc-700/50 transition-all"
          title="Buka menu"
        >
          <component :is="sidebarOpen ? PanelLeftClose : PanelLeftOpen" class="w-4 h-4" />
        </button>
      </div>
    </div>
  </div>

</template>

<style>
/* Sidebar panel – fade+blur via CSS class (v-show, no unmount = no scroll reset) */
.sidebar-panel {
  transition: opacity 0.15s ease, filter 0.15s ease;
}
.sidebar-panel--active {
  opacity: 1;
  filter: blur(0px);
  pointer-events: auto;
  visibility: visible;
}
.sidebar-panel--hidden {
  opacity: 0;
  filter: blur(5px);
  pointer-events: none;
  visibility: hidden;
}

.custom-scrollbar {
  scrollbar-width: thin;
  scrollbar-color: hsl(var(--muted-foreground) / 0.2) transparent;
  -webkit-overflow-scrolling: touch;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: hsl(var(--muted-foreground) / 0.2);
  border-radius: 20px;
  border: 1px solid transparent;
  background-clip: content-box;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: hsl(var(--muted-foreground) / 0.4);
}

/* Smooth transitions */
html {
  transition: color 0.2s ease, background-color 0.2s ease;
}

/* Notification panel transition */
.notif-panel-enter-active, .notif-panel-leave-active {
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.notif-panel-enter-from, .notif-panel-leave-to {
  opacity: 0;
  transform: scale(0.97) translateY(-6px);
}
</style>