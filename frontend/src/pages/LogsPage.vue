<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePermission } from '@/composables/usePermission'
import { useNotification } from '@/composables/useNotification'
import AppLayout from '@/components/AppLayout.vue'
import Badge from '@/components/ui/Badge.vue'
import Button from '@/components/ui/Button.vue'
import api from '@/lib/api'
import { Loader2, Eye, Search, PlayCircle, RefreshCw, Download, FileText, Database, X, ChevronUp, ChevronDown, Copy, Check } from 'lucide-vue-next'
import DataTablePagination from '@/components/ui/DataTablePagination.vue'

const { can } = usePermission()
const { notify } = useNotification()

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

const activeIndex = computed(() => {
  if (!detailLog.value) return -1
  return filteredLogs.value.findIndex(l => l.id === detailLog.value.id)
})

function nextLog() {
  if (activeIndex.value >= 0 && activeIndex.value < filteredLogs.value.length - 1) {
    detailLog.value = filteredLogs.value[activeIndex.value + 1]
  }
}

function prevLog() {
  if (activeIndex.value > 0) {
    detailLog.value = filteredLogs.value[activeIndex.value - 1]
  }
}

const isCopied = ref(false)
async function copyLog() {
  if (!detailLog.value) return
  const textToCopy = JSON.stringify(detailLog.value, null, 2)
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(textToCopy)
    } else {
      const textArea = document.createElement('textarea')
      textArea.value = textToCopy
      textArea.style.position = 'fixed'
      textArea.style.left = '-999999px'
      textArea.style.top = '-999999px'
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      try {
        document.execCommand('copy')
      } finally {
        textArea.remove()
      }
    }
    
    isCopied.value = true
    notify({
      title: 'Log copied',
      message: 'Audit log details copied to clipboard.',
      type: 'success',
      duration: 3000
    })
    setTimeout(() => isCopied.value = false, 2000)
  } catch (err) {
    console.error('Failed to copy log', err)
    notify({
      title: 'Copy failed',
      message: 'Could not copy log. Clipboard access denied.',
      type: 'error',
      duration: 3000
    })
  }
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

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('id-ID', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

function formatVercelDateTop(dt) {
  if (!dt) return '-'
  const d = new Date(dt)
  const month = d.toLocaleString('en-US', { month: 'short' }).toUpperCase()
  const day = d.getDate().toString().padStart(2, '0')
  return `${month} ${day}`
}

function formatVercelDateBottom(dt) {
  if (!dt) return '-'
  const d = new Date(dt)
  const time = d.toLocaleString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' })
  const ms = d.getMilliseconds().toString().padStart(2, '0').slice(0, 2)
  return `${time}.${ms} GMT+7`
}

function formatVercelDate(dt) {
  if (!dt) return '-'
  const d = new Date(dt)
  const month = d.toLocaleString('en-US', { month: 'short' }).toUpperCase()
  const day = d.getDate().toString().padStart(2, '0')
  const time = d.toLocaleString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' })
  const ms = d.getMilliseconds().toString().padStart(2, '0').slice(0, 2)
  return `${month} ${day} ${time}.${ms}`
}

function getStatusTextColor(status) {
  if (!status) return 'text-zinc-500'
  const code = parseInt(status)
  if (code >= 200 && code < 300) return 'text-emerald-600 dark:text-emerald-400'
  if (code >= 300 && code < 400) return 'text-blue-600 dark:text-blue-400'
  if (code >= 400 && code < 500) return 'text-amber-600 dark:text-amber-500'
  return 'text-red-600 dark:text-red-500'
}
</script>

<template>
  <AppLayout>
    <div class="h-[calc(100vh-3rem)] flex flex-col font-sans -m-5 bg-white dark:bg-[#0a0a0a]">
      <!-- Top Bar (Filters & Actions) -->
      <div class="flex items-center justify-between p-3 lg:px-6 border-b border-zinc-200 dark:border-zinc-800 shrink-0">
        <!-- Search Input -->
        <div class="flex items-center gap-2 w-full max-w-2xl bg-zinc-100 dark:bg-[#111111] border border-zinc-200 dark:border-zinc-800 rounded-md px-3 py-1.5 focus-within:ring-1 focus-within:ring-zinc-300 dark:focus-within:ring-zinc-700 transition-shadow">
          <Search class="h-4 w-4 text-zinc-500 shrink-0" />
          <input
            v-model="searchQuery"
            placeholder="method:GET"
            class="bg-transparent border-none outline-none text-[13px] w-full font-mono text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-500 focus:ring-0 px-1"
          />
        </div>
        
        <!-- Actions -->
        <div class="flex items-center gap-2 ml-4 shrink-0">
          <Button variant="outline" size="sm" class="h-8 gap-2 bg-zinc-100 dark:bg-[#111111] border-zinc-200 dark:border-zinc-800 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-200 dark:hover:bg-[#222]">
            <PlayCircle class="h-3.5 w-3.5" />
            <span class="hidden sm:inline">Live</span>
          </Button>
          <Button variant="outline" size="icon" class="h-8 w-8 bg-zinc-100 dark:bg-[#111111] border-zinc-200 dark:border-zinc-800 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-200 dark:hover:bg-[#222]" @click="fetchLogs(0)">
            <RefreshCw class="h-3.5 w-3.5" :class="{ 'animate-spin': loading }" />
          </Button>
          <Button variant="outline" size="icon" class="h-8 w-8 bg-zinc-100 dark:bg-[#111111] border-zinc-200 dark:border-zinc-800 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-200 dark:hover:bg-[#222]">
            <Download class="h-3.5 w-3.5" />
          </Button>
        </div>
      </div>

      <!-- Logs Table Area -->
      <div class="flex-1 overflow-x-auto overflow-y-auto custom-scrollbar relative">
        <table class="w-full text-left border-collapse min-w-[800px]">
          <thead class="sticky top-0 z-10 bg-white/95 dark:bg-[#0a0a0a]/95 backdrop-blur-sm">
            <tr class="text-[11px] font-semibold text-zinc-500 dark:text-zinc-400 border-b border-zinc-200 dark:border-zinc-800">
              <th class="px-6 py-2.5 font-medium w-[180px]">Time</th>
              <th class="px-4 py-2.5 font-medium w-[120px]">Status</th>
              <th class="px-4 py-2.5 font-medium w-[180px]">Host</th>
              <th class="px-4 py-2.5 font-medium">Request</th>
              <th class="px-4 py-2.5 font-medium">Messages</th>
            </tr>
          </thead>
          <tbody class="font-mono text-[14px]">
            <tr v-if="loading && logs.length === 0" class="border-b border-zinc-100 dark:border-zinc-800/50">
               <td colspan="5" class="py-12 text-center"><Loader2 class="h-5 w-5 animate-spin text-zinc-500 mx-auto" /></td>
            </tr>
            <tr v-else-if="filteredLogs.length === 0" class="border-b border-zinc-100 dark:border-zinc-800/50">
               <td colspan="5" class="py-12 text-center text-zinc-500 font-sans text-sm">No logs found matching your criteria.</td>
            </tr>
            <tr
              v-else
              v-for="log in filteredLogs"
              :key="log.id"
              class="border-b border-zinc-100 dark:border-zinc-800/50 odd:bg-white dark:odd:bg-[#0a0a0a] even:bg-zinc-50/80 dark:even:bg-[#111111] hover:!bg-zinc-100 dark:hover:!bg-[#1a1a1a] transition-colors cursor-pointer"
              @click="viewDetail(log)"
            >
              <td class="px-6 py-3 text-zinc-500 dark:text-zinc-400 whitespace-nowrap">{{ formatVercelDate(log.requestAt) }}</td>
              <td class="px-4 py-3 whitespace-nowrap font-medium tracking-tight">
                <span class="text-zinc-500 dark:text-zinc-400 mr-2.5">{{ log.method || '-' }}</span>
                <span :class="getStatusTextColor(log.responseStatus)">{{ log.responseStatus || '-' }}</span>
              </td>
              <td class="px-4 py-3 text-zinc-700 dark:text-zinc-300 truncate max-w-[180px]">site-pribadi-2yk...</td>
              <td class="px-4 py-3 text-zinc-700 dark:text-zinc-300">
                <div class="flex items-center gap-2 overflow-hidden">
                  <div class="flex items-center justify-center border border-zinc-200 dark:border-zinc-700 rounded-[3px] bg-white dark:bg-zinc-900 p-0.5 shrink-0">
                    <Database class="h-3.5 w-3.5 text-zinc-500 dark:text-zinc-400" />
                  </div>
                  <span class="truncate">{{ log.url || '-' }}</span>
                </div>
              </td>
              <td class="px-4 py-3 text-zinc-500 dark:text-zinc-500 truncate text-[13px] font-sans">-</td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Bottom Pagination Bar -->
      <div class="shrink-0 p-2 flex items-center justify-between border-t border-zinc-200 dark:border-zinc-800 bg-white dark:bg-[#0a0a0a]">
        <div class="text-[11px] text-zinc-500 px-4 font-mono font-medium">
           Total: {{ pagination.totalElements }}
        </div>
        <DataTablePagination
          v-if="pagination.totalElements > 0 && !loading"
          :page="pagination.page + 1"
          :page-size="pagination.size"
          :total="pagination.totalElements"
          @update:page="fetchLogs($event - 1)"
          @update:page-size="updateLogsSize($event)"
        />
      </div>
    </div>
    
    <!-- ─── Detail Modal ─── -->
    <Teleport to="body">
      <div v-if="showDetail && detailLog" class="fixed inset-0 z-50 sm:pointer-events-none">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm sm:pointer-events-auto" @click="showDetail = false" />
        <div class="absolute bottom-0 left-0 right-0 sm:left-auto sm:bottom-6 sm:right-6 w-full sm:max-w-[480px] max-h-[85vh] flex flex-col rounded-t-xl sm:rounded-xl bg-white dark:bg-[#0a0a0a] shadow-[0_-8px_30px_rgba(0,0,0,0.12)] sm:shadow-[0_8px_30px_rgba(0,0,0,0.12)] dark:shadow-[0_-8px_40px_rgba(0,0,0,0.5)] sm:dark:shadow-[0_8px_40px_rgba(0,0,0,0.5)] border-t sm:border-y sm:border-x border-zinc-200 dark:border-zinc-800 animate-in slide-in-from-bottom-full sm:slide-in-from-bottom-8 duration-200 sm:pointer-events-auto">
        
        <!-- Header -->
        <div class="flex items-center justify-between px-4 py-3 border-b border-zinc-200 dark:border-zinc-800 bg-white dark:bg-[#0a0a0a] rounded-t-xl shrink-0 gap-2">
          <div class="flex items-center gap-2 min-w-0 flex-1">
             <span class="font-mono text-zinc-900 dark:text-zinc-100 font-bold border border-zinc-200 dark:border-zinc-800 px-1.5 py-0.5 rounded text-xs bg-zinc-100 dark:bg-[#111] shrink-0">{{ detailLog.method || '-' }}</span>
             <span class="font-mono text-zinc-900 dark:text-zinc-100 text-sm truncate min-w-0">{{ detailLog.url || '-' }}</span>
             <span class="font-mono text-xs border border-zinc-200 dark:border-zinc-800 px-1.5 py-0.5 rounded bg-zinc-50 dark:bg-emerald-950/20 shrink-0" :class="getStatusTextColor(detailLog.responseStatus)">{{ detailLog.responseStatus || '-' }}</span>
          </div>
          <div class="flex items-center gap-1 shrink-0">
             <Button variant="ghost" size="icon" class="h-7 w-7 text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100" @click="prevLog" :disabled="activeIndex <= 0">
               <ChevronUp class="h-4 w-4" />
             </Button>
             <Button variant="ghost" size="icon" class="h-7 w-7 text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100" @click="nextLog" :disabled="activeIndex === -1 || activeIndex >= filteredLogs.length - 1">
               <ChevronDown class="h-4 w-4" />
             </Button>
             <div class="w-px h-4 bg-zinc-200 dark:bg-zinc-800 mx-1"></div>
             <Button variant="ghost" size="icon" class="h-7 w-7 text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100 transition-all" @click="copyLog">
               <Check v-if="isCopied" class="h-4 w-4 text-emerald-500" />
               <Copy v-else class="h-4 w-4" />
             </Button>
             <Button variant="ghost" size="icon" class="h-7 w-7 text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100" @click="showDetail = false">
               <X class="h-4 w-4" />
             </Button>
          </div>
        </div>

        <!-- Body -->
        <div class="flex-1 overflow-y-auto p-4 custom-scrollbar">
          <div class="flex items-start gap-4">
            <!-- Timeline Indicator -->
            <div class="mt-1 w-3.5 h-3.5 rounded-full border-[2px] border-zinc-400 dark:border-zinc-600 shrink-0"></div>
            
            <div class="flex-1 min-w-0">
              <!-- Timeline Header -->
              <div class="flex justify-between items-start mb-3">
                <span class="font-semibold text-zinc-900 dark:text-zinc-100">Request started</span>
                <div class="text-right text-[11px] font-mono text-zinc-500">
                   <p>{{ formatVercelDateTop(detailLog.requestAt) }}</p>
                   <p>{{ formatVercelDateBottom(detailLog.requestAt) }}</p>
                </div>
              </div>
              
              <!-- Data Card -->
              <div class="bg-zinc-50 dark:bg-[#111111] border border-zinc-200 dark:border-zinc-800 rounded-lg p-3 space-y-3 font-mono text-[13px] text-zinc-700 dark:text-zinc-300">
                <div class="flex items-start">
                   <span class="w-[110px] shrink-0 text-zinc-500">Request ID</span>
                   <span class="truncate text-zinc-900 dark:text-zinc-100">{{ detailLog.id }}</span>
                </div>
                <div class="h-px w-full bg-zinc-200 dark:bg-zinc-800/60 my-1"></div>
                <div class="flex items-start">
                   <span class="w-[110px] shrink-0 text-zinc-500">Path</span>
                   <span class="truncate text-zinc-900 dark:text-zinc-100">{{ detailLog.url }}</span>
                </div>
                <div class="h-px w-full bg-zinc-200 dark:bg-zinc-800/60 my-1"></div>
                <div class="flex items-start">
                   <span class="w-[110px] shrink-0 text-zinc-500">Host</span>
                   <span class="truncate text-zinc-900 dark:text-zinc-100">{{ detailLog.userFullname || detailLog.userId || 'system' }}</span>
                </div>
                <div class="h-px w-full bg-zinc-200 dark:bg-zinc-800/60 my-1"></div>
                <div class="flex items-start">
                   <span class="w-[110px] shrink-0 text-zinc-500">User Agent</span>
                   <span class="truncate text-zinc-900 dark:text-zinc-100">{{ detailLog.userAgent || '-' }}</span>
                </div>
                <div class="h-px w-full bg-zinc-200 dark:bg-zinc-800/60 my-1"></div>
                <div class="flex items-start">
                   <span class="w-[110px] shrink-0 text-zinc-500">Duration</span>
                   <span class="text-zinc-900 dark:text-zinc-100">{{ formatDuration(detailLog.durationMs) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      </div>
    </Teleport>
  </AppLayout>
</template>


