<script setup>
import { ref, onMounted, computed } from 'vue'
import { storeToRefs } from 'pinia'
import AppLayout from '@/components/AppLayout.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import DashboardCard from '@/components/dashboard/Card.vue'
import ActivityTimeline from '@/components/dashboard/ActivityTimeline.vue'
import QuickActions from '@/components/dashboard/QuickActions.vue'
import MobileSummaryTabs from '@/components/dashboard/MobileSummaryTabs.vue'
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
  TrendingUp,
  TrendingDown,
  Clock,
  ArrowRight,
  Plus,
  Eye,
  Settings,
} from 'lucide-vue-next'

const auth = useAuthStore()
const { user } = storeToRefs(auth)
const { can } = usePermission()

const loading = ref(true)
const error = ref(null)
const lastRefresh = ref(null)

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
  if (can('post.index')) {
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

// Quick actions based on permissions
const quickActions = computed(() => {
  const actions = []
  if (can('post.store')) {
    actions.push({ label: 'Buat Post', icon: Plus, to: '/dashboard/posts', variant: 'default' })
  }
  if (can('user.store')) {
    actions.push({ label: 'Tambah User', icon: UserPlus, to: '/dashboard/users', variant: 'outline' })
  }
  if (can('role.store')) {
    actions.push({ label: 'Buat Role', icon: ShieldCheck, to: '/dashboard/roles', variant: 'outline' })
  }
  return actions
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

onMounted(fetchStats)
</script>

<template>
  <AppLayout>
    <div class="space-y-6">
      <!-- Welcome Section -->
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 class="text-2xl font-bold tracking-tight">Selamat datang, {{ user?.fullname || user?.username }}! 👋</h1>
          <p class="text-sm text-muted-foreground mt-1">
            Berikut ringkasan data dan aktivitas aplikasi hari ini.
          </p>
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

      <!-- Quick Actions -->
      <QuickActions v-if="quickActions.length > 0" :actions="quickActions" />

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
        <MobileSummaryTabs
          :recent-posts="recentPosts"
          :recent-users="recentUsers"
          :recent-activities="recentActivities"
          :format-date="formatDate"
        />

        <!-- ===== MOBILE ONLY: Stat cards grid (2 col) ===== -->
        <div v-if="statCards.length > 0" class="lg:hidden grid grid-cols-2 gap-3">
          <StatCard
            v-for="card in statCards"
            :key="card.label"
            v-bind="card"
          />
        </div>

        <!-- ===== DESKTOP (lg+): Full grid layout ===== -->
        <div class="hidden lg:grid grid-cols-1 lg:grid-cols-3 gap-6">
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
            <h2 class="text-lg font-semibold tracking-tight">Statistik & Data</h2>
            <div v-if="statCards.length > 0" class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <StatCard
                v-for="card in statCards"
                :key="card.label"
                v-bind="card"
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
      </template>
    </div>
  </AppLayout>
</template>
