<script setup>
import { ref, computed, onMounted } from 'vue'
import { useToast } from '@/composables/useToast'
import api from '@/lib/api'
import { 
  Warehouse, 
  Building2, 
  ArrowUpDown, 
  Plus, 
  Loader2, 
  X, 
  Shield 
} from 'lucide-vue-next'
import Button from '@/components/ui/button/Button.vue'
import Label from '@/components/ui/Label.vue'
import Input from '@/components/ui/Input.vue'

const { toast } = useToast()

// ─── STATE MASTER DATA ──────────────────────────────────────────────────────
const warehouses = ref([])
const branches = ref([])
const products = ref([])
const loadingData = ref(false)
const sending = ref(false)
const showError = ref(null)

// Drawer Toggle Control
const showDrawer = ref(true) // Set true untuk testing, sesuaikan dengan parent trigger lu

// ─── FORM STATE (Sesuai DTO StockTransferRequest Backend Lu) ───────────────
const form = ref({
  fromLocationType: 'WAREHOUSE', // 'WAREHOUSE' | 'BRANCH'
  fromLocationId: '',
  toLocationType: 'BRANCH',      // 'WAREHOUSE' | 'BRANCH'
  toLocationId: '',
  productId: '',
  qty: 1,
  notes: '' // Opsional buat pelengkap UI
})

// ─── COMPUTED DATA FILTERING ────────────────────────────────────────────────
// List Lokasi Asal Berdasarkan Pilihan Toggle
const fromLocationsList = computed(() => {
  return form.value.fromLocationType === 'WAREHOUSE' ? warehouses.value : branches.value
})

// List Lokasi Tujuan Berdasarkan Pilihan Toggle
const toLocationsList = computed(() => {
  return form.value.toLocationType === 'WAREHOUSE' ? warehouses.value : branches.value
})

// ─── ACTIONS / FETCH DATA ───────────────────────────────────────────────────
async function fetchMasterData() {
  loadingData.value = true
  try {
    // Ambil data gudang, cabang, dan produk secara parallel
    const [resW, resB, resP] = await Promise.all([
      api.get('/api/v1/warehouses'),
      api.get('/api/v1/branches'),
      api.get('/api/v1/products') // Sesuaikan endpoint master produk lu
    ])
    
    warehouses.value = resW.data?.data || []
    branches.value = resB.data?.data || []
    products.value = resP.data?.data || []
  } catch (err) {
    toast.error('Gagal memuat master data lokasi & produk.')
  } finally {
    loadingData.value = false
  }
}

// Fungsi Swap Rute (Tombol lingkaran di tengah rute)
function swapRoute() {
  const tempType = form.value.fromLocationType
  const tempId = form.value.fromLocationId
  
  form.value.fromLocationType = form.value.toLocationType
  form.value.fromLocationId = form.value.toLocationId
  
  form.value.toLocationType = tempType
  form.value.toLocationId = tempId
}

// Kirim Permintaan Transfer ke Backend
async function submitTransfer() {
  // Validasi Awal di Frontend
  if (!form.value.fromLocationId || !form.value.toLocationId) {
    showError.value = 'Lokasi asal dan tujuan wajib dipilih!'
    return
  }
  if (!form.value.productId) {
    showError.value = 'Produk wajib dipilih!'
    return
  }
  if (form.value.fromLocationType === form.value.toLocationType && form.value.fromLocationId === form.value.toLocationId) {
    showError.value = 'Lokasi asal dan tujuan tidak boleh sama persis!'
    return
  }
  if (form.value.qty <= 0) {
    showError.value = 'Jumlah transfer minimal adalah 1!'
    return
  }

  sending.value = true
  showError.value = null
  
  try {
    // Hit POST controller lu: @PostMapping("/transfer")
    await api.post('/api/v1/stock-balances/transfer', {
      productId: Long(form.value.productId),
      qty: Long(form.value.qty),
      fromLocationType: form.value.fromLocationType,
      fromLocationId: Long(form.value.fromLocationId),
      toLocationType: form.value.toLocationType,
      toLocationId: Long(form.value.toLocationId)
    })

    toast.success('Transfer stok sukses dieksekusi dan dicatat!')
    showDrawer.value = false
    // Trigger reset form atau emmit refresh ke table list di sini
  } catch (err) {
    showError.value = err.response?.data?.message || 'Gagal melakukan transfer stok.'
    toast.error(showError.value)
  } finally {
    sending.value = false
  }
}

// Helper konversi tipe data ke number primitif
const Long = (val) => parseInt(val, 10)

onMounted(fetchMasterData)
</script>

<template>
  <div v-if="showDrawer" class="fixed inset-y-0 right-0 z-[50] flex flex-col w-full sm:max-w-[460px] bg-card shadow-2xl border-l overflow-hidden border-zinc-200 dark:border-zinc-800">
    
    <div class="flex items-center justify-between px-6 py-4 border-b shrink-0 border-zinc-100 dark:border-zinc-800">
      <div>
        <h3 class="font-bold text-base text-zinc-900 dark:text-zinc-50">Buat Transfer Stok</h3>
        <p class="text-xs text-muted-foreground mt-0.5">Atur mutasi barang antar lokasi bisnis.</p>
      </div>
      <Button variant="ghost" size="icon" @click="showDrawer = false" class="h-8 w-8 text-zinc-400">
        <X class="h-4 w-4" />
      </Button>
    </div>

    <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5">
      
      <div v-if="showError" class="p-3 bg-destructive/10 border border-destructive/20 text-destructive text-xs font-medium rounded-lg">
        {{ showError }}
      </div>

      <div class="border rounded-xl p-4 bg-zinc-50/50 dark:bg-zinc-900/20 space-y-4 border-zinc-200 dark:border-zinc-800 relative">
        <span class="absolute -top-2.5 left-3 px-1.5 bg-background text-[10px] font-bold tracking-wider text-zinc-400 uppercase">Rute Mutasi</span>
        
        <div class="space-y-2">
          <div class="flex items-center justify-between">
            <Label class="text-xs font-semibold text-zinc-700 dark:text-zinc-300">Dari (Asal) <span class="text-destructive">*</span></Label>
          </div>
          
          <div class="grid grid-cols-2 gap-1.5 p-1 bg-zinc-100 dark:bg-zinc-800/60 rounded-lg">
            <button 
              type="button" 
              @click="form.fromLocationType = 'WAREHOUSE'; form.fromLocationId = ''"
              :class="['flex items-center justify-center gap-2 h-8 rounded-md text-xs font-medium transition-all', form.fromLocationType === 'WAREHOUSE' ? 'bg-background dark:bg-zinc-900 text-zinc-900 dark:text-zinc-50 shadow-sm border border-zinc-200/50 dark:border-zinc-700' : 'text-zinc-500 hover:text-zinc-900']"
            >
              <Warehouse class="h-3.5 w-3.5" /> Gudang
            </button>
            <button 
              type="button" 
              @click="form.fromLocationType = 'BRANCH'; form.fromLocationId = ''"
              :class="['flex items-center justify-center gap-2 h-8 rounded-md text-xs font-medium transition-all', form.fromLocationType === 'BRANCH' ? 'bg-background dark:bg-zinc-900 text-zinc-900 dark:text-zinc-50 shadow-sm border border-zinc-200/50 dark:border-zinc-700' : 'text-zinc-500 hover:text-zinc-900']"
            >
              <Building2 class="h-3.5 w-3.5" /> Cabang
            </button>
          </div>

          <select v-model="form.fromLocationId" class="w-full h-10 px-3 text-sm rounded-md border border-input bg-background outline-none focus:ring-2 focus:ring-primary transition-all">
            <option value="" disabled>Pilih lokasi asal...</option>
            <option v-for="loc in fromLocationsList" :key="loc.id" :value="loc.id">
              {{ loc.name }}
            </option>
          </select>
        </div>

        <div class="flex justify-center -my-2 relative z-10">
          <button 
            type="button" 
            @click="swapRoute" 
            class="p-2 rounded-full border bg-background hover:bg-zinc-50 border-zinc-200 dark:border-zinc-700 dark:hover:bg-zinc-800 text-zinc-500 transition-all shadow-sm"
            title="Tukar Rute Asal/Tujuan"
          >
            <ArrowUpDown class="h-3.5 w-3.5" />
          </button>
        </div>

        <div class="space-y-2">
          <Label class="text-xs font-semibold text-zinc-700 dark:text-zinc-300">Ke (Tujuan) <span class="text-destructive">*</span></Label>
          
          <div class="grid grid-cols-2 gap-1.5 p-1 bg-zinc-100 dark:bg-zinc-800/60 rounded-lg">
            <button 
              type="button" 
              @click="form.toLocationType = 'WAREHOUSE'; form.toLocationId = ''"
              :class="['flex items-center justify-center gap-2 h-8 rounded-md text-xs font-medium transition-all', form.toLocationType === 'WAREHOUSE' ? 'bg-background dark:bg-zinc-900 text-zinc-900 dark:text-zinc-50 shadow-sm border border-zinc-200/50 dark:border-zinc-700' : 'text-zinc-500 hover:text-zinc-900']"
            >
              <Warehouse class="h-3.5 w-3.5" /> Gudang
            </button>
            <button 
              type="button" 
              @click="form.toLocationType = 'BRANCH'; form.toLocationId = ''"
              :class="['flex items-center justify-center gap-2 h-8 rounded-md text-xs font-medium transition-all', form.toLocationType === 'BRANCH' ? 'bg-background dark:bg-zinc-900 text-zinc-900 dark:text-zinc-50 shadow-sm border border-zinc-200/50 dark:border-zinc-700' : 'text-zinc-500 hover:text-zinc-900']"
            >
              <Building2 class="h-3.5 w-3.5" /> Cabang
            </button>
          </div>

          <select v-model="form.toLocationId" class="w-full h-10 px-3 text-sm rounded-md border border-input bg-background outline-none focus:ring-2 focus:ring-primary transition-all">
            <option value="" disabled>Pilih lokasi tujuan...</option>
            <option v-for="loc in toLocationsList" :key="loc.id" :value="loc.id">
              {{ loc.name }}
            </option>
          </select>
        </div>

      </div>

      <div class="space-y-3 relative pt-3">
        <div class="flex items-center justify-between">
          <span class="text-xs font-bold uppercase tracking-wider text-zinc-500 flex items-center gap-1.5">
            <Shield class="h-3.5 w-3.5 text-zinc-400" /> Item Transfer
          </span>
          <button type="button" class="text-xs font-medium text-zinc-500 hover:text-zinc-900 flex items-center gap-1 opacity-60 cursor-not-allowed border px-2 py-1 rounded bg-zinc-50" title="Backend DTO lu saat ini baru menampung transfer single item" disabled>
            <Plus class="h-3 w-3" /> Tambah Item
          </button>
        </div>

        <div class="grid grid-cols-4 gap-3">
          <div class="col-span-3 space-y-1.5">
            <Label class="text-xs text-muted-foreground">Produk</Label>
            <select v-model="form.productId" class="w-full h-10 px-3 text-sm rounded-md border border-input bg-background outline-none focus:ring-2 focus:ring-primary">
              <option value="" disabled>Pilih produk...</option>
              <option v-for="product in products" :key="product.id" :value="product.id">
                {{ product.name }}
              </option>
            </select>
          </div>

          <div class="col-span-1 space-y-1.5">
            <Label class="text-xs text-muted-foreground">Qty</Label>
            <Input type="number" min="1" v-model="form.qty" class="text-center h-10" />
          </div>
        </div>
      </div>

      <div class="space-y-1.5">
        <Label class="text-xs font-semibold text-zinc-700 dark:text-zinc-300">Catatan</Label>
        <textarea 
          v-model="form.notes" 
          rows="3" 
          class="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary border-zinc-200 dark:border-zinc-800"
          placeholder="Alasan transfer atau instruksi..."
        />
      </div>

    </div>

    <div class="flex justify-end gap-3 px-6 py-4 border-t bg-zinc-50/50 dark:bg-zinc-900/30 border-zinc-100 dark:border-zinc-800 shrink-0">
      <Button variant="outline" @click="showDrawer = false" :disabled="sending">Batal</Button>
      <Button @click="submitTransfer" :disabled="sending || loadingData" class="bg-zinc-900 hover:bg-zinc-800 text-white dark:bg-zinc-50 dark:text-zinc-900 font-medium">
        <Loader2 v-if="sending" class="h-4 w-4 mr-2 animate-spin" />
        Kirim Permintaan
      </Button>
    </div>

  </div>
</template>