<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useNotification } from '@/composables/useNotification'
import { useRouter } from 'vue-router'
import AppLayout from '@/components/AppLayout.vue'
import api from '@/lib/api'
import {
  Loader2,
  Search,
  RefreshCw,
  Trash2,
  Bookmark,
  Bell,
  Package,
  User,
  ClipboardList,
  ShoppingCart,
  CheckSquare,
  Square,
  InboxIcon,
  ArchiveIcon,
} from 'lucide-vue-next'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { notify } = useNotification()
const router = useRouter()

// ─── State ────────────────────────────────────────────────────────────────────
const notifications = ref([])
const activeTab = ref('inbox') // 'inbox' | 'draft'
const pagination = ref({ page: 0, size: 20, totalPages: 0, totalElements: 0 })
const loading = ref(false)
const actionLoading = ref(false)
const error = ref(null)
const searchQuery = ref('')
const selectedIds = ref([])

// ─── Computed ─────────────────────────────────────────────────────────────────
const filteredNotifications = computed(() => {
  if (!searchQuery.value) return notifications.value
  const q = searchQuery.value.toLowerCase()
  return notifications.value.filter(n =>
    (n.name || '').toLowerCase().includes(q) ||
    (n.description || '').toLowerCase().includes(q) ||
    (n.createdByName || '').toLowerCase().includes(q)
  )
})

const isAllSelected = computed(() => {
  const visible = filteredNotifications.value
  if (visible.length === 0) return false
  return visible.every(n => selectedIds.value.includes(n.id))
})

// ─── Watch Tab Change ─────────────────────────────────────────────────────────
watch(activeTab, () => {
  selectedIds.value = []
  searchQuery.value = ''
  fetchNotifications(0)
})

// ─── Fetch ────────────────────────────────────────────────────────────────────
async function fetchNotifications(page = 0) {
  loading.value = true
  error.value = null
  const isDraftParam = activeTab.value === 'draft'
  const size = pagination.value.size || 20  // fallback jika undefined
  try {
    const res = await api.get(`/api/v1/notifications?isDraft=${isDraftParam}&page=${page}&size=${size}`)
    const data = res.data.data
    notifications.value = data.content
    pagination.value = {
      page: data.number,
      size: data.size || 20,
      totalPages: data.totalPages,
      totalElements: data.totalElements,
    }
  } catch (err) {
    console.error('[Fetch] error:', err.response?.status, err.response?.data || err.message)
    error.value = err.response?.data?.message || 'Gagal memuat notifikasi.'
  } finally {
    loading.value = false
  }
}

function updateSize(newSize) {
  pagination.value.size = newSize || 20
  fetchNotifications(0)
}

onMounted(() => fetchNotifications(0))

// ─── Helpers ──────────────────────────────────────────────────────────────────
function formatRelativeTime(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHrs = Math.floor(diffMins / 60)
  const diffDays = Math.floor(diffHrs / 24)
  if (diffMins < 1) return 'Baru saja'
  if (diffMins < 60) return `${diffMins}m lalu`
  if (diffHrs < 24) return `${diffHrs}j lalu`
  if (diffDays === 1) return 'Kemarin'
  return `${diffDays}h lalu`
}

function getNotificationIcon(name) {
  if (!name) return Bell
  const lower = name.toLowerCase()
  if (lower.includes('product')) return Package
  if (lower.includes('user')) return User
  if (lower.includes('purchase order')) return ClipboardList
  if (lower.includes('order')) return ShoppingCart
  return Bell
}

function getIconColorClass(name) {
  if (!name) return 'text-zinc-400'
  const lower = name.toLowerCase()
  if (lower.includes('product')) return 'text-blue-500'
  if (lower.includes('user')) return 'text-indigo-500'
  if (lower.includes('purchase')) return 'text-amber-500'
  if (lower.includes('order')) return 'text-emerald-500'
  return 'text-zinc-400'
}

// ─── Route Mapping ────────────────────────────────────────────────────────────
const NOTIFICATION_ROUTES = {
  'product':        '/dashboard/products',
  'user':           '/dashboard/users',
  'purchase order': '/dashboard/purchase-orders',
  'order':          '/dashboard/orders',
  'supplier':       '/dashboard/suppliers',
  'warehouse':      '/dashboard/warehouses',
  'branch':         '/dashboard/branches',
  'stock':          '/dashboard/inventory',
  'transfer':       '/dashboard/transfer-requests',
  'voucher':        '/dashboard/vouchers',
  'partner':        '/dashboard/partners',
  'role':           '/dashboard/roles',
  'permission':     '/dashboard/permissions',
  'module':         '/dashboard/modules',
  'shift':          '/dashboard/shifts',
  'report':         '/dashboard/reports',
  'log':            '/dashboard/logs',
}

function getNotificationRoute(name) {
  if (!name) return null
  const lower = name.toLowerCase()
  for (const [key, path] of Object.entries(NOTIFICATION_ROUTES)) {
    if (lower.includes(key)) return path
  }
  return null
}

function handleRowClick(n) {
  const route = getNotificationRoute(n.name)
  if (route) router.push(route)
}

// ─── Selection ────────────────────────────────────────────────────────────────
function toggleSelectAll() {
  const visible = filteredNotifications.value
  if (isAllSelected.value) {
    const visibleIds = visible.map(n => n.id)
    selectedIds.value = selectedIds.value.filter(id => !visibleIds.includes(id))
  } else {
    const visibleIds = visible.map(n => n.id)
    const newSelected = [...selectedIds.value]
    visibleIds.forEach(id => { if (!newSelected.includes(id)) newSelected.push(id) })
    selectedIds.value = newSelected
  }
}

function toggleSelectRow(id) {
  if (selectedIds.value.includes(id)) {
    selectedIds.value = selectedIds.value.filter(item => item !== id)
  } else {
    selectedIds.value.push(id)
  }
}

// ─── Actions ──────────────────────────────────────────────────────────────────
async function markSingleAsDraft(id) {
  if (actionLoading.value) return
  actionLoading.value = true
  try {
    await api.post(`/api/v1/notifications/${id}/draft`)
    notifications.value = notifications.value.filter(n => n.id !== id)
    selectedIds.value = selectedIds.value.filter(sid => sid !== id)
    notify({ title: 'Dipindahkan ke Draft', message: 'Buka tab Draft untuk melihatnya.', type: 'success' })
    fetchNotifications(0)
  } catch (err) {
    const msg = err.response?.data?.message || err.message || 'Gagal menyimpan ke draft.'
    notify({ title: 'Gagal', message: msg, type: 'error' })
  } finally {
    actionLoading.value = false
  }
}

async function deleteSingle(id) {
  if (actionLoading.value) return
  actionLoading.value = true
  try {
    await api.delete(`/api/v1/notifications/${id}`)
    notifications.value = notifications.value.filter(n => n.id !== id)
    selectedIds.value = selectedIds.value.filter(sid => sid !== id)
    notify({ title: 'Dihapus', message: 'Notifikasi berhasil dihapus.', type: 'success' })
    fetchNotifications(0)
  } catch (err) {
    const msg = err.response?.data?.message || err.message || 'Gagal menghapus notifikasi.'
    notify({ title: 'Gagal', message: msg, type: 'error' })
  } finally {
    actionLoading.value = false
  }
}

async function bulkMarkAsDraft() {
  if (selectedIds.value.length === 0 || actionLoading.value) return
  const ids = [...selectedIds.value]
  actionLoading.value = true
  try {
    await api.post('/api/v1/notifications/bulk-draft', ids)
    notifications.value = notifications.value.filter(n => !ids.includes(n.id))
    selectedIds.value = []
    notify({ title: 'Dipindahkan ke Draft', message: `${ids.length} notifikasi dipindahkan. Buka tab Draft.`, type: 'success' })
    fetchNotifications(0)
  } catch (err) {
    const msg = err.response?.data?.message || err.message || 'Gagal memproses bulk draft.'
    notify({ title: 'Gagal', message: msg, type: 'error' })
  } finally {
    actionLoading.value = false
  }
}

async function bulkDelete() {
  if (selectedIds.value.length === 0 || actionLoading.value) return
  const ids = [...selectedIds.value]
  actionLoading.value = true
  try {
    await api.post('/api/v1/notifications/bulk-delete', ids)
    console.log('[BulkDelete] sukses', ids)
    notifications.value = notifications.value.filter(n => !ids.includes(n.id))
    selectedIds.value = []
    notify({ title: 'Dihapus', message: `${ids.length} notifikasi berhasil dihapus.`, type: 'success' })
    fetchNotifications(0)
  } catch (err) {
    const msg = err.response?.data?.message || err.message || 'Gagal menghapus notifikasi.'
    notify({ title: 'Gagal', message: msg, type: 'error' })
  } finally {
    actionLoading.value = false
  }
}
</script>

<template>
  <AppLayout>
    <div class="h-[calc(100vh-3rem)] flex flex-col -m-5 bg-zinc-50 dark:bg-[#060606]">

      <!-- ── Header Bar ── -->
      <div class="shrink-0 flex items-center justify-between px-5 py-3 bg-white dark:bg-[#0a0a0a] border-b border-zinc-200 dark:border-zinc-800 gap-4">

        <!-- Tabs -->
        <div class="flex items-center gap-1 p-0.5 bg-zinc-100 dark:bg-zinc-900 rounded-lg border border-zinc-200 dark:border-zinc-800">
          <button
            @click="activeTab = 'inbox'"
            class="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-semibold transition-all"
            :class="activeTab === 'inbox'
              ? 'bg-white dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 shadow-sm'
              : 'text-zinc-500 hover:text-zinc-700 dark:hover:text-zinc-300'"
          >
            <InboxIcon class="w-3.5 h-3.5" />
            Kotak Masuk
          </button>
          <button
            @click="activeTab = 'draft'"
            class="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-semibold transition-all"
            :class="activeTab === 'draft'
              ? 'bg-white dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 shadow-sm'
              : 'text-zinc-500 hover:text-zinc-700 dark:hover:text-zinc-300'"
          >
            <ArchiveIcon class="w-3.5 h-3.5" />
            Draft
          </button>
        </div>

        <!-- Search -->
        <div class="flex items-center gap-2 flex-1 max-w-sm bg-zinc-100 dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-lg px-3 py-1.5 focus-within:ring-1 focus-within:ring-zinc-300 dark:focus-within:ring-zinc-700">
          <Search class="h-3.5 w-3.5 text-zinc-400 shrink-0" />
          <input
            v-model="searchQuery"
            placeholder="Cari notifikasi..."
            class="bg-transparent border-none outline-none text-[13px] w-full text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-400 focus:ring-0"
          />
        </div>

        <!-- Right Actions -->
        <div class="flex items-center gap-2 shrink-0">
          <button
            @click="fetchNotifications(0)"
            :disabled="loading"
            class="flex items-center gap-1.5 h-8 px-3 rounded-lg text-xs font-medium bg-zinc-100 dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 text-zinc-600 dark:text-zinc-400 hover:bg-zinc-200 dark:hover:bg-zinc-800 transition-colors disabled:opacity-50"
          >
            <RefreshCw class="h-3.5 w-3.5" :class="{ 'animate-spin': loading }" />
            Refresh
          </button>
        </div>
      </div>

      <!-- ── Bulk Actions Bar (visible when items selected) ── -->
      <Transition
        enter-active-class="transition duration-150 ease-out"
        enter-from-class="-translate-y-1 opacity-0"
        enter-to-class="translate-y-0 opacity-100"
        leave-active-class="transition duration-100 ease-in"
        leave-from-class="translate-y-0 opacity-100"
        leave-to-class="-translate-y-1 opacity-0"
      >
        <div
          v-if="selectedIds.length > 0"
          class="shrink-0 flex items-center justify-between px-5 py-2 bg-zinc-50 dark:bg-zinc-900/60 border-b border-zinc-200 dark:border-zinc-800"
        >
          <span class="text-xs text-zinc-500 dark:text-zinc-400 font-medium">
            <span class="font-bold text-zinc-800 dark:text-zinc-200">{{ selectedIds.length }}</span> dipilih
          </span>
          <div class="flex items-center gap-2">
            <button
              v-if="activeTab === 'inbox'"
              @click="bulkMarkAsDraft"
              :disabled="actionLoading"
              class="flex items-center gap-1.5 h-7 px-3 rounded-md text-xs font-medium border border-zinc-300 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors disabled:opacity-50"
            >
              <Bookmark class="h-3 w-3" />
              Simpan ke Draft
            </button>
            <button
              @click="bulkDelete"
              :disabled="actionLoading"
              class="flex items-center gap-1.5 h-7 px-3 rounded-md text-xs font-medium bg-red-500 hover:bg-red-600 text-white transition-colors disabled:opacity-50"
            >
              <Trash2 class="h-3 w-3" />
              Hapus
            </button>
          </div>
        </div>
      </Transition>

      <!-- ── Table Area ── -->
      <div class="flex-1 overflow-auto">

        <!-- Loading skeleton -->
        <div v-if="loading && notifications.length === 0" class="flex items-center justify-center py-20">
          <Loader2 class="h-6 w-6 animate-spin text-zinc-400" />
          <span class="ml-2 text-sm text-zinc-400">Memuat...</span>
        </div>

        <!-- Empty state -->
        <div v-else-if="filteredNotifications.length === 0" class="flex flex-col items-center justify-center py-24 text-center">
          <div class="w-12 h-12 rounded-full bg-zinc-100 dark:bg-zinc-900 flex items-center justify-center mb-3">
            <Bell class="w-5 h-5 text-zinc-400" />
          </div>
          <p class="text-sm font-medium text-zinc-500 dark:text-zinc-400">Tidak ada notifikasi</p>
          <p class="text-xs text-zinc-400 dark:text-zinc-600 mt-1">
            {{ activeTab === 'draft' ? 'Belum ada notifikasi yang disimpan ke draft.' : 'Notifikasi baru akan muncul di sini.' }}
          </p>
        </div>

        <!-- Table -->
        <table v-else class="w-full text-sm">
          <thead>
            <tr class="border-b border-zinc-200 dark:border-zinc-800 bg-white dark:bg-[#0a0a0a]">
              <!-- Checkbox col -->
              <th class="w-10 px-4 py-2.5">
                <button @click="toggleSelectAll" class="text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 transition-colors">
                  <component :is="isAllSelected ? CheckSquare : Square" class="w-4 h-4" />
                </button>
              </th>
              <!-- Type -->
              <th class="w-8 px-2 py-2.5"></th>
              <!-- Name / Desc -->
              <th class="px-3 py-2.5 text-left text-[11px] font-semibold text-zinc-400 dark:text-zinc-500 uppercase tracking-wider">
                Notifikasi
              </th>
              <!-- Creator -->
              <th class="w-36 px-3 py-2.5 text-left text-[11px] font-semibold text-zinc-400 dark:text-zinc-500 uppercase tracking-wider hidden md:table-cell">
                Dari
              </th>
              <!-- Time -->
              <th class="w-28 px-3 py-2.5 text-right text-[11px] font-semibold text-zinc-400 dark:text-zinc-500 uppercase tracking-wider hidden sm:table-cell">
                Waktu
              </th>
              <!-- Actions -->
              <th class="w-20 px-3 py-2.5"></th>
            </tr>
          </thead>

          <tbody class="divide-y divide-zinc-100 dark:divide-zinc-800/80 bg-white dark:bg-[#0a0a0a]">
            <tr
              v-for="n in filteredNotifications"
              :key="n.id"
              @click="handleRowClick(n)"
              class="group transition-colors hover:bg-zinc-50 dark:hover:bg-zinc-900/40"
              :class="[
                { 'bg-zinc-50/50 dark:bg-zinc-900/20': selectedIds.includes(n.id) },
                getNotificationRoute(n.name) ? 'cursor-pointer' : 'cursor-default'
              ]"
            >
              <!-- Checkbox — stop propagation agar tidak trigger navigasi -->
              <td class="px-4 py-3" @click.stop>
                <button @click="toggleSelectRow(n.id)" class="text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 transition-colors">
                  <component
                    :is="selectedIds.includes(n.id) ? CheckSquare : Square"
                    class="w-4 h-4"
                    :class="selectedIds.includes(n.id) ? 'text-zinc-800 dark:text-zinc-100' : ''"
                  />
                </button>
              </td>

              <!-- Icon -->
              <td class="px-2 py-3">
                <component :is="getNotificationIcon(n.name)" :class="['w-4 h-4', getIconColorClass(n.name)]" />
              </td>

              <!-- Content -->
              <td class="px-3 py-3 min-w-0">
                <div class="flex items-center gap-2 mb-0.5">
                  <span class="text-[11px] font-mono text-zinc-400 dark:text-zinc-600">#{{ n.id }}</span>
                  <span
                    class="text-[13px] font-semibold text-zinc-800 dark:text-zinc-200 transition-colors"
                    :class="getNotificationRoute(n.name) ? 'group-hover:text-primary' : ''"
                  >{{ n.name }}</span>
                  <span v-if="!n.isSeen && activeTab === 'inbox'" class="w-1.5 h-1.5 rounded-full bg-blue-500 shrink-0"></span>
                </div>
                <p class="text-[12px] text-zinc-500 dark:text-zinc-400 truncate max-w-lg">{{ n.description }}</p>
              </td>

              <!-- Creator -->
              <td class="px-3 py-3 hidden md:table-cell">
                <span class="text-[12px] text-zinc-500 dark:text-zinc-400">{{ n.createdByName || 'System' }}</span>
              </td>

              <!-- Time -->
              <td class="px-3 py-3 text-right hidden sm:table-cell">
                <span class="text-[11px] text-zinc-400 dark:text-zinc-600 whitespace-nowrap">{{ formatRelativeTime(n.createdAt) }}</span>
              </td>

              <!-- Row Actions — stop propagation agar tidak trigger navigasi -->
              <td class="px-3 py-3" @click.stop>
                <div class="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                  <!-- Draft button — hanya tampil di inbox -->
                  <button
                    v-if="activeTab === 'inbox'"
                    @click="markSingleAsDraft(n.id)"
                    :disabled="actionLoading"
                    title="Simpan ke Draft"
                    class="p-1.5 rounded-md text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors disabled:opacity-40"
                  >
                    <Bookmark class="w-3.5 h-3.5" />
                  </button>
                  <!-- Delete button -->
                  <button
                    @click="deleteSingle(n.id)"
                    :disabled="actionLoading"
                    title="Hapus"
                    class="p-1.5 rounded-md text-zinc-400 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-950/20 transition-colors disabled:opacity-40"
                  >
                    <Trash2 class="w-3.5 h-3.5" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- ── Footer Pagination ── -->
      <div class="shrink-0 flex items-center justify-between px-5 py-2.5 border-t border-zinc-200 dark:border-zinc-800 bg-white dark:bg-[#0a0a0a]">
        <span class="text-[11px] text-zinc-400 dark:text-zinc-600 font-mono">
          {{ pagination.totalElements }} notifikasi
        </span>
        <DataTablePagination
          v-if="pagination.totalElements > 0 && !loading"
          :page="pagination.page + 1"
          :page-size="pagination.size"
          :total="pagination.totalElements"
          @update:page="fetchNotifications($event - 1)"
          @update:page-size="updateSize($event)"
        />
      </div>

    </div>
  </AppLayout>
</template>
