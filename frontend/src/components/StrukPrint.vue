<template>
  <Teleport to="body">
    <div id="struk-portal">
      <div class="struk">
        <div class="struk-header">
          <h2>{{ storeName }}</h2>
          <p>{{ branchName }}</p>
          <p>{{ date }}</p>
          <div class="divider">--------------------------------</div>
        </div>

        <div class="struk-items">
          <div v-for="item in items" :key="item.id" class="struk-item">
            <span class="item-name">{{ item.productName }}</span>
            <div class="item-detail">
              <span>{{ item.qty }} x {{ formatCurrency(item.unitPrice) }}</span>
              <span>{{ formatCurrency(item.subtotal) }}</span>
            </div>
          </div>
        </div>

        <div class="divider">--------------------------------</div>

        <div class="struk-totals">
          <div class="struk-row">
            <span>Subtotal</span>
            <span>{{ formatCurrency(order.subtotal) }}</span>
          </div>
          <div v-if="order.discountAmount > 0" class="struk-row">
            <span>Diskon</span>
            <span>-{{ formatCurrency(order.discountAmount) }}</span>
          </div>
          <div class="struk-row total">
            <span>TOTAL</span>
            <span>{{ formatCurrency(order.total) }}</span>
          </div>
          <div class="struk-row">
            <span>Metode</span>
            <span>{{ order.payments?.[0]?.method === 'CASH' ? 'Tunai' : 'Transfer' }}</span>
          </div>
          <template v-if="order.payments?.[0]?.method === 'CASH'">
            <div class="struk-row">
              <span>Tunai</span>
              <span>{{ formatCurrency(order.payments[0].cashTendered) }}</span>
            </div>
            <div class="struk-row">
              <span>Kembalian</span>
              <span>{{ formatCurrency(order.payments[0].changeDue) }}</span>
            </div>
          </template>
        </div>

        <div class="divider">--------------------------------</div>

        <div class="struk-footer">
          <p>Kasir: {{ order.cashierName || '-' }}</p>
          <p>{{ order.orderNumber }}</p>
          <p>Terima kasih!</p>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps(['order', 'branchName', 'storeName'])

const date = new Date().toLocaleString('id-ID', {
  day: '2-digit', month: 'short', year: 'numeric',
  hour: '2-digit', minute: '2-digit'
})

const items = computed(() => props.order?.items || [])

function formatCurrency(val) {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency', currency: 'IDR', minimumFractionDigits: 0
  }).format(val)
}
</script>

<style>
#struk-portal { display: none; }

@media print {
  * { margin: 0; padding: 0; box-sizing: border-box; }

  body > *:not(#struk-portal) { display: none !important; }
  #struk-portal { display: block !important; }

  @page { size: A4; margin: 10mm; }

  #struk-portal {
    width: 100%;
    padding: 0;
  }

  .struk {
    width: 100%;
    font-family: 'Courier New', monospace;
    font-size: 16px;
    line-height: 1.8;
    color: #000;
  }

  .struk-header { text-align: center; margin-bottom: 10px; }
  .struk-header h2 { font-size: 24px; font-weight: 900; letter-spacing: 2px; text-transform: uppercase; margin-bottom: 4px; }
  .struk-header p { font-size: 14px; margin: 2px 0; }

  .divider { 
  border: none; 
  border-top: 1px dashed #000; 
  margin: 8px 0; 
  width: 100%;
  display: block;
}

  .struk-items { margin: 6px 0; }
  .struk-item { margin-bottom: 8px; }
  .item-name { display: block; font-weight: bold; font-size: 16px; }
  .item-detail { display: flex; justify-content: space-between; font-size: 14px; padding-left: 8px; color: #333; }

  .struk-totals { margin: 6px 0; }
  .struk-row { display: flex; justify-content: space-between; font-size: 16px; margin-bottom: 4px; }
  .struk-row.total { font-weight: 900; font-size: 20px; border-top: 1px dashed #000; border-bottom: 1px dashed #000; padding: 6px 0; margin: 8px 0; }

  .struk-footer { text-align: center; margin-top: 12px; font-size: 14px; line-height: 2; }
  .struk-footer p:last-child { font-size: 18px; font-weight: bold; margin-top: 6px; }
}
</style>