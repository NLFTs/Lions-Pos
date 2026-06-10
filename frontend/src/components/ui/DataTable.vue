<script setup>
import { computed } from 'vue'
import { cn } from '@/lib/utils'
import { ArrowUpDown, ChevronLeft, ChevronRight, ChevronDown } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import Table from './Table.vue'
import TableHeader from './TableHeader.vue'
import TableBody from './TableBody.vue'
import TableRow from './TableRow.vue'
import TableCell from './TableCell.vue'
import TableHead from './TableHead.vue'
import DataTablePagination from './DataTablePagination.vue'

const props = defineProps({
  data: { type: Array, required: true },
  columns: { type: Array, required: true },
  // Pagination
  page: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  total: { type: Number, default: 0 },
  // Loading
  loading: { type: Boolean, default: false },
  // Empty state
  emptyMessage: { type: String, default: 'Tidak ada data.' },
  // Features
  sortable: { type: Boolean, default: true },
  selectable: { type: Boolean, default: false },
  // Custom row key
  rowKey: { type: String, default: 'id' },
})

const emit = defineEmits(['update:page', 'update:pageSize', 'sort', 'selection-change'])

// Computed page info
const totalPages = computed(() => Math.ceil(props.total / props.pageSize))
const startItem = computed(() => (props.page - 1) * props.pageSize + 1)
const endItem = computed(() => Math.min(props.page * props.pageSize, props.total))

// Handle sort
function handleSort(column) {
  if (!props.sortable || !column.sortable) return
  emit('sort', {
    field: column.key,
    order: column.sortOrder === 'asc' ? 'desc' : 'asc',
  })
}

function getSortIcon(column) {
  if (!column.sortOrder) return 'opacity-30'
  return column.sortOrder === 'asc' ? 'rotate-180' : ''
}
</script>

<template>
  <div class="flex flex-col">
    <!-- Table Container -->
    <div class="bg-card">
      <div v-if="loading" class="flex items-center justify-center py-20">
        <div class="flex flex-col items-center gap-3">
          <div class="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
          <p class="text-sm text-muted-foreground">Memuat data...</p>        </div>
      </div>

      <template v-else>
        <div v-if="data.length === 0" class="flex flex-col items-center justify-center py-20 px-6">
          <div class="rounded-full bg-muted p-4 mb-3">
            <slot name="empty-icon">
              <div class="h-8 w-8 text-muted-foreground" />
            </slot>
          </div>
          <p class="text-sm text-muted-foreground">{{ emptyMessage }}</p>
          <slot name="empty-action" />
        </div>

        <div v-else class="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow class="hover:bg-transparent">
                <TableHead v-if="selectable" class="w-12">
                  <slot name="select-all" />
                </TableHead>
                <template v-for="column in columns" :key="column.key">
                  <TableHead
                    :style="{ width: column.width, minWidth: column.minWidth }"
                    :class="{ 'cursor-pointer select-none hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors': sortable && column.sortable }"
                    @click="handleSort(column)"
                  >
                    <div class="flex items-center gap-2">
                      <span>{{ column.label }}</span>
                      <ArrowUpDown
                        v-if="sortable && column.sortable"
                        :class="['h-3.5 w-3.5 shrink-0 transition-opacity', column.sortOrder ? 'opacity-100' : 'opacity-20']"
                      />
                    </div>
                  </TableHead>
                </template>
                <TableHead v-if="$slots.actions" class="w-24 text-right pr-6">Aksi</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow
                v-for="(item, index) in data"
                :key="item[rowKey] || index"
                class="group"
              >
                <TableCell v-if="selectable">
                  <slot name="select" :item="item" />
                </TableCell>
                <template v-for="column in columns" :key="column.key">
                  <TableCell class="align-middle whitespace-nowrap">
                    <slot :name="`cell-${column.key}`" :item="item" :value="item[column.key]" :index="index">
                      <span class="text-zinc-600 dark:text-zinc-400 font-medium">{{ item[column.key] }}</span>
                    </slot>
                  </TableCell>
                </template>
                <TableCell v-if="$slots.actions" class="text-right">
                  <slot name="actions" :item="item" />
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
      </template>
    </div>

    <!-- Pagination -->
    <DataTablePagination
      v-if="data.length > 0 && !loading"
      :page="page"
      :page-size="pageSize"
      :total="total"
      @update:page="emit('update:page', $event)"
      @update:page-size="emit('update:pageSize', $event)"
    />
  </div>
</template>
