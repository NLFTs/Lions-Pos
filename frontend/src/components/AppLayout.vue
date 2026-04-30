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
  SmilePlus,
  Home,
  Pencil,
  LifeBuoy,
  Book,
} from 'lucide-vue-next'
import Toast from '@/components/ui/Toast.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
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
          { label: 'Posts', icon: ScrollText, to: '/dashboard/posts', permission: 'post.index' },
          { label: 'Kategori', icon: FileText, to: '/dashboard/categories', permission: 'category.index' },
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

onMounted(() => {
  expandActiveParents()
})

onBeforeUnmount(() => {
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
            Spravel
          </span>
        </div>
      </div>

      <!-- ─── BODY: Menu dengan Groups ─────────────────────────────────────── -->
      <nav class="flex-1 overflow-y-auto py-4 px-3 custom-scrollbar">
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
              <DropdownMenuItem class="justify-between px-2 py-2 text-sm cursor-pointer">
                <span>Feedback</span>
                <SmilePlus class="h-4 w-4 text-zinc-500" />
              </DropdownMenuItem>

              <div class="flex items-center justify-between px-2 py-1.5 text-sm">
                <span>Theme</span>
                <div class="flex items-center gap-1 border border-border rounded-full p-0.5">
                  <button @click="setThemePreference('system')" class="p-1 rounded-full transition-colors" :class="{ 'bg-zinc-100 dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 shadow-sm': themePreference === 'system', 'text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100': themePreference !== 'system' }">
                    <Monitor class="h-3.5 w-3.5" />
                  </button>
                  <button @click="setThemePreference('light')" class="p-1 rounded-full transition-colors" :class="{ 'bg-zinc-100 dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 shadow-sm': themePreference === 'light', 'text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100': themePreference !== 'light' }">
                    <Sun class="h-3.5 w-3.5" />
                  </button>
                  <button @click="setThemePreference('dark')" class="p-1 rounded-full transition-colors" :class="{ 'bg-zinc-100 dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 shadow-sm': themePreference === 'dark', 'text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100': themePreference !== 'dark' }">
                    <Moon class="h-3.5 w-3.5" />
                  </button>
                </div>
              </div>

              <DropdownMenuItem @click="router.push('/')" class="justify-between px-2 py-2 text-sm cursor-pointer">
                <span>Home Page</span>
                <Home class="h-4 w-4 text-zinc-500" />
              </DropdownMenuItem>

              <DropdownMenuItem @click="router.push('/changelog')" class="justify-between px-2 py-2 text-sm cursor-pointer">
                <span>Changelog</span>
                <Pencil class="h-4 w-4 text-zinc-500" />
              </DropdownMenuItem>

              <DropdownMenuItem @click="router.push('/help')" class="justify-between px-2 py-2 text-sm cursor-pointer">
                <span>Help</span>
                <LifeBuoy class="h-4 w-4 text-zinc-500" />
              </DropdownMenuItem>

              <DropdownMenuItem @click="router.push('/docs')" class="justify-between px-2 py-2 text-sm cursor-pointer">
                <span>Docs</span>
                <Book class="h-4 w-4 text-zinc-500" />
              </DropdownMenuItem>

              <DropdownMenuItem @click="auth.logout()" class="justify-between px-2 py-2 text-sm cursor-pointer text-zinc-900 dark:text-zinc-100">
                <span>Log Out</span>
                <LogOut class="h-4 w-4 text-zinc-500" />
              </DropdownMenuItem>
            </div>

            <!-- Upgrade Button -->
            <div class="px-3 pb-3">
              <Button class="w-full justify-center bg-zinc-900 text-white hover:bg-zinc-800 dark:bg-zinc-100 dark:text-zinc-900 dark:hover:bg-zinc-200 shadow-sm font-semibold h-9 mt-1">
                Upgrade to Pro
              </Button>
            </div>

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
      <header class="flex h-12 shrink-0 items-center justify-between bg-white dark:bg-zinc-950 border-b border-border px-4">
        <!-- Left: Toggle Sidebar -->
        <div class="flex items-center gap-4">
          <button
            @click="toggleSidebar"
            class="p-1.5 rounded-md hover:bg-zinc-100 dark:hover:bg-zinc-900 text-zinc-500 transition-colors lg:hidden"
            title="Toggle sidebar"
          >
            <PanelLeftOpen class="w-4 h-4" />
          </button>
        </div>
        <!-- Right: Language + Help -->
        <div class="flex items-center gap-3">
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
