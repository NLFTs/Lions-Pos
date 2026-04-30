<script setup>
import { ref, computed } from 'vue'
import { RouterLink } from 'vue-router'
import { FileText, Users, Activity, UserPlus, ArrowRight, ChevronRight } from 'lucide-vue-next'

const props = defineProps({
  recentPosts: { type: Array, default: () => [] },
  recentUsers: { type: Array, default: () => [] },
  recentActivities: { type: Array, default: () => [] },
  formatDate: { type: Function, required: true },
})

const tabs = [
  { key: 'posts',      label: 'Post Terbaru',  icon: FileText,  actionTo: '/dashboard/posts',  actionLabel: 'Lihat Semua' },
  { key: 'users',      label: 'User Terbaru',   icon: UserPlus,  actionTo: '/dashboard/users',  actionLabel: 'Lihat Semua' },
  { key: 'activities', label: 'Aktivitas',       icon: Activity,  actionTo: null,                actionLabel: '' },
]

const activeTab = ref('posts')
const activeTabData = computed(() => tabs.find(t => t.key === activeTab.value))
</script>

<template>
  <!-- Mobile Only -->
  <div class="lg:hidden rounded-xl border border-border/60 bg-card shadow-sm overflow-hidden">

    <!-- ── Tab Strip: flex-1 agar tiap tab rata mengisi lebar penuh ── -->
    <div class="flex border-b border-border">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        @click="activeTab = tab.key"
        class="flex-1 flex items-center justify-center gap-1.5 py-3 text-xs font-medium border-b-2 transition-colors duration-150"
        :class="activeTab === tab.key
          ? 'border-primary text-primary bg-primary/5'
          : 'border-transparent text-muted-foreground hover:text-foreground hover:bg-muted/20'"
      >
        <component :is="tab.icon" class="w-3.5 h-3.5 shrink-0" />
        <span class="truncate">{{ tab.label }}</span>
      </button>
    </div>

    <!-- ── Sub-header: jumlah + Lihat Semua ── -->
    <div
      v-if="activeTabData?.actionTo"
      class="flex items-center justify-between px-4 py-2 bg-muted/20 border-b border-border/40"
    >
      <span class="text-xs text-muted-foreground">
        {{ activeTab === 'posts'
            ? `${recentPosts.length} post ditampilkan`
            : activeTab === 'users'
            ? `${recentUsers.length} user ditampilkan`
            : '' }}
      </span>
      <RouterLink
        :to="activeTabData.actionTo"
        class="inline-flex items-center gap-1 text-xs font-medium text-primary hover:text-primary/80 transition-colors"
      >
        {{ activeTabData.actionLabel }}
        <ChevronRight class="w-3.5 h-3.5" />
      </RouterLink>
    </div>

    <!-- ── Tab Panels ──
         Semua panel dirender sekaligus (tidak unmount) sehingga height tidak collapse.
         Hanya CSS opacity + display yang berubah → tidak ada geter. -->
    <div class="p-4">

      <!-- Post Terbaru -->
      <div :class="['tab-panel', { 'tab-panel--active': activeTab === 'posts' }]">
        <div v-if="recentPosts.length > 0" class="space-y-2">
          <RouterLink
            v-for="post in recentPosts"
            :key="post.id"
            to="/dashboard/posts"
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
            <ArrowRight class="w-4 h-4 text-muted-foreground group-hover:text-primary group-hover:translate-x-0.5 transition-all shrink-0 ml-2" />
          </RouterLink>
        </div>
        <div v-else class="py-10 text-center">
          <FileText class="w-8 h-8 mx-auto text-muted-foreground/40 mb-2" />
          <p class="text-sm text-muted-foreground">Belum ada post.</p>
        </div>
      </div>

      <!-- User Terbaru -->
      <div :class="['tab-panel', { 'tab-panel--active': activeTab === 'users' }]">
        <div v-if="recentUsers.length > 0" class="space-y-2">
          <div
            v-for="u in recentUsers"
            :key="u.id"
            class="flex items-center gap-3 p-3 rounded-lg border border-border/40 hover:bg-muted/30 hover:border-primary/20 transition-all"
          >
            <div class="w-9 h-9 rounded-full bg-primary/10 flex items-center justify-center text-primary text-sm font-bold shrink-0">
              {{ (u.fullname || u.username).charAt(0).toUpperCase() }}
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium truncate">{{ u.fullname || u.username }}</p>
              <p class="text-xs text-muted-foreground">@{{ u.username }}</p>
            </div>
          </div>
        </div>
        <div v-else class="py-10 text-center">
          <Users class="w-8 h-8 mx-auto text-muted-foreground/40 mb-2" />
          <p class="text-sm text-muted-foreground">Belum ada user.</p>
        </div>
      </div>

      <!-- Aktivitas Terbaru -->
      <div :class="['tab-panel', { 'tab-panel--active': activeTab === 'activities' }]">
        <div v-if="recentActivities.length > 0" class="space-y-3">
          <div
            v-for="(item, i) in recentActivities"
            :key="i"
            class="flex items-start gap-3"
          >
            <div
              class="w-8 h-8 rounded-full flex items-center justify-center shrink-0 mt-0.5"
              :class="item.color"
            >
              <component :is="item.icon" class="w-4 h-4" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium truncate">{{ item.title }}</p>
              <p class="text-xs text-muted-foreground">{{ item.description }}</p>
            </div>
            <span class="text-[11px] text-muted-foreground shrink-0 mt-0.5">{{ item.time }}</span>
          </div>
        </div>
        <div v-else class="py-10 text-center">
          <Activity class="w-8 h-8 mx-auto text-muted-foreground/40 mb-2" />
          <p class="text-sm text-muted-foreground">Belum ada aktivitas.</p>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
/*
  Semua panel tetap di DOM (tidak di-unmount) → tidak ada height collapse → tidak geter.
  Hanya opacity yang dianimasikan untuk efek smooth.
*/
.tab-panel {
  display: none;
  opacity: 0;
  transition: opacity 0.18s ease;
}

.tab-panel--active {
  display: block;
  opacity: 1;
}
</style>
