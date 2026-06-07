<script setup>
import { ref, computed, watch } from 'vue'
import { ChevronLeft, ChevronRight, ChevronDown } from 'lucide-vue-next'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({ start: null, end: null }),
  },
  open: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue', 'close'])

// ── Internal state ──────────────────────────────────────────────────────────
const today = new Date()

// Left calendar = previous month, right calendar = current month
const leftYear  = ref(today.getMonth() === 0 ? today.getFullYear() - 1 : today.getFullYear())
const leftMonth = ref(today.getMonth() === 0 ? 11 : today.getMonth() - 1)
const rightYear  = computed(() => leftMonth.value === 11 ? leftYear.value + 1 : leftYear.value)
const rightMonth = computed(() => (leftMonth.value + 1) % 12)

const rangeStart = ref(props.modelValue?.start ? new Date(props.modelValue.start) : null)
const rangeEnd   = ref(props.modelValue?.end   ? new Date(props.modelValue.end)   : null)
const hovering   = ref(null)

const MONTHS = ['January','February','March','April','May','June','July','August','September','October','November','December']
const DAYS   = ['Su','Mo','Tu','We','Th','Fr','Sa']

// ── Presets ─────────────────────────────────────────────────────────────────
const presets = [
  { label: 'This year',   fn: () => rangeOf(new Date(today.getFullYear(), 0, 1), new Date(today.getFullYear(), 11, 31)) },
  { label: 'This month',  fn: () => rangeOf(new Date(today.getFullYear(), today.getMonth(), 1), new Date(today.getFullYear(), today.getMonth() + 1, 0)) },
  { label: 'This week',   fn: () => { const d = new Date(today); d.setDate(today.getDate() - today.getDay()); const e = new Date(d); e.setDate(d.getDate() + 6); rangeOf(d, e) } },
  { label: 'Last year',   fn: () => rangeOf(new Date(today.getFullYear()-1, 0, 1), new Date(today.getFullYear()-1, 11, 31)) },
  { label: 'Last month',  fn: () => rangeOf(new Date(today.getFullYear(), today.getMonth()-1, 1), new Date(today.getFullYear(), today.getMonth(), 0)) },
  { label: 'Last week',   fn: () => { const d = new Date(today); d.setDate(today.getDate() - today.getDay() - 7); const e = new Date(d); e.setDate(d.getDate() + 6); rangeOf(d, e) } },
]

function rangeOf(s, e) {
  rangeStart.value = s
  rangeEnd.value = e
  leftYear.value  = s.getFullYear()
  leftMonth.value = s.getMonth()
  emit('update:modelValue', { start: s, end: e })
}

// ── Calendar building ────────────────────────────────────────────────────────
function buildCalendar(year, month) {
  const firstDay  = new Date(year, month, 1).getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const prevDays  = new Date(year, month, 0).getDate()

  const cells = []
  // Prev month filler
  for (let i = firstDay - 1; i >= 0; i--) {
    cells.push({ day: prevDays - i, current: false, date: new Date(month === 0 ? year - 1 : year, month === 0 ? 11 : month - 1, prevDays - i) })
  }
  // Current month
  for (let d = 1; d <= daysInMonth; d++) {
    cells.push({ day: d, current: true, date: new Date(year, month, d) })
  }
  // Next month filler
  const remaining = 42 - cells.length
  for (let d = 1; d <= remaining; d++) {
    cells.push({ day: d, current: false, date: new Date(month === 11 ? year + 1 : year, month === 11 ? 0 : month + 1, d) })
  }
  return cells
}

const leftCells  = computed(() => buildCalendar(leftYear.value,  leftMonth.value))
const rightCells = computed(() => buildCalendar(rightYear.value, rightMonth.value))

// ── Month/Year navigation ────────────────────────────────────────────────────
function prevMonth() {
  if (leftMonth.value === 0) { leftMonth.value = 11; leftYear.value-- }
  else leftMonth.value--
}
function nextMonth() {
  if (leftMonth.value === 11) { leftMonth.value = 0; leftYear.value++ }
  else leftMonth.value++
}

// ── Month selector dropdown ──────────────────────────────────────────────────
const showLeftMonthPicker  = ref(false)
const showLeftYearPicker   = ref(false)
const showRightMonthPicker = ref(false)
const showRightYearPicker  = ref(false)

function setLeftMonth(m)   { leftMonth.value = m;  showLeftMonthPicker.value  = false }
function setLeftYear(y)    { leftYear.value  = y;  showLeftYearPicker.value   = false }
function setRightMonth(m)  {
  // adjust leftMonth so rightMonth = m
  leftMonth.value = m === 0 ? 11 : m - 1
  if (m === 0) leftYear.value = rightYear.value - 1
  showRightMonthPicker.value = false
}
function setRightYear(y) {
  // keep same offset, adjust left year
  leftYear.value = y - (leftMonth.value === 11 ? 1 : 0)
  if (leftMonth.value !== 11) leftYear.value = y
  showRightYearPicker.value = false
}

const yearRange = computed(() => {
  const base = leftYear.value
  return Array.from({ length: 12 }, (_, i) => base - 5 + i)
})

// ── Day click logic ──────────────────────────────────────────────────────────
function clickDay(date) {
  if (!rangeStart.value || (rangeStart.value && rangeEnd.value)) {
    rangeStart.value = new Date(date)
    rangeEnd.value   = null
  } else {
    if (date < rangeStart.value) {
      rangeEnd.value   = new Date(rangeStart.value)
      rangeStart.value = new Date(date)
    } else {
      rangeEnd.value = new Date(date)
    }
    emit('update:modelValue', { start: rangeStart.value, end: rangeEnd.value })
    emit('close')
  }
}

// ── Day styling ──────────────────────────────────────────────────────────────
function isSameDay(a, b) {
  if (!a || !b) return false
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate()
}

function dayState(date) {
  const s = rangeStart.value
  const e = rangeEnd.value || hovering.value
  const isStart = isSameDay(date, s)
  const isEnd   = e && isSameDay(date, e)
  const inRange = s && e && date > (s < e ? s : e) && date < (s < e ? e : s)
  return { isStart, isEnd, inRange }
}

function isTodayDate(date) { return isSameDay(date, today) }

// Active preset detection
const activePreset = ref(null)
watch([rangeStart, rangeEnd], () => { activePreset.value = null })
</script>

<template>
  <Teleport to="body">
    <Transition name="drp-fade">
      <div
        v-if="open"
        class="drp-overlay"
        @mousedown.self="emit('close')"
      >
        <div class="drp-panel" @mousedown.stop>
          <!-- ── Preset Sidebar ───────────────────────────────────────────── -->
          <div class="drp-sidebar">
            <button
              v-for="preset in presets"
              :key="preset.label"
              class="drp-preset-btn"
              :class="{ 'drp-preset-active': activePreset === preset.label }"
              @click="preset.fn(); activePreset = preset.label"
            >
              {{ preset.label }}
            </button>
          </div>

          <!-- ── Calendars ──────────────────────────────────────────────── -->
          <div class="drp-calendars">
            <!-- Navigation row -->
            <div class="drp-nav">
              <button class="drp-nav-btn" @click="prevMonth">
                <ChevronLeft class="w-4 h-4" />
              </button>

              <!-- Left month/year -->
              <div class="drp-month-controls">
                <div class="drp-dropdown-wrap">
                  <button class="drp-month-btn" @click.stop="showLeftMonthPicker = !showLeftMonthPicker; showLeftYearPicker = false">
                    {{ MONTHS[leftMonth] }} <ChevronDown class="w-3 h-3 inline" />
                  </button>
                  <div v-if="showLeftMonthPicker" class="drp-dropdown" @mousedown.stop>
                    <button v-for="(m, i) in MONTHS" :key="m" class="drp-dropdown-item" :class="{ 'drp-dropdown-item-active': i === leftMonth }" @click="setLeftMonth(i)">{{ m }}</button>
                  </div>
                </div>
                <div class="drp-dropdown-wrap">
                  <button class="drp-month-btn" @click.stop="showLeftYearPicker = !showLeftYearPicker; showLeftMonthPicker = false">
                    {{ leftYear }} <ChevronDown class="w-3 h-3 inline" />
                  </button>
                  <div v-if="showLeftYearPicker" class="drp-dropdown drp-dropdown-year" @mousedown.stop>
                    <button v-for="y in yearRange" :key="y" class="drp-dropdown-item" :class="{ 'drp-dropdown-item-active': y === leftYear }" @click="setLeftYear(y)">{{ y }}</button>
                  </div>
                </div>
              </div>

              <div class="drp-divider" />

              <!-- Right month/year -->
              <div class="drp-month-controls">
                <div class="drp-dropdown-wrap">
                  <button class="drp-month-btn" @click.stop="showRightMonthPicker = !showRightMonthPicker; showRightYearPicker = false">
                    {{ MONTHS[rightMonth] }} <ChevronDown class="w-3 h-3 inline" />
                  </button>
                  <div v-if="showRightMonthPicker" class="drp-dropdown" @mousedown.stop>
                    <button v-for="(m, i) in MONTHS" :key="m" class="drp-dropdown-item" :class="{ 'drp-dropdown-item-active': i === rightMonth }" @click="setRightMonth(i)">{{ m }}</button>
                  </div>
                </div>
                <div class="drp-dropdown-wrap">
                  <button class="drp-month-btn" @click.stop="showRightYearPicker = !showRightYearPicker; showRightMonthPicker = false">
                    {{ rightYear }} <ChevronDown class="w-3 h-3 inline" />
                  </button>
                  <div v-if="showRightYearPicker" class="drp-dropdown drp-dropdown-year" @mousedown.stop>
                    <button v-for="y in yearRange" :key="y" class="drp-dropdown-item" :class="{ 'drp-dropdown-item-active': y === rightYear }" @click="setRightYear(y)">{{ y }}</button>
                  </div>
                </div>
              </div>

              <button class="drp-nav-btn" @click="nextMonth">
                <ChevronRight class="w-4 h-4" />
              </button>
            </div>

            <!-- Dual calendar grid -->
            <div class="drp-grids">
              <!-- LEFT calendar -->
              <div class="drp-grid">
                <div class="drp-weekdays">
                  <span v-for="d in DAYS" :key="d" class="drp-weekday">{{ d }}</span>
                </div>
                <div class="drp-days">
                  <div
                    v-for="(cell, idx) in leftCells"
                    :key="idx"
                    class="drp-day-wrap"
                    :class="{
                      'drp-in-range': cell.current && dayState(cell.date).inRange,
                      'drp-range-start': cell.current && dayState(cell.date).isStart,
                      'drp-range-end':   cell.current && dayState(cell.date).isEnd,
                    }"
                    @mouseenter="hovering = cell.current ? cell.date : null"
                    @mouseleave="hovering = null"
                  >
                    <button
                      class="drp-day"
                      :class="{
                        'drp-day-other':   !cell.current,
                        'drp-day-today':    cell.current && isTodayDate(cell.date),
                        'drp-day-selected': cell.current && (dayState(cell.date).isStart || dayState(cell.date).isEnd),
                        'drp-day-in-range': cell.current && dayState(cell.date).inRange,
                      }"
                      @click="cell.current && clickDay(cell.date)"
                    >{{ cell.day }}</button>
                  </div>
                </div>
              </div>

              <!-- RIGHT calendar -->
              <div class="drp-grid">
                <div class="drp-weekdays">
                  <span v-for="d in DAYS" :key="d" class="drp-weekday">{{ d }}</span>
                </div>
                <div class="drp-days">
                  <div
                    v-for="(cell, idx) in rightCells"
                    :key="idx"
                    class="drp-day-wrap"
                    :class="{
                      'drp-in-range':    cell.current && dayState(cell.date).inRange,
                      'drp-range-start': cell.current && dayState(cell.date).isStart,
                      'drp-range-end':   cell.current && dayState(cell.date).isEnd,
                    }"
                    @mouseenter="hovering = cell.current ? cell.date : null"
                    @mouseleave="hovering = null"
                  >
                    <button
                      class="drp-day"
                      :class="{
                        'drp-day-other':   !cell.current,
                        'drp-day-today':    cell.current && isTodayDate(cell.date),
                        'drp-day-selected': cell.current && (dayState(cell.date).isStart || dayState(cell.date).isEnd),
                        'drp-day-in-range': cell.current && dayState(cell.date).inRange,
                      }"
                      @click="cell.current && clickDay(cell.date)"
                    >{{ cell.day }}</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* ── Overlay ─────────────────────────────────────────────────────────────── */
.drp-overlay {
  position: fixed;
  inset: 0;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ── Panel ───────────────────────────────────────────────────────────────── */
.drp-panel {
  display: flex;
  background: #111118;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  overflow: visible;
  box-shadow: 0 32px 80px rgba(0, 0, 0, 0.7), 0 0 0 1px rgba(255,255,255,0.05);
  user-select: none;
  position: relative;
}

/* ── Sidebar ─────────────────────────────────────────────────────────────── */
.drp-sidebar {
  display: flex;
  flex-direction: column;
  padding: 16px 8px;
  border-right: 1px solid rgba(255, 255, 255, 0.07);
  min-width: 130px;
  gap: 2px;
}

.drp-preset-btn {
  text-align: left;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 500;
  color: #94a3b8;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s;
  cursor: pointer;
  white-space: nowrap;
}
.drp-preset-btn:hover {
  background: rgba(255,255,255,0.07);
  color: #f1f5f9;
}
.drp-preset-active {
  background: rgba(255,255,255,0.1) !important;
  color: #fff !important;
}

/* ── Calendars container ─────────────────────────────────────────────────── */
.drp-calendars {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ── Nav row ─────────────────────────────────────────────────────────────── */
.drp-nav {
  display: flex;
  align-items: center;
  gap: 12px;
}

.drp-nav-btn {
  width: 28px;
  height: 28px;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  transition: background 0.15s, color 0.15s;
  cursor: pointer;
  flex-shrink: 0;
}
.drp-nav-btn:hover {
  background: rgba(255,255,255,0.1);
  color: #f1f5f9;
}

.drp-divider {
  flex: 1;
}

.drp-month-controls {
  display: flex;
  align-items: center;
  gap: 4px;
}

.drp-dropdown-wrap {
  position: relative;
}

.drp-month-btn {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 14px;
  font-weight: 700;
  color: #f1f5f9;
  padding: 4px 8px;
  border-radius: 7px;
  cursor: pointer;
  transition: background 0.15s;
  white-space: nowrap;
}
.drp-month-btn:hover {
  background: rgba(255,255,255,0.08);
}

/* Dropdowns for month/year selection */
.drp-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 50%;
  transform: translateX(-50%);
  background: #1a1a24;
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 10px;
  padding: 6px;
  z-index: 100;
  min-width: 120px;
  box-shadow: 0 16px 40px rgba(0,0,0,0.6);
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2px;
}
.drp-dropdown-year {
  grid-template-columns: repeat(3, 1fr);
  min-width: 160px;
}

.drp-dropdown-item {
  font-size: 12px;
  font-weight: 500;
  color: #94a3b8;
  padding: 5px 8px;
  border-radius: 6px;
  cursor: pointer;
  text-align: center;
  transition: background 0.12s, color 0.12s;
  white-space: nowrap;
}
.drp-dropdown-item:hover { background: rgba(255,255,255,0.08); color: #f1f5f9; }
.drp-dropdown-item-active { background: rgba(255,255,255,0.12) !important; color: #fff !important; }

/* ── Dual grids ──────────────────────────────────────────────────────────── */
.drp-grids {
  display: flex;
  gap: 28px;
}

.drp-grid {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.drp-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 36px);
  gap: 0;
}
.drp-weekday {
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  height: 30px;
  line-height: 30px;
}

.drp-days {
  display: grid;
  grid-template-columns: repeat(7, 36px);
  gap: 0;
}

/* ── Day wrapper (handles range background) ──────────────────────────────── */
.drp-day-wrap {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 34px;
}

.drp-in-range::before {
  content: '';
  position: absolute;
  inset: 3px 0;
  background: rgba(255, 255, 255, 0.08);
  z-index: 0;
}
.drp-range-start::before {
  content: '';
  position: absolute;
  inset: 3px 0 3px 50%;
  background: rgba(255, 255, 255, 0.08);
  z-index: 0;
}
.drp-range-end::before {
  content: '';
  position: absolute;
  inset: 3px 50% 3px 0;
  background: rgba(255, 255, 255, 0.08);
  z-index: 0;
}

/* ── Day button ──────────────────────────────────────────────────────────── */
.drp-day {
  position: relative;
  z-index: 1;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  font-size: 13px;
  font-weight: 500;
  color: #cbd5e1;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.drp-day:hover {
  background: rgba(255,255,255,0.12);
  color: #fff;
}
.drp-day-other {
  color: #334155;
  cursor: default;
  pointer-events: none;
}
.drp-day-today {
  font-weight: 700;
  color: #fff;
  border: 1.5px solid rgba(255,255,255,0.3);
}
.drp-day-selected {
  background: #ffffff !important;
  color: #000000 !important;
  font-weight: 700;
}
.drp-day-in-range {
  background: transparent;
  color: #e2e8f0;
}

/* ── Transition ──────────────────────────────────────────────────────────── */
.drp-fade-enter-active,
.drp-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.drp-fade-enter-from,
.drp-fade-leave-to {
  opacity: 0;
  transform: scale(0.97) translateY(-4px);
}
</style>
