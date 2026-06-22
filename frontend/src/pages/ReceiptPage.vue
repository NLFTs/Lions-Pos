<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/lib/api'

const route = useRoute()
const orderNumber = route.params.orderNumber

const receipt = ref(null)
const loading = ref(true)
const error = ref(null)

onMounted(async () => {
  try {
    const res = await api.get(`/api/v1/public/receipt/${orderNumber}`)
    receipt.value = res.data?.data
  } catch (err) {
    error.value = err.response?.status === 404
      ? 'Struk tidak ditemukan. Pastikan nomor order sudah benar.'
      : 'Gagal memuat struk. Coba lagi beberapa saat.'
  } finally {
    loading.value = false
  }
})

function fmt(v) {
  if (v == null) return 'Rp 0'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency', currency: 'IDR', minimumFractionDigits: 0,
  }).format(v)
}

function formatDate(val) {
  if (!val) return '-'
  return new Date(val).toLocaleString('id-ID', {
    day: '2-digit', month: 'long', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

const statusLabel = computed(() => {
  const s = (receipt.value?.status || '').toUpperCase()
  if (s === 'PAID') return { text: 'Lunas ✓', cls: 'bg-emerald-100 text-emerald-700' }
  if (s === 'DRAFT') {
    const m = (receipt.value?.paymentMethod || '').toUpperCase()
    if (m === 'TRANSFER') return { text: 'Menunggu Konfirmasi Transfer', cls: 'bg-amber-100 text-amber-700' }
    return { text: 'Menunggu Pembayaran', cls: 'bg-amber-100 text-amber-700' }
  }
  if (s === 'CANCELED') return { text: 'Dibatalkan', cls: 'bg-red-100 text-red-700' }
  if (s === 'RETURN') return { text: 'Diretur', cls: 'bg-violet-100 text-violet-700' }
  return { text: s, cls: 'bg-zinc-100 text-zinc-600' }
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-zinc-50 to-zinc-100 flex items-center justify-center p-4">
    <!-- Loading -->
    <div v-if="loading" class="flex flex-col items-center gap-3 text-zinc-400">
      <div class="w-10 h-10 border-4 border-zinc-200 border-t-primary rounded-full animate-spin"></div>
      <p class="text-sm font-medium">Memuat struk...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="max-w-sm w-full bg-white rounded-3xl shadow-xl p-8 text-center">
      <div class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <svg class="w-8 h-8 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
        </svg>
      </div>
      <h2 class="text-lg font-bold text-zinc-800 mb-2">Struk Tidak Ditemukan</h2>
      <p class="text-sm text-zinc-500">{{ error }}</p>
    </div>

    <!-- Receipt -->
    <div v-else-if="receipt" class="max-w-sm w-full">
      <!-- Card struk -->
      <div class="bg-white rounded-3xl shadow-xl overflow-hidden">

        <!-- Header toko -->
        <div class="bg-gradient-to-br from-zinc-900 to-zinc-700 px-6 pt-8 pb-6 text-white text-center">
          <div class="w-12 h-12 bg-white/10 rounded-2xl flex items-center justify-center mx-auto mb-3">
            <svg class="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
          </div>
          <h1 class="text-xl font-black tracking-wide">{{ receipt.branchName || 'KASIR' }}</h1>
          <p class="text-xs text-white/60 mt-1">Bukti Transaksi Digital</p>
        </div>

        <!-- Status badge -->
        <div class="flex justify-center -mt-3 mb-4 px-6">
          <span :class="['px-4 py-1.5 rounded-full text-xs font-bold shadow-sm', statusLabel.cls]">
            {{ statusLabel.text }}
          </span>
        </div>

        <!-- Info order -->
        <div class="px-6 pb-4 space-y-1.5 text-sm">
          <div class="flex justify-between">
            <span class="text-zinc-400">No. Order</span>
            <span class="font-mono font-bold text-zinc-800">{{ receipt.orderNumber }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-zinc-400">Tanggal</span>
            <span class="font-medium text-zinc-700">{{ formatDate(receipt.createdAt) }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-zinc-400">Kasir</span>
            <span class="font-medium text-zinc-700">{{ receipt.cashierName || '-' }}</span>
          </div>
          <div v-if="receipt.buyerName" class="flex justify-between">
            <span class="text-zinc-400">Pembeli</span>
            <span class="font-medium text-zinc-700">{{ receipt.buyerName }}</span>
          </div>
        </div>

        <!-- Divider dashed -->
        <div class="mx-6 border-t border-dashed border-zinc-200 relative">
          <div class="absolute -left-3 -top-3 w-6 h-6 bg-zinc-100 rounded-full"></div>
          <div class="absolute -right-3 -top-3 w-6 h-6 bg-zinc-100 rounded-full"></div>
        </div>

        <!-- Items -->
        <div class="px-6 py-4 space-y-3">
          <p class="text-[10px] font-bold text-zinc-400 uppercase tracking-wider">Item Pesanan</p>
          <div v-for="item in receipt.items" :key="item.productName" class="flex items-start justify-between gap-3">
            <div class="flex-1 min-w-0">
              <p class="text-sm font-bold text-zinc-800 leading-snug">{{ item.productName }}</p>
              <p v-if="item.itemNote" class="text-xs text-zinc-400 italic mt-0.5">{{ item.itemNote }}</p>
              <p class="text-xs text-zinc-400 mt-0.5">{{ item.qty }} × {{ fmt(item.unitPrice) }}</p>
            </div>
            <span class="text-sm font-black text-zinc-800 shrink-0">{{ fmt(item.subtotal) }}</span>
          </div>
        </div>

        <!-- Divider dashed -->
        <div class="mx-6 border-t border-dashed border-zinc-200 relative">
          <div class="absolute -left-3 -top-3 w-6 h-6 bg-zinc-100 rounded-full"></div>
          <div class="absolute -right-3 -top-3 w-6 h-6 bg-zinc-100 rounded-full"></div>
        </div>

        <!-- Totals -->
        <div class="px-6 py-4 space-y-2">
          <div class="flex justify-between text-sm text-zinc-500">
            <span>Subtotal</span>
            <span>{{ fmt(receipt.subtotal) }}</span>
          </div>
          <div v-if="receipt.discountAmount > 0" class="flex justify-between text-sm text-red-500">
            <span>Diskon</span>
            <span>-{{ fmt(receipt.discountAmount) }}</span>
          </div>
          <div class="flex justify-between text-xl font-black text-zinc-900 pt-2 border-t border-zinc-100">
            <span>Total</span>
            <span>{{ fmt(receipt.total) }}</span>
          </div>
        </div>

        <!-- Payment method -->
        <div class="mx-6 mb-6 bg-zinc-50 rounded-2xl px-4 py-3">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span class="text-lg">{{ receipt.paymentMethod === 'CASH' ? '💵' : receipt.paymentMethod === 'TRANSFER' ? '🏦' : '💳' }}</span>
              <div>
                <p class="text-xs font-bold text-zinc-700">{{ receipt.paymentMethod === 'CASH' ? 'Tunai' : receipt.paymentMethod === 'TRANSFER' ? 'Transfer Bank' : receipt.paymentMethod }}</p>
                <p class="text-[10px] text-zinc-400">Metode Pembayaran</p>
              </div>
            </div>
            <span :class="['text-[10px] font-bold px-2 py-1 rounded-full', receipt.paymentStatus === 'VERIFIED' ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700']">
              {{ receipt.paymentStatus === 'VERIFIED' ? 'Terverifikasi' : 'Pending' }}
            </span>
          </div>
        </div>

        <!-- Footer -->
        <div class="border-t border-dashed border-zinc-100 px-6 py-5 text-center">
          <p class="text-xs text-zinc-400">Terima kasih sudah berbelanja 🙏</p>
          <p class="text-[10px] text-zinc-300 mt-1 font-mono">{{ receipt.orderNumber }}</p>
        </div>
      </div>

      <!-- Powered by note -->
      <p class="text-center text-[10px] text-zinc-400 mt-4">
        Struk digital diverifikasi oleh sistem Gaptek
      </p>
    </div>
  </div>
</template>
