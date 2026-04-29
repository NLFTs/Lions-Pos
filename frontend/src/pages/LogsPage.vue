<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePermission } from '@/composables/usePermission'
import AppLayout from '@/components/AppLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Alert from '@/components/ui/Alert.vue'
import Badge from '@/components/ui/Badge.vue'
import Button from '@/components/ui/Button.vue'
import api from '@/lib/api'
import { Loader2, ChevronLeft, ChevronRight, Eye, Clock, Globe, User, LayoutGrid, ChevronDown } from 'lucide-vue-next'
import DataTableSearch from '@/components/ui/DataTableSearch.vue'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()

// ─── State ────────────────────────────────────────────────────────────────────
const logs = ref([])
const pagination = ref({ page: 0, size: 20, totalPages: 0, totalElements: 0 })
const loading = ref(false)
const error = ref(null)
const searchQuery = ref('')

const filteredLogs = computed(() => {
  if (!searchQuery.value) return logs.value
  const q = searchQuery.value.toLowerCase()
  return logs.value.filter(l => 
    (l.url || '').toLowerCase().includes(q) || 
    (l.userFullname || '').toLowerCase().includes(q) ||
    (l.method || '').toLowerCase().includes(q)
  )
})

// Detail modal
const showDetail = ref(false)
const detailLog = ref(null)

// ─── Fetch ────────────────────────────────────────────────────────────────────
async function fetchLogs(page = 0) {
  loading.value = true
  error.value = null
  try {
    const res = await api.get(`/api/v1/logs?page=${page}&size=${pagination.value.size}`)
    const data = res.data.data
    logs.value = data.content
    pagination.value = {
      page: data.number,
      size: data.size,
      totalPages: data.totalPages,
      totalElements: data.totalElements,
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load logs.'
  } finally {
    loading.value = false
  }
}

function updateLogsSize(newSize) {
  pagination.value.size = newSize
  fetchLogs(0)
}

onMounted(() => fetchLogs())

// ─── Detail ───────────────────────────────────────────────────────────────────
function viewDetail(log) {
  detailLog.value = log
  showDetail.value = true
}

// ─── Utils ────────────────────────────────────────────────────────────────────
function formatDuration(ms) {
  if (ms == null) return '-'
  if (ms < 1000) return `${ms}ms`
  return `${(ms / 1000).toFixed(2)}s`
}

function statusColor(status) {
  if (!status) return 'secondary'
  const code = parseInt(status)
  if (code >= 200 && code < 300) return 'default'
  if (code >= 300 && code < 400) return 'secondary'
  if (code >= 400 && code < 500) return 'destructive'
  return 'destructive'
}

function methodColor(method) {
  if (!method) return 'secondary'
  const m = method.toUpperCase()
  if (m === 'GET') return 'default'
  if (m === 'POST') return 'secondary'
  if (m === 'PUT') return 'secondary'
  if (m === 'DELETE') return 'destructive'
  return 'secondary'
}

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('id-ID', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}
</script>

<template>
  <AppLayout>
    <div class="pb-6">
      <!-- Page Header -->
      <div class="mb-6">
        <h1 class="text-xl font-bold tracking-tight text-zinc-900">Audit Logs</h1>
        <p class="text-xs text-zinc-500 mt-0.5">
          View system activities and audit logs.
        </p>
      </div>

      <!-- Table Controls -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5">
        <DataTableSearch
          v-model="searchQuery"
          placeholder="Search logs..."
          class="w-full max-w-sm"
          input-class="h-9 text-xs"
        />
        <div class="flex items-center gap-2 w-full sm:w-auto">
          <Button variant="outline" size="sm" class="flex-1 sm:flex-none flex items-center justify-center gap-2 border-zinc-200">
            <LayoutGrid class="h-3.5 w-3.5" />
            <span>Customize Columns</span>
            <ChevronDown class="h-3 w-3 text-zinc-400" />
          </Button>
        </div>
      </div>

      <Alert v-if="error" variant="destructive" class="mb-4">{{ error }}</Alert>

      <Card>
      <CardContent class="p-0">
        <div v-if="loading" class="flex items-center justify-center py-20">
          <Loader2 class="h-6 w-6 animate-spin text-muted-foreground" />
        </div>

        <div v-else-if="filteredLogs.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
          <Clock class="h-10 w-10 mb-3 opacity-40" />
          <p class="text-sm">No log entries found.</p>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b bg-muted/40">
                <th class="px-4 py-3 text-left font-medium text-muted-foreground w-12">#</th>
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">Method</th>
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">URL</th>
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">Status</th>
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">User</th>
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">Duration</th>
                <th class="px-4 py-3 text-left font-medium text-muted-foreground">Time</th>
                <th class="px-4 py-3 text-right font-medium text-muted-foreground">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="log in filteredLogs"
                :key="log.id"
                class="border-b last:border-0 hover:bg-muted/30 transition-colors"
              >
                <td class="px-4 py-3 text-muted-foreground">{{ log.id }}</td>
                <td class="px-4 py-3">
                  <Badge :variant="methodColor(log.method)" class="text-xs font-mono">{{ log.method || '-' }}</Badge>
                </td>
                <td class="px-4 py-3 font-mono text-xs max-w-[240px] truncate">{{ log.url || '-' }}</td>
                <td class="px-4 py-3">
                  <Badge :variant="statusColor(log.responseStatus)" class="text-xs">{{ log.responseStatus || '-' }}</Badge>
                </td>
                <td class="px-4 py-3 text-xs">
                  <span v-if="log.userFullname" class="flex items-center gap-1">
                    <User class="h-3 w-3" />
                    {{ log.userFullname }}
                  </span>
                  <span v-else class="text-muted-foreground">-</span>
                </td>
                <td class="px-4 py-3 text-xs">{{ formatDuration(log.durationMs) }}</td>
                <td class="px-4 py-3 text-xs text-muted-foreground whitespace-nowrap">{{ formatDate(log.requestAt) }}</td>
                <td class="px-4 py-3 text-right">
                  <Button v-if="can('log.show')" variant="ghost" size="icon" @click="viewDetail(log)">
                    <Eye class="h-4 w-4" />
                  </Button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <DataTablePagination
          v-if="pagination.totalElements > 0 && !loading"
          :page="pagination.page + 1"
          :page-size="pagination.size"
          :total="pagination.totalElements"
          @update:page="fetchLogs($event - 1)"
          @update:page-size="updateLogsSize($event)"
        />
      </CardContent>
    </Card>
    </div>

    <!-- ─── Detail Modal ─── -->
    <Teleport to="body">
      <div v-if="showDetail && detailLog" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="showDetail = false" />
        <div class="relative z-10 w-full max-w-2xl max-h-[90vh] overflow-y-auto rounded-lg bg-card shadow-xl border">
          <div class="flex items-center justify-between border-b px-6 py-4">
            <h3 class="font-semibold text-lg">Log Detail</h3>
            <Button variant="ghost" size="icon" @click="showDetail = false">
              <Eye class="h-4 w-4" />
            </Button>
          </div>

          <div class="px-6 py-4 space-y-4 text-sm">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <span class="text-muted-foreground">Method</span>
                <p class="font-mono font-medium">{{ detailLog.method || '-' }}</p>
              </div>
              <div>
                <span class="text-muted-foreground">Status</span>
                <p><Badge :variant="statusColor(detailLog.responseStatus)">{{ detailLog.responseStatus || '-' }}</Badge></p>
              </div>
            </div>

            <div>
              <span class="text-muted-foreground">URL</span>
              <p class="font-mono text-xs break-all">{{ detailLog.url || '-' }}</p>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <span class="text-muted-foreground">User</span>
                <p>{{ detailLog.userFullname || '-' }} (ID: {{ detailLog.userId || '-' }})</p>
              </div>
              <div>
                <span class="text-muted-foreground">Duration</span>
                <p>{{ formatDuration(detailLog.durationMs) }}</p>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <span class="text-muted-foreground">Request At</span>
                <p>{{ formatDate(detailLog.requestAt) }}</p>
              </div>
              <div>
                <span class="text-muted-foreground">Response At</span>
                <p>{{ formatDate(detailLog.responseAt) }}</p>
              </div>
            </div>

            <div>
              <span class="text-muted-foreground">User Agent</span>
              <p class="text-xs text-muted-foreground break-all">{{ detailLog.userAgent || '-' }}</p>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </AppLayout>
</template>
