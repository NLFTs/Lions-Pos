<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { storeToRefs } from 'pinia'
import AppLayout from '@/components/AppLayout.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import DashboardCard from '@/components/dashboard/Card.vue'
import ActivityTimeline from '@/components/dashboard/ActivityTimeline.vue'
import MobileSummaryTabs from '@/components/dashboard/MobileSummaryTabs.vue'
import AboutModal from '@/components/dashboard/AboutModal.vue'
import { useAuthStore } from '@/stores/auth'
import { usePermission } from '@/composables/usePermission'
import api from '@/lib/api'
import {
  Users,
  FileText,
  Boxes,
  ShieldCheck,
  KeyRound,
  Zap,
  Activity,
  UserPlus,
  Loader2,
  Clock,
  ArrowRight,
  Plus,
  Eye,
  Search,
  LayoutGrid,
  List,
  ChevronDown,
} from 'lucide-vue-next'

const auth = useAuthStore()
const { user } = storeToRefs(auth)
const { can } = usePermission()

const loading = ref(true)
const error = ref(null)
const lastRefresh = ref(null)

// Stats toolbar state
const searchQuery = ref('')
const statsViewMode = ref('grid') // 'grid' | 'list'

// About Modal state
const isAboutModalOpen = ref(false)

// Dropdown "Buat Baru"
const dropdownOpen = ref(false)
const dropdownRef = ref(null)
function handleOutsideClick(e) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    dropdownOpen.value = false
  }
}
onMounted(() => { fetchStats(); document.addEventListener('click', handleOutsideClick) })
onUnmounted(() => document.removeEventListener('click', handleOutsideClick))

// Stats from backend
const stats = ref({
  totalUsers: 0,
  totalPosts: 0,
  totalCategories: 0,
  totalRoles: 0,
  totalPermissions: 0,
  totalModules: 0,
  publishedPosts: 0,
  draftPosts: 0,
})

const recentUsers = ref([])
const recentPosts = ref([])
const recentActivities = ref([])

async function fetchStats() {
  loading.value = true
  error.value = null
  try {
    const [usersRes, postsRes, categoriesRes, rolesRes, permsRes, modulesRes] = await Promise.allSettled([
      api.get('/api/v1/users'),
      api.get('/api/v1/posts?page=0&size=5'),
      api.get('/api/v1/categories'),
      api.get('/api/v1/roles'),
      api.get('/api/v1/permissions'),
      api.get('/api/v1/modules'),
    ])

    if (usersRes.status === 'fulfilled') {
      const users = usersRes.value.data.data
      stats.value.totalUsers = Array.isArray(users) ? users.length : 0
      recentUsers.value = Array.isArray(users) ? users.slice(0, 5) : []
    }
    if (postsRes.status === 'fulfilled') {
      const postsData = postsRes.value.data.data
      stats.value.totalPosts = postsData?.totalElements ?? 0
      stats.value.publishedPosts = postsData?.content?.filter(p => p.status === 'published').length ?? 0
      stats.value.draftPosts = postsData?.content?.filter(p => p.status === 'draft').length ?? 0
      recentPosts.value = postsData?.content?.slice(0, 5) ?? []
    }
    if (categoriesRes.status === 'fulfilled') {
      const cats = categoriesRes.value.data.data
      stats.value.totalCategories = Array.isArray(cats) ? cats.length : 0
    }
    if (rolesRes.status === 'fulfilled') {
      const roles = rolesRes.value.data.data
      stats.value.totalRoles = Array.isArray(roles) ? roles.length : 0
    }
    if (permsRes.status === 'fulfilled') {
      const perms = permsRes.value.data.data
      stats.value.totalPermissions = Array.isArray(perms) ? perms.length : 0
    }
    if (modulesRes.status === 'fulfilled') {
      const mods = modulesRes.value.data.data
      stats.value.totalModules = Array.isArray(mods) ? mods.length : 0
    }

    // Generate recent activities
    generateRecentActivities()
    lastRefresh.value = new Date()
  } catch (err) {
    error.value = err.response?.data?.message || 'Gagal memuat dashboard.'
  } finally {
    loading.value = false
  }
}

function generateRecentActivities() {
  const activities = []

  // Recent users as activities
  recentUsers.value.slice(0, 3).forEach(u => {
    activities.push({
      title: `User baru: ${u.fullname || u.username}`,
      description: `@${u.username} bergabung`,
      icon: UserPlus,
      color: 'bg-blue-50 text-blue-600 dark:bg-blue-950/40 dark:text-blue-400',
      time: formatDate(u.createdAt),
      initials: (u.fullname || u.username).charAt(0).toUpperCase(),
    })
  })

  // Recent posts as activities
  recentPosts.value.slice(0, 2).forEach(p => {
    activities.push({
      title: `Post: ${p.title}`,
      description: `Status: ${p.status === 'published' ? 'Published' : 'Draft'}`,
      icon: FileText,
      color: 'bg-emerald-50 text-emerald-600 dark:bg-emerald-950/40 dark:text-emerald-400',
      time: formatDate(p.createdAt),
    })
  })

  recentActivities.value = activities.slice(0, 5)
}

// Stat cards — only show if user has permission
const statCards = computed(() => {
  const cards = []

  if (can('user.index')) {
    cards.push({
      label: 'Total User',
      value: stats.value.totalUsers,
      icon: Users,
      to: '/dashboard/users',
      color: 'text-blue-600',
      bg: 'bg-blue-50 dark:bg-blue-950/30',
      trend: '+3',
      description: `${recentUsers.value.length} terbaru ditampilkan`,
    })
  }
  if (can('produk.index')) {
    cards.push({
      label: 'Total Post',
      value: stats.value.totalPosts,
      icon: FileText,
      to: '/dashboard/posts',
      color: 'text-emerald-600',
      bg: 'bg-emerald-50 dark:bg-emerald-950/30',
      trend: `${stats.value.publishedPosts} published`,
      description: `${stats.value.draftPosts} draft menunggu`,
    })
  }
  if (can('category.index')) {
    cards.push({
      label: 'Kategori',
      value: stats.value.totalCategories,
      icon: Boxes,
      to: '/dashboard/categories',
      color: 'text-violet-600',
      bg: 'bg-violet-50 dark:bg-violet-950/30',
    })
  }
  if (can('role.index')) {
    cards.push({
      label: 'Role',
      value: stats.value.totalRoles,
      icon: ShieldCheck,
      to: '/dashboard/roles',
      color: 'text-amber-600',
      bg: 'bg-amber-50 dark:bg-amber-950/30',
    })
  }
  if (can('permission.index')) {
    cards.push({
      label: 'Permission',
      value: stats.value.totalPermissions,
      icon: KeyRound,
      to: '/dashboard/permissions',
      color: 'text-rose-600',
      bg: 'bg-rose-50 dark:bg-rose-950/30',
    })
  }
  if (can('module.index')) {
    cards.push({
      label: 'Modul',
      value: stats.value.totalModules,
      icon: Zap,
      to: '/dashboard/modules',
      color: 'text-cyan-600',
      bg: 'bg-cyan-50 dark:bg-cyan-950/30',
    })
  }

  return cards
})

// Hanya Buat Post yang tersisa
const canCreatePost = computed(() => can('produk.store'))

// Menu "Buat Baru" dropdown — tampilkan item sesuai permission
const createMenuItems = computed(() => {
  const items = []
  if (can('user.store'))       items.push({ label: 'User',       icon: Users,       to: '/dashboard/users' })
  if (can('produk.store'))       items.push({ label: 'Post',        icon: FileText,    to: '/dashboard/posts' })
  if (can('category.store'))   items.push({ label: 'Kategori',   icon: Boxes,       to: '/dashboard/categories' })
  if (can('role.store'))       items.push({ label: 'Role',        icon: ShieldCheck, to: '/dashboard/roles' })
  if (can('permission.store')) items.push({ label: 'Permission', icon: KeyRound,    to: '/dashboard/permissions' })
  if (can('module.store'))     items.push({ label: 'Modul',      icon: Zap,         to: '/dashboard/modules' })
  return items
})

// Filtered stat cards berdasarkan search
const filteredStatCards = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return statCards.value
  return statCards.value.filter(c => c.label.toLowerCase().includes(q))
})

function formatDate(dt) {
  if (!dt) return '-'
  const date = new Date(dt)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return 'Baru saja'
  if (minutes < 60) return `${minutes} menit lalu`
  if (hours < 24) return `${hours} jam lalu`
  if (days < 7) return `${days} hari lalu`
  return date.toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' })
}

function formatTime(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <AppLayout>
    <div class="space-y-6">
      <!-- Welcome Section -->
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Selamat datang, {{ user?.fullname || user?.username }}! 👋</h1>
          <div class="flex items-center gap-2 mt-1">
            <p class="text-sm text-muted-foreground">
              Berikut ringkasan data dan aktivitas aplikasi hari ini.
            </p>
            <span class="text-zinc-300 dark:text-zinc-700">•</span>
            <button 
              @click="isAboutModalOpen = true"
              class="text-xs font-semibold text-primary hover:underline transition-all"
            >
              Tentang Kami
            </button>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <span class="text-xs text-muted-foreground">
            <Clock class="w-3 h-3 inline mr-1" />
            Terakhir diperbarui: {{ lastRefresh ? formatTime(lastRefresh) : '-' }}
          </span>
          <button
            @click="fetchStats"
            :disabled="loading"
            class="inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium bg-primary text-primary-foreground hover:bg-primary/90 transition-colors disabled:opacity-50"
          >
            <Loader2 v-if="loading" class="w-3.5 h-3.5 animate-spin" />
            <Eye v-else class="w-3.5 h-3.5" />
            Refresh
          </button>
        </div>
      </div>

      <!-- ── Toolbar: Search | View Toggle | Buat Baru dropdown (kanan) ── -->
      <div class="flex items-center gap-2">

        <!-- Search bar (flex-1, mengisi ruang) -->
        <div class="relative flex-1">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Cari statistik & data..."
            class="w-full rounded-lg border border-border bg-card pl-9 pr-4 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary transition-all"
          />
        </div>

        <!-- View mode toggle -->
        <div class="flex items-center gap-1 rounded-lg border border-border bg-card p-1 shrink-0">
          <button
            @click="statsViewMode = 'grid'"
            class="rounded-md p-1.5 transition-colors"
            :class="statsViewMode === 'grid' ? 'bg-primary text-primary-foreground' : 'text-muted-foreground hover:text-foreground'"
            title="Tampilan kotak"
          >
            <LayoutGrid class="w-4 h-4" />
          </button>
          <button
            @click="statsViewMode = 'list'"
            class="rounded-md p-1.5 transition-colors"
            :class="statsViewMode === 'list' ? 'bg-primary text-primary-foreground' : 'text-muted-foreground hover:text-foreground'"
            title="Tampilan daftar"
          >
            <List class="w-4 h-4" />
          </button>
        </div>

        <!-- Dropdown Buat Baru (ujung kanan) -->
        <div class="relative shrink-0" ref="dropdownRef">
          <button
            @click.stop="dropdownOpen = !dropdownOpen"
            class="inline-flex items-center gap-2 rounded-lg px-3.5 py-2 text-sm font-medium bg-primary text-primary-foreground hover:bg-primary/90 transition-all shadow-sm hover:shadow"
          >
            <Plus class="w-4 h-4" />
            Buat Baru
            <ChevronDown class="w-4 h-4 transition-transform duration-200" :class="dropdownOpen ? 'rotate-180' : ''" />
          </button>

          <!-- Dropdown Menu -->
          <div
            v-if="dropdownOpen"
            class="absolute right-0 top-full mt-1.5 w-44 rounded-xl border border-border bg-card shadow-lg z-50 overflow-hidden py-1"
          >
            <RouterLink
              v-for="item in createMenuItems"
              :key="item.label"
              :to="item.to"
              @click="dropdownOpen = false"
              class="flex items-center gap-2.5 px-3.5 py-2.5 text-sm hover:bg-muted/60 transition-colors"
            >
              <component :is="item.icon" class="w-4 h-4 text-muted-foreground" />
              {{ item.label }}
            </RouterLink>
            <div v-if="createMenuItems.length === 0" class="px-4 py-3 text-xs text-muted-foreground">
              Tidak ada akses buat.
            </div>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex items-center justify-center py-20">
        <div class="flex flex-col items-center gap-3">
          <Loader2 class="w-8 h-8 animate-spin text-primary" />
          <p class="text-sm text-muted-foreground">Memuat data dashboard...</p>
        </div>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="rounded-xl border border-destructive/30 bg-destructive/5 px-5 py-4">
        <div class="flex items-start gap-3">
          <Activity class="w-5 h-5 text-destructive shrink-0 mt-0.5" />
          <div>
            <p class="text-sm font-medium text-destructive">{{ error }}</p>
            <p class="text-xs text-muted-foreground mt-1">Silakan coba refresh halaman.</p>
          </div>
        </div>
      </div>

      <template v-else>
        <!-- ===== MOBILE: Single card with tab switcher (informasi terbaru) ===== -->
        <!-- Sembunyikan saat ada pencarian aktif -->
        <MobileSummaryTabs
          v-if="!searchQuery"
          :recent-posts="recentPosts"
          :recent-users="recentUsers"
          :recent-activities="recentActivities"
          :format-date="formatDate"
        />

        <!-- ===== MOBILE ONLY: Stat cards (mengikuti view mode) ===== -->
        <div
          v-if="filteredStatCards.length > 0"
          class="lg:hidden"
          :class="statsViewMode === 'grid' ? 'grid grid-cols-2 gap-3' : 'flex flex-col gap-2'"
        >
          <StatCard
            v-for="card in filteredStatCards"
            :key="card.label"
            v-bind="card"
            :horizontal="statsViewMode === 'list'"
          />
        </div>
        <div v-else-if="searchQuery && filteredStatCards.length === 0" class="lg:hidden py-6 text-center text-sm text-muted-foreground">
          Tidak ada statistik yang cocok dengan "{{ searchQuery }}"
        </div>

        <!-- ===== DESKTOP (lg+): Full grid layout ===== -->

        <!-- Tampilan normal (tanpa pencarian): 3-kolom dengan recent cards di kiri -->
        <div v-if="!searchQuery" class="hidden lg:grid grid-cols-1 lg:grid-cols-3 gap-6">
          <!-- Left Column: Recent Items (1/3 width) -->
          <div class="space-y-6 lg:col-span-1">
            <!-- Post Terbaru -->
            <DashboardCard
              title="Post Terbaru"
              subtitle="5 post terakhir"
              :icon="FileText"
              action-label="Lihat Semua"
              action-to="/dashboard/posts"
            >
              <div v-if="recentPosts.length > 0" class="space-y-3">
                <RouterLink
                  v-for="post in recentPosts"
                  :key="post.id"
                  :to="`/dashboard/posts`"
                  class="flex items-center justify-between p-3 rounded-lg border border-border/40 hover:bg-muted/30 hover:border-primary/20 transition-all group"
                >
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium truncate group-hover:text-primary transition-colors">
                      {{ post.title }}
                    </p>
                    <div class="flex items-center gap-2 mt-1">
                      <span
                        class="inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold"
                        :class="post.status === 'published'
                          ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400'
                          : 'bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400'"
                      >
                        {{ post.status === 'published' ? 'Published' : 'Draft' }}
                      </span>
                      <span class="text-xs text-muted-foreground">{{ formatDate(post.createdAt) }}</span>
                    </div>
                  </div>
                  <ArrowRight class="w-4 h-4 text-muted-foreground group-hover:text-primary group-hover:translate-x-0.5 transition-all" />
                </RouterLink>
              </div>
              <div v-else class="py-8 text-center">
                <FileText class="w-8 h-8 mx-auto text-muted-foreground/40 mb-2" />
                <p class="text-sm text-muted-foreground">Belum ada post.</p>
              </div>
            </DashboardCard>

            <!-- User Terbaru -->
            <DashboardCard
              title="User Terbaru"
              subtitle="5 user terbaru"
              :icon="UserPlus"
              action-label="Lihat Semua"
              action-to="/dashboard/users"
            >
              <div v-if="recentUsers.length > 0" class="space-y-3">
                <div
                  v-for="u in recentUsers"
                  :key="u.id"
                  class="flex items-center gap-3 p-3 rounded-lg border border-border/40 hover:bg-muted/30 hover:border-primary/20 transition-all"
                >
                  <div class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-primary text-sm font-bold shrink-0">
                    {{ (u.fullname || u.username).charAt(0).toUpperCase() }}
                  </div>
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium truncate">{{ u.fullname || u.username }}</p>
                    <p class="text-xs text-muted-foreground">@{{ u.username }}</p>
                  </div>
                </div>
              </div>
              <div v-else class="py-8 text-center">
                <Users class="w-8 h-8 mx-auto text-muted-foreground/40 mb-2" />
                <p class="text-sm text-muted-foreground">Belum ada user.</p>
              </div>
            </DashboardCard>

            <!-- Aktivitas Terbaru -->
            <DashboardCard
              title="Aktivitas Terbaru"
              :icon="Activity"
            >
              <ActivityTimeline :items="recentActivities" />
            </DashboardCard>
          </div>

          <!-- Right Column: Stats (2/3 width) -->
          <div class="lg:col-span-2 space-y-4">
            <h2 class="text-lg font-semibold tracking-tight">Statistik &amp; Data</h2>

            <!-- Grid view -->
            <div
              v-if="filteredStatCards.length > 0 && statsViewMode === 'grid'"
              class="grid grid-cols-1 sm:grid-cols-2 gap-4"
            >
              <StatCard
                v-for="card in filteredStatCards"
                :key="card.label"
                v-bind="card"
              />
            </div>

            <!-- List view -->
            <div
              v-else-if="filteredStatCards.length > 0 && statsViewMode === 'list'"
              class="flex flex-col gap-2"
            >
              <StatCard
                v-for="card in filteredStatCards"
                :key="card.label"
                v-bind="card"
                :horizontal="true"
              />
            </div>

            <!-- Empty State - No Permissions -->
            <div v-else class="flex flex-col items-center justify-center py-20 text-center border rounded-xl border-dashed">
              <div class="w-16 h-16 rounded-full bg-muted flex items-center justify-center mb-4">
                <Activity class="w-8 h-8 text-muted-foreground/40" />
              </div>
              <h3 class="text-lg font-semibold mb-1">Tidak Ada Akses</h3>
              <p class="text-sm text-muted-foreground max-w-sm">
                Anda belum memiliki akses ke modul apapun. Hubungi administrator untuk meminta hak akses.
              </p>
            </div>
          </div>
        </div>

        <!-- Tampilan mode pencarian (desktop): recent cards disembunyikan, hasil pencarian full-width -->
        <div v-else class="hidden lg:block space-y-4">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold tracking-tight">Statistik &amp; Data</h2>
            <p class="text-sm text-muted-foreground">
              {{ filteredStatCards.length }} hasil untuk
              <span class="font-medium text-foreground">"{{ searchQuery }}"</span>
            </p>
          </div>

          <!-- Grid view -->
          <div
            v-if="filteredStatCards.length > 0 && statsViewMode === 'grid'"
            class="grid grid-cols-2 xl:grid-cols-3 gap-4"
          >
            <StatCard
              v-for="card in filteredStatCards"
              :key="card.label"
              v-bind="card"
            />
          </div>

          <!-- List view -->
          <div
            v-else-if="filteredStatCards.length > 0 && statsViewMode === 'list'"
            class="flex flex-col gap-2"
          >
            <StatCard
              v-for="card in filteredStatCards"
              :key="card.label"
              v-bind="card"
              :horizontal="true"
            />
          </div>

          <!-- Empty search result -->
          <div v-else class="flex flex-col items-center justify-center py-20 text-center border rounded-xl border-dashed">
            <div class="w-16 h-16 rounded-full bg-muted flex items-center justify-center mb-4">
              <Search class="w-8 h-8 text-muted-foreground/40" />
            </div>
            <h3 class="text-lg font-semibold mb-1">Tidak ditemukan</h3>
            <p class="text-sm text-muted-foreground max-w-sm">
              Tidak ada statistik yang cocok dengan "{{ searchQuery }}". Coba kata kunci lain.
            </p>
          </div>
        </div>
      </template>
    </div>

    <!-- About Modal -->
    <AboutModal 
      :is-open="isAboutModalOpen" 
      @close="isAboutModalOpen = false" 
    />
  </AppLayout>
</template>
