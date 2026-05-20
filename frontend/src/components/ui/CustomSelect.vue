<script setup>
import { computed } from 'vue'
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuLabel,
  DropdownMenuSeparator
} from '@/components/ui/dropdown-menu'
import { Button } from '@/components/ui/button'
import { ChevronDown, Filter } from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const props = defineProps({
  modelValue: { type: [String, Number], required: true },
  options: { type: Array, required: true },
  placeholder: { type: String, default: 'Select...' },
  label: { type: String, default: '' },
  class: { type: String, default: '' },
  showIcon: { type: Boolean, default: true },
  align: { type: String, default: 'end' }
})

const emit = defineEmits(['update:modelValue'])

const selectedLabel = computed(() => {
  const option = props.options.find(opt => opt.value === props.modelValue)
  return option ? option.label : props.placeholder
})
</script>

<template>
  <DropdownMenu>
    <DropdownMenuTrigger as-child>
      <Button 
        variant="outline" 
        :class="cn(
          'w-full justify-between px-3 h-9 font-medium border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 hover:bg-zinc-50 dark:hover:bg-zinc-900 transition-all duration-200 shadow-sm group', 
          props.class
        )"
      >
        <div class="flex items-center gap-2 overflow-hidden text-left">
          <Filter v-if="showIcon" class="h-3.5 w-3.5 text-muted-foreground group-hover:text-primary transition-colors shrink-0" />
          <span class="truncate">{{ selectedLabel }}</span>
        </div>
        <ChevronDown class="h-4 w-4 opacity-50 group-hover:opacity-100 transition-opacity ml-1 shrink-0" />
      </Button>
    </DropdownMenuTrigger>
    <DropdownMenuContent :class="cn('w-56 p-1.5', $attrs.contentClass)" :align="align">
      <DropdownMenuLabel v-if="label" class="text-[10px] font-bold uppercase tracking-wider text-muted-foreground px-2 py-1.5">
        {{ label }}
      </DropdownMenuLabel>
      <DropdownMenuSeparator v-if="label" />
      <DropdownMenuRadioGroup :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)">
        <DropdownMenuRadioItem 
          v-for="opt in options" 
          :key="opt.value" 
          :value="opt.value"
          :class="cn('cursor-pointer py-2', opt.class)"
        >
          {{ opt.label }}
        </DropdownMenuRadioItem>
      </DropdownMenuRadioGroup>
    </DropdownMenuContent>
  </DropdownMenu>
</template>
