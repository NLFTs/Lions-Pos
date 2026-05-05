<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { usePermission } from '@/composables/usePermission'
import { useConfirmStore } from '@/stores/confirm'
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
  MapPin,
  ShoppingCart,
  Search,
  Ticket,
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

// About Modal state
const isAboutModalOpen = ref(false)

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

// ─── Menu Groups (dengan section header) ────────────────────────────────────
const MENU_GROUPS = [
  {
    label: 'Management',
    items: [
      {
        label: 'Dashboard',
        icon: LayoutDashboard,
        to: '/dashboard',
        permission: null,
      },
      {
        label: 'User Management',
        icon: Users,
        to: '/dashboard/users',
        permission: 'user.index',
      },
      {
        label: 'Konten',
        icon: FileText,
        permission: null,
        children: [
          { label: 'Produk', icon: ScrollText, to: '/dashboard/products', permission: 'post.index' },
          { label: 'Kategori', icon: FileText, to: '/dashboard/categories', permission: 'category.index' },
          { label: 'Mutasi Stok', icon: ArrowLeftRight, to: '/dashboard/stock-mutations', permission: null },
          { label: 'Partner', icon: Users, to: '/dashboard/partners', permission: null },
          { label: 'Lokasi', icon: MapPin, to: '/dashboard/locations', permission: null },
          { label: 'Voucer', icon: Ticket, to: '/dashboard/vouchers', permission: null },
        ],
      },
    ],
  },
  {
    label: 'Access Control',
    items: [
      {
        label: 'Roles',
        icon: ShieldCheck,
        to: '/dashboard/roles',
        permission: 'role.index',
      },
      {
        label: 'Permissions',
        icon: KeyRound,
        to: '/dashboard/permissions',
        permission: 'permission.index',
      },
      {
        label: 'Modul',
        icon: Zap,
        to: '/dashboard/modules',
        permission: 'module.index',
      },
    ],
  },
  {
    label: 'Transaksi',
    items: [
      {
        label: 'Kasir',
        icon: ShoppingCart,
        to: '/dashboard/kasir',
        permission: null,
      },
    ],
  },
  {
    label: 'Master Data',
    items: [
      {
        label: 'Audit Log',
        icon: Activity,
        to: '/dashboard/logs',
        permission: 'log.index',
      },
    ],
  },
]

// PROFILE_MENU_ITEMS removed

// Filter menu by permission
function filterMenu(groups) {
  return groups.reduce((acc, group) => {
    const filteredItems = group.items.reduce((items, item) => {
      if (item.permission && !can(item.permission)) return items
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

// Watch route changes and expand active parents
watch(() => route.path, () => {
  expandActiveParents()
  sidebarOpen.value = false
}, { immediate: true })

// handleProfileMenuAction removed

// ─── User Info ────────────────────────────────────────────────────────
const displayName = computed(() => user.value?.fullname || user.value?.username || 'User')
const displayEmail = computed(() => user.value?.username || '')
const userInitial = computed(() => {
  const name = displayName.value
  return name.charAt(0).toUpperCase()
})

// ─── Language Switcher ────────────────────────────────────────────────
const languages = [
  { code: 'ID', label: 'ID' },
  { code: 'EN', label: 'EN' },
]
const currentLang = ref('EN')

function switchLang(code) {
  currentLang.value = code
  // TODO: Implement actual language switching logic
}

// Get active color based on theme
const activeLanguageColor = computed(() => {
  // Use primary color from theme
  return 'text-[var(--primary)]'
})

// Theme Preference State (system | light | dark)
const themePreference = ref(localStorage.getItem('isDark') === null ? 'system' : (themeStore.isDark ? 'dark' : 'light'))

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
          to: item.to,
          icon: item.icon
        })
      }
    })
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
  if (route.path === '/dashboard') return 'Overview'
  
  const matchedItem = searchableMenuItems.value.find(item => item.to === route.path)
  if (matchedItem) return matchedItem.label
  
  const segments = route.path.split('/').filter(Boolean)
  if (segments.length > 0) {
    const last = segments[segments.length - 1]
    return last.charAt(0).toUpperCase() + last.slice(1).replace(/-/g, ' ')
  }
  
  return 'Overview'
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
  setTimeout(() => {
    if (searchInputRef.value) searchInputRef.value.focus()
  }, 150)
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

onMounted(() => {
  expandActiveParents()
  window.addEventListener('keydown', handleGlobalKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleGlobalKeydown)
})
</script>

<template>
  <div class="flex h-screen bg-background text-foreground transition-colors duration-200 overflow-hidden">
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

    <!-- Invisible Backdrop for Search to click outside -->
    <div v-if="isSearchOpen" class="fixed inset-0 z-40" @click="closeSearch"></div>

    <!-- Mobile Sidebar Overlay -->
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 bg-black/50 z-40 lg:hidden transition-opacity"
      @click="sidebarOpen = false"
    ></div>

    <!-- ═══════════════════════════════════════════════════════════ SIDEBAR ═══ -->
    <aside
      class="flex flex-col bg-white dark:bg-zinc-950 border-r border-border shrink-0 transition-transform duration-300 ease-in-out w-[280px]"
      :class="[
        'fixed inset-y-0 left-0 z-50 lg:static lg:translate-x-0',
        sidebarOpen ? 'translate-x-0' : '-translate-x-full'
      ]"
    >
      <!-- ─── HEADER: Logo ─────────────────────────────────────────────────── -->
      <div class="flex h-12 items-center px-4 border-b border-border shrink-0">
        <div class="flex items-center gap-2.5 overflow-hidden">
          <div class="w-8 h-8 bg-primary rounded-lg flex items-center justify-center shrink-0">
            <Zap class="w-5 h-5 text-primary-foreground" />
          </div>
          <span class="text-lg font-bold tracking-tight whitespace-nowrap transition-opacity duration-200">
            GAPTEK
          </span>
        </div>
      </div>

      <!-- ─── SEARCH COMPONENT (Outside scrollable nav) ──────────────────── -->
      <div class="px-3 pt-4 pb-2 shrink-0 relative z-[60]">
        <!-- Normal Button (when closed) -->
        <button
          v-if="!isSearchOpen"
          @click="openSearch"
          class="flex items-center gap-2 w-full px-3 py-2 text-[13px] text-zinc-500 bg-zinc-100 dark:bg-zinc-900 hover:bg-zinc-200 dark:hover:bg-zinc-800 rounded-lg transition-colors outline-none border border-transparent"
        >
          <Search class="w-4 h-4 shrink-0 text-zinc-400" />
          <span class="flex-1 text-left font-medium">Find...</span>
          <kbd class="hidden lg:inline-flex items-center justify-center h-5 px-1.5 text-[10px] font-medium rounded border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-950 text-zinc-500 shadow-sm">F</kbd>
        </button>
        
        <!-- Opened State (Input + Dropdown) -->
        <div v-else class="absolute top-4 left-3 w-[400px] max-w-[calc(100vw-24px)] bg-white dark:bg-[#09090b] rounded-xl shadow-[0_10px_40px_rgba(0,0,0,0.1)] dark:shadow-[0_10px_40px_rgba(0,0,0,0.5)] border border-zinc-200 dark:border-zinc-800 overflow-hidden flex flex-col z-[60] animate-in fade-in zoom-in-95 duration-200">
          <!-- Search Input -->
          <div class="flex items-center px-3 py-2.5 border-b border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-[#09090b]">
            <Search class="w-4 h-4 text-zinc-500 shrink-0" />
            <input
              ref="searchInputRef"
              v-model="searchQuery"
              type="text"
              placeholder="Find..."
              class="flex-1 bg-transparent border-none outline-none focus:ring-0 focus:outline-none px-2.5 text-[14px] text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-500"
              @keydown.down.prevent="navigateSearch(1)"
              @keydown.up.prevent="navigateSearch(-1)"
              @keydown.enter.prevent="selectActiveSearch"
              @keydown.esc="closeSearch"
            />
            <kbd class="hidden sm:inline-flex items-center justify-center h-5 px-1.5 text-[10px] font-medium rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-100 dark:bg-zinc-900 text-zinc-500 cursor-pointer shadow-sm hover:bg-zinc-200 dark:hover:bg-zinc-800 transition-colors" @click="closeSearch">Esc</kbd>
          </div>

          <!-- Results List -->
          <div class="max-h-[350px] overflow-y-auto p-1.5 custom-scrollbar bg-white dark:bg-[#09090b]">
            <div v-if="filteredSearchItems.length === 0" class="py-6 text-center text-sm text-zinc-500">
              No results found.
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

      <!-- ─── BODY: Menu dengan Groups ─────────────────────────────────────── -->
      <nav class="flex-1 overflow-y-auto py-2 px-3 custom-scrollbar">
          <template v-for="group in filteredMenuGroups" :key="group.label">
            <!-- Group Header -->
            <div class="mb-2 mt-4 px-3">
              <p class="text-xs font-semibold text-zinc-400 dark:text-zinc-500 uppercase tracking-wider">
                {{ group.label }}
              </p>
            </div>

            <!-- Menu Items -->
            <div class="space-y-0.5">
              <template v-for="item in group.items" :key="item.label">
                <!-- Item without children -->
                <template v-if="!item.children">
                  <RouterLink
                    :to="item.to"
                    class="flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-all"
                    :class="isItemActive(item)
                      ? 'bg-primary/10 text-primary'
                      : 'text-zinc-600 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-900 hover:text-zinc-900 dark:hover:text-zinc-200'"
                  >
                    <component :is="item.icon" class="w-4 h-4 shrink-0" />
                    <span>{{ item.label }}</span>
                  </RouterLink>
                </template>

                <!-- Item with children -->
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

                  <!-- Sub-items -->
                  <div
                    v-if="isItemExpanded(item)"
                    class="ml-3 mt-0.5 space-y-0.5 pl-3 border-l border-zinc-200 dark:border-zinc-800"
                  >
                    <RouterLink
                      v-for="child in item.children"
                      :key="child.to || child.label"
                      :to="child.to"
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
      </nav>

      <!-- ─── FOOTER: Profile Button ───────────────────────────────────────── -->
      <div class="p-2 border-t border-border shrink-0 relative z-30">
        <DropdownMenu>
          <DropdownMenuTrigger as-child>
            <button
              class="flex w-full items-center gap-2 rounded-md p-1.5 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors outline-none"
            >
              <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-xs shrink-0 shadow-sm border border-primary/20">
                {{ userInitial }}
              </div>
              <div class="flex flex-col flex-1 text-left overflow-hidden">
                <span class="text-sm font-semibold text-zinc-900 dark:text-zinc-100 truncate leading-none mb-0.5">{{ displayName }}</span>
                <span class="text-[10px] text-zinc-500 dark:text-zinc-500 font-medium tracking-tight uppercase">{{ user?.role || 'Administrator' }}</span>
              </div>
              <ChevronDown class="w-3.5 h-3.5 text-zinc-400 shrink-0" />
            </button>
          </DropdownMenuTrigger>

          <DropdownMenuContent side="top" align="start" :side-offset="8" class="w-[280px] p-0">
            <!-- Header -->
            <div class="flex items-center justify-between px-3 py-3 border-b border-border">
              <div class="flex flex-col space-y-0.5">
                <p class="text-sm font-semibold leading-none text-zinc-900 dark:text-zinc-100">{{ displayName }}</p>
                <p class="text-[13px] leading-none text-zinc-500">{{ displayEmail }}</p>
              </div>
              <button @click="router.push('/dashboard/profile')" class="text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100 transition-colors">
                <Settings class="h-4 w-4" />
              </button>
            </div>

            <!-- Main actions -->
            <div class="p-1">
              <DropdownMenuItem @click="router.push('/')" class="justify-between px-2 py-2 text-sm cursor-pointer">
                <span>Home Page</span>
                <Home class="h-4 w-4 text-zinc-500" />
              </DropdownMenuItem>

              <DropdownMenuSub>
                <DropdownMenuSubTrigger class="flex w-full justify-between items-center px-2 py-2 text-sm cursor-pointer outline-none">
                  <span>Theme Color</span>
                </DropdownMenuSubTrigger>
                <DropdownMenuPortal>
                  <DropdownMenuSubContent side="right" align="start" class="min-w-[140px]">
                    <DropdownMenuItem
                      v-for="themeOption in themeStore.themeLabels"
                      :key="themeOption.key"
                      @click="themeStore.setTheme(themeOption.key)"
                      class="flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer"
                    >
                      <div class="w-3.5 h-3.5 rounded-full shrink-0 shadow-sm border border-zinc-200 dark:border-zinc-700" :style="{ backgroundColor: themeOption.color }"></div>
                      <span class="flex-1">{{ themeOption.label }}</span>
                      <Check
                        v-if="themeStore.currentTheme === themeOption.key"
                        class="w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto"
                      />
                    </DropdownMenuItem>
                  </DropdownMenuSubContent>
                </DropdownMenuPortal>
              </DropdownMenuSub>

              <DropdownMenuSub>
                <DropdownMenuSubTrigger class="flex w-full justify-between items-center px-2 py-2 text-sm cursor-pointer outline-none">
                  <span>Display Mode</span>
                </DropdownMenuSubTrigger>
                <DropdownMenuPortal>
                  <DropdownMenuSubContent side="right" align="start" class="min-w-[140px]">
                    <DropdownMenuItem @click="setThemePreference('light')" class="flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer">
                      <Sun class="h-4 w-4 text-zinc-500" />
                      <span class="flex-1">Siang (Light)</span>
                      <Check v-if="themePreference === 'light'" class="w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto" />
                    </DropdownMenuItem>
                    <DropdownMenuItem @click="setThemePreference('dark')" class="flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer">
                      <Moon class="h-4 w-4 text-zinc-500" />
                      <span class="flex-1">Malam (Dark)</span>
                      <Check v-if="themePreference === 'dark'" class="w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto" />
                    </DropdownMenuItem>
                    <DropdownMenuItem @click="setThemePreference('system')" class="flex items-center gap-2.5 px-2 py-1.5 text-sm cursor-pointer">
                      <Monitor class="h-4 w-4 text-zinc-500" />
                      <span class="flex-1">Sistem</span>
                      <Check v-if="themePreference === 'system'" class="w-3.5 h-3.5 text-zinc-900 dark:text-zinc-100 shrink-0 ml-auto" />
                    </DropdownMenuItem>
                  </DropdownMenuSubContent>
                </DropdownMenuPortal>
              </DropdownMenuSub>

              <DropdownMenuItem @click="auth.logout()" class="justify-between px-2 py-2 text-sm cursor-pointer text-zinc-900 dark:text-zinc-100">
                <span>Log Out</span>
                <LogOut class="h-4 w-4 text-zinc-500" />
              </DropdownMenuItem>
            </div>

            <!-- Upgrade Button -->
            <!-- <div class="px-3 pb-3">
              <Button class="w-full justify-center bg-zinc-900 text-white hover:bg-zinc-800 dark:bg-zinc-100 dark:text-zinc-900 dark:hover:bg-zinc-200 shadow-sm font-semibold h-9 mt-1">
                Upgrade to Pro
              </Button>
            </div> -->

            <div class="border-t border-border bg-zinc-50/50 dark:bg-zinc-900/50 px-3 py-2.5 flex items-center justify-between rounded-b-md">
              <div class="flex flex-col">
                <span class="text-xs font-medium text-zinc-500">Platform Status</span>
                <span class="text-[13px] text-zinc-900 dark:text-zinc-100">All systems normal.</span>
              </div>
              <div class="h-2.5 w-2.5 rounded-full bg-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.5)]"></div>
            </div>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </aside>

    <!-- ═══════════════════════════════════════════════════════════ MAIN ════════ -->
    <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <!-- ─── CLEAN TOP BAR ─────────────────────────────────────────────────────── -->
      <header class="relative flex h-12 shrink-0 items-center justify-between bg-white dark:bg-zinc-950 border-b border-border px-4">
        <!-- Left: Toggle Sidebar -->
        <div class="flex items-center gap-4 w-1/3">
          <button
            @click="toggleSidebar"
            class="p-1.5 rounded-md hover:bg-zinc-100 dark:hover:bg-zinc-900 text-zinc-500 transition-colors lg:hidden"
            title="Toggle sidebar"
          >
            <PanelLeftOpen class="w-4 h-4" />
          </button>
        </div>

        <!-- Center: Page Title -->
        <div class="absolute left-1/2 -translate-x-1/2 text-[14px] font-semibold text-zinc-800 dark:text-zinc-200 truncate max-w-[50%] text-center pointer-events-none">
          {{ currentPageTitle }}
        </div>

        <!-- Right: Language + Help -->
        <div class="flex items-center justify-end gap-3 w-1/3">
          <!-- Language Switcher -->
          <div class="flex items-center gap-2 text-[11px] font-medium tracking-tight">
            <button
              @click="switchLang('ID')"
              class="transition-colors px-1"
              :class="currentLang === 'ID' ? 'font-semibold text-primary' : 'text-zinc-400 hover:text-zinc-600'"
            >
              ID
            </button>
            <span class="text-zinc-300 dark:text-zinc-700">|</span>
            <button
              @click="switchLang('EN')"
              class="transition-colors px-1"
              :class="currentLang === 'EN' ? 'font-semibold text-primary' : 'text-zinc-400 hover:text-zinc-600'"
            >
              EN
            </button>
          </div>

          <!-- Help Button -->
          <button
            class="p-1.5 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-900 text-zinc-500 transition-colors"
            title="Help"
          >
            <HelpCircle class="w-4 h-4" />
          </button>
        </div>
      </header>

      <!-- ─── PAGE CONTENT ────────────────────────────────────────────────── -->
      <main class="flex-1 overflow-y-auto custom-scrollbar bg-zinc-50/50 dark:bg-zinc-950">
        <!-- Page content wrapper -->
        <div class="p-5">
          <slot />
        </div>
      </main>
    </div>

    <!-- About Modal -->
    <AboutModal 
      :is-open="isAboutModalOpen" 
      @close="isAboutModalOpen = false" 
    />
  </div>
</template>

<style>
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

/* Smooth transitions */
html {
  transition: color 0.2s ease, background-color 0.2s ease;
}
</style>
