<script setup>
import { ref, computed } from 'vue'
import { cn } from '@/lib/utils'
import { Search, X } from 'lucide-vue-next'
import Input from '@/components/ui/Input.vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: 'Cari...' },
  debounce: { type: Number, default: 300 },
  class: { type: [String, Object, Array], default: '' },
  inputClass: { type: [String, Object, Array], default: '' },
})

const emit = defineEmits(['update:modelValue', 'search'])

const localValue = ref(props.modelValue)
let timeout = null

const debouncedValue = computed({
  get: () => localValue.value,
  set: (value) => {
    localValue.value = value
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      emit('update:modelValue', value)
      emit('search', value)
    }, props.debounce)
  },
})

function clearSearch() {
  localValue.value = ''
  emit('update:modelValue', '')
  emit('search', '')
}

const wrapperClass = computed(() => cn('relative', props.class))
</script>

<template>
  <div :class="wrapperClass">
    <Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
    <Input
      v-model="debouncedValue"
      :placeholder="placeholder"
      :class="cn('h-10 w-full pl-9 pr-9', props.inputClass)"
    />
    <button
      v-if="localValue"
      @click="clearSearch"
      class="absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
    >
      <X class="h-4 w-4" />
    </button>
  </div>
</template>
