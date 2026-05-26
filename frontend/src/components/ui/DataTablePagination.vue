<script setup>
import { ref, computed, nextTick, onUnmounted } from 'vue'
import { ChevronLeft, ChevronRight, ChevronDown } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'

const props = defineProps({
  page: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  total: { type: Number, required: true },
})

const emit = defineEmits(['update:page', 'update:pageSize'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

// Dropdown States
const isOpen = ref(false)
const triggerRef = ref(null)
const dropdownStyle = ref({})
const isUpwards = ref(false) // State penanda arah animasi

function prevPage() {
  if (props.page > 1) emit('update:page', props.page - 1)
}

function nextPage() {
  if (props.page < totalPages.value) emit('update:page', props.page + 1)
}

// Pas dropdown ditekan, hitung koordinat dan sisa space layar
function toggleDropdown() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    nextTick(() => {
      updatePosition()
    })
    window.addEventListener('scroll', closeDropdown, true)
    window.addEventListener('resize', closeDropdown)
  } else {
    removeListeners()
  }
}

function updatePosition() {
  if (triggerRef.value) {
    const rect = triggerRef.value.getBoundingClientRect()
    const dropdownHeight = 185 // Estimasi tinggi kotak menu (5 pilihan ukuran)
    const spaceBelow = window.innerHeight - rect.bottom

    // 🔥 SMART DETECTOR: Jika mentok di bawah layar, paksa buka ke atas!
    if (spaceBelow < dropdownHeight) {
      isUpwards.value = true
      dropdownStyle.value = {
        position: 'fixed',
        bottom: `${window.innerHeight - rect.top + 6}px`,
        left: `${rect.left}px`,
        width: `${rect.width}px`,
        zIndex: '99999'
      }
    } else {
      // Jika ruang bawah lega, normal buka ke bawah
      isUpwards.value = false
      dropdownStyle.value = {
        position: 'fixed',
        top: `${rect.bottom + 6}px`,
        left: `${rect.left}px`,
        width: `${rect.width}px`,
        zIndex: '99999'
      }
    }
  }
}

function closeDropdown() {
  isOpen.value = false
  removeListeners()
}

function removeListeners() {
  window.removeEventListener('scroll', closeDropdown, true)
  window.removeEventListener('resize', closeDropdown)
}

function changePageSize(size) {
  emit('update:pageSize', size)
  emit('update:page', 1)
  closeDropdown()
}

onUnmounted(removeListeners)

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
  <div class="flex flex-col sm:flex-row items-center justify-between gap-4 px-6 py-5 border-t border-zinc-100 dark:border-zinc-800 bg-white dark:bg-zinc-950">
    
    <div class="flex items-center gap-3">
      <div class="relative">
        
        <button
          ref="triggerRef"
          type="button"
          @click="toggleDropdown"
          class="flex h-9 w-[74px] items-center justify-center gap-1.5 rounded-lg border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 text-zinc-900 dark:text-zinc-100 text-sm font-medium hover:bg-zinc-50 dark:hover:bg-zinc-900 shadow-sm transition-all focus:outline-none focus:ring-2 focus:ring-zinc-950 dark:focus:ring-zinc-300 relative z-50"
        >
          <span>{{ pageSize }}</span>
          <ChevronDown class="h-3.5 w-3.5 text-zinc-500 transition-transform duration-200" :class="{ 'rotate-180': isOpen }" />
        </button>

        <Teleport to="body">
          <div v-if="isOpen" class="fixed inset-0 z-[99998]">
            <div class="absolute inset-0" @click="closeDropdown" />
            
            <div 
              :style="dropdownStyle"
              :class="[
                'rounded-lg border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 p-1 shadow-lg animate-in fade-in duration-100',
                isUpwards ? 'slide-up-origin' : 'slide-down-origin'
              ]"
            >
              <button
                v-for="size in [10, 20, 30, 50, 100]"
                :key="size"
                type="button"
                @click="changePageSize(size)"
                class="w-full text-center px-2 py-1.5 text-xs font-semibold rounded-md transition-colors block outline-none text-zinc-700 dark:text-zinc-300 hover:bg-zinc-50 dark:hover:bg-zinc-900 hover:text-zinc-900 dark:hover:text-zinc-100"
              >
                {{ size }}
              </button>
            </div>
          </div>
        </Teleport>
      </div>
      <span class="text-sm font-medium text-zinc-500">Rows per page</span>
    </div>

    <div class="flex items-center gap-4 text-xs font-medium">
      <span class="text-zinc-500">
        Page <span class="text-zinc-900 dark:text-zinc-100">{{ page }}</span> of <span class="text-zinc-900 dark:text-zinc-100">{{ totalPages }}</span>
      </span>

      <div class="flex items-center gap-1">
        <Button variant="outline" size="icon" :disabled="page === 1" @click="prevPage" class="h-8 w-8 border-zinc-200 dark:border-zinc-800 hover:bg-zinc-50 dark:hover:bg-zinc-900">
          <ChevronLeft class="h-3.5 w-3.5 text-zinc-600 dark:text-zinc-400" />
        </Button>

        <div class="flex items-center gap-1">
          <template v-for="pageNum in pageNumbers" :key="pageNum">
            <Button
              :variant="pageNum === page ? 'default' : 'outline'"
              @click="emit('update:page', pageNum)"
              class="h-8 w-8 p-0 text-[11px] font-bold shadow-none"
              :class="pageNum === page ? 'bg-primary text-primary-foreground border-none' : 'border-zinc-200 dark:border-zinc-800 text-zinc-600 dark:text-zinc-400 hover:bg-zinc-50 dark:hover:bg-zinc-900'"
            >
              {{ pageNum }}
            </Button>
          </template>
        </div>

        <Button variant="outline" size="icon" :disabled="page === totalPages" @click="nextPage" class="h-8 w-8 border-zinc-200 dark:border-zinc-800 hover:bg-zinc-50 dark:hover:bg-zinc-900">
          <ChevronRight class="h-3.5 w-3.5 text-zinc-600 dark:text-zinc-400" />
        </Button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Origin animasi dinamis biar pop up-nya rapih */
.slide-down-origin {
  transform-origin: top center;
  animation: slideDown 0.12s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-origin {
  transform-origin: bottom center;
  animation: slideUp 0.12s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideDown {
  from { opacity: 0; transform: scale(0.96) translateY(-4px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
@keyframes slideUp {
  from { opacity: 0; transform: scale(0.96) translateY(4px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
</style>