<script setup>
import { computed } from 'vue'
import { ChevronLeft, ChevronRight, ChevronDown } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'

const props = defineProps({
  page: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  total: { type: Number, required: true },
})

const emit = defineEmits(['update:page', 'update:pageSize'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

function prevPage() {
  if (props.page > 1) emit('update:page', props.page - 1)
}

function nextPage() {
  if (props.page < totalPages.value) emit('update:page', props.page + 1)
}

const pageNumbers = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, props.page - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible - 1)

  if (end - start < maxVisible - 1) {
    start = Math.max(1, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})
</script>

<template>
  <div class="flex flex-col sm:flex-row items-center justify-between gap-4 px-6 py-5 border-t border-zinc-100 dark:border-zinc-800">
    <!-- Rows per page -->
    <div class="flex items-center gap-3">
      <div class="relative group">
        <div class="flex h-9 w-[72px] items-center justify-center gap-1.5 rounded-lg border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 text-zinc-900 dark:text-zinc-100 text-sm font-medium group-focus-within:ring-2 group-focus-within:ring-primary/20">
          <span>{{ pageSize }}</span>
          <ChevronDown class="h-4 w-4 text-zinc-500" />
        </div>
        <select
          :value="pageSize"
          @change="emit('update:pageSize', Number($event.target.value))"
          class="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
        >
          <option v-for="size in [10, 20, 30, 50, 100]" :key="size" :value="size" class="text-zinc-900 dark:text-zinc-100 dark:bg-zinc-950">
            {{ size }}
          </option>
        </select>
      </div>
      <span class="text-sm font-medium text-zinc-500">Rows per page</span>
    </div>

    <!-- Page info & controls -->
    <div class="flex items-center gap-4 text-xs font-medium">
      <span class="text-zinc-500">
        Page <span class="text-zinc-900 dark:text-zinc-100">{{ page }}</span> of <span class="text-zinc-900 dark:text-zinc-100">{{ totalPages }}</span>
      </span>

      <div class="flex items-center gap-1">
        <!-- Prev -->
        <Button
          variant="outline"
          size="icon"
          :disabled="page === 1"
          @click="prevPage"
          class="h-8 w-8 border-zinc-200 dark:border-zinc-800 hover:bg-zinc-50"
        >
          <ChevronLeft class="h-3.5 w-3.5 text-zinc-600" />
        </Button>

        <!-- Pages -->
        <div class="flex items-center gap-1">
          <template v-for="pageNum in pageNumbers" :key="pageNum">
            <Button
              :variant="pageNum === page ? 'default' : 'outline'"
              @click="emit('update:page', pageNum)"
              class="h-8 w-8 p-0 text-[11px] font-bold shadow-none"
              :class="pageNum === page ? 'bg-primary text-primary-foreground hover:bg-primary/90 border-none' : 'border-zinc-200 dark:border-zinc-800 text-zinc-600 hover:bg-zinc-50'"
            >
              {{ pageNum }}
            </Button>
          </template>
        </div>

        <!-- Next -->
        <Button
          variant="outline"
          size="icon"
          :disabled="page === totalPages"
          @click="nextPage"
          class="h-8 w-8 border-zinc-200 dark:border-zinc-800 hover:bg-zinc-50"
        >
          <ChevronRight class="h-3.5 w-3.5 text-zinc-600" />
        </Button>
      </div>
    </div>
  </div>
</template>