<script setup>
import { computed, watch } from 'vue'

const props = defineProps({
  order: Object,
  branchName: String,
  storeName: { type: String, default: 'GAPTEK' },
  // Jika true, langsung print saat order berubah (dipanggil dari KasirPage setelah checkout)
  autoPrint: { type: Boolean, default: false },
})

const emit = defineEmits(['printed'])

// QR code: encode URL atau order number sebagai data URI via Google Charts API
// Tidak perlu library eksternal — cukup pakai URL API publik untuk generate QR
function qrUrl(text) {
  if (!text) return ''
  const encoded = encodeURIComponent(text)
  return `https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=${encoded}`
}

const qrData = computed(() => {
  if (!props.order) return ''
  // URL publik — bisa diakses pelanggan tanpa login
  const base = typeof window !== 'undefined' ? window.location.origin : ''
  return `${base}/receipt/${props.order.orderNumber}`
})

function fmt(v) {
  if (v == null) return 'Rp 0'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency', currency: 'IDR', minimumFractionDigits: 0,
  }).format(v)
}

function formatDate(val) {
  if (!val) return new Date().toLocaleString('id-ID', { day:'2-digit', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' })
  return new Date(val).toLocaleString('id-ID', { day:'2-digit', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' })
}

function doPrint() {
  if (!props.order) return
  const o = props.order

  const isSplit = o.payments?.length > 1
  const cashPay  = o.payments?.find(p => p.method === 'CASH')
  const xferPay  = o.payments?.find(p => p.method === 'TRANSFER')

  const itemsHtml = (o.items || []).map(i => `
    <tr>
      <td class="item-name" style="max-width:140px;word-break:break-word;padding:2px 0">
        ${i.productName || 'Produk'}
        ${i.itemNote ? `<br><span style="font-size:9px;font-style:italic;color:#555">${i.itemNote}</span>` : ''}
      </td>
      <td style="text-align:center;padding:2px 4px;white-space:nowrap">${i.qty}</td>
      <td style="text-align:right;white-space:nowrap;padding:2px 0">${fmt(i.subtotal)}</td>
    </tr>`).join('')

  let paymentHtml = ''
  if (isSplit) {
    paymentHtml = `
      <tr><td colspan="2" style="padding-top:4px;font-weight:bold;font-size:10px;text-transform:uppercase;letter-spacing:.5px">Split Payment</td></tr>
      ${cashPay  ? `<tr><td>💵 Tunai</td><td style="text-align:right">${fmt(cashPay.amount)}</td></tr>` : ''}
      ${xferPay  ? `<tr><td>🏦 Transfer</td><td style="text-align:right">${fmt(xferPay.amount)}</td></tr>` : ''}
      ${cashPay?.changeDue > 0 ? `<tr><td style="font-weight:bold">Kembalian</td><td style="text-align:right;font-weight:bold">${fmt(cashPay.changeDue)}</td></tr>` : ''}`
  } else if (cashPay) {
    paymentHtml = `
      <tr><td>Metode</td><td style="text-align:right">💵 Tunai</td></tr>
      <tr><td>Uang Diterima</td><td style="text-align:right">${fmt(cashPay.cashTendered)}</td></tr>
      <tr><td style="font-weight:bold">Kembalian</td><td style="text-align:right;font-weight:bold">${fmt(cashPay.changeDue)}</td></tr>`
  } else if (xferPay) {
    paymentHtml = `
      <tr><td>Metode</td><td style="text-align:right">🏦 Transfer</td></tr>
      ${xferPay.bankName ? `<tr><td>Bank</td><td style="text-align:right">${xferPay.bankName}</td></tr>` : ''}
      ${xferPay.referenceNo ? `<tr><td>Ref.</td><td style="text-align:right;font-family:monospace">${xferPay.referenceNo}</td></tr>` : ''}
      <tr><td colspan="2" style="text-align:center;color:#d97706;font-weight:bold;font-size:10px;padding-top:2px">⏳ Menunggu Konfirmasi</td></tr>`
  }

  const html = `<!DOCTYPE html><html><head>
    <meta charset="utf-8"/>
    <title>Struk ${o.orderNumber}</title>
    <style>
      @page { size: 80mm auto; margin: 3mm 4mm; }
      * { box-sizing: border-box; margin: 0; padding: 0; }
      body { font-family: 'Courier New', monospace; font-size: 11px; color: #000; width: 72mm; }
      .center { text-align: center; }
      .bold { font-weight: bold; }
      .lg { font-size: 15px; }
      .divider { border-top: 1px dashed #000; margin: 5px 0; display:block; }
      table { width: 100%; border-collapse: collapse; }
      th { text-align: left; border-bottom: 1px solid #000; padding-bottom: 3px; font-size:10px; }
      th:last-child { text-align:right; }
      .total-row td { font-weight: bold; font-size: 14px; padding-top: 5px; border-top: 1px dashed #000; }
      .qr-section { text-align:center; margin-top: 8px; }
      .qr-section img { width: 72px; height: 72px; }
      .qr-section p { font-size: 8px; color: #666; margin-top: 2px; }
    </style>
  </head><body>
    <p class="center bold lg">${props.storeName}</p>
    <p class="center" style="font-size:11px">${o.branchName || props.branchName || ''}</p>
    <div class="divider"></div>
    <table style="font-size:10px">
      <tr><td>No. Order</td><td style="text-align:right;font-weight:bold">${o.orderNumber}</td></tr>
      <tr><td>Tanggal</td><td style="text-align:right">${formatDate(o.createdAt)}</td></tr>
      <tr><td>Kasir</td><td style="text-align:right">${o.cashierName || '-'}</td></tr>
      ${o.buyerName ? `<tr><td>Pembeli</td><td style="text-align:right">${o.buyerName}</td></tr>` : ''}
    </table>
    <div class="divider"></div>
    <table>
      <thead><tr>
        <th>Item</th><th style="text-align:center">Qty</th><th style="text-align:right">Harga</th>
      </tr></thead>
      <tbody>${itemsHtml}</tbody>
    </table>
    <div class="divider"></div>
    <table style="font-size:11px">
      <tr><td>Subtotal</td><td style="text-align:right">${fmt(o.subtotal)}</td></tr>
      ${o.discountAmount > 0 ? `<tr><td>Diskon</td><td style="text-align:right;color:#dc2626">-${fmt(o.discountAmount)}</td></tr>` : ''}
    </table>
    <table>
      <tr class="total-row"><td>TOTAL</td><td style="text-align:right">${fmt(o.total)}</td></tr>
    </table>
    <div class="divider"></div>
    <table style="font-size:11px">${paymentHtml}</table>
    <div class="divider"></div>
    <div class="qr-section">
      <img src="${qrUrl(qrData.value)}" alt="QR" />
      <p>Scan untuk cek status pesanan</p>
      <p style="font-size:9px;font-weight:bold;letter-spacing:1px;margin-top:4px">${o.orderNumber}</p>
    </div>
    <div class="divider"></div>
    <p class="center" style="font-size:10px;margin-top:4px">Terima kasih atas kunjungan Anda 🙏</p>
  </body></html>`

  const win = window.open('', '_blank', 'width=340,height=650')
  if (!win) return
  win.document.write(html)
  win.document.close()
  win.focus()
  setTimeout(() => {
    win.print()
    win.close()
    emit('printed')
  }, 400)
}

// Expose doPrint agar bisa dipanggil dari parent via ref
defineExpose({ doPrint })

watch(() => props.order, (o) => {
  if (props.autoPrint && o) doPrint()
})
</script>

<template>
  <!-- Komponen ini headless — tidak render UI, hanya logic cetak -->
  <!-- Panggil doPrint() via ref, atau gunakan prop autoPrint=true -->
  <span style="display:none" />
</template>
