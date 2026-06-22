<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="show" class="modal-overlay" @click.self="onClose">
        <div class="modal-box">

          <!-- Header -->
          <div class="modal-header">
            <div class="modal-title-group">
              <div class="modal-icon" :class="closedShift ? 'icon-purple' : mode === 'open' ? 'icon-green' : 'icon-red'">
                <CheckCircle v-if="closedShift" :size="20" />
                <Clock v-else-if="mode === 'open'" :size="20" />
                <LogOut v-else :size="20" />
              </div>
              <div>
                <h2 class="modal-title">
                  {{ closedShift ? 'Shift Ditutup' : mode === 'open' ? 'Buka Shift Kasir' : 'Tutup Shift Kasir' }}
                </h2>
                <p class="modal-subtitle">
                  {{ closedShift ? 'Ringkasan omzet shift telah dihitung' : mode === 'open' ? 'Mulai sesi kasir Anda sekarang' : 'Akhiri sesi dan lihat ringkasan omzet' }}
                </p>
              </div>
            </div>
            <button class="btn-close" @click="onClose">
              <X :size="18" />
            </button>
          </div>

          <!-- Body -->
          <div class="modal-body">

            <!-- MODE: OPEN SHIFT -->
            <template v-if="mode === 'open' && !closedShift">
              <div class="form-group">
                <label class="form-label">Cabang</label>
                <div class="branch-display">
                  <Building2 :size="16" />
                  <span>{{ branchName }}</span>
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">Modal Awal (Uang Kas)</label>
                <div class="input-prefix-wrap">
                  <span class="input-prefix">Rp</span>
                  <input
                    v-model.number="form.startingCash"
                    type="number"
                    min="0"
                    placeholder="0"
                    class="form-input with-prefix"
                  />
                </div>
                <p class="form-hint">Jumlah uang tunai yang Anda bawa saat mulai shift</p>
              </div>
              <div class="form-group">
                <label class="form-label">Catatan (opsional)</label>
                <textarea
                  v-model="form.notes"
                  rows="2"
                  placeholder="Misal: Shift pagi, menggantikan kasir A..."
                  class="form-textarea"
                />
              </div>
            </template>

            <!-- MODE: CLOSE SHIFT — form konfirmasi -->
            <template v-else-if="mode === 'close' && !closedShift">
              <div class="shift-summary">
                <div class="summary-row">
                  <span class="summary-label">Kasir</span>
                  <span class="summary-value">{{ activeShift?.cashierUsername }}</span>
                </div>
                <div class="summary-row">
                  <span class="summary-label">Cabang</span>
                  <span class="summary-value">{{ activeShift?.branchName }}</span>
                </div>
                <div class="summary-row">
                  <span class="summary-label">Mulai Shift</span>
                  <span class="summary-value">{{ formatDateTime(activeShift?.startedAt) }}</span>
                </div>
                <div class="summary-row">
                  <span class="summary-label">Modal Awal</span>
                  <span class="summary-value">{{ formatCurrency(activeShift?.startingCash) }}</span>
                </div>
                <div class="summary-divider" />
                <div class="summary-row highlight">
                  <span class="summary-label">Estimasi Omzet</span>
                  <span class="summary-value omzet">Dihitung saat tutup</span>
                </div>
              </div>
              <div class="form-group mt-4">
                <label class="form-label">Catatan Penutupan (opsional)</label>
                <textarea
                  v-model="form.closingNotes"
                  rows="2"
                  placeholder="Catatan akhir shift..."
                  class="form-textarea"
                />
              </div>
            </template>

            <!-- MODE: HASIL TUTUP SHIFT — ringkasan omzet aktual -->
            <template v-else-if="closedShift">
              <div class="result-summary">
                <!-- Omzet highlight -->
                <div class="result-omzet">
                  <span class="result-omzet-label">Total Omzet</span>
                  <span class="result-omzet-value">{{ formatCurrency(closedShift.totalRevenue) }}</span>
                </div>
                <div class="summary-divider" />
                <div class="summary-row">
                  <span class="summary-label">Kasir</span>
                  <span class="summary-value">{{ closedShift.cashierFullname || closedShift.cashierUsername }}</span>
                </div>
                <div class="summary-row">
                  <span class="summary-label">Cabang</span>
                  <span class="summary-value">{{ closedShift.branchName }}</span>
                </div>
                <div class="summary-row">
                  <span class="summary-label">Mulai Shift</span>
                  <span class="summary-value">{{ formatDateTime(closedShift.startedAt) }}</span>
                </div>
                <div class="summary-row">
                  <span class="summary-label">Selesai Shift</span>
                  <span class="summary-value">{{ formatDateTime(closedShift.endedAt) }}</span>
                </div>
                <div class="summary-row">
                  <span class="summary-label">Durasi</span>
                  <span class="summary-value">{{ formatDuration(closedShift.startedAt, closedShift.endedAt) }}</span>
                </div>
                <div class="summary-row">
                  <span class="summary-label">Modal Awal</span>
                  <span class="summary-value">{{ formatCurrency(closedShift.startingCash) }}</span>
                </div>
                <div class="summary-divider" />
                <!-- Breakdown pembayaran -->
                <div class="breakdown-grid">
                  <div class="breakdown-card breakdown-cash">
                    <span class="breakdown-icon">💵</span>
                    <div class="breakdown-info">
                      <span class="breakdown-label">Tunai</span>
                      <span class="breakdown-value">{{ formatCurrency(closedShift.cashRevenue) }}</span>
                      <span class="breakdown-count">{{ closedShift.cashTransactions }} transaksi</span>
                    </div>
                  </div>
                  <div class="breakdown-card breakdown-transfer">
                    <span class="breakdown-icon">🏦</span>
                    <div class="breakdown-info">
                      <span class="breakdown-label">Transfer</span>
                      <span class="breakdown-value">{{ formatCurrency(closedShift.transferRevenue) }}</span>
                      <span class="breakdown-count">{{ closedShift.transferTransactions }} transaksi</span>
                    </div>
                  </div>
                </div>
                <div class="summary-divider" />
                <div class="summary-row highlight">
                  <span class="summary-label">Total Transaksi</span>
                  <span class="summary-value">{{ closedShift.totalTransactions }} order</span>
                </div>
                <div class="summary-row highlight">
                  <span class="summary-label">Total Omzet</span>
                  <span class="summary-value omzet">{{ formatCurrency(closedShift.totalRevenue) }}</span>
                </div>
                <div v-if="closedShift.closingNotes" class="form-group" style="margin-top:.5rem">
                  <span class="form-label">Catatan Penutupan</span>
                  <p class="form-hint" style="color:#374151">{{ closedShift.closingNotes }}</p>
                </div>
              </div>
            </template>
          </div>

          <!-- Footer -->
          <div class="modal-footer">
            <template v-if="closedShift">
              <button class="btn btn-print" @click="printShiftSummary">
                <Printer :size="15" /> Cetak Ringkasan
              </button>
              <button class="btn btn-success" @click="onClose">
                Selesai
              </button>
            </template>
            <template v-else>
              <button class="btn btn-ghost" @click="onClose" :disabled="loading">
                Batal
              </button>
              <button
                class="btn"
                :class="mode === 'open' ? 'btn-success' : 'btn-danger'"
                @click="onSubmit"
                :disabled="loading"
              >
                <Loader2 v-if="loading" :size="16" class="spin" />
                {{ mode === 'open' ? 'Buka Shift' : 'Tutup & Hitung Omzet' }}
              </button>
            </template>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Clock, LogOut, X, Building2, Loader2, CheckCircle, Printer } from 'lucide-vue-next'

const props = defineProps({
  show: Boolean,
  mode: { type: String, default: 'open' }, // 'open' | 'close'
  branchId: Number,
  branchName: String,
  activeShift: Object,
  // Dikirim dari parent setelah close shift berhasil, berisi data shift yang sudah ditutup
  closedShift: { type: Object, default: null },
})

const emit = defineEmits(['close', 'submitted'])

const loading = ref(false)
const form = ref({ startingCash: 0, notes: '', closingNotes: '' })

watch(() => props.show, (val) => {
  if (val) form.value = { startingCash: 0, notes: '', closingNotes: '' }
})

function onClose() {
  if (!loading.value) emit('close')
}

async function onSubmit() {
  emit('submitted', { ...form.value })
}

function formatCurrency(val) {
  if (val == null) return 'Rp 0'
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(val)
}

function formatDateTime(val) {
  if (!val) return '-'
  return new Date(val).toLocaleString('id-ID', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

function formatDuration(start, end) {
  if (!start || !end) return '-'
  const ms = new Date(end) - new Date(start)
  const h = Math.floor(ms / 3600000)
  const m = Math.floor((ms % 3600000) / 60000)
  return `${h} jam ${m} menit`
}

function printShiftSummary() {
  const s = props.closedShift
  if (!s) return

  const html = `<!DOCTYPE html><html><head>
    <meta charset="utf-8"/>
    <title>Ringkasan Shift — ${s.cashierUsername}</title>
    <style>
      @page { size: 80mm auto; margin: 4mm; }
      * { box-sizing: border-box; }
      body { font-family: 'Courier New', monospace; font-size: 11px; color: #000; width: 72mm; margin: 0 auto; }
      .center { text-align: center; }
      .bold { font-weight: bold; }
      .lg { font-size: 14px; }
      .xl { font-size: 18px; }
      .divider { border-top: 1px dashed #000; margin: 5px 0; }
      .row { display: flex; justify-content: space-between; margin: 2px 0; }
      .highlight { font-weight: bold; font-size: 13px; }
    </style>
  </head><body>
    <div class="center bold lg">${s.branchName || 'KASIR'}</div>
    <div class="center" style="font-size:10px">RINGKASAN SHIFT KASIR</div>
    <div class="divider"></div>
    <div class="row"><span>Kasir</span><span class="bold">${s.cashierFullname || s.cashierUsername}</span></div>
    <div class="row"><span>Cabang</span><span>${s.branchName || '-'}</span></div>
    <div class="row"><span>Mulai</span><span>${formatDateTime(s.startedAt)}</span></div>
    <div class="row"><span>Selesai</span><span>${formatDateTime(s.endedAt)}</span></div>
    <div class="row"><span>Durasi</span><span>${formatDuration(s.startedAt, s.endedAt)}</span></div>
    <div class="divider"></div>
    <div class="row"><span>Modal Awal</span><span>${formatCurrency(s.startingCash)}</span></div>
    <div class="divider"></div>
    <div class="row highlight"><span>Jumlah Transaksi</span><span>${s.totalTransactions} order</span></div>
    <div class="row highlight" style="font-size:15px;margin-top:4px">
      <span>TOTAL OMZET</span><span>${formatCurrency(s.totalRevenue)}</span>
    </div>
    ${s.closingNotes ? `<div class="divider"></div><div style="font-size:10px">Catatan: ${s.closingNotes}</div>` : ''}
    <div class="divider"></div>
    <div class="center" style="font-size:9px;margin-top:4px">Dicetak: ${formatDateTime(new Date().toISOString())}</div>
  </body></html>`

  const win = window.open('', '_blank', 'width=340,height=500')
  if (!win) return
  win.document.write(html)
  win.document.close()
  win.focus()
  setTimeout(() => { win.print(); win.close() }, 300)
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 1rem;
}

.modal-box {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 20px 60px rgba(0,0,0,.2);
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #f1f5f9;
}

.modal-title-group {
  display: flex;
  align-items: center;
  gap: .875rem;
}

.modal-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-green  { background: #dcfce7; color: #16a34a; }
.icon-red    { background: #fee2e2; color: #dc2626; }
.icon-purple { background: #ede9fe; color: #7c3aed; }

.modal-title { font-size: .9375rem; font-weight: 600; color: #111827; margin: 0; }
.modal-subtitle { font-size: .8125rem; color: #6b7280; margin: 0; }

.btn-close {
  background: none;
  border: none;
  cursor: pointer;
  color: #9ca3af;
  display: flex;
  align-items: center;
  padding: .25rem;
  border-radius: 6px;
  transition: background .15s;
}
.btn-close:hover { background: #f3f4f6; color: #374151; }

.modal-body { padding: 1.5rem; display: flex; flex-direction: column; gap: 1rem; }

.form-group { display: flex; flex-direction: column; gap: .375rem; }
.form-label { font-size: .8125rem; font-weight: 500; color: #374151; }
.form-hint  { font-size: .75rem; color: #9ca3af; margin: 0; }

.branch-display {
  display: flex;
  align-items: center;
  gap: .5rem;
  padding: .5625rem .875rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: .875rem;
  color: #374151;
}

.input-prefix-wrap {
  display: flex;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  overflow: hidden;
}

.input-prefix {
  padding: .5625rem .75rem;
  background: #f8fafc;
  color: #6b7280;
  font-size: .875rem;
  border-right: 1px solid #d1d5db;
  white-space: nowrap;
}

.form-input.with-prefix {
  flex: 1;
  border: none;
  outline: none;
  padding: .5625rem .875rem;
  font-size: .875rem;
  color: #111827;
}

.form-textarea {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: .5625rem .875rem;
  font-size: .875rem;
  resize: vertical;
  outline: none;
  color: #111827;
  font-family: inherit;
}
.form-textarea:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,.1); }

.mt-4 { margin-top: .5rem; }

/* Shift summary */
.shift-summary {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: .625rem;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: .875rem;
}

.summary-label { color: #6b7280; }
.summary-value { font-weight: 500; color: #111827; }
.summary-value.omzet { color: #6366f1; font-style: italic; }
.summary-divider { height: 1px; background: #e2e8f0; margin: .25rem 0; }
.summary-row.highlight .summary-label { font-weight: 600; color: #374151; }

/* Result summary setelah close */
.result-summary {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: .625rem;
}

.result-omzet {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 10px;
  padding: .875rem 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: .25rem;
}

.result-omzet-label {
  font-size: .8125rem;
  font-weight: 600;
  color: rgba(255,255,255,.85);
}

.result-omzet-value {
  font-size: 1.25rem;
  font-weight: 800;
  color: #fff;
}

.modal-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: flex-end;
  gap: .75rem;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: .375rem;
  padding: .5625rem 1.25rem;
  border-radius: 8px;
  font-size: .875rem;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: background .15s, opacity .15s;
}
.btn:disabled { opacity: .6; cursor: not-allowed; }

.btn-ghost   { background: transparent; color: #6b7280; border: 1px solid #e5e7eb; }
.btn-ghost:hover:not(:disabled) { background: #f9fafb; }

.btn-success { background: #16a34a; color: white; }
.btn-success:hover:not(:disabled) { background: #15803d; }

.btn-danger  { background: #dc2626; color: white; }
.btn-danger:hover:not(:disabled) { background: #b91c1c; }

.btn-print   { background: #f8fafc; color: #374151; border: 1px solid #e2e8f0; }
.btn-print:hover:not(:disabled) { background: #f1f5f9; }

.spin { animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.modal-enter-active, .modal-leave-active { transition: opacity .2s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
