<script setup>
import { computed } from 'vue'
import { cn } from '@/lib/utils'
import Button from '@/components/ui/Button.vue'
import DropdownMenu from '@/components/ui/dropdown-menu/DropdownMenu.vue'
import DropdownMenuItem from '@/components/ui/dropdown-menu/DropdownMenuItem.vue'
import DropdownMenuSeparator from '@/components/ui/dropdown-menu/DropdownMenuSeparator.vue'
import { MoreHorizontal } from 'lucide-vue-next'

const props = defineProps({
  label: { type: String, default: 'Actions' },
  items: { type: Array, required: true },
  class: { type: String, default: '' },
})

function handleItemClick(item) {
  if (item.disabled) return
  item.onClick?.()
}

const buttonClass = computed(() => cn('h-8 w-8', props.class))
</script>

<template>
  <DropdownMenu>
    <template #trigger>
      <Button variant="ghost" size="icon" :class="buttonClass">
        <span class="sr-only">{{ label }}</span>
        <MoreHorizontal class="h-4 w-4" />
      </Button>
    </template>

    <template #content>
      <template v-for="(item, index) in items" :key="index">
        <DropdownMenuSeparator v-if="item.separator" />
        <DropdownMenuItem
          :disabled="item.disabled"
          @select="handleItemClick(item)"
          :class="{ 'text-red-600 dark:text-red-400': item.variant === 'destructive' }"
        >
          <component :is="item.icon" v-if="item.icon" class="mr-2 h-4 w-4" />
          <span>{{ item.label }}</span>
        </DropdownMenuItem>
      </template>
    </template>
  </DropdownMenu>
</template>
